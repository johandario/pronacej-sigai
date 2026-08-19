import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MatCheckboxChange,
    MatCheckboxModule,
} from '@angular/material/checkbox';
import {
    DateAdapter,
    MAT_DATE_FORMATS,
    MAT_DATE_LOCALE,
} from '@angular/material/core';
import {
    MatDatepickerInputEvent,
    MatDatepickerModule,
} from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
    MatPaginator,
    MatPaginatorIntl,
    MatPaginatorModule,
    PageEvent,
} from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionDocumentoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaDeIdentificacionDocumentoDTO } from 'app/core/model/request/ia/FichaDeIdentificacionDocumentoDTO.model';
import { FichaPrincipalDocumentosRequest } from 'app/core/model/request/ia/FichaPrincipalDocumentosRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { PdfService } from 'app/core/services/pdf.service';
import { UtilsService } from 'app/core/services/utils.service';
import {
    CUSTOM_DATE_FORMATS,
    CustomDateAdapter,
    FuncionesUtils,
} from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaPrincipalDocumentoService } from 'app/modules/administracion/services/fichaPrincipalDocumento.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { LocalidadService } from 'app/modules/seguridad/services/localidad.service';
import { environment } from 'environments/environment';
import { Subscription, debounceTime, distinctUntilChanged, filter, forkJoin } from 'rxjs';
import { HistorialDeFotosComponent } from './historial-de-fotos/historial-de-fotos.component';
import { SeleccionarUbigeoComponent } from './seleccionar-ubigeo/seleccionar-ubigeo.component';
import { SubidaDocumentosFichaPrincipalComponent } from './subida-documentos-ficha-principal/subida-documentos-ficha-principal.component';
import { VisualizacionDocFichaPComponent } from './subida-documentos-ficha-principal/visualizacion-doc-ficha-p/visualizacion-doc-ficha-p.component';
import { BusquedaAdolescenteDialogComponent } from '../busqueda-adolescente-dialog/busqueda-adolescente-dialog.component';
import { EstudiosComponent } from '../estudios/estudios.component';
import { TrabajoLaboralComponent } from '../trabajo-laboral/trabajo-laboral.component';

@Component({
    selector: 'app-datos-generales',
    standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MatInputModule,
        MatButtonModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        CommonModule,
        MatTabsModule,
        MatDatepickerModule,
        MatButtonModule,
        MatRadioModule,
        MatCheckboxModule,
        MatIconModule,
        MatExpansionModule,
        MatTableModule,
        MatPaginatorModule,
        VisualizacionDocFichaPComponent,
        HistorialDeFotosComponent,
        EstudiosComponent,
        TrabajoLaboralComponent
    ],
    templateUrl: './datos-generales.component.html',
    styleUrl: './datos-generales.component.scss',
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    ],
})
export class DatosGeneralesComponent implements OnInit, AfterViewInit {
    esMujer: boolean;
    tieneOtroOrigenEtnico: boolean;
    private subscriptions = new Subscription();
    private subscriptionIdentificacion = new Subscription();

    tituloPantalla = 'Ficha Principal';

    previewUrl1: string | ArrayBuffer | null = null;
    previewUrl2: string | ArrayBuffer | null = null;

    modalidadEstudio: string = '';
    listaNivelEBR: CatalogoDTO[] = [];
    listaNivelSuperior: CatalogoDTO[] = [];
    listaNivelEBA: CatalogoDTO[] = [];

    /*-------Seccion Documentos--------*/
    @ViewChild(MatPaginator) paginator: MatPaginator;

    fichaIdentificacionForm: FormGroup;
    fichaIdentificacionDTO: FichaIdentificacionDTO;

    esEdicion = false;

    nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;
    tokenIdentificadorFichaIdentificacion: string;

    @ViewChild('documentosComp')
    tablaDocumentos: VisualizacionDocFichaPComponent;

    ES_PAIS_PERU: boolean = false;
    paises: LocalidadDTO[] = [];
    departamentos: LocalidadDTO[] = [];
    provincias: LocalidadDTO[] = [];
    distritos: LocalidadDTO[] = [];

    ubigeoNacimiento: LocalidadDTO;
    ubigeoDireccion: LocalidadDTO;

    listaTipoDocumento: CatalogoDTO[] = [];
    listaTipoSexo: CatalogoDTO[] = [];
    listaOrigenEtnico: CatalogoDTO[] = [];
    listaTipoGenero: CatalogoDTO[] = [];
    listaViveParentesco: CatalogoDTO[] = [];
    listaModalidadEstudio: CatalogoDTO[] = [];
    listaTipoTrabajos: CatalogoDTO[] = [];
    listaUbigeoDireccion: CatalogoDTO[] = [];
    listaEstadoCivil: CatalogoDTO[] = [];
    listaGruposVulnerables: CatalogoDTO[] = [];

