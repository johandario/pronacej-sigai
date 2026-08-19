import { CdkTableDataSourceInput } from '@angular/cdk/table';
import {
    ChangeDetectorRef,
    Component,
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
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
    MatPaginatorIntl,
    MatPaginatorModule,
    PageEvent,
} from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import {
    MatTable,
    MatTableDataSource,
    MatTableModule,
} from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CriterioEvaluacionMedicaSeguimientoDTO } from 'app/core/model/both/criterioEvaluacionMedicaSeguimientoDTO.model';
import { DiagnosticoDTO } from 'app/core/model/both/EJE/seguimiento-medico/DiagnosticoDTO.model';
import { EstadoNutricionalDTO } from 'app/core/model/both/EJE/seguimiento-medico/EstadoNutricionalDTO.model';
import { EvaluacionMedicaDTO } from 'app/core/model/both/EJE/seguimiento-medico/EvaluacionMedicaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CustomPaginatorIntl } from 'app/core/services/custom-paginator-intl.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SnackbarService } from 'app/core/services/snackbar.service';
import { CatalogoService } from 'app/modules/catalogo/catalogo.service';
import { environment } from 'environments/environment';
import { List } from 'lodash';
import moment from 'moment';
import { catchError, combineLatest, distinctUntilChanged, map, merge, Observable, of } from 'rxjs';
import { EvaluacionMedicaService } from '../evaluacion-medica.service';
import { ModalCrearEditarCriteriosSeguimientoComponent } from './modal-crear-editar-criterios-seguimiento/modal-crear-editar-criterios-seguimiento.component';
import { DetalleRecetaDTO } from 'app/core/model/both/EJE/detalleRecetaDTO.model';
import { ModalCrearDetalleRecetaComponent } from './modal-crear-detalle-receta/modal-crear-detalle-receta.component';
import { RecetaDTO } from 'app/core/model/both/EJE/recetaDTO.model';
import { CommonModule } from '@angular/common';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
    selector: 'app-formulario-evaluacion-medica',
    standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MatInputModule,
        MatSelectModule,
        MatTableModule,
        MatButtonModule,
        MatStepperModule,
        MatIconModule,
        MatPaginatorModule,
        CommonModule
    ],
    providers: [{ provide: MatPaginatorIntl, useClass: CustomPaginatorIntl }],
    templateUrl: './form-evaluacion-medica.component.html',
    styleUrl:
        '../crear-evaluacion-medica/crear-evaluacion-medica.component.scss',
})
export class FormEvaluacionMedicaComponent implements OnInit {
    tituloPantalla = 'Evaluación de salud';

    nemonicoEtapa: string = 'ETAPAS';
    nemonicoTipoEvaluacion: string = 'TIPOS_EVALUACION_MEDICA';
    nemonicoMotivoConsulta: string = 'MOTIVOS_CONSULTA_MEDICA';
    nemonicoTipoDiagnostico: string = 'TIPO_DIAGNOSTICO_MEDICO';
    nemonicoCriterio: string = 'CRITERIOS_ESTADO_NUTRICIONAL';
    nemonicoGrado: string = 'GRADOS_ESTADO_NUTRICIONAL';

    maxlenghtTextArea: number = 200;

    pageDiagnostico = 0;
    listSizeDiagnostico = [5, 10, 15, 20];
    sizeDiagnostico = this.listSizeDiagnostico[0];
    totalItemsDiagnostico = 0;

    pageEstadoNutricional = 0;
    listSizeEstadoNutricional = [5, 10, 15, 20];
    sizeEstadoNutricional = this.listSizeEstadoNutricional[0];
    totalItemsEstadoNutricional = 0;

    diagnosticos: DiagnosticoDTO[] = [];
    estadosNutricion: EstadoNutricionalDTO[] = [];
    dataSourceDiagnostico = new MatTableDataSource<DiagnosticoDTO>([]);
    dataSourceEstadoNutricional = new MatTableDataSource<EstadoNutricionalDTO>(
        []
    );

    catalogosEtapa: CatalogoDTO[] = [];
    catalogosTipoEvaluacion: CatalogoDTO[] = [];
    catalogosMotivoConsulta: CatalogoDTO[] = [];
    catalogosTipoDiagnostico: CatalogoDTO[] = [];
    catalogosCriterio: CatalogoDTO[] = [];
    catalogosGrado: CatalogoDTO[] = [];

    keyLabelsDiagnostico: any = {
        acciones: 'Acciones',
        codDiagnostico: 'Código diagnostico',
        diagnostico: 'Diagnostico',
        tipoDiagnostico: 'Tipo',
        tratamiento: 'Tratamiento',
        indicaciones: 'Indicaciones',
        examenes: 'Examenes',
        medicamentos: 'Medicamentos',
    };

    keyLabelsNutricion: any = {
        acciones: 'Acciones',
        criterio: 'Criterio',
        grado: 'Grado',
    };

    fechaActual: Date = new Date();

    tokenFichaMedica: string = '';
    evaluacionMedica: EvaluacionMedicaDTO;
    tokenEvaluacionMedica: string = '';

    deshabilitarEdicion: boolean = false;

    evaluacionEdicionCargada: boolean = false;

    // #region formulario evaluacion medica
    evaluacionMedicaForm: UntypedFormGroup;

    readonly formFieldFecha = new FormControl(this.fechaActual);
    readonly formFieldNumRef = new FormControl('', [
        Validators.required,
        this.noSoloEspaciosValidator,
    ]);
    readonly formFieldTalla = new FormControl('', [
        Validators.required,

    ]);
    readonly formFieldPeso = new FormControl('', [
        Validators.required,
        this.noSoloEspaciosValidator,
    ]);

