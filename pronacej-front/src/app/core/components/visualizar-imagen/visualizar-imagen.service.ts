import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VisualizarImagenCompComponent } from './visualizar-imagen-comp/visualizar-imagen-comp.component';

@Injectable({
  providedIn: 'root'
})
export class VisualizarImagenService {

  constructor(private matDialog: MatDialog) { }

  abrirVista(base64Encoded: string, titulo: string) {

    let dialogRef = this.matDialog.open(
      VisualizarImagenCompComponent,
      {
        panelClass: ["w-full", "h-4/5"],
        disableClose: true
      }
    );

    dialogRef.componentInstance.titulo = titulo;
    dialogRef.componentInstance.base64Img = base64Encoded;

    return dialogRef;
  }
}
