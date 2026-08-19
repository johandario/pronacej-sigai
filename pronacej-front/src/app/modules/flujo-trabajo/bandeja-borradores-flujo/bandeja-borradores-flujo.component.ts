import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { InstanciaProcesoDTO, TareaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import moment from 'moment';
import { FlujoTrabajoService } from '../flujo-trabajo.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';

@Component({
  selector: 'app-bandeja-borradores-flujo',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule
  ],
  templateUrl: './bandeja-borradores-flujo.component.html',
  styleUrl: './bandeja-borradores-flujo.component.scss'
})
export class BandejaBorradoresFlujoComponent {
  page = 0;
    listSize = [5, 10, 15, 20];
    size = this.listSize[0];
    totalItems = 0;
  
    listaTareas: TareaDTO[] = [];
  
    keyLabelsTable: any = {    
      acciones: "",
      fechaCreacion: "Fecha recibido",    
      tipo: "Tipo",
      descripcion: "Descripción",
      estado: "Estado",
    };
  
    tareasDataSource = new MatTableDataSource();
    nemonicoMenu = etiquetasModel.NEMONICO_FLUJO_BANDEJA_BORRADORES;
    nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO; 
    funcionarioActivo: FuncionarioDTO; 
    tokenJerarquia: any
    jerarquia: any;
    tokenFilter: any
    centroActual: JerarquiaDTO;
  
    constructor(
      private flujoTrabajoService: FlujoTrabajoService,
      private router: Router,
      private dialogMensajeService: DialogMensajeService,
      private funcionarioService: FuncionarioService,    
      private jerarquiaService: JerarquiaService,         
    ) {}
  
    ngOnInit(): void {
      this.obtenerTokenDepartamento().then(() => {
        this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
            
          this.jerarquia = data.data.filter(j => j.nombre === this.tokenJerarquia);
          if (this.jerarquia.length > 0) {
            this.centroActual = this.jerarquia[0];
          } else {
            this.centroActual = null;
          }
          
          this.obtenerTareas();

        });
      });
    }
  
    obtenerTareas() {  
      let paginacionRequest = new PaginacionRequest();
      paginacionRequest.size = this.size;
      paginacionRequest.page = this.page;
      paginacionRequest.tokenIdentificador = this.centroActual.tokenIdentificador;
  
      this.flujoTrabajoService.obtenerTareasBorrador(paginacionRequest, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<TareaDTO>>) => {
  
            if (!response.exito) {
              this.flujoTrabajoService.checkError(response);
              return;
            }
            this.listaTareas = response.data.data;          
            this.tareasDataSource = new MatTableDataSource(this.listaTareas);
            this.totalItems = response.data.totalItems;
          },
          error: (error: any) => {
            this.flujoTrabajoService.checkError(error);
          }
        }
      );
    }
  
    getKeys() {
      return Object.keys(this.keyLabelsTable);
    }
  
    getLocalDate(date: Date) {
      return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
   }
  
    handlePageEvent(pageEvent: PageEvent) {
      this.size = pageEvent.pageSize;
      this.page = pageEvent.pageIndex;
      this.obtenerTareas();
    } 
  
    // irAPantalla(row: TareaDTO) {
    //   this.router.navigate([row.url], {
    //     queryParams: {estado: row.estado}
    //   });
    // }
  
    irAPantalla(tarea: TareaDTO) {
      this.router.navigate([tarea.url], {
        queryParams: {
          tokenTarea: tarea.tokenIdentificador,
          sourceSite: "bandeja-borrador"
        }
      });
    }

    eliminarTarea(tarea: TareaDTO) {    
      
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        `A continuación se borrará el flujo referente a la tarea creada`,
        "Deseas continuar?"
      );

      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {              
  
              this.flujoTrabajoService.eliminarInstanciaProcesoPorTarea(tarea, this.nemonicoMenu).subscribe({
                next: (response: RespuestaPorDefecto<InstanciaProcesoDTO>) => {
  
                  if (!response.exito) {
                    this.flujoTrabajoService.checkError(response);
  
                    return;
                  }
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                  this.obtenerTareas();
                 
                },
                error: (error: any) => {
                  this.flujoTrabajoService.checkError(error);
                }
              })
            }
          }
        }
      );
  
    }

    obtenerTokenDepartamento(): Promise<void> {
      return new Promise((resolve) => {
        this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenuinicio).subscribe({
          next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
            if (!response.exito) {
              resolve();
              return;
            }
            this.funcionarioActivo = response.data;
            console.log('funcionarioActivo',this.funcionarioActivo);
  
            this.tokenJerarquia = this.funcionarioActivo.departamento;
            this.tokenFilter = this.funcionarioActivo.tokenIdentificadorDepartamento
            resolve();
          },
          error: (error: any) => {
            console.error('Error al obtener el departamento:', error);
            resolve();
          }
        });
      });
    }
} 
    