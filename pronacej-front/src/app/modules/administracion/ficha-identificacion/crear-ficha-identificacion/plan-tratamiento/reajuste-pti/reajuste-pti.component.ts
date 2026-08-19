import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { PlanTratamientoIndDTO, PlanTratamientoIndIntervDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { catchError, concatMap, forkJoin, iif, Observable, of, tap, throwError } from 'rxjs';
import { ModalEditaIntervComponent } from '../crear-editar-pti-cerrado/modal-edita-interv/modal-edita-interv.component';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule, Location } from '@angular/common';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { ModalEditaIntervAbiertoComponent } from '../crear-editar-pti-abierto/modal-edita-interv-abierto/modal-edita-interv-abierto.component';

@Component({
  selector: 'app-reajuste-pti',
  standalone: true,
  imports: [
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatTooltipModule,
    CommonModule
  ],
  templateUrl: './reajuste-pti.component.html',
  styleUrl: './reajuste-pti.component.scss'
})
export class ReajustePtiComponent {

  planTratamiento: PlanTratamientoIndDTO;
  planTratamientoOriginal: PlanTratamientoIndDTO;
  estadoVisualizar: boolean = false;
  estadoEditar: boolean = false;

  mostrarTablasReajustePtiCerrado: boolean = false;
  mostrarTablasReajustePtiAbierto: boolean = false;
  mostrarTablasComunidad: boolean = false;
  mostrarTablaPtiCompleta: boolean = false;

  displayedColumnsPtiCerrado: string[] = [
    'acciones', 'dimension', 'objetivo', 'actividadPrograma', 'equipoResponsable', 'tiempoEstimado',
     'reajuste', 'fechaReajuste', 'fundamentacionReajuste'
    ];

  columnasMatrizPti: string[] = [
    'acciones', 
    'dimension', 
    'objetivo', 
    'actividadPrograma', 
    'tiempoEstimado', 
    'equipoResponsable',
    'reajuste', 'fechaReajuste', 'fundamentacionReajuste'
  ];

  columnasMatrizPtiCompleta: string[] = [
    'acciones', 
    'dimension', 
    'objetivo', 
    'actividadPrograma', 
    'tiempoEstimado', 
    'modalidad', 
    'frecuencia', 
    'equipoResponsable',
    'reajuste', 'fechaReajuste', 'fundamentacionReajuste'
  ];

  columnasMedidas: string[] = [
    'acciones', 
    'dimension', 
    'objetivo', 
    'actividadPrograma', 
    'equipoResponsable',
    'tiempoEstimado', 
    'lugar',
    'numAtencionGrupal',
    'modalidad', 
    'frecuencia', 
    'reajuste', 'fechaReajuste', 'fundamentacionReajuste'
  ];

  columnasFase: string[] = [
    'acciones', 
    'dimension', 
    'objetivo', 
    'actividadPrograma', 
    'numAtencionGrupal', 
    'tiempoEstimado', 
    'reajuste', 'fechaReajuste', 'fundamentacionReajuste'
  ];

  dataSourceIntervObjetivos: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceIntervNoCriminogenos: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceIntervDiferenciada: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceIntervFases: MatTableDataSource<PlanTratamientoIndIntervDTO>;

  @ViewChild('paginatorIntervObjetivos') paginatorIntervObjetivos: MatPaginator;
  @ViewChild('paginatorIntervNoCriminogenos') paginatorIntervNoCriminogenos: MatPaginator;
  @ViewChild('paginatorIntervDiferenciada') paginatorIntervDiferenciada: MatPaginator;
  @ViewChild('paginatorIntervFases') paginatorIntervFases: MatPaginator;


  dimensionesCjdr: CatalogoDTO[];
  dimensionesSoa: CatalogoDTO[];
  modalidades: CatalogoDTO[];
  componentesMatriz: CatalogoDTO[];
  controles: CatalogoDTO[];
  frecuencias: CatalogoDTO[];
  estados: CatalogoDTO[];
  fasesPreparacion: CatalogoDTO[];
  fasesEjecucion: CatalogoDTO[];

  motivoReajusteFormControl = new FormControl('', Validators.required);

  uuid_fp: string;

