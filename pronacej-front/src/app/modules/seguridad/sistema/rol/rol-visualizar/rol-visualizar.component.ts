import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { MatTableModule } from '@angular/material/table';
import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { AccionCustom, AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import moment from "moment";
import { environment } from 'environments/environment.development';
import { MatIconModule } from '@angular/material/icon';
import { NavigationService } from 'app/core/navigation/navigation.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { ActivatedRoute, Router } from '@angular/router';
@Component({
  selector: 'app-rol-visualizar',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule
  ],
  templateUrl: './rol-visualizar.component.html',
  styleUrl: './rol-visualizar.component.scss'
})
export class RolVisualizarComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ROL;

  listaDeRoles: CreacionDeRol[] = [];
  dataSource: CdkTableDataSourceInput<CreacionDeRol>;

  @Output() editarRolEvent = new EventEmitter<CreacionDeRol>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    codigo: "Código",
    nombre: "Nombre",
    descripcion: "Descripción",
    diasExpiracionPassword: "Días expiración password",
    esRolPorDefecto: "Rol por defecto",
    esSuperRol: "Super rol"
  };

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.obtenerRoles();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(creacionDeRol: CreacionDeRol) {
    let action = (creacionDeRol.bloqueadoRelacion ? "Desbloquear" : "Bloquear");
    let ref = this.accionesSheet.open(AccionesUsuarioComponent,
      {
        data: {
          mostrar: true,
          textAccion: action,
          keyAccion: action
        }
      }
    );

    ref.afterDismissed().subscribe(
      {
        next: (result: "editar" | "eliminar" | "Desbloquear" | "Bloquear") => {
          if (result == "editar") {
            this.editarRolEvent.emit(creacionDeRol);
          } else if (result == "eliminar") {
            this.eliminarRol(creacionDeRol);
          }
          else if (result == "Desbloquear" || result == "Bloquear") {
            this.bloquearODesbloquearRol(creacionDeRol);
          }
        }
      }
    );
  }

  eliminarRol(creacionDeRol: CreacionDeRol) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + creacionDeRol.nombre + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el rol..");
            this.authSerguridadServicio.eliminarRol(creacionDeRol).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerRoles();
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

  bloquearODesbloquearRol(creacionDeRol: CreacionDeRol) {
    let text = (creacionDeRol.bloqueadoRelacion ? "\"desbloquear\"" : "\"bloquear\"");
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de " + text +
      " a: \"" + creacionDeRol.nombre + "\".",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let text2 = (creacionDeRol.bloqueadoRelacion ? "\"Desbloqueando\"" : "\"Bloqueando\"");

            let load = this.dialogMensajeService.mensajeLoading(text2 + " el rol..");

            creacionDeRol.bloqueadoRelacion = !creacionDeRol.bloqueadoRelacion
            this.authSerguridadServicio.bloquearRol(creacionDeRol, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerRoles();
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

  obtenerRoles() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;

    this.authSerguridadServicio.obtenerRolesValidos(paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaDeRoles = response.data.data;
          this.dataSource = this.listaDeRoles;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          console.log(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerRoles();
  }
}
