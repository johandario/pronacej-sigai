import { CommonModule, registerLocaleData } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { InformeTecnicoSustentatorioDTO } from 'app/core/model/both/informeTecnicoSustentatorioDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { InformeTecnicoSustentatorioService } from 'app/modules/seguridad/services/informeTecnicoSustentatorio.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import localeEs from '@angular/common/locales/es';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { TabService } from 'app/core/services/tab.service';

// Registrar el locale español para formateo de fechas
registerLocaleData(localeEs);

@Component({
  selector: 'app-informe-tecnico-sustentatorio-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
  ],
  // Configuración para el manejo de fechas en formato español
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './informe-tecnico-sustentatorio-crear-editar.component.html',
  styleUrl: './informe-tecnico-sustentatorio-crear-editar.component.scss'
})
export class InformeTecnicoSustentatorioCrearEditarComponent {
  // Variables de identificación
  uuid_fp: string;

  // Variables de entidad
  centro: JerarquiaDTO;
  informeTecnicoForm: FormGroup;
  informeTecnicoDTO: InformeTecnicoSustentatorioDTO;

  // Variables de configuración
  tituloPantalla = "Informe técnico sustentatorio";
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME_TECNICO_SUSTENTATORIO;

  // Variables para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioInformeTecnico: InformeTecnicoSustentatorioService,
    private servicioJerarquia: JerarquiaService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private ruta: ActivatedRoute,
    public utilidades: FuncionesUtils,
    public servicioPdf: PdfService,
    private servicioTab: TabService,
    private adaptadorFecha: DateAdapter<any>,
    private http: HttpClient,
  ) {
    // Configuramos el locale para el adaptador de fecha
    this.adaptadorFecha.setLocale('es');
    this.construirForm();
  }

  ngOnInit(): void {
    this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
    this.informeTecnicoDTO = history.state.informeTecnicoDTO;
    this.cargarCentro();

    if (this.informeTecnicoDTO) {
      this.esVisualizacion = this.informeTecnicoDTO.esVisualizacion;

      if (this.esVisualizacion) {
        this.informeTecnicoForm.disable();
      }
      this.esEdicion = true;
      this.empezarEdicion(this.informeTecnicoDTO);
    }
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   * @returns Validador personalizado
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      // Si el valor es nulo o undefined, no hay error de espacios
      if (control.value === null || control.value === undefined) {
        return null;
      }
     
      // Si es string, verificar que no sea solo espacios
      if (typeof control.value === 'string') {
        return control.value.trim().length === 0 && control.value.length > 0 ? { 'soloEspacios': true } : null;
      }
     
      return null;
    };
  }

  /**
   * Construye el formulario con las validaciones correspondientes
   */
  construirForm() {
    this.informeTecnicoForm = this.constructorFormulario.group({
      motivo: ['', [Validators.required, this.validarNoEspacios()]],
      criteriosSeleccion: ['', [Validators.required, this.validarNoEspacios()]],
      analisisPsicologico: ['', [Validators.required, this.validarNoEspacios()]],
      analisisSocial: ['', [Validators.required, this.validarNoEspacios()]],
      analisisConductual: ['', [Validators.required, this.validarNoEspacios()]],
      analisisFamiliar: ['', [Validators.required, this.validarNoEspacios()]],
      propuestaActividadFormativa: ['', [Validators.required, this.validarNoEspacios()]],
      importanciaParticipacionAdolescente: ['', [Validators.required, this.validarNoEspacios()]],
      objetivosConseguir: ['', [Validators.required, this.validarNoEspacios()]],
      duracion: [null, [Validators.required]],
      conclusiones: ['', [Validators.required, this.validarNoEspacios()]],
      recomendaciones: ['', [Validators.required, this.validarNoEspacios()]],
    });
  }

  /**
   * Actualiza el valor de la fecha en el formulario cuando cambia en el datepicker
   * @param event Evento de cambio de fecha del datepicker
   * @param controlName Nombre del control del formulario a actualizar
   */
  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = new Date(event.value);
      this.informeTecnicoForm.get(controlName).setValue(fecha);
    }
  }

  /**
   * Obtiene el valor de un campo del formulario
   * @param clave Nombre del campo
   * @returns Valor del campo
   */
  private obtenerValor(clave: string) {    
    return this.informeTecnicoForm.get(clave)?.value;
  }

  /**
   * Carga información del centro al que pertenece el usuario
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

          if (!environment.production) {
            console.log('Centro cargado:', this.centro);
          }
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        }
      });
  }

  /**
   * Inicia el modo edición con los datos proporcionados
   * @param informeTecnicoEditar Datos del informe técnico para editar
   */
  empezarEdicion(informeTecnicoEditar: InformeTecnicoSustentatorioDTO) {
    this.informeTecnicoForm.patchValue({
      motivo: informeTecnicoEditar.motivo,
      criteriosSeleccion: informeTecnicoEditar.criteriosSeleccion,
      analisisPsicologico: informeTecnicoEditar.analisisPsicologico,
      analisisSocial: informeTecnicoEditar.analisisSocial,
      analisisConductual: informeTecnicoEditar.analisisConductual,
      analisisFamiliar: informeTecnicoEditar.analisisFamiliar,
      propuestaActividadFormativa: informeTecnicoEditar.propuestaActividadFormativa,
      importanciaParticipacionAdolescente: informeTecnicoEditar.importanciaParticipacionAdolescente,
      objetivosConseguir: informeTecnicoEditar.objetivosConseguir,
      duracion: this.utilidades.convertirDecimalATiempo(informeTecnicoEditar.duracion),
      conclusiones: informeTecnicoEditar.conclusiones,
      recomendaciones: informeTecnicoEditar.recomendaciones,
    });
  }

  /**
   * Cancela la edición y regresa a la vista anterior
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.informeTecnicoForm.reset();
    this.informeTecnicoDTO = null;
    this.enrutador.navigate(['../'], { relativeTo: this.ruta });
    this.servicioTab.cambiarTab(0);
  }

  /**
   * Crea o actualiza el informe técnico según los datos del formulario
   */
  crearActualizar() {
      // Si ya está procesando una solicitud, ignorar clicks adicionales
      if (this.estaProcesandoGuardado) {
          return;
      }
      
      // Marcar todos los campos como tocados para activar validaciones
      Object.keys(this.informeTecnicoForm.controls).forEach(key => {
          const control = this.informeTecnicoForm.get(key);
          control.markAsTouched();
          control.markAsDirty();
          control.updateValueAndValidity();
      });
      
      // Verificar si hay campos con error de "soloEspacios"
      let tieneEspaciosEnBlanco = false;
      Object.keys(this.informeTecnicoForm.controls).forEach(key => {
          const control = this.informeTecnicoForm.get(key);
          if (control.errors && control.errors['soloEspacios']) {
              tieneEspaciosEnBlanco = true;
          }
      });
      
      if (this.informeTecnicoForm.invalid) {
          if (tieneEspaciosEnBlanco) {
              this.servicioMensajes.mensajeError('No se permiten campos con solo espacios en blanco.');
          } else {
              this.servicioMensajes.mensajeError('Por favor complete los campos obligatorios.');
          }
          return;
      }
      
      // Establecer bandera de procesamiento
      this.estaProcesandoGuardado = true;
      this.informeTecnicoForm.disable();

      const informeTecnico = new InformeTecnicoSustentatorioDTO();
      const valoresFormulario = this.informeTecnicoForm.getRawValue();
      
      // Limpiar espacios en blanco de los campos de texto
      Object.keys(valoresFormulario).forEach(key => {
          if (typeof valoresFormulario[key] === 'string') {
              valoresFormulario[key] = valoresFormulario[key].trim();
          }
      });
      
      Object.assign(informeTecnico, {
          ...valoresFormulario,
          duracion: this.utilidades.convertirTiempoADecimal(valoresFormulario.duracion),
          tokenIdentificadorFichaIdentificacion: this.uuid_fp,
          tokenIdentificador: this.informeTecnicoDTO?.tokenIdentificador,
          centro: this.centro,
          esEdicion: this.esEdicion
      });

      this.servicioInformeTecnico.crearInformeTecnico(informeTecnico, this.nemonicoMenu).subscribe({
          next: (respuesta: RespuestaPorDefecto<InformeTecnicoSustentatorioDTO>) => {
              // Restablecer bandera de procesamiento
              this.estaProcesandoGuardado = false;
              this.informeTecnicoForm.enable();

              if (!respuesta.exito) {
                  this.servicioInformeTecnico.checkError(respuesta);
                  return;
              }
              
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              this.enrutador.navigate(['../'], { relativeTo: this.ruta });
              this.servicioTab.cambiarTab(0);
          },
          error: (error: any) => {
              this.servicioInformeTecnico.checkError(error);
              
              // Restablecer bandera de procesamiento en caso de error
              this.estaProcesandoGuardado = false;
              this.informeTecnicoForm.enable();
          }
      });
  }

  /**
   * Genera e imprime el informe técnico sustentatorio en formato PDF
   */
  imprimirFicha() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe técnico sustentatorio?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión del informe técnico sustentatorio...");
          
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
                      
                      // 5. Preparar los datos personales del adolescente
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      
                      // 6. Crear solicitud para el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_TECNICO_SUSTENTATORIO';
                      
                      // 7. Incluir todas las variables para el PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        // Datos de cabecera
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'Centro no especificado'),
                        
                        // Datos del adolescente
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        
                        // Datos del informe
                        "[MOTIVO]": this.utilidades.escaparHTML(this.obtenerValor("motivo") || 'No especificado'),
                        "[CRITERIOS-SELECCION]": this.utilidades.escaparHTML(this.obtenerValor("criteriosSeleccion") || 'No especificado'),
                        "[DURACION]": this.utilidades.escaparHTML(this.obtenerValor("duracion") || 'No especificado'),
                        
                        // Análisis multidisciplinario
                        "[ANALISIS-PSICOLOGICO]": this.utilidades.escaparHTML(this.obtenerValor("analisisPsicologico") || 'No especificado'),
                        "[ANALISIS-SOCIAL]": this.utilidades.escaparHTML(this.obtenerValor("analisisSocial") || 'No especificado'),
                        "[ANALISIS-CONDUCTUAL]": this.utilidades.escaparHTML(this.obtenerValor("analisisConductual") || 'No especificado'),
                        "[ANALISIS-FAMILIAR]": this.utilidades.escaparHTML(this.obtenerValor("analisisFamiliar") || 'No especificado'),
                        
                        // Propuesta y objetivos
                        "[PROPUESTA-ACTIVIDAD]": this.utilidades.escaparHTML(this.obtenerValor("propuestaActividadFormativa") || 'No especificado'),
                        "[IMPORTANCIA-PARTICIPACION]": this.utilidades.escaparHTML(this.obtenerValor("importanciaParticipacionAdolescente") || 'No especificado'),
                        "[OBJETIVOS-CONSEGUIR]": this.utilidades.escaparHTML(this.obtenerValor("objetivosConseguir") || 'No especificado'),
                        
                        // Conclusiones y recomendaciones
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(this.obtenerValor("conclusiones") || 'No especificado'),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(this.obtenerValor("recomendaciones") || 'No especificado')
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
                      console.error('Error al obtener ficha de identificación:', error);
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