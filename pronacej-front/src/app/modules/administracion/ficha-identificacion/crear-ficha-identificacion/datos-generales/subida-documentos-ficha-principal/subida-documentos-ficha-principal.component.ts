import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FuseConfirmationDialogComponent } from '@fuse/services/confirmation/dialog/dialog.component';
import { FileUploadComponent, FileUploadModule, FileUploadValidators } from '@iplab/ngx-file-upload';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { VisualizarImagenService } from 'app/core/components/visualizar-imagen/visualizar-imagen.service';
import { VisualizarPdfService } from 'app/core/components/visualizar-pdf/visualizar-pdf.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { v4 as uuidv4 } from 'uuid';
import { environment } from 'environments/environment';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { ListaTipoArchivosComponent } from 'app/core/components/documentos/subida-de-documentos-sistema/lista-tipo-archivos/lista-tipo-archivos.component';
import { DocumentoDTOFichaPrincipal } from 'app/core/model/both/documento/documentoDTOFichaPrincipal.model';
import { DocumentoCargadoFichaPrincipal } from 'app/core/model/both/documento/documentoCargadoFichaPrincipal.model';
import { SubidaDeDocumentosSistemaComponent } from 'app/core/components/documentos/subida-de-documentos-sistema/subida-de-documentos-sistema.component';
import { ViewportScroller } from '@angular/common';
import { MatDividerModule } from '@angular/material/divider';
import { FichaPrincipalDocumentoService } from 'app/modules/administracion/services/fichaPrincipalDocumento.service';
import { FichaPrincipalDocumentoDTO } from 'app/core/model/request/ia/FichaPrincipalDocumentoDTO.model';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';

@Component({
  selector: 'app-subida-documentos-ficha-principal',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    SubidaDeDocumentosSistemaComponent,
    FileUploadModule,
    MatDialogModule,
    ListaTipoArchivosComponent,
    MatDividerModule
  ],
  templateUrl: './subida-documentos-ficha-principal.component.html',
  styleUrl: './subida-documentos-ficha-principal.component.scss'
})
export class SubidaDocumentosFichaPrincipalComponent implements OnInit {

  idFormDoc = "form_doc";

  tiposDeDocumentosSistema: TipoDeDocumento[];
  @Input({ required: true }) nemonicoMenu: string;
  @Input({ required: true }) tokenIdentificadorFichaIdentificacion: string;

  acceptFiles: string[] = ["application/pdf", "image/png",
    "image/jpg", "image/jpeg"];

  tiposDeDocumentosFichaPrincipal: CatalogoDTO[];

  @ViewChild("vistaDocSubidos") vistaDocSubidos: SubidaDeDocumentosSistemaComponent;
  @ViewChild("uploadFiles") uploadFiles: FileUploadComponent;

  @Output() subirArchivosEvent = new EventEmitter<DocumentoCargadoFichaPrincipal[]>();

  documentosSubidos: DocumentoCargadoFichaPrincipal[] = [];

  subidaDeDocumentoForm: FormGroup;

  seleccionoOtroComoTipoDeDocumentoSistema = false;

  // funcionesUtils = new FuncionesUtils();
  maxBytesSize = 10 * 1024 * 1024;
  selected = null;

  esEdicion = false;
  textEditar = "Edita el documento"

  tiposDeDocumentosRequeridos: TipoDeDocumento[] = [];
  tiposDeDocumentosOpcionales: TipoDeDocumento[] = [];

