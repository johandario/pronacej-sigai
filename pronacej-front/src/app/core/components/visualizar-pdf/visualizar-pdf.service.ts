import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VisualizarPdfCompComponent } from './visualizar-pdf-comp/visualizar-pdf-comp.component';

@Injectable({
  providedIn: 'root'
})
export class VisualizarPdfService {

  constructor(private matDialog: MatDialog) { }

  abrirVistaPdf(base64Encoded: string, titulo: string) {

    let dialogRef = this.matDialog.open(
      VisualizarPdfCompComponent,
      {
        panelClass: ["w-full", "h-4/5"],
        disableClose: true
      }
    );

    dialogRef.componentInstance.titulo = titulo;
    dialogRef.componentInstance.base64Encoded = base64Encoded;

    return dialogRef;
  }
}
