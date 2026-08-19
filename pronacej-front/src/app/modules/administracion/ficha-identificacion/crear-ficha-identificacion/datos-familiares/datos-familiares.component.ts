import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule, registerLocaleData } from '@angular/common';
import { Component, OnDestroy, ViewChild } from '@angular/core';
import {
    AbstractControl,
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    UntypedFormControl,
    Validators,
} from '@angular/forms';
import {
    MatBottomSheetModule,
} from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
    MatPaginator,
    MatPaginatorModule,
    PageEvent,
} from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTable, MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { DireccionPersonaRelacionadaDTO } from 'app/core/model/both/DireccionPersonaRelacionada.model';
import { InformacionUbicacionDTO } from 'app/core/model/both/InformacionUbicacionDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DatosFamiliaresDTO } from 'app/core/model/both/datosFamiliaresDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { InformacionUbicacionService } from 'app/modules/seguridad/services/informacionUbicacion.service';
import { environment } from 'environments/environment';
import { debounceTime, distinctUntilChanged, forkJoin, Subject, takeUntil } from 'rxjs';
import { ModalEditarInformacionComponent } from './modal-editar-informacion/modal-editar-informacion.component';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { HttpClient } from '@angular/common/http';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { DatosFamiliaresDocumentoDTO } from 'app/core/model/request/ia/DatosFamiliaresDocumentoDTO.model';
import { DatosFamiliaresDocumentosRequest } from 'app/core/model/request/ia/DatosFamiliaresDocumentosRequest.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import localeEs from '@angular/common/locales/es';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';

registerLocaleData(localeEs);
import { UtilsService } from 'app/core/services/utils.service';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { EstudiosDTO } from 'app/core/model/both/EstudiosDTO.model';
import { EstudiosService } from 'app/modules/seguridad/services/EstudiosService.service';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { EstudiosComponent } from '../estudios/estudios.component';
import { TrabajoLaboralComponent } from '../trabajo-laboral/trabajo-laboral.component';

@Component({
    selector: 'app-datos-familiares',
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
        EstudiosComponent,
        TrabajoLaboralComponent
    ],
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
    ],
    templateUrl: './datos-familiares.component.html',
    styleUrl: './datos-familiares.component.scss',
})
export class DatosFamiliaresComponent implements OnDestroy {
    // Configuración de paginación
    listSize = [5, 10, 15, 20];
    page = 0;
    size = this.listSize[0];
    totalItems = 0;
    listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
    // Reemplazar dataSource por fuenteDatosPersonasRelacionadas
    fuenteDatosPersonasRelacionadas = new MatTableDataSource<PersonaRelacionadaDTO>([]);
    esEdicion = false;
    modalidadEstudio: string = '';

    // Datos Familiares
    loadingPersonas: boolean = true;
    pagePersonas = 0;
    listSizePersonas = [5, 10, 15, 20];
    sizePersonas = this.listSize[0];
    totalItemsPersonas = 0;
    listaPersonasRelacionadasMain: PersonaRelacionadaDTO[] = [];
    dataSourcePersonas: CdkTableDataSourceInput<PersonaRelacionadaDTO>;

    // Añadir ViewChild para el paginador
    @ViewChild('personasRelacionadasPag') paginadorPersonasRelacionadas!: MatPaginator;

    keyLabelsTablePersonas: any = {
        acciones: 'Acciones',
        nombre: 'Nombres completos',
        parentesco: 'Parentesco',
        indentificacion: 'Identificación',
    };

    personaRelacionadaForm: FormGroup;

    listaTipoDocumento: CatalogoDTO[] = [];
    listaTipoParentesco: CatalogoDTO[] = [];
    listaTipoEstadoCivil: CatalogoDTO[] = [];
    listaNivelAfectivo: CatalogoDTO[] = [];
    listaTipoSexo: CatalogoDTO[] = [];
    listaModalidadEstudio: CatalogoDTO[] = [];
    listaNivelEBR: CatalogoDTO[] = [];
    listaNivelSuperior: CatalogoDTO[] = [];
    listaNivelEBA: CatalogoDTO[] = [];
    listaReligion: CatalogoDTO[] = [];

    editarCrearPersona = false;
    esEdicionPersona = false;
    centro: JerarquiaDTO;
    personaRelacionadaEditando: PersonaRelacionadaDTO;
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR;

    // Variable para rastrear si estamos en modo edición para datos familiares
    private modoEdicionDatosFamiliares: boolean = false;

    direccionPersonaRelacionadaForm: FormGroup;
    listaTipoDirecciones: CatalogoDTO[] = [];

    listaDireccionesRelacionadas: DireccionPersonaRelacionadaDTO[] = [];
    dataSourceDirecciones: CdkTableDataSourceInput<DireccionPersonaRelacionadaDTO>;

    keyDirecciones: any = {
        acciones: 'Acciones',
        tipoDireccion: 'Tipo Dirección',
        direccion: 'direccion',
    };

    listaInformacionUbicaciones: InformacionUbicacionDTO[] = [];
    dataSourceInformacionUbicaciones: CdkTableDataSourceInput<InformacionUbicacionDTO>;

    keyInformacionUbicacion: any = {
        acciones: 'Acciones',
        tipoInformacion: 'Tipo Información',
        valor: 'Valor',
    };

    uuid_fp: string;
    listaInformacionUbicacionesEliminacion: string[] = [];

    @ViewChild(MatTable) table: MatTable<InformacionUbicacionDTO>;

    datosFamiliaresForm: FormGroup;
    listaGradosInstruccion: CatalogoDTO[] = [];
    listaTiposFamilia: CatalogoDTO[] = [];
    listaOrganizacionFamiliar: CatalogoDTO[] = [];
    listaTipoSacramento: CatalogoDTO[] = [];

    listaTipoOcupacion: CatalogoDTO[] = [];

    listaDocumentoIde = [
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_DNI',
            nombre: 'Documento Nacional de Identidad',
            minLength: 8,
            maxLength: 8,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE',
            nombre: 'Pasaporte',
            minLength: 6,
            maxLength: 10,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA',
            nombre: 'Documento Extranjería',
            minLength: 10,
            maxLength: 10,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA',
            nombre: 'Carné de Extranjería',
            minLength: 9,
            maxLength: 9,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO',
            nombre: 'Sin Documento',
            minLength: 0,
            maxLength: 0,
        }
    ];

    @ViewChild("documentosComp") tablaDocumentos: DocumentosSubidosTablaComponent;
    tiposDeDocumentosSistema: TipoDeDocumento[] = [];
    mostrarSubidaDocumentos: boolean = true;

    dataPeru: any;
    private unsubscribe$: Subject<void> = new Subject<void>();

    // Variables para controlar el estado de procesamiento
    estaProcesandoPersona: boolean = false;
    estaProcesandoDatosFamiliares: boolean = false;

    listaEstudios: EstudiosDTO[] = [];

