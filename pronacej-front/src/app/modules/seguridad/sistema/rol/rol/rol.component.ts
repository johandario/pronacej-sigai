import { Component, OnInit, ViewChild } from '@angular/core';
import { RolCrearEditarComponent } from '../rol-crear-editar/rol-crear-editar.component';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';
import { RolVisualizarComponent } from '../rol-visualizar/rol-visualizar.component';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-rol',
  standalone: true,
  imports: [
    RolCrearEditarComponent,
    MatExpansionModule,
    RolVisualizarComponent
  ],
  templateUrl: './rol.component.html',
  styleUrl: './rol.component.scss'
})
export class RolComponent implements OnInit {

  esEdicion = false;

  @ViewChild("rolCrearComp") rolCrearComp: RolCrearEditarComponent;
  @ViewChild("visulizacionMatExpComp") visulizacionMatExpComp: MatExpansionPanel;
  @ViewChild("creacionComp") creacionComp: MatExpansionPanel;
  @ViewChild("visulizacionComp") visulizacionComp: RolVisualizarComponent;
  modo: number = 3; // 1 = Crear, 2 = Editar, 3 = Visualiza

  constructor(private router: Router, 
    private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.url.subscribe(urlSegments => {
        const url = urlSegments.map(segment => segment.path).join('/');
        if (url.includes('crear')) {
            this.modo = 1; // Modo Crear
        } else if (url.includes('editar')) {
            this.modo = 2; // Modo Editar
        } else {
            this.modo = 3; // Modo Visualizar
        }
    });
  }

  irACrearRol() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarRolEvent(creacionDeRolEditar: CreacionDeRol) {
    console.log("entra editarRolEvent");
    this.router.navigate(['editar'], { relativeTo: this.route });
    this.esEdicion = true;
    // this.visulizacionMatExpComp.close();
    // this.creacionComp.open();    
    this.rolCrearComp.empezarEdicion(creacionDeRolEditar);
  }
  
  completoOperacion(estado: boolean) {
    if (estado) {
      this.visulizacionComp.obtenerRoles();
    }
  }

  canceloEdicion(edicion: Boolean) {
    this.esEdicion = !edicion;
  }
}
