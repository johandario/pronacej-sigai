import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule } from '@angular/material/dialog';
import { ContenidoCarpetaResponse } from 'app/core/model/response/ia/ContenidoCarpetaResponse.model';
import { MatMenuModule } from '@angular/material/menu';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DocumentoService } from 'app/core/services/documento.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { VisualizarPdfService } from 'app/core/components/visualizar-pdf/visualizar-pdf.service';
import { VisualizarImagenService } from 'app/core/components/visualizar-imagen/visualizar-imagen.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-detalles-archivo',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatMenuModule
  ],
  templateUrl: './detalles-archivo.component.html',
  styleUrl: './detalles-archivo.component.scss'
})
export class DetallesArchivoComponent {
  @Input() declare contenidoCarpetaResponse: ContenidoCarpetaResponse;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_DOCUMENTOS;

  constructor(private funcionesUtils: FuncionesUtils,
    private documentoService: DocumentoService,
    private dialogMensajeService: DialogMensajeService,
    private visualizarPdfService: VisualizarPdfService,
    private visualizarImagenService: VisualizarImagenService
  ) { }

  getFecha(date: Date) {
    return this.funcionesUtils.getLocalDate(date);
  }

  getTamanio(bytes: number) {
    return this.funcionesUtils.formatBytes(bytes);
  }

  accionDocumento(contenido: ContenidoCarpetaResponse, accion: "Descargar" | "Ver") {
    let load = this.dialogMensajeService.mensajeLoading("Cargando el documento: " +
      contenido.nombre
    );

    this.documentoService.obtenerDocumento(
      contenido.tokenIdentificadorDocumento,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: ArrayBuffer) => {
          load.close();
          const blob = new Blob([response], { type: contenido.tipo });

          if (accion == "Descargar") {
            const url = window.URL.createObjectURL(blob);
            window.open(url);
          } else if (accion == "Ver") {
            let base64Encoded = this.funcionesUtils.arrayBufferToBase64(response);
            if (contenido.tipo.includes("pdf")) {
              this.visualizarPdfService.abrirVistaPdf(
                base64Encoded, contenido.nombre
              );
            } else {
              this.visualizarImagenService.abrirVista(
                "data:image/png;base64," + base64Encoded, contenido.nombre
              );
            }
          }
        },
        error: (error: any) => {
          load.close();

          this.documentoService.checkError(error);
        }
      }
    );

  }

}
