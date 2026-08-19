import { Component, ViewChild } from '@angular/core';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';
import { MenuCrearEditarComponent } from '../menu-crear-editar/menu-crear-editar.component';
import { MenuVisualizarComponent } from '../menu-visualizar/menu-visualizar.component';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    MenuCrearEditarComponent,
    MatExpansionModule,
    MenuVisualizarComponent

  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {

  esEdicion = false;

  @ViewChild("menuCrearComp") menuCrearComp: MenuCrearEditarComponent;
  @ViewChild("visualizacionMatExpComp") visualizacionMatExpComp: MatExpansionPanel;
  @ViewChild("creacionComp") creacionComp: MatExpansionPanel;
  @ViewChild("visualizacionComp") visualizacionComp: MenuVisualizarComponent;

  editarMenuEvent(menuDTO: MenuDTO) {
    this.esEdicion = true;
    this.visualizacionMatExpComp.close();
    this.creacionComp.open();
    this.menuCrearComp.empezarEdicion(menuDTO);
  }

  completoOperacion(estado: boolean) {
    if (estado) {
      this.visualizacionComp.obtenerMenus();
     
    }
  }

  canceloEdicion(edicion: boolean) {
    this.esEdicion = edicion;
  }

}
