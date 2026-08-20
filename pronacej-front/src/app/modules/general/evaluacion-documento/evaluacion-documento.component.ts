import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { EncuestaService } from '../services/encuesta.service';
import { EvaluacionDocumentoDTO } from 'app/core/model/both/encuesta/evaluacionDocumentoDTO.model';
import { MatIconModule } from '@angular/material/icon';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-evaluacion-documento',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    SubidaDeDocumentosComponent
  ],
  templateUrl: './evaluacion-documento.component.html',
  styleUrl: './evaluacion-documento.component.scss'
})
export class EvaluacionDocumentoComponent {
  nemonicoMenu: string;
  nemonicoCarpeta: string;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  item: EncabezadoDTO;
  seccion: string;

  constructor(
    public dialogRef: MatDialogRef<EvaluacionDocumentoComponent>,
    private dialogMensajeService: DialogMensajeService,
    private encuestaService: EncuestaService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    @Inject(MAT_DIALOG_DATA) public data: { encabezado: EncabezadoDTO, nemonicoMenu: string, nemonicoCarpeta: string, seccion: string }
  ) {
    this.item = data.encabezado;
    this.nemonicoMenu = data.nemonicoMenu;
    this.nemonicoCarpeta = data.nemonicoCarpeta;
    this.seccion = data.seccion;
  }

  ngOnInit() {
    if (!this.seccion)
      this.seccion = etiquetasModel.SECCION_FICHA_IDENT_EVALUACIONES;

    this.obtenerTiposDeDocumentos();
  }

  subirDocumento(documentos: DocumentoSubido[]) {
    //TODO: GUARDAR REGISTRO DE MANDATO ANTES DE SUBIR UN ARCHIVO
    if (documentos.length > 0) {

      let files = documentos?.map(doc => doc.documento) ?? null;

      let encabezadoDTO = new EncabezadoDTO();
      encabezadoDTO.tokenIdentificador = this.item?.tokenIdentificador;
      encabezadoDTO.evaluacionDocumentoDTO = new EvaluacionDocumentoDTO();
      encabezadoDTO.evaluacionDocumentoDTO.nemonicoCarpeta = this.nemonicoCarpeta;
      encabezadoDTO.evaluacionDocumentoDTO.documentoDTOList = documentos?.map(doc => doc.documentoDTO)


      let load = this.dialogMensajeService.mensajeLoading("Subiendo los documentos.");

      this.encuestaService.subirDocumentos(
        encabezadoDTO,
        files,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (response: RespuestaPorDefecto<Boolean>) => {

            load.close();
            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al subir el/los documento. ' + (response.mensaje || 'Inténtalo de nuevo.')
              );
              return;
            }

            this.dialogMensajeService.mensajeExitoso(
              'Subir',
              'Documento subido correctamente.'
            ).afterClosed().subscribe(() => {
              this.cerrar();
            });
          },
          error: (error: any) => {
            load.close();
            console.error('Error al subir documento de evaluación', error);
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al subir el/los documento. Inténtalo de nuevo.'
            );
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
    }
  }

  obtenerTiposDeDocumentos() {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(this.seccion,
      '').subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {

            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
              return;
            }

            let tiposArchivos = response.data;

            if (tiposArchivos.length == 0) {
              this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
              return;
            }

            this.tiposDeDocumentosSistema =
              tiposArchivos.map(
                (tipoArch) => {
                  let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
                  let tipoDeDocumento = new TipoDeDocumento();
                  tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
                  tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
                  tipoDeDocumento.requerido = tipoArch.requerido;
                  tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
                  tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

                  return tipoDeDocumento;
                }
              );
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
  }

  cerrar() {
    this.dialogRef.close();
  }
}
