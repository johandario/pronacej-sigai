import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-falta-verificacion',
  standalone: true,
  imports: [],
  templateUrl: './falta-verificacion.component.html',
  styleUrl: './falta-verificacion.component.scss'
})
export class FaltaVerificacionComponent {

  constructor(private _location: Location,
    private router: Router
  ) { }
  regresar() {
    this.router.navigate(['/home']);
  }

}
