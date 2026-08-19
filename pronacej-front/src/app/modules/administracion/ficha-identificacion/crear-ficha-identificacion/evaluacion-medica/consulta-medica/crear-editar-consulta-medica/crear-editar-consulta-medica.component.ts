import { CommonModule } from '@angular/common';
import {
    AfterViewInit,
    Component,
    EventEmitter,
    Inject,
    LOCALE_ID,
    OnInit,
    Output,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/modules/catalogo/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { EvaluacionMedicaService } from '../../evaluacion-medica.service';
import { ConsultaAtencionIntegralDTO } from 'app/core/model/both/EJE/ConsultaAtencionIntegralDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { catchError, combineLatest, map, Observable, of, Subject, take, takeUntil } from 'rxjs';
import { List } from 'lodash';
import { MatDialog } from '@angular/material/dialog';
import { ModalCrearDetalleRecetaComponent } from '../../form-evaluacion-medica/modal-crear-detalle-receta/modal-crear-detalle-receta.component';
import { RecetaDTO } from 'app/core/model/both/EJE/recetaDTO.model';
import { MatStepperModule } from '@angular/material/stepper';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { DetalleRecetaDTO } from 'app/core/model/both/EJE/detalleRecetaDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { ModalCrearDetalleOrdenComponent } from '../../form-evaluacion-medica/modal-crear-detalle-orden/modal-crear-detalle-orden.component';
import { OrdenMedicaDTO, OrdenMedicaDetalleDTO } from 'app/core/model/both/EJE/ordenMedicaDTO.model';

@Component({
    selector: 'app-crear-editar-consulta-medica',
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
        MatStepperModule,


    ],
    templateUrl: './crear-editar-consulta-medica.component.html',
    styleUrl: './crear-editar-consulta-medica.component.scss',
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
    ]
})
export class CrearEditarConsultaMedicaComponent implements OnInit, AfterViewInit {

    consultaAtencionForm: FormGroup;
    @Output() volver = new EventEmitter<void>();
    tituloPantalla = 'Consulta Médica';
    tokenFichaMedica: string = '';
    tokenConsultaAtencion: string = '';
    uuid_fp: string;

    consultaAtencion: ConsultaAtencionIntegralDTO;
    isLoading: boolean = false;
    soloLectura: boolean = false;

    private destroy$ = new Subject<void>();

    formReceta: FormGroup;
    listSizeDetallesReceta = [5, 10, 15, 20];
    listaDetallesReceta = [];
    dataSourceDetallesReceta = [];
    displayedColumnsDetallesReceta = ['acciones', 'medicamento', 'dosis', 'indicaciones', 'concentracion', 'formaFarmaceutica'];
    pageDetallesReceta = 0;
    sizeDetallesReceta = this.listSizeDetallesReceta[0];
    totalItemsDetallesReceta = 0;

    formOrden: FormGroup;
    listSizeDetallesOrden = [5, 10, 15, 20];
    listaDetallesOrden = [];
    dataSourceDetallesOrden = [];
    displayedColumnsDetallesOrden = ['acciones', 'especialidad', 'producto'];
    pageDetallesOrden = 0;
    sizeDetallesOrden = this.listSizeDetallesOrden[0];
    totalItemsDetallesOrden = 0;

    nemonicoEsepecialidadRecetas: string = 'ESPECIALIDAD_MEDICA';
    catalogosEspecialidadReceta: CatalogoDTO[] = [];
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

    centro: JerarquiaDTO;

    constructor(
        public funcionesUtils: FuncionesUtils,
        private formBuilder: FormBuilder,
        private router: Router,
        private dialogMensajeService: DialogMensajeService,
        private catalogoService: CatalogoService,
        private _evaluacionMedicaService: EvaluacionMedicaService,
        private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
        private route: ActivatedRoute,
        @Inject(LOCALE_ID) private locale: string,
        public dialog: MatDialog,
        private readonly _fuseConfirmationService: FuseConfirmationService,
        private http: HttpClient,
        private fichaIdentificacionService: FichaIdentificacionService,
        public pdfService: PdfService,
        private jerarquiaService: JerarquiaService,
    ) {
        this.construirForm();
        this.contruirFormReceta();
        this.contruirFormOrden();
    }

