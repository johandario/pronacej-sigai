import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { AccionesUsuarioComponent } from '../../acciones-usuario/acciones-usuario.component';
import { AccionCustom } from '../../acciones-usuario/accionCustom.model';
import { DocumentoDTOFichaPrincipal } from 'app/core/model/both/documento/documentoDTOFichaPrincipal.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-subida-de-documentos-sistema',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    AccionesUsuarioComponent
  ],
  templateUrl: './subida-de-documentos-sistema.component.html',
  styleUrl: './subida-de-documentos-sistema.component.scss'
})
export class SubidaDeDocumentosSistemaComponent implements OnInit {

  accionesCustom: AccionCustom[] = [
    {
      icono: "visibility",
      nombre: "Ver",
      clave: "ver"
    }
  ]

  dataSource = new MatTableDataSource<DocumentoDTOFichaPrincipal>();

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  @Output() editarDocumentoEvent = new EventEmitter<{ documentoDTO: DocumentoDTOFichaPrincipal, index: number, numero: number }>();
  @Output() eliminarDocumentoEvent = new EventEmitter<{ documentoDTO: DocumentoDTOFichaPrincipal, index: number, numero: number }>();
  @Output() verDocumentoEvent = new EventEmitter<{ documentoDTO: DocumentoDTOFichaPrincipal, index: number, numero: number }>();


  @ViewChild("paginator") paginator: MatPaginator;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombre: "Nombre",
    tamanioBytes: "Tamaño bytes",
    tipoDeDocumentoFichaPrincipal: "Documento",
    tipoDeDocumentoSistemaNombre: "Tipo de documento",
    mimeType: "Tipo de documento",
    descripcion: "Descripción",
    tipoDeDocumentoSistemaOtro: "Tipo de documento otro"
  };

  constructor(private funcionesUtils: FuncionesUtils,
    private cdRef: ChangeDetectorRef,
  ) { }

  ngOnInit(): void {
  }

  actualizarLista(documentosDTO: DocumentoDTOFichaPrincipal[]) {
    this.dataSource.data = documentosDTO;
    this.totalItems = documentosDTO.length;
   
    this.dataSource.paginator = this.paginator;

    this.cdRef.detectChanges();

  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  getBytes(valor: number) {
    return this.funcionesUtils.formatBytes(valor);
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

