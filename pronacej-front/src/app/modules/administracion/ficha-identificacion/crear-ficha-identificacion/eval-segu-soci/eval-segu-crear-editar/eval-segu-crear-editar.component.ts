import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { SeguimientoSocialDTO } from 'app/core/model/both/ia/SeguimientoSocialDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { SeguimientoSocialService } from 'app/modules/administracion/services/seguimientoSocial.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { environment } from 'environments/environment';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { CommonModule, registerLocaleData } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { TabService } from 'app/core/services/tab.service';
import localeEs from '@angular/common/locales/es';

// Registrar el locale español para formateo de fechas
registerLocaleData(localeEs);

@Component({
  selector: 'app-eval-segu-soci-crear-editar',
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
  // Configuración para el manejo de fechas en formato español
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './eval-segu-crear-editar.component.html',
  styleUrl: './eval-segu-crear-editar.component.scss'
})
export class EvalSeguSociCrearEditarComponent {
  // Identificadores
  uuid_fp: string;
  formularioSeguimientoSocial: FormGroup;
  seguimientoSocialDTO: SeguimientoSocialDTO;
  
  // Configuración
  tituloPantalla = "seguimiento social";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_SOCIAL;

  
  // Variable de control para evitar envíos duplicados
  estaProcesandoGuardado: boolean = false;
  
  // Estados
  esEdicion = false;
  esVisualizacion = false;
  programaSeleccionado = false;
  tipoCentro: string = 'CJDR'; // Valor por defecto - CJDR, SOA o UAPICE

