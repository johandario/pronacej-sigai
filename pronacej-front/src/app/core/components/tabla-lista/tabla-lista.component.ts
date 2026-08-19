import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import * as XLSX from 'xlsx';
import moment from 'moment';
import { MatTooltipModule } from '@angular/material/tooltip';
import etiquetasModel from 'app/core/etiquetas.model';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import { ActivatedRoute } from '@angular/router';

export interface EstadoBorrador {
  prop1: string;
  prop2?: string;
  nemonico: string;
}
@Component({
  selector: 'app-tabla-lista',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatFormFieldModule
  ],
  templateUrl: './tabla-lista.component.html',
  styleUrl: './tabla-lista.component.scss'
})
export class TablaListaComponent<T> implements OnChanges {
  private permisosService = inject(PermisoRolUsuarioService);    
  private route = inject(ActivatedRoute);    

  @Input() title: string = "";
  @Input() dataList: T[] = [];
  @Input() keyLabels: { [key: string]: string } = {};
  @Input() pagination: Paginacion = new Paginacion();
  @Input() agregar: boolean = true;
  @Input() editar: boolean = true;
  @Input() eliminar: boolean = true;
  @Input() bloqueo: boolean = true;
  @Input() visualizar: boolean = true;
  @Input() icono: string;
  @Input() tooltip: string;
  @Input() icono2: string;
  @Input() tooltip2: string;
  @Input() icono3: string;
  @Input() tooltip3: string;
  @Input() icono4: string;
  @Input() tooltip4: string;
  @Input() editarVer: boolean = false;
  @Input() editarYVer: boolean = false;
  @Input() visualizarCampo: string;
  @Input() eliminarCampo: string = '';
  @Input() soloFecha: boolean = false;
  @Input() excel: boolean = true;
  @Input() nemonicoMenu!: string;
  @Input() validarHistorico: boolean = false;
  @Input() estadoBorrador: EstadoBorrador;

  @Output() refresh = new EventEmitter<void>();
  @Output() add = new EventEmitter<void>();
  @Output() pdf = new EventEmitter<void>();
  @Output() xlsx = new EventEmitter<void>();
  @Output() view = new EventEmitter<T>();
  @Output() edit = new EventEmitter<T>();
  @Output() delete = new EventEmitter<T>();
  @Output() block = new EventEmitter<T>();
  @Output() search = new EventEmitter<string>();
  @Output() sortChange = new EventEmitter<Sort>();
  @Output() pageChange = new EventEmitter<PageEvent>();
  @Output() customAction = new EventEmitter<T>();
  @Output() customAction2 = new EventEmitter<T>();
  @Output() customAction3 = new EventEmitter<T>();
  @Output() customAction4 = new EventEmitter<T>();
  @Output() requestAllData = new EventEmitter<void>();

  dataSource: CdkTableDataSourceInput<T>;
  searchTerm: string = '';

  etiquetaEditar = etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR;
  etiquetaEliminar = etiquetasModel.ACCIONES_MENU_PERMISO_ELIMINAR;

  ngOnInit() {
    this.obtenerPermisos();
    this.dataSource = this.dataList;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['dataList'] && changes['dataList'].currentValue) {
      this.dataSource = this.dataList;
    }
  }

  onRequestAllData() {
    this.requestAllData.emit();
  }

  obtenerPermisos() {
    if (!this.nemonicoMenu) {
      return;
    }

    this.agregar = this.permisosService.hasPermission(
      this.nemonicoMenu,
      etiquetasModel.ACCIONES_MENU_PERMISO_AGREGAR
    );   

  }

  exportXLSX(data: T[]) {
    // Primero, transformamos los datos para que las claves se mapeen a los títulos definidos en keyLabels
    const transformedData = data.map((item, i) => {
      const transformedItem: any = {};

      // Recorremos las claves de keyLabels y excluimos 'acciones'
      Object.keys(this.keyLabels).forEach(key => {
        if (key !== 'acciones') { // Excluir 'acciones'
          const label = this.keyLabels[key] || key; // Si no hay un label, usamos la clave original

          // Manejo de campos especiales
          if (key === 'numero') {
            transformedItem[label] = this.pagination.totalItems - (i + (this.pagination.pageIndex * this.pagination.pageSize));
          } else if (key.includes('fecha')) {
            transformedItem[label] = this.getLocalDate(item[key]);
          } else if (typeof item[key] === 'boolean') { // Si el valor es booleano
            transformedItem[label] = item[key] ? 'Sí' : 'No'; // Reemplazar true/false por 'Si'/'No'
          } else {
            transformedItem[label] = item[key];
          }
        }
      });

      return transformedItem;
    });

    // Convertimos los datos a una hoja de trabajo de Excel
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet([]);

    // Agregar una fila de título antes de la tabla
    const title = "Listado de " + this.title;
    ws['A1'] = { t: 's', v: title }; // Texto en la celda A1
    ws['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: Object.keys(this.keyLabels).length - 1 } }]; // Fusiona celdas

    // Aplicamos estilo en negrita al título
    ws['A1'].s = { font: { bold: true, sz: 14 } };

    // Agregar encabezados con estilos en negrita
    const headers = Object.values(this.keyLabels).filter(label => label !== 'Acciones');
    XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A2' });

    // Aplicar negrita a los encabezados
    headers.forEach((_, index) => {
      const cellAddress = XLSX.utils.encode_cell({ r: 1, c: index });
      if (!ws[cellAddress]) ws[cellAddress] = { t: 's', v: headers[index] };
      ws[cellAddress].s = { font: { bold: true } };
    });

    // Agregar los datos debajo de los encabezados
    XLSX.utils.sheet_add_json(ws, transformedData, { origin: 'A3', skipHeader: true });

    // Definir anchos de columnas
    ws['!cols'] = [
      { wch: 20 },
      { wch: 25 },
      { wch: 30 },
      { wch: 20 }
    ];

    // Crear el libro y agregar la hoja de trabajo
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Datos');

    // Guardar el archivo Excel
    const fileName = `${this.title}.xlsx`;
    XLSX.writeFile(wb, fileName);
  }

  onSortChange(event: Sort) {
    this.sortChange.emit(event);
  }

  onSearch() {
    this.search.emit(this.searchTerm);
  }

  onRefresh() {
    this.refresh.emit();
  }

  onAdd() {
    this.add.emit();
  }

  onPDF() {
    this.add.emit();
  }

  viewRow(row: T) {
    this.view.emit(row);
  }

  editRow(row: T) {
    this.edit.emit(row);
  }

  deleteRow(row: T) {
    this.delete.emit(row);
  }

  blockRow(row: T) {
    this.block.emit(row);
  }

  onAction(row: T) {
    this.customAction.emit(row);
  }

  onAction2(row: T) {
    this.customAction2.emit(row);
  }

  onAction3(row: T) {
    this.customAction3.emit(row);
  }

  onAction4(row: T) {
    this.customAction4.emit(row);
  }

  onPageChange(event: PageEvent) {
    this.pageChange.emit(event);
  }

  getKeys() {
    return Object.keys(this.keyLabels);
  }

  getLocalDate(date: Date) {
    return moment(date).format("DD-MM-YYYY HH:mm:ss");
  }

  getLocalDateSoloFecha(date: Date) {
    return moment(date).format("YYYY/MM/DD");
  }
}
