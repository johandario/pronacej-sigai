import { CommonModule, formatDate } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import {
    AbstractControl,
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    ValidatorFn,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DatosHijoIngresadoDTO } from 'app/core/model/both/DatosHijoIngresadoDTO.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIngresoDocumentoDTO } from 'app/core/model/request/ia/FichaIngresoDocumentoDTO.model';
import { FichaIngresoDocumentosRequest } from 'app/core/model/request/ia/FichaIngresoDocumentosRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import {
    CUSTOM_DATE_FORMATS,
    CustomDateAdapter,
    FuncionesUtils,
} from 'app/core/utils/funcionesUtils.model';
import {
    DateAdapter,
    MAT_DATE_FORMATS,
    MAT_DATE_LOCALE,
    MatNativeDateModule,
} from '@angular/material/core';

@Component({
    selector: 'app-ficha-ingreso-crear-editar',
    standalone: true,
    imports: [
        CommonModule,
        MatExpansionModule,
        ReactiveFormsModule,
        MatInputModule,
        MatFormFieldModule,
        MatButtonModule,
        MatCheckboxModule,
        MatRadioModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatDatepickerModule,
        MatIconModule,
        SubidaDeDocumentosComponent,
        DocumentosSubidosTablaComponent,
    ],
    templateUrl: './ficha-ingreso-crear-editar.component.html',
    styleUrl: './ficha-ingreso-crear-editar.component.scss',
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    ],
})
export class FichaIngresoCrearEditarComponent implements OnInit, AfterViewInit {
    uuid_fp: string;

    medioCerrado = false;
    tieneLesiones = false;
    tieneMoretones = false;
    tieneCicatrices = false;
    tieneTatuajes = false;
    tienePiercing = false;
    tieneOtros = false;
    esVictimaAgresion = false;
    hijoEsVictimaAgresion = false;
    hijoMoretones = false;
    hijoCicatrices = false;
    hijoTatuajes = false;
    esEmbarazada: boolean = false;

    fichaIngresoGeneralForm: FormGroup;
    fichaIngresoAbiertoForm: FormGroup;
    fichaIngresoCerradoForm: FormGroup;
    datosHijoIngresadoForm: FormGroup;
    fichaIngresoDTO: FichaIngresoDTO;

    tituloPantalla = 'ficha de ingreso';

    esEdicion = false;
    esVisualizacion = false;
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_INGRESO;

    mostrarSubidaDocumentos: boolean = true;
    @ViewChild("documentosComp") tablaDocumentos: DocumentosSubidosTablaComponent;

    totalItems = 0;
    pageSizeOptions = [5, 10, 15, 20];
    pageSize = this.pageSizeOptions[0];
    pageIndex = 0;

    tiposDeDocumentosSistema: TipoDeDocumento[] = [];

    centro: JerarquiaDTO;
    listaTutores: CatalogoDTO[] = [];
    listaProgramasDerivados: CatalogoDTO[] = [];
    listaSegurosSalud: CatalogoDTO[] = [];
    listaFormaCabeza: CatalogoDTO[] = [];
    listaFormaNariz: CatalogoDTO[] = [];
    listaFormaLabios: CatalogoDTO[] = [];
    listaFormaCuerpo: CatalogoDTO[] = [];
    listaAnomaliaOjos: CatalogoDTO[] = [];
    listaTiposSexo: CatalogoDTO[] = [];
    listaParentescos: CatalogoDTO[] = [];

    listaFormaCabezaMujer: CatalogoDTO[] = [];
    listaFormaNarizMujer: CatalogoDTO[] = [];
    listaFormaLabiosMujer: CatalogoDTO[] = [];
    listaFormaCuerpoMujer: CatalogoDTO[] = [];
    listaAnomaliaOjosMujer: CatalogoDTO[] = [];

    fichaIdentificacionDTO: FichaIdentificacionDTO;
    listaTipoSexo: CatalogoDTO[] = [];
    sexoFicha: CatalogoDTO | undefined;
    centroFemenino = true;

    base64Image: string | null = null;

    listaEstadosAdolescente: CatalogoDTO[] = [];
    constructor(
        private formBuilder: FormBuilder,
        private dialogMensajeService: DialogMensajeService,
        private fichaIngresoService: FichaIngresoService,
        private jerarquiaService: JerarquiaService,
        private fichaIdentificacionService: FichaIdentificacionService,
        private router: Router,
        private route: ActivatedRoute,
        public funcionesUtils: FuncionesUtils,
        private pdfService: PdfService,
        private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
        private catalogoService: CatalogoService,
        private http: HttpClient,
    ) { }

    ngOnInit(): void {
        this.uuid_fp = this.route.snapshot.params['uuid_fp'];
        this.fichaIngresoDTO = history.state.fichaIngresoDTO;
        this.construirForm();
        console.log('ficha ingreso', this.fichaIngresoDTO);
        this.cargarCentro();
        this.cargarDatosCatalogo();
        this.datosHijoIngresadoForm.disable();
        this.mostrarSubidaDocumentos = true;
        if (Boolean(this.fichaIngresoDTO?.tokenIdentificador)) {
            // Si existe el objeto, significa que estamos en modo edición
            this.esVisualizacion = this.fichaIngresoDTO.esVisualizacion;
            if (this.esVisualizacion) {
                this.fichaIngresoGeneralForm.disable();
                this.fichaIngresoAbiertoForm.disable();
                this.fichaIngresoCerradoForm.disable();
                this.datosHijoIngresadoForm.disable();
            }

            this.empezarEdicion(this.fichaIngresoDTO);

        }
        this.obtenerFichaIdentificacionPorToken(this.uuid_fp);

    }

    ngAfterViewInit(): void {
        if (this.mostrarSubidaDocumentos) {
            this.obtenerDocumentos();
        }
        this.loadImageAsBase64();
    }

