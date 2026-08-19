import { Component, inject, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';

@Component({
  selector: 'app-subida-de-foto-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    SubidaDeDocumentosComponent
  ],
  templateUrl: './subida-de-foto-dialog.component.html',
  styleUrl: './subida-de-foto-dialog.component.scss'
})
export class SubidaDeFotoDialogComponent {

  public dataDialog = inject<
    {
      tiposDeDocumentos: TipoDeDocumento[]
    }>(MAT_DIALOG_DATA);
  acceptFiles: string[] = ["image/png",
    "image/jpg", "image/jpeg"]

  @ViewChild("subidaDocsComp") subidaDocsComp: SubidaDeDocumentosComponent;

  constructor(private refDialog: MatDialogRef<SubidaDeFotoDialogComponent>,
    private dialogMensajeService: DialogMensajeService
  ) { }

  cancelar() {
    let docs = this.subidaDocsComp?.documentosSubidos;
    if (docs?.length > 0) {
      let conf = this.dialogMensajeService.mensajeConConfirmacion(
        "Estas seguro de cancelar, actualmentes tienes: " +
        docs.length + " documento subidos que se perderán",
        "Deseas continuar?"
      );

      conf.afterClosed().subscribe(
        {
          next: (result: "confirmed" | "canceled") => {
            if (result == "confirmed") {
              this.refDialog.close();
            }
          }
        }
      );
    } else {
      this.refDialog.close();
    }
  }

  subirDocumentos() {
    let docs = this.subidaDocsComp?.documentosSubidos;
    if (docs?.length > 0) {
      let conf = this.dialogMensajeService.mensajeConConfirmacion(
        "Se van a subir un total de: " + docs.length + " documentos.",
        "Deseas continuar?"
      );

      conf.afterClosed().subscribe(
        {
          next: (result: "confirmed" | "canceled") => {
            if (result == "confirmed") {
              this.refDialog.close(
                docs
              );
            }
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("Debes de cargar al menos 1 documento para continuar");
    }
  }
}
