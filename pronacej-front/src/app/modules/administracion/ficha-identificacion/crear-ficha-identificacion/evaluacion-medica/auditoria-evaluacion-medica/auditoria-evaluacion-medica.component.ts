import { Component } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-auditoria-evaluacion-medica',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './auditoria-evaluacion-medica.component.html',
  styleUrl: '../crear-evaluacion-medica/crear-evaluacion-medica.component.scss'
})
export class AuditoriaEvaluacionMedicaComponent {

}
