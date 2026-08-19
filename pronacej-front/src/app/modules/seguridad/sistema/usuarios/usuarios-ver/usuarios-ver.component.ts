import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
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
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { ReseteoDePasswordDTO } from 'app/core/model/both/seguridad/ReseteoDePasswordDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { ReseteoDeContraseniaRequest } from 'app/core/model/request/ReseteoDeContraseniaRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { RecaptchaV3Service } from 'app/modules/seguridad/services/recaptchav3.service';
import { ReseteoDeContraseniaService } from 'app/modules/seguridad/services/reseteoContrasenia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-usuarios-ver',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TablaListaComponent,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule
  ],
  templateUrl: './usuarios-ver.component.html',
  styleUrl: './usuarios-ver.component.scss'
})
export class UsuariosVerComponent {
  paginacion: Paginacion = new Paginacion();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;
  searchTerm: string = '';
  listaDeUsuarios: CreacionDeUsuarioSistema[] = [];

  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  @Output() editarUsuarioEvent = new EventEmitter<CreacionDeUsuarioSistema>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombres: "Nombres",
    apellidos: "Apellidos",
    email: "Email",
    userName: "Usuario",
    nombreRol: "Rol",
    numeroDeCelular: "No. de celular",
    numeroDeDocumento: "No. documento",
    telefono: "Teléfono",
    fechaCreacion: "Fecha de creación"
  };

  // idPantalla = etiquetasModel.ID_PANTALLA_USUARIO;

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private reseteoDeContraseniaService: ReseteoDeContraseniaService,
    private recaptchaV3Service: RecaptchaV3Service
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      this.nemonicoMenu
    );
    this.obtenerUsuarios();
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
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.authSerguridadServicio.obtenerUsuariosValidos(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeUsuarioSistema>>) => {
          if (!environment.production) {
            //console.log(response);
          }

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }

          this.listaDeUsuarios = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;

    this.authSerguridadServicio.obtenerUsuariosValidos(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeUsuarioSistema>>) => {

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  agregarUsuario() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarUsuario(creacionDeUsuarioSistema: CreacionDeUsuarioSistema) {
    this.router.navigate(['editar'], { state: { item: creacionDeUsuarioSistema }, relativeTo: this.route });
  }

  reestablecerPassword(creacionDeUsuarioSistema: CreacionDeUsuarioSistema): void {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de reestablecer la contraseña de: \"" + creacionDeUsuarioSistema.nombres + "\"?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.recaptchaV3Service.execute(
              etiquetasModel.ACCION_CREACION_RESETEO_PASSWORD
            ).subscribe(
              {
                next: (tokenRecapthcaV3: string) => {
                  let reseteoDeContraseniaRequest = new ReseteoDeContraseniaRequest();
                  reseteoDeContraseniaRequest.email = creacionDeUsuarioSistema.email
                  reseteoDeContraseniaRequest.recaptchaV3 = tokenRecapthcaV3;

                  this.reseteoDeContraseniaService.empezar(
                    reseteoDeContraseniaRequest,
                    this.nemonicoMenu
                  ).subscribe(
                    {
                      next: (response: RespuestaPorDefecto<ReseteoDePasswordDTO>) => {

                        if (!response.exito) {
                          this.dialogMensajeService.mensajeError(
                            'Error al iniciar el proceso de reestablecimiento de contraseña'
                          )
                          return;
                        }

                        this.dialogMensajeService.mensajeExitoso(
                          'Reestablecimiento', 'Se ha iniciado el reestablecimiento de contraseña. Informar al usuario.'
                        )

                      },
                      error: (error: any) => {
                        this.dialogMensajeService.mensajeError(
                          'Error al iniciar el proceso de reestablecimiento de contraseña'
                        )
                        this.reseteoDeContraseniaService.checkError(error);
                      }
                    }
                  );
                },
                error: (error: any) => {
                  this.dialogMensajeService.mensajeError(
                    'Error al iniciar el proceso de reestablecimiento de contraseña'
                  )
                }
              }
            );
          }
        }
      });
  }

  handlePageEvent(event: PageEvent) {
    this.paginacion.pageSize = event.pageSize;
    this.paginacion.pageIndex = event.pageIndex;
    this.obtenerUsuarios();
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
    this.obtenerUsuarios();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerUsuarios();
  }
}

