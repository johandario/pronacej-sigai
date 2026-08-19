import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { InformeTecnicoSustentatorioComponent } from '../informe-tecnico-sustentatorio/informe-tecnico-sustentatorio.component';
import { InformeSeguimientoComponent } from '../informe-seguimiento/informe-seguimiento.component';
import { TabService } from 'app/core/services/tab.service';
import { InformeEgresoPiiComponent } from '../informe-egreso-pii/informe-egreso-pii.component';
import { Router } from '@angular/router';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-programa-intervencion-intensiva',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    ReactiveFormsModule,
    InformeTecnicoSustentatorioComponent,
    InformeSeguimientoComponent,
    InformeEgresoPiiComponent,
  ],
  templateUrl: './programa-intervencion-intensiva.component.html',
  styleUrl: './programa-intervencion-intensiva.component.scss'
})
export class ProgramaIntervencionIntensivaComponent {
  tituloPantalla = 'informe técnico sustentatorio';
  indiceTabSeleccionado: number = 0;

  constructor(
    private tabService: TabService,
    public router: Router,  // No traducir este componente, porque probocará que no se seleccione en el menu latera
    private authSerguridadServicio: AuthSerguridadServicio,
  ) {}

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_PROGRAMA_DE_INTERVENCION_INTENSIVA"
    );
    this.tabService.tabIndex$.subscribe(indice => {
      this.indiceTabSeleccionado = indice;
    });
  }

  onTabChange(event: any): void {
    const tabIndex = event.index;
    
    switch (tabIndex) {
      case 0:
        this.tituloPantalla = 'Informe técnico sustentatorio';
        break;
      case 1:
        this.tituloPantalla = 'Seguimiento de informe';
        break;
      case 2:
        this.tituloPantalla = 'Informe de Egreso PII';
        break;
      default:
        this.tituloPantalla = '';
    }
  }

  cambiarPestana(indice: number) {
    this.indiceTabSeleccionado = indice;
  }
}