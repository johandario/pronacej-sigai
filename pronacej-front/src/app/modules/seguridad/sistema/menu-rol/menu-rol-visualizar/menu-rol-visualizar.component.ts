import { Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment.development';

@Component({
  selector: 'app-menu-rol-visualizar',
  standalone: true,
  imports: [
    MatBottomSheetModule,
    MatButtonModule,
    MatIconModule,
    TablaDatosComponent
  ],
  templateUrl: './menu-rol-visualizar.component.html',
  styleUrl: './menu-rol-visualizar.component.scss'
})
export class MenuRolVisualizarComponent implements OnInit {
  listaDeRoles: CreacionDeRol[] = [];
  paginacion: Paginacion = new Paginacion();
  terminoBusqueda: string = '';
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  @Output() editarRolEvent = new EventEmitter<CreacionDeRol>();

  etiquetasColumnas: any = {
    numero: "No.",
    acciones: "Acciones",
    codigo: "Código",
    nombre: "Nombre",
    fechaCreacion: "Fecha Creación"
  };

  constructor(
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private utilidades: FuncionesUtils
  ) { }

  ngOnInit(): void {
    this.obtenerRoles();
  }

  obtenerRoles() {
    // Crear objeto de paginación
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.paginacion?.pageSize || 5;
    paginacionRequest.page = this.paginacion?.pageIndex ?? 0;

    // Verificar si el filtro parece una fecha
    const filtroOriginal = this.terminoBusqueda || '';
    const esFiltroDeFecha = this.utilidades?.esPosibleFiltroFecha ?
      this.utilidades.esPosibleFiltroFecha(filtroOriginal) : false;

    // Aplicar filtro según condición
    paginacionRequest.filter = esFiltroDeFecha ? '' : filtroOriginal;

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort) {
      paginacionRequest.sort = this.solicitudPaginacion.sort;
      paginacionRequest.direction = this.solicitudPaginacion.direction || 'ASC'; // Valor por defecto
    } else {
      // Valores por defecto si no hay ordenamiento
      paginacionRequest.sort = null;
      paginacionRequest.direction = null;
    }

    // Realizar la petición al backend utilizando obtenerRolesValidosPorValor para filtrado
    this.authSerguridadServicio.obtenerRolesValidosPorValor(paginacionRequest, etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>) => {
        if (!environment.production) {
          console.log('Respuesta obtenerRoles:', response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        // Procesar los datos recibidos
        let datos = response.data.data;

        // Si es filtro de fecha, aplicar filtrado local
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaCreacion');
          this.paginacion.totalItems = datos.length;
        } else {
          this.paginacion.totalItems = response.data.totalItems;
        }

        this.listaDeRoles = datos;
      },
      error: (error: any) => {
        console.error('Error al obtener roles:', error);
        //this.authSerguridadServicio.checkError(error);
      }
    });
  }

  descargarExcelCompleto() {
    // Crear objeto de paginación
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = 100000;
    paginacionRequest.page = 0;

    // Verificar si el filtro parece una fecha
    const filtroOriginal = this.terminoBusqueda || '';
    const esFiltroDeFecha = this.utilidades?.esPosibleFiltroFecha ?
      this.utilidades.esPosibleFiltroFecha(filtroOriginal) : false;

    // Aplicar filtro según condición
    paginacionRequest.filter = esFiltroDeFecha ? '' : filtroOriginal;

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort) {
      paginacionRequest.sort = this.solicitudPaginacion.sort;
      paginacionRequest.direction = this.solicitudPaginacion.direction || 'ASC'; // Valor por defecto
    } else {
      // Valores por defecto si no hay ordenamiento
      paginacionRequest.sort = null;
      paginacionRequest.direction = null;
    }

    // Realizar la petición al backend utilizando obtenerRolesValidosPorValor para filtrado
    this.authSerguridadServicio.obtenerRolesValidosPorValor(paginacionRequest, etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        // Procesar los datos recibidos
        let datos = response.data.data;

        // Si es filtro de fecha, aplicar filtrado local
        if (esFiltroDeFecha && filtroOriginal)
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaCreacion');


        this.tablaComponent.exportXLSX(datos);
      },
      error: (error: any) => {
        console.error('Error al obtener roles:', error);
        //this.authSerguridadServicio.checkError(error);
      }
    });
  }

  // Manejadores de eventos para el componente TablaDatosComponent
  onRefrescar() {
    this.terminoBusqueda = '';
    this.solicitudPaginacion = new PaginacionRequest();
    this.obtenerRoles();
  }

  onEditar(rol: CreacionDeRol) {
    this.editarRolEvent.emit(rol);
  }

  onBuscar(termino: string) {
    this.terminoBusqueda = termino;
    this.paginacion.pageIndex = 0; // Volver a la primera página
    this.obtenerRoles();
  }

  onCambiarPagina(evento: PageEvent) {
    this.paginacion.pageSize = evento.pageSize || 5;
    this.paginacion.pageIndex = evento.pageIndex || 0;
    this.obtenerRoles();
  }

  onCambiarOrden(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction.toUpperCase();
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerRoles();
  }
}