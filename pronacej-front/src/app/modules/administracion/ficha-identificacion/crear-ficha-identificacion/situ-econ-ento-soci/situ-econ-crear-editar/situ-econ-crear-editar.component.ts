import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EvaluacionSocialArtefactoDTO } from 'app/core/model/both/EvaluacionSocialArtefactoDTO.model';
import { EvaluacionSocialDTO } from 'app/core/model/both/EvaluacionSocialDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { PaginacionPersonasRelacionadasRequest } from 'app/core/model/request/paginacionPersonaRelacionadaRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { environment } from 'environments/environment';
import { EvaluacionSocialArtefactoService } from 'app/modules/seguridad/services/evaluacionSocialArtefactoService';
import { EvaluacionSocialService } from 'app/modules/seguridad/services/evaluacionSocial.service';
import { MdRegiPersComponent } from './md-regi-pers/md-regi-pers.component';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MdRegiArteComponent } from './md-regi-arte/md-regi-arte.component';
import { MdRegiCritComponent } from './md-regi-crit/md-regi-crit.component';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { HttpClient } from '@angular/common/http';
import { TabService } from 'app/core/services/tab.service';

@Component({
  selector: 'app-situ-econ-crear-editar',
  standalone: true,
  imports: [
    MatTableModule,
    CommonModule,
    MatTabsModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDatepickerModule,
    MatBottomSheetModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatIconModule,
  ],
  templateUrl: './situ-econ-crear-editar.component.html',
  styleUrl: './situ-econ-crear-editar.component.scss'
})
export class SituEconCrearEditarComponent {
  // Variables de identificación
  uuid_fp: string;
  uuid_es: string;

  // Variables de paginación
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  // Variables de formulario
  evaluacionSocialForm: FormGroup;
  evaluacionSocialDTO: EvaluacionSocialDTO;
  tituloPantalla = "Situación Económica/Entorno Social";

  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SITUACION_ECONOMICA_ENTORNO_SOCIAL;

  // Variables de datos
  listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
  personaRelacionadaDS: MatTableDataSource<PersonaRelacionadaDTO>;
  listaArtefactos: EvaluacionSocialArtefactoDTO[] = [];
  artefactoDS: MatTableDataSource<EvaluacionSocialArtefactoDTO>;
  listaCriterios: any[] = [];
  criterioDS: MatTableDataSource<any>;

  // Variables para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  // Variable para controlar si todos los catálogos se han cargado completamente
  catalogosCargados: boolean = false;

  // Listas de catálogos
  listaPersonasRelacionadasTotales: PersonaRelacionadaDTO[] = [];
  listaArtefactosVivienda: CatalogoDTO[] = [];
  listaCriteriosCatalogo: CatalogoDTO[] = [];
  listaCondicionesLaborales: CatalogoDTO[] = [];
  listaZonasViviendas: CatalogoDTO[] = [];
  listaSubZonas: CatalogoDTO[] = [];
  listaSubZonasUrbanas: CatalogoDTO[] = [];
  listaSubZonasRurales: CatalogoDTO[] = [];
  listaMaterialesParedVivienda: CatalogoDTO[] = [];
  listaMaterialesTechoVivienda: CatalogoDTO[] = [];
  listaMaterialesPisoVivienda: CatalogoDTO[] = [];
  listaTiposAbastecimientoAgua: CatalogoDTO[] = [];
  listaTiposVivienda: CatalogoDTO[] = [];
  listaTiposAlumbrado: CatalogoDTO[] = [];
  listaCombustibleCocinar: CatalogoDTO[] = [];
  listaTiposDesague: CatalogoDTO[] = [];
  listaTenencias: CatalogoDTO[] = [];
  listaOtrosServicios: CatalogoDTO[] = [];
  listaModalidadEstudio: CatalogoDTO[] = [];
  listaNivelEBR: CatalogoDTO[] = [];
  listaNivelSuperior: CatalogoDTO[] = [];
  listaNivelEBA: CatalogoDTO[] = [];
  modalidadEstudio: string = '';

  // Configuración de columnas
  columnasPersonaRelacionada: string[] = [
    'primerNombre',
    'parentesco',
    'numeroDocumento',
    'ingresoPromedio',
    'numeroHijos',
    'esResponsableEconom',
  ];
  columnasArtefacto: string[] = [
    'artefactosVivienda',
    'cantidad',
  ];
  columnasCriterio: string[] = [
    'criteriosVivienda',
    'comentario',
  ];

