import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { NotificacionDTO } from 'app/core/model/both/ia/notificacionDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { NotificacionService } from 'app/core/services/notificacion.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-notificaciones-ver',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    TablaListaComponent
  ],
  templateUrl: './notificaciones-ver.component.html',
  styleUrl: './notificaciones-ver.component.scss'
})
export class NotificacionesVerComponent {

  esEdicion: boolean = false;
  uuid_fp: string = "";
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_NOTIFICACIONES;
  titulo: string = "Notificacion";
  listaNotificaciones: NotificacionDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de Envío",
    destinatarios: "Enviado a",
    asunto: "Asunto",
    medio: "Medio"
  };

  constructor(
    private notificacionService: NotificacionService,
    private dialogMensajeService: DialogMensajeService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private router: Router,
    private route: ActivatedRoute,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_NOTIFICACIONES"
    );
    this.uuid_fp = this.route.snapshot.paramMap.get('uuid_fp');

    if (this.uuid_fp) {
      this.esEdicion = true;
      this.obtenerNotificacionesPorToken(this.uuid_fp);
    }
    else
      this.esEdicion = false;
  }

  obtenerNotificacionesPorToken(uuid_fp: string) {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = uuid_fp;

    this.notificacionService.obtenerNotificacionesPorToken(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<NotificacionDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaNotificaciones = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.notificacionService.obtenerNotificacionesPorToken(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<NotificacionDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  refrescar() {
    if (this.esEdicion)
      this.obtenerNotificacionesPorToken(this.uuid_fp);
  }

  nuevaNotificacion() {
    this.router.navigate(['crear'], { state: { listaPrev: "true" }, relativeTo: this.route });
  }

  verNotificacion(notificacionDTO: NotificacionDTO) {
    this.router.navigate(['ver'], { state: { listaPrev: "true", item: notificacionDTO }, relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    if (this.esEdicion)
      this.obtenerNotificacionesPorToken(this.uuid_fp);
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

    if (this.esEdicion)
      this.obtenerNotificacionesPorToken(this.uuid_fp);
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    if (this.esEdicion)
      this.obtenerNotificacionesPorToken(this.uuid_fp);
  }

}
