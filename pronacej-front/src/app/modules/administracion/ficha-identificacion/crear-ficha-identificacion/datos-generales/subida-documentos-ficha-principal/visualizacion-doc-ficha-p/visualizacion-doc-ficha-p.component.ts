import { ChangeDetectorRef, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AccionCustom } from 'app/core/components/acciones-usuario/accionCustom.model';
import { AccionesUsuarioComponent } from 'app/core/components/acciones-usuario/acciones-usuario.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { VisualizarImagenService } from 'app/core/components/visualizar-imagen/visualizar-imagen.service';
import { VisualizarPdfService } from 'app/core/components/visualizar-pdf/visualizar-pdf.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { DocumentoService } from 'app/core/services/documento.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { environment } from 'environments/environment';
import { DocumentoDTOTablaFicha } from './DocumentoDTOTablaFicha.model';
import { EditarDocFichaPComponent } from '../editar-doc-ficha-p/editar-doc-ficha-p.component';
import { FichaDeIdentificacionDocumentoDTO } from 'app/core/model/request/ia/FichaDeIdentificacionDocumentoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';

@Component({
  selector: 'app-visualizacion-doc-ficha-p',
  standalone: true,
  imports: [MatPaginatorModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    AccionesUsuarioComponent,
    FormsModule,
    MatTooltipModule,],
  templateUrl: './visualizacion-doc-ficha-p.component.html',
  styleUrl: './visualizacion-doc-ficha-p.component.scss'
})
export class VisualizacionDocFichaPComponent {
  @Output() buscarEvent = new EventEmitter<string>();
  @Output() pageEvent = new EventEmitter<PageEvent>();
  @Output() eliminacionDocumento = new EventEmitter<DocumentoDTOTablaFicha>();
  @Output() edicionEvent = new EventEmitter<boolean>();
  @Output() agregarEvent = new EventEmitter<boolean>();

  fichaDeIdentificacionDocumentoDTO: FichaDeIdentificacionDocumentoDTO[];

  @Input({ required: true }) declare nemonicoMenu: string;
  @Input() tituloBotonAgregar = "Agregar nueva foto";

  @Input() butonAgregar = false;

  totalItems = 0;
  textoBuscar: string;

  dataSource: MatTableDataSource<DocumentoDTOTablaFicha> = new MatTableDataSource();

  pageSizeList = [5, 10, 15, 20]
  pageSize = this.pageSizeList[0];
  page = 0;

  // private funcionesUtils = new FuncionesUtils();

