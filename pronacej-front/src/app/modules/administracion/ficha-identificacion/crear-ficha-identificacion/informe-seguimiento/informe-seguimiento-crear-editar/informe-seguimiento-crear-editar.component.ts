import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformeSeguimientoPIIDTO } from 'app/core/model/both/informeSeguimientoPIIDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PdfService } from 'app/core/services/pdf.service';
import { MatDialog } from '@angular/material/dialog';
import { InstrumentoEvaluacionDTO } from 'app/core/model/both/instrumentoEvaluacionDTO.model';
import { InformeSeguimientoPIIService } from 'app/modules/seguridad/services/informeSeguimiento.service';
import { MdRegiInfoComponent } from './md-regi-info/md-regi-info.component';
import { InformeTecnicoSustentatorioDTO } from 'app/core/model/both/informeTecnicoSustentatorioDTO.model';
import { HttpClient } from '@angular/common/http';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { TabService } from 'app/core/services/tab.service';

@Component({
  selector: 'app-informe-seguimiento-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatIconModule,
  ],
  templateUrl: './informe-seguimiento-crear-editar.component.html',
  styleUrl: './informe-seguimiento-crear-editar.component.scss'
})
export class InformeSeguimientoCrearEditarComponent {
  // Variables de identificación
  uuid_fp: string;
  informeTecnicoDTO: InformeTecnicoSustentatorioDTO;
  informeSeguimientoDTO: InformeSeguimientoPIIDTO;

  // Variables de formulario
  informeSeguimientoForm: FormGroup;

  // Variables de configuración
  tituloPantalla = "informe de seguimiento PII";
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME_SEGUIMIENTO_PII;

  // Variables para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;
  estadoVisualizar = false;

  // Datos para la tabla de instrumentos
  @ViewChild('instrumentoPag') instrumentoPag: MatPaginator;
  listaInstrumentos: InstrumentoEvaluacionDTO[] = [];
  instrumentoDS: MatTableDataSource<InstrumentoEvaluacionDTO>;
  
  // Catálogos
  listaTiposInstrumento: CatalogoDTO[] = [];
  listaNivelesRiesgo: CatalogoDTO[] = []; // Lista para catálogo de niveles de riesgo
  
