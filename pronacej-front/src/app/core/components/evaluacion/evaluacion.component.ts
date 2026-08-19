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
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TabService } from 'app/core/services/tab.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
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
    MatStepperModule
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

  // Nemónicos diferenciados para cada tipo de evaluación
  nemonicoMenuEvaluacionConductual = etiquetasModel.NEMONICO_MENU_EVALUACION_CONDUCTUAL;
  nemonicoMenuEvaluacionPsicologica = etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOLOGICA;
  nemonicoMenuPruebasPsicologicas = etiquetasModel.NEMONICO_MENU_PRUEBAS_PSICOLOGICAS;
  nemonicoMenuNivelRiesgo = etiquetasModel.NEMONICO_MENU_NIVEL_RIESGO;
  
  // Nemónico actual que se usará basado en el tipo de evaluación
  nemonicoMenuActual: string;

  esVisualizacion: boolean = false;
  esEdicion: boolean = false;

  evaluacionForm: FormGroup;

  mostrarResumen: boolean = false;
  dataSource: any[] = [];
  displayedColumns: string[] = ['seccion', 'totalRespuestas'];
  opcionesUnicas: string[] = []; // Opciones únicas encontradas
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
  ) { }

  ngOnInit() {
    this.listaPrev = history.state.listaPrev;
    this.evaluacionForm = this.fb.group({});

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

    // Determinar el tipo de evaluación desde el state si no se proporcionó como Input
    if (state && state.tipoEvaluacion) {
      this.tipoEvaluacion = state.tipoEvaluacion;
    }

    // Establecer el nemónico actual basado en el tipo de evaluación
    this.determinarNemonicoMenu();

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

  ngOnChanges(changes: SimpleChanges) {
    if ((changes['tokenEncuesta'] && changes['tokenEncuesta'].currentValue) ||
      (changes['tokenEncabezado'] && changes['tokenEncabezado'].currentValue) ||
      (changes['completada'] && changes['completada'].currentValue) ||
      (changes['tipoEvaluacion'] && changes['tipoEvaluacion'].currentValue)) {

      // Actualizar el nemónico si cambió el tipo de evaluación
      if (changes['tipoEvaluacion']) {
        this.determinarNemonicoMenu();
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

  /**
   * Determina qué nemónico usar basado en el tipo de evaluación
   */
  private determinarNemonicoMenu(): void {
    switch (this.tipoEvaluacion) {
      case 'psicologica':
        this.nemonicoMenuActual = this.nemonicoMenuEvaluacionPsicologica;
        break;
      case 'prueba':
        this.nemonicoMenuActual = this.nemonicoMenuPruebasPsicologicas;
        break;
      case 'riesgo': // ✅ Agregado caso para nivel de riesgo
        this.nemonicoMenuActual = this.nemonicoMenuNivelRiesgo;
        break;
      case 'conductual':
      default:
        this.nemonicoMenuActual = this.nemonicoMenuEvaluacionConductual;
        break;
    }
  }

  /**
   * Obtiene el texto descriptivo para el tipo de evaluación
   */
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
        // Si no es selección múltiple, inicializa con la primera contestación o null
        respuesta: pregunta.requerido
          ? [pregunta.contestaciones?.[0]?.contestacion || null, Validators.required]
          : [pregunta.contestaciones?.[0]?.contestacion || null],
      };

      if (pregunta.categoria === "PREG_SEL_UNICA") {
        let temp = pregunta.respuestas.find(r => r.idRespuesta === pregunta.contestaciones?.[0]?.idRespuesta) || null;

        // Asignar el valor de respuesta como el objeto con id y valor de la contestación seleccionada
        controls["respuesta"] = pregunta.requerido
          ? [temp || null, Validators.required]
          : [temp || null]
      }

      // Manejar selección múltiple
      if (pregunta.categoria === "PREG_OP_MULTIPLE") {
        // Inicializar el FormArray con grupos para cada contestación
        controls["respuestas"] = this.fb.array(
          (pregunta.contestaciones || []).map((c) =>
            this.fb.group({
              id: c.idRespuesta,
              texto: c.contestacion,
            })
          )
        );
      }

      // Inicializar observaciones si aplica
      if (pregunta.tieneObservaciones) {
        controls["observaciones"] = [
          pregunta.contestaciones?.[0]?.observacion || null,
        ];
      }

      // Inicializar documentos si aplica
      if (pregunta.permiteDocumentos) {
        controls["documentos"] = [[]];
      }

      return this.fb.group(controls);
    });
  }

  guardarEvaluacion() {
    let encabezadoDTO = new EncabezadoDTO();

    encabezadoDTO = this.transformFormToDTO();
    encabezadoDTO.completada = true;
    if (this.esEdicion)
      encabezadoDTO.tokenIdentificador = this.tokenEncabezado;

    // Usar el nemónico correspondiente al tipo de evaluación
    this.encuestaService.crearEvaluacion(encabezadoDTO, this.nemonicoMenuActual).subscribe({
      next: () => {
        this.calcularResumenPuntuacion(); // Llama al cálculo del resumen
        if (this.mostrarResumen) {
          this.esEdicion = false;
          // Mostrar el resumen
          const tipoTexto = this.obtenerTextoTipoEvaluacion();
          this.dialogMensajeService.mensajeExitoso(
            'Guardar',
            `Evaluación ${tipoTexto} guardada correctamente. Mostrando resumen de puntuación.`
          );
          this.tabService.cambiarTab(0);
        } else {
          // Si no hay resumen, regresa
          const tipoTexto = this.obtenerTextoTipoEvaluacion();
          this.dialogMensajeService.mensajeExitoso(
            'Guardar',
            `Evaluación ${tipoTexto} guardada correctamente.`
          ).afterClosed().subscribe(() => {
            this.cancelar();
          });
        }
      },
      error: (err) => {
        const tipoTexto = this.obtenerTextoTipoEvaluacion();
        this.dialogMensajeService.mensajeError(
          `Hubo un problema al guardar la evaluación ${tipoTexto}. Inténtalo de nuevo.`
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

    // Usar el nemónico correspondiente al tipo de evaluación
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
      error: (err) => {
        const tipoTexto = this.obtenerTextoTipoEvaluacion();
        this.dialogMensajeService.mensajeError(
          `Hubo un problema al guardar la evaluación ${tipoTexto}. Inténtalo de nuevo.`
        );
      }
    });
  }

  calcularResumenPuntuacion() {
    if (!this.evaluacion || !this.evaluacion.secciones) return;

    // Filtrar secciones con puntuación
    const seccionesConPuntuacion = this.evaluacion.secciones.filter(
      (seccion: any) => seccion.tienePuntuacion
    );

    // Mostrar resumen solo si hay al menos una sección con puntuación
    this.mostrarResumen = seccionesConPuntuacion.length > 0;
    if (!this.mostrarResumen) return;

    // Obtener todas las opciones únicas de las respuestas (dinámicas)
    const opcionesSet = new Set<string>();
    seccionesConPuntuacion.forEach((seccion: SeccionDTO) => {
      seccion.preguntas.forEach((pregunta: PreguntaDTO) => {
        if (pregunta.respuestas && Array.isArray(pregunta.respuestas)) {
          pregunta.respuestas.forEach((opcion: RespuestaDTO) => {
            let respuestaLimpiada = opcion.respuesta;

            // Si la respuesta contiene tags <b></b>, extraemos solo el texto dentro de esos tags
            const matches = respuestaLimpiada.match(/<b>(.*?)<\/b>/g);
            if (matches) {
              matches.forEach((match) => {
                // Extraemos solo el texto dentro de <b></b>
                const textoB = match.replace(/<b>/g, '').replace(/<\/b>/g, '').trim();
                opcionesSet.add(textoB);
                this.valorRespuestas[textoB] = opcion.valorRespuesta;
              });
            } else {
              // Si no hay tags <b></b>, agregamos la respuesta completa
              opcionesSet.add(respuestaLimpiada);
              this.valorRespuestas[respuestaLimpiada] = opcion.valorRespuesta;
            }
          });
        }
      });
    });

    // Convertir el Set a un array
    this.opcionesUnicas = Array.from(opcionesSet);

    // Definir las columnas para la tabla dinámica
    this.displayedColumns = ['seccion', ...this.opcionesUnicas, 'totalRespuestas'];

    // Inicializar el total general con las opciones dinámicas
    this.totalGeneral = { totalRespuestas: 0, puntuacion: 0 };
    this.opcionesUnicas.forEach((opcion) => {
      this.totalGeneral[opcion] = 0; // Inicializamos cada opción con 0
    });

    // Calcular el resumen para cada sección
    this.dataSource = seccionesConPuntuacion.map((seccion: SeccionDTO) => {
      const seccionResumen: any = {
        seccion: seccion.nombre,
        totalRespuestas: 0,
        puntuacion: 0,
      };

      // Inicializar contador para cada opción única
      this.opcionesUnicas.forEach((opcion) => {
        seccionResumen[opcion] = 0; // Inicializar el contador de cada opción
      });

      // Contar respuestas por opción
      seccion.preguntas.forEach((pregunta: PreguntaDTO) => {
        if (pregunta.contestaciones && Array.isArray(pregunta.contestaciones)) {
          pregunta.contestaciones.forEach((contestacion: ContestacionDTO) => {
            // Encontramos la respuesta seleccionada en contestacion
            const respuestaSeleccionada = pregunta.respuestas.find(
              (respuesta) => respuesta.idRespuesta === contestacion.idRespuesta
            );

            if (respuestaSeleccionada) {
              let opcionTexto = respuestaSeleccionada.respuesta;

              // Si la respuesta contiene tags <b></b>, extraemos solo el texto dentro de esos tags
              const matches = opcionTexto.match(/<b>(.*?)<\/b>/g);
              if (matches) {
                matches.forEach((match) => {
                  // Extraemos solo el texto dentro de <b></b>
                  opcionTexto = match.replace(/<b>/g, '').replace(/<\/b>/g, '').trim();
                });
              }

              // Contabilizamos las respuestas seleccionadas
              seccionResumen[opcionTexto] += 1;
              this.totalGeneral[opcionTexto] += 1;

              // Sumar al total de puntuación según el valorRespuesta
              seccionResumen.puntuacion += respuestaSeleccionada.valorRespuesta || 0;
              this.totalGeneral.puntuacion += respuestaSeleccionada.valorRespuesta || 0;
            }
          });

          // Incrementar el total de respuestas por sección
          seccionResumen.totalRespuestas += pregunta.contestaciones.length;
        }
      });

      // Sumar al total general
      this.totalGeneral.totalRespuestas += seccionResumen.totalRespuestas;

      return seccionResumen;
    });
  }

  cancelar() {
    this.evaluacionForm.reset();
    this.regresar();
  }

  onMultipleAnswersChange(event: any, sectionIndex: number, questionIndex: number, opcion: any) {
    const respuestasArray = this.getPreguntas(sectionIndex).at(questionIndex).get('respuestas') as FormArray;
    const respuestaControl = this.getPreguntas(sectionIndex).at(questionIndex).get('respuesta');

    if (event.checked) {
      // Agregar la respuesta seleccionada al FormArray
      respuestasArray.push(this.fb.group({
        id: opcion.idRespuesta,
        texto: opcion.respuesta
      }));
    } else {
      // Eliminar la respuesta deseleccionada
      const index = respuestasArray.controls.findIndex(
        control => control.value.id === opcion.id
      );

      if (index !== -1) {
        respuestasArray.removeAt(index);
      }
    }

    // Actualizar el control "respuesta" para validar si hay elementos seleccionados
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

    // Usar el nemónico correspondiente al tipo de evaluación
    this.encuestaService.obtenerEncuestaPorTokenEncuesta(encabezadoDTO, this.nemonicoMenuActual).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO>) => {

          if (!response.exito) {
            const tipoTexto = this.obtenerTextoTipoEvaluacion();
            this.dialogMensajeService.mensajeError(
              `La evaluación ${tipoTexto} no se encuentra configurada. Por favor contacte a su administrador.`
            ).afterClosed().subscribe(result => {
              this.cancelar();
            });
            return;
          }

          this.evaluacion = response.data;

          this.evaluacionForm = this.fb.group({
            secciones: this.fb.array(this.buildSections()),
          });
        },
        error: (error: any) => {
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

    // Usar el nemónico correspondiente al tipo de evaluación
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

          if (this.esVisualizacion)
            this.evaluacionForm.disable();
          this.respuestasLargas = this.evaluacion.secciones.find(x => x.nombre == "Conclusiones" && !x.tienePuntuacion)?.preguntas;
          this.calcularResumenPuntuacion();
        },
        error: (error: any) => {
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
    return seccion.id; // Suponiendo que cada sección tiene un id único
  }

  trackByPregunta(index: number, pregunta: any): any {
    return pregunta.id; // Suponiendo que cada pregunta tiene un id único
  }

  obtenerTotalSteps(): number {
    if (this.mostrarResumen && this.esVisualizacion)
      return this.evaluacion?.secciones?.length;
    else
      return this.evaluacion?.secciones?.length - 1;
  }

  transformFormToDTO(): EncabezadoDTO {
    const formValue = this.evaluacionForm.value;
    const contestaciones: ContestacionDTO[] = [];

    formValue.secciones.forEach((seccion: any, seccionIndex) => {
      seccion.preguntas.forEach((pregunta: any, preguntaIndex) => {
        // Selección múltiple
        if (pregunta.respuestas && Array.isArray(pregunta.respuestas)) {
          pregunta.respuestas.forEach((respuesta: any) => {
            let nuevaContestacion = {
              idPregunta: pregunta.idPregunta,
              idRespuesta: respuesta.id, // ID de la respuesta seleccionada
              contestacion: respuesta.texto, // Texto de la respuesta seleccionada
              observacion: pregunta.observaciones || null,
            };
            this.evaluacion.secciones.at(seccionIndex).preguntas.at(preguntaIndex).contestaciones.push(nuevaContestacion);
            contestaciones.push(nuevaContestacion);
          });
        } else if (pregunta.respuesta) {
          // Respuesta única
          let nuevaContestacion = {
            idPregunta: pregunta.idPregunta,
            idRespuesta: (typeof pregunta.respuesta === 'object' && !(pregunta.respuesta instanceof Date)) ? pregunta.respuesta.idRespuesta : null,
            contestacion: (pregunta.respuesta instanceof Date)
              ? pregunta.respuesta.toISOString() // Convertir a formato "YYYY-MM-DD"
              : (typeof pregunta.respuesta === 'object' ? pregunta.respuesta.respuesta : pregunta.respuesta || null),
            observacion: pregunta.observaciones || null,
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