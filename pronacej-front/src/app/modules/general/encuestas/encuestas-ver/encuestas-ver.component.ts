import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import moment from 'moment';

@Component({
  selector: 'app-encuestas-ver',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule,
    TablaListaComponent
  ],
  templateUrl: './encuestas-ver.component.html',
  styleUrl: './encuestas-ver.component.scss'
})
export class EncuestasVerComponent {

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ENCUESTAS;
  titulo: string = "evaluación";
  searchTerm: string = '';
  listaEncuestas: EncuestaDTO[] = [];

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;
  @Output() editarEncuestaEvent = new EventEmitter<EncuestaDTO>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    nombre: "Nombre de evaluación",
    descripcion: "Descripción",
    tipoCentro: "Tipo de centro"
  };

  constructor(private encuestaService: EncuestaService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.obtenerEncuestas();
  }

  eliminarEncuesta(encuestaDTO: EncuestaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la encuesta: \"" + encuestaDTO.nombre + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la evaluación..");
            this.encuestaService.eliminarEncuesta(encuestaDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerEncuestas();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerEncuestas() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.encuestaService.obtenerListaEncuestas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncuestaDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaEncuestas = response.data.data;
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

    this.encuestaService.obtenerListaEncuestas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncuestaDTO>>) => {

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

  agregarEncuesta() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarEncuesta(encuestaDTO: EncuestaDTO) {
    this.router.navigate(['editar'], { state: { tokenEncuesta: encuestaDTO.tokenIdentificador }, relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerEncuestas();
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

    this.obtenerEncuestas();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerEncuestas();
  }
}
