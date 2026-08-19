import { CommonModule, ViewportScroller } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule, MatSelectionListChange } from '@angular/material/list';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { ExpedienteMatrizDelitoDTO, ExpedienteMatrizDetalleDTO, ExpedienteMatrizMedidaDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { ExpedienteMatrizDetalleDocumentoDTO } from 'app/core/model/request/ia/ExpedienteMatrizDetalleDocumentoDTO.model';
import { ExpedienteMatrizDetalleDocumentosRequest } from 'app/core/model/request/ia/ExpedienteMatrizDetalleDocumentosRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import { catchError, forkJoin, Observable, throwError, tap, concatMap, startWith, map, iif } from 'rxjs';

@Component({
  selector: 'app-crear-editar-registro-legal',
  standalone: true,
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatListModule,
    MatDatepickerModule,
    FormsModule,
    ReactiveFormsModule,  
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    SubidaDeDocumentosComponent,
    MatAutocompleteModule,
    CommonModule,
    DocumentosSubidosTablaComponent,
    MatCardModule
  ],
  providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
      ],
  templateUrl: './crear-editar-registro-legal.component.html',
  styleUrl: './crear-editar-registro-legal.component.scss'
})
export class CrearEditarRegistroLegalComponent implements OnInit, OnDestroy, OnChanges {
  @Input() mandato: ExpedienteMatrizDetalleDTO = null;
  //TODO: string = null;
  @Input() tipoCentro: string = null;
  @Input() modoEstadoVisualizar?: boolean = false;
  @Output() guardar = new EventEmitter<any>();
  @Output() cancelar = new EventEmitter<any>();

  estadoEditar: boolean = false;

  mostrarSubidaDocumentos: boolean = false;

  tituloFormulario: string = null;
  tituloDelitoInfraccion: string = null;
  textoDelitoInfraccion: string = null;

  anios: number;
  meses: number;
  dias: number;

  cortesJusticiaFiltrado: Observable<CatalogoDTO[]>;
  delitosGenericosFiltrado: Observable<CatalogoDTO[]>;
  delitosExpecificosFiltrado: Observable<CatalogoDTO[]>;

  esVariacionMedida: boolean = false;

  mostrarTipoVariacion: boolean = false;
  mostrarVariacionMedida: boolean = false;
  mostrarSituacionJuridica: boolean = false;
  mostrarMotivoExternacion: boolean = false;
  mostrarMotivoInternacion: boolean = false;

  delitosEliminados: ExpedienteMatrizDelitoDTO[] = [];
  medidasAccesoriasElminadas: ExpedienteMatrizMedidaDTO[] = [];
  medidasSocioeducativasEliminadas: ExpedienteMatrizMedidaDTO[] = [];


  displayedColumns: string[] = ['acciones', 'delitoGenerico', 'delitoEspecifico'];
  dataSourceDelitos = new MatTableDataSource<any>;
  formDelitos: FormGroup;
  @ViewChild('paginatorDelito') paginatorDelito: MatPaginator;

  @ViewChild("documentosComp") tablaDocumentos: DocumentosSubidosTablaComponent;
  
  displayedColumnsMedidas: string[] = ['acciones', 'medida'];
  @ViewChild('paginatorMedidasSocioeducativas') paginatorMedidasSocioeducativas: MatPaginator;
  dataSourceMedidasSocieducativas = new MatTableDataSource<any>;
  @ViewChild('paginatorMedidasAccesorias') paginatorMedidasAccesorias: MatPaginator;
  dataSourceMedidasAccesorias = new MatTableDataSource<any>;
  habilitarAgregarMedidaSocioeducativa: boolean = false;
  habilitarAgregarMedidaAccesoria: boolean = false;

  tiposMandato: CatalogoDTO[];
  tiposInfraccion: CatalogoDTO[];
  situacionesJuridicas: CatalogoDTO[];
  situacionesJuridicas2: CatalogoDTO[];

  situacionesLegales: CatalogoDTO[];
  situacionesLegales2: CatalogoDTO[];

