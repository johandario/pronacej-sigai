import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { 
  AbstractControl, 
  FormBuilder, 
  FormGroup, 
  ReactiveFormsModule, 
  ValidatorFn, 
  Validators 
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { InformeEgresoPIIDTO } from 'app/core/model/both/informeEgresoPIIDTO.model';
import { InformeSeguimientoPIIDTO } from 'app/core/model/both/informeSeguimientoPIIDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { 
  CustomDateAdapter, 
  CUSTOM_DATE_FORMATS, 
  FuncionesUtils 
} from 'app/core/utils/funcionesUtils.model';
import { InformeEgresoPIIService } from 'app/modules/seguridad/services/informeEgresoPII.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { HttpClient } from '@angular/common/http';
import { TabService } from 'app/core/services/tab.service';

@Component({
  selector: 'app-informe-egreso-pii-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './informe-egreso-pii-crear-editar.component.html',
  styleUrl: './informe-egreso-pii-crear-editar.component.scss'
})
export class InformeEgresoPiiCrearEditarComponent implements OnInit {
  // Identificadores
  identificadorFichaPrincipal: string;
  identificadorInformeSeguimiento: string;

  // Variables de formulario
  formularioInformeEgreso: FormGroup;
  informeEgresoDTO: InformeEgresoPIIDTO;
  informeSeguimientoDTO: InformeSeguimientoPIIDTO;

  // Variables de configuración
  tituloPantalla = "Informe de egreso PII";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_INFORME_EGRESO_PII;

