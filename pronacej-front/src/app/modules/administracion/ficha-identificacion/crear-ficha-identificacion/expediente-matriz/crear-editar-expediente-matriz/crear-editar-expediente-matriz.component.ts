import { Component, Inject, LOCALE_ID, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { ExpedienteMatrizDetalleDTO, ExpedienteMatrizDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { catchError, concatMap, finalize, forkJoin, iif, Observable, of, tap, throwError } from 'rxjs';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MatDividerModule } from '@angular/material/divider';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import etiquetasModel from 'app/core/etiquetas.model';
import { CrearEditarRegistroLegalComponent } from './crear-editar-registro-legal/crear-editar-registro-legal.component';
import { MatCardModule } from '@angular/material/card';
import { concat } from 'lodash';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';

@Component({
  selector: 'app-crear-editar-expediente-matriz',
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
    MatPaginatorModule,
    MatCardModule,
    RouterLink,
    MatDividerModule,
    CrearEditarRegistroLegalComponent,
  ],
  providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
      ],
  templateUrl: './crear-editar-expediente-matriz.component.html',
  styleUrl: './crear-editar-expediente-matriz.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class CrearEditarExpedienteMatrizComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EXPEDIENTE_LEGAL;
  private _paginadorMandato: MatPaginator | null = null;
  @ViewChild('paginadorMandato')
  set paginadorMandato(paginator: MatPaginator | undefined) {
    this._paginadorMandato = paginator ?? null;
    this.asignarPaginador();
  }

  mostrarCrearMandato: boolean = false;

  mostrarTablaRegistros: boolean = false;
  tituloTablaRegistros: string = '';
  nombreBotonTablaRegistros: string = '';

  mandatoSeleccionado: any = null;
  indexSeleccionado: number = null;

  detallesEliminados: ExpedienteMatrizDetalleDTO[] = [];

  estadoEditar: boolean = false;  
  estadoVisualizar: boolean = false;
  mostrarImporte: boolean = false;
  modoVisualizarMandato: boolean = false;

  estados: CatalogoDTO[];
  tiposInfraccion: CatalogoDTO[];
  situacionesJuridicas: CatalogoDTO[];
  situacionesLegales: CatalogoDTO[];
  tiposMedidas: CatalogoDTO[];
  frecuenciasIngreso: CatalogoDTO[];
  delitosGenericos: CatalogoDTO[];
  delitosEspecificos: CatalogoDTO[];

  tiposMandato: CatalogoDTO[];
  situacionesJuridicas2: CatalogoDTO[];

  situacionesLegales2: CatalogoDTO[];


  variacionesMedida: CatalogoDTO[];
  motivosExternacion: CatalogoDTO[];
  motivosInternacion: CatalogoDTO[];
  cortesSuperioresJusticia: CatalogoDTO[];
  instancias: CatalogoDTO[];
  especialidades: CatalogoDTO[];
  medidasAccesorias: CatalogoDTO[];

  tipoCentro: string;  
  tiempoLabel: string = '';

  expediente: ExpedienteMatrizDTO = new ExpedienteMatrizDTO;

  ingreso: FichaIngresoDTO = new FichaIngresoDTO;
  ingresos: FichaIngresoDTO[] = [];

  uuid_fp: string;
  fichaIdentifacion: FichaIdentificacionDTO

  ingresoGeneralFormGroup = this.fb.group({
    ingreso: [null as FichaIngresoDTO, Validators.required],
    motivoIngreso: [null],
    numExpediente: [{value: null, disabled: true}],
    fechaRegistro: [{value: null as Date, disabled: true}],
    estado: [null as CatalogoDTO],
    numOficio: [null],
    fechaOficio: [null as Date],    
    observacion: [null],
    numExpedienteJudicial: [null],
  })

  // keyLabelsTable: any = {    
  //   acciones: "",
  //   fechaResolucion: "Fecha",
  //   numResolucion: "Resolución #",
  //   tipoRegistro: "Tipo",
  //   situacionJuridica: "Situación",
  // };  

  keyLabelsTable: any = {    
    acciones: "Acciones",
    fechaResolucion: "Fecha",
    numResolucion: "Resolución #",
    tipoRegistro: "Tipo",
    situacionJuridica: "Situación",
    tipoVariacion: "Variación",
    fechaInicioMedida: "Fecha de inicio",
    fechaFinMedida: "Fecha de fin",
    corteJusticia: "Corte",
    instancia: "Instancia",
    especialidad: "Especialidad",
    organoJurisdiccional: "Órgano",
    montoReparacion: "Monto de reparación civil",
  };  
  
  dataSource = new MatTableDataSource<any>;
  base64Image: string | null = null;


  constructor(
    private dateAdapter: DateAdapter<any>,
    private dialogMensajeService: DialogMensajeService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private expedienteMatrizService: ExpedienteMatrizService,
    private fichaIngresoService: FichaIngresoService,
    private catalogoService: CatalogoService,
    public funcionesUtils: FuncionesUtils,
    @Inject(LOCALE_ID) private locale: string,
    private fichaIdentificacionService: FichaIdentificacionService,
    private http: HttpClient,   
    private pdfService: PdfService,
  ) {
    this.dateAdapter.setLocale('es-ES');
  }
  
  ngOnInit(): void {
    this.cargarDatos(); 
  }

  cargarDatos(): void {
    this.ingresoGeneralFormGroup.markAllAsTouched();
  
    this.uuid_fp = this.route.snapshot.params['uuid_fp']; // Obtener token de Ficha de Identificación
  
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');
  
    this.obtenerFichasIngreso().pipe(
     // concatMap(() => this.obtenerCatalogos()),
     concatMap(() => this.obtenerParametrosDeConsulta()),
     concatMap(() =>
       forkJoin([
         this.obtenerFichaIdentificacion(), 
         iif(
           () => (this.estadoEditar || this.estadoVisualizar),
           this.obtenerExpediente(),
           this.obtenerFichaIngresoValida(this.uuid_fp)
         )
       ])
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

  obtenerFichaIdentificacion(): Observable<any> {
    return this.fichaIdentificacionService
      .obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
      .pipe(
        tap((response) => {
          console.log("Ficha de Identificación cargada:", response.data);
          this.fichaIdentifacion = response.data
        }),
        catchError((error) => {
          console.error("Error al obtener ficha de identificación:", error);
          return of(null); 
        })
      );
  }
  
  

  obtenerCatalogos() : Observable<any> {
      const nemonicosCatalogos = [
        'TIPOS_REGISTROS_EXPEDIENTE', 
        'SANCIONES_IMPUESTAS',
        'SITUACIONES_LEGALES',
        'TIPOS_MEDIDA_SOCIOEDUCATIVA', 
        'TIPOS_FRENCUENCIA_INGRESO',
        'LISTADO_DELITOS_EXPEDIENTE',
        'VARIACION_MEDIDA_EXPEDIENTE',
        'MOTIVO_EXTERNACION_EXPEDIENTE',    
        'MOTIVO_INTERNACION_EXPEDIENTE',      
        'CORTE_SUPERIOR_JUSTICIA',
        'INSTANCIA_EXPEDIENTE',
        'ESPECIALIDAD_EXPEDIENTE',
        'ESTADOS_EXPEDIENTES_LEGALES',
        'TIPOS_MEDIDAS_ACCESORIAS',
      ];
  
      const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, ''));
      
      return forkJoin(solicitudes).pipe(
        tap((results: any[]) => {
          this.tiposMandato = results[0]?.data;
          this.situacionesJuridicas = results[1]?.data;
          this.situacionesJuridicas2 = results[1]?.data;
          this.situacionesLegales = results[2]?.data;  
          this.situacionesLegales2 = results[2]?.data;
          this.tiposMedidas = results[3]?.data;
          this.frecuenciasIngreso = results[4]?.data;
          this.delitosGenericos = results[5]?.data;
          this.variacionesMedida = results[6]?.data;
          this.motivosExternacion = results[7]?.data;
          this.motivosInternacion = results[8]?.data;
          this.cortesSuperioresJusticia = results[9]?.data;
          this.instancias = results[10]?.data;
          this.especialidades = results[11]?.data;
          this.estados = results[12]?.data;
          this.medidasAccesorias = results[13]?.data;
  
          console.log("Se han obtenido todos los catálogos")
          
  
        }),
        catchError(err => {
          this.catalogoService.checkError(err);
          return throwError(() => err); 
        })
      );
    }

  obtenerParametrosDeConsulta(): Observable<any> {
    return new Observable((observer) => {
      this.route.queryParams.subscribe((params) => {
        const numDoc = params['numDoc'];
        if (numDoc) {
          const state = params['state'];
          if (state && state === 'show') {
            this.estadoVisualizar = true;               
          } else {
            this.estadoEditar = true;        
          }
        }
        observer.next();
        observer.complete();
      });
    });
  }

  obtenerExpediente(): Observable<any> {
    return this.expedienteMatrizService.obtenerExpedientePorNum(this.route.snapshot.queryParams['numDoc'], '').pipe(
      tap((item) => {
        this.expediente = item.data;
        console.log(this.expediente);
        this.tipoCentro = this.expediente.tipoCentro;
        this.expediente.esEdicion = true;                

        this.ingresoGeneralFormGroup.patchValue(this.expediente);        
        this.ingresoGeneralFormGroup.controls['fechaRegistro'].setValue(new Date(this.expediente.fechaCreacion));
        this.ingresoGeneralFormGroup.controls['fechaOficio'].setValue(this.expediente.fechaOficio ? new Date(this.expediente.fechaOficio) : null);

        const ingresoSeleccionado = this.ingresos.find(ingreso => ingreso.tokenIdentificador == this.expediente.tokenFichaIngreso);
        if (ingresoSeleccionado) {
          this.ingresoGeneralFormGroup.controls['ingreso'].setValue(ingresoSeleccionado);          
          let event = {value: ingresoSeleccionado};
          this.seleccionarIngreso(event);
        }

        if (this.expediente.expedienteDetalle.length > 0) {
          this.dataSource = new MatTableDataSource(this.expediente.expedienteDetalle);
          this.asignarPaginador();
        }

        if (this.estadoVisualizar) {
          this.ingresoGeneralFormGroup.disable();          
        }

      }),
      catchError(err => {
        this.expedienteMatrizService.checkError(err);
        return throwError(() => err); 
      })
    );
  }  

  obtenerFichaIngresoValida(tokenFichaIdentificacion: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;
    
    return this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, '').pipe(
      tap((response) => {
        this.ingreso = response.data;
        console.log(this.ingreso);
        
        // if (this.ingreso?.centro.nombre.includes('CJDR')) {
        //   this.tipoCentro = 'CJDR';
        //   this.ingresoGeneralFormGroup.controls['estado'].disable();
        //   this.expediente.estado = this.estados.find(estado => estado?.nemonico.includes('INSCRITO'));
        //   this.ingresoGeneralFormGroup.controls['estado'].setValue(this.expediente.estado);
        // } else if (this.ingreso?.centro.nombre.includes('SOA')) {
        //   this.tipoCentro = 'SOA';
        // }
      }),
      catchError(err => {
        this.fichaIngresoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  obtenerFichasIngreso() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = 0;
    paginacionRequest.size = 100;
    paginacionRequest.tokenIdentificador = this.uuid_fp;
    
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

  guardar() {
    // if (this.ingresoGeneralFormGroup.invalid) {
    //   return;
    // }

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            const loadingDialog = this.dialogMensajeService.mensajeLoading('Guardando información...');
            Object.assign(this.expediente, this.ingresoGeneralFormGroup.value);  
            this.expediente.expedienteDetalle = this.expediente.expedienteDetalle.concat(this.detallesEliminados);  
            this.expediente.tokenFichaIdentificacion = this.uuid_fp;
            this.expediente.tokenFichaIngreso = (this.ingresoGeneralFormGroup.controls['ingreso'].value)?.tokenIdentificador;
            this.expediente.tipoCentro = this.tipoCentro;           
            console.log(this.expediente);
            this.expedienteMatrizService.crearExpediente(this.expediente, this.nemonicoMenu)
            .pipe(
              finalize(() => {
                loadingDialog.close();
              })
            )
            .subscribe(
              {
                next: (response: RespuestaPorDefecto<ExpedienteMatrizDTO>) => {
                  
                  if (!response.exito) {
                    this.expedienteMatrizService.checkError(response);
        
                    return;
                  }                  
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);      
                  if (this.estadoEditar) {
                    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}`]);  
                  } else {
                    this.fichaIdentificacionService.actualizacionFicha(true);
                    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}`]).then(() => {
                      //window.location.reload();
                    });                  
                  }                 
                },
                error: (error: any) => {
                  this.expedienteMatrizService.checkError(error);
                }
              }
            )
           
          }
        }
      }
    );
  }

  guardarSinSalir() {
    // if (this.ingresoGeneralFormGroup.invalid) {
    //   return;
    // }

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            
            Object.assign(this.expediente, this.ingresoGeneralFormGroup.value);  
            this.expediente.expedienteDetalle = this.expediente.expedienteDetalle.concat(this.detallesEliminados);  
            this.expediente.tokenFichaIdentificacion = this.uuid_fp;
            this.expediente.tokenFichaIngreso = (this.ingresoGeneralFormGroup.controls['ingreso'].value)?.tokenIdentificador;
            this.expediente.tipoCentro = this.tipoCentro;           
            console.log(this.expediente);
            this.expedienteMatrizService.crearExpediente(this.expediente, '').subscribe(
              {
                next: (response: RespuestaPorDefecto<ExpedienteMatrizDTO>) => {
                  
                  if (!response.exito) {
                    this.expedienteMatrizService.checkError(response);
        
                    return;
                  }                 
                  this.expediente = response.data;
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);    
                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}/crear-editar`],  {queryParams: {numDoc: response.data.numExpediente}});  
                                 
                },
                error: (error: any) => {
                  this.expedienteMatrizService.checkError(error);
                }
              }
            )
           
          }
        }
      }
    );
  }

  public simboloDecimalValidator(locale: string): ValidatorFn {
    const decimalSeparator = locale === 'es' ? '.' : ',';
    const regex = new RegExp(`^[0-9${decimalSeparator}]*$`);

    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null; // No validar si el campo está vacío

      return regex.test(control.value) ? null : { invalidDecimal: true };
    };       
  }

  validarImporte(event: any): void {
    const decimalSeparator = this.locale === 'es' ? '.' : ',';
    const input = event.target as HTMLInputElement;
    let valor = input.value;

    const patronDecimal = new RegExp(`^[0-9${decimalSeparator}]*$`);
    if (!patronDecimal.test(valor)) {
      valor = valor.replace(new RegExp(`[^0-9${decimalSeparator}]`, 'g'), '');
    }

    if (valor && !isNaN(parseFloat(valor.replace(decimalSeparator, '.')))) {
      const numero = parseFloat(valor.replace(decimalSeparator, '.')).toFixed(6);
      input.value = numero.replace('.', decimalSeparator);
    } else {
      input.value = valor;
    }
  }

  validarNumeros(event: any) {
    let valor = event.target.value;
    valor = valor.trim()
    valor = valor.replace(/[^0-9]/g, '');
    event.target.value = valor;     
  }    

  validarNumerosSentencia(event: any) {
    let valor = event.target.value;
    valor = valor.trim()
    valor = valor.replace(/[^0-9]/g, '');
    if (valor.length > 4) {
      valor = valor.slice(0, 4);
    }

    event.target.value = valor;     
  }

  prevenirInputNumberInvalido(event: KeyboardEvent): void {
    const invalidKeys = ['+', '-', 'e', 'E'];
    if (invalidKeys.includes(event.key)) {
      event.preventDefault();
    }
  }  

  agregarMandato(mandato: any) {
    if (this.mandatoSeleccionado || this.indexSeleccionado) {
      this.expediente.expedienteDetalle[this.indexSeleccionado] = mandato;
      this.mandatoSeleccionado = null;
      this.indexSeleccionado = null;
    } else {
      mandato.removido = false;
      this.expediente.expedienteDetalle.push(mandato);
      // if (this.ingresoGeneralFormGroup.invalid) {
      //   return;
      // }
    }

      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
        "Deseas continuar?"
      );

      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              
              Object.assign(this.expediente, this.ingresoGeneralFormGroup.value);  
              this.expediente.expedienteDetalle = this.expediente.expedienteDetalle.concat(this.detallesEliminados);  
              this.expediente.tokenFichaIdentificacion = this.uuid_fp;
              this.expediente.tokenFichaIngreso = (this.ingresoGeneralFormGroup.controls['ingreso'].value)?.tokenIdentificador;
              this.expediente.tipoCentro = this.tipoCentro;           
              this.expedienteMatrizService.crearExpediente(this.expediente, '').subscribe(
                {
                  next: (response: RespuestaPorDefecto<ExpedienteMatrizDTO>) => {
                    
                    if (!response.exito) {
                      this.expedienteMatrizService.checkError(response);
          
                      return;
                    }                  
                    this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);                                              
                    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}/crear-editar`], { queryParams: {numDoc: response.data.numExpediente}}); 
                    this.ngOnInit();                                 
                  },
                  error: (error: any) => {
                    this.expedienteMatrizService.checkError(error);
                  }
                }
              )
            
            }
          }
        }
      );

    this.mostrarCrearMandato = false;
    this.dataSource = new MatTableDataSource(this.expediente.expedienteDetalle);
    this.asignarPaginador();
  }

  cancelarCreacionMandato() {
    this.mostrarCrearMandato = false;
    this.mandatoSeleccionado = null;
    this.indexSeleccionado = null;
    this.modoVisualizarMandato = false;
  }

  visualizarMandato(mandato: any, index: number) {
    this.mandatoSeleccionado = {...mandato};
    this.indexSeleccionado = index;
    this.mostrarCrearMandato = true;
    this.modoVisualizarMandato = true;
  }

  editarMandato(mandato: any, index: number) {
    this.mandatoSeleccionado = {...mandato};
    this.indexSeleccionado = index;
    this.mostrarCrearMandato = true;
    this.modoVisualizarMandato = false;
  }

  eliminarMandato(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el item seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {            
            let expedienteEliminado = this.expediente.expedienteDetalle.splice(index, 1);
            expedienteEliminado[0].removido = true;
            this.detallesEliminados.push(expedienteEliminado[0]);
            this.dataSource = new MatTableDataSource(this.expediente.expedienteDetalle);
            this.asignarPaginador();
          }
        }
      }
    );
  }  

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  seleccionarIngreso(event: any) {
    this.tipoCentro = event?.value?.centro?.jerarquiaPadre?.nemonico;    
    this.ingresoGeneralFormGroup.controls['ingreso'].disable();   
    if (this.tipoCentro === 'SOA' || this.tipoCentro === 'CJDR') {
      this.mostrarTablaRegistros = true;
      this.tituloTablaRegistros = 'Disposiciones';
      this.nombreBotonTablaRegistros = 'Agregar disposición';
      this.asignarPaginador();
    }
    // if (this.tipoCentro == 'SOA') {
    //   this.tituloTablaRegistros = 'Sentencias';
    //   this.nombreBotonTablaRegistros = 'Agregar sentencia';
    // } else if (this.tipoCentro == 'CJDR') {
    //   this.tituloTablaRegistros = 'Mandatos judiciales';
    //   this.nombreBotonTablaRegistros = 'Agregar mandato';
    // }
    // if (this.tipoCentro == 'SOA') {
    //   this.tituloTablaRegistros = 'Disposiciones';
    //   this.nombreBotonTablaRegistros = 'Agregar disposición';
    // } else if (this.tipoCentro == 'CJDR') {
      // this.tituloTablaRegistros = 'Disposiciones';
      // this.nombreBotonTablaRegistros = 'Agregar disposición';
    // }
  }

  formularioInvalido() {
    const invalido = this.ingresoGeneralFormGroup.invalid;
    return invalido;
  }

  // loadImageAsBase64() {
  //   this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
  //     .subscribe((data: ArrayBuffer) => {
  //       const base64String = this.arrayBufferToBase64(data);
  //       this.base64Image = `data:image/png;base64,${base64String}`;
  //     });
  // }


  loadImageAsBase64(): Observable<string> {
    return new Observable((observer) => {
      this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
        .subscribe({
          next: (data: ArrayBuffer) => {
            const base64String = this.arrayBufferToBase64(data);
            this.base64Image = `data:image/png;base64,${base64String}`;
            observer.next(this.base64Image);
            observer.complete();
          },
          error: (err) => {
            console.error('Error al cargar imagen:', err);
            observer.error(err);
          }
        });
    });
  }

  generarPDF() {
    console.log(this.fichaIdentifacion);
    
    this.loadImageAsBase64().subscribe({
      next: (base64) => {
        let request = new GeneracionPdfRequest();
        request.nemonico = etiquetasModel.FORMULARIO_EXPEDIENTE_LEGAL_SOA;
        console.log(this.expediente);
        let filasTabla: any[] = [];
        let numero = 1;
        for (let detalle of this.expediente.expedienteDetalle) {
          let fila = {
            No: numero.toString(),
            Tipo: detalle.tipoRegistro?.nombre ?? '-',
            Situación: detalle.situacionJuridica?.nombre ?? '-',
            Variación: detalle.tipoVariacion?.nombre ?? '-',
            Resolución: detalle.numResolucion ?? '-',
            Fecha: this.formatFecha(detalle.fechaResolucion.toString()),
            Corte: detalle.corteJusticia?.nombre ?? '-',
            Instancia: detalle.instancia?.nombre ?? '-',
            Especialidad: detalle.especialidad?.nombre ?? '-',
            Órgano: detalle.organoJurisdiccional ?? '-',
            Monto: detalle.montoReparacion ?? '-',
          };
          filasTabla.push(fila);
          numero++;
        }
  
        let tablaExpediente = new TablaPlantilla();
        tablaExpediente.encabezados = ['No', 'Tipo', 'Situación', 'Variación', 'Resolución', 'Fecha', 'Corte', 'Instancia', 'Especialidad', 'Órgano', 'Monto de reparación'];
        tablaExpediente.filas = filasTabla;
        const tituloPantalla = 'Informe expediente legal';
        const fecha = this.formatFecha((new Date()).toString());
        const hora = this.formatHora((new Date()).toString());
        const fechaOficion = new Date(this.expediente.fechaOficio).toLocaleDateString('es-ES') ?? 'Sin información';
        const nombreAdolescente = `${this.fichaIdentifacion.nombres ?? ''} ${this.fichaIdentifacion.apellidoPaterno ?? ''} ${this.fichaIdentifacion.apellidoMaterno ?? ''}`.trim();
        const fechaNacimientoFormateada = this.formatFecha(this.fichaIdentifacion.fechaNacimiento?.toString());


        request.variables = {
          "[IMG-BASE64]": base64, 
          "[TITULO-INFORME]": tituloPantalla,
          "[TITULO-PLANTILLA]": tituloPantalla,
          "[FECHA-REGISTRO]": fecha,
          "[HORA-REGISTRO]": hora,
          "[CENTRO]": this.tipoCentro,
          "[NUM-EXPEDIENTE]": this.expediente.numExpediente ,
          "[NUM-OFICIO]": this.expediente.numOficio,
          "[NUM-EXPJUDICIAL]": this.expediente.numExpedienteJudicial,
          "[MOTIVO-INGRESO]": this.expediente.motivoIngreso ,
          "[OBSERVACION]": this.expediente.observacion ,
          "[FECHA-OFICIO]": fechaOficion,
          "[TABLA-EXPEDIENTE]": JSON.stringify(tablaExpediente),
          "[NOMBRE-ADOLESCENTE]": nombreAdolescente,
          "[FECHA-NACIMIENTO]":fechaNacimientoFormateada,
          "[LUGAR-NACIMIENTO]": this.fichaIdentifacion.lugarNacimiento,
          "[NUMERO-DOCUMENTO]": this.fichaIdentifacion.numeroDocumento,
          "[DIRECCION]": this.fichaIdentifacion.direccion,
          "[GRADO-INSTRUCCION]": this.fichaIdentifacion.modalidadEstudio,
        };
        this.pdfService.generarPdf(request, '').subscribe({
          next: (response: RespuestaPorDefecto<string>) => {
            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
              );
              return;
            }
            const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
            window.open(url);
          },
          error: () => {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
          }
        });
      },
      error: () => {
        this.dialogMensajeService.mensajeError('No se pudo cargar la imagen para el PDF.');
      }
    });
  }
  

  // generarPDFSOA() {
  //   this.loadImageAsBase64().subscribe({
  //     next: (base64) => {
  //       let request = new GeneracionPdfRequest();
  //       request.nemonico = etiquetasModel.FORMULARIO_EXPEDIENTE_LEGAL_SOA;
  //       console.log(this.expediente);
  //       let filasTabla: any[] = [];
  //       let numero = 1;
  //       for (let detalle of this.expediente.expedienteDetalle) {
  //         let fila = {
  //           No: numero.toString(),
  //           Tipo: detalle.tipoRegistro?.nombre ?? '-',
  //           Situación: detalle.situacionJuridica?.nombre ?? '-',
  //           Variación: detalle.tipoVariacion?.nombre ?? '-',
  //           Resolución: detalle.numResolucion ?? '-',
  //           Fecha: this.formatFecha(detalle.fechaResolucion.toString()),
  //           Corte: detalle.corteJusticia?.nombre ?? '-',
  //           Instancia: detalle.instancia?.nombre ?? '-',
  //           Especialidad: detalle.especialidad?.nombre ?? '-',
  //           Órgano: detalle.organoJurisdiccional ?? '-',
  //           Monto: detalle.montoReparacion ?? '-',
  //         };
  //         filasTabla.push(fila);
  //         numero++;
  //       }
  
  //       let tablaExpediente = new TablaPlantilla();
  //       tablaExpediente.encabezados = ['No', 'Tipo', 'Situación', 'Variación', 'Resolución', 'Fecha', 'Corte', 'Instancia', 'Especialidad', 'Órgano', 'Monto de reparación'];
  //       tablaExpediente.filas = filasTabla;
  //       const tituloPantalla = 'Informe expediente legal';
  //       const fecha = this.formatFecha((new Date()).toString());
  //       const hora = this.formatHora((new Date()).toString());
  //       const fechaOficion = new Date(this.expediente.fechaOficio).toLocaleDateString('es-ES') ?? 'Sin información';
  //       request.variables = {
  //         "[IMG-BASE64]": base64, // Imagen cargada
  //         "[TITULO-INFORME]": tituloPantalla,
  //         "[TITULO-PLANTILLA]": tituloPantalla,
  //         "[FECHA-REGISTRO]": fecha,
  //         "[HORA-REGISTRO]": hora,
  //         "[CENTRO]": this.tipoCentro,
  //         "[NUM-EXPEDIENTE]": this.expediente.numExpediente ,
  //         "[NUM-OFICIO]": this.expediente.numOficio,
  //         "[MOTIVO-INGRESO]": this.expediente.motivoIngreso ,
  //         "[OBSERVACION]": this.expediente.observacion ,
  //         "[FECHA-OFICIO]": fechaOficion,
  //         "[TABLA-EXPEDIENTE]": JSON.stringify(tablaExpediente)
  //       };
  //       this.pdfService.generarPdf(request, '').subscribe({
  //         next: (response: RespuestaPorDefecto<string>) => {
  //           if (!response.exito) {
  //             this.dialogMensajeService.mensajeError(
  //               'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
  //             );
  //             return;
  //           }
  //           const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
  //           window.open(url);
  //         },
  //         error: () => {
  //           this.dialogMensajeService.mensajeError(
  //             'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
  //           );
  //         }
  //       });
  //     },
  //     error: () => {
  //       this.dialogMensajeService.mensajeError('No se pudo cargar la imagen para el PDF.');
  //     }
  //   });
  // }
  

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
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

  private asignarPaginador(): void {
    if (!this._paginadorMandato || !this.dataSource) {
      return;
    }

    this.dataSource.paginator = this._paginadorMandato;
  }


  validarExpedienteJudicial(event: Event): void {
  const input = event.target as HTMLInputElement;
  const valorOriginal = input.value;
  // Remueve los caracteres no permitidos
  const valorFiltrado = valorOriginal.replace(/[\/$]/g, '');
  if (valorOriginal !== valorFiltrado) {
    input.value = valorFiltrado;
    this.ingresoGeneralFormGroup.get('numExpedienteJudicial')?.setValue(valorFiltrado);
  }
}
}
