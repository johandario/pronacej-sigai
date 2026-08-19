import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperModule } from '@angular/material/stepper';
import {
    MatTable,
    MatTableDataSource,
    MatTableModule,
} from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';
import { EvaluacionMedicaProgresoDTO } from 'app/core/model/both/EJE/EvaluacionMedicaProgresoDTO.model';
import { FichaMedicaEnfermedadDTO } from 'app/core/model/both/fichaMedicaEnfermedadDTO.model';
import { FichaMedicaDTO } from 'app/core/model/both/ia/ficha-medica/FichaMedicaDTO.model';
import { PersonaRelacionadaEnfermedadDTO } from 'app/core/model/both/personaRelacionadaEnfermedadDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EnfermedadPersonaRelacionadaService } from 'app/modules/seguridad/services/enfermedadPersonaRelacionada.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import { EvaluacionMedicaService } from '../evaluacion-medica/evaluacion-medica.service';
import { CrearEditarEvaluacionMedicaProgresoComponent } from '../evaluacion-medica/progreso-evaluacion-medica/crear-editar-evaluacion-medica-progreso/crear-editar-evaluacion-medica-progreso.component';
import { ProgresoEvaluacionMedicaComponent } from '../evaluacion-medica/progreso-evaluacion-medica/progreso-evaluacion-medica.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
    selector: 'app-historia-clinica',
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
        MatDatepickerModule,
        MatTabsModule,
        ProgresoEvaluacionMedicaComponent,
        CommonModule,
        CrearEditarEvaluacionMedicaProgresoComponent,
    ],
    templateUrl: './historia-clinica.component.html',
    styleUrl: './historia-clinica.component.scss',
})
export class HistoriaClinicaComponent implements OnInit {
    conductasForm: FormGroup;
    tituloPantalla = 'Evaluación médica';
    uuid_fp: string;

    listSizeEnfermedadPersonas = [5, 10, 15, 20];
    pageEnfermedadPersonas = 0;
    sizeEnfermedadPersonas = this.listSizeEnfermedadPersonas[0];
    totalItemsEnfermedadPersonas = 0;
    listaEnfermedadPersonasRelacionadas: PersonaRelacionadaEnfermedadDTO[] = [];
    dataSourceEnfermedadPersonas: CdkTableDataSourceInput<PersonaRelacionadaEnfermedadDTO>;

    @ViewChild('tablaEnfermedadPersonas')
    tableEnfermedadesPersonas: MatTable<PersonaRelacionadaEnfermedadDTO>;

    isLoading: boolean = false;
    fichaMedica: FichaMedicaDTO;

    keyLabelsTablePersonasEnfermedad: any = {
        parentesco: 'Parentesco',
        nombreEnfermedad: 'Enfermedad',
        detalle: 'Detalle',
    };

    listSizeEnfermedadFicha = [5, 10, 15, 20];
    pageEnfermedadFicha = 0;
    sizeEnfermedadFicha = this.listSizeEnfermedadFicha[0];
    totalItemsEnfermedadFicha = 0;
    listaEnfermedadFicha: FichaMedicaEnfermedadDTO[] = [];
    dataSourceEnfermedadFicha: CdkTableDataSourceInput<FichaMedicaEnfermedadDTO>;

    @ViewChild('tablaEnfermedadFicha')
    tableEnfermedadesFicha: MatTable<FichaMedicaEnfermedadDTO>;

    keyLabelsTableEnfermedadFicha: any = {
        nombreEnfermedad: 'Enfermedad',
        detalle: 'Tratamiento',
        edadPresente: 'Edad presencia',
    };

    page = 0;
    listSize = [5, 10, 15, 20];
    size = this.listSize[0];
    totalItems = 0;

    keyLabelsTable: any = {
        acciones: 'Acciones',
        fecha: 'Fecha',
        estadoNutricional: 'Estado nutricional',
        tipoEvaluacionProgreso: 'Tipo evaluación',
    };

    evaluaciones: EvaluacionMedicaProgresoDTO[] = [];
    datasource = new MatTableDataSource<EvaluacionMedicaProgresoDTO>([]);