  tiposMedidas: CatalogoDTO[];
  frecuenciasIngreso: CatalogoDTO[];
  delitosGenericos: CatalogoDTO[];
  delitosEspecificos: CatalogoDTO[];
  variacionesMedida: CatalogoDTO[];
  motivosExternacion: CatalogoDTO[];
  motivosInternacion: CatalogoDTO[];
  cortesSuperioresJusticia: CatalogoDTO[];
  instancias: CatalogoDTO[];
  especialidades: CatalogoDTO[];
  estados: CatalogoDTO[];
  medidasAccesorias: CatalogoDTO[];

  medidasFormGroup = this.fb.group({    
    medidaSocieducativa:  [null as CatalogoDTO],
    medidaAccesoria:  [null as CatalogoDTO],
  })

  generalFormGroup = this.fb.group({
    tipoRegistro: [null as CatalogoDTO, Validators.required],
    estado: [null as CatalogoDTO],
    situacionJuridica: [null as CatalogoDTO],
    variacionMedida: [null as CatalogoDTO],
    tipoVariacion: [null as CatalogoDTO],
    motivoVariacion: [null as CatalogoDTO],
    numResolucion: [null as string, Validators.required],
    fechaResolucion: [null as Date, Validators.required],
    decision: [null],
    tiempoMedSocEduAnios: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
    tiempoMedSocEduMeses: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
    tiempoMedSocEduDias: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
    fechaInicioMedida: [null as Date],
    fechaFinMedida: [null as Date],
    corteJusticia: [null as CatalogoDTO, Validators.required],
    instancia: [null as CatalogoDTO, Validators.required],
    especialidad: [null as CatalogoDTO, Validators.required],
    organoJurisdiccional: [null],
    juez: [null],
    secretario: [null],
    sancionImpuesta: [null as CatalogoDTO],
    montoReparacion: [0, [Validators.min(0)]], 
  })

  tiposDeDocumentosSistema: TipoDeDocumento[] = [];

  // cjdrFormGroup = this.fb.group({
  //   sancionImpuesta: [null as CatalogoDTO, Validators.required],
  //   situacionLegal: [{ value: null as CatalogoDTO, disabled: true }, Validators.required],
  //   tiempoMedSocEduAnios: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
  //   tiempoMedSocEduMeses: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
  //   tiempoMedSocEduDias: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
  //   corte: [null as string, Validators.required],
  //   juzgado: [null as string, Validators.required],
  //   juez: [null as string, Validators.required],
  //   importe: [0, [Validators.min(0), this.simboloDecimalValidator(this.locale)]],   
  // })

  // soaFormGroup = this.fb.group({
  //   tipoMedSocEduImp: [null as CatalogoDTO, Validators.required],
  //   fechaOficio: [null as Date],
  //   fechaConsentimiento: [null as Date],
  //   numJornadas: [0, [Validators.max(999999), Validators.min(0)]],
  //   fechaInicioTratamiento: [null as Date],
  //   fechaLimNotJuez: [null as Date],
  //   frecuenciaIngreso: [null as CatalogoDTO],
  //   juzgadoFamilia: [null as string, Validators.required],
  //   lugarInfraccion: [null],
  // })


  constructor(
    private dateAdapter: DateAdapter<any>,    
    private catalogoService: CatalogoService,
    public funcionesUtils: FuncionesUtils,
    private dialogMensajeService: DialogMensajeService,
    private expedienteMatrizService: ExpedienteMatrizService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    private fb: FormBuilder,
    private fichaIdentificacionService: FichaIdentificacionService,
  ) {
    this.dateAdapter.setLocale('es-ES');

    this.formDelitos = this.fb.group({
      filas: this.fb.array([]),
    });

    this.dataSourceDelitos = new MatTableDataSource((this.formDelitos.get('filas') as FormArray).controls);
    this.dataSourceDelitos.paginator = this.paginatorDelito;
  }

  get filas(): FormArray {
    return this.formDelitos.get('filas') as FormArray;
  }

  ngOnDestroy(): void {
    this.mandato = null;
  }

  agregarDelito() {
    const fila = this.fb.group({
      idExpedienteDelito: [null as number],
      delitoGenerico: [null as CatalogoDTO, Validators.required],
      delitoEspecifico: [null as CatalogoDTO, Validators.required],
      delitosEspecificos: [[]],
    });

    fila.markAllAsTouched();

    this.filas.controls.unshift(fila);
    this.actualizarTabla();
  }

