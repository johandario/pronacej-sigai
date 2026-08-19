import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AccionCustom } from './accionCustom.model';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-acciones-usuario',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ],
  templateUrl: './acciones-usuario.component.html',
  styleUrl: './acciones-usuario.component.scss'
})
export class AccionesUsuarioComponent {

  @Input() accionesCustom: AccionCustom[] = [];
  @Input() mostrarEliminar = true;

  @Output() accionEvent = new EventEmitter<"editar" | "eliminar" | string>();

  realizarAccion(accion: "editar" | "eliminar" | string) {
    this.accionEvent.emit(accion);
  }
}
