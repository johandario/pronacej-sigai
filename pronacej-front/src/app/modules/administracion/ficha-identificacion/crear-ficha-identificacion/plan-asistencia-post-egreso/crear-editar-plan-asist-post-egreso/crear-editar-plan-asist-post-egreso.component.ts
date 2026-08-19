import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PlanAsistenciaPostEgresoDetalleDTO, PlanAsistenciaPostEgresoDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanAsistenciaService } from 'app/modules/seguridad/services/planAsistencia.service';
import { catchError, concatMap, forkJoin, iif, Observable, of, tap, throwError } from 'rxjs';
import { ModalPlanAsistPeComponent } from './modal-plan-asist-pe/modal-plan-asist-pe.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatTabsModule } from '@angular/material/tabs';
import { AsistenciaSeguimientoPostEgresoComponent } from '../../post-egreso/asistencia-seguimiento-post-egreso/asistencia-seguimiento-post-egreso.component';
import { InformeFinalAsistenciaComponent } from '../../post-egreso/informe-final-asistencia/informe-final-asistencia.component';
import { CommonModule, Location } from '@angular/common';

@Component({
  selector: 'app-crear-editar-plan-asist-post-egreso',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatButtonModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatInputModule,
    MatIconModule,
    MatPaginatorModule,
    MatTableModule,
    ReactiveFormsModule,
    MatTabsModule,
    AsistenciaSeguimientoPostEgresoComponent,
    InformeFinalAsistenciaComponent,
    CommonModule
  ],
  providers: [provideNativeDateAdapter()],  
  templateUrl: './crear-editar-plan-asist-post-egreso.component.html',
  styleUrl: './crear-editar-plan-asist-post-egreso.component.scss'
})
export class CrearEditarPlanAsistPostEgresoComponent {

  dimensiones: CatalogoDTO[];
  estados: CatalogoDTO[];

  dataSource: MatTableDataSource<PlanAsistenciaPostEgresoDetalleDTO>;
  @ViewChild('paginator') paginator: MatPaginator;
  
  estadoEditar: boolean = false;
  estadoVisualizar: boolean = false;
  selectedIndex: number = 0;

  planAsistencia: PlanAsistenciaPostEgresoDTO;

  displayedColumns: string[] = [
    'acciones',
    'area',
    'factores',
    'objetivoGeneral',
    'objetivoEspecifico',
    'actividades',
    'institucion',
    'frecuencia',
    'indicador',
  ];

  uuid_fp: string;

  fechasFormGroup = this.fb.group({
    fechaInicio: [new Date as Date, Validators.required],
    fechaFin: [new Date as Date, Validators.required],    
  })

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private catalogoService: CatalogoService,
    private planAsistenciaservice: PlanAsistenciaService,
    private dialogMensajeService: DialogMensajeService,
    public dialog: MatDialog,    
    private location: Location,
  ) {
    
  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.route.queryParams.subscribe(params => {
      this.estadoVisualizar = params['mode'] === 'ver';
  
      // Si es modo "visualizar", deshabilitar el formulario
      if (this.estadoVisualizar) {
        this.fechasFormGroup.disable();
      }
    });
    this.cargarDatos();
  }

  cargarDatos(): void {        
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerIndiceTabs().pipe(    
      concatMap(() => this.obtenerCatalogos()),  
      concatMap(() => this.obtenerParametrosDeConsulta()),
      concatMap(() =>
        iif(
          () => this.estadoEditar, 
          this.obtenerPlanAsistencia(),          
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

  obtenerIndiceTabs() : Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tabIndex = params['tabIndex'];
        if (tabIndex) {
          this.selectedIndex = parseInt(tabIndex);
        }
      })
    );
  }

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const numDoc = params['tokenPlan'];
        if (numDoc) {
          const state = params['state'];
          if (state) {
            this.estadoVisualizar = true;
            this.fechasFormGroup.disable();
          }
          this.estadoEditar = true;         
        } else {
          this.fechasFormGroup.markAllAsTouched();
          this.planAsistencia = new PlanAsistenciaPostEgresoDTO;
          this.dataSource = new MatTableDataSource(this.planAsistencia.planDetalle);
          this.dataSource.paginator = this.paginator;
        }
      })
    );
  }

  obtenerPlanAsistencia(): Observable<any> {
    return this.planAsistenciaservice.obtenerPlanAsistenciaPorToken(this.route.snapshot.queryParams['tokenPlan'], '').pipe(
      tap((item) => {
        this.planAsistencia = item.data;
        this.planAsistencia.esEdicion = true;
        this.fechasFormGroup.controls['fechaInicio'].setValue(new Date(this.planAsistencia.fechaInicio));
        this.fechasFormGroup.controls['fechaFin'].setValue(new Date(this.planAsistencia.fechaFin));     
        this.dataSource = new MatTableDataSource(this.planAsistencia.planDetalle);
        this.dataSource.paginator = this.paginator;          
      }),
      catchError(err => {
        this.planAsistenciaservice.checkError(err);
        return throwError(() => err); 
      })
    );
  }  

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'PLAN_ASISTENCIA_POST_EGRESO_AREAS',  
      'ESTADOS_PLAN_ASISTENCIA',
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, ''));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.dimensiones = results[0]?.data;
        this.estados = results[1]?.data;
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  agregarFila() {
    const dialogRef = this.dialog.open(ModalPlanAsistPeComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: PlanAsistenciaPostEgresoDetalleDTO) => {
      if (result) {   
        this.planAsistencia.planDetalle.push(result);     
        this.dataSource = new MatTableDataSource(this.planAsistencia.planDetalle);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  editarFila(fila: PlanAsistenciaPostEgresoDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalPlanAsistPeComponent, {
      disableClose: true,
      data: { fila: fila, dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: PlanAsistenciaPostEgresoDetalleDTO) => {
      if (result) {        
        this.planAsistencia.planDetalle[index] = result;
        this.dataSource = new MatTableDataSource(this.planAsistencia.planDetalle);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  guardarPlanAsistencia() {
    let fechaInicio = this.fechasFormGroup.controls['fechaInicio'].value;
    let fechaFinal = this.fechasFormGroup.controls['fechaFin'].value;
    
    fechaInicio = new Date(fechaInicio.getFullYear(), fechaInicio.getMonth(), fechaInicio.getDate())
    fechaFinal = new Date(fechaFinal.getFullYear(), fechaFinal.getMonth(), fechaFinal.getDate())

    if (fechaInicio.getTime() <= fechaFinal.getTime()) {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se guardará el nuevo registro con la información ingresada.',
        "Deseas continuar?"
      );
  
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              Object.assign(this.planAsistencia, this.fechasFormGroup.value);              
              this.planAsistencia.tokenFichaIdenticacion = this.uuid_fp;
              this.planAsistencia.estado = this.estados.find(estado => estado.nemonico === 'ESTADO_PLAN_ASISTENCIA_ACTIVO');
              this.planAsistenciaservice.crearPlanAsistencia(this.planAsistencia, '').subscribe({
                next: (response: RespuestaPorDefecto<PlanAsistenciaPostEgresoDTO>) => {
  
                  if (!response.exito) {
                    this.planAsistenciaservice.checkError(response);
  
                    return;
                  }
                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`], {queryParams: {tabIndex: 1}});                
                },
                error: (error: any) => {
                  this.planAsistenciaservice.checkError(error);
                }
              })
            }
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeAdvertencia("Fecha incorrecta", "La fecha de fin no puede ser menor a la fecha inicio")
    } 

    
  }

  validarPlanAsistencia() {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'Se guardará el nuevo registro con la información ingresada.',
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            Object.assign(this.planAsistencia, this.fechasFormGroup.value);              
            this.planAsistencia.tokenFichaIdenticacion = this.uuid_fp;
            this.planAsistencia.estado = this.estados.find(estado => estado.nemonico === 'ESTADO_PLAN_ASISTENCIA_ACTIVO');
            this.planAsistenciaservice.crearPlanAsistencia(this.planAsistencia, '').subscribe({
              next: (response: RespuestaPorDefecto<PlanAsistenciaPostEgresoDTO>) => {

                if (!response.exito) {
                  this.planAsistenciaservice.checkError(response);

                  return;
                }
                history.state.plan = response.data;           
                this.ngOnInit();
              },
              error: (error: any) => {
                this.planAsistenciaservice.checkError(error);
              }
            })
          }
        }
      }
    );
  }

  asignarIndice(event: any) {  
    this.selectedIndex = event;

    const queryParams: Params = { tabIndex: event };

    this.router.navigate(
      [], 
      {
        relativeTo: this.route,
        queryParams, 
        queryParamsHandling: 'merge',
      }
    );  
  }  
  
  regresar() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`], {queryParams: {tabIndex: 1}});
  }

}