    cargarDatosCatalogo() {
        this.funcionesUtils
            .obtenerListaCatalogo('TUTORES', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaTutores = data),
                error: (error) =>
                    console.error('Error cargando tutores:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('PROGRAMA_DERIVADO', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaProgramasDerivados = data),
                error: (error) =>
                    console.error('Error cargando programas derivados:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('SEGURO_SALUD', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaSegurosSalud = data),
                error: (error) =>
                    console.error('Error cargando seguros de salud:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DE_LA_CABEZA', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaCabeza = data),
                error: (error) =>
                    console.error('Error cargando formas de la cabeza:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DE_LA_NARIZ', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaNariz = data),
                error: (error) =>
                    console.error('Error cargando formas de la nariz:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DE_LOS_LABIOS', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaLabios = data),
                error: (error) =>
                    console.error(
                        'Error cargando formas de los labios:',
                        error
                    ),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DEL_CUERPO', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaCuerpo = data),
                error: (error) =>
                    console.error('Error cargando formas del cuerpo:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('ANOMALIA_EN_OJOS', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaAnomaliaOjos = data),
                error: (error) =>
                    console.error(
                        'Error cargando anomalías en los ojos:',
                        error
                    ),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DE_LA_CABEZA_MUJER', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaCabezaMujer = data),
                error: (error) =>
                    console.error('Error cargando formas de la cabeza:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DE_LA_NARIZ_MUJER', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaNarizMujer = data),
                error: (error) =>
                    console.error('Error cargando formas de la nariz:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo(
                'FORMA_DE_LOS_LABIOS_MUJER',
                this.nemonicoMenu
            )
            .subscribe({
                next: (data) => (this.listaFormaLabiosMujer = data),
                error: (error) =>
                    console.error(
                        'Error cargando formas de los labios:',
                        error
                    ),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('FORMA_DEL_CUERPO_MUJER', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaFormaCuerpoMujer = data),
                error: (error) =>
                    console.error('Error cargando formas del cuerpo:', error),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('ANOMALIA_EN_OJOS_MUJER', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaAnomaliaOjosMujer = data),
                error: (error) =>
                    console.error(
                        'Error cargando anomalías en los ojos:',
                        error
                    ),
            });

        this.funcionesUtils
            .obtenerListaCatalogo('TIPO_SEXO', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaTiposSexo = data),
                error: (error) =>
                    console.error('Error cargando tipos de sexo:', error),
            });
        this.funcionesUtils
            .obtenerListaCatalogo('PARENTESCO', this.nemonicoMenu)
            .subscribe({
                next: (data) => (this.listaParentescos = data),
                error: (error) =>
                    console.error('Error cargando parentescos:', error),
            });
        // this.funcionesUtils.obtenerListaCatalogo('GENERO', this.nemonicoMenu).subscribe({
        //   next: (data) => this.listaTipoSexo = data,
        //   error: (error) => console.error('Error cargando parentescos:', error)
        // });

        this.funcionesUtils
            .obtenerListaCatalogo('ESTADO_ADOLESCENTE', this.nemonicoMenu)
            .subscribe({
                next: (data: any) => {
                    this.listaEstadosAdolescente = data;
                    this.listaEstadosAdolescente = this.listaEstadosAdolescente.filter((estado) => estado.nemonico === 'ESTADO_ADOLESCENTE_POST_EGRESO');
                },
                error: (error: any) => {
                    console.error('Error cargando los estados adolescentes:', error);
                }
            });
    }

    private actualizarParentescoPorSexo(tipoSexoTokenIdentificador: string) {
        // Buscar el tipo de sexo en la lista para obtener su nombre
        const tipoSexoSeleccionado = this.listaTiposSexo.find(
            (sexo) => sexo.tokenIdentificador === tipoSexoTokenIdentificador
        );

        if (!tipoSexoSeleccionado) return;

        // Buscar el parentesco correspondiente según el sexo
        let parentescoABuscar: string | undefined;

        // Asumiendo que estas son las palabras clave que pueden aparecer en el nombre del sexo
        if (tipoSexoSeleccionado.nemonico.toUpperCase().includes('TIPO_SEXO_MASCULINO')) {
            // Buscar el parentesco que contenga "HIJO" en su nombre
            const parentescoHijo = this.listaParentescos.find(
                (p) =>
                    p.nombre.toUpperCase().includes('HIJO') &&
                    !p.nombre.toUpperCase().includes('HIJA')
            );
            if (parentescoHijo) {
                parentescoABuscar = parentescoHijo.tokenIdentificador;
            }
        } else if (
            tipoSexoSeleccionado.nemonico.toUpperCase().includes('TIPO_SEXO_FEMENINO')
        ) {
            // Buscar el parentesco que contenga "HIJA" en su nombre
            const parentescoHija = this.listaParentescos.find((p) =>
                p.nombre.toUpperCase().includes('HIJA')
            );
            if (parentescoHija) {
                parentescoABuscar = parentescoHija.tokenIdentificador;
            }
        }

        // Actualizar el valor en el formulario si se encontró un parentesco
        if (parentescoABuscar) {
            this.datosHijoIngresadoForm.patchValue({
                hijoParentesco: parentescoABuscar,
            });
        }
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
                    if (!this.fichaIngresoDTO) {
                        this.fichaIngresoGeneralForm
                            .get('centro')
                            ?.setValue(this.centro.nombre);
                        this.fichaIngresoGeneralForm
                            .get('ubigeo')
                            .setValue(this.centro.direccion);
                        this.medioCerrado =
                            this.centro.jerarquiaPadre.nemonico == 'SOA'
                                ? false
                                : true;
                        if (this.centro.genero) {
                            this.centroFemenino =
                                this.centro.genero.nemonico == 'TIPO_SEXO_FEMENINO';
                        }
                    }
                    if (this.mostrarSubidaDocumentos) {
                        this.obtenerTiposDeDocumentos();

                    }
                    console.log('centro', this.centro);
                },
                error: (error: any) => {
                    this.jerarquiaService.checkError(error);
                },
            });
    }

    construirForm() {
        this.fichaIngresoGeneralForm = this.formBuilder.group({
            fechaIngreso: [new Date().toISOString().substring(0, 10)],
            horaIngreso: [
                new Date().toTimeString().substring(0, 5),
                [Validators.required],
            ],
            centro: ['', [Validators.required]],
            tipoCentro: [this.medioCerrado ? 'C' : 'A', []],
            ubigeo: ['', [Validators.required]],
            observaciones: [''],
            tipoEstadoAdolescente: [null],
        });
        this.fichaIngresoAbiertoForm = this.formBuilder.group({
            responsableInscripcion: [
                '',
                [Validators.required, this.validarNoEspacios()],
            ],
            // tutor: ["0", []],
            caracteristicasParticulares: ['', [, this.validarNoEspacios()]],
        });
        this.fichaIngresoCerradoForm = this.formBuilder.group({
            // atencionSalud: [null],
            // motivo: ["", ],
            programaDerivado: [null],
            lesiones: [null],
            especificarZonaLesiones: ['', [this.validarNoEspacios()]],
            moretones: [null],
            especificarZonaMoretones: ['', [this.validarNoEspacios()]],
            cicatrices: [null],
            especificarZonaCicatrices: ['', [this.validarNoEspacios()]],
            tatuajes: [null],
            especificarZonaTatuajes: ['', [this.validarNoEspacios()]],
            piercing: [null],
            especificarZonaPiercing: ['', [this.validarNoEspacios()]],
            otros: [null],
            especificarZonaOtros: ['', [this.validarNoEspacios()]],
            victimaAgresion: [null],
            especificarAgresion: ['', [this.validarNoEspacios()]],
            seguroSalud: ['0'],
            formaCabeza: [null],
            formaNariz: [null],
            formaLabios: [null],
            formaCuerpo: [null],
            anomaliaOjos: [null],
            esEmbarazada: [null],
            mesesEmbarazo: [0],
            ingresaConHijo: [null, []],
        });
        this.datosHijoIngresadoForm = this.formBuilder.group({
            hijoApellidoPaterno: ['', [this.validarNoEspacios()]],
            hijoApellidoMaterno: ['', [this.validarNoEspacios()]],
            hijoNombresCompletos: ['', [this.validarNoEspacios()]],
            hijoFechaNacimiento: [
                '',
                [Validators.required, this.validarFechaNacimientoHijo()],
            ],
            hijoDNI: [
                '',
                [
                    Validators.required,
                    Validators.minLength(8),
                    Validators.maxLength(8),
                    Validators.pattern('^[0-9]*$'),
                    this.validarNoEspacios(),
                ],
            ],
            hijoTipoSexo: ['0', [Validators.required]],
            // Campos adicionales internos (no visibles)
            hijoOcupacion: ['NINGUNA'],
            hijoParentesco: ['HIJO/A'],
            hijoTelefono: ['0000000000'],
            hijoEstadoCivil: ['SOLTERO/A'],
            hijoInstruccion: ['NINGUNA'],
            hijoRoles: ['NO APLICA'],
            hijoVictimaAgresion: [null],
            hijoEspecificarAgresion: ['', [this.validarNoEspacios()]],
            hijoMoretones: [null],
            hijoEspecificarZonaMoretones: ['', [this.validarNoEspacios()]],
            hijoCicatrices: [null],
            hijoEspecificarZonaCicatrices: ['', [this.validarNoEspacios()]],
            hijoTatuajes: [null],
            hijoEspecificarZonaTatuajes: ['', [this.validarNoEspacios()]],
            hijoOtroEspecificar: ['', [this.validarNoEspacios()]],
            hijoObservaciones: ['', [this.validarNoEspacios()]],

        });
        this.datosHijoIngresadoForm
            .get('hijoTipoSexo')
            ?.valueChanges.subscribe((value) => {
                if (value !== '0') {
                    this.actualizarParentescoPorSexo(value);
                }
            });
    }

    observadorCambioEnCampo(campo: string, event: any) {
        if (campo === 'tipoCentro') {
            this.medioCerrado = event.value === 'C';
            this.fichaIngresoGeneralForm.get('centro')?.setValue(0);
            this.fichaIngresoGeneralForm.get('ubigeo')?.setValue('');
        } else if (campo === 'lesiones') {
            this.tieneLesiones = event.value === 'S';
            if (!this.tieneLesiones) {
                this.fichaIngresoCerradoForm
                    .get('especificarZonaLesiones')
                    ?.setValue('');
            }
        } else if (campo === 'moretones') {
            this.tieneMoretones = event.value === 'S';
            if (!this.tieneMoretones) {
                this.fichaIngresoCerradoForm
                    .get('especificarZonaMoretones')
                    ?.setValue('');
            }
        } else if (campo === 'cicatrices') {
            this.tieneCicatrices = event.value === 'S';
            if (!this.tieneCicatrices) {
                this.fichaIngresoCerradoForm
                    .get('especificarZonaCicatrices')
                    ?.setValue('');
            }
        } else if (campo === 'tatuajes') {
            this.tieneTatuajes = event.value === 'S';
            if (!this.tieneTatuajes) {
                this.fichaIngresoCerradoForm
                    .get('especificarZonaTatuajes')
                    ?.setValue('');
            }
        } else if (campo === 'piercing') {
            this.tienePiercing = event.value === 'S';
            if (!this.tienePiercing) {
                this.fichaIngresoCerradoForm
                    .get('especificarZonaPiercing')
                    ?.setValue('');
            }
        } else if (campo === 'otros') {
            this.tieneOtros = event.value === 'S';
            if (!this.tieneOtros) {
                this.fichaIngresoCerradoForm
                    .get('especificarZonaOtros')
                    ?.setValue('');
            }
        } else if (campo === 'victimaAgresion') {
            this.esVictimaAgresion = event.value === 'S';
            if (!this.esVictimaAgresion) {
                this.fichaIngresoCerradoForm
                    .get('especificarAgresion')
                    ?.setValue('');
            }
        } else if (campo === 'esEmbarazada') {
            this.esEmbarazada = event.value === 'S';
            if (!this.esEmbarazada) {
                this.fichaIngresoCerradoForm
                    .get('mesesEmbarazo')
                    ?.setValue(null);
            }
        } else if (campo === 'hijoVictimaAgresion') {
            this.hijoEsVictimaAgresion = event.value === 'S';
            if (!this.esVictimaAgresion) {
                this.datosHijoIngresadoForm
                    .get('hijoEspecificarAgresion')
                    ?.setValue('');
            }
        } else if (campo === 'hijoMoretones') {
            this.hijoMoretones = event.value === 'S';
            if (!this.hijoMoretones) {
                this.datosHijoIngresadoForm
                    .get('hijoEspecificarZonaMoretones')
                    ?.setValue('');
            }
        } else if (campo === 'hijoCicatrices') {
            this.hijoCicatrices = event.value === 'S';
            if (!this.hijoCicatrices) {
                this.datosHijoIngresadoForm
                    .get('hijoEspecificarZonaCicatrices')
                    ?.setValue('');
            }
        } else if (campo === 'hijoTatuajes') {
            this.hijoTatuajes = event.value === 'S';
            if (!this.hijoTatuajes) {
                this.datosHijoIngresadoForm
                    .get('hijoEspecificarZonaTatuajes')
                    ?.setValue('');
            }
        }
    }

    validarFechaNacimiento(): ValidatorFn {
        return (control: AbstractControl): { [key: string]: any } | null => {
            const fechaNacimiento = control.value;
            if (!fechaNacimiento || fechaNacimiento > new Date()) {
                return { fechaInvalida: true };
            }
            return null;
        };
    }

    validarNoEspacios(): ValidatorFn {
        return (control: AbstractControl): { [key: string]: any } | null => {
            const esInvalido =
                control?.value && control?.value?.trim().length === 0;
            return esInvalido ? { soloEspacios: true } : null;
        };
    }

    validarSoloNumeros(event: KeyboardEvent): boolean {
        const charCode = event.which ? event.which : event.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57)) {
            return false;
        }
        return true;
    }

    private obtenerValor(key: string) {
        // Lista de controles de cada formulario
        let listaCamposGenerales: string[] = Object.keys(
            this.fichaIngresoGeneralForm.controls
        );
        let listaCamposAbiertos: string[] = Object.keys(
            this.fichaIngresoAbiertoForm.controls
        );
        let listaCamposCerrados: string[] = Object.keys(
            this.fichaIngresoCerradoForm.controls
        );
        let listaCamposHijo: string[] = Object.keys(
            this.datosHijoIngresadoForm.controls
        );

        // Buscar el valor en el formulario correspondiente
        if (listaCamposGenerales.includes(key)) {
            return this.fichaIngresoGeneralForm.get(key)?.value;
        } else if (listaCamposAbiertos.includes(key)) {
            return this.fichaIngresoAbiertoForm.get(key)?.value;
        } else if (listaCamposCerrados.includes(key)) {
            return this.fichaIngresoCerradoForm.get(key)?.value;
        } else if (listaCamposHijo.includes(key)) {
            return this.datosHijoIngresadoForm.get(key)?.value;
        }

        // Si no se encuentra el campo en ningún formulario
        console.warn(`Campo no encontrado en ningún formulario: ${key}`);
        return null;
    }

    private validarFechaNacimientoHijo(): ValidatorFn {
        return (control: AbstractControl): { [key: string]: any } | null => {
            if (!control.value) {
                return null;
            }

            const fechaNacimiento = new Date(control.value);
            const hoy = new Date();

            // Validar que la fecha no sea futura
            if (fechaNacimiento > hoy) {
                return { fechaFutura: true };
            }

            // Calcular la edad en años
            let edad = hoy.getFullYear() - fechaNacimiento.getFullYear();
            const mes = hoy.getMonth() - fechaNacimiento.getMonth();

            // Ajustar la edad si aún no ha cumplido años en este mes
            if (
                mes < 0 ||
                (mes === 0 && hoy.getDate() < fechaNacimiento.getDate())
            ) {
                edad--;
            }

            // Validar que la edad no sea mayor a 2 años
            if (edad > 2) {
                return { edadMaxima: true };
            }

            return null;
        };
    }

    empezarEdicion(fichaIngresoEditar: FichaIngresoDTO) {
        console.log('fichaIngresoEditar', fichaIngresoEditar);
        console.log('Boolean(fichaIngresoEditar.tokenIdentificador)', Boolean(fichaIngresoEditar.tokenIdentificador));
        this.esEdicion = Boolean(fichaIngresoEditar.tokenIdentificador);
        this.fichaIngresoDTO = fichaIngresoEditar;
        this.centro = fichaIngresoEditar.centro;
        if (fichaIngresoEditar.ingresaConHijo) {
            this.datosHijoIngresadoForm.enable();
        }
        // Campos para centro
        this.fichaIngresoGeneralForm
            .get('centro')
            ?.setValue(fichaIngresoEditar.centro.nombre);
        this.medioCerrado =
            fichaIngresoEditar.centro.jerarquiaPadre.nemonico == 'SOA'
                ? false
                : true;
        this.fichaIngresoGeneralForm
            .get('tipoCentro')
            ?.setValue(this.medioCerrado ? 'C' : 'A');
        this.fichaIngresoGeneralForm
            .get('ubigeo')
            ?.setValue(fichaIngresoEditar.centro.direccion);

        const fechaDate = new Date(fichaIngresoEditar.fechaIngreso); // viene como string ISO

        const fechaLocal = new Date(
            fechaDate.getFullYear(),
            fechaDate.getMonth(),
            fechaDate.getDate()
        );
        const horaLocal = fechaDate.toTimeString().substring(0, 5); // HH:mm

        this.fichaIngresoGeneralForm.get('fechaIngreso')?.setValue(fechaLocal);
        this.fichaIngresoGeneralForm.get('horaIngreso')?.setValue(horaLocal);

        // console.log('fecha', fechaSinZona)
        // console.log('fechaIso', fechaISO)


        this.fichaIngresoGeneralForm
            .get('observaciones')
            ?.setValue(fichaIngresoEditar.observaciones);

        // Si es medio abierto
        this.fichaIngresoAbiertoForm
            .get('responsableInscripcion')
            ?.setValue(fichaIngresoEditar.responsableInscripcion);
        this.fichaIngresoAbiertoForm
            .get('caracteristicasParticulares')
            ?.setValue(fichaIngresoEditar.caracteristicasParticulares);
        // this.fichaIngresoAbiertoForm.get("tutor")?.setValue(fichaIngresoEditar.tokenIdentificadorTutor);

        // Si es medio cerrado
        // this.fichaIngresoCerradoForm.get("atencionSalud")?.setValue(fichaIngresoEditar.atencionSalud ? "S" : "N");
        // this.fichaIngresoCerradoForm.get("motivo")?.setValue(fichaIngresoEditar.motivo);
        this.fichaIngresoCerradoForm
            .get('programaDerivado')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorProgramaDerivado);
        this.fichaIngresoCerradoForm
            .get('lesiones')
            ?.setValue(fichaIngresoEditar.lesiones ? 'S' : 'N');
        fichaIngresoEditar.lesiones
            ? (this.tieneLesiones = true)
            : (this.tieneLesiones = false);
        this.fichaIngresoCerradoForm
            .get('especificarZonaLesiones')
            ?.setValue(fichaIngresoEditar.especificarZonaLesiones);
        this.fichaIngresoCerradoForm
            .get('moretones')
            ?.setValue(fichaIngresoEditar.moretones ? 'S' : 'N');
        fichaIngresoEditar.moretones
            ? (this.tieneMoretones = true)
            : (this.tieneMoretones = false);
        this.fichaIngresoCerradoForm
            .get('especificarZonaMoretones')
            ?.setValue(fichaIngresoEditar.especificarZonaMoretones);
        this.fichaIngresoCerradoForm
            .get('cicatrices')
            ?.setValue(fichaIngresoEditar.cicatrices ? 'S' : 'N');
        fichaIngresoEditar.cicatrices
            ? (this.tieneCicatrices = true)
            : (this.tieneCicatrices = false);
        this.fichaIngresoCerradoForm
            .get('especificarZonaCicatrices')
            ?.setValue(fichaIngresoEditar.especificarZonaCicatrices);
        this.fichaIngresoCerradoForm
            .get('tatuajes')
            ?.setValue(fichaIngresoEditar.tatuajes ? 'S' : 'N');
        fichaIngresoEditar.tatuajes
            ? (this.tieneTatuajes = true)
            : (this.tieneTatuajes = false);
        this.fichaIngresoCerradoForm
            .get('especificarZonaTatuajes')
            ?.setValue(fichaIngresoEditar.especificarZonaTatuajes);
        this.fichaIngresoCerradoForm
            .get('piercing')
            ?.setValue(fichaIngresoEditar.piercing ? 'S' : 'N');
        fichaIngresoEditar.piercing
            ? (this.tienePiercing = true)
            : (this.tienePiercing = false);
        this.fichaIngresoCerradoForm
            .get('especificarZonaPiercing')
            ?.setValue(fichaIngresoEditar.especificarZonaPiercing);
        this.fichaIngresoCerradoForm
            .get('otros')
            ?.setValue(fichaIngresoEditar.otros ? 'S' : 'N');
        fichaIngresoEditar.otros
            ? (this.tieneOtros = true)
            : (this.tieneOtros = false);
        this.fichaIngresoCerradoForm
            .get('especificarZonaOtros')
            ?.setValue(fichaIngresoEditar.especificarZonaOtros);
        this.fichaIngresoCerradoForm
            .get('victimaAgresion')
            ?.setValue(fichaIngresoEditar.victimaAgresion ? 'S' : 'N');
        fichaIngresoEditar.victimaAgresion
            ? (this.esVictimaAgresion = true)
            : (this.esVictimaAgresion = false);
        this.fichaIngresoCerradoForm
            .get('especificarAgresion')
            ?.setValue(fichaIngresoEditar.especificarAgresion);
        this.fichaIngresoCerradoForm
            .get('seguroSalud')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorSeguroSalud);
        this.fichaIngresoCerradoForm
            .get('formaCabeza')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorFormaCabeza);
        this.fichaIngresoCerradoForm
            .get('formaNariz')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorFormaNariz);
        this.fichaIngresoCerradoForm
            .get('formaLabios')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorFormaLabios);
        this.fichaIngresoCerradoForm
            .get('formaCuerpo')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorFormaCuerpo);
        this.fichaIngresoCerradoForm
            .get('anomaliaOjos')
            ?.setValue(fichaIngresoEditar.tokenIdentificadorAnomaliaOjos);
        this.fichaIngresoCerradoForm
            .get('esEmbarazada')
            ?.setValue(fichaIngresoEditar.esEmbarazada ? 'S' : 'N');
        fichaIngresoEditar.esEmbarazada
            ? (this.esEmbarazada = true)
            : (this.esEmbarazada = false);
        this.fichaIngresoCerradoForm
            .get('mesesEmbarazo')
            ?.setValue(fichaIngresoEditar.mesesEmbarazo);
        this.fichaIngresoCerradoForm
            .get('ingresaConHijo')
            ?.setValue(fichaIngresoEditar.ingresaConHijo ? 'S' : 'N');

        if (fichaIngresoEditar.ingresaConHijo) {
            const datosHijoIngresadoDTO = fichaIngresoEditar.datosHijoIngresado;

            if (fichaIngresoEditar.ingresaConHijo) {
                console.log('hijo', fichaIngresoEditar.datosHijoIngresado);
                const datosHijoIngresadoDTO =
                    fichaIngresoEditar.datosHijoIngresado;

                if (datosHijoIngresadoDTO) {
                    this.datosHijoIngresadoForm
                        .get('hijoApellidoPaterno')
                        ?.setValue(datosHijoIngresadoDTO.hijoApellidoPaterno);
                    this.datosHijoIngresadoForm
                        .get('hijoApellidoMaterno')
                        ?.setValue(datosHijoIngresadoDTO.hijoApellidoMaterno);
                    this.datosHijoIngresadoForm.get('hijoNombresCompletos')?.setValue(
                        (datosHijoIngresadoDTO?.hijoPrimerNombre ? datosHijoIngresadoDTO.hijoPrimerNombre : '') +
                        ' ' +
                        (datosHijoIngresadoDTO?.hijoSegundoNombre ? datosHijoIngresadoDTO.hijoSegundoNombre : '')
                    );
                    if (datosHijoIngresadoDTO.hijoFechaNacimiento) {
                        const fecha = datosHijoIngresadoDTO.hijoFechaNacimiento
                            ? new Date(datosHijoIngresadoDTO.hijoFechaNacimiento)
                            : null;

                        this.datosHijoIngresadoForm.get('hijoFechaNacimiento')?.setValue(fecha);
                    }
                    this.datosHijoIngresadoForm
                        .get('hijoDNI')
                        ?.setValue(datosHijoIngresadoDTO.hijoDNI);
                    this.datosHijoIngresadoForm
                        .get('hijoTipoSexo')
                        ?.setValue(datosHijoIngresadoDTO.hijoTipoSexo);

                    // Victima de agresión
                    this.datosHijoIngresadoForm
                        .get('hijoVictimaAgresion')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoVictimaAgresion
                                ? 'S'
                                : 'N'
                        );
                    datosHijoIngresadoDTO.hijoVictimaAgresion
                        ? (this.hijoEsVictimaAgresion = true)
                        : (this.hijoEsVictimaAgresion = false);
                    this.datosHijoIngresadoForm
                        .get('hijoEspecificarAgresion')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoEspecificarAgresion
                        );

                    // Moretones
                    this.datosHijoIngresadoForm
                        .get('hijoMoretones')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoMoretones ? 'S' : 'N'
                        );
                    datosHijoIngresadoDTO.hijoMoretones
                        ? (this.hijoMoretones = true)
                        : (this.hijoMoretones = false);
                    this.datosHijoIngresadoForm
                        .get('hijoEspecificarZonaMoretones')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoEspecificarZonaMoretones
                        );

                    // Cicatrices
                    this.datosHijoIngresadoForm
                        .get('hijoCicatrices')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoCicatrices ? 'S' : 'N'
                        );
                    datosHijoIngresadoDTO.hijoCicatrices
                        ? (this.hijoCicatrices = true)
                        : (this.hijoCicatrices = false);
                    this.datosHijoIngresadoForm
                        .get('hijoEspecificarZonaCicatrices')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoEspecificarZonaCicatrices
                        );

                    // Tatuajes
                    this.datosHijoIngresadoForm
                        .get('hijoTatuajes')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoTatuajes ? 'S' : 'N'
                        );
                    datosHijoIngresadoDTO.hijoTatuajes
                        ? (this.hijoTatuajes = true)
                        : (this.hijoTatuajes = false);
                    this.datosHijoIngresadoForm
                        .get('hijoEspecificarZonaTatuajes')
                        ?.setValue(
                            datosHijoIngresadoDTO.hijoEspecificarZonaTatuajes
                        );

                    this.datosHijoIngresadoForm
                        .get('hijoOtroEspecificar')
                        ?.setValue(datosHijoIngresadoDTO.hijoOtroEspecificar);
                    this.datosHijoIngresadoForm
                        .get('hijoObservaciones')
                        ?.setValue(datosHijoIngresadoDTO.hijoObservaciones);
                }
            }
        }
    }

    cancelarEdicion() {
        this.esEdicion = false;
        this.fichaIngresoGeneralForm.reset();
        this.fichaIngresoAbiertoForm.reset();
        this.fichaIngresoCerradoForm.reset();
        this.fichaIngresoDTO = null;

        this.router.navigate(['../'], { relativeTo: this.route });
    }

    validarGuardado() {
        let mensaje = '';
        // Lista de estados válidos de la ficha de identificación
        const estadosValidos = [
            etiquetasModel.NEMONICO_ESTADO_ADOLESCENTE_LIBRE,
            etiquetasModel.NEMONICO_ESTADO_ADOLESCENTE_FUGADO,
            etiquetasModel.NEMONICO_ESTADO_ADOLESCENTE_SENTENCIADO_PROCESADO

        ];
         
        // Si el adolescente no está libre/fugado y la ficha de ingreso es nueva
        if (
            !estadosValidos.includes(this.fichaIdentificacionDTO.estadoAdolescente.nemonico) 
            && (this.fichaIngresoDTO?.tokenIdentificador == null || this.fichaIdentificacionDTO?.tokenIdentificador === '')
        ) {
            mensaje = 
                "No es posible realizar el registro del adolescente debido a que el CJDR/SOA: " + 
                this.fichaIdentificacionDTO.centroIngreso +
                " no ha generado su registro de salida.";
            this.dialogMensajeService.mensajeAdvertencia('', mensaje);
            return;
        }

        // Lista de centros que permiten reingresar al adolescente por medio de una ficha de ingreso
        const centrosReingresoPorFicha = [
            "SOA",
            "UAPISE"
        ]

        const esCentroSoaUapise = centrosReingresoPorFicha.some(centro =>
            this.fichaIdentificacionDTO.centroIngreso
                ?.toUpperCase()
                .includes(centro)
        )
        
        // Si la ficha pertenece a alguno de los centros anteriores y la ficha de ingreso es nueva, se muestra ingreso en ese centro
        // caso contrario, se muestra mensaje de confirmación
        if (
            esCentroSoaUapise
            && (this.fichaIngresoDTO?.tokenIdentificador == null || this.fichaIdentificacionDTO?.tokenIdentificador === '')
        ) {
            mensaje = 
                "¿Está seguro de guardar la ficha de ingreso del adolescente: \"" + this.fichaIdentificacionDTO.nombres + " " 
                + this.fichaIdentificacionDTO.apellidoPaterno +
                "\"   al centro de: \"" + this.centro.nombre + "\"?, esta operación es irreversible";
        } else {
            mensaje = "Guardar ficha de ingreso."
        }       

        let ref = this.dialogMensajeService.mensajeConConfirmacion(
            mensaje,"Deseas continuar?"
        );
        ref.afterClosed().subscribe(
            {
                next: (resp: "confirmed" | "cancelled") => {
                    if (resp == "confirmed") {
                        this.crearActualizar();
                    } 
                }
            }
        );       
    }

    crearActualizar() {

        // this.obtenerControlesConErrores(this.fichaIngresoGeneralForm);
        // this.obtenerControlesConErrores(this.fichaIngresoCerradoForm);
        // this.obtenerControlesConErrores(this.datosHijoIngresadoForm);

        // return 0;



        this.fichaIngresoGeneralForm.disable();

        let fichaIngreso = new FichaIngresoDTO();

        // Campos generales

        // Armar fecha completa con fecha y hora ingreso
        // let fechaIngreso = this.obtenerValor('fechaIngreso');
        const fecha: Date = this.obtenerValor('fechaIngreso');
        let horaIngreso = this.obtenerValor('horaIngreso');
        const [horas, minutos] = horaIngreso.split(':').map(Number);
        const fechaConHora = new Date(fecha);
        fechaConHora.setHours(horas, minutos, 0, 0);
        fichaIngreso.fechaIngreso = fechaConHora;

        // Centro se envia como objeto tipo jerarquia
        let centro = new JerarquiaDTO();
        centro.id = this.obtenerValor('centro');
        fichaIngreso.centro = this.centro;

        fichaIngreso.tokenIdentificadorFichaIdentificacion = this.uuid_fp;
        fichaIngreso.tokenIdentificador =
            this.fichaIngresoDTO?.tokenIdentificador;
        fichaIngreso.esEdicion = this.esEdicion && this.fichaIngresoDTO?.tokenIdentificador ? true : false;

        fichaIngreso.observaciones = this.obtenerValor('observaciones');
        console.log('fichaIngreso a guardar', fichaIngreso);
        if (this.medioCerrado) {
            this.fichaIngresoCerradoForm.disable();

            // Campos medio cerrado
            // fichaIngreso.atencionSalud = this.obtenerValor("atencionSalud")=="S" ? true : false;
            // fichaIngreso.motivo = this.obtenerValor("motivo");
            fichaIngreso.tokenIdentificadorProgramaDerivado =
                this.obtenerValor('programaDerivado');
            fichaIngreso.lesiones =
                this.obtenerValor('lesiones') == 'S' ? true : false;
            fichaIngreso.especificarZonaLesiones = this.obtenerValor(
                'especificarZonaLesiones'
            );
            fichaIngreso.moretones =
                this.obtenerValor('moretones') == 'S' ? true : false;
            fichaIngreso.especificarZonaMoretones = this.obtenerValor(
                'especificarZonaMoretones'
            );
            fichaIngreso.cicatrices =
                this.obtenerValor('cicatrices') == 'S' ? true : false;
            fichaIngreso.especificarZonaCicatrices = this.obtenerValor(
                'especificarZonaCicatrices'
            );
            fichaIngreso.tatuajes =
                this.obtenerValor('tatuajes') == 'S' ? true : false;
            fichaIngreso.especificarZonaTatuajes = this.obtenerValor(
                'especificarZonaTatuajes'
            );
            fichaIngreso.piercing =
                this.obtenerValor('piercing') == 'S' ? true : false;
            fichaIngreso.especificarZonaPiercing = this.obtenerValor(
                'especificarZonaPiercing'
            );
            fichaIngreso.otros =
                this.obtenerValor('otros') == 'S' ? true : false;
            fichaIngreso.especificarZonaOtros = this.obtenerValor(
                'especificarZonaOtros'
            );
            fichaIngreso.victimaAgresion =
                this.obtenerValor('victimaAgresion') == 'S' ? true : false;
            fichaIngreso.especificarAgresion = this.obtenerValor(
                'especificarAgresion'
            );
            fichaIngreso.tokenIdentificadorSeguroSalud =
                this.obtenerValor('seguroSalud');
            fichaIngreso.tokenIdentificadorFormaCabeza =
                this.obtenerValor('formaCabeza');
            fichaIngreso.tokenIdentificadorFormaNariz =
                this.obtenerValor('formaNariz');
            fichaIngreso.tokenIdentificadorFormaLabios =
                this.obtenerValor('formaLabios');
            fichaIngreso.tokenIdentificadorFormaCuerpo =
                this.obtenerValor('formaCuerpo');
            fichaIngreso.tokenIdentificadorAnomaliaOjos =
                this.obtenerValor('anomaliaOjos');
            fichaIngreso.esEmbarazada =
                this.obtenerValor('esEmbarazada') == 'S' ? true : false;
            fichaIngreso.mesesEmbarazo = this.obtenerValor('mesesEmbarazo');
            fichaIngreso.ingresaConHijo =
                this.obtenerValor('ingresaConHijo') == 'S' ? true : false;
            fichaIngreso.estadoAdolescente = this.obtenerValor('tipoEstadoAdolescente');

            if (fichaIngreso.ingresaConHijo) {
                let datosHijo;
                if (
                    this.esEdicion &&
                    this.fichaIngresoDTO?.datosHijoIngresado
                ) {
                    datosHijo = this.fichaIngresoDTO.datosHijoIngresado;
                } else {
                    datosHijo = new DatosHijoIngresadoDTO();
                    datosHijo.tokenIdentificadorFichaIdentificacion =
                        this.uuid_fp;
                }

                // Datos personales básicos
                datosHijo.hijoApellidoPaterno = this.datosHijoIngresadoForm.get(
                    'hijoApellidoPaterno'
                )?.value;
                datosHijo.hijoApellidoMaterno = this.datosHijoIngresadoForm.get(
                    'hijoApellidoMaterno'
                )?.value;

                // Manejar nombres completos
                const nombresCompletos = this.datosHijoIngresadoForm.get(
                    'hijoNombresCompletos'
                )?.value;
                if (nombresCompletos && nombresCompletos.trim()) {
                    const nombres = nombresCompletos.trim().split(/\s+/);
                    datosHijo.hijoPrimerNombre = nombres[0] || '';
                    datosHijo.hijoSegundoNombre =
                        nombres.slice(1).join(' ') || '';
                }

                datosHijo.hijoFechaNacimiento = this.datosHijoIngresadoForm.get(
                    'hijoFechaNacimiento'
                )?.value ? this.datosHijoIngresadoForm.get(
                    'hijoFechaNacimiento'
                )?.value : null;
                datosHijo.hijoDNI =
                    this.datosHijoIngresadoForm.get('hijoDNI')?.value;
                datosHijo.hijoTipoSexo =
                    this.datosHijoIngresadoForm.get('hijoTipoSexo')?.value;
                datosHijo.hijoOcupacion =
                    this.datosHijoIngresadoForm.get('hijoOcupacion')?.value;
                datosHijo.hijoParentesco =
                    this.datosHijoIngresadoForm.get('hijoParentesco')?.value;
                datosHijo.hijoTelefono =
                    this.datosHijoIngresadoForm.get('hijoTelefono')?.value;
                datosHijo.hijoEstadoCivil =
                    this.datosHijoIngresadoForm.get('hijoEstadoCivil')?.value;
                datosHijo.hijoInstruccion =
                    this.datosHijoIngresadoForm.get('hijoInstruccion')?.value;
                datosHijo.hijoRoles =
                    this.datosHijoIngresadoForm.get('hijoRoles')?.value;

                // Datos de agresión
                datosHijo.hijoVictimaAgresion =
                    this.datosHijoIngresadoForm.get('hijoVictimaAgresion')
                        ?.value === 'S';
                datosHijo.hijoEspecificarAgresion =
                    this.datosHijoIngresadoForm.get(
                        'hijoEspecificarAgresion'
                    )?.value;

                // Moretones
                datosHijo.hijoMoretones =
                    this.datosHijoIngresadoForm.get('hijoMoretones')?.value ===
                    'S';
                datosHijo.hijoEspecificarZonaMoretones =
                    this.datosHijoIngresadoForm.get(
                        'hijoEspecificarZonaMoretones'
                    )?.value;

                // Cicatrices
                datosHijo.hijoCicatrices =
                    this.datosHijoIngresadoForm.get('hijoCicatrices')?.value ===
                    'S';
                datosHijo.hijoEspecificarZonaCicatrices =
                    this.datosHijoIngresadoForm.get(
                        'hijoEspecificarZonaCicatrices'
                    )?.value;

                // Tatuajes
                datosHijo.hijoTatuajes =
                    this.datosHijoIngresadoForm.get('hijoTatuajes')?.value ===
                    'S';
                datosHijo.hijoEspecificarZonaTatuajes =
                    this.datosHijoIngresadoForm.get(
                        'hijoEspecificarZonaTatuajes'
                    )?.value;

                // Otros datos
                datosHijo.hijoOtroEspecificar = this.datosHijoIngresadoForm.get(
                    'hijoOtroEspecificar'
                )?.value;
                datosHijo.hijoObservaciones =
                    this.datosHijoIngresadoForm.get('hijoObservaciones')?.value;

                // Asignar a la ficha de ingreso
                fichaIngreso.datosHijoIngresado = datosHijo;
            }
        } else {
            this.fichaIngresoAbiertoForm.disable();

            // Campos medio abierto
            fichaIngreso.responsableInscripcion = this.obtenerValor(
                'responsableInscripcion'
            );
            fichaIngreso.caracteristicasParticulares = this.obtenerValor(
                'caracteristicasParticulares'
            );
            // fichaIngreso.tokenIdentificadorTutor = this.obtenerValor("tutor");
        }
        console.log('fichaIngreso', fichaIngreso);
        this.fichaIngresoService
            .crearFichaIngreso(fichaIngreso, this.nemonicoMenu)
            .subscribe({
                next: (response: RespuestaPorDefecto<FichaIngresoDTO>) => {
                    this.fichaIngresoGeneralForm.enable();
                    this.fichaIngresoAbiertoForm.enable();
                    this.fichaIngresoCerradoForm.enable();

                    // this.completoOperacion.emit(response.exito);
                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensajeError
                        );
                        //this.fichaIngresoService.checkError(response);

                        return;
                    }
                    this.dialogMensajeService.mensajeExitoso(
                        response.titulo,
                        response.mensaje
                    );
                    this.fichaIngresoService.actualizarFichaIngreso$.next();
                    this.router.navigate(['../'], { relativeTo: this.route });
                },
                error: (error: any) => {                    
                    this.fichaIngresoService.checkError(error);
                    this.fichaIngresoGeneralForm.enable();
                    this.fichaIngresoAbiertoForm.enable();
                    this.fichaIngresoCerradoForm.enable();
                },
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
                        const datosCabecera = {
                            '[NOMBRES-APELLIDOS]':
                                `${fichaIdentificacion.nombres} ${fichaIdentificacion.apellidoPaterno} ${fichaIdentificacion.apellidoMaterno}`.trim(),
                            '[DNI]': fichaIdentificacion.numeroDocumento || '',
                            '[LUGAR-FECHA-NACIMIENTO]': `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`,
                            '[EDAD]': `${this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento)}`,
                        };

                        resolve(datosCabecera);
                    },
                    error: (error: any) => {
                        reject(error);
                    },
                });
        });
    }

    formatearFechaISO(fecha: any): string {
        try {
            if (!fecha) return 'No disponible';
            const fechaStr = typeof fecha === 'string' ? fecha : new Date(fecha).toISOString();
            return fechaStr.split('T')[0];
        } catch {
            return 'Fecha inválida';
        }
    }

    async imprimirFicha() {
        try {
            let solicitudPdf = new GeneracionPdfRequest();
            const datosCabecera = await this.obtenerDatosCabecera();

            const normalizarRespuesta = (valor: string | null) => {
                if (valor === 'S') return 'Sí';
                if (valor === 'N') return 'No';
                return valor || 'No especificado';
            };

            const variablesGenerales = {
                ...datosCabecera,
                '[FECHA-INGRESO]': this.formatearFechaISO(this.obtenerValor('fechaIngreso')),
                '[HORA-INGRESO]': this.obtenerValor('horaIngreso') || '',
                '[CENTRO]': this.obtenerValor('centro') || '',
                '[UBIGEO]': this.obtenerValor('ubigeo') || '',
                '[OBSERVACIONES]': this.obtenerValor('observaciones') || '',
                "[FECHA-REGISTRO]": (new Date()).toISOString().split("T")[0],
                "[HORA-REGISTRO]": (new Date()).toTimeString().split(" ")[0],
                "[TITULO-INFORME]": 'Ficha Ingreso',
                "[IMG_BASE64]": this.base64Image
            };

            console.log('variables cabecera', variablesGenerales);

            if (!this.medioCerrado) {
                // Medio Abierto
                solicitudPdf.nemonico = 'FORMULARIO_FICHA_INGRESO_ABIERTO';
                solicitudPdf.variables = {
                    ...variablesGenerales,
                    '[RESPONSABLE-INSCRIPCION]':
                        this.obtenerValor('responsableInscripcion') || '',
                    '[CARACTERISTICAS-PARTICULARES]':
                        this.obtenerValor('caracteristicasParticulares') || '',
                    '[TUTOR]': '', // Lógica de tutor si es necesario
                };
            } else {
                // Medio Cerrado
                const variablesMedioCerrado = {
                    ...variablesGenerales,
                    '[SEGURO-SALUD]':
                        this.funcionesUtils.obtenerNombreCatalogoPorToken(
                            this.obtenerValor('seguroSalud'),
                            this.listaSegurosSalud
                        ) || '',
                };

                // Función para manejar campos con especificación condicional
                const agregarCampoCondicional = (
                    nombreCampo: string,
                    valorCampo: string,
                    nombreEspecificacion: string
                ) => {
                    const valor = normalizarRespuesta(valorCampo);
                    variablesMedioCerrado[nombreCampo] = valor;

                    if (valor === 'Sí') {
                        variablesMedioCerrado[nombreEspecificacion] =
                            this.obtenerValor(
                                nombreEspecificacion
                                    .replace(/\[|\]/g, '')
                                    .toLowerCase()
                            ) || '';
                    } else {
                        variablesMedioCerrado[nombreEspecificacion] = '';
                    }
                };

                // Campos condicionados
                const camposCondicionados = [
                    {
                        campo: '[LESIONES]',
                        especificacion: '[ESPECIFICAR-ZONA-LESIONES]',
                    },
                    {
                        campo: '[MORETONES]',
                        especificacion: '[ESPECIFICAR-ZONA-MORETONES]',
                    },
                    {
                        campo: '[CICATRICES]',
                        especificacion: '[ESPECIFICAR-ZONA-CICATRICES]',
                    },
                    {
                        campo: '[TATUAJES]',
                        especificacion: '[ESPECIFICAR-ZONA-TATUAJES]',
                    },
                    {
                        campo: '[PIERCING]',
                        especificacion: '[ESPECIFICAR-ZONA-PIERCING]',
                    },
                    {
                        campo: '[OTROS]',
                        especificacion: '[ESPECIFICAR-ZONA-OTROS]',
                    },
                    // { campo: '[VICTIMA-AGRESION]', especificacion: '[ESPECIFICAR-AGRESION]' }
                ];

                camposCondicionados.forEach((item) =>
                    agregarCampoCondicional(
                        item.campo,
                        this.obtenerValor(
                            item.campo.replace(/\[|\]/g, '').toLowerCase()
                        ),
                        item.especificacion
                    )
                );

                // Campos físicos según sexo
                const obtenerCampoFisico = (
                    nombreCampo: string,
                    listaMasculina: any[],
                    listaFemenina: any[]
                ) => {
                    const valorCampo = this.obtenerValor(nombreCampo);
                    const listaSexo =
                        this.sexoFicha?.nemonico === 'TIPO_SEXO_MASCULINO'
                            ? listaMasculina
                            : listaFemenina;
                    return (
                        this.funcionesUtils.obtenerNombreCatalogoPorToken(
                            valorCampo,
                            listaSexo
                        ) || ''
                    );
                };

                variablesMedioCerrado['[FORMA-CABEZA]'] = obtenerCampoFisico(
                    'formaCabeza',
                    this.listaFormaCabeza,
                    this.listaFormaCabezaMujer
                );

                variablesMedioCerrado['[FORMA-NARIZ]'] = obtenerCampoFisico(
                    'formaNariz',
                    this.listaFormaNariz,
                    this.listaFormaNarizMujer
                );

                variablesMedioCerrado['[FORMA-LABIOS]'] = obtenerCampoFisico(
                    'formaLabios',
                    this.listaFormaLabios,
                    this.listaFormaLabiosMujer
                );

                variablesMedioCerrado['[FORMA-CUERPO]'] = obtenerCampoFisico(
                    'formaCuerpo',
                    this.listaFormaCuerpo,
                    this.listaFormaCuerpoMujer
                );

                variablesMedioCerrado['[ANOMALIA-OJOS]'] = obtenerCampoFisico(
                    'anomaliaOjos',
                    this.listaAnomaliaOjos,
                    this.listaAnomaliaOjosMujer
                );

                // Campos de embarazo solo para mujeres
                if (this.sexoFicha?.nemonico === 'TIPO_SEXO_FEMENINO') {
                    variablesMedioCerrado['[ES-EMBARAZADA]'] =
                        normalizarRespuesta(this.obtenerValor('esEmbarazada'));
                    variablesMedioCerrado['[MESES-EMBARAZO]'] =
                        this.obtenerValor('esEmbarazada') === 'S'
                            ? this.obtenerValor('mesesEmbarazo')
                            : '';
                } else {
                    variablesMedioCerrado['[ES-EMBARAZADA]'] = '';
                    variablesMedioCerrado['[MESES-EMBARAZO]'] = '';
                }

                variablesMedioCerrado['[INGRESA-CON-HIJO]'] =
                    normalizarRespuesta(this.obtenerValor('ingresaConHijo'));

                // Determinar nemónico según ingreso con hijo
                if (this.obtenerValor('ingresaConHijo') === 'S') {
                    solicitudPdf.nemonico =
                        'FORMULARIO_FICHA_INGRESO_CERRADO_CON_HIJO';
                    solicitudPdf.variables = {
                        ...variablesMedioCerrado,
                        '[HIJO-APELLIDO-PATERNO]':
                            this.obtenerValor('hijoApellidoPaterno') || '',
                        '[HIJO-APELLIDO-MATERNO]':
                            this.obtenerValor('hijoApellidoMaterno') || '',
                        '[HIJO-NOMBRES]':
                            this.obtenerValor('hijoNombresCompletos') || '',
                        '[HIJO-FECHA-NACIMIENTO]':
                            this.obtenerValor('hijoFechaNacimiento') || '',
                        '[HIJO-DNI]': this.obtenerValor('hijoDNI') || '',
                        '[HIJO-SEXO]':
                            this.funcionesUtils.obtenerNombreCatalogoPorToken(
                                this.obtenerValor('hijoTipoSexo'),
                                this.listaTiposSexo
                            ) || '',
                        '[HIJO-VICTIMA-AGRESION]': normalizarRespuesta(
                            this.obtenerValor('hijoVictimaAgresion')
                        ),
                        '[HIJO-ESPECIFICAR-AGRESION]':
                            this.obtenerValor('hijoEspecificarAgresion') || '',
                        '[HIJO-MORETONES]': normalizarRespuesta(
                            this.obtenerValor('hijoMoretones')
                        ),
                        '[HIJO-ESPECIFICAR-ZONA-MORETONES]':
                            this.obtenerValor('hijoEspecificarZonaMoretones') ||
                            '',
                        '[HIJO-CICATRICES]': normalizarRespuesta(
                            this.obtenerValor('hijoCicatrices')
                        ),
                        '[HIJO-ESPECIFICAR-ZONA-CICATRICES]':
                            this.obtenerValor(
                                'hijoEspecificarZonaCicatrices'
                            ) || '',
                        '[HIJO-TATUAJES]': normalizarRespuesta(
                            this.obtenerValor('hijoTatuajes')
                        ),
                        '[HIJO-ESPECIFICAR-ZONA-TATUAJES]':
                            this.obtenerValor('hijoEspecificarZonaTatuajes') ||
                            '',
                        '[HIJO-OTROS]':
                            this.obtenerValor('hijoOtroEspecificar') || '',
                        '[HIJO-OBSERVACIONES]':
                            this.obtenerValor('hijoObservaciones') || '',
                    };
                } else {
                    solicitudPdf.nemonico =
                        'FORMULARIO_FICHA_INGRESO_CERRADO_SIN_HIJO';
                    solicitudPdf.variables = variablesMedioCerrado;
                }
            }

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

    obtenerFichaIdentificacionPorToken(tokenIdentificador: string) {
        this.fichaIdentificacionService
            .obtenerFichaIdentificacionPorTokenIdentificador(tokenIdentificador, this.nemonicoMenu)
            .subscribe({
                next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {

                    // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                    if (!resp.exito) {
                        return;
                    }
                    // this.esEdicion = true;
                    console.log('resp', resp);
                    this.fichaIdentificacionDTO = resp.data;
                    this.sexoFicha = this.listaTiposSexo.find(
                        (t) =>
                            t.tokenIdentificador ===
                            this.fichaIdentificacionDTO.tipoSexo
                    );
                    console.log('genero', this.fichaIdentificacionDTO);
                    console.log('validacion', this.sexoFicha.nemonico === 'TIPO_SEXO_MASCULINO' || !this.fichaIdentificacionDTO.ingresahijos || !this.esEdicion);
                    if (this.sexoFicha.nemonico === 'TIPO_SEXO_MASCULINO' || !this.fichaIdentificacionDTO.ingresahijos || !this.esEdicion) {
                        const control =
                            this.datosHijoIngresadoForm.get('hijoDNI');
                        control?.clearValidators();
                        control?.updateValueAndValidity();
                        control?.disable();
                        const controlFecha = this.datosHijoIngresadoForm.get(
                            'hijoFechaNacimiento'
                        );
                        controlFecha?.clearValidators();
                        controlFecha?.updateValueAndValidity();
                        controlFecha?.disable();
                        const controlGenero =
                            this.datosHijoIngresadoForm.get('hijoTipoSexo');
                        controlGenero?.clearValidators();
                        controlGenero?.updateValueAndValidity();
                        controlGenero?.disable();
                    }
                },
                error: (error: any) => {

                    this.fichaIdentificacionService.checkError(error);
                },
            });
    }

    obtenerDocumentos() {
        let page = this.tablaDocumentos.page;
        let pageSize = this.tablaDocumentos.pageSize;

        let fichaIngresoDocumentosRequest = new FichaIngresoDocumentosRequest();
        fichaIngresoDocumentosRequest.page = page;
        fichaIngresoDocumentosRequest.size = pageSize;
        fichaIngresoDocumentosRequest.textoBuscar =
            this.tablaDocumentos.textoBuscar;
        // fichaIngresoDocumentosRequest.tokenIdentificadorFichaIngreso =
        //     this.fichaIngresoDTO.tokenIdentificador;
        fichaIngresoDocumentosRequest.tokenIdentificador = this.uuid_fp;

        this.fichaIngresoService
            .obtenerDocumentosFichaIngreso(fichaIngresoDocumentosRequest, this.nemonicoMenu)
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
                        this.tablaDocumentos.actualizarTabla(
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

    eliminacionDocumento(documentoDTO: DocumentoDTO) {
        let load = this.dialogMensajeService.mensajeLoading(
            'Quitando el documento: ' + documentoDTO.nombre + ' del detalle..'
        );
        let fichaIngresoDocumento = new FichaIngresoDocumentoDTO();
        fichaIngresoDocumento.documentoDTO = documentoDTO;
        fichaIngresoDocumento.tokenIdentificadorFichaIngreso =
            this.fichaIngresoDTO.tokenIdentificador;
        this.fichaIngresoService
            .eliminarDocumentoFichaIngreso(fichaIngresoDocumento, this.nemonicoMenu)
            .subscribe({
                next: (
                    respone: RespuestaPorDefecto<FichaIngresoDocumentoDTO>
                ) => {
                    load.close();
                    if (!respone.exito) {
                        this.fichaIngresoService.checkError(respone);
                    }

                    this.obtenerDocumentos();
                },
                error: (error: any) => {
                    load.close();
                    this.fichaIngresoService.checkError(error);
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
                let fichaIngresoDocumento = new FichaIngresoDocumentoDTO();
                // fichaIngresoDocumento.tokenIdentificadorFichaIngreso =
                //     this.fichaIngresoDTO.tokenIdentificador;
                fichaIngresoDocumento.tokenFichaIdentificacion = this.uuid_fp;
                fichaIngresoDocumento.documentoDTO =
                    documentoSubido.documentoDTO;

                let load = this.dialogMensajeService.mensajeLoading(
                    'Subiendo el documento: ' + documentoSubido.documento.name
                );
                this.fichaIngresoService
                    .subirDocumentoFichaIngreso(
                        documentoSubido.documento,
                        fichaIngresoDocumento,
                        this.nemonicoMenu
                    )
                    .subscribe({
                        next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
                            load.close();
                            if (!response.exito) {
                                this.fichaIngresoService.checkError(response);
                                return;
                            }

                            //Refrescar la tabla de documentos
                            this.obtenerDocumentos();
                        },
                        error: (error: any) => {
                            load.close();
                            this.fichaIngresoService.checkError(error);
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

    obtenerTiposDeDocumentos() {
        let nemonico = this.centro.jerarquiaPadre.nemonico == 'SOA' ? 'TIPOS_ADJUNTOS_FICHA_INGRESO' : etiquetasModel.NEMONICO_TIPO_SIGNOS_ALTERACIONES_HISTORIAL;
        console.log('documento de', nemonico);
        this.catalogoService.obtenerHijos(
            nemonico, this.nemonicoMenu
        ).subscribe(
            {
                next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {

                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this.catalogoService.checkError(response);
                        return;
                    }

                    this.tiposDeDocumentosSistema = response.data?.sort(
                        (a, b) => {
                            if (a.nombre < b.nombre) { return -1; }
                            if (a.nombre > b.nombre) { return 1; }
                            return 0;
                        }
                    )?.map(
                        (cat) => {
                            let tipoDoc = cat as TipoDeDocumento;
                            return tipoDoc;
                        }
                    );
                },
                error: (error: any) => {
                    this.catalogoService.checkError(error);
                }
            }
        );
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


    obtenerControlesConErrores(form: FormGroup): void {
        const errores: string[] = [];

        Object.keys(form.controls).forEach((key) => {
            const control = form.get(key);
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

    actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
        if (event.value) {
            const fecha = event.value;
            this.datosHijoIngresadoForm.get(controlName).setValue(fecha);
        }
    }

    formatearFechaSinZona(dateString: string): string {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
}
