import { Component, OnInit, ViewChild } from '@angular/core';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { PermisoSalidaService } from '../salida-permiso/permiso-salida.service';
import { PermisoSalidaDTO } from 'app/core/model/both/salida/PermisoSalidaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import etiquetasModel from 'app/core/etiquetas.model';



@Component({
  selector: 'app-registro-salida',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './registro-salida.component.html',
  styleUrl: './registro-salida.component.scss'
})
export class RegistroSalidaComponent implements OnInit {
  tituloPantalla: string = "";

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  uuid_fp: string;

  listaProcesos: PermisoSalidaDTO[] = [];
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  paginacion: Paginacion = new Paginacion();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PERMISO_SALIDA;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    idPermisoSalida: "No.",
    acciones: "Acciones",
    nroDocumento: "Nro. de salida",
    usuarioSalida: "Usuario que registra salida",
    fechaHoraSalida: "Fecha inicio",
    fechaHoraRegreso: "Fecha fin",
    nombreFrecuenciSalida: "Frecuencia de salida",
    nombreTipoSalida: "Tipo de salida",

  };

  constructor(
    private router: Router,
    private dialogMensajeService: DialogMensajeService,
    private salidaService: PermisoSalidaService,
    private route: ActivatedRoute,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_PERMISO_DE_SALIDA"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    registerLocaleData(localeEs, 'es-ES');
    this.obtenerProcesos();
  }


  obtenerProcesos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.filter = this.paginacionRequest.filter;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    this.salidaService.obtenerlistadoPorToken(this.paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PermisoSalidaDTO>>) => {
          if (!response.exito) {
            this.salidaService.checkError(response);
            return;
          }
          this.listaProcesos = response.data.data
          console.log(this.listaProcesos);

          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.salidaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    
    this.salidaService.obtenerlistadoPorToken(this.paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PermisoSalidaDTO>>) => {
          if (!response.exito) {
            this.salidaService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.salidaService.checkError(error);
        }
      }
    );
  }

  agregarProceso() {
    const uuid = this.uuid_fp;
    this.router.navigate(['/gestion-adolescente/ficha-identificacion/crear-editar/salidas', uuid, 'crear']);
  }

  editarProceso(proceso: PermisoSalidaDTO) {
    this.router.navigate(
      ['/gestion-adolescente/ficha-identificacion/crear-editar/salidas/ver', proceso.tokenIdentificador],
      {
        queryParams: { uuid_fp: this.uuid_fp }
      }
    );
  }


  eliminarProceso(gestionFugaDTO: PermisoSalidaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar este registro, esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la fuga..");
            this.salidaService.eliminarPermisoSalidas(gestionFugaDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerProcesos();
                },
                error: (error: any) => {
                  load.close();

                  // this.gestionFugaService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;
    this.obtenerProcesos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerProcesos();

  }

  refrescar() {
    this.obtenerProcesos()
  }

  visualizar(proceso: PermisoSalidaDTO) {
    this.router.navigate(
      ['/gestion-adolescente/ficha-identificacion/crear-editar/salidas/ver', proceso.tokenIdentificador],
      {
        queryParams: { uuid_fp: this.uuid_fp, mode: 'ver' }
      }
    );

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
    this.obtenerProcesos();
  }
}
