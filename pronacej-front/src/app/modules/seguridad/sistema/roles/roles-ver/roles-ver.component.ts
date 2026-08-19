import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
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
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-roles-ver',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TablaListaComponent,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule
  ],
  templateUrl: './roles-ver.component.html',
  styleUrl: './roles-ver.component.scss'
})
export class RolesVerComponent {

  paginacion: Paginacion = new Paginacion();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ROL;
  searchTerm: string = '';
  listaDeRoles: CreacionDeRol[] = [];
  dataSource: CdkTableDataSourceInput<CreacionDeRol>;

  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  @Output() editarRolEvent = new EventEmitter<CreacionDeRol>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    codigo: "Código",
    nombre: "Nombre",
    descripcion: "Descripción",
    diasExpiracionPassword: "Días expiración password",
    esRolPorDefecto: "Rol por defecto",
    esSuperRol: "Super rol"
  };

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private utilidades: FuncionesUtils
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      this.nemonicoMenu
    );
    this.obtenerRoles();
  }

  eliminarRol(creacionDeRol: CreacionDeRol) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + creacionDeRol.nombre + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el rol..");
            this.authSerguridadServicio.eliminarRol(creacionDeRol, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerRoles();
                },
                error: (error: any) => {
                  load.close();

                  this.authSerguridadServicio.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  bloquearODesbloquearRol(creacionDeRol: CreacionDeRol) {
    let text = (creacionDeRol.bloqueadoRelacion ? "\"desbloquear\"" : "\"bloquear\"");
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de " + text +
      " a: \"" + creacionDeRol.nombre + "\".",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let text2 = (creacionDeRol.bloqueadoRelacion ? "\"Desbloqueando\"" : "\"Bloqueando\"");

            let load = this.dialogMensajeService.mensajeLoading(text2 + " el rol..");

            creacionDeRol.bloqueadoRelacion = !creacionDeRol.bloqueadoRelacion
            this.authSerguridadServicio.bloquearRol(creacionDeRol, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerRoles();
                },
                error: (error: any) => {
                  load.close();

                  this.authSerguridadServicio.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  // Modificación del método obtenerRoles en RolesVerComponent
  obtenerRoles() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    // Mantener la solicitud original ya que no hay filtros de fecha en esta llamada
    this.authSerguridadServicio.obtenerRolesValidos(this.paginacionRequest, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>) => {
        if (!environment.production) {
          console.log(response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        this.listaDeRoles = response.data.data;
        this.dataSource = this.listaDeRoles;
        this.paginacion.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        console.log(error);
        this.authSerguridadServicio.checkError(error);
      }
    });
  }

  // Modificar el método obtenerRolesPorValor para incluir el filtrado de fechas
  obtenerRolesPorValor() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    // Guardar el filtro original
    const filtroOriginal = this.paginacionRequest?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si parece ser un filtro de fecha, hacemos el procesamiento en el frontend
    if (esFiltroDeFecha) {
      // Crear una copia de la solicitud sin el filtro
      const solicitudSinFiltro = { ...this.paginacionRequest };
      solicitudSinFiltro.filter = '';

      this.authSerguridadServicio.obtenerRolesValidosPorValor(solicitudSinFiltro, etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe({
        next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          // Aplicar filtro de fecha en el frontend
          // Asumiendo que el campo de fecha se llama 'fechaCreacion'
          // Ajusta el nombre del campo según tu modelo
          let rolesFiltrados = response.data.data;
          if (filtroOriginal) {
            rolesFiltrados = this.utilidades.filtrarPorFecha(response.data.data, filtroOriginal, 'fechaCreacion');
            this.paginacion.totalItems = rolesFiltrados.length;
          } else {
            this.paginacion.totalItems = response.data.totalItems;
          }

          this.listaDeRoles = rolesFiltrados;
          this.dataSource = this.listaDeRoles;
        },
        error: (error: any) => {
          console.log(error);
          this.authSerguridadServicio.checkError(error);
        }
      });
    } else {
      // Si no es un filtro de fecha, usar el método normal
      this.authSerguridadServicio.obtenerRolesValidosPorValor(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_MENU_ROL).subscribe({
        next: (response: RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaDeRoles = response.data.data;
          this.dataSource = this.listaDeRoles;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          console.log(error);
          this.authSerguridadServicio.checkError(error);
        }
      });
    }
  }

  descargarExcelCompleto() {
    // Crear objeto de paginación
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = 100000;
    paginacionRequest.page = 0;

    // Verificar si el filtro parece una fecha
    const filtroOriginal = this.paginacionRequest?.filter || '';
    const esFiltroDeFecha = this.utilidades?.esPosibleFiltroFecha ?
      this.utilidades.esPosibleFiltroFecha(filtroOriginal) : false;

    // Aplicar filtro según condición
    paginacionRequest.filter = esFiltroDeFecha ? '' : filtroOriginal;

    // Aplicar ordenamiento si existe
    if (this.paginacionRequest?.sort) {
      paginacionRequest.sort = this.paginacionRequest.sort;
      paginacionRequest.direction = this.paginacionRequest.direction || 'ASC'; // Valor por defecto
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

  // Modificar el método handleSearchEvent para detectar si es un filtro de fecha
  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerRolesPorValor();
  }

  agregarRol() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarRol(creacionDeRol: CreacionDeRol) {
    this.router.navigate(['editar'], { state: { item: creacionDeRol }, relativeTo: this.route });
  }

  handlePageEvent(event: PageEvent) {
    this.paginacion.pageSize = event.pageSize;
    this.paginacion.pageIndex = event.pageIndex;
    this.obtenerRoles();
  }

  handleSortEvent(event: Sort) {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    }
    else {
      this.paginacionRequest.sort = null;
      this.paginacionRequest.direction = null;
    }
    this.obtenerRoles();
  }

}

