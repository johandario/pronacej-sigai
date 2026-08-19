import { Component } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { AuditoriaEvaluacionMedicaComponent } from '../auditoria-evaluacion-medica/auditoria-evaluacion-medica.component';
import { DocsEvaluacionMedicaComponent } from '../docs-evaluacion-medica/docs-evaluacion-medica.component';
import { FormEvaluacionMedicaComponent } from '../form-evaluacion-medica/form-evaluacion-medica.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-crear-evaluacion-medica',
  standalone: true,
  imports: [
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    FormEvaluacionMedicaComponent,
    DocsEvaluacionMedicaComponent,
    AuditoriaEvaluacionMedicaComponent
  ],
  templateUrl: './crear-evaluacion-medica.component.html',
  styleUrl: './crear-evaluacion-medica.component.scss'
})
export class CrearEvaluacionMedicaComponent {
  tituloPantalla = "Evaluación médica";


  constructor(
    private router: Router, private route: ActivatedRoute,
  ){}


  atras() {
    this.router.navigate(['../seguimiento'], { relativeTo: this.route });
  }
}
