import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EvaluacionSeguimientoEducativoLaboralDTO } from 'app/core/model/both/evaluacionSeguimientoEducativoLaboralDTO.model';
import { RecomendacionComentarioPorEvalSeguDTO } from 'app/core/model/both/recomendacionComentarioPorEvalSeguDTO.model';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { InstitucionService } from 'app/modules/institucion/institucion.service';
import { EvaluacionSeguimientoEducativoLaboralService } from 'app/modules/seguridad/services/evaluacionSeguimiento.service';
import { environment } from 'environments/environment';
import { MdRegiEvalComponent } from './md-regi-eval/md-regi-eval.component';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS } from 'app/core/utils/funcionesUtils.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { catchError, firstValueFrom, Observable, switchMap, tap, throwError } from 'rxjs';
import { ValidationErrors } from '@iplab/ngx-file-upload';
import { TabService } from 'app/core/services/tab.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { HttpClient } from '@angular/common/http';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { PdfService } from 'app/core/services/pdf.service';

// Registra la localización española para fechas
registerLocaleData(localeEs);

@Component({
  selector: 'app-eval-segu-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatExpansionModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './eval-segu-crear-editar.component.html',
  styleUrl: './eval-segu-crear-editar.component.scss'
})
export class EvalSeguCrearEditarComponent implements OnInit {
  // Identificadores
  uuid_fp: string;
  uuid_es: string;

  // Formulario principal
  evaluacionSeguimientoForm: FormGroup;
  evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO;
  tituloPantalla = "evaluación educativa/laboral";

  // Estados del componente
  esEdicion = false;
  esVisualizacion = false;
  estadoVisualizar = false;
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_EVALUACION_EDUCATIVA_LABORAL;

  // Variable de control para evitar envíos duplicados
  estaProcesandoGuardado: boolean = false;

  // Institución seleccionada
  institucionSeleccionada: RegistroInstitucionDTO | null = null;

  // Datos de recomendaciones
  listaRecomendacionesComentarios: RecomendacionComentarioPorEvalSeguDTO[] = [];
  recomendacionComentarioDS: MatTableDataSource<RecomendacionComentarioPorEvalSeguDTO>;

  // Listado de catálogos
  listaInstituciones: RegistroInstitucionDTO[] = [];
  listaInstitucionesPorCentro: RegistroInstitucionDTO[] = [];
  listaTipos: CatalogoDTO[] = [];
  listaMediosVerificacion: CatalogoDTO[] = [];

  // Configuración de columnas
  columnasRecomendacionComentario: string[] = ['acciones', 'fecha', 'comentario'];

  @ViewChild('recomendacionComentarioPag') recomendacionComentarioPag: MatPaginator;

