import { CommonModule, registerLocaleData } from '@angular/common';
import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EvaluacionDomiciliariaDTO } from 'app/core/model/both/EvaluacionDomiciliariaDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { EvaluacionDomiciliariaService } from 'app/modules/seguridad/services/EvaluacionDomiciliaria.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import localeEs from '@angular/common/locales/es';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { HttpClient } from '@angular/common/http';
import { TabService } from 'app/core/services/tab.service';

// Registrar el locale español para formateo de fechas
registerLocaleData(localeEs);

@Component({
  selector: 'app-evaluacion-domiciliaria-crear-editar',
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
  ],
  // Configuración para el manejo de fechas en formato español
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './evaluacion-domiciliaria-crear-editar.component.html',
  styleUrl: './evaluacion-domiciliaria-crear-editar.component.scss'
})
export class EvaluacionDomiciliariaCrearEditarComponent {
  // Variables de identificación
  uuid_fp: string;
  visitaRealizada = false;
  mostrarCampoOtraPersona = false;

  // Variables de entidad
  centro: JerarquiaDTO;
  evaluacionDomiciliariaForm: FormGroup;
  evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO;

  // Variables de configuración
  tituloPantalla: string;

  // Variable de control para evitar envíos duplicados
  estaProcesandoGuardado: boolean = false;
  
  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;
  medioCerrado = false;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA;

  // Variables de datos
  listaCriterios: CatalogoDTO[] = [];
  listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
  