  constructor(private fb: FormBuilder, private dialogMensajeService: DialogMensajeService,
    private visualizarPdfService: VisualizarPdfService,
    private visualizarImagenService: VisualizarImagenService,
    private funcionesUtils: FuncionesUtils,
    private dialogRef: MatDialogRef<SubidaDocumentosFichaPrincipalComponent>,
    private catalogoService: CatalogoService,
    private viewportScroller: ViewportScroller,
    private fichaPrincipalDocumentoService: FichaPrincipalDocumentoService,
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
  ngOnInit(): void {
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

            this.tiposDeDocumentosOpcionales = this.tiposDeDocumentosSistema.filter(
              (tipo) => !tipo.requerido
            );

            this.tiposDeDocumentosRequeridos = this.tiposDeDocumentosSistema.filter(
              (tipo) => tipo.requerido
            );
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
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
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  subirDocumentos() {
    if (!environment.production) {
      console.log(this.documentosSubidos);
    }

    const lengthDoc = this.documentosSubidos.length;

    if (lengthDoc < 1) {
      this.dialogMensajeService.mensajeError("Debes de cargar al menos 1 documento para continuar");
      return;
    }

    //Verificando archivos requeridos
    let tiposRequeridos = this.tiposDeDocumentosSistema.filter(
      (doc) => doc.requerido
    );
    let tiposRequeridosNoEncontrados: CatalogoDTO[] = [];
    let cantidadDeRequeridosEncontrados = 0;

    let tipoDocSistemaSubidos = this.documentosSubidos.map(
      (doc) => doc.documento.tipoDocumentoSistema
    );

    for (let i = 0; tiposRequeridos.length > i; i++) {
      let tipo = tiposRequeridos[i];

      let tipoDocRequeridoEncontrado = tipoDocSistemaSubidos?.find(
        (doc2) => doc2.tokenIdentificador == tipo.tokenIdentificador
      );
      if (tipoDocRequeridoEncontrado) {
        cantidadDeRequeridosEncontrados++;
      } else {
        tiposRequeridosNoEncontrados.push(tipo);
      }
    }

    let subidaExitosa = cantidadDeRequeridosEncontrados == tiposRequeridos.length;
    let mensaje = "Documentos cargados con exito";

    if (!subidaExitosa) {
      let nombresNoEncontrados = "";

      for (let i = 0; tiposRequeridosNoEncontrados.length > i; i++) {
        let tipoNoEncontrado = tiposRequeridosNoEncontrados[i];
        let signo: string;
        if ((i + 1) == tiposRequeridosNoEncontrados.length) {
          signo = "";
        } else {
          signo = ", ";
        }

        nombresNoEncontrados = nombresNoEncontrados + tipoNoEncontrado.nombre + signo;
      }

      mensaje = "Faltan subir archivos de tipo: " + nombresNoEncontrados +
        ", revisa en la parte superior los documentos que son requeridos de subir en esta sección.";

      this.dialogMensajeService.mensajeError(mensaje);
      return;
    }

    let mensajeSegunTipoDocficha = "";
    for (let i = 0; this.tiposDeDocumentosFichaPrincipal?.length > i; i++) {
      let tipoDocFicha = this.tiposDeDocumentosFichaPrincipal[i];

      let totalDocsFicha = this.documentosSubidos.filter(
        (doc) => doc.documento.tipoDeDocumentoFicha.tokenIdentificador == tipoDocFicha.tokenIdentificador
      );

      if (totalDocsFicha?.length > 0) {
        let separador = ", ";
        if ((i + 1) == this.tiposDeDocumentosFichaPrincipal.length) {
          separador = ".";
        }
        mensajeSegunTipoDocficha = mensajeSegunTipoDocficha + tipoDocFicha.nombre + ": " +
          totalDocsFicha.length + " documento(s)" + separador;
      }
    }
    let confirm = this.dialogMensajeService.mensajeConConfirmacion("Vas a subir un total de: "
      + lengthDoc + " documentos",
      "Detalles de los documentos por subir: " + mensajeSegunTipoDocficha
    );

    confirm.afterClosed().subscribe(
      {
        next: (response: "confirmed" | "cancelled") => {
          if (response == "confirmed") {
            this.subirDocumentosAlsistema(this.documentosSubidos);
          }
        },
        error: (error: any) => {
          console.error(error);
        }
      }
    );

  }

  subirDocumentosAlsistema(documentos: DocumentoCargadoFichaPrincipal[]) {
    if (documentos && documentos.length > 0) {

      if (!this.tokenIdentificadorFichaIdentificacion) {
        this.dialogMensajeService.mensajeError("No existe un id de ficha de identificación válido");
        return;
      }

      for (let documentoSubido of documentos) {
        let fichaPrincipalDocumentoDTO = new FichaPrincipalDocumentoDTO();
        fichaPrincipalDocumentoDTO.tokenIdentificadorFichaPrincipal = this.tokenIdentificadorFichaIdentificacion;
        fichaPrincipalDocumentoDTO.documentoDTO = documentoSubido.documento;

        let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
          documentoSubido.file.name
        );
        this.fichaPrincipalDocumentoService.subirDocumento(
          documentoSubido.file,
          fichaPrincipalDocumentoDTO,
          this.nemonicoMenu
        ).subscribe(
          {
            next: (response: RespuestaPorDefecto<DocumentoDTOFichaPrincipal>) => {

              load.close();
              if (!response.exito) {
                this.fichaPrincipalDocumentoService.checkError(response);
                return;
              }

              //cerrar el modal y decir que fue exitoso la subida
              this.dialogRef.close(true);

            },
            error: (error: any) => {
              load.close();
              this.fichaPrincipalDocumentoService.checkError(error);
            }
          }
        );
      }
    } else {
      this.dialogMensajeService.mensajeError("No se obtenieron documentos para ser subidos");
    }
  }

  cerrar() {

    if (this.documentosSubidos.length > 0) {
      let confirm = this.dialogMensajeService.mensajeConConfirmacion("Deseas cerrar la carga de documentos",
        "Actualmente tienes: " + this.documentosSubidos.length + " documento(s) subido(s), estos se perderán deseas continuar?"
      );

      confirm.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              this.dialogRef.close(false)
            }
          }
        }
      );
    } else {
      this.dialogRef.close(false);
    }
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

  cargarArchivo() {

    if (!environment.production) {
      console.log(this.subidaDeDocumentoForm);
    }

    if (this.subidaDeDocumentoForm.invalid) {
      this.subidaDeDocumentoForm.markAllAsTouched();
      let errorsTipo = this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema")?.errors;
      let errorsTipoOtro = this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro")?.errors;
      let errorsArchivo = this.subidaDeDocumentoForm.get("archivos")?.errors;

      let mensaje = "Debes de completar toda la información requerida para continuar";
      if (errorsArchivo && !errorsTipo && !errorsTipoOtro) {
        mensaje = "Se encontraron archivos que no son válidos quitalos antes de continuar (los archivos deben de ser de tipo: "
          + this.acceptFiles + " y tener un peso de máximo de: " + this.maxBytesSize + " bytes).";
      }
      this.dialogMensajeService.mensajeError(mensaje);
      return;
    }

    //verificando si hay archivos repetidos entre tipos
    let files = this.subidaDeDocumentoForm.get("archivos").value as File[];
    if (this.documentosSubidos.length > 0 && files.length > 0) {
      for (let file of files) {
        let docRepetido = this.documentosSubidos.find(
          (doc) => doc.file.name == file.name
        );

        if (docRepetido) {
          this.dialogMensajeService.mensajeError("El archivo con nombre: " +
            docRepetido.file.name + " ya ha sido subido anteriormente para el tipo: " +
            docRepetido.documento.tipoDocumentoSistema.nombre + ", debes de cargar otro documento con otro nombre " +
            "o renombrarlo e intentar nuevamente."
          );
          return;
        }
      }
    }
    this.cargarArchivosSubidos(files);

  }

  cargarArchivosSubidos(files: File[]) {
    let load = this.dialogMensajeService.mensajeLoading("Cargando archivos..");

    for (let i = 0; files.length > i; i++) {
      let file = files[i];
      let documentoSubido = new DocumentoCargadoFichaPrincipal();
      documentoSubido.file = file;

      let documentoDTO = this.getDocumentoDTO(file);

      documentoSubido.documento = documentoDTO;

      if (this.esEdicion) {
        let index = +this.subidaDeDocumentoForm.get("indexDocEdicion").value;
        this.documentosSubidos.splice(index, 1, documentoSubido);
      } else {
        this.documentosSubidos.unshift(documentoSubido);
      }

    }

    if (this.esEdicion) {
      this.cancelarEdicion();
    } else {
      this.resetearFormalSubir();
    }
    this.vistaDocSubidos?.actualizarLista(this.documentosSubidos.map(doc => doc.documento));
    load.close();
  }

  getDocumentoDTO(file: File): DocumentoDTOFichaPrincipal {
    let tipoDeDocumentoOtro = this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro").value;
    let tokenTipoDeDocumentoSistema = this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema").value;
    let tokenTipoDeDocumentoFichaPrincipal = this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoFicha").value;
    let descripcion = this.subidaDeDocumentoForm.get("descripcion").value;

    let documentoDTO = new DocumentoDTOFichaPrincipal();
    documentoDTO.tokenIdentificador = uuidv4();
    documentoDTO.mimeType = file.type;
    documentoDTO.nombre = file.name;
    documentoDTO.tamanioBytes = file.size;
    documentoDTO.tipoDeDocumentoSistemaOtro = tipoDeDocumentoOtro;

    documentoDTO.tipoDocumentoSistema =
      this.tiposDeDocumentosSistema.find((tipo) => tipo.tokenIdentificador == tokenTipoDeDocumentoSistema);

    documentoDTO.tipoDeDocumentoFicha = this.tiposDeDocumentosFichaPrincipal.find(
      (tipo) => tipo.tokenIdentificador == tokenTipoDeDocumentoFichaPrincipal
    );

    documentoDTO.fechaCreacion = new Date(file.lastModified);
    documentoDTO.descripcion = descripcion;

    return documentoDTO;
  }

  editarDocEvent(event: { documentoDTO: DocumentoDTOFichaPrincipal, index: number, numero: number }) {
    this.esEdicion = true;
    this.textEditar = "Edita el documento: " + event.numero;

    let doc = this.documentosSubidos.find(
      (doc) => doc.documento.tokenIdentificador == event.documentoDTO.tokenIdentificador
    );

    this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema").setValue(event.documentoDTO?.tipoDocumentoSistema.tokenIdentificador);
    this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro").setValue(event.documentoDTO?.tipoDeDocumentoSistemaOtro);;
    this.subidaDeDocumentoForm.get("descripcion").setValue(event.documentoDTO?.descripcion);
    this.subidaDeDocumentoForm.get("indexDocEdicion").setValue(event.index);
    this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoFicha").setValue(event.documentoDTO?.tipoDeDocumentoFicha?.tokenIdentificador);

    if (doc) {
      this.uploadFiles?.control.clear();
      this.uploadFiles?.control.setValue([doc.file]);
    }

    //scroll up
    this.viewportScroller.scrollToAnchor(this.idFormDoc);

  }

  eliminarDocEvent(event: { documentoDTO: DocumentoDTOFichaPrincipal, index: number, numero: number }) {
    let conf = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el documento: " + event.documentoDTO.nombre + " ?",
      "Está operación no se puede revertir"
    );

    conf.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {

          if (resp == "confirmed") {
            this.documentosSubidos.splice(event.index, 1);
            this.vistaDocSubidos?.actualizarLista(this.documentosSubidos.map(doc => doc.documento));
          }

        }
      }
    );
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.subidaDeDocumentoForm.reset();
    this.uploadFiles?.control?.clear();

    this.seleccionoOtroComoTipoDeDocumentoSistema = false;
  }

  resetearFormalSubir() {
    this.esEdicion = false;

    this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema")?.reset();
    this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro")?.reset();
    this.subidaDeDocumentoForm.get("descripcion")?.reset();
    this.subidaDeDocumentoForm.get("indexDocEdicion")?.reset();
    this.subidaDeDocumentoForm.get("archivos")?.reset();

    this.uploadFiles?.control?.clear();

    this.seleccionoOtroComoTipoDeDocumentoSistema = false;
  }

  verDocEvent(event: { documentoDTO: DocumentoDTOFichaPrincipal, index: number, numero: number }) {

    let load = this.dialogMensajeService.mensajeLoading("Creando vista del pdf");

    let doc = this.documentosSubidos.find(
      (doc) => doc.documento.tokenIdentificador == event.documentoDTO.tokenIdentificador
    );

    if (!doc) {
      this.dialogMensajeService.mensajeError("El documento no fue encontrado");
      return;
    }

    let file = doc.file;

    if (file.type?.includes("pdf")) {
      this.visualizarFile(file, load);
    } else if (file.type?.includes("image")) {
      this.visualizarFile(file, load);
    } else {
      load.close();
      this.dialogMensajeService.mensajeError("No se puede crear una vista previa del documento");
    }
  }

  private visualizarFile(file: File, load: MatDialogRef<FuseConfirmationDialogComponent, any>) {
    this.funcionesUtils.obtenerBase64(file).then(
      (base64Encoded: string) => {

        load.close();

        if (file.type.includes("pdf")) {
          this.visualizarPdfService.abrirVistaPdf(
            base64Encoded, file.name
          );
        } else {
          this.visualizarImagenService.abrirVista(
            base64Encoded, file.name
          );
        }

      }
    ).catch(
      (ex: any) => {
        load.close();
        this.dialogMensajeService.mensajeError("No se pudo crear el base 64 del archivo debido a: "
          + ex.toString()
        );
      }
    );
  }

  validarFile(file: File) {
    return this.acceptFiles.includes(file.type) && this.maxBytesSize > file.size;
  }

  subirArchivos() {

    //Verificando archivos requeridos
    let tiposRequeridos = this.tiposDeDocumentosSistema.filter(
      (doc) => doc.requerido
    );
    let tiposRequeridosNoEncontrados: CatalogoDTO[] = [];
    let cantidadDeRequeridosEncontrados = 0;

    let tipoDocSistemaSubidos = this.documentosSubidos.map(
      (doc) => doc.documento.tipoDocumentoSistema
    );

    for (let i = 0; tiposRequeridos.length > i; i++) {
      let tipo = tiposRequeridos[i];

      let tipoDocRequeridoEncontrado = tipoDocSistemaSubidos?.find(
        (doc2) => doc2.tokenIdentificador == tipo.tokenIdentificador
      );
      if (tipoDocRequeridoEncontrado) {
        cantidadDeRequeridosEncontrados++;
      } else {
        tiposRequeridosNoEncontrados.push(tipo);
      }
    }

    let subidaExitosa = cantidadDeRequeridosEncontrados == tiposRequeridos.length;
    let mensaje = "Documentos cargados con exito";

    if (!subidaExitosa) {
      let nombresNoEncontrados = "";

      for (let i = 0; tiposRequeridosNoEncontrados.length > i; i++) {
        let tipoNoEncontrado = tiposRequeridosNoEncontrados[i];
        let signo: string;
        if ((i + 1) == tiposRequeridosNoEncontrados.length) {
          signo = "";
        } else {
          signo = ", ";
        }

        nombresNoEncontrados = nombresNoEncontrados + tipoNoEncontrado.nombre + signo;
      }

      mensaje = "Faltan subir archivos de tipo: " + nombresNoEncontrados;

      this.dialogMensajeService.mensajeError(mensaje);
      return;
    }
    let copia: DocumentoCargadoFichaPrincipal[] = [];

    for (let i = 0; i < this.documentosSubidos.length; i++) {
      copia.push(this.documentosSubidos[i]);
    }

    this.subirArchivosEvent.emit(copia);

    this.cancelarEdicion();
    this.documentosSubidos = [];
    this.vistaDocSubidos?.actualizarLista([]);

  }

  actualizarList(documentosSubidos: DocumentoCargadoFichaPrincipal[]) {
    this.documentosSubidos = documentosSubidos;
    this.vistaDocSubidos?.actualizarLista(this.documentosSubidos.map(doc => doc.documento));
  }

  borrarDatos() {
    this.cancelarEdicion();
    this.documentosSubidos = [];
  }

}