  // Control del campo "Otros"
  mostrarCampoOtros = false;
  centro: JerarquiaDTO;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioInstitucion: InstitucionService,
    private servicioEvaluacionSeguimiento: EvaluacionSeguimientoEducativoLaboralService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public dialogoModal: MatDialog,
    public utilidades: FuncionesUtils,
    private adaptadorFecha: DateAdapter<any>,
    private servicioJerarquias: JerarquiaService,
    private servicioTab: TabService,
    private http: HttpClient,
    public servicioPdf: PdfService,
  ) {
    // Configurar el locale para el adaptador de fechas
    this.adaptadorFecha.setLocale('es');
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.uuid_fp = this.rutaActiva.snapshot.params['uuid_fp'];
    this.evaluacionSeguimientoDTO = history.state.evaluacionSeguimientoDTO;
    this.cargarCentro().pipe(
      switchMap(() => this.cargarInstituciones())
    ).subscribe();
    
    this.cargarDatosCatalogo();

    if (this.evaluacionSeguimientoDTO) {
      this.esVisualizacion = this.evaluacionSeguimientoDTO.esVisualizacion;
      this.estadoVisualizar = this.esVisualizacion;
      if (this.esVisualizacion) {
        this.evaluacionSeguimientoForm.disable();
      }
      this.esEdicion = true;
      this.empezarEdicion(this.evaluacionSeguimientoDTO);
      this.obtenerRecomendacionesComentariosPorEvaluacionSeguimiento();
    }
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (control.value === null || control.value === undefined) {
        return null;
      }

      if (typeof control.value === 'string') {
        return control.value.trim().length === 0 && control.value.length > 0 ? { 'soloEspacios': true } : null;
      }

      return null;
    };
  }

  /**
   * Validador personalizado para campos tipo select
   * Verifica que no se seleccione el valor '0' (opción "Seleccione")
   */
  validarSelect(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (control.value === '0') {
        return { 'seleccionInvalida': true };
      }
      return null;
    };
  }

  /**
   * Construye el formulario con validadores
   */
  construirFormulario() {
    this.evaluacionSeguimientoForm = this.constructorFormulario.group({
      fechaInicio: [new Date(), [Validators.required,]],
      fechaFin: [new Date(), [Validators.required,]],
      tokenIdentificadorTipoEvaluacionSeguimiento: ['0', [Validators.required, this.validarSelect()]],
      tokenIdentificadorInstitucion: ['0', [Validators.required, this.validarSelect()]],
      tokenIdentificadorMedioVerificacion: ['0', [Validators.required, this.validarSelect()]],
      resultadoSeguimiento: [null, [Validators.required, this.validarNoEspacios()]],
      nombreInstitucionOtros: [{ value: '', disabled: true }, [Validators.required, this.validarNoEspacios()]]
    }, { 
      validators: this.validarFechas() 
    });

    // Observamos cambios en el selector de institución
    this.evaluacionSeguimientoForm.get('tokenIdentificadorInstitucion').valueChanges
      .subscribe((identificadorInstitucion: string) => {
        this.institucionSeleccionada = this.listaInstitucionesPorCentro.find(i => i.tokenIdentificador === identificadorInstitucion) || null;
      });
  }

  /**
   * Valida que la fecha fin no sea anterior a la fecha inicio
   * @returns Validador personalizado
   */
  validarFechas(): ValidatorFn {
    return (formGroup: AbstractControl): ValidationErrors | null => {
      const fechaInicio = formGroup.get('fechaInicio')?.value;
      const fechaFin = formGroup.get('fechaFin')?.value;
      
      // Verificar que ambas fechas existan
      if (fechaInicio && fechaFin) {
        // Convertir a objetos Date para comparación correcta
        const fechaInicioDate = new Date(fechaInicio);
        const fechaFinDate = new Date(fechaFin);
        
        // Normalizar las fechas (eliminar la parte de hora)
        fechaInicioDate.setHours(0, 0, 0, 0);
        fechaFinDate.setHours(0, 0, 0, 0);
        
        if (fechaFinDate < fechaInicioDate) {
          // Si la fecha fin es anterior a la fecha inicio, establecer el error
          const controlFechaFin = formGroup.get('fechaFin');
          if (controlFechaFin) {
            // Mantener otros errores que puedan existir
            const erroresActuales = controlFechaFin.errors || {};
            controlFechaFin.setErrors({ ...erroresActuales, fechaInvalida: true });
          }
          return { fechasInvalidas: true };
        } else {
          // Si no hay error, limpiar el error específico de fechaInvalida
          const controlFechaFin = formGroup.get('fechaFin');
          if (controlFechaFin && controlFechaFin.errors) {
            const erroresActuales = { ...controlFechaFin.errors };
            
            if (erroresActuales.fechaInvalida) {
              delete erroresActuales.fechaInvalida;
              
              // Si no quedan más errores, establecer a null, de lo contrario mantener los otros errores
              controlFechaFin.setErrors(Object.keys(erroresActuales).length ? erroresActuales : null);
            }
          }
        }
      }
      
      return null;
    };
  }

  /**
   * Actualiza la fecha en el formulario cuando cambia en el datepicker
   * @param evento Evento del datepicker
   * @param nombreControl Nombre del control (fechaInicio o fechaFin)
   */
  actualizarFecha(evento: MatDatepickerInputEvent<Date>, nombreControl: string) {
    if (evento.value) {
      const fecha = new Date(evento.value);
      this.evaluacionSeguimientoForm.get(nombreControl).setValue(fecha);
      
      // Verificar si necesitamos actualizar automáticamente la otra fecha
      if (nombreControl === 'fechaInicio') {
        // Si la fecha de inicio es posterior a la fecha fin, actualizar la fecha fin
        const fechaFin = this.evaluacionSeguimientoForm.get('fechaFin').value;
        if (fechaFin) {
          const fechaFinDate = new Date(fechaFin);
          const fechaInicioDate = new Date(fecha);
          
          // Normalizar fechas para comparación
          fechaFinDate.setHours(0, 0, 0, 0);
          fechaInicioDate.setHours(0, 0, 0, 0);
          
          if (fechaInicioDate > fechaFinDate) {
            // Establecer la fecha fin igual a la fecha inicio
            this.evaluacionSeguimientoForm.get('fechaFin').setValue(new Date(fecha));
          }
        }
      }
      
      // Revalidar el formulario completo
      this.evaluacionSeguimientoForm.updateValueAndValidity();
    }
  }

  tieneError(controlName: string, errorName: string) {
    return this.evaluacionSeguimientoForm.get(controlName)?.hasError(errorName);
  }
  /**
   * Carga los catálogos necesarios
   */
  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('TIPOS_EVALUACION_SEGUIMIENTO', this.nemonicoMenu).subscribe({
      next: (data) => this.listaTipos = data,
      error: (error) => console.error('Error cargando tipos:', error)
    });

    this.utilidades.obtenerListaCatalogo('MEDIOS_VERIFICACION', this.nemonicoMenu).subscribe({
      next: (data) => this.listaMediosVerificacion = data,
      error: (error) => console.error('Error cargando medios de verificación:', error)
    });
  }

  /**
   * Carga la lista de instituciones
   */
  cargarInstituciones(): Observable<RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>> {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.centro.tokenIdentificador;
  
    return this.servicioInstitucion.obtenerRegistroInstituciones(solicitudPaginacion, this.nemonicoMenu).pipe(
      tap((respuesta: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
        if (respuesta.exito) {
          this.listaInstituciones = respuesta.data.data;
          console.log(this.listaInstituciones);
          console.log(this.centro);
          this.listaInstitucionesPorCentro = this.listaInstituciones.filter(institucion => 
            (institucion.finalidadInstitucion === this.centro.jerarquiaPadre?.nemonico || institucion.finalidadInstitucion === 'Todas') &&
            institucion.estado === 'Activo'
          );
        }
      }),
      catchError((error) => {
        console.error('Error cargando instituciones:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Abre el diálogo para agregar recomendación
   */
  agregarRecomendacionComentario() {
    const refDialogo = this.dialogoModal.open(MdRegiEvalComponent, {
      data: {},
      width: '600px',
    });

    refDialogo.afterClosed().subscribe(async (resultado: RecomendacionComentarioPorEvalSeguDTO) => {
      if (resultado) {
        this.listaRecomendacionesComentarios.unshift(resultado);
        this.actualizarFuenteDeDatosRecomendaciones();
      }
    });
  }

  /**
   * Abre el diálogo para editar recomendación
   */
  editarRecomendacionComentario(fila: RecomendacionComentarioPorEvalSeguDTO, indice: number) {
    const refDialogo = this.dialogoModal.open(MdRegiEvalComponent, {
      data: { fila },
      width: '600px',
    });

    refDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaRecomendacionesComentarios[indice] = resultado;
        this.actualizarFuenteDeDatosRecomendaciones();
      }
    });
  }

  /**
   * Elimina una recomendación de la lista
   */
  eliminarRecomendacionComentario(indice: number) {
    let refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta recomendación? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const elementoEliminar = this.listaRecomendacionesComentarios[indice];
          if (elementoEliminar.tokenIdentificador === "0") {
            this.listaRecomendacionesComentarios.splice(indice, 1);
            this.actualizarFuenteDeDatosRecomendaciones();
          } else {
            this.eliminarRecomendacionComentarioPorEvaluacionSeguimiento(elementoEliminar);
          }
        }
      }
    });
  }

  /**
   * Obtiene las recomendaciones asociadas a la evaluación
   */
  obtenerRecomendacionesComentariosPorEvaluacionSeguimiento() {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.tokenIdentificador = this.uuid_es;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.size = 10;

    this.servicioEvaluacionSeguimiento.obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(solicitudPaginacion).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        this.listaRecomendacionesComentarios = respuesta.data.data;
        this.actualizarFuenteDeDatosRecomendaciones();
      },
      error: (error: any) => {
        console.error('Error obteniendo recomendaciones:', error);
      }
    });
  }

  /**
   * Actualiza el datasource de la tabla
   */
  actualizarFuenteDeDatosRecomendaciones() {
    this.recomendacionComentarioDS = new MatTableDataSource(this.listaRecomendacionesComentarios);
    this.recomendacionComentarioDS.paginator = this.recomendacionComentarioPag;
  }

  /**
   * Configura el formulario para edición
   */
  empezarEdicion(evaluacionSeguimientoEditar: EvaluacionSeguimientoEducativoLaboralDTO) {
    this.evaluacionSeguimientoDTO = evaluacionSeguimientoEditar;
    this.uuid_es = evaluacionSeguimientoEditar.tokenIdentificador;

    // Conversión correcta de fechas para el datepicker
    const fechaInicio = evaluacionSeguimientoEditar.fechaInicio ? new Date(evaluacionSeguimientoEditar.fechaInicio) : new Date();
    const fechaFin = evaluacionSeguimientoEditar.fechaFin ? new Date(evaluacionSeguimientoEditar.fechaFin) : new Date();

    this.evaluacionSeguimientoForm.patchValue({
      fechaInicio: fechaInicio,
      fechaFin: fechaFin,
      tokenIdentificadorTipoEvaluacionSeguimiento: evaluacionSeguimientoEditar.tokenIdentificadorTipoEvaluacionSeguimiento,
      tokenIdentificadorInstitucion: evaluacionSeguimientoEditar.tokenIdentificadorInstitucion || "1",
      tokenIdentificadorMedioVerificacion: evaluacionSeguimientoEditar.tokenIdentificadorMedioVerificacion,
      resultadoSeguimiento: evaluacionSeguimientoEditar.resultadoSeguimiento,
      nombreInstitucionOtros: evaluacionSeguimientoEditar.nombreInstitucionOtros
    });

    // Configura la visualización del campo "Otros"
    if (!evaluacionSeguimientoEditar.tokenIdentificadorInstitucion) {
      this.mostrarCampoOtros = true;
      this.evaluacionSeguimientoForm.get('nombreInstitucionOtros').enable();
    }
  }

  /**
   * Guarda o actualiza la evaluación de seguimiento
   */
  crearActualizar() {
      // Si ya está procesando una solicitud, ignorar clicks adicionales
      if (this.estaProcesandoGuardado) {
          return;
      }
      
      // Marcar todos los campos como tocados para activar validaciones
      Object.keys(this.evaluacionSeguimientoForm.controls).forEach(clave => {
          const control = this.evaluacionSeguimientoForm.get(clave);
          control.markAsTouched();
      });

      // Verificar si hay campos con error de "soloEspacios"
      let tieneEspaciosEnBlanco = false;
      let tieneSeleccionInvalida = false;

      Object.keys(this.evaluacionSeguimientoForm.controls).forEach(clave => {
          const control = this.evaluacionSeguimientoForm.get(clave);
          if (control.errors) {
              if (control.errors['soloEspacios']) {
                  tieneEspaciosEnBlanco = true;
              }
              if (control.errors['seleccionInvalida']) {
                  tieneSeleccionInvalida = true;
              }
          }
      });

      if (this.evaluacionSeguimientoForm.invalid) {
          if (tieneEspaciosEnBlanco) {
              this.servicioMensajes.mensajeError('No se permiten campos con solo espacios en blanco.');
              return;
          }
          if (tieneSeleccionInvalida) {
              this.servicioMensajes.mensajeError('Debe seleccionar una opción válida en todos los campos de lista.');
              return;
          }
          this.servicioMensajes.mensajeError('Por favor complete los campos obligatorios.');
          return;
      }

      // Establecer bandera de procesamiento
      this.estaProcesandoGuardado = true;
      this.evaluacionSeguimientoForm.disable();

      let evaluacionSeguimiento = new EvaluacionSeguimientoEducativoLaboralDTO();
      const valoresFormulario = this.evaluacionSeguimientoForm.getRawValue();

      // Limpiar espacios en blanco de los campos de texto
      Object.keys(valoresFormulario).forEach(clave => {
          if (typeof valoresFormulario[clave] === 'string') {
              valoresFormulario[clave] = valoresFormulario[clave].trim();
          }
      });

      // Aseguramos que las fechas sean objetos Date válidos
      const fechaInicio = valoresFormulario.fechaInicio instanceof Date
          ? valoresFormulario.fechaInicio
          : new Date(valoresFormulario.fechaInicio);

      const fechaFin = valoresFormulario.fechaFin instanceof Date
          ? valoresFormulario.fechaFin
          : new Date(valoresFormulario.fechaFin);

      Object.assign(evaluacionSeguimiento, {
          ...valoresFormulario,
          fechaInicio: fechaInicio,
          fechaFin: fechaFin,
          listaRecomendacionesComentarios: this.listaRecomendacionesComentarios,
          tokenIdentificadorFichaIdentificacion: this.uuid_fp,
          tokenIdentificador: this.evaluacionSeguimientoDTO?.tokenIdentificador,
          esEdicion: this.esEdicion
      });

      const dialogoCarga = this.servicioMensajes.mensajeLoading(`Guardando ${this.tituloPantalla}...`);

      this.servicioEvaluacionSeguimiento.crearEvaluacionSeguimiento(evaluacionSeguimiento, this.nemonicoMenu).subscribe({
          next: (respuesta: RespuestaPorDefecto<EvaluacionSeguimientoEducativoLaboralDTO>) => {
              dialogoCarga.close();
              
              // Restablecer bandera de procesamiento
              this.estaProcesandoGuardado = false;
              this.evaluacionSeguimientoForm.enable();

              if (!respuesta.exito) {
                  this.servicioEvaluacionSeguimiento.checkError(respuesta);
                  return;
              }

              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              this.servicioTab.cambiarTab(3);
              this.enrutador.navigate(['../../'], {
                  relativeTo: this.rutaActiva,
                  state: {
                      selectedTab: 3 // índice de la pestaña de Evaluación y Seguimiento
                  }
              });
          },
          error: (error: any) => {
              dialogoCarga.close();
              this.servicioEvaluacionSeguimiento.checkError(error);
              
              // Restablecer bandera de procesamiento en caso de error
              this.estaProcesandoGuardado = false;
              this.evaluacionSeguimientoForm.enable();
          }
      });
  }

  /**
   * Cancela la edición y regresa
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.evaluacionSeguimientoForm.reset();
    this.evaluacionSeguimientoDTO = null;
    this.servicioTab.cambiarTab(3);
    this.enrutador.navigate(['../../'], {
      relativeTo: this.rutaActiva,
      state: {
        selectedTab: 3
      }
    });
  }

  /**
   * Elimina una recomendación del servidor
   */
  eliminarRecomendacionComentarioPorEvaluacionSeguimiento(recomendacion: RecomendacionComentarioPorEvalSeguDTO) {
    let refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta recomendación? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          let dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando recomendación...");
          this.servicioEvaluacionSeguimiento.eliminarRecomendacionComentario(recomendacion).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) {
                return;
              }

              this.obtenerRecomendacionesComentariosPorEvaluacionSeguimiento();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioEvaluacionSeguimiento.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Maneja el cambio en el selector de institución
   */
  onInstitucionChange(event: any) {
    if (event.value === '1') {
      this.mostrarCampoOtros = true;
      this.evaluacionSeguimientoForm.get('nombreInstitucionOtros').enable();
    } else {
      this.mostrarCampoOtros = false;
      this.evaluacionSeguimientoForm.get('nombreInstitucionOtros').disable();
      this.evaluacionSeguimientoForm.get('nombreInstitucionOtros').setValue('');
    }
  }

  cargarCentro(): Observable<RespuestaPorDefecto<JerarquiaDTO>> {
    return this.servicioJerarquias.obtenerJerarquiaPorNumeroDeDocumento('').pipe(
      tap((respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
        if (!environment.production) {
          console.log(respuesta.data);
        }
        if (!respuesta.exito) {
          this.servicioJerarquias.checkError(respuesta);
          return;
        }
        this.centro = respuesta.data;
      }),
      catchError((error) => {
        this.servicioJerarquias.checkError(error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Imprime la evaluación y seguimiento educativo/laboral
   */
  async imprimirFicha() {
    try {
      // 1. Mostrar diálogo de confirmación
      const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
        "¿Está seguro de imprimir la evaluación y seguimiento educativo/laboral?",
        "¿Desea continuar?"
      );

      refDialogo.afterClosed().subscribe({
        next: async (respuesta: "confirmed" | "cancelled") => {
          if (respuesta == "confirmed") {
            // 2. Mostrar diálogo de carga
            const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión...");

            try {
              // 3. Cargar la imagen como base64
              const datos = await firstValueFrom(this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' }));
              const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
              const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

              // 4. Obtener datos de la ficha de identificación
              const respuestaFicha = await firstValueFrom(
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
              );

              if (!respuestaFicha.exito) {
                dialogoCarga.close();
                this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                return;
              }

              const fichaIdentificacion = respuestaFicha.data;
              const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
              const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
              const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

              // 5. Obtener recomendaciones y comentarios
              let paginacionRequest = new PaginacionRequest();
              paginacionRequest.tokenIdentificador = this.evaluacionSeguimientoDTO?.tokenIdentificador || '';
              paginacionRequest.page = 0;
              paginacionRequest.size = 100;

              const respuestaRecomendaciones = await firstValueFrom(
                this.servicioEvaluacionSeguimiento.obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(paginacionRequest)
              );

              let listaRecomendaciones = [];
              if (respuestaRecomendaciones.exito) {
                listaRecomendaciones = respuestaRecomendaciones.data.data;
              }

              // 6. Crear tabla de recomendaciones
              let tablaRecomendaciones = new TablaPlantilla();
              tablaRecomendaciones.encabezados = ['Fecha', 'Comentario'];

              if (listaRecomendaciones && listaRecomendaciones.length > 0) {
                tablaRecomendaciones.filas = listaRecomendaciones.map(recomendacion => ({
                  'Fecha': recomendacion.fecha ? this.utilidades.formatearFecha(recomendacion.fecha) : 'No especificado',
                  'Comentario': recomendacion.comentario || 'No especificado'
                }));
              } else {
                tablaRecomendaciones.filas = [{
                  'Fecha': '',
                  'Comentario': 'No hay recomendaciones registradas'
                }];
              }

              // 7. Obtener información de institución
              let nombreInstitucion = this.evaluacionSeguimientoForm.get('nombreInstitucionOtros')?.value || '';
              let tipoEntidad = 'No especificado';

              // Obtener valores del formulario
              const formValues = this.evaluacionSeguimientoForm.getRawValue();
              
              if (formValues.tokenIdentificadorInstitucion && 
                  formValues.tokenIdentificadorInstitucion !== '0' && 
                  formValues.tokenIdentificadorInstitucion !== '1') {
                
                const institucion = this.listaInstitucionesPorCentro.find(
                  inst => inst.tokenIdentificador === formValues.tokenIdentificadorInstitucion
                );
                
                if (institucion) {
                  nombreInstitucion = institucion.nombreOrganizacion || '';
                  tipoEntidad = institucion.tipoOrganizacion?.nombre || '';
                }
              } else if (formValues.tokenIdentificadorInstitucion === '1') {
                nombreInstitucion = formValues.nombreInstitucionOtros || 'No especificado';
                tipoEntidad = 'Otro';
              }

              // 8. Obtener tipo de evaluación y medio de verificación
              const tipoEvaluacion = this.listaTipos.find(
                tipo => tipo.tokenIdentificador === formValues.tokenIdentificadorTipoEvaluacionSeguimiento
              )?.nombre || 'No especificado';

              const medioVerificacion = this.listaMediosVerificacion.find(
                medio => medio.tokenIdentificador === formValues.tokenIdentificadorMedioVerificacion
              )?.nombre || 'No especificado';

              // 9. Crear la solicitud para generar el PDF
              let solicitudPdf = new GeneracionPdfRequest();
              solicitudPdf.nemonico = 'FORMULARIO_EVALUACION_SEGUIMIENTO_EDUCATIVO_LABORAL';

              // 10. Configurar las variables para el PDF
              solicitudPdf.variables = {
                "[IMG_BASE64]": imagenBase64,
                "[FECHA_REGISTRO]": this.utilidades.formatearFecha(new Date()),
                "[HORA_REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                "[CENTRO]": this.centro?.nombre || fichaIdentificacion.centroIngreso || '',
                "[NOMBRES-APELLIDOS]": nombreCompleto,
                "[DNI]": fichaIdentificacion.numeroDocumento || '',
                "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
                "[EDAD]": edadActual,
                "[TIPO_EVALUACION]": tipoEvaluacion,
                "[MEDIO_VERIFICACION]": medioVerificacion,
                "[INSTITUCION]": nombreInstitucion,
                "[TIPO_ENTIDAD]": tipoEntidad,
                "[FECHA_INICIO]": formValues.fechaInicio ? this.utilidades.formatearFecha(formValues.fechaInicio) : 'No especificado',
                "[FECHA_FIN]": formValues.fechaFin ? this.utilidades.formatearFecha(formValues.fechaFin) : 'No especificado',
                "[RESULTADO_SEGUIMIENTO]": formValues.resultadoSeguimiento || 'No especificado',
                "[TABLA_RECOMENDACIONES]": JSON.stringify(tablaRecomendaciones)
              };

              // 11. Generar y mostrar el PDF
              this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                next: (respuesta: RespuestaPorDefecto<string>) => {
                  dialogoCarga.close();

                  if (!respuesta.exito) {
                    console.error('Error al generar PDF:', respuesta);
                    this.servicioMensajes.mensajeError(
                      'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                    );
                    return;
                  }

                  // Abrir el PDF en una nueva ventana
                  const url = window.URL.createObjectURL(this.utilidades.getPdfBlob(respuesta.data));
                  window.open(url);
                },
                error: (error: any) => {
                  dialogoCarga.close();
                  console.error('Error al generar PDF:', error);
                  this.servicioMensajes.mensajeError(
                    'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                  );
                }
              });
            } catch (error) {
              dialogoCarga.close();
              console.error('Error al procesar datos:', error);
              this.servicioMensajes.mensajeError(
                'Hubo un problema al procesar los datos. Inténtalo de nuevo.'
              );
            }
          }
        }
      });
    } catch (error) {
      console.error('Error al iniciar impresión:', error);
      this.servicioMensajes.mensajeError(
        'Hubo un problema al iniciar el proceso de impresión.'
      );
    }
  }
  
}