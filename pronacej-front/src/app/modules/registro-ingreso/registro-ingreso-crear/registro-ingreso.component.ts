import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {
    DateAdapter,
    MAT_DATE_FORMATS,
    MAT_DATE_LOCALE,
    MatNativeDateModule,
} from '@angular/material/core';
import {
    MatDatepickerInputEvent,
    MatDatepickerModule,
} from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { HistoricoEntradaSalidaDTO } from 'app/core/model/both/HistoricoEntradaSalidaDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { HistoricoEntradaSalidaRequest } from 'app/core/model/request/HistoricoEntradaSalidaRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import {
    CUSTOM_DATE_FORMATS,
    CustomDateAdapter,
    FuncionesUtils,
} from 'app/core/utils/funcionesUtils.model';
import { HistoricoEntradaSalidaService } from 'app/modules/administracion/services/historicoEntradaSalida.service';
import { environment } from 'environments/environment';
import {
    Observable,
    Subject,
    Subscription,
    catchError,
    debounceTime,
    distinctUntilChanged,
    forkJoin,
    map,
    startWith,
    takeUntil,
    tap,
    throwError,
} from 'rxjs';
import { FichaIdentificacionService } from '../../administracion/services/fichaIdentificacion.service';
import { JerarquiaService } from '../../seguridad/services/jerarquia.service';
import { TrasladoAdolescenteDTO, TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { PermisoSalidaDTO } from 'app/core/model/both/salida/PermisoSalidaDTO.model';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { UtilsService } from 'app/core/services/utils.service';
import moment from 'moment';
import { InformeFinalAbiertoDTO } from 'app/core/model/both/informeFinalAbiertoDTO.model';
import { ActaExternamientoDTO } from 'app/core/model/both/ia/actaExternamientoDTO.model';
import { ValidarIngresoFichaRequest } from 'app/core/model/request/ValidarIngresoFichaRequest.model';

@Component({
    selector: 'app-registro-ingreso',
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
        MatNativeDateModule,
        MatSlideToggleModule,
        MatAutocompleteModule,
    ],
    templateUrl: './registro-ingreso.component.html',
    styleUrl: './registro-ingreso.component.scss',
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    ],
})
export class RegistroIngresoComponent implements OnInit {
    fichaIngresoForm: FormGroup;
    listaTipoDocumento: CatalogoDTO[] = [];
    listaTipoSexo: CatalogoDTO[] = [];
    listaTipoEntrada: CatalogoDTO[] = [];
    listaTipoEntradaSeleccion: CatalogoDTO[] = [];
    tituloPantalla: string = 'Registro de ingreso al centro: ';
    fichaIdentificacionDTO: FichaIdentificacionDTO;
    nombreCentro: string = '';

    sexoEscogido: CatalogoDTO = new CatalogoDTO();
    centro: JerarquiaDTO;
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_NUEVO_INGRESO;
    private subscriptions = new Subscription();
    cargando: boolean = false;

    private numeroIdentificacionSub: Subscription;  // Nueva variable para manejar la suscripción

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
            minLength: 4,
            maxLength: 20,
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

    listaTiposDocumentoIngreso: CatalogoDTO[] = [];
    cortesJusticiaFiltrado: Observable<CatalogoDTO[]>;
    instancias: CatalogoDTO[];
    especialidades: CatalogoDTO[];
    cortesSuperioresJusticia: CatalogoDTO[];
    cortesFiltradas: CatalogoDTO[];

    tokenIdentificadorFichaIdentificacion: string;
    documentoSeleccionado: CatalogoDTO;
    maxDate: Date = new Date();

    fichaEncontrada: FichaIdentificacionDTO;
    historicoEncontrado: HistoricoEntradaSalidaDTO;

    validaIngreso = false;
    tipoEntrada: CatalogoDTO;

    dataPeru: any;

    private unsubscribe$: Subject<void> = new Subject<void>();

    constructor(
        public funcionesUtils: FuncionesUtils,
        private formBuilder: FormBuilder,
        private router: Router,
        private dialogMensajeService: DialogMensajeService,
        private fichaIdentificacionService: FichaIdentificacionService,
        private jerarquiaService: JerarquiaService,
        private catalogoService: CatalogoService,
        private route: ActivatedRoute,
        private dateAdapter: DateAdapter<any>,
        private historicoEntradaSalidaService: HistoricoEntradaSalidaService,
        private utilsService: UtilsService,
    ) {
        this.construirForm();
        this.cargarCatalogos().then(() => {
            console.log('Catálogos cargados completamente');
        });
        this.dateAdapter.setLocale('es');
    }

    async ngOnInit(): Promise<void> {
        this.cargarCentro();

        this.tokenIdentificadorFichaIdentificacion =
            this.route.snapshot.paramMap.get('uuid_fp');

        if (this.tokenIdentificadorFichaIdentificacion) {
            this.obtenerFichaIdentificacionPorToken(
                this.tokenIdentificadorFichaIdentificacion
            );
        } else {
            this.fichaIngresoForm.get('numeroIdentificacion')?.enable();
            this.setupNumeroIdentificacionListener();
        }
    }

    async cargarCatalogos(): Promise<void> {
        const catalogos$ = forkJoin({
            tipoDocumento: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_DOCUMENTO_IDENTIFICACION',
                etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            ),
            tipoTipoSexo: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_SEXO',
                etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            ),
            tipoDocumentosIngreso: this.funcionesUtils.obtenerListaCatalogo(
                'DOCUMENTO_INGRESO',
                etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            ),
            tipoEntrada: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_ENTRADA',
                etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            ),
        });

        catalogos$.subscribe({
            next: (result) => {
                this.listaTipoDocumento = result.tipoDocumento;
                this.listaTipoSexo = result.tipoTipoSexo;
                this.listaTiposDocumentoIngreso = result.tipoDocumentosIngreso;
                this.listaTipoEntrada = result.tipoEntrada;
                this.listaTipoEntradaSeleccion = result.tipoEntrada;
            },
            error: (err) => {
                console.error('Error al cargar los catálogos:', err);
            },
        });
    }

    obtenerCatalogos(): Observable<any> {
        console.log('valores encontrado');
        const nemonicosCatalogos = [
            'INSTANCIA_EXPEDIENTE',
            'ESPECIALIDAD_EXPEDIENTE',
            'CORTE_SUPERIOR_JUSTICIA',
        ];

        const solicitudes = nemonicosCatalogos.map((solicitud) =>
            this.catalogoService.obtenerHijos(solicitud, '')
        );

        return forkJoin(solicitudes).pipe(
            tap((results: any[]) => {
                this.instancias = results[0]?.data;
                this.especialidades = results[1]?.data;
                this.cortesSuperioresJusticia = results[2]?.data;
                console.log('valores encontrado', results);
                this.cortesJusticiaFiltrado = this.fichaIngresoForm.controls[
                    'corteJusticia'
                ].valueChanges.pipe(
                    startWith(''),
                    map((value) =>
                        typeof value === 'string'
                            ? this._filter(value)
                            : this.cortesSuperioresJusticia
                    )
                );
                this.cortesJusticiaFiltrado.subscribe({
                    next: (datos: CatalogoDTO[]) => {
                        this.cortesFiltradas = datos;
                    },
                });
            }),
            catchError((err) => {
                this.catalogoService.checkError(err);
                return throwError(() => err);
            })
        );
    }

    private _filter(value: string): CatalogoDTO[] {
        const filterValue = value.toLowerCase();

        return this.cortesSuperioresJusticia.filter((option) =>
            option.nombre.toLowerCase().includes(filterValue)
        );
    }
    F;

    construirForm() {
        this.fichaIngresoForm = this.formBuilder.group({
            apellidoPaterno: [
                '',
                [
                    Validators.required,
                    Validators.pattern(
                        '^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 áÁéÉíÍóÓúÚñÑ]+$'
                    ),
                ],
            ],
            apellidoMaterno: [
                '',
                [
                    Validators.required,
                    Validators.pattern(
                        '^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 áÁéÉíÍóÓúÚñÑ]+$'
                    ),
                ],
            ],
            nombres: [
                '',
                [
                    Validators.required,
                    Validators.pattern(
                        '^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 áÁéÉíÍóÓúÚñÑ]+$'
                    ),
                ],
            ],
            tipoIdentificacion: [null, [Validators.required]],
            tipoSexo: [null, [Validators.required]],
            numeroIdentificacion: [null, [Validators.required]],
            fechaIngreso: [
                new Date().toISOString().substring(0, 10),
                [Validators.required],
            ],
            horaIngreso: [
                new Date().toTimeString().substring(0, 5),
                [Validators.required],
            ],
            juezIngreso: [
                null,
                [
                    Validators.required,
                    Validators.pattern(
                        '^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 áÁéÉíÍóÓúÚñÑ]+$'
                    ),
                ],
            ],
            juzgadoIngreso: [
                null,
                [
                    Validators.required,
                    Validators.pattern(
                        '^(?=.*[a-zA-Z0-9])[a-zA-Z0-9 áÁéÉíÍóÓúÚñÑ]+$'
                    ),
                ],
            ],
            centro: [null, []],
            numeroFojas: [null, []],
            ingresahijos: [false, [Validators.required]],
            observacionesIngreso: [null, [Validators.required]],
            documentosSeleccionados: [''],
            fechaNacimiento: [null, [Validators.required]],
            edad: [null, [Validators.max(50)]],
            tipoEntrada: [null, [Validators.required]],
        });
    }

    verificarTipoDocumento(event: any) {
        let token = event.value;
        const tipoDocu = this.listaTipoDocumento.find(
            (x) => x.tokenIdentificador == token
        );
        this.documentoSeleccionado = tipoDocu;
        console.log('Documento seleccionado:', this.documentoSeleccionado);

        if (this.tipoEntrada) {
            const numeroIdentificacionControl = this.fichaIngresoForm.get('numeroIdentificacion');

            if (tipoDocu.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO' &&
                this.tipoEntrada?.nemonico === 'ENTRADA_INGRESO_NUEVO') {

                numeroIdentificacionControl.disable();
                numeroIdentificacionControl.setValue(null);
                this.validaIngreso = true;

            } else {
                numeroIdentificacionControl.enable();
                numeroIdentificacionControl.setValidators([Validators.required]);
                numeroIdentificacionControl.updateValueAndValidity();

                this.actualizarValidaciones(tipoDocu.nemonico);

                const value = numeroIdentificacionControl.value;
                if (this.numeroIdentificacionEsValido(value)) {
                    console.log('Ejecutando búsqueda por cambio en tipoIdentificacion.');
                    this.obtenerFichaIdentificacionNumeroDocumento(value);
                }
            }
        }
    }




    seleccionSexo(event: any) {
        let token = event.value;
        this.sexoEscogido = this.listaTipoSexo.find(
            (x) => x.tokenIdentificador == token
        );
        if (this.sexoEscogido.nemonico !== 'TIPO_SEXO_FEMENINO') {
            this.fichaIngresoForm.get('ingresahijos').setValue(false);
            this.fichaIngresoForm.get('ingresahijos').disable();
        } else {
            this.fichaIngresoForm.get('ingresahijos').enable();
        }
    }

    // actualizarValidaciones(tipoIdentificacion: string): void {
    //     console.log('tipo identificacion', tipoIdentificacion);
    //     const numeroDocumentoControl = this.fichaIngresoForm.get(
    //         'numeroIdentificacion'
    //     );
    //     const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
    //         (doc) => doc.nemonico === tipoIdentificacion
    //     );
    //     console.log('tipoDocumentoSeleccionado', tipoDocumentoSeleccionado);

    //     if (tipoDocumentoSeleccionado) {
    //         const { minLength, maxLength, regex } = tipoDocumentoSeleccionado;

    //         // Aplica validaciones dinámicas
    //         this.fichaIngresoForm.get('numeroIdentificacion').enable();
    //         numeroDocumentoControl?.setValidators([
    //             Validators.required,
    //             Validators.minLength(minLength),
    //             Validators.maxLength(maxLength),
    //             Validators.pattern(regex),
    //         ]);
    //         this.fichaIngresoForm.get('numeroIdentificacion').updateValueAndValidity();
    //         // this.agregarSubs();
    //         const num = numeroDocumentoControl.value;
    //         if (num && num.length > 0) {
    //             numeroDocumentoControl.markAsTouched();
    //         }
    //     }

    //     // Refresca el estado del control
    //     numeroDocumentoControl?.updateValueAndValidity();
    // }


    actualizarValidaciones(tipoIdentificacion: string): void {
        const numeroDocumentoControl = this.fichaIngresoForm.get('numeroIdentificacion');
        const tipoDocumentoSeleccionado = this.listaDocumentoIde.find(
            (doc) => doc.nemonico === tipoIdentificacion
        );

        if (!numeroDocumentoControl || !tipoDocumentoSeleccionado) return;

        let validators = [Validators.required];


        if (
            tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA' ||
            tipoIdentificacion === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA'
        ) {
            validators.push(Validators.minLength(4));
        } else {
            const { minLength: min, maxLength: max, regex } = tipoDocumentoSeleccionado;
            validators.push(Validators.minLength(min));
            validators.push(Validators.maxLength(max));
            validators.push(Validators.pattern(regex));
        }

        numeroDocumentoControl.setValidators(validators);
        numeroDocumentoControl.updateValueAndValidity();
        numeroDocumentoControl.markAsTouched();
    }

    updateAllControls() {
        Object.keys(this.fichaIngresoForm.controls).forEach((controlName) => {
            this.fichaIngresoForm.get(controlName)?.updateValueAndValidity();
        });
    }

    cancelarEdicion() {
        this.fichaIngresoForm.reset();
        this.fichaIdentificacionDTO = null;

        this.router.navigate(['/registro-ingreso']);
    }

    ejecutarAccion() {
        console.log('🚀 Ejecutando acción...');
        if (!this.fichaIngresoForm.valid) {
            this.obtenerControlesConErrores();
            return;
        }

        // console.log('validacion', this.validaIngreso)
        // return;
        this.cargando = true;

        this.fichaIngresoForm.disable();

        let fichaIdentificacion = new FichaIdentificacionDTO();
        let fechaIngreso = this.obtenerValor('fechaIngreso');
        let horaIngreso = this.obtenerValor('horaIngreso');
        let fechaCompletaString = `${fechaIngreso}T${horaIngreso}:00`;
        let fechaIngresoCompleta = new Date(fechaCompletaString);
        fichaIdentificacion.fechaIngreso = fechaIngresoCompleta;

        fichaIdentificacion.apellidoPaterno = this.fichaIngresoForm
            .get('apellidoPaterno')
            .value.trim();
        fichaIdentificacion.apellidoMaterno = this.fichaIngresoForm
            .get('apellidoMaterno')
            .value.trim();
        fichaIdentificacion.nombres = this.fichaIngresoForm
            .get('nombres')
            .value.trim();
        fichaIdentificacion.tipoDocumento =
            this.fichaIngresoForm.get('tipoIdentificacion').value;

        fichaIdentificacion.tipoSexo =
            this.fichaIngresoForm.get('tipoSexo').value;

        // fichaIdentificacion.fechaIngreso =
        //     this.fichaIngresoForm.get('fechaIngreso')?.value;
        fichaIdentificacion.horaIngreso =
            this.fichaIngresoForm.get('horaIngreso')?.value;
        fichaIdentificacion.juez = this.fichaIngresoForm
            .get('juezIngreso')
            ?.value?.trim();
        fichaIdentificacion.juzgado = this.fichaIngresoForm
            .get('juzgadoIngreso')
            ?.value?.trim();
        // fichaIdentificacion.centroIngreso = this.fichaIngresoForm.get('centroIngreso')?.value;
        fichaIdentificacion.numeroDocumento = this.fichaIngresoForm.get(
            'numeroIdentificacion'
        )?.value;
        fichaIdentificacion.ingresahijos =
            this.fichaIngresoForm.get('ingresahijos')?.value;
        fichaIdentificacion.observacionIngreso = this.fichaIngresoForm.get(
            'observacionesIngreso'
        )?.value;

        fichaIdentificacion.esEdicion = false;
        fichaIdentificacion.tokensDocumentosIngreso = [this.fichaIngresoForm.get(
            'documentosSeleccionados'
        ).value];
        fichaIdentificacion.crearFichaIngreso = true;
        fichaIdentificacion.numeroFojas =
            this.fichaIngresoForm.get('numeroFojas').value;
        let centro = new JerarquiaDTO();
        centro.id = this.obtenerValor('centro');
        fichaIdentificacion.centro = this.centro;
        fichaIdentificacion.fechaNacimiento =
            this.fichaIngresoForm.get('fechaNacimiento').value;
        fichaIdentificacion.tipoEntrada =
            this.tipoEntrada;
        // fichaIdentificacion.corteJusticia =
        //     this.fichaIngresoForm.get('corteJusticia').value;
        // fichaIdentificacion.instancia =
        //     this.fichaIngresoForm.get('instancia').value;
        // fichaIdentificacion.especialidad =
        //     this.fichaIngresoForm.get('especialidad').value;
        // fichaIdentificacion.organoJurisdiccional = this.fichaIngresoForm.get(
        //     'organoJurisdiccional'
        // ).value;
        // fichaIdentificacion.secretario =
        //     this.fichaIngresoForm.get('secretario').value;
        console.log('fichaIdentificacion', fichaIdentificacion);
        if (this.tipoEntrada.nemonico === 'ENTRADA_TRASLADO') {
            const trasladoAdolescente = new TrasladoAdolescenteDTO();
            trasladoAdolescente.idTrasladoAdolescente = this.historicoEncontrado.trasladoAdolescente.idTrasladoAdolescente;
            console.log('trasladoAdolescente', trasladoAdolescente)
            fichaIdentificacion.trasladoAdolescente = trasladoAdolescente;
        } else if (this.tipoEntrada.nemonico === 'ENTRADA_SALIDA_TEMPORAL') {
            const salidaTemporal = new PermisoSalidaDTO();
            salidaTemporal.tokenIdentificador = this.historicoEncontrado.permisoSalida.tokenIdentificador;
            fichaIdentificacion.permisoSalida = salidaTemporal;
        } else if (this.tipoEntrada.nemonico === 'ENTRADA_FUGA') {
            const fuga = new GestionFugaDTO();
            fuga.tokenIdentificador = this.historicoEncontrado.fuga.tokenIdentificador;
            fichaIdentificacion.fuga = fuga;
        } else if (this.tipoEntrada.nemonico === 'ENTRADA_INGRESO_NUEVO') {
            if (this.historicoEncontrado?.informeFinalAbierto) {
                const informeFinal = new InformeFinalAbiertoDTO();
                informeFinal.tokenIdentificador = this.historicoEncontrado.informeFinalAbierto.tokenIdentificador;
                fichaIdentificacion.informeFinalAbierto = informeFinal;
            } else if (this.historicoEncontrado?.actaExternamiento) {
                const actaExternamiento = new ActaExternamientoDTO();
                actaExternamiento.tokenIdentificador = this.historicoEncontrado.actaExternamiento.tokenIdentificador;
                fichaIdentificacion.actaExternamiento = actaExternamiento;
            }
        }
        this.fichaIdentificacionService
            .crearFichaIdentificacion(
                fichaIdentificacion,
                etiquetasModel.NEMONICO_MENU_NUEVO_INGRESO
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<FichaIdentificacionDTO>
                ) => {
                    this.fichaIngresoForm.enable();

                    if (!response.exito) {
                        this.fichaIdentificacionService.checkError(response);
                        this.cargando = false;
                        return;
                    }
                    this.dialogMensajeService.mensajeExitoso(
                        response.titulo,
                        response.mensaje
                    );
                    this.cargando = false;
                    this.router.navigate(['/registro-ingreso']);
                },
                error: (error: any) => {
                    this.cargando = false;
                    this.fichaIdentificacionService.checkError(error);
                    this.fichaIngresoForm.enable();
                },
                complete: () => {
                    this.cargando = false;
                },
            });
    }

    private obtenerValor(key: string) {
        return this.fichaIngresoForm.get(key)?.value;
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
                    this.fichaIngresoForm
                        .get('centro')
                        ?.setValue(this.centro.nombre);
                    this.nombreCentro = this.centro.nombre;
                },
                error: (error: any) => {
                    this.jerarquiaService.checkError(error);
                },
            });
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

    permitirSoloLetrasYEspacios(event: KeyboardEvent): void {
        const regex = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]$/; // Permitir letras, tildes, ñ y espacios
        const key = event.key;

        // Bloquear si no es una tecla válida (letra, espacio, retroceso o tabulación)
        if (!regex.test(key) && key !== 'Backspace' && key !== 'Tab') {
            event.preventDefault();
        }
    }

    displayFn(option: CatalogoDTO): string {
        return option && option.nombre ? option.nombre : '';
    }

    obtenerFichaIdentificacionPorToken(tokenIdentificador: string) {
        let load = this.dialogMensajeService.mensajeLoading(
            'Obteniendo la ficha de identificación..'
        );
        this.fichaIdentificacionService
            .obtenerFichaIdentificacionPorTokenIdentificador(tokenIdentificador, this.nemonicoMenu)
            .subscribe({
                next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                    load.close();
                    // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
                    if (!resp.exito) {
                        return;
                    }
                    //   this.esEdicion = true;
                    this.fichaIdentificacionDTO = resp.data;
                    this.empezarEdicion(resp.data);
                },
                error: (error: any) => {
                    load.close();

                    this.fichaIdentificacionService.checkError(error);
                },
            });
    }

    empezarEdicion(fichaIdentificacionEditar: FichaIdentificacionDTO) {
        console.log('ficha a editar', fichaIdentificacionEditar);
        this.fichaIngresoForm
            .get('apellidoPaterno')
            .setValue(fichaIdentificacionEditar.apellidoPaterno);
        this.fichaIngresoForm
            .get('apellidoMaterno')
            ?.setValue(fichaIdentificacionEditar.apellidoMaterno);
        this.fichaIngresoForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);
        this.fichaIngresoForm
            .get('tipoSexo')
            .setValue(fichaIdentificacionEditar.tipoSexo);
        this.fichaIngresoForm
            .get('ingresahijos')
            ?.setValue(fichaIdentificacionEditar.ingresahijos);
        this.fichaIngresoForm
            .get('tipoIdentificacion')
            .setValue(fichaIdentificacionEditar.tipoDocumento);
        this.fichaIngresoForm
            .get('numeroIdentificacion')
            .setValue(fichaIdentificacionEditar.numeroDocumento);
        this.fichaIngresoForm
            .get('tipoEntrada').setValue(fichaIdentificacionEditar.tipoEntrada);

        this.fichaIngresoForm
            .get('juezIngreso')
            ?.setValue(fichaIdentificacionEditar.juez);
        if (fichaIdentificacionEditar.fechaNacimiento) {
            let fechaNacimiento = new Date(
                fichaIdentificacionEditar.fechaNacimiento
            );
            if (fechaNacimiento) {
                this.fichaIngresoForm
                    .get('edad')
                    .setValue(
                        this.funcionesUtils.getEdad(
                            fichaIdentificacionEditar.fechaNacimiento
                        )
                    );
                this.fichaIngresoForm.get('edad').markAsTouched();
            }
            this.fichaIngresoForm
                .get('fechaNacimiento')
                ?.setValue(fechaNacimiento);
        }
        // this.fichaIngresoForm.get("instancia")?.setValue(fichaIdentificacionEditar.instancia);
        // this.fichaIngresoForm.get("especialidad")?.setValue(fichaIdentificacionEditar.especialidad);
        // this.fichaIngresoForm.get("organoJurisdiccional")?.setValue(fichaIdentificacionEditar.organoJurisdiccional);
        this.fichaIngresoForm
            .get('observacionesIngreso')
            ?.setValue(fichaIdentificacionEditar.observacionIngreso);

        this.fichaIngresoForm
            .get('numeroFojas')
            ?.setValue(fichaIdentificacionEditar.numeroFojas);
        // this.fichaIngresoForm.get("corteJusticia")?.setValue(fichaIdentificacionEditar.corteJusticia);
        // this.fichaIngresoForm.get("secretario")?.setValue(fichaIdentificacionEditar.secretario);
        this.fichaIngresoForm.disable();
    }

    datosEncontrados(fichaIdentificacionEditar: FichaIdentificacionDTO) {
        this.fichaIngresoForm
            .get('apellidoPaterno')
            .setValue(fichaIdentificacionEditar.apellidoPaterno);
        this.fichaIngresoForm
            .get('apellidoMaterno')
            ?.setValue(fichaIdentificacionEditar.apellidoMaterno);
        this.fichaIngresoForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);
        this.fichaIngresoForm
            .get('tipoSexo')
            .setValue(fichaIdentificacionEditar.tipoSexo);
        this.sexoEscogido = this.listaTipoSexo.find(
            (x) => x.tokenIdentificador == fichaIdentificacionEditar.tipoSexo
        );
        // if (this.sexoEscogido.nemonico !== 'TIPO_SEXO_FEMENINO') {
        //     this.fichaIngresoForm.get('ingresahijos').setValue(false);
        //     this.fichaIngresoForm.get('ingresahijos').disable();
        // } 
        // else {
        //     this.fichaIngresoForm.get('ingresahijos').enable();
        // }
        this.fichaIngresoForm
            .get('ingresahijos')
            ?.setValue(fichaIdentificacionEditar.ingresahijos);
        const fechaNacimiento = new Date(
            fichaIdentificacionEditar.fechaNacimiento
        );
        this.fichaIngresoForm.get('fechaNacimiento').setValue(fechaNacimiento);
        this.fichaIngresoForm
            .get('edad')
            .setValue(
                this.funcionesUtils.getEdad(
                    fichaIdentificacionEditar.fechaNacimiento
                )
            );
        this.fichaIngresoForm.get('edad').markAsTouched();
        // this.listaTipoEntradaSeleccion = [];
        // this.listaTipoEntradaSeleccion = this.listaTipoEntrada;
        this.fichaEncontrada = fichaIdentificacionEditar;
        // this.deshabilitarCampos();
    }

    datosNoEncontrados() {
        this.fichaIngresoForm.get('apellidoPaterno').setValue(null);
        this.fichaIngresoForm.get('apellidoMaterno')?.setValue(null);
        this.fichaIngresoForm.get('nombres')?.setValue(null);
        this.fichaIngresoForm.get('tipoSexo').setValue(null);
        this.fichaIngresoForm.get('fechaNacimiento').setValue(null);
        this.fichaIngresoForm.get('edad').setValue(null);
        this.fichaIngresoForm.get('ingresahijos')?.setValue(false);
        // this.listaTipoEntradaSeleccion = [];
        // this.listaTipoEntradaSeleccion = [
        //     this.listaTipoEntrada.find(
        //         (x) => x.nemonico == 'ENTRADA_INGRESO_NUEVO'
        //     ),
        // ];
        this.fichaEncontrada = null;
        // this.habilitarCampos();
    }

    obtenerFichaIdentificacionNumeroDocumento(numeroIdentificacion: string) {
        console.log('🚀 Entró al método obtenerFichaIdentificacionNumeroDocumento con:', numeroIdentificacion);
        this.fichaIdentificacionService
            .obtenerFichaIdentificacionPorNumeroDocumento(numeroIdentificacion, this.nemonicoMenu)
            .subscribe({
                next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                    //this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
                    console.log('respuesta fichaIdentificacion:', resp);
                    if (!resp.exito) {
                        this.datosNoEncontrados();
                        console.log("holaaaa");

                        const nem = this.documentoSeleccionado?.nemonico;
                        const documentosConConsultaExterna = [
                            'TIPO_DOCUMENTO_IDENTIFICACION_DNI',
                            // 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA',
                            'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA',
                        ];

                        if (documentosConConsultaExterna.includes(nem)) {
                            this.obtenerDataNumeroDocumento(numeroIdentificacion);
                        }

                        this.evaluarConEntrada();
                        return;
                    }
                    // if (!resp.exito) {
                    //     this.datosNoEncontrados();
                    //     if (this.documentoSeleccionado.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
                    //         // Solo DNI intenta consultar en Reniec
                    //         this.obtenerDataNumeroDocumento(numeroIdentificacion);
                    //     }

                    //     // ⚠️ Evitamos consulta para documentos de extranjería
                    //     this.evaluarConEntrada();
                    //     return;
                    // }

                    // TODO: también se debe validar el tipo de entrada
                    const adolescenteEncontrado: FichaIdentificacionDTO = resp.data;
                    const tipoEntrada: CatalogoDTO = this.fichaIngresoForm.get('tipoEntrada')?.value;

                    let validarIngresoFichaRequest: ValidarIngresoFichaRequest = {
                        tokenIdentificadorFicha: adolescenteEncontrado.tokenIdentificador,
                        nemonicoTipoIngreso: tipoEntrada?.nemonico
                    }

                    this.fichaIdentificacionService.validarIngresoNuevo(validarIngresoFichaRequest).subscribe({
                        next: (response: RespuestaPorDefecto<boolean>) => {
                            if (!response.exito) {
                                this.fichaIdentificacionService.checkError(response);
                            }

                            if (response.data != null && response.data === true) {
                                this.registrarAdolescente(numeroIdentificacion, adolescenteEncontrado);
                            } else {
                                this.dialogMensajeService.mensajeAdvertencia(
                                    // 'El adolescente con número de identificación: "' +
                                    // numeroIdentificacion +
                                    // '"ya existe.',
                                    // 'Deseas cargar su información?'
                                    '',
                                    response.mensaje
                                    // `No es posible realizar el registro del adolescente debido a que el CJDR/SOA: ${adolescenteEncontrado?.centroIngreso} no ha generado su registro de salida.`
                                );

                                this.datosNoEncontrados();
                                this.fichaIngresoForm.get('numeroIdentificacion').reset();
                            }

                        },
                        error: (error: any) => {
                            this.fichaIdentificacionService.checkError(error);
                        },
                    });
                                                                   
                    

                    // let load = this.dialogMensajeService.mensajeLoading(
                    //     'Obteniendo la ficha de identificación..'
                    // );
                    // this.datosEncontrados(resp.data);
                    // this.evaluarConEntrada();
                    // load.close();
                },
                error: (error: any) => {
                    this.fichaIdentificacionService.checkError(error);
                },
            });
    }
    
    private registrarAdolescenteIngresoNuevo(adolescenteEncontrado: FichaIdentificacionDTO, numeroIdentificacion: string) {
        const estadosValidos = [
            etiquetasModel.NEMONICO_ESTADO_ADOLESCENTE_LIBRE,
            etiquetasModel.NEMONICO_ESTADO_ADOLESCENTE_FUGADO,
            etiquetasModel.NEMONICO_ESTADO_ADOLESCENTE_SENTENCIADO_PROCESADO,
        ];

        if (adolescenteEncontrado != null
            && adolescenteEncontrado.estadoAdolescente?.nemonico
            && estadosValidos.some(e => adolescenteEncontrado.estadoAdolescente.nemonico.includes(e))) {
            this.registrarAdolescente(numeroIdentificacion, adolescenteEncontrado);
        } else {

            this.dialogMensajeService.mensajeAdvertencia(
                // 'El adolescente con número de identificación: "' +
                // numeroIdentificacion +
                // '"ya existe.',
                // 'Deseas cargar su información?'
                '',
                `No es posible realizar el registro del adolescente debido a que el CJDR/SOA: ${adolescenteEncontrado?.centroIngreso} no ha generado su registro de salida.`
            );

            this.datosNoEncontrados();
            this.fichaIngresoForm.get('numeroIdentificacion').reset();
        }
    }

    private registrarAdolescente(numeroIdentificacion: string, adolescenteEncontrado: FichaIdentificacionDTO) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
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
                    let load = this.dialogMensajeService.mensajeLoading(
                        'Cargando datos!'
                    );
                    this.datosEncontrados(adolescenteEncontrado);
                    this.evaluarConEntrada();
                    load.close();
                } else {
                    this.datosNoEncontrados();
                    this.fichaIngresoForm.get('numeroIdentificacion').reset();
                }
            },
        });
    }

    configurarFormulario() {
        const susc = this.fichaIngresoForm
            .get('numeroIdentificacion')
            ?.valueChanges.subscribe(() => {
                const numeroIdentificacionControl = this.fichaIngresoForm.get(
                    'numeroIdentificacion'
                );

                if (
                    numeroIdentificacionControl?.valid &&
                    numeroIdentificacionControl?.value?.trim() !== ''
                ) {
                    this.fichaIngresoForm.get('tipoEntrada')?.enable();
                    console.log('no hay errores', numeroIdentificacionControl);
                } else {
                    this.fichaIngresoForm.get('tipoEntrada')?.disable();
                }
            });
        this.subscriptions.add(susc);
    }

    agregarSubs() {
        const numeroIdentificacionControl = this.fichaIngresoForm.get('numeroIdentificacion');

        if (!numeroIdentificacionControl) return; // Evitar errores si no existe el control

        // Re-suscribirte cada vez que se habilite el control
        numeroIdentificacionControl.statusChanges.subscribe(status => {
            console.log('estado numeroIdentificacion', status);
            if (status === 'VALID') {
                this.subscribirCambios(); // Ejecutar el subscribe cuando el control esté habilitado
            }
        });

        this.subscribirCambios();
    }

    private subscribirCambios() {
        const numeroIdentificacionControl = this.fichaIngresoForm.get('numeroIdentificacion');

        if (!numeroIdentificacionControl) return;

        // Cancelar suscripciones anteriores para evitar múltiple ejecución
        this.subscriptions.unsubscribe();
        this.subscriptions = new Subscription(); // Resetear suscripciones

        const numeroIdent = numeroIdentificacionControl.valueChanges.subscribe((value) => {
            numeroIdentificacionControl.updateValueAndValidity(); // Asegurar que se validen cambios

            console.log('Número de Identificación Cambiado:', value);
            let limiteInf = 0;
            let limiteSup = 0;
            if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
                limiteInf = limiteSup = 8;
            } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA') {
                limiteInf = limiteSup = 12;
            } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE'
            ) {
                limiteInf = 6;
                limiteSup = 10;
            } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
                limiteInf = 1;
                limiteSup = 10;
            }
            else {
                limiteInf = 8;
                limiteSup = 8;
            }

            if (value && value.length >= limiteInf && value.length <= limiteSup) {
                console.log('Buscando por número de identificación:', value);
                this.obtenerFichaIdentificacionNumeroDocumento(value);
            }
        });

        this.subscriptions.add(numeroIdent);
    }


    calcularEdad(event: any) {
        this.fichaIngresoForm
            .get('edad')
            .setValue(this.funcionesUtils.calcularEdad(event));
        const edad = this.funcionesUtils.calcularEdad(event);
        if (edad >= 51) {
            this.dialogMensajeService.mensajeAdvertencia(
                'Atención',
                'La edad del ingresado no puede ser mayor a 50 años.'
            );
        }
        this.fichaIngresoForm.get('edad').markAsTouched();
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
            this.fichaIngresoForm.get(controlName).setValue(fecha);
        }
    }

    evaluarTipoEntrada(entradaSeleccionada: any): void {
        console.log('Tipo de Entrada Seleccionado:', entradaSeleccionada);

        this.tipoEntrada = entradaSeleccionada;
        const numeroIdentificacionControl = this.fichaIngresoForm.get('numeroIdentificacion');

        if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO'
            && this.tipoEntrada?.nemonico === 'ENTRADA_INGRESO_NUEVO') {

            numeroIdentificacionControl.disable();
            numeroIdentificacionControl.setValue(null);
            this.validaIngreso = true;
        } else {
            if (this.documentoSeleccionado) {
                numeroIdentificacionControl.enable();
                numeroIdentificacionControl.setValidators([Validators.required]);
                numeroIdentificacionControl.updateValueAndValidity();
                this.actualizarValidaciones(this.documentoSeleccionado?.nemonico);

                const value = numeroIdentificacionControl.value;
                if (this.numeroIdentificacionEsValido(value)) {
                    this.obtenerFichaIdentificacionNumeroDocumento(value);
                }
            }
        }

        if (this.tipoEntrada?.nemonico === 'ENTRADA_FUGA' || this.tipoEntrada?.nemonico === 'ENTRADA_INGRESO_NUEVO') {
            const juzgadoIngresoControl = this.fichaIngresoForm.get('juzgadoIngreso');
            juzgadoIngresoControl.enable();
            juzgadoIngresoControl.setValidators([Validators.required]);
            juzgadoIngresoControl.updateValueAndValidity();
            // if(this.tipoEntrada?.nemonico === 'ENTRADA_INGRESO_NUEVO'){
            //     const fojasControl = this.fichaIngresoForm.get('numeroFojas');
            //     fojasControl.enable();
            //     fojasControl.setValidators([Validators.required]);
            //     fojasControl.updateValueAndValidity();
            // }
            this.habilitarCampos();
        } else {
            this.deshabilitarCampos();
        }
    }





    deshabilitarCampos(): void {
        // this.fichaIngresoForm.get('numeroFojas')?.disable();
        this.fichaIngresoForm.get('documentosSeleccionados')?.disable();


        this.fichaIngresoForm.get('juezIngreso')?.disable();


        // Remover las validaciones de requerido
        // this.fichaIngresoForm.get('numeroFojas')?.clearValidators();
        this.fichaIngresoForm.get('documentosSeleccionados')?.clearValidators();

        this.fichaIngresoForm.get('juezIngreso')?.clearValidators();


        // Actualizar los estados de los controles
        // this.fichaIngresoForm.get('numeroFojas')?.updateValueAndValidity();
        this.fichaIngresoForm.get('documentosSeleccionados')?.updateValueAndValidity();

        this.fichaIngresoForm.get('juezIngreso')?.updateValueAndValidity();

        if (this.tipoEntrada.nemonico !== 'ENTRADA_FUGA' && this.tipoEntrada.nemonico !== 'ENTRADA_INGRESO_NUEVO') {
            this.fichaIngresoForm.get('juzgadoIngreso')?.disable();
            this.fichaIngresoForm.get('juzgadoIngreso')?.clearValidators();
            this.fichaIngresoForm.get('juzgadoIngreso')?.updateValueAndValidity();
        }

    }

    habilitarCampos(): void {
        this.fichaIngresoForm.get('documentosSeleccionados')?.enable();
        this.fichaIngresoForm.get('juzgadoIngreso')?.enable();
        this.fichaIngresoForm.get('juezIngreso')?.enable();


        // Agregar nuevamente la validación de requerido
        this.fichaIngresoForm.get('documentosSeleccionados')?.setValidators([Validators.required]);
        this.fichaIngresoForm.get('juzgadoIngreso')?.setValidators([Validators.required]);
        this.fichaIngresoForm.get('juezIngreso')?.setValidators([Validators.required]);


        // Actualizar los estados de los controles
        this.fichaIngresoForm.get('numeroFojas')?.enable();
        this.fichaIngresoForm.get('numeroFojas')?.clearValidators();
        if (this.tipoEntrada?.nemonico === 'ENTRADA_INGRESO_NUEVO') {
            this.fichaIngresoForm.get('numeroFojas')?.setValidators([Validators.required]);
        }
        this.fichaIngresoForm.get('numeroFojas')?.updateValueAndValidity();

        this.fichaIngresoForm.get('documentosSeleccionados')?.updateValueAndValidity();
        this.fichaIngresoForm.get('juzgadoIngreso')?.updateValueAndValidity();
        this.fichaIngresoForm.get('juezIngreso')?.updateValueAndValidity();

    }

    obtenerControlesConErrores(): void {
        const errores: string[] = [];

        Object.keys(this.fichaIngresoForm.controls).forEach((key) => {
            const control = this.fichaIngresoForm.get(key);
            if (control && control.invalid) {
                errores.push(`${key}: ${JSON.stringify(control.errors)}`);
            }
        });

        if (errores.length > 0) {
            console.log('Controles con errores:', errores.join(', '));
        } else {
            console.log('No hay errores en el formulario.');
        }
    }

    obtenerDataNumeroDocumento(numeroIdentificacion: string) {
        // this.utilsService
        //     .data(numeroIdentificacion)
        //     .subscribe({
        //         next: (resp: any) => {
        //             // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
        //             this.dataPeru = resp;
        //             let load = this.dialogMensajeService.mensajeLoading(
        //                 'Obteniendo la ficha de identificación..'
        //             );
        //             console.log('datos encontrados', resp);
        //             this.datosEncontradosReniec(resp.row);
        //             load.close();
        //         },
        //         error: (error: any) => {
        //             console.error('Error al obtener datos de Reniec:', error);
        //             this.dialogMensajeService.mensajeError('No se pudo obtener la información del DNI ingresado, por favor, ingrese manualmente los datos.');
        //             this.datosNoEncontrados();
        //         }
        //     });

        this.utilsService
            .data(numeroIdentificacion)
            .subscribe({
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
                    this.dialogMensajeService.mensajeError('No se pudo obtener la información del DNI ingresado, por favor, ingrese manualmente los datos.');
                    this.datosNoEncontrados();
                }
            });
    }

    datosEncontradosReniec(fichaIdentificacionEditar: any) {
        this.fichaIngresoForm
            .get('apellidoPaterno')
            .setValue(fichaIdentificacionEditar.apellido_paterno);
        this.fichaIngresoForm
            .get('apellidoMaterno')
            ?.setValue(fichaIdentificacionEditar.apellido_materno);
        this.fichaIngresoForm
            .get('nombres')
            ?.setValue(fichaIdentificacionEditar.nombres);
        this.fichaIngresoForm
            .get('tipoSexo')
        if (this.sexoEscogido.nemonico !== 'TIPO_SEXO_FEMENINO') {
            this.fichaIngresoForm.get('ingresahijos').setValue(false);
            this.fichaIngresoForm.get('ingresahijos').disable();
        } else {
            this.fichaIngresoForm.get('ingresahijos').enable();
        }
        const fechaString = fichaIdentificacionEditar.fecha_nacimiento;
        const partes = fechaString.split('-');
        const fechaNacimiento = new Date(
            Number(partes[0]), // Año
            Number(partes[1]) - 1, // Mes 
            Number(partes[2]) // Día
        );
        console.log('fechaNacimiento', fechaNacimiento);
        this.fichaIngresoForm.get('fechaNacimiento').setValue(fechaNacimiento);
        this.fichaIngresoForm
            .get('edad')
            .setValue(
                this.funcionesUtils.getEdad(
                    fechaString
                )
            );
        this.fichaIngresoForm.get('edad').markAsTouched();

    }

    evaluarConEntrada() {
        console.log('evaluando entrada')
        if (this.fichaEncontrada) {
            const historicoRequest = new HistoricoEntradaSalidaRequest();
            historicoRequest.tokenIdentificador =
                this.fichaEncontrada.tokenIdentificador;
            historicoRequest.tipoEntrada = this.tipoEntrada;
            this.historicoEntradaSalidaService
                .obtenerHistoricoSalidaActivo(historicoRequest)
                .subscribe({
                    next: (
                        resp: RespuestaPorDefecto<HistoricoEntradaSalidaDTO>
                    ) => {
                        // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
                        if (!resp.exito) {
                            this.fichaIdentificacionService.checkError(resp);
                            return;
                        }
                        let load = this.dialogMensajeService.mensajeLoading(
                            'Revisando historico de entradas/salidas'
                        );

                        load.close();
                        console.log('dto de historico', resp.data);
                        if (resp.data.fechaSalida == null) {
                            this.historicoEncontrado = null;
                            if (this.tipoEntrada.nemonico === 'ENTRADA_INGRESO_NUEVO'
                                && this.fichaEncontrada == null
                            ) {
                                this.validaIngreso = true;
                                this.dialogMensajeService.mensajeAdvertencia(
                                    'Atención',
                                    'No existe un registro asociado al tipo de entrada seleccionada.'
                                );
                                return;
                            }
                            this.validaIngreso = false;
                            // this.dialogMensajeService.mensajeAdvertencia(
                            //     'Atención',
                            //     'No existe un registro asociado al tipo de entrada seleccionada.'
                            // );
                            return;
                        } else {
                            this.historicoEncontrado = resp.data;
                            this.validaIngreso = true;
                            if (this.tipoEntrada.nemonico === 'ENTRADA_FUGA') {
                                this.fichaIngresoForm.get('juzgadoIngreso')?.enable();
                                this.fichaIngresoForm.get('juzgadoIngreso')?.setValidators([Validators.required]);
                                this.fichaIngresoForm.get('juzgadoIngreso')?.updateValueAndValidity();
                            }
                        }


                    },
                    error: (error: any) => {
                        this.fichaIdentificacionService.checkError(error);
                    },
                });
        } else {
            this.validaIngreso = true;
            if (this.tipoEntrada.nemonico !== 'ENTRADA_INGRESO_NUEVO') {
                this.validaIngreso = false;
                this.dialogMensajeService.mensajeAdvertencia(
                    'Atención',
                    'No existe el adolescente dentro del sistema, por ende no puede ser reingresado.'
                );
            }
        }
    }

    private procesarNumeroIdentificacion(value: string) {
        console.log('value', value);
        let limiteInf = 0;
        let limiteSup = 0;

        if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
            limiteInf = limiteSup = 8;
        } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA') {
            limiteInf = limiteSup = 12;
        } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE'
        ) {
            limiteInf = 6;
            limiteSup = 10;
        } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
            limiteInf = 1;
            limiteSup = 10;
        }
        else {
            limiteInf = 8;
            limiteSup = 8;
        }

        if (value && value.length >= limiteInf && value.length <= limiteSup) {
            console.log('buscando por value', value);
            this.obtenerFichaIdentificacionNumeroDocumento(value);
        }
    }

    ngOnDestroy() {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private setupNumeroIdentificacionListener() {
        const numeroIdentificacionControl = this.fichaIngresoForm.get('numeroIdentificacion');

        if (!numeroIdentificacionControl) return;

        numeroIdentificacionControl.valueChanges
            .pipe(
                debounceTime(1000),
                distinctUntilChanged(),
                takeUntil(this.unsubscribe$)
            )
            .subscribe((value: string) => {
                console.log('🟡 Se disparó el valueChanges:', value);
                if (this.numeroIdentificacionEsValido(value)) {
                    console.log('🟢 Valor válido, llamando a obtenerFicha...');
                    console.log('Número de Identificación válido, procediendo a buscar:', value);
                    this.obtenerFichaIdentificacionNumeroDocumento(value);
                }
            });
    }

    private numeroIdentificacionEsValido(value: string): boolean {
        if (!value) return false;

        let limiteInf = 0;
        let limiteSup = 0;

        if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
            limiteInf = limiteSup = 8;
        } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DOCUMENTO_EXTRANJERIA'
            || this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_CARNET_EXTRANJERIA'
        ) {
            return value.length >= 4;
        } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_PASAPORTE'
        ) {
            limiteInf = 6;
            limiteSup = 10;
        } else if (this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO') {
            limiteInf = 1;
            limiteSup = 10;
        }
        else {
            limiteInf = 8;
            limiteSup = 8;
        }

        return value.length >= limiteInf && value.length <= limiteSup;
    }

    compararTipos(o1: any, o2: any): boolean {
        return o1 && o2 ? o1.nemonico === o2.nemonico : o1 === o2;
    }
}

