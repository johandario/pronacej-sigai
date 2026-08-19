import { ChangeDetectorRef, Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PlanTratamientoIndSeguiDetalleDTO, PlanTratamientoIndSeguiDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { catchError, forkJoin, Observable, tap, throwError } from 'rxjs';
import { ModalSegPtiComponent } from './modal-seg-pti/modal-seg-pti.component';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { MatSelectModule } from '@angular/material/select';
import { ModalSegPtiAbiertoComponent } from './modal-seg-pti-abierto/modal-seg-pti-abierto.component';
import moment from 'moment';
import { CommonModule, Location } from '@angular/common';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { ExpedienteMatrizDetalleDTO, ExpedienteMatrizMedidaDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-seg-pti-cerrado',
  standalone: true,
  imports: [
    MatButtonModule,
    MatExpansionModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatDatepickerModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatTooltipModule,
    MatSelectModule,
    CommonModule
  ],
  providers: [
      { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
      { provide: DateAdapter, useClass: CustomDateAdapter },
      { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
    ],
  templateUrl: './seg-pti-cerrado.component.html',
  styleUrl: './seg-pti-cerrado.component.scss',
  encapsulation: ViewEncapsulation.None,
  
})
export class SegPtiCerradoComponent implements OnInit {  

  estadoVisualizar: boolean = false;
  estadoEditar: boolean = false;
  dimensiones: CatalogoDTO;
  frecuencias: CatalogoDTO;
  tiposPeriodo: CatalogoDTO;

  datosCabecera: any;
  datosCabeceraCargados: boolean = false;

  planTratamientoTokenIdentificador: string;

  mostrarTablasReajustePtiCerrado: boolean = false;
  mostrarTablasReajustePtiAbierto: boolean = false;
  mostrarTablasComunidad: boolean = false;

  planTratamientoSegui: PlanTratamientoIndSeguiDTO = new PlanTratamientoIndSeguiDTO;

  displayedColumns: string[] = ['acciones', 'actividadPrograma', 'frecuencia',
    // 'fechaInicio', 'fechaFin',
    'situacionActual',
    'actitud',
    'aprovechamiento'
  ];

  headerRowDefGrupo = ['grupo1', 'grupoIndicadores', 'grupo2'];

  displayedColumnsAbierto: string[] = [
    'acciones', 
    'componente',
    'objetivo', 
    'actividadPrograma', 
    // 'frecuencia',
    // 'fechaInicio', 'fechaFin',
    // 'situacionActual',
    // 'actitud',
    // 'aprovechamiento',
    'indicadorDeficiente',
    'indicadorEnProceso',
    'indicadorLogrado',
    'analisis',
  ];

  dataSourceIntervObjetivos: MatTableDataSource<any> = new MatTableDataSource();
  dataSourceIntervNoCriminogenos: MatTableDataSource<any> = new MatTableDataSource();
  dataSourceIntervDiferenciada: MatTableDataSource<any> = new MatTableDataSource();
  dataSourceIntervMedidas: MatTableDataSource<any> = new MatTableDataSource();

  private paginatorIntervObjetivos!: MatPaginator;
  private paginatorIntervNoCriminogenos!: MatPaginator;
  private paginatorIntervDiferenciada!: MatPaginator;
  private paginatorIntervMedidas!: MatPaginator;

  
  @ViewChild('paginatorIntervObjetivos')
  set matPaginator1(mp: MatPaginator) {
    this.paginatorIntervObjetivos = mp;
    if (this.dataSourceIntervObjetivos) {
      this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
    }
  }

  @ViewChild('paginatorIntervNoCriminogenos')
  set matPaginator2(mp: MatPaginator) {
    this.paginatorIntervNoCriminogenos = mp;
    if (this.dataSourceIntervNoCriminogenos) {
      this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
    }
  }

  @ViewChild('paginatorIntervDiferenciada')
  set matPaginator3(mp: MatPaginator) {
    this.paginatorIntervDiferenciada = mp;
    if (this.dataSourceIntervDiferenciada) {
      this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
    }
  }

  @ViewChild('paginatorIntervMedidas')
  set matPaginator4(mp: MatPaginator) {
    this.paginatorIntervMedidas = mp;
    if (this.dataSourceIntervMedidas) {
      this.dataSourceIntervMedidas.paginator = this.paginatorIntervMedidas;
    }
  }
  
  // @ViewChild('paginatorIntervObjetivos') paginatorIntervObjetivos: MatPaginator;  
  // @ViewChild('paginatorIntervNoCriminogenos') paginatorIntervNoCriminogenos: MatPaginator;
  // @ViewChild('paginatorIntervDiferenciada') paginatorIntervDiferenciada: MatPaginator; 
  // @ViewChild('paginatorIntervMedidas') paginatorIntervMedidas: MatPaginator; 


  uuid_fp: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_PTI;

  formularioIngresoFormGroup = this.fb.group({ 
    periodoTiempo: [null],
    programa: [null],
    resumen: [null],
    estadoSalud: [null],
    observaciones: [null],
    recomendaciones: [null],
    fechaInicio: [new Date, Validators.required],
    fechaFin: [new Date, Validators.required],
  })

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private planTratamientoService: PlanTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    private dateAdapter: DateAdapter<any>, 
    private fb: FormBuilder,
    public dialog: MatDialog,       
    public funcionesUtils: FuncionesUtils,
    private catalogoService: CatalogoService,    
    private location: Location,
    private http: HttpClient,
    private servicioPdf: PdfService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private expedienteMatrizService: ExpedienteMatrizService,
    private cdr: ChangeDetectorRef
  ) {
    this.dateAdapter.setLocale('es-ES');
  }

  ngOnInit(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.obtenerCatalogos().subscribe(item => {
      if (history.state.seguimiento) {
        this.estadoVisualizar = true;
        this.formularioIngresoFormGroup.disable();
        this.planTratamientoSegui = history.state.seguimiento;

        if (history.state.editar) {
          this.planTratamientoSegui.esEdicion = true;
          this.estadoVisualizar = false;
          this.estadoEditar = true;
          this.formularioIngresoFormGroup.enable();
        }        
        
        this.planTratamientoService.obtenerPlanTratamientoPorId(this.planTratamientoSegui.idPlanTratamiento, this.nemonicoMenu).subscribe(response => {
          let planTratamiento = response.data;
          if (planTratamiento.tipoCentro === 'CJDR') {
            this.mostrarTablasReajustePtiCerrado = true;       
          } else if (planTratamiento.tipoCentro === 'SOA') {
            this.mostrarTablasReajustePtiAbierto = true;
            if (planTratamiento.tipoAbierto === 'Prestación de Servicios a la Comunidad') {
              this.mostrarTablasComunidad = true;
              this.dataSourceIntervMedidas = new MatTableDataSource(this.planTratamientoSegui.intervMedidas);
              this.dataSourceIntervMedidas.paginator = this.paginatorIntervMedidas;
            }            
          }
          this.formularioIngresoFormGroup.patchValue(this.planTratamientoSegui);
          this.formularioIngresoFormGroup.controls['fechaInicio'].setValue(new Date(this.planTratamientoSegui.fechaInicio));
          this.formularioIngresoFormGroup.controls['fechaFin'].setValue(new Date(this.planTratamientoSegui.fechaFin))
  
          this.dataSourceIntervObjetivos = new MatTableDataSource(this.planTratamientoSegui.intervObjetivos);
          this.dataSourceIntervNoCriminogenos = new MatTableDataSource(this.planTratamientoSegui.intervNoCriminogenos);
          this.dataSourceIntervDiferenciada = new MatTableDataSource(this.planTratamientoSegui.intervDiferenciada);          
          
          
          
          const periodoSeleccionado: CatalogoDTO = this.formularioIngresoFormGroup.controls['periodoTiempo'].value;
          if (periodoSeleccionado) {
            const nemonico = periodoSeleccionado.nemonico;
            if (nemonico === 'PTI_CERRADO_SEG_TIPOS_PERIODO_SEMESTRAL') {
              this.formularioIngresoFormGroup.controls['fechaFin'].disable();
            } else if (nemonico === 'PTI_CERRADO_SEG_TIPOS_PERIODO_TRIMESTRAL') {
              this.formularioIngresoFormGroup.controls['fechaFin'].disable();
            } else {
              this.formularioIngresoFormGroup.controls['fechaFin'].enable();
            }
          }
          
          setTimeout(() => {
            this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
            this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
            this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
          });

          this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
            .subscribe({
              next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                if (!respuestaFicha.exito) {
                  load.close();
                  this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
                  return;
                }
                
                const fichaIdentificacion = respuestaFicha.data;
                this.datosCabecera = {
                  nombreCompleto: `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim(),
                  numeroDocumento: fichaIdentificacion.numeroDocumento || '',
                  edadActual: fichaIdentificacion.fechaNacimiento ? this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '',
                  lugarFechaNacimiento: `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`,
                  direccion: fichaIdentificacion.direccion || '',
                }

                  // 5. Obtener grado de instrucción desde catálogos
                this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
                  next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
                    const catalogoModalidadEstudio = respuestaCatalogo.data;
                    this.datosCabecera = {
                      ...this.datosCabecera,
                      gradoInstruccion: catalogoModalidadEstudio?.nombre || ''
                    }

                    // 6. Obtener último detalle de expediente matriz
                    this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                      next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                        const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                        this.datosCabecera = {
                          ...this.datosCabecera,
                          juzgadoProcedencia: detalleExpediente.organoJurisdiccional || '',
                          numOficio: detalleExpediente.numResolucion || '',
                          infraccion: detalleExpediente.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '',
                          fechaSentencia: detalleExpediente.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaCreacion) : '',
                          tipoMedida: detalleExpediente.medidasSocioeducativas?.[0]?.medida?.nombre || '',
                          duracionMedida: `${detalleExpediente.tiempoMedSocEduDias ? detalleExpediente.tiempoMedSocEduDias : 0} días, ${detalleExpediente.tiempoMedSocEduMeses ? detalleExpediente.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente.tiempoMedSocEduAnios ? detalleExpediente.tiempoMedSocEduAnios : 0} años.`,
                          inicioMedida: detalleExpediente.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaInicioMedida) : '',
                          finMedida: detalleExpediente.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaFinMedida) : ''
                        }

                        this.datosCabeceraCargados = true;

                        load.close();
                      },
                      error: (error: any) => {
                        load.close();
                        console.error('Error al obtener el detalle del último expediente:', error);
                        this.dialogMensajeService.mensajeError('Error al obtener el detalle del último expediente');
                      }
                    });                          
                  },
                  error: (error: any) => {
                    load.close();
                    console.error('Error al obtener catálogo:', error);
                    this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
                  }
                });                      
              },
              error: (error: any) => {
                load.close();
                console.error('Error al obtener ficha:', error);
                this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
              }
            });
          
          //load.close();
        });
        
      } else {
        this.obtenerPlanTratamientoActivo().subscribe(response => {
          if (!response.data) {
            this.estadoVisualizar = true;
            this.formularioIngresoFormGroup.disable();
          }
          load.close()
        });
      }
    })

  }

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'PTI_CERRADO_SEG_TIPOS_PERIODO',  
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.tiposPeriodo = results[0]?.data;
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }
  
  obtenerPlanTratamientoActivo(): Observable<any> {
    return this.planTratamientoService.obtenerPlanTratamientoActivoPorTokenFicha(this.uuid_fp, this.nemonicoMenu).pipe(
      tap((response) => {
        if (!response.exito) {
          this.planTratamientoService.checkError(response);
          return;
        }        
        if (response) {          
          let planTratamiento = response.data;
          this.planTratamientoSegui.idPlanTratamiento = planTratamiento.idPlanTratamiento;

          if (planTratamiento.tipoCentro === 'CJDR') {
            this.mostrarTablasReajustePtiCerrado = true;       
          } else if (planTratamiento.tipoCentro === 'SOA') {
            this.mostrarTablasReajustePtiAbierto = true;
            if (planTratamiento.tipoAbierto === 'Prestación de Servicios a la Comunidad') {
              this.mostrarTablasComunidad = true;

              let rows: PlanTratamientoIndSeguiDetalleDTO[] = [];
              for (let item of planTratamiento.intervMedidas) {
                if(item.activo) {
                  let row: PlanTratamientoIndSeguiDetalleDTO = new PlanTratamientoIndSeguiDetalleDTO;
                  row.planTratamientoIndInterv = item;
                  rows.push(row);
                }
              }
              this.planTratamientoSegui.intervMedidas = rows;
              this.dataSourceIntervMedidas = new MatTableDataSource(rows);
              this.dataSourceIntervMedidas.paginator = this.paginatorIntervMedidas;
            }            
          }

          let rows: PlanTratamientoIndSeguiDetalleDTO[] = [];
          for (let item of planTratamiento.intervObjetivos) {
            if(item.activo) {
              let row: PlanTratamientoIndSeguiDetalleDTO = new PlanTratamientoIndSeguiDetalleDTO;
              row.planTratamientoIndInterv = item;
              rows.push(row);
            }
          }
          this.planTratamientoSegui.intervObjetivos = rows;
          this.dataSourceIntervObjetivos.data = this.planTratamientoSegui.intervObjetivos;
          this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;

          rows = [];
          for (let item of planTratamiento.intervNoCriminogenos) {
            if(item.activo) {
              let row: PlanTratamientoIndSeguiDetalleDTO = new PlanTratamientoIndSeguiDetalleDTO;
              row.planTratamientoIndInterv = item;
              rows.push(row);
            }
          }
          this.planTratamientoSegui.intervNoCriminogenos = rows;
          this.dataSourceIntervNoCriminogenos = new MatTableDataSource(rows);
          this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;

          rows = [];
          for (let item of planTratamiento.intervDiferenciada) {
            if(item.activo) {
              let row: PlanTratamientoIndSeguiDetalleDTO = new PlanTratamientoIndSeguiDetalleDTO;
              row.planTratamientoIndInterv = item;
              rows.push(row);
            }
          }
          this.planTratamientoSegui.intervDiferenciada = rows;
          this.dataSourceIntervDiferenciada = new MatTableDataSource(rows);
          this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
        }

        this.cdr.detectChanges();

      }),
      catchError(err => {
        this.planTratamientoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }  

  mostrarFilaInterv(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    this.dialog.open(ModalSegPtiComponent, {      
      data: {fila: fila, estado: 'ver'},
      width: '600px'
    }); 
  }  

  editarFilaIntervObjetivos(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalSegPtiComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) { 
        this.planTratamientoSegui.intervObjetivos[index] = result;
        this.dataSourceIntervObjetivos = new MatTableDataSource(this.planTratamientoSegui.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  }

  editarFilaIntervNoCriminogenos(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalSegPtiComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {        
        this.planTratamientoSegui.intervNoCriminogenos[index] = result;
        this.dataSourceIntervNoCriminogenos = new MatTableDataSource(this.planTratamientoSegui.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  }

  editarFilaIntervDiferenciada(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalSegPtiComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {        
        this.planTratamientoSegui.intervDiferenciada[index] = result;
        this.dataSourceIntervDiferenciada = new MatTableDataSource(this.planTratamientoSegui.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
      }
    })
  }

  mostrarFilaIntervAbierto(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    this.dialog.open(ModalSegPtiAbiertoComponent, {      
      data: {fila: fila, estado: 'ver'},
      width: '600px'
    }); 
  }  

  editarFilaIntervNoCriminogenosAbierto(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalSegPtiAbiertoComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) { 
        this.planTratamientoSegui.intervNoCriminogenos[index] = result;
        this.dataSourceIntervNoCriminogenos = new MatTableDataSource(this.planTratamientoSegui.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  }

  editarFilaIntervMedidasAbierto(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalSegPtiAbiertoComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) { 
        this.planTratamientoSegui.intervMedidas[index] = result;
        this.dataSourceIntervMedidas = new MatTableDataSource(this.planTratamientoSegui.intervMedidas);
        this.dataSourceIntervMedidas.paginator = this.paginatorIntervMedidas;
      }
    })
  }

  editarFilaIntervDiferenciadaAbierto(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    console.log('fila',fila);
    const dialogRef = this.dialog.open(ModalSegPtiAbiertoComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) { 
        this.planTratamientoSegui.intervDiferenciada[index] = result;
        this.dataSourceIntervDiferenciada = new MatTableDataSource(this.planTratamientoSegui.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
      }
    })
  }

  editarFilaIntervObjetivosAbierto(fila: PlanTratamientoIndSeguiDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalSegPtiAbiertoComponent, {
      disableClose: true,
      data: {fila: fila},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) { 
        this.planTratamientoSegui.intervObjetivos[index] = result;
        this.dataSourceIntervObjetivos = new MatTableDataSource(this.planTratamientoSegui.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  }

  seleccionarTipo(event: any) { 
    const nemonico = event.value.nemonico;
    if (nemonico) {
      this.formularioIngresoFormGroup.controls['fechaFin'].disable();
      if (nemonico === 'PTI_CERRADO_SEG_TIPOS_PERIODO_SEMESTRAL') {
        const fechaHastaMoment = moment(this.formularioIngresoFormGroup.controls['fechaInicio'].value);
        const nuevaFecha = fechaHastaMoment.add(6, 'months').toDate();
        this.formularioIngresoFormGroup.controls['fechaFin'].setValue(nuevaFecha);
      } else if (nemonico === 'PTI_CERRADO_SEG_TIPOS_PERIODO_TRIMESTRAL') {
        const fechaHastaMoment = moment(this.formularioIngresoFormGroup.controls['fechaInicio'].value);
        const nuevaFecha = fechaHastaMoment.add(3, 'months').toDate();
        this.formularioIngresoFormGroup.controls['fechaFin'].setValue(nuevaFecha);
      } else {
        this.formularioIngresoFormGroup.controls['fechaFin'].enable();
  
      }
    }
  }

  calcularFechaSalida(event: any) {    
    const periodoSeleccionado: CatalogoDTO = this.formularioIngresoFormGroup.controls['periodoTiempo'].value;
    if (periodoSeleccionado) {
      const nemonico = periodoSeleccionado.nemonico;
      this.formularioIngresoFormGroup.controls['fechaFin'].disable();
      if (nemonico === 'PTI_CERRADO_SEG_TIPOS_PERIODO_SEMESTRAL') {
        const fechaHastaMoment = moment(this.formularioIngresoFormGroup.controls['fechaInicio'].value);
        const nuevaFecha = fechaHastaMoment.add(6, 'months').toDate();
        this.formularioIngresoFormGroup.controls['fechaFin'].setValue(nuevaFecha);
      } else if (nemonico === 'PTI_CERRADO_SEG_TIPOS_PERIODO_TRIMESTRAL') {
        const fechaHastaMoment = moment(this.formularioIngresoFormGroup.controls['fechaInicio'].value);
        const nuevaFecha = fechaHastaMoment.add(3, 'months').toDate();
        this.formularioIngresoFormGroup.controls['fechaFin'].setValue(nuevaFecha);
      } else {
        this.formularioIngresoFormGroup.controls['fechaFin'].enable();
      }
    }
  }

  regresar() {
    this.location.back();
  }

  guardarSeguimiento() {
    let fechaInicio = this.formularioIngresoFormGroup.controls['fechaInicio'].value;
    let fechaFinal = this.formularioIngresoFormGroup.controls['fechaFin'].value;
    
    fechaInicio = new Date(fechaInicio.getFullYear(), fechaInicio.getMonth(), fechaInicio.getDate())
    fechaFinal = new Date(fechaFinal.getFullYear(), fechaFinal.getMonth(), fechaFinal.getDate())

    if (fechaInicio.getTime() <= fechaFinal.getTime()) {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se guardará un registro de seguimiento de Plan de Tratamiento Individual.',
        "Deseas continuar?"
      );
      
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              Object.assign(this.planTratamientoSegui, this.formularioIngresoFormGroup.value);
              this.planTratamientoSegui.fechaFin = this.formularioIngresoFormGroup.controls['fechaFin'].value;
              this.planTratamientoSegui.tokenPadre = this.uuid_fp;  
              // this.planTratamientoSegui.idPlanTratamiento = this.planTratamientoTokenIdentificador;  
              this.planTratamientoService.crearSeguimientoPlanTratamiento(this.planTratamientoSegui, this.nemonicoMenu).subscribe({
                next: (response: RespuestaPorDefecto<PlanTratamientoIndSeguiDTO>) => {
                  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
        
                    return;
                  }      
                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}`], { queryParams: { tabIndex: 1 } });         
                },
                error: (error: any) => {
                  this.planTratamientoService.checkError(error);
                }
              });
            }
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeAdvertencia("Fecha incorrecta", "La fecha de fin no puede ser menor a la fecha inicio")
    }    
  }

  /**
   * Imprime un PDF para el seguimiento PTI actual
   */
  imprimirFichaCJDR() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de seguimiento PTI?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.dialogMensajeService.mensajeLoading("Preparando la impresión del informe de seguimiento PTI...");
          
          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
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
                      const direccion = fichaIdentificacion.direccion || 'N/A';
                      
                      //Obtener grado de instrucción desde catálogos
                      this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
                        next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
                          console.log(respuestaCatalogo);
                          const catalogoModalidadEstudio = respuestaCatalogo.data;
                          const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';
              
                          // 6. Obtener último detalle de expediente matriz
                          this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                            next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                              const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                              const juzgadoProcedencia = detalleExpediente.organoJurisdiccional || '';
                              const numExpediente = detalleExpediente.numExpediente || '';
                              const infraccion = detalleExpediente.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                              const fechaSentencia = detalleExpediente.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaCreacion) : '';
                              const tipoMedida = detalleExpediente.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                              const duracionMedida = `${detalleExpediente.tiempoMedSocEduDias ? detalleExpediente.tiempoMedSocEduDias : 0} días, ${detalleExpediente.tiempoMedSocEduMeses ? detalleExpediente.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente.tiempoMedSocEduAnios ? detalleExpediente.tiempoMedSocEduAnios : 0} años.`;
                              const inicioMedida = detalleExpediente.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaInicioMedida) : '';
                              const finMedida = detalleExpediente.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaFinMedida) : '';
            
                              // 5. Crear tabla de objetivos de intervención
                              let tablaObjetivosIntervencion = new TablaPlantilla();
                              tablaObjetivosIntervencion.encabezados = [
                                'Actividad o Programa',
                                'Situación Actual',
                                'Frecuencia',
                                'Actitud',
                                'Aprovechamiento'
                              ];
                              
                              tablaObjetivosIntervencion.filas = this.planTratamientoSegui.intervObjetivos && this.planTratamientoSegui.intervObjetivos.length > 0
                                ? this.planTratamientoSegui.intervObjetivos.map(objetivo => ({
                                    'Actividad o Programa': objetivo.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                    'Situación Actual': objetivo.situacionActual?.nombre || 'No especificado',
                                    'Frecuencia': objetivo.frecuenciaParticipacion?.nombre || 'No especificado',
                                    'Actitud': objetivo.actitud?.nombre || 'No especificado',
                                    'Aprovechamiento': objetivo.aprovechamiento?.nombre || 'No especificado'
                                  }))
                                : [{ 
                                    'Actividad o Programa': 'No hay registros disponibles',
                                    'Situación Actual': '-',
                                    'Frecuencia': '-',
                                    'Actitud': '-',
                                    'Aprovechamiento': '-'
                                  }];
                              
                              // 6. Crear tabla de factores no criminógenos
                              let tablaFactoresNoCriminogenos = new TablaPlantilla();
                              tablaFactoresNoCriminogenos.encabezados = [
                                'Actividad o Programa',
                                'Situación Actual',
                                'Frecuencia',
                                'Actitud',
                                'Aprovechamiento'
                              ];
                              
                              tablaFactoresNoCriminogenos.filas = this.planTratamientoSegui.intervNoCriminogenos && this.planTratamientoSegui.intervNoCriminogenos.length > 0
                                ? this.planTratamientoSegui.intervNoCriminogenos.map(factor => ({
                                    'Actividad o Programa': factor.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                    'Situación Actual': factor.situacionActual?.nombre || 'No especificado',
                                    'Frecuencia': factor.frecuenciaParticipacion?.nombre || 'No especificado',
                                    'Actitud': factor.actitud?.nombre || 'No especificado',
                                    'Aprovechamiento': factor.aprovechamiento?.nombre || 'No especificado'
                                  }))
                                : [{ 
                                    'Actividad o Programa': 'No hay registros disponibles',
                                    'Situación Actual': '-',
                                    'Frecuencia': '-',
                                    'Actitud': '-',
                                    'Aprovechamiento': '-'
                                  }];
                              
                              // 7. Crear tabla de intervención diferenciada
                              let tablaIntervencionDiferenciada = new TablaPlantilla();
                              tablaIntervencionDiferenciada.encabezados = [
                                'Actividad o Programa',
                                'Situación Actual',
                                'Frecuencia',
                                'Actitud',
                                'Aprovechamiento'
                              ];
                              
                              tablaIntervencionDiferenciada.filas = this.planTratamientoSegui.intervDiferenciada && this.planTratamientoSegui.intervDiferenciada.length > 0
                                ? this.planTratamientoSegui.intervDiferenciada.map(intervencion => ({
                                    'Actividad o Programa': intervencion.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                    'Situación Actual': intervencion.situacionActual?.nombre || 'No especificado',
                                    'Frecuencia': intervencion.frecuenciaParticipacion?.nombre || 'No especificado',
                                    'Actitud': intervencion.actitud?.nombre || 'No especificado',
                                    'Aprovechamiento': intervencion.aprovechamiento?.nombre || 'No especificado'
                                  }))
                                : [{ 
                                    'Actividad o Programa': 'No hay registros disponibles',
                                    'Situación Actual': '-',
                                    'Frecuencia': '-',
                                    'Actitud': '-',
                                    'Aprovechamiento': '-'
                                  }];
                              
                              // 8. Crear la solicitud para generar el PDF
                              let solicitudPdf = new GeneracionPdfRequest();
                              solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_PTI';
                              
                              // 9. Obtener los valores del formulario para los datos generales
                              const formValues = this.formularioIngresoFormGroup.getRawValue();
                              
                              // 10. Incluir las variables para el PDF
                              solicitudPdf.variables = {
                                "[IMG_BASE64]": imagenBase64,
                                "[FECHA-REGISTRO]": this.funcionesUtils.formatearFecha(new Date()),
                                "[HORA-REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                                "[CENTRO]": fichaIdentificacion.centroIngreso || '',
                                "[NOMBRES-APELLIDOS]": nombreCompleto,
                                "[DNI]": fichaIdentificacion.numeroDocumento || '',
                                "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
                                "[EDAD]": edadActual,
                                "[DIRECCION]": direccion,                    
                                "[GRADO-INSTRUCCION]": gradoInstruccion,
                                "[JUZGADO-PROCEDENCIA]": juzgadoProcedencia,
                                "[NUM_EXPEDIENTE]": numExpediente,
                                "[INFRACCION]": infraccion,
                                "[FECHA-SENTENCIA]": fechaSentencia,
                                "[TIPO-MEDIDA]": tipoMedida,
                                "[DURACION-MEDIDA]": duracionMedida,
                                "[FECHA-INICIO-MEDIDA]": inicioMedida,
                                "[FECHA-FINALIZACION]": finMedida,                 
                                "[TIPO-PERIODO]": this.planTratamientoSegui.periodoTiempo?.nombre || formValues.periodoTiempo?.nombre || 'No especificado',
                                "[FECHA-INICIO]": this.funcionesUtils.formatearFecha(this.planTratamientoSegui.fechaInicio || formValues.fechaInicio) || 'No especificado',
                                "[FECHA-FIN]": this.funcionesUtils.formatearFecha(this.planTratamientoSegui.fechaFin || formValues.fechaFin) || 'No especificado',
                                "[PROGRAMA]": this.planTratamientoSegui.programa || formValues.programa || 'No especificado',
                                "[RESUMEN]": this.planTratamientoSegui.resumen || formValues.resumen || 'No especificado',
                                "[ESTADO-SALUD]": this.planTratamientoSegui.estadoSalud || formValues.estadoSalud || 'No especificado',
                                "[TABLA-OBJETIVOS-INTERVENCION]": JSON.stringify(tablaObjetivosIntervencion),
                                "[TABLA-FACTORES-NO-CRIMINOGENOS]": JSON.stringify(tablaFactoresNoCriminogenos),
                                "[TABLA-INTERVENCION-DIFERENCIADA]": JSON.stringify(tablaIntervencionDiferenciada),
                                "[OBSERVACIONES]": this.planTratamientoSegui.observaciones || formValues.observaciones || 'No se registraron observaciones',
                                "[RECOMENDACIONES]": this.planTratamientoSegui.recomendaciones || formValues.recomendaciones || 'No se registraron recomendaciones'
                              };
                              
                              // 11. Llamar al servicio para generar el PDF
                              this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                                next: (respuesta: RespuestaPorDefecto<string>) => {
                                  dialogoCarga.close();
                                  if (!respuesta.exito) {
                                    console.error('Error al generar PDF:', respuesta);
                                    this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                                    return;
                                  }
                                  
                                  // 12. Abrir el PDF en una nueva pestaña
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
                              console.error('Error al obtener el expediente:', error);
                              this.dialogMensajeService.mensajeError('Error al obtener el expediente');
                            }
                          });
                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al obtener catálogo:', error);
                          this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
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
   * Imprime un PDF para el seguimiento PTI en centros abiertos (SOA)
   */
  imprimirFichaSOA() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de seguimiento PTI para SOA?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.dialogMensajeService.mensajeLoading("Preparando la impresión del informe de seguimiento PTI...");
          
          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
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
                      const direccion = fichaIdentificacion.direccion || '';

                       // 5. Obtener grado de instrucción desde catálogos
                      this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
                        next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
                          const catalogoModalidadEstudio = respuestaCatalogo.data;
                          const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

                          // 6. Obtener último detalle de expediente matriz
                          this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                            next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                              const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                              const juzgadoProcedencia = detalleExpediente.organoJurisdiccional || '';
                              const numOficio = detalleExpediente.numResolucion || '';
                              const infraccion = detalleExpediente.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                              const fechaSentencia = detalleExpediente.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaCreacion) : '';
                              const tipoMedida = detalleExpediente.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                              const duracionMedida = `${detalleExpediente.tiempoMedSocEduDias ? detalleExpediente.tiempoMedSocEduDias : 0} días, ${detalleExpediente.tiempoMedSocEduMeses ? detalleExpediente.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente.tiempoMedSocEduAnios ? detalleExpediente.tiempoMedSocEduAnios : 0} años.`;
                              const inicioMedida = detalleExpediente.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaInicioMedida) : '';
                              const finMedida = detalleExpediente.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente.fechaFinMedida) : '';
                      
                              // 5. Tablas específicas para SOA
                              // Matriz PTI
                              let tablaMatrizPTI = new TablaPlantilla();
                              tablaMatrizPTI.encabezados = [
                                'Componente',
                                'Objetivo',
                                'Actividad o Programa',
                                'Indicador (D)',
                                'Indicador (EP)',
                                'Indicador (L)',
                                'Análisis'
                              ];
                              
                              tablaMatrizPTI.filas = this.planTratamientoSegui.intervObjetivos && this.planTratamientoSegui.intervObjetivos.length > 0
                                ? this.planTratamientoSegui.intervObjetivos.map(objetivo => ({
                                    'Componente': objetivo.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                    'Objetivo': objetivo.planTratamientoIndInterv?.objetivo || 'No especificado',
                                    'Actividad o Programa': objetivo.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                    'Indicador (D)': objetivo.indicadorDeficiente ? 'X' : '',
                                    'Indicador (EP)': objetivo.indicadorEnProceso ? 'X' : '',
                                    'Indicador (L)': objetivo.indicadorLogrado ? 'X' : '',
                                    'Análisis': objetivo.analisis || 'No especificado'
                                  }))
                                : [{ 
                                    'Componente': 'No hay registros disponibles',
                                    'Objetivo': 'No hay registros disponibles',
                                    'Actividad o Programa': '-',
                                    'Indicador (D)': '-',
                                    'Indicador (EP)': '-',
                                    'Indicador (L)': '-',
                                    'Análisis': '-'
                                  }];
                              
                              // Matriz de cumplimiento de medidas accesorias
                              let tablaMedidasAccesorias = new TablaPlantilla();
                              tablaMedidasAccesorias.encabezados = [
                                'Componente',
                                'Objetivo',
                                'Actividad o Programa',
                                'Indicador (D)',
                                'Indicador (EP)',
                                'Indicador (L)',
                                'Análisis'
                              ];
                              
                              tablaMedidasAccesorias.filas = this.planTratamientoSegui.intervDiferenciada && this.planTratamientoSegui.intervDiferenciada.length > 0
                                ? this.planTratamientoSegui.intervDiferenciada.map(intervencion => ({
                                    'Componente': intervencion.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                    'Objetivo': intervencion.planTratamientoIndInterv?.objetivo || 'No especificado',
                                    'Actividad o Programa': intervencion.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                    'Indicador (D)': intervencion.indicadorDeficiente ? 'X' : '',
                                    'Indicador (EP)': intervencion.indicadorEnProceso ? 'X' : '',
                                    'Indicador (L)': intervencion.indicadorLogrado ? 'X' : '',
                                    'Análisis': intervencion.analisis || 'No especificado'
                                  }))
                                : [{ 
                                    'Componente': 'No hay registros disponibles',
                                    'Objetivo': 'No hay registros disponibles',
                                    'Actividad o Programa': '-',
                                    'Indicador (D)': '-',
                                    'Indicador (EP)': '-',
                                    'Indicador (L)': '-',
                                    'Análisis': '-'
                                  }];
                              
                              // Si hay tablas de comunidad, preparamos sus datos
                              let tablaFasePreparacion = null;
                              let tablaFaseEjecucion = null;
                              
                              if (this.mostrarTablasComunidad) {
                                tablaFasePreparacion = new TablaPlantilla();
                                tablaFasePreparacion.encabezados = [
                                  'Componente',
                                  'Objetivo',
                                  'Actividad o Programa',
                                  'Indicador (D)',
                                  'Indicador (EP)',
                                  'Indicador (L)',
                                  'Análisis'
                                ];
                                
                                tablaFasePreparacion.filas = this.planTratamientoSegui.intervNoCriminogenos && this.planTratamientoSegui.intervNoCriminogenos.length > 0
                                  ? this.planTratamientoSegui.intervNoCriminogenos.map(item => ({
                                      'Componente': item.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                      'Objetivo': item.planTratamientoIndInterv?.objetivo || 'No especificado',
                                      'Actividad o Programa': item.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                      'Indicador (D)': item.indicadorDeficiente ? 'X' : '',
                                      'Indicador (EP)': item.indicadorEnProceso ? 'X' : '',
                                      'Indicador (L)': item.indicadorLogrado ? 'X' : '',
                                      'Análisis': item.analisis || 'No especificado'
                                    }))
                                  : [{ 
                                      'Componente': 'No hay registros disponibles',
                                      'Objetivo': 'No hay registros disponibles',
                                      'Actividad o Programa': '-',
                                      'Indicador (D)': '-',
                                      'Indicador (EP)': '-',
                                      'Indicador (L)': '-',
                                      'Análisis': '-'
                                    }];
                                    
                                tablaFaseEjecucion = new TablaPlantilla();
                                tablaFaseEjecucion.encabezados = [
                                  'Componente',
                                  'Objetivo',
                                  'Actividad o Programa',
                                  'Indicador (D)',
                                  'Indicador (EP)',
                                  'Indicador (L)',
                                  'Análisis'
                                ];
                                
                                tablaFaseEjecucion.filas = this.planTratamientoSegui.intervMedidas && this.planTratamientoSegui.intervMedidas.length > 0
                                  ? this.planTratamientoSegui.intervMedidas.map(item => ({
                                      'Componente': item.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                      'Objetivo': item.planTratamientoIndInterv?.objetivo || 'No especificado',
                                      'Actividad o Programa': item.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                      'Indicador (D)': item.indicadorDeficiente ? 'X' : '',
                                      'Indicador (EP)': item.indicadorEnProceso ? 'X' : '',
                                      'Indicador (L)': item.indicadorLogrado ? 'X' : '',
                                      'Análisis': item.analisis || 'No especificado'
                                    }))
                                  : [{ 
                                      'Componente': 'No hay registros disponibles',
                                      'Objetivo': 'No hay registros disponibles',
                                      'Actividad o Programa': '-',
                                      'Indicador (D)': '-',
                                      'Indicador (EP)': '-',
                                      'Indicador (L)': '-',
                                      'Análisis': '-'
                                    }];
                              }
                              
                              // 6. Crear la solicitud para generar el PDF
                              let solicitudPdf = new GeneracionPdfRequest();
                              solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_PTI_SOA';
                              
                              // 7. Obtener los valores del formulario
                              const formValues = this.formularioIngresoFormGroup.getRawValue();
                              
                              // 8. Incluir las variables para el PDF
                              solicitudPdf.variables = {
                                "[IMG_BASE64]": imagenBase64,
                                "[FECHA-REGISTRO]": this.funcionesUtils.formatearFecha(new Date()),
                                "[HORA-REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                                "[CENTRO]": fichaIdentificacion.centroIngreso || '',
                                "[NOMBRES-APELLIDOS]": nombreCompleto,
                                "[DNI]": fichaIdentificacion.numeroDocumento || '',
                                "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
                                "[EDAD]": edadActual,
                                "[GRADO-INSTRUCCION]": gradoInstruccion,
                                "[DIRECCION]": direccion,
                                "[JUZGADO-PROCEDENCIA]": juzgadoProcedencia,
                                "[NUM-OFICIO-JUZGADO]": numOficio,
                                "[INFRACCION]": infraccion,
                                "[FECHA-SENTENCIA]": fechaSentencia,
                                "[TIPO-MEDIDA]": tipoMedida,
                                "[DURACION-MEDIDA]": duracionMedida,
                                "[FECHA-INICIO-MEDIDA]": inicioMedida,
                                "[FECHA-FINALIZACION]": finMedida,                        
                                "[FECHA-INICIO]": this.funcionesUtils.formatearFecha(this.planTratamientoSegui.fechaInicio || formValues.fechaInicio) || 'No especificado',
                                "[FECHA-FIN]": this.funcionesUtils.formatearFecha(this.planTratamientoSegui.fechaFin || formValues.fechaFin) || 'No especificado',
                                "[TABLA-MATRIZ-PTI]": JSON.stringify(tablaMatrizPTI),
                                "[TABLA-MEDIDAS-ACCESORIAS]": JSON.stringify(tablaMedidasAccesorias),
                                "[RECOMENDACIONES]": this.planTratamientoSegui.recomendaciones || formValues.recomendaciones || 'No se registraron recomendaciones'
                              };
                              
                              // Agregar variables condicionales para comunidad
                              if (this.mostrarTablasComunidad) {
                                solicitudPdf.variables["[MOSTRAR-COMUNIDAD]"] = "block";
                                solicitudPdf.variables["[TABLA-FASE-PREPARACION]"] = JSON.stringify(tablaFasePreparacion);
                                solicitudPdf.variables["[TABLA-FASE-EJECUCION]"] = JSON.stringify(tablaFaseEjecucion);
                              } else {
                                solicitudPdf.variables["[MOSTRAR-COMUNIDAD]"] = "none";
                                let tablaVacia = new TablaPlantilla;
                                solicitudPdf.variables["[TABLA-FASE-PREPARACION]"] = JSON.stringify(tablaVacia);
                                solicitudPdf.variables["[TABLA-FASE-EJECUCION]"] = JSON.stringify(tablaVacia);
                                    
                              }
                              
                              // 9. Llamar al servicio para generar el PDF
                              this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                                next: (respuesta: RespuestaPorDefecto<string>) => {
                                  dialogoCarga.close();
                                  if (!respuesta.exito) {
                                    console.error('Error al generar PDF:', respuesta);
                                    this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                                    return;
                                  }
                                  
                                  // 10. Abrir el PDF en una nueva pestaña
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
                              console.error('Error al obtener el detalle del último expediente:', error);
                              this.dialogMensajeService.mensajeError('Error al obtener el detalle del último expediente');
                            }
                          });                          
                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al obtener catálogo:', error);
                          this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
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
   * Función principal de impresión que determina qué formato usar según el tipo de centro
   */
  imprimirFicha() {
    if (this.mostrarTablasReajustePtiAbierto) {
      // Si es un centro abierto (SOA), usar el formato para SOA
      this.imprimirFichaSOA();
    } else {
      // Si es un centro cerrado (CJDR), usar el formato para CJDR
      this.imprimirFichaCJDR();
    }
  }
}
