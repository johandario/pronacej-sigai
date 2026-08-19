import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { AdolescenteExternadoDTO } from 'app/core/model/both/ReportesDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { ReporteService } from 'app/modules/seguridad/services/reporte.service';
import { environment } from 'environments/environment';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-adolescentes-externados',
  standalone: true,
  imports: [
    TablaDatosComponent,
    MatFormFieldModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    CommonModule,
    FormsModule
  ],
  templateUrl: './adolescentes-externados.component.html',
  styleUrl: './adolescentes-externados.component.scss'
})
export class AdolescentesExternadosComponent implements OnInit {
  private reporteService = inject(ReporteService);
  private dialogMensajeService = inject(DialogMensajeService);  
  private jerarquiaService = inject(JerarquiaService);  

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;    

  listaDeAdolescentesExternados: AdolescenteExternadoDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  terminoBusqueda: string = '';
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();
  nemonicoMenu = etiquetasModel.NEMONICO_REPORTE_ADOLESCENTES_EXTERNADOS;
  totalItems: number = 0;

  filtroCentro: string = '';
  centros: JerarquiaDTO[] = [];

  etiquetasColumnas: any = {
    numero: "No.",
    nombreCompleto: "Nombre completo",
    numeroIdentificacion: "Número de identificación",
    centro: "Centro",
    numeroExpediente: "Número de expediente",
    fechaIngreso: "Fecha de ingreso",
    fechaSalida: "Fecha de salida",
    motivoIngreso: "Motivo de ingreso",
    motivoSalida: "Motivo de salida",
    // observacionIngreso: "Observación de ingreso",
    // observacionSalida: "Observación de salida",
  };

  constructor() { }

  ngOnInit(): void {
    this.cargarCentros();
    this.obtenerReporte();
  }

  obtenerReporte() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.paginacion?.pageSize || 5;
    paginacionRequest.page = this.paginacion?.pageIndex ?? 0;
    paginacionRequest.filter = this.terminoBusqueda || '';
    paginacionRequest.tokenIdentificador = this.filtroCentro || '';

    this.reporteService.obtenerAdolescentesExternados(paginacionRequest).subscribe({
      next: (response) => {
        if (!environment.production) {
          console.log('Respuesta reportes:', response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        // Procesar los datos recibidos
        let datos = response.data.data;
        this.listaDeAdolescentesExternados = datos;
        this.paginacion.totalItems = response.data.totalItems;
        this.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        console.error('Error al obtener los registros:', error);
      }
    });
  }

  descargarExcelCompleto() {
    if (this.totalItems == 0) return;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.totalItems;
    paginacionRequest.page = 0;
    paginacionRequest.filter = this.terminoBusqueda || '';
    paginacionRequest.tokenIdentificador = this.filtroCentro || '';
    
    this.reporteService.obtenerAdolescentesExternados(paginacionRequest).subscribe({
      next: (response) => {
        if (!environment.production) {
          console.log('Respuesta reportes:', response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        // Procesar los datos recibidos
        let datos = response.data.data;
        this.tablaComponent.exportXLSX(datos);

      },
      error: (error: any) => {
        console.error('Error al obtener los registros:', error);
      }
    });    
  }

  cargarCentros(): void {
    forkJoin({
      centrosSOA: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("SOA", this.nemonicoMenu),
      centrosOTRO: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("CJDR", this.nemonicoMenu)
    }).subscribe({
      next: ({ centrosSOA, centrosOTRO }) => {

        const listaSOA = centrosSOA.exito ? (centrosSOA.data || []) : [];
        const listaOTRO = centrosOTRO.exito ? (centrosOTRO.data || []) : [];

        // combinar resultados
        this.centros = [...listaSOA, ...listaOTRO];

        this.centros.sort((a, b) => a.nombre.localeCompare(b.nombre));

        console.log('Centros combinados:', this.centros);
      },

      error: (error: any) => {
        console.error('Error al cargar centros:', error);
      }
    });
  }

  onRefrescar() {
    this.obtenerReporte();
  }

  onBuscar(termino: string) {
    this.terminoBusqueda = termino;
    this.paginacion.pageIndex = 0; // Volver a la primera página
    this.obtenerReporte();
  }

  onCambiarPagina(evento: PageEvent) {
    this.paginacion.pageSize = evento.pageSize || 5;
    this.paginacion.pageIndex = evento.pageIndex || 0;
    this.obtenerReporte();
  }

  onChangeCentro(nuevoCentroId: string): void {
    this.filtroCentro = nuevoCentroId;
  }
}
