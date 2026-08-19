import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormGroup, ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { EvaluacionMedicaProgresoDTO } from 'app/core/model/both/EJE/EvaluacionMedicaProgresoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SnackbarService } from 'app/core/services/snackbar.service';
import moment from 'moment';
import { EvaluacionMedicaService } from '../evaluacion-medica.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CommonModule } from '@angular/common';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PdfService } from 'app/core/services/pdf.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { environment } from 'environments/environment';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { HttpClient } from '@angular/common/http';
import { CriterioEvaluacionMedicaProgresoItemImpresionDTO } from 'app/core/model/both/EJE/criterioEvaluacionMedicaProgresoDTO.model';

@Component({
    selector: 'app-progreso-evaluacion-medica',
    standalone: true,
    imports: [
        MatProgressBarModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatTableModule,
        MatPaginatorModule,
        CommonModule
    ],
    templateUrl: './progreso-evaluacion-medica.component.html',
    styleUrl: './progreso-evaluacion-medica.component.scss',
})
export class ProgresoEvaluacionMedicaComponent implements OnInit {

    @Output() crear = new EventEmitter<void>();
    @Input() vistaDoctor: boolean = false;
    @Input() puedeEditar: boolean = false;
    @Input() puedeEliminar: boolean = false;

    tokenFichaMedica: string = '';

    isLoading: boolean = true;
    searchInputControl: UntypedFormControl = new UntypedFormControl();

    nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_HISTORIA_CLINICA_ESQUEMA_CORPORAL_ECTOSCOPICO;
    uuid_fp = this.route.snapshot.paramMap.get('uuid_fp') || '';
    centro: JerarquiaDTO = new JerarquiaDTO();
    base64Image: string | null = null;

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

    constructor(
        public dialog: MatDialog,
        private router: Router,
        private route: ActivatedRoute,
        private _evaluacionMedicaService: EvaluacionMedicaService,
        private readonly changeDetector: ChangeDetectorRef,
        private readonly _fuseConfirmationService: FuseConfirmationService,
        private readonly customSnackbar: SnackbarService,
        private readonly accionesSheet: MatBottomSheet,
        private readonly _dialogMensajeService: DialogMensajeService,
        private funcionesUtils: FuncionesUtils,
        private pdfService: PdfService,
        private fichaIdentificacionService: FichaIdentificacionService,
        private jerarquiaService: JerarquiaService,
        private http: HttpClient,
    ) { }