  obtenerControl(index: number, campo: string) {
    return (this.filas.at(index) as FormGroup).get(campo);
  }

  actualizarTabla() {
    this.dataSourceDelitos.data = this.filas.controls;
    this.dataSourceDelitos.paginator = this.paginatorDelito;
  }

  eliminarDelito(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el delito seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let delitoEliminado: ExpedienteMatrizDelitoDTO = this.filas.at(index).value;
            delitoEliminado.removido = true;
            this.delitosEliminados.push(delitoEliminado);
            this.filas.removeAt(index);
            this.actualizarTabla();
          }
        }
      }
    );
  }

  ngOnInit(): void {
    this.generalFormGroup.markAllAsTouched();

    // this.obtenerTiposDeDocumentos();
    // this.cargarDatos();    
  }  

  private _filter(value: string): CatalogoDTO[] {
    const filterValue = value.toLowerCase();

    return this.cortesSuperioresJusticia.filter(option => option.nombre.toLowerCase().includes(filterValue))
  }

  displayFn(option: CatalogoDTO): string {
    return option && option.nombre ? option.nombre : '';
  }

  seleccionarTipo(event: any) {
    this.generalFormGroup.controls['variacionMedida'].setValidators([]);
    this.generalFormGroup.controls['tipoVariacion'].setValidators([]);
    this.generalFormGroup.controls['motivoVariacion'].setValidators([]);
    this.generalFormGroup.controls['situacionJuridica'].setValidators([]);

    this.generalFormGroup.controls['variacionMedida'].reset();
    this.generalFormGroup.controls['tipoVariacion'].reset();
    this.generalFormGroup.controls['motivoVariacion'].reset();
    this.generalFormGroup.controls['situacionJuridica'].reset();
    this.mostrarVariacionMedida = false;
    this.mostrarTipoVariacion = false;
    this.mostrarMotivoExternacion = false;
    this.mostrarMotivoInternacion = false;
    this.mostrarSituacionJuridica = false;
    const nemonico = event.value.nemonico;
    if (nemonico.includes('INICIAL') || nemonico.includes('FINAL')) {
      this.mostrarVariacionMedida = false;
      this.mostrarTipoVariacion = true;
      this.situacionesJuridicas = [...this.situacionesJuridicas2];
      this.generalFormGroup.controls['tipoVariacion'].setValidators([Validators.required]);
      this.generalFormGroup.controls['tipoVariacion'].markAllAsTouched();

      this.generalFormGroup.controls['situacionJuridica'].setValidators([Validators.required]);
      this.generalFormGroup.controls['situacionJuridica'].markAllAsTouched();
      if (nemonico.includes('INICIAL')) {
        this.situacionesJuridicas = this.situacionesJuridicas.filter(situacion => situacion.nemonico.includes('INTER'));
      } else if (nemonico.includes('FINAL')) {
        this.situacionesJuridicas = this.situacionesJuridicas.filter(situacion => situacion.nemonico.includes('EXTER'));
      }
    } else if (nemonico.includes('SEG')) {
      this.mostrarVariacionMedida = true;
      this.generalFormGroup.controls['variacionMedida'].setValidators([Validators.required]);
      this.generalFormGroup.controls['variacionMedida'].markAllAsTouched();
    }
  }

  seleccionarTipoVariacion(event: any) {
    this.generalFormGroup.controls['motivoVariacion'].setValidators([Validators.required]);
    this.generalFormGroup.controls['motivoVariacion'].markAllAsTouched();
    const nemonico = event.value.nemonico;
    if (nemonico.includes('INTER')) {
      this.mostrarMotivoExternacion = false;
      this.mostrarMotivoInternacion = true;
      this.mostrarSituacionJuridica = true;
      this.situacionesLegales = [...this.situacionesLegales2];
      if (nemonico.includes('INTERN')) {
        this.situacionesLegales = this.situacionesLegales.filter(situacion => situacion.nemonico.includes('SENTENCIADO'));
      } else if (nemonico.includes('INTERV')) {
        this.situacionesLegales = this.situacionesLegales.filter(situacion => situacion.nemonico.includes('PROCESADO'));
      }
    } else if (nemonico.includes('EXTER')) {
      this.mostrarMotivoExternacion = true;
      this.mostrarMotivoInternacion = false;
      this.mostrarSituacionJuridica = false;
    }
  }

  ngOnChanges() {  
    
    this.cargarDatos();    

    if(this.mandato && this.mandato.tokenIdentificador) {
      this.mostrarSubidaDocumentos = true;
    } 

    if (this.tipoCentro == 'CJDR') {
      this.tituloFormulario = 'disposición';
      this.tituloDelitoInfraccion = 'Delitos';
      this.textoDelitoInfraccion = 'Delito';

    } else if (this.tipoCentro == 'SOA') {
      this.tituloFormulario = 'disposición';
      this.tituloDelitoInfraccion = 'Infracciones';
      this.textoDelitoInfraccion = 'Infracción';
    }
    // if (this.tipoCentro == 'CJDR') {
    //   this.tituloFormulario = 'mandato';
    //   this.tituloDelitoInfraccion = 'Delitos';
    //   this.textoDelitoInfraccion = 'Delito';

    // } else if (this.tipoCentro == 'SOA') {
    //   this.tituloFormulario = 'sentencia';
    //   this.tituloDelitoInfraccion = 'Infracciones';
    //   this.textoDelitoInfraccion = 'Infracción';
    // }

    if (this.mandato) {
      this.estadoEditar = true;
      console.log(this.mandato);      

      this.mandato.situacionJuridica && (this.mostrarSituacionJuridica = true);
      this.mandato.variacionMedida && (this.mostrarVariacionMedida = true);
      this.mandato.tipoVariacion && (this.mostrarTipoVariacion = true);

      if (this.mandato.tipoVariacion) {
        const nemonico = this.mandato.tipoVariacion.nemonico;
        if (nemonico.includes('EXTER')) {
          this.mostrarMotivoExternacion = true;
        } else {
          this.mostrarMotivoInternacion = true;
        }
      }     
      
      this.generalFormGroup.patchValue(this.mandato);
      this.generalFormGroup.controls['fechaResolucion'].setValue(this.mandato.fechaResolucion ? new Date(this.mandato.fechaResolucion) : null);
      this.generalFormGroup.controls['fechaInicioMedida'].setValue(this.mandato.fechaInicioMedida ? new Date(this.mandato.fechaInicioMedida) : null);
      this.generalFormGroup.controls['fechaFinMedida'].setValue(this.mandato.fechaFinMedida ? new Date(this.mandato.fechaFinMedida) : null);

      // if (this.mandato.expedienteDelitos.length > 0) {
      //   for (let delito of this.mandato.expedienteDelitos) {
      //     const delitosEspecificos = await this.obtenerDelitosEspecificos(delito.delitoGenerico.nemonico);
      //     const fila = this.fb.group({
      //       delitoGenerico: [delito.delitoGenerico as CatalogoDTO, Validators.required],
      //       delitoEspecifico: [delito.delitoEspecifico as CatalogoDTO, Validators.required],
      //       delitosEspecificos: [delitosEspecificos]
      //     });
      //     this.filas.controls.push(fila);
      //   }
      //   this.actualizarTabla();
      // }

      if (this.mandato.expedienteDelitos.length > 0) {
        const peticiones = this.mandato.expedienteDelitos
        .filter(delito => !delito.removido)
        .map(delito =>
          this.catalogoService.obtenerHijos(delito.delitoGenerico.nemonico, '').pipe(
            map(result => ({
              idExpedienteDelito: delito.idExpedienteDelito,
              delitoGenerico: delito.delitoGenerico as CatalogoDTO,
              delitoEspecifico: delito.delitoEspecifico as CatalogoDTO,
              delitosEspecificos: result.data,
            }))
          )
        );

        this.delitosEliminados = this.mandato.expedienteDelitos.filter(delito => delito.removido);
      
        forkJoin(peticiones).subscribe(respuestas => {
          respuestas.forEach(data => {
            data.delitosEspecificos.sort((a,b) =>  a.nombre.toLowerCase().localeCompare(b.nombre.toLowerCase()));
            const fila = this.fb.group({
              idExpedienteDelito: [data.idExpedienteDelito],
              delitoGenerico: [data.delitoGenerico, Validators.required],
              delitoEspecifico: [data.delitoEspecifico, Validators.required],
              delitosEspecificos: [data.delitosEspecificos],
            });
            this.filas.controls.push(fila);
          });
          this.actualizarTabla();
        });
      }

      // this.obtenerDocumentos();

    } else {
      this.inicializarControles();
      this.agregarDelito();
    }
  }

  cargarDatos(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerCatalogos().pipe(
      // concatMap(() => this.obtenerFichasIngreso()),
      // concatMap(() => this.obtenerParametrosDeConsulta()),
      // concatMap(() =>
      //   iif(
      //     () => this.estadoEditar, 
      //     this.obtenerExpediente(),
      //     this.obtenerFichaIngresoValida(this.uuid_fp) 
      //   )
      // )
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

        if(this.tipoCentro == 'SOA') {
          this.situacionesLegales = this.situacionesLegales.filter(situacion => situacion.nemonico.includes('SENTENC'));
        }

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

        this.delitosGenericos.sort((a,b) =>  a.nombre.toLowerCase().localeCompare(b.nombre.toLowerCase()));

        this.cortesJusticiaFiltrado = this.generalFormGroup.controls['corteJusticia'].valueChanges.pipe(
          startWith(''),
          map(value => typeof value === 'string' ? this._filter(value) : this.cortesSuperioresJusticia),
        );   
        
        if (this.mandato && this.mandato.medidasSocioeducativas.length > 0) {
          for (let item of this.mandato.medidasSocioeducativas) {
            const medida = item.medida;
            this.dataSourceMedidasSocieducativas.data.push(item);
            this.dataSourceMedidasSocieducativas.paginator = this.paginatorMedidasSocioeducativas;
            this.tiposMedidas = this.tiposMedidas?.filter(tipoMedida => tipoMedida.tokenIdentificador != medida.tokenIdentificador);
          }
        }
  
        if (this.mandato && this.mandato.medidasAccesorias.length > 0) {        
          for (let item of this.mandato.medidasAccesorias) {
            const medida = item.medida;
            this.dataSourceMedidasAccesorias.data.push(item);
            this.dataSourceMedidasAccesorias.paginator = this.paginatorMedidasAccesorias;
            this.medidasAccesorias = this.medidasAccesorias?.filter(tipoMedida => tipoMedida.tokenIdentificador != medida.tokenIdentificador);
          }
        }

      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  cargarDelitoEspecifico(event: any, index: number) {
    const nemonico = event.value.nemonico;

    this.catalogoService.obtenerHijos(nemonico, '').subscribe(result => {
      this.delitosEspecificos = result.data;      
      result.data.sort((a,b) =>  a.nombre.toLowerCase().localeCompare(b.nombre.toLowerCase()));
      this.filas.at(index).get('delitosEspecificos')?.setValue(result.data);      
    })
  }

  guardarObjeto() {
    if(!this.mandato) {
      this.mandato = new ExpedienteMatrizDetalleDTO;
    }
    this.mandato.expedienteDelitos = [];
    this.dataSourceDelitos.data.map(item => {
      item.value.removido = false;
      this.mandato.expedienteDelitos.push(item.value);
    });

    this.mandato.medidasSocioeducativas = [];
    for (let item of this.dataSourceMedidasSocieducativas.data) {      
      this.mandato.medidasSocioeducativas.push(item)
    }
    this.mandato.medidasSocioeducativas = this.mandato.medidasSocioeducativas.concat(this.medidasSocioeducativasEliminadas);

    this.mandato.medidasAccesorias = [];
    for (let item of this.dataSourceMedidasAccesorias.data) {     
      this.mandato.medidasAccesorias.push(item);
    }
    // this.mandato.medidasAccesorias = this.mandato.medidasAccesorias.concat(this.medidasAccesoriasElminadas);

    // this.mandato.expedienteDelitos = this.mandato.expedienteDelitos.concat(this.delitosEliminados);
    Object.assign(this.mandato, this.generalFormGroup.value);
    console.log(this.mandato);
    this.fichaIdentificacionService.actualizacionFicha(true);
    this.guardar.emit(this.mandato);
  }

  cancelarCreacion() {
    this.cancelar.emit();
  }  

  // calcularFechaFin(event: any) {
    
     
  //   const fechaMoment = moment(this.generalFormGroup.controls['fechaInicioMedida'].value);

  //   let nuevaFecha;
  //   if (this.dias > 0) {
  //     nuevaFecha = fechaMoment.add(this.dias, 'days').toDate();
  //     this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  //   }

  //   if (this.meses > 0) {
  //     nuevaFecha = fechaMoment.add(this.meses, 'months').toDate();
  //     this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  //   }

  //   if (this.anios > 0) {
  //     nuevaFecha = fechaMoment.add(this.anios, 'years').toDate();
  //     this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  //   } 
    
  //   this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  // }

  calcularFechaFin(event: any) {
    const fechaInicio = this.generalFormGroup.controls['fechaInicioMedida'].value;
    if (!fechaInicio) {
      this.generalFormGroup.controls['fechaFinMedida'].setValue(null);
      return;
    }
    const anios = this.generalFormGroup.controls['tiempoMedSocEduAnios'].value || 0;
    const meses = this.generalFormGroup.controls['tiempoMedSocEduMeses'].value || 0;
    const dias = this.generalFormGroup.controls['tiempoMedSocEduDias'].value || 0;
    const fechaMoment = moment(fechaInicio);
    const nuevaFecha = fechaMoment
      .clone()
      .add(anios, 'years')
      .add(meses, 'months')
      .add(dias, 'days')
      .toDate();
    this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  }
  

  sumarDias(event: any) {
    if (!this.generalFormGroup.controls['fechaFinMedida'].value) {
      const fechaMoment = moment(this.generalFormGroup.controls['fechaInicioMedida'].value);
      this.generalFormGroup.controls['fechaFinMedida'].setValue(fechaMoment.toDate());     
    }
    this.dias = event.data; 
    const fechaMoment = moment(this.generalFormGroup.controls['fechaFinMedida'].value);
    const nuevaFecha = fechaMoment.add(this.dias, 'days').toDate();
    this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  }

  sumarMeses(event: any) {
    if (!this.generalFormGroup.controls['fechaFinMedida'].value) {
      const fechaMoment = moment(this.generalFormGroup.controls['fechaInicioMedida'].value);
      this.generalFormGroup.controls['fechaFinMedida'].setValue(fechaMoment.toDate());     
    }
    this.meses = event.data;  
    const fechaMoment = moment(this.generalFormGroup.controls['fechaFinMedida'].value);
    const nuevaFecha = fechaMoment.add(this.meses, 'months').toDate();
    this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  }

  sumarAnios(event: any) {
    if (!this.generalFormGroup.controls['fechaFinMedida'].value) {
      const fechaMoment = moment(this.generalFormGroup.controls['fechaInicioMedida'].value);
      this.generalFormGroup.controls['fechaFinMedida'].setValue(fechaMoment.toDate());     
    }
    this.anios = event.data;  
    const fechaMoment = moment(this.generalFormGroup.controls['fechaFinMedida'].value);
    const nuevaFecha = fechaMoment.add(this.anios, 'years').toDate();
    this.generalFormGroup.controls['fechaFinMedida'].setValue(nuevaFecha);
  }

  prevenirInputNumberInvalido(event: KeyboardEvent): void {
    const invalidKeys = ['+', '-', 'e', 'E'];
    if (invalidKeys.includes(event.key)) {
      event.preventDefault();
    }
  }  

  subirArchivosEvent(documentos: DocumentoSubido[]) {
    //TODO: GUARDAR REGISTRO DE MANDATO ANTES DE SUBIR UN ARCHIVO
    if (documentos && documentos.length > 0) {
        
      for (let documentoSubido of documentos) {
        let expedienteDetalleDocumentoDTO = new ExpedienteMatrizDetalleDocumentoDTO();
        expedienteDetalleDocumentoDTO.tokenIdentificadorExpedienteDetalle = this.mandato.tokenIdentificador;
        expedienteDetalleDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

        let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
          documentoSubido.documento.name
        );
        this.expedienteMatrizService.subirDocumento(
          documentoSubido.documento,
          expedienteDetalleDocumentoDTO,
          ''
        ).subscribe(
          {
            next: (response: RespuestaPorDefecto<DocumentoDTO>) => {

              load.close();
              if (!response.exito) {
                this.expedienteMatrizService.checkError(response);
                return;
              }

              //Refrescar la tabla de documentos
              this.obtenerDocumentos();
            },
            error: (error: any) => {
              load.close();
              this.expedienteMatrizService.checkError(error);
            }
          }
        );
      }
    } else {
      this.dialogMensajeService.mensajeError("No se obtenieron documentos para ser subidos");
    }
  }

  ngAfterViewInit(): void {
    if (this.mostrarSubidaDocumentos) {
      this.obtenerTiposDeDocumentos();
      this.obtenerDocumentos();
    }
  }

  pageEventDocumentos(event: PageEvent) {
      this.tablaDocumentos.page = event.pageIndex;
      this.tablaDocumentos.pageSize = event.pageSize;
  
      this.obtenerDocumentos();
  }

  obtenerDocumentos() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let expedienteMatrizDetalleDocumentosRequest = new ExpedienteMatrizDetalleDocumentosRequest();
    expedienteMatrizDetalleDocumentosRequest.page = page;
    expedienteMatrizDetalleDocumentosRequest.size = pageSize;
    expedienteMatrizDetalleDocumentosRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
    expedienteMatrizDetalleDocumentosRequest.tokenIdentificadorExpedienteDetalle = this.mandato?.tokenIdentificador;

    this.expedienteMatrizService.obtenerDocumentos(
      expedienteMatrizDetalleDocumentosRequest,
      ''
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {

          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.expedienteMatrizService.checkError(response);
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }

        },
        error: (error: any) => {
          this.expedienteMatrizService.checkError(error);
        }
      }
    );
  }

  private obtenerTiposDeDocumentos() {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(etiquetasModel.SECCION_FICHA_IDENT_EXPEDIENTE_MATRIZ,
      '').subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
            if (!environment.production) {
              console.log(response);
            }

            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
              return;
            }
            let tiposArchivos = response.data;

            if (tiposArchivos.length == 0) {
              this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
              return;
            }

            this.tiposDeDocumentosSistema =
              tiposArchivos.map(
                (tipoArch) => {
                  let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
                  let tipoDeDocumento = new TipoDeDocumento();
                  tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
                  tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
                  tipoDeDocumento.requerido = tipoArch.requerido;
                  tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
                  tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

                  return tipoDeDocumento;
                }
              );
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
  }

  edicionEvent(exito: boolean) {
    if (exito) {
      this.obtenerDocumentos();
    }
  }

  eliminacionDocumento(documentoDTO: DocumentoDTO) {

    let load = this.dialogMensajeService.mensajeLoading("Quitando el documento: " + documentoDTO.nombre + " del detalle..");
    let expedienteMatrizDetalleDocumentoDTO = new ExpedienteMatrizDetalleDocumentoDTO();
    expedienteMatrizDetalleDocumentoDTO.documentoDTO = documentoDTO;
    expedienteMatrizDetalleDocumentoDTO.tokenIdentificadorExpedienteDetalle = this.mandato.tokenIdentificador;
    this.expedienteMatrizService.eliminarDocumento(
      expedienteMatrizDetalleDocumentoDTO,
      ''
    ).subscribe(
      {
        next: (respone: RespuestaPorDefecto<ExpedienteMatrizDetalleDocumentoDTO>) => {
          load.close();
          if (!respone.exito) {
            this.expedienteMatrizService.checkError(respone);
          }

          this.obtenerDocumentos();
        },
        error: (error: any) => {
          load.close();
          this.expedienteMatrizService.checkError(error);
        }
      }
    );
  }

  inicializarControles() {
    this.mostrarVariacionMedida = false;
    this.mostrarSituacionJuridica = false;
    this.mostrarVariacionMedida = false;
    this.mostrarTipoVariacion = false;
    this.mostrarMotivoExternacion = false;
    this.mostrarMotivoInternacion = false;

    this.generalFormGroup = this.fb.group({
      estado: [null as CatalogoDTO],
      tipoRegistro: [null as CatalogoDTO, Validators.required],
      situacionJuridica: [null as CatalogoDTO],
      variacionMedida: [null as CatalogoDTO],
      tipoVariacion: [null as CatalogoDTO],
      motivoVariacion: [null as CatalogoDTO],
      numResolucion: [null as string, Validators.required],
      fechaResolucion: [null as Date, Validators.required],
      decision: [null],
      tiempoMedSocEduAnios: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
      tiempoMedSocEduMeses: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
      tiempoMedSocEduDias: [0, [Validators.required, Validators.max(9999), Validators.min(0)]],
      fechaInicioMedida: [null as Date],
      fechaFinMedida: [null as Date],
      corteJusticia: [null as CatalogoDTO, Validators.required],
      instancia: [null as CatalogoDTO, Validators.required],
      especialidad: [null as CatalogoDTO, Validators.required],
      organoJurisdiccional: [null],
      juez: [null],
      secretario: [null],
      sancionImpuesta: [null as CatalogoDTO],
      montoReparacion: [0, [Validators.min(0)]],  
    })
  }

  agregarMedidaSocioeducativa() {
    const medida = this.medidasFormGroup.controls['medidaSocieducativa'].value;
    let medidaAgregada: ExpedienteMatrizMedidaDTO = new ExpedienteMatrizMedidaDTO;
    medidaAgregada.medida = medida;
    medidaAgregada.removido = false;
    this.dataSourceMedidasSocieducativas.data.unshift(medidaAgregada);
    this.dataSourceMedidasSocieducativas.paginator = this.paginatorMedidasSocioeducativas;
    this.medidasFormGroup.controls['medidaSocieducativa'].reset();
    this.tiposMedidas = this.tiposMedidas.filter(item => item.tokenIdentificador != medida.tokenIdentificador);
    this.habilitarAgregarMedidaSocioeducativa = false;
  }    

  eliminarMedidasSocioeducativas(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el registro seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            const eliminado = this.dataSourceMedidasSocieducativas.data.splice(index, 1);
            this.dataSourceMedidasSocieducativas = new MatTableDataSource(this.dataSourceMedidasSocieducativas.data);
            this.tiposMedidas.push(eliminado[0].medida);

            if(eliminado[0].tokenIdentificador) {
              let medidaEliminada: ExpedienteMatrizMedidaDTO = eliminado[0];
              medidaEliminada.removido = true;
              this.medidasSocioeducativasEliminadas.push(medidaEliminada);
            }
          }
        }
      }
    )
  }

  agregarMedidaAccesoria() {      
    const medida = this.medidasFormGroup.controls['medidaAccesoria'].value;
    let medidaAgregada: ExpedienteMatrizMedidaDTO = new ExpedienteMatrizMedidaDTO;
    medidaAgregada.medida = medida;
    medidaAgregada.removido = false;
    this.dataSourceMedidasAccesorias.data.unshift(medidaAgregada);
    this.dataSourceMedidasAccesorias.paginator = this.paginatorMedidasAccesorias;
    this.medidasFormGroup.controls['medidaAccesoria'].reset();
    this.medidasAccesorias = this.medidasAccesorias.filter(item => item.tokenIdentificador != medida.tokenIdentificador);
    this.habilitarAgregarMedidaAccesoria = false;
  }    

  eliminarMedidasAccesoria(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el registro seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            const eliminado = this.dataSourceMedidasAccesorias.data.splice(index, 1);
            this.dataSourceMedidasAccesorias = new MatTableDataSource(this.dataSourceMedidasAccesorias.data);
            this.medidasAccesorias.push(eliminado[0].medida);

            if(eliminado[0].tokenIdentificador) {
              let medidaEliminada: ExpedienteMatrizMedidaDTO = eliminado[0];
              medidaEliminada.removido = true;
              this.medidasSocioeducativasEliminadas.push(medidaEliminada);
            }

          }
        }
      }
    )
  }

  formularioInvalido() : boolean {
    const value = 
                    this.generalFormGroup.invalid || 
                    this.filas.controls.some((row) => row.invalid)
    return value;
  }
  
}
