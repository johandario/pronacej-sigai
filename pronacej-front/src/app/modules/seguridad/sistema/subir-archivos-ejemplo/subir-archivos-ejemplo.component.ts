import { Component, OnInit, ViewChild } from '@angular/core';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { DocumentoService } from 'app/core/services/documento.service';

@Component({
  selector: 'app-subir-archivos-ejemplo',
  standalone: true,
  imports: [
    SubidaDeDocumentosComponent
  ],
  templateUrl: './subir-archivos-ejemplo.component.html',
  styleUrl: './subir-archivos-ejemplo.component.scss'
})
export class SubirArchivosEjemploComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_AUDITORIAS_SISTEMA;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  private documentosSubidos: DocumentoSubido[] = [];

  @ViewChild("subidaDeDocumentosComp") subidaDeDocumentosComp: SubidaDeDocumentosComponent;

  constructor(private dialogMensajeService: DialogMensajeService,
    private documentoService: DocumentoService,
    private catalogoService: CatalogoService
  ) { }

  ngOnInit(): void {
    this.obtenerTiposDeDocumentos();
  }

  private obtenerTiposDeDocumentos() {
    this.catalogoService.obtenerHijos(etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          let tiposCat = response.data;
          for (let i = 0; tiposCat.length > i; i++) {
            let cat = tiposCat[i];
            let tipoDoc = cat as TipoDeDocumento;
            tipoDoc.requerido = cat.nemonico != etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS;

            this.tiposDeDocumentosSistema.push(tipoDoc);
          }
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  async subirArchivosEvent(documentos: DocumentoSubido[]) {
    if (documentos && documentos.length > 0) {

      let load = this.dialogMensajeService.mensajeLoading("Subiendo los archivos..");
      this.documentosSubidos = this.documentosSubidos.concat(documentos);

      for (let i = 0; documentos.length > i; i++) {
        let documento = documentos[i];
        try {
          let resp = await this.subirDocumento(documento);
          if (!resp.exito) {
            load.close();
            this.documentoService.checkError(resp);
          }
        } catch (ex: any) {
          console.error(ex);
          load.close();
          this.dialogMensajeService.mensajeError("Ha ocurrido el siguiente error: "
            + ex.toString()
          );
        }
      }

      load.close();
    }

  }

  private async subirDocumento(documento: DocumentoSubido): Promise<RespuestaPorDefecto<DocumentoDTO>> {
    let promise = new Promise<RespuestaPorDefecto<DocumentoDTO>>(
      (resol, err) => {
        this.documentoService.subirDocumento(documento.documento,
          documento.documentoDTO, this.nemonicoMenu
        ).subscribe(
          {
            next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
              resol(response);
            },
            error: (error: any) => {
              err(error);
            }
          }
        );
      }
    );

    return promise;
  }
}