  // Datos
  listaTiposActividadSocial: CatalogoDTO[] = [];
  listaProgramas: JerarquiaDTO[] = [];
  listaAmbientes: JerarquiaDTO[] = [];
  centro: JerarquiaDTO;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioSeguimientoSocial: SeguimientoSocialService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioJerarquia: JerarquiaService,
    private enrutador: Router,
    private ruta: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private servicioTab: TabService,
    public servicioPdf: PdfService,
    private http: HttpClient,
    private adaptadorFecha: DateAdapter<any>,
  ) {
    // Configuramos el locale para el adaptador de fecha
    this.adaptadorFecha.setLocale('es');
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
    
    // Obtener parámetros del history.state
    this.seguimientoSocialDTO = history.state.seguimientoSocialDTO;
    this.tipoCentro = history.state.tipoCentro || 'CJDR';
    this.centro = history.state.centro;

    // Log para depuración en ambiente no productivo
    if (!environment.production) {
      console.log('Tipo de centro recibido:', this.tipoCentro);
      console.log('Centro recibido:', this.centro);
    }

    this.cargarDatosCatalogo();

    if (this.seguimientoSocialDTO) {
      this.esVisualizacion = this.seguimientoSocialDTO.esVisualizacion;
      if (this.esVisualizacion) {
        this.formularioSeguimientoSocial.disable();
      }
      this.esEdicion = true;
      if (this.seguimientoSocialDTO.centro) {
        this.centro = this.seguimientoSocialDTO.centro;
        this.verificarTipoCentro();
        if (this.debeVerProgramaYAmbiente()) {
          this.cargarProgramas(this.centro.tokenIdentificador);
        }
      }
      this.empezarEdicion(this.seguimientoSocialDTO);
    } else {
      // Si no tenemos centro del history.state, lo cargamos
      if (!this.centro) {
        this.cargarCentro();
      } else {
        this.verificarTipoCentro();
        if (this.debeVerProgramaYAmbiente()) {
          this.cargarProgramas(this.centro.tokenIdentificador);
        }
      }
    }

    // Ajustar validaciones después de determinar el tipo de centro
    this.ajustarValidacionesFormulario();
  }

  /**
   * Determina si se deben mostrar campos de programa y ambiente según el tipo de centro
   */
  debeVerProgramaYAmbiente(): boolean {
    return this.tipoCentro === 'CJDR';
  }

  /**
   * Verifica el tipo de centro y actualiza la variable tipoCentro
   */
  verificarTipoCentro() {
    // Si ya tenemos el tipo de centro definido, no hacemos nada
    if (this.tipoCentro !== 'CJDR') {
      return;
    }

    // Detectar el tipo de centro por el nemonico o nombre del centro
    if (this.centro?.jerarquiaPadre?.nemonico === 'SOA') {
      this.tipoCentro = 'SOA';
    } else if (this.centro?.nemonico === 'UAPISE' || 
              (this.centro?.nombre && this.centro.nombre.includes('UAPISE')) || 
              (this.centro?.jerarquiaPadre?.nemonico === 'UAPISE')) {
      this.tipoCentro = 'UAPICE';
    } else {
      this.tipoCentro = 'CJDR'; // Por defecto
    }

    // Debug para verificar el tipo de centro
    if (!environment.production) {
      console.log('Tipo de centro detectado:', this.tipoCentro);
      console.log('Centro:', this.centro);
    }
  }

  /**
   * Ajusta las validaciones del formulario según el tipo de centro
   */
  ajustarValidacionesFormulario() {
    if (!this.debeVerProgramaYAmbiente()) {
      // Si no debe ver programa y ambiente (SOA o UAPICE), quitar validadores
      this.formularioSeguimientoSocial.get('programa').clearValidators();
      this.formularioSeguimientoSocial.get('ambiente').clearValidators();
      this.formularioSeguimientoSocial.get('programa').updateValueAndValidity();
      this.formularioSeguimientoSocial.get('ambiente').updateValueAndValidity();
    }
  }

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
          this.verificarTipoCentro();
          this.ajustarValidacionesFormulario();
          
          // Cargar programas solo si debe ver programa y ambiente
          if (this.debeVerProgramaYAmbiente()) {
            this.cargarProgramas(this.centro.tokenIdentificador);
          }
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
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

  construirFormulario() {
    this.formularioSeguimientoSocial = this.constructorFormulario.group({
      programa: ['0', [Validators.required]],
      ambiente: ['0'],
      nemonicoTipoActividadSocial: ['0', [Validators.required]],
      descripcionSocial: [null, [Validators.required, this.validarNoEspacios()]],
      accionesAdoptadas: [null, [Validators.required, this.validarNoEspacios()]],
      comentarios: [null, [Validators.required, this.validarNoEspacios()]],
      fecha: [new Date(), [Validators.required]], // Cambiado a objeto Date directamente
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
      this.formularioSeguimientoSocial.get(controlName).setValue(fecha);
    }
  }

  observadorCambioEnCampo(campo: string, evento: any) {
    if (campo === 'programa') {
      const tokenPrograma = evento.value;
      if (tokenPrograma && tokenPrograma !== '0') {
        this.cargarAmbientes(tokenPrograma);
        this.programaSeleccionado = true;
      } else {
        this.programaSeleccionado = false;
      }
      this.formularioSeguimientoSocial.get('ambiente').setValue('0');
    }
  }

  formularioValido(): boolean {
    if (!this.debeVerProgramaYAmbiente()) {
      // Si no debe ver programa y ambiente (SOA o UAPICE), solo validar tipo de actividad
      const tipoActividad = this.formularioSeguimientoSocial.get('nemonicoTipoActividadSocial').value;
      return tipoActividad !== '0';
    } else {
      // Si debe ver programa y ambiente (CJDR), validar programa y tipo de actividad
      const programa = this.formularioSeguimientoSocial.get('programa').value;
      const tipoActividad = this.formularioSeguimientoSocial.get('nemonicoTipoActividadSocial').value;
      return programa !== '0' && tipoActividad !== '0';
    }
  }

  cargarProgramas(tokenCentro: string) {
    this.servicioJerarquia.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenCentro)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (respuesta.exito) {
            this.listaProgramas = respuesta.data;
          }
        },
        error: (error) => this.servicioJerarquia.checkError(error)
      });
  }

  cargarAmbientes(tokenPrograma: string) {
    this.servicioJerarquia.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenPrograma)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (respuesta.exito) {
            this.listaAmbientes = respuesta.data;
          }
        },
        error: (error) => this.servicioJerarquia.checkError(error)
      });
  }

  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('TIPO_ACTIVIDAD_SOCIAL', this.nemonicoMenu)
      .subscribe({
        next: (datos) => this.listaTiposActividadSocial = datos,
        error: (error) => console.error('Error al cargar tipos de actividad social:', error)
      });
  }

  empezarEdicion(seguimientoSocialEditar: SeguimientoSocialDTO) {
    this.esEdicion = true;
    this.seguimientoSocialDTO = seguimientoSocialEditar;
    
    if (!environment.production) {
      console.log('Datos para edición:', this.seguimientoSocialDTO);
    }

    // Solo cargar ambientes si debe ver programa y ambiente y si tiene programa
    if (seguimientoSocialEditar.programa && this.debeVerProgramaYAmbiente()) {
      this.programaSeleccionado = true;
      this.cargarAmbientes(seguimientoSocialEditar.programa.tokenIdentificador);
    }

    // Conversión correcta de fechas para el datepicker
    const fecha = seguimientoSocialEditar.fecha ? new Date(seguimientoSocialEditar.fecha) : new Date();

    this.formularioSeguimientoSocial.patchValue({
      programa: seguimientoSocialEditar.programa?.tokenIdentificador || '0',
      ambiente: seguimientoSocialEditar.ambiente?.tokenIdentificador || '0',
      nemonicoTipoActividadSocial: seguimientoSocialEditar.nemonicoTipoActividadSocial,
      descripcionSocial: seguimientoSocialEditar.descripcionSocial,
      accionesAdoptadas: seguimientoSocialEditar.accionesAdoptadas,
      comentarios: seguimientoSocialEditar.comentarios,
      fecha: fecha,
    });
  }

  private obtenerValor(llave: string) {
    return this.formularioSeguimientoSocial.get(llave)?.value;
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.formularioSeguimientoSocial.reset();
    this.seguimientoSocialDTO = null;
    this.enrutador.navigate(['../'], { relativeTo: this.ruta });
    this.servicioTab.cambiarTab(2);
  }

  crearActualizar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }

    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioSeguimientoSocial.controls).forEach(key => {
      const control = this.formularioSeguimientoSocial.get(key);
      control.markAsTouched();
    });
    
    // Verificar si hay campos con error de "soloEspacios"
    let tieneEspaciosEnBlanco = false;
    Object.keys(this.formularioSeguimientoSocial.controls).forEach(key => {
      const control = this.formularioSeguimientoSocial.get(key);
      if (control.errors && control.errors['soloEspacios']) {
        tieneEspaciosEnBlanco = true;
      }
    });
    
    if (this.formularioSeguimientoSocial.invalid || !this.formularioValido()) {
      if (tieneEspaciosEnBlanco) {
        this.servicioMensajes.mensajeError('No se permiten campos con solo espacios en blanco.');
      } else {
        this.servicioMensajes.mensajeError('Por favor, complete todos los campos requeridos');
      }
      return;
    }

    // Activar el indicador de envío y deshabilitar el formulario
    this.estaProcesandoGuardado = true;
    this.formularioSeguimientoSocial.disable();

    const dialogoCarga = this.servicioMensajes.mensajeLoading(`Guardando ${this.tituloPantalla}...`);

    let seguimientoSocial = new SeguimientoSocialDTO();
    const valoresFormulario = this.formularioSeguimientoSocial.getRawValue();

    // Aseguramos que la fecha sea un objeto Date válido
    const fecha = valoresFormulario.fecha instanceof Date 
      ? valoresFormulario.fecha 
      : new Date(valoresFormulario.fecha);

    // Encontrar los objetos completos basados en los tokenIdentificador
    let programa = null;
    let ambiente = null;
    
    // Solo buscar programa y ambiente si debe verlos
    if (this.debeVerProgramaYAmbiente()) {
      programa = this.listaProgramas.find(p => p.tokenIdentificador === valoresFormulario.programa);
      ambiente = this.listaAmbientes.find(a => a.tokenIdentificador === valoresFormulario.ambiente);
    }

    // Limpiar espacios en blanco de los campos de texto
    Object.keys(valoresFormulario).forEach(key => {
      if (typeof valoresFormulario[key] === 'string' && key !== 'programa' && key !== 'ambiente' && key !== 'nemonicoTipoActividadSocial') {
        valoresFormulario[key] = valoresFormulario[key].trim();
      }
    });

    Object.assign(seguimientoSocial, {
      ...valoresFormulario,
      fecha: fecha, // Aseguramos que es un objeto Date
      programa: programa || null,
      ambiente: ambiente || null,
      centro: this.centro,
      tokenEvaluacion: this.uuid_fp,
      tokenIdentificador: this.seguimientoSocialDTO?.tokenIdentificador,
      esEdicion: this.esEdicion
    });

    this.servicioSeguimientoSocial.crearSeguimientoSocial(seguimientoSocial, this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<SeguimientoSocialDTO>) => {
          dialogoCarga.close();
          // Restablecer estado sin importar el resultado
          this.estaProcesandoGuardado = false;
          this.formularioSeguimientoSocial.enable();
          if (!respuesta.exito) {
            this.servicioSeguimientoSocial.checkError(respuesta);
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
          this.formularioSeguimientoSocial.enable();
          this.servicioSeguimientoSocial.checkError(error);
        }
      });
  }

  /**
   * Genera e imprime la ficha de seguimiento social en formato PDF
   * Este método recopila los datos del formulario actual para generar un PDF
   * con la información completa del seguimiento social
   */
  imprimirFicha() {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de imprimir el seguimiento social?`,
      "¿Desea continuar?"
    );
  
    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Preparando la impresión del seguimiento social...`);
          
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
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
                      
                      const tipoActividadSeleccionado = this.listaTiposActividadSocial.find(t => 
                        t.nemonico === this.obtenerValor("nemonicoTipoActividadSocial"));
                      
                      const tipoActividadNombre = tipoActividadSeleccionado?.nombre || 'No especificado';

                      // Obtener información del programa
                      const programaToken = this.obtenerValor("programa");
                      let programaNombre = 'No especificado';
                      if (programaToken && programaToken !== '0' && this.debeVerProgramaYAmbiente()) {
                        const programaSeleccionado = this.listaProgramas.find(p => p.tokenIdentificador === programaToken);
                        if (programaSeleccionado) {
                          programaNombre = programaSeleccionado.nombre;
                        }
                      }
                      
                      // Obtener información del ambiente
                      const ambienteToken = this.obtenerValor("ambiente");
                      let ambienteNombre = 'No especificado';
                      if (ambienteToken && ambienteToken !== '0' && this.debeVerProgramaYAmbiente()) {
                        const ambienteSeleccionado = this.listaAmbientes.find(a => a.tokenIdentificador === ambienteToken);
                        if (ambienteSeleccionado) {
                          ambienteNombre = ambienteSeleccionado.nombre;
                        }
                      }
                      
                      // Determinar modalidad según tipo de centro
                      let modalidad = 'Medio cerrado';
                      if (this.tipoCentro === 'SOA') {
                        modalidad = 'Medio abierto';
                      } else if (this.tipoCentro === 'UAPICE') {
                        modalidad = 'UAPICE';
                      }
                      
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_SOCIAL';
                      
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'Centro de rehabilitación'),
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        
                        "[PROGRAMA]": this.utilidades.escaparHTML(programaNombre),
                        "[AMBIENTE]": this.utilidades.escaparHTML(ambienteNombre),
                        "[FECHA-SEGUIMIENTO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(this.obtenerValor("fecha"))),
                        "[TIPO-SEGUIMIENTO]": this.utilidades.escaparHTML(tipoActividadNombre),
                        "[MODALIDAD]": this.utilidades.escaparHTML(modalidad),
                        
                        "[MOTIVO]": this.utilidades.escaparHTML(tipoActividadNombre),
                        "[DESCRIPCION]": this.utilidades.escaparHTML(this.obtenerValor("descripcionSocial") || 'No especificado'),
                        "[ACUERDOS-COMPROMISOS]": this.utilidades.escaparHTML(this.obtenerValor("accionesAdoptadas") || 'No especificado'),
                        "[OBSERVACIONES]": this.utilidades.escaparHTML(this.obtenerValor("comentarios") || 'No especificado'),

                        "[MOSTRAR_CJDR]": this.tipoCentro === 'CJDR' ? 'table-row-group' : 'none',
                        "[MOSTRAR_NO_CJDR]": this.tipoCentro === 'CJDR' ? 'none' : 'table-row-group'
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
