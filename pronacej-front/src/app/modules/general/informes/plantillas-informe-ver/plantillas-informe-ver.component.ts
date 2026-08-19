import { CommonModule, Location } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { PlantillaInformeDTO } from 'app/core/model/both/informe/plantillaInformeDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformeService } from '../../services/informe.service';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-plantillas-informe-ver',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    TablaListaComponent
  ],
  templateUrl: './plantillas-informe-ver.component.html',
  styleUrl: './plantillas-informe-ver.component.scss'
})
export class PlantillasInformeVerComponent {

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PLANTILLAS_INFORMES;
  titulo: string = "plantilla";
  listaPlantillas: PlantillaInformeDTO[] = [];

  informePrev: boolean = false;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;
  @Output() editarPlantillaEvent = new EventEmitter<PlantillaInformeDTO>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    nombre: "Nombre",
    descripcion: "Descripción",
    tipoCentro: "Tipo de centro",
  };

  constructor(private informeService: InformeService,
    private dialogMensajeService: DialogMensajeService,
    private location: Location,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.informePrev = history.state.informePrev;
    this.obtenerPlantillas();
  }

  eliminarPlantilla(plantillaInformeDTO: PlantillaInformeDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la plantilla? esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la plantilla..");
            this.informeService.eliminarPlantilla(plantillaInformeDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar la plantilla. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerPlantillas();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al eliminar la plantilla. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerPlantillas() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.informeService.obtenerListaPlantillas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlantillaInformeDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaPlantillas = response.data.data;
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

    this.informeService.obtenerListaPlantillas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlantillaInformeDTO>>) => {

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

  agregarPlantilla() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarPlantilla(plantillaInformeDTO: PlantillaInformeDTO) {
    this.router.navigate(['editar'], { state: { item: plantillaInformeDTO }, relativeTo: this.route });
  }

  regresar() {
    this.location.back();
  }

  visualizar(plantillaInformeDTO: PlantillaInformeDTO) {
    this.router.navigate(['visualizar'], { state: { item: plantillaInformeDTO }, relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerPlantillas();
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

    this.obtenerPlantillas();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerPlantillas();
  }
}
