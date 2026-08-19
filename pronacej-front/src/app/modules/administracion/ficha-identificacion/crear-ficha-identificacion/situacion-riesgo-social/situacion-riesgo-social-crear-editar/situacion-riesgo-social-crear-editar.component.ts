import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { SituacionRiesgoSocialDTO } from 'app/core/model/both/situacionRiesgoSocialDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { TabService } from 'app/core/services/tab.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { SituacionRiesgoSocialService } from 'app/modules/seguridad/services/situacionRiesgoSocial.service';

@Component({
  selector: 'app-situacion-riesgo-social-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatRadioModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './situacion-riesgo-social-crear-editar.component.html',
  styleUrl: './situacion-riesgo-social-crear-editar.component.scss'
})
export class SituacionRiesgoSocialCrearEditarComponent {
  // Variables de identificación
  identificadorFichaPrincipal: string;

  centro: JerarquiaDTO;

  // Variables de formulario
  formularioSituacionRiesgoSocial: FormGroup;
  entidadSituacionRiesgoSocial: SituacionRiesgoSocialDTO;

  // Variables de configuración
  tituloPantalla = "situación de riesgo social";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SITUACION_RIESGO_SOCIAL;

  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;

  // Variables para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioSituacionRiesgoSocial: SituacionRiesgoSocialService,
    private servicioJerarquia: JerarquiaService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    private servicioTab: TabService,
    public utilidades: FuncionesUtils,
    public servicioPdf: PdfService,
    private http: HttpClient,
  ) {
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarCentro(); 
    this.entidadSituacionRiesgoSocial = history.state.situacionRiesgoSocialDTO;
    
    if (this.entidadSituacionRiesgoSocial) {
      this.esVisualizacion = this.entidadSituacionRiesgoSocial.esVisualizacion;

      if (this.esVisualizacion) {
        this.formularioSituacionRiesgoSocial.disable();
      }
      
      this.empezarEdicion(this.entidadSituacionRiesgoSocial);
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
        return control.value.trim().length === 0 ? { 'soloEspacios': true } : null;
      }
      
      return null;
    };
  }


  /**
   * Carga la información del centro de rehabilitación
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
        }
      });
  }

  /**
   * Construye el formulario con validaciones
   */
  construirFormulario() {
    this.formularioSituacionRiesgoSocial = this.constructorFormulario.group({
      antecedentesDelictivos: [null, [Validators.required, this.validarNoEspacios()]],
      manifestacionesInfractoras: [null, [Validators.required, this.validarNoEspacios()]],
      evasionHogar: [null, [Validators.required]],
      estadoSaludGeneral: [null, [Validators.required, this.validarNoEspacios()]],
      problemasLegales: [null, [Validators.required, this.validarNoEspacios()]],
      observaciones: [null, [Validators.required, this.validarNoEspacios()]],
    });
  }

  /**
   * Obtiene el valor de un campo del formulario
   * @param clave Nombre del campo
   * @returns Valor del campo
   */
  private obtenerValor(clave: string) {    
    return this.formularioSituacionRiesgoSocial.get(clave)?.value;
  }

  /**
   * Configura el formulario para edición
   * @param situacionRiesgoSocialEditar Datos para editar
   */
  empezarEdicion(situacionRiesgoSocialEditar: SituacionRiesgoSocialDTO) {
    this.esEdicion = true;
    this.entidadSituacionRiesgoSocial = situacionRiesgoSocialEditar;
    
    this.formularioSituacionRiesgoSocial.patchValue({
      antecedentesDelictivos: situacionRiesgoSocialEditar.anteDeliFami,
      manifestacionesInfractoras: situacionRiesgoSocialEditar.primManiInfrAdol,
      evasionHogar: situacionRiesgoSocialEditar.evasionHogar ? "S" : "N",
      estadoSaludGeneral: situacionRiesgoSocialEditar.estadoSaludGeneral,
      problemasLegales: situacionRiesgoSocialEditar.problemasLegales,
      observaciones: situacionRiesgoSocialEditar.observaciones
    });
  }

  /**
   * Cancela la edición y regresa a la pantalla anterior
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.formularioSituacionRiesgoSocial.reset();
    this.entidadSituacionRiesgoSocial = null;

    this.servicioTab.cambiarTab(3);
    this.enrutador.navigate(['../'], { relativeTo: this.rutaActiva });
  }

  /**
   * Crea o actualiza el registro
   */
  crearActualizar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }

    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioSituacionRiesgoSocial.controls).forEach(key => {
      const control = this.formularioSituacionRiesgoSocial.get(key);
      control.markAsTouched();
      control.markAsDirty();
      control.updateValueAndValidity();
      this.servicioTab.cambiarTab(3);
    });
    
    // Verificar espacios en blanco en todos los campos de texto
    let tieneEspaciosEnBlanco = false;
    
    Object.keys(this.formularioSituacionRiesgoSocial.controls).forEach(key => {
      const control = this.formularioSituacionRiesgoSocial.get(key);
      const valor = control.value;
      
      if (typeof valor === 'string' && valor !== null && valor !== undefined) {
        if (valor.trim().length === 0 && valor.length > 0) {
          tieneEspaciosEnBlanco = true;
          control.setErrors({ 'soloEspacios': true });
        }
      }
    });
    
    if (this.formularioSituacionRiesgoSocial.invalid || tieneEspaciosEnBlanco) {
      this.servicioMensajes.mensajeError('Por favor, corrija los campos marcados en rojo antes de guardar.');
      return;
    }

    // Activar el indicador de envío y deshabilitar el formulario
    this.estaProcesandoGuardado = true;
    this.formularioSituacionRiesgoSocial.disable();

    const situacionRiesgoSocial = new SituacionRiesgoSocialDTO();
    situacionRiesgoSocial.anteDeliFami = this.obtenerValor("antecedentesDelictivos").trim();
    situacionRiesgoSocial.primManiInfrAdol = this.obtenerValor("manifestacionesInfractoras").trim();
    situacionRiesgoSocial.evasionHogar = this.obtenerValor("evasionHogar") === "S";
    situacionRiesgoSocial.estadoSaludGeneral = this.obtenerValor("estadoSaludGeneral").trim();
    situacionRiesgoSocial.problemasLegales = this.obtenerValor("problemasLegales").trim();
    situacionRiesgoSocial.observaciones = this.obtenerValor("observaciones").trim();

    situacionRiesgoSocial.tokenIdentificadorFichaIdentificacion = this.identificadorFichaPrincipal;
    situacionRiesgoSocial.tokenIdentificador = this.entidadSituacionRiesgoSocial?.tokenIdentificador;
    situacionRiesgoSocial.esEdicion = this.esEdicion;
    
    this.servicioSituacionRiesgoSocial.crearSituacionRiesgoSocial(
      situacionRiesgoSocial, 
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<SituacionRiesgoSocialDTO>) => {
        // Resetear el estado al finalizar, sin importar el resultado
        this.estaProcesandoGuardado = false;
        this.formularioSituacionRiesgoSocial.enable();

        if (!respuesta.exito) {
          this.servicioSituacionRiesgoSocial.checkError(respuesta);
          return;
        }
        
        this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
        this.enrutador.navigate(['../'], { relativeTo: this.rutaActiva });
      },
      error: (error: any) => {
        // Resetear el estado en caso de error
        this.estaProcesandoGuardado = false;
        this.formularioSituacionRiesgoSocial.enable();
        this.servicioSituacionRiesgoSocial.checkError(error);
      }
    });
  }

  /**
   * Genera e imprime la ficha en formato PDF
   */
  imprimirFicha() {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir la situación de riesgo social?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión de situación de riesgo social...");
          
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // Obtener información del adolescente
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
                      
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SITUACION_RIESGO_SOCIAL';
                      
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'No especificado'),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto || 'No especificado'),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || 'No especificado'),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento || 'No especificado'),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual || 'No especificado'),
                        "[ANTECEDENTES-DELICTIVOS-FAMILIARES]": this.utilidades.escaparHTML(this.obtenerValor("antecedentesDelictivos") || 'No proporcionado'),
                        "[PRIMERA-MANIFESTACION-INFRACCION]": this.utilidades.escaparHTML(this.obtenerValor("manifestacionesInfractoras") || 'No proporcionado'),
                        "[EVASION-HOGAR]": this.obtenerValor("evasionHogar") === "S" ? 'Sí' : 'No',
                        "[ESTADO-SALUD-GENERAL]": this.utilidades.escaparHTML(this.obtenerValor("estadoSaludGeneral") || 'No proporcionado'),
                        "[PROBLEMAS-LEGALES]": this.utilidades.escaparHTML(this.obtenerValor("problemasLegales") || 'No proporcionado'),
                        "[OBSERVACIONES]": this.utilidades.escaparHTML(this.obtenerValor("observaciones") || 'No proporcionado')
                      };
                      
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }
                          
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