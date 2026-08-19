import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import {
    ChangeDetectorRef,
    Component,
    inject,
    Inject,
    LOCALE_ID,
    OnInit,
    signal,
    ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    UntypedFormBuilder,
    UntypedFormControl,
    UntypedFormGroup,
    Validators,
} from '@angular/forms';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DATE_LOCALE,
    provideNativeDateAdapter,
} from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogClose } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
    MatPaginatorIntl,
    MatPaginatorModule,
    PageEvent,
} from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperModule } from '@angular/material/stepper';
import {
    MatTable,
    MatTableDataSource,
    MatTableModule,
} from '@angular/material/table';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaMedicaEnfermedadDTO } from 'app/core/model/both/fichaMedicaEnfermedadDTO.model';
import { AntecedenteFamiliarDTO } from 'app/core/model/both/ia/ficha-medica/AntecedenteFamiliarDTO.model';
import { FichaMedicaDTO } from 'app/core/model/both/ia/ficha-medica/FichaMedicaDTO.model';
import { IngresoCentroJuvenilDTO } from 'app/core/model/both/ia/ficha-medica/IngresoCentroJuvenilDTO.model';
import { InformacionUbicacionDTO } from 'app/core/model/both/InformacionUbicacionDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { PersonaRelacionadaEnfermedadDTO } from 'app/core/model/both/personaRelacionadaEnfermedadDTO.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CustomPaginatorIntl } from 'app/core/services/custom-paginator-intl.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { SnackbarService } from 'app/core/services/snackbar.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import {
    AccionesCatalogo,
    AccionesCatalogoComponent,
} from 'app/modules/catalogo/buttom-sheet-catalago/acciones-catalogo.component';
import { CatalogoService } from 'app/modules/catalogo/catalogo.service';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { EnfermedadPersonaRelacionadaService } from 'app/modules/seguridad/services/enfermedadPersonaRelacionada.service';
import { environment } from 'environments/environment';
import { List, merge } from 'lodash';
import moment from 'moment';
import 'moment/locale/es';
import { catchError, map, Observable, of } from 'rxjs';
import { EvaluacionMedicaService } from './evaluacion-medica.service';
import { ModalCrearEditarEnfermedadFichaComponent } from './modal-crear-editar-enfermedad-ficha/modal-crear-editar-enfermedad-ficha.component';
import { ModalCrearEditarEnfermedadPersonaRelacionadaComponent } from './modal-crear-editar-enfermedad-persona-relacionada/modal-crear-editar-enfermedad-persona-relacionada.component';
import { CrearEditarEvaluacionMedicaProgresoComponent } from './progreso-evaluacion-medica/crear-editar-evaluacion-medica-progreso/crear-editar-evaluacion-medica-progreso.component';
import { ProgresoEvaluacionMedicaComponent } from './progreso-evaluacion-medica/progreso-evaluacion-medica.component';
import { ConsultaMedicaComponent } from './consulta-medica/consulta-medica.component';
import { CrearEditarConsultaMedicaComponent } from './consulta-medica/crear-editar-consulta-medica/crear-editar-consulta-medica.component';
import { HttpClient } from '@angular/common/http';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { HistClinGestionDocsComponent } from './hist-clin-gestion-docs/hist-clin-gestion-docs.component';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';

