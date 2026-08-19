import { CommonModule } from '@angular/common';
import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { TrabajoLaboralDTO } from 'app/core/model/both/TrabajoLaboralDTO.model';
import { TrabajoLaboralService } from 'app/modules/seguridad/services/trabajoLaboral.service';
import { TrabajoLaboralCrearEditarComponent } from './trabajo-laboral-crear-editar/trabajo-laboral-crear-editar.component';

@Component({
  selector: 'app-trabajo-laboral',
  standalone: true,
  imports: [
    CommonModule,
    TablaListaComponent,
    TrabajoLaboralCrearEditarComponent
  ],
  templateUrl: './trabajo-laboral.component.html',
  styleUrl: './trabajo-laboral.component.scss'
})
export class TrabajoLaboralComponent implements OnInit {

  @Input() uuid_fp!: string;
  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

  listaTrabajosLaborales: TrabajoLaboralDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  mostrarFormulario = false;
  modoFormulario: 'crear' | 'editar' | 'ver' = 'crear';
  trabajoLaboralSeleccionado: TrabajoLaboralDTO = null;

  keyLabelsTable: any = {
    numero: 'No.',
    acciones: 'Acciones',
    fechaCreacion: 'Fecha y Hora de Registro',
    inicioTrabajo: 'F. Ingreso laboral',
    cargoLaboral: 'Cargo laboral',
    nombreInstitucion: 'Institución',
    rucInstitucion: 'RUC'
  };

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private trabajoLaboralService: TrabajoLaboralService,
    private authSerguridadServicio: AuthSerguridadServicio
  ) {}

  async ngOnInit(): Promise<void> {
    if (!this.uuid_fp) {
      this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    }

    this.obtenerTrabajosLaborales();
  }

  obtenerTrabajosLaborales(): void {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.trabajoLaboralService.obtenerListaTrabajoLaboral(
      this.paginacionRequest,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<TrabajoLaboralDTO>>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.listaTrabajosLaborales = response.data.data.map((item: any) => ({
          ...item,
           inicioTrabajo: this.formatearSoloFecha(item.fechaIngresoLaboral),
          nombreInstitucion: item.registroInstitucion?.nombreOrganizacion ?? '',
          rucInstitucion: item.registroInstitucion?.ruc ?? ''
        }));

        this.paginacion.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        this.trabajoLaboralService.checkError(error);
      }
    });
  }

  agregarTrabajoLaboral(): void {
    this.trabajoLaboralSeleccionado = null;
    this.modoFormulario = 'crear';
    this.mostrarFormulario = true;
  }

  verTrabajoLaboral(trabajoLaboralDTO: TrabajoLaboralDTO): void {
    this.trabajoLaboralSeleccionado = trabajoLaboralDTO;
    this.modoFormulario = 'ver';
    this.mostrarFormulario = true;
  }

  editarTrabajoLaboral(trabajoLaboralDTO: TrabajoLaboralDTO): void {
    this.trabajoLaboralSeleccionado = trabajoLaboralDTO;
    this.modoFormulario = 'editar';
    this.mostrarFormulario = true;
  }

  cerrarFormularioTrabajoLaboral(): void {
    this.mostrarFormulario = false;
    this.trabajoLaboralSeleccionado = null;
    this.modoFormulario = 'crear';
    this.obtenerTrabajosLaborales();
  }

  eliminarTrabajoLaboral(trabajoLaboralDTO: TrabajoLaboralDTO): void {
    const ref = this.dialogMensajeService.mensajeConConfirmacion(
      '¿Estás seguro de eliminar el trabajo laboral? Esta operación es irreversible.',
      '¿Deseas continuar?'
    );

    ref.afterClosed().subscribe({
      next: (resp: 'confirmed' | 'cancelled') => {
        if (resp === 'confirmed') {
          const load = this.dialogMensajeService.mensajeLoading('Eliminando el trabajo laboral...');

          this.trabajoLaboralService.eliminarTrabajoLaboral(
            trabajoLaboralDTO,
            this.nemonicoMenu
          ).subscribe({
            next: (response: RespuestaPorDefecto<boolean>) => {
              load.close();

              if (!response.exito) {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al eliminar el registro. Inténtalo de nuevo.'
                );
                return;
              }

              this.dialogMensajeService.mensajeExitoso(
                response.titulo,
                response.mensaje
              );

              this.obtenerTrabajosLaborales();
            },
            error: (error: any) => {
              load.close();
              this.trabajoLaboralService.checkError(error);
            }
          });
        }
      }
    });
  }

  descargarExcelCompleto(): void {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.trabajoLaboralService.obtenerListaTrabajoLaboral(
      this.paginacionRequest,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<TrabajoLaboralDTO>>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        const dataTransformada = response.data.data.map((item: any, index: number) => ({
          numero: index + 1,
          fechaCreacion: item.fechaCreacion,
          inicioTrabajo: this.formatearSoloFecha(item.fechaIngresoLaboral),
          cargoLaboral: item.cargoLaboral,
          nombreInstitucion: item.registroInstitucion?.nombreOrganizacion ?? '',
          rucInstitucion: item.registroInstitucion?.ruc ?? ''          
        }));

        this.tablaComponent.exportXLSX(dataTransformada);
      },
      error: (error: any) => {
        this.trabajoLaboralService.checkError(error);
      }
    });
  }

  handlePageEvent(pageEvent: PageEvent): void {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;
    this.obtenerTrabajosLaborales();
  }

  handleSortEvent(event: Sort): void {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    } else {
      this.paginacionRequest.sort = null;
      this.paginacionRequest.direction = null;
    }

    this.obtenerTrabajosLaborales();
  }

  handleSearchEvent(filter: string): void {
    this.paginacionRequest.filter = filter;
    this.obtenerTrabajosLaborales();
  }

  formatearSoloFecha(fecha: any): string {
    if (!fecha) {
      return '';
    }
    const fechaTexto = String(fecha);
    if (fechaTexto.includes('T')) {
      const partes = fechaTexto.substring(0, 10).split('-');
      return `${partes[2]}-${partes[1]}-${partes[0]}`;
    }
    if (fechaTexto.includes(' ')) {
      return fechaTexto.split(' ')[0];
    }
    return fechaTexto;
  }
}