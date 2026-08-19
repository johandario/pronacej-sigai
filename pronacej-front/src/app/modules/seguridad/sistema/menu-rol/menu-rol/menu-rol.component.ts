import { Component, OnInit, ViewChild } from '@angular/core';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';
import { MenuRolVisualizarComponent } from '../menu-rol-visualizar/menu-rol-visualizar.component';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { MenuRolEditarPermisosComponent } from '../menu-rol-editar-permisos/menu-rol-editar-permisos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { MenuService } from 'app/modules/seguridad/services/menu.service';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { environment } from 'environments/environment.development';

@Component({
  selector: 'app-menu-rol',
  standalone: true,
  imports: [
    MenuRolEditarPermisosComponent,
    MatExpansionModule,
    MenuRolVisualizarComponent
  ],
  templateUrl: './menu-rol.component.html',
  styleUrl: './menu-rol.component.scss'
})
export class MenuRolComponent implements OnInit{
  
  esEdicion = false;
  menusTotales: MenuDTO[] = [];

  @ViewChild("menuRolEditarComp") menuRolEditarComp: MenuRolEditarPermisosComponent;
  @ViewChild("visulizacionMatExpComp") visulizacionMatExpComp: MatExpansionPanel;
  @ViewChild("edicionComp") edicionComp: MatExpansionPanel;
  @ViewChild("visulizacionComp") visulizacionComp: MenuRolVisualizarComponent;

  constructor(
    private menuService: MenuService,
    private dialogMensajeService: DialogMensajeService
  ) { }

  ngOnInit(): void {
    this.obtenerMenusPorEmpresa();
  }

  editarRolEvent(creacionDeRolEditar: CreacionDeRol) {
    this.esEdicion = true;
    this.visulizacionMatExpComp.close();
    this.edicionComp.open();
    this.menuRolEditarComp.empezarEdicion(creacionDeRolEditar);
  }
  
  completoOperacion(estado: boolean) {
    this.visulizacionMatExpComp.open();
    this.edicionComp.close();
    if (estado) {
      this.visulizacionComp.obtenerRoles();
    
    }
  }

  canceloEdicion(edicion: Boolean) {
    this.esEdicion = !edicion;
  }

  obtenerMenusPorEmpresa() {
    this.menuService.obtenerMenusPorEmpresa(etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe(
        {
          next: (resp: RespuestaPorDefecto<MenuDTO[]>) => {
            if (!environment.production) {
              console.log(resp);
            }
  
            if (!resp.exito) {
              this.dialogMensajeService.mensajeError(resp.mensaje);
              return;
            }
  
            this.menusTotales = resp.data;
          },
          error: (error: any) => {
            this.menuService.checkError(error);
          }
        }
      );
  }
}