    readonly formFieldClasificacionIMC = new FormControl('');

    readonly formFieldIMC = new FormControl('', [
        Validators.required,
        this.noSoloEspaciosValidator,
    ]);

    readonly formFieldEtapa = new FormControl('', [Validators.required]);
    readonly formFieldTipoEvaluacion = new FormControl(null as CatalogoDTO, [
        Validators.required,
    ]);
    readonly formFieldMotivo = new FormControl('', [Validators.required]);

    errorMessageNumRef = signal('');
    errorMessageTalla = signal('');
    errorMessagePeso = signal('');
    // #endregion

    // #region formularion diagnostico
    diagnosticoForm: UntypedFormGroup;

    readonly formFieldCodDiagnostico = new FormControl('', [
        Validators.required,

    ]);
    readonly formFieldDiagnostico = new FormControl('', [
        Validators.required,

    ]);
    readonly formFieldTipoDiagnostico = new FormControl('', [
        Validators.required,
    ]);
    readonly formFieldTratamiento = new FormControl('', [
        Validators.required,
        Validators.maxLength(this.maxlenghtTextArea),

    ]);
    readonly formFieldIndicaciones = new FormControl('', [
        Validators.required,
        Validators.maxLength(this.maxlenghtTextArea),

    ]);
    readonly formFieldExamenes = new FormControl('', [
        Validators.required,
        Validators.maxLength(this.maxlenghtTextArea),

    ]);
    readonly formFieldMedicamentos = new FormControl('', [
        Validators.required,
        Validators.maxLength(this.maxlenghtTextArea),

    ]);

    errorMessageCodDiagnostico = signal('');
    errorMessageDiagnostico = signal('');
    errorMessageTratamiento = signal('');
    errorMessageIndicaciones = signal('');
    errorMessageExamenes = signal('');
    errorMessageMedicamentos = signal('');

    // #endregion

    // #region formulario estado nutricional
    estadoNutricionalForm: UntypedFormGroup;

    readonly formFieldCriterio = new FormControl('', [Validators.required]);
    readonly formFieldGrado = new FormControl('', [Validators.required]);
    // #endregion

    // #region formulario recomendacion
    recomendacionForm: UntypedFormGroup;

    readonly formFieldRecomendacion = new FormControl('', [
        Validators.required,
        Validators.maxLength(this.maxlenghtTextArea),

    ]);

    errorMessageRecomendacion = signal('');
    // #endregion

    creacionDiagnostico: boolean = false;
    creacionAntecedente: boolean = false;
    creacionNutricion: boolean = false;

    isLoading: boolean = false;

    tokenEdicion: string;

    //Criterios Evaluacion Seguimiento

    keyLabelsCriteriosSeguimiento: any = {
        acciones: 'Acciones',
        nombreEvaluacion: 'Evaluación',
        nombreCriterio: 'Criterio',
        detalle: 'Detalle',
    };

    listSizeCriterios = [5, 10, 15, 20];
    pageCriterios = 0;
    sizeCriterios = this.listSizeCriterios[0];
    totalItemsCriterios = 0;
    listaCriterios: CriterioEvaluacionMedicaSeguimientoDTO[] = [];
    dataSourceCriterios: CdkTableDataSourceInput<CriterioEvaluacionMedicaSeguimientoDTO>;

    @ViewChild('tablaCriterios')
    tablaCriterios: MatTable<CriterioEvaluacionMedicaSeguimientoDTO>;

    listaCriteriosEliminar: string[] = [];

    // #receta
    keyLabelsReceta: any = {
        acciones: 'Acciones',
        detalle: 'Detalle Receta',
        nombreMedicamento: 'Medicamento',
        concentracion: 'Concentracion',
        cantidad: 'Cantidad',
        indicaciones: 'Indicaciones'
    };

    visualizar = false
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

    // catalogosEspecialidadReceta: CatalogoDTO[] = [];
    // nemonicoEsepecialidadRecetas: string = 'ESPECIALIDAD_MEDICA';
    // formReceta: FormGroup;

    // listSizeDetallesReceta = [5, 10, 15, 20];
    // listaDetallesReceta = [];
    // dataSourceDetallesReceta = [];
    // displayedColumnsDetallesReceta = ['acciones', 'medicamento', 'dosis', 'indicaciones', 'concentracion', 'formaFarmaceutica'];
    // pageDetallesReceta = 0;
    // sizeDetallesReceta = this.listSizeDetallesReceta[0];
    // totalItemsDetallesReceta = 0;

    constructor(
        private readonly _formBuilder: UntypedFormBuilder,
        private _catalogoService: CatalogoService,
        private _evaluacionMedicaService: EvaluacionMedicaService,
        private readonly customSnackbar: SnackbarService,
        private readonly changeDetector: ChangeDetectorRef,
        private route: ActivatedRoute,
        private readonly _fuseConfirmationService: FuseConfirmationService,
        private readonly accionesSheet: MatBottomSheet,
        private _dialogMensajeService: DialogMensajeService,
        public dialog: MatDialog,
        @Inject(LOCALE_ID) private locale: string,
        private router: Router,
        private funcionesUtils: FuncionesUtils
    ) {
        merge(
            this.formFieldNumRef.statusChanges,
            this.formFieldNumRef.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageNumRef());

        merge(
            this.formFieldTalla.statusChanges,
            this.formFieldTalla.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageTalla());

        merge(this.formFieldPeso.statusChanges, this.formFieldPeso.valueChanges)
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessagePeso());

        merge(this.formFieldIMC.statusChanges, this.formFieldIMC.valueChanges)
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessagePeso());

        merge(
            this.formFieldCodDiagnostico.statusChanges,
            this.formFieldCodDiagnostico.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageCodDiagnostico());

        merge(
            this.formFieldDiagnostico.statusChanges,
            this.formFieldDiagnostico.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageDiagnostico());

        merge(
            this.formFieldTratamiento.statusChanges,
            this.formFieldTratamiento.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageTratamiento());

        merge(
            this.formFieldIndicaciones.statusChanges,
            this.formFieldIndicaciones.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageIndicaciones());

        merge(
            this.formFieldExamenes.statusChanges,
            this.formFieldExamenes.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageExamenes());

        merge(
            this.formFieldMedicamentos.statusChanges,
            this.formFieldMedicamentos.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageMedicamentos());

        merge(
            this.formFieldRecomendacion.statusChanges,
            this.formFieldRecomendacion.valueChanges
        )
            .pipe(takeUntilDestroyed())
            .subscribe(() => this.updateErrorMessageRecomendacion());

        this.fechaActual = new Date();
        // this.contruirFormReceta();
        this.evaluacionMedicaForm = this._formBuilder.group({
            fecha: this.formFieldFecha,
            numRef: this.formFieldNumRef,
            talla: this.formFieldTalla,
            peso: this.formFieldPeso,
            etapa: this.formFieldEtapa,
            tipoEvaluacion: this.formFieldTipoEvaluacion,
            motivo: this.formFieldMotivo,
            imc: this.formFieldIMC,
            lugarAtencion: [null],
            doctorAtencion: [null],
        });
    }

    async ngOnInit(): Promise<void> {
        
        this.detectarCambiosIMC();

        await this.getCatalogoMotivoConsulta();
        await this.getCatalogoTipoEvaluacion();
        await this.getCatalogoEtapa();
        // await this.getCatalogoEspecialidadesReceta();
        this.route.queryParams.subscribe((params) => {
            this.tokenEdicion = params['token'];
            if (this.tokenEdicion) {
                // Llama aquí a una función para cargar los datos del formulario para edición
                this.obtenerEvaluacionMedicaEdicion(this.tokenEdicion);
                this.criteriosAsociadosSeguimiento(this.tokenEdicion);
            }
        });

        if(this.router.url.includes('visualizar')){
            this.evaluacionMedicaForm.disable();
            this.visualizar=true;
        }

        this.diagnosticoForm = this._formBuilder.group({
            codDiagnostico: this.formFieldCodDiagnostico,
            diagnostico: this.formFieldDiagnostico,
            tipoDiagnostico: this.formFieldTipoDiagnostico,
            tratamiento: this.formFieldTratamiento,
            indicaciones: this.formFieldIndicaciones,
            examenes: this.formFieldExamenes,
            medicamentos: this.formFieldMedicamentos,
        });

        this.estadoNutricionalForm = this._formBuilder.group({
            criterio: this.formFieldCriterio,
            grado: this.formFieldGrado,
        });

        this.recomendacionForm = this._formBuilder.group({
            recomendacion: this.formFieldRecomendacion,
        });



        this._evaluacionMedicaService.fichaMedica$.subscribe((ficha) => {
            if (ficha) {
                this.tokenFichaMedica = ficha;
            }
        });

        this._evaluacionMedicaService.evaluacionMedica$.subscribe(
            (evaluacion) => {
                if (evaluacion) {
                    this.tokenEvaluacionMedica = evaluacion;
                    console.log(evaluacion);
                }
            }
        );
        this.calcularIMCAutomaticamente();
    }

    detectarCambiosIMC() {
        this.formFieldIMC
        ?.valueChanges
        .pipe(distinctUntilChanged())
        .subscribe((valorIMC: any) => {
        if (valorIMC) {
            const clasificacionIMC = this.funcionesUtils.obtenerClasificacionIMC(valorIMC);
            this.formFieldClasificacionIMC?.setValue(clasificacionIMC, { emitEvent: false });
        } else {
            this.formFieldClasificacionIMC?.setValue(null, { emitEvent: false });
        }
        });
    }

    // #region peticiones al back

    async obtenerEvaluacionMedica() {
        await this.getCatalogoMotivoConsulta();
        await this.getCatalogoTipoEvaluacion();
        await this.getCatalogoEtapa();
        this.isLoading = true;
        this._evaluacionMedicaService
            .getEvaluacionMedicaByTokenId(
                this.evaluacionMedica.tokenIdentificador
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<EvaluacionMedicaDTO>) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }
                    this.evaluacionMedica = response.data;
                    console.log('evaluacion medica', this.evaluacionMedica);
                    if (this.evaluacionMedica) {
                        this.tokenEdicion = this.evaluacionMedica.tokenIdentificador;
                        this.evaluacionMedicaForm.patchValue({
                            fecha: this.evaluacionMedica.fecha,
                            numRef: this.evaluacionMedica.numReferencia,
                            talla: this.evaluacionMedica.talla,
                            peso: this.evaluacionMedica.peso,
                            etapa: this.evaluacionMedica.etapa as CatalogoDTO,
                            tipoEvaluacion: this.evaluacionMedica.tipoEvaluacion as CatalogoDTO,
                            motivo: this.evaluacionMedica.motivoConsulta as CatalogoDTO,
                            lugarAtencion: this.evaluacionMedica.lugarAtencion,
                            doctorAtencion: this.evaluacionMedica.doctorAtencion,
                        });

                        this._evaluacionMedicaService.setTokenEvaluacionMedica(
                            this.evaluacionMedica.tokenIdentificador
                        );
                        // this.evaluacionMedicaForm.disable();
                        this.deshabilitarEdicion = true;
                        this.changeDetector.detectChanges();
                    }
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
                complete: () => {
                    this.isLoading = false;
                    this.evaluacionEdicionCargada = true;
                },
            });
    }

    obtenerEvaluacionMedicaEdicion(tokenId: string) {
        this.isLoading = true;
        this._evaluacionMedicaService
            .getEvaluacionMedicaByTokenId(tokenId)
            .subscribe({
                next: (response: RespuestaPorDefecto<EvaluacionMedicaDTO>) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }
                    this.evaluacionMedica = response.data;

                    if (this.evaluacionMedica) {
                        this.evaluacionMedicaForm.patchValue({
                            fecha: this.evaluacionMedica.fecha,
                            numRef: this.evaluacionMedica.numReferencia,
                            talla: this.evaluacionMedica.talla,
                            peso: this.evaluacionMedica.peso,
                            etapa: this.evaluacionMedica.etapa,
                            tipoEvaluacion:
                                this.evaluacionMedica.tipoEvaluacion,
                            motivo: this.evaluacionMedica.motivoConsulta,
                            lugarAtencion: this.evaluacionMedica.lugarAtencion,
                            doctorAtencion: this.evaluacionMedica.doctorAtencion
                        });

                        // Si existe una receta asociada
                        // if (this.evaluacionMedica.receta) {
                        //     console.log('receta medica', this.evaluacionMedica.receta);
                        //     const especialidadSeleccionada = this.catalogosEspecialidadReceta.find(
                        //         cat => cat.tokenIdentificador === this.evaluacionMedica.receta.especialidad.tokenIdentificador
                        //     );
                        //     console.log('cat encontrado', especialidadSeleccionada);
                        //     this.formReceta.patchValue({
                        //         numeroReceta: this.evaluacionMedica.receta.numeroReceta,
                        //         especialidad: especialidadSeleccionada,
                        //         fechaEmision: this.evaluacionMedica.receta.fechaEmision,
                        //         observaciones: this.evaluacionMedica.receta.observaciones,
                        //     });
                        //     console.log('recete', this.evaluacionMedica.receta);

                        //     // Cargar detalles de la receta
                        //     this.listaDetallesReceta = this.evaluacionMedica.receta.detalles || [];
                        //     this.totalItemsDetallesReceta = this.listaDetallesReceta.length;

                        //     // Aplicar paginación inicial a la tabla de detalles
                        //     const startIndex = this.pageDetallesReceta * this.sizeDetallesReceta;
                        //     const endIndex = startIndex + this.sizeDetallesReceta;
                        //     this.dataSourceDetallesReceta = this.listaDetallesReceta.slice(startIndex, endIndex);
                        // }

                        this._evaluacionMedicaService.setTokenEvaluacionMedica(
                            this.evaluacionMedica.tokenIdentificador
                        );
                        this.deshabilitarEdicion = false;
                        this.changeDetector.detectChanges();
                    }
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
                complete: () => {
                    this.obtenerDiagnosticos();
                    this.obtenerEstadoNutricional();
                    this.isLoading = false;
                    this.evaluacionEdicionCargada = true;
                },
            });
    }

    async obtenerDiagnosticos() {
        this.isLoading = true;
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeDiagnostico;
        paginacionRequest.page = this.pageDiagnostico;
        paginacionRequest.tokenIdentificador =
            this.evaluacionMedica.tokenIdentificador;

        this._evaluacionMedicaService
            .getDiagnosticosByEvaluacionMedica(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<DiagnosticoDTO>
                    >
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }

                    this.diagnosticos = response.data.data;
                    console.log(this.diagnosticos);
                    this.dataSourceDiagnostico.data = this.diagnosticos;
                    this.isLoading = false;
                    this.changeDetector.detectChanges();
                    this.totalItemsDiagnostico = response.data.totalItems;
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
            });
    }

    async obtenerEstadoNutricional() {
        this.isLoading = true;
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeEstadoNutricional;
        paginacionRequest.page = this.pageEstadoNutricional;
        paginacionRequest.tokenIdentificador =
            this.evaluacionMedica.tokenIdentificador;

        this._evaluacionMedicaService
            .getEstadoNutricionalByEvaluacionMedica(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<EstadoNutricionalDTO>
                    >
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }

                    this.estadosNutricion = response.data.data;
                    this.dataSourceEstadoNutricional.data =
                        this.estadosNutricion;
                    this.isLoading = false;
                    this.changeDetector.detectChanges();
                    this.totalItemsEstadoNutricional = response.data.totalItems;
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
            });
    }

    crearEvaluacionMedica() {
        if (this.evaluacionMedicaForm.valid) {
            this.isLoading = true;

            // const receta: RecetaDTO = {
            //     numeroReceta: this.formReceta.get('numeroReceta')?.value,
            //     especialidad: this.formReceta.get('especialidad')?.value,
            //     fechaEmision: this.formReceta.get('fechaEmision')?.value,
            //     observaciones: this.formReceta.get('observaciones')?.value,
            //     detalles: this.listaDetallesReceta // Array de DetalleRecetaDTO ya preparado
            // };

            const evaluacionMedica: EvaluacionMedicaDTO = {
                tokenIdFichaMedica: this.tokenFichaMedica,
                fecha: this.evaluacionMedicaForm.get('fecha')?.value,
                talla: this.evaluacionMedicaForm.get('talla')?.value,
                peso: this.evaluacionMedicaForm.get('peso')?.value,
                numReferencia: this.evaluacionMedicaForm.get('numRef')?.value,
                etapa: this.evaluacionMedicaForm.get('etapa')?.value,
                tipoEvaluacion:
                    this.evaluacionMedicaForm.get('tipoEvaluacion')?.value,
                motivoConsulta: this.evaluacionMedicaForm.get('motivo')?.value,
                criteriosAsociadosSeguimiento: this.listaCriterios,
                lugarAtencion: this.evaluacionMedicaForm.get('lugarAtencion')?.value,
                doctorAtencion: this.evaluacionMedicaForm.get('doctorAtencion')?.value,
            };

            this._evaluacionMedicaService
                .postEvaluacionMedica(evaluacionMedica)
                .subscribe({
                    next: (response) => {
                        this.evaluacionMedica = response.data;
                        this.customSnackbar.show(
                            'Evaluacion médica creada con éxito',
                            'Cerrar',
                            'success'
                        );

                        this.obtenerEvaluacionMedica();
                        this.criteriosAsociadosSeguimiento(this.evaluacionMedica.tokenIdentificador);
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

    crearDiagnostico() {
        if (this.diagnosticoForm.valid) {
            this.isLoading = true;
            const diagnostico: DiagnosticoDTO = {
                tokenIdEvaluacionMedica: this.tokenEvaluacionMedica,
                tipoDiagnostico:
                    this.diagnosticoForm.get('tipoDiagnostico')?.value,
                codDiagnostico:
                    this.diagnosticoForm.get('codDiagnostico')?.value,
                diagnostico: this.diagnosticoForm.get('diagnostico')?.value,
                tratamiento: this.diagnosticoForm.get('tratamiento')?.value,
                indicaciones: this.diagnosticoForm.get('indicaciones')?.value,
                examenes: this.diagnosticoForm.get('examenes')?.value,
                medicamentos: this.diagnosticoForm.get('medicamentos')?.value,
            };

            this._evaluacionMedicaService
                .postDiagnostico(diagnostico)
                .subscribe({
                    next: (response) => {
                        this.customSnackbar.show(
                            'Diagnostico creado con éxito',
                            'Cerrar',
                            'success'
                        );
                        this.obtenerDiagnosticos();
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

    crearEstadoNutricional() {
        if (this.estadoNutricionalForm.valid) {
            this.isLoading = true;
            const estadoNutricional: EstadoNutricionalDTO = {
                tokenIdEvaluacionMedica:
                    this.evaluacionMedica.tokenIdentificador,
                criterio: this.estadoNutricionalForm.get('criterio')?.value,
                grado: this.estadoNutricionalForm.get('grado')?.value,
            };

            this._evaluacionMedicaService
                .postEstadoNutricional(estadoNutricional)
                .subscribe({
                    next: (response) => {
                        this.customSnackbar.show(
                            'Estado nutricional creado con éxito',
                            'Cerrar',
                            'success'
                        );
                        this.obtenerEstadoNutricional();
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

    async actualizarEvaluacionMedica() {

        if (this.evaluacionMedicaForm.valid) {
            this.isLoading = true;

            // const receta: RecetaDTO = {
            //     numeroReceta: this.formReceta.get('numeroReceta')?.value,
            //     especialidad: this.formReceta.get('especialidad')?.value,
            //     // fechaEmision: this.evaluacionMedicaForm.get('fechaEmision')?.value,
            //     observaciones: this.formReceta.get('observaciones')?.value,
            //     detalles: this.listaDetallesReceta // Array de DetalleRecetaDTO ya preparado
            // };

            const evaluacionMedica: EvaluacionMedicaDTO = {
                tokenIdentificador: this.tokenEdicion,
                tokenIdFichaMedica: this.tokenFichaMedica,
                fecha: this.evaluacionMedicaForm.get('fecha')?.value,
                talla: this.evaluacionMedicaForm.get('talla')?.value,
                peso: this.evaluacionMedicaForm.get('peso')?.value,
                numReferencia: this.evaluacionMedicaForm.get('numRef')?.value,
                etapa: this.evaluacionMedicaForm.get('etapa')?.value,
                tipoEvaluacion:
                    this.evaluacionMedicaForm.get('tipoEvaluacion')?.value,
                motivoConsulta: this.evaluacionMedicaForm.get('motivo')?.value,
                criteriosAsociadosSeguimiento: this.listaCriterios,
                tokensCriteriosEliminar: this.listaCriteriosEliminar,
                lugarAtencion: this.evaluacionMedicaForm.get('lugarAtencion')?.value,
                doctorAtencion: this.evaluacionMedicaForm.get('doctorAtencion')?.value,

            };

            this._evaluacionMedicaService
                .updateEvaluacionMedica(evaluacionMedica)
                .subscribe({
                    next: (response) => {
                        if (response.data) {
                            this.recomendacionForm.patchValue({
                                recomendacion:
                                    this.evaluacionMedica.recomendacion,
                            });

                            this.recomendacionForm.disable();
                            this.changeDetector.detectChanges();
                        }
                        this.customSnackbar.show(
                            'Evaluacion actualizada con éxito',
                            'Cerrar',
                            'success'
                        );
                        this.obtenerEvaluacionMedica();
                        this.criteriosAsociadosSeguimiento(this.evaluacionMedica.tokenIdentificador);
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

    actualizarRecomendacion() {
        if (this.recomendacionForm.valid) {
            this.isLoading = true;
            const evaluacionMedica: EvaluacionMedicaDTO = {
                tokenIdFichaMedica: this.tokenFichaMedica,
                tokenIdentificador: this.evaluacionMedica.tokenIdentificador,
                fecha: this.evaluacionMedica.fecha,
                talla: this.evaluacionMedica.talla,
                peso: this.evaluacionMedica.peso,
                numReferencia: this.evaluacionMedica.numReferencia,
                etapa: this.evaluacionMedica.etapa,
                tipoEvaluacion: this.evaluacionMedica.tipoEvaluacion,
                motivoConsulta: this.evaluacionMedica.motivoConsulta,
                lugarAtencion: this.evaluacionMedica.lugarAtencion,
                doctorAtencion: this.evaluacionMedica.doctorAtencion,
                recomendacion:
                    this.recomendacionForm.get('recomendacion')?.value,
            };

            this._evaluacionMedicaService
                .updateEvaluacionMedica(evaluacionMedica)
                .subscribe({
                    next: (response) => {
                        if (response.data) {
                            this.recomendacionForm.patchValue({
                                recomendacion:
                                    this.evaluacionMedica.recomendacion,
                            });

                            this.recomendacionForm.disable();
                            this.changeDetector.detectChanges();
                        }
                        this.customSnackbar.show(
                            'Recomendacion agregada con éxito',
                            'Cerrar',
                            'success'
                        );
                        this.obtenerEvaluacionMedica();
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

    eliminarDiagnostico(diagnostico: DiagnosticoDTO) {
        const confirmation = this._fuseConfirmationService.open({
            title: 'Eliminar diagnostico',
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
                    .deleteDiagnostico(diagnostico)
                    .subscribe({
                        next: (response) => {
                            this.obtenerDiagnosticos();

                            this.customSnackbar.show(
                                'Diagnostico eliminado con exito',
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

    eliminarEstadoNutricional(nuticion: EstadoNutricionalDTO) {
        const confirmation = this._fuseConfirmationService.open({
            title: 'Eliminar estado nutricional',
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
                    .deleteEstadoNutricional(nuticion)
                    .subscribe({
                        next: (response) => {
                            this.obtenerEstadoNutricional();

                            this.customSnackbar.show(
                                'Estado nutricional eliminado con exito',
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

    abrirFormDiagnostico() {
        this.creacionDiagnostico = !this.creacionDiagnostico;
        this.getCatalogoTipoDiagnostico();
    }

    abrirFormEstadoNutricional() {
        this.creacionNutricion = !this.creacionNutricion;
        this.getCatalogoCriterio();
        this.getCatalogoGrado();
    }

    async getCatalogoEtapa() {
        this.getCatalogos(this.nemonicoEtapa).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosEtapa = catalogos;
            }
        );
    }

    // async getCatalogoEspecialidadesReceta() {
    //     this.getCatalogos(this.nemonicoEsepecialidadRecetas).subscribe(
    //         (catalogos: CatalogoDTO[]) => {
    //             this.catalogosEspecialidadReceta = catalogos;
    //         }
    //     );
    // }

    async getCatalogoTipoEvaluacion() {
        this.getCatalogos(this.nemonicoTipoEvaluacion).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosTipoEvaluacion = catalogos;
            }
        );
    }

    async getCatalogoMotivoConsulta() {
        this.getCatalogos(this.nemonicoMotivoConsulta).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosMotivoConsulta = catalogos;
            }
        );
    }

    async getCatalogoTipoDiagnostico() {
        this.getCatalogos(this.nemonicoTipoDiagnostico).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosTipoDiagnostico = catalogos;
            }
        );
    }

    async getCatalogoCriterio() {
        this.getCatalogos(this.nemonicoCriterio).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosCriterio = catalogos;
            }
        );
    }

    async getCatalogoGrado() {
        this.getCatalogos(this.nemonicoGrado).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosGrado = catalogos;
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

    // #region manejo mensajes de error
    updateErrorMessageNumRef() {
        if (this.formFieldNumRef.hasError('required')) {
            this.errorMessageNumRef.set('El campo es obligatorio');
        } else if (this.formFieldNumRef.hasError('onlySpaces')) {
            this.errorMessageNumRef.set('No puede contener solo espacios');
        } else {
            this.errorMessageNumRef.set('');
        }
    }

    updateErrorMessageTalla() {
        if (this.formFieldTalla.hasError('required')) {
            this.errorMessageTalla.set('El campo es obligatorio');
        } else if (this.formFieldTalla.hasError('onlySpaces')) {
            this.errorMessageTalla.set('No puede contener solo espacios');
        } else {
            this.errorMessageTalla.set('');
        }
    }

    updateErrorMessagePeso() {
        if (this.formFieldPeso.hasError('required')) {
            this.errorMessagePeso.set('El campo es obligatorio');
        } else if (this.formFieldPeso.hasError('onlySpaces')) {
            this.errorMessagePeso.set('No puede contener solo espacios');
        } else {
            this.errorMessagePeso.set('');
        }
    }

    updateErrorMessageIMC() {
        if (this.formFieldIMC.hasError('required')) {
            this.errorMessagePeso.set('El campo es obligatorio');
        } else if (this.formFieldIMC.hasError('onlySpaces')) {
            this.errorMessagePeso.set('No puede contener solo espacios');
        } else {
            this.errorMessagePeso.set('');
        }
    }

    updateErrorMessageCodDiagnostico() {
        if (this.formFieldCodDiagnostico.hasError('required')) {
            this.errorMessageCodDiagnostico.set('El campo es obligatorio');
        } else if (this.formFieldCodDiagnostico.hasError('onlySpaces')) {
            this.errorMessageCodDiagnostico.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageCodDiagnostico.set('');
        }
    }

    updateErrorMessageDiagnostico() {
        if (this.formFieldDiagnostico.hasError('required')) {
            this.errorMessageDiagnostico.set('El campo es obligatorio');
        } else if (this.formFieldDiagnostico.hasError('onlySpaces')) {
            this.errorMessageDiagnostico.set('No puede contener solo espacios');
        } else {
            this.errorMessageDiagnostico.set('');
        }
    }

    updateErrorMessageTratamiento() {
        if (this.formFieldTratamiento.hasError('required')) {
            this.errorMessageTratamiento.set('El campo es obligatorio');
        } else if (this.formFieldTratamiento.hasError('maxlength')) {
            this.errorMessageTratamiento.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldTratamiento.hasError('onlySpaces')) {
            this.errorMessageTratamiento.set('No puede contener solo espacios');
        } else {
            this.errorMessageTratamiento.set('');
        }
    }

    updateErrorMessageIndicaciones() {
        if (this.formFieldIndicaciones.hasError('required')) {
            this.errorMessageIndicaciones.set('El campo es obligatorio');
        } else if (this.formFieldIndicaciones.hasError('maxlength')) {
            this.errorMessageIndicaciones.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldIndicaciones.hasError('onlySpaces')) {
            this.errorMessageIndicaciones.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageIndicaciones.set('');
        }
    }

    updateErrorMessageExamenes() {
        if (this.formFieldExamenes.hasError('required')) {
            this.errorMessageExamenes.set('El campo es obligatorio');
        } else if (this.formFieldExamenes.hasError('maxlength')) {
            this.errorMessageExamenes.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldExamenes.hasError('onlySpaces')) {
            this.errorMessageExamenes.set('No puede contener solo espacios');
        } else {
            this.errorMessageExamenes.set('');
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

    updateErrorMessageRecomendacion() {
        if (this.formFieldRecomendacion.hasError('required')) {
            this.errorMessageRecomendacion.set('El campo es obligatorio');
        } else if (this.formFieldRecomendacion.hasError('maxlength')) {
            this.errorMessageRecomendacion.set(
                `Máximo ${this.maxlenghtTextArea} caracteres`
            );
        } else if (this.formFieldRecomendacion.hasError('onlySpaces')) {
            this.errorMessageRecomendacion.set(
                'No puede contener solo espacios'
            );
        } else {
            this.errorMessageRecomendacion.set('');
        }
    }
    // #endregion

    // #region validadores
    noSoloEspaciosValidator(control: UntypedFormControl) {
        if (control.value.trim() === '') {
            return { onlySpaces: true };
        }
        return null;
    }
    // #endregion

    getKeysDiagnostico() {
        return Object.keys(this.keyLabelsDiagnostico);
    }

    getKeysNutricion() {
        return Object.keys(this.keyLabelsNutricion);
    }

    getKeysCriterios() {
        return Object.keys(this.keyLabelsCriteriosSeguimiento);
    }

    handlePageEventDiagnostico(pageEvent: PageEvent) {
        this.sizeDiagnostico = pageEvent.pageSize;
        this.pageDiagnostico = pageEvent.pageIndex;
        this.obtenerDiagnosticos();
    }

    handlePageEventNutricion(pageEvent: PageEvent) {
        this.sizeEstadoNutricional = pageEvent.pageSize;
        this.pageEstadoNutricional = pageEvent.pageIndex;
        this.obtenerEstadoNutricional();
    }

    getFormatedDate(date: Date) {
        return moment(date, 'YYYY-MM-DDTHH:mm:ssZ').toDate().toLocaleString();
    }

    async criteriosAsociadosSeguimiento(tokenIdentificador: string) {
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeCriterios;
        paginacionRequest.page = this.pageCriterios;
        paginacionRequest.tokenIdentificador = tokenIdentificador;

        this._evaluacionMedicaService
            .getCriteriosAsociadosSeguimiento(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>
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
                    this.listaCriterios = response.data.data;
                    this.dataSourceCriterios = this.listaCriterios;
                    this.totalItemsCriterios = response.data.totalItems;
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    handlePageEventCriteriosSeguimiento(pageEvent: PageEvent) {
        this.sizeCriterios = pageEvent.pageSize;
        this.pageCriterios = pageEvent.pageIndex;
        this.criteriosAsociadosSeguimiento(this.tokenEdicion);
    }

    borrarCriterio(id_temp: number, id: string) {

        const confirmation = this._fuseConfirmationService.open({
            title: 'Eliminar registro',
            message:
                '¿Estás seguro de eliminar este registro?',
            actions: {
                confirm: {
                    label: 'Eliminar',
                },
                cancel: {
                    label: 'Cancelar'
                }
            },
        });

        confirmation.afterClosed().subscribe((result) => {
            if (result === 'confirmed') {
                if (id) {
                    const datosActualizados = this.listaCriterios.filter(
                        (item) => item.tokenIdentificador !== id
                    );
                    this.dataSourceCriterios = datosActualizados;
                    this.listaCriterios = datosActualizados;

                    this.listaCriteriosEliminar.push(id);
                } else {
                    const datosActualizados = this.listaCriterios.filter(
                        (item) => item.id_temporal !== id_temp
                    );
                    this.listaCriterios = datosActualizados;
                    this.dataSourceCriterios = datosActualizados;
                }
            }
        });


    }

    aniadirFilaCriterio() {
        const dialogRef = this.dialog.open(
            ModalCrearEditarCriteriosSeguimientoComponent,
            {
                data: {},
                width: '600px',
            }
        );
        dialogRef.afterClosed().subscribe(async (result) => {
            console.log('dato desde modal', result);
            if (result && !result.esEdicion) {
                const datosActualizados = this.listaCriterios;
                datosActualizados.push(result);
                this.dataSourceCriterios = datosActualizados;
                this.tablaCriterios.renderRows();
            }
        });
    }

    editarFilaInformacion(informacion: CriterioEvaluacionMedicaSeguimientoDTO) {
        const dialogRef = this.dialog.open(
            ModalCrearEditarCriteriosSeguimientoComponent,
            {
                data: {
                    informacion: informacion,
                },
                width: '600px',
            }
        );

        dialogRef.afterClosed().subscribe(async (result) => {
            console.log('resultado', result.id_temporal);
            if (result.esEdicion) {
                // this.obtenerInformacionUbicacion(
                //     this.personaRelacionadaEditando.idPersonaRelacionada
                // );
                const datosActualizados = this.listaCriterios;
                const index = this.listaCriterios.findIndex(
                    (x) => x.tokenIdentificador == result.tokenIdentificador
                );
                if (index !== -1) {
                    datosActualizados[index] = result;
                    this.listaCriterios[index] = result;
                    console.log('se encontro y se cambio', datosActualizados);
                } else {

                    const index = this.listaCriterios.findIndex(
                        (x) => x.id_temporal == result.id_temporal
                    );
                    if (index !== -1) {
                        datosActualizados[index] = result;
                        this.listaCriterios[index] =
                            result;
                    }
                }
                this.dataSourceCriterios = datosActualizados;
                this.tablaCriterios.renderRows();
            }
        });
    }

    soloNumero(event: KeyboardEvent): void {
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

    ingresoMedidasUnDecimal(event: any): void {
        const decimalSeparator = this.locale === 'es' ? '.' : ',';
        const input = event.target as HTMLInputElement;
        let valor = input.value;
        const patronDecimal = new RegExp(`^[0-9${decimalSeparator}]*$`);
        if (patronDecimal.test(valor)) {
            input.value = ((parseFloat(valor)).toFixed(1)).toString();
        }
    }

    // contruirFormReceta() {
    //     this.formReceta = this._formBuilder.group({
    //         numeroReceta: [null, Validators.required],
    //         fechaEmision: [null], // Puedes agregar validaciones si lo requieres
    //         observaciones: [null],
    //         especialidad: [null],
    //         // detalles: this._formBuilder.array([]) // Inicialmente vacío, agregarás detalles dinámicamente
    //     });
    // }

    // agregarDetalleReceta() {
    //     this.dialog.open(ModalCrearDetalleRecetaComponent, {
    //         width: '600px',
    //         data: {}
    //     }).afterClosed().subscribe((resultado) => {
    //         if (resultado) {
    //             // resultado es un DetalleRecetaDTO
    //             this.listaDetallesReceta.push(resultado);
    //             this.dataSourceDetallesReceta = [...this.listaDetallesReceta];
    //         }
    //     });
    // }

    // editarDetalleReceta(detalle) {
    //     this.dialog.open(ModalCrearDetalleRecetaComponent, {
    //         width: '600px',
    //         data: { informacion: detalle }
    //     }).afterClosed().subscribe((resultado) => {
    //         if (resultado) {
    //             // Actualizar el detalle en la lista
    //             const index = this.listaDetallesReceta.findIndex(d => d.tokenIdentificador === detalle.tokenIdentificador);
    //             if (index > -1) {
    //                 this.listaDetallesReceta[index] = resultado;
    //             }
    //             this.dataSourceDetallesReceta = [...this.listaDetallesReceta];
    //         }
    //     });
    // }

    // borrarDetalleReceta(detalle) {
    //     const index = this.listaDetallesReceta.indexOf(detalle);
    //     if (index > -1) {
    //         // Marcar como eliminado o remover de la lista
    //         this.listaDetallesReceta.splice(index, 1);
    //         this.dataSourceDetallesReceta = [...this.listaDetallesReceta];
    //     }
    // }

    // handlePageEventDetallesReceta(event: PageEvent) {
    //     this.pageDetallesReceta = event.pageIndex;
    //     this.sizeDetallesReceta = event.pageSize;

    //     const startIndex = this.pageDetallesReceta * this.sizeDetallesReceta;
    //     const endIndex = startIndex + this.sizeDetallesReceta;

    //     // Mostrar solo la porción de la lista correspondiente a la página actual
    //     this.dataSourceDetallesReceta = this.listaDetallesReceta.slice(startIndex, endIndex);
    // }

    compararCatalogos(o1: CatalogoDTO, o2: CatalogoDTO): boolean {
        return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
    }

    atras() {
        this.router.navigate(['../seguimiento'], { relativeTo: this.route });
    }

    calcularIMCAutomaticamente(): void {
        combineLatest([
            this.formFieldTalla.valueChanges,
            this.formFieldPeso.valueChanges
        ]).subscribe(([talla, peso]) => {
            if (talla && peso) {
                const tallam = parseFloat(talla);
                const pesom = parseFloat(peso);
                // Verifica que talla sea mayor que cero para evitar división por cero
                if (!isNaN(tallam) && tallam > 0 && !isNaN(pesom)) {
                    const imc = pesom / (tallam * tallam);
                    // Establece el IMC formateado a dos decimales
                    this.formFieldIMC.setValue(imc.toFixed(2), { emitEvent: true });
                } else {
                    this.formFieldIMC.setValue(null, { emitEvent: false });
                }
            } else {
                this.formFieldIMC.setValue(null, { emitEvent: false });
            }
        });
    }
}
