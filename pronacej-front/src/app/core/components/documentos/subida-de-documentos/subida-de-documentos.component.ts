import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { TipoDeDocumento } from '../modelos/TipoDeDocumento.model';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DocumentoSubido } from '../modelos/DocumentoSubido.model';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { MatInputModule } from '@angular/material/input';
import { environment } from 'environments/environment';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { VistaDocumentosSubidosComponent } from '../vista-documentos-subidos/vista-documentos-subidos.component';
import { v4 as uuidv4 } from 'uuid';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { VisualizarPdfService } from '../../visualizar-pdf/visualizar-pdf.service';
import { MatDialogRef } from '@angular/material/dialog';
import { FuseConfirmationDialogComponent } from '@fuse/services/confirmation/dialog/dialog.component';
import { VisualizarImagenService } from '../../visualizar-imagen/visualizar-imagen.service';
import { FileUploadComponent, FileUploadModule, FileUploadValidators } from '@iplab/ngx-file-upload';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-subida-de-documentos',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    VistaDocumentosSubidosComponent,
    FileUploadModule
  ],
  templateUrl: './subida-de-documentos.component.html',
  styleUrl: './subida-de-documentos.component.scss'
})
export class SubidaDeDocumentosComponent {

  @Input({ required: true }) tiposDeDocumentosSistema: TipoDeDocumento[];
  @Input() acceptFiles: string[] = ["application/pdf", "image/png",
    "image/jpg", "image/jpeg"];
  @Input() multiple = false;
  @Input() mostrarSubirArchivos = true;
  @Input() solo1Archivo = false;
  @Input() tituloSeleccionTipoDocumento = "Selecciona el tipo";

  @Input() validarArchivosObligatorios = true;

  esEdicion = false;
  textEditar = "Edita el documento"

  @ViewChild("vistaDocSubidos") vistaDocSubidos: VistaDocumentosSubidosComponent;
  @ViewChild("uploadFiles") uploadFiles: FileUploadComponent;

  @Output() subirArchivosEvent = new EventEmitter<DocumentoSubido[]>();

  documentosSubidos: DocumentoSubido[] = [];

  subidaDeDocumentoForm: FormGroup;

  seleccionoOtroComoTipoDeDocumentoSistema = false;

  // funcionesUtils = new FuncionesUtils();
  maxBytesSize = 10 * 1024 * 1024;

  constructor(private fb: FormBuilder, private dialogMensajeService: DialogMensajeService,
    private visualizarPdfService: VisualizarPdfService,
    private visualizarImagenService: VisualizarImagenService,
    private funcionesUtils: FuncionesUtils,
  ) {

    this.subidaDeDocumentoForm = this.fb.group(
      {
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

    let files = this.subidaDeDocumentoForm.get("archivos").value as File[];

    this.cargarArchivosSubidos(files);

  }

  cargarArchivosSubidos(files: File[]) {
    let load = this.dialogMensajeService.mensajeLoading("Cargando archivo..");

    for (let i = 0; files.length > i; i++) {
      let file = files[i];
      let documentoSubido = new DocumentoSubido();
      documentoSubido.documento = file;

      let documentoDTO = this.getDocumentoDTO(file);

      documentoSubido.documentoDTO = documentoDTO;

      if (this.esEdicion) {
        let index = +this.subidaDeDocumentoForm.get("indexDocEdicion").value;
        this.documentosSubidos.splice(index, 1, documentoSubido);
      } else {
        this.documentosSubidos.unshift(documentoSubido);
      }
    }

    this.cancelarEdicion();
    this.vistaDocSubidos?.actualizarLista(this.documentosSubidos.map(doc => doc.documentoDTO));
    load.close();
  }

  getDocumentoDTO(file: File) {
    let tipoDeDocumentoOtro = this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro").value;
    let tokenTipoDeDocumentoSistema = this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema").value;
    let descripcion = this.subidaDeDocumentoForm.get("descripcion").value;

    let documentoDTO = new DocumentoDTO();
    documentoDTO.tokenIdentificador = uuidv4();
    documentoDTO.mimeType = file.type;
    documentoDTO.nombre = file.name;
    documentoDTO.tamanioBytes = file.size;
    documentoDTO.tipoDeDocumentoSistemaOtro = tipoDeDocumentoOtro;
    documentoDTO.tipoDocumentoSistema =
      this.tiposDeDocumentosSistema.find((tipo) => tipo.tokenIdentificador == tokenTipoDeDocumentoSistema);
    documentoDTO.fechaCreacion = new Date(file.lastModified);
    documentoDTO.descripcion = descripcion;

    return documentoDTO;
  }

  editarDocEvent(event: { documentoDTO: DocumentoDTO, index: number, numero: number }) {
    this.esEdicion = true;
    this.textEditar = "Edita el documento: " + event.numero;

    let doc = this.documentosSubidos.find(
      (doc) => doc.documentoDTO.tokenIdentificador == event.documentoDTO.tokenIdentificador
    );

    this.subidaDeDocumentoForm.get("tokenTipoDeDocumentoSistema").setValue(event.documentoDTO?.tipoDocumentoSistema.tokenIdentificador);
    this.subidaDeDocumentoForm.get("tipoDeDocumentoOtro").setValue(event.documentoDTO?.tipoDeDocumentoSistemaOtro);;
    this.subidaDeDocumentoForm.get("descripcion").setValue(event.documentoDTO?.descripcion);
    this.subidaDeDocumentoForm.get("indexDocEdicion").setValue(event.index);
    if (doc) {
      this.uploadFiles?.control.clear();
      this.uploadFiles?.control.setValue([doc.documento]);
    }


  }

  eliminarDocEvent(event: { documentoDTO: DocumentoDTO, index: number, numero: number }) {
    let conf = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el documento: " + event.documentoDTO.nombre + " ?",
      "Está operación no se puede revertir"
    );

    conf.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {

          if (resp == "confirmed") {
            this.documentosSubidos.splice(event.index, 1);
            this.vistaDocSubidos?.actualizarLista(this.documentosSubidos.map(doc => doc.documentoDTO));
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

  verDocEvent(event: { documentoDTO: DocumentoDTO, index: number, numero: number }) {

    let load = this.dialogMensajeService.mensajeLoading("Creando vista del pdf");

    let doc = this.documentosSubidos.find(
      (doc) => doc.documentoDTO.tokenIdentificador == event.documentoDTO.tokenIdentificador
    );

    if (!doc) {
      this.dialogMensajeService.mensajeError("El documento no fue encontrado");
      return;
    }

    let file = doc.documento;

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
      (doc) => doc.documentoDTO.tipoDocumentoSistema
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

    if (!subidaExitosa && this.validarArchivosObligatorios) {
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
    let copia: DocumentoSubido[] = [];

    for (let i = 0; i < this.documentosSubidos.length; i++) {
      copia.push(this.documentosSubidos[i]);
    }

    this.subirArchivosEvent.emit(copia);

    this.cancelarEdicion();
    this.documentosSubidos = [];
    this.vistaDocSubidos?.actualizarLista([]);

  }

  get documentosCargados():File[]{
    return this.subidaDeDocumentoForm.controls.archivos.value;
  }

  actualizarList(documentosSubidos: DocumentoSubido[]) {
    this.documentosSubidos = documentosSubidos;
    this.vistaDocSubidos?.actualizarLista(this.documentosSubidos.map(doc => doc.documentoDTO));
  }

  borrarDatos() {
    this.cancelarEdicion();
    this.documentosSubidos = [];
    this.vistaDocSubidos?.actualizarLista([]);
  }
}