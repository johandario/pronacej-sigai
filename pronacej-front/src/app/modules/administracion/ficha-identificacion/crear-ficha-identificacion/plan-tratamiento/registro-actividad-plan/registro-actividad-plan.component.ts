import { CommonModule, DatePipe } from '@angular/common';
import { Component, LOCALE_ID, OnInit, Pipe, PipeTransform, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PlanTratamientoIndDTO, PlanTratamientoIndIntervDTO, PlanTratamientoSeguimientoDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import moment from 'moment';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { MatDialog } from '@angular/material/dialog';
import { ModalEditaRegistroComponent } from './modal-edita-registro/modal-edita-registro.component';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';

registerLocaleData(localeEs);

@Component({
  selector: 'app-registro-actividad-plan',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    FormsModule,
    MatSelectModule,
    MatDatepickerModule,
    MatInputModule,    
    MatIconModule,
    CommonModule        
  ],
  providers: [
    provideNativeDateAdapter(),
    { provide: LOCALE_ID, useValue: 'es' }
  ],
  templateUrl: './registro-actividad-plan.component.html',
  styleUrl: './registro-actividad-plan.component.scss'
})
export class RegistroActividadPlanComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  editaFila: boolean = false;

  keyLabelsTable: any = {    
    acciones: "",
    actividad: "Actividad",    
    fecha: "Fecha",    
    horaInicio: "Hora de Inicio",
    horaFin: "Hora de Fin",
    observaciones: "Observaciones"
  };

  planTratamiento: PlanTratamientoIndDTO;
  listaSeguimiento: PlanTratamientoSeguimientoDTO[];
  intervDiferenciada: PlanTratamientoIndIntervDTO[]; 

  displayedColumns: string[] = ['acciones', 'fecha', 'horaInicio', 'horaFin', 'observaciones'];
  dataSourceEspecif: MatTableDataSource<PlanTratamientoSeguimientoDTO>;

  registroFormGroup = this.fb.group({
    actividad: [null],
    fecha: [new Date()],
    horaInicio: [null],
    horaFin: [null],
    observaciones: [null],
    
  })

  constructor(
    private fb: FormBuilder,
    private dateAdapter: DateAdapter<any>,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private planTratamientoService: PlanTratamientoService,
  ) {
    this.dateAdapter.setLocale('es');
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const numDoc = params['numDoc'];
      if (numDoc) {
        this.planTratamientoService.obtenerPlanTratamientoPorId(numDoc, '').subscribe(item => {
          this.planTratamiento = item.data;
          this.intervDiferenciada = this.planTratamiento.intervDiferenciada;
          this.obtenerPlanesSeguimiento();
        })
      }
    });
  }

  obtenerPlanesSeguimiento() {
    let paginacionRequest = new PaginacionRequest();
          paginacionRequest.size = this.size;
          paginacionRequest.page = this.page;
          paginacionRequest.tokenIdentificador = this.planTratamiento.tokenIdentificador;
          this.planTratamientoService.obtenerSeguimientos(paginacionRequest, '').subscribe({
            next: (response: RespuestaPorDefecto<PaginacionResponse<PlanTratamientoSeguimientoDTO>>) => {
              if (!environment.production) {
                console.log(response);
              }
    
              if (!response.exito) {
                this.planTratamientoService.checkError(response);
                return;
              }
              this.listaSeguimiento = response.data.data;
              this.dataSourceEspecif = new MatTableDataSource(this.listaSeguimiento);
              this.totalItems = response.data.totalItems;
            },
            error: (error: any) => {
              this.planTratamientoService.checkError(error);
            }
          })
  }
 
  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  aniadirRegistro() {
    const dialogRef = this.dialog.open(ModalEditaRegistroComponent, {
      data: { intervDiferenciada: this.intervDiferenciada },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        const planSeguimiento: PlanTratamientoSeguimientoDTO = result;
        this.planTratamientoService.crearSeguimiento(planSeguimiento, '').subscribe(
          {
            next: (response: RespuestaPorDefecto<PlanTratamientoSeguimientoDTO>) => {
              
              if (!response.exito) {
                this.planTratamientoService.checkError(response);
    
                return;
              }                  
              this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
              this.obtenerPlanesSeguimiento();
            },
            error: (error: any) => {
              this.planTratamientoService.checkError(error);
            }
          }
        )
      }
    })
  }

  editarRegistro(fila: PlanTratamientoSeguimientoDTO) {
    const dialogRef = this.dialog.open(ModalEditaRegistroComponent, {
      data: { 
        intervDiferenciada: this.intervDiferenciada,
        fila: fila,
      },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        fila.actividad = result.actividad;
        fila.fecha = result.fecha;
        fila.horaInicio = result.horaInicio;
        fila.horaFin = result.horaFin;
        fila.observaciones = result.observaciones;
        fila.esEdicion = true;
        this.planTratamientoService.crearSeguimiento(fila, '').subscribe(
          {
            next: (response: RespuestaPorDefecto<PlanTratamientoSeguimientoDTO>) => {
              
              if (!response.exito) {
                this.planTratamientoService.checkError(response);
    
                return;
              }                  
              this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
              this.obtenerPlanesSeguimiento();
            },
            error: (error: any) => {
              this.planTratamientoService.checkError(error);
            }
          }
        )
      }
    })
  }

  eliminarPlanSeguimiento(planSeguimiento: PlanTratamientoSeguimientoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro? Esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.planTratamientoService.eliminarSeguimiento(planSeguimiento, '').subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerPlanesSeguimiento();
                },
                error: (error: any) => {
                  load.close();

                  this.planTratamientoService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }
  
  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
  }
}