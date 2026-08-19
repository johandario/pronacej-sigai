import { CommonModule, registerLocaleData } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatNativeDateModule } from '@angular/material/core';
import { ActivatedRoute, Router } from '@angular/router';
import localeEs from '@angular/common/locales/es';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { SeguimientoEducativoLaboralOtrosDTO } from 'app/core/model/both/ia/seguimientoEducativoLaboralOtrosDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { SeguimientoEducativoLaboralOtrosService } from 'app/modules/seguridad/services/seguimientoEducativoLaboralOtros.service';
import { TabService } from 'app/core/services/tab.service';

registerLocaleData(localeEs);

@Component({
  selector: 'app-segu-educ-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSlideToggleModule,
  ],
  templateUrl: './segu-educ-crear-editar.component.html',
  styleUrl: './segu-educ-crear-editar.component.scss'
})
export class SeguEducCrearEditarComponent {
  // Identificadores y propiedades
  uuid_fp: string;
  formularioSeguimientoEducativo: FormGroup;
  dtoSeguimientoEducativo: SeguimientoEducativoLaboralOtrosDTO;
  tituloPantalla = "seguimiento educativo/laboral/otros";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_EDUCATIVO_LABORAL;

  // Variable de control para evitar envíos duplicados
  estaProcesandoGuardado: boolean = false;
  
  // Estados
  esEdicion = false;
  esVisualizacion = false;