  // Variables para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioInformeEgresoPII: InformeEgresoPIIService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private ruta: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private servicioPdf: PdfService,
    private servicioTab: TabService,
    private adaptadorFecha: DateAdapter<any>,
    private http: HttpClient
  ) {
    // Configuramos el locale para el adaptador de fecha
    this.adaptadorFecha.setLocale('es');
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.ruta.snapshot.params['uuid_fp'];
    this.informeSeguimientoDTO = history.state.informeSeguimientoDTO;
    this.informeEgresoDTO = history.state.informeEgresoDTO;

    if (this.informeSeguimientoDTO) {
      this.identificadorInformeSeguimiento = this.informeSeguimientoDTO.tokenIdentificador;
    }

    if (this.informeEgresoDTO) {
      this.esVisualizacion = this.informeEgresoDTO.esVisualizacion;

      if (this.esVisualizacion) {
        this.formularioInformeEgreso.disable();
      }
      this.iniciarEdicion(this.informeEgresoDTO);
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
  construirFormulario() {
    this.formularioInformeEgreso = this.constructorFormulario.group({
      motivoIngresoPII: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionPsicologicaPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionSocialPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionConductualPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionFamiliarPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionNivelRiesgoPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionEvolucionPsicologicaPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionEvolucionSocialPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionEvolucionConductualPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionEvolucionFamiliarPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      descripcionEvolucionNivelRiesgoPlanTratamiento: [null, [Validators.required, this.validarNoEspacios()]],
      conclusiones: [null, [Validators.required, this.validarNoEspacios()]],
      recomendaciones: [null, [Validators.required, this.validarNoEspacios()]]
    });
  }

  /**
   * Obtiene el valor de un campo del formulario
   * @param clave Nombre del campo
   * @returns Valor del campo
   */
  private obtenerValor(clave: string) {
    return this.formularioInformeEgreso.get(clave)?.value;
  }

  /**
   * Inicia el modo edición con los datos proporcionados
   * @param informeEgresoEditar Datos de informe de egreso para editar
   */
  iniciarEdicion(informeEgresoEditar: InformeEgresoPIIDTO) {
    this.esEdicion = true;
    this.informeEgresoDTO = informeEgresoEditar;

    this.formularioInformeEgreso.patchValue({
      motivoIngresoPII: informeEgresoEditar.motivoIngresoPII,
      descripcionPsicologicaPlanTratamiento: informeEgresoEditar.descripcionPsicologicaPlanTratamiento,
      descripcionSocialPlanTratamiento: informeEgresoEditar.descripcionSocialPlanTratamiento,
      descripcionConductualPlanTratamiento: informeEgresoEditar.descripcionConductualPlanTratamiento,
      descripcionFamiliarPlanTratamiento: informeEgresoEditar.descripcionFamiliarPlanTratamiento,
      descripcionNivelRiesgoPlanTratamiento: informeEgresoEditar.descripcionNivelRiesgoPlanTratamiento,
      descripcionEvolucionPsicologicaPlanTratamiento: informeEgresoEditar.descripcionEvolucionPsicologicaPlanTratamiento,
      descripcionEvolucionSocialPlanTratamiento: informeEgresoEditar.descripcionEvolucionSocialPlanTratamiento,
      descripcionEvolucionConductualPlanTratamiento: informeEgresoEditar.descripcionEvolucionConductualPlanTratamiento,
      descripcionEvolucionFamiliarPlanTratamiento: informeEgresoEditar.descripcionEvolucionFamiliarPlanTratamiento,
      descripcionEvolucionNivelRiesgoPlanTratamiento: informeEgresoEditar.descripcionEvolucionNivelRiesgoPlanTratamiento,
      conclusiones: informeEgresoEditar.conclusiones,
      recomendaciones: informeEgresoEditar.recomendaciones
    });
  }

  /**
   * Crea o actualiza el informe de egreso
   */
  crearActualizar() {
      // Si ya está procesando una solicitud, ignorar clicks adicionales
      if (this.estaProcesandoGuardado) {
          return;
      }
      
      // Marcar todos los campos como tocados para activar validaciones
      Object.keys(this.formularioInformeEgreso.controls).forEach(key => {
          const control = this.formularioInformeEgreso.get(key);
          control.markAsTouched();
          control.markAsDirty();
          control.updateValueAndValidity();
      });
    
      // Verificar si hay campos con error de "soloEspacios"
      let tieneEspaciosEnBlanco = false;
      Object.keys(this.formularioInformeEgreso.controls).forEach(key => {
          const control = this.formularioInformeEgreso.get(key);
          if (control.errors && control.errors['soloEspacios']) {
              tieneEspaciosEnBlanco = true;
          }
      });
    
      if (this.formularioInformeEgreso.invalid) {
          if (tieneEspaciosEnBlanco) {
              this.servicioMensajes.mensajeError('No se permiten campos con solo espacios en blanco.');
          } else {
              this.servicioMensajes.mensajeError('Por favor complete los campos obligatorios.');
          }
          return;
      }
    
      // Establecer bandera de procesamiento
      this.estaProcesandoGuardado = true;
      this.formularioInformeEgreso.disable();

      const informeEgreso = new InformeEgresoPIIDTO();
      const datosFormulario = this.formularioInformeEgreso.getRawValue();
    
      // Limpiar espacios en blanco de los campos de texto
      Object.keys(datosFormulario).forEach(key => {
          if (typeof datosFormulario[key] === 'string') {
              datosFormulario[key] = datosFormulario[key].trim();
          }
      });

      Object.assign(informeEgreso, {
          ...datosFormulario,
          tokenIdentificadorFichaIdentificacion: this.identificadorFichaPrincipal,
          tokenIdentificadorInformeSeguimientoPII: this.identificadorInformeSeguimiento,
          tokenIdentificador: this.informeEgresoDTO?.tokenIdentificador,
          esEdicion: this.esEdicion,
      });

      this.servicioInformeEgresoPII.crearInformeEgreso(informeEgreso, this.nemonicoMenu).subscribe({
          next: (respuesta: RespuestaPorDefecto<InformeEgresoPIIDTO>) => {
              // Restablecer bandera de procesamiento
              this.estaProcesandoGuardado = false;
              this.formularioInformeEgreso.enable();

              if (!respuesta.exito) {
                  this.servicioInformeEgresoPII.checkError(respuesta);
                  return;
              }
              
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              this.enrutador.navigate(['../'], { 
                  relativeTo: this.ruta,
                  queryParams: { tab: 2 }
              });
              this.servicioTab.cambiarTab(2);
          },
          error: (error: any) => {
              this.servicioInformeEgresoPII.checkError(error);
              
              // Restablecer bandera de procesamiento en caso de error
              this.estaProcesandoGuardado = false;
              this.formularioInformeEgreso.enable();
          }
      });
  }

  /**
   * Cancela la edición y regresa a la vista anterior
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.formularioInformeEgreso.reset();
    this.informeEgresoDTO = null;
    this.enrutador.navigate(['../'], { 
      relativeTo: this.ruta,
      queryParams: { tab: 2 }
    });
    this.servicioTab.cambiarTab(2);
  }

  /**
   * Genera e imprime el informe de egreso PII en formato PDF
   */
  imprimirFicha() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de egreso PII?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión del informe de egreso PII...");
          
          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.identificadorFichaPrincipal, this.nemonicoMenu)
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
                      
                      // 5. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_EGRESO_PII';
                      
                      // 6. Incluir las variables para el PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[MOTIVO-INGRESO-PII]": this.utilidades.escaparHTML(this.obtenerValor("motivoIngresoPII") || ''),
                        "[DESCRIPCION-PSICOLOGICA]": this.utilidades.escaparHTML(this.obtenerValor("descripcionPsicologicaPlanTratamiento") || ''),
                        "[DESCRIPCION-SOCIAL]": this.utilidades.escaparHTML(this.obtenerValor("descripcionSocialPlanTratamiento") || ''),
                        "[DESCRIPCION-CONDUCTUAL]": this.utilidades.escaparHTML(this.obtenerValor("descripcionConductualPlanTratamiento") || ''),
                        "[DESCRIPCION-FAMILIAR]": this.utilidades.escaparHTML(this.obtenerValor("descripcionFamiliarPlanTratamiento") || ''),
                        "[DESCRIPCION-NIVEL-RIESGO]": this.utilidades.escaparHTML(this.obtenerValor("descripcionNivelRiesgoPlanTratamiento") || ''),
                        "[EVOLUCION-PSICOLOGICA]": this.utilidades.escaparHTML(this.obtenerValor("descripcionEvolucionPsicologicaPlanTratamiento") || ''),
                        "[EVOLUCION-SOCIAL]": this.utilidades.escaparHTML(this.obtenerValor("descripcionEvolucionSocialPlanTratamiento") || ''),
                        "[EVOLUCION-CONDUCTUAL]": this.utilidades.escaparHTML(this.obtenerValor("descripcionEvolucionConductualPlanTratamiento") || ''),
                        "[EVOLUCION-FAMILIAR]": this.utilidades.escaparHTML(this.obtenerValor("descripcionEvolucionFamiliarPlanTratamiento") || ''),
                        "[EVOLUCION-NIVEL-RIESGO]": this.utilidades.escaparHTML(this.obtenerValor("descripcionEvolucionNivelRiesgoPlanTratamiento") || ''),
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(this.obtenerValor("conclusiones") || ''),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(this.obtenerValor("recomendaciones") || '')
                      };
                      
                      // 7. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }
                          
                          // 8. Abrir el PDF en una nueva pestaña
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