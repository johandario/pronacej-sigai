import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { AccionesUsuarioComponent } from '../../acciones-usuario/acciones-usuario.component';
import { AccionCustom } from '../../acciones-usuario/accionCustom.model';

@Component({
  selector: 'app-vista-documentos-subidos',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    AccionesUsuarioComponent
  ],
  templateUrl: './vista-documentos-subidos.component.html',
  styleUrl: './vista-documentos-subidos.component.scss'
})
export class VistaDocumentosSubidosComponent implements OnInit {

  accionesCustom: AccionCustom[] = [
    {
      icono: "visibility",
      nombre: "Ver",
      clave: "ver"
    }
  ]
  dataSource = new MatTableDataSource<DocumentoDTO>();

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  @Output() editarDocumentoEvent = new EventEmitter<{ documentoDTO: DocumentoDTO, index: number, numero: number }>();
  @Output() eliminarDocumentoEvent = new EventEmitter<{ documentoDTO: DocumentoDTO, index: number, numero: number }>();
  @Output() verDocumentoEvent = new EventEmitter<{ documentoDTO: DocumentoDTO, index: number, numero: number }>();


  @ViewChild(MatPaginator) paginator: MatPaginator;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombre: "Nombre",
    tamanioBytes: "Tamaño bytes",
    tipoDeDocumentoSistemaNombre: "Tipo de documento",
    mimeType: "Tipo de documento",
    descripcion: "Descripción",
    tipoDeDocumentoSistemaOtro: "Tipo de documento otro"
  };

  constructor(private accionesSheet: MatBottomSheet,
    private cdRef: ChangeDetectorRef,

  ) { }

  ngOnInit(): void {
  }

  actualizarLista(documentosDTO: DocumentoDTO[]) {
    this.dataSource.data = documentosDTO;
    this.dataSource.paginator = this.paginator;
    this.totalItems = documentosDTO.length;

    this.cdRef.detectChanges();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  accionEvent(result: "editar" | "eliminar" | string, index: number, numero: number) {

    let documentoDTO = this.dataSource.data[index]
    if (result == "editar") {
      this.editarDocumentoEvent.emit({ documentoDTO: documentoDTO, index, numero });
    } else if (result == "eliminar") {
      this.eliminarDocumentoEvent.emit({ documentoDTO: documentoDTO, index, numero });
    }
    else if (result == "ver") {
      this.verDocumentoEvent.emit({ documentoDTO: documentoDTO, index, numero });
    }
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
  }
}