  // Catálogos y datos
  listaTiposSeguimiento: CatalogoDTO[] = [];
  centro: JerarquiaDTO;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioSeguimientoEducativoLaboral: SeguimientoEducativoLaboralOtrosService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioJerarquia: JerarquiaService,
    private enrutador: Router,
    private ruta: ActivatedRoute,
    public utilidades: FuncionesUtils,
    public servicioPdf: PdfService,
    private servicioTab: TabService,
  ) {
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
    this.dtoSeguimientoEducativo = history.state.seguimientoEducativoDTO;

    this.cargarDatosCatalogo();

    if (this.dtoSeguimientoEducativo) {
      this.esVisualizacion = this.dtoSeguimientoEducativo.esVisualizacion;
      if (this.esVisualizacion) {
        this.formularioSeguimientoEducativo.disable();
      }
      this.esEdicion = true;
      if (this.dtoSeguimientoEducativo.centro) {
        this.centro = this.dtoSeguimientoEducativo.centro;
      }
      this.iniciarEdicion(this.dtoSeguimientoEducativo);
    } else {
      this.cargarCentro();
    }
  }

  /**
   * Carga la información del centro al que pertenece el usuario
   */
  cargarCentro() {
    this.servicioJerarquia
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.servicioJerarquia.checkError(respuesta);
            return;
          }
          this.centro = respuesta.data;
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  /**
   * Construye el formulario con las validaciones correspondientes
   */
  construirFormulario() {
    this.formularioSeguimientoEducativo = this.constructorFormulario.group({
      // Programa y ambiente se mantienen en el formulario pero sin validaciones
      programa: ['0'],
      ambiente: ['0'],
      tokenIdentificadorTipoSeguimientoSocial: ['0', [Validators.required]],
      institucionVisitada: [null, [Validators.required]],
      personaEntrevistada: [null, [Validators.required]],
      direccion: [null, [Validators.required]],
      fechaSeguimiento: [new Date(), [Validators.required]],
      medioVerificacion: [null, [Validators.required]],
      resultadoSeguimiento: [null, [Validators.required]],
      sugerenciasRecomendaciones: [null, [Validators.required]]
    });
  }

  /**
   * Carga los catálogos necesarios para el formulario
   */
  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('TIPO_SEGUIMIENTO_EDUCATIVO_LABORAL', this.nemonicoMenu)
      .subscribe({
        next: (datos) => this.listaTiposSeguimiento = datos,
        error: (error) => console.error('Error al cargar tipos de seguimiento:', error)
      });
  }
  
  /**
   * Actualiza el valor de fecha en el formulario
   * @param evento Evento del datepicker
   * @param nombreControl Nombre del control a actualizar
   */
  actualizarFecha(evento: MatDatepickerInputEvent<Date>, nombreControl: string) {
    if (evento.value) {
      const fecha = evento.value;
      this.formularioSeguimientoEducativo.get(nombreControl).setValue(fecha);
    }
  }

  /**
   * Inicializa el formulario con los datos del seguimiento en modo edición
   * @param seguimientoEducativoEditar DTO con los datos del seguimiento a editar
   */
  iniciarEdicion(seguimientoEducativoEditar: SeguimientoEducativoLaboralOtrosDTO) {
    this.esEdicion = true;
    this.dtoSeguimientoEducativo = seguimientoEducativoEditar;

    // Convertir fechaSeguimiento a objeto Date
    const fechaSeguimiento = seguimientoEducativoEditar.fechaSeguimiento ? new Date(seguimientoEducativoEditar.fechaSeguimiento) : null;

    this.formularioSeguimientoEducativo.patchValue({
      programa: seguimientoEducativoEditar.programa?.tokenIdentificador || '0',
      ambiente: seguimientoEducativoEditar.ambiente?.tokenIdentificador || '0',
      tokenIdentificadorTipoSeguimientoSocial: seguimientoEducativoEditar.tokenIdentificadorTipoSeguimientoSocial,
      institucionVisitada: seguimientoEducativoEditar.institucionVisitada,
      personaEntrevistada: seguimientoEducativoEditar.personaEntrevistada,
      direccion: seguimientoEducativoEditar.direccion,
      fechaSeguimiento: fechaSeguimiento,
      medioVerificacion: seguimientoEducativoEditar.medioVerificacion,
      resultadoSeguimiento: seguimientoEducativoEditar.resultadoSeguimiento,
      sugerenciasRecomendaciones: seguimientoEducativoEditar.sugerenciasRecomendaciones
    });
  }

  /**
   * Obtiene el valor de un campo del formulario
   * @param llave Nombre del campo
   * @returns Valor del campo
   */
  private obtenerValorCampo(llave: string) {
    return this.formularioSeguimientoEducativo.get(llave)?.value;
  }

  /**
   * Cancela la edición y vuelve al listado
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.formularioSeguimientoEducativo.reset();
    this.dtoSeguimientoEducativo = null;
    this.enrutador.navigate(['../'], { relativeTo: this.ruta });
    this.servicioTab.cambiarTab(2);
  }

  /**
   * Crea o actualiza un seguimiento educativo
   */
  crearActualizar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }

    if (this.formularioSeguimientoEducativo.invalid) {
      // Mostrar mensaje al usuario sobre campos requeridos
      this.servicioMensajes.mensajeError('Por favor, complete todos los campos requeridos');
      return;
    }

    // Activar el indicador de envío y deshabilitar el formulario
    this.estaProcesandoGuardado = true;
    this.formularioSeguimientoEducativo.disable();
    
    const dialogoCarga = this.servicioMensajes.mensajeLoading(`Guardando ${this.tituloPantalla}...`);

    let seguimientoEducativo = new SeguimientoEducativoLaboralOtrosDTO();
    const valoresFormulario = this.formularioSeguimientoEducativo.getRawValue();

    Object.assign(seguimientoEducativo, {
      ...valoresFormulario,
      // Programa y ambiente se envían como nulos
      programa: null,
      ambiente: null,
      centro: this.centro,
      tokenEvaluacionSeguimiento: this.uuid_fp,
      tokenIdentificador: this.dtoSeguimientoEducativo?.tokenIdentificador,
      esEdicion: this.esEdicion
    });

    this.servicioSeguimientoEducativoLaboral.crearSeguimiento(seguimientoEducativo, this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<SeguimientoEducativoLaboralOtrosDTO>) => {
          dialogoCarga.close();
          // Restablecer estado sin importar el resultado
          this.estaProcesandoGuardado = false;
          this.formularioSeguimientoEducativo.enable();
          
          if (!respuesta.exito) {
            this.servicioSeguimientoEducativoLaboral.checkError(respuesta);
            return;
          }
          this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
          this.enrutador.navigate(['../'], { relativeTo: this.ruta });
          this.servicioTab.cambiarTab(2);
        },
        error: (error: any) => {
          dialogoCarga.close();
          // Restablecer estado en caso de error
          this.estaProcesandoGuardado = false;
          this.formularioSeguimientoEducativo.enable();
          this.servicioSeguimientoEducativoLaboral.checkError(error);
        }
      });
  }

  /**
   * Obtiene los datos de la cabecera para el PDF
   * @returns Promesa con los datos de la cabecera
   */
  private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
    return new Promise((resolver, rechazar) => {
      this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
        next: (respuesta: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!respuesta.exito) {
            rechazar('Error al obtener la ficha de identificación');
            return;
          }

          const fichaIdentificacion = respuesta.data;
          const datosCabecera = {
            "[NOMBRES-APELLIDOS]": `${fichaIdentificacion.nombres} ${fichaIdentificacion.apellidoPaterno} ${fichaIdentificacion.apellidoMaterno}`.trim(),
            "[DNI]": fichaIdentificacion.numeroDocumento || '',
            "[LUGAR-FECHA-NACIMIENTO]": `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`,
            "[EDAD]": `${this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento)}`
          };

          resolver(datosCabecera);
        },
        error: (error: any) => {
          rechazar(error);
        }
      });
    });
  }

  /**
   * Genera e imprime el PDF del seguimiento
   */
  imprimirFicha() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de imprimir el seguimiento educativo/laboral/otros?`,
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Preparando la impresión del seguimiento...`);
          
          // Si no tenemos centro y estamos en modo edición, intentamos obtenerlo del DTO
          if (!this.centro && this.dtoSeguimientoEducativo?.centro) {
            this.centro = this.dtoSeguimientoEducativo.centro;
          }
          
          // Si aún no tenemos centro, carguémoslo
          const prepararPDF = () => {
            // 3. Cargar la imagen como base64
            fetch('images/logo/logo.png')
              .then(response => response.arrayBuffer())
              .then(buffer => {
                const array = new Uint8Array(buffer);
                const cadenaCaracteres = String.fromCharCode(...array);
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
                      
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      
                      // 5. Obtener el tipo de seguimiento seleccionado
                      const tipoSeguimientoSeleccionado = this.listaTiposSeguimiento.find(t => 
                        t.tokenIdentificador === this.obtenerValorCampo("tokenIdentificadorTipoSeguimientoSocial"));
                      
                      // 6. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_EDUCATIVO_LABORAL';
                      
                      // 7. Preparar variables para la plantilla PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'Centro no especificado'),
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[ADOLESCENTE-NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[ADOLESCENTE-DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        
                        // Datos del seguimiento educativo/laboral/otros - APLICANDO escaparHTML a todos los valores
                        "[FECHA-SEGUIMIENTO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(this.obtenerValorCampo("fechaSeguimiento"))),
                        "[TIPO-SEGUIMIENTO]": this.utilidades.escaparHTML(tipoSeguimientoSeleccionado?.nombre || 'No especificado'),
                        "[INSTITUCION-VISITADA]": this.utilidades.escaparHTML(this.obtenerValorCampo("institucionVisitada") || 'No especificado'),
                        "[PERSONA-ENTREVISTADA]": this.utilidades.escaparHTML(this.obtenerValorCampo("personaEntrevistada") || 'No especificado'),
                        "[DIRECCION]": this.utilidades.escaparHTML(this.obtenerValorCampo("direccion") || 'No especificado'),
                        "[MEDIO-VERIFICACION]": this.utilidades.escaparHTML(this.obtenerValorCampo("medioVerificacion") || 'No especificado'),
                        "[RESULTADO-SEGUIMIENTO]": this.utilidades.escaparHTML(this.obtenerValorCampo("resultadoSeguimiento") || 'No especificado'),
                        "[SUGERENCIAS-RECOMENDACIONES]": this.utilidades.escaparHTML(this.obtenerValorCampo("sugerenciasRecomendaciones") || 'No especificado')
                      };
                      
                      // 8. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }
                          
                          // 9. Abrir el PDF en una nueva pestaña
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
              })
              .catch(error => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.servicioMensajes.mensajeError('Error al cargar la imagen del logo');
              });
          };
          
          // Si no tenemos centro, lo cargamos primero
          if (!this.centro) {
            this.servicioJerarquia.obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu).subscribe({
              next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
                if (respuesta.exito) {
                  this.centro = respuesta.data;
                }
                // Continuamos con la impresión independientemente del resultado
                prepararPDF();
              },
              error: (error) => {
                console.error('Error al cargar centro:', error);
                // Continuamos con la impresión aunque haya error
                prepararPDF();
              }
            });
          } else {
            // Ya tenemos el centro, procedemos directamente
            prepararPDF();
          }
        }
      }
    });
  }
}