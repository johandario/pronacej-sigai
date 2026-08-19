import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { SeguimientoPsicologicoDTO } from 'app/core/model/both/ia/seguimientoPsicologicoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SeguimientoService } from 'app/modules/administracion/services/seguimiento.service';

@Component({
  selector: 'app-seguimiento-psicologico-ver',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    TablaListaComponent
  ],
  templateUrl: './seguimiento-psicologico-ver.component.html',
  styleUrl: './seguimiento-psicologico-ver.component.scss'
})
export class SeguimientoPsicologicoVerComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOLOGICA;

  uuid_fp: string;
  tokenEncabezado: string;
  centro: JerarquiaDTO;

  listaSeguimientos: SeguimientoPsicologicoDTO[] = [];
  paginacionSeguimiento: Paginacion = new Paginacion();
  paginacionRequestSeguimiento: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsSeguimiento: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    nombreUsuarioCrea: "Usuario que registra",
    intervencionConcejeria: "Intervención/concejería",
    accionesRealizar: "Acciones a realizar",
    comentariosObservaciones: "Comentarios/observaciones"
  };

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private seguimientoService: SeguimientoService
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.centro = history.state.centro;
    this.tokenEncabezado = history.state.tokenEncabezado;

    this.obtenerSeguimientos();
  }

  obtenerSeguimientos() {
    this.paginacionRequestSeguimiento.size = this.paginacionSeguimiento.pageSize;
    this.paginacionRequestSeguimiento.page = this.paginacionSeguimiento.pageIndex;
    this.paginacionRequestSeguimiento.tokenIdentificador = this.tokenEncabezado;

    this.seguimientoService.obtenerSeguimientosPsicologicos(this.paginacionRequestSeguimiento, etiquetasModel.NEMONICO_MENU_ENCUESTA).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SeguimientoPsicologicoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaSeguimientos = response.data.data;
          this.paginacionSeguimiento.totalItems = response.data.totalItems;

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
    this.paginacionRequestSeguimiento.size = 100000;
    this.paginacionRequestSeguimiento.page = 0;
    this.paginacionRequestSeguimiento.tokenIdentificador = this.tokenEncabezado;

    this.seguimientoService.obtenerSeguimientosPsicologicos(this.paginacionRequestSeguimiento, etiquetasModel.NEMONICO_MENU_ENCUESTA).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SeguimientoPsicologicoDTO>>) => {

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

  agregarSeguimiento() {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      state: {
        tokenEncabezado: this.tokenEncabezado,
        centro: this.centro
      }
    });
  }

  verSeguimiento(psicologicoDTO: SeguimientoPsicologicoDTO) {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      state: {
        item: psicologicoDTO,
        centro: this.centro,
        esVisualizacion: true
      }
    });
  }

  eliminarSeguimiento(psicologicoDTO: SeguimientoPsicologicoDTO) {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el seguimiento? esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el seguimiento..");
            this.seguimientoService.eliminarSeguimientoPsicologico(psicologicoDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar el seguimiento. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerSeguimientos();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al guardar el seguimiento. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEventSeguimiento(pageEvent: PageEvent) {
    this.paginacionSeguimiento.pageSize = pageEvent.pageSize;
    this.paginacionSeguimiento.pageIndex = pageEvent.pageIndex;

    this.obtenerSeguimientos();
  }

  handleSortEventSeguimiento(event: Sort) {
    if (event.direction) {
      this.paginacionRequestSeguimiento.sort = event.active;
      this.paginacionRequestSeguimiento.direction = event.direction;
    }
    else {
      this.paginacionRequestSeguimiento.sort = null;
      this.paginacionRequestSeguimiento.direction = null;
    }

    this.obtenerSeguimientos();
  }

  handleSearchEventSeguimiento(filter: string) {
    this.paginacionRequestSeguimiento.filter = filter;

    this.obtenerSeguimientos();
  }

  regresar() {
    this.router.navigate(['../'], { relativeTo: this.route });
  }
}
