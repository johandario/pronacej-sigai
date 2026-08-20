import { CdkColumnDef } from '@angular/cdk/table';
import { CommonModule, Location } from '@angular/common';
import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import etiquetasModel from 'app/core/etiquetas.model';
import { ContestacionDTO } from 'app/core/model/both/encuesta/contestacionDTO.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { PreguntaDTO } from 'app/core/model/both/encuesta/preguntaDTO.model';
import { RespuestaDTO } from 'app/core/model/both/encuesta/respuestaDTO.model';
import { SeccionDTO } from 'app/core/model/both/encuesta/seccionDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TabService } from 'app/core/services/tab.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import {
  esEncuestaFactoresRiesgo,
  calcularResumenSavryDesdeEncuesta,
  calcularResumenSavryDesdeFormulario,
  SavryGrupoResumen,
} from 'app/core/utils/savryResumen.utils';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';

@Component({
  selector: 'app-evaluacion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatLabel,
    MatIconModule,
    MatTableModule,
    CdkColumnDef,
    MatCheckboxModule,
    MatDatepickerModule,
    MatTooltipModule,
    MatRadioModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatSelectModule
  ],
  templateUrl: './evaluacion.component.html',
  styleUrl: './evaluacion.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ]
})
export class EvaluacionComponent implements OnInit {

  @Input() tokenEncuesta: string;
  @Input() tokenEncabezado: string;
  @Input() completada: boolean;
  @Input() uuid_fp: string;
  @Input() tipoEvaluacion: 'conductual' | 'psicologica' | 'prueba' | 'riesgo' = 'conductual';

  listaPrev: boolean = false;

  evaluacion: EncuestaDTO;

  nemonicoMenuEvaluacionConductual = etiquetasModel.NEMONICO_MENU_EVALUACION_CONDUCTUAL;
  nemonicoMenuEvaluacionPsicologica = etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOLOGICA;
  nemonicoMenuPruebasPsicologicas = etiquetasModel.NEMONICO_MENU_PRUEBAS_PSICOLOGICAS;
  nemonicoMenuNivelRiesgo = etiquetasModel.NEMONICO_MENU_NIVEL_RIESGO;

  nemonicoMenuActual: string;

  esVisualizacion: boolean = false;
  esEdicion: boolean = false;

  evaluacionForm: FormGroup;
  valoracionForm: FormGroup;
  listaNivelesRiesgo: CatalogoDTO[] = [];
  mostrarPanelValoracion: boolean = false;
  modoRevalorar: boolean = false;
  resumenSavryGrupos: SavryGrupoResumen[] = [];

  mostrarResumen: boolean = false;
  dataSource: any[] = [];
  displayedColumns: string[] = ['seccion', 'totalRespuestas'];
  opcionesUnicas: string[] = [];
  totalGeneral: any = {
    totalRespuestas: 0,
    puntuacion: 0,
  };
  valorRespuestas: any[] = [];