  // Constante para el valor de "Otros" en el select de persona relacionada
  readonly VALOR_OTROS_PERSONA_RELACIONADA = "OTROS";

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioEvaluacionDomiciliaria: EvaluacionDomiciliariaService,
    private servicioJerarquia: JerarquiaService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioDatosFamiliares: DatosFamiliaresService,
    private enrutador: Router,
    private ruta: ActivatedRoute,
    public utilidades: FuncionesUtils,
    public servicioPdf: PdfService,
    private servicioTab: TabService,
    private adaptadorFecha: DateAdapter<any>,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
  ) {
    // Configuramos el locale para el adaptador de fecha
    this.adaptadorFecha.setLocale('es');
    this.construirForm();
  }

  ngOnInit(): void {
    this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
    this.evaluacionDomiciliariaDTO = history.state.evaluacionDomiciliariaDTO;
    this.cargarDatosCatalogo();
    this.obtenerTodasPersonasRelacionadas();
    
    // Cargar centro del usuario logueado si no hay evaluación en edición
    if (!this.evaluacionDomiciliariaDTO) {
      this.cargarCentroUsuarioLogueado();
    }
  
    if (this.evaluacionDomiciliariaDTO) {
      this.esVisualizacion = this.evaluacionDomiciliariaDTO.esVisualizacion;
      this.esEdicion = true;
      
      // Primero cargar los datos
      this.empezarEdicion(this.evaluacionDomiciliariaDTO);
      
      // Deshabilitar el formulario si es visualización
      if (this.esVisualizacion) {
        this.evaluacionDomiciliariaForm.disable();
      }
    }
  }

  /**
   * Carga datos de catálogo necesarios para el formulario
   */
  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('CRITERIOS', this.nemonicoMenu).subscribe({
      next: (data) => this.listaCriterios = data,
      error: (error) => console.error('Error al cargar los criterios:', error)
    });
  }

  /**
   * Obtiene todas las personas relacionadas para mostrarlas en el selector
   */
  obtenerTodasPersonasRelacionadas() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    this.servicioDatosFamiliares.obtenerPersonasRelacionadas(solicitudPaginacion,this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
          if (!respuesta.exito) {
            this.servicioMensajes.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }

          this.listaPersonasRelacionadas = respuesta.data.data;
          
          // Agregar la opción "Otros" al final de la lista
          const personaOtros = new PersonaRelacionadaDTO();
          personaOtros.tokenIdentificador = this.VALOR_OTROS_PERSONA_RELACIONADA;
          personaOtros.nombres = "Otros";
          this.listaPersonasRelacionadas.push(personaOtros);
        }
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
      if (!this.esVisualizacion) {
        this.evaluacionDomiciliariaForm.get(controlName).setValue(fecha);
      }
    }
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   * @returns Validador personalizado
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
   * Construye el formulario con las validaciones correspondientes
   */
  construirForm() {
    this.evaluacionDomiciliariaForm = this.constructorFormulario.group({
      fechaRegistro: [new Date(), [Validators.required]],
      fechaEntrevista: [new Date(), [Validators.required]],
      duracionVista: [null, [Validators.required]],
      visitaRealizada: [null, [Validators.required]],
      motivoNoVisita: ["", [this.validarNoEspacios()]],
      objetivoGeneral: ["", [this.validarNoEspacios()]],
      desarrolloVisitaDomiciliaria: ["", [this.validarNoEspacios()]],
      caracteristicasDomicilioVisitado: ["", [this.validarNoEspacios()]],
      conclusiones: ["", [this.validarNoEspacios()]],
      recomendaciones: ["", [this.validarNoEspacios()]],
      // Campos para persona relacionada
      personaRelacionada: ["0"],
      otraPersonaRelacionada: ["", [this.validarNoEspacios()]],
      // Campos Medio Cerrado
      dinamicaFamiliarDisfuncional: ["", [this.validarNoEspacios()]],
      caracteristicasEntornoSocialMC: ["", [this.validarNoEspacios()]],
      factoresProtectores: ["", [this.validarNoEspacios()]],
      // Campos Medio Abierto
      factoresRiesgoFamilia: ["", [this.validarNoEspacios()]],
      factoresRiesgoSocial: ["", [this.validarNoEspacios()]],
      factoresProtectoresFamilia: ["", [this.validarNoEspacios()]],
      factoresProtectoresSocial: ["", [this.validarNoEspacios()]],
    });
    this.actualizarCamposRequeridos();
  }

  /**
   * Maneja los cambios en campos específicos del formulario
   * @param campo Nombre del campo que cambió
   * @param event Evento que contiene el nuevo valor
   */
  observadorCambioEnCampo(campo: string, event: any) {
    if (campo === "visitaRealizada") {
      this.visitaRealizada = event.value === 'S';
      if (this.visitaRealizada) {
        this.evaluacionDomiciliariaForm.get('motivoNoVisita')?.setValue("");
      }
    } else if (campo === "personaRelacionada") {
      const personaRelacionadaSeleccionada = event.value;
      
      if (personaRelacionadaSeleccionada === this.VALOR_OTROS_PERSONA_RELACIONADA) {
        this.mostrarCampoOtraPersona = true;
        this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').setValidators([Validators.required, this.validarNoEspacios()]);
      } else {
        this.mostrarCampoOtraPersona = false;
        this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').setValue("");
        this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').clearValidators();
        this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').setValidators([this.validarNoEspacios()]);
      }
      
      this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').updateValueAndValidity();
    }
  }

  /**
   * Actualiza las validaciones de campos según dependencias
   */
  actualizarCamposRequeridos() {
    const visitaRealizadaControl = this.evaluacionDomiciliariaForm.get('visitaRealizada');
    const motivoNoVisitaControl = this.evaluacionDomiciliariaForm.get('motivoNoVisita');

    visitaRealizadaControl?.valueChanges.subscribe((valor) => {
      if (valor === 'N') {
        motivoNoVisitaControl?.setValidators([Validators.required, this.validarNoEspacios()]);
      } else {
        motivoNoVisitaControl?.clearValidators();
        motivoNoVisitaControl?.setValidators([this.validarNoEspacios()]);
      }
      motivoNoVisitaControl?.updateValueAndValidity();
    });
  }

  /**
   * Obtiene el valor de un campo del formulario
   * @param key Nombre del campo
   * @returns Valor del campo
   */
  private obtenerValor(key: string) {
    return this.evaluacionDomiciliariaForm.get(key)?.value;
  }

  /**
   * Evalúa si la visita fue realizada desde un DTO original
   * @param valor Valor del DTO que puede ser boolean, string, etc.
   * @returns true si la visita fue realizada, false en caso contrario
   */
  private evaluarVisitaRealizadaDTO(valor: any): boolean {
    if (valor === null || valor === undefined) {
      return false;
    }

    if (typeof valor === 'boolean') {
      return valor;
    }

    if (typeof valor === 'string') {
      const valorLimpio = valor.trim().toLowerCase();
      return valorLimpio === 'true' || valorLimpio === 's' || valorLimpio === 'si' || valorLimpio === 'sí' || valorLimpio === '1';
    }

    if (typeof valor === 'number') {
      return valor === 1;
    }

    return Boolean(valor);
  }

  /**
   * Evalúa si la visita fue realizada desde un valor de formulario
   * @param valor Valor del formulario que puede ser 'S', 'N', boolean, etc.
   * @returns true si la visita fue realizada, false en caso contrario
   */
  private evaluarVisitaRealizadaFormulario(valor: any): boolean {
    if (valor === null || valor === undefined) {
      return false;
    }

    if (typeof valor === 'boolean') {
      return valor;
    }

    if (typeof valor === 'string') {
      const valorLimpio = valor.trim().toLowerCase();
      return valorLimpio === 'true' || valorLimpio === 's' || valorLimpio === 'si' || valorLimpio === 'sí' || valorLimpio === '1';
    }

    if (typeof valor === 'number') {
      return valor === 1;
    }

    return Boolean(valor);
  }

  /**
   * Convierte una fecha de manera segura manejando diferentes formatos
   * @param fecha Fecha en formato string, Date o null/undefined
   * @returns Objeto Date válido o fecha actual si hay error
   */
  private convertirFechaSegura(fecha: any): Date {
    if (!fecha) {
      return new Date();
    }

    try {
      if (fecha instanceof Date) {
        return isNaN(fecha.getTime()) ? new Date() : fecha;
      }

      if (typeof fecha === 'string') {
        const fechaLimpia = fecha.trim();
        
        if (fechaLimpia.includes('T') || fechaLimpia.includes('Z')) {
          const fechaParseada = new Date(fechaLimpia);
          if (!isNaN(fechaParseada.getTime())) {
            return fechaParseada;
          }
        }
        
        const partesGuion = fechaLimpia.split('-');
        const partesSlash = fechaLimpia.split('/');
        
        if (partesGuion.length === 3 && partesGuion[0].length <= 2) {
          const dia = parseInt(partesGuion[0], 10);
          const mes = parseInt(partesGuion[1], 10) - 1;
          const año = parseInt(partesGuion[2], 10);
          const fechaFormateada = new Date(año, mes, dia);
          if (!isNaN(fechaFormateada.getTime())) {
            return fechaFormateada;
          }
        }
        
        if (partesSlash.length === 3 && partesSlash[0].length <= 2) {
          const dia = parseInt(partesSlash[0], 10);
          const mes = parseInt(partesSlash[1], 10) - 1;
          const año = parseInt(partesSlash[2], 10);
          const fechaFormateada = new Date(año, mes, dia);
          if (!isNaN(fechaFormateada.getTime())) {
            return fechaFormateada;
          }
        }
        
        const fechaDirecta = new Date(fechaLimpia);
        if (!isNaN(fechaDirecta.getTime())) {
          return fechaDirecta;
        }
      }

      if (typeof fecha === 'number') {
        const fechaTimestamp = new Date(fecha);
        return isNaN(fechaTimestamp.getTime()) ? new Date() : fechaTimestamp;
      }

      const fechaConvertida = new Date(fecha);
      return isNaN(fechaConvertida.getTime()) ? new Date() : fechaConvertida;
      
    } catch (error) {
      console.error('Error al convertir fecha:', fecha, error);
      return new Date();
    }
  }

  /**
   * Obtiene el nombre completo de una persona relacionada
   * @param tokenIdentificador ID de la persona relacionada
   * @returns Nombre completo o valor alternativo
   */
  obtenerNombrePersonaRelacionada(tokenIdentificador: string): string {
    if (!tokenIdentificador || tokenIdentificador === "0") {
      return "No especificado";
    }

    if (tokenIdentificador === this.VALOR_OTROS_PERSONA_RELACIONADA) {
      const otraPersona = this.obtenerValor("otraPersonaRelacionada");
      return otraPersona ? otraPersona.trim() : "Otro";
    }
    
    const personaEncontrada = this.listaPersonasRelacionadas.find(
      p => p.tokenIdentificador === tokenIdentificador
    );
    
    if (personaEncontrada) {
      if (personaEncontrada.nombres) {
        return personaEncontrada.nombres;
      }
      
      const partes: string[] = [];
      
      if (personaEncontrada.primerNombre) partes.push(personaEncontrada.primerNombre);
      if (personaEncontrada.segundoNombre) partes.push(personaEncontrada.segundoNombre);
      if (personaEncontrada.apellidoPaterno) partes.push(personaEncontrada.apellidoPaterno);
      if (personaEncontrada.apellidoMaterno) partes.push(personaEncontrada.apellidoMaterno);
      
      if (partes.length === 0) {
        if (personaEncontrada.primerApellido) partes.push(personaEncontrada.primerApellido);
        if (personaEncontrada.segundoApellido) partes.push(personaEncontrada.segundoApellido);
      }
      
      return partes.length > 0 ? partes.join(' ') : "No especificado";
    }
    
    return "No especificado";
  }

  /**
   * Carga información del centro al que pertenece el usuario logueado
   * En el sistema multi-jerárquico, esto obtiene la jerarquía específica del usuario
   */
  cargarCentroUsuarioLogueado() {
    this.servicioJerarquia
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.servicioJerarquia.checkError(respuesta);
            return;
          }

          this.centro = respuesta.data;
          this.medioCerrado = this.centro?.jerarquiaPadre?.nemonico !== 'SOA';
          this.ajustarTituloSegunCentro();
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  /**
   * Ajusta el título de la pantalla según tipo de centro
   */
  ajustarTituloSegunCentro() {
    const esSOA = this.centro?.jerarquiaPadre?.nemonico === 'SOA';
    this.tituloPantalla = esSOA ? 'Evaluación de visita domiciliaria' : 'Evaluación domiciliaria';
  }

  /**
   * Getter que indica si el centro no es de tipo SOA
   */
  get noEsSOA(): boolean {
    return this.centro?.jerarquiaPadre?.nemonico !== 'SOA';
  }

  /**
   * Getter que retorna el texto descriptivo según tipo de centro
   */
  get textoTipoEvaluacion(): string {
    return this.noEsSOA ? 'evaluación domiciliaria' : 'evaluación de visita domiciliaria';
  }

  /**
   * Inicia el modo edición/visualización con los datos proporcionados
   * @param evaluacionDomiciliariaEditar Datos de evaluación domiciliaria para editar/visualizar
   */
  empezarEdicion(evaluacionDomiciliariaEditar: EvaluacionDomiciliariaDTO) {
    this.esEdicion = true;
    this.evaluacionDomiciliariaDTO = evaluacionDomiciliariaEditar;

    // En el sistema multi-jerárquico, usar el centro de la evaluación si existe
    // Si no existe, cargar el centro del usuario logueado
    if (evaluacionDomiciliariaEditar.centro) {
      this.centro = evaluacionDomiciliariaEditar.centro;
      this.medioCerrado = this.centro?.jerarquiaPadre?.nemonico !== 'SOA';
      this.ajustarTituloSegunCentro();
    } else {
      this.cargarCentroUsuarioLogueado();
    }

    const fechaRegistro = this.convertirFechaSegura(evaluacionDomiciliariaEditar.fechaRegistro);
    const fechaEntrevista = this.convertirFechaSegura(evaluacionDomiciliariaEditar.fechaEntrevista);

    let duracionFormateada = '';
    if (evaluacionDomiciliariaEditar.duracionVista !== null && evaluacionDomiciliariaEditar.duracionVista !== undefined) {
      if (typeof evaluacionDomiciliariaEditar.duracionVista === 'number') {
        duracionFormateada = this.utilidades.convertirDecimalATiempo(evaluacionDomiciliariaEditar.duracionVista);
      } else if (typeof evaluacionDomiciliariaEditar.duracionVista === 'string') {
        duracionFormateada = evaluacionDomiciliariaEditar.duracionVista;
      }
    }

    if (evaluacionDomiciliariaEditar.otraPersonaRelacionada && evaluacionDomiciliariaEditar.otraPersonaRelacionada.trim() !== '') {
      this.mostrarCampoOtraPersona = true;
      if (!evaluacionDomiciliariaEditar.tokenIdentificadorPersonaRelacionada) {
        evaluacionDomiciliariaEditar.tokenIdentificadorPersonaRelacionada = this.VALOR_OTROS_PERSONA_RELACIONADA;
      }
    } else if (evaluacionDomiciliariaEditar.tokenIdentificadorPersonaRelacionada === this.VALOR_OTROS_PERSONA_RELACIONADA) {
      this.mostrarCampoOtraPersona = true;
    } else {
      this.mostrarCampoOtraPersona = false;
    }

    this.evaluacionDomiciliariaForm.patchValue({
      fechaRegistro: fechaRegistro,
      fechaEntrevista: fechaEntrevista,
      duracionVista: duracionFormateada,
      visitaRealizada: this.evaluarVisitaRealizadaDTO(evaluacionDomiciliariaEditar.visitaRealizada) ? "S" : "N",
      motivoNoVisita: evaluacionDomiciliariaEditar.motivoNoVisita || "",
      objetivoGeneral: evaluacionDomiciliariaEditar.objetivoGeneral || "",
      desarrolloVisitaDomiciliaria: evaluacionDomiciliariaEditar.desarrolloVisitaDomiciliaria || "",
      caracteristicasDomicilioVisitado: evaluacionDomiciliariaEditar.caracteristicasDomicilioVisitado || "",
      conclusiones: evaluacionDomiciliariaEditar.conclusiones || "",
      recomendaciones: evaluacionDomiciliariaEditar.recomendaciones || "",
      dinamicaFamiliarDisfuncional: evaluacionDomiciliariaEditar.dinamicaFamiliarDisfuncional || "",
      caracteristicasEntornoSocialMC: evaluacionDomiciliariaEditar.caracteristicasEntornoSocialMC || "",
      factoresProtectores: evaluacionDomiciliariaEditar.factoresProtectores || "",
      factoresRiesgoFamilia: evaluacionDomiciliariaEditar.factoresRiesgoFamilia || "",
      factoresRiesgoSocial: evaluacionDomiciliariaEditar.factoresRiesgoSocial || "",
      factoresProtectoresFamilia: evaluacionDomiciliariaEditar.factoresProtectoresFamilia || "",
      factoresProtectoresSocial: evaluacionDomiciliariaEditar.factoresProtectoresSocial || "",
      personaRelacionada: evaluacionDomiciliariaEditar.tokenIdentificadorPersonaRelacionada || "0",
      otraPersonaRelacionada: evaluacionDomiciliariaEditar.otraPersonaRelacionada || "",
    });

    if (this.mostrarCampoOtraPersona) {
      this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').setValidators([Validators.required, this.validarNoEspacios()]);
    } else {
      this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').clearValidators();
      this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').setValidators([this.validarNoEspacios()]);
    }
    this.evaluacionDomiciliariaForm.get('otraPersonaRelacionada').updateValueAndValidity();

    this.visitaRealizada = this.evaluarVisitaRealizadaDTO(evaluacionDomiciliariaEditar.visitaRealizada);

    this.cdr.detectChanges();
    this.evaluacionDomiciliariaForm.updateValueAndValidity();
  }

  /**
   * Cancela la edición y regresa a la vista anterior
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.evaluacionDomiciliariaForm.reset();
    this.evaluacionDomiciliariaDTO = null;
    this.enrutador.navigate(['../'], { relativeTo: this.ruta });
    this.servicioTab.cambiarTab(0);
  }

/**
 * Crea o actualiza la evaluación domiciliaria según los datos del formulario
 */
  crearActualizar() {
    if (this.estaProcesandoGuardado) {
      return;
    }

    Object.keys(this.evaluacionDomiciliariaForm.controls).forEach(key => {
      const control = this.evaluacionDomiciliariaForm.get(key);
      control.markAsTouched();
    });
    
    let tieneEspaciosEnBlanco = false;
    Object.keys(this.evaluacionDomiciliariaForm.controls).forEach(key => {
      const control = this.evaluacionDomiciliariaForm.get(key);
      if (control.errors && control.errors['soloEspacios']) {
        tieneEspaciosEnBlanco = true;
      }
    });
    
    if (this.evaluacionDomiciliariaForm.invalid) {
      if (tieneEspaciosEnBlanco) {
        this.servicioMensajes.mensajeError('No se permiten campos con solo espacios en blanco.');
      } else {
        this.servicioMensajes.mensajeError('Por favor complete los campos obligatorios.');
      }
      return;
    }
    
    this.estaProcesandoGuardado = true;
    this.evaluacionDomiciliariaForm.disable();

    let evaluacionDomiciliaria = new EvaluacionDomiciliariaDTO();
    const datosFormulario = this.evaluacionDomiciliariaForm.getRawValue();
    
    Object.keys(datosFormulario).forEach(key => {
      if (typeof datosFormulario[key] === 'string') {
        datosFormulario[key] = datosFormulario[key].trim();
      }
    });

    const fechaRegistro = datosFormulario.fechaRegistro instanceof Date 
      ? datosFormulario.fechaRegistro 
      : new Date(datosFormulario.fechaRegistro);
      
    const fechaEntrevista = datosFormulario.fechaEntrevista instanceof Date 
      ? datosFormulario.fechaEntrevista 
      : new Date(datosFormulario.fechaEntrevista);

    Object.assign(evaluacionDomiciliaria, {
      ...datosFormulario,
      fechaRegistro: fechaRegistro,
      fechaEntrevista: fechaEntrevista,
      centro: this.centro,
      duracionVista: this.utilidades.convertirTiempoADecimal(datosFormulario.duracionVista),
      visitaRealizada: datosFormulario.visitaRealizada === "S",
      tokenIdentificadorFichaIdentificacion: this.uuid_fp,
      tokenIdentificador: this.evaluacionDomiciliariaDTO?.tokenIdentificador,
      tokenIdentificadorPersonaRelacionada: datosFormulario.personaRelacionada,
      otraPersonaRelacionada: datosFormulario.personaRelacionada === this.VALOR_OTROS_PERSONA_RELACIONADA ? datosFormulario.otraPersonaRelacionada : null,
      esEdicion: this.esEdicion,
    });

    const dialogoCarga = this.servicioMensajes.mensajeLoading(`Guardando ${this.textoTipoEvaluacion}...`);
    
    this.servicioEvaluacionDomiciliaria.crearEvaluacionDomiciliaria(evaluacionDomiciliaria, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<EvaluacionDomiciliariaDTO>) => {
        dialogoCarga.close();
        this.estaProcesandoGuardado = false;
        this.evaluacionDomiciliariaForm.enable();

        if (!respuesta.exito) {
          this.servicioEvaluacionDomiciliaria.checkError(respuesta);
          return;
        }
        
        this.evaluacionDomiciliariaDTO = respuesta.data;
        
        this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
        this.enrutador.navigate(['../'], { relativeTo: this.ruta });
        this.servicioTab.cambiarTab(0);
      },
      error: (error: any) => {
        dialogoCarga.close();
        this.estaProcesandoGuardado = false;
        this.evaluacionDomiciliariaForm.enable();
        this.servicioEvaluacionDomiciliaria.checkError(error);
      }
    });
  }

  /**
   * Genera e imprime la ficha de evaluación domiciliaria en formato PDF
   */
  imprimirFicha() {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de imprimir la ${this.textoTipoEvaluacion}?`,
      "¿Desea continuar?"
    );
  
    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Preparando la impresión de ${this.textoTipoEvaluacion}...`);
          
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp,this.nemonicoMenu)
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
                      
                      const personaRelacionadaId = this.obtenerValor("personaRelacionada");
                      const nombrePersonaRelacionada = this.obtenerNombrePersonaRelacionada(personaRelacionadaId);
                      const esOtraPersona = personaRelacionadaId === this.VALOR_OTROS_PERSONA_RELACIONADA;
                      const detalleOtraPersona = esOtraPersona ? this.obtenerValor("otraPersonaRelacionada") : '';

                      const visitaRealizadaValor = this.obtenerValor("visitaRealizada");
                      const visitaRealizada = this.evaluarVisitaRealizadaFormulario(visitaRealizadaValor);
                      const mostrarMotivoNoVisita = !visitaRealizada;
                      
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_EVALUACION_DOMICILIARIA';
                      
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || ''),
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(this.obtenerValor("fechaRegistro"))),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[FECHA-ENTREVISTA]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(this.obtenerValor("fechaEntrevista"))),
                        "[DURACION-VISITA]": this.utilidades.escaparHTML(
                          this.obtenerValor("duracionVista") 
                            ? this.utilidades.convertirDecimalATiempo(
                                this.utilidades.convertirTiempoADecimal(this.obtenerValor("duracionVista"))
                              )
                            : 'No especificado'
                        ),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[PERSONA-ENTREVISTADA]": this.utilidades.escaparHTML(esOtraPersona ? detalleOtraPersona : nombrePersonaRelacionada),
                        "[VISITA-REALIZADA]": visitaRealizada ? 'Sí' : 'No',
                        "[DISPLAY-MOTIVO-NO-VISITA]": mostrarMotivoNoVisita ? 'table-row' : 'none',
                        "[MOTIVO-NO-VISITA]": this.utilidades.escaparHTML(this.obtenerValor("motivoNoVisita") || ''),
                        "[OBJETIVO-GENERAL]": this.utilidades.escaparHTML(this.obtenerValor("objetivoGeneral") || ''),
                        "[DESARROLLO-VISITA]": this.utilidades.escaparHTML(this.obtenerValor("desarrolloVisitaDomiciliaria") || ''),
                        "[DISPLAY-MC]": this.medioCerrado ? 'block' : 'none',
                        "[DINAMICA-FAMILIAR-DISFUNCIONAL]": this.utilidades.escaparHTML(this.obtenerValor("dinamicaFamiliarDisfuncional") || ''),
                        "[CARACTERISTICAS-ENTORNO-SOCIAL-MC]": this.utilidades.escaparHTML(this.obtenerValor("caracteristicasEntornoSocialMC") || ''),
                        "[FACTORES-PROTECTORES]": this.utilidades.escaparHTML(this.obtenerValor("factoresProtectores") || ''),
                        "[DISPLAY-MA]": !this.medioCerrado ? 'block' : 'none',
                        "[FACTORES-RIESGO-FAMILIA]": this.utilidades.escaparHTML(this.obtenerValor("factoresRiesgoFamilia") || ''),
                        "[FACTORES-RIESGO-SOCIAL]": this.utilidades.escaparHTML(this.obtenerValor("factoresRiesgoSocial") || ''),
                        "[FACTORES-PROTECTORES-FAMILIA]": this.utilidades.escaparHTML(this.obtenerValor("factoresProtectoresFamilia") || ''),
                        "[FACTORES-PROTECTORES-SOCIAL]": this.utilidades.escaparHTML(this.obtenerValor("factoresProtectoresSocial") || ''),
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(this.obtenerValor("conclusiones") || ''),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(this.obtenerValor("recomendaciones") || ''),
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
