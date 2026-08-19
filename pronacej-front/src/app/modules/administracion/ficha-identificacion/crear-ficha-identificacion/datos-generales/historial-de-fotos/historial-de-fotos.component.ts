import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { AccionesUsuarioComponent } from 'app/core/components/acciones-usuario/acciones-usuario.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { HistorialDeFotosFichaIdentificacionDTO } from 'app/core/model/both/ia/HistorialDeFotosFichaIdentificacionDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { environment } from 'environments/environment';
import { SubidaDeFotoDialogComponent } from './subida-de-foto-dialog/subida-de-foto-dialog.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { HistorialDeFotosFichaIdentificacionService } from 'app/modules/administracion/services/HistorialDeFotosFichaIdentificacionService.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { HistorialDeFotosFichaIdentificacionRequest } from 'app/core/model/request/ia/HistorialDeFotosFichaIdentificacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';

@Component({
  selector: 'app-historial-de-fotos',
  standalone: true,
  imports: [
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    FormsModule,
    ReactiveFormsModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './historial-de-fotos.component.html',
  styleUrl: './historial-de-fotos.component.scss'
})
export class HistorialDeFotosComponent implements OnInit, AfterViewInit {

  @Input() declare tokenIdentificadorFichaIdentificacion: string;
  @Input() declare nemonicoMenu: string;

  searchTerm: string;

  totalItems = 0;
  pageSizeOptions = [5, 10, 15, 20];
  pageSize = this.pageSizeOptions[0];
  pageIndex = 0;

  dataList: HistorialDeFotosFichaIdentificacionDTO[] = [];
  dataSource: MatTableDataSource<any>;
  listaDeTiposDeDocumentos: TipoDeDocumento[];

  @ViewChild("docuSubidosTabla") docuSubidosTabla: DocumentosSubidosTablaComponent;

  constructor(private catalogoService: CatalogoService,
    private dialogService: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private historialDeFotosFichaIdentificacionService: HistorialDeFotosFichaIdentificacionService,
    private fichaIdentificacionService: FichaIdentificacionService
  ) { }

  ngAfterViewInit(): void {
    this.obtenerDatos();
  }

  ngOnInit(): void {
    this.obtenerTiposDeDocumentos();
  }

  obtenerTiposDeDocumentos() {
    let nemonico = etiquetasModel.NEMONICO_TIPO_HISTORIAL_ARCHIVOS;
    this.catalogoService.obtenerHijos(
      nemonico, this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {

          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }

          this.listaDeTiposDeDocumentos = response.data?.sort(
            (a, b) => {
              if (a.nombre < b.nombre) { return -1; }
              if (a.nombre > b.nombre) { return 1; }
              return 0;
            }
          )?.map(
            (cat) => {
              let tipoDoc = cat as TipoDeDocumento;
              return tipoDoc;
            }
          );
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  onAdd() {
    let ref = this.dialogService.open(SubidaDeFotoDialogComponent,
      {
        data: {
          tiposDeDocumentos: this.listaDeTiposDeDocumentos
        },
        height: "87vh",
        width: "85vh",
        panelClass: ["w-full"]
      }
    );

    ref.afterClosed().subscribe(
      {
        next: (documentosSubidos: DocumentoSubido[]) => {
          if (documentosSubidos?.length > 0) {

            for (let i = 0; documentosSubidos.length > i; i++) {

              let doc = documentosSubidos[i];
              let hist = new HistorialDeFotosFichaIdentificacionDTO();
              let lod = this.dialogMensajeService.mensajeLoading("Subiendo el archivo: " +
                doc.documentoDTO.nombre
              );

              hist.documentoDTO = doc.documentoDTO;
              hist.tipo = doc.documentoDTO.tipoDocumentoSistema;
              hist.fichaIdentificacionDTO = new FichaIdentificacionDTO();
              hist.fichaIdentificacionDTO.tokenIdentificador = this.tokenIdentificadorFichaIdentificacion;

              this.historialDeFotosFichaIdentificacionService.subirHistorial(
                hist, doc.documento, this.nemonicoMenu
              ).subscribe(
                {
                  next: (response: RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>) => {
                    lod.close();
                    if (!response.exito) {
                      this.historialDeFotosFichaIdentificacionService.checkError(response);
                      return;
                    }

                    this.fichaIdentificacionService.actualizacionFotoPerfil(true);
                    this.obtenerDatos();
                  },
                  error: (error: any) => {
                    lod.close();
                    this.historialDeFotosFichaIdentificacionService.checkError(error);
                  }
                }
              );
            }

          }
        }
      }
    );
  }

  obtenerDatos() {
    let historialDeFotosFichaIdentificacionRequest = new HistorialDeFotosFichaIdentificacionRequest();
    historialDeFotosFichaIdentificacionRequest.filtroBusqueda = this.docuSubidosTabla.textoBuscar;
    historialDeFotosFichaIdentificacionRequest.page =
      this.docuSubidosTabla.page;
    historialDeFotosFichaIdentificacionRequest.size = this.docuSubidosTabla.pageSize;
    historialDeFotosFichaIdentificacionRequest.tokenIdentificadorFichaDeIdentificacion =
      this.tokenIdentificadorFichaIdentificacion;

    this.historialDeFotosFichaIdentificacionService.obtener(historialDeFotosFichaIdentificacionRequest,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>>) => {

          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.historialDeFotosFichaIdentificacionService.checkError(response);
            return;
          }

          let paginacionResponse = response.data;
          this.dataList = paginacionResponse.data;
          if (paginacionResponse.totalItems > 0) {
            let docus: DocumentoDTO[] = paginacionResponse.data.map(
              (doc) => doc.documentoDTO
            );
            this.docuSubidosTabla.actualizarTabla(
              docus,
              paginacionResponse.totalItems
            );
          }
        },
        error: (error: any) => {
          this.historialDeFotosFichaIdentificacionService.checkError(error);
        }
      }
    );

  }

  onPageChange(event: PageEvent) {
    this.docuSubidosTabla.page = event.pageIndex
    this.docuSubidosTabla.pageSize = event.pageSize;
    this.obtenerDatos();

  }

  buscarEvent(event: any) {
    this.obtenerDatos();
  }

  eliminacionDocumento(documentoDTO: DocumentoDTO) {
    if (documentoDTO?.tokenIdentificador) {
      let hist = this.dataList.find(
        (hist) => hist.documentoDTO.tokenIdentificador == documentoDTO.tokenIdentificador
      );
      let historialDeFotosFichaIdentificacionDTO = new HistorialDeFotosFichaIdentificacionDTO();
      historialDeFotosFichaIdentificacionDTO.tokenIdentificador = hist.tokenIdentificador;

      this.historialDeFotosFichaIdentificacionService.eliminar(
        historialDeFotosFichaIdentificacionDTO,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (response: RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>) => {
            if (!environment.production) {
              console.log(response);
            }

            if (!response.exito) {
              this.historialDeFotosFichaIdentificacionService.checkError(response);
              return;
            }

            this.obtenerDatos();
          },
          error: (error: any) => {
            this.historialDeFotosFichaIdentificacionService.checkError(error);
          }
        }
      );
    }
  }

  edicionEvent(event: boolean) {
    if (event) {
      this.obtenerDatos();
    }
  }
}