  constructor(
    private _location: Location,
    private router: Router,
    private route: ActivatedRoute,
    private planTratamientoService: PlanTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    public dialog: MatDialog,
    public funcionesUtils: FuncionesUtils,
    private catalogoService: CatalogoService,    
  ) {
    this.planTratamiento = new PlanTratamientoIndDTO(); 
    this.planTratamiento.intervObjetivos = [];
    this.planTratamiento.intervNoCriminogenos = [];
    this.planTratamiento.intervDiferenciada = [];
    this.planTratamiento.intervMedidas = [];
    this.dataSourceIntervObjetivos = new MatTableDataSource(this.planTratamiento.intervObjetivos);
    this.dataSourceIntervNoCriminogenos = new MatTableDataSource(this.planTratamiento.intervNoCriminogenos);
    this.dataSourceIntervDiferenciada = new MatTableDataSource(this.planTratamiento.intervDiferenciada);
    this.dataSourceIntervFases = new MatTableDataSource(this.planTratamiento.intervMedidas);
  }

  ngOnInit(): void {
    this.motivoReajusteFormControl.markAllAsTouched();

    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.cargarDatos();
  }

  cargarDatos(): void {        
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerParametrosDeConsulta().pipe(      
      concatMap(() => this.obtenerCatalogos()),
      concatMap(() =>
        iif(
          () => this.estadoEditar, 
          this.obtenerPlanTratamiento(),          
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

  obtenerPlanTratamiento(): Observable<any> {
    return this.planTratamientoService.obtenerPlanTratamientoPorId(this.route.snapshot.queryParams['numDoc'], '').pipe(
      tap((item) => {
        this.planTratamiento = item.data;
                
        this.planTratamientoOriginal = structuredClone(this.planTratamiento);
        this.planTratamiento.esEdicion = true;
        this.dataSourceIntervObjetivos = this.ordenarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervNoCriminogenos = this.ordenarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervDiferenciada = this.ordenarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceIntervFases = this.ordenarDataSource(this.planTratamiento.intervMedidas);

        if (this.planTratamiento.tipoCentro === 'CJDR') {
          this.mostrarTablasReajustePtiCerrado = true;       
        } else if (this.planTratamiento.tipoCentro === 'SOA') {
          this.mostrarTablasReajustePtiAbierto = true;
          if (this.planTratamiento.tipoAbierto === 'Prestación de Servicios a la Comunidad') {
            this.mostrarTablasComunidad = true;
          } else if (this.planTratamiento.tipoAbierto === 'Libertad Restringida/Libertad Asistida') {
            this.mostrarTablaPtiCompleta = true;
          }
          // if (this.planTratamiento.tipoAbierto == 'Libertad Restringida/Libertad Asistida' || this.planTratamiento.tipoAbierto == 'Amonestación o Semilibertad') {
          //   this.tituloIntervObjetivos = 'Matriz del Plan de tratamiento individual';
          //   this.mostrarIntervObjetivos = true;
          //   this.tituloIntervDiferenciada = 'Matriz del cumplimiento de medidas accesorias';
          //   this.mostrarIntervDiferenciada = true;

          // } else if (this.planTratamiento.tipoAbierto == 'Prestación de Servicios a la Comunidad') {

          // }
        }

        setTimeout(() => {
          this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
          this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
          this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;    
          this.dataSourceIntervFases.paginator = this.paginatorIntervFases;
        });
      }),
      catchError(err => {
        this.planTratamientoService.checkError(err);
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
          if (state) {
            this.estadoVisualizar = true;               
          }
          this.estadoEditar = true;        
        } 
        observer.next();
        observer.complete();
      });
    });
  }

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'DIMENSIONES_PLAN_TRATAMIENTO',
      'DIMENSIONES_PLAN_TRATAMIENTO_SOA',
      'PTI_SOA_MODALIDAD',
      'PTI_SOA_MATRIZ_COMPONENTES',    
      'PTI_SOA_CONTROL_ASISTENCIA',    
      'PTI_SOA_FRECUENCIA',      
      'ESTADOS_PTI',
      'PTI_SOA_FASE_PREPARACION',
      'PTI_SOA_FASE_EJECUCION',   
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, ''));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.dimensionesCjdr = results[0]?.data;
        this.dimensionesSoa = results[1]?.data; 
        this.modalidades = results[2]?.data; 
        this.componentesMatriz = results[3]?.data; 
        this.controles = results[4]?.data; 
        this.frecuencias = results[5]?.data; 
        this.estados = results[6]?.data; 
        this.fasesPreparacion = results[7]?.data; 
        this.fasesEjecucion = results[8]?.data; 
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  aniadirFilaIntervObjetivos() {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensionesCjdr },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos.unshift(result);
        this.dataSourceIntervObjetivos = this.ordenarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  } 

  mostrarFilaInterv(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervComponent, {
      data: {fila: fila, dimensiones: this.dimensionesCjdr, visualizar: true},
      width: '600px'
    });     
  }  

  eliminarItemIntervObjetivos(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervObjetivos.splice(index, 1);
            this.dataSourceIntervObjetivos = this.ordenarDataSource(this.planTratamiento.intervObjetivos);
            this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
          }
        }
      }
    )         
  }

  aniadirFilaIntervNoCriminogenos() {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensionesCjdr },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervNoCriminogenos.unshift(result);
        this.dataSourceIntervNoCriminogenos = this.ordenarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  } 

  editarFilaIntervNoCriminogenos(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: {fila: fila, dimensiones: this.dimensionesCjdr},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        this.planTratamiento.intervNoCriminogenos[index] = result;
        this.dataSourceIntervNoCriminogenos = this.ordenarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  }  

  eliminarItemIntervNoCriminogenos(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervNoCriminogenos.splice(index, 1);
            this.dataSourceIntervNoCriminogenos = this.ordenarDataSource(this.planTratamiento.intervNoCriminogenos);
            this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
          }
        }
      }
    )   
  }
  
  aniadirFilaIntervDiferenciada() {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensionesCjdr },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervDiferenciada.unshift(result);
        this.dataSourceIntervDiferenciada = this.ordenarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
      }
    })
  } 

  editarFilaIntervDiferenciada(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: {fila: fila, dimensiones: this.dimensionesCjdr},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        this.planTratamiento.intervDiferenciada[index] = result;
        this.dataSourceIntervDiferenciada = this.ordenarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
      }
    })
  }  

  eliminarItemIntervDiferenciada(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervDiferenciada.splice(index, 1);
            this.dataSourceIntervDiferenciada = this.ordenarDataSource(this.planTratamiento.intervDiferenciada);
            this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
          }
        }
      }
    ) 
  }

  aniadirFilaMedidas() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {dimensiones: this.planTratamiento.medidasAccesorias, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-cumplimiento'},
      width: '600px'
    });  

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervDiferenciada.unshift(result);
        this.dataSourceIntervDiferenciada = this.ordenarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervObjetivos;
      }
    })
  } 

  mostartFilaMedidas(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.planTratamiento.medidasAccesorias, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-cumplimiento', visualizar: true},
      width: '600px'
    });     
  }  

  aniadirFilaMatrizPti() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {dimensiones: this.componentesMatriz, tipo: 'matriz-pti-simple'},
      width: '600px'
    });  

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos.unshift(result);
        this.dataSourceIntervObjetivos = this.ordenarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  } 

  mostrarFilaMatrizPti(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.componentesMatriz, tipo: 'matriz-pti-simple', visualizar: true},
      width: '600px'
    });     
  }  

  aniadirFilaMatrizPtiCompleta() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: { dimensiones: this.componentesMatriz, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-pti' },
      width: '600px'
    });  

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos.unshift(result);
        this.dataSourceIntervObjetivos = this.ordenarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  } 

  mostrarFilaMatrizPtiCompleta(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.componentesMatriz, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-pti', visualizar: true},
      width: '600px'
    });     
  }  

  aniadirFilaFasePreparacion() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {dimensiones: this.fasesPreparacion, tipo: 'tabla-fase'},
      width: '600px'
    });  

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervNoCriminogenos.unshift(result);
        this.dataSourceIntervNoCriminogenos = this.ordenarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  } 

  mostrarFilaFasePreparacion(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.fasesPreparacion, tipo: 'tabla-fase', visualizar: true},
      width: '600px'
    });     
  }  

  aniadirFilaFaseEjecucion() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {dimensiones: this.fasesEjecucion, tipo: 'tabla-fase'},
      width: '600px'
    });  

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervMedidas.unshift(result);
        this.dataSourceIntervFases = this.ordenarDataSource(this.planTratamiento.intervMedidas);
        this.dataSourceIntervFases.paginator = this.paginatorIntervFases;
      }
    })
  } 

  mostrarFilaFaseEjecucion(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.fasesEjecucion, tipo: 'tabla-fase', visualizar: true},
      width: '600px'
    });     
  }  

  eliminarFilaFaseEjecucion(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervMedidas.splice(index, 1);
            this.dataSourceIntervFases = this.ordenarDataSource(this.planTratamiento.intervMedidas);
            this.dataSourceIntervFases.paginator = this.paginatorIntervFases;
          }
        }
      }
    )      
  }

  ordenarDataSource(lista: PlanTratamientoIndIntervDTO[]) : MatTableDataSource<PlanTratamientoIndIntervDTO> {
    let listaOrdenada: PlanTratamientoIndIntervDTO[] = [];

    listaOrdenada = lista.sort((a, b) => {
      // const fechaDiff = new Date(b.fechaReajuste).getTime() - new Date(a.fechaReajuste).getTime();
      // if (fechaDiff !== 0) {
      //   return fechaDiff;
      // }
      // return (b.activo === a.activo) ? 0 : (b.activo ? 1 : -1);      
      if (a.activo !== b.activo) {
        return a.activo ? -1 : 1; // Activo: true (-1), falso (+1)
      }
      return new Date(b.fechaReajuste).getTime() - new Date(a.fechaReajuste).getTime();
    });

    return new MatTableDataSource(listaOrdenada);
  }

  
  regresar() {
    this._location.back();
  }

  guardarReajuste() {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'Se realizará el ajuste del PTI de acuerdo a los registros creados/modificados.',
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.tokenPadre = this.uuid_fp;

            for (let interv of this.planTratamiento.intervObjetivos) {
              if (!interv.tokenIdentificador) {
                interv.fechaReajuste = new Date();
                interv.reajuste = true;
                interv.fundamentacionReajuste = this.motivoReajusteFormControl.value;
              } else {
                const itemEncontrado = this.planTratamientoOriginal.intervObjetivos.find(objetivo => objetivo.tokenIdentificador == interv.tokenIdentificador);
                if (itemEncontrado.activo !== interv.activo) {
                  interv.fechaReajuste = new Date();
                  interv.reajuste = true;
                  interv.fundamentacionReajuste = this.motivoReajusteFormControl.value;
                }
              }
            }

            for (let interv of this.planTratamiento.intervNoCriminogenos) {
              if (!interv.tokenIdentificador) {
                interv.fechaReajuste = new Date();
                interv.reajuste = true;
                interv.fundamentacionReajuste = this.motivoReajusteFormControl.value;
              } else {
                const itemEncontrado = this.planTratamientoOriginal.intervNoCriminogenos.find(objetivo => objetivo.tokenIdentificador == interv.tokenIdentificador);
                if (itemEncontrado.activo !== interv.activo) {
                  interv.fechaReajuste = new Date();
                  interv.reajuste = true;
                  interv.fundamentacionReajuste = this.motivoReajusteFormControl.value;
                }
              }
            }

            for (let interv of this.planTratamiento.intervDiferenciada) {
              if (!interv.tokenIdentificador) {
                interv.fechaReajuste = new Date();
                interv.reajuste = true;
                interv.fundamentacionReajuste = this.motivoReajusteFormControl.value;
              } else {
                const itemEncontrado = this.planTratamientoOriginal.intervDiferenciada.find(objetivo => objetivo.tokenIdentificador == interv.tokenIdentificador);
                if (itemEncontrado.activo !== interv.activo) {
                  interv.fechaReajuste = new Date();
                  interv.reajuste = true;
                  interv.fundamentacionReajuste = this.motivoReajusteFormControl.value;
                }
              }
            }

            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, '').subscribe(
              {
                next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
                  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
        
                    return;
                  }      
                  // this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar`], {queryParams: {numDoc: this.planTratamiento.idPlanTratamiento}})                  
                  this._location.back();
                },
                error: (error: any) => {
                  this.planTratamientoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );    
    
  }

}