editarCrearEstudio = false;
esEdicionEstudio = false;



    constructor(
        public dialog: MatDialog,
        private readonly router: Router,
        private readonly dialogMensajeService: DialogMensajeService,
        private datosFamiliaresService: DatosFamiliaresService,
        private servicioJerarquia: JerarquiaService,
        private informacionUbicacionService: InformacionUbicacionService,
        private fichaIdentificacionService: FichaIdentificacionService,
        public pdfService: PdfService,
        private route: ActivatedRoute,
        private formBuilder: FormBuilder,
        private catalogoService: CatalogoService,
        public funcionesUtils: FuncionesUtils,
        private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
        private http: HttpClient,
        private utilsService: UtilsService,
        private estudiosService: EstudiosService,
    ) {
        this.construirFormPersona();
        this.construirFormDatosFamiliares();
    }

    /**
     * Carga información del centro al que pertenece el usuario
     */
    cargarCentro(): Promise<void> {
        return new Promise((resolve, reject) => {
            this.servicioJerarquia
                .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
                .subscribe({
                    next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
                        if (!respuesta.exito) {
                            this.servicioJerarquia.checkError(respuesta);
                            resolve(); // Resolvemos la promesa aunque haya error para no bloquear otras operaciones
                            return;
                        }
                        this.centro = respuesta.data;
                        resolve();
                    },
                    error: (error: any) => {
                        this.servicioJerarquia.checkError(error);
                        reject(error);
                    }
                });
        });
    }

    /**
     * Inicializa el componente
     */
    async ngOnInit(): Promise<void> {
        // Obtener parámetro de la ruta
        this.uuid_fp = this.route.snapshot.params['uuid_fp'];

        // Inicializar componente básico
        this.personaRelacionadaEditando = null;
        this.mostrarSubidaDocumentos = false;
        this.inicializarListaDocumentos();

        this.detectarCambiosFechaNacimiento();

        try {
            // Ejecutar estas operaciones en paralelo
            await Promise.all([
                this.cargarCentro(),
                this.obtenerTiposDeDocumentos(),
                this.cargarCatalogos()
            ]);

            // Activar indicador de carga
            this.loadingPersonas = true;

            // Cargar datos principales en paralelo
            await Promise.all([
                this.obtenerDatosFamiliares(),
                this.obtenerSituacionFamiliar()
            ]);
        } catch (error) {
            console.error('Error durante la inicialización:', error);
            this.dialogMensajeService.mensajeError('Ocurrió un error al cargar la información. Inténtelo de nuevo.');
        } finally {
            // Asegurar que el indicador de carga se desactiva en todos los casos
            this.loadingPersonas = false;
        }
    }

    /**
     * Inicializa el paginador después de que la vista esté lista
     */
    ngAfterViewInit() {
        // Configurar el paginador después de que los datos estén cargados
        setTimeout(() => {
            if (this.fuenteDatosPersonasRelacionadas && this.paginadorPersonasRelacionadas) {

                this.paginadorPersonasRelacionadas.pageIndex = this.page;
                this.paginadorPersonasRelacionadas.pageSize = this.size;
                this.paginadorPersonasRelacionadas.length = this.totalItems;
            }
        }, 100);
    }

    detectarCambiosFechaNacimiento() {
        this.personaRelacionadaForm
        .get('fechaNacimiento')
        ?.valueChanges
        .pipe(distinctUntilChanged())
        .subscribe((fecha: Date) => {
        if (fecha) {
            const edad = this.calcularEdad(fecha);
            this.personaRelacionadaForm.get('edad')?.setValue(edad, { emitEvent: false });
        } else {
            this.personaRelacionadaForm.get('edad')?.setValue(null, { emitEvent: false });
        }
        });
    }

    private calcularEdad(fechaNacimiento: Date): number {
        const hoy = new Date();
        let edad = hoy.getFullYear() - fechaNacimiento.getFullYear();

        const mes = hoy.getMonth() - fechaNacimiento.getMonth();

        if (mes < 0 || (mes === 0 && hoy.getDate() < fechaNacimiento.getDate())) {
            edad--;
        }

        if (edad < 0) {
            edad = 0;
        }

        return edad;
    }

    crearEditarPersona() {
        this.editarCrearPersona = true;
        this.esEdicionPersona = false;
        this.personaRelacionadaForm.reset();
        this.listaInformacionUbicaciones = [];
        this.dataSourceInformacionUbicaciones = [];
        this.table.renderRows();

        // Inicializar persona relacionada temporal para mantener documentos
        this.personaRelacionadaEditando = new PersonaRelacionadaDTO();
        this.personaRelacionadaEditando.tokenIdentificador = 'temp-' + Date.now();

        // Activamos la subida de documentos
        this.mostrarSubidaDocumentos = true;

        // Inicializamos el listener para validar el número de documento
        this.setupNumeroIdentificacionListener();
    }

    // Método a agregar para inicializar la lista de documentos con la configuración correcta
    inicializarListaDocumentos() {
        this.listaDocumentoIde = [
            {
                nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_DNI',
                nombre: 'Documento Nacional de Identidad',
                minLength: 8,
                maxLength: 8,
            },
            {
                nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE',
                nombre: 'Pasaporte',
                minLength: 6,
                maxLength: 10,
            },
            {
                nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA',
                nombre: 'Documento Extranjería', // Cambiado de "DNI extranjero"
                minLength: 10,
                maxLength: 10,
            },
            {
                nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA',
                nombre: 'Carné de Extranjería',
                minLength: 9,
                maxLength: 9,
            },
            {
                nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO',
                nombre: 'Sin Documento',
                minLength: 0,
                maxLength: 0,
            }
        ];
    }

    // actualizarValidaciones(tipoIdentificacion: string): void {
    //     const numeroDocumentoControl = this.personaRelacionadaForm.get('numeroDocumento');

    //     // Si el tipo es SIN_DOCUMENTO, deshabilitar el control y vaciar el campo
    //     if (tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
    //         numeroDocumentoControl?.disable();
    //         return;
    //     } else {
    //         // Si no es SIN_DOCUMENTO, habilitar el control y establecer validadores
    //         numeroDocumentoControl?.enable();
    //     }

    //     const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
    //         (doc) => doc.nemonico === tipoIdentificacion
    //     );

    //     if (tipoDocumentoSeleccionado) {
    //         const { minLength, maxLength } = tipoDocumentoSeleccionado;

    //         // Conjunto base de validadores
    //         const validators = [
    //             Validators.required,
    //             Validators.minLength(minLength),
    //             Validators.maxLength(maxLength),
    //         ];

    //         // Agregar validación numérica solo para DNI
    //         if (tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
    //             validators.push(
    //                 Validators.pattern('^[0-9]*$') // Solo permite números
    //             );
    //         }

    //         numeroDocumentoControl?.setValidators(validators);
    //     } else {
    //         numeroDocumentoControl?.setValidators([Validators.required]);
    //     }

    //     // Refresca el estado del control
    //     numeroDocumentoControl?.updateValueAndValidity();
    // }


    actualizarValidaciones(tipoIdentificacion: string): void {
        const numeroDocumentoControl = this.personaRelacionadaForm.get('numeroIdentificacion');
        const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
            (doc) => doc.nemonico === tipoIdentificacion
        );
        if (!numeroDocumentoControl || !tipoDocumentoSeleccionado) return;
        const validators = [Validators.required];
        if (
            tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA' ||
            tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA'
        ) {
            validators.push(Validators.minLength(4));
        } else {
            validators.push(Validators.minLength(tipoDocumentoSeleccionado.minLength));
            validators.push(Validators.maxLength(tipoDocumentoSeleccionado.maxLength));
            // Solo agrega el patrón si realmente existe
            // validators.push(Validators.pattern(tipoDocumentoSeleccionado.regex));
        }

        numeroDocumentoControl.setValidators(validators);
        numeroDocumentoControl.updateValueAndValidity();
    }

    async cargarCatalogos(): Promise<void> {
        const catalogos$ = forkJoin({
            tipoSacramento: this.funcionesUtils.obtenerListaCatalogo(
                'SACRAMENTO',
                this.nemonicoMenu
            ),
            tiposFamilia: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_FAMILIA',
                this.nemonicoMenu
            ),
            organizacionFamiliar: this.funcionesUtils.obtenerListaCatalogo(
                'ORGANIZACION_FAMILIAR',
                this.nemonicoMenu
            ),
            tipoOcupacion: this.funcionesUtils.obtenerListaCatalogo(
                'OCUPACION',
                this.nemonicoMenu
            ),
            nivelAfectivo: this.funcionesUtils.obtenerListaCatalogo(
                'NIVEL_AFECTIVO',
                this.nemonicoMenu
            ),
            tipoDocumento: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_DOCUMENTO_IDENTIFICACION',
                this.nemonicoMenu
            ),
            tipoEstadoCivil: this.funcionesUtils.obtenerListaCatalogo(
                'ESTADO_CIVIL',
                this.nemonicoMenu
            ),
            tipoParentesco: this.funcionesUtils.obtenerListaCatalogo(
                'PARENTESCO',
                this.nemonicoMenu
            ),
            tipoDireccion: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_DIRECCION',
                this.nemonicoMenu
            ),
            tipoSexo: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_SEXO',
                this.nemonicoMenu
            ),
            religion: this.funcionesUtils.obtenerListaCatalogo(
                'RELIGION',
                this.nemonicoMenu
            ),
            modalidadEstudio: this.funcionesUtils.obtenerListaCatalogo('MODALIDAD_ESTUDIO', this.nemonicoMenu),
            nivelEBR: this.funcionesUtils.obtenerListaCatalogo('NIVEL_EBR', this.nemonicoMenu),
            nivelSuperior: this.funcionesUtils.obtenerListaCatalogo('NIVEL_SUPERIOR', this.nemonicoMenu),
            nivelEBA: this.funcionesUtils.obtenerListaCatalogo('NIVEL_EBA', this.nemonicoMenu),
        });

        catalogos$.subscribe({
            next: (result) => {
                this.listaTipoSacramento = result.tipoSacramento;
                this.listaTiposFamilia = result.tiposFamilia;
                this.listaOrganizacionFamiliar = result.organizacionFamiliar;
                this.listaTipoOcupacion = result.tipoOcupacion;
                this.listaNivelAfectivo = result.nivelAfectivo;
                this.listaTipoDocumento = result.tipoDocumento;
                this.listaTipoEstadoCivil = result.tipoEstadoCivil;
                this.listaTipoParentesco = result.tipoParentesco;
                this.listaTipoDirecciones = result.tipoDireccion;
                this.listaModalidadEstudio = result.modalidadEstudio;
                this.listaNivelEBR = result.nivelEBR;
                this.listaNivelSuperior = result.nivelSuperior;
                this.listaNivelEBA = result.nivelEBA;
                this.listaTipoDocumento = this.listaTipoDocumento.filter(
                    (x) => x.nemonico !== 'SIN_DOCUMENTO'
                );
                this.listaTipoSexo = result.tipoSexo;
                this.listaReligion = result.religion;
            },
            error: (err) => {
                console.error('Error al cargar los catálogos:', err);
            },
        });
    }

    get noEsSOA(): boolean {
        return this.centro?.jerarquiaPadre?.nemonico !== 'SOA';
    }

    get esSOA(): boolean {
        return this.centro?.jerarquiaPadre?.nemonico == 'SOA';
    }

    isLoading: boolean = true;
    searchInputControl: UntypedFormControl = new UntypedFormControl();

    keyLabelsTable: any = {
        acciones: 'Acciones',
        nombre: 'Nombre Completo',
        parentesco: 'Parentesco',
        indentificacion: 'Identificación',
    };

    /*
     * Método para dirigirse a la pantalla de creación de evaluación médica
     */
    abrirFormulario() {
        // this.router.navigate(['/administracion/fichaDeIdentificacion/administrar/crear/evaluacionMedica/crear']);
    }

    getKeys() {
        return Object.keys(this.keyLabelsTable);
    }

    /**
     * Maneja el evento de paginación
     */
    handlePageEvent(event: PageEvent) {
        // Actualizar las propiedades de paginación ANTES de la llamada al servicio
        this.page = event.pageIndex;
        this.size = event.pageSize;

        // Realizar la consulta
        this.obtenerDatosFamiliares();
    }

    /**
     * Devuelve las claves para usar en la tabla
     */
    getKeysDatosFamiliares(): string[] {
        return Object.keys(this.keyLabelsTablePersonas);
    }

    editarPersonaRelacionadaDatos(tokenIdentificador: string) {
        this.borrarSubscription();
        this.listaInformacionUbicaciones = [];
        this.dataSourceInformacionUbicaciones = [];
        if (this.table) {
            this.table.renderRows();
        }

        this.esEdicionPersona = true;
        this.editarCrearPersona = true;

        // Indicador de carga
        const load = this.dialogMensajeService.mensajeLoading('Obteniendo la persona relacionada..');

        this.datosFamiliaresService
            .obtenerPersonaRelacionada(tokenIdentificador, this.nemonicoMenu)
            .subscribe({
                next: async (resp: RespuestaPorDefecto<PersonaRelacionadaDTO>) => {
                    if (!resp.exito) {
                        load.close();
                        return;
                    }

                    this.esEdicion = true;
                    this.personaRelacionadaEditando = resp.data;
                    this.empezarEdicionPersona(resp.data);

                    // Cargamos información de ubicación en paralelo
                    try {
                        await this.obtenerInformacionUbicacion(resp.data.idPersonaRelacionada);
                    } catch (error) {
                        console.error('Error al obtener información de ubicación:', error);
                    }

                    // Activamos la carga de documentos
                    this.mostrarSubidaDocumentos = true;
                    if (this.tablaDocumentos) {
                        this.obtenerDocumentos();
                    }

                    load.close();
                },
                error: (error: any) => {
                    load.close();
                    this.datosFamiliaresService.checkError(error);
                }
            });
    }

    empezarEdicionPersona(personaRelacionada: PersonaRelacionadaDTO) {
        this.esEdicionPersona = true;
        this.editarCrearPersona = true;

        // Campos básicos
        this.personaRelacionadaForm.get('nombres').setValue(personaRelacionada.nombres);
        this.personaRelacionadaForm.get('primerApellido').setValue(personaRelacionada.apellidoPaterno);
        this.personaRelacionadaForm.get('segundoApellido').setValue(personaRelacionada.apellidoMaterno);
        this.personaRelacionadaForm.get('estadoCivil').setValue(personaRelacionada.estadoCivil ? personaRelacionada.estadoCivil : '0');
        this.personaRelacionadaForm.get('parentesco').setValue(personaRelacionada.tipoParentesco ? personaRelacionada.tipoParentesco : '0');

        // Configurar sexo
        if (personaRelacionada.tipoSexo) {
            this.personaRelacionadaForm.get('sexo').setValue(personaRelacionada.tipoSexo);
        } else {
            this.personaRelacionadaForm.get('sexo').setValue('0');
        }

        // Configurar modalidad de estudio y los niveles correspondientes
        if (personaRelacionada.modalidadEstudio) {
            this.personaRelacionadaForm.get('modalidadEstudio').setValue(personaRelacionada.modalidadEstudio);
            this.modalidadEstudio = personaRelacionada.modalidadEstudio;

            // Dependiendo de la modalidad de estudio, establecer el nivel correspondiente
            switch (personaRelacionada.modalidadEstudio) {
                case 'MODALIDAD_ESTUDIO_EBR':
                    this.personaRelacionadaForm.get('nivelEBR').setValue(personaRelacionada.nivelEBR || '0');
                    break;
                case 'MODALIDAD_ESTUDIO_EBA':
                    this.personaRelacionadaForm.get('nivelEBA').setValue(personaRelacionada.nivelEBA || '0');
                    break;
                case 'MODALIDAD_ESTUDIO_SUPERIOR':
                    this.personaRelacionadaForm.get('nivelSuperior').setValue(personaRelacionada.nivelSuperior || '0');
                    break;
            }
        }

        // Configurar tipo y número de identificación
        this.personaRelacionadaForm.get('tipoIdentificacion').setValue(
            personaRelacionada.tipoIdentificacion ? personaRelacionada.tipoIdentificacion : '0'
        );

        const numeroDocumentoControl = this.personaRelacionadaForm.get('numeroDocumento');

        if (personaRelacionada.tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
            numeroDocumentoControl.disable();
            numeroDocumentoControl.clearValidators();
        } else {
            numeroDocumentoControl.enable();
        }

        numeroDocumentoControl.setValue(personaRelacionada.numeroDocumento);

        // Configurar fecha de nacimiento
        if (personaRelacionada.fechaNacimiento) {
            this.personaRelacionadaForm.get('fechaNacimiento').setValue(
                personaRelacionada.fechaNacimiento instanceof Date
                    ? personaRelacionada.fechaNacimiento
                    : new Date(personaRelacionada.fechaNacimiento)
            );
        }

        // Campos que faltaban anteriormente
        this.personaRelacionadaForm.get('observaciones').setValue(personaRelacionada.observaciones);
        this.personaRelacionadaForm.get('telefono').setValue(personaRelacionada.telefono);

        // Fijar tipo de ocupación
        this.personaRelacionadaForm.get('tipoOcupacion').setValue(personaRelacionada.tipoOcupacion || '0');

        // Configurar booleanos S/N
        this.personaRelacionadaForm.get('visitaAutorizada').setValue(personaRelacionada.visitaAutorizada || 'N');
        this.personaRelacionadaForm.get('fallecido').setValue(personaRelacionada.fallecido || 'N');
        this.personaRelacionadaForm.get('esTutor').setValue(personaRelacionada.esTutor || 'N');

        // Fijar relación afectiva
        this.personaRelacionadaForm.get('relacionAfectiva').setValue(personaRelacionada.relacionAfectiva || '0');

        // Fijar roles e influencias
        this.personaRelacionadaForm.get('rolesInfluencias').setValue(personaRelacionada.rolesInfluencias || '');

        // Cargar información de ubicación
        this.obtenerInformacionUbicacion(personaRelacionada.idPersonaRelacionada);
    }

    obtenerValorPorTipoInformacionUbicacion(nemonicoTipo: string) {
        for (let infoUbicacion of this.listaInformacionUbicaciones) {
            if (infoUbicacion.tipoInformacionUbicacion == nemonicoTipo) {
                return infoUbicacion.valor
            }
        }
        return ""
    }

    obtenerInformacionUbicacion(idPersonaRelacionada: number): Promise<void> {
        return new Promise((resolve, reject) => {
            this.informacionUbicacionService
                .obtenerInformacionUbicacionesRelacionadas(idPersonaRelacionada)
                .subscribe({
                    next: (response: RespuestaPorDefecto<PaginacionResponse<InformacionUbicacionDTO>>) => {
                        if (!response.exito) {
                            this.dialogMensajeService.mensajeErrorConTitulo(
                                response.titulo,
                                response.mensaje
                            );
                            resolve();
                            return;
                        }

                        this.listaInformacionUbicaciones = response.data.data;
                        this.dataSourceInformacionUbicaciones = this.listaInformacionUbicaciones;
                        resolve();
                    },
                    error: (error: any) => {
                        console.error('Error al obtener información de ubicación:', error);
                        reject(error);
                    }
                });
        });
    }

    eliminarPersonaRelacionada(personaRelacionadaDTO: PersonaRelacionadaDTO) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Estás seguro de eliminar a: "' +
            personaRelacionadaDTO.nombres +
            '" esta operación es irreversible',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    let load = this.dialogMensajeService.mensajeLoading(
                        'Eliminando la ficha de identificación..'
                    );
                    this.datosFamiliaresService
                        .eliminarFichaIdentificacion(personaRelacionadaDTO, this.nemonicoMenu)
                        .subscribe({
                            next: (resp: RespuestaPorDefecto<boolean>) => {
                                load.close();
                                this.dialogMensajeService.mensajeExitoso(
                                    resp.titulo,
                                    resp.mensaje
                                );

                                if (!resp.exito) {
                                    return;
                                }

                                this.obtenerDatosFamiliares();
                            },
                            error: (error: any) => {
                                load.close();

                                this.datosFamiliaresService.checkError(error);
                            },
                        });
                }
            },
        });
    }

    /**
     * Obtiene todas las personas relacionadas
     */
    obtenerDatosFamiliares(): Promise<void> {
        return new Promise((resolve, reject) => {
            let paginacionRequest = new PaginacionRequest();
            paginacionRequest.size = this.size;
            paginacionRequest.page = this.page;
            paginacionRequest.tokenIdentificador = this.uuid_fp;

            this.loadingPersonas = true;

            this.datosFamiliaresService
                .obtenerPersonasRelacionadas(paginacionRequest, this.nemonicoMenu)
                .subscribe({
                    next: (response: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
                        this.loadingPersonas = false;

                        if (!response.exito) {
                            this.dialogMensajeService.mensajeErrorConTitulo(
                                response.titulo,
                                response.mensaje
                            );
                            resolve();
                            return;
                        }

                        if (response.data && response.data.data) {
                            this.listaPersonasRelacionadas = response.data.data;
                            this.totalItems = response.data.totalItems;

                            this.fuenteDatosPersonasRelacionadas.data = this.listaPersonasRelacionadas;

                            // Asegurar que el paginador tenga la configuración correcta
                            if (this.paginadorPersonasRelacionadas) {
                                this.paginadorPersonasRelacionadas.pageIndex = this.page;
                                this.paginadorPersonasRelacionadas.pageSize = this.size;
                                this.paginadorPersonasRelacionadas.length = this.totalItems;
                            }

                        } else {
                            this.listaPersonasRelacionadas = [];
                            this.fuenteDatosPersonasRelacionadas.data = [];
                            this.totalItems = 0;

                            if (this.paginadorPersonasRelacionadas) {
                                this.paginadorPersonasRelacionadas.pageIndex = 0;
                                this.paginadorPersonasRelacionadas.length = 0;
                            }
                        }

                        resolve();
                    },
                    error: (error: any) => {
                        this.loadingPersonas = false;
                        console.error('Error en obtenerDatosFamiliares:', error);
                        this.dialogMensajeService.mensajeError(
                            'Ocurrió un error al cargar las personas relacionadas'
                        );
                        reject(error);
                    }
                });
        });
    }

    cancelarEdicion() {
        this.editarCrearPersona = false;
        this.personaRelacionadaForm.reset();
        // this.fichaIdentificacionDTO = null;

        // this.router.navigate(['/gestion-adolescente/ficha-identificacion']);
    }

    esTutor() {
        return this.obtenerValor("esTutor") == "S" ? true : false
    }

    generarActaCompromisoInscripcion() {
        const refDialogo = this.dialogMensajeService.mensajeConConfirmacion(
            `¿Está seguro de generar el acta de compromiso de inscripción?`,
            "¿Desea continuar?"
        );

        refDialogo.afterClosed().subscribe({
            next: (respuesta: "confirmed" | "cancelled") => {
                if (respuesta == "confirmed") {
                    const dialogoCarga = this.dialogMensajeService.mensajeLoading(`Generando el acta de compromiso de inscripción...`);

                    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
                        .subscribe({
                            next: (datos: ArrayBuffer) => {
                                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                                this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                                    .subscribe({
                                        next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                                            if (!respuestaFicha.exito) {
                                                dialogoCarga.close();
                                                this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
                                                return;
                                            }

                                            const fichaIdentificacion = respuestaFicha.data;
                                            const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                                            const edadActual = fichaIdentificacion.fechaNacimiento ? this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                                            const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

                                            let solicitudPdf = new GeneracionPdfRequest();
                                            solicitudPdf.nemonico = etiquetasModel.ACTA_COMPROMISO_INSCRIPCION;
                                            let fechaHoy = new Date();

                                            solicitudPdf.variables = {
                                                "[DIA]": fechaHoy.getDate() + "",
                                                "[MES-TEXTO]": this.funcionesUtils.convertirNumeroMesATexto(fechaHoy.getMonth()),
                                                "[AÑO]": fechaHoy.getFullYear() + "",
                                                "[NOMBRE-COMPLETO-ADOLESCENTE]": fichaIdentificacion.nombres + " " + fichaIdentificacion.apellidoPaterno + " " + (fichaIdentificacion.apellidoMaterno || ""),
                                                "[TIPO-DOC-ADOLESCENTE]": fichaIdentificacion.nombreTipoDocumento,
                                                "[NUMERO-DOCUMENTO-ADOLESCENTE]": fichaIdentificacion.numeroDocumento,
                                                "[NOMBRE-COMPLETO-TUTOR]": (this.obtenerValor("nombres") || '') + " " + (this.obtenerValor("primerApellido") || '') + " " + (this.obtenerValor("segundoApellido") || ''),
                                                "[TIPO-DOC-TUTOR]": this.listaTipoDocumento.find(x => x.nemonico == this.obtenerValor("tipoIdentificacion"))?.nombre || "",
                                                "[NUMERO-DOCUMENTO-TUTOR]": this.obtenerValor("numeroDocumento") || '',
                                                "[RELACION-TUTOR]": this.listaTipoParentesco.find(x => x.nemonico == this.obtenerValor("parentesco"))?.nombre || "",
                                                "[DOMICILIO]": this.obtenerValorPorTipoInformacionUbicacion("INFORMACION_PERSONAL_DIRECCION"),
                                                "[CELULAR]": this.obtenerValorPorTipoInformacionUbicacion("INFORMACION_PERSONAL_TELEFONO") || (this.obtenerValor("telefono") || ''),
                                                "[WHATSAPP]": this.obtenerValorPorTipoInformacionUbicacion("INFORMACION_PERSONAL_TELEFONO") || (this.obtenerValor("telefono") || ''),
                                                "[CORREO-ELECTRONICO]": this.obtenerValorPorTipoInformacionUbicacion("INFORMACION_PERSONAL_CORREO"),
                                            };

                                            this.pdfService.generarPdf(solicitudPdf, "").subscribe({
                                                next: (respuesta: RespuestaPorDefecto<string>) => {
                                                    dialogoCarga.close();
                                                    if (!respuesta.exito) {
                                                        console.error('Error al generar PDF:', respuesta);
                                                        this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                                                        return;
                                                    }

                                                    const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
                                                    window.open(url);
                                                },
                                                error: (error: any) => {
                                                    dialogoCarga.close();
                                                    console.error('Error al generar PDF:', error);
                                                    this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                                                }
                                            });
                                        },
                                        error: (error: any) => {
                                            dialogoCarga.close();
                                            console.error('Error al obtener ficha:', error);
                                            this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
                                        }
                                    });
                            },
                            error: (error) => {
                                dialogoCarga.close();
                                console.error('Error al cargar imagen:', error);
                                this.dialogMensajeService.mensajeError('Error al cargar la imagen del logo');
                            }
                        });
                }
            }
        });
    }

    /**
     * Ejecuta la acción para guardar o actualizar una persona relacionada
     * Implementa control para evitar procesamiento duplicado
     */
    ejecutarAccionPersonaRelacionada() {
        // Si ya está procesando una solicitud, ignorar clicks adicionales
        if (this.estaProcesandoPersona) {
            return;
        }

        // Aplicar truncamiento automático antes de validar el formulario
        this.asegurarLongitudesValidasParaGuardar();

        // Establecer bandera de procesamiento
        this.estaProcesandoPersona = true;
        this.checkFormValidity(this.personaRelacionadaForm);

        let personaRelacionadaDTO = new PersonaRelacionadaDTO();
        personaRelacionadaDTO.nombres = this.obtenerValor('nombres');
        personaRelacionadaDTO.apellidoPaterno = this.capitalizarPalabras(this.obtenerValor('primerApellido'));
        personaRelacionadaDTO.apellidoMaterno = this.capitalizarPalabras(this.obtenerValor('segundoApellido'));

        personaRelacionadaDTO.estadoCivil = this.obtenerValor('estadoCivil');
        personaRelacionadaDTO.modalidadEstudio = this.obtenerValor('modalidadEstudio');
        switch (personaRelacionadaDTO.modalidadEstudio) {
            case 'MODALIDAD_ESTUDIO_EBR':
                personaRelacionadaDTO.nivelEBR = this.obtenerValor('nivelEBR');
                break;
            case 'MODALIDAD_ESTUDIO_SUPERIOR':
                personaRelacionadaDTO.nivelSuperior = this.obtenerValor('nivelSuperior');
                break;
            case 'MODALIDAD_ESTUDIO_EBA':
                personaRelacionadaDTO.nivelEBA = this.obtenerValor('nivelEBA');
                break;
        }
        personaRelacionadaDTO.tipoParentesco = this.obtenerValor('parentesco');
        personaRelacionadaDTO.tipoIdentificacion = this.obtenerValor('tipoIdentificacion');
        personaRelacionadaDTO.tipoSexo = this.obtenerValor('sexo');

        personaRelacionadaDTO.observaciones = this.obtenerValor('observaciones');
        personaRelacionadaDTO.telefono = this.obtenerValor('telefono');
        personaRelacionadaDTO.tipoOcupacion = this.obtenerValor('tipoOcupacion');
        personaRelacionadaDTO.numeroDocumento = this.obtenerValor('numeroDocumento');

        personaRelacionadaDTO.fechaNacimiento = this.obtenerValor('fechaNacimiento');

        personaRelacionadaDTO.esTutor = this.obtenerValor('esTutor') || 'N';
        personaRelacionadaDTO.fallecido = this.obtenerValor('fallecido') || 'N';
        personaRelacionadaDTO.visitaAutorizada = this.obtenerValor('visitaAutorizada') || 'N';
        personaRelacionadaDTO.relacionAfectiva = this.obtenerValor('relacionAfectiva');
        personaRelacionadaDTO.rolesInfluencias = this.obtenerValor('rolesInfluencias');

        personaRelacionadaDTO.esEdicion = this.esEdicionPersona;

        // Guardar referencia al token temporal si estamos creando
        const temporalToken = !this.esEdicionPersona ? this.personaRelacionadaEditando?.tokenIdentificador : null;

        // Validación para tutores
        if (personaRelacionadaDTO.esTutor == 'S' && this.listaInformacionUbicaciones.length < 1) {
            this.dialogMensajeService.mensajeError(
                'Por favor ingrese una información de ubicación, esto debido a que es tutor.'
            );
            // Restablecer bandera de procesamiento en caso de error de validación
            this.estaProcesandoPersona = false;
            return;
        } else {
            // Deshabilitar el formulario mientras se procesa
            this.personaRelacionadaForm.disable();

            personaRelacionadaDTO.informacionUbicaciones = this.listaInformacionUbicaciones;
            personaRelacionadaDTO.informacionUbicacionesEliminar = this.listaInformacionUbicacionesEliminacion;

            if (this.esEdicionPersona) {
                personaRelacionadaDTO.tokenIdentificador = this.personaRelacionadaEditando.tokenIdentificador;
            }
            personaRelacionadaDTO.tokenIdentificadorFicha = this.uuid_fp;

            // Llamada al servicio
            this.datosFamiliaresService
                .crearPersonaRelacionada(personaRelacionadaDTO, this.nemonicoMenu)
                .subscribe({
                    next: (response: RespuestaPorDefecto<PersonaRelacionadaDTO>) => {
                        // Volver a habilitar el formulario
                        this.personaRelacionadaForm.enable();
                        // Restablecer bandera de procesamiento al completar
                        this.estaProcesandoPersona = false;

                        if (!response.exito) {
                            this.datosFamiliaresService.checkError(response);
                            return;
                        }

                        // Si estamos creando y hay un token temporal, mover documentos
                        if (!this.esEdicionPersona && temporalToken && temporalToken.startsWith('temp-')) {
                            // Usar el método obtenerDocumentos en lugar de moverDocumentos
                            // Ya que moverDocumentos no existe en el servicio
                            const requestDocumentos = new DatosFamiliaresDocumentosRequest();
                            requestDocumentos.page = 0;
                            requestDocumentos.size = 100; // Un tamaño suficientemente grande
                            requestDocumentos.tokenIdentificadorDatosFamiliares = temporalToken;

                            this.datosFamiliaresService.obtenerDocumentos(requestDocumentos, '')
                                .subscribe({
                                    next: (resp: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {
                                        if (resp.exito && resp.data && resp.data.data && resp.data.data.length > 0) {
                                            console.log(`Documentos temporales encontrados: ${resp.data.data.length}`);
                                            // Aquí podrías implementar alguna lógica para asociar 
                                            // los documentos al nuevo registro, pero por ahora
                                            // solo mostramos un mensaje informativo
                                        }
                                    },
                                    error: (error: any) => {
                                        console.error('Error al obtener documentos:', error);
                                    }
                                });
                        }

                        this.dialogMensajeService.mensajeExitoso(
                            response.titulo,
                            response.mensaje
                        );

                        this.editarCrearPersona = false;
                        this.esEdicionPersona = false;
                        this.recargar();
                        this.obtenerDatosFamiliares();
                    },
                    error: (error: any) => {
                        this.datosFamiliaresService.checkError(error);
                        this.personaRelacionadaForm.enable();
                        // Restablecer bandera de procesamiento en caso de error
                        this.estaProcesandoPersona = false;
                    },
                });
        }
    }

    /**
     * Ejecuta la acción para guardar los datos familiares
     * Implementa control para evitar procesamiento duplicado
     */
    ejecutarAccionDatosFamiliares() {
        // Si ya está procesando una solicitud, ignorar clicks adicionales
        if (this.estaProcesandoDatosFamiliares) {
            return;
        }

        // Establecer bandera de procesamiento
        this.estaProcesandoDatosFamiliares = true;

        let datos = new DatosFamiliaresDTO();

        // NUEVO: Establecer si es edición basado en la variable de clase
        datos.esEdicion = this.modoEdicionDatosFamiliares;

        // Deshabilitar el formulario mientras se procesa
        this.datosFamiliaresForm.disable();

        // Llenar el DTO con los datos del formulario
        datos.tipoFamilia = this.obtenerValorFormDatos('tipoFamilia');
        datos.organizacionFamiliar = this.obtenerValorFormDatos('organizacionFamiliar');
        datos.partidaNacimiento = this.obtenerValorFormDatos('partidaNacimiento');
        datos.relacionIntraFamiliarFilial = this.obtenerValorFormDatos('relacionIntraFamiliarFilial');
        datos.relacionIntraFamiliarPadres = this.obtenerValorFormDatos('relacionIntraFamiliarPadres');
        datos.relacionIntraFamiliarParentales = this.obtenerValorFormDatos('relacionIntraFamiliarParentales');
        datos.relacionIntraFamiliarPareja = this.obtenerValorFormDatos('relacionIntraFamiliarPareja');
        datos.religion = this.obtenerValorFormDatos('religion');
        datos.tipoSacramento = this.obtenerValorFormDatos('tipoSacramento');
        datos.otroSacramento = this.obtenerValorFormDatos('otroSacramento');
        datos.ejercicioAutoridad = this.obtenerValorFormDatos('ejercicioAutoridad');
        datos.entornoFamiliar = this.obtenerValorFormDatos('entornoFamiliar');
        datos.observacionesRelacionIntrafamiliar = this.obtenerValorFormDatos('observacionesRelacionIntrafamiliar');
        datos.causaAusenciaPadres = this.obtenerValorFormDatos('causaAusenciaPadres');
        datos.tokenIdentificadorFicha = this.uuid_fp;

        // Llamada al servicio
        this.datosFamiliaresService.crearDatosPersonales(datos, this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<DatosFamiliaresDTO>) => {
                // Volver a habilitar el formulario
                this.datosFamiliaresForm.enable();
                // Restablecer bandera de procesamiento al completar
                this.estaProcesandoDatosFamiliares = false;

                if (!response.exito) {
                    this.datosFamiliaresService.checkError(response);
                    return;
                }

                this.dialogMensajeService.mensajeExitoso(
                    response.titulo,
                    response.mensaje
                );
            },
            error: (error: any) => {
                this.datosFamiliaresService.checkError(error);
                this.datosFamiliaresForm.enable();
                // Restablecer bandera de procesamiento en caso de error
                this.estaProcesandoDatosFamiliares = false;
            },
        });
    }

    obtenerSituacionFamiliar(): Promise<void> {
        return new Promise((resolve, reject) => {
            this.datosFamiliaresService
                .obtenerDatosPersonales(this.uuid_fp, this.nemonicoMenu)
                .subscribe({
                    next: (resp: RespuestaPorDefecto<DatosFamiliaresDTO>) => {
                        if (!resp.exito) {
                            // Si no hay éxito, probablemente no hay datos = es creación
                            this.modoEdicionDatosFamiliares = false;
                            console.log('DEBUG: No hay datos familiares existentes - MODO CREACIÓN');
                            resolve(); // Resolvemos aunque haya error para no bloquear
                            return;
                        }

                        // Verificar si realmente hay datos
                        let datos = resp.data;
                        if (datos && (datos.tipoFamilia || datos.organizacionFamiliar || datos.religion)) {
                            // SI HAY DATOS REALES, ES MODO EDICIÓN
                            this.modoEdicionDatosFamiliares = true;
                            console.log('DEBUG: Datos familiares existentes encontrados - MODO EDICIÓN');

                            this.datosFamiliaresForm.patchValue({
                                tipoFamilia: datos.tipoFamilia ?? datos.tipoFamilia,
                                organizacionFamiliar: datos.organizacionFamiliar ?? datos.organizacionFamiliar,
                                partidaNacimiento: datos.partidaNacimiento ?? datos.partidaNacimiento,
                                relacionIntraFamiliarFilial: datos.relacionIntraFamiliarFilial ?? datos.relacionIntraFamiliarFilial,
                                relacionIntraFamiliarPadres: datos.relacionIntraFamiliarPadres ?? datos.relacionIntraFamiliarPadres,
                                relacionIntraFamiliarParentales: datos.relacionIntraFamiliarParentales ?? datos.relacionIntraFamiliarParentales,
                                relacionIntraFamiliarPareja: datos.relacionIntraFamiliarPareja ?? datos.relacionIntraFamiliarPareja,
                                religion: datos.religion ?? datos.religion,
                                otroSacramento: datos.otroSacramento ?? datos.otroSacramento,
                                ejercicioAutoridad: datos.ejercicioAutoridad ?? datos.ejercicioAutoridad,
                                entornoFamiliar: datos.entornoFamiliar ?? datos.entornoFamiliar,
                                observacionesRelacionIntrafamiliar: datos.observacionesRelacionIntrafamiliar ?? datos.observacionesRelacionIntrafamiliar,
                                causaAusenciaPadres: datos.causaAusenciaPadres ?? datos.causaAusenciaPadres,
                                tipoSacramento: datos.tipoSacramento ?? datos.tipoSacramento,
                            });
                        } else {
                            // Si no hay datos reales, es creación
                            this.modoEdicionDatosFamiliares = false;
                            console.log('DEBUG: Respuesta exitosa pero sin datos reales - MODO CREACIÓN');
                        }

                        resolve();
                    },
                    error: (error: any) => {
                        // SI HAY ERROR, PROBABLEMENTE NO HAY DATOS = ES CREACIÓN
                        this.modoEdicionDatosFamiliares = false;
                        console.log('DEBUG: Error al obtener datos familiares - MODO CREACIÓN');
                        console.error('Error en obtenerSituacionFamiliar:', error);
                        this.datosFamiliaresService.checkError(error);
                        reject(error);
                    }
                });
        });
    }

    private obtenerValor(key: string) {
        return this.personaRelacionadaForm.get(key)?.value;
    }

    aniadirFilaInformacion() {
        const dialogRef = this.dialog.open(ModalEditarInformacionComponent, {
            data: {},
            width: '600px',
        });

        dialogRef.afterClosed().subscribe(async (result) => {
            if (result && !result.esEdicion) {
                const datosActualizados = this.listaInformacionUbicaciones;
                datosActualizados.push(result);
                this.dataSourceInformacionUbicaciones = datosActualizados;
                // this.cd.detectChanges();
                this.table.renderRows();
            }
        });
    }

    getKeyInformacion() {
        return Object.keys(this.keyInformacionUbicacion);
    }

    editarFilaInformacion(informacion: InformacionUbicacionDTO) {
        const dialogRef = this.dialog.open(ModalEditarInformacionComponent, {
            data: {
                idPersonaRelacionada:
                    this.personaRelacionadaEditando.idPersonaRelacionada,
                informacion: informacion,
            },
            width: '600px',
        });

        dialogRef.afterClosed().subscribe(async (result) => {
            if (result.esEdicion) {
                // this.obtenerInformacionUbicacion(
                //     this.personaRelacionadaEditando.idPersonaRelacionada
                // );
                const datosActualizados = this.listaInformacionUbicaciones;
                const index = this.listaInformacionUbicaciones.findIndex(
                    (x) => x.tokenIdentificador == result.tokenIdentificador
                );
                if (index !== -1) {
                    datosActualizados[index] = result;
                    this.listaInformacionUbicaciones[index] = result;
                }
                this.dataSourceInformacionUbicaciones = datosActualizados;
                this.table.renderRows();
            }
        });
    }

    eliminarInformacionUbicacion(idInformacionUbicacion: string) {
        let informacion = new InformacionUbicacionDTO();
        informacion.tokenIdentificador = idInformacionUbicacion;
        this.informacionUbicacionService
            .eliminarInformacionUbicacion(informacion)
            .subscribe({
                next: (resp: RespuestaPorDefecto<boolean>) => {
                    this.dialogMensajeService.mensajeExitoso(
                        resp.titulo,
                        resp.mensaje
                    );

                    if (!resp.exito) {
                        return;
                    }

                    this.obtenerInformacionUbicacion(
                        this.personaRelacionadaEditando.idPersonaRelacionada
                    );
                },
                error: (error: any) => {
                    this.datosFamiliaresService.checkError(error);
                },
            });
    }

    construirFormPersona() {
        // Validador personalizado interno
        const soloLetrasYEspacios = (): ValidatorFn => {
            return (control: AbstractControl): { [key: string]: any } | null => {
                if (!control.value) {
                    return null;
                }
                const regex = /^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]+$/;
                return regex.test(control.value) ? null : { 'formatoInvalido': { value: control.value } };
            };
        };

        this.personaRelacionadaForm = this.formBuilder.group({
            primerApellido: ['', [
                Validators.required,
                soloLetrasYEspacios()
            ]],
            segundoApellido: ['', [
                Validators.required,
                soloLetrasYEspacios()
            ]],
            fechaNacimiento: [null],
            edad: [null as number],
            sexo: ['0', [Validators.required]],
            modalidadEstudio: ['0', [Validators.required]],
            nivelEBR: ['0'],
            nivelSuperior: ['0'],
            nivelEBA: ['0'],
            observaciones: ['',],
            tipoIdentificacion: ['0', [Validators.required]],
            numeroDocumento: [''],
            telefono: ['', [
                Validators.maxLength(9),
                Validators.pattern('^[0-9]*$')
            ]],
            estadoCivil: ['0',],
            parentesco: ['0',],
            visitaAutorizada: ['',],
            fallecido: ['',],
            esTutor: ['',],
            relacionAfectiva: ['',],
            rolesInfluencias: ['',],
            nombres: ['', [
                Validators.required,
                soloLetrasYEspacios()
            ]],
            tipoOcupacion: ['0',],
        });
    }

    borrarDireccion(id_temp: number, id: string) {
        if (id) {
            const datosActualizados = this.listaInformacionUbicaciones.filter(
                (item) => item.tokenIdentificador !== id
            );
            this.dataSourceInformacionUbicaciones = datosActualizados;
            this.listaInformacionUbicaciones = datosActualizados;

            this.listaInformacionUbicacionesEliminacion.push(id);
        } else {
            const datosActualizados = this.listaInformacionUbicaciones.filter(
                (item) => item.id_temporal !== id_temp
            );
            this.listaInformacionUbicaciones = datosActualizados;
            this.dataSourceInformacionUbicaciones = datosActualizados;
        }
    }

    recargar() {
        this.editarCrearPersona = false;
        this.esEdicionPersona = false;
        this.personaRelacionadaEditando = null;
        this.listaInformacionUbicaciones = [];
        this.dataSourceInformacionUbicaciones = null;
        this.listaInformacionUbicacionesEliminacion = [];
    }

    async obtenerListaCatalogo(nemonicoPadre: string) {
        this.catalogoService
            .obtenerHijos(
                nemonicoPadre,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }
                    if (nemonicoPadre == 'ESTADO_CIVIL') {
                        this.listaTipoEstadoCivil = response.data;
                    } else if (nemonicoPadre == 'PARENTESCO') {
                        this.listaTipoParentesco = response.data;
                    } else if (
                        nemonicoPadre == 'TIPO_DOCUMENTO_IDENTIFICACION'
                    ) {
                        this.listaTipoDocumento = response.data;
                    } else if (nemonicoPadre == 'TIPO_DIRECCION') {
                        this.listaTipoDirecciones = response.data;
                    } else if (nemonicoPadre == 'NIVEL_AFECTIVO') {
                        this.listaNivelAfectivo = response.data;
                    }
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    construirFormDatosFamiliares() {
        this.datosFamiliaresForm = this.formBuilder.group({
            tipoFamilia: [''],
            organizacionFamiliar: [''],
            ejercicioAutoridad: [''],
            entornoFamiliar: [''],
            relacionIntraFamiliarPadres: [''],
            relacionIntraFamiliarFilial: [''],
            relacionIntraFamiliarParentales: [''],
            relacionIntraFamiliarPareja: [''],
            partidaNacimiento: [''],
            // bautismo: ['', ],
            // primeraComunion: ['', ],
            // confirmacion: ['', ],
            observacionesRelacionIntrafamiliar: [''],
            causaAusenciaPadres: [''],
            religion: [''],
            otroSacramento: [''],
            tipoSacramento: [''],
        });
    }

    obtenerValorFormDatos(key: string) {
        return this.datosFamiliaresForm.get(key).value;
    }

    checkFormValidity(formGroup: FormGroup): void {
        Object.keys(formGroup.controls).forEach((key) => {
            const control = formGroup.get(key);
            if (control.invalid) {
                console.error(`Control "${key}" es inválido`, control.errors);
            }
        });
    }

    /**
     * Método para prevenir la entrada de caracteres especiales en los campos de texto
     * @param event El evento keydown del teclado
     */
    prevenirCaracteresEspeciales(event: KeyboardEvent): void {
        // Lista de teclas permitidas de control y navegación
        const allowedKeys = [
            'Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
            'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown',
            'Home', 'End', ' ' // Espacio
        ];

        // Permitir teclas de control y navegación
        if (allowedKeys.includes(event.key)) {
            return;
        }

        // Permitir combinaciones con Ctrl para copiar, pegar, etc.
        if (event.ctrlKey) {
            return;
        }

        // Expresión regular para validar letras (incluyendo acentuadas), espacios, y ñ/Ñ
        const regex = /^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]$/;

        // Si no pasa la validación, prevenir la acción por defecto
        if (!regex.test(event.key)) {
            event.preventDefault();
        }
    }

    /**
     * Método mejorado para permitir solo números en ciertos campos
     * @param event El evento keydown del teclado
     */
    soloNumero(event: KeyboardEvent): void {
        // Lista de teclas permitidas para navegación y edición
        const allowedKeys = [
            'Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
            'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown',
            'Home', 'End'
        ];

        // Permitir teclas de control y navegación
        if (allowedKeys.includes(event.key)) {
            return;
        }

        // Permitir combinaciones con Ctrl para copiar, pegar, etc.
        if (event.ctrlKey) {
            return;
        }

        // Verificar si el tipo de documento actual requiere solo números
        const tipoDocumentoActual = this.personaRelacionadaForm.get('tipoIdentificacion')?.value;

        // Para DNI estrictamente solo números
        if (tipoDocumentoActual === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
            const isNumberKey = event.key >= '0' && event.key <= '9';
            if (!isNumberKey) {
                event.preventDefault();
            }
        }
        // Para otros tipos de documento, permitir alfanuméricos pero no caracteres especiales
        else {
            const regex = /^[a-zA-Z0-9]$/;
            if (!regex.test(event.key)) {
                event.preventDefault();
            }
        }
    }

    /**
     * Genera e imprime el PDF de composición familiar
     */
    async imprimirFicha() {
        try {
            const dialogoCarga = this.dialogMensajeService.mensajeLoading('Generando PDF...');

            this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
                .subscribe({
                    next: async (datos: ArrayBuffer) => {
                        const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                        const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                        try {
                            const datosCabecera = await this.obtenerDatosCabecera();

                            let tablaPersonasRelacionadas = new TablaPlantilla();
                            tablaPersonasRelacionadas.encabezados = [
                                'Nombre completo', 'Parentesco', 'Identificación', 'Edad', 'Fecha de nacimiento'
                            ];

                            tablaPersonasRelacionadas.filas = this.listaPersonasRelacionadas.map(persona => {
                                let nombreCompleto;

                                if (persona.nombres) {
                                    nombreCompleto = persona.nombres;
                                } else {
                                    const partes: string[] = [];

                                    if (persona.primerNombre) partes.push(persona.primerNombre);
                                    if (persona.segundoNombre) partes.push(persona.segundoNombre);
                                    if (persona.apellidoPaterno) partes.push(persona.apellidoPaterno);
                                    if (persona.apellidoMaterno) partes.push(persona.apellidoMaterno);

                                    if (partes.length === 0) {
                                        if (persona.primerApellido) partes.push(persona.primerApellido);
                                        if (persona.segundoApellido) partes.push(persona.segundoApellido);
                                    }

                                    nombreCompleto = partes.length > 0 ? partes.join(' ') : 'No especificado';
                                }

                                let parentesco = 'No especificado';
                                if (persona.tipoParentesco) {
                                    const parentescoEncontrado = this.listaTipoParentesco.find(p => p.nemonico === persona.tipoParentesco);
                                    if (parentescoEncontrado) {
                                        parentesco = parentescoEncontrado.descripcion || parentescoEncontrado.nombre || 'No especificado';
                                    }
                                }

                                return {
                                    'Nombre completo': this.funcionesUtils.escaparHTML(nombreCompleto),
                                    'Parentesco': this.funcionesUtils.escaparHTML(parentesco),
                                    'Identificación': this.funcionesUtils.escaparHTML(persona.numeroDocumento || 'No especificado'),
                                    'Edad': this.funcionesUtils.escaparHTML(persona.fechaNacimiento ? this.funcionesUtils.getEdad(persona.fechaNacimiento.toString()).toString() : 'No especificado'),
                                    'Fecha de nacimiento': this.funcionesUtils.escaparHTML(persona.fechaNacimiento ? this.funcionesUtils.formatearFecha(persona.fechaNacimiento) : 'No especificado')
                                };
                            });

                            if (tablaPersonasRelacionadas.filas.length === 0) {
                                tablaPersonasRelacionadas.filas.push({
                                    'Nombre completo': '-',
                                    'Parentesco': 'No hay personas relacionadas registradas',
                                    'Identificación': '-',
                                    'Edad': '-',
                                    'Fecha de nacimiento': '-'
                                });
                            }

                            const solicitudPdf = new GeneracionPdfRequest();
                            solicitudPdf.nemonico = 'FORMULARIO_COMPOSICION_FAMILIAR';

                            solicitudPdf.variables = {
                                ...datosCabecera,
                                "[IMG_BASE64]": imagenBase64,
                                "[FECHA-REGISTRO]": this.funcionesUtils.escaparHTML(this.funcionesUtils.formatearFecha(new Date())),
                                "[HORA-REGISTRO]": this.funcionesUtils.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                                "[CENTRO]": this.funcionesUtils.escaparHTML(this.centro?.nombre || 'No especificado'),

                                "[TABLA-PERSONAS-RELACIONADAS]": JSON.stringify(tablaPersonasRelacionadas),

                                "[TIPO-FAMILIA]": this.funcionesUtils.escaparHTML(this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                    this.obtenerValorFormDatos("tipoFamilia"),
                                    this.listaTiposFamilia
                                ) || 'No especificado'),

                                "[ORGANIZACION-FAMILIAR]": this.funcionesUtils.escaparHTML(this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                    this.obtenerValorFormDatos("organizacionFamiliar"),
                                    this.listaOrganizacionFamiliar
                                ) || 'No especificado'),

                                "[PARTIDA-NACIMIENTO]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("partidaNacimiento") === 'S' ? 'Sí' : 'No'),

                                "[RELACION-INTRAFAMILIAR-PADRES]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("relacionIntraFamiliarPadres") === 'S' ? 'Sí' : 'No'),
                                "[RELACION-INTRAFAMILIAR-FILIAL]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("relacionIntraFamiliarFilial") === 'S' ? 'Sí' : 'No'),
                                "[RELACION-INTRAFAMILIAR-PARENTALES]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("relacionIntraFamiliarParentales") === 'S' ? 'Sí' : 'No'),
                                "[RELACION-INTRAFAMILIAR-PAREJA]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("relacionIntraFamiliarPareja") === 'S' ? 'Sí' : 'No'),

                                "[RELIGION]": this.funcionesUtils.escaparHTML(this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                    this.obtenerValorFormDatos("religion"),
                                    this.listaReligion
                                ) || 'No especificado'),

                                "[TIPO-SACRAMENTO]": this.funcionesUtils.escaparHTML(this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                    this.obtenerValorFormDatos("tipoSacramento"),
                                    this.listaTipoSacramento
                                ) || 'No especificado'),

                                "[OTRO-SACRAMENTO]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("otroSacramento") || 'No especificado'),

                                "[EJERCICIO-AUTORIDAD]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("ejercicioAutoridad") || 'No especificado'),
                                "[ENTORNO-FAMILIAR]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("entornoFamiliar") || 'No especificado'),

                                "[OBSERVACIONES-RELACION-INTRAFAMILIAR]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("observacionesRelacionIntrafamiliar") || 'No especificado'),
                                "[CAUSA-AUSENCIA-PADRES]": this.funcionesUtils.escaparHTML(this.obtenerValorFormDatos("causaAusenciaPadres") || 'No especificado')
                            };

                            this.pdfService.generarPdf(solicitudPdf, etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR).subscribe({
                                next: (respuesta: RespuestaPorDefecto<string>) => {
                                    dialogoCarga.close();

                                    if (!respuesta.exito) {
                                        this.dialogMensajeService.mensajeError(
                                            'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                                        );
                                        return;
                                    }

                                    const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
                                    window.open(url);
                                },
                                error: (error: any) => {
                                    dialogoCarga.close();
                                    console.error('Error al generar PDF:', error);
                                    this.dialogMensajeService.mensajeError(
                                        'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                                    );
                                }
                            });
                        } catch (error) {
                            dialogoCarga.close();
                            console.error('Error al procesar datos:', error);
                            this.dialogMensajeService.mensajeError(
                                'Hubo un problema al procesar los datos. Inténtalo de nuevo.'
                            );
                        }
                    },
                    error: (error) => {
                        dialogoCarga.close();
                        console.error('Error al cargar imagen:', error);
                        this.dialogMensajeService.mensajeError(
                            'Error al cargar la imagen del logo.'
                        );
                    }
                });
        } catch (error) {
            console.error('Error al imprimir ficha:', error);
            this.dialogMensajeService.mensajeError(
                'Hubo un problema al generar el PDF.'
            );
        }
    }

    // Método para obtener datos de la cabecera
    private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
        return new Promise((resolve, reject) => {
            this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
                next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                    if (!response.exito) {
                        reject('Error al obtener la ficha de identificación');
                        return;
                    }

                    const fichaIdentificacion = response.data;
                    const datosCabecera = {
                        "[NOMBRES-APELLIDOS]": `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim(),
                        "[DNI]": fichaIdentificacion.numeroDocumento || '',
                        "[LUGAR-FECHA-NACIMIENTO]": `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`,
                        "[EDAD]": `${this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento)}`
                    };

                    resolve(datosCabecera);
                },
                error: (error: any) => {
                    reject(error);
                }
            });
        });
    }

    onModalidadEstudioChange(event: any) {
        this.modalidadEstudio = event.value;

        // Limpiar los valores de los otros niveles
        if (this.modalidadEstudio !== 'EBR') {
            this.personaRelacionadaForm.get('nivelEBR').setValue('0');
        }
        if (this.modalidadEstudio !== 'SUPERIOR') {
            this.personaRelacionadaForm.get('nivelSuperior').setValue('0');
        }
        if (this.modalidadEstudio !== 'EBA') {
            this.personaRelacionadaForm.get('nivelEBA').setValue('0');
        }
    }

    private capitalizarPalabras(texto: string): string {
        return texto.replace(/\b\w/g, letra => letra.toUpperCase());
    }

    pageEventDocumentos(event: PageEvent) {
        this.tablaDocumentos.page = event.pageIndex;
        this.tablaDocumentos.pageSize = event.pageSize;
        this.obtenerDocumentos();
    }

    eliminacionDocumento(documentoDTO: DocumentoDTO) {
        let load = this.dialogMensajeService.mensajeLoading("Quitando el documento: " + documentoDTO.nombre + " del detalle..");
        let datosFamiliaresDocumentoDTO = new DatosFamiliaresDocumentoDTO();
        datosFamiliaresDocumentoDTO.documentoDTO = documentoDTO;
        datosFamiliaresDocumentoDTO.tokenIdentificadorDatosFamiliares = this.personaRelacionadaEditando.tokenIdentificador;

        this.datosFamiliaresService.eliminarDocumento(
            datosFamiliaresDocumentoDTO,
            this.nemonicoMenu
        ).subscribe({
            next: (response: RespuestaPorDefecto<DatosFamiliaresDocumentoDTO>) => {
                load.close();
                if (!response.exito) {
                    this.datosFamiliaresService.checkError(response);
                    return;
                }
                this.obtenerDocumentos();
            },
            error: (error: any) => {
                load.close();
                this.datosFamiliaresService.checkError(error);
            }
        });
    }

    edicionEvent(exito: boolean) {
        if (exito) {
            this.obtenerDocumentos();
        }
    }

    obtenerDocumentos() {
        if (!this.tablaDocumentos || !this.personaRelacionadaEditando?.tokenIdentificador) {
            return;
        }

        let page = this.tablaDocumentos.page;
        let pageSize = this.tablaDocumentos.pageSize;

        let datosFamiliaresDocumentosRequest = new DatosFamiliaresDocumentosRequest();
        datosFamiliaresDocumentosRequest.page = page;
        datosFamiliaresDocumentosRequest.size = pageSize;
        datosFamiliaresDocumentosRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
        datosFamiliaresDocumentosRequest.tokenIdentificadorDatosFamiliares = this.personaRelacionadaEditando.tokenIdentificador;

        this.datosFamiliaresService.obtenerDocumentos(
            datosFamiliaresDocumentosRequest,
            this.nemonicoMenu
        ).subscribe({
            next: (response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {
                if (!response.exito) {
                    this.datosFamiliaresService.checkError(response);
                    return;
                }

                if (response.data?.data) {
                    this.tablaDocumentos.actualizarTabla(
                        response.data.data,
                        response.data.totalItems
                    );
                }
            },
            error: (error: any) => {
                this.datosFamiliaresService.checkError(error);
            }
        });
    }

    obtenerTiposDeDocumentos(): Promise<void> {
        return new Promise((resolve, reject) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(
                etiquetasModel.SECCION_FICHA_IDENT_PERSONA_RELACIONADA,
                this.nemonicoMenu
            ).subscribe({
                next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
                        resolve(); // Resolvemos aunque haya error para no bloquear
                        return;
                    }

                    let tiposArchivos = response.data;
                    if (tiposArchivos.length === 0) {
                        console.warn("No se ha configurado los tipos de documentos para esta sección");
                        resolve();
                        return;
                    }

                    this.tiposDeDocumentosSistema =
                        tiposArchivos.map((tipoArch) => {
                            let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
                            let tipoDeDocumento = new TipoDeDocumento();
                            tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
                            tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
                            tipoDeDocumento.requerido = tipoArch.requerido;
                            tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
                            tipoDeDocumento.nombre = catalogoTipoDoc.nombre;
                            return tipoDeDocumento;
                        });
                    resolve();
                },
                error: (error: any) => {
                    console.error('Error al obtener tipos de documentos:', error);
                    this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
                    reject(error);
                }
            });
        });
    }

    subirArchivosEvent(documentos: DocumentoSubido[]) {
        if (!this.personaRelacionadaEditando?.tokenIdentificador) {
            this.dialogMensajeService.mensajeError("No se puede subir documentos porque no hay una persona relacionada seleccionada");
            return;
        }

        if (documentos && documentos.length > 0) {
            for (let documentoSubido of documentos) {
                let datosFamiliaresDocumentoDTO = new DatosFamiliaresDocumentoDTO();
                datosFamiliaresDocumentoDTO.tokenIdentificadorDatosFamiliares = this.personaRelacionadaEditando.tokenIdentificador;
                datosFamiliaresDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

                let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
                    documentoSubido.documento.name
                );

                this.datosFamiliaresService.subirDocumento(
                    documentoSubido.documento,
                    datosFamiliaresDocumentoDTO,
                    this.nemonicoMenu
                ).subscribe({
                    next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
                        load.close();
                        if (!response.exito) {
                            this.datosFamiliaresService.checkError(response);
                            return;
                        }

                        this.obtenerDocumentos();
                    },
                    error: (error: any) => {
                        load.close();
                        this.datosFamiliaresService.checkError(error);
                    }
                });
            }
        } else {
            this.dialogMensajeService.mensajeError("No se obtuvieron documentos para ser subidos");
        }
    }

    subirDocumentoParaPersona(persona: PersonaRelacionadaDTO) {
        // Verificación básica
        if (!persona || !persona.tokenIdentificador) {
            this.dialogMensajeService.mensajeError('No se puede subir el documento sin una persona válida.');
            return;
        }

        // Obtener la ruta actual para construir la ruta relativa correcta
        const currentPath = this.router.url;
        const basePath = currentPath.substring(0, currentPath.lastIndexOf('/'));
        const relativePath = 'subir-documento';

        // Crear un objeto simplificado para evitar problemas con propiedades innecesarias
        const personaSimplificada = {
            tokenIdentificador: persona.tokenIdentificador,
            nombres: persona.nombres || 'Persona relacionada'
        };

        // Navegar a la página de subida de documentos
        this.router.navigate([relativePath], {
            relativeTo: this.route, // Usar relativeTo para mayor precisión
            state: {
                item: personaSimplificada,
                nemonicoMenu: this.nemonicoMenu,
                nemonicoCarpeta: etiquetasModel.CARPETA_FAMILIAR || 'CARPETA_FAMILIAR',
                tipoServicio: 'personaRelacionada',
                seccionTipoDocumento: etiquetasModel.SECCION_FICHA_IDENT_PERSONA_RELACIONADA
            }
        });
    }

    obtenerFichaIdentificacionNumeroDocumento(numeroIdentificacion: string) {
        const tipoDocumentoActual = this.personaRelacionadaForm.get('tipoIdentificacion')?.value;
        if (tipoDocumentoActual === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI'
        ) {
            this.obtenerDataNumeroDocumento(numeroIdentificacion);
        }
    }

    // Este método debe ser llamado cuando se cambia de persona relacionada o se cancela
    private borrarSubscription() {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
        this.unsubscribe$ = new Subject<void>();
    }

    /**
     * Limpia las suscripciones al destruir el componente
     */
    ngOnDestroy() {
        if (this.unsubscribe$) {
            this.unsubscribe$.next();
            this.unsubscribe$.complete();
        }
    }

    // Método para validar el número de documento según el tipo seleccionado
    private numeroIdentificacionEsValido(value: string): boolean {
        if (!value) return false;

        const tipoDocumentoActual = this.personaRelacionadaForm.get('tipoIdentificacion')?.value;

        // No validamos si no hay tipo de documento o es "sin documento"
        if (!tipoDocumentoActual || tipoDocumentoActual === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
            return false;
        }

        // Obtenemos los valores de longitud mínima y máxima según el tipo de documento
        const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
            doc => doc.nemonico === tipoDocumentoActual
        );

        if (!tipoDocumentoSeleccionado) return false;

        const { minLength, maxLength } = tipoDocumentoSeleccionado;

        // Validamos la longitud
        return value.length >= minLength && value.length <= maxLength;
    }

    // Método mejorado para inicializar la validación del número de documento
    private setupNumeroIdentificacionListener() {
        const numeroDocumentoControl = this.personaRelacionadaForm.get('numeroDocumento');

        if (!numeroDocumentoControl) return;

        // Limpiamos la suscripción anterior si existe
        if (this.unsubscribe$) {
            this.unsubscribe$.next();
            this.unsubscribe$.complete();
            this.unsubscribe$ = new Subject<void>();
        }

        // Nos suscribimos a los cambios en el campo de número de documento
        numeroDocumentoControl.valueChanges
            .pipe(
                debounceTime(500), // Esperamos medio segundo después de que el usuario deje de escribir
                distinctUntilChanged(),
                takeUntil(this.unsubscribe$)
            )
            .subscribe(async (numeroDocumento: string) => {
                // Si no hay valor o está editando, no hacemos nada
                if (!numeroDocumento || this.esEdicionPersona) return;

                // Validamos que el número de documento sea válido según el tipo seleccionado
                if (!this.numeroIdentificacionEsValido(numeroDocumento)) {
                    return;
                }

                // Obtenemos el tipo de documento seleccionado
                const tipoIdentificacion = this.personaRelacionadaForm.get('tipoIdentificacion')?.value;

                // Si no hay tipo de documento seleccionado o es "sin documento", no continuamos
                if (!tipoIdentificacion || tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
                    return;
                }

                // Verificar si la persona ya existe en nuestro sistema
                const loading = this.dialogMensajeService.mensajeLoading('Verificando número de documento...');

                try {
                    // Convertimos la llamada al servicio a una Promise para manejarla más fácilmente
                    const response = await this.datosFamiliaresService.buscarPersonaRelacionadaPorNumeroDocumento(numeroDocumento)
                        .toPromise();

                    loading.close();

                    if (!response.exito) {
                        this.dialogMensajeService.mensajeError(response.mensaje);
                        return;
                    }

                    // IMPORTANTE: Verificamos si ya existe una relación con esta ficha
                    if (response.data && response.data.length > 0) {
                        const personasEncontradas = response.data;

                        // Verificamos si alguna de las personas encontradas ya está relacionada con esta ficha
                        const personaRelacionadaConEstaFicha = personasEncontradas.find(
                            persona => persona.tokenIdentificadorFicha === this.uuid_fp
                        );

                        if (personaRelacionadaConEstaFicha) {
                            this.dialogMensajeService.mensajeErrorConTitulo(
                                "Persona ya relacionada",
                                `Ya existe una persona con número de documento ${numeroDocumento} relacionada a esta ficha.`
                            );
                            numeroDocumentoControl.setValue('');
                            return;
                        }

                        // Si la persona existe pero no está relacionada con esta ficha
                        const dialogRef = this.dialogMensajeService.mensajeConConfirmacion(
                            `La persona con número de documento ${numeroDocumento} ya existe en el sistema.`,
                            '¿Desea completar los datos automáticamente?'
                        );

                        dialogRef.afterClosed().subscribe((result: 'confirmed' | 'cancelled') => {
                            if (result === 'confirmed') {
                                // Rellenamos el formulario con los datos existentes
                                this.completarFormularioPersonaExistente(personasEncontradas[0]);
                            } else {
                                // Si el usuario no confirma, limpiamos el campo
                                numeroDocumentoControl.setValue('');
                            }
                        });
                        return;
                    }

                    // Solo si el documento no existe en nuestro sistema Y es un DNI, 
                    // entonces consultamos el servicio externo
                    if (tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
                        await this.obtenerDataNumeroDocumento(numeroDocumento);
                    }
                } catch (error) {
                    loading.close();
                    console.error('Error al verificar persona relacionada:', error);
                    this.dialogMensajeService.mensajeError('Ocurrió un error al verificar el número de documento.');
                }
            });
    }

    // Método separado para la consulta en servicio externo (RENIEC)
    private async obtenerDataNumeroDocumento(numeroDocumento: string): Promise<boolean> {
        const loading = this.dialogMensajeService.mensajeLoading('Consultando información del documento...');

        try {
            // Usar 'any' para evitar el error de tipo
            const resp: any = await this.utilsService.data(numeroDocumento).toPromise();
            loading.close();

            if (!resp || !resp.row) {
                this.dialogMensajeService.mensajeError(
                    'No se encontró información para el número de documento ingresado. Por favor, ingrese los datos manualmente.'
                );
                return false;
            }

            this.dataPeru = resp;
            this.datosEncontradosReniec(resp.row);
            return true;
        } catch (error) {
            loading.close();
            console.error('Error al obtener datos del servicio externo:', error);
            this.dialogMensajeService.mensajeError(
                'No se pudo obtener la información del documento ingresado. Por favor, ingrese los datos manualmente.'
            );
            return false;
        }
    }

    // Método para verificar si la persona existe en nuestro sistema
    private async verificarPersonaRelacionadaExistente(numeroDocumento: string): Promise<void> {

        // Primero verificamos localmente si existe en la lista actual
        const personaExistenteLocal = this.listaPersonasRelacionadas.find(
            persona => persona.numeroDocumento === numeroDocumento
        );

        if (personaExistenteLocal) {
            this.dialogMensajeService.mensajeErrorConTitulo(
                "Persona ya relacionada",
                `Ya existe una persona con número de documento ${numeroDocumento} relacionada a esta ficha.`
            );
            this.personaRelacionadaForm.get('numeroDocumento').setValue('');
            return;
        }

        // Si no existe localmente, consultamos en el backend
        const loading = this.dialogMensajeService.mensajeLoading('Verificando número de documento...');

        try {
            // Convertimos la llamada al servicio a una Promise para manejarla más fácilmente
            const response = await this.consultarPersonaRelacionadaBackend(numeroDocumento);
            loading.close();

            if (!response.exito) {
                this.dialogMensajeService.mensajeError(response.mensaje);
                return;
            }

            if (!response.data || response.data.length === 0) {
                // Si no existe en el backend, consultamos en el servicio externo
                if (this.personaRelacionadaForm.get('tipoIdentificacion').value === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
                    await this.obtenerDataNumeroDocumento(numeroDocumento);
                }
                return;
            }

            const personasEncontradas = response.data;
            // Verificamos si alguna de las personas encontradas ya está relacionada con esta ficha
            const personaRelacionadaConEstaFicha = personasEncontradas.find(
                persona => persona.tokenIdentificadorFicha === this.uuid_fp
            );

            if (personaRelacionadaConEstaFicha) {
                this.dialogMensajeService.mensajeErrorConTitulo(
                    "Persona ya relacionada",
                    `Ya existe una persona con número de documento ${numeroDocumento} relacionada a esta ficha.`
                );
                this.personaRelacionadaForm.get('numeroDocumento').setValue('');
                return;
            }

            // Caso: La persona existe en el sistema pero para otra ficha diferente
            // Preguntamos al usuario si desea completar los datos automáticamente
            const dialogRef = this.dialogMensajeService.mensajeConConfirmacion(
                `La persona con número de documento ${numeroDocumento} ya existe en el sistema para otra ficha.`,
                '¿Desea completar los datos automáticamente?'
            );

            dialogRef.afterClosed().subscribe((result: 'confirmed' | 'cancelled') => {
                if (result === 'confirmed') {
                    // Rellenamos automáticamente los datos con la primera persona encontrada
                    this.completarFormularioPersonaExistente(personasEncontradas[0]);
                } else {
                    // Si el usuario no confirma, limpiamos el campo
                    this.personaRelacionadaForm.get('numeroDocumento').setValue('');
                }
            });
        } catch (error) {
            loading.close();
            console.error('Error al verificar persona relacionada:', error);
            this.dialogMensajeService.mensajeError('Ocurrió un error al verificar el número de documento.');
        }
    }

    // Método para convertir la llamada al servicio a una Promise
    private consultarPersonaRelacionadaBackend(numeroDocumento: string): Promise<RespuestaPorDefecto<PersonaRelacionadaDTO[]>> {
        return new Promise((resolve, reject) => {
            this.datosFamiliaresService.buscarPersonaRelacionadaPorNumeroDocumento(numeroDocumento)
                .subscribe({
                    next: (resp: RespuestaPorDefecto<PersonaRelacionadaDTO[]>) => {
                        resolve(resp);
                    },
                    error: (error: any) => {
                        console.error('Error en la consulta al backend:', error);
                        reject(error);
                    }
                });
        });
    }

    // Método mejorado para completar el formulario con datos existentes
    private completarFormularioPersonaExistente(persona: PersonaRelacionadaDTO) {
        try {
            // Convertir la fecha de nacimiento si existe
            let fechaNacimiento = null;
            if (persona.fechaNacimiento) {
                try {
                    // Si es una cadena, convertirla a objeto Date
                    if (typeof persona.fechaNacimiento === 'string') {
                        fechaNacimiento = new Date(persona.fechaNacimiento);
                    } else {
                        // Si ya es un objeto Date, usarlo directamente
                        fechaNacimiento = persona.fechaNacimiento;
                    }

                    // Verificar si la fecha es válida
                    if (isNaN(fechaNacimiento.getTime())) {
                        fechaNacimiento = null;
                    }
                } catch (error) {
                    console.error('Error al procesar la fecha de nacimiento:', error);
                    fechaNacimiento = null;
                }
            }

            // Verificar número de teléfono y truncar si es necesario
            if (persona.telefono) {
                const telefonoMaxLength = 9; // Longitud máxima permitida
                if (persona.telefono.length > telefonoMaxLength) {
                    // Truncar el número para que pueda cargarse en el formulario
                    persona.telefono = persona.telefono.substring(0, telefonoMaxLength);
                }

                // Verificar si contiene solo números y filtrar caracteres no numéricos
                if (!/^\d+$/.test(persona.telefono)) {
                    persona.telefono = persona.telefono.replace(/\D/g, '');
                }
            }

            // Objeto para almacenar valores a actualizar
            const formValues: any = {
                nombres: persona.nombres || '',
                primerApellido: persona.apellidoPaterno || '',
                segundoApellido: persona.apellidoMaterno || '',
                fechaNacimiento: fechaNacimiento,
                telefono: persona.telefono || '',
                observaciones: persona.observaciones || '',
                rolesInfluencias: persona.rolesInfluencias || ''
            };

            // Verificar longitud de campos de texto y truncar automáticamente si exceden el límite
            const camposTexto: { [key: string]: { valor: string, maxLength: number } } = {
                nombres: { valor: formValues.nombres, maxLength: 128 },
                primerApellido: { valor: formValues.primerApellido, maxLength: 128 },
                segundoApellido: { valor: formValues.segundoApellido, maxLength: 128 },
                observaciones: { valor: formValues.observaciones, maxLength: 500 },
                rolesInfluencias: { valor: formValues.rolesInfluencias, maxLength: 500 }
            };

            // Truncar automáticamente los campos sin generar advertencias
            for (const [key, config] of Object.entries(camposTexto)) {
                if (config.valor && config.valor.length > config.maxLength) {
                    formValues[key] = config.valor.substring(0, config.maxLength);
                }
            }

            // Verificar y asignar campos de selección solo si tienen valores válidos

            // Sexo
            if (persona.tipoSexo && this.listaTipoSexo && this.listaTipoSexo.some(tipo => tipo.nemonico === persona.tipoSexo)) {
                formValues.sexo = persona.tipoSexo;
            } else {
                formValues.sexo = '0';  // Valor por defecto "Seleccione"
            }

            // Estado civil
            if (persona.estadoCivil && this.listaTipoEstadoCivil && this.listaTipoEstadoCivil.some(tipo => tipo.nemonico === persona.estadoCivil)) {
                formValues.estadoCivil = persona.estadoCivil;
            } else {
                formValues.estadoCivil = '0';
            }

            // Parentesco
            if (persona.tipoParentesco && this.listaTipoParentesco && this.listaTipoParentesco.some(tipo => tipo.nemonico === persona.tipoParentesco)) {
                formValues.parentesco = persona.tipoParentesco;
            } else {
                formValues.parentesco = '0';
            }

            // Modalidad de estudio
            if (persona.modalidadEstudio && this.listaModalidadEstudio && this.listaModalidadEstudio.some(tipo => tipo.nemonico === persona.modalidadEstudio)) {
                formValues.modalidadEstudio = persona.modalidadEstudio;
                this.modalidadEstudio = persona.modalidadEstudio;

                // También verificar nivel según la modalidad
                switch (persona.modalidadEstudio) {
                    case 'MODALIDAD_ESTUDIO_EBR':
                        if (persona.nivelEBR && this.listaNivelEBR && this.listaNivelEBR.some(nivel => nivel.tokenIdentificador === persona.nivelEBR)) {
                            formValues.nivelEBR = persona.nivelEBR;
                        } else {
                            formValues.nivelEBR = '0';
                        }
                        break;
                    case 'MODALIDAD_ESTUDIO_SUPERIOR':
                        if (persona.nivelSuperior && this.listaNivelSuperior && this.listaNivelSuperior.some(nivel => nivel.tokenIdentificador === persona.nivelSuperior)) {
                            formValues.nivelSuperior = persona.nivelSuperior;
                        } else {
                            formValues.nivelSuperior = '0';
                        }
                        break;
                    case 'MODALIDAD_ESTUDIO_EBA':
                        if (persona.nivelEBA && this.listaNivelEBA && this.listaNivelEBA.some(nivel => nivel.tokenIdentificador === persona.nivelEBA)) {
                            formValues.nivelEBA = persona.nivelEBA;
                        } else {
                            formValues.nivelEBA = '0';
                        }
                        break;
                }
            } else {
                formValues.modalidadEstudio = '0';
            }

            // Tipo de ocupación
            if (persona.tipoOcupacion && this.listaTipoOcupacion && this.listaTipoOcupacion.some(tipo => tipo.tokenIdentificador === persona.tipoOcupacion)) {
                formValues.tipoOcupacion = persona.tipoOcupacion;
            } else {
                formValues.tipoOcupacion = '0';
            }

            // Relación afectiva
            if (persona.relacionAfectiva && this.listaNivelAfectivo && this.listaNivelAfectivo.some(nivel => nivel.tokenIdentificador === persona.relacionAfectiva)) {
                formValues.relacionAfectiva = persona.relacionAfectiva;
            } else {
                formValues.relacionAfectiva = '0';
            }

            // Valores booleanos representados como strings
            formValues.esTutor = persona.esTutor === 'S' ? 'S' : 'N';
            formValues.visitaAutorizada = persona.visitaAutorizada === 'S' ? 'S' : 'N';
            formValues.fallecido = persona.fallecido === 'S' ? 'S' : 'N';

            // Ahora actualizamos el formulario con todos los valores validados
            this.personaRelacionadaForm.patchValue(formValues);

            // Ajustar validaciones según el tipo de parentesco
            this.ajustarValidacionesPorParentesco(formValues.parentesco);

            // Inicializamos las listas de información de ubicación
            this.listaInformacionUbicaciones = [];

            // Si hay información de ubicación, la copiamos truncando los valores largos si es necesario
            if (persona.informacionUbicaciones && persona.informacionUbicaciones.length > 0) {
                // Creamos una copia de cada elemento para evitar referencias
                persona.informacionUbicaciones.forEach(info => {
                    // Verificar longitud del valor y truncar si es necesario
                    let valorTruncado = info.valor;
                    if (valorTruncado && valorTruncado.length > 500) {
                        valorTruncado = valorTruncado.substring(0, 500);
                    }

                    // Añadir a la lista después de validar
                    const infoDto = new InformacionUbicacionDTO();
                    infoDto.idInformacionUbicacion = info.idInformacionUbicacion;
                    infoDto.tokenIdentificador = info.tokenIdentificador;
                    infoDto.tipoInformacionUbicacion = info.tipoInformacionUbicacion;
                    infoDto.valor = valorTruncado;
                    infoDto.idPersonaRelacionada = info.idPersonaRelacionada;
                    infoDto.nombreTipoInformacion = info.nombreTipoInformacion;
                    infoDto.id_temporal = Date.now() + Math.floor(Math.random() * 1000);

                    this.listaInformacionUbicaciones.push(infoDto);
                });
            }

            // Actualizamos el dataSource para la tabla de información de ubicación
            this.dataSourceInformacionUbicaciones = [...this.listaInformacionUbicaciones];

            // Actualizar tabla si existe
            if (this.table) {
                this.table.renderRows();
            }

            // Verificación final para campos críticos después de la asignación
            setTimeout(() => {
                // Verificar la fecha
                const fechaAsignada = this.personaRelacionadaForm.get('fechaNacimiento')?.value;
                if (!fechaAsignada && fechaNacimiento) {
                    this.personaRelacionadaForm.get('fechaNacimiento')?.setValue(fechaNacimiento, { emitEvent: false });
                }

                // Verificar parentesco
                const parentescoAsignado = this.personaRelacionadaForm.get('parentesco')?.value;
                if (parentescoAsignado === '0' && persona.tipoParentesco) {
                    this.personaRelacionadaForm.get('parentesco')?.setValue(persona.tipoParentesco, { emitEvent: false });
                    // Volver a ajustar validaciones
                    this.ajustarValidacionesPorParentesco(persona.tipoParentesco);
                }

                // Verificar estado de validación del formulario
                this.verificarValidacionFormulario();
            }, 100);

            // Mostrar mensaje de éxito sin advertencias
            this.dialogMensajeService.mensajeExitoso(
                'Datos completados',
                'Se han completado los datos con la información de la persona existente.'
            );
        } catch (error) {
            console.error('Error al completar formulario con datos existentes:', error);
            this.dialogMensajeService.mensajeError('Ocurrió un error al cargar los datos de la persona');
        }
    }

    // Método para verificar la validación del formulario y mostrar errores
    private verificarValidacionFormulario() {
        // Verificar cada control del formulario
        Object.keys(this.personaRelacionadaForm.controls).forEach(key => {
            const control = this.personaRelacionadaForm.get(key);

            // Marcar como tocado si tiene errores distintos a maxlength/minlength
            if (control && control.invalid) {
                // Filtrar errores de longitud que ya hemos manejado truncando los valores
                const erroresRelevantes = Object.keys(control.errors || {})
                    .filter(error => error !== 'maxlength' && error !== 'formatoInvalido');

                if (erroresRelevantes.length > 0) {
                    control.markAsTouched();
                }
            }
        });

        // Solo mostramos mensaje si hay errores críticos, no por longitudes excedidas
        const erroresCriticos = Object.keys(this.personaRelacionadaForm.controls)
            .some(key => {
                const control = this.personaRelacionadaForm.get(key);
                if (!control || !control.errors) return false;
                return Object.keys(control.errors).some(
                    error => error !== 'maxlength' && error !== 'formatoInvalido'
                );
            });

        if (erroresCriticos) {
            this.dialogMensajeService.mensajeAdvertencia(
                'Formulario con errores',
                'El formulario contiene campos con errores críticos. Por favor, revise y corrija antes de guardar.'
            );
        }
    }

    /**
     * Método para verificar y truncar textos largos antes de guardar
     * Asegura que todos los campos de texto cumplan con las longitudes máximas permitidas
     */
    private asegurarLongitudesValidasParaGuardar() {
        // Lista de campos con sus longitudes máximas
        const camposMaxLen = {
            'nombres': 128,
            'primerApellido': 128,
            'segundoApellido': 128,
            'observaciones': 500,
            'rolesInfluencias': 500,
            'telefono': 9
        };

        // Truncar automáticamente valores que excedan el máximo
        for (const [campo, maxLen] of Object.entries(camposMaxLen)) {
            const control = this.personaRelacionadaForm.get(campo);
            if (control && control.value && typeof control.value === 'string' && control.value.length > maxLen) {
                // Aplicar truncamiento
                control.setValue(control.value.substring(0, maxLen));
            }
        }

        // También verificar información de ubicación
        if (this.listaInformacionUbicaciones && this.listaInformacionUbicaciones.length > 0) {
            this.listaInformacionUbicaciones.forEach(info => {
                if (info.valor && info.valor.length > 500) {
                    info.valor = info.valor.substring(0, 500);
                }
            });
        }
    }

    // Método para ajustar las validaciones según el tipo de parentesco
    private ajustarValidacionesPorParentesco(tipoParentesco: string) {
        // Si no hay parentesco o es inválido, no hacemos ajustes
        if (!tipoParentesco || tipoParentesco === '0') {
            return;
        }

        // Verificar si es hijo u otro tipo que requiera menos validaciones
        const esHijo = tipoParentesco === 'PARENTESCO_HIJO' || tipoParentesco === 'PARENTESCO_HIJA';

        // Obtener los controles que necesitan ajuste
        const estadoCivilControl = this.personaRelacionadaForm.get('estadoCivil');
        const modalidadEstudioControl = this.personaRelacionadaForm.get('modalidadEstudio');
        const tipoOcupacionControl = this.personaRelacionadaForm.get('tipoOcupacion');

        if (esHijo) {
            // Para hijos, estos campos no son obligatorios
            estadoCivilControl?.clearValidators();
            modalidadEstudioControl?.clearValidators();
            tipoOcupacionControl?.clearValidators();
        } else {
            // Para otros parentescos, restaurar validadores si son necesarios
            // (Aquí puedes agregar otros validadores según tu lógica de negocio)
            estadoCivilControl?.setValidators([Validators.required]);
            modalidadEstudioControl?.setValidators([Validators.required]);
            tipoOcupacionControl?.setValidators([Validators.required]);
        }

        // Actualizar validaciones
        estadoCivilControl?.updateValueAndValidity();
        modalidadEstudioControl?.updateValueAndValidity();
        tipoOcupacionControl?.updateValueAndValidity();
    }

    // Método para manejar los datos encontrados en RENIEC
    datosEncontradosReniec(fichaIdentificacionEditar: any) {

        // Actualizamos los datos del formulario con la información obtenida
        this.personaRelacionadaForm
            .get('primerApellido')
            .setValue(fichaIdentificacionEditar.apellido_paterno);
        this.personaRelacionadaForm
            .get('segundoApellido')
            ?.setValue(fichaIdentificacionEditar.apellido_materno);
        this.personaRelacionadaForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);

        // Formateamos y asignamos la fecha de nacimiento
        const fechaString = fichaIdentificacionEditar.fecha_nacimiento;
        if (fechaString) {
            const partes = fechaString.split('-');
            if (partes.length === 3) {
                const fechaNacimiento = new Date(
                    Number(partes[0]), // Año
                    Number(partes[1]) - 1, // Mes (0-11 en JavaScript)
                    Number(partes[2]) // Día
                );
                this.personaRelacionadaForm.get('fechaNacimiento').setValue(fechaNacimiento);
            }
        }

        // Si hay información adicional en la respuesta, también la usamos
        if (fichaIdentificacionEditar.sexo) {
            const sexoNemonico = fichaIdentificacionEditar.sexo === 'M' ?
                'TIPO_SEXO_MASCULINO' : 'TIPO_SEXO_FEMENINO';

            const sexoEncontrado = this.listaTipoSexo.find(tipo => tipo.nemonico === sexoNemonico);
            if (sexoEncontrado) {
                this.personaRelacionadaForm.get('sexo').setValue(sexoEncontrado.nemonico);
            }
        }

        // Notificamos al usuario una sola vez
        this.dialogMensajeService.mensajeExitoso(
            'Datos completados',
            'Se han completado los datos básicos con la información obtenida del servicio externo.'
        );
    }

    // Método para el cambio de tipo de identificación
    onTipoIdentificacionChange(event: any) {
        const tipoIdentificacion = event.value;
        const numeroDocumentoControl = this.personaRelacionadaForm.get('numeroDocumento');

        // Verifica si estamos en modo edición y el tipo de documento es "Sin documento"
        if (tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
            // Si estamos editando un registro que ya era "Sin documento"
            if (this.esEdicionPersona &&
                this.personaRelacionadaEditando?.tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {

                // Mantener el valor y estado actual
                numeroDocumentoControl.setValue(this.personaRelacionadaEditando.numeroDocumento);
                numeroDocumentoControl.disable();
                numeroDocumentoControl.clearValidators();
            } else {
                // Para nuevos registros o cambio de otro tipo a sin documento, vaciar
                numeroDocumentoControl.setValue('');
                numeroDocumentoControl.disable();
                numeroDocumentoControl.clearValidators();
            }
            } else {
    // Si cambia de sin documento a otro tipo
    if (this.personaRelacionadaForm.get('tipoIdentificacion').value ===
        'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
        numeroDocumentoControl.setValue('');
    }

    numeroDocumentoControl.enable();

    // Detectar si el tipo es de extranjería
    if (
        tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA' ||
        tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA'
    ) {
        numeroDocumentoControl.setValidators([
            Validators.required,
            Validators.minLength(4) // Solo mínimo
        ]);
    } else {
        // Para otros tipos, usar la config estándar
        const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
            (doc) => doc.nemonico === tipoIdentificacion
        );

        if (tipoDocumentoSeleccionado) {
            const { minLength, maxLength } = tipoDocumentoSeleccionado;
            const validators = [
                Validators.required,
                Validators.minLength(minLength),
                Validators.maxLength(maxLength),
            ];

            if (tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
                validators.push(Validators.pattern('^[0-9]*$'));
            }

            numeroDocumentoControl.setValidators(validators);
        } else {
            numeroDocumentoControl.setValidators([Validators.required]);
        }
    }

    numeroDocumentoControl.updateValueAndValidity();
    this.setupNumeroIdentificacionListener();
}

        numeroDocumentoControl.updateValueAndValidity();

        // Reiniciamos el listener para validación de número de documento
        this.setupNumeroIdentificacionListener();
    }

    /**
     * Obtiene el nombre completo de una persona
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

}
