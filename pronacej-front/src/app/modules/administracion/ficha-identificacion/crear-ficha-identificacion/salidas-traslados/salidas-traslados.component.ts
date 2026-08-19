import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Router, ActivatedRoute } from '@angular/router';
import { PermisoDirective } from 'app/core/directives/permiso.directive';
import etiquetasModel from 'app/core/etiquetas.model';
import { RegistroSalidaDTO } from 'app/core/model/both/salida/RegistroSalidaDTO.model';
import { TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { TrasladoService } from 'app/modules/flujo-trabajo/traslado/traslado.service';
import { SalidaService } from 'app/modules/salida/salida.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-salidas-traslados',
  standalone: true,
  imports: [MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatInputModule,
    CommonModule,
    FormsModule,
    MatSortModule,
    PermisoDirective
  ],
  templateUrl: './salidas-traslados.component.html',
  styleUrl: './salidas-traslados.component.scss'
})
export class SalidasTrasladosComponent implements OnInit {

  uuid_fp: string;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  filter: string = '';

  tituloPantalla: string = "";
  salidas: RegistroSalidaDTO[] = [];
  dataSourceSalidas: CdkTableDataSourceInput<RegistroSalidaDTO>;

  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_FICHA_FLUJOS;
  nemonicoVisualizar = etiquetasModel.ACCIONES_MENU_PERMISO_VER;

  keyLabelsTable: any = {
    acciones: "Acciones",
    nroDocumento: "Número Documento",
    nombreMotivoSalida: "Motivo salida",
    // centroOrigen: "Centro origen",
    // centroDestino: "Centro destino",
    observaciones: "Observaciones",
    fechaHoraSalida: "Fecha salida", 
    asunto: "Asunto",

  };

  listaSalidasTraslados: TrasladoDTO[] = [];
  dataSource: CdkTableDataSourceInput<TrasladoDTO>;

  salidasTraslados: PaginacionRequest = new PaginacionRequest();

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private trasladoService: TrasladoService,
    private salidaService: SalidaService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private funcionesUtils: FuncionesUtils
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_SALIDAS_FINALIZADAS"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    // this.obtenerTrasladosCompletos();
    this.obtenerTrasladosFugasCompletos();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  obtenerTrasladosCompletos() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.uuid_fp;
    paginacionRequest.direction = this.salidasTraslados.direction;
    // if (this.filter.length > 0) {
    //   paginacionRequest.filter = this.filter;
    //   paginacionRequest.page = 0;
    //   this.page = 0;
    // }

    this.trasladoService.obtenerTrasladosPorTokenFicha(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<TrasladoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaSalidasTraslados = response.data.data;
          this.dataSource = this.listaSalidasTraslados;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          console.log(error);
        }
      }
    );
  }

  obtenerTrasladosFugasCompletos() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    if (this.filter && this.filter.trim() != "") {
      paginacionRequest.filter = this.filter;
    }
    paginacionRequest.tokenIdentificador = this.uuid_fp;
    paginacionRequest.direction = this.salidasTraslados.direction;
    // if (this.filter.length > 0) {
    //   paginacionRequest.filter = this.filter;
    //   paginacionRequest.page = 0;
    //   this.page = 0;
    // }

    this.salidaService.obtenerlistadoPorTokenCompletos(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroSalidaDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.salidas = response.data.data;
          this.dataSourceSalidas = this.salidas;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          console.log(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerTrasladosFugasCompletos();
  }

  refrescar() {
    this.filter = '';
    this.page = 0;
    this.obtenerTrasladosFugasCompletos();
  }

  onXLSX() {
    const datosVisibles = this.salidas;
    const transformedData = datosVisibles.map((item, i) => {
    const transformedItem: any = {};
      Object.keys(this.keyLabelsTable).forEach(key => {
        if (key !== 'acciones') {
          const label = this.keyLabelsTable[key] || key;
          if (key === 'numero') {
            transformedItem[label] = this.totalItems - (i + (this.page * this.size));
          } 
          else if (key === 'fechaHoraSalida') {
            transformedItem[label] = item.fechaHoraSalida
              ? new Date(item.fechaHoraSalida).toLocaleString('es-EC')
              : '';
          } 
          else if (key === 'asunto') {
            transformedItem[label] = item.nombreMotivoSalida === 'Fuga'
              ? item.eventoFuga?.asunto || ''
              : '';
          } 
          else {
            transformedItem[label] = item[key] ?? '';
          }
        }
      });
      return transformedItem;
    });
    const headers = Object.keys(this.keyLabelsTable)
      .filter(key => key !== 'acciones')
      .map(key => this.keyLabelsTable[key]);
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(transformedData, { header: headers });
    ws['!cols'] = headers.map(() => ({ wch: 20 }));
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Datos');
    XLSX.writeFile(wb, 'datos.xlsx');
  }

  vista(traslado: RegistroSalidaDTO) {
    let token = "";
    if(traslado.eventoFuga){
      token = traslado.eventoFuga.tokenIdentificador;
    } else if(traslado.traslado){
      token = traslado.traslado.tokenIdentificador;
    }
    this.router.navigate(['vista/' + token], {
      relativeTo: this.route,
      state: { traslado } // 👈 Aquí viaja el objeto
    });
  }
  
}