  @ViewChild('situEconLaboOcioPag') personaRelacionadaPag: MatPaginator;
  @ViewChild('artefactoPag') artefactoPag: MatPaginator;
  @ViewChild('criterioPag') criterioPag: MatPaginator;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioEvaluacionSocial: EvaluacionSocialService,
    private servicioEvaluacionSocialArtefacto: EvaluacionSocialArtefactoService,
    private servicioDatosFamiliares: DatosFamiliaresService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public dialogoModal: MatDialog,
    public utilidades: FuncionesUtils,
    private http: HttpClient,
    private servicioTab: TabService,
    public servicioPdf: PdfService,
  ) {
    this.construirForm();
  }

  /**
   * Inicializa el componente y configura el estado inicial
   */
  ngOnInit(): void {
    // Deshabilitar el formulario mientras se cargan los datos
    this.evaluacionSocialForm.disable();

    this.uuid_fp = this.rutaActiva.snapshot.params['uuid_fp'];
    this.evaluacionSocialDTO = history.state.evaluacionSocialDTO;

    // Iniciar la carga de catálogos y datos
    this.cargarDatosCatalogo();
    this.obtenerTodasPersonasRelacionadas();

    if (this.evaluacionSocialDTO) {
      this.esVisualizacion = this.evaluacionSocialDTO.esVisualizacion;

      // Actualizar las columnas basado en esVisualizacion
      if (!this.esVisualizacion) {
        this.columnasPersonaRelacionada = ['acciones', ...this.columnasPersonaRelacionada];
        this.columnasArtefacto = ['acciones', ...this.columnasArtefacto];
      }

      // El formulario se habilitará cuando los catálogos terminen de cargarse
      this.obtenerPersonasRelacionadasPorEvaluacionSocial();
      // Los artefactos se cargarán después de que todos los catálogos estén listos
    } else {
      // Si es una creación nueva, añadir las columnas de acciones
      this.columnasPersonaRelacionada = ['acciones', ...this.columnasPersonaRelacionada];
      this.columnasArtefacto = ['acciones', ...this.columnasArtefacto];
    }
  }

  ngAfterViewInit(){
    this.personaRelacionadaPag.pageIndex = 0;
    this.personaRelacionadaPag.pageSizeOptions = [5, 10, 15, 20];
    this.personaRelacionadaPag.pageSize = this.personaRelacionadaPag.pageSizeOptions[0];
    this.personaRelacionadaPag.length = 0;
  }


  private actualizarColumnas(): void {
    const columnasBasePersonaRelacionada = [
      'primerNombre',
      'parentesco',
      'numeroDocumento',
      'ingresoPromedio',
      'numeroHijos',
      'esResponsableEconom',
    ];
    const columnasBaseArtefacto = [
      'artefactosVivienda',
      'cantidad',
    ];
    const columnasBaseCriterio: string[] = [
      'criteriosVivienda',
      'comentario',
    ];

    if (!this.esVisualizacion) {
      this.columnasPersonaRelacionada = ['acciones', ...columnasBasePersonaRelacionada];
      this.columnasArtefacto = ['acciones', ...columnasBaseArtefacto];
    } else {
      this.columnasPersonaRelacionada = [...columnasBasePersonaRelacionada];
      this.columnasArtefacto = [...columnasBaseArtefacto];
    }
  }

  /**
   * Carga todos los catálogos necesarios para el funcionamiento del formulario
   * Mantiene el formulario deshabilitado hasta que todos los catálogos estén cargados
   */
  cargarDatosCatalogo() {
    // Deshabilitamos el formulario al inicio de la carga
    this.evaluacionSocialForm.disable();

    const catalogos = [
      { clave: 'CONDICION_LABORAL', lista: 'listaCondicionesLaborales' },
      { clave: 'ZONA_VIVIENDA', lista: 'listaZonasViviendas' },
      { clave: 'SUBZONAS_URBANAS', lista: 'listaSubZonasUrbanas' },
      { clave: 'SUBZONAS_RURALES', lista: 'listaSubZonasRurales' },
      { clave: 'MATERIAL_PARED', lista: 'listaMaterialesParedVivienda' },
      { clave: 'MATERIAL_TECHO', lista: 'listaMaterialesTechoVivienda' },
      { clave: 'MATERIAL_PISO', lista: 'listaMaterialesPisoVivienda' },
      { clave: 'ABASTECIMIENTO_AGUA', lista: 'listaTiposAbastecimientoAgua' },
      { clave: 'TIPOS_VIVIENDA', lista: 'listaTiposVivienda' },
      { clave: 'TIPO_ALUMBRADO', lista: 'listaTiposAlumbrado' },
      { clave: 'COMBUSTIBLE_COCINA', lista: 'listaCombustibleCocinar' },
      { clave: 'DESAGUE_VIVIENDA', lista: 'listaTiposDesague' },
      { clave: 'ARTEFACTOS_VIVIENDA', lista: 'listaArtefactosVivienda' },
      { clave: 'CRITERIOS_CALIDAD_VIVIENDA', lista: 'listaCriteriosCatalogo' },
      { clave: 'TENENCIA_VIVIENDA', lista: 'listaTenencias' },
      { clave: 'OTROS_SERVICIOS_VIVIENDA', lista: 'listaOtrosServicios' }
    ];

    // Contador para rastrear cuántos catálogos se han cargado
    let catalogosCargadosCount = 0;
    const totalCatalogos = catalogos.length;

    catalogos.forEach(catalogo => {
      this.utilidades.obtenerListaCatalogo(catalogo.clave, this.nemonicoMenu).subscribe({
        next: (data) => {
          // Guardar los datos del catálogo
          this[catalogo.lista] = data;
          catalogosCargadosCount++;

          // Si todos los catálogos han sido cargados, habilitar el formulario
          if (catalogosCargadosCount === totalCatalogos) {
            this.habilitarFormularioDespuesDeCarga();
          }
        },
        error: (error) => {
          console.error(`Error cargando ${catalogo.clave}:`, error);
          catalogosCargadosCount++;

          // Incluso en caso de error, continuamos con la aplicación
          if (catalogosCargadosCount === totalCatalogos) {
            this.habilitarFormularioDespuesDeCarga();
          }
        }
      });
    });
  }

  registrarPersonaRelacionada() {
    const dialogRef = this.dialogoModal.open(MdRegiPersComponent, {
      data: {
        listaPersonasRelacionadasTotales: this.listaPersonasRelacionadasTotales,
        listaCondicionesLaborales: this.listaCondicionesLaborales,
      },
      width: '600px',
      disableClose: true,

    });

    dialogRef.afterClosed().subscribe(async (resultado: PersonaRelacionadaDTO) => {
      if (resultado) {
        this.listaPersonasRelacionadas.unshift(resultado);
        this.personaRelacionadaDS = new MatTableDataSource(this.listaPersonasRelacionadas);
        this.personaRelacionadaDS.paginator = this.personaRelacionadaPag;
      }
    });
  }

  registrarArtefacto() {
    const dialogRef = this.dialogoModal.open(MdRegiArteComponent, {
      data: {
        listaArtefactosVivienda: this.listaArtefactosVivienda,
      },
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaArtefactos.unshift(resultado);
        this.artefactoDS = new MatTableDataSource(this.listaArtefactos);
        this.artefactoDS.paginator = this.artefactoPag;
      }
    });
  }

  registrarCriterio() {
    const dialogRef = this.dialogoModal.open(MdRegiCritComponent, {
      data: {
        listaCriteriosCatalogo: this.listaCriteriosCatalogo,
      },
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaCriterios.unshift(resultado);
        this.criterioDS = new MatTableDataSource(this.listaCriterios);
        this.criterioDS.paginator = this.criterioPag;
      }
    });
  }

  editarPersonaRelacionada(personaRelacionada: PersonaRelacionadaDTO, indice: number) {
    const dialogRef = this.dialogoModal.open(MdRegiPersComponent, {
      data: {
        fila: personaRelacionada,
        uuid_fp: this.uuid_fp,
        listaPersonasRelacionadasTotales: this.listaPersonasRelacionadasTotales,
        listaCondicionesLaborales: this.listaCondicionesLaborales,
      },
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaPersonasRelacionadas[indice] = resultado;
        this.personaRelacionadaDS = new MatTableDataSource(this.listaPersonasRelacionadas);
        this.personaRelacionadaDS.paginator = this.personaRelacionadaPag;
      }
    });
  }

  editarArtefacto(fila: EvaluacionSocialArtefactoDTO, indice: number) {
    const dialogRef = this.dialogoModal.open(MdRegiArteComponent, {
      data: {
        fila: fila,
        uuid_fp: this.uuid_fp,
        listaArtefactosVivienda: this.listaArtefactosVivienda,
      },
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaArtefactos[indice] = resultado;
        this.artefactoDS = new MatTableDataSource(this.listaArtefactos);
        this.artefactoDS.paginator = this.artefactoPag;
      }
    });
  }


  eliminarPersonaRelacionada(indice: number) {
    const elementoEliminar = this.listaPersonasRelacionadas[indice];

    // Mostrar mensaje de confirmación
    const refConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta persona relacionada? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si es un registro nuevo (no está en BD todavía)
          if (elementoEliminar.tokenIdentificadorEvaluacionSocial === "0") {
            this.listaPersonasRelacionadas.splice(indice, 1);
            this.personaRelacionadaDS = new MatTableDataSource(this.listaPersonasRelacionadas);
            this.personaRelacionadaDS.paginator = this.personaRelacionadaPag;
            this.servicioMensajes.mensajeExitoso("Registro eliminado", "La persona relacionada se ha eliminado correctamente");
          }
          // Si es un registro de la BD
          else {
            const cargador = this.servicioMensajes.mensajeLoading("Eliminando persona relacionada...");

            this.servicioDatosFamiliares.eliminarPersonaRelacionadaPorSituacionEconomicaSocial(elementoEliminar)
              .subscribe({
                next: (respuesta: RespuestaPorDefecto<boolean>) => {
                  cargador.close();
                  this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

                  if (respuesta.exito) {
                    this.obtenerPersonasRelacionadasPorEvaluacionSocial();
                  }
                },
                error: (error: any) => {
                  cargador.close();
                  this.servicioEvaluacionSocial.checkError(error);
                }
              });
          }
        }
      }
    });
  }

  eliminarArtefacto(indice: number) {
    const elementoEliminar = this.listaArtefactos[indice];

    // Mostrar mensaje de confirmación
    const refConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar este artefacto? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si es un registro nuevo (no está en BD todavía)
          if (elementoEliminar.tokenIdentificador === "0") {
            this.listaArtefactos.splice(indice, 1);
            this.artefactoDS = new MatTableDataSource(this.listaArtefactos);
            this.artefactoDS.paginator = this.artefactoPag;
            this.servicioMensajes.mensajeExitoso("Registro eliminado", "El artefacto se ha eliminado correctamente");
          }
          // Si es un registro de la BD
          else {
            const cargador = this.servicioMensajes.mensajeLoading("Eliminando artefacto...");

            this.servicioEvaluacionSocialArtefacto.eliminarArtefactoPorEvaluacionSocial(elementoEliminar)
              .subscribe({
                next: (respuesta: RespuestaPorDefecto<boolean>) => {
                  cargador.close();
                  this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

                  if (respuesta.exito) {
                    this.obtenerArtefactosPorEvaluacionSocial();
                  }
                },
                error: (error: any) => {
                  cargador.close();
                  this.servicioEvaluacionSocial.checkError(error);
                }
              });
          }
        }
      }
    });
  }

  eliminarCriterio(indice: number) {
    // Mostrar mensaje de confirmación
    const refConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar este criterio? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          this.listaCriterios.splice(indice, 1);
          this.criterioDS = new MatTableDataSource(this.listaCriterios);
          this.criterioDS.paginator = this.criterioPag;
          this.servicioMensajes.mensajeExitoso("Registro eliminado", "El criterio se ha eliminado correctamente");
        }
      }
    });
  }

  construirForm() {
    this.evaluacionSocialForm = this.constructorFormulario.group(
      {
        zonaVivienda: ['0',],
        subZona: ['0'],
        materialParedVivienda: [0,],
        materialTechoVivienda: [0,],
        materialPisoVivienda: [0,],
        tipoAbastecimientoAgua: [0,],
        tipoVivienda: [0,],
        tipoAlumbrado: [0,],
        combustibleCocinar: [0,],
        tipoDesague: [0,],
        tenencia: ['0', [Validators.required]],
        otrosServicios: ['0', [Validators.required]],

        // Campos numéricos (no necesitan validación de espacios)
        numeroAmbientes: [null, [
          Validators.required,
          Validators.min(1),
          Validators.max(999999999)
        ]],
        numeroOcupantes: [null, [
          Validators.required,
          Validators.min(1),
          Validators.max(999999999)
        ]],
        numeroHabitaciones: [null, [
          Validators.required,
          Validators.min(1),
          Validators.max(999999999)
        ]],
        numeroDormitorios: [null, [
          Validators.required,
          Validators.min(1),
          Validators.max(999999999)
        ]],

        // Campos de texto (aplicar validación de espacios)
        grupoAmical: [null, [Validators.required, this.validarNoEspacios()]],
        factorRiesgoMedio: [null, [Validators.required, this.validarNoEspacios()]],
        areaAcademicoLaboral: [null, [Validators.required, this.validarNoEspacios()]],
        areaSocialRecreacional: [null, [Validators.required, this.validarNoEspacios()]],
        areaFamiliarPareja: [null, [Validators.required, this.validarNoEspacios()]],
        areaPersonal: [null, [Validators.required, this.validarNoEspacios()]],
      },
    );
  }

  /**
   * Obtiene el nombre completo de una persona relacionada
   * @param persona Objeto PersonaRelacionadaDTO 
   * @returns String con el nombre completo formateado
   */
  obtenerNombreCompleto(persona: PersonaRelacionadaDTO): string {
    if (!persona) {
      return 'No especificado';
    }

    // Si tiene el campo nombres que ya contiene el nombre completo, usarlo
    if (persona.nombres) {
      return persona.nombres;
    }

    // Si no tiene nombres pero tiene los componentes individuales
    const partes: string[] = [];

    if (persona.primerNombre) partes.push(persona.primerNombre);
    if (persona.segundoNombre) partes.push(persona.segundoNombre);
    if (persona.apellidoPaterno) partes.push(persona.apellidoPaterno);
    if (persona.apellidoMaterno) partes.push(persona.apellidoMaterno);

    // Si aún no hay partes, usar los campos alternativos
    if (partes.length === 0) {
      if (persona.primerApellido) partes.push(persona.primerApellido);
      if (persona.segundoApellido) partes.push(persona.segundoApellido);
    }

    return partes.length > 0 ? partes.join(' ') : 'No especificado';
  }

  observadorCambioEnCampo(campo: string, event: any) {
    if (campo === "zonaVivienda") {
      const zona = this.listaZonasViviendas.find(elemento => elemento.tokenIdentificador === event.value);
      if (zona?.nombre?.toLowerCase().includes('urbana')) {
        this.listaSubZonas = this.listaSubZonasUrbanas;
      } else if (zona?.nombre?.toLowerCase().includes('rural')) {
        this.listaSubZonas = this.listaSubZonasRurales;
      } else {
        this.listaSubZonas = [];
      }
      this.evaluacionSocialForm.get('subZona').setValue('0');
      this.evaluacionSocialForm.get('subZona').markAsTouched();
      this.evaluacionSocialForm.get('subZona').updateValueAndValidity();
    }
  }

  obtenerPersonasRelacionadasPorEvaluacionSocial() {
    const solicitudPaginacion = new PaginacionPersonasRelacionadasRequest();
    solicitudPaginacion.size = this.size;
    solicitudPaginacion.page = this.page;
    solicitudPaginacion.tokenIdentificador = this.evaluacionSocialDTO.tokenIdentificador;

    this.servicioDatosFamiliares.obtenerPersonasRelacionadasPorEvaluacionSocial(solicitudPaginacion)
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
          this.personaRelacionadaDS = new MatTableDataSource(this.listaPersonasRelacionadas);
          this.personaRelacionadaPag.length = respuesta.data.totalItems;
          this.personaRelacionadaDS.paginator = this.personaRelacionadaPag;
          console.log(this.listaPersonasRelacionadas);
        },
        error: (error: any) => {
          this.servicioEvaluacionSocial.checkError(error);
        }
      });
  }

  obtenerTodasPersonasRelacionadas() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.size;
    solicitudPaginacion.page = this.page;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    this.servicioDatosFamiliares.obtenerPersonasRelacionadas(solicitudPaginacion)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
          if (!respuesta.exito) {
            this.servicioMensajes.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }
          this.listaPersonasRelacionadasTotales = respuesta.data.data;
        },
        error: (error: any) => {
          this.servicioEvaluacionSocial.checkError(error);
        }
      });
  }

  obtenerArtefactosPorEvaluacionSocial() {
    const tokenEvaluacion = this.evaluacionSocialDTO?.tokenIdentificador;
    
    if (!tokenEvaluacion || tokenEvaluacion === '0') {
      console.log('No hay token de evaluación social válido para cargar artefactos');
      this.listaArtefactos = [];
      this.artefactoDS = new MatTableDataSource(this.listaArtefactos);
      if (this.artefactoPag) {
        this.artefactoDS.paginator = this.artefactoPag;
      }
      return;
    }

    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.size;
    solicitudPaginacion.page = this.page;
    solicitudPaginacion.tokenIdentificador = tokenEvaluacion;

    this.servicioEvaluacionSocialArtefacto.obtenerArtefactosPorEvaluacionSocialPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionSocialArtefactoDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(
            respuesta.titulo,
            respuesta.mensaje
          );
          return;
        }
        this.listaArtefactos = respuesta.data.data;
        this.artefactoDS = new MatTableDataSource(this.listaArtefactos);
        this.artefactoDS.paginator = this.artefactoPag;
      },
      error: (error: any) => {
        this.servicioEvaluacionSocial.checkError(error);
      }
    });
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.evaluacionSocialForm.reset();
    this.evaluacionSocialDTO = null;
    this.enrutador.navigate(['../'], { relativeTo: this.rutaActiva });
    this.servicioTab.cambiarTab(1);
  }

  eliminarArtefactoPorEvaluacionSocial(evaluacionSocialArtefactoDTO: EvaluacionSocialArtefactoDTO) {
    const referencia = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de eliminar el artefacto? Esta operación es irreversible`,
      "¿Desea continuar?"
    );

    referencia.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const carga = this.servicioMensajes.mensajeLoading("Eliminando artefacto...");

          this.servicioEvaluacionSocialArtefacto.eliminarArtefactoPorEvaluacionSocial(evaluacionSocialArtefactoDTO)
            .subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                carga.close();

                if (respuesta.exito) {
                  this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
                  this.obtenerArtefactosPorEvaluacionSocial();
                }
              },
              error: (error: any) => {
                carga.close();
                this.servicioEvaluacionSocial.checkError(error);
              }
            });
        }
      }
    });
  }

  eliminarPersonaRelacionadaPorSituacionEconomicaSocial(personaRelacionadaDTO: PersonaRelacionadaDTO) {
    const referencia = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de eliminar la persona relacionada? Esta operación es irreversible`,
      "¿Desea continuar?"
    );

    referencia.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const carga = this.servicioMensajes.mensajeLoading("Eliminando persona relacionada...");

          this.servicioDatosFamiliares.eliminarPersonaRelacionadaPorSituacionEconomicaSocial(personaRelacionadaDTO)
            .subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                carga.close();

                if (respuesta.exito) {
                  this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
                  this.obtenerPersonasRelacionadasPorEvaluacionSocial();
                }
              },
              error: (error: any) => {
                carga.close();
                this.servicioEvaluacionSocial.checkError(error);
              }
            });
        }
      }
    });
  }

  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const esInvalido = control?.value && control?.value?.trim().length === 0;
      return esInvalido ? { 'soloEspacios': true } : null;
    };
  }

  private obtenerValor(llave: string) {
    return this.evaluacionSocialForm.get(llave)?.value;
  }

  /**
   * Habilita el formulario después de que todos los catálogos se han cargado
   * y configura el estado adecuado según el modo (visualización/edición)
   */
  private habilitarFormularioDespuesDeCarga() {
    // Marcar los catálogos como cargados
    this.catalogosCargados = true;

    // Si no estamos en modo visualización, habilitamos el formulario
    if (!this.esVisualizacion) {
      this.evaluacionSocialForm.enable();
    }

    // Si estamos en modo edición, actualizar el formulario con los datos existentes
    if (this.evaluacionSocialDTO && this.esEdicion) {
      this.empezarEdicion(this.evaluacionSocialDTO);
    } else if (this.evaluacionSocialDTO) {
      // Si estamos visualizando, iniciar la edición para cargar los datos pero mantener deshabilitado
      this.empezarEdicion(this.evaluacionSocialDTO);
    }

    // Cargar artefactos después de que todo esté configurado
    if (this.evaluacionSocialDTO?.tokenIdentificador) {
      this.obtenerArtefactosPorEvaluacionSocial();
    }
  }

  async empezarEdicion(evaluacionSocialEditar: EvaluacionSocialDTO) {
    this.esEdicion = true;
    this.evaluacionSocialDTO = evaluacionSocialEditar;
    this.uuid_es = evaluacionSocialEditar.tokenIdentificador;

    await Promise.all([
      new Promise<void>(resolve => {
        const subscription = this.utilidades.obtenerListaCatalogo('SUBZONAS_URBANAS', this.nemonicoMenu).subscribe({
          next: (data) => {
            this.listaSubZonasUrbanas = data;
            subscription.unsubscribe();
            resolve();
          }
        });
      }),
      new Promise<void>(resolve => {
        const subscription = this.utilidades.obtenerListaCatalogo('SUBZONAS_RURALES', this.nemonicoMenu).subscribe({
          next: (data) => {
            this.listaSubZonasRurales = data;
            subscription.unsubscribe();
            resolve();
          }
        });
      })
    ]);

    this.evaluacionSocialForm.get("zonaVivienda")?.setValue(evaluacionSocialEditar.tokenIdentificadorZonaVivienda);
    const zonaSeleccionada = this.listaZonasViviendas.find(elemento =>
      elemento.tokenIdentificador === evaluacionSocialEditar.tokenIdentificadorZonaVivienda
    );
    if (zonaSeleccionada?.nombre?.toLowerCase().includes('urbana')) {
      this.listaSubZonas = this.listaSubZonasUrbanas;
    } else if (zonaSeleccionada?.nombre?.toLowerCase().includes('rural')) {
      this.listaSubZonas = this.listaSubZonasRurales;
    }
    this.evaluacionSocialForm.get("subZona")?.setValue(evaluacionSocialEditar.tokenIdentificadorSubZona ? evaluacionSocialEditar.tokenIdentificadorSubZona : '0');
    this.evaluacionSocialForm.get("materialParedVivienda")?.setValue(evaluacionSocialEditar.tokenIdentificadorMaterialParedVivienda);
    this.evaluacionSocialForm.get("materialPisoVivienda")?.setValue(evaluacionSocialEditar.tokenIdentificadorMaterialPisoVivienda);
    this.evaluacionSocialForm.get("materialTechoVivienda")?.setValue(evaluacionSocialEditar.tokenIdentificadorMaterialTechoVivienda);
    this.evaluacionSocialForm.get("tipoAbastecimientoAgua")?.setValue(evaluacionSocialEditar.tokenIdentificadorAbastecimientoAguaVivienda);
    this.evaluacionSocialForm.get("tipoVivienda")?.setValue(evaluacionSocialEditar.tokenIdentificadorTipoVivienda);
    this.evaluacionSocialForm.get("tipoAlumbrado")?.setValue(evaluacionSocialEditar.tokenIdentificadorTipoAlumbradoVivienda);
    this.evaluacionSocialForm.get("combustibleCocinar")?.setValue(evaluacionSocialEditar.tokenIdentificadorCombustibleCocinarVivienda);
    this.evaluacionSocialForm.get("tipoDesague")?.setValue(evaluacionSocialEditar.tokenIdentificadorTipoDesagueVivienda);
    this.evaluacionSocialForm.get("tenencia")?.setValue(evaluacionSocialEditar.tokenIdentificadorTenencia);
    this.evaluacionSocialForm.get("otrosServicios")?.setValue(evaluacionSocialEditar.tokenIdentificadorOtrosServicios);
    this.evaluacionSocialForm.get("numeroAmbientes")?.setValue(evaluacionSocialEditar.numeroAmbientes);
    this.evaluacionSocialForm.get("numeroOcupantes")?.setValue(evaluacionSocialEditar.numeroOcupantes);
    this.evaluacionSocialForm.get("numeroHabitaciones")?.setValue(evaluacionSocialEditar.numeroHabitaciones);
    this.evaluacionSocialForm.get("numeroDormitorios")?.setValue(evaluacionSocialEditar.numeroDormitorios);
    this.evaluacionSocialForm.get("grupoAmical")?.setValue(evaluacionSocialEditar.grupoAmical);
    this.evaluacionSocialForm.get("factorRiesgoMedio")?.setValue(evaluacionSocialEditar.factorRiesgoMedio);
    this.evaluacionSocialForm.get("areaAcademicoLaboral")?.setValue(evaluacionSocialEditar.areaAcademicoLaboral);
    this.evaluacionSocialForm.get("areaSocialRecreacional")?.setValue(evaluacionSocialEditar.areaSocialRecreacional);
    this.evaluacionSocialForm.get("areaFamiliarPareja")?.setValue(evaluacionSocialEditar.areaFamiliarPareja);
    this.evaluacionSocialForm.get("areaPersonal")?.setValue(evaluacionSocialEditar.areaPersonal);
  }

  /**
   * Método para crear o actualizar la situación económica y entorno social
   * Implementa control para evitar procesamiento duplicado
   */
  crearActualizar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }

    // Establecer bandera de procesamiento
    this.estaProcesandoGuardado = true;

    // Deshabilitar el formulario mientras se procesa
    this.evaluacionSocialForm.disable();

    let evaluacionSocial = new EvaluacionSocialDTO();
    evaluacionSocial.tokenIdentificadorZonaVivienda = this.obtenerValor("zonaVivienda");
    evaluacionSocial.tokenIdentificadorSubZona = this.obtenerValor("subZona");
    evaluacionSocial.tokenIdentificadorMaterialParedVivienda = this.obtenerValor("materialParedVivienda");
    evaluacionSocial.tokenIdentificadorMaterialTechoVivienda = this.obtenerValor("materialTechoVivienda");
    evaluacionSocial.tokenIdentificadorMaterialPisoVivienda = this.obtenerValor("materialPisoVivienda");
    evaluacionSocial.tokenIdentificadorAbastecimientoAguaVivienda = this.obtenerValor("tipoAbastecimientoAgua");
    evaluacionSocial.tokenIdentificadorTipoVivienda = this.obtenerValor("tipoVivienda");
    evaluacionSocial.tokenIdentificadorTipoAlumbradoVivienda = this.obtenerValor("tipoAlumbrado");
    evaluacionSocial.tokenIdentificadorCombustibleCocinarVivienda = this.obtenerValor("combustibleCocinar");
    evaluacionSocial.tokenIdentificadorTipoDesagueVivienda = this.obtenerValor("tipoDesague");
    evaluacionSocial.tokenIdentificadorTenencia = this.obtenerValor("tenencia");
    evaluacionSocial.tokenIdentificadorOtrosServicios = this.obtenerValor("otrosServicios");
    evaluacionSocial.numeroAmbientes = this.obtenerValor("numeroAmbientes");
    evaluacionSocial.numeroOcupantes = this.obtenerValor("numeroOcupantes");
    evaluacionSocial.numeroHabitaciones = this.obtenerValor("numeroHabitaciones");
    evaluacionSocial.numeroDormitorios = this.obtenerValor("numeroDormitorios");
    evaluacionSocial.grupoAmical = this.obtenerValor("grupoAmical");
    evaluacionSocial.factorRiesgoMedio = this.obtenerValor("factorRiesgoMedio");
    evaluacionSocial.areaAcademicoLaboral = this.obtenerValor("areaAcademicoLaboral");
    evaluacionSocial.areaSocialRecreacional = this.obtenerValor("areaSocialRecreacional");
    evaluacionSocial.areaFamiliarPareja = this.obtenerValor("areaFamiliarPareja");
    evaluacionSocial.areaPersonal = this.obtenerValor("areaPersonal");
    evaluacionSocial.listaPersonasRelacionadas = this.listaPersonasRelacionadas;
    evaluacionSocial.listaArtefactos = this.listaArtefactos;

    evaluacionSocial.tokenIdentificadorFichaIdentificacion = this.uuid_fp;
    evaluacionSocial.tokenIdentificador = this.evaluacionSocialDTO?.tokenIdentificador;
    evaluacionSocial.esEdicion = this.esEdicion;

    this.servicioEvaluacionSocial.crearEvaluacionSocial(evaluacionSocial, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<EvaluacionSocialDTO>) => {
        // Volver a habilitar el formulario
        this.evaluacionSocialForm.enable();
        // Restablecer bandera de procesamiento
        this.estaProcesandoGuardado = false;

        if (!respuesta.exito) {
          this.servicioEvaluacionSocial.checkError(respuesta);
          return;
        }

        // Actualizar el DTO con la respuesta del servidor
        this.evaluacionSocialDTO = respuesta.data;
        this.uuid_es = respuesta.data.tokenIdentificador;

        this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
        this.enrutador.navigate(['../'], { relativeTo: this.rutaActiva });
        this.servicioTab.cambiarTab(1);
      },
      error: (error: any) => {
        this.servicioEvaluacionSocial.checkError(error);
        // Volver a habilitar el formulario en caso de error
        this.evaluacionSocialForm.enable();
        // Restablecer bandera de procesamiento en caso de error
        this.estaProcesandoGuardado = false;
      }
    });
  }

  imprimirFicha() {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir la situación económica y entorno social?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión...");

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

                      let tablaPersonaRelacionada = new TablaPlantilla();
                      tablaPersonaRelacionada.encabezados = [
                        'Nombres',
                        'Parentesco',
                        'N° documento',
                        'Salario',
                        'N° hijos',
                        'Resp. económico'
                      ];

                      tablaPersonaRelacionada.filas = this.listaPersonasRelacionadas.map(persona => ({
                        'Nombres': this.utilidades.escaparHTML(persona.nombres || "Sin nombre"),
                        'Parentesco': this.utilidades.escaparHTML(persona.parentesco || "No especificado"),
                        'N° documento': this.utilidades.escaparHTML(persona.numeroDocumento || "No especificado"),
                        'Salario': this.utilidades.escaparHTML(persona.ingresoPromedio ? `S/ ${persona.ingresoPromedio}` : "S/ 0"),
                        'N° hijos': persona.numeroHijos ? Math.trunc(persona.numeroHijos).toString() : '0',
                        'Resp. económico': persona.esResponsableEconom ? 'Si' : 'No'
                      }));

                      let tablaArtefacto = new TablaPlantilla();
                      tablaArtefacto.encabezados = [
                        'Artefacto',
                        'Cantidad'
                      ];

                      tablaArtefacto.filas = this.listaArtefactos.map(artefacto => ({
                        'Artefacto': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(artefacto.tokenIdentificadorArtefactosVivienda, this.listaArtefactosVivienda) || 'No especificado'),
                        'Cantidad': Math.trunc(artefacto.cantidad).toString() || '0'
                      }));

                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SITUACION_ECONOMICA_ENTORNO_SOCIAL';

                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[TABLA-PERSONA-RELACIONADA]": JSON.stringify(tablaPersonaRelacionada),
                        "[TABLA-ARTEFACTO]": JSON.stringify(tablaArtefacto),
                        "[ZONA-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("zonaVivienda"), this.listaZonasViviendas) || ''),
                        "[MATERIAL-PARED-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("materialParedVivienda"), this.listaMaterialesParedVivienda) || ''),
                        "[MATERIAL-PISO-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("materialPisoVivienda"), this.listaMaterialesPisoVivienda) || ''),
                        "[MATERIAL-TECHO-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("materialTechoVivienda"), this.listaMaterialesTechoVivienda) || ''),
                        "[TIPO-ABASTESIMIENTO-AGUA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("tipoAbastecimientoAgua"), this.listaTiposAbastecimientoAgua) || ''),
                        "[TIPO-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("tipoVivienda"), this.listaTiposVivienda) || ''),
                        "[TIPO-ALUMBRADO]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("tipoAlumbrado"), this.listaTiposAlumbrado) || ''),
                        "[COMBUSTIBLE-COCINAR]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("combustibleCocinar"), this.listaCombustibleCocinar) || ''),
                        "[TIPO-DESAGUE]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("tipoDesague"), this.listaTiposDesague) || ''),
                        "[NUMERO-AMBIENTES]": this.utilidades.escaparHTML(this.obtenerValor("numeroAmbientes")?.toString() || ''),
                        "[TENENCIA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(this.obtenerValor("tenencia"), this.listaTenencias) || ''),
                        "[NUMERO-OCUPANTES]": this.utilidades.escaparHTML(this.obtenerValor("numeroOcupantes")?.toString() || ''),
                        "[NUMERO-HABITACIONES]": this.utilidades.escaparHTML(this.obtenerValor("numeroHabitaciones")?.toString() || ''),
                        "[NUMERO-DORMITORIOS]": this.utilidades.escaparHTML(this.obtenerValor("numeroDormitorios")?.toString() || ''),
                        "[GRUPO-AMICAL]": this.utilidades.escaparHTML(this.obtenerValor("grupoAmical") || ''),
                        "[FACTORES-RIESGO-MEDIO]": this.utilidades.escaparHTML(this.obtenerValor("factorRiesgoMedio") || ''),
                        "[AREA-ACADEMICO-LABORAL]": this.utilidades.escaparHTML(this.obtenerValor("areaAcademicoLaboral") || ''),
                        "[AREA-SOCIAL-RECREACIONAL]": this.utilidades.escaparHTML(this.obtenerValor("areaSocialRecreacional") || ''),
                        "[AREA-FAMILIAR-PAREJA]": this.utilidades.escaparHTML(this.obtenerValor("areaFamiliarPareja") || ''),
                        "[AREA-PERSONAL]": this.utilidades.escaparHTML(this.obtenerValor("areaPersonal") || '')
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

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    
    this.obtenerPersonasRelacionadasPorEvaluacionSocial();
  }
}
