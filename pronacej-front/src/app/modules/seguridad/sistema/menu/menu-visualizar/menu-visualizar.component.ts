import { Component, EventEmitter, inject, OnInit, Output, ViewChild } from '@angular/core';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-menu-visualizar',
  standalone: true,
  imports: [
    MatSortModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatPaginatorModule
  ],
  templateUrl: './menu-visualizar.component.html',
  styleUrl: './menu-visualizar.component.scss'
})
export class MenuVisualizarComponent implements OnInit {
  @ViewChild(MatSort) sort!: MatSort;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  listaDeMenus: MenuDTO[] = [];
  dataSource!: MatTableDataSource<MenuDTO>;

  @Output() editarMenuEvent = new EventEmitter<MenuDTO>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    realizaAuditoria: "Tiene Auditoria",
    title: "Titulos",
    subtitle: "Subtitulos",
  };

  constructor(
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet
  ) { }

  ngOnInit(): void {
    this.obtenerMenus();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(menuDTO: MenuDTO) {
    let ref = this.accionesSheet.open(AccionesUsuarioComponent, {
      data: {
        mostrar: true,
        textAccion: "",
        keyAccion: ""
      },
      disableClose: true
    });

    ref.afterDismissed().subscribe({
      next: (result: "editar" | "eliminar" | "Desbloquear" | "Bloquear") => {
        if (result == "editar") {
          this.editarMenuEvent.emit(menuDTO);
        } else if (result == "eliminar") {
          this.eliminarMenu(menuDTO);
        }
      }
    });
  }

  eliminarMenu(menuDTO: MenuDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + menuDTO.title + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            console.log(menuDTO);
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el menú..");
            this.authSerguridadServicio.eliminarMenu(menuDTO).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerMenus();
                },
                error: (error: any) => {
                  load.close();

                  this.authSerguridadServicio.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerMenus() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;

    this.authSerguridadServicio.obtenerMenusValidos(paginacionRequest, etiquetasModel.NEMONICO_MENU_MENU).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<MenuDTO>>) => {
        if (!environment.production) {
          console.log(response);
        }

        if (!response.exito) {
          this.authSerguridadServicio.checkError(response);
          return;
        }

        this.listaDeMenus = response.data.data;
        this.dataSource = new MatTableDataSource(this.listaDeMenus);
        setTimeout(() => {
          this.dataSource.sort = this.sort;
        }, 200);
        this.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        this.authSerguridadServicio.checkError(error);
      }
    });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerMenus();
  }
}