    async ngOnInit(): Promise<void> {
        this.funcionesUtils.vincularClasificacionIMC(
            this.consultaAtencionForm,
            'IMC',                
            'clasificacionIMC'   
        );

        this._evaluacionMedicaService.consultaAtencionSoloLectura$
            .pipe(takeUntil(this.destroy$))
            .subscribe((soloLectura) => {
                this.soloLectura = soloLectura;

                if (this.soloLectura) {
                    this.consultaAtencionForm.disable({ emitEvent: false });
                    this.formReceta.disable({ emitEvent: false });
                    this.formOrden.disable({ emitEvent: false });
                } else {
                    this.consultaAtencionForm.enable({ emitEvent: false });
                    this.formReceta.enable({ emitEvent: false });
                    this.formOrden.enable({ emitEvent: false });
                }
            });

        this._evaluacionMedicaService.fichaMedica$.subscribe((ficha) => {
            if (ficha) {
                this.tokenFichaMedica = ficha;
            }
        });
        this._evaluacionMedicaService.consultaAtencionSubject$.pipe(take(1))
            .subscribe((ficha) => {
                if (ficha) {
                    this.tokenConsultaAtencion = ficha;
                    this.obtenerConsultaAtencion();
                }
            });
        this.uuid_fp = this.route.snapshot.params['uuid_fp'];
        this.calcularIMCAutomaticamente();
        this.getCatalogoEspecialidadesReceta();
        this.cargarCentro();
    }

    ngAfterViewInit(): void {

    }



    construirForm() {
        this.consultaAtencionForm = this.formBuilder.group({
            fechaInicio: [new Date().toISOString().substring(0, 10)],
            observaciones: [null, Validators.maxLength(500)],
            motivoConsulta: [null, Validators.required],
            edad: [null, Validators.pattern('^[0-9]+$')],
            tipoEnfermedad: [null],
            formaDeInicio: [null],
            estadoDeAnimo: [null],
            sed: [false],
            sueno: [false],
            apetito: [false],
            orina: [null],
            deposiciones: [null],
            fiebre15dias: [null],
            tos15dias: [null],
            secrecionGenitales: [null],
            perdidaPeso: [null],
            peso: [null, Validators.pattern('^[0-9]+(\\.[0-9]{1,2})?$')],
            talla: [null, Validators.pattern('^[0-9]+(\\.[0-9]{1,2})?$')],
            presion: [null],
            IMC: [null],
            clasificacionIMC: [null],
            temperatura: [null, Validators.pattern('^[0-9]+(\\.[0-9]{1,2})?$')],
            diagnostico: [null, Validators.required],
            tratamiento: [null, Validators.required],
            examenesAuxiliares: [null],
            fechaProximaCita: [null],
            tiempoEnfermedad: [null],
            horaInicio: [
                new Date().toTimeString().substring(0, 5)
            ],
            lugarAtencion: [null],
            doctorAtencion: [null],
        });
    }