@Component({
    selector: 'app-evaluacion-medica',
    standalone: true,
    imports: [
    MatStepperModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatRadioModule,
    MatPaginatorModule,
    MatExpansionModule,
    RouterOutlet,
    MatDatepickerModule,
    MatTabsModule,
    ProgresoEvaluacionMedicaComponent,
    CommonModule,
    CrearEditarEvaluacionMedicaProgresoComponent,
    ConsultaMedicaComponent,
    CrearEditarConsultaMedicaComponent,
    HistClinGestionDocsComponent,
    MatDialogClose
],
    providers: [
        { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl },
        { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
        provideNativeDateAdapter(),
    ],
    templateUrl: './evaluacion-medica.component.html',
    styleUrl: './evaluacion-medica.component.scss',
})
export class EvaluacionMedicaComponent implements OnInit {
    private permisosService = inject(PermisoRolUsuarioService);

    nemonicoTiposSangre: string = 'TIPOS_SANGRE';
    nemonicoMotivo: string = 'MOTIVOS_INGRESO_CENTROS';
    nemonicoParentezco: string = 'PARENTESCO';
    nemonicoEnfermedad: string = 'ENFERMEDADES';
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_MEDICA;

    formatoFecha: string = 'DD/MM/YYYY';

    fichaIdentificacion: FichaIdentificacionDTO;

    pageCentros = 0;
    listSizeCentros = [5, 10, 15, 20];
    sizeCentros = this.listSizeCentros[0];
    totalItemsCentros = 0;

    pageAntecedentes = 0;
    listSizeAntecedentes = [5, 10, 15, 20];
    sizeAntecedentes = this.listSizeAntecedentes[0];
    totalItemsAntecedentes = 0;

    fichaMedica: FichaMedicaDTO;

    antecedentes: AntecedenteFamiliarDTO[] = [];
    ingresosCentros: IngresoCentroJuvenilDTO[] = [];
    dataSourceAntecedentes = new MatTableDataSource<AntecedenteFamiliarDTO>([]);
    dataSourceCentros = new MatTableDataSource<IngresoCentroJuvenilDTO>([]);

    catalogosTipoSangre: CatalogoDTO[] = [];
    catalogosMotivo: CatalogoDTO[] = [];
    catalogosParentezco: CatalogoDTO[] = [];
    catalogosEnfermedad: CatalogoDTO[] = [];
    listaTipoGenero: CatalogoDTO[] = [];

    showFormCentro: boolean = false;
    showFormAntecedente: boolean = false;
    showSeguimiento: boolean = false;
    showLesiones: boolean = false;
    showEnfermedades: boolean = false;
    showMedicamentos: boolean = false;
    showListado: boolean = true;

    datosPrincipales: boolean = false;

    otroSeguro: string = '';

    maxlenghtTextArea: number = 200;

    uuid_fp: string;
    isLoading: boolean = false;

    deshabilitarEdicion: boolean = false;

    permisos: Record<string, Record<string, boolean>> = {};

    // #region Formulario ficha medica
    // fichaMedicaForm: FormGroup;

    readonly formFieldTipoSangre = new FormControl('', []);
    readonly formFieldLesiones = new FormControl('', [
        Validators.maxLength(this.maxlenghtTextArea),
        this.noSoloEspaciosValidator,
    ]);
    readonly formFieldEnfermedades = new FormControl('', [
        Validators.maxLength(this.maxlenghtTextArea),
        this.noSoloEspaciosValidator,
    ]);
    readonly formFieldMedicamentos = new FormControl('', [
        Validators.maxLength(this.maxlenghtTextArea),
        this.noSoloEspaciosValidator,
    ]);
    readonly formFieldSeguroMedico = new FormControl('', []);
    readonly formFieldOtroSeguro = new FormControl(
        { value: '', disabled: true },
        []
    );
    readonly formFieldInstitucionAcude = new FormControl('', [
        this.noSoloEspaciosValidator,
    ]);
    readonly formFieldInternadoHospital = new FormControl('', [
        this.noSoloEspaciosValidator,
    ]);

    readonly alergiaMedicamentos = new FormControl(false, [
        Validators.required,
    ]);

    readonly detalleAlergias = new FormControl('', []);

    readonly alergiaAlimentos = new FormControl(false, [
        Validators.required,
    ]);

    readonly detalleAlergiasAlimentos = new FormControl('', []);

    readonly cirugiaQuirurgica = new FormControl(true, [Validators.required]);

    readonly detalleCirugias = new FormControl('', []);

    readonly fractura = new FormControl(false, [Validators.required]);

    readonly detalleFracturas = new FormControl('', []);

    readonly irs = new FormControl('', []);

    readonly icd = new FormControl('', []);

    readonly usoPreservativo = new FormControl(false, []);

    readonly relacionGenero = new FormControl('', []);

    readonly drogaInicio = new FormControl('', []);

    readonly habitosNocivos = new FormControl(false, []);

    readonly consumeAlcohol = new FormControl(false, []);

    readonly edadConsumeAlcohol = new FormControl('', []);

    readonly consumeTabaco = new FormControl(false, []);

    readonly edadConsumeTabaco = new FormControl('', []);

    errorMessageLesiones = signal('');
    errorMessageEnfermedades = signal('');
    errorMessageMedicamentos = signal('');
    errorMessageInstitucionAcude = signal('');
    errorMessageInternadoHospital = signal('');

    // #endregion

    // #region Formulario ingreso a centros
    ingresoCentrosForm: UntypedFormGroup;

    readonly formFieldCentro = new FormControl('', [
        Validators.required,
        this.noSoloEspaciosValidator,
    ]);
    readonly formFieldMotivo = new FormControl('', [Validators.required]);
    readonly formFieldFechaIngreso = new FormControl('', [Validators.required]);
    readonly formFieldFechaEgreso = new FormControl('', [Validators.required]);

    errorMessageCentro = signal('');

    // #endregion

    // #region Formulario antecedente
    antecedentesForm: UntypedFormGroup;

    readonly formFieldParentesco = new FormControl('', [Validators.required]);
    readonly formFieldEnfermedad = new FormControl('', [Validators.required]);
    // #endregion

    keyLabelsAntecedentes: any = {
        acciones: 'Acciones',
        enfermedad: 'Enfermedad',
        parentesco: 'Parentesco',
    };

    keyLabelsCentros: any = {
        acciones: 'Acciones',
        centro: 'Centro',
        motivo: 'Motivo',
        fechaIngreso: 'Fecha de ingreso',
        fechaEgreso: 'Fecha de egreso',
    };

    keyLabelsTablePersonas: any = {
        nombre: 'Nombre completo',
        parentesco: 'Parentesco',
        indentificacion: 'Identificación',
        enfermo: 'Enfermo',
    };
    listSizePersonas = [5, 10, 15, 20];
    pagePersonas = 0;
    sizePersonas = this.listSizePersonas[0];
    totalItemsPersonas = 0;
    listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
    dataSourcePersonas: CdkTableDataSourceInput<PersonaRelacionadaDTO>;

    keyLabelsTablePersonasEnfermedad: any = {
        acciones: 'Acciones',
        // nombre: 'Nombre completo',
        parentesco: 'Parentesco',
        nombreEnfermedad: 'Enfermedad',
        detalle: 'Detalle',
        activo: 'Enfermedad activa',
    };

    listSizeEnfermedadPersonas = [5, 10, 15, 20];
    pageEnfermedadPersonas = 0;
    sizeEnfermedadPersonas = this.listSizePersonas[0];
    totalItemsEnfermedadPersonas = 0;
    listaEnfermedadPersonasRelacionadas: PersonaRelacionadaEnfermedadDTO[] = [];
    dataSourceEnfermedadPersonas: CdkTableDataSourceInput<PersonaRelacionadaEnfermedadDTO>;

    @ViewChild('tablaEnfermedadPersonas')
    tableEnfermedadesPersonas: MatTable<PersonaRelacionadaEnfermedadDTO>;

    listaEnfermedadPersonaEliminar: string[] = [];

    listSizeEnfermedadFicha = [5, 10, 15, 20];
    pageEnfermedadFicha = 0;
    sizeEnfermedadFicha = this.listSizeEnfermedadFicha[0];
    totalItemsEnfermedadFicha = 0;
    listaEnfermedadFicha: FichaMedicaEnfermedadDTO[] = [];
    dataSourceEnfermedadFicha: CdkTableDataSourceInput<FichaMedicaEnfermedadDTO>;

    @ViewChild('tablaEnfermedadFicha')
    tableEnfermedadesFicha: MatTable<FichaMedicaEnfermedadDTO>;

    listaEnfermedadFichaEliminar: string[] = [];

    keyLabelsTableEnfermedadFicha: any = {
        acciones: 'Acciones',
        nombreEnfermedad: 'Enfermedad',
        detalle: 'Detalle',
        activo: 'Enfermedad activa',
        edadPresente: 'Edad presencia',
        fecha: 'Fecha aparición',
    };

    bloquearToggle = false;

    step1FormGroup: FormGroup;
    step1FormCompleted = false;

    fichaMedicaForm = this._formBuilder.group({
        tipoSangre: this.formFieldTipoSangre,
        // lesiones: this.formFieldLesiones,
        // enfermedades: this.formFieldEnfermedades,
        // medicamentos: this.formFieldMedicamentos,
        // seguro: new FormControl(''),
        // institucionAcude: this.formFieldInstitucionAcude,
        // internadoHospital: this.formFieldInternadoHospital,
        alergiaMedicamentos: this.alergiaMedicamentos,
        detalleAlergias: this.detalleAlergias,
        alergiaAlimentos: this.alergiaAlimentos,
        detalleAlergiasAlimentos: this.detalleAlergiasAlimentos,
        cirugiaQuirurgica: this.cirugiaQuirurgica,
        detalleCirugias: this.detalleCirugias,
        fractura: this.fractura,
        detalleFracturas: this.detalleFracturas,
        irs: this.irs,
        icd: this.icd,
        usoPreservativo: this.usoPreservativo,
        relacionGenero: this.relacionGenero,
        drogaInicio: this.drogaInicio,
        habitosNocivos: this.habitosNocivos,
        consumeAlcohol: this.consumeAlcohol,
        edadConsumeAlcohol: this.edadConsumeAlcohol,
        consumeTabaco: this.consumeTabaco,
        edadConsumeTabaco: this.edadConsumeTabaco,
        // peso: new FormControl('', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]),
        // talla: new FormControl('', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]),
        estadoSalud: new FormControl('', []),
        aspectoGeneralFisico: new FormControl('', []),
        inspeccion: new FormControl('', []),
        pielFaneras: new FormControl('', []),
        // indiceMasaCorporal: new FormControl('', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]),
        // presion: new FormControl('', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]),
        // saturacionOxigeno: new FormControl('', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]),
        cabezaDetalle: new FormControl('', []),
        ojosDetalle: new FormControl('', []),
        oidoDetalle: new FormControl('', []),
        narizDetalle: new FormControl('', []),
        bocaDetalle: new FormControl('', []),
        orofaringeDetalle: new FormControl('', []),
        corazonDetalle: new FormControl('', []),
        pulmonesDetalle: new FormControl('', []),
        abdomenDetalle: new FormControl('', []),
        urinarioDetalle: new FormControl('', []),
        pplDetalle: new FormControl('', []),
        pruDetalle: new FormControl('', []),
        impresionDiagnostico: new FormControl('', []),
    });

    selectedTabIndex = 0;
    mostrarListado = true;
    mostrarListadoConsultas = true;
    base64Image: string | null = null;
    centro: JerarquiaDTO;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private _catalogoService: CatalogoService,
        private _evaluacionMedicaService: EvaluacionMedicaService,
        private readonly _formBuilder: UntypedFormBuilder,
        private readonly customSnackbar: SnackbarService,
        private readonly accionesSheet: MatBottomSheet,
        private readonly changeDetector: ChangeDetectorRef,
        private readonly _fuseConfirmationService: FuseConfirmationService,
        private _datosFamiliaresService: DatosFamiliaresService,
        private _dialogMensajeService: DialogMensajeService,
        private _enfermedadPersonasRelacionada: EnfermedadPersonaRelacionadaService,
        public dialog: MatDialog,
        public funcionesUtils: FuncionesUtils,
        @Inject(LOCALE_ID) private locale: string,
        private fichaIdentificacionService: FichaIdentificacionService,
        public pdfService: PdfService,
        private http: HttpClient,
        private jerarquiaService: JerarquiaService,
        private authSerguridadServicio: AuthSerguridadServicio,
    ) {
        merge(
            this.formFieldLesiones.statusChanges,
            this.formFieldLesiones.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageLesiones());

        merge(
            this.formFieldEnfermedades.statusChanges,
            this.formFieldEnfermedades.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageEnfermedades());

        merge(
            this.formFieldMedicamentos.statusChanges,
            this.formFieldMedicamentos.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageMedicamentos());

        merge(
            this.formFieldInstitucionAcude.statusChanges,
            this.formFieldInstitucionAcude.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageInstitucionAcude());

        merge(
            this.formFieldInternadoHospital.statusChanges,
            this.formFieldInternadoHospital.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageInternadoHospital());

        merge(
            this.formFieldCentro.statusChanges,
            this.formFieldCentro.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageCentro());

        this.formFieldSeguroMedico?.valueChanges.subscribe((value) => {
            if (value === 'otro') {
                this.formFieldOtroSeguro.enable();
            } else {
                this.formFieldOtroSeguro?.disable();
                this.formFieldOtroSeguro?.reset();
            }
            this.actualizarCampoSeguro();
        });

        this.formFieldOtroSeguro.valueChanges.subscribe(() => {
            this.actualizarCampoSeguro();
        });
    }

    async ngOnInit(): Promise<void> {
        await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
            etiquetasModel.NEMONICO_MENU_HISTORIA_CLINICA,
        );

        this.obtenerPermisos();

        this.cargarCentro();
        this.step1FormGroup = this._formBuilder.group({
            control1: ['', Validators.required],
        });
        
        this.uuid_fp = this.route.snapshot.params['uuid_fp'];
        this.fichaIdentificacion = await this.obtenerFichaIdentificacion();

        await this.obtenerFichaMedica();

        this.getCatalogoTiposSangre();
        // await this.obtenerPersonasRelacionadas();
        await this.cargarCatalogos();

        //Setea el form de ficha médica

        this.fichaMedicaForm.get('cirugiaQuirurgica').setValue(true);
        this.fichaMedicaForm.get('cirugiaQuirurgica').updateValueAndValidity();
        this.detalleAlergiasAlimentos.disable();
        this.detalleAlergias.disable();
        this.detalleCirugias.disable();
        this.detalleFracturas.disable();
        this.fichaMedicaForm.updateValueAndValidity();

        if (!this.tienePermiso('MENU_HC_FICHA_SALUD', 'editar')) {
            this.fichaMedicaForm.disable();
        }

        //Setea el form de ingreso a centros
        this.ingresoCentrosForm = this._formBuilder.group({
            centro: this.formFieldCentro,
            motivo: this.formFieldMotivo,
            fechaIngreso: this.formFieldFechaIngreso,
            fechaEgreso: this.formFieldFechaEgreso,
        });

        //Setea el form de antecedentes
        this.antecedentesForm = this._formBuilder.group({
            parentesco: this.formFieldParentesco,
            enfermedad: this.formFieldEnfermedad,
        });
        this.loadImageAsBase64();
    }

    async getCatalogoTiposSangre() {
        this.getCatalogos(this.nemonicoTiposSangre).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosTipoSangre = catalogos;
            }
        );
    }

    compareCatalogo(catalogoA: CatalogoDTO, catalogoB: CatalogoDTO): boolean {
        if (!catalogoA || !catalogoB) {
            return catalogoA === catalogoB;
        }

        return catalogoA.tokenIdentificador === catalogoB.tokenIdentificador;
    }

    async getCatalogoMotivo() {
        this.getCatalogos(this.nemonicoMotivo).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosMotivo = catalogos;
            }
        );
    }

    async getCatalogoParentezco() {
        this.getCatalogos(this.nemonicoParentezco).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosParentezco = catalogos;
            }
        );
    }

    async getCatalogoEnfermedad() {
        this.getCatalogos(this.nemonicoEnfermedad).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosEnfermedad = catalogos;
            }
        );
    }

    getCatalogos(nemonico: string): Observable<CatalogoDTO[]> {
        return this._catalogoService.getCatalogosPorNemonicPadre(nemonico,this.nemonicoMenu).pipe(
            map((response: RespuestaPorDefecto<List<CatalogoDTO>>) => {
                if (!response.exito) {
                    this._catalogoService.checkError(response);
                    return [];
                }
                return Array.from(response.data);
            }),
            catchError((error: any) => {
                this._catalogoService.checkError(error);
                return of([]);
            })
        );
    }

    private actualizarCampoSeguro() {
        const valorSeguro =
            this.formFieldSeguroMedico.value === 'otro'
                ? this.formFieldOtroSeguro.value
                : this.formFieldSeguroMedico.value;

        this.fichaMedicaForm.get('seguro')?.setValue(valorSeguro);
    }

    async cargarCatalogos() {
        this.funcionesUtils
            .obtenerListaCatalogo('ORIENTACION_SEXUAL', '')
            .subscribe({
                next: (data) => {
                    this.listaTipoGenero = data;
                },
                error: (error) =>
                    console.error(
                        'Error cargando grados de instrucción:',
                        error
                    ),
            });
    }

    // #region Peticiones al back

    async crearFichaMedica() {
        Object.keys(this.fichaMedicaForm.controls).forEach((key) => {
            const control = this.fichaMedicaForm.get(key);
        });
        if (this.fichaMedicaForm.valid) {
            this.isLoading = true;
            const fichaMedica: FichaMedicaDTO = {
                tokenIdFichaIdentificacion: this.uuid_fp,
                tipoSangre: this.fichaMedicaForm.get('tipoSangre')?.value,
                estadoSalud: this.fichaMedicaForm.get('estadoSalud')?.value,
                // lesiones: this.fichaMedicaForm.get('lesiones')?.value,
                // enfermedades: this.fichaMedicaForm.get('enfermedades')?.value,
                // medicamentos: this.fichaMedicaForm.get('medicamentos')?.value,
                // institucionAcude:
                //     this.fichaMedicaForm.get('institucionAcude')?.value,
                // internadoHospital:
                //     this.fichaMedicaForm.get('internadoHospital')?.value,
                // seguroMedico: this.fichaMedicaForm.get('seguro')?.value,
                enfermedadesPersonasRelacionada:
                    this.listaEnfermedadPersonasRelacionadas,
                tokensEnfermedadEliminar: this.listaEnfermedadPersonaEliminar,
                enfermedadesRelacionadas: this.listaEnfermedadFicha,
                tokensEnfermedadesFichaEliminar:
                    this.listaEnfermedadFichaEliminar,
                alergiaMedicamentos: this.fichaMedicaForm.get(
                    'alergiaMedicamentos'
                )?.value,
                medicamentosAlergicos:
                    this.fichaMedicaForm.get('detalleAlergias')?.value,
                alergiaAlimentos: this.fichaMedicaForm.get(
                    'alergiaAlimentos'
                )?.value,
                detalleAlergiasAlimentos:
                    this.fichaMedicaForm.get('detalleAlergiasAlimentos')?.value,
                cirugiaQuirurgica:
                    this.fichaMedicaForm.get('cirugiaQuirurgica')?.value,
                detalleCirugias:
                    this.fichaMedicaForm.get('detalleCirugias')?.value,
                fracturas: this.fichaMedicaForm.get('fractura')?.value,
                detalleFracturas:
                    this.fichaMedicaForm.get('detalleFracturas')?.value,
                irs: this.fichaMedicaForm.get('irs')?.value,
                usoDePreservativo:
                    this.fichaMedicaForm.get('usoPreservativo')?.value,
                relacionGenero:
                    this.fichaMedicaForm.get('relacionGenero')?.value,
                icd: this.fichaMedicaForm.get('icd')?.value,
                drogaInicio: this.fichaMedicaForm.get('drogaInicio')?.value,
                habitosNocivos:
                    this.fichaMedicaForm.get('habitosNocivos')?.value,
                tomaAlcohol: this.fichaMedicaForm.get('consumeAlcohol')?.value,
                tabaco: this.fichaMedicaForm.get('consumeTabaco')?.value,
                edadAlcohol:
                    this.fichaMedicaForm.get('edadConsumeAlcohol')?.value,
                edadTabaco:
                    this.fichaMedicaForm.get('edadConsumeTabaco')?.value,
                // peso: this.fichaMedicaForm.get('peso')?.value,
                // talla: this.fichaMedicaForm.get('talla')?.value,
                aspectoGeneralFisico: this.fichaMedicaForm.get(
                    'aspectoGeneralFisico'
                )?.value,
                inspeccion: this.fichaMedicaForm.get('inspeccion')?.value,
                pielFaneras: this.fichaMedicaForm.get('pielFaneras')?.value,
                // indiceMasaCorporal: this.fichaMedicaForm.get('indiceMasaCorporal')?.value,
                // saturacionOxigeno: this.fichaMedicaForm.get('saturacionOxigeno')?.value,
                // presion: this.fichaMedicaForm.get('presion')?.value
                cabezaDetalle: this.fichaMedicaForm.get('cabezaDetalle')?.value,
                ojosDetalle: this.fichaMedicaForm.get('ojosDetalle')?.value,
                oidoDetalle: this.fichaMedicaForm.get('oidoDetalle')?.value,
                narizDetalle: this.fichaMedicaForm.get('narizDetalle')?.value,
                bocaDetalle: this.fichaMedicaForm.get('bocaDetalle')?.value,
                orofaringeDetalle:
                    this.fichaMedicaForm.get('orofaringeDetalle')?.value,
                corazonDetalle:
                    this.fichaMedicaForm.get('corazonDetalle')?.value,
                pulmonesDetalle:
                    this.fichaMedicaForm.get('pulmonesDetalle')?.value,
                abdomenDetalle:
                    this.fichaMedicaForm.get('abdomenDetalle')?.value,
                urinarioDetalle:
                    this.fichaMedicaForm.get('urinarioDetalle')?.value,
                pplDetalle: this.fichaMedicaForm.get('pplDetalle')?.value,
                pruDetalle: this.fichaMedicaForm.get('pruDetalle')?.value,
                impresionDiagnostico: this.fichaMedicaForm.get(
                    'impresionDiagnostico'
                )?.value,
            };

            this._evaluacionMedicaService
                .postFichaMedica(fichaMedica, etiquetasModel.MENU_FICHA_HISTORIA_CLINICA)
                .subscribe({
                    next: (response) => {
                        this.customSnackbar.show(
                            'Ficha médica creada con éxito',
                            'Cerrar',
                            'success'
                        );
                        this.obtenerFichaMedica();
                    },
                    error: (err) => {
                        this.customSnackbar.show(
                            'No se pudo crear',
                            'Cerrar',
                            'error'
                        );
                    },
                    complete: () => {
                        this.isLoading = false;
                    },
                });
        }
    }

    async crearIngresoCentro() {
        if (this.ingresoCentrosForm.valid) {
            this.isLoading = true;
            const ingresoCentro: IngresoCentroJuvenilDTO = {
                tokenIdFichaIdentificacion: this.uuid_fp,
                motivo: this.ingresoCentrosForm.get('motivo')?.value,
                centro: this.ingresoCentrosForm.get('centro')?.value,
                fechaIngreso:
                    this.ingresoCentrosForm.get('fechaIngreso')?.value,
                fechaEgreso: this.ingresoCentrosForm.get('fechaEgreso')?.value,
            };

            this._evaluacionMedicaService
                .postIngresoCentroJuvenil(ingresoCentro)
                .subscribe({
                    next: async (response) => {
                        this.customSnackbar.show(
                            'Ingreso a centro creado con éxito',
                            'Cerrar',
                            'success'
                        );
                        await this.obtenerIngresoCentros();
                    },
                    error: (err) => {
                        this.customSnackbar.show(
                            'No se pudo crear',
                            'Cerrar',
                            'error'
                        );
                    },
                    complete: () => {
                        this.isLoading = false;
                    },
                });
        }
    }

    async crearAntecedente() {
        if (this.antecedentesForm.valid) {
            this.isLoading = true;
            const antecedente: AntecedenteFamiliarDTO = {
                tokenIdFichaIdentificacion: this.uuid_fp,
                enfermedad: this.antecedentesForm.get('enfermedad')?.value,
                parentesco: this.antecedentesForm.get('parentesco')?.value,
            };

            this._evaluacionMedicaService
                .postAntecedenteFamiliar(antecedente)
                .subscribe({
                    next: (response) => {
                        this.customSnackbar.show(
                            'Antecedente familiar creado con éxito',
                            'Cerrar',
                            'success'
                        );
                        this.obtenerAntecedentes();
                    },
                    error: (err) => {
                        this.customSnackbar.show(
                            'No se pudo crear',
                            'Cerrar',
                            'error'
                        );
                    },
                    complete: () => {
                        this.isLoading = false;
                    },
                });
        }
    }

    async obtenerFichaMedica() {
        this.isLoading = true;
        this._evaluacionMedicaService
            .getFichaMedicaByFichaIden(this.uuid_fp, etiquetasModel.MENU_FICHA_HISTORIA_CLINICA)
            .subscribe({
                next: (response: RespuestaPorDefecto<FichaMedicaDTO>) => {
                    if (!response.exito) {
                        if (!response.data) {
                            this._evaluacionMedicaService.checkError(response);
                            return;
                        }
                    }
                    this.fichaMedica = response.data;
                    if (this.fichaMedica) {

                        this.fichaMedicaForm.patchValue({
                            estadoSalud: 
                                this.fichaMedica.estadoSalud ?? 
                                this.fichaMedica?.estadoSalud,
                            tipoSangre:
                                this.fichaMedica?.tipoSangre ??
                                this.fichaMedica.tipoSangre,
                            formFieldTipoSangre:
                                this.fichaMedica?.tipoSangre ??
                                this.fichaMedica.tipoSangre,
                            irs: this.fichaMedica.irs ?? this.fichaMedica?.irs,
                            icd: this.fichaMedica.icd ?? this.fichaMedica.icd,
                            alergiaMedicamentos: this.fichaMedica
                                .alergiaMedicamentos
                                ? this.fichaMedica.alergiaMedicamentos
                                : false,
                            alergiaAlimentos: this.fichaMedica
                                .alergiaAlimentos
                                ? this.fichaMedica.alergiaAlimentos
                                : false,
                            cirugiaQuirurgica: this.fichaMedica
                                .cirugiaQuirurgica
                                ? this.fichaMedica.cirugiaQuirurgica
                                : false,
                            fractura: this.fichaMedica.fracturas
                                ? this.fichaMedica.fracturas
                                : false,
                            relacionGenero:
                                this.fichaMedica.relacionGenero ??
                                this.fichaMedica.relacionGenero,
                            usoPreservativo:
                                this.fichaMedica.usoDePreservativo ??
                                this.fichaMedica.usoDePreservativo,
                            habitosNocivos:
                                this.fichaMedica.habitosNocivos ??
                                this.fichaMedica.habitosNocivos,
                            consumeAlcohol:
                                this.fichaMedica.tomaAlcohol ??
                                this.fichaMedica.tomaAlcohol,
                            consumeTabaco:
                                this.fichaMedica.tabaco ??
                                this.fichaMedica.tabaco,
                            aspectoGeneralFisico:
                                this.fichaMedica.aspectoGeneralFisico ??
                                this.fichaMedica.aspectoGeneralFisico,
                            inspeccion:
                                this.fichaMedica.inspeccion ??
                                this.fichaMedica.inspeccion,
                            pielFaneras:
                                this.fichaMedica.pielFaneras ??
                                this.fichaMedica.pielFaneras,
                            detalleAlergias:
                                this.fichaMedica.medicamentosAlergicos ??
                                this.fichaMedica.medicamentosAlergicos,                            
                            detalleAlergiasAlimentos:
                                this.fichaMedica.detalleAlergiasAlimentos ??
                                this.fichaMedica.detalleAlergiasAlimentos,
                            detalleCirugias:
                                this.fichaMedica.detalleCirugias ??
                                this.fichaMedica.detalleCirugias,
                            detalleFracturas:
                                this.fichaMedica.detalleFracturas ??
                                this.fichaMedica.detalleFracturas,
                            drogaInicio:
                                this.fichaMedica.drogaInicio ??
                                this.fichaMedica.drogaInicio,
                            edadConsumeAlcohol:
                                this.fichaMedica.edadAlcohol ??
                                this.fichaMedica.edadAlcohol,
                            edadConsumeTabaco:
                                this.fichaMedica.edadTabaco ??
                                this.fichaMedica.edadTabaco,
                            cabezaDetalle:
                                this.fichaMedica.cabezaDetalle ??
                                this.fichaMedica.cabezaDetalle,
                            ojosDetalle:
                                this.fichaMedica.ojosDetalle ??
                                this.fichaMedica.ojosDetalle,
                            oidoDetalle:
                                this.fichaMedica.oidoDetalle ??
                                this.fichaMedica.oidoDetalle,
                            narizDetalle:
                                this.fichaMedica.narizDetalle ??
                                this.fichaMedica.narizDetalle,
                            bocaDetalle:
                                this.fichaMedica.bocaDetalle ??
                                this.fichaMedica.bocaDetalle,
                            orofaringeDetalle:
                                this.fichaMedica.orofaringeDetalle ??
                                this.fichaMedica.orofaringeDetalle,
                            corazonDetalle:
                                this.fichaMedica.corazonDetalle ??
                                this.fichaMedica.corazonDetalle,
                            pulmonesDetalle:
                                this.fichaMedica.pulmonesDetalle ??
                                this.fichaMedica.pulmonesDetalle,
                            abdomenDetalle:
                                this.fichaMedica.abdomenDetalle ??
                                this.fichaMedica.abdomenDetalle,
                            urinarioDetalle:
                                this.fichaMedica.urinarioDetalle ??
                                this.fichaMedica.urinarioDetalle,
                            pplDetalle:
                                this.fichaMedica.pplDetalle ??
                                this.fichaMedica.pplDetalle,
                            pruDetalle:
                                this.fichaMedica.pruDetalle ??
                                this.fichaMedica.pruDetalle,
                            impresionDiagnostico:
                                this.fichaMedica.impresionDiagnostico ??
                                this.fichaMedica.impresionDiagnostico,
                        });

                        if (!this.fichaMedica.habitosNocivos) {
                            this.fichaMedicaForm
                                .get('consumeAlcohol')
                                .disable();
                            this.fichaMedicaForm.get('consumeTabaco').disable();
                        }
                        if (!this.fichaMedica.tomaAlcohol) {
                            this.fichaMedicaForm
                                .get('edadConsumeAlcohol')
                                .disable();
                        }
                        if (!this.fichaMedica.tabaco) {
                            this.fichaMedicaForm
                                .get('edadConsumeTabaco')
                                .disable();
                        }
                        if (this.fichaMedica.alergiaMedicamentos) {
                            this.detalleAlergias.enable();
                        }
                        if (this.fichaMedica.alergiaAlimentos) {
                            this.detalleAlergiasAlimentos.enable();
                        }
                        if (this.fichaMedica.fracturas) {
                            this.detalleFracturas.enable();
                        }
                        if (this.fichaMedica.cirugiaQuirurgica) {
                            this.detalleCirugias.enable();
                        }

                        this.changeDetector.detectChanges();
                        if (this.fichaMedica.tokenIdentificador) {
                            this._evaluacionMedicaService.setToken(
                                this.fichaMedica.tokenIdentificador
                            );
                            this.completeStep1();
                        }

                        if (this.fichaMedica.tokenIdFichaIdentificacion) {
                            this.obtenerEnfermedadesPersonasRelacionadas(
                                this.uuid_fp
                            );
                            this.obtenerEnfermedadesFichas(
                                this.fichaMedica.tokenIdFichaIdentificacion
                            );
                        }
                    }
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
                complete: () => {
                    if (this.fichaMedica.tokenIdentificador) {
                        this._evaluacionMedicaService.setToken(
                            this.fichaMedica.tokenIdentificador
                        );
                    }

                    //this.irSeguimiento();
                    this.isLoading = false;
                },
            });
    }

    async obtenerIngresoCentros() {
        this.isLoading = true;
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeCentros;
        paginacionRequest.page = this.pageCentros;
        paginacionRequest.tokenIdentificador = this.uuid_fp;

        this._evaluacionMedicaService
            .getIngresoCentroJuvenilByFichaMedica(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<IngresoCentroJuvenilDTO>
                    >
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }

                    this.ingresosCentros = response.data.data;
                    this.dataSourceCentros.data = this.ingresosCentros;
                    this.isLoading = false;
                    this.changeDetector.detectChanges();
                    this.totalItemsCentros = response.data.totalItems;
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
            });
    }

    async obtenerAntecedentes() {
        this.isLoading = true;
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeAntecedentes;
        paginacionRequest.page = this.pageAntecedentes;
        paginacionRequest.tokenIdentificador = this.uuid_fp;

        this._evaluacionMedicaService
            .getAntecedenteFamiliarByFichaMedica(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<AntecedenteFamiliarDTO>
                    >
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }

                    this.antecedentes = response.data.data;
                    this.dataSourceAntecedentes.data = this.antecedentes;
                    this.isLoading = false;
                    this.changeDetector.detectChanges();
                    this.totalItemsAntecedentes = response.data.totalItems;
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
            });
    }

    async actualizarCentro() { }

    async actualizarAntecedente() { }

    async eliminarCentro(centro: IngresoCentroJuvenilDTO) {
        const confirmation = this._fuseConfirmationService.open({
            title: 'Eliminar registro',
            message: '¿Estás seguro de eliminar este registro?',
            actions: {
                confirm: {
                    label: 'Eliminar',
                },
                cancel: {
                    label: 'Cancelar',
                },
            },
        });

        confirmation.afterClosed().subscribe((result) => {
            if (result === 'confirmed') {
                this._evaluacionMedicaService
                    .deleteIngresoCentroJuvenil(centro)
                    .subscribe({
                        next: (response) => {
                            this.obtenerIngresoCentros();

                            this.customSnackbar.show(
                                'Registro eliminado con exito',
                                'Cerrar',
                                'success'
                            );
                        },
                        error: (err) => {
                            this.customSnackbar.show(
                                'No se pudo eliminar',
                                'Cerrar',
                                'error'
                            );
                        },
                    });
            }
        });
    }

    async eliminarAntecedente(antecedente: AntecedenteFamiliarDTO) {
        const confirmation = this._fuseConfirmationService.open({
            title: 'Eliminar registro',
            message: '¿Estás seguro de eliminar este registro?',
            actions: {
                confirm: {
                    label: 'Eliminar',
                },
                cancel: {
                    label: 'Cancelar',
                },
            },
        });

        confirmation.afterClosed().subscribe((result) => {
            if (result === 'confirmed') {
                this._evaluacionMedicaService
                    .deleteAntecedenteFamiliar(antecedente)
                    .subscribe({
                        next: (response) => {
                            this.obtenerAntecedentes();

                            this.customSnackbar.show(
                                'Registro eliminado con exito',
                                'Cerrar',
                                'success'
                            );
                        },
                        error: (err) => {
                            this.customSnackbar.show(
                                'No se pudo eliminar',
                                'Cerrar',
                                'error'
                            );
                        },
                    });
            }
        });
    }

    // #endregion

    // #region Validadores

    //Validador para no permitir solo espacios
    noSoloEspaciosValidator(control: UntypedFormControl) {
        if (control.value.trim() === '') {
            return { onlySpaces: true };
        }
        return null;
    }

    // #endregion

    // #region Mensajes de error
    updateErrorMessageLesiones() {
        if (this.formFieldLesiones.hasError('required')) {
            this.errorMessageLesiones.set('El campo es obligatorio');
        } else if (this.formFieldLesiones.hasError('maxlength')) {
            this.errorMessageLesiones.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldLesiones.hasError('onlySpaces')) {
            this.errorMessageLesiones.set('No puede contener solo espacios');
        } else {
            this.errorMessageLesiones.set('');
        }
    }

    updateErrorMessageEnfermedades() {
        if (this.formFieldEnfermedades.hasError('required')) {
            this.errorMessageEnfermedades.set('El campo es obligatorio');
        } else if (this.formFieldEnfermedades.hasError('maxlength')) {
            this.errorMessageEnfermedades.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldEnfermedades.hasError('onlySpaces')) {
            this.errorMessageEnfermedades.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageEnfermedades.set('');
        }
    }

    updateErrorMessageMedicamentos() {
        if (this.formFieldMedicamentos.hasError('required')) {
            this.errorMessageMedicamentos.set('El campo es obligatorio');
        } else if (this.formFieldMedicamentos.hasError('maxlength')) {
            this.errorMessageMedicamentos.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldMedicamentos.hasError('onlySpaces')) {
            this.errorMessageMedicamentos.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageMedicamentos.set('');
        }
    }

    updateErrorMessageInstitucionAcude() {
        if (this.formFieldInstitucionAcude.hasError('required')) {
            this.errorMessageInstitucionAcude.set('El campo es obligatorio');
        } else if (this.formFieldInstitucionAcude.hasError('maxlength')) {
            this.errorMessageInstitucionAcude.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldInstitucionAcude.hasError('onlySpaces')) {
            this.errorMessageInstitucionAcude.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageInstitucionAcude.set('');
        }
    }

    updateErrorMessageInternadoHospital() {
        if (this.formFieldInternadoHospital.hasError('required')) {
            this.errorMessageInternadoHospital.set('El campo es obligatorio');
        } else if (this.formFieldInternadoHospital.hasError('maxlength')) {
            this.errorMessageInternadoHospital.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldInternadoHospital.hasError('onlySpaces')) {
            this.errorMessageInternadoHospital.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageInternadoHospital.set('');
        }
    }

    updateErrorMessageCentro() {
        if (this.formFieldCentro.hasError('required')) {
            this.errorMessageCentro.set('El campo es obligatorio');
        } else if (this.formFieldCentro.hasError('onlySpaces')) {
            this.errorMessageCentro.set('No puede contener solo espacios');
        } else {
            this.errorMessageCentro.set('');
        }
    }

    // #endregion

    datosInicialesIngresados(): void {
        this.datosPrincipales = !this.datosPrincipales;
    }

    toggleFormCentro(): void {
        this.getCatalogoMotivo();
        this.showFormCentro = !this.showFormCentro;
    }

    toggleFormAntecedente(): void {
        this.getCatalogoParentezco();
        this.getCatalogoEnfermedad();
        this.showFormAntecedente = !this.showFormAntecedente;
    }

    toggleSeguimiento(): void {
        this.showSeguimiento = !this.showSeguimiento;
    }

    toggleLesiones(): void {
        this.showLesiones = !this.showLesiones;
    }

    toggleEnfermedades(): void {
        this.showEnfermedades = !this.showEnfermedades;
    }

    toggleMedicamentos(): void {
        this.showMedicamentos = !this.showMedicamentos;
    }

    toggleListado(): void {
        this.showListado = !this.showListado;
    }

    toggleAlergias(): void {
        if (this.fichaMedicaForm.get('alergiaMedicamentos').value) {
            this.detalleAlergias.enable();
        } else {
            this.detalleAlergias.disable();
        }
    }

    toggleAlergiasAlimentos(): void {
        if (this.fichaMedicaForm.get('alergiaAlimentos').value) {
            this.detalleAlergiasAlimentos.enable();
        } else {
            this.detalleAlergiasAlimentos.disable();
        }
    }

    toggleCirugias(): void {
        if (this.fichaMedicaForm.get('cirugiaQuirurgica').value) {
            this.detalleCirugias.enable();
        } else {
            this.detalleCirugias.disable();
        }
    }

    toggleFracturas(): void {
        if (this.fichaMedicaForm.get('fractura').value) {
            this.detalleFracturas.enable();
        } else {
            this.detalleFracturas.disable();
        }
    }

    toggleHabitosNocivos() {
        if (this.fichaMedicaForm.get('habitosNocivos').value) {
            this.consumeAlcohol.enable();
            this.consumeTabaco.enable();
        } else {
            this.consumeAlcohol.disable();
            this.consumeTabaco.disable();
        }
    }

    toggleAlcohol() {
        if (this.fichaMedicaForm.get('consumeAlcohol').value) {
            this.edadConsumeAlcohol.enable();
        } else {
            this.edadConsumeAlcohol.disable();
        }
    }

    toggleTabaco() {
        if (this.fichaMedicaForm.get('consumeTabaco').value) {
            this.edadConsumeTabaco.enable();
        } else {
            this.edadConsumeTabaco.disable();
        }
    }

    activarAcciones(tipoObjeto: 'centro' | 'antecedentes', objeto: any) {
        const accionesData: AccionesCatalogo = {
            mostrar: true,
            subCatalogo: false,
            textAccion: '',
            keyAccion: '',
        };

        let ref = this.accionesSheet.open(AccionesCatalogoComponent, {
            data: accionesData,
        });

        ref.afterDismissed().subscribe({
            next: (result: 'editar' | 'eliminar') => {
                // if (result === "editar") {
                //   if (tipoObjeto === 'centro') {
                //     this.editarCentro(objeto);
                //   } else if (tipoObjeto === 'antecedentes') {
                //     this.editarAntecedente(objeto);
                //   }
                // } else if (result === "eliminar") {
                //   if (tipoObjeto === 'centro') {
                //     this.eliminarCentro(objeto);
                //   } else if (tipoObjeto === 'antecedentes') {
                //     this.eliminarAntecedente(objeto);
                //   }
                // }
            },
        });
    }

    getFormatedDate(date: Date) {
        return moment(date, 'YYYY-MM-DDTHH:mm:ssZ').toDate().toLocaleString();
    }

    getKeysCentros() {
        return Object.keys(this.keyLabelsCentros);
    }

    getKeysAntecedentes() {
        return Object.keys(this.keyLabelsAntecedentes);
    }

    handlePageEventCentros(pageEvent: PageEvent) {
        this.sizeCentros = pageEvent.pageSize;
        this.pageCentros = pageEvent.pageIndex;
        this.obtenerIngresoCentros();
    }

    handlePageEventAntecedentes(pageEvent: PageEvent) {
        this.sizeAntecedentes = pageEvent.pageSize;
        this.pageAntecedentes = pageEvent.pageIndex;
        this.obtenerAntecedentes();
    }

    // async obtenerPersonasRelacionadas() {
    //     let paginacionRequest = new PaginacionRequest();
    //     paginacionRequest.size = this.sizePersonas;
    //     paginacionRequest.page = this.pagePersonas;
    //     paginacionRequest.tokenIdentificador = this.uuid_fp;

    //     this._datosFamiliaresService
    //         .obtenerPersonasRelacionadas(paginacionRequest)
    //         .subscribe({
    //             next: (
    //                 response: RespuestaPorDefecto<
    //                     PaginacionResponse<PersonaRelacionadaDTO>
    //                 >
    //             ) => {
    //                 if (!environment.production) {
    //                     console.log(response);
    //                 }

    //                 if (!response.exito) {
    //                     this._dialogMensajeService.mensajeErrorConTitulo(
    //                         response.titulo,
    //                         response.mensaje
    //                     );
    //                     return;
    //                 }
    //                 this.listaPersonasRelacionadas = response.data.data;
    //                 this.dataSourcePersonas = this.listaPersonasRelacionadas;
    //                 this.totalItemsPersonas = response.data.totalItems;
    //             },
    //             error: (error: any) => {
    //                 console.log(error);
    //             },
    //         });
    // }

    // handlePageEventPersonasRelacionadas(pageEvent: PageEvent) {
    //     this.sizePersonas = pageEvent.pageSize;
    //     this.pagePersonas = pageEvent.pageIndex;
    //     this.obtenerPersonasRelacionadas();
    // }

    getKeysDatosFamiliares() {
        return Object.keys(this.keyLabelsTablePersonas);
    }

    togglePersonaEnfermo(tokenIdentificador: string, valorActual: Boolean) {
        let personaRelacionadaDTO = new PersonaRelacionadaDTO();
        personaRelacionadaDTO.tokenIdentificador = tokenIdentificador;
        personaRelacionadaDTO.enfermo = !valorActual;
        this.bloquearToggle = true;
        this._datosFamiliaresService
            .crearPersonaRelacionada(personaRelacionadaDTO, '', true)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<PersonaRelacionadaDTO>
                ) => {
                    this.bloquearToggle = false;
                    if (!response.exito) {
                        this._datosFamiliaresService.checkError(response);
                        personaRelacionadaDTO.enfermo = valorActual;
                        return;
                    }
                },
                error: (error: any) => {
                    this._datosFamiliaresService.checkError(error);
                    this.bloquearToggle = false;
                    personaRelacionadaDTO.enfermo = valorActual;
                },
            });
    }

    async obtenerEnfermedadesPersonasRelacionadas(tokenIdentificador: string) {
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizePersonas;
        paginacionRequest.page = this.pagePersonas;
        paginacionRequest.tokenIdentificador = tokenIdentificador;

        this._enfermedadPersonasRelacionada
            .obtenerEnfermedadPersonasRelacionadas(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<PersonaRelacionadaEnfermedadDTO>
                    >
                ) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this._dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }
                    this.listaEnfermedadPersonasRelacionadas =
                        response.data.data;
                    this.dataSourceEnfermedadPersonas =
                        this.listaEnfermedadPersonasRelacionadas;
                    this.totalItemsEnfermedadPersonas =
                        response.data.totalItems;
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    getkeyLabelsTablePersonasEnfermedad() {
        return Object.keys(this.keyLabelsTablePersonasEnfermedad);
    }

    handlePageEventEnfermedadesPersonasRelacionadas(pageEvent: PageEvent) {
        this.sizePersonas = pageEvent.pageSize;
        this.pagePersonas = pageEvent.pageIndex;
        this.obtenerEnfermedadesPersonasRelacionadas(this.uuid_fp);
    }

    aniadirFilaInformacion() {
        const dialogRef = this.dialog.open(
            ModalCrearEditarEnfermedadPersonaRelacionadaComponent,
            {
                data: {
                    uuid_fp: this.uuid_fp,
                },
                disableClose: true,
                width: '600px',
            }
        );
        dialogRef.afterClosed().subscribe(async (result) => {
            if (result && !result.esEdicion) {
                const datosActualizados =
                    this.listaEnfermedadPersonasRelacionadas;
                datosActualizados.push(result);
                this.dataSourceEnfermedadPersonas = datosActualizados;
                // this.cd.detectChanges();
                this.tableEnfermedadesPersonas.renderRows();
            }
        });
    }

    borrarEnfermedadPersonaR(id_temp: number, id: string) {
        let ref = this._dialogMensajeService.mensajeConConfirmacion(
            'Estás seguro de eliminar a la enfermedad registrada',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (id) {
                        const datosActualizados =
                            this.listaEnfermedadPersonasRelacionadas.filter(
                                (item) => item.tokenIdentificador !== id
                            );
                        this.dataSourceEnfermedadPersonas = datosActualizados;
                        this.listaEnfermedadPersonasRelacionadas =
                            datosActualizados;

                        this.listaEnfermedadPersonaEliminar.push(id);
                    } else {
                        const datosActualizados =
                            this.listaEnfermedadPersonasRelacionadas.filter(
                                (item) => item.id_temporal !== id_temp
                            );
                        this.listaEnfermedadPersonasRelacionadas =
                            datosActualizados;
                        this.dataSourceEnfermedadPersonas = datosActualizados;
                    }
                }
            },
        });
    }

    verFilaInformacion(informacion: InformacionUbicacionDTO) {
        const dialogRef = this.dialog.open(
            ModalCrearEditarEnfermedadPersonaRelacionadaComponent,
            {
                data: {
                    informacion: informacion,
                    uuid_fp: this.uuid_fp,
                    modoVisualizacion: true,
                },
                disableClose: true,
                width: '600px',
            }
        );        
    }

    editarFilaInformacion(informacion: InformacionUbicacionDTO) {
        const dialogRef = this.dialog.open(
            ModalCrearEditarEnfermedadPersonaRelacionadaComponent,
            {
                data: {
                    informacion: informacion,
                    uuid_fp: this.uuid_fp,
                },
                disableClose: true,
                width: '600px',
            }
        );

        dialogRef.afterClosed().subscribe(async (result) => {
            if (result.esEdicion) {
                // this.obtenerInformacionUbicacion(
                //     this.personaRelacionadaEditando.idPersonaRelacionada
                // );
                const datosActualizados =
                    this.listaEnfermedadPersonasRelacionadas;
                const index =
                    this.listaEnfermedadPersonasRelacionadas.findIndex(
                        (x) => x.tokenIdentificador == result.tokenIdentificador
                    );
                if (index !== -1) {
                    datosActualizados[index] = result;
                    this.listaEnfermedadPersonasRelacionadas[index] = result;
                } else {

                    const index =
                        this.listaEnfermedadPersonasRelacionadas.findIndex(
                            (x) => x.id_temporal == result.id_temporal
                        );
                    if (index !== -1) {
                        datosActualizados[index] = result;
                        this.listaEnfermedadPersonasRelacionadas[index] =
                            result;

                    }
                }
                this.dataSourceEnfermedadPersonas = datosActualizados;
                this.tableEnfermedadesPersonas.renderRows();
            }
        });
    }

    async obtenerEnfermedadesFichas(tokenIdentificador: string) {
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizePersonas;
        paginacionRequest.page = this.pagePersonas;
        paginacionRequest.tokenIdentificador = tokenIdentificador;

        this._evaluacionMedicaService
            .getEnfermedadesAsociadasFicha(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<FichaMedicaEnfermedadDTO>
                    >
                ) => {
                    if (!environment.production) {
                        console.log('respuesta enfermedades ficha', response);
                    }

                    if (!response.exito) {
                        this._dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }
                    this.listaEnfermedadFicha = response.data.data;
                    this.dataSourceEnfermedadFicha = this.listaEnfermedadFicha;
                    this.totalItemsEnfermedadFicha = response.data.totalItems;
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    handlePageEventEnfermedadesFicha(pageEvent: PageEvent) {
        this.sizeEnfermedadFicha = pageEvent.pageSize;
        this.pageEnfermedadFicha = pageEvent.pageIndex;
        this.obtenerEnfermedadesFichas(this.fichaMedica.tokenIdentificador);
    }

    getKeysEnfermedadFicha() {
        return Object.keys(this.keyLabelsTableEnfermedadFicha);
    }

    borrarEnfermedadFicha(id_temp: number, id: string) {
        let ref = this._dialogMensajeService.mensajeConConfirmacion(
            'Estás seguro de eliminar a la enfermedad registrada',
            'Deseas continuar?'
        );

        ref.afterClosed().subscribe({
            next: (resp: 'confirmed' | 'cancelled') => {
                if (resp == 'confirmed') {
                    if (id) {
                        const datosActualizados =
                            this.listaEnfermedadFicha.filter(
                                (item) => item.tokenIdentificador !== id
                            );
                        this.dataSourceEnfermedadFicha = datosActualizados;
                        this.listaEnfermedadFicha = datosActualizados;

                        this.listaEnfermedadFichaEliminar.push(id);
                    } else {
                        const datosActualizados =
                            this.listaEnfermedadFicha.filter(
                                (item) => item.id_temporal !== id_temp
                            );
                        this.listaEnfermedadFicha = datosActualizados;
                        this.dataSourceEnfermedadFicha = datosActualizados;
                    }
                }
            },
        });
    }

    aniadirFilaEnfermedadFicha() {
        const dialogRef = this.dialog.open(
            ModalCrearEditarEnfermedadFichaComponent,
            {
                data: {
                    uuid_fp: this.uuid_fp,
                    sexoFicha: this.fichaIdentificacion.nombreSexo || '',
                },
                width: '600px',
            }
        );
        dialogRef.afterClosed().subscribe(async (result) => {
            if (result && !result.esEdicion) {
                const datosActualizados = this.listaEnfermedadFicha;
                datosActualizados.push(result);
                this.dataSourceEnfermedadFicha = datosActualizados;
                // this.cd.detectChanges();
                this.tableEnfermedadesFicha.renderRows();
            }
        });
    }

    editarFilaEnfermedadFicha(informacion: FichaMedicaEnfermedadDTO) {
        const dialogRef = this.dialog.open(
            ModalCrearEditarEnfermedadFichaComponent,
            {
                data: {
                    informacion: informacion,
                    uuid_fp: this.uuid_fp,
                    sexoFicha: this.fichaIdentificacion.nombreSexo || '',
                },
                width: '600px',
            }
        );

        dialogRef.afterClosed().subscribe(async (result) => {
            if (result.esEdicion) {
                // this.obtenerInformacionUbicacion(
                //     this.personaRelacionadaEditando.idPersonaRelacionada
                // );
                const datosActualizados = this.listaEnfermedadFicha;
                const index = this.listaEnfermedadFicha.findIndex(
                    (x) => x.tokenIdentificador == result.tokenIdentificador
                );
                if (index !== -1) {
                    datosActualizados[index] = result;
                    this.listaEnfermedadFicha[index] = result;
                } else {
                    const index = this.listaEnfermedadFicha.findIndex(
                        (x) => x.id_temporal == result.id_temporal
                    );
                    if (index !== -1) {
                        datosActualizados[index] = result;
                        this.listaEnfermedadFicha[index] = result;
                    }
                }
                this.dataSourceEnfermedadFicha = datosActualizados;
                this.tableEnfermedadesFicha.renderRows();
            }
        });
    }

    completeStep1() {
        this.step1FormCompleted = true;
        this.step1FormGroup.get('control1').setValue('xd');
    }

    ingresoMedidas(event: any): void {
        const decimalSeparator = this.locale === 'es' ? '.' : ',';
        const input = event.target as HTMLInputElement;
        let valor = input.value;
        const patronDecimal = new RegExp(`^[0-9${decimalSeparator}]*$`);
        if (patronDecimal.test(valor)) {
            input.value = parseFloat(valor).toFixed(2).toString();
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

    onTabChange(event: MatTabChangeEvent) {
        if (!this.fichaMedica) {
            // Si intenta cambiar de pestaña y el objeto no existe
            alert('Crea la ficha médica para poder continuar');
            this.selectedTabIndex = 0; // Regresa al primer tab
        }

        this.actualizarPermisos();
    }

    private actualizarPermisos() {
        const uuid = this.route.snapshot.paramMap.get('uuid_fp');
        this.permisosService.obtenerPermisosUsuario('', uuid).subscribe(() => this.obtenerPermisos());
    }

    // Comienza mostrando el listado

    // Cambiar a mostrar el formulario de creación
    mostrarFormularioCreacion() {
        this.mostrarListado = false;
    }

    // Cambiar a mostrar el listado
    mostrarListadoDeNuevo() {
        this.mostrarListado = true;
    }

    async imprimirAntecendentesFamiliares() {
        console.log('imprimiendo');
        try {
            let tablaEnfPersonasRelacionadas = new TablaPlantilla();
            tablaEnfPersonasRelacionadas.encabezados = [
                'Parentesco',
                'Enfermedad',
            ];
            tablaEnfPersonasRelacionadas.filas =
                this.listaEnfermedadPersonasRelacionadas.map((persona) => ({                    
                    Parentesco: persona?.tipoParentesco?.nombre || 'No especificado',
                    Enfermedad: (
                        persona.clasificacionEnfermedad 
                        ? `${persona.clasificacionEnfermedad?.codigo} | ${persona.clasificacionEnfermedad?.nombre}`
                        : persona.nombreEnfermedad
                    ) || 'No especificado',
                }));
            
            let request = new GeneracionPdfRequest();
            request.nemonico = 'FORMULARIO_HISTORIA_CLINICA_ANTEC_FAMILIARES';

            // Obtener datos de cabecera
            const datosCabecera = await this.obtenerDatosCabecera();

            request.variables = {
                '[CENTRO]': this.centro.nombre || '',
                "[TITULO-PLANTILLA]": 'Informe evaluación médica - Antecedentes familiares',
                "[IMG_BASE64]": this.base64Image,
                "[FECHA_REGISTRO]": (new Date()).toISOString().split("T")[0],
                "[HORA_REGISTRO]": (new Date()).toTimeString().split(" ")[0],
                "[TITULO-INFORME]": 'Evaluación Médica',
                ...datosCabecera,
                '[TABLA-ANTECEDENTES-FAMILARES]': JSON.stringify(
                    tablaEnfPersonasRelacionadas
                )                
            };

            this.pdfService
                .generarPdf(
                    request,
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                )
                .subscribe({
                    next: (response: RespuestaPorDefecto<string>) => {
                        if (!response.exito) {
                            this._dialogMensajeService.mensajeError(
                                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                            );
                            return;
                        }

                        const url = window.URL.createObjectURL(
                            this.funcionesUtils.getPdfBlob(response.data)
                        );
                        const pwa = window.open(url);
                    },
                    error: (error: any) => {
                        this._dialogMensajeService.mensajeError(
                            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                        );
                    },
                });
        } catch (error) {
            console.log('error', error);
            this._dialogMensajeService.mensajeError(
                'Hubo un problema al obtener los datos de la cabecera.'
            );
        }
    }

    async imprimirFicha() {
        console.log('imprimiendo');
        try {

            let tablaEnfRelacionadas = new TablaPlantilla();
            tablaEnfRelacionadas.encabezados = [
                'Enfermedad',
                'Detalle',
                'Edad',
            ];
            console.log('tablaEnfRelacionadas', tablaEnfRelacionadas);

            tablaEnfRelacionadas.filas = this.listaEnfermedadFicha.map(
                (persona) => ({
                    Enfermedad: (
                        persona.clasificacionEnfermedad 
                        ? `${persona.clasificacionEnfermedad?.codigo} | ${persona.clasificacionEnfermedad?.nombre}`
                        : persona.nombreEnfermedad
                    ) || 'No especificado',
                    Detalle: persona.detalle || 'No especificado',
                    Edad: persona.edadPresente || 'No especificado',
                })
            );
            let request = new GeneracionPdfRequest();
            request.nemonico = 'FORMULARIO_HISTORIA_CLINICA_FICHA_SALUD';

            // Obtener datos de cabecera
            const datosCabecera = await this.obtenerDatosCabecera();

            request.variables = {
                '[CENTRO]': this.centro.nombre || '',
                "[TITULO-PLANTILLA]": 'Informe evaluación médica',
                "[IMG_BASE64]": this.base64Image,
                "[FECHA_REGISTRO]": (new Date()).toISOString().split("T")[0],
                "[HORA_REGISTRO]": (new Date()).toTimeString().split(" ")[0],
                "[TITULO-INFORME]": 'Evaluación Médica',
                ...datosCabecera,                
                '[TIPO-SANGRE]': this.fichaMedica?.tipoSangre?.nombre || '',
                '[TABLA-ANTECEDENTES-PERSONALES]':
                    JSON.stringify(tablaEnfRelacionadas),
                '[ESTADO-SALUD]': this.fichaMedica?.estadoSalud || '',
                '[ALERGIA-MEDICAMENTOS]': this.fichaMedica?.alergiaMedicamentos
                    ? 'Si'
                    : 'No',
                '[DETALLE-ALERGIA]':
                    this.fichaMedica?.medicamentosAlergicos || '',
                '[ALERGIA-ALIMENTOS]': this.fichaMedica?.alergiaAlimentos
                    ? 'Si'
                    : 'No',
                '[DETALLE-ALERGIA-ALIMENTOS]':
                    this.fichaMedica?.detalleAlergiasAlimentos || '',
                '[CIRUGIAS-QUIRURJICAS]': this.fichaMedica?.cirugiaQuirurgica
                    ? 'Si'
                    : 'No',
                '[FRACTURAS]': this.fichaMedica?.fracturas ? 'Si' : 'No',
                '[DETALLE-CIRUGIAS]': this.fichaMedica?.detalleCirugias || '',
                '[DETALLE-FRACTURAS]': this.fichaMedica?.detalleFracturas || '',
                '[IRS]': this.fichaMedica?.irs || '',
                '[DCI]': this.fichaMedica?.icd || '',
                '[RELACION-GENERO]':
                    this.funcionesUtils.obtenerNombreCatalogoPorToken(
                        this.fichaMedica?.relacionGenero,
                        this.listaTipoGenero
                    ) || '',
                '[USOPRESERVATIVO]': this.fichaMedica?.usoDePreservativo
                    ? 'Si'
                    : 'No',
                '[HABITOSNOCIVOS]': this.fichaMedica?.habitosNocivos
                    ? 'Si'
                    : 'No',
                '[ALCOHOL]': this.fichaMedica?.tomaAlcohol ? 'Si' : 'No',
                '[DROGAINICIO]': this.fichaMedica?.drogaInicio || '',
                '[EDADALCOHOL]': this.fichaMedica?.edadAlcohol || '',
                '[TABACO]': this.fichaMedica?.tabaco ? 'Si' : 'No',
                '[EDADTABACO]': this.fichaMedica?.edadTabaco || '',
                '[CABEZA]': this.fichaMedica?.cabezaDetalle || '',
                '[OJOS]': this.fichaMedica?.ojosDetalle || '',
                '[NARIZ]': this.fichaMedica?.narizDetalle || '',
                '[BOCA]': this.fichaMedica?.bocaDetalle || '',
                '[OIDOS]': this.fichaMedica?.oidoDetalle || '',
                '[OROFARINGE]': this.fichaMedica?.orofaringeDetalle || '',
                '[CORAZON]': this.fichaMedica?.corazonDetalle || '',
                '[URINARIO]': this.fichaMedica?.urinarioDetalle || '',
                '[PULMONES]': this.fichaMedica?.pulmonesDetalle || '',
                '[ABDOMEN]': this.fichaMedica?.abdomenDetalle || '',
                '[PPL]': this.fichaMedica?.pplDetalle || '',
                '[PRU]': this.fichaMedica?.pruDetalle || '',
                '[IMPRESIONDIAGNOSTICO]':
                    this.fichaMedica?.impresionDiagnostico || '',
                '[ASPECTOGENERAL]':
                    this.fichaMedica?.aspectoGeneralFisico || '',
                '[INSPECCION]': this.fichaMedica?.inspeccion || '',
                '[PIELFANERAS]': this.fichaMedica?.pielFaneras || '',
            };

            this.pdfService
                .generarPdf(
                    request,
                    etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
                )
                .subscribe({
                    next: (response: RespuestaPorDefecto<string>) => {
                        if (!response.exito) {
                            this._dialogMensajeService.mensajeError(
                                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                            );
                            return;
                        }

                        const url = window.URL.createObjectURL(
                            this.funcionesUtils.getPdfBlob(response.data)
                        );
                        const pwa = window.open(url);
                    },
                    error: (error: any) => {
                        this._dialogMensajeService.mensajeError(
                            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                        );
                    },
                });
        } catch (error) {
            console.log('error', error);
            this._dialogMensajeService.mensajeError(
                'Hubo un problema al obtener los datos de la cabecera.'
            );
        }
    }

    private obtenerFichaIdentificacion(): Promise<FichaIdentificacionDTO> {
        return new Promise((resolve, reject) => {
            this.fichaIdentificacionService
                .obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                .subscribe({
                    next: (
                        response: RespuestaPorDefecto<FichaIdentificacionDTO>
                    ) => {
                        if (!response.exito) {
                            reject(
                                'Error al obtener la ficha de identificación'
                            );
                            return;
                        }

                        const fichaIdentificacion = response.data;                        

                        resolve(fichaIdentificacion);
                    },
                    error: (error: any) => {
                        reject(error);
                    },
                });
        });
    }

    private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
        return new Promise((resolve, reject) => {
            this.fichaIdentificacionService
                .obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                .subscribe({
                    next: (
                        response: RespuestaPorDefecto<FichaIdentificacionDTO>
                    ) => {
                        if (!response.exito) {
                            reject(
                                'Error al obtener la ficha de identificación'
                            );
                            return;
                        }

                        const fichaIdentificacion = response.data;
                        const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
                        const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
                        const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


                        const datosCabecera = {
                            "[ADOLESCENTE]": nombreAdolescente,
                            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
                            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                        };

                        resolve(datosCabecera);
                    },
                    error: (error: any) => {
                        reject(error);
                    },
                });
        });
    }

    mostrarFormularioCreacionConsulta() {
        this.mostrarListadoConsultas = false;
    }

    // Cambiar a mostrar el listado
    mostrarListadoDeNuevoConsulta() {
        this.mostrarListadoConsultas = true;
    }

    loadImageAsBase64() {
        this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe((data: ArrayBuffer) => {
                const base64String = this.arrayBufferToBase64(data);
                this.base64Image = `data:image/png;base64,${base64String}`;
            });
    }

    arrayBufferToBase64(buffer: ArrayBuffer): string {
        const binary = String.fromCharCode(...new Uint8Array(buffer));
        return window.btoa(binary);
    }

    cargarCentro() {
        this.jerarquiaService
            .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
            .subscribe({
                next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
                    if (!respuesta.exito) {
                        this.jerarquiaService.checkError(respuesta);
                        return;
                    }

                    if (!environment.production) {
                        console.log(respuesta.data);
                    }

                    this.centro = respuesta.data;
                    console.log('centro', this.centro);
                },
                error: (error: any) => {
                    this.jerarquiaService.checkError(error);
                },
            });
    }

    obtenerPermisos() {    
        const acciones = [
            etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR,
            etiquetasModel.ACCIONES_MENU_PERMISO_ELIMINAR
        ];

        const modulos = [
            'MENU_HC_ANTECENDENTES_FAMILIARES',
            'MENU_HC_FICHA_SALUD',
            'MENU_HC_ESQUEMA_CORPORAL',
            'MENU_HC_CONSULTA_MEDICA'
        ];

        modulos.forEach(modulo => {
            const [editar, eliminar] =
            this.permisosService.hasPermissionArray(modulo, ...acciones);

            this.permisos[modulo] = {
                editar,
                eliminar
            };
        });   
    }

    tienePermiso(modulo: string, accion: string): boolean {
        return this.permisos[modulo]?.[accion] ?? false;
    }
}