  // Configuración de columnas
  columnasInstrumento: string[] = ['acciones', 'tipoInstrumento'];

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioInformeSeguimiento: InformeSeguimientoPIIService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private servicioTab: TabService,
    private servicioPdf: PdfService,
    public dialogoModal: MatDialog,
    private http: HttpClient,
    private servicioFichaIdentificacion: FichaIdentificacionService
  ) {
    this.construirForm();
  }

  ngOnInit(): void {
    this.uuid_fp = this.rutaActiva.snapshot.params['uuid_fp'];
    this.informeTecnicoDTO = history.state.informeTecnicoDTO;
    this.informeSeguimientoDTO = history.state.informeSeguimientoDTO;
    this.cargarDatosCatalogo();

    if (this.informeSeguimientoDTO) {
      this.esVisualizacion = this.informeSeguimientoDTO.esVisualizacion;
      if (this.esVisualizacion) {
        this.informeSeguimientoForm.disable();
      }
      this.esEdicion = true;
      this.empezarEdicion(this.informeSeguimientoDTO);
    } else {
      this.esEdicion = false;
    }
  }

  /**
   * Construye el formulario con validaciones
   */
  construirForm() {
    this.informeSeguimientoForm = this.constructorFormulario.group({
      motivoIngreso: ['', [Validators.required, this.validarNoEspacios()]],
      antecedentesOrganicidad: ['', [Validators.required, this.validarNoEspacios()]],
      tecnicasUtilizadas: ['', [Validators.required, this.validarNoEspacios()]],
      observacionConductual: ['', [Validators.required, this.validarNoEspacios()]],
      evaluacionPlanPsicologica: ['', [Validators.required, this.validarNoEspacios()]],
      evaluacionPlanSocial: ['', [Validators.required, this.validarNoEspacios()]],
      evaluacionPlanConductual: ['', [Validators.required, this.validarNoEspacios()]],
      evaluacionPlanFamiliar: ['', [Validators.required, this.validarNoEspacios()]],
      evaluacionPlanEducativa: ['', [Validators.required, this.validarNoEspacios()]],
      evaluacionPlanLaboral: ['', [Validators.required, this.validarNoEspacios()]],
      tokenIdentificadorNivelRiesgo: ['0', [Validators.required, Validators.pattern(/^(?!0$).*$/)]],
      conclusiones: ['', [Validators.required, this.validarNoEspacios()]],
      recomendaciones: ['', [Validators.required, this.validarNoEspacios()]],
    });
  }

  /**
   * Carga los catálogos necesarios para el formulario
   */
  cargarDatosCatalogo() {
    // Cargar catálogo de tipos de instrumento
    this.utilidades.obtenerListaCatalogo('TIPOS_INSTRUMENTO', this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaTiposInstrumento = data;
      },
      error: (error) => console.error('Error cargando tipos de instrumento:', error)
    });

    // Cargar catálogo de niveles de riesgo
    this.utilidades.obtenerListaCatalogo('NIVEL_RIESGO', this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaNivelesRiesgo = data;
      },
      error: (error) => console.error('Error cargando niveles de riesgo:', error)
    });
  }

  /**
   * Inicia la edición con los datos del informe de seguimiento
   * @param informeSeguimientoEditar Informe de seguimiento a editar
   */
  empezarEdicion(informeSeguimientoEditar: InformeSeguimientoPIIDTO) {
    this.informeSeguimientoDTO = informeSeguimientoEditar;
    console.log(informeSeguimientoEditar);
    // Primero cargar los datos básicos
    this.informeSeguimientoForm.patchValue({
      motivoIngreso: informeSeguimientoEditar.motivoIngreso,
      antecedentesOrganicidad: informeSeguimientoEditar.antecedentesOrganicidad,
      tecnicasUtilizadas: informeSeguimientoEditar.tecnicasUtilizadas,
      observacionConductual: informeSeguimientoEditar.observacionConductual,
      evaluacionPlanPsicologica: informeSeguimientoEditar.evaluacionPlanPsicologica,
      evaluacionPlanSocial: informeSeguimientoEditar.evaluacionPlanSocial,
      evaluacionPlanConductual: informeSeguimientoEditar.evaluacionPlanConductual,
      evaluacionPlanFamiliar: informeSeguimientoEditar.evaluacionPlanFamiliar,
      evaluacionPlanEducativa: informeSeguimientoEditar.evaluacionPlanEducativa,
      evaluacionPlanLaboral: informeSeguimientoEditar.evaluacionPlanLaboral,
      tokenIdentificadorNivelRiesgo: informeSeguimientoEditar.tokenIdentificadorNivelRiesgo || '0',
      conclusiones: informeSeguimientoEditar.conclusiones,
      recomendaciones: informeSeguimientoEditar.recomendaciones,
    });

    // Cargar instrumentos si existen
    if (informeSeguimientoEditar.listaInstrumentosAplicados) {
      this.listaInstrumentos = informeSeguimientoEditar.listaInstrumentosAplicados;
      this.instrumentoDS = new MatTableDataSource(this.listaInstrumentos);
      this.instrumentoDS.paginator = this.instrumentoPag;
    }
  }

  /**
   * Agrega una fila de instrumento al listado
   */
  agregarFilaInstrumento() {
    const dialogRef = this.dialogoModal.open(MdRegiInfoComponent, {
      data: {
        listaTiposInstrumento: this.listaTiposInstrumento,
      },
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaInstrumentos.unshift(resultado);
        this.instrumentoDS = new MatTableDataSource(this.listaInstrumentos);
        this.instrumentoDS.paginator = this.instrumentoPag;
      }
    });
  }

  /**
   * Edita una fila de instrumento
   * @param instrumento Instrumento a editar
   * @param indice Índice del instrumento en la lista
   */
  editarFilaInstrumento(instrumento: InstrumentoEvaluacionDTO, indice: number) {
    const dialogRef = this.dialogoModal.open(MdRegiInfoComponent, {
      data: {
        fila: instrumento,
        listaTiposInstrumento: this.listaTiposInstrumento,
      },
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        this.listaInstrumentos[indice] = result;
        this.instrumentoDS = new MatTableDataSource(this.listaInstrumentos);
        this.instrumentoDS.paginator = this.instrumentoPag;
      }
    });
  }

  /**
   * Elimina una fila de instrumento
   * @param indice Índice del instrumento a eliminar
   */
  eliminarFilaInstrumento(indice: number) {
    const elementoEliminar = this.listaInstrumentos[indice];
    if (elementoEliminar.tokenIdentificador === "0") {
      this.listaInstrumentos.splice(indice, 1);
      this.instrumentoDS = new MatTableDataSource(this.listaInstrumentos);
      this.instrumentoDS.paginator = this.instrumentoPag;
    } else {
      this.eliminarInstrumentoPorInformeSeguimiento(elementoEliminar);
    }
  }

  /**
   * Elimina un instrumento del informe de seguimiento a través del servicio
   * @param instrumento Instrumento a eliminar
   */
  eliminarInstrumentoPorInformeSeguimiento(instrumento: InstrumentoEvaluacionDTO) {
    const ref = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar el instrumento seleccionado? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    ref.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const carga = this.servicioMensajes.mensajeLoading("Eliminando el instrumento...");
          
          this.servicioInformeSeguimiento.eliminarInstrumentoPorInformeSeguimiento(instrumento).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              carga.close();
              if (!respuesta.exito) {
                this.servicioInformeSeguimiento.checkError(respuesta);
                return;
              }
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              // Recargar la lista de instrumentos
              this.listaInstrumentos = this.listaInstrumentos.filter(i => i.tokenIdentificador !== instrumento.tokenIdentificador);
              this.instrumentoDS = new MatTableDataSource(this.listaInstrumentos);
              this.instrumentoDS.paginator = this.instrumentoPag;
            },
            error: (error: any) => {
              carga.close();
              this.servicioInformeSeguimiento.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Valida que un campo no contenga sólo espacios en blanco
   * @returns Validador para el formulario
   */
  validarNoEspacios() {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const esInvalido = control?.value && control?.value?.trim().length === 0;
      return esInvalido ? { 'soloEspacios': true } : null;
    };
  }

  /**
   * Cancela la edición y regresa a la pantalla anterior
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.informeSeguimientoForm.reset();
    this.informeSeguimientoDTO = null;
    this.enrutador.navigate(['../'], { 
      relativeTo: this.rutaActiva,
      state: { informeTecnicoDTO: this.informeTecnicoDTO },
    });
    this.servicioTab.cambiarTab(1);
  }

  /**
   * Crea o actualiza el informe de seguimiento
   */
  crearActualizar() {
      // Si ya está procesando una solicitud, ignorar clicks adicionales
      if (this.estaProcesandoGuardado) {
          return;
      }
      
      // Marcar todos los campos como tocados para activar validaciones
      Object.keys(this.informeSeguimientoForm.controls).forEach(key => {
          const control = this.informeSeguimientoForm.get(key);
          control.markAsTouched();
          control.markAsDirty();
          control.updateValueAndValidity();
      });
      
      // Verificar si hay campos con error de "soloEspacios"
      let tieneEspaciosEnBlanco = false;
      Object.keys(this.informeSeguimientoForm.controls).forEach(key => {
          const control = this.informeSeguimientoForm.get(key);
          if (control.errors && control.errors['soloEspacios']) {
              tieneEspaciosEnBlanco = true;
          }
      });

      if (this.informeSeguimientoForm.invalid) {
          if (tieneEspaciosEnBlanco) {
              this.servicioMensajes.mensajeError('No se permiten campos con solo espacios en blanco.');
          } else {
              this.servicioMensajes.mensajeError('Por favor complete los campos obligatorios.');
          }
          return;
      }

      // Establecer bandera de procesamiento
      this.estaProcesandoGuardado = true;
      this.informeSeguimientoForm.disable();

      const informeSeguimiento = new InformeSeguimientoPIIDTO();
      const formValues = this.informeSeguimientoForm.getRawValue();
      
      // Limpiar espacios en blanco de los campos de texto
      Object.keys(formValues).forEach(key => {
          if (typeof formValues[key] === 'string') {
              formValues[key] = formValues[key].trim();
          }
      });
      
      Object.assign(informeSeguimiento, {
          ...formValues,
          listaInstrumentosAplicados: this.listaInstrumentos,
          tokenIdentificadorFichaIdentificacion: this.uuid_fp,
          tokenIdentificador: this.informeSeguimientoDTO?.tokenIdentificador,
          esEdicion: this.esEdicion
      });

      this.servicioInformeSeguimiento.crearInformeSeguimiento(informeSeguimiento, this.nemonicoMenu).subscribe({
          next: (respuesta: RespuestaPorDefecto<InformeSeguimientoPIIDTO>) => {
              // Restablecer bandera de procesamiento
              this.estaProcesandoGuardado = false;
              this.informeSeguimientoForm.enable();

              if (!respuesta.exito) {
                  this.servicioInformeSeguimiento.checkError(respuesta);
                  return;
              }
              
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              this.enrutador.navigate(['../'], { 
                  relativeTo: this.rutaActiva,
                  state: { informeTecnicoDTO: this.informeTecnicoDTO }
              });
              this.servicioTab.cambiarTab(1);
          },
          error: (error: any) => {
              this.servicioInformeSeguimiento.checkError(error);
              
              // Restablecer bandera de procesamiento en caso de error
              this.estaProcesandoGuardado = false;
              this.informeSeguimientoForm.enable();
          }
      });
  }

  /**
   * Imprime la ficha del informe de seguimiento
   */
  imprimirFicha() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de seguimiento PII?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión del informe de seguimiento PII...");
          
          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }
                      
                      // 5. Preparar datos del adolescente
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      
                      // 6. Obtener nombre del nivel de riesgo
                      const nivelRiesgoToken = this.informeSeguimientoForm.get('tokenIdentificadorNivelRiesgo').value;
                      const nombreNivelRiesgo = this.utilidades.obtenerNombreCatalogoPorToken(nivelRiesgoToken, this.listaNivelesRiesgo) || 'No especificado';
                      
                      // 7. Crear la solicitud para generar el PDF
                      const solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_SEGUIMIENTO_PII';
                      
                      // 8. Incluir las variables para el PDF con los nombres correctos de variables - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[MOTIVO-INGRESO]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('motivoIngreso').value || 'No especificado'),
                        "[ANTECEDENTES-ORGANICIDAD]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('antecedentesOrganicidad').value || 'No especificado'),
                        "[TECNICAS-UTILIZADAS]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('tecnicasUtilizadas').value || 'No especificado'),
                        "[OBSERVACION-CONDUCTUAL]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('observacionConductual').value || 'No especificado'),
                        "[EVALUACION-PLAN-PSICOLOGICA]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('evaluacionPlanPsicologica').value || 'No especificado'),
                        "[EVALUACION-PLAN-SOCIAL]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('evaluacionPlanSocial').value || 'No especificado'),
                        "[EVALUACION-PLAN-CONDUCTUAL]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('evaluacionPlanConductual').value || 'No especificado'),
                        "[EVALUACION-PLAN-FAMILIAR]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('evaluacionPlanFamiliar').value || 'No especificado'),
                        "[EVALUACION-PLAN-EDUCATIVA]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('evaluacionPlanEducativa').value || 'No especificado'),
                        "[EVALUACION-PLAN-LABORAL]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('evaluacionPlanLaboral').value || 'No especificado'),
                        "[NIVEL-RIESGO]": this.utilidades.escaparHTML(nombreNivelRiesgo),
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('conclusiones').value || 'No especificado'),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(this.informeSeguimientoForm.get('recomendaciones').value || 'No especificado'),
                      };
                      
                      // 9. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }
                          
                          // 10. Abrir el PDF en una nueva pestaña
                          const url = window.URL.createObjectURL(this.utilidades.getPdfBlob(respuesta.data));
                          window.open(url);
                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al generar PDF:', error);
                          this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                        }
                      });
                    },
                    error: (error: any) => {
                      dialogoCarga.close();
                      console.error('Error al obtener ficha:', error);
                      this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                    }
                  });
              },
              error: (error) => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.servicioMensajes.mensajeError('Error al cargar la imagen del logo');
              }
            });
        }
      }
    });
  }
}