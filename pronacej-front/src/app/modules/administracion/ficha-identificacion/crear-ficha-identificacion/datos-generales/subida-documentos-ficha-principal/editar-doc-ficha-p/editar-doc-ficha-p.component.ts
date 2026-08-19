import { Component, inject, Input, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FileUploadModule, FileUploadValidators } from '@iplab/ngx-file-upload';
import { EdicionDocumentoComponent } from 'app/core/components/documentos/edicion-documento/edicion-documento.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DocumentoDTOFichaPrincipal } from 'app/core/model/both/documento/documentoDTOFichaPrincipal.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { FichaDeIdentificacionDocumentoDTO } from 'app/core/model/request/ia/FichaDeIdentificacionDocumentoDTO.model';
import { FichaPrincipalDocumentoDTO } from 'app/core/model/request/ia/FichaPrincipalDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { FichaPrincipalDocumentoService } from 'app/modules/administracion/services/fichaPrincipalDocumento.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-editar-doc-ficha-p',
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    ReactiveFormsModule,
    FormsModule,
    FileUploadModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './editar-doc-ficha-p.component.html',
  styleUrl: './editar-doc-ficha-p.component.scss'
})
export class EditarDocFichaPComponent {

  acceptFiles: string[] = ["application/pdf", "image/png",
    "image/jpg", "image/jpeg"];

  seleccionoOtroComoTipoDeDocumentoSistema = false;

  // funcionesUtils = new FuncionesUtils();
  maxBytesSize = 10 * 1024 * 1024;

  tiposDeDocumentosSistema: TipoDeDocumento[];
  @Input({ required: true }) declare fichaDeIdentificacionDocumentoDTO: FichaDeIdentificacionDocumentoDTO;
  @Input({ required: true }) declare nemonicoMenu: string;
  tiposDeDocumentosFichaPrincipal: CatalogoDTO[];

  titulo: string;
  subidaDeDocumentoForm: FormGroup;

  documentoDTO: DocumentoDTO;

  constructor(private dialogRef: MatDialogRef<EdicionDocumentoComponent>,
    private fichaPrincipalDocumentoService: FichaPrincipalDocumentoService,
    private dialogMensajeService: DialogMensajeService,
    private fb: FormBuilder,
    private catalogoService: CatalogoService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService
  ) {

    this.subidaDeDocumentoForm = this.fb.group(
      {
        tokenTipoDeDocumentoFicha: [null, [Validators.required]],
        tokenTipoDeDocumentoSistema: [null, [Validators.required]],
        tipoDeDocumentoOtro: [null],
        descripcion: [null],
        indexDocEdicion: [null],
        archivos: [null, [Validators.required, FileUploadValidators.accept(this.acceptFiles),
        FileUploadValidators.fileSize(this.maxBytesSize)
        ]]
      }
    );
  }

  ngAfterViewInit(): void {
    //Ingresando los datos a actualizar
    this.documentoDTO = this.fichaDeIdentificacionDocumentoDTO.documentoDTO;

    let tipoDocFichaIdentiicacion = this.fichaDeIdentificacionDocumentoDTO?.tipoDeDocumentoFichaDeIdentificacion;
    this.subidaDeDocumentoForm?.get("tokenTipoDeDocumentoSistema").setValue(
      this.documentoDTO.tipoDocumentoSistema.tokenIdentificador
    );
    this.subidaDeDocumentoForm?.get("tipoDeDocumentoOtro").setValue(
      this.documentoDTO.tipoDeDocumentoSistemaOtro
    );
    this.subidaDeDocumentoForm?.get("descripcion").setValue(
      this.documentoDTO?.descripcion
    );

    this.subidaDeDocumentoForm?.get("tokenTipoDeDocumentoFicha").setValue(
      tipoDocFichaIdentiicacion?.tokenIdentificador
    );


    if (!environment.production) {
      console.log(this.fichaDeIdentificacionDocumentoDTO);
    }

  }

  ngOnInit(): void {
    this.titulo = "Edita el documento: " + this.fichaDeIdentificacionDocumentoDTO.documentoDTO?.nombre;

    this.obtenerTiposDeDocumentosFichaPrincipal();
  }

  cambioDocumentoDe(tokenIdentificador: string) {
    if (tokenIdentificador && this.tiposDeDocumentosFichaPrincipal.length > 0) {
      let catalogoDTO = this.tiposDeDocumentosFichaPrincipal?.find(
        (cat) => cat.tokenIdentificador == tokenIdentificador
      );

      let nemonicoFinal: string;

      if (catalogoDTO?.nemonico == etiquetasModel.NEMONICO_TIPO_DE_DOCUMENTO_FICHA_PRINCIPAL_DOC_INGRESO) {
        nemonicoFinal = etiquetasModel.SECCION_FICHA_IDENT_FICHA_PRINCIPAL_DOCUMENTOS_INGRESO;

      } else if (catalogoDTO?.nemonico == etiquetasModel.NEMONICO_TIPO_DE_DOCUMENTO_FICHA_PRINCIPAL_DOC_SALIDA) {
        nemonicoFinal = etiquetasModel.SECCION_FICHA_IDENT_FICHA_PRINCIPAL_DOCUMENTOS_SALIDA;
      } else {
        this.dialogMensajeService.mensajeError("El documento de la ficha principal no ha sido configurado");
        return;
      }

      this.obtenerTiposDeDocumentos(nemonicoFinal);
    }
  }

  private obtenerTiposDeDocumentos(nemonicoSeccionFichaPrincipal: string) {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(
      nemonicoSeccionFichaPrincipal,
      this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
            if (!environment.production) {
              console.log(response);
            }

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
              )?.sort((a, b) => a.nombre.localeCompare(b.nombre));

            if (this.tiposDeDocumentosSistema?.length == 0) {
              this.dialogMensajeService.mensajeError("No se han cargado los tipos de documentos (recomendación vuelve a recargar la pestaña)")
              return;
            }
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
  }

  validarFile(file: File) {
    return this.acceptFiles.includes(file.type) && this.maxBytesSize > file.size;
  }

  obtenerTiposDeDocumentosFichaPrincipal() {
    this.catalogoService.obtenerHijos(etiquetasModel.NEMONICO_TIPOS_DE_DOCUMENTOS_FICHA_PRINCIPAL,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (respuesta: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!respuesta.exito) {
            this.catalogoService.checkError(respuesta);
            return;
          }
          this.tiposDeDocumentosFichaPrincipal = respuesta.data;

          let nemonicoFinal: string;
          let tipoDocFichaIdentiicacion = this.fichaDeIdentificacionDocumentoDTO?.tipoDeDocumentoFichaDeIdentificacion;
          let catalogoDTO = this.tiposDeDocumentosFichaPrincipal?.find(
            (cat) => cat.tokenIdentificador == tipoDocFichaIdentiicacion.tokenIdentificador
          );
          if (catalogoDTO?.nemonico == etiquetasModel.NEMONICO_TIPO_DE_DOCUMENTO_FICHA_PRINCIPAL_DOC_INGRESO) {
            nemonicoFinal = etiquetasModel.SECCION_FICHA_IDENT_FICHA_PRINCIPAL_DOCUMENTOS_INGRESO;

          } else if (catalogoDTO?.nemonico == etiquetasModel.NEMONICO_TIPO_DE_DOCUMENTO_FICHA_PRINCIPAL_DOC_SALIDA) {
            nemonicoFinal = etiquetasModel.SECCION_FICHA_IDENT_FICHA_PRINCIPAL_DOCUMENTOS_SALIDA;
          } else {
            this.dialogMensajeService.mensajeError("El documento de la ficha principal no ha sido configurado");
            return;
          }

          this.obtenerTiposDeDocumentos(nemonicoFinal);
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  cambioElTipoDeDocumentoSistema(tokenIdentificacion: string) {
    let tipoDeDocumentoSistema = this.tiposDeDocumentosSistema.find(
      (tipo) => tipo.tokenIdentificador == tokenIdentificacion
    );

    this.seleccionoOtroComoTipoDeDocumentoSistema =
      tipoDeDocumentoSistema?.nemonico == etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS
      || tipoDeDocumentoSistema?.nemonico == etiquetasModel.NEMONICO_TIPO_HISTORIAL_ARCHIVOS_OTROS;

    let validators = [];
    if (this.seleccionoOtroComoTipoDeDocumentoSistema) {
      validators = [Validators.required];
    }
    this.subidaDeDocumentoForm.setControl(
      "tipoDeDocumentoOtro",
      new FormControl(null, validators)
    )
  }

  actualizarDoc() {

    let controlDoc = this.subidaDeDocumentoForm.get("archivos");
    if (controlDoc.invalid) {
      this.dialogMensajeService.mensajeError("El archivo es inválido");
      return;
    }

    if (this.subidaDeDocumentoForm.invalid) {
      this.dialogMensajeService.mensajeError("Verifica los datos requeridos");
      return;
    }

    let files = controlDoc.value as File[]

    if (files.length == 0) {
      this.dialogMensajeService.mensajeError("Debes de subir al menos 1 archivo para continuar");
      return;
    }

    let file = files[0];

    let fichaPrincipalDocDTO = new FichaDeIdentificacionDocumentoDTO();
    let documentoDTO = this.getDocumentoDTO(file, this.documentoDTO.tokenIdentificador);
    documentoDTO.tokenIdentificador = this.documentoDTO.tokenIdentificador;
    documentoDTO.esEdicion = true;

    fichaPrincipalDocDTO.documentoDTO = documentoDTO;
    fichaPrincipalDocDTO.fichaIdentificacionDTO = this.fichaDeIdentificacionDocumentoDTO
      .fichaIdentificacionDTO;
    fichaPrincipalDocDTO.tipoDeDocumentoFichaDeIdentificacion =
      this.tiposDeDocumentosFichaPrincipal.find(
        (tipo) => tipo.tokenIdentificador == this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoFicha")?.value
      );
    fichaPrincipalDocDTO.tokenIdentificador = this.fichaDeIdentificacionDocumentoDTO.tokenIdentificador;

    let load = this.dialogMensajeService.mensajeLoading("Actualizando el documento..");
    this.fichaPrincipalDocumentoService.editarDocumento(
      file,
      fichaPrincipalDocDTO,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaDeIdentificacionDocumentoDTO>) => {
          load.close();
          if (!response.exito) {
            this.fichaPrincipalDocumentoService.checkError(response);
            return;
          }

          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.dialogRef.close(true);
        },
        error: (error: any) => {
          load.close();
          this.fichaPrincipalDocumentoService.checkError(error);
        }
      }
    );
  }

  cancelar() {
    this.dialogRef.close(
      false
    );
  }

  getDocumentoDTO(file: File, tokenItendificadorDoc: string) {
    let tipoDeDocumentoOtro = this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro").value;
    let tokenTipoDeDocumentoSistema = this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema").value;
    let tokenTipoDeDocumentoFicha = this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoFicha").value;

    let descripcion = this.subidaDeDocumentoForm.get("descripcion").value;

    let documentoDTO = new DocumentoDTOFichaPrincipal();
    documentoDTO.tokenIdentificador = tokenItendificadorDoc;
    documentoDTO.mimeType = file.type;
    documentoDTO.nombre = file.name;
    documentoDTO.tamanioBytes = file.size;
    documentoDTO.tipoDeDocumentoSistemaOtro = tipoDeDocumentoOtro;
    documentoDTO.tipoDocumentoSistema =
      this.tiposDeDocumentosSistema.find((tipo) => tipo.tokenIdentificador == tokenTipoDeDocumentoSistema);
    documentoDTO.fechaCreacion = new Date(file.lastModified);
    documentoDTO.descripcion = descripcion;
    documentoDTO.tipoDeDocumentoFicha = this.tiposDeDocumentosFichaPrincipal.find(
      (tipo) => tipo.tokenIdentificador == tokenTipoDeDocumentoFicha
    );

    return documentoDTO;
  }
}
