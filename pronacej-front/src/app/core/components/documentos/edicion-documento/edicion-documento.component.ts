import { AfterViewInit, Component, inject, Input, OnInit, ViewChild } from '@angular/core';
import { TipoDeDocumento } from '../modelos/TipoDeDocumento.model';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { SubidaDeDocumentosComponent } from '../subida-de-documentos/subida-de-documentos.component';
import { DocumentoService } from 'app/core/services/documento.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-edicion-documento',
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    SubidaDeDocumentosComponent,
    MatButtonModule
  ],
  templateUrl: './edicion-documento.component.html',
  styleUrl: './edicion-documento.component.scss'
})
export class EdicionDocumentoComponent implements OnInit, AfterViewInit {
  public dataDialog = inject<
    {
      tiposDeDocumentosSistema: TipoDeDocumento[],
      documentoDTO: DocumentoDTO,
      nemonicoMenu: string
    }>(MAT_DIALOG_DATA);

  titulo: string;

  @ViewChild("subidaDocComp") subidaDocComp: SubidaDeDocumentosComponent;
  constructor(private dialogRef: MatDialogRef<EdicionDocumentoComponent>,
    private documentoService: DocumentoService,
    private dialogMensajeService: DialogMensajeService
  ) { }

  ngAfterViewInit(): void {
    //Ingresando los datos a actualizar
    let documentoDTO = this.dataDialog.documentoDTO;
    this.subidaDocComp.subidaDeDocumentoForm?.get("tokenTipoDeDocumentoSistema").setValue(
      documentoDTO.tipoDocumentoSistema.tokenIdentificador
    );
    this.subidaDocComp.subidaDeDocumentoForm?.get("tipoDeDocumentoOtro").setValue(
      documentoDTO.tipoDeDocumentoSistemaOtro
    );
    this.subidaDocComp.subidaDeDocumentoForm?.get("descripcion").setValue(
      documentoDTO.descripcion
    );
  }

  ngOnInit(): void {
    console.log(this.dataDialog);
    this.titulo = "Edita el documento: " + this.dataDialog.documentoDTO.nombre;
  }

  actualizarDoc() {
    let controlDoc = this.subidaDocComp.subidaDeDocumentoForm.get("archivos");
    if (controlDoc.invalid) {
      this.dialogMensajeService.mensajeError("El archivo es inválido");
      return;
    }

    let files = controlDoc.value as File[]

    if (files.length == 0) {
      this.dialogMensajeService.mensajeError("Debes de subir al menos 1 archivo para continuar");
      return;
    }

    let file = files[0];

    let documentoDTO = this.subidaDocComp.getDocumentoDTO(file);
    documentoDTO.tokenIdentificador = this.dataDialog.documentoDTO.tokenIdentificador;
    documentoDTO.esEdicion = true;

    let load = this.dialogMensajeService.mensajeLoading("Actualizando el documento..");
    this.documentoService.actualizarDocumento(
      file,
      documentoDTO,
      this.dataDialog.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
          load.close();
          if (!response.exito) {
            this.documentoService.checkError(response);
            return;
          }

          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.dialogRef.close(true);
        },
        error: (error: any) => {
          load.close();
          this.dialogRef.close(false);
          this.documentoService.checkError(error);
        }
      }
    );
  }

  cancelar() {
    this.dialogRef.close(
      false
    );
  }
}
