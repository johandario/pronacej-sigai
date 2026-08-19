import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { FuseConfirmationDialogComponent } from '@fuse/services/confirmation/dialog/dialog.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@Component({
  selector: 'app-visualizar-pdf-comp',
  standalone: true,
  imports: [
    NgxExtendedPdfViewerModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './visualizar-pdf-comp.component.html',
  styleUrl: './visualizar-pdf-comp.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualizarPdfCompComponent implements OnInit {

  @Input({ required: true }) declare titulo: string;
  @Input({ required: true }) declare base64Encoded: string;

  private load: MatDialogRef<FuseConfirmationDialogComponent, any>;

  constructor(private dialogMensajeService: DialogMensajeService) { }

  ngOnInit(): void {
    if (this.base64Encoded?.includes("data:application/pdf;base64,")) {
      this.base64Encoded = this.base64Encoded.replace("data:application/pdf;base64,", "");
    }
  }

  onEvent(tipo: string, event: any) {

    if (tipo == "pdfLoadingStarts") {
      this.load = this.dialogMensajeService.mensajeLoading("Cargando vista del pdf");
    }

    if (tipo == "pdfLoadingFailed") {
      let error = event as Error;
      this.dialogMensajeService.mensajeError("No se pudo cargar el pdf debido a: " +
        error?.message
      );
    }

    if (tipo == "pdfLoaded") {
      this.load?.close();
    }
  }
}
