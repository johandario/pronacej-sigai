import { Component, OnInit, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaUbicacionDTO } from 'app/core/model/both/fichaUbicacion.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FichaUbicacionService } from 'app/modules/seguridad/services/fichaUbicacion.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-ficha-ubicacion',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './ficha-ubicacion.component.html',
  styleUrl: './ficha-ubicacion.component.scss'
})
export class FichaUbicacionComponent implements OnInit {

  listaUbicaciones: FichaUbicacionDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_FICHA_UBICACIONES;
  uuid_fp!: string;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<FichaUbicacionDTO>;

  keyLabelsTable: any = {
    numero: 'No.',
    acciones: 'Acciones',
    fechaIngreso: 'Fecha de ingreso',
    ubicacionActual: 'Ubicación actual',
    atencionPrioritaria: 'Atención prioritaria',
    ingresoExpediente: 'Ingreso con expediente después de traslado',
    centroActualTexto: 'Centro',
    celdaActualTexto: 'Celda',
    observaciones: 'Observaciones',
  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private fichaUbicacionService: FichaUbicacionService,
    private dialogMensajeService: DialogMensajeService,
  ) {
  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerUbicaciones();
  }

  obtenerUbicaciones() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.fichaUbicacionService.obtenerListaPaginada(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaUbicacionDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.fichaUbicacionService.checkError(response);
            return;
          }

          this.listaUbicaciones = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.fichaUbicacionService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.fichaUbicacionService.obtenerListaPaginada(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaUbicacionDTO>>) => {

          if (!response.exito) {
            this.fichaUbicacionService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.fichaUbicacionService.checkError(error);
        }
      }
    );
  }

  verUbicacion(fichaUbicacion: FichaUbicacionDTO) {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      queryParams: { token: fichaUbicacion.tokenIdentificador, state: 'show' }
    });
  }

  editarUbicacion(fichaUbicacion: FichaUbicacionDTO) {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      queryParams: { token: fichaUbicacion.tokenIdentificador }
    });
  }

  agregarUbicacion() {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
    });
  }

  eliminarUbicacion(fichaUbicacion: FichaUbicacionDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'Estas seguro de eliminar el registro seleccionado, esta operacion es irreversible',
      'Deseas continuar?'
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: 'confirmed' | 'cancelled') => {
          if (resp == 'confirmed') {
            let load = this.dialogMensajeService.mensajeLoading('Eliminando el registro..');
            this.fichaUbicacionService.eliminar(fichaUbicacion, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);

                  if (!response.exito) {
                    return;
                  }

                  this.obtenerUbicaciones();
                },
                error: (error: any) => {
                  load.close();
                  this.fichaUbicacionService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerUbicaciones();
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

    this.obtenerUbicaciones();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerUbicaciones();
  }

}
