import { AfterViewInit, Component, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { Form, FormArray, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PertenenciaDetalleDTO, PertenenciaDTO } from 'app/core/model/both/pertenenciaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PertenenciaService } from 'app/modules/seguridad/services/pertenencia.service';
import { ModalEditaPertenenciaComponent } from './modal-edita-pertenencia/modal-edita-pertenencia.component';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { environment } from 'environments/environment';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { catchError, concatMap, forkJoin, iif, map, Observable, of, tap, throwError } from 'rxjs';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { PdfService } from 'app/core/services/pdf.service';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { PertenenciaDocumentoDTO } from 'app/core/model/request/ia/PertenenciaDocumentoDTO.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { PertenenciaDocumentosRequest } from 'app/core/model/request/ia/PertenenciaDocumentosRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-crear-editar-recepcion-entrega',
  standalone: true,
  imports: [
    MatTabsModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatRadioModule,
    MatSlideToggleModule,
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    RouterLink,
    MatTooltipModule,
    MatPaginatorModule,
    SubidaDeDocumentosComponent,
    DocumentosSubidosTablaComponent,
  ],
  templateUrl: './crear-editar-recepcion-entrega.component.html',
  styleUrl: './crear-editar-recepcion-entrega.component.scss',
  encapsulation: ViewEncapsulation.None,  
})
export class CrearEditarRecepcionEntregaComponent implements OnInit {
  @Input() estadoEditar: boolean;
  estadoVisualizar: Boolean = false;

  selectedIndex: number = 0;

  displayedColumns: string[] = ['acciones', 'tipo', 'nombre', 'cantidad', 'estado'];
  columnsSalidaEgreso: string[] = ['tipo', 'nombre', 'cantidad', 'estado'];

  @ViewChild("tablaDocumentos") tablaDocumentos: DocumentosSubidosTablaComponent;
  tiposDeDocumentosSistema: TipoDeDocumento[];  

  mostrarSubidaDocumentos: boolean = false;

  idPertenencia: number;
  tokenIdentificadorPertenencia: string = '58a047c9-5787-4d55-9545-ba6ab0537b46';

  dataSourceEgreso = new MatTableDataSource<any>;
  dataSourceIngreso = new MatTableDataSource<any>;
  dataSourceSalidaEgreso = new MatTableDataSource<any>;
  dataSourceSalidaIngreso = new MatTableDataSource<any>;
  
  registro: PertenenciaDTO;

  ingreso: FichaIngresoDTO = new FichaIngresoDTO;
  fichaIdentificacion: FichaIdentificacionDTO = new FichaIdentificacionDTO;

  filaEditada: PertenenciaDetalleDTO = null;

  comentarioIngresosFormControl = new FormControl('');
  comentarioEgresosFormControl = new FormControl('');
  comentarioSalidaIngresosFormControl = new FormControl('');
  comentarioSalidaEgresosFormControl = new FormControl('');

  ingresoFormControl = new FormControl(null);

  formEgresos: FormGroup;
  formIngresos: FormGroup;
  formSalidaIngresos: FormGroup;

  tipos: CatalogoDTO[] = [];
  estados: CatalogoDTO[] = [];

  uuid_fp: string;

  ingresos: FichaIngresoDTO[] = [];

  base64Image: string | null = null;
  nemonicoMenu = etiquetasModel.NEMONICO_RETIRO_PERTENENCIAS;

