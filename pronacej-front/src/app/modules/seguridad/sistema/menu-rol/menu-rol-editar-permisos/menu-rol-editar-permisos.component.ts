import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { environment } from 'environments/environment.development';
import { AngularDualListBoxModule } from 'angular-dual-listbox';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { AdministrarMenuRolRequest } from 'app/core/model/request/administrarMenuRolRequest.model';


@Component({
  selector: 'app-menu-rol-editar-permisos',
  standalone: true,
  imports: [
    MatButtonModule,
    AngularDualListBoxModule
  ],
  templateUrl: './menu-rol-editar-permisos.component.html',
  styleUrl: './menu-rol-editar-permisos.component.scss'
})

export class MenuRolEditarPermisosComponent implements OnInit {
  
  rolEdicion: CreacionDeRol;

  esEdicion = false;

  listRoles: RolDTO[] = [];

  @Input() menusTotales: MenuDTO[] = [

  ];

  // @Output() actualizacionExitosa: EventEmitter<DefaultRespuesta<any>> = new EventEmitter();
  format: any;

  menusElegidos: MenuDTO[] = [];

  @Output() completoOperacion = new EventEmitter<boolean>();

  constructor(
    private authSeguridadServicio: AuthSerguridadServicio,
    private rolService: RolService,
    private dialogMensajeService: DialogMensajeService
  ) {
    this.format = {
      add: 'Agregar', remove: 'Remover',
      all: 'Seleccionar todo',
      none: 'Deseleccionar todo', direction: 'left-to-right',
      draggable: false, locale: undefined
    }
  }

  ngOnInit(): void {
    this.obtenerRoles();
  }

  ejecutarAccion() {
    let request: AdministrarMenuRolRequest = new AdministrarMenuRolRequest;
    request.rol = this.rolEdicion;
    request.listaMenus = this.menusElegidos;
    let load = this.dialogMensajeService.mensajeLoading("Guardando los permisos..");
    this.authSeguridadServicio.crearRelacionMenusRol(request, etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe(
      {
        next: (response: RespuestaPorDefecto<boolean>) => {
          if (!environment.production) {
            console.log(response);
          }
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo,
              response.mensaje);
          }
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          load.close();
          this.rolEdicion=null;
          this.menusElegidos = [];
          this.completoOperacion.emit(true);
        },
        error: (error: any) => {
          load.close();
          this.authSeguridadServicio.checkError(error);
        }
      }
    );
  }

  empezarEdicion(creacionDeRolEditar: CreacionDeRol) {
    this.esEdicion = true;
    this.rolEdicion = creacionDeRolEditar;
    this.menusElegidos = [];
    this.obtenerMenusAccesiblesPorRol();
  }

  obtenerMenusAccesiblesPorRol() {
    let load = this.dialogMensajeService.mensajeLoading("Cargando data...");
    this.authSeguridadServicio.obtenerMenusAccesiblesPorRol(this.rolEdicion, etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe(
      {
        next: (resp: RespuestaPorDefecto<MenuDTO[]>) => {
          load.close();
          if (!environment.production) {
            console.log(resp);
          }

          if (!resp.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(resp.titulo,
              resp.mensaje);
              this.menusElegidos = [];
              return ;
          }
          this.menusElegidos = resp.data;
        },
        error: (error: any) => {
          load.close();
          this.rolService.checkError(error);
        }
      }
    );
  }
  
  obtenerRoles() {
    let load = this.dialogMensajeService.mensajeLoading("Cargando data...");
    this.rolService.obtenerRoles(etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe(
      {
        next: (resp: RespuestaPorDefecto<RolDTO[]>) => {
          load.close();
          if (!environment.production) {
            console.log(resp);
          }

          if (!resp.exito) {
            this.dialogMensajeService.mensajeError(resp.mensaje);
            return;
          }

          this.listRoles = resp.data;
        },
        error: (error: any) => {
          load.close();
          this.rolService.checkError(error);
        }
      }
    );
  }

  mostrar(object: MenuDTO) {
    return object.title + " - " + (object.mostrarEnFront ? "visible" : "no visible");
  }
}
