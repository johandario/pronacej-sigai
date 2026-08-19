import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { PermisoDirective } from 'app/core/directives/permiso.directive';
import etiquetasModel from 'app/core/etiquetas.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PermisoRolUsuarioNombresDTO } from 'app/core/model/both/permisoRolUsuario.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-lista-menu-permiso',
  standalone: true,
  imports: [
    MatBottomSheetModule,
    MatButtonModule,
    MatIconModule,
    TablaDatosComponent,
  ],
  templateUrl: './lista-menu-permiso.component.html',
  styleUrl: './lista-menu-permiso.component.scss'
})
export class ListaMenuPermisoComponent implements OnInit {
  listaDeRoles: any[] = [];
  paginacion: Paginacion = new Paginacion();
  terminoBusqueda: string = '';
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PERMISOS;
  totalItems: number = 0;

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;  

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private permisoRolUsuarioService = inject(PermisoRolUsuarioService);
  private dialogMensajeService = inject(DialogMensajeService);
  
  etiquetasColumnas: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha Creación",
    tipoAsignacion: "Tipo de asignación",
    tipoPermiso: "Tipo de permiso",
    nombreFuncionario: "Colaborador",
    nombreRoles: "Roles asignados"
  };

  permitirVer = false;
  permitirEditar = false;

  constructor() {}

  ngOnInit(): void {    
    // this.permitirVer = this.permisoRolUsuarioService.hasPermission(
    //   this.nemonicoMenu,
    //   etiquetasModel.ACCIONES_MENU_PERMISO_VER
    // );

    // this.permitirEditar = this.permisoRolUsuarioService.hasPermission(
    //   this.nemonicoMenu,
    //   etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR
    // );

    this.obtenerPermisos();
  }

  obtenerPermisos() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.paginacion?.pageSize || 5;
    paginacionRequest.page = this.paginacion?.pageIndex ?? 0;
    paginacionRequest.filter = this.terminoBusqueda || '';

    this.permisoRolUsuarioService.obtenerPermisos(paginacionRequest, '').subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<PermisoRolUsuarioNombresDTO>>) => {
        if (!environment.production) {
          console.log('Respuesta obtenerPermisos:', response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        // Procesar los datos recibidos
        let datos = response.data.data;
        // Obtención correcta del tipo de dato fecha
        datos.map(dato => dato.fechaCreacion = new Date(dato.fechaCreacion));
        this.listaDeRoles = datos;        
        this.paginacion.totalItems = response.data.totalItems;
        this.totalItems = response.data.totalItems;

      },
      error: (error: any) => {
        console.error('Error al obtener roles:', error);
      }
    });
  }

  descargarExcelCompleto() {
    if (this.totalItems == 0) return;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.totalItems;
    paginacionRequest.page = 0;
    paginacionRequest.filter = this.terminoBusqueda || '';

    this.permisoRolUsuarioService.obtenerPermisos(paginacionRequest, '').subscribe({
      next: (response) => {
        if (!environment.production) {
          console.log('Respuesta obtenerPermisos:', response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        let datos = response.data.data;
        datos.map(dato => dato.fechaCreacion = new Date(dato.fechaCreacion));
        this.tablaComponent.exportXLSX(datos);

      },
      error: (error: any) => {
        console.error('Error al obtener roles:', error);
      }
    });
  }

  onRefrescar() {
    this.terminoBusqueda = '';
    this.obtenerPermisos();
  }

  onAgregar() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  onEditar(permiso: PermisoRolUsuarioNombresDTO) {
    this.router.navigate(['editar', permiso.tokenIdentificador], { relativeTo: this.route });
  }

  onEliminar(permiso: PermisoRolUsuarioNombresDTO) {  
    const confirmar = this.dialogMensajeService.mensajeConConfirmacion(
      'Confirmar eliminación',
      `¿Está seguro que desea eliminar los permisos asignados al rol/usuario seleccionado?`
    );

    confirmar.afterClosed().subscribe((result) => {
      if (result == "confirmed") {
        this.permisoRolUsuarioService.eliminarPermisos(permiso, this.nemonicoMenu).subscribe(
          {
            next: (response) => {
              if (!environment.production) {
                console.log(response);
              }

              if (!response.exito) {
                this.permisoRolUsuarioService.checkError(response);
                return;
              }

              this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
              this.obtenerPermisos();
              
            },
            error: (error: any) => {
              this.permisoRolUsuarioService.checkError(error);
            }
          }
        );
      }
    });    
  }
  
  onBuscar(termino: string) {
    this.terminoBusqueda = termino;
    this.paginacion.pageIndex = 0; // Volver a la primera página
    this.obtenerPermisos();
  }

  onCambiarPagina(evento: PageEvent) {
    this.paginacion.pageSize = evento.pageSize || 5;
    this.paginacion.pageIndex = evento.pageIndex || 0;
    this.obtenerPermisos();
  }

  onCambiarOrden(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction.toUpperCase();
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerPermisos();
  }
}