  @ViewChild(MatPaginator) paginator: MatPaginator;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombre: "Nombre",
    tipo: "Tipo",
    documentoDe: "Documento de",
    descripcion: "Descripción",
    mime_type: "Extensión",
    tamanioBytes: "Tamaño",
    fechaCreacion: "Fecha creación"
  }

  accionesCustom: AccionCustom[] = [
    {
      icono: "cloud_download",
      nombre: "Descargar",
      clave: "Descargar"
    },
    {
      icono: "visibility",
      nombre: "Ver documento",
      clave: "Ver"
    }
  ]

  constructor(private cdRef: ChangeDetectorRef,
    private documentoService: DocumentoService,
    private dialogMensajeService: DialogMensajeService,
    private visualizarPdfService: VisualizarPdfService,
    private visualizarImagenService: VisualizarImagenService,
    private dialogService: MatDialog,
    private funcionesUtils: FuncionesUtils,
  ) { }

  ngOnInit(): void {
    this.dataSource.data = [];
    this.dataSource.paginator = this.paginator;
  }

  onAdd() {
    this.agregarEvent.emit(true);
  }

  actualizarTabla(fichaDeIdentificacionDocumentoDTOList: FichaDeIdentificacionDocumentoDTO[], totalItems: number) {
    this.fichaDeIdentificacionDocumentoDTO = fichaDeIdentificacionDocumentoDTOList;
    this.dataSource.data = fichaDeIdentificacionDocumentoDTOList?.map(
      (fichaDeIdentificacionDocumentoDTO, index) => {
        let docDto = fichaDeIdentificacionDocumentoDTO.documentoDTO;
        let tipo = docDto.tipoDocumentoSistema;
        let textoTipo = tipo?.nombre;
        if (tipo?.nemonico == etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS) {
          textoTipo = docDto.tipoDeDocumentoSistemaOtro + " (" + tipo?.nombre + ")";
        }
        let tipoDocfichaPrincipal = fichaDeIdentificacionDocumentoDTO.tipoDeDocumentoFichaDeIdentificacion;

        let objeto: DocumentoDTOTablaFicha = {
          numero: totalItems - (index + (this.page * this.pageSize)),
          nombre: docDto.nombre,
          tipo: textoTipo,
          descripcion: docDto.descripcion,
          mime_type: docDto.mimeType,
          tamanioBytes: this.funcionesUtils.formatBytes(docDto.tamanioBytes),
          fechaCreacion: this.getLocalDate(docDto.fechaCreacion),
          tokenIdentificador: docDto.tokenIdentificador,
          documentoDe: tipoDocfichaPrincipal?.nombre,

          tokenFichaPrincipalDocumento: fichaDeIdentificacionDocumentoDTO.tokenIdentificador
        }

        return objeto;
      }
    );
    this.totalItems = totalItems;
    this.cdRef.detectChanges();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  buscar() {
    this.emitirValor(this.textoBuscar);
  }

  accionEvent(event: string, documentoTabla: DocumentoDTOTablaFicha) {
    if (!environment.production) {
      console.log(event);
    }

    if (event == "Ver" || event == "Descargar") {
      this.verDocumento(documentoTabla, event);
    } else if (event == "eliminar") {
      this.eliminarDocumento(documentoTabla);
    } else if (event == "editar") {

      let ref = this.dialogService.open(EditarDocFichaPComponent,
        {
          panelClass: ["w-full", "h-3/4"]
        }
      );

      ref.componentInstance.fichaDeIdentificacionDocumentoDTO = this.fichaDeIdentificacionDocumentoDTO.find(
        (doc) => doc.tokenIdentificador == documentoTabla.tokenFichaPrincipalDocumento);
      ref.componentInstance.nemonicoMenu = this.nemonicoMenu;

      ref.afterClosed().subscribe(
        {
          next: (exito: boolean) => {
            this.edicionEvent.emit(exito);
          }
        }
      );
    }
  }

  private eliminarDocumento(documentoTabla: DocumentoDTOTablaFicha) {
    let dilog = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el documento: " + documentoTabla.nombre + "?",
      "Este proceso es irreversible deseas continuar?"
    );

    dilog.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el documento: " + documentoTabla.nombre + " ..");
            this.documentoService.eliminarDocumento(
              documentoTabla.tokenIdentificador,
              this.nemonicoMenu
            ).subscribe(
              {
                next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
                  load.close();
                  if (!environment.production) {
                    console.log(response);
                  }

                  if (!response.exito) {
                    this.documentoService.checkError(response);
                    return;
                  }

                  this.eliminacionDocumento.emit(documentoTabla);
                },
                error: (error: any) => {
                  load.close();
                  this.documentoService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  verDocumento(documentoTabla: DocumentoDTOTablaFicha, accion: "Descargar" | "Ver") {
    let load = this.dialogMensajeService.mensajeLoading(
      "Recuperando el documento: " + documentoTabla.nombre
    );

    this.documentoService.obtenerDocumento(
      documentoTabla.tokenIdentificador,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: ArrayBuffer) => {
          load.close();
          let nombreArchivo = documentoTabla.nombre;
          const blob = new Blob([response], { type: documentoTabla.mime_type });

          if (accion == "Descargar") {
            const url = window.URL.createObjectURL(blob);
            window.open(url);
          } else if (accion == "Ver") {
            let base64Encoded = this.funcionesUtils.arrayBufferToBase64(response);
            if (documentoTabla.mime_type.includes("pdf")) {
              this.visualizarPdfService.abrirVistaPdf(
                base64Encoded, nombreArchivo
              );
            } else {
              this.visualizarImagenService.abrirVista(
                "data:image/png;base64," + base64Encoded, nombreArchivo
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

  private emitirValor(valor: string) {
    this.buscarEvent.emit(valor);
  }

  onPaginateChange(event: PageEvent) {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;

    this.pageEvent.emit(event);
  }

  getLocalDate(date: Date) {
    return this.funcionesUtils.getLocalDate(date);
  }
}
