import { Component, LOCALE_ID, ViewChild } from '@angular/core';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioCrearEditarComponent } from './funcionario-crear-editar/funcionario-crear-editar.component';
import { FuncionarioVisualizarComponent } from './funcionario-visualizar/funcionario-visualizar.component';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { MAT_DATE_LOCALE } from '@angular/material/core';

@Component({
  selector: 'app-funcionario',
  standalone: true,
  imports: [
    FuncionarioCrearEditarComponent,
    MatExpansionModule,
    FuncionarioVisualizarComponent
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },    
    { provide: LOCALE_ID, useValue: 'es' },
    { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() }     
  ],
  templateUrl: './funcionario.component.html',
  styleUrl: './funcionario.component.scss'
})
export class FuncionarioComponent {

  esEdicion = false;

  @ViewChild("funcionarioCrearComp") funcionarioCrearComp: FuncionarioCrearEditarComponent;
  @ViewChild("visulizacionMatExpComp") visulizacionMatExpComp: MatExpansionPanel;
  @ViewChild("creacionComp") creacionComp: MatExpansionPanel;
  @ViewChild("visulizacionComp") visulizacionComp: FuncionarioVisualizarComponent;

  editarFuncionarioEvent(creacionDeFuncionarioEditar: FuncionarioDTO) {
    this.esEdicion = true;
    this.visulizacionMatExpComp.close();
    this.creacionComp.open();
    this.funcionarioCrearComp.empezarEdicion(creacionDeFuncionarioEditar);
  }
  
  completoOperacion(estado: boolean) {
    if (estado) {
      this.visulizacionComp.obtenerFuncionarios();
    }
  }

  canceloEdicion(edicion: Boolean) {
    this.esEdicion = !edicion;
  }
}

export function getEspPaginatorIntl() {
  const paginatorIntl = new MatPaginatorIntl();
  
  paginatorIntl.itemsPerPageLabel = 'Elementos por página:';
  paginatorIntl.firstPageLabel = 'Ir al inicio';
  paginatorIntl.nextPageLabel = 'Siguiente';
  paginatorIntl.previousPageLabel = 'Anterior';
  paginatorIntl.lastPageLabel = 'Ir al final';
  // paginatorIntl.getRangeLabel = EspRangeLabel;

  paginatorIntl.getRangeLabel = (page: number, pageSize: number, length: number) =>  {
    if (length === 0 || pageSize === 0) {
      return `0 / ${length}`;
    }
    length = Math.max(length, 0);
    const startIndex = page * pageSize;
    const endIndex = startIndex < length ? Math.min(startIndex + pageSize, length) : startIndex + pageSize;
    return `${startIndex + 1} - ${endIndex} de ${length}`;
  }
  ;
  
  return paginatorIntl;
}
