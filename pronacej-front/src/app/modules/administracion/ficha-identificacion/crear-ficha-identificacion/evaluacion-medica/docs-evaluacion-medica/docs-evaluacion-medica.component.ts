import { Component } from '@angular/core';
import { ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { CustomPaginatorIntl } from 'app/core/services/custom-paginator-intl.service';
import { SubirDocumentoComponent } from "./subir-documento/subir-documento.component";

@Component({
  selector: 'app-documentos-evaluacion-medica',
  standalone: true,
  imports: [
    MatProgressBarModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    SubirDocumentoComponent
],
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl },
  ],
  templateUrl: './docs-evaluacion-medica.component.html',
  styleUrl: './docs-evaluacion-medica.component.scss'
})
export class DocsEvaluacionMedicaComponent {
  constructor(
    public dialog: MatDialog,
  ){

  }


  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  isLoading: boolean = true;
  subirDocumento: boolean = false;
  searchInputControl: UntypedFormControl = new UntypedFormControl();

  DOCUMENTOS: any[] = [
    {nombre: 'Documento 1', descripcion: 'Documento medico', tipo: '.pdf', size: '2 mb', fecha: '15/10/2024'},
    {nombre: 'Documento 2', descripcion: 'Documento diagnóstico', tipo: '.pdf', size: '200 kb', fecha: '15/10/2024'},
    {nombre: 'Documento 3', descripcion: 'Documento antecedentes', tipo: '.pdf', size: '250 kb', fecha: '15/10/2024'},
  ];

  columnsDocumentos: string[] = [
    "nombre", "desc", "tipo", "size", "fecha"
  ];


  dataSource = this.DOCUMENTOS;

  toggleSubirDocumento(){
    this.subirDocumento = !this.subirDocumento
  }
  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    //TODO: Obtener evaluaciones médicas
  }
}