  respuestasLargas: PreguntaDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private encuestaService: EncuestaService,
    private dialogMensajeService: DialogMensajeService,
    private tabService: TabService,
    private funcionesUtils: FuncionesUtils,
  ) { }

  ngOnInit() {
    this.listaPrev = history.state.listaPrev;
    this.evaluacionForm = this.fb.group({});
    this.valoracionForm = this.fb.group({
      tokenIdentificadorValoracionFinal: ['0', [Validators.required, Validators.pattern(/^(?!0$).*$/)]],
      justificacionValoracion: ['', [Validators.required]],
    });

    const state = history.state;

    if (!this.tokenEncuesta)
      if (state && state.tokenEncuesta)
        this.tokenEncuesta = state.tokenEncuesta;

    if (!this.tokenEncabezado)
      if (state && state.tokenEncabezado)
        this.tokenEncabezado = state.tokenEncabezado;

    if (!this.completada) {
      if (state && state.completada)
        this.completada = state.completada;
    }

    if (!this.uuid_fp)
      if (state && state.uuid_fp)
        this.uuid_fp = state.uuid_fp;

    if (state && state.tipoEvaluacion) {
      this.tipoEvaluacion = state.tipoEvaluacion;
    }

    this.determinarNemonicoMenu();

    if (this.tokenEncabezado) {
      if (this.completada)
        this.esVisualizacion = true;
      else
        this.esEdicion = true;
    }

    if (this.esRiesgo) {
      this.cargarNivelesRiesgo();
    }

    if (this.esVisualizacion || this.esEdicion)
      this.obtenerEvaluacion();
    else
      this.obtenerEncuesta();
  }

  get esRiesgo(): boolean {
    return this.tipoEvaluacion === 'riesgo';
  }

  /** Solo encuesta SAVRY (FACTORES_DE_RIESGO), no HCR-20 / DASH / ERASOR. */
  get esSavry(): boolean {
    return this.esRiesgo && esEncuestaFactoresRiesgo(this.evaluacion);
  }

  ngOnChanges(changes: SimpleChanges) {
    if ((changes['tokenEncuesta'] && changes['tokenEncuesta'].currentValue) ||
      (changes['tokenEncabezado'] && changes['tokenEncabezado'].currentValue) ||
      (changes['completada'] && changes['completada'].currentValue) ||
      (changes['tipoEvaluacion'] && changes['tipoEvaluacion'].currentValue)) {

      if (changes['tipoEvaluacion']) {
        this.determinarNemonicoMenu();
        if (this.esRiesgo) {
          this.cargarNivelesRiesgo();
        }
      }

      if (this.tokenEncabezado) {
        if (this.completada)
          this.esVisualizacion = true;
        else
          this.esEdicion = true;
      }

      if (this.esVisualizacion || this.esEdicion)
        this.obtenerEvaluacion();
      else
        this.obtenerEncuesta();
    }

    if (changes['uuid_fp'] && changes['uuid_fp'].currentValue)
      this.uuid_fp = changes['uuid_fp'].currentValue;
  }

  private determinarNemonicoMenu(): void {
    switch (this.tipoEvaluacion) {
      case 'psicologica':
        this.nemonicoMenuActual = this.nemonicoMenuEvaluacionPsicologica;
        break;
      case 'prueba':
        this.nemonicoMenuActual = this.nemonicoMenuPruebasPsicologicas;
        break;
      case 'riesgo':
        this.nemonicoMenuActual = this.nemonicoMenuNivelRiesgo;
        break;
      case 'conductual':
      default:
        this.nemonicoMenuActual = this.nemonicoMenuEvaluacionConductual;
        break;
    }
  }

  private cargarNivelesRiesgo(): void {
    this.funcionesUtils.obtenerListaCatalogo('NIVEL_RIESGO', this.nemonicoMenuNivelRiesgo).subscribe({
      next: (data) => {
        this.listaNivelesRiesgo = data || [];
      },
      error: () => {
        this.listaNivelesRiesgo = [];
      }
    });
  }

  private cargarValoracionEnFormulario(): void {
    if (!this.evaluacion) {
      return;
    }
    this.valoracionForm.patchValue({
      tokenIdentificadorValoracionFinal: this.evaluacion.tokenIdentificadorValoracionFinal || '0',
      justificacionValoracion: this.evaluacion.justificacionValoracion || '',
    });
  }

  private obtenerTextoTipoEvaluacion(): string {
    switch (this.tipoEvaluacion) {
      case 'psicologica':
        return 'psicológica';
      case 'prueba':
        return 'prueba psicológica';
      case 'riesgo':
        return 'de nivel de riesgo';
      case 'conductual':
      default:
        return 'conductual';
    }
  }

  buildSections() {
    return this.evaluacion.secciones.map((seccion: any) =>
      this.fb.group({
        preguntas: this.fb.array(this.buildQuestions(seccion.preguntas)),
      })
    );
  }

  buildQuestions(preguntas: PreguntaDTO[]) {
    return preguntas.map((pregunta: PreguntaDTO) => {
      const controls: any = {
        idPregunta: pregunta.idPregunta,
        respuesta: pregunta.requerido
          ? [pregunta.contestaciones?.[0]?.contestacion || null, Validators.required]
          : [pregunta.contestaciones?.[0]?.contestacion || null],
        critico: [Boolean(pregunta.contestaciones?.[0]?.critico)],
      };

      if (pregunta.categoria === "PREG_SEL_UNICA") {
        let temp = pregunta.respuestas.find(r => r.idRespuesta === pregunta.contestaciones?.[0]?.idRespuesta) || null;

        controls["respuesta"] = pregunta.requerido
          ? [temp || null, Validators.required]
          : [temp || null]
      }

      if (pregunta.categoria === "PREG_OP_MULTIPLE") {
        controls["respuestas"] = this.fb.array(
          (pregunta.contestaciones || []).map((c) =>
            this.fb.group({
              id: c.idRespuesta,
              texto: c.contestacion,
            })
          )
        );
      }

      if (pregunta.tieneObservaciones) {
        controls["observaciones"] = [
          pregunta.contestaciones?.[0]?.observacion || null,
        ];
      }

      if (pregunta.permiteDocumentos) {
        controls["documentos"] = [[]];
      }

      return this.fb.group(controls);
    });
  }

  guardarEvaluacion() {
    if (this.esSavry) {
      this.modoRevalorar = false;
      this.recalcularResumenSavry(true);
      this.mostrarPanelValoracion = true;
      return;
    }
    this.enviarEvaluacionCompleta();
  }

  /**
   * Recalcula la tabla resumen SAVRY (Bajo/Medio/Alto/Presente/Ausente/Críticos).
   * @param desdeFormulario true al completar (form); false al visualizar/revalorar (contestaciones cargadas).
   */
  recalcularResumenSavry(desdeFormulario: boolean = false): void {
    if (!this.esSavry || !this.evaluacion) {
      this.resumenSavryGrupos = [];
      return;
    }

    if (desdeFormulario && this.evaluacionForm) {
      this.resumenSavryGrupos = calcularResumenSavryDesdeFormulario(
        this.evaluacion,
        (sIdx, pIdx) => this.obtenerRespuestaYCriticoDelForm(sIdx, pIdx)
      );
      return;
    }

    this.resumenSavryGrupos = calcularResumenSavryDesdeEncuesta(this.evaluacion);
  }

  private obtenerRespuestaYCriticoDelForm(
    sectionIndex: number,
    questionIndex: number
  ): { idRespuesta?: number; critico?: boolean } {
    const secciones = this.evaluacionForm?.get('secciones') as FormArray;
    const seccionCtrl = secciones?.at(sectionIndex) as FormGroup;
    const preguntas = seccionCtrl?.get('preguntas') as FormArray;
    const preguntaCtrl = preguntas?.at(questionIndex) as FormGroup;
    if (!preguntaCtrl) {
      return {};
    }

    const critico = Boolean(preguntaCtrl.get('critico')?.value);
    const respuesta = preguntaCtrl.get('respuesta')?.value;
    let idRespuesta: number | undefined;

    if (respuesta && typeof respuesta === 'object' && !(respuesta instanceof Date)) {
      idRespuesta = respuesta.idRespuesta;
    }

    return { idRespuesta, critico };
  }

  confirmarValoracionYGuardar() {
    if (this.valoracionForm.invalid) {
      this.valoracionForm.markAllAsTouched();
      this.dialogMensajeService.mensajeError(
        'Debe seleccionar el nivel de riesgo final e ingresar la justificación.'
      );
      return;
    }

    if (this.modoRevalorar) {
      this.enviarRevaloracion();
      return;
    }

    this.enviarEvaluacionCompleta();
  }

  cancelarPanelValoracion() {
    this.mostrarPanelValoracion = false;
    this.modoRevalorar = false;
  }

  abrirRevalorar() {
    this.modoRevalorar = true;
    this.recalcularResumenSavry(false);
    this.mostrarPanelValoracion = true;
    this.cargarValoracionEnFormulario();
  }

  private enviarEvaluacionCompleta() {
    let encabezadoDTO = new EncabezadoDTO();

    encabezadoDTO = this.transformFormToDTO();
    encabezadoDTO.completada = true;
    if (this.esEdicion)
      encabezadoDTO.tokenIdentificador = this.tokenEncabezado;

    if (this.esSavry) {
      encabezadoDTO.tokenIdentificadorValoracionFinal = this.valoracionForm.value.tokenIdentificadorValoracionFinal;
      encabezadoDTO.justificacionValoracion = this.valoracionForm.value.justificacionValoracion;
    }

    this.encuestaService.crearEvaluacion(encabezadoDTO, this.nemonicoMenuActual).subscribe({
      next: (resp: RespuestaPorDefecto<boolean>) => {
        if (resp && resp.exito === false) {
          this.dialogMensajeService.mensajeError(resp.mensaje || 'No se pudo completar la evaluación.');
          return;
        }
        this.mostrarPanelValoracion = false;
        // Ya no se muestra la tabla antigua "resumen de puntuación" (Sección / Bajo…).
        // El resumen SAVRY (Grupo / Bajo / Medio / Alto) sigue en el panel de valoración.
        const tipoTexto = this.obtenerTextoTipoEvaluacion();
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          `Evaluación ${tipoTexto} guardada correctamente.`
        ).afterClosed().subscribe(() => {
          this.cancelar();
        });
      },
      error: () => {
        const tipoTexto = this.obtenerTextoTipoEvaluacion();
        this.dialogMensajeService.mensajeError(
          `Hubo un problema al guardar la evaluación ${tipoTexto}. Inténtalo de nuevo.`
        );
      }
    });
  }

  private enviarRevaloracion() {
    const encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.tokenIdentificador = this.tokenEncabezado;
    encabezadoDTO.encuesta = this.evaluacion?.tokenIdentificador || this.tokenEncuesta;
    encabezadoDTO.fichaIdentificacion = this.uuid_fp;
    encabezadoDTO.completada = true;
    encabezadoDTO.soloValoracion = true;
    encabezadoDTO.tokenIdentificadorValoracionFinal = this.valoracionForm.value.tokenIdentificadorValoracionFinal;
    encabezadoDTO.justificacionValoracion = this.valoracionForm.value.justificacionValoracion;

    this.encuestaService.crearEvaluacion(encabezadoDTO, this.nemonicoMenuActual).subscribe({
      next: (resp: RespuestaPorDefecto<boolean>) => {
        if (resp && resp.exito === false) {
          this.dialogMensajeService.mensajeError(resp.mensaje || 'No se pudo actualizar la valoración.');
          return;
        }
        this.evaluacion.tokenIdentificadorValoracionFinal = encabezadoDTO.tokenIdentificadorValoracionFinal;
        this.evaluacion.justificacionValoracion = encabezadoDTO.justificacionValoracion;
        this.evaluacion.nombreValoracionFinal = this.listaNivelesRiesgo.find(
          (n) => n.tokenIdentificador === encabezadoDTO.tokenIdentificadorValoracionFinal
        )?.nombre;
        this.evaluacion.fechaValoracion = new Date();
        this.mostrarPanelValoracion = false;
        this.modoRevalorar = false;
        this.dialogMensajeService.mensajeExitoso(
          'Revalorar',
          'Valoración final actualizada correctamente.'
        );
      },
      error: () => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar la valoración final. Inténtalo de nuevo.'
        );
      }
    });
  }

  guardarBorrador() {
    let encabezadoDTO = new EncabezadoDTO();

    encabezadoDTO = this.transformFormToDTO();
    encabezadoDTO.completada = false;
    if (this.esEdicion)
      encabezadoDTO.tokenIdentificador = this.tokenEncabezado;

    this.encuestaService.crearEvaluacion(encabezadoDTO, this.nemonicoMenuActual).subscribe({
      next: (resp: RespuestaPorDefecto<boolean>) => {
        if (!resp.exito) {
          const tipoTexto = this.obtenerTextoTipoEvaluacion();
          this.dialogMensajeService.mensajeError(`Hubo un problema al guardar la evaluación ${tipoTexto}. ${resp.mensaje}`);
          return;
        }

        const tipoTexto = this.obtenerTextoTipoEvaluacion();
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          `Evaluación ${tipoTexto} guardada correctamente.`
        ).afterClosed().subscribe(() => {
          this.cancelar();
        });
      },
      error: () => {
        const tipoTexto = this.obtenerTextoTipoEvaluacion();
        this.dialogMensajeService.mensajeError(
          `Hubo un problema al guardar la evaluación ${tipoTexto}. Inténtalo de nuevo.`
        );
      }
    });
  }

  /**
   * La tabla antigua de "resumen de puntuación" (columnas Sección / textos largos Bajo…)
   * quedó deshabilitada. Se conserva el resumen SAVRY por grupo.
   */
  calcularResumenPuntuacion() {
    this.mostrarResumen = false;
    this.dataSource = [];
    this.opcionesUnicas = [];
    this.displayedColumns = ['seccion', 'totalRespuestas'];
    this.totalGeneral = { totalRespuestas: 0, puntuacion: 0 };
  }

  cancelar() {
    this.evaluacionForm.reset();
    this.regresar();
  }

  onMultipleAnswersChange(event: any, sectionIndex: number, questionIndex: number, opcion: any) {
    const respuestasArray = this.getPreguntas(sectionIndex).at(questionIndex).get('respuestas') as FormArray;
    const respuestaControl = this.getPreguntas(sectionIndex).at(questionIndex).get('respuesta');

    if (event.checked) {
      respuestasArray.push(this.fb.group({
        id: opcion.idRespuesta,
        texto: opcion.respuesta
      }));
    } else {
      const index = respuestasArray.controls.findIndex(
        control => control.value.id === opcion.id
      );

      if (index !== -1) {
        respuestasArray.removeAt(index);
      }
    }

    respuestaControl?.setValue(respuestasArray.length > 0 ? 'valid' : null);
  }

  isOptionSelected(sectionIndex: number, questionIndex: number, optionId: number): boolean {
    const respuestasArray = this.getPreguntas(sectionIndex).at(questionIndex).get('respuestas') as FormArray;

    return respuestasArray.controls.some(control => control.value.id === optionId);
  }

  get listaSeccionesArray() {
    return this.evaluacionForm.get('secciones')['controls'];
  }

  get listaSecciones() {
    return this.evaluacionForm.get('secciones') as FormArray;
  }

  getPreguntas(seccionIndex: number): FormArray {
    return this.listaSecciones.at(seccionIndex).get('preguntas') as FormArray;
  }

  getRespuestaControl(i: number, j: number): AbstractControl {
    return this.evaluacionForm.get(['secciones', i, 'preguntas', j, 'respuesta']);
  }

  obtenerEncuesta() {
    let encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.encuesta = this.tokenEncuesta

    this.encuestaService.obtenerEncuestaPorTokenEncuesta(encabezadoDTO, this.nemonicoMenuActual).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO>) => {

          if (!response.exito) {
            const tipoTexto = this.obtenerTextoTipoEvaluacion();
            this.dialogMensajeService.mensajeError(
              `La evaluación ${tipoTexto} no se encuentra configurada. Por favor contacte a su administrador.`
            ).afterClosed().subscribe(() => {
              this.cancelar();
            });
            return;
          }

          this.evaluacion = response.data;

          this.evaluacionForm = this.fb.group({
            secciones: this.fb.array(this.buildSections()),
          });
        },
        error: () => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  obtenerEvaluacion() {
    let encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.tokenIdentificador = this.tokenEncabezado

    this.encuestaService.obtenerEvaluacionPorTokenEncabezado(encabezadoDTO, this.nemonicoMenuActual).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.evaluacion = response.data;
          this.evaluacionForm = this.fb.group({
            secciones: this.fb.array(this.buildSections()),
          });
          this.cargarValoracionEnFormulario();

          if (this.esVisualizacion)
            this.evaluacionForm.disable();
          this.respuestasLargas = this.evaluacion.secciones.find(x => x.nombre == "Conclusiones" && !x.tienePuntuacion)?.preguntas;
          if (this.esSavry) {
            this.recalcularResumenSavry(false);
          } else {
            this.calcularResumenPuntuacion();
          }
        },
        error: () => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  regresar() {
    this.tabService.cambiarTab(0);
    this.location.back();
  }

  trackBySeccion(index: number, seccion: any): any {
    return seccion.id;
  }

  trackByPregunta(index: number, pregunta: any): any {
    return pregunta.id;
  }

  obtenerTotalSteps(): number {
    return (this.evaluacion?.secciones?.length || 1) - 1;
  }

  transformFormToDTO(): EncabezadoDTO {
    const formValue = this.evaluacionForm.getRawValue();
    const contestaciones: ContestacionDTO[] = [];

    formValue.secciones.forEach((seccion: any, seccionIndex) => {
      seccion.preguntas.forEach((pregunta: any, preguntaIndex) => {
        if (pregunta.respuestas && Array.isArray(pregunta.respuestas)) {
          pregunta.respuestas.forEach((respuesta: any) => {
            let nuevaContestacion = {
              idPregunta: pregunta.idPregunta,
              idRespuesta: respuesta.id,
              contestacion: respuesta.texto,
              observacion: pregunta.observaciones || null,
              critico: this.esSavry ? Boolean(pregunta.critico) : false,
            };
            this.evaluacion.secciones.at(seccionIndex).preguntas.at(preguntaIndex).contestaciones.push(nuevaContestacion);
            contestaciones.push(nuevaContestacion);
          });
        } else if (pregunta.respuesta) {
          let nuevaContestacion = {
            idPregunta: pregunta.idPregunta,
            idRespuesta: (typeof pregunta.respuesta === 'object' && !(pregunta.respuesta instanceof Date)) ? pregunta.respuesta.idRespuesta : null,
            contestacion: (pregunta.respuesta instanceof Date)
              ? pregunta.respuesta.toISOString()
              : (typeof pregunta.respuesta === 'object' ? pregunta.respuesta.respuesta : pregunta.respuesta || null),
            observacion: pregunta.observaciones || null,
            critico: this.esSavry ? Boolean(pregunta.critico) : false,
          };
          this.evaluacion.secciones.at(seccionIndex).preguntas.at(preguntaIndex).contestaciones.push(nuevaContestacion);
          contestaciones.push(nuevaContestacion);
        }
      });
    });

    return {
      encuesta: this.evaluacion.tokenIdentificador,
      fichaIdentificacion: this.uuid_fp,
      contestaciones: contestaciones,
    };
  }
}
