import { Component, ViewChild } from '@angular/core';
import { UsuarioCrearEditarComponent } from '../usuario-crear-editar/usuario-crear-editar.component';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';
import { UsuarioVisualizarComponent } from '../usuario-visualizar/usuario-visualizar.component';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';

@Component({
  selector: 'app-usuario',
  standalone: true,
  imports: [
    UsuarioCrearEditarComponent,
    MatExpansionModule,
    UsuarioVisualizarComponent
  ],
  templateUrl: './usuario.component.html',
  styleUrl: './usuario.component.scss'
})
export class UsuarioComponent {

  esEdicion = false;

  @ViewChild("usuarioCrearComp") usuarioCrearComp: UsuarioCrearEditarComponent;
  @ViewChild("visulizacionMatExpComp") visulizacionMatExpComp: MatExpansionPanel;
  @ViewChild("creacionComp") creacionComp: MatExpansionPanel;
  @ViewChild("visulizacionComp") visulizacionComp: UsuarioVisualizarComponent;

  editarUsuarioEvent(creacionDeUsuarioSistemaEditar: CreacionDeUsuarioSistema) {
    this.esEdicion = true;
    this.visulizacionMatExpComp.close();
    this.creacionComp.open();
    this.usuarioCrearComp.empezarEdicion(creacionDeUsuarioSistemaEditar);
  }
  
  completoOperacion(estado: boolean) {
    if (estado) {
      this.visulizacionComp.obtenerUsuarios();
    }
  }

  canceloEdicion(edicion: Boolean) {
    this.esEdicion = !edicion;
  }
}
