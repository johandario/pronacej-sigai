import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-sin-permisos',
  standalone: true,
  imports: [ ],
  templateUrl: './sin-permisos.component.html',
  styleUrl: './sin-permisos.component.scss'
})
export class SinPermisosComponent {

  constructor(private _location: Location,
    private router: Router,
  ) { }

  regresar() {
    this.router.navigate(['/home']);
  }

}