    async obtenerConsultaAtencion() {
        this.isLoading = true;
        this._evaluacionMedicaService
            .getConsultaByTokenId(
                this._evaluacionMedicaService.getTokenConsultaAtencion()
            ).subscribe({
                next: (response: RespuestaPorDefecto<ConsultaAtencionIntegralDTO>
                ) => {
                    if (!response.exito) {
                        this._evaluacionMedicaService.checkError(response);
                        return;
                    }

                    this.consultaAtencion = response.data;
                    if (this.consultaAtencion) {
                        this.consultaAtencionForm.patchValue({
                            fechaInicio: this.consultaAtencion?.fechaInicio ? this.consultaAtencion?.fechaInicio.toString().split('T')[0] : null,
                            observaciones: this.consultaAtencion?.observaciones || null,
                            motivoConsulta: this.consultaAtencion?.motivoConsulta || null,
                            edad: this.consultaAtencion?.edad || null,
                            tipoEnfermedad: this.consultaAtencion?.tipoEnfermedad || null,
                            formaDeInicio: this.consultaAtencion?.formaDeInicio || null,
                            estadoDeAnimo: this.consultaAtencion?.estadoDeAnimo || null,
                            sed: this.consultaAtencion?.sed || false,
                            sueno: this.consultaAtencion?.sueno || false,
                            apetito: this.consultaAtencion?.apetito || false,
                            orina: this.consultaAtencion?.orina || null,
                            deposiciones: this.consultaAtencion?.deposiciones || null,
                            fiebre15dias: this.consultaAtencion?.fiebre15dias || null,
                            tos15dias: this.consultaAtencion?.tos15dias || null,
                            secrecionGenitales: this.consultaAtencion?.secrecionGenitales || null,
                            perdidaPeso: this.consultaAtencion?.perdidaPeso || null,
                            peso: this.consultaAtencion?.peso || null,
                            talla: this.consultaAtencion?.talla || null,
                            presion: this.consultaAtencion?.presion || null,
                            IMC: this.consultaAtencion?.imc || null,
                            temperatura: this.consultaAtencion?.temperatura || null,
                            diagnostico: this.consultaAtencion?.diagnostico || null,
                            tratamiento: this.consultaAtencion?.tratamiento || null,
                            examenesAuxiliares: this.consultaAtencion?.examenesAuxiliares || null,
                            fechaProximaCita: this.consultaAtencion?.fechaProximaCita ? new Date(this.consultaAtencion?.fechaProximaCita) : null,
                            tiempoEnfermedad: this.consultaAtencion?.tiempoEnfermedad || null,
                            // lugarAtencion: this.consultaAtencion?.lugarAtencion || null,
                            // doctorAtencion: this.consultaAtencion?.doctorAtencion || null,

                        });
                        if (this.consultaAtencion.receta) {
                            console.log('receta medica', this.consultaAtencion.receta);
                            const especialidadSeleccionada = this.catalogosEspecialidadReceta.find(
                                cat => cat.tokenIdentificador === this.consultaAtencion?.receta?.especialidad?.tokenIdentificador
                            );
                            console.log('cat encontrado', especialidadSeleccionada);
                            this.formReceta.patchValue({
                                numeroReceta: this.consultaAtencion.receta.numeroReceta,
                                especialidad: especialidadSeleccionada,
                                fechaEmision: this.consultaAtencion.receta.fechaEmision ? new Date(this.consultaAtencion.receta.fechaEmision) : null,
                                observaciones: this.consultaAtencion.receta.observaciones,
                            });

                            // Cargar detalles de la receta
                            this.listaDetallesReceta = this.consultaAtencion.receta.detalles || [];
                            this.totalItemsDetallesReceta = this.listaDetallesReceta.length;

                            // Aplicar paginación inicial a la tabla de detalles
                            const startIndex = this.pageDetallesReceta * this.sizeDetallesReceta;
                            const endIndex = startIndex + this.sizeDetallesReceta;
                            this.dataSourceDetallesReceta = this.listaDetallesReceta.slice(startIndex, endIndex);
                        }

                        if (this.consultaAtencion.orden) {
                            console.log('orden medica', this.consultaAtencion.orden);
                            
                            this.formOrden.patchValue({
                                numeroOrden: this.consultaAtencion.orden.numeroOrden,
                                fechaEmision: this.consultaAtencion.orden.fechaEmision ? new Date(this.consultaAtencion.orden.fechaEmision) : null,
                                observaciones: this.consultaAtencion.orden.observaciones,
                            });

                            // Cargar detalles de la orden
                            this.listaDetallesOrden = this.consultaAtencion.orden.detalles || [];
                            this.totalItemsDetallesOrden = this.listaDetallesOrden.length;

                            // Aplicar paginación inicial a la tabla de detalles
                            const startIndex = this.pageDetallesOrden * this.sizeDetallesOrden;
                            const endIndex = startIndex + this.sizeDetallesOrden;
                            this.dataSourceDetallesOrden = this.listaDetallesOrden.slice(startIndex, endIndex);
                        }
                    }

                    console.log('atencion', this.consultaAtencion);

                    this._evaluacionMedicaService.setTokenConsultaAtencion(
                        this.consultaAtencion.tokenIdentificador
                    );


                }, error: (error: any) => {
                    this._evaluacionMedicaService.checkError(error);
                    this.isLoading = false;
                },
                complete: () => {
                    this.isLoading = false;
                },
            });
    }

