import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { MatTableModule } from '@angular/material/table';
import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import moment from "moment";
import { environment } from 'environments/environment';
import { MatIconModule } from '@angular/material/icon';
import etiquetasModel from 'app/core/etiquetas.model';
@Component({
  selector: 'app-usuario-visualizar',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule
  ],
  templateUrl: './usuario-visualizar.component.html',
  styleUrl: './usuario-visualizar.component.scss'
})
export class UsuarioVisualizarComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;

  listaDeUsuarios: CreacionDeUsuarioSistema[] = [];
  dataSource: CdkTableDataSourceInput<CreacionDeUsuarioSistema>;

  @Output() editarUsuarioEvent = new EventEmitter<CreacionDeUsuarioSistema>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombres: "Nombres",
    apellidos: "Apellidos",
    email: "Email",
    nombreRol: "Rol",
    numeroDeCelular: "No. de celular",
    numeroDeDocumento: "No. documento",
    telefono: "Teléfono",
    fechaCreacion: "Fecha de creación"
  };

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet
  ) { }

  ngOnInit(): void {
    this.obtenerUsuarios();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  eliminarUsuario(creacionDeUsuarioSistema: CreacionDeUsuarioSistema) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + creacionDeUsuarioSistema.nombres + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el usuario..");
            this.authSerguridadServicio.eliminarUsuario(creacionDeUsuarioSistema, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerUsuarios();
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

  bloquearODesbloquearUsuario(creacionDeUsuarioSistema: CreacionDeUsuarioSistema) {
    let text = (creacionDeUsuarioSistema.bloqueadoRelacion ? "\"desbloquear\"" : "\"bloquear\"");
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de " + text +
      " a: \"" + creacionDeUsuarioSistema.nombres + "\".",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let text2 = (creacionDeUsuarioSistema.bloqueadoRelacion ? "\"Desbloqueando\"" : "\"Bloqueando\"");

            let load = this.dialogMensajeService.mensajeLoading(text2 + " el usuario..");

            creacionDeUsuarioSistema.bloqueadoRelacion = !creacionDeUsuarioSistema.bloqueadoRelacion
            this.authSerguridadServicio.bloquearUsuario(creacionDeUsuarioSistema, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    this.authSerguridadServicio.checkError(resp);
                    return;
                  }

                  this.obtenerUsuarios();
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

  obtenerUsuarios() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;

    this.authSerguridadServicio.obtenerUsuariosValidos(paginacionRequest, etiquetasModel.NEMONICO_MENU_USUARIO).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeUsuarioSistema>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }

          this.listaDeUsuarios = response.data.data;
          this.dataSource = this.listaDeUsuarios;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerUsuarios();
  }
}