  @ViewChild('paginatorEgreso') paginatorEgreso: MatPaginator;
  @ViewChild('paginatorIngreso') paginatorIngreso: MatPaginator;
  @ViewChild('paginatorSalidaEgreso') paginatorSalidaEgreso: MatPaginator;
  @ViewChild('paginatorSalidaIngreso') paginatorSalidaIngreso: MatPaginator;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private router: Router,
    private pertenenciaService: PertenenciaService,
    private fichaIngresoService: FichaIngresoService,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private funcionesUtils: FuncionesUtils,
    private pdfService: PdfService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    private http: HttpClient,
    public dialog: MatDialog,
  ) {
    this.formEgresos = this.fb.group({
      filas: this.fb.array([]),
    });

    this.formIngresos = this.fb.group({
      filas: this.fb.array([]),
    });

    this.formSalidaIngresos = this.fb.group({
      filas: this.fb.array([]),
    });

    this.dataSourceEgreso = new MatTableDataSource((this.formEgresos.get('filas') as FormArray).controls);
    this.dataSourceEgreso.paginator = this.paginatorEgreso;

    this.dataSourceIngreso = new MatTableDataSource((this.formIngresos.get('filas') as FormArray).controls);
    this.dataSourceIngreso.paginator = this.paginatorIngreso;

    this.dataSourceSalidaIngreso = new MatTableDataSource((this.formSalidaIngresos.get('filas') as FormArray).controls);
    this.dataSourceSalidaIngreso.paginator = this.paginatorSalidaIngreso;
  }  

  get filasEgreso(): FormArray {
    return this.formEgresos.get('filas') as FormArray;
  }

  get filasIngreso(): FormArray {
    return this.formIngresos.get('filas') as FormArray;
  }

  get filasSalidaIngreso(): FormArray {
    return this.formSalidaIngresos.get('filas') as FormArray;
  }

  ngOnInit(): void {    
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    
    this.cargarDatos();
  }

  // ngOnInit(): void {
  //   this.obtenerFichasIngreso();
  //   this.obtenerCatalogos();
  //   this.uuid_fp = this.route.snapshot.params['uuid_fp'];
  //   this.route.queryParams.subscribe(params => {
  //     const numDoc = params['numDoc'];
  //     if (numDoc) {
  //       const state = params['state'];
  //       if (state) {
  //         this.estadoVisualizar = true;    
  //       }
  //       this.estadoEditar = true;
  //       this.pertenenciaService.obtenerPertenenciaPorId(numDoc, '').subscribe(item => {
  //         // this.obtenerFichaIdentificacion();
  //         this.registro = item.data; 
  //         this.registro.esEdicion = true;
  //         const ingresoSeleccionado = this.ingresos.find(ingreso => ingreso.tokenIdentificador == this.registro.tokenFichaIngreso);
  //         if (ingresoSeleccionado) {
  //           this.ingresoFormControl.setValue(ingresoSeleccionado);          
  //           let event = {value: ingresoSeleccionado};
  //           this.seleccionarIngreso(event);
  //         }

  //         if (this.registro && this.registro.tokenIdentificador) {
  //           this.mostrarSubidaDocumentos = true;
  //         }
          
  //         for (let egreso of this.registro.detalleEgresos) {
  //           const fila = this.fb.group({
  //             tipo: [{ value: egreso.tipo as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],
  //             nombre: [{ value: egreso.nombre, disabled: this.estadoVisualizar}, Validators.required],
  //             cantidad: [{ value: egreso.cantidad, disabled: this.estadoVisualizar}, [Validators.required, Validators.max(999999), Validators.min(0)]],    
  //             estado: [{ value: egreso.estado as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],   
  //             observacion: [{ value: egreso.observacion, disabled: this.estadoVisualizar}]
  //           });            
        
  //           this.filasEgreso.controls.push(fila);
  //         }
  //         this.actualizarTablaEgresos();

  //         for (let ingreso of this.registro.detalleIngresos) {
  //           const fila = this.fb.group({
  //             tipo: [{ value: ingreso.tipo as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],
  //             nombre: [{ value: ingreso.nombre, disabled: this.estadoVisualizar}, Validators.required],
  //             cantidad: [{ value: ingreso.cantidad, disabled: this.estadoVisualizar}, [Validators.required, Validators.max(999999), Validators.min(0)]],    
  //             estado: [{ value: ingreso.estado as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],   
  //             observacion: [{ value: ingreso.observacion, disabled: this.estadoVisualizar}]
  //           });            
        
  //           this.filasIngreso.controls.push(fila);
  //         }
  //         this.actualizarTablaIngreso();

  //         for (let ingreso of this.registro.detalleSalidaIngresos) {
  //           const fila = this.fb.group({
  //             tipo: [{ value: ingreso.tipo as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],
  //             nombre: [{ value: ingreso.nombre, disabled: this.estadoVisualizar}, Validators.required],
  //             cantidad: [{ value: ingreso.cantidad, disabled: this.estadoVisualizar}, [Validators.required, Validators.max(999999), Validators.min(0)]],    
  //             estado: [{ value: ingreso.estado as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],   
  //             observacion: [{ value: ingreso.observacion, disabled: this.estadoVisualizar}]
  //           });            
        
  //           this.filasSalidaIngreso.controls.push(fila);
  //         }
  //         this.actualizarTablaSalidaIngreso();

  //         this.dataSourceSalidaEgreso = new MatTableDataSource(this.registro.detalleIngresos);
  //         this.comentarioEgresosFormControl.setValue(this.registro.comentarioEgresos);
  //         this.comentarioIngresosFormControl.setValue(this.registro.comentarioIngresos);
  //         this.comentarioSalidaEgresosFormControl.setValue(this.registro.comentarioSalidaEgresos);
  //         this.comentarioSalidaIngresosFormControl.setValue(this.registro.comentarioSalidaIngresos);
  //         this.dataSourceEgreso.paginator = this.paginatorEgreso;
  //         this.dataSourceIngreso.paginator = this.paginatorIngreso;  
  //         this.dataSourceSalidaEgreso.paginator = this.paginatorSalidaEgreso;
  //         this.dataSourceSalidaIngreso.paginator = this.paginatorSalidaIngreso;
          
  //         if (this.mostrarSubidaDocumentos) {
  //           this.obtenerTiposDeDocumentos();
  //           this.obtenerDocumentos();
  //         }
  //       });
  //     } else {
  //       this.registro = new PertenenciaDTO();
  //       this.registro.detalleEgresos = [];
  //       this.registro.detalleIngresos = [];
  //       this.registro.detalleSalidaIngresos = [];
  //       this.dataSourceEgreso = new MatTableDataSource(this.registro.detalleEgresos);
  //       this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
  //       this.dataSourceSalidaIngreso = new MatTableDataSource(this.registro.detalleSalidaIngresos);
  //     }
  //   });
  // }

  cargarDatos() : void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerCatalogos().pipe(
      concatMap(() => this.obtenerFichasIngreso()),  
      concatMap(() => this.obtenerParametrosDeConsulta()),  
      concatMap(() => this.obtenerTiposDeDocumentos()),  
      concatMap(() =>
        iif(
          () => this.estadoEditar, 
          this.obtenerPertenenciaPorId().pipe(
            concatMap(() => this.obtenerDocumentos()),  
          ),          
          of(null),
        )
      )

    ).subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });
  }

  obtenerCatalogos() {
    const nemonicosCatalogos = [
      'TIPOS_PERTENENCIAS',  
      'ESTADOS_PERTENENCIAS',
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));    

    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.tipos = results[0]?.data;
        this.estados = results[1]?.data;
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap(params => {
        const numDoc = params['numDoc'];
        if (numDoc) {
          this.idPertenencia = numDoc;
          const state = params['state'];
          if (state) {
            this.estadoVisualizar = true;
          }
          this.estadoEditar = true;
        } else {
          this.registro = new PertenenciaDTO();
          this.registro.detalleEgresos = [];
          this.registro.detalleIngresos = [];
          this.registro.detalleSalidaIngresos = [];
          this.dataSourceEgreso = new MatTableDataSource(this.registro.detalleEgresos);
          this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
          this.dataSourceSalidaIngreso = new MatTableDataSource(this.registro.detalleSalidaIngresos);
        }
      })
    );
  }

  // obtenerCatalogos() {
  //   const tiposRequest = this.catalogoService.obtenerHijos('TIPOS_PERTENENCIAS','');
  //   const estadosRequest = this.catalogoService.obtenerHijos('ESTADOS_PERTENENCIAS','');

  //   forkJoin([tiposRequest, estadosRequest]).subscribe(([tiposResponse, estadosResponse]) => {
  //     this.tipos = tiposResponse.data;
  //     this.estados = estadosResponse.data;      
  //   });
  // }

  // obtenerFichaIngresoValida() {
  //   this.uuid_fp = this.route.snapshot.params['uuid_fp'];
  //   let paginacionRequest = new PaginacionRequest();
  //   paginacionRequest.page = null;
  //   paginacionRequest.size = null;
  //   paginacionRequest.tokenIdentificador = this.uuid_fp;
    
  //   this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, '').subscribe(
  //       {
  //         next: (response: RespuestaPorDefecto<FichaIngresoDTO>) => {
  //           if (!environment.production) {
  //             console.log(response);
  //           }
  
  //           if (!response.exito) {
  //             this.fichaIngresoService.checkError(response);
  //             return;
  //           }
  //           //obtener ficha de ingreso
  //           if (response.data) {
  //             this.ingreso = response.data;   
  //             console.log(this.ingreso);           
  //           }
  //         },
  //         error: (error: any) => {
  //           this.fichaIngresoService.checkError(error);
  //         }
  //       }
  //     );
  // }

  obtenerPertenenciaPorId() {   
    
    return this.pertenenciaService.obtenerPertenenciaPorId(this.idPertenencia, this.nemonicoMenu).pipe(
      tap((response) => {
        this.registro = response.data;
        this.registro.esEdicion = true;

        const ingresoSeleccionado = this.ingresos.find(ingreso => ingreso.tokenIdentificador == this.registro.tokenFichaIngreso);
        if (ingresoSeleccionado) {
          this.ingresoFormControl.setValue(ingresoSeleccionado);          
          let event = {value: ingresoSeleccionado};
          this.seleccionarIngreso(event);
        }

        if (this.registro && this.registro.tokenIdentificador) {
          this.mostrarSubidaDocumentos = true;
        }
        
        for (let egreso of this.registro.detalleEgresos) {
          const fila = this.fb.group({
            tipo: [{ value: egreso.tipo as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],
            nombre: [{ value: egreso.nombre, disabled: this.estadoVisualizar}, Validators.required],
            cantidad: [{ value: egreso.cantidad, disabled: this.estadoVisualizar}, [Validators.required, Validators.max(999999), Validators.min(0)]],    
            estado: [{ value: egreso.estado as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],   
            observacion: [{ value: egreso.observacion, disabled: this.estadoVisualizar}]
          });            
      
          this.filasEgreso.controls.push(fila);
        }
        this.actualizarTablaEgresos();

        for (let ingreso of this.registro.detalleIngresos) {
          const fila = this.fb.group({
            tipo: [{ value: ingreso.tipo as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],
            nombre: [{ value: ingreso.nombre, disabled: this.estadoVisualizar}, Validators.required],
            cantidad: [{ value: ingreso.cantidad, disabled: this.estadoVisualizar}, [Validators.required, Validators.max(999999), Validators.min(0)]],    
            estado: [{ value: ingreso.estado as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],   
            observacion: [{ value: ingreso.observacion, disabled: this.estadoVisualizar}]
          });            
      
          this.filasIngreso.controls.push(fila);
        }
        this.actualizarTablaIngreso();

        for (let ingreso of this.registro.detalleSalidaIngresos) {
          const fila = this.fb.group({
            tipo: [{ value: ingreso.tipo as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],
            nombre: [{ value: ingreso.nombre, disabled: this.estadoVisualizar}, Validators.required],
            cantidad: [{ value: ingreso.cantidad, disabled: this.estadoVisualizar}, [Validators.required, Validators.max(999999), Validators.min(0)]],    
            estado: [{ value: ingreso.estado as CatalogoDTO, disabled: this.estadoVisualizar}, Validators.required],   
            observacion: [{ value: ingreso.observacion, disabled: this.estadoVisualizar}]
          });            
      
          this.filasSalidaIngreso.controls.push(fila);
        }
        this.actualizarTablaSalidaIngreso();

        this.dataSourceSalidaEgreso = new MatTableDataSource(this.registro.detalleIngresos);
        this.comentarioEgresosFormControl.setValue(this.registro.comentarioEgresos);
        this.comentarioIngresosFormControl.setValue(this.registro.comentarioIngresos);
        this.comentarioSalidaEgresosFormControl.setValue(this.registro.comentarioSalidaEgresos);
        this.comentarioSalidaIngresosFormControl.setValue(this.registro.comentarioSalidaIngresos);
        this.dataSourceEgreso.paginator = this.paginatorEgreso;
        this.dataSourceIngreso.paginator = this.paginatorIngreso;  
        this.dataSourceSalidaEgreso.paginator = this.paginatorSalidaEgreso;
        this.dataSourceSalidaIngreso.paginator = this.paginatorSalidaIngreso;

      }),
      catchError(err => {
        this.pertenenciaService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  // obtenerFichaIdentificacion() {
  //   this.uuid_fp = this.route.snapshot.params['uuid_fp'];   
    
  //   this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, '').subscribe(
  //       {
  //         next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
  //           if (!environment.production) {
  //             console.log(response);
  //           }
  
  //           if (!response.exito) {
  //             this.fichaIdentificacionService.checkError(response);
  //             return;
  //           }
  //           //obtener ficha de ingreso
  //           if (response.data) {
  //             this.fichaIdentificacion = response.data;   
  //             console.log(this.fichaIdentificacion);
  //           }
  //         },
  //         error: (error: any) => {
  //           this.fichaIdentificacionService.checkError(error);
  //         }
  //       }
  //     );
  // }

  obtenerFichasIngreso() : Observable<any> {
    const tokenFichaIdentificacion = this.route.snapshot.params['uuid_fp'];
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = 0;
    paginacionRequest.size = 100;
    paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;
    
    return this.fichaIngresoService.obtenerFichasIngresoPaginado(paginacionRequest, this.nemonicoMenu).pipe(
      tap((response) => {
        this.ingresos = response.data.data;
        this.ingresos.sort((a,b) => new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime());

      }),
      catchError(err => {
        this.fichaIngresoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  // obtenerFichasIngreso() {
  //   const tokenFichaIdentificacion = this.route.snapshot.params['uuid_fp'];
  //   let paginacionRequest = new PaginacionRequest();
  //   paginacionRequest.page = 0;
  //   paginacionRequest.size = 100;
  //   paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;
    
  //   this.fichaIngresoService.obtenerFichasIngresoPaginado(paginacionRequest, '').subscribe(
  //     (response) => {
  //       this.ingresos = response.data.data;
  //       console.log(this.ingresos);
  //     })    
  // }

  aniadirFilaEgresos() {
    const fila = this.fb.group({
      tipo: [null as CatalogoDTO, Validators.required],
      nombre: [null, Validators.required],
      cantidad: [null, [Validators.required, Validators.max(999999), Validators.min(0)]],    
      estado: [null as CatalogoDTO, Validators.required],   
      observacion: [null]
    });

    this.filasEgreso.controls.unshift(fila);
    this.filasEgreso.markAllAsTouched();
    this.actualizarTablaEgresos();
    // const dialogRef = this.dialog.open(ModalEditaPertenenciaComponent, {
    //   width: '600px'
    // }); 

    // dialogRef.afterClosed().subscribe(async (result) => {
    //   if (result) {
    //     this.registro.detalleEgresos.unshift(result);
    //     this.dataSourceEgreso = new MatTableDataSource(this.registro.detalleEgresos);
    //     this.dataSourceEgreso.paginator = this.paginatorEgreso;
    //   }
    // });
  }

  obtenerControlIngreso(index: number, campo: string) {
    return (this.filasIngreso.at(index) as FormGroup).get(campo);
  }

  actualizarTablaIngreso() {
    this.dataSourceIngreso.data = this.filasIngreso.controls;
    this.dataSourceIngreso.paginator = this.paginatorIngreso;
  }

  obtenerControlSalidaIngreso(index: number, campo: string) {
    return (this.filasSalidaIngreso.at(index) as FormGroup).get(campo);
  }

  actualizarTablaSalidaIngreso() {
    this.dataSourceSalidaIngreso.data = this.filasSalidaIngreso.controls;
    this.dataSourceSalidaIngreso.paginator = this.paginatorSalidaIngreso;
  }

  obtenerControlEgreso(index: number, campo: string) {
    return (this.filasEgreso.at(index) as FormGroup).get(campo);
  }

  actualizarTablaEgresos() {
    this.dataSourceEgreso.data = this.filasEgreso.controls;
    this.dataSourceEgreso.paginator = this.paginatorEgreso;
  }

  editarFilaEgresos(detalle: PertenenciaDetalleDTO, index: number) {
    // const dialogRef = this.dialog.open(ModalEditaPertenenciaComponent, {
    //   data: detalle,
    //   width: '600px'
    // }); 

    // dialogRef.afterClosed().subscribe(async (result) => {
    //   if (result) {
    //     this.registro.detalleEgresos[index] = result;
    //     this.dataSourceEgreso = new MatTableDataSource(this.registro.detalleEgresos);
    //     this.dataSourceEgreso.paginator = this.paginatorEgreso;
    //   }
    // });
  }

  eliminarItemEgresos(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará la pertenencia seleccionada de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.filasEgreso.removeAt(index);
            this.actualizarTablaEgresos();
              // this.registro.detalleEgresos.splice(index, 1);
              // this.dataSourceEgreso = new MatTableDataSource(this.registro.detalleEgresos);
              // this.dataSourceEgreso.paginator = this.paginatorEgreso;
          }
        }
      }
    );
  }

  // eliminarItemEgresos(index: number) {
  //   this.registro.detalleEgresos.splice(index, 1);
  //   this.dataSourceEgreso = new MatTableDataSource(this.registro.detalleEgresos);
  //   this.dataSourceEgreso.paginator = this.paginatorEgreso;
  // }

  aniadirFilaIngresos() {
    const fila = this.fb.group({
      tipo: [null as CatalogoDTO, Validators.required],
      nombre: [null, Validators.required],
      cantidad: [null, [Validators.required, Validators.max(999999), Validators.min(0)]],    
      estado: [null as CatalogoDTO, Validators.required],   
      observacion: [null]
    });

    this.filasIngreso.controls.unshift(fila);
    this.filasIngreso.markAllAsTouched();
    this.actualizarTablaIngreso();


    // const dialogRef = this.dialog.open(ModalEditaPertenenciaComponent, {
    //   width: '600px'
    // }); 

    // dialogRef.afterClosed().subscribe(async (result) => {
    //   if (result) {
    //     this.registro.detalleIngresos.unshift(result);
    //     this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
    //     this.dataSourceIngreso.paginator = this.paginatorIngreso;
    //   }
    // });
  }

  editarFilaIngresos(detalle: PertenenciaDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaPertenenciaComponent, {
      data: detalle,
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        this.registro.detalleIngresos[index] = result;
        this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
        this.dataSourceIngreso.paginator = this.paginatorIngreso;
      }
    });
  }

  // eliminarItemIngresos(index: number) {
  //   this.registro.detalleIngresos.splice(index, 1);
  //   this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
  //   this.dataSourceIngreso.paginator = this.paginatorIngreso;
  // }

  eliminarItemIngresos(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará la pertenencia seleccionada de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.filasIngreso.removeAt(index);
            this.actualizarTablaIngreso();
            // this.registro.detalleIngresos.splice(index, 1);
            // this.dataSourceIngreso = new MatTableDataSource(this.registro.detalleIngresos);
            // this.dataSourceIngreso.paginator = this.paginatorIngreso;
          }
        }
      }
    );
  }

  aniadirFilaSalidaIngresos() {
    const fila = this.fb.group({
      tipo: [null as CatalogoDTO, Validators.required],
      nombre: [null, Validators.required],
      cantidad: [null, [Validators.required, Validators.max(999999), Validators.min(0)]],    
      estado: [null as CatalogoDTO, Validators.required],   
      observacion: [null]
    });

    this.filasSalidaIngreso.controls.unshift(fila);
    this.filasSalidaIngreso.markAllAsTouched();
    this.actualizarTablaSalidaIngreso();   
  }

  editarFilaSalidaIngresos(detalle: PertenenciaDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaPertenenciaComponent, {
      data: detalle,
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        this.registro.detalleSalidaIngresos[index] = result;
        this.dataSourceSalidaIngreso = new MatTableDataSource(this.registro.detalleSalidaIngresos);
        this.dataSourceSalidaIngreso.paginator = this.paginatorSalidaIngreso;
      }
    });
  }  

  eliminarItemSalidaIngresos(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará la pertenencia seleccionada de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.filasSalidaIngreso.removeAt(index);
            this.actualizarTablaSalidaIngreso();           
          }
        }
      }
    );
  }

  prevenirInputNumberInvalido(event: KeyboardEvent): void {
    const invalidKeys = ['+', '-', 'e', 'E'];
    if (invalidKeys.includes(event.key)) {
      event.preventDefault();
    }
  }

  obtenerTipoCatalogoPorNemonico(nemonico: string) {
    return this.tipos.find(tipo => tipo.nemonico == nemonico);
  } 

  obtenerEstadoCatalogoPorNemonico(nemonico: string) {
    return this.estados.find(estado => estado.nemonico == nemonico);
  } 

  compararTipos(o1: any, o2: any): boolean {
    return o1 && o2 ? o1.nemonico === o2.nemonico : o1 === o2;
  }

  esFormularioInvalido(): boolean {
    const filasInvalidas = this.filasIngreso.controls.some((row) => row.invalid) 
                        || this.filasEgreso.controls.some((row) => row.invalid) 
                        || this.dataSourceEgreso.data.length < 1 
                        || this.dataSourceIngreso.data.length < 1;
    return filasInvalidas;
  }

  guardar() {   
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {

            this.registro.detalleEgresos = [];
            this.dataSourceEgreso.data.map(item => this.registro.detalleEgresos.push(item.value));
            this.registro.detalleIngresos = [];
            this.dataSourceIngreso.data.map(item => this.registro.detalleIngresos.push(item.value));
            this.registro.detalleSalidaIngresos = [];
            this.dataSourceSalidaIngreso.data.map(item => this.registro.detalleSalidaIngresos.push(item.value));
            this.registro.comentarioIngresos = this.comentarioIngresosFormControl.value;
            this.registro.comentarioEgresos = this.comentarioEgresosFormControl.value;
            this.registro.comentarioSalidaIngresos = this.comentarioSalidaIngresosFormControl.value;
            this.registro.comentarioSalidaEgresos = this.comentarioSalidaEgresosFormControl.value;
            this.registro.tokenFichaIdentificacion = this.uuid_fp;
            this.registro.tokenFichaIngreso = (this.ingresoFormControl.value)?.tokenIdentificador;

            console.log(this.registro);
            this.pertenenciaService.crearPertenencia(this.registro, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<PertenenciaDTO>) => {
                  
                  if (!response.exito) {
                    this.pertenenciaService.checkError(response);
        
                    return;
                  }                  
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                  this.fichaIdentificacionService.actualizacionFicha(true);
                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/entregaRecepcionUniformesArticulos/${this.uuid_fp}`])
                },
                error: (error: any) => {
                  this.pertenenciaService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }

  guardarSinSalir() {      
    this.registro.detalleEgresos = [];
    this.dataSourceEgreso.data.map(item => this.registro.detalleEgresos.push(item.value));
    this.registro.detalleIngresos = [];
    this.dataSourceIngreso.data.map(item => this.registro.detalleIngresos.push(item.value));
    this.registro.detalleSalidaIngresos = [];
    this.dataSourceSalidaIngreso.data.map(item => this.registro.detalleSalidaIngresos.push(item.value));
    this.registro.comentarioIngresos = this.comentarioIngresosFormControl.value;
    this.registro.comentarioEgresos = this.comentarioEgresosFormControl.value;
    this.registro.comentarioSalidaIngresos = this.comentarioSalidaIngresosFormControl.value;
    this.registro.comentarioSalidaEgresos = this.comentarioSalidaEgresosFormControl.value;
    this.registro.tokenFichaIdentificacion = this.uuid_fp;
    this.registro.tokenFichaIngreso = (this.ingresoFormControl.value)?.tokenIdentificador;

    this.pertenenciaService.crearPertenencia(this.registro, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PertenenciaDTO>) => {
          
          if (!response.exito) {
            this.pertenenciaService.checkError(response);

            return;
          }                  
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.registro = response.data;
          this.ngOnInit();

        },
        error: (error: any) => {
          this.pertenenciaService.checkError(error);
        }
      }
    )    
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    this.obtenerDocumentos().subscribe();
  }

  eliminacionDocumento(documentoDTO: DocumentoDTO) {    
    let load = this.dialogMensajeService.mensajeLoading("Quitando el documento: " + documentoDTO.nombre + " del detalle..");
    let pertenenciaDocumentoDTO = new PertenenciaDocumentoDTO();
    pertenenciaDocumentoDTO.documentoDTO = documentoDTO;
    pertenenciaDocumentoDTO.tokenIdentificadorPertenencia = this.registro.tokenIdentificador;
    this.pertenenciaService.eliminarDocumento(
      pertenenciaDocumentoDTO,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (respone: RespuestaPorDefecto<PertenenciaDocumentoDTO>) => {
          load.close();
          if (!respone.exito) {
            this.pertenenciaService.checkError(respone);
          }

          this.obtenerDocumentos();
        },
        error: (error: any) => {
          load.close();
          this.pertenenciaService.checkError(error);
        }
      }
    );
  }

  edicionEvent(exito: boolean) {
    if (exito) {
      this.obtenerDocumentos();
    }
  }

  // obtenerDocumentos() {
  //   let page = this.tablaDocumentos.page;
  //   let pageSize = this.tablaDocumentos.pageSize;

  //   let pertenenciaDocumentosRequest = new PertenenciaDocumentosRequest();
  //   pertenenciaDocumentosRequest.page = page;
  //   pertenenciaDocumentosRequest.size = pageSize;
  //   pertenenciaDocumentosRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
  //   pertenenciaDocumentosRequest.tokenIdentificadorPertenencia = this.registro.tokenIdentificador;

  //   this.pertenenciaService.obtenerDocumentos(
  //     pertenenciaDocumentosRequest,
  //     ''
  //   ).subscribe(
  //     {
  //       next: (response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {

  //         if (!environment.production) {
  //           console.log(response);
  //         }

  //         if (!response.exito) {
  //           this.pertenenciaService.checkError(response);
  //         }

  //         if (response.data?.data) {
  //           this.tablaDocumentos.actualizarTabla(
  //             response.data.data,
  //             response.data.totalItems
  //           );
  //         }

  //       },
  //       error: (error: any) => {
  //         this.pertenenciaService.checkError(error);
  //       }
  //     }
  //   );
  // }

  private obtenerDocumentos(): Observable<void> {    
    // let page = this.tablaDocumentos.page;
    // let pageSize = this.tablaDocumentos.pageSize;
  
    let pertenenciaDocumentosRequest = new PertenenciaDocumentosRequest();
    pertenenciaDocumentosRequest.page = this.tablaDocumentos.page;
    pertenenciaDocumentosRequest.size = this.tablaDocumentos.pageSize;
    pertenenciaDocumentosRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
    pertenenciaDocumentosRequest.tokenIdentificadorPertenencia = this.registro.tokenIdentificador;
  
    return this.pertenenciaService
      .obtenerDocumentos(pertenenciaDocumentosRequest, this.nemonicoMenu)
      .pipe(
        tap((response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }
  
          if (!response.exito) {
            this.pertenenciaService.checkError(response);
            throw new Error(response.mensaje); // Lanza error para interrumpir el flujo
          }
  
          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        }),
        catchError(error => {
          this.pertenenciaService.checkError(error);
          return throwError(() => error); // Propaga el error
        }),
        map(() => void 0) // Devuelve void para indicar que no se necesita un valor de retorno
      );
  }

  subirArchivosEvent(documentos: DocumentoSubido[]) {
      if (documentos && documentos.length > 0) {
          
        for (let documentoSubido of documentos) {
          let pertenenciaDocumentoDTO = new PertenenciaDocumentoDTO();
          pertenenciaDocumentoDTO.tokenIdentificadorPertenencia = this.registro.tokenIdentificador;
          pertenenciaDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;
  
          let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
            documentoSubido.documento.name
          );
          this.pertenenciaService.subirDocumento(
            documentoSubido.documento,
            pertenenciaDocumentoDTO,
            this.nemonicoMenu
          ).subscribe(
            {
              next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
  
                load.close();
                if (!response.exito) {
                  this.pertenenciaService.checkError(response);
                  return;
                }
  
                //Refrescar la tabla de documentos
                this.obtenerDocumentos();
              },
              error: (error: any) => {
                load.close();
                this.pertenenciaService.checkError(error);
              }
            }
          );
        }
      } else {
        this.dialogMensajeService.mensajeError("No se obtuvieron documentos para ser subidos");
      }
    }
  
  // private obtenerTiposDeDocumentos() {
  //   this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(etiquetasModel.SECCION_FICHA_IDENT_ENTREGA_RETIRO_DE_PERTENENCIAS,
  //     '').subscribe(
  //       {
  //         next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
  //           if (!environment.production) {
  //             console.log(response);
  //           }

  //           if (!response.exito) {
  //             this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
  //             return;
  //           }
  //           let tiposArchivos = response.data;

  //           if (tiposArchivos.length == 0) {
  //             this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
  //             return;
  //           }

  //           this.tiposDeDocumentosSistema =
  //             tiposArchivos.map(
  //               (tipoArch) => {
  //                 let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
  //                 let tipoDeDocumento = new TipoDeDocumento();
  //                 tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
  //                 tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
  //                 tipoDeDocumento.requerido = tipoArch.requerido;
  //                 tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
  //                 tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

  //                 return tipoDeDocumento;
  //               }
  //             );
  //         },
  //         error: (error: any) => {
  //           this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
  //         }
  //       }
  //     );
  // }

  private obtenerTiposDeDocumentos(): Observable<TipoDeDocumento[]> {
    return this.tipoDeIdentificacionTipoDeDocumentoService
      .obtenerTiposDeDocumentos(
        etiquetasModel.SECCION_FICHA_IDENT_ENTREGA_RETIRO_DE_PERTENENCIAS, this.nemonicoMenu
      )
      .pipe(
        tap((response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }
  
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            throw new Error(response.mensaje); // Lanza un error para que el observable maneje la interrupción
          }
  
          if (response.data.length === 0) {
            this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
            throw new Error("Tipos de documentos no configurados"); 
          }
  
          this.tiposDeDocumentosSistema = response.data.map(tipoArch => {
            let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
            let tipoDeDocumento = new TipoDeDocumento();
            tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
            tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
            tipoDeDocumento.requerido = tipoArch.requerido;
            tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
            tipoDeDocumento.nombre = catalogoTipoDoc.nombre;
  
            return tipoDeDocumento;
          });
        }),
        catchError(error => {
          this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          return throwError(() => error); // Propaga el error al flujo de observables
        }),
        map(() => this.tiposDeDocumentosSistema) // Retorna la lista de tipos de documentos
      );
  }  

  generarPDF() {    
    this.registro.detalleEgresos = [];
    this.dataSourceEgreso.data.map(item => this.registro.detalleEgresos.push(item.value));
    this.registro.detalleIngresos = [];
    this.dataSourceIngreso.data.map(item => this.registro.detalleIngresos.push(item.value));
    this.registro.detalleSalidaIngresos = [];
    this.dataSourceSalidaIngreso.data.map(item => this.registro.detalleSalidaIngresos.push(item.value));
    this.registro.comentarioIngresos = this.comentarioIngresosFormControl.value;
    this.registro.comentarioEgresos = this.comentarioEgresosFormControl.value;
    this.registro.comentarioSalidaIngresos = this.comentarioSalidaIngresosFormControl.value;
    this.registro.comentarioSalidaEgresos = this.comentarioSalidaEgresosFormControl.value;

    this.loadImageAsBase64();

    let numero = 1;
    if (this.selectedIndex == 0) {
      let elementosEgresoEnviar: any[] = [];
      for (let egreso of this.registro.detalleEgresos) {
        let elemento = {
          No: (numero++).toString(),
          Nombre: egreso.nombre,
          Tipo: egreso.tipo.nombre,
          Estado: egreso.estado.nombre,
          Cantidad: egreso.cantidad.toString()
        }
        elementosEgresoEnviar.push(elemento);
      }

      let tablaEgreso = new TablaPlantilla();
      tablaEgreso.encabezados = ['No', 'Nombre', 'Tipo', 'Estado', 'Cantidad'];
      tablaEgreso.filas = elementosEgresoEnviar;

      let elementosIngresoEnviar: any[] = [];
      numero = 1;
      for (let ingreso of this.registro.detalleIngresos) {
        let elemento = {
          No: (numero++).toString(),
          Nombre: ingreso.nombre,
          Tipo: ingreso.tipo.nombre,
          Estado: ingreso.estado.nombre,
          Cantidad: ingreso.cantidad.toString()
        }
        elementosIngresoEnviar.push(elemento);
      }

      let tablaIngreso = new TablaPlantilla();
      tablaIngreso.encabezados = ['No', 'Nombre', 'Tipo', 'Estado', 'Cantidad'];
      tablaIngreso.filas = elementosIngresoEnviar;

      console.log(this.registro);
      this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(this.registro.idFichaIdentificacion, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
            if (!response.exito) {
              return;
            }

            const fichaIdentificacion: FichaIdentificacionDTO = response.data;
            console.log(fichaIdentificacion);
            const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
            const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
            const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


            let request = new GeneracionPdfRequest();
            request.nemonico = etiquetasModel.FORMULARIO_PERTENENCIAS_INGRESO;
            request.variables = {
              "[IMG_BASE64]": this.base64Image,
              "[TITULO-PLANTILLA]": 'Acta de documentos y artículos personales',
              "[TITULO-INFORME]": 'Anexo 14 - Acta de documentos y artículos personales - Ingreso al Centro',
              "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
              "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
              "[CENTRO]": fichaIdentificacion.centroIngreso,
              "[ADOLESCENTE]": nombreAdolescente,
              "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
              "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
              "[TABLA-RETIRO-PERT]": JSON.stringify(tablaIngreso),
              "[COMENTARIO-RETIRO]": this.registro?.comentarioIngresos,
              "[TABLA-ENTREGA-PERT]": JSON.stringify(tablaEgreso),
              "[COMENTARIO-ENTREGA]": this.registro?.comentarioEgresos
            }
            this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<string>) => {

                if (!response.exito) {
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                  );
                  return;
                }

                console.log(response);

                const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

                const pwa = window.open(url);

              },
              error: (error: any) => {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                );
              }
            });
            
          },
          error: (error: any) => {
            this.fichaIdentificacionService.checkError(error);
          }
        }
      );
    } else if (this.selectedIndex == 1) {     

      let elementosIngresoEnviar: any[] = [];
      numero = 1;
      for (let ingreso of this.registro.detalleIngresos) {
        let elemento = {
          No: (numero++).toString(),
          Nombre: ingreso.nombre,
          Tipo: ingreso.tipo.nombre,
          Estado: ingreso.estado.nombre,
          Cantidad: ingreso.cantidad.toString()
        }
        elementosIngresoEnviar.push(elemento);
      }

      let tablaIngreso = new TablaPlantilla();
      tablaIngreso.encabezados = ['No', 'Nombre', 'Tipo', 'Estado', 'Cantidad'];
      tablaIngreso.filas = elementosIngresoEnviar;

      let elementosEgresoEnviar: any[] = [];
      numero = 1;
      for (let egreso of this.registro.detalleSalidaIngresos) {
        let elemento = {
          No: (numero++).toString(),
          Nombre: egreso.nombre,
          Tipo: egreso.tipo.nombre,
          Estado: egreso.estado.nombre,
          Cantidad: egreso.cantidad.toString()
        }
        elementosEgresoEnviar.push(elemento);
      }

      let tablaEgreso = new TablaPlantilla();
      tablaEgreso.encabezados = ['No', 'Nombre', 'Tipo', 'Estado', 'Cantidad'];
      tablaEgreso.filas = elementosEgresoEnviar;

      console.log(this.registro);
      this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(this.registro.idFichaIdentificacion, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
            if (!response.exito) {
              return;
            }

            const fichaIdentificacion: FichaIdentificacionDTO = response.data;
            console.log(fichaIdentificacion);
            const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
            const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
            const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


            let request = new GeneracionPdfRequest();
            request.nemonico = etiquetasModel.FORMULARIO_PERTENENCIAS_SALIDA;
            request.variables = {
              "[IMG_BASE64]": this.base64Image,
              "[TITULO-PLANTILLA]": 'Acta de documentos y artículos personales',
              "[TITULO-INFORME]": 'Anexo 14 - Acta de documentos y artículos personales - Salida del Centro',
              "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
              "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
              "[CENTRO]": fichaIdentificacion.centroIngreso,
              "[ADOLESCENTE]": nombreAdolescente,
              "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
              "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
              "[TABLA-SALIDA-PERT]": JSON.stringify(tablaIngreso),
              "[COMENTARIO-RETIRO]": this.registro?.comentarioSalidaEgresos,
              "[TABLA-ENTREGA-PERT]": JSON.stringify(tablaEgreso),
              "[COMENTARIO-ENTREGA]": this.registro?.comentarioSalidaIngresos
            }
            this.pdfService.generarPdf(request, '').subscribe({
              next: (response: RespuestaPorDefecto<string>) => {

                if (!response.exito) {
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                  );
                  return;
                }

                console.log(response);

                const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

                const pwa = window.open(url);

              },
              error: (error: any) => {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                );
              }
            });
            
          },
          error: (error: any) => {
            this.fichaIdentificacionService.checkError(error);
          }
        }
      );
    }
      
    
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

  seleccionarIngreso(event: any) {
    this.ingresoFormControl.disable();  
    
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatHora(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }

  cambiarTab(event: MatTabChangeEvent) {
    this.selectedIndex = event.index;
  }

} 
