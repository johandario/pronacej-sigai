import { Component, EventEmitter, Inject, Input, LOCALE_ID, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink } from '@angular/router';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import moment from 'moment';

@Component({
  selector: 'app-registros-legales',
  standalone: true,
  imports: [
    MatTabsModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatRadioModule,
    MatSlideToggleModule,
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatCardModule,
    MatPaginatorModule,
  ],
  templateUrl: './registros-legales.component.html',
  styleUrl: './registros-legales.component.scss'
})
export class RegistrosLegalesComponent implements OnInit {

  @Input() mandatos: any[] = [];
  @Output() editar = new EventEmitter<any>();

  dataSource: MatTableDataSource<any>;

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  keyLabelsTable: any = {    
    acciones: "Acciones",
    fechaResolucion: "Fecha",
    numResolucion: "Resolución #",
    tipoRegistro: "Tipo",
    situacionJuridica: "Situación",
    tipoVariacion: "Variación",
    fechaInicioMedida: "F. Inicio",
    fechaFinMedida: "F. Fin",
    corteJusticia: "Corte",
    instancia: "Instancia",
    especialidad: "Especialidad",
    organoJurisdiccional: "Órgano",
    montoReparacion: "Monto de reparación civil",
  };  

  constructor(
    public funcionesUtils: FuncionesUtils
  ) {
  }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.mandatos);

  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
  }

  mostrarDatos() {
    this.dataSource = new MatTableDataSource(this.mandatos);
    console.log(this.mandatos);
  }

  editarMandato(mandato: any) {
    this.editar.emit(mandato);
  }
}