    async ngOnInit(): Promise<void> {
        this.cargarCentro();
        this.loadImageAsBase64();
        this._evaluacionMedicaService.fichaMedica$.subscribe((ficha) => {
            if (ficha) {
                this.tokenFichaMedica = ficha;
                this.obtenerEvaluacionMedicaProgreso(); // Solo llama a este método cuando la ficha médica está disponible
            }else{
                this.isLoading = false;
            }
        });
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
                    this.totalItems = response.data.totalItems;
                    this.changeDetector.detectChanges();
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

    getFormatedDate(date: Date) {
        return moment(date, 'YYYY-MM-DDTHH:mm:ssZ').toDate().toLocaleString();
    }

    handlePageEvent(pageEvent: PageEvent) {
        this.size = pageEvent.pageSize;
        this.page = pageEvent.pageIndex;
        this.obtenerEvaluacionMedicaProgreso();
    }

    getKeys() {
        return Object.keys(this.keyLabelsTable);
    }

    crearObjeto() {
        this._evaluacionMedicaService.setTokenEvaluacionMedicaProgreso(null);
        this._evaluacionMedicaService.setVistaDoctorProgreso(this.vistaDoctor);
        this.crear.emit(); 
    }

    editarEvaluacionProgreso(id: string) {
        this._evaluacionMedicaService.setTokenEvaluacionMedicaProgreso(id);
        this._evaluacionMedicaService.setVistaDoctorProgreso(this.vistaDoctor); 
        this.crear.emit();
    }

    verEvaluacionProgreso(id: string) {
        this._evaluacionMedicaService.setTokenEvaluacionMedicaProgreso(id);
        this._evaluacionMedicaService.setVistaDoctorProgreso(false); 
        this.crear.emit();
    }

    async imprimirEvaluacionProgreso(evaluacion: EvaluacionMedicaProgresoDTO){
        try {
            const fichaMedicaProgreso = await this.obtenerFichaMedicaProgreso(evaluacion.tokenIdentificador);

            const resultadoMap = this.agruparPorCriterioPadre(fichaMedicaProgreso.criteriosEvaluacionProgresoAsociados || []);

            const tablaCicatrices = this.construirTabla(resultadoMap, "CICATRICES");
            const tablaLunares = this.construirTabla(resultadoMap, "LUNARES");
            const tablaDiscromias = this.construirTabla(resultadoMap, "DISCROMIAS");
            const tablaTatuajes = this.construirTabla(resultadoMap, "TATUAJE");            
            const tablaHerida = this.construirTabla(resultadoMap, "HERIDA-LESION"); 
            const tablaMutilaciones = this.construirTabla(resultadoMap, "MUTILACIONES");
            const tablaNodulos = this.construirTabla(resultadoMap, "NODULO_TUMOR");
            const tablaAlteracionNeurologica = this.construirTabla(resultadoMap, "ALTERACION_NEUROLOGICA");
            const tablaRegionPerineal = this.construirTabla(resultadoMap, "REGION_PERINEAL");
            const tablaColumnaVertebrales = this.construirTabla(resultadoMap, "COL_VERTEBRALES");
            const tablaAparatoGenital = this.construirTabla(resultadoMap, "APARATO_GENITAL");
            const tablaMalformacionesCongenitas = this.construirTabla(resultadoMap, "MALFORMACION_CONGENITA");
            
            let request = new GeneracionPdfRequest();
            request.nemonico = etiquetasModel.FORMULARIO_HISTORIA_CLINICA_ESQUEMA_CORPORAL;

            // Obtener datos de cabecera
            const datosCabecera = await this.obtenerDatosCabecera();

            request.variables = {
                '[CENTRO]': this.centro.nombre || '',
                "[TITULO-PLANTILLA]": 'Esquema Corporal Ectoscópico',
                "[IMG_BASE64]": this.base64Image,
                "[FECHA_REGISTRO]": (new Date()).toISOString().split("T")[0],
                "[HORA_REGISTRO]": (new Date()).toTimeString().split(" ")[0],
                "[TITULO-INFORME]": 'Esquema Corporal Ectoscópico',
                ...datosCabecera, 
                "[FECHA_HORA_INGRESO]": fichaMedicaProgreso.fecha ? (new Date(fichaMedicaProgreso.fecha)).toISOString().split("T")[0] + ' ' + (new Date(fichaMedicaProgreso.fecha)).toTimeString().split(" ")[0] : '',
                "[TIPO_EVALUACION]": fichaMedicaProgreso.tipoEvaluacionProgreso?.nombre || '',
                "[ESTADO_NUTRICIONAL]": fichaMedicaProgreso.estadoNutricional?.nombre || '',
                "[ESTADO_DESNUTRICION]": fichaMedicaProgreso.tipoDesnutricion?.nombre || '',
                "[GRADO_DESNUTRICION]": fichaMedicaProgreso.grado || '',
                "[PESO]": fichaMedicaProgreso.peso ? fichaMedicaProgreso.peso?.toString() + ' kg' : '',
                "[TALLA]": fichaMedicaProgreso.talla ?fichaMedicaProgreso.talla?.toString() + ' m' : '',
                "[IMC]": fichaMedicaProgreso.imc ? `${fichaMedicaProgreso.imc?.toString()} (${this.funcionesUtils.obtenerClasificacionIMC(fichaMedicaProgreso.imc?.toString())})` : '',
                "[CLINICAMENTE_SANO]": fichaMedicaProgreso.clinicamenteSano ? 'Sí' : 'No',
                "[ESTADO_ENFERMO]": fichaMedicaProgreso.enfermo ? 'Sí' : 'No',
                "[COMENTARIO_IMPRESION_DIAGNOSTICA]": fichaMedicaProgreso.impresionDiagnostico || 'No hay datos disponibles',
                "[COMENTARIO_MANEJO_TERAPÉUTICO]": fichaMedicaProgreso.manejoTerapeutico || 'No hay datos disponibles',
                "[TABLA-CICATRICES]": JSON.stringify(tablaCicatrices || []),
                "[TABLA-LUNARES]": JSON.stringify(tablaLunares || []),
                "[TABLA-DISCROMAS]": JSON.stringify(tablaDiscromias || []),
                "[TABLA-TATUAJES]": JSON.stringify(tablaTatuajes || []),
                "[TABLA-HERIDA-LESION]": JSON.stringify(tablaHerida || []),
                "[TABLA-MUTILACIONES]": JSON.stringify(tablaMutilaciones || []),
                "[TABLA-NODULOS]": JSON.stringify(tablaNodulos || []),
                "[TABLA-ALTERACION-NEUROLOGICA]": JSON.stringify(tablaAlteracionNeurologica || []),
                "[TABLA-REGION-PERINEAL]": JSON.stringify(tablaRegionPerineal || []),
                "[TABLA-COL-VERTEBRALES]": JSON.stringify(tablaColumnaVertebrales || []),
                "[TABLA-APARATO-GENITAL]": JSON.stringify(tablaAparatoGenital || []),
                "[TABLA-MALFORMACIONES-CONGENITAS]": JSON.stringify(tablaMalformacionesCongenitas || [])
            };

            this.pdfService
                .generarPdf(
                    request,
                    this.nemonicoMenu
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

    agruparPorCriterioPadre(data: any[]): Map<string, CriterioEvaluacionMedicaProgresoItemImpresionDTO[]> {
        const mapa = new Map<string, CriterioEvaluacionMedicaProgresoItemImpresionDTO[]>();

        data.forEach(item => {
            const key = item.criterioPadre?.nemonico;

            if (!key) return;

            const valor: CriterioEvaluacionMedicaProgresoItemImpresionDTO = {
            signo_alteracion: item.criterioHijo?.nombre,
            clave: item.criterioHijo?.descripcion,
            ubicacion: item.ubiacionSigno?.nombre,
            lado: item.ladoSigno?.nombre,
            presente: item.presente ? 'Sí' : 'No'
            };

            if (!mapa.has(key)) {
            mapa.set(key, []);
            }

            mapa.get(key)!.push(valor);
        });

        return mapa;
    }

    construirTabla(
        mapa: Map<string, any[]>,
        nemonico: string
        ): TablaPlantilla | null {

        const data = mapa.get(nemonico);

        if (!data || data.length === 0) {
            return null;
        }

        const tabla = new TablaPlantilla();

        // tabla.encabezados = [
        //     'Signo/Alteración',
        //     'Clave',
        //     'Ubicación',
        //     'Lado',
        //     'Presente'
        // ];

        tabla.filas = data.map(item => {
            const fila: any = {
            'Signo/Alteración': item.signo_alteracion,
            'Clave': item.clave,
            'Ubicación': item.ubicacion,
            'Lado': item.lado,
            'Presente': item.presente
            };

            // 🔥 eliminar propiedades undefined
            Object.keys(fila).forEach(key => {
            if (fila[key] === undefined) {
                delete fila[key];
            }
            });

            return fila;
        });

        tabla.encabezados = Object.keys(tabla.filas[0] || {});

        return tabla;
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

    private obtenerFichaMedicaProgreso(tokenIdentificador: string): Promise<any> {
        return new Promise((resolve, reject) => {
            this._evaluacionMedicaService
                .getEvaluacionMedicaProgresoByTokenId(tokenIdentificador)
                .subscribe({
                    next: (
                        response: RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>
                    ) => {
                        if (!response.exito) {
                            reject(
                                'Error al obtener la ficha de identificación'
                            );
                            return;
                        }

                        const fichaMedicaProgreso = response.data;                        
                        resolve(fichaMedicaProgreso);
                    },
                    error: (error: any) => {
                        reject(error);
                    },
                });
        });
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

    eliminarEvaluacionProgreso(evaluacion: EvaluacionMedicaProgresoDTO){
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
              this._evaluacionMedicaService.deleteEvaluacionMedicaProgreso(evaluacion).subscribe({
                  next: (response) => {
                      this.obtenerEvaluacionMedicaProgreso();
    
                      this.customSnackbar.show('Evaluación eliminada con exito', 'Cerrar', "success");
                  },
                  error: (err) => {
                      this.customSnackbar.show('No se pudo eliminar', 'Cerrar', "error");
                  }
              });
          }
      });
    }
}
