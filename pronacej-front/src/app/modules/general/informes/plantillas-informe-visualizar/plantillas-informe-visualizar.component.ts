import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { InformeComponent } from 'app/core/components/informe/informe.component';
import { PlantillaInformeDTO } from 'app/core/model/both/informe/plantillaInformeDTO.model';

@Component({
  selector: 'app-plantillas-informe-visualizar',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    InformeComponent
  ],
  templateUrl: './plantillas-informe-visualizar.component.html',
  styleUrl: './plantillas-informe-visualizar.component.scss'
})
export class PlantillasInformeVisualizarComponent {

  item: PlantillaInformeDTO;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit() {
    this.item = history.state.item;
  }

  regresar() {
    this.router.navigate(['../'], { relativeTo: this.route });
  }
}
