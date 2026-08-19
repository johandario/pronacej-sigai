import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EstudiosDTO } from 'app/core/model/both/EstudiosDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EstudiosService } from 'app/modules/seguridad/services/EstudiosService.service';
import { EstudiosCrearEditarComponent } from './estudios-crear-editar/estudios-crear-editar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-estudios',
  standalone: true,
  imports: [
    TablaListaComponent,
    EstudiosCrearEditarComponent,
    CommonModule
  ],
  templateUrl: './estudios.component.html',
  styleUrl: './estudios.component.scss'
})
export class EstudiosComponent implements OnInit {
  @Input() uuid_fp!: string;
  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;
  mostrarFormulario = false;
  modoFormulario: 'crear' | 'editar' | 'ver' = 'crear';
  estudioSeleccionado: EstudiosDTO = null;


  listaEstudios: EstudiosDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  keyLabelsTable: any = {
    numero: 'No.',
    acciones: 'Acciones',
    fechaCreacion: 'Fecha y Hora de Registro',
    inicioEstudios: 'F. Inicio Estudios',
    cicloAcademicoActual: 'Ciclo académico actual',
    nombreInstitucion: 'Institución educativa',
    rucInstitucion: 'RUC',
    convenioPronacejTxt: 'Convenio PRONACEJ',
    independienteTxt: 'Independiente'
  };

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private estudiosService: EstudiosService
  ) {}

  ngOnInit(): void {
   console.log('UUID recibido:', this.uuid_fp);
    this.obtenerEstudios();
  }

  obtenerEstudios(): void {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.estudiosService.obtenerListaEstudios(
      this.paginacionRequest,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<EstudiosDTO>>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.listaEstudios = response.data.data.map((item: any) => ({
          ...item,
          inicioEstudios: this.formatearSoloFecha(item.fechaInicioEstudios),
          nombreInstitucion: item.registroInstitucion?.nombreOrganizacion ?? '',
          rucInstitucion: item.registroInstitucion?.ruc ?? '',
          convenioPronacejTxt: item.convenioPronacej ? 'Sí' : 'No',
          independienteTxt: item.independiente ? 'Sí' : 'No'
        }));
        console.log(this.listaEstudios);
        

        this.paginacion.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        this.estudiosService.checkError(error);
      }
    });
  }

  agregarEstudios(): void {

  this.estudioSeleccionado = null;
  this.modoFormulario = 'crear';
  this.mostrarFormulario = true;

}

verEstudios(estudiosDTO: EstudiosDTO): void {

  this.estudioSeleccionado = estudiosDTO;
  this.modoFormulario = 'ver';
  this.mostrarFormulario = true;

}

editarEstudios(estudiosDTO: EstudiosDTO): void {

  this.estudioSeleccionado = estudiosDTO;
  this.modoFormulario = 'editar';
  this.mostrarFormulario = true;

}

  eliminarEstudios(estudiosDTO: EstudiosDTO): void {
    const ref = this.dialogMensajeService.mensajeConConfirmacion(
      '¿Estás seguro de eliminar el registro de estudios? Esta operación es irreversible.',
      '¿Deseas continuar?'
    );

    ref.afterClosed().subscribe({
      next: (resp: 'confirmed' | 'cancelled') => {
        if (resp === 'confirmed') {
          const load = this.dialogMensajeService.mensajeLoading('Eliminando el registro de estudios...');

          this.estudiosService.eliminarEstudios(
            estudiosDTO,
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

              this.obtenerEstudios();
            },
            error: (error: any) => {
              load.close();
              this.estudiosService.checkError(error);
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

    this.estudiosService.obtenerListaEstudios(
      this.paginacionRequest,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<EstudiosDTO>>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        const dataTransformada = response.data.data.map((item: any, index: number) => ({
          numero: index + 1,
          fechaCreacion: item.fechaCreacion,
          inicioEstudios: this.formatearSoloFecha(item.fechaInicioEstudios),
          cicloAcademicoActual: item.cicloAcademicoActual,
          nombreInstitucion: item.registroInstitucion?.nombreOrganizacion ?? '',
          rucInstitucion: item.registroInstitucion?.ruc ?? '',
          convenioPronacejTxt: item.convenioPronacej ? 'Sí' : 'No',
          independienteTxt: item.independiente ? 'Sí' : 'No'
        }));

        this.tablaComponent.exportXLSX(dataTransformada);
      },
      error: (error: any) => {
        this.estudiosService.checkError(error);
      }
    });
  }

  handlePageEvent(pageEvent: PageEvent): void {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;
    this.obtenerEstudios();
  }

  handleSortEvent(event: Sort): void {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    } else {
      this.paginacionRequest.sort = null;
      this.paginacionRequest.direction = null;
    }

    this.obtenerEstudios();
  }

  handleSearchEvent(filter: string): void {
    this.paginacionRequest.filter = filter;
    this.obtenerEstudios();
  }

  cerrarFormularioEstudios(): void {
    this.mostrarFormulario = false;
    this.estudioSeleccionado = null;
    this.modoFormulario = 'crear';
    this.obtenerEstudios();
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