import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import moment from 'moment';
import { FlujoTrabajoService } from '../flujo-trabajo.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { Router } from '@angular/router';
import { ProcesoDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { InstanciaProcesoDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
@Component({
  selector: 'app-flujo-instancia',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule
  ],
  templateUrl: './flujo-instancia.component.html',
  styleUrl: './flujo-instancia.component.scss'
})
export class FlujoInstanciaComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  listaProcesos = [];
  procesosDataSource = new MatTableDataSource();

  tituloPantalla: string = "Nueva instancia de proceso";
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FLUJO;

  keyLabelsTable: any = {
    acciones: "",
    nombre: "Nombre",
    version: "Version",
    nemonico: "Nemónico"
  };

  constructor(
    private flujoTrabajoService: FlujoTrabajoService,
    private dialogMensajeService: DialogMensajeService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.obtenerProcesos();
  }

  obtenerProcesos() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;

    this.flujoTrabajoService.obtenerProcesos(paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<any>>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }

          this.listaProcesos = response.data.data;
          this.procesosDataSource = new MatTableDataSource(this.listaProcesos);
        },
        error: (error: any) => {
          this.flujoTrabajoService.checkError(error);
        }
      }
    );
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
  }

  nuevaInstancia(proceso: ProcesoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'A continuación se redirigirá a la pantalla de creación del documento',
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp === "confirmed") {
            this.router.navigate(['/flujo-trabajo/traslado'], {
              state: { proceso },
            });
          }
        }
      }
    );
  }

}
