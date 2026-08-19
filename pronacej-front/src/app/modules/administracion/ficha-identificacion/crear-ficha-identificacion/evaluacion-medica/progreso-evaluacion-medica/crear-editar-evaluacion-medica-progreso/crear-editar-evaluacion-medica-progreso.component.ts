import { CommonModule } from '@angular/common';
import {
    AfterViewInit,
    Component,
    EventEmitter,
    Inject,
    Input,
    LOCALE_ID,
    OnInit,
    Output,
    ViewChild,
} from '@angular/core';
import {
    FormArray,
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
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
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
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { EvaluacionMedicaProgresoDTO } from 'app/core/model/both/EJE/EvaluacionMedicaProgresoDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EvaluacionMedicaProgresoDocumentoDTO } from 'app/core/model/request/ia/EvaluacionMedicaProgresoDocumentoDTO.model';
import { EvaluacionMedicaProgresoDocumentosRequest } from 'app/core/model/request/ia/EvaluacionMedicaProgresoDocumentosRequest.model';
import { FichaIngresoDocumentosRequest } from 'app/core/model/request/ia/FichaIngresoDocumentosRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { MatDialogRef } from '@angular/material/dialog';
import { FuseConfirmationDialogComponent } from '@fuse/services/confirmation/dialog/dialog.component';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { environment } from 'environments/environment';
import { combineLatest, forkJoin, finalize, tap } from 'rxjs';
import { EvaluacionMedicaService } from '../../evaluacion-medica.service';

@Component({
    selector: 'app-crear-editar-evaluacion-medica-progreso',
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
        MatSlideToggleModule,
        SubidaDeDocumentosComponent,
        DocumentosSubidosTablaComponent,
    ],
    templateUrl: './crear-editar-evaluacion-medica-progreso.component.html',
    styleUrl: './crear-editar-evaluacion-medica-progreso.component.scss',
})
export class CrearEditarEvaluacionMedicaProgresoComponent
    implements OnInit, AfterViewInit {
    @Output() volver = new EventEmitter<void>();

    evaluacionMedicaProgresoForm: FormGroup;
    listaEvaluacionMedicaProgreso: CatalogoDTO[];
    listaNutricion: CatalogoDTO[];
    listaDesNutricion: CatalogoDTO[];
    tituloPantalla = 'Esquema corporal ectoscópico';

    displayedColumns: string[] = [
        'acciones',
        'signo',
        'clave',
        'ubicacion',
        'lado'
    ];
    displayedOtros: string[] = [
        'acciones',
        'signo',
        'clave',
        'ubicacion',
        'lado',
        'detalle'
    ];
    displayedColumnsPresente: string[] = [
        'acciones',
        'signo',
        'clave',
        'presente',
    ];
    displayedColumnsPresenteOtros: string[] = [
        'acciones',
        'signo',
        'clave',
        'presente',
        'detalle'
    ];
    tipos: CatalogoDTO[] = [];
    //Cicatrices
    dataSourceSignosCicatrices = new MatTableDataSource<any>();
    formCicatrices: FormGroup;
    signosCicatrices: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosCicatrices')
    paginatorSignosCicatrices: MatPaginator;

    //Luanares
    dataSourceSignosLunares = new MatTableDataSource<any>();
    formLunares: FormGroup;
    signosLunares: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosLunares') paginatorSignosLunares: MatPaginator;

    //DISCROMIAS
    dataSourceSignosDiscromias = new MatTableDataSource<any>();
    formDiscromias: FormGroup;
    signosDiscromias: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosDiscromias')
    paginatorSignosDiscromias: MatPaginator;

    //TATUAJES
    dataSourceSignosTatuajes = new MatTableDataSource<any>();
    formTatuajes: FormGroup;
    signosTatuajes: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosTatuajes') paginatorSignosTatuajes: MatPaginator;

    //HERIDA-LESION
    dataSourceSignosHeridas = new MatTableDataSource<any>();
    formHeridas: FormGroup;
    signosHeridas: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosHeridas') paginatorSignosHeridas: MatPaginator;

    //MUTILACION
    dataSourceSignosMutilaciones = new MatTableDataSource<any>();
    formMutilaciones: FormGroup;
    signosMutilaciones: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosMutilaciones')
    paginatorSignosMutilaciones: MatPaginator;

    //NODULOS
    dataSourceSignosNodulos = new MatTableDataSource<any>();
    formNodulos: FormGroup;
    signosNodulos: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosNodulos') paginatorSignosNodulos: MatPaginator;

    //ALTERACION
    dataSourceSignosAlteracion = new MatTableDataSource<any>();
    formAlteracion: FormGroup;
    signosAlteracion: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosNodulos')
    paginatorSignosAlteracion: MatPaginator;

    //RegionPerineal
    dataSourceSignosRegionPerineal = new MatTableDataSource<any>();
    formRegionPerineal: FormGroup;
    signosRegionPerineal: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosRegionPerineal')
    paginatorSignosRegionPerineal: MatPaginator;

    //ColVertebrales
    dataSourceSignosColVertebrales = new MatTableDataSource<any>();
    formColVertebrales: FormGroup;
    signosColVertebrales: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosColVertebrales')
    paginatorSignosColVertebrales: MatPaginator;

    //AparatoGenital
    dataSourceSignosAparatoGenital = new MatTableDataSource<any>();
    formAparatoGenital: FormGroup;
    signosAparatoGenital: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosAparatoGenital')
    paginatorSignosAparatoGenital: MatPaginator;

    //Malformacion
    dataSourceSignosMalformacion = new MatTableDataSource<any>();
    formMalformacion: FormGroup;
    signosMalformacion: CatalogoDTO[] = [];
    @ViewChild('paginatorSignosMalformacion')
    paginatorSignosMalformacion: MatPaginator;

    lados: CatalogoDTO[] = [];
    ubicaciones: CatalogoDTO[] = [];

    @Input() estadoEditar: boolean;
    estadoVisualizar: Boolean = false;

    registro: EvaluacionMedicaProgresoDTO;
    registroEditar: EvaluacionMedicaProgresoDTO;
    tokenFichaMedica: string = '';
    tokenEvaluacionMedicaProgreso: string = '';

    tokensAEliminar: string[] = [];

    mostrarSubidaDocumentos: boolean = false;
    @ViewChild('documentosComp')
    tablaDocumentos: DocumentosSubidosTablaComponent;

    @ViewChild('documentosCompIngreso')
    tablaDocumentosIngreso: DocumentosSubidosTablaComponent;

    tiposDeDocumentosSistema: TipoDeDocumento[] = [];
    uuid_fp: string;

    vistaDoctor: Boolean = false;

    constructor(
        public funcionesUtils: FuncionesUtils,
        private formBuilder: FormBuilder,
        private router: Router,
        private dialogMensajeService: DialogMensajeService,
        private catalogoService: CatalogoService,
        private _evaluacionMedicaService: EvaluacionMedicaService,
        private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
        private route: ActivatedRoute,
        private fichaIngresoService: FichaIngresoService,
        @Inject(LOCALE_ID) private locale: string,
    ) {
        this.construirForm();
        this.construirSignosForm();
        this.dataSourceSignosCicatrices = new MatTableDataSource(
            (this.formCicatrices.get('filas') as FormArray).controls
        );
        this.dataSourceSignosCicatrices.paginator =
            this.paginatorSignosCicatrices;

        this.dataSourceSignosLunares = new MatTableDataSource(
            (this.formLunares.get('filas') as FormArray).controls
        );
        this.dataSourceSignosLunares.paginator = this.paginatorSignosLunares;

        this.dataSourceSignosDiscromias = new MatTableDataSource(
            (this.formDiscromias.get('filas') as FormArray).controls
        );
        this.dataSourceSignosDiscromias.paginator =
            this.paginatorSignosDiscromias;

        this.dataSourceSignosTatuajes = new MatTableDataSource(
            (this.formTatuajes.get('filas') as FormArray).controls
        );
        this.dataSourceSignosTatuajes.paginator = this.paginatorSignosTatuajes;

        this.dataSourceSignosHeridas = new MatTableDataSource(
            (this.formHeridas.get('filas') as FormArray).controls
        );
        this.dataSourceSignosHeridas.paginator = this.paginatorSignosHeridas;

        this.dataSourceSignosMutilaciones = new MatTableDataSource(
            (this.formMutilaciones.get('filas') as FormArray).controls
        );
        this.dataSourceSignosMutilaciones.paginator =
            this.paginatorSignosMutilaciones;

        this.dataSourceSignosNodulos = new MatTableDataSource(
            (this.formNodulos.get('filas') as FormArray).controls
        );
        this.dataSourceSignosNodulos.paginator = this.paginatorSignosNodulos;

        this.dataSourceSignosAlteracion = new MatTableDataSource(
            (this.formAlteracion.get('filas') as FormArray).controls
        );
        this.dataSourceSignosAlteracion.paginator =
            this.paginatorSignosAlteracion;

        this.dataSourceSignosRegionPerineal = new MatTableDataSource(
            (this.formRegionPerineal.get('filas') as FormArray).controls
        );
        this.dataSourceSignosRegionPerineal.paginator =
            this.paginatorSignosRegionPerineal;

        this.dataSourceSignosColVertebrales = new MatTableDataSource(
            (this.formColVertebrales.get('filas') as FormArray).controls
        );
        this.dataSourceSignosColVertebrales.paginator =
            this.paginatorSignosColVertebrales;

        this.dataSourceSignosAparatoGenital = new MatTableDataSource(
            (this.formAparatoGenital.get('filas') as FormArray).controls
        );
        this.dataSourceSignosAparatoGenital.paginator =
            this.paginatorSignosAparatoGenital;

        this.dataSourceSignosMalformacion = new MatTableDataSource(
            (this.formMalformacion.get('filas') as FormArray).controls
        );
        this.dataSourceSignosMalformacion.paginator =
            this.paginatorSignosMalformacion;
    }

    async ngOnInit(): Promise<void> {
        this.funcionesUtils.vincularClasificacionIMC(
            this.evaluacionMedicaProgresoForm,
            'IMC',                
            'clasificacionIMC'   
        );

        const loadingCatalogosRef = this.dialogMensajeService.mensajeLoading('Cargando datos...');
        forkJoin([this.cargarCatalogos(), this.obtenerCatalogos()])
            .pipe(finalize(() => loadingCatalogosRef.close()))
            .subscribe({
                error: () => {
                    this.dialogMensajeService.mensajeError('Ocurrió un error al cargar los catálogos. Intente nuevamente.');
                },
            });

        this._evaluacionMedicaService.fichaMedica$.subscribe((ficha) => {
            if (ficha) {
                this.tokenFichaMedica = ficha;
            }
        });
        this._evaluacionMedicaService.evaluacionProgreso$.subscribe((ficha) => {
            if (ficha) {
                this.tokenEvaluacionMedicaProgreso = ficha;
                const loadingRegistroRef = this.dialogMensajeService.mensajeLoading('Cargando datos del registro...');
                this.obtenerEvaluacionMedicaProgreso(loadingRegistroRef);
                this.mostrarSubidaDocumentos = true;
                if (this.mostrarSubidaDocumentos) {
                    this.obtenerTiposDeDocumentos();
                }
            }
        });
        this._evaluacionMedicaService.vistaDoctorSubject$.subscribe(
            (vista) => {
                console.log('visto doctor', vista);
                if (Boolean(vista)) {
                    this.vistaDoctor = vista;
                } else {
                    this.evaluacionMedicaProgresoForm.disable();
                }
            }
        );
        this.uuid_fp = this.route.snapshot.params['uuid_fp'];
        this.evaluacionMedicaProgresoForm.get('clinicamenteSano')?.valueChanges.subscribe((value) => {
            if (value) {
                this.evaluacionMedicaProgresoForm.get('enfermo')?.setValue(false, { emitEvent: false });
            }
        });

        this.evaluacionMedicaProgresoForm.get('enfermo')?.valueChanges.subscribe((value) => {
            if (value) {
                this.evaluacionMedicaProgresoForm.get('clinicamenteSano')?.setValue(false, { emitEvent: false });
            }
        });

        this.calcularIMCAutomaticamente();
    }

    ngAfterViewInit(): void {
        if (this.mostrarSubidaDocumentos) {
            this.obtenerDocumentos();
            if (
                this.registroEditar?.tipoEvaluacionProgreso?.nemonico ===
                'TIPO_EVALUACION_INGRESO'
            ) {
                this.obtenerDocumentosFichaIngreso();
            }
        }
    }

    construirForm() {
        this.evaluacionMedicaProgresoForm = this.formBuilder.group({
            tipoNutricion: [null as CatalogoDTO],
            tipoEvaluacion: [null as CatalogoDTO, Validators.required],
            fechaIngreso: [
                new Date().toISOString().substring(0, 10),
                [Validators.required],
            ],
            horaIngreso: [
                new Date().toTimeString().substring(0, 5),
                [Validators.required],
            ],
            tipoDesnutricion: [null as CatalogoDTO],
            grado: [''],
            talla: [null, Validators.pattern('^[0-9]+(\\.[0-9]{1,2})?$')],
            peso: [null, Validators.pattern('^[0-9]+(\\.[0-9]{1,2})?$')],
            IMC: [null],
            clasificacionIMC: [null],
            clinicamenteSano: [true],
            enfermo: [false],
            impresionDiagnostico: [''],
            manejoTerapeutico: [''],
        });
        this.evaluacionMedicaProgresoForm.markAllAsTouched();
    }

    cargarCatalogos() {
        return forkJoin({
            tipoNutricion: this.funcionesUtils.obtenerListaCatalogo(
                'ESTADO_NUTRICIONAL',
                etiquetasModel.NEMONICO_MENU_EVALUACION_MEDICA
            ),
            tipoEvaluacion: this.funcionesUtils.obtenerListaCatalogo(
                'TIPO_EVALUACION_PROGRESO',
                etiquetasModel.NEMONICO_MENU_EVALUACION_MEDICA
            ),
            tipoDesnutricion: this.funcionesUtils.obtenerListaCatalogo(
                'ESTADO_NUTRICIONAL_DESNUTRICION',
                etiquetasModel.NEMONICO_MENU_EVALUACION_MEDICA
            ),
        }).pipe(
            tap((result) => {
                this.listaEvaluacionMedicaProgreso = result.tipoEvaluacion;
                this.listaNutricion = result.tipoNutricion;
                this.listaDesNutricion = result.tipoDesnutricion;
            })
        );
    }

    volverAlListado() {
        this.volver.emit(); // Notifica al padre que se debe mostrar el listado
    }

    cancelarEdicion() {
        this.volver.emit();
    }

    construirSignosForm() {
        this.formCicatrices = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formLunares = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formDiscromias = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formTatuajes = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formHeridas = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formMutilaciones = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formNodulos = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formAlteracion = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formRegionPerineal = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formAparatoGenital = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formColVertebrales = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });

        this.formMalformacion = this.formBuilder.group({
            filas: this.formBuilder.array([]),
        });
    }

    obtenerCatalogos() {
        const tiposRequest = this.catalogoService.obtenerHijos(
            'ESQUEMA_CORPORAL',
            ''
        );
        const signosRequest = this.catalogoService.obtenerHijos(
            'CICATRICES',
            ''
        );
        const signosLunaresRequest = this.catalogoService.obtenerHijos(
            'LUNARES',
            ''
        );
        const signosDiscromiasRequest = this.catalogoService.obtenerHijos(
            'DISCROMIAS',
            ''
        );
        const signosTatuajesRequest = this.catalogoService.obtenerHijos(
            'TATUAJE',
            ''
        );
        const signosMutilacionesRequest = this.catalogoService.obtenerHijos(
            'MUTILACIONES',
            ''
        );
        const signosNodulosRequest = this.catalogoService.obtenerHijos(
            'NODULO_TUMOR',
            ''
        );
        const signosAlteracionesRequest = this.catalogoService.obtenerHijos(
            'ALTERACION_NEUROLOGICA',
            ''
        );
        const signosHeridasRequest = this.catalogoService.obtenerHijos(
            'HERIDA-LESION',
            ''
        );
        const ladosSignos = this.catalogoService.obtenerHijos(
            'LADO_SIGNO_ALTERACION',
            ''
        );
        const ubicacionesSignos = this.catalogoService.obtenerHijos(
            'UBICACION_SIGNO_ALTERACION',
            ''
        );
        const regionPerinealRequest = this.catalogoService.obtenerHijos(
            'REGION_PERINEAL',
            ''
        );
        const aparatoGenitalRequest = this.catalogoService.obtenerHijos(
            'APARATO_GENITAL',
            ''
        );
        const colVertebralesRequest = this.catalogoService.obtenerHijos(
            'COL_VERTEBRALES',
            ''
        );
        const malformacionRequest = this.catalogoService.obtenerHijos(
            'MALFORMACION_CONGENITA',
            ''
        );

        return forkJoin([
            tiposRequest,
            signosRequest,
            ladosSignos,
            ubicacionesSignos,
            signosLunaresRequest,
            signosDiscromiasRequest,
            signosTatuajesRequest,
            signosMutilacionesRequest,
            signosNodulosRequest,
            signosAlteracionesRequest,
            signosHeridasRequest,
            regionPerinealRequest,
            colVertebralesRequest,
            aparatoGenitalRequest,
            malformacionRequest,
        ]).pipe(
            tap((
                [
                    tiposResponse,
                    signosResponse,
                    ladosSignosResponse,
                    ubicacionesSignosResponse,
                    signosLunaresResponse,
                    signosDiscromiasResponse,
                    signosTatuajesResponse,
                    signosMutilacionesResponse,
                    signosNodulosResponse,
                    signosAlteracionesResponse,
                    signosHeridasResponse,
                    regionPerinealResponse,
                    colVertebralesResponse,
                    aparatoGenitalResponse,
                    malFormacionResponse,
                ]
            ) => {
                this.tipos = tiposResponse.data;
                this.signosCicatrices = signosResponse.data;
                this.signosLunares = signosLunaresResponse.data;
                this.lados = ladosSignosResponse.data;
                this.signosDiscromias = signosDiscromiasResponse.data;
                this.signosTatuajes = signosTatuajesResponse.data;
                this.signosMutilaciones = signosMutilacionesResponse.data;
                this.signosNodulos = signosNodulosResponse.data;
                this.signosAlteracion = signosAlteracionesResponse.data;
                this.signosHeridas = signosHeridasResponse.data;
                this.ubicaciones = ubicacionesSignosResponse.data;
                this.signosRegionPerineal = regionPerinealResponse.data;
                this.signosColVertebrales = colVertebralesResponse.data;
                this.signosAparatoGenital = aparatoGenitalResponse.data;
                this.signosMalformacion = malFormacionResponse.data;
            })
        );
    }

    aniadirFilaCicatrices() {
        const fila = this.formBuilder.group({
            id_temporal: [Date.now(), []],
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
            detalle: [null], // <-- Nuevo campo, sin required, solo para OTROS
        });

        this.filasCicatrices.controls.unshift(fila);
        this.filasCicatrices.markAllAsTouched();
        this.actualizarTablaCicatrices();
    }

    get filasCicatrices(): FormArray {
        return this.formCicatrices.get('filas') as FormArray;
    }

    actualizarTablaCicatrices() {
        this.dataSourceSignosCicatrices.data = this.filasCicatrices.controls;
        this.dataSourceSignosCicatrices.paginator =
            this.paginatorSignosCicatrices;
    }

    eliminarItemCicatrices(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                    }
                    this.filasCicatrices.removeAt(index);
                    this.actualizarTablaCicatrices();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlCicatrices(index: number, campo: string) {
        return (this.filasCicatrices.at(index) as FormGroup).get(campo);
    }

    compararSignos(o1: any, o2: any): boolean {
        return o1 && o2 ? o1.nemonico === o2.nemonico : o1 === o2;
    }

    //Lunares
    get filasLunares(): FormArray {
        return this.formLunares.get('filas') as FormArray;
    }

    actualizarTablaLunares() {
        this.dataSourceSignosLunares.data = this.filasLunares.controls;
        this.dataSourceSignosLunares.paginator = this.paginatorSignosLunares;
    }

    eliminarItemLunares(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {

                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                    }
                    this.filasLunares.removeAt(index);
                    this.actualizarTablaLunares();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlLunares(index: number, campo: string) {
        return (this, this.filasLunares.at(index) as FormGroup).get(campo);
    }

    aniadirFilaLunares() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
        });

        this.filasLunares.controls.unshift(fila);
        this.filasLunares.markAllAsTouched();
        this.actualizarTablaLunares();
    }

    //Tatuajes
    get filasTatuajes(): FormArray {
        return this.formTatuajes.get('filas') as FormArray;
    }

    actualizarTablatatuajes() {
        this.dataSourceSignosTatuajes.data = this.filasTatuajes.controls;
        this.dataSourceSignosTatuajes.paginator = this.paginatorSignosTatuajes;
    }

    eliminarItemTatuajes(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                    }
                    this.filasTatuajes.removeAt(index);
                    this.actualizarTablatatuajes();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlTatuajes(index: number, campo: string) {
        return (this, this.filasTatuajes.at(index) as FormGroup).get(campo);
    }

    aniadirFilaTatuajes() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
            detalle: [null],
        });

        this.filasTatuajes.controls.unshift(fila);
        this.filasTatuajes.markAllAsTouched();
        this.actualizarTablatatuajes();
    }

    //Discromias
    get filasDiscromias(): FormArray {
        return this.formDiscromias.get('filas') as FormArray;
    }

    actualizarTablaDiscromias() {
        this.dataSourceSignosDiscromias.data = this.filasDiscromias.controls;
        this.dataSourceSignosDiscromias.paginator =
            this.paginatorSignosDiscromias;
    }

    eliminarItemDiscromias(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {

                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasDiscromias.removeAt(index);
                    this.actualizarTablaDiscromias();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlDiscromias(index: number, campo: string) {
        return (this, this.filasDiscromias.at(index) as FormGroup).get(campo);
    }

    aniadirFilaDiscromias() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
            detalle: [null],
        });

        this.filasDiscromias.controls.unshift(fila);
        this.filasDiscromias.markAllAsTouched();
        this.actualizarTablaDiscromias();
    }

    //Heridas
    get filasHeridas(): FormArray {
        return this.formHeridas.get('filas') as FormArray;
    }

    actualizarTablaHeridas() {
        this.dataSourceSignosHeridas.data = this.filasHeridas.controls;
        this.dataSourceSignosHeridas.paginator = this.paginatorSignosHeridas;
    }

    eliminarItemHeridas(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasHeridas.removeAt(index);
                    this.actualizarTablaHeridas();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlHeridas(index: number, campo: string) {
        return (this, this.filasHeridas.at(index) as FormGroup).get(campo);
    }

    aniadirFilaHeridas() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
            detalle: [null],
        });

        this.filasHeridas.controls.unshift(fila);
        this.filasHeridas.markAllAsTouched();
        this.actualizarTablaHeridas();
    }

    //MUTILACIONES
    get filasMutilaciones(): FormArray {
        return this.formMutilaciones.get('filas') as FormArray;
    }

    actualizarTablaMutilaciones() {
        this.dataSourceSignosMutilaciones.data =
            this.filasMutilaciones.controls;
        this.dataSourceSignosMutilaciones.paginator =
            this.paginatorSignosMutilaciones;
    }

    eliminarItemMutilaciones(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasMutilaciones.removeAt(index);
                    this.actualizarTablaMutilaciones();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlMutilaciones(index: number, campo: string) {
        return (this, this.filasMutilaciones.at(index) as FormGroup).get(campo);
    }

    aniadirFilaMutilaciones() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
        });

        this.filasMutilaciones.controls.unshift(fila);
        this.filasMutilaciones.markAllAsTouched();
        this.actualizarTablaMutilaciones();
    }

    //Nodulos
    get filasNodulos(): FormArray {
        return this.formNodulos.get('filas') as FormArray;
    }

    actualizarTablaNodulos() {
        this.dataSourceSignosNodulos.data = this.filasNodulos.controls;
        this.dataSourceSignosNodulos.paginator = this.paginatorSignosNodulos;
    }

    eliminarItemNodulos(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {

                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasNodulos.removeAt(index);
                    this.actualizarTablaNodulos();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlNodulos(index: number, campo: string) {
        return (this, this.filasNodulos.at(index) as FormGroup).get(campo);
    }

    aniadirFilaNodulos() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
            detalle: [null],
        });

        this.filasNodulos.controls.unshift(fila);
        this.filasNodulos.markAllAsTouched();
        this.actualizarTablaNodulos();
    }

    //Alteraciones
    get filasAlteraciones(): FormArray {
        return this.formAlteracion.get('filas') as FormArray;
    }

    actualizarTablaAlteraciones() {
        this.dataSourceSignosAlteracion.data = this.filasAlteraciones.controls;
        this.dataSourceSignosAlteracion.paginator =
            this.paginatorSignosAlteracion;
    }

    eliminarItemAlteraciones(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasAlteraciones.removeAt(index);
                    this.actualizarTablaAlteraciones();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlAlteraciones(index: number, campo: string) {
        return (this, this.filasAlteraciones.at(index) as FormGroup).get(campo);
    }

    aniadirFilaAlteraciones() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            ubiacionSigno: [
                null,
                [
                    Validators.required,
                    Validators.max(999999),
                    Validators.min(0),
                ],
            ],
            ladoSigno: [null as CatalogoDTO, Validators.required],
            detalle: [null],
        });

        this.filasAlteraciones.controls.unshift(fila);
        this.filasAlteraciones.markAllAsTouched();
        this.actualizarTablaAlteraciones();
    }

    //Region Perineal
    get filasRegionPerineal(): FormArray {
        return this.formRegionPerineal.get('filas') as FormArray;
    }

    actualizarTablaRegionPerineal() {
        this.dataSourceSignosRegionPerineal.data =
            this.filasRegionPerineal.controls;
        this.dataSourceSignosRegionPerineal.paginator =
            this.paginatorSignosRegionPerineal;
    }

    eliminarItemRegionPerineal(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasRegionPerineal.removeAt(index);
                    this.actualizarTablaRegionPerineal();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlRegionPerineal(index: number, campo: string) {
        return (this, this.filasRegionPerineal.at(index) as FormGroup).get(
            campo
        );
    }

    aniadirFilaRegionPerineal() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            presente: [null, [Validators.required]],
            detalle: [null],
        });

        this.filasRegionPerineal.controls.unshift(fila);
        this.filasRegionPerineal.markAllAsTouched();
        this.actualizarTablaRegionPerineal();
    }

    //Col Vertebrales
    get filasColVertebrales(): FormArray {
        return this.formColVertebrales.get('filas') as FormArray;
    }

    actualizarTablaColVertebrales() {
        this.dataSourceSignosColVertebrales.data =
            this.filasColVertebrales.controls;
        this.dataSourceSignosColVertebrales.paginator =
            this.paginatorSignosColVertebrales;
    }

    eliminarItemColVertebrales(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasColVertebrales.removeAt(index);
                    this.actualizarTablaColVertebrales();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlColVertebrales(index: number, campo: string) {
        return (this, this.filasColVertebrales.at(index) as FormGroup).get(
            campo
        );
    }

    aniadirFilaColVertebrales() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            presente: [null, [Validators.required]],
            detalle: [null],
        });

        this.filasColVertebrales.controls.unshift(fila);
        this.filasColVertebrales.markAllAsTouched();
        this.actualizarTablaColVertebrales();
    }

    //Aparato Genital
    get filasAparatoGenital(): FormArray {
        return this.formAparatoGenital.get('filas') as FormArray;
    }

    actualizarTablaAparatoGenital() {
        this.dataSourceSignosAparatoGenital.data =
            this.filasAparatoGenital.controls;
        this.dataSourceSignosAparatoGenital.paginator =
            this.paginatorSignosAparatoGenital;
    }

    eliminarItemAparatoGenital(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasAparatoGenital.removeAt(index);
                    this.actualizarTablaAparatoGenital();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlAparatoGenital(index: number, campo: string) {
        return (this, this.filasAparatoGenital.at(index) as FormGroup).get(
            campo
        );
    }

    aniadirFilaAparatoGenital() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            presente: [null, [Validators.required]],
            detalle: [null],
        });

        this.filasAparatoGenital.controls.unshift(fila);
        this.filasAparatoGenital.markAllAsTouched();
        this.actualizarTablaAparatoGenital();
    }

    //Aparato Genital
    get filasMalformaciones(): FormArray {
        return this.formMalformacion.get('filas') as FormArray;
    }

    actualizarTablaMalformaciones() {
        this.dataSourceSignosMalformacion.data =
            this.filasMalformaciones.controls;
        this.dataSourceSignosMalformacion.paginator =
            this.paginatorSignosMalformacion;
    }

    eliminarItemMalformaciones(index: number, item?: any) {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            'Se eliminará la pertenencia seleccionada de la lista',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (
                        item.get('tokenIdentificador')?.value &&
                        item.get('tokenIdentificador')?.value.value.length > 0
                    ) {
                        this.tokensAEliminar.push(
                            item.get('tokenIdentificador')?.value.value
                        );
                        console.log('token a eliminar', this.tokensAEliminar);
                    }
                    this.filasMalformaciones.removeAt(index);
                    this.actualizarTablaMalformaciones();
                    // this.registro.detalleIngresos.splice(index, 1);
                    // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
                    // this.dataSourceIngreso.paginator = this.paginatorIngreso;
                }
            },
        });
    }

    obtenerControlMalformaciones(index: number, campo: string) {
        return (this, this.filasMalformaciones.at(index) as FormGroup).get(
            campo
        );
    }

    aniadirFilaMalformaciones() {
        const fila = this.formBuilder.group({
            tokenIdentificador: ['', []],
            criterioHijo: [null as CatalogoDTO, Validators.required],
            clave: [null, Validators.required],
            presente: [null, [Validators.required]],
            detalle: [null],
        });

        this.filasMalformaciones.controls.unshift(fila);
        this.filasMalformaciones.markAllAsTouched();
        this.actualizarTablaMalformaciones();
    }

    actualizarClave(
        index: number,
        signoSeleccionado: any,
        nemonico: string
    ): void {
        // Verifica si el objeto seleccionado tiene la propiedad "clave" o un atributo específico
        const clave = signoSeleccionado?.descripcion || '';
        let controlClave = null; // Reemplaza 'clave' con el nombre del atributo deseado
        if (nemonico == 'CICATRICES') {
            controlClave = this.obtenerControlCicatrices(index, 'clave');
        } else if (nemonico == 'LUNARES') {
            controlClave = this.obtenerControlLunares(index, 'clave');
        } else if (nemonico == 'DISCROMIAS') {
            controlClave = this.obtenerControlDiscromias(index, 'clave');
        } else if (nemonico == 'TATUAJES') {
            controlClave = this.obtenerControlTatuajes(index, 'clave');
        } else if (nemonico == 'HERIDAS') {
            controlClave = this.obtenerControlHeridas(index, 'clave');
        } else if (nemonico == 'MUTILACIONES') {
            controlClave = this.obtenerControlMutilaciones(index, 'clave');
        } else if (nemonico == 'NODULOS') {
            controlClave = this.obtenerControlNodulos(index, 'clave');
        } else if (nemonico == 'ALTERACIONES') {
            controlClave = this.obtenerControlAlteraciones(index, 'clave');
        } else if (nemonico == 'REGION_PERINEAL') {
            controlClave = this.obtenerControlRegionPerineal(index, 'clave');
        } else if (nemonico == 'COL_VERTEBRALES') {
            controlClave = this.obtenerControlColVertebrales(index, 'clave');
        } else if (nemonico == 'APARATO_GENITAL') {
            controlClave = this.obtenerControlAparatoGenital(index, 'clave');
        } else if (nemonico == 'MALFORMACION_CONGENITA') {
            controlClave = this.obtenerControlMalformaciones(index, 'clave');
        }
        if (controlClave) {
            controlClave.setValue(clave); // Establece el valor del input
        }
    }

    trackByFn(index: number, item: any): any {
        return item.id || index; // Usa un identificador único o el índice
    }

    esFormularioInvalido(): boolean {
        const formularioInvalido = this.evaluacionMedicaProgresoForm.invalid;
        const filasInvalidas =
            this.filasCicatrices.controls.some((row) => row.invalid);
            // ||
            // this.dataSourceSignosCicatrices.data.length < 1;
        return formularioInvalido || filasInvalidas;
    }

    guardar() {
        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
            'Deseas continuar?'
        );
        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    let fechaIngreso =
                        this.evaluacionMedicaProgresoForm.get(
                            'fechaIngreso'
                        ).value;
                    let horaIngreso =
                        this.evaluacionMedicaProgresoForm.get(
                            'horaIngreso'
                        ).value;
                    let fechaCompletaString = `${fechaIngreso}T${horaIngreso}:00`;
                    let fechaIngresoCompleta = new Date(fechaCompletaString);
                    this.registro = new EvaluacionMedicaProgresoDTO();
                    this.registro.criteriosEvaluacionProgresoAsociados = [];
                    this.dataSourceSignosCicatrices.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'CICATRICES'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosLunares.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'LUNARES'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosDiscromias.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'DISCROMIAS'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosTatuajes.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'TATUAJE'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosHeridas.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'HERIDA-LESION'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosMutilaciones.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'MUTILACIONES'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosNodulos.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'NODULO_TUMOR'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosAlteracion.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) =>
                                        x.nemonico == 'ALTERACION_NEUROLOGICA'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosRegionPerineal.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'REGION_PERINEAL'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosColVertebrales.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'COL_VERTEBRALES'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosAparatoGenital.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) => x.nemonico == 'APARATO_GENITAL'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.dataSourceSignosMalformacion.data.map((item) =>
                        this.registro.criteriosEvaluacionProgresoAsociados.push(
                            item.value
                        )
                    );
                    this.registro.criteriosEvaluacionProgresoAsociados.forEach(
                        (item) => {
                            if (!item.criterioPadre) {
                                item.criterioPadre = this.tipos.find(
                                    (x) =>
                                        x.nemonico == 'MALFORMACION_CONGENITA'
                                );
                            }
                            if ((item.tokenIdentificador as any)?.value) {
                                item.tokenIdentificador = (
                                    item.tokenIdentificador as any
                                ).value;
                            }
                        }
                    );
                    this.registro.estadoNutricional =
                        this.evaluacionMedicaProgresoForm.get(
                            'tipoNutricion'
                        ).value;
                    this.registro.tipoEvaluacionProgreso =
                        this.evaluacionMedicaProgresoForm.get(
                            'tipoEvaluacion'
                        ).value;
                    this.registro.tipoDesnutricion = this.evaluacionMedicaProgresoForm.get('tipoDesnutricion').value;
                    this.registro.grado = this.evaluacionMedicaProgresoForm.get('grado').value;
                    this.registro.peso = this.evaluacionMedicaProgresoForm.get('peso').value;
                    this.registro.talla = this.evaluacionMedicaProgresoForm.get('talla').value;
                    this.registro.clinicamenteSano = this.evaluacionMedicaProgresoForm.get('clinicamenteSano').value;
                    this.registro.imc = this.evaluacionMedicaProgresoForm.get('IMC').value;
                    this.registro.enfermo = this.evaluacionMedicaProgresoForm.get('enfermo').value;
                    this.registro.impresionDiagnostico = this.evaluacionMedicaProgresoForm.get('impresionDiagnostico').value;
                    this.registro.manejoTerapeutico = this.evaluacionMedicaProgresoForm.get('manejoTerapeutico').value;

                    this.registro.tokenIdFichaMedica = this.tokenFichaMedica;
                    this.registro.fecha = fechaIngresoCompleta;
                    this.registro.tokensCriteriosEliminar =
                        this.tokensAEliminar;
                    this.registro.tokenIdentificadorFichaIdentificacion =
                        this.uuid_fp;
                    console.log('data saliend', this.registro);

                    console.log(this.registro);
                    if (this.tokenEvaluacionMedicaProgreso) {
                        this.registro.tokenIdentificador =
                            this.tokenEvaluacionMedicaProgreso;
                        this._evaluacionMedicaService
                            .updateEvaluacionMedicaProgreso(this.registro)
                            .subscribe({
                                next: (
                                    response: RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>
                                ) => {
                                    if (!response.exito) {
                                        this._evaluacionMedicaService.checkError(
                                            response
                                        );

                                        return;
                                    }
                                    this.dialogMensajeService.mensajeExitoso(
                                        response.titulo,
                                        response.mensaje
                                    );
                                    this.tokenEvaluacionMedicaProgreso = null;
                                    this.cancelarEdicion();
                                },
                                error: (error: any) => {
                                    this._evaluacionMedicaService.checkError(
                                        error
                                    );
                                },
                            });
                    } else {
                        this._evaluacionMedicaService
                            .postEvaluacionMedicaProgreso(this.registro)
                            .subscribe({
                                next: (
                                    response: RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>
                                ) => {
                                    if (!response.exito) {
                                        this._evaluacionMedicaService.checkError(
                                            response
                                        );

                                        return;
                                    }
                                    this.dialogMensajeService.mensajeExitoso(
                                        response.titulo,
                                        response.mensaje
                                    );
                                    this.tokenEvaluacionMedicaProgreso = null;
                                    this.cancelarEdicion();
                                },
                                error: (error: any) => {
                                    this._evaluacionMedicaService.checkError(
                                        error
                                    );
                                },
                            });
                    }
                }
            },
        });
    }

    async obtenerEvaluacionMedicaProgreso(loadingRef?: MatDialogRef<FuseConfirmationDialogComponent>) {
        this._evaluacionMedicaService
            .getEvaluacionMedicaProgresoByTokenId(
                this._evaluacionMedicaService.getTokenEvaluacionMedicaProgreso()
            )
            .pipe(finalize(() => loadingRef?.close()))
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }
                    this.registroEditar = response.data;
                    let fechaIngresoCompleta = new Date(
                        this.registroEditar.fecha
                    );
                    const fechaIngreso = fechaIngresoCompleta
                        .toISOString()
                        .split('T')[0];
                    const horaIngreso = fechaIngresoCompleta
                        .toTimeString()
                        .slice(0, 5);
                    this.evaluacionMedicaProgresoForm
                        .get('fechaIngreso')
                        ?.setValue(fechaIngreso);
                    this.evaluacionMedicaProgresoForm
                        .get('horaIngreso')
                        ?.setValue(horaIngreso);
                    this.evaluacionMedicaProgresoForm
                        .get('tipoNutricion')
                        .setValue(this.registroEditar.estadoNutricional);
                    this.evaluacionMedicaProgresoForm
                        .get('tipoEvaluacion')
                        .setValue(this.registroEditar.tipoEvaluacionProgreso);
                    this.evaluacionMedicaProgresoForm.get('grado').setValue(this.registroEditar?.grado);
                    if (this.registroEditar?.tipoDesnutricion) {
                        this.evaluacionMedicaProgresoForm.get('tipoDesnutricion').setValue(this.registroEditar.tipoDesnutricion);
                    }
                    this.evaluacionMedicaProgresoForm.get('peso').setValue(this.registroEditar?.peso);
                    this.evaluacionMedicaProgresoForm.get('talla').setValue(this.registroEditar?.talla);
                    this.evaluacionMedicaProgresoForm.get('clinicamenteSano').setValue(this.registroEditar?.clinicamenteSano);
                    this.evaluacionMedicaProgresoForm.get('IMC').setValue(this.registroEditar?.imc);
                    this.evaluacionMedicaProgresoForm.get('enfermo').setValue(this.registroEditar?.enfermo);
                    this.evaluacionMedicaProgresoForm.get('impresionDiagnostico').setValue(this.registroEditar?.impresionDiagnostico);
                    this.evaluacionMedicaProgresoForm.get('manejoTerapeutico').setValue(this.registroEditar?.manejoTerapeutico);
                    console.log('registro', this.registroEditar);
                    for (let criterio of this.registroEditar
                        .criteriosEvaluacionProgresoAsociados) {
                        if (
                            criterio.criterioPadre.nemonico !=
                            'REGION_PERINEAL' &&
                            criterio.criterioPadre.nemonico !=
                            'COL_VERTEBRALES' &&
                            criterio.criterioPadre.nemonico !=
                            'APARATO_GENITAL' &&
                            criterio.criterioPadre.nemonico !=
                            'MALFORMACION_CONGENITA'
                        ) {
                            const fila = this.formBuilder.group({
                                tokenIdentificador: [
                                    { value: criterio.tokenIdentificador },
                                ],
                                criterioHijo: [
                                    {
                                        value: criterio.criterioHijo as CatalogoDTO,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                                clave: [
                                    {
                                        value: criterio.criterioHijo?.descripcion,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                                ubiacionSigno: [
                                    {
                                        value: criterio.ubiacionSigno as CatalogoDTO,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                                ladoSigno: [
                                    {
                                        value: criterio.ladoSigno as CatalogoDTO,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                            });
                            if (!this.vistaDoctor) {
                                fila.disable();
                            }
                            if (
                                criterio.criterioPadre.nemonico == 'CICATRICES' || criterio.criterioPadre.nemonico == 'DISCROMIAS'
                                || criterio.criterioPadre.nemonico == 'TATUAJE' || criterio.criterioPadre.nemonico == 'HERIDA-LESION'
                                || criterio.criterioPadre.nemonico == 'ALTERACION_NEUROLOGICA' || criterio.criterioPadre.nemonico == 'NODULO_TUMOR'
                            ) {
                                const fila = this.formBuilder.group({
                                    tokenIdentificador: [{ value: criterio.tokenIdentificador }],
                                    criterioHijo: [
                                        { value: criterio.criterioHijo as CatalogoDTO, disabled: this.estadoVisualizar },
                                        Validators.required,
                                    ],
                                    clave: [
                                        { value: criterio.criterioHijo?.descripcion, disabled: this.estadoVisualizar },
                                        Validators.required,
                                    ],
                                    ubiacionSigno: [
                                        { value: criterio.ubiacionSigno as CatalogoDTO, disabled: this.estadoVisualizar },
                                        Validators.required,
                                    ],
                                    ladoSigno: [
                                        { value: criterio.ladoSigno as CatalogoDTO, disabled: this.estadoVisualizar },
                                        Validators.required,
                                    ],
                                    detalle: [
                                        { value: criterio.detalle ?? null, disabled: this.estadoVisualizar },

                                    ],
                                });
                                if (criterio.criterioPadre.nemonico == 'DISCROMIAS') {
                                    this.filasDiscromias.controls.push(fila);
                                } else if (criterio.criterioPadre.nemonico == 'CICATRICES') {
                                    this.filasCicatrices.controls.push(fila);
                                } else if (
                                    criterio.criterioPadre.nemonico == 'TATUAJE'
                                ) {
                                    this.filasTatuajes.controls.push(fila);
                                } else if (criterio.criterioPadre.nemonico == 'HERIDA-LESION') {
                                    this.filasHeridas.controls.push(fila);
                                } else if (criterio.criterioPadre.nemonico == 'ALTERACION_NEUROLOGICA') {
                                    this.filasAlteraciones.controls.push(fila);
                                } else if (criterio.criterioPadre.nemonico == 'NODULO_TUMOR') {
                                    this.filasNodulos.controls.push(fila);
                                }
                            } else if (
                                criterio.criterioPadre.nemonico == 'LUNARES'
                            ) {
                                this.filasLunares.controls.push(fila);
                            } else if (
                                criterio.criterioPadre.nemonico ==
                                'HERIDA-LESION'
                            ) {
                                this.filasHeridas.controls.push(fila);
                            } else if (
                                criterio.criterioPadre.nemonico ==
                                'MUTILACIONES'
                            ) {
                                this.filasMutilaciones.controls.push(fila);
                            }
                        } else {
                            const fila = this.formBuilder.group({
                                tokenIdentificador: [
                                    { value: criterio.tokenIdentificador },
                                ],
                                criterioHijo: [
                                    {
                                        value: criterio.criterioHijo as CatalogoDTO,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                                clave: [
                                    {
                                        value: criterio.criterioHijo
                                            .descripcion,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                                presente: [
                                    {
                                        value: criterio.presente,
                                        disabled: this.estadoVisualizar,
                                    },
                                    Validators.required,
                                ],
                                detalle: [
                                    { value: criterio.detalle ?? null, disabled: this.estadoVisualizar },

                                ],
                            });
                            if (!this.vistaDoctor) {
                                fila.disable();
                            }
                            if (
                                criterio.criterioPadre.nemonico ==
                                'REGION_PERINEAL'
                            ) {
                                this.filasRegionPerineal.controls.push(fila);
                            } else if (
                                criterio.criterioPadre.nemonico ==
                                'COL_VERTEBRALES'
                            ) {
                                this.filasColVertebrales.controls.push(fila);
                            } else if (
                                criterio.criterioPadre.nemonico ==
                                'APARATO_GENITAL'
                            ) {
                                this.filasAparatoGenital.controls.push(fila);
                            } else if (
                                criterio.criterioPadre.nemonico ==
                                'MALFORMACION_CONGENITA'
                            ) {
                                this.filasMalformaciones.controls.push(fila);
                            }
                        }
                    }
                    this.actualizarTablaCicatrices();
                    this.actualizarTablaLunares();
                    this.actualizarTablaDiscromias();
                    this.actualizarTablaAlteraciones();
                    this.actualizarTablaNodulos();
                    this.actualizarTablaMutilaciones();
                    this.actualizarTablatatuajes();
                    this.actualizarTablaHeridas();
                    this.actualizarTablaRegionPerineal();
                    this.actualizarTablaColVertebrales();
                    this.actualizarTablaAparatoGenital();
                    this.actualizarTablaMalformaciones();
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.dialogMensajeService.mensajeError('Ocurrió un error al cargar los datos del registro. Intente nuevamente.');
                },
                complete: () => { },
            });
    }

    obtenerTiposDeDocumentos() {
        let nemonico =
            etiquetasModel.NEMONICO_TIPO_SIGNOS_ALTERACIONES_HISTORIAL;
        this.catalogoService.obtenerHijos(nemonico, '').subscribe({
            next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
                if (!environment.production) {
                    console.log(response);
                }

                if (!response.exito) {
                    this.catalogoService.checkError(response);
                    return;
                }

                this.tiposDeDocumentosSistema = response.data
                    ?.sort((a, b) => {
                        if (a.nombre < b.nombre) {
                            return -1;
                        }
                        if (a.nombre > b.nombre) {
                            return 1;
                        }
                        return 0;
                    })
                    ?.map((cat) => {
                        let tipoDoc = cat as TipoDeDocumento;
                        return tipoDoc;
                    });
            },
            error: (error: any) => {
                this.catalogoService.checkError(error);
            },
        });
    }

    obtenerDocumentos() {
        let page = this.tablaDocumentos.page;
        let pageSize = this.tablaDocumentos.pageSize;

        let medicaProgresoDocumentosRequest =
            new EvaluacionMedicaProgresoDocumentosRequest();
        medicaProgresoDocumentosRequest.page = page;
        medicaProgresoDocumentosRequest.size = pageSize;
        medicaProgresoDocumentosRequest.textoBuscar =
            this.tablaDocumentos.textoBuscar;
        medicaProgresoDocumentosRequest.tokenIdentificadorEvaluacionMedicaProgreso =
            this.tokenEvaluacionMedicaProgreso;

        this._evaluacionMedicaService
            .obtenerDocumentosEvaluacionMedicaProgreso(
                medicaProgresoDocumentosRequest,
                ''
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<DocumentoDTO>
                    >
                ) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                    }

                    if (response.data?.data) {
                        this.tablaDocumentos.actualizarTabla(
                            response.data.data,
                            response.data.totalItems
                        );
                    }
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                },
            });
    }

    eliminacionDocumento(documentoDTO: DocumentoDTO) {
        let load = this.dialogMensajeService.mensajeLoading(
            'Quitando el documento: ' + documentoDTO.nombre + ' del detalle..'
        );
        let medicaProgresoDocumento =
            new EvaluacionMedicaProgresoDocumentoDTO();
        medicaProgresoDocumento.documentoDTO = documentoDTO;
        medicaProgresoDocumento.tokenIdentificadorEvaluacionMedicaProgreso =
            this.tokenEvaluacionMedicaProgreso;
        this._evaluacionMedicaService
            .eliminarDocumentoEvaluacionMedicaProgreso(
                medicaProgresoDocumento,
                ''
            )
            .subscribe({
                next: (
                    respone: RespuestaPorDefecto<EvaluacionMedicaProgresoDocumentoDTO>
                ) => {
                    load.close();
                    if (!respone.exito) {
                        this._evaluacionMedicaService.checkError(respone);
                    }

                    this.obtenerDocumentos();
                },
                error: (error: any) => {
                    load.close();
                    this._evaluacionMedicaService.checkError(error);
                },
            });
    }

    edicionEvent(exito: boolean) {
        if (exito) {
            this.obtenerDocumentos();
        }
    }

    subirArchivosEvent(documentos: DocumentoSubido[]) {
        if (documentos && documentos.length > 0) {
            for (let documentoSubido of documentos) {
                let fichaIngresoDocumento =
                    new EvaluacionMedicaProgresoDocumentoDTO();
                fichaIngresoDocumento.tokenIdentificadorEvaluacionMedicaProgreso =
                    this.tokenEvaluacionMedicaProgreso;
                fichaIngresoDocumento.documentoDTO =
                    documentoSubido.documentoDTO;

                let load = this.dialogMensajeService.mensajeLoading(
                    'Subiendo el documento: ' + documentoSubido.documento.name
                );
                this._evaluacionMedicaService
                    .subirDocumentoEvaluacionMedicaProgreso(
                        documentoSubido.documento,
                        fichaIngresoDocumento,
                        ''
                    )
                    .subscribe({
                        next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
                            load.close();
                            if (!response.exito) {
                                this._evaluacionMedicaService.checkError(
                                    response
                                );
                                return;
                            }

                            //Refrescar la tabla de documentos
                            this.obtenerDocumentos();
                        },
                        error: (error: any) => {
                            load.close();
                            this._evaluacionMedicaService.checkError(error);
                        },
                    });
            }
        } else {
            this.dialogMensajeService.mensajeError(
                'No se obtenieron documentos para ser subidos'
            );
        }
    }

    pageEventDocumentos(event: PageEvent) {
        this.tablaDocumentos.page = event.pageIndex;
        this.tablaDocumentos.pageSize = event.pageSize;

        this.obtenerDocumentos();
    }

    obtenerDocumentosFichaIngreso() {
        let page = this.tablaDocumentosIngreso.page;
        let pageSize = this.tablaDocumentosIngreso.pageSize;

        let fichaIngresoDocumentosRequest = new FichaIngresoDocumentosRequest();
        fichaIngresoDocumentosRequest.page = page;
        fichaIngresoDocumentosRequest.size = pageSize;
        fichaIngresoDocumentosRequest.textoBuscar =
            this.tablaDocumentosIngreso.textoBuscar;
        fichaIngresoDocumentosRequest.tokenIdentificador = this.uuid_fp;
        this.fichaIngresoService
            .obtenerTodosDocumentosFichaIngreso(
                fichaIngresoDocumentosRequest,
                ''
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<DocumentoDTO>
                    >
                ) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this.fichaIngresoService.checkError(response);
                    }

                    if (response.data?.data) {
                        this.tablaDocumentosIngreso.actualizarTabla(
                            response.data.data,
                            response.data.totalItems
                        );
                    }
                },
                error: (error: any) => {
                    this.fichaIngresoService.checkError(error);
                },
            });
    }

    pageEventDocumentosFichaIngreso(event: PageEvent) {
        this.tablaDocumentosIngreso.page = event.pageIndex;
        this.tablaDocumentosIngreso.pageSize = event.pageSize;

        this.obtenerDocumentosFichaIngreso();
    }

    soloNumero(event: KeyboardEvent): void {
        const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete'];
        const isNumberKey = event.key >= '0' && event.key <= '9';

        if (!isNumberKey && !allowedKeys.includes(event.key)) {
            event.preventDefault();
        }
    }

    soloNumeroDecimal(event: KeyboardEvent): void {
        const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete', '.'];
        const isNumberKey = event.key >= '0' && event.key <= '9';

        if (!isNumberKey && !allowedKeys.includes(event.key)) {
            event.preventDefault();
        }
    }

    ingresoMedidas(event: any): void {
        const decimalSeparator = this.locale === 'es' ? '.' : ',';
        const input = event.target as HTMLInputElement;
        let valor = input.value;
        const patronDecimal = new RegExp(`^[0-9${decimalSeparator}]*$`);
        if (patronDecimal.test(valor)) {
            input.value = ((parseFloat(valor)).toFixed(2)).toString();
        }
    }

    onClinicamenteSanoChange(value: boolean): void {
        if (value) {
            this.evaluacionMedicaProgresoForm.get('enfermo')?.setValue(false, { emitEvent: false });
        }
    }

    onEnfermoChange(value: boolean): void {
        if (value) {
            this.evaluacionMedicaProgresoForm.get('clinicamenteSano')?.setValue(false, { emitEvent: false });
        }
    }

    calcularIMCAutomaticamente(): void {
        combineLatest([
            this.evaluacionMedicaProgresoForm.get('talla').valueChanges,
            this.evaluacionMedicaProgresoForm.get('peso').valueChanges
        ]).subscribe(([talla, peso]) => {
            if (talla && peso) {
                const tallam = parseFloat(talla);
                const pesom = parseFloat(peso);
                // Verifica que talla sea mayor que cero para evitar división por cero
                if (!isNaN(tallam) && tallam > 0 && !isNaN(pesom)) {
                    const imc = pesom / (tallam * tallam);
                    // Establece el IMC formateado a dos decimales
                    this.evaluacionMedicaProgresoForm.get('IMC').setValue(imc.toFixed(2), { emitEvent: true });
                } else {
                    this.evaluacionMedicaProgresoForm.get('IMC').setValue(null, { emitEvent: false });
                }
            } else {
                this.evaluacionMedicaProgresoForm.get('IMC').setValue(null, { emitEvent: false });
            }
        });
    }

    esSignoOtros(i: number, tipo: string): boolean {
        let control;
        if (tipo === 'CICATRICES') {
            control = this.obtenerControlCicatrices(i, 'criterioHijo');
        } else if (tipo === 'DISCROMIAS') {
            control = this.obtenerControlDiscromias(i, 'criterioHijo');
        }
        else if (tipo === 'TATUAJES') {
            control = this.obtenerControlTatuajes(i, 'criterioHijo');
        } else if (tipo === 'HERIDA-LESION') {
            control = this.obtenerControlHeridas(i, 'criterioHijo');
        } else if (tipo === 'ALTERACION_NEUROLOGICA') {
            control = this.obtenerControlAlteraciones(i, 'criterioHijo');
        } else if (tipo === 'NODULO-TUMOR') {
            control = this.obtenerControlNodulos(i, 'criterioHijo');
        } else if (tipo === 'REGION_PERINEAL') {
            control = this.obtenerControlRegionPerineal(i, 'criterioHijo');
        } else if (tipo === 'APARATO_GENITAL') {
            control = this.obtenerControlAparatoGenital(i, 'criterioHijo');
        } else if (tipo === 'COL_VERTEBRALES') {
            control = this.obtenerControlColVertebrales(i, 'criterioHijo');
        } else if (tipo === 'MALFORMACION_CONGENITA') {
            control = this.obtenerControlMalformaciones(i, 'criterioHijo');
        }
        return control && control.value && control.value.nemonico && control.value.nemonico.toUpperCase().includes('OTRO');
    }
}