    async ejecutarAccion() {
        if (this.soloLectura) {
            return;
        }

        let consultaAtencionDTO: ConsultaAtencionIntegralDTO = new ConsultaAtencionIntegralDTO();

        // Verificar si es edición
        if (this.consultaAtencion) {
            consultaAtencionDTO.tokenIdentificador = this.consultaAtencion.tokenIdentificador;
            consultaAtencionDTO.esEdicion = true;
        }

        let fechaIngreso = this.consultaAtencionForm.controls['fechaInicio'].value;
        let horaIngreso = this.consultaAtencionForm.controls['horaInicio'].value;
        let fechaCompletaString = `${fechaIngreso}T${horaIngreso}:00`;
        let fechaIngresoCompleta = new Date(fechaCompletaString);

        // Llenar el DTO desde el formulario
        consultaAtencionDTO.tokenIdFichaMedica = this.tokenFichaMedica;
        consultaAtencionDTO.fechaInicio = fechaIngresoCompleta;
        consultaAtencionDTO.observaciones = this.consultaAtencionForm.controls['observaciones'].value;
        consultaAtencionDTO.motivoConsulta = this.consultaAtencionForm.controls['motivoConsulta'].value;
        consultaAtencionDTO.edad = this.consultaAtencionForm.controls['edad'].value;
        consultaAtencionDTO.tipoEnfermedad = this.consultaAtencionForm.controls['tipoEnfermedad'].value;
        consultaAtencionDTO.formaDeInicio = this.consultaAtencionForm.controls['formaDeInicio'].value;
        consultaAtencionDTO.estadoDeAnimo = this.consultaAtencionForm.controls['estadoDeAnimo'].value;
        consultaAtencionDTO.sed = this.consultaAtencionForm.controls['sed'].value;
        consultaAtencionDTO.sueno = this.consultaAtencionForm.controls['sueno'].value;
        consultaAtencionDTO.apetito = this.consultaAtencionForm.controls['apetito'].value;
        consultaAtencionDTO.orina = this.consultaAtencionForm.controls['orina'].value;
        consultaAtencionDTO.deposiciones = this.consultaAtencionForm.controls['deposiciones'].value;
        consultaAtencionDTO.fiebre15dias = this.consultaAtencionForm.controls['fiebre15dias'].value;
        consultaAtencionDTO.tos15dias = this.consultaAtencionForm.controls['tos15dias'].value;
        consultaAtencionDTO.secrecionGenitales = this.consultaAtencionForm.controls['secrecionGenitales'].value;
        consultaAtencionDTO.perdidaPeso = this.consultaAtencionForm.controls['perdidaPeso'].value;
        consultaAtencionDTO.peso = this.consultaAtencionForm.controls['peso'].value;
        consultaAtencionDTO.talla = this.consultaAtencionForm.controls['talla'].value;
        consultaAtencionDTO.presion = this.consultaAtencionForm.controls['presion'].value;
        consultaAtencionDTO.imc = this.consultaAtencionForm.controls['IMC'].value;
        consultaAtencionDTO.temperatura = this.consultaAtencionForm.controls['temperatura'].value;
        consultaAtencionDTO.diagnostico = this.consultaAtencionForm.controls['diagnostico'].value;
        consultaAtencionDTO.tratamiento = this.consultaAtencionForm.controls['tratamiento'].value;
        consultaAtencionDTO.examenesAuxiliares = this.consultaAtencionForm.controls['examenesAuxiliares'].value;
        consultaAtencionDTO.fechaProximaCita = this.consultaAtencionForm.controls['fechaProximaCita'].value;
        consultaAtencionDTO.tiempoEnfermedad = this.consultaAtencionForm.controls['tiempoEnfermedad'].value;
        consultaAtencionDTO.lugarAtencion = this.consultaAtencionForm.controls['lugarAtencion'].value;
        consultaAtencionDTO.doctorAtencion = this.consultaAtencionForm.controls['doctorAtencion'].value;


        const receta: RecetaDTO = {
            numeroReceta: this.formReceta.get('numeroReceta')?.value,
            especialidad: this.formReceta.get('especialidad')?.value,
            fechaEmision: this.formReceta.get('fechaEmision')?.value,
            observaciones: this.formReceta.get('observaciones')?.value,
            detalles: this.listaDetallesReceta // Array de DetalleRecetaDTO ya preparado
        };

        if (this.validarReceta(receta)) {
            consultaAtencionDTO.receta = receta
        } else {
            const confirmado = await this.confirmarAccion();
            consultaAtencionDTO.receta = confirmado ? receta : null;
        }

        const orden: OrdenMedicaDTO = {
            numeroOrden: this.formOrden.get('numeroOrden')?.value,
            fechaEmision: this.formOrden.get('fechaEmision')?.value,
            observaciones: this.formOrden.get('observaciones')?.value,
            detalles: this.listaDetallesOrden 
        };

        if (this.validarOrden(orden)) {
            consultaAtencionDTO.orden = orden
        } else {
            const confirmado = await this.confirmarAccion();
            consultaAtencionDTO.orden = confirmado ? orden : null;
        }


        // console.log('consultaAtencionDTO', consultaAtencionDTO);

        this._evaluacionMedicaService
            .postConsultaAtencion(consultaAtencionDTO)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<ConsultaAtencionIntegralDTO>
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
                    this.tokenConsultaAtencion = null;
                    this.cancelarEdicion();
                },
                error: (error: any) => {
                    this._evaluacionMedicaService.checkError(
                        error
                    );
                },
            });

    }

    cancelarEdicion() {
        this.volver.emit();
    }

    volverAlListado() {
        this.volver.emit(); // Notifica al padre que se debe mostrar el listado
    }

    soloNumero(event: KeyboardEvent): void {
        const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete', '.'];
        const isNumberKey = event.key >= '0' && event.key <= '9';

        if (!isNumberKey && !allowedKeys.includes(event.key)) {
            event.preventDefault();
        }
    }

    calcularIMCAutomaticamente(): void {
        combineLatest([
            this.consultaAtencionForm.get('talla').valueChanges,
            this.consultaAtencionForm.get('peso').valueChanges
        ]).subscribe(([talla, peso]) => {
            if (talla && peso) {
                const tallam = parseFloat(talla);
                const pesom = parseFloat(peso);
                // Verifica que talla sea mayor que cero para evitar división por cero
                if (!isNaN(tallam) && tallam > 0 && !isNaN(pesom)) {
                    const imc = pesom / (tallam * tallam);
                    // Establece el IMC formateado a dos decimales
                    this.consultaAtencionForm.get('IMC').setValue(imc.toFixed(2), { emitEvent: true });
                } else {
                    this.consultaAtencionForm.get('IMC').setValue(null, { emitEvent: false });
                }
            } else {
                this.consultaAtencionForm.get('IMC').setValue(null, { emitEvent: false });
            }
        });
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

    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this.destroy$.next();
        this.destroy$.complete();
    }

    async getCatalogoEspecialidadesReceta() {
        this.getCatalogos(this.nemonicoEsepecialidadRecetas).subscribe(
            (catalogos: CatalogoDTO[]) => {
                this.catalogosEspecialidadReceta = catalogos;
            }
        );
    }

    getCatalogos(nemonico: string): Observable<CatalogoDTO[]> {
        return this.catalogoService.getCatalogosPorNemonicPadre(nemonico,this.nemonicoMenu).pipe(
            map((response: RespuestaPorDefecto<List<CatalogoDTO>>) => {
                if (!response.exito) {
                    this.catalogoService.checkError(response);
                    return [];
                }
                return Array.from(response.data);
            }),
            catchError((error: any) => {
                this.catalogoService.checkError(error);
                return of([]);
            })
        );
    }

    contruirFormReceta() {
        this.formReceta = this.formBuilder.group({
            numeroReceta: [null, Validators.required],
            fechaEmision: [null], // Puedes agregar validaciones si lo requieres
            observaciones: [null],
            especialidad: [null],
            // detalles: this._formBuilder.array([]) // Inicialmente vacío, agregarás detalles dinámicamente
        });
    }

    contruirFormOrden() {
        this.formOrden = this.formBuilder.group({
            numeroOrden: [null, Validators.required],
            fechaEmision: [null],
            observaciones: [null],
        });
    }

    compararCatalogos(o1: CatalogoDTO, o2: CatalogoDTO): boolean {
        return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
    }

    agregarDetalleOrden() {
        if (this.soloLectura) {
            return;
        }

        this.dialog.open(ModalCrearDetalleOrdenComponent, {
            width: '600px',
            disableClose: true,
            data: {}
        }).afterClosed().subscribe((resultado) => {
            if (resultado) {
                // resultado es un DetalleRecetaDTO
                this.listaDetallesOrden.push(resultado);
                this.dataSourceDetallesOrden = [...this.listaDetallesOrden];
            }
        });
    }

    editarDetalleOrden(detalle) {
        if (this.soloLectura) {
            return;
        }

        this.dialog.open(ModalCrearDetalleOrdenComponent, {
            width: '600px',
            disableClose: true,
            data: { informacion: detalle }
        }).afterClosed().subscribe((resultado) => {
            if (resultado) {
                // Actualizar el detalle en la lista
                const index = this.listaDetallesOrden.findIndex(d => d.tokenIdentificador === detalle.tokenIdentificador);
                if (index > -1) {
                    this.listaDetallesOrden[index] = resultado;
                }
                this.dataSourceDetallesOrden = [...this.listaDetallesOrden];
            }
        });
    }

    borrarDetalleOrden(detalle) {
        if (this.soloLectura) {
            return;
        }

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
                const index = this.listaDetallesOrden.indexOf(detalle);
                if (index > -1) {
                    // Marcar como eliminado o remover de la lista
                    this.listaDetallesOrden.splice(index, 1);
                    this.dataSourceDetallesOrden = [...this.listaDetallesOrden];
                }
            }
        });

    }

    handlePageEventDetallesOrden(event: PageEvent) {
        this.pageDetallesOrden = event.pageIndex;
        this.sizeDetallesOrden = event.pageSize;

        const startIndex = this.pageDetallesOrden * this.sizeDetallesOrden;
        const endIndex = startIndex + this.sizeDetallesOrden;

        // Mostrar solo la porción de la lista correspondiente a la página actual
        this.dataSourceDetallesOrden = this.listaDetallesOrden.slice(startIndex, endIndex);
    }

    validarOrden(orden: OrdenMedicaDTO): boolean {
        if (!orden.numeroOrden && !orden.fechaEmision && !orden.observaciones) {
            return false; // Algún campo obligatorio está vacío
        }

        if (!orden.detalles || orden.detalles.length === 0) {
            return false; // La lista de detalles no debe estar vacía
        }

        return true; // Pasa la validación
    }

    agregarDetalleReceta() {
        if (this.soloLectura) {
            return;
        }

        this.dialog.open(ModalCrearDetalleRecetaComponent, {
            width: '600px',
            disableClose: true,
            data: {}
        }).afterClosed().subscribe((resultado) => {
            if (resultado) {
                // resultado es un DetalleRecetaDTO
                this.listaDetallesReceta.push(resultado);
                this.dataSourceDetallesReceta = [...this.listaDetallesReceta];
            }
        });
    }

    editarDetalleReceta(detalle) {
        if (this.soloLectura) {
            return;
        }

        this.dialog.open(ModalCrearDetalleRecetaComponent, {
            width: '600px',
            disableClose: true,
            data: { informacion: detalle }
        }).afterClosed().subscribe((resultado) => {
            if (resultado) {
                // Actualizar el detalle en la lista
                const index = this.listaDetallesReceta.findIndex(d => d.tokenIdentificador === detalle.tokenIdentificador);
                if (index > -1) {
                    this.listaDetallesReceta[index] = resultado;
                }
                this.dataSourceDetallesReceta = [...this.listaDetallesReceta];
            }
        });
    }

    borrarDetalleReceta(detalle) {
        if (this.soloLectura) {
            return;
        }

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
                const index = this.listaDetallesReceta.indexOf(detalle);
                if (index > -1) {
                    // Marcar como eliminado o remover de la lista
                    this.listaDetallesReceta.splice(index, 1);
                    this.dataSourceDetallesReceta = [...this.listaDetallesReceta];
                }
            }
        });

    }

    handlePageEventDetallesReceta(event: PageEvent) {
        this.pageDetallesReceta = event.pageIndex;
        this.sizeDetallesReceta = event.pageSize;

        const startIndex = this.pageDetallesReceta * this.sizeDetallesReceta;
        const endIndex = startIndex + this.sizeDetallesReceta;

        // Mostrar solo la porción de la lista correspondiente a la página actual
        this.dataSourceDetallesReceta = this.listaDetallesReceta.slice(startIndex, endIndex);
    }

    validarReceta(receta: RecetaDTO): boolean {
        if (!receta.numeroReceta && !receta.especialidad && !receta.fechaEmision && !receta.observaciones) {
            return false; // Algún campo obligatorio está vacío
        }

        if (!receta.detalles || receta.detalles.length === 0) {
            return false; // La lista de detalles no debe estar vacía
        }

        return true; // Pasa la validación
    }

    confirmarAccion(): Promise<boolean> {
        return new Promise((resolve) => {
            const confirmation = this._fuseConfirmationService.open({
                title: 'La receta está incompleta',
                message: '¿Deseas guardarla?',
                actions: {
                    confirm: { label: 'Si' },
                    cancel: { label: 'Cancelar' }
                }
            });

            confirmation.afterClosed().subscribe((result) => {
                resolve(result === 'confirmed');
            });
        });
    }

    actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
        if (event.value) {
            if (controlName == 'fechaEmision') {
                const fecha = event.value;
                this.formReceta.get(controlName).setValue(fecha);
            } else {
                const fecha = event.value;
                this.consultaAtencionForm.get(controlName).setValue(fecha);
            }
        }
    }

    async imprimirReceta() {
        try {
            // Mostrar diálogo de carga
            const dialogoCarga = this.dialogMensajeService.mensajeLoading('Generando PDF...');

            // Obtener imagen del logo para incluir en el PDF
            this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
                .subscribe({
                    next: async (datos: ArrayBuffer) => {
                        // Convertir imagen a base64 para incluirla en el PDF
                        const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                        const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                        try {


                            const datosCabecera = await this.obtenerDatosCabecera();

                            let tablaPersonasRelacionadas = new TablaPlantilla();
                            tablaPersonasRelacionadas.encabezados = [
                                'Medicamento', 'Dosis', 'Indicaciones', 'Concentración', 'Forma farmacéutica'
                            ];

                            // Mapear los datos de las personas relacionadas a la tabla con solo las columnas requeridas
                            tablaPersonasRelacionadas.filas = this.listaDetallesReceta.map((persona: DetalleRecetaDTO) => {


                                return {
                                    'Medicamento': persona.medicamentoCompleto ? persona.medicamentoCompleto.nombre : persona.medicamento,
                                    'Dosis': persona.dosis,
                                    'Indicaciones': persona.indicaciones,
                                    'Concentración': persona.concentracion,
                                    'Forma farmacéutica': persona.formaFarmaceutica?.nombre,

                                };
                            });


                            // Configurar solicitud para la generación del PDF
                            const solicitudPdf = new GeneracionPdfRequest();
                            solicitudPdf.nemonico = 'FORMULARIO_RECETA_MEDICA';

                            let fecha = ""

                            if ((this.formReceta.get('fechaEmision')?.value)) {
                                fecha = this.funcionesUtils.formatearFecha(this.formReceta.get('fechaEmision')?.value);
                            }

                            // Preparar variables para el PDF
                            solicitudPdf.variables = {
                                // Datos de cabecera
                                ...datosCabecera,
                                "[IMG_BASE64]": imagenBase64,
                                "[FECHA-INGRESO]": this.funcionesUtils.formatearFecha(new Date()),
                                "[HORA-INGRESO]": new Date().toLocaleTimeString('es-ES'),
                                "[CENTRO]": this.centro?.nombre || 'No especificado',
                                "[TITULO-INFORME]": 'Receta Médica',
                                "[TITULO-PLANTILLA]": 'Receta Médica',


                                // Tabla de personas relacionadas (serializada como JSON)
                                "[TABLA-DETALLE-RECETA]": JSON.stringify(tablaPersonasRelacionadas),

                                // Información familiar general
                                "[NUMERO-RECETA]": this.formReceta.get('numeroReceta')?.value || 'No especificado',
                                "[ESPECIALIDAD]": (this.formReceta.get('especialidad')?.value as CatalogoDTO)?.nombre || 'No especificado',
                                "[FECHA-EMISION]": fecha || 'No especificado',
                                "[OBSERVACIONES]": this.formReceta.get('observaciones')?.value || 'No especificado',

                            };

                            // Generar el PDF usando el servicio
                            this.pdfService.generarPdf(solicitudPdf, etiquetasModel.MENU_FICHA_HISTORIA_CLINICA).subscribe({
                                next: (respuesta: RespuestaPorDefecto<string>) => {
                                    dialogoCarga.close();

                                    if (!respuesta.exito) {
                                        this.dialogMensajeService.mensajeError(
                                            'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                                        );
                                        return;
                                    }

                                    // Abrir el PDF en una nueva ventana
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

    async imprimirOrden() {
        try {
            // Mostrar diálogo de carga
            const dialogoCarga = this.dialogMensajeService.mensajeLoading('Generando PDF...');

            // Obtener imagen del logo para incluir en el PDF
            this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
                .subscribe({
                    next: async (datos: ArrayBuffer) => {
                        // Convertir imagen a base64 para incluirla en el PDF
                        const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                        const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                        try {
                            const datosCabecera = await this.obtenerDatosCabecera();

                            let tablaEspecialidadProductos = new TablaPlantilla();
                            tablaEspecialidadProductos.encabezados = [
                                'Especialidad', 'Producto'
                            ];

                            // Mapear los datos de las personas relacionadas a la tabla con solo las columnas requeridas
                            tablaEspecialidadProductos.filas = this.listaDetallesOrden.map((persona: OrdenMedicaDetalleDTO) => {

                                return {
                                    'Especialidad': persona.especialidadProducto?.especialidad || 'No especificada',
                                    'Producto': persona.especialidadProducto?.producto || 'No especificado',
                                };
                            });

                            // Configurar solicitud para la generación del PDF
                            const solicitudPdf = new GeneracionPdfRequest();
                            solicitudPdf.nemonico = 'FORMULARIO_CONSULTA_MEDICA_ORDEN';

                            let fecha = ""

                            if ((this.formOrden.get('fechaEmision')?.value)) {
                                fecha = this.funcionesUtils.formatearFecha(this.formOrden.get('fechaEmision')?.value);
                            }

                            // Preparar variables para el PDF
                            solicitudPdf.variables = {
                                // Datos de cabecera
                                ...datosCabecera,
                                "[IMG_BASE64]": imagenBase64,
                                "[FECHA_REGISTRO]": this.funcionesUtils.formatearFecha(new Date()),
                                "[HORA_REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                                "[CENTRO]": this.centro?.nombre || 'No especificado',
                                "[TITULO-INFORME]": 'Orden Médica',
                                "[TITULO-PLANTILLA]": 'Orden Médica',

                                // Tabla de personas relacionadas (serializada como JSON)
                                "[TABLA-ESPECIALIDAD-PRODUCTOS]": JSON.stringify(tablaEspecialidadProductos),

                                // Información familiar general
                                "[NUMERO_ORDEN]": this.formOrden.get('numeroOrden')?.value || 'No especificado',
                                "[FECHA_EMISION]": fecha || 'No especificado',
                                "[COMENTARIO_IMPRESION_DIAGNOSTICA]": this.formOrden.get('observaciones')?.value || 'No especificado',

                            };

                            // Generar el PDF usando el servicio
                            this.pdfService.generarPdf(solicitudPdf, etiquetasModel.MENU_FICHA_HISTORIA_CLINICA).subscribe({
                                next: (respuesta: RespuestaPorDefecto<string>) => {
                                    dialogoCarga.close();

                                    if (!respuesta.exito) {
                                        this.dialogMensajeService.mensajeError(
                                            'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                                        );
                                        return;
                                    }

                                    // Abrir el PDF en una nueva ventana
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

    cargarCentro() {
        this.jerarquiaService
            .obtenerJerarquiaPorNumeroDeDocumento('')
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
                },
                error: (error: any) => {
                    this.jerarquiaService.checkError(error);
                },
            });
    }

    private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
        return new Promise((resolve, reject) => {
            this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
                next: async (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                    if (!response.exito) {
                        reject('Error al obtener la ficha de identificación');
                        return;
                    }

                    const fichaIdentificacion = response.data;

                    var datosCabeceraCatalogo = null;

                    const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
                    const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
                    const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';
                    const sexo = fichaIdentificacion.nombreSexo || 'N/A';

                    const datosCabecera = {
                        "[ADOLESCENTE]": nombreAdolescente,
                        "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
                        "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                        "[SEXO_ADOLESCENTE]": sexo
                    };

                    resolve(datosCabecera);
                },
                error: (error: any) => {
                    reject(error);
                }
            });
        });
    }

}
