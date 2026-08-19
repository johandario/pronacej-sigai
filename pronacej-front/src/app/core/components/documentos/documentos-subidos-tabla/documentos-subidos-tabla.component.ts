import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, OnDestroy, Output, ViewChild } from '@angular/core';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { AccionCustom } from '../../acciones-usuario/accionCustom.model';
import { AccionesUsuarioComponent } from '../../acciones-usuario/acciones-usuario.component';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FormsModule } from '@angular/forms';
import etiquetasModel from 'app/core/etiquetas.model';
import { environment } from 'environments/environment';
import { DocumentoService } from 'app/core/services/documento.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { DocumentoDTOTabla } from './documentoDTOTabla.model';
import { VisualizarPdfService } from '../../visualizar-pdf/visualizar-pdf.service';
import { VisualizarImagenService } from '../../visualizar-imagen/visualizar-imagen.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { EdicionDocumentoComponent } from '../edicion-documento/edicion-documento.component';
import { TipoDeDocumento } from '../modelos/TipoDeDocumento.model';
import { getEspPaginatorIntl } from 'app/app.component';

@Component({
  selector: 'app-documentos-subidos-tabla',
  standalone: true,
  imports: [
    MatPaginatorModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    AccionesUsuarioComponent,
    FormsModule,
    MatTooltipModule,
  ],
  templateUrl: './documentos-subidos-tabla.component.html',
  styleUrl: './documentos-subidos-tabla.component.scss',
  providers: [
      { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() },
    ],
})
export class DocumentosSubidosTablaComponent implements OnInit, OnDestroy {

  @Output() buscarEvent = new EventEmitter<string>();
  @Output() pageEvent = new EventEmitter<PageEvent>();
  @Output() eliminacionDocumento = new EventEmitter<DocumentoDTO>();
  @Output() edicionEvent = new EventEmitter<boolean>();
  @Output() agregarEvent = new EventEmitter<boolean>();

  private busquedaSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  documentosDTO: DocumentoDTO[];

  @Input({ required: true }) tiposDeDocumentosSistema: TipoDeDocumento[];
  @Input({ required: true }) declare nemonicoMenu: string;
  @Input() tituloBotonAgregar = "Agregar nueva foto";

  @Input() butonAgregar = false;

  totalItems = 0;
  textoBuscar: string;

  dataSource: MatTableDataSource<DocumentoDTOTabla> = new MatTableDataSource();

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

    this.busquedaSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((valor) => {
        this.emitirValor(valor);
      });
  }

  onAdd() {
    this.agregarEvent.emit(true);
  }

  actualizarTabla(documentos: DocumentoDTO[], totalItems: number) {
    this.documentosDTO = documentos;
    this.dataSource.data = documentos?.map(
      (doc, index) => {
        let tipo = doc.tipoDocumentoSistema;
        let textoTipo = tipo?.nombre;
        if (tipo?.nemonico == etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS) {
          textoTipo = doc.tipoDeDocumentoSistemaOtro + " (" + tipo?.nombre + ")";
        }
        let objeto: DocumentoDTOTabla = {
          numero: totalItems - (index + (this.page * this.pageSize)),
          nombre: doc.nombre,
          tipo: textoTipo,
          descripcion: doc.descripcion,
          mime_type: doc.mimeType,
          tamanioBytes: this.funcionesUtils.formatBytes(doc.tamanioBytes),
          fechaCreacion: this.getLocalDate(doc.fechaCreacion),
          tokenIdentificador: doc.tokenIdentificador
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
    this.busquedaSubject.next(this.textoBuscar);
  }

  accionEvent(event: string, documentoTabla: DocumentoDTOTabla) {
    if (!environment.production) {
      console.log(event);
    }

    if (event == "Ver" || event == "Descargar") {
      this.verDocumento(documentoTabla, event);
    } else if (event == "eliminar") {
      this.eliminarDocumento(documentoTabla);
    } else if (event == "editar") {

      let ref = this.dialogService.open(EdicionDocumentoComponent,
        {
          data: {
            tiposDeDocumentosSistema: this.tiposDeDocumentosSistema,
            documentoDTO: this.documentosDTO.find((doc) => doc.tokenIdentificador == documentoTabla.tokenIdentificador),
            nemonicoMenu: this.nemonicoMenu
          }
        }
      );

      ref.afterClosed().subscribe(
        {
          next: (exito: boolean) => {
            this.edicionEvent.emit(exito);
          }
        }
      );
    }
  }

  private eliminarDocumento(documentoTabla: DocumentoDTOTabla) {
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

                  this.eliminacionDocumento.emit(response.data);
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

  verDocumento(documentoTabla: DocumentoDTOTabla, accion: "Descargar" | "Ver") {
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