    tokenFichaMedica: string = '';
    mostrarListado = true;

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private _dialogMensajeService: DialogMensajeService,
        private _enfermedadPersonasRelacionada: EnfermedadPersonaRelacionadaService,
        public dialog: MatDialog,
        private _evaluacionMedicaService: EvaluacionMedicaService,
        private readonly changeDetector: ChangeDetectorRef,
        private authSerguridadServicio: AuthSerguridadServicio,
    ) {
        this.construirForm();
    }

    async ngOnInit(): Promise<void> {
        await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
          etiquetasModel.NEMONICO_MENU_EVALUACION_SALUD,
        );
        this.uuid_fp = this.route.snapshot.params['uuid_fp'];
        await this.obtenerFichaMedica();
    }

    construirForm() {
        this.conductasForm = this.fb.group({
            sexual: [{ value: '' }],
            psicoactivas: [''],
            otras: [''],
            detalleAlergias: [''],
            alergiaMedicamentos: [{ value: false }],
        });
    }

    async obtenerEnfermedadesPersonasRelacionadas(tokenIdentificador: string) {
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeEnfermedadPersonas;
        paginacionRequest.page = this.pageEnfermedadPersonas;
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

    handlePageEventEnfermedadesPersonasRelacionadas(pageEvent: PageEvent) {
        this.sizeEnfermedadPersonas = pageEvent.pageSize;
        this.pageEnfermedadPersonas = pageEvent.pageIndex;
        this.obtenerEnfermedadesPersonasRelacionadas(this.uuid_fp);
    }

    async obtenerFichaMedica() {
        this.isLoading = true;
        this._evaluacionMedicaService
            .getFichaMedicaByFichaIden(this.uuid_fp, etiquetasModel.MENU_FICHA_HISTORIA_CLINICA)
            .subscribe({
                next: (response: RespuestaPorDefecto<FichaMedicaDTO>) => {
                    if (!response.exito) {
                        console.log('no fue exitosa el get ficha medica');
                        if (!response.data) {
                            this._evaluacionMedicaService.checkError(response);
                            return;
                        }
                    }
                    this.fichaMedica = response.data;
                    console.log('ficha medica', this.fichaMedica);
                    if (this.fichaMedica) {
                        console.log('ok todo entra');

                        this.conductasForm.patchValue({
                            sexual:
                                this.fichaMedica.irs ?? this.fichaMedica?.irs,
                            alergiaMedicamentos: this.fichaMedica
                                .alergiaMedicamentos
                                ? this.fichaMedica.alergiaMedicamentos
                                : false,
                            detalleAlergias:
                                this.fichaMedica.medicamentosAlergicos ??
                                this.fichaMedica.medicamentosAlergicos,
                            // peso: this.fichaMedica.peso ?? this.fichaMedica.peso,
                            // talla: this.fichaMedica.talla ?? this.fichaMedica.talla,
                            // indiceMasaCorporal: this.fichaMedica.indiceMasaCorporal ?? this.fichaMedica.indiceMasaCorporal,
                            // saturacionOxigeno: this.fichaMedica.saturacionOxigeno ?? this.fichaMedica.saturacionOxigeno,
                            // presion: this.fichaMedica.presion ?? this.fichaMedica.presion,
                            psicoactivas: this.fichaMedica.icd,
                        });

                        if (!this.fichaMedica.habitosNocivos) {
                        }
                        if (!this.fichaMedica.tomaAlcohol) {
                        }
                        if (!this.fichaMedica.tabaco) {
                        }
                        if (this.fichaMedica.alergiaMedicamentos) {
                        }
                        if (this.fichaMedica.fracturas) {
                        }
                        if (this.fichaMedica.cirugiaQuirurgica) {
                        }

                        if (this.fichaMedica.tokenIdFichaIdentificacion) {
                            this.obtenerEnfermedadesPersonasRelacionadas(
                                this.uuid_fp
                            );
                            this.obtenerEnfermedadesFichas(
                                this.fichaMedica.tokenIdFichaIdentificacion
                            );
                            this._evaluacionMedicaService.setToken(
                                this.fichaMedica.tokenIdentificador
                            );
                            this.obtenerEvaluacionMedicaProgreso();
                        }

                        this.changeDetector.detectChanges();
                    }
                },
                error: (error: any) => {
                    console.log('error peticion');
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

    getkeyLabelsTablePersonasEnfermedad() {
        return Object.keys(this.keyLabelsTablePersonasEnfermedad);
    }

    async obtenerEnfermedadesFichas(tokenIdentificador: string) {
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.sizeEnfermedadFicha;
        paginacionRequest.page = this.pageEnfermedadFicha;
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

    async obtenerEvaluacionMedicaProgreso() {
        this.isLoading = true;
        this.tokenFichaMedica = this._evaluacionMedicaService.getToken();
        let paginacionRequest = new PaginacionRequest();
        paginacionRequest.size = this.size;
        paginacionRequest.page = this.page;
        paginacionRequest.tokenIdentificador = this.tokenFichaMedica;
        this._evaluacionMedicaService
            .getEvaluacionMedicaProgresoByFichaMedica(paginacionRequest)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<
                        PaginacionResponse<EvaluacionMedicaProgresoDTO>
                    >
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }
                    this.evaluaciones = response.data.data;
                    this.datasource.data = this.evaluaciones;
                    this.changeDetector.detectChanges();
                    this.totalItems = response.data.totalItems;
                    console.log('evaluaciones', this.evaluaciones);
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
                complete: () => {
                    this.isLoading = false;
                },
            });
    }

    getKeys() {
        return Object.keys(this.keyLabelsTable);
    }

    handlePageEvent(pageEvent: PageEvent) {
        this.size = pageEvent.pageSize;
        this.page = pageEvent.pageIndex;
        this.obtenerEvaluacionMedicaProgreso();
    }

    getFormatedDate(date: Date) {
        return moment(date, 'YYYY-MM-DDTHH:mm:ssZ').toDate().toLocaleString();
    }

    mostrarFormularioCreacion() {
        this.mostrarListado = false;
    }

    mostrarListadoDeNuevo() {
        this.mostrarListado = true;
    }
}
