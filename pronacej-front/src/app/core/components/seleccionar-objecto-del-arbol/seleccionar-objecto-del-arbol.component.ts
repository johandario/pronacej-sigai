import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { ObjectoArbol } from './ObjectoArbol.model';
import { MatDialog } from '@angular/material/dialog';
import { SeleccionarModalArbolComponent } from './seleccionar-modal-arbol/seleccionar-modal-arbol.component';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-seleccionar-objecto-del-arbol',
  standalone: true,
  imports: [
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './seleccionar-objecto-del-arbol.component.html',
  styleUrl: './seleccionar-objecto-del-arbol.component.scss'
})
export class SeleccionarObjectoDelArbolComponent<T> {

  @Input({ required: true }) objectosArbol: ObjectoArbol<T>[];
  @Input({ required: true }) textoBoton: string = "";
  @Input({ required: true }) textoObjeto: string = "";
  @Input({ required: true }) objectoArbolElegido: ObjectoArbol<T>;
  @Output() seleccionoObjeto = new EventEmitter<T>;

  readonly dialog = inject(MatDialog);

  protected abrirSeleccion() {
    let ref = this.dialog.open(SeleccionarModalArbolComponent<T>,
      {
        data: {
          objetosArbol: this.objectosArbol,
          titulo: this.textoBoton
        },
        disableClose: true
      }
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: ObjectoArbol<T>) => {
          this.seleccionoObjeto.emit(resp?.data);
          this.objectoArbolElegido = resp;
        }
      }
    );
  }
}
