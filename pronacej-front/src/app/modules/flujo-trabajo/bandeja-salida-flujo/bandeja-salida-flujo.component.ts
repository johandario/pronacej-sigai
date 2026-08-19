import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TareaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import moment from 'moment';
import { FlujoTrabajoService } from '../flujo-trabajo.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { Router } from '@angular/router';
import { MatTabGroup, MatTabsModule } from '@angular/material/tabs';
import { List } from 'lodash';
import { FormControl, FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { TrasladoService } from '../traslado/traslado.service';
import { GestionFugaService } from '../gestion-fuga/gestion-fuga.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';

@Component({
  selector: 'app-bandeja-salida-flujo',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatTabsModule,
    FormsModule,
    MatSelectModule
  ],
  templateUrl: './bandeja-salida-flujo.component.html',
  styleUrl: './bandeja-salida-flujo.component.scss'
})
export class BandejaSalidaFlujoComponent {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  eventoSeleccionado: any;

  listaTareas: TareaDTO[] = [];
  tiposTareas: string[] = [];

  keyLabelsTable: any = {    
    acciones: "",
    fechaEdicion: "Fecha Envío",
    tipo: "Tipo",
    descripcion: "Descripción",
    estado: "Estado",
    // numDoc: "N° de documento",
    // numeroIdentificacion: "N° de identificación",
  };

  tareasDataSource = new MatTableDataSource();
  nemonicoMenu = etiquetasModel.NEMONICO_FLUJO_BORRADORES_SALIDAS;
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO; 
  funcionarioActivo: FuncionarioDTO; 
  tokenJerarquia: any
  jerarquia: any;
  tokenFilter: any
  centroActual: JerarquiaDTO;

  constructor(
    private flujoTrabajoService: FlujoTrabajoService,
    private funcionarioService: FuncionarioService,    
    private router: Router,
    private trasladoService: TrasladoService,
    private fugaService: GestionFugaService,
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

          this.obtenerTiposTareas();
          this.obtenerTareas();

      });
    });
  }

  refrescar(event: any) {
    if (event) {
      this.buscarPorTipo(event);
    } else {
      this.obtenerTodo();
    }
  }

  obtenerTodo() {
    this.obtenerTiposTareas();
    this.obtenerTareas();
  }

  obtenerTiposTareas() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;

    this.flujoTrabajoService.obtenerTiposTareasEnviadas(paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<string[]>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }
          // console.log(response);
          this.tiposTareas = response.data;
          // this.listaTareas = response.data.data;          
          // this.tareasDataSource = new MatTableDataSource(this.listaTareas);
          // this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.flujoTrabajoService.checkError(error);
        }
      }
    );
  }

  obtenerTareas() {  
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.centroActual.tokenIdentificador;

    this.flujoTrabajoService.obtenerTareasEnviadas(paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<TareaDTO>>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }
          this.listaTareas = response.data.data;        
          console.log('salidas',this.listaTareas)
          console.log(this.listaTareas);
          
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
  //   this.router.navigate([row.url], {queryParams: {estado: row.estado}});
  // }
  irAPantalla(tarea: TareaDTO) {
    console.log(tarea.url);
    console.log(tarea);
    
    const partesURL = tarea.url.split('/');
    const tokenID = partesURL[partesURL.length - 1]; 
    this.router.navigate([tarea.url], {
      queryParams: {
        tokenTarea: tarea.tokenIdentificador,
        token: tarea.paso.tokenIdentificador,
        tokenID: tokenID,
        sourceSite: "bandeja-salida"      
      }
    });
  }

  buscarPorTipo(event: any) {
    const tipo = event.value;
    this.eventoSeleccionado = event;
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.filter = tipo;
    paginacionRequest.tokenIdentificador = this.centroActual.tokenIdentificador;
  
    this.flujoTrabajoService.obtenerTareasEnviadasPorTipo(paginacionRequest, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<TareaDTO>>) => {
        if (!response.exito) {
          this.flujoTrabajoService.checkError(response);
          return;
        }
  
        const tareas = response.data.data;
        this.totalItems = response.data.totalItems;
        console.log(response);
        
        // Para cada tarea, buscar número de identificación si aplica
        tareas.forEach(tarea => {
          const url = tarea.url;
          const partes = url.split('/');
          const tipo = partes[2];
          const tokenId = partes[partes.length - 1];
          console.log(tipo);
          
          if (tipo === 'fuga') {
            this.fugaService.obtenerFugasPorTokenID(tokenId, this.nemonicoMenu).subscribe({
              next: (res) => {
                if (res.exito && res.data ) {
                  tarea['numDoc'] = res.data.numFuga;
                  tarea['numeroIdentificacion'] = res.data.numeroIdentificacion;
                  
                }
              }
            });
          } else if (tipo === 'traslado') {
            this.trasladoService.obtenerTrasladoPorTokenID(tokenId, this.nemonicoMenu).subscribe({
              next: (res) => {
                if (res ) {
                  tarea['numDoc'] = res.data.numTraslado;
                  const numerosIdentificacion = res.data.trasladoAdolescentes?.map(
                    (item: any) => item.fichaIdentificacion?.numeroIdentificacion
                  ).filter((ni: string) => ni); // filtra null/undefined
          
                  // Puedes unirlos en un string separado por coma (o como prefieras)
                  tarea['numeroIdentificacion'] = numerosIdentificacion?.join(', ');
                  // console.log(res);
                  
                  // tarea.numeroIdentificacion = res.data.fichaIdentificacion.numeroIdentificacion;
                }
              }
            });
          }
        });
  
        this.listaTareas = tareas;
        this.tareasDataSource = new MatTableDataSource(this.listaTareas);
      },
      error: (error: any) => {
        this.flujoTrabajoService.checkError(error);
      }
    });
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
