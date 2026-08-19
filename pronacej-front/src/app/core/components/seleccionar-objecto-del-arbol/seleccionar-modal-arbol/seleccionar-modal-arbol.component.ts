import { Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { SeleccionarObjectoDelArbolComponent } from '../seleccionar-objecto-del-arbol.component';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { ObjectoArbol } from '../ObjectoArbol.model';
import { NestedTreeControl } from '@angular/cdk/tree';

@Component({
  selector: 'app-seleccionar-modal-arbol',
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    SeleccionarObjectoDelArbolComponent,
    MatTreeModule
  ],
  templateUrl: './seleccionar-modal-arbol.component.html',
  styleUrl: './seleccionar-modal-arbol.component.scss'
})
export class SeleccionarModalArbolComponent<T> implements OnInit {

  data = inject<any>(MAT_DIALOG_DATA);
  readonly dialogRef = inject(MatDialogRef<SeleccionarModalArbolComponent<T>>);
  dataSource = new MatTreeNestedDataSource<ObjectoArbol<T>>();
  controladorArbol = (node: ObjectoArbol<T>) => node.hijos ?? [];
  titulo: string;

  ngOnInit(): void {
    this.dataSource.data = this.data.objetosArbol;
    this.titulo = this.data.titulo;
  }

  childrenAccessor = (node: ObjectoArbol<T>) => node.hijos ?? [];

  hasChild = (_: number, node: ObjectoArbol<T>) => !!node.hijos && node.hijos.length > 0;

  seleccionoNodo(nodo: ObjectoArbol<T>) {
    this.dialogRef.close(
      nodo
    );
  }

}
