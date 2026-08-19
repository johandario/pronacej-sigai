import { Component, OnInit, ViewChild } from '@angular/core';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FlujoTrabajoService } from '../flujo-trabajo.service';
import { ProcesoDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-flujo-proceso',
  standalone: true,
  imports: [
    TablaListaComponent
  ],
  templateUrl: './flujo-proceso.component.html',
  styleUrl: './flujo-proceso.component.scss'
})
export class FlujoProcesoComponent implements OnInit {
  tituloPantalla: string = "Gestión de procesos";

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  listaProcesos: ProcesoDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FLUJO_PROCESOS;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    // idProceso: "ID",   
    nombre: "Nombre",
    fecCreacion: "Fecha Creación",
    version: "Versión",
    nemonico: "Nemónico"
  };

  constructor(
    private flujoTrabajoService: FlujoTrabajoService,
    private router: Router,
    private dialogMensajeService: DialogMensajeService,
  ) { }

  ngOnInit(): void {
    this.obtenerProcesos();
  }

  obtenerProcesos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.flujoTrabajoService.obtenerProcesos(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ProcesoDTO>>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }

          this.listaProcesos = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.flujoTrabajoService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;

    this.flujoTrabajoService.obtenerProcesos(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ProcesoDTO>>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.flujoTrabajoService.checkError(error);
        }
      }
    );
  }

  agregarProceso() {
    this.router.navigate(['/flujo-trabajo/admin-procesos/crear-editar']);
  }

  editarProceso(proceso: ProcesoDTO) {
    this.router.navigate(['/flujo-trabajo/admin-procesos/crear-editar'], { queryParams: { ID: proceso.tokenIdentificador } })
  }

  eliminarProceso(proceso: ProcesoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `Se eliminará el registro "${proceso.nombre}"`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {

            this.flujoTrabajoService.eliminarProceso(proceso, '').subscribe(
              {
                next: (response: RespuestaPorDefecto<ProcesoDTO>) => {

                  if (!response.exito) {
                    this.flujoTrabajoService.checkError(response);

                    return;
                  }
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                  this.obtenerProcesos();
                },
                error: (error: any) => {
                  this.flujoTrabajoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }

  // handlePageEvent(pageEvent: PageEvent) {
  //   this.size = pageEvent.pageSize;
  //   this.page = pageEvent.pageIndex;
  // }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerProcesos();
  }

  handleSortEvent(event: Sort) {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    }
    else {
      this.paginacionRequest.sort = null;
      this.paginacionRequest.direction = null;
    }

    this.obtenerProcesos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerProcesos();
  }
}
