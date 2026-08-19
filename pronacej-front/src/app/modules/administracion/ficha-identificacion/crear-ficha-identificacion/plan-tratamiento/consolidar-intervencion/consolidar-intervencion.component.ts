import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ActividadIntervencionDTO, PlanTratamientoIndDTO, PlanTratamientoIndIntervDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/modules/catalogo/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { List } from 'lodash';
import moment from 'moment';
import { Observable, map, catchError, of } from 'rxjs';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { ActividadIntervencionDialogComponent } from './actividad-intervencion-dialog/actividad-intervencion-dialog.component';
import {Location} from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-consolidar-intervencion',
  standalone: true,
  imports: [MatStepperModule,
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
    CommonModule,
    MatTooltipModule
  ],
  templateUrl: './consolidar-intervencion.component.html',
  styleUrl: './consolidar-intervencion.component.scss',
  providers: [provideNativeDateAdapter(),]
})
export class ConsolidarIntervencionComponent {

  planTratamiento: PlanTratamientoIndDTO;
  listaSeguimiento: PlanTratamientoIndDTO[];
  intervDiferenciadas: PlanTratamientoIndIntervDTO[];
  intervDiferenciada: PlanTratamientoIndIntervDTO;

  uuid_fp: string;
  numDoc: string;
  numIntr: string;

  listaTipoFrecuencia: CatalogoDTO[] = [];

  registroFormGroup = this.fb.group({
    programa: [null],
    area: [null],
    fechaInicio: [new Date()],
    fechaFin: [new Date()],
    horaInicio: [null],
    horaFin: [null],
    frecuencia: [null as CatalogoDTO, Validators.required],
    subactividad: [null],
  })

  displayedColumnsActividades: string[] = ['acciones', 'subactividad', 'frecuencia', 'fechaInicio', 'fechaFin'];
  dataSourceActividades = new MatTableDataSource<ActividadIntervencionDTO>([]);
  estadoVisualizar = false; // Controla la habilitación de botones, ajusta según tu lógica

  @ViewChild('paginatorActividades') paginator: MatPaginator;
  listSize = [5, 10, 15, 20];
  page = 0;
  size = this.listSize[0];
  totalItems = 0;
  listaActividades: ActividadIntervencionDTO[] = [];

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_PTI;

