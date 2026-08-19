import { Component } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-mantenimiento',
  standalone: true,
  imports: [],
  templateUrl: './mantenimiento.component.html',
  styleUrl: './mantenimiento.component.scss'
})
export class MantenimientoComponent {

  constructor(private _location: Location) { }
  regresar() {
    this._location.back();
  }

}