    listaDocumentoIde = [
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_DNI',
            nombre: 'Documento Nacional de Identidad',
            minLength: 8,
            maxLength: 8,
            regex: /^\d{8}$/,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE',
            nombre: 'Pasaporte',
            minLength: 6,
            maxLength: 10,
            regex: /^[A-Za-z0-9]{6,}$/,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA',
            nombre: 'Documento de Extranjería',
            minLength: 12,
            maxLength: 12,
            regex: /^\d{12}$/,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA',
            nombre: 'Carné de Extranjería',
            minLength: 8,
            maxLength: 8,
            regex: /^\d{8}$/,
        },
        {
            nemonico: 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO',
            nombre: 'Sin Documento',
            minLength: 1,
            maxLength: 10,
            regex: /^[A-Za-z0-9]{1,}$/,
        },
    ];

    centro: JerarquiaDTO;

    base64Image: string | null = null;
    documentoSeleccionado: CatalogoDTO;
    maxDate: Date = new Date();
    sexoEscogido: CatalogoDTO = new CatalogoDTO();

    fichaEncontrada: FichaIdentificacionDTO;
    dataPeru: any;

    mostrarBotonBusqueda: boolean = false;

    constructor(
        private formBuilder: FormBuilder,
        private dialogMensajeService: DialogMensajeService,
        private fichaIdentificacionService: FichaIdentificacionService,
        private router: Router,
        private route: ActivatedRoute,
        private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
        private fichaPrincipalDocumentoService: FichaPrincipalDocumentoService,
        private localidadService: LocalidadService,
        public funcionesUtils: FuncionesUtils,
        public dialog: MatDialog,
        private jerarquiaService: JerarquiaService,
        private pdfService: PdfService,
        private http: HttpClient,
        private utilsService: UtilsService,
        private authSerguridadServicio: AuthSerguridadServicio
    ) {
        this.construirForm();
    }

    ngAfterViewInit(): void {
        this.obtenerDocumentos();
        this.loadImageAsBase64();
        if (!this.esEdicion) {
            this.agregarSubs();
        }
    }

    async ngOnInit(): Promise<void> {
        await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
            'MENU_FICHA_PRINCIPAL'
        );
        
        // Cargar centro antes de otros procesos que puedan depender de él
        this.cargarCentro();

        this.listarLocalidad('PAIS');
        this.tokenIdentificadorFichaIdentificacion = this.route.snapshot.paramMap.get('uuid_fp');

        // mostrar control de búsqueda para nuevos registros
        if (!this.tokenIdentificadorFichaIdentificacion) {  
            this.mostrarBotonBusqueda = true;
        }

        await this.cargarCatalogos();

        console.log('Catálogos cargados completamente');

        if (this.tokenIdentificadorFichaIdentificacion) {
            this.esEdicion = true;
            await this.obtenerFichaIdentificacionPorToken(this.tokenIdentificadorFichaIdentificacion);
            this.esEdicion = !!this.fichaIdentificacionDTO || this.tokenIdentificadorFichaIdentificacion?.length > 0;
        }

        const subUbigeo = this.fichaIdentificacionForm
            .get('codigoUbigeoNacimiento')
            .valueChanges.subscribe((value) => {
                if (value && value.length % 2 === 0) {
                    this.localidadService
                        .obtenerLocalidadUbigeo(value, this.nemonicoMenu)
                        .subscribe({
                            next: (resp: RespuestaPorDefecto<LocalidadDTO>) => {
                                if (!resp.exito) {
                                    return;
                                }
                                if (
                                    resp.data.rutaUbigeo &&
                                    resp.data.rutaUbigeo.length > 0
                                ) {
                                    this.fichaIdentificacionForm
                                        .get('rutaUbigeoNacimiento')
                                        .setValue(resp.data.rutaUbigeo);
                                } else {
                                    this.fichaIdentificacionForm
                                        .get('rutaUbigeoNacimiento')
                                        .setValue(null);
                                }
                            },
                            error: (error: any) => {
                                this.localidadService.checkError(error);
                            },
                        });
                }
            });
        
        const subUbigeoUbi = this.fichaIdentificacionForm
            .get('codigoUbigeoUbicacion')
            .valueChanges.subscribe((value) => {
                if (value && value.length % 2 === 0) {
                    this.localidadService
                        .obtenerLocalidadUbigeo(value, this.nemonicoMenu)
                        .subscribe({
                            next: (resp: RespuestaPorDefecto<LocalidadDTO>) => {
                                if (!resp.exito) {
                                    return;
                                }
                                if (
                                    resp.data.rutaUbigeo &&
                                    resp.data.rutaUbigeo.length > 0
                                ) {
                                    this.fichaIdentificacionForm
                                        .get('rutaUbigeoDireccion')
                                        .setValue(resp.data.rutaUbigeo);
                                } else {
                                    this.fichaIdentificacionForm
                                        .get('rutaUbigeoDireccion')
                                        .setValue(null);
                                }
                            },
                            error: (error: any) => {
                                this.localidadService.checkError(error);
                            },
                        });
                }
            });
        this.subscriptions.add(subUbigeo);
        this.subscriptions.add(subUbigeoUbi);
    }

    async cargarCatalogos(): Promise<void> {
        return new Promise((resolve, reject) => {
            const catalogos$ = forkJoin({
                tipoDocumento: this.funcionesUtils.obtenerListaCatalogo(
                    'TIPO_DOCUMENTO_IDENTIFICACION',
                    etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
                ),
                tipoGenero: this.funcionesUtils.obtenerListaCatalogo(
                    'GENERO',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                tipoParentesco: this.funcionesUtils.obtenerListaCatalogo(
                    'PARENTESCO',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                tipoTipoSexo: this.funcionesUtils.obtenerListaCatalogo(
                    'TIPO_SEXO',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                origenEtnico: this.funcionesUtils.obtenerListaCatalogo(
                    'ORIGEN_ETNICO',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                tipoTipoIntruccion: this.funcionesUtils.obtenerListaCatalogo(
                    'MODALIDAD_ESTUDIO',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                tipoTipoTrabajos: this.funcionesUtils.obtenerListaCatalogo(
                    'OCUPACION',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                ubigeoDireccion: this.funcionesUtils.obtenerListaCatalogo(
                    'UBIGEO_DIRECCION',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                estadoCivil: this.funcionesUtils.obtenerListaCatalogo(
                    'ESTADO_CIVIL',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                gruposVulnerables: this.funcionesUtils.obtenerListaCatalogo(
                    'GRUPO_VULNERABLE',
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                ),
                nivelEBR: this.funcionesUtils.obtenerListaCatalogo(
                    'NIVEL_EBR',
                    this.nemonicoMenu
                ),
                nivelSuperior: this.funcionesUtils.obtenerListaCatalogo(
                    'NIVEL_SUPERIOR',
                    this.nemonicoMenu
                ),
                nivelEBA: this.funcionesUtils.obtenerListaCatalogo(
                    'NIVEL_EBA',
                    this.nemonicoMenu
                ),
            });

            catalogos$.subscribe({
                next: (result) => {
                    this.listaTipoDocumento = result.tipoDocumento ?? [];
                    this.listaTipoGenero = result.tipoGenero ?? [];
                    this.listaViveParentesco = result.tipoParentesco ?? [];
                    this.listaTipoSexo = result.tipoTipoSexo ?? [];
                    this.listaOrigenEtnico = result.origenEtnico ?? [];
                    this.listaModalidadEstudio = result.tipoTipoIntruccion ?? [];
                    this.listaTipoTrabajos = result.tipoTipoTrabajos ?? [];
                    this.listaUbigeoDireccion = result.ubigeoDireccion ?? [];
                    this.listaEstadoCivil = result.estadoCivil ?? [];
                    this.listaGruposVulnerables = result.gruposVulnerables ?? [];
                    this.listaNivelEBR = result.nivelEBR ?? [];
                    this.listaNivelSuperior = result.nivelSuperior ?? [];
                    this.listaNivelEBA = result.nivelEBA ?? [];

                    console.log('✅ Catálogos cargados:', result); // Log de control

                    resolve(); // ✅ Aquí notificamos que todo terminó bien
                },
                error: (err) => {
                    console.error('❌ Error al cargar los catálogos:', err);
                    reject(err);
                }
            });
        });
    }

    // async cargarCatalogos(): Promise<void> {
    //     const catalogos$ = forkJoin({
    //         tipoDocumento: this.funcionesUtils.obtenerListaCatalogo(
    //             'TIPO_DOCUMENTO_IDENTIFICACION',
    //             etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
    //         ),
    //         tipoGenero: this.funcionesUtils.obtenerListaCatalogo(
    //             'GENERO',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         tipoParentesco: this.funcionesUtils.obtenerListaCatalogo(
    //             'PARENTESCO',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         tipoTipoSexo: this.funcionesUtils.obtenerListaCatalogo(
    //             'TIPO_SEXO',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         origenEtnico: this.funcionesUtils.obtenerListaCatalogo(
    //             'ORIGEN_ETNICO',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         tipoTipoIntruccion: this.funcionesUtils.obtenerListaCatalogo(
    //             'MODALIDAD_ESTUDIO',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         tipoTipoTrabajos: this.funcionesUtils.obtenerListaCatalogo(
    //             'OCUPACION',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         ubigeoDireccion: this.funcionesUtils.obtenerListaCatalogo(
    //             'UBIGEO_DIRECCION',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         estadoCivil: this.funcionesUtils.obtenerListaCatalogo(
    //             'ESTADO_CIVIL',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         gruposVulnerables: this.funcionesUtils.obtenerListaCatalogo(
    //             'GRUPO_VULNERABLE',
    //             etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
    //         ),
    //         nivelEBR: this.funcionesUtils.obtenerListaCatalogo(
    //             'NIVEL_EBR',
    //             this.nemonicoMenu
    //         ),
    //         nivelSuperior: this.funcionesUtils.obtenerListaCatalogo(
    //             'NIVEL_SUPERIOR',
    //             this.nemonicoMenu
    //         ),
    //         nivelEBA: this.funcionesUtils.obtenerListaCatalogo(
    //             'NIVEL_EBA',
    //             this.nemonicoMenu
    //         ),
    //     });

    //     catalogos$.subscribe({
    //         next: (result) => {
    //             this.listaTipoDocumento = result.tipoDocumento;
    //             this.listaTipoGenero = result.tipoGenero;
    //             this.listaViveParentesco = result.tipoParentesco;
    //             this.listaTipoSexo = result.tipoTipoSexo;
    //             this.listaOrigenEtnico = result.origenEtnico;
    //             this.listaModalidadEstudio = result.tipoTipoIntruccion;
    //             this.listaTipoTrabajos = result.tipoTipoTrabajos;
    //             this.listaUbigeoDireccion = result.ubigeoDireccion;
    //             this.listaEstadoCivil = result.estadoCivil;
    //             this.listaGruposVulnerables = result.gruposVulnerables;
    //             this.listaNivelEBR = result.nivelEBR;
    //             this.listaNivelSuperior = result.nivelSuperior;
    //             this.listaNivelEBA = result.nivelEBA;
    //         },
    //         error: (err) => {
    //             console.error('Error al cargar los catálogos:', err);
    //         },
    //     });
    // }

    construirForm() {
        this.fichaIdentificacionForm = this.formBuilder.group({
            apellidoPaterno: ['', [Validators.required]],
            apellidoMaterno: ['', [Validators.required]],
            nombres: ['', [Validators.required]],
            fechaNacimiento: [null, [Validators.required]],
            edad: [null],
            alias: [''],
            estadoCivil: [0, [Validators.required]], // Campo de catálogo
            numeroHijos: [0],
            origenEtnico: [0],
            impedimentoDiscapacidad: [null],
            nombrePadre: [''],
            nombreMadre: [''],
            direccion: [''],
            ocupacion: ['', [Validators.required]],
            lugarNacimiento: [''],
            oficioInternamiento: [null],
            sentenciaResolucion: [null],
            fichaRENIEC: [null],
            examenesMedicos: [null],
            otrosEspecificar: [''],
            id_pais: [null, [Validators.required]],
            codigoUbigeoNacimiento: [null, []],
            rutaUbigeoNacimiento: [],
            codigoUbigeoUbicacion: [null],
            rutaUbigeoDireccion: [null, [Validators.required]],

            ubigeoDireccion: [0, []],

            tipoIdentificacion: [null, [Validators.required]],
            tipoSexo: [null, [Validators.required]],
            viveConParentesco: [null],
            genero: [null, [Validators.required]],
            numeroIdentificacion: [null, [Validators.required]],
            // tipoOrientacion:[null, [Validators.required]],
            edadFicha: [null, [Validators.max(50)]],
            ingresahijos: [],
            otroOrigenEtnico: [''],
            modalidadEstudio: ['0', Validators.required],
            nivelEBR: ['0'],
            nivelSuperior: ['0'],
            nivelEBA: ['0'],
            email: [null, []],
        });
        const ubigeoControl = this.fichaIdentificacionForm.get(
            'codigoUbigeoNacimiento'
        );
        ubigeoControl.disable();
    }

    observadorCambioEnCampo(campo: string, event: any) {
        if (campo === 'tipoSexo') {
            const sexoSeleccionado = event.value;
            if (sexoSeleccionado && sexoSeleccionado.nombre) {
                this.esMujer = sexoSeleccionado.nombre === 'Femenino';
                if (!this.esMujer) {
                    this.fichaIdentificacionForm
                        .get('ingresaHijos')
                        ?.setValue(false);
                }
            } else {
                this.esMujer = false;
            }
        } else if (campo === 'origenEtnico') {
            const origenEtnicoSeleccionado = this.listaOrigenEtnico.find(
                (elemento) => elemento.tokenIdentificador === event.value
            );
            if (origenEtnicoSeleccionado && origenEtnicoSeleccionado.nombre) {
                this.tieneOtroOrigenEtnico =
                    origenEtnicoSeleccionado.nombre === 'Otros';
                if (!this.tieneOtroOrigenEtnico) {
                    this.fichaIdentificacionForm
                        .get('otroOrigenEtnico')
                        ?.setValue('');
                }
            } else {
                this.tieneOtroOrigenEtnico = false;
            }
        }
    }

    private obtenerValor(key: string) {
        return this.fichaIdentificacionForm.get(key)?.value;
    }

    sinDNIChange(event: MatCheckboxChange): void {
        if (event.checked) {
            this.fichaIdentificacionForm.get('dni').setValue('');
            this.fichaIdentificacionForm.controls.dni.disable();
        } else {
            this.fichaIdentificacionForm.controls.dni.enable();
        }
    }

    empezarEdicion(fichaIdentificacionEditar: FichaIdentificacionDTO) {
        console.log('fichaIdentificacion', fichaIdentificacionEditar);
        // this.esEdicion = true;
        this.fichaIdentificacionDTO = fichaIdentificacionEditar;
        this.fichaIdentificacionForm
            .get('apellidoPaterno')
            ?.setValue(fichaIdentificacionEditar.apellidoPaterno);
        this.fichaIdentificacionForm
            .get('apellidoMaterno')
            ?.setValue(fichaIdentificacionEditar.apellidoMaterno);
        this.fichaIdentificacionForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);

        if (fichaIdentificacionEditar.fechaNacimiento) {
            let fechaNacimiento = new Date(
                fichaIdentificacionEditar.fechaNacimiento
            );
            if (fechaNacimiento) {
                this.fichaIdentificacionForm
                    .get('edadFicha')
                    .setValue(
                        this.funcionesUtils.getEdad(
                            fichaIdentificacionEditar.fechaNacimiento
                        )
                    );
                this.fichaIdentificacionForm.get('edadFicha').markAsTouched();
            }
            this.fichaIdentificacionForm
                .get('fechaNacimiento')
                ?.setValue(fechaNacimiento);
        }

        this.fichaIdentificacionForm
            .get('edad')
            ?.setValue(fichaIdentificacionEditar.edad);
        // this.fichaIdentificacionForm.get("sexo")?.setValue(fichaIdentificacionEditar.sexo);
        this.fichaIdentificacionForm
            .get('alias')
            ?.setValue(fichaIdentificacionEditar.alias);
        // this.fichaIdentificacionForm.get("nacionalidad")?.setValue(fichaIdentificacionEditar.nacionalidad);
        // this.fichaIdentificacionForm.get("sinDni")?.setValue(fichaIdentificacionEditar.sinDni ? "S" : "N");
        // this.fichaIdentificacionForm.get("dni")?.setValue(fichaIdentificacionEditar.dni);
        // this.fichaIdentificacionForm.get("viveCon")?.setValue(fichaIdentificacionEditar.viveCon);
        this.fichaIdentificacionForm
            .get('estadoCivil')
            ?.setValue(fichaIdentificacionEditar.tokenIdentificadorEstadoCivil);
        this.fichaIdentificacionForm
            .get('numeroHijos')
            ?.setValue(fichaIdentificacionEditar.numeroHijos);
        this.fichaIdentificacionForm
            .get('origenEtnico')
            ?.setValue(
                fichaIdentificacionEditar.tokenIdentificadorOrigenEtnico
            );
        this.fichaIdentificacionForm
            .get('impedimentoDiscapacidad')
            ?.setValue(
                fichaIdentificacionEditar.impedimentoDiscapacidad ? 'S' : 'N'
            );
        this.fichaIdentificacionForm
            .get('nombrePadre')
            ?.setValue(fichaIdentificacionEditar.nombrePadre);
        this.fichaIdentificacionForm
            .get('nombreMadre')
            ?.setValue(fichaIdentificacionEditar.nombreMadre);
        this.fichaIdentificacionForm
            .get('domicilioActual')
            ?.setValue(fichaIdentificacionEditar.domicilioActual);
        this.fichaIdentificacionForm
            .get('direccion')
            ?.setValue(fichaIdentificacionEditar.direccion);
        // this.fichaIdentificacionForm.get("gradoInstruccion")?.setValue(fichaIdentificacionEditar.gradoInstruccion);
        this.fichaIdentificacionForm
            .get('ocupacion')
            ?.setValue(fichaIdentificacionEditar.ocupacion);

        this.fichaIdentificacionForm
            .get('fotoPerfil')
            ?.setValue(fichaIdentificacionEditar.fotoPerfil);
        this.previewUrl1 = fichaIdentificacionEditar.fotoPerfil;
        this.fichaIdentificacionForm
            .get('fotoFrente')
            ?.setValue(fichaIdentificacionEditar.fotoFrente);
        this.previewUrl2 = fichaIdentificacionEditar.fotoFrente;
        this.fichaIdentificacionForm
            .get('oficioInternamiento')
            ?.setValue(fichaIdentificacionEditar.oficioInternamiento);
        this.fichaIdentificacionForm
            .get('sentenciaResolucion')
            ?.setValue(fichaIdentificacionEditar.sentenciaResolucion);
        // this.fichaIdentificacionForm.get("dnifisico")?.setValue(fichaIdentificacionEditar.dnifisico);
        this.fichaIdentificacionForm
            .get('fichaRENIEC')
            ?.setValue(fichaIdentificacionEditar.fichaRENIEC);
        if (fichaIdentificacionEditar.email) {
            this.fichaIdentificacionForm
                .get('email')
                ?.setValue(fichaIdentificacionEditar.email);
        }
        this.fichaIdentificacionForm
            .get('fichaRENIEC')
            ?.setValue(fichaIdentificacionEditar.fichaRENIEC);

        this.fichaIdentificacionForm
            .get('examenesMedicos')
            ?.setValue(fichaIdentificacionEditar.examenesMedicos);
        this.fichaIdentificacionForm
            .get('otrosEspecificar')
            ?.setValue(fichaIdentificacionEditar.otrosEspecificar);
        this.fichaIdentificacionForm
            .get('grupoVulnerable')
            ?.setValue(
                fichaIdentificacionEditar.tokenIdentificadorGrupoVulnerable
            );

        this.fichaIdentificacionForm
            .get('ubigeoDireccion')
            ?.setValue(
                fichaIdentificacionEditar.tokenIdentificadorUbigeoDireccion
            );

        this.fichaIdentificacionForm
            .get('id_pais')
            .setValue(fichaIdentificacionEditar.paisNacimiento);
        const lugarDeNacimientoControl =
            this.fichaIdentificacionForm.get('lugarNacimiento');
        const ubigeoControl = this.fichaIdentificacionForm.get(
            'codigoUbigeoNacimiento'
        );
        const rutaUbigeoNacimientoControl = this.fichaIdentificacionForm.get(
            'rutaUbigeoNacimiento'
        );
        if (fichaIdentificacionEditar.paisNacimiento === 'PAIS-PERU') {
            this.fichaIdentificacionForm
                .get('codigoUbigeoNacimiento')
                .setValue(fichaIdentificacionEditar.ubigeoNacimiento);
            this.fichaIdentificacionForm
                .get('rutaUbigeoNacimiento')
                .setValue(fichaIdentificacionEditar.lugarNacimiento);
            this.ES_PAIS_PERU = true;
            ubigeoControl.enable();
            ubigeoControl?.setValidators([Validators.required]);
            rutaUbigeoNacimientoControl?.setValidators([Validators.required]);

            lugarDeNacimientoControl?.clearValidators();
        } else {
            this.fichaIdentificacionForm.get('lugarNacimiento').enable();
            this.fichaIdentificacionForm
                .get('lugarNacimiento')
                ?.setValue(fichaIdentificacionEditar.lugarNacimiento);
            lugarDeNacimientoControl?.setValidators([Validators.required]);
            ubigeoControl?.clearValidators();
            rutaUbigeoNacimientoControl?.clearValidators();
            ubigeoControl.disable();
            console.log(
                'fichaIdentificacionEditar.lugarNacimiento',
                fichaIdentificacionEditar
            );
        }
        this.fichaIdentificacionForm
            .get('codigoUbigeoUbicacion')
            ?.setValue(fichaIdentificacionEditar.ubigeoUbicacion);
        if (fichaIdentificacionEditar.ubigeoUbicacion) {
            this.localidadService
                .obtenerLocalidadUbigeo(
                    fichaIdentificacionEditar.ubigeoUbicacion,
                    this.nemonicoMenu
                )
                .subscribe({
                    next: (resp: RespuestaPorDefecto<LocalidadDTO>) => {
                        // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
                        if (!resp.exito) {
                            return;
                        }
                    },
                    error: (error: any) => {
                        this.localidadService.checkError(error);
                    },
                });
        }
        this.updateAllControls();

        this.fichaIdentificacionForm
            .get('tipoIdentificacion')
            .setValue(fichaIdentificacionEditar.tipoDocumento);
        this.fichaIdentificacionForm
            .get('numeroIdentificacion')
            .setValue(fichaIdentificacionEditar.numeroDocumento);
        this.fichaIdentificacionForm
            .get('tipoSexo')
            .setValue(
                this.listaTipoSexo.find(
                    (elemento) =>
                        elemento.tokenIdentificador ===
                        fichaIdentificacionEditar.tipoSexo
                )
            );
        this.encontrarTipoSexo(fichaIdentificacionEditar.tipoSexo) ===
            'Femenino'
            ? (this.esMujer = true)
            : (this.esMujer = false);
        console.log(
            'sexo escogido',
            this.listaTipoSexo.find(
                (elemento) =>
                    elemento.tokenIdentificador ===
                    fichaIdentificacionEditar.tipoSexo
            )
        );
        console.log('sexo escogido token', fichaIdentificacionEditar.tipoSexo);
        this.fichaIdentificacionForm
            .get('genero')
            .setValue(fichaIdentificacionEditar.tipoGenero);
        const catDocu = this.listaTipoDocumento.find(
            (x) =>
                x.tokenIdentificador == fichaIdentificacionEditar.tipoDocumento
        );
        this.fichaIdentificacionForm
            .get('ingresahijos')
            ?.setValue(fichaIdentificacionEditar.ingresahijos ? 'S' : 'N');
        this.esMujer =
            this.encontrarTipoSexo(fichaIdentificacionEditar.tipoSexo) ===
            'Femenino';
        this.fichaIdentificacionForm
            .get('otroOrigenEtnico')
            ?.setValue(fichaIdentificacionEditar.otroOrigenEtnico);
        this.tieneOtroOrigenEtnico =
            this.listaOrigenEtnico.find(
                (elemento) =>
                    elemento.tokenIdentificador ===
                    fichaIdentificacionEditar.tokenIdentificadorOrigenEtnico
            )?.nombre === 'Otros';

        // Agregar el manejo de modalidad de estudio y niveles
        if (fichaIdentificacionEditar.modalidadEstudio) {
            this.fichaIdentificacionForm
                .get('modalidadEstudio')
                ?.setValue(fichaIdentificacionEditar.modalidadEstudio);
            this.modalidadEstudio = fichaIdentificacionEditar.modalidadEstudio;
            console.log(fichaIdentificacionEditar.modalidadEstudio);
            // Setear el nivel correspondiente según la modalidad
            switch (fichaIdentificacionEditar.modalidadEstudio) {
                case 'MODALIDAD_ESTUDIO_EBR':
                    this.fichaIdentificacionForm
                        .get('nivelEBR')
                        ?.setValue(fichaIdentificacionEditar.nivelEBR);
                    break;
                case 'MODALIDAD_ESTUDIO_SUPERIOR':
                    this.fichaIdentificacionForm
                        .get('nivelSuperior')
                        ?.setValue(fichaIdentificacionEditar.nivelSuperior);
                    break;
                case 'MODALIDAD_ESTUDIO_EBA':
                    this.fichaIdentificacionForm
                        .get('nivelEBA')
                        ?.setValue(fichaIdentificacionEditar.nivelEBA);
                    break;
            }
        }

        // this.actualizarValidaciones(
        //     this.listaTipoDocumento.find(
        //         (x) =>
        //             x.tokenIdentificador ===
        //             fichaIdentificacionEditar.tipoDocumento
        //     )?.nemonico
        // );
        this.actualizarValidaciones(
            this.listaTipoDocumento.find(
                (x) =>
                    x.tokenIdentificador === fichaIdentificacionEditar.tipoDocumento
            )?.nemonico
        );

        this.fichaIdentificacionForm
            .get('numeroIdentificacion')
            .setValue(fichaIdentificacionEditar.numeroDocumento);

        const controlNumero = this.fichaIdentificacionForm.get('numeroIdentificacion');
        controlNumero.updateValueAndValidity({ onlySelf: true, emitEvent: false });
        controlNumero.markAsPristine();
        controlNumero.markAsUntouched();
    }

    encontrarTipoSexo(tokenIdentificadorTipoSexo: string) {
        let tipoSexo = this.listaTipoSexo.find(
            (elemento) =>
                elemento.tokenIdentificador === tokenIdentificadorTipoSexo
        )?.nombre;
        return tipoSexo;
    }

    cancelarEdicion() {
        this.esEdicion = false;
        this.fichaIdentificacionForm.reset();
        this.fichaIdentificacionDTO = null;

        this.router.navigate(['/gestion-adolescente/ficha-identificacion']);
    }

    ejecutarAccion() {
        this.checkFormValidity(this.fichaIdentificacionForm);
        if (!this.fichaIdentificacionForm.valid) {
            const invalidControls = this.getInvalidControls();
            console.log('Controles inválidos:', invalidControls);
            return;
        }
        this.fichaIdentificacionForm.disable();

        let fichaIdentificacion = new FichaIdentificacionDTO();

        fichaIdentificacion.tokenIdentificador =
            this.fichaIdentificacionDTO?.tokenIdentificador;

        fichaIdentificacion.apellidoPaterno =
            this.obtenerValor('apellidoPaterno');
        fichaIdentificacion.apellidoMaterno =
            this.obtenerValor('apellidoMaterno');
        fichaIdentificacion.nombres = this.obtenerValor('nombres');
        fichaIdentificacion.fechaNacimiento =
            this.obtenerValor('fechaNacimiento');
        fichaIdentificacion.edad = this.obtenerValor('edad');
        // fichaIdentificacion.sexo = this.obtenerValor("sexo");
        fichaIdentificacion.alias = this.obtenerValor('alias');
        // fichaIdentificacion.nacionalidad = this.obtenerValor("nacionalidad");
        // fichaIdentificacion.sinDni = this.obtenerValor("sinDni") == "S" ? true : false;
        // fichaIdentificacion.dni = this.obtenerValor("sinDni") == "S" ? "" : this.obtenerValor("dni");
        fichaIdentificacion.tokenIdentificadorEstadoCivil =
            this.obtenerValor('estadoCivil');
        fichaIdentificacion.numeroHijos = this.obtenerValor('numeroHijos');
        fichaIdentificacion.tokenIdentificadorOrigenEtnico =
            this.obtenerValor('origenEtnico');
        fichaIdentificacion.impedimentoDiscapacidad =
            this.fichaIdentificacionForm.get('impedimentoDiscapacidad')
                .value === 'S'
                ? true
                : false;
        fichaIdentificacion.nombrePadre = this.obtenerValor('nombrePadre');
        fichaIdentificacion.nombreMadre = this.obtenerValor('nombreMadre');
        fichaIdentificacion.domicilioActual =
            this.obtenerValor('domicilioActual');
        fichaIdentificacion.direccion = this.obtenerValor('direccion');
        // fichaIdentificacion.tokenIdentificadorProvincia = this.obtenerValor("provincia");
        // fichaIdentificacion.tokenIdentificadorDistrito = this.obtenerValor("distrito");
        // fichaIdentificacion.gradoInstruccion = this.obtenerValor("gradoInstruccion");
        fichaIdentificacion.ocupacion = this.obtenerValor('ocupacion');
        // fichaIdentificacion.viveCon = this.obtenerValor("viveCon");
        fichaIdentificacion.lugarNacimiento =
            this.obtenerValor('lugarNacimiento');
        fichaIdentificacion.fotoPerfil = this.obtenerValor('fotoPerfil');
        fichaIdentificacion.fotoFrente = this.obtenerValor('fotoFrente');
        fichaIdentificacion.oficioInternamiento = this.obtenerValor(
            'oficioInternamiento'
        );
        fichaIdentificacion.sentenciaResolucion = this.obtenerValor(
            'sentenciaResolucion'
        );
        // fichaIdentificacion.dnifisico = this.obtenerValor("dnifisico") == "S" ? true : false;;
        fichaIdentificacion.fichaRENIEC =
            this.obtenerValor('fichaRENIEC') == 'S' ? true : false;
        fichaIdentificacion.examenesMedicos =
            this.obtenerValor('examenesMedicos') == 'S' ? true : false;
        fichaIdentificacion.otrosEspecificar =
            this.obtenerValor('otrosEspecificar');
        fichaIdentificacion.fotoPerfil = this.obtenerValor('fotoPerfil');
        fichaIdentificacion.fotoFrente = this.obtenerValor('fotoFrente');
        fichaIdentificacion.tokenIdentificadorGrupoVulnerable =
            this.obtenerValor('grupoVulnerable');
        fichaIdentificacion.paisNacimiento = this.obtenerValor('id_pais');
        if (fichaIdentificacion.paisNacimiento === 'PAIS-PERU') {
            fichaIdentificacion.ubigeoNacimiento = this.obtenerValor(
                'codigoUbigeoNacimiento'
            );
        }
        fichaIdentificacion.ubigeoUbicacion = this.obtenerValor(
            'codigoUbigeoUbicacion'
        );
        fichaIdentificacion.tokenIdentificadorUbigeoDireccion =
            this.obtenerValor('ubigeoDireccion');
        fichaIdentificacion.tipoDocumento =
            this.fichaIdentificacionForm.get('tipoIdentificacion').value;
        fichaIdentificacion.numeroDocumento = this.fichaIdentificacionForm.get(
            'numeroIdentificacion'
        ).value;
        fichaIdentificacion.tipoSexo =
            this.fichaIdentificacionForm.get(
                'tipoSexo'
            ).value.tokenIdentificador;
        fichaIdentificacion.tipoGenero =
            this.fichaIdentificacionForm.get('genero').value;
        fichaIdentificacion.modalidadEstudio =
            this.obtenerValor('modalidadEstudio');
        // Asignar el nivel según la modalidad seleccionada
        if (this.modalidadEstudio === 'MODALIDAD_ESTUDIO_EBR') {
            fichaIdentificacion.nivelEBR = this.obtenerValor('nivelEBR');
        } else if (this.modalidadEstudio === 'MODALIDAD_ESTUDIO_SUPERIOR') {
            fichaIdentificacion.nivelSuperior =
                this.obtenerValor('nivelSuperior');
        } else if (this.modalidadEstudio === 'MODALIDAD_ESTUDIO_EBA') {
            fichaIdentificacion.nivelEBA = this.obtenerValor('nivelEBA');
        }
        // fichaIdentificacion.tipoViveCon = this.fichaIdentificacionForm.get('viveConParentesco').value;
        fichaIdentificacion.ingresahijos =
            this.fichaIdentificacionForm.get('ingresahijos').value === 'S'
                ? true
                : false;
        fichaIdentificacion.otroOrigenEtnico =
            this.fichaIdentificacionForm.get('otroOrigenEtnico').value;
        fichaIdentificacion.centro = this.centro;
        fichaIdentificacion.crearFichaIngreso = !this.esEdicion;
        fichaIdentificacion.esEdicion = this.esEdicion;
        fichaIdentificacion.email =
            this.fichaIdentificacionForm.get('email').value;
        this.fichaIdentificacionService
            .crearFichaIdentificacion(
                fichaIdentificacion,
                etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<FichaIdentificacionDTO>
                ) => {
                    this.fichaIdentificacionForm.enable();

                    // this.completoOperacion.emit(response.exito);
                    if (!response.exito) {
                        this.fichaIdentificacionService.checkError(response);

                        return;
                    }

                    this.router.navigate([
                        '/gestion-adolescente/ficha-identificacion',
                    ]);
                },
                error: (error: any) => {
                    this.fichaIdentificacionService.checkError(error);
                    this.fichaIdentificacionForm.enable();
                },
            });
    }

    obtenerFichaIdentificacionPorToken(tokenIdentificador: string) {
        let load = this.dialogMensajeService.mensajeLoading(
            'Obteniendo la ficha de identificación..'
        );
        this.fichaIdentificacionService
            .obtenerFichaIdentificacionPorTokenIdentificador(
                tokenIdentificador,
                this.nemonicoMenu
            )
            .subscribe({
                next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                    load.close();
                    // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                    if (!resp.exito) {
                        return;
                    }
                    this.esEdicion = true;
                    this.empezarEdicion(resp.data);
                },
                error: (error: any) => {
                    load.close();

                    this.fichaIdentificacionService.checkError(error);
                },
            });
    }

    agregarNuevoDocumento() {
        let ref = this.dialog.open(SubidaDocumentosFichaPrincipalComponent, {
            hasBackdrop: false,
            disableClose: true,
            panelClass: ['w-full', 'h-4/5'],
        });

        ref.componentInstance.nemonicoMenu = this.nemonicoMenu;
        ref.componentInstance.tokenIdentificadorFichaIdentificacion =
            this.tokenIdentificadorFichaIdentificacion;

        ref.afterClosed().subscribe({
            next: (response: boolean) => {
                if (response) {
                    this.obtenerDocumentos();
                }
            },
            error: (error: any) => {
                console.error(error);
            },
        });
    }

    obtenerDocumentos() {
        if (
            !this.tokenIdentificadorFichaIdentificacion ||
            !this.tablaDocumentos
        ) {
            return;
        }

        const page = this.tablaDocumentos.page || 0; // Valor por defecto si no existe
        const pageSize = this.tablaDocumentos.pageSize || 10;

        const fichaPrincipalDocumentosRequest =
            new FichaPrincipalDocumentosRequest();
        fichaPrincipalDocumentosRequest.page = page;
        fichaPrincipalDocumentosRequest.size = pageSize;
        fichaPrincipalDocumentosRequest.textoBuscar =
            this.tablaDocumentos.textoBuscar || '';
        fichaPrincipalDocumentosRequest.tokenIdentificadorFichaIdentificacion =
            this.tokenIdentificadorFichaIdentificacion;

        this.fichaPrincipalDocumentoService
            .obtenerDocumentos(
                fichaPrincipalDocumentosRequest,
                this.nemonicoMenu
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<FichaDeIdentificacionDocumentoDTO>
                    >
                ) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this.fichaPrincipalDocumentoService.checkError(
                            response
                        );
                    }

                    if (response.data?.data && this.tablaDocumentos) {
                        this.tablaDocumentos.actualizarTabla(
                            response.data.data,
                            response.data.totalItems
                        );
                    }
                },
                error: (error: any) => {
                    this.fichaPrincipalDocumentoService.checkError(error);
                },
            });
    }

    eliminacionDocumento(documentoDTO: DocumentoDTO) {
        let load = this.dialogMensajeService.mensajeLoading(
            'Quitando el documento: ' + documentoDTO.nombre + ' de la ficha..'
        );
        let fichaIdentificacionDocumentoDTO =
            new FichaIdentificacionDocumentoDTO();
        fichaIdentificacionDocumentoDTO.tokenIdentificadorDocumento =
            documentoDTO.tokenIdentificador;
        fichaIdentificacionDocumentoDTO.tokenIdentificadorFichaIdentificacion =
            this.tokenIdentificadorFichaIdentificacion;
        this.fichaPrincipalDocumentoService
            .eliminarDocumento(
                fichaIdentificacionDocumentoDTO,
                this.nemonicoMenu
            )
            .subscribe({
                next: (
                    respone: RespuestaPorDefecto<FichaIdentificacionDocumentoDTO>
                ) => {
                    load.close();
                    if (!respone.exito) {
                        this.fichaPrincipalDocumentoService.checkError(respone);
                    }

                    this.obtenerDocumentos();
                },
                error: (error: any) => {
                    load.close();
                    this.fichaPrincipalDocumentoService.checkError(error);
                },
            });
    }

    edicionEvent(exito: boolean) {
        if (exito) {
            this.obtenerDocumentos();
        }
    }

    buscarAchivo(texto: string) {
        if (!environment.production) {
            console.log(texto);
        }

        this.obtenerDocumentos();
    }

    pageEventDocumentos(event: PageEvent) {
        this.tablaDocumentos.page = event.pageIndex;
        this.tablaDocumentos.pageSize = event.pageSize;

        this.obtenerDocumentos();
    }

    verificarPais(event: any) {
        let nemonico = event.value;
        this.ES_PAIS_PERU =
            this.paises?.find((pais) => pais.nemonico === 'PAIS-PERU')
                ?.nemonico === nemonico;

        const lugarDeNacimientoControl =
            this.fichaIdentificacionForm.get('lugarNacimiento');
        const ubigeoControl = this.fichaIdentificacionForm.get(
            'codigoUbigeoNacimiento'
        );
        const rutaUbigeoNacimientoControl = this.fichaIdentificacionForm.get(
            'rutaUbigeoNacimiento'
        );

        if (this.ES_PAIS_PERU) {
            ubigeoControl.enable();
            ubigeoControl?.setValidators([Validators.required]);
            rutaUbigeoNacimientoControl?.setValidators([Validators.required]);

            lugarDeNacimientoControl?.clearValidators();
        } else {
            lugarDeNacimientoControl?.setValidators([Validators.required]);
            ubigeoControl?.clearValidators();
            rutaUbigeoNacimientoControl?.clearValidators();
            ubigeoControl.disable();
            if (this.fichaIdentificacionDTO?.lugarNacimiento) {
                this.fichaIdentificacionForm
                    .get('lugarNacimiento')
                    .setValue(this.fichaIdentificacionDTO?.lugarNacimiento);
            } else {
                this.fichaIdentificacionForm
                    .get('lugarNacimiento')
                    .setValue(null);
            }
        }
        this.updateAllControls();
    }

    // if (tipo_de_identificacion.nemonico === etiqueta.SIN_IDENTIFICACIÓN) {
    //   this.formulario_fp.setControl("documentoIdentidad", new FormControl("", []));
    //   this.formulario_fp.get("documentoIdentidad")?.disable();
    // } else {
    //   this.formulario_fp.setControl("documentoIdentidad",
    //     new FormControl(this.formulario_fp.get("documentoIdentidad").value, [Validators.required]));
    //   this.formulario_fp.get("documentoIdentidad")?.enable();
    // }

    // verificarTipoDocumento(event: any) {
    //   let token = event.value;
    //   const tipoDocu = this.listaTipoDocumento.find(x => x.tokenIdentificador == token);
    //   this.documentoSeleccionado = tipoDocu;
    //   this.fichaIdentificacionForm.get('numeroIdentificacion').enable();
    //   if (tipoDocu.nemonico === "TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO") {
    //     this.fichaIdentificacionForm.setControl('numeroIdentificacion', new
    //       FormControl('', []));
    //     this.fichaIdentificacionForm.get("numeroIdentificacion").disable();
    //   } else {
    //     // ideControl?.setValidators([Validators.required]);
    //     this.fichaIdentificacionForm.setControl('numeroIdentificacion', new
    //       FormControl(this.fichaIdentificacionForm.get("numeroIdentificacion").value, [Validators.required]));
    //     this.fichaIdentificacionForm.get("numeroIdentificacion").enable();
    //     this.actualizarValidaciones(tipoDocu.nemonico);
    //   }
    //   if(!this.esEdicion){
    //     this.agregarSubs();
    //   }
    //   const num = this.fichaIdentificacionForm.get('numeroIdentificacion').value;
    //   if (num && num.length > 0) {
    //     this.fichaIdentificacionForm.get('numeroIdentificacion').markAsTouched();
    //   }
    //   this.updateAllControls();
    // }

    verificarTipoDocumento(event: any): void {
        const tipoIdentificacion = event.value;
        this.documentoSeleccionado = this.listaTipoDocumento.find(
            (x) => x.tokenIdentificador === tipoIdentificacion
        );

        const controlNumero = this.fichaIdentificacionForm.get(
            'numeroIdentificacion'
        );
        if (!controlNumero) return;

        // Limpia validaciones anteriores
        controlNumero.clearValidators();

        // Configura validaciones según tipo de documento
        if (
            tipoIdentificacion ===
            'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA' ||
            tipoIdentificacion ===
            'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA'
        ) {
            controlNumero.setValidators([
                Validators.required,
                Validators.minLength(4),
                Validators.maxLength(20),
            ]);
        } else if (tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
            controlNumero.setValidators([
                Validators.required,
                Validators.minLength(8),
                Validators.maxLength(8),
                Validators.pattern(/^\d+$/),
            ]);
            // Puedes colocar aquí tu llamada directa a RENIEC si es inmediata:
            // this.consultarDatosReniec();  <-- SOLO si quieres que se consulte enseguida
        } else {
            // Otros documentos con validación básica
            controlNumero.setValidators([Validators.required]);
        }

        controlNumero.setValue(''); // Limpia input
        controlNumero.updateValueAndValidity({
            onlySelf: true,
            emitEvent: false,
        });
        controlNumero.markAsUntouched();
        controlNumero.markAsPristine();
    }

    obtenerLocalidadesPorPadre(nemonicoPadre: string, nemonicoTipo: string) {
        console.log('datos', nemonicoPadre + nemonicoTipo);
        this.localidadService
            .obtenerHijos(
                nemonicoPadre,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
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

                    if (nemonicoTipo === 'PAIS') {
                        this.paises = response.data;
                    } else if (nemonicoTipo === 'DEPARTAMENTO') {
                        this.departamentos = response.data;
                    } else if (nemonicoTipo === 'PROVINCIA') {
                        this.provincias = response.data;
                    } else if (nemonicoTipo === 'DISTRITO') {
                        this.distritos = response.data;
                    }
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    updateAllControls() {
        Object.keys(this.fichaIdentificacionForm.controls).forEach(
            (controlName) => {
                this.fichaIdentificacionForm
                    .get(controlName)
                    ?.updateValueAndValidity();
            }
        );
    }

    consultarProvincias(event: any) {
        this.provincias = [];
        this.distritos = [];
        this.fichaIdentificacionForm.get('id_provincia').setValue(null);
        this.fichaIdentificacionForm.get('provincia_no_peru').setValue(null);
        this.fichaIdentificacionForm.get('distrito_no_peru').setValue(null);
        this.fichaIdentificacionForm.get('id_distrito').setValue(null);
        let nemonico = event.value;
        this.obtenerLocalidadesPorPadre(nemonico, 'PROVINCIA');
    }

    consultarDistritos(event: any) {
        this.distritos = [];
        this.fichaIdentificacionForm.get('distrito_no_peru').setValue(null);
        this.fichaIdentificacionForm.get('id_distrito').setValue(null);
        let nemonico = event.value;
        this.obtenerLocalidadesPorPadre(nemonico, 'DISTRITO');
    }

    listarLocalidad(nemonicoTipo: string) {
        this.localidadService
            .obtenerPorTipo(
                nemonicoTipo,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
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

                    if (nemonicoTipo === 'PAIS') {
                        this.paises = response.data;
                    } else if (nemonicoTipo === 'DEPARTAMENTO') {
                        this.departamentos = response.data;
                    }
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
        this.subscriptionIdentificacion.unsubscribe();
    }

    checkFormValidity(formGroup: FormGroup): void {
        Object.keys(formGroup.controls).forEach((key) => {
            const control = formGroup.get(key);
            if (control.invalid) {
                console.error(`Control "${key}" es inválido`, control.errors);
            }
        });
    }

    calcularEdad(event: any) {
        console.log('Al cambiar fecha: ' + event.value);
        this.fichaIdentificacionForm
            .get('edadFicha')
            .setValue(this.funcionesUtils.calcularEdad(event));
        const edad = this.funcionesUtils.calcularEdad(event);
        if (edad >= 51) {
            this.dialogMensajeService.mensajeAdvertencia(
                'Atención',
                'La edad del ingresado no puede ser mayor a 50 años.'
            );
        }
    }

    soloNumero(event: KeyboardEvent): void {
        const allowedKeys = [
            'Backspace',
            'ArrowLeft',
            'ArrowRight',
            'Tab',
            'Delete',
        ];
        const isNumberKey = event.key >= '0' && event.key <= '9';

        if (!isNumberKey && !allowedKeys.includes(event.key)) {
            event.preventDefault();
        }
    }

    actualizarValidaciones(tipoIdentificacion: string): void {
        const numeroDocumentoControl = this.fichaIdentificacionForm.get(
            'numeroIdentificacion'
        );
        const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
            (doc) => doc.nemonico === tipoIdentificacion
        );

        if (tipoDocumentoSeleccionado) {
            const { minLength, maxLength, regex } = tipoDocumentoSeleccionado;

            // Aplica validaciones dinámicas
            numeroDocumentoControl?.setValidators([
                Validators.required,
                Validators.minLength(minLength),
                Validators.maxLength(maxLength),
                Validators.pattern(regex),
            ]);
            const num = numeroDocumentoControl.value;
            if (num && num.length > 0) {
                numeroDocumentoControl.markAsTouched();
            }
        } else {
            // Limpia las validaciones si no hay un tipo seleccionado válido
            numeroDocumentoControl?.setValidators([Validators.required]);
        }

        // Refresca el estado del control
        numeroDocumentoControl?.updateValueAndValidity();
    }

    abrirModalUbigeo() {
        const dialogRef = this.dialog.open(SeleccionarUbigeoComponent, {
            width: '600px',
        });
        dialogRef.afterClosed().subscribe(async (result) => {
            if (result) {
                this.fichaIdentificacionForm
                    .get('codigoUbigeoNacimiento')
                    .setValue(result);
            }
        });
    }

    abrirModalUbigeoActual() {
        const dialogRef = this.dialog.open(SeleccionarUbigeoComponent, {
            width: '600px',
        });
        dialogRef.afterClosed().subscribe(async (result) => {
            console.log('result', result);
            if (result) {
                this.fichaIdentificacionForm
                    .get('codigoUbigeoUbicacion')
                    .setValue(result);
            }
        });
    }

    cargarCentro() {
        this.jerarquiaService
            .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
            .subscribe({
                next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
                    if (!environment.production) {
                        console.log(respuesta.data);
                    }
                    if (!respuesta.exito) {
                        this.jerarquiaService.checkError(respuesta);
                        return;
                    }

                    this.centro = respuesta.data;
                    
                    // Validación adicional para evitar errores
                    if (this.centro?.jerarquiaPadre?.nemonico !== 'SOA') {
                        this.fichaIdentificacionForm
                            .get('email')
                            ?.clearValidators();
                        this.fichaIdentificacionForm
                            .get('email')
                            ?.updateValueAndValidity();
                        this.fichaIdentificacionForm.get('email')?.disable();
                    }
                },
                error: (error: any) => {
                    this.jerarquiaService.checkError(error);
                    // Inicializar centro con objeto vacío en caso de error
                    this.centro = new JerarquiaDTO();
                },
            });
    }

    async imprimirFicha() {
        try {
            let solicitudPdf = new GeneracionPdfRequest();
            solicitudPdf.nemonico = 'FORMULARIO_FICHA_INDENTIFICACION';
            const variablesGenerales = {
                '[TITULO-PLANTILLA]': 'Informe Datos Generales',
                '[IMG_BASE64]': this.base64Image,
                '[FECHA_REGISTRO]': new Date().toISOString().split('T')[0],
                '[HORA_REGISTRO]': new Date().toTimeString().split(' ')[0],
                '[TITULO-INFORME]': 'Ficha de Identificación',
                '[CENTRO]': this.fichaIdentificacionDTO.centroIngreso,
                '[TIPO-DOCUMENTO]':
                    this.funcionesUtils.obtenerNombreCatalogoPorToken(
                        this.obtenerValor('tipoIdentificacion'),
                        this.listaTipoDocumento
                    ) || '',
                '[NOMBRE-APELLIDOS]':
                    this.fichaIdentificacionDTO.apellidoPaterno +
                    ' ' +
                    this.fichaIdentificacionDTO.apellidoMaterno +
                    ' ' +
                    this.fichaIdentificacionDTO.nombres,
                '[FECHA-NACIMIENTO]': this.funcionesUtils.formatearFecha(
                    this.fichaIdentificacionDTO.fechaNacimiento
                ),
                '[NUMERO-IDENTIFICACION]': this.obtenerValor(
                    'numeroIdentificacion'
                ),
                '[EDAD]': this.obtenerValor('edadFicha') || '',
                '[ALIAS]': this.fichaIdentificacionDTO.alias || '',
                '[SEXO]':
                    this.funcionesUtils.obtenerNombreCatalogoPorToken(
                        this.fichaIdentificacionDTO.tipoSexo,
                        this.listaTipoSexo
                    ) || '',
                '[GENERO]':
                    this.funcionesUtils.obtenerNombreCatalogoPorToken(
                        this.obtenerValor('genero'),
                        this.listaTipoGenero
                    ) || '',
                '[ORIGENETNICO]':
                    this.funcionesUtils.obtenerNombreCatalogoPorToken(
                        this.obtenerValor('origenEtnico'),
                        this.listaOrigenEtnico
                    ) || '',
                '[ESTADO-CIVIL]':
                    this.funcionesUtils.obtenerNombreCatalogoPorToken(
                        this.obtenerValor('estadoCivil'),
                        this.listaEstadoCivil
                    ) || '',
                '[PAIS-NACIMIENTO]':
                    this.funcionesUtils.obtenerNombreLocalidadPorToken(
                        this.obtenerValor('id_pais'),
                        this.paises
                    ) || '',
                '[NUMERO-HIJOS]': this.obtenerValor('numeroHijos') || '',
                '[LUGAR-NACIMIENTO]':
                    this.obtenerValor('id_pais') == 'PAIS-PERU'
                        ? this.obtenerValor('rutaUbigeoNacimiento')
                        : this.obtenerValor('lugarNacimiento') || 's/n',

                '[UBIGEO-DIRECCION]':
                    this.obtenerValor('rutaUbigeoDireccion') || 's/n',
                '[DIRECCION]': this.obtenerValor('direccion') || 's/n',
                '[DISCAPACIDAD]': this.fichaIdentificacionDTO
                    .impedimentoDiscapacidad
                    ? 'SI'
                    : 'NO',
                '[MODALIDAD-ESTUDIO]':
                    this.funcionesUtils.obtenerNombreCatalogoPorNemonico(
                        this.obtenerValor('modalidadEstudio'),
                        this.listaModalidadEstudio
                    ) || '',
                '[TIPO-MODALIDAD-ESTUDIO]':
                    this.listaModalidadEstudio.find(
                        (x) =>
                            x.nemonico == this.obtenerValor('modalidadEstudio')
                    )?.nombre || '',
                '[NIVEL-EBA]':
                    this.obtenerValor('modalidadEstudio') ==
                        'MODALIDAD_ESTUDIO_EBR'
                        ? this.funcionesUtils.obtenerNombreCatalogoPorToken(
                            this.obtenerValor('nivelEBR'),
                            this.listaNivelEBR
                        ) || ''
                        : this.obtenerValor('modalidadEstudio') ==
                            'MODALIDAD_ESTUDIO_SUPERIOR'
                            ? this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                this.obtenerValor('nivelSuperior'),
                                this.listaNivelSuperior
                            ) || ''
                            : this.obtenerValor('modalidadEstudio') ==
                                'MODALIDAD_ESTUDIO_EBA'
                                ? this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                    this.obtenerValor('nivelEBA'),
                                    this.listaNivelEBA
                                ) || ''
                                : '',
            };
            solicitudPdf.variables = variablesGenerales;
            this.pdfService
                .generarPdf(solicitudPdf, this.nemonicoMenu)
                .subscribe({
                    next: (respuesta: RespuestaPorDefecto<string>) => {
                        if (!respuesta.exito) {
                            this.dialogMensajeService.mensajeError(
                                'Error al generar el PDF'
                            );
                            return;
                        }
                        window.open(
                            window.URL.createObjectURL(
                                this.funcionesUtils.getPdfBlob(respuesta.data)
                            )
                        );
                    },
                    error: () =>
                        this.dialogMensajeService.mensajeError(
                            'Error al procesar la solicitud'
                        ),
                });
        } catch (error) {
            this.dialogMensajeService.mensajeError('Error al generar el PDF');
        }
    }

    getInvalidControls() {
        const invalidControls: { [key: string]: any } = {};
        Object.keys(this.fichaIdentificacionForm.controls).forEach((key) => {
            const control = this.fichaIdentificacionForm.get(key);
            if (control && control.invalid) {
                invalidControls[key] = control.errors;
            }
        });
        return invalidControls;
    }

    onModalidadEstudioChange(event: any) {
        const modalidad = event.value;
        this.modalidadEstudio = modalidad;

        // Resetear todos los niveles
        this.fichaIdentificacionForm.get('nivelEBR').setValue('0');
        this.fichaIdentificacionForm.get('nivelSuperior').setValue('0');
        this.fichaIdentificacionForm.get('nivelEBA').setValue('0');

        // Aplicar validadores según la modalidad seleccionada
        if (modalidad === 'MODALIDAD_ESTUDIO_EBR') {
            this.fichaIdentificacionForm
                .get('nivelEBR')
                .setValidators([Validators.required]);
        } else if (modalidad === 'MODALIDAD_ESTUDIO_SUPERIOR') {
            this.fichaIdentificacionForm
                .get('nivelSuperior')
                .setValidators([Validators.required]);
        } else if (modalidad === 'MODALIDAD_ESTUDIO_EBA') {
            this.fichaIdentificacionForm
                .get('nivelEBA')
                .setValidators([Validators.required]);
        }

        // Actualizar validaciones
        this.fichaIdentificacionForm.get('nivelEBR').updateValueAndValidity();
        this.fichaIdentificacionForm
            .get('nivelSuperior')
            .updateValueAndValidity();
        this.fichaIdentificacionForm.get('nivelEBA').updateValueAndValidity();
    }

    formatFecha(fecha: string): string {
        const date = new Date(fecha);
        return date.toLocaleDateString('es-ES', {
            day: '2-digit',
            month: 'long',
            year: 'numeric',
        });
    }

    loadImageAsBase64() {
        this.http
            .get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe((data: ArrayBuffer) => {
                const base64String = this.arrayBufferToBase64(data);
                this.base64Image = `data:image/png;base64,${base64String}`;
            });
    }

    arrayBufferToBase64(buffer: ArrayBuffer): string {
        const binary = String.fromCharCode(...new Uint8Array(buffer));
        return window.btoa(binary);
    }

    soloNumeroPasaporte(event: KeyboardEvent): void {
        const allowedKeys = [
            'Backspace',
            'ArrowLeft',
            'ArrowRight',
            'Tab',
            'Delete',
        ];

        if (
            this.documentoSeleccionado.nemonico !==
            'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE'
        ) {
            const isNumberKey = event.key >= '0' && event.key <= '9';

            if (!isNumberKey && !allowedKeys.includes(event.key)) {
                event.preventDefault();
            }
        }
    }

    actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
        if (event.value) {
            const fecha = event.value;
            this.fichaIdentificacionForm.get(controlName).setValue(fecha);
        }
    }

    datosEncontrados(fichaIdentificacionEditar: FichaIdentificacionDTO) {
        this.fichaIdentificacionForm
            .get('apellidoPaterno')
            .setValue(fichaIdentificacionEditar.apellidoPaterno);
        this.fichaIdentificacionForm
            .get('apellidoMaterno')
            ?.setValue(fichaIdentificacionEditar.apellidoMaterno);
        this.fichaIdentificacionForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);
        this.fichaIdentificacionForm
            .get('tipoSexo')
            .setValue(fichaIdentificacionEditar.tipoSexo);
        this.sexoEscogido = this.listaTipoSexo.find(
            (x) => x.tokenIdentificador == fichaIdentificacionEditar.tipoSexo
        );
        if (this.sexoEscogido.nemonico !== 'TIPO_SEXO_FEMENINO') {
            this.fichaIdentificacionForm.get('ingresahijos').setValue(false);
            this.fichaIdentificacionForm.get('ingresahijos').disable();
        } else {
            this.fichaIdentificacionForm.get('ingresahijos').enable();
        }
        this.fichaIdentificacionForm
            .get('ingresahijos')
            ?.setValue(fichaIdentificacionEditar.ingresahijos);
        const fechaNacimiento = new Date(
            fichaIdentificacionEditar.fechaNacimiento
        );
        this.fichaIdentificacionForm
            .get('fechaNacimiento')
            .setValue(fechaNacimiento);
        this.fichaIdentificacionForm
            .get('edad')
            .setValue(
                this.funcionesUtils.getEdad(
                    fichaIdentificacionEditar.fechaNacimiento
                )
            );
    }

    datosNoEncontrados() {
        this.fichaIdentificacionForm.get('apellidoPaterno').setValue(null);
        this.fichaIdentificacionForm.get('apellidoMaterno')?.setValue(null);
        this.fichaIdentificacionForm.get('nombres')?.setValue(null);
        this.fichaIdentificacionForm.get('tipoSexo').setValue(null);
        this.fichaIdentificacionForm.get('ingresahijos')?.setValue(false);
        this.fichaEncontrada = null;
    }

    // agregarSubs() {
    //   const numeroIdentControl = this.fichaIdentificacionForm.get('numeroIdentificacion');

    //   if (!numeroIdentControl) {
    //     console.error('El control numeroIdentificacion no existe en el formulario.');
    //     return;
    //   }

    //   const numeroIdent = numeroIdentControl.valueChanges
    //     .pipe(
    //       filter(value => !!value), // Ignora valores vacíos o null
    //       distinctUntilChanged() // Evita valores repetidos
    //     )
    //     .subscribe((value) => {
    //       console.log('Valor cambiado:', value);

    //       let limiteInf = 0;
    //       let limiteSup = 0;
    //       if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
    //         limiteInf = limiteSup = 8;
    //       } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA'
    //         || this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA'
    //       ) {
    //         this.obtenerFichaIdentificacionNumeroDocumento(value);
    //         limiteInf = limiteSup = 12;
    //       }
    //       else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE'
    //       ) {
    //         limiteInf = 6;
    //         limiteSup = 10;
    //       } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
    //         limiteInf = 1;
    //         limiteSup = 10;
    //       }
    //       else {
    //         limiteInf = 8;
    //         limiteSup = 8;
    //       }

    //       if (value.length >= limiteInf && value.length <= limiteSup) {
    //         console.log('obteniendo ficha');
    //         this.obtenerFichaIdentificacionNumeroDocumento(value);
    //       }
    //     });

    //   this.subscriptionIdentificacion.add(numeroIdent);
    // }

    // agregarSubs() {
    //     const numeroIdentControl = this.fichaIdentificacionForm.get(
    //         'numeroIdentificacion'
    //     );

    //     if (!numeroIdentControl) {
    //         console.error(
    //             'El control numeroIdentificacion no existe en el formulario.'
    //         );
    //         return;
    //     }

    //     const numeroIdent = numeroIdentControl.valueChanges
    //         .pipe(
    //             filter((value) => !!value),
    //             distinctUntilChanged()
    //         )
    //         .subscribe((value) => {
    //             console.log('Valor cambiado:', value);

    //             let limiteInf = 0;
    //             let limiteSup = 0;
    //             const tipoDoc = this.documentoSeleccionado?.nemonico;

    //             if (tipoDoc === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
    //                 limiteInf = limiteSup = 8;
    //             } else if (
    //                 tipoDoc ===
    //                 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA' ||
    //                 tipoDoc ===
    //                 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA'
    //             ) {
    //                 // ❌ NO LLAMAMOS A RENIEC AQUÍ
    //                 limiteInf = 1; // o lo que desees
    //                 limiteSup = 20; // longitud indefinida si quieres
    //             } else if (
    //                 tipoDoc === 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE'
    //             ) {
    //                 limiteInf = 6;
    //                 limiteSup = 10;
    //             } else if (
    //                 tipoDoc === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO'
    //             ) {
    //                 limiteInf = 1;
    //                 limiteSup = 10;
    //             } else {
    //                 limiteInf = limiteSup = 8;
    //             }

    //             if (
    //                 value.length >= limiteInf &&
    //                 value.length <= limiteSup 
    //             ) {
    //                 console.log('obteniendo ficha');
    //                 this.obtenerFichaIdentificacionNumeroDocumento(value);
    //             }
    //         });

    //     this.subscriptionIdentificacion.add(numeroIdent);
    // }

    agregarSubs() {
        const numeroIdentControl = this.fichaIdentificacionForm.get('numeroIdentificacion');

        if (!numeroIdentControl) {
            console.error('El control numeroIdentificacion no existe en el formulario.');
            return;
        }

        const sub = numeroIdentControl.valueChanges.pipe(
            filter(value => !!value),
            debounceTime(1000),
            distinctUntilChanged(),
            filter(value => {
                const tipoDoc = this.documentoSeleccionado?.nemonico;
                let limiteInf = 0;
                let limiteSup = 0;

                switch (tipoDoc) {
                    case 'TIPO_DOCUMENTO_IDENTIFICACION_DNI':
                        limiteInf = limiteSup = 8;
                        break;

                    case 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA':
                    case 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA':
                        limiteInf = 1;
                        limiteSup = 20;
                        break;

                    case 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE':
                        limiteInf = 6;
                        limiteSup = 10;
                        break;

                    case 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO':
                        limiteInf = 1;
                        limiteSup = 10;
                        break;

                    default:
                        limiteInf = limiteSup = 8;
                }

                return value.length >= limiteInf && value.length <= limiteSup;
            })
        ).subscribe(value => {
            console.log('obteniendo ficha');
            this.obtenerFichaIdentificacionNumeroDocumento(value);
        });

        this.subscriptionIdentificacion.add(sub);
    }


    obtenerFichaIdentificacionNumeroDocumento(numeroIdentificacion: string) {
        this.fichaIdentificacionService
            .obtenerFichaIdentificacionPorNumeroDocumento(numeroIdentificacion)
            .subscribe({
                next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                    // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
                    if (!resp.exito) {
                        this.datosNoEncontrados();
                        if (
                            // this.documentoSeleccionado.nemonico ===
                            // 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA' ||
                            this.documentoSeleccionado.nemonico ===
                            'TIPO_DOCUMENTO_IDENTIFICACION_DNI'
                        ) {
                            this.obtenerDataNumeroDocumento(
                                numeroIdentificacion
                            );
                        }
                        return;
                    }

                    if (!this.esEdicion) {
                        if (resp.data != null) {
                            let ref =
                                this.dialogMensajeService.mensajeConConfirmacion(
                                    // 'El adolescente con número de identificación: "' +
                                    // numeroIdentificacion +
                                    // '"ya existe.',
                                    // 'Deseas cargar su información?'
                                    `El adolescente "${numeroIdentificacion}" ya se encuentra registrado en el Sistema`,
                                    '¿desea recuperarlo?'
                                );

                            ref.afterClosed().subscribe({
                                next: (rp: 'confirmed' | 'cancelled') => {
                                    if (rp == 'confirmed') {
                                        let load =
                                            this.dialogMensajeService.mensajeLoading(
                                                'Cargando datos!'
                                            );
                                        this.empezarEdicion(resp.data);
                                        this.editarFichaIdentificacion(resp.data);
                                        load.close();
                                    } else {
                                        this.datosNoEncontrados();            
                                        this.fichaIdentificacionForm
                                            .get('numeroIdentificacion')
                                            ?.setValue(null);
                                        this.fichaIdentificacionForm
                                            .get('tipoIdentificacion')
                                            ?.setValue(null);
                                    }
                                },
                            });
                        }                         
                    }

                    // this.empezarEdicion(resp.data);
                    // console.log('datos encontrados', resp.data);
                },
                error: (error: any) => {
                    this.fichaIdentificacionService.checkError(error);
                },
            });
    }

    obtenerDataNumeroDocumento(numeroIdentificacion: string) {
        // this.utilsService
        //   .data(numeroIdentificacion)
        //   .subscribe({
        //     next: (resp: any) => {
        //       // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
        //       this.dataPeru = resp;
        //       let load = this.dialogMensajeService.mensajeLoading(
        //         'Obteniendo la ficha de identificación..'
        //       );
        //       console.log('datos encontrados', resp);
        //       this.datosEncontradosReniec(resp.row);
        //       load.close();
        //     },
        //     error: (error: any) => {
        //       console.error('Error al obtener datos de Reniec:', error);
        //       this.dialogMensajeService.mensajeError('No se pudo obtener la información del DNI ingresado, por favor, ingrese manualmente los datos.');
        //       this.datosNoEncontrados();
        //     }
        //   });

        this.utilsService.data(numeroIdentificacion).subscribe({
            next: (resp: any) => {
                if (!resp || !resp.row || resp.row.length === 0) {
                    this.dialogMensajeService.mensajeError(
                        'No se pudo obtener la información del DNI ingresado, por favor ingrese los datos manualmente.'
                    );
                    this.datosNoEncontrados();
                    return;
                }

                this.dataPeru = resp;
                let load = this.dialogMensajeService.mensajeLoading(
                    'Obteniendo la ficha de identificación..'
                );
                this.datosEncontradosReniec(resp.row);
                load.close();
            },
            error: (error: any) => {
                console.error('Error al obtener datos de Reniec:', error);
                this.dialogMensajeService.mensajeError(
                    'No se pudo obtener la información del DNI ingresado, por favor, ingrese manualmente los datos.'
                );
                this.datosNoEncontrados();
            },
        });
    }

    datosEncontradosReniec(fichaIdentificacionEditar: any) {
        this.fichaIdentificacionForm
            .get('apellidoPaterno')
            .setValue(fichaIdentificacionEditar.apellido_paterno);
        this.fichaIdentificacionForm
            .get('apellidoMaterno')
            ?.setValue(fichaIdentificacionEditar.apellido_materno);
        this.fichaIdentificacionForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);
        this.fichaIdentificacionForm.get('tipoSexo');
        //     .setValue(fichaIdentificacionEditar.tipoSexo);
        // this.sexoEscogido = this.listaTipoSexo.find(
        //     (x) => x.tokenIdentificador == fichaIdentificacionEditar.tipoSexo
        // );
        // if (this.sexoEscogido.nemonico !== 'TIPO_SEXO_FEMENINO') {
        //   this.fichaIdentificacionForm.get('ingresahijos').setValue(false);
        //   this.fichaIdentificacionForm.get('ingresahijos').disable();
        // } else {
        //   this.fichaIdentificacionForm.get('ingresahijos').enable();
        // }
        // const fechaNacimiento = moment(fichaIdentificacionEditar.fecha_nacimiento, "YYYY-MM-DD").toDate();
        // const fechaNacimiento = new Date(Date.parse(fichaIdentificacionEditar.fecha_nacimiento));
        const fechaString = fichaIdentificacionEditar.fecha_nacimiento;
        const partes = fechaString.split('-');
        const fechaNacimiento = new Date(
            Number(partes[0]), // Año
            Number(partes[1]) - 1, // Mes
            Number(partes[2]) // Día
        );
        console.log('fechaNacimiento', fechaNacimiento);
        this.fichaIdentificacionForm
            .get('fechaNacimiento')
            .setValue(fechaNacimiento);
        const edad = this.funcionesUtils.getEdad(fechaString);
        this.fichaIdentificacionForm
            .get('edadFicha')
            .setValue(edad);
        this.fichaIdentificacionForm.get('edadFicha').markAsTouched();

        if (edad < 14) {
            const numDoc = this.fichaIdentificacionForm.get('numeroIdentificacion')?.value;
            this.obtenerDataMenores(numDoc);
        }
    }

    obtenerDataMenores(numeroIdentificacion: string) {
        // TODO: implementar servicio para completar datos de menores de 14 años
        console.log('obtenerDataMenores - número de documento:', numeroIdentificacion);
    }

    editarFichaIdentificacion(fichaIdentificacionDTO: FichaIdentificacionDTO) {
        const ruta = fichaIdentificacionDTO.tokenIdentificador;

        this.router
            .navigateByUrl('/', { skipLocationChange: false })
            .then(() => {
                this.router.navigate([ruta], { relativeTo: this.route });
            });
    }

    abrirModalBusquedaAdolescente() {
        this.dialog.open(BusquedaAdolescenteDialogComponent, {
            width: '80vw',
            disableClose: true,
        });
    }
}

export function getEspPaginatorIntl() {
    const paginatorIntl = new MatPaginatorIntl();

    paginatorIntl.itemsPerPageLabel = 'Elementos por página:';
    paginatorIntl.firstPageLabel = 'Ir al inicio';
    paginatorIntl.nextPageLabel = 'Siguiente';
    paginatorIntl.previousPageLabel = 'Anterior';
    paginatorIntl.lastPageLabel = 'Ir al final';
    // paginatorIntl.getRangeLabel = EspRangeLabel;

    paginatorIntl.getRangeLabel = (
        page: number,
        pageSize: number,
        length: number
    ) => {
        if (length === 0 || pageSize === 0) {
            return `0 / ${length}`;
        }
        length = Math.max(length, 0);
        const startIndex = page * pageSize;
        const endIndex =
            startIndex < length
                ? Math.min(startIndex + pageSize, length)
                : startIndex + pageSize;
        return `${startIndex + 1} - ${endIndex} de ${length}`;
    };

    return paginatorIntl;
}