  constructor(
    private fb: FormBuilder,
    private dateAdapter: DateAdapter<any>,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private planTratamientoService: PlanTratamientoService,
    private router: Router,
    private _catalogoService: CatalogoService,
    private _location: Location,
  ) {
    this.dateAdapter.setLocale('es');
  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.route.queryParams.subscribe(params => {
      const numInterv = params['numInterv'];
      const numDoc = params['numDoc'];
      this.numDoc = numDoc;
      if (numInterv) {
        this.numIntr = numInterv;
        this.obtenerActividadesIntervencion()
        this.obtenerIntervencion(numInterv);
      }
    });
    this.getCatalogoFrecuencia();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  cancelarEdicion(): void {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar`], {
      queryParams:
        { numDoc: this.numDoc }
    })
  }

  async getCatalogoFrecuencia() {
    this.getCatalogos('FRECUENCIA').subscribe(
      (catalogos: CatalogoDTO[]) => {
        this.listaTipoFrecuencia = catalogos;
      }
    );
  }

  getCatalogos(nemonico: string): Observable<CatalogoDTO[]> {
    return this._catalogoService.getCatalogosPorNemonicPadre(nemonico,this.nemonicoMenu).pipe(
      map((response: RespuestaPorDefecto<List<CatalogoDTO>>) => {
        if (!response.exito) {
          this._catalogoService.checkError(response);
          return [];
        }
        return Array.from(response.data);
      }),
      catchError((error: any) => {
        this._catalogoService.checkError(error);
        return of([]);
      })
    );
  }

  private obtenerValor(key: string) {
    return this.registroFormGroup.get(key)?.value;
  }

  ejecutarAccion(): void {

    this.registroFormGroup.disable();

    const fecha: Date = this.registroFormGroup.get('fechaInicio')?.value;
    const hora: string = this.registroFormGroup.get('horaInicio')?.value;

    const [hours, minutes] = hora.split(':').map(Number);
    const fechaHora = new Date(fecha);
    fechaHora.setHours(hours);
    fechaHora.setMinutes(minutes);
    fechaHora.setSeconds(0);
    fechaHora.setMilliseconds(0);
    fechaHora;

    const fechaFin: Date = this.registroFormGroup.get('fechaFin')?.value;
    const horaFin: string = this.registroFormGroup.get('horaFin')?.value;

    const [hoursF, minutesF] = horaFin.split(':').map(Number);
    const fechaHoraFin = new Date(fechaFin);
    fechaHoraFin.setHours(hoursF);
    fechaHoraFin.setMinutes(minutesF);
    fechaHoraFin.setSeconds(0);
    fechaHoraFin.setMilliseconds(0);
    fechaHoraFin;

    // let fechaFin = this.obtenerValor('fechaFin');
    // let horaFin = this.obtenerValor('horaFin');

    let intervencionDTO = new PlanTratamientoIndIntervDTO();
    intervencionDTO.fechaInicio = fechaHora;
    intervencionDTO.fechaFin = fechaHoraFin;
    intervencionDTO.frecuencia = this.obtenerValor('frecuencia');
    intervencionDTO.idPlanTratIndInterv = this.intervDiferenciada.idPlanTratIndInterv;
    intervencionDTO.frecuencia = this.obtenerValor('frecuencia');
    intervencionDTO.tokenFichaIdentificacion = this.uuid_fp;

    console.log('intervencionDTO', intervencionDTO);

    this.planTratamientoService.actualizarIntervencion(intervencionDTO).subscribe({
      next: (
        response: RespuestaPorDefecto<PlanTratamientoIndIntervDTO>
      ) => {
        this.registroFormGroup.enable();

        if (!response.exito) {
          this.planTratamientoService.checkError(response);

          return;
        }
        this.dialogMensajeService.mensajeExitoso(
          response.titulo,
          response.mensaje
        );

        this.obtenerIntervencion(this.intervDiferenciada.idPlanTratIndInterv.toString());
      },
      error: (error: any) => {
        this.planTratamientoService.checkError(error);
        this.registroFormGroup.enable();
      },
    });

  }

  compararCatalogos(o1: any, o2: any): boolean {
    return o1 && o2 ? o1.nemonico === o2.nemonico : o1 === o2;
  }

  obtenerIntervencion(numInterv: string) {
    this.planTratamientoService.obtenerIntervencion(numInterv).subscribe(item => {
      console.log('intervencion', item)
      this.intervDiferenciada = item.data;
      this.registroFormGroup.get('programa').setValue(this.intervDiferenciada.actividadPrograma);
      this.registroFormGroup.get('area').setValue(this.intervDiferenciada.dimension.descripcion);
      if (this.intervDiferenciada.frecuencia) {
        this.registroFormGroup.get('frecuencia').setValue(this.intervDiferenciada.frecuencia);
      }
      if (this.intervDiferenciada.fechaInicio) {
        const fechaInicioObj = new Date(this.intervDiferenciada.fechaInicio);
        const horas = ('0' + fechaInicioObj.getHours()).slice(-2);
        const minutos = ('0' + fechaInicioObj.getMinutes()).slice(-2);
        const horaFormateada = `${horas}:${minutos}`;
        this.registroFormGroup.patchValue({
          fechaInicio: fechaInicioObj,
          horaInicio: horaFormateada
        });
      }
      if (this.intervDiferenciada.fechaFin) {
        const fechaFinObj = new Date(this.intervDiferenciada.fechaFin);
        const horas = ('0' + fechaFinObj.getHours()).slice(-2);
        const minutos = ('0' + fechaFinObj.getMinutes()).slice(-2);
        const horaFormateada = `${horas}:${minutos}`;
        this.registroFormGroup.patchValue({
          fechaFin: fechaFinObj,
          horaFin: horaFormateada
        });
      }
    })
  }

  obtenerActividadesIntervencion() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    // Asigna aquí el identificador del plan de tratamiento si es necesario para el filtrado
    paginacionRequest.filter = this.numIntr;

    this.planTratamientoService
      .obtenerActividadesTratamiento(paginacionRequest, '')
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<ActividadIntervencionDTO>>
        ) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }
          // Supongamos que tienes una lista y dataSource configurados para las actividades
          this.listaActividades = response.data.data;
          this.dataSourceActividades.data = this.listaActividades;
          this.dataSourceActividades.paginator = this.paginator;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  aniadirActividadIntervencion() {
    const dialogRef = this.dialog.open(ActividadIntervencionDialogComponent, {
      disableClose: true,
      width: '600px',
      data: {  } // pasa datos necesarios al modal
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Al cerrar el modal con resultado, recargar la tabla con los nuevos datos.
        this.obtenerActividadesIntervencion(); // Llama al método para refrescar la tabla
      }
    });
  }

  editarActividadIntervencion(actividad: ActividadIntervencionDTO) {
    const dialogRef = this.dialog.open(ActividadIntervencionDialogComponent, {
      disableClose: true,
      width: '600px',
      data: { actividad: actividad, frecuencia: this.listaTipoFrecuencia }
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Al cerrar el modal con resultado, recargar la tabla con los nuevos datos.
        this.obtenerActividadesIntervencion(); // Llama al método para refrescar la tabla
      }
    });
  }
  
  irAConsolidarActividad(fila: ActividadIntervencionDTO) {
    console.log('intervencion dif', fila);
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/seguimiento-actividades`], { queryParams: 
      {numDoc: this.numDoc, numInterv: this.numIntr, numActividad: fila.idActividadIntervencion } })
  }

  eliminarActividadIntervencion(actividad: ActividadIntervencionDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamientoService.eliminarActividadIntervencion(actividad.idActividadIntervencion, 'nemonicoMenu').subscribe({
              next: (response) => {
                if (response.exito) {
                  this.obtenerActividadesIntervencion();
                } else {
                  this.dialogMensajeService.mensajeErrorConTitulo(
                    response.titulo,
                    response.mensaje
                  )
                }
              },
              error: (error) => {
                this.planTratamientoService.checkError(error);
                console.error('Error en la solicitud:', error);
              }
            });
          }
        }
      }
    )
    
    
  }

  salir(){
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar`], { queryParams: 
      {numDoc: this.numDoc } })
  }

  volver() {
    this._location.back();
  }

  
}
