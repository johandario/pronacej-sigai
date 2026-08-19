import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { InformeDTO } from 'app/core/model/both/informe/informeDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { AlertaService } from '../../services/alerta.service';
import { AlertaDTO } from 'app/core/model/both/AlertaDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';

@Component({
  selector: 'app-alertas-ver',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    TablaDatosComponent
  ],
  templateUrl: './alertas-ver.component.html',
  styleUrl: './alertas-ver.component.scss'
})
export class AlertasVerComponent {
  esEdicion: boolean = false;
  uuid_fp: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ALERTAS;
  titulo: string = "alerta";
  listaAlertas: AlertaDTO[] = [];
  base64Image: string | null = null;

  funcionarioActivo: FuncionarioDTO;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;
  @Output() editarInformeEvent = new EventEmitter<InformeDTO>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    tabla: "Tabla",
    campo: "Campo",
    prioridad: "Prioridad",
    activo: "Activo",
    descripcion: "Descripción"
  };

  constructor(
    private alertaService: AlertaService,
    private dialogMensajeService: DialogMensajeService,
    private funcionarioService: FuncionarioService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.obtenerFuncionario();
  }

  obtenerFuncionario() {
    this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {

          if (!response.exito) {
            return;
          }

          this.funcionarioActivo = response.data;
          this.obtenerAlertas();
        },
        error: (error: any) => {
          console.log('Hubo un problema al recuperar los registros. Inténtalo de nuevo.');
        }
      }
    );
  }

  eliminarAlerta(alertaDTO: AlertaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la alerta? esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la alerta..");
            this.alertaService.removerAlerta(alertaDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerAlertas();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al eliminar la alerta. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerAlertas() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.funcionarioActivo.tokenIdentificadorDepartamento;

    this.alertaService.obtenerListaAlertas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<AlertaDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaAlertas = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.funcionarioActivo.tokenIdentificadorDepartamento;

    this.alertaService.obtenerListaAlertas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<AlertaDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  agregarAlerta() {
    this.router.navigate(['crear-editar'], { state: { tokenCentro: this.funcionarioActivo.tokenIdentificadorDepartamento }, relativeTo: this.route });
  }

  verAlerta(alertaDTO: AlertaDTO) {
    this.router.navigate(['crear-editar'], { state: { item: alertaDTO, esVisualizacion: true, tokenCentro: this.funcionarioActivo.tokenIdentificadorDepartamento }, relativeTo: this.route });
  }

  editarAlerta(alertaDTO: AlertaDTO) {
    this.router.navigate(['crear-editar'], { state: { item: alertaDTO, tokenCentro: this.funcionarioActivo.tokenIdentificadorDepartamento }, relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerAlertas();
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

    this.obtenerAlertas();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerAlertas();
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatHora(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }
}
