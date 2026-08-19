import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CargosJerarquiaDTO } from 'app/core/model/both/cargosJerarquiaDTO.model';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { CargosJerarquiaService } from 'app/modules/seguridad/services/cargosJerarquia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-cargos-ver',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TablaDatosComponent,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule
  ],
  templateUrl: './cargos-ver.component.html',
  styleUrl: './cargos-ver.component.scss'
})
export class CargosVerComponent implements OnInit {

  paginacion: Paginacion = new Paginacion();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_CARGOS_JERARQUIA;
  searchTerm: string = '';

  listaDeCargos: CargosJerarquiaDTO[] = [];
  dataSource: CdkTableDataSourceInput<CargosJerarquiaDTO>;

  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;
  @Output() editarRolEvent = new EventEmitter<CreacionDeRol>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombre: "Nombre",
    esJefe: "Es jefe",
    // departamento: "Departamento"
  };

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private cargosJerarquiaService: CargosJerarquiaService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Initialize default pagination values
    this.paginacion.pageSize = 10; // Default page size
    this.paginacion.pageIndex = 0; // Start at first page

    // Initialize pagination request
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.direction = 'desc'; // Default sort direction
    this.paginacionRequest.sort = 'idCargosJerarquia'; // Default sort field

    // Load initial data
    this.obtenerCargos();
  }

  eliminarCargo(cargosJerarquiaDTO: CargosJerarquiaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + cargosJerarquiaDTO.nombre + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el cargo..");
            this.cargosJerarquiaService.eliminarCargoJerarquia(cargosJerarquiaDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerCargos();
                },
                error: (error: any) => {
                  load.close();
                  console.error('Error al eliminar cargo:', error);
                  this.authSerguridadServicio.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerCargos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    // Asegúrate de que siempre haya valores para sort y direction
    if (!this.paginacionRequest.sort) {
      this.paginacionRequest.sort = 'idCargosJerarquia';
    }
    if (!this.paginacionRequest.direction) {
      this.paginacionRequest.direction = 'desc';
    }

    // Show loading indicator
    let loading = this.dialogMensajeService.mensajeLoading("Cargando datos...");


    this.cargosJerarquiaService.obtenerCargosJerarquiasPaginado(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CargosJerarquiaDTO>>) => {
          loading.close();

          if (!environment.production) {
            console.log('Respuesta del servicio:', response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaDeCargos = response.data.data;
          this.dataSource = this.listaDeCargos;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          loading.close();
          console.error('Error al obtener cargos:', error);
          this.dialogMensajeService.mensajeErrorConTitulo('Error', 'Ocurrió un error al obtener los cargos. Por favor, intente nuevamente.');
          this.cargosJerarquiaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;

    // Asegúrate de que siempre haya valores para sort y direction
    if (!this.paginacionRequest.sort) {
      this.paginacionRequest.sort = 'idCargosJerarquia';
    }
    if (!this.paginacionRequest.direction) {
      this.paginacionRequest.direction = 'desc';
    }

    // Show loading indicator
    let loading = this.dialogMensajeService.mensajeLoading("Cargando datos...");


    this.cargosJerarquiaService.obtenerCargosJerarquiasPaginado(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CargosJerarquiaDTO>>) => {
          loading.close();

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          loading.close();
          console.error('Error al obtener cargos:', error);
          this.dialogMensajeService.mensajeErrorConTitulo('Error', 'Ocurrió un error al obtener los cargos. Por favor, intente nuevamente.');
          this.cargosJerarquiaService.checkError(error);
        }
      }
    );
  }

  agregarCargo() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarCargo(cargosJerarquiaDTO: CargosJerarquiaDTO) {
    this.router.navigate(['editar'], { state: { item: cargosJerarquiaDTO }, relativeTo: this.route });
  }

  handlePageEvent(event: PageEvent) {
    this.paginacion.pageSize = event.pageSize;
    this.paginacion.pageIndex = event.pageIndex;
    this.obtenerCargos();
  }

  handleSortEvent(event: Sort) {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    }
    else {
      // If no direction is specified, set default sort values
      this.paginacionRequest.sort = 'idCargosJerarquia';
      this.paginacionRequest.direction = 'desc';
    }
    this.obtenerCargos();
  }

  handleSearchEvent(filter: string) {
    // Reset pagination to first page when searching
    this.paginacion.pageIndex = 0;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.filter = filter;

    if (filter && filter.trim().length > 0) {
      this.obtenerCargosValor();
    } else {
      // If search is empty, get all cargos
      this.obtenerCargos();
    }
  }

  obtenerCargosValor() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    // Asegúrate de que siempre haya valores para sort y direction
    if (!this.paginacionRequest.sort) {
      this.paginacionRequest.sort = 'idCargosJerarquia';
    }
    if (!this.paginacionRequest.direction) {
      this.paginacionRequest.direction = 'desc';
    }

    // Show loading indicator
    let loading = this.dialogMensajeService.mensajeLoading("Buscando...");
    console.log(this.paginacionRequest);

    this.cargosJerarquiaService.obtenerCargosJerarquiaPorValor(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CargosJerarquiaDTO>>) => {
          loading.close();

          if (!environment.production) {
            console.log('Respuesta de búsqueda:', response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaDeCargos = response.data.data;
          this.dataSource = this.listaDeCargos;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          loading.close();
          console.error('Error al buscar cargos:', error);
          this.dialogMensajeService.mensajeErrorConTitulo('Error', 'Ocurrió un error al buscar los cargos. Por favor, intente nuevamente.');
          this.cargosJerarquiaService.checkError(error);
        }
      }
    );
  }
}