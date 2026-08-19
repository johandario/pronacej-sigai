import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { AuditoriaAccionesSistemaDTO } from 'app/core/model/both/AuditoriaAccionesSistemaDTO.model';
import { PaginacionAuditoriasAccionesRequest } from 'app/core/model/request/PaginacionAuditoriasAccionesRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { AuditoriAccionUsuarioSistemaService } from 'app/modules/seguridad/services/auditoriaAccionUsuarioSistema.service';
import { environment } from 'environments/environment';
import { MatTableModule } from '@angular/material/table';
import { VisualizarJsonService } from 'app/core/components/visualizador-json/visualizar-json.service';
import jsonToCsvExport, { HeaderMapping } from "json-to-csv-export";
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatDialogRef } from '@angular/material/dialog';
import { FuseConfirmationDialogComponent } from '@fuse/services/confirmation/dialog/dialog.component';
@Component({
  selector: 'app-auditoria-sistema-visualizar',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatPaginatorModule,
    MatTableModule
  ],
  templateUrl: './auditoria-sistema-visualizar.component.html',
  styleUrl: './auditoria-sistema-visualizar.component.scss'
})
export class AuditoriaSistemaVisualizarComponent implements OnInit {

  // private funcionesUtils = new FuncionesUtils();

  @Input({ required: true }) nemonicoPantalla: string;
  @Input({ required: true }) paginacionAuditoriasAccionesRequest
    = new PaginacionAuditoriasAccionesRequest();

  protected listaDeAccionessistemaDto: AuditoriaAccionesSistemaDTO[] = [];
  protected dataSource: CdkTableDataSourceInput<any>;

  protected page = 0;
  protected sizeList = [5, 10, 15, 20];
  protected size = this.sizeList[0];
  protected totalItems = 0;

  keyLabelsTable: any = {
    numero: "No.",
    fechaInicioAccion: "Fecha inicio acción",
    fechaFinAccion: "Fecha fin acción",
    nombreAccion: "Acción",

    accept: "Accept",
    acceptLanguage: "AcceptLanguage",
    contentLength: "Longitud del contenido",
    contentType: "Tipo de contenido",
    //fechaRequest: "Fecha del request",
    //fechaResponse: "Fecha del response",
    headersJson: "Json header",
    jsonRequest: "Json request",
    jsonResponse: "Json response",
    origin: "Origen",
    platform: "Plataforma",
    referer: "Referer",
    tipoDeMetodo: "Tipo de método",
    url: "Url",
    userAgent: "UserAgent",

    nombreMenu: "Opción",
    modulo: "Modulo",
    descripcion: "Descripción",

    nombreRol: "Rol",
    nombreUsuarioQueRealizaLaAccion: "Nombre usuario",
    userNameUsuarioQueRealizaLaAccion: "User name",
    emailUsuarioQueRealizaLaAccion: "Email usuario"
  };

  constructor(
    private auditoriAccionUsuarioSistemaService: AuditoriAccionUsuarioSistemaService,
    private cdRef: ChangeDetectorRef,
    private visualizarJsonService: VisualizarJsonService,
    private dialogMensajeService: DialogMensajeService,
    private funcionesUtils: FuncionesUtils,
  ) { }

  ngOnInit(): void {
    this.refresh();
  }

  verJson(titulo: string, json: string) {
    this.visualizarJsonService.abrirVistaDeJson(
      "Visualiza el json de: " + titulo, json
    );
  }

  esKeyObject(key: string) {
    return key == "accept" || key == "acceptLanguage"
      || key == "contentType" || key == "fechaRequest" || key == "fechaResponse" || key == "headersJson"
      || key == "origin" || key == "platform" || key == "referer"
      || key == "tipoDeMetodo" || key == "url" || key == "userAgent"
      || key == "jsonRequest" || key == "jsonResponse" || key == "contentLength";
  }

  esJson(key: string) {
    return key == "jsonRequest" || key == "jsonResponse" || key == "headersJson";
  }

  getLocalDate(date: Date) {
    return this.funcionesUtils.getLocalDate(date);;
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  protected refresh() {
    this.consultarAcciones(false);
  }

  consultarAcciones(generarReporte: boolean) {
    this.paginacionAuditoriasAccionesRequest.emitirReporte = generarReporte;

    if (!generarReporte) {
      this.paginacionAuditoriasAccionesRequest.page = this.page;
      this.paginacionAuditoriasAccionesRequest.size = this.size;

      this.listaDeAccionessistemaDto = [];
      this.dataSource = this.listaDeAccionessistemaDto;
      this.totalItems = 0;
    }

    let load: MatDialogRef<FuseConfirmationDialogComponent, any>;

    if (generarReporte) {
      load = this.dialogMensajeService.mensajeLoading("Estamos contruyendo tu reporte..");
    }
    this.auditoriAccionUsuarioSistemaService.obtenerPorFiltros(
      this.paginacionAuditoriasAccionesRequest,
      this.nemonicoPantalla
    ).subscribe(
      {
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<AuditoriaAccionesSistemaDTO>>) => {
          load?.close();
          if (!environment.production) {
            console.log(respuesta);
          }

          if (!respuesta.exito) {
            this.auditoriAccionUsuarioSistemaService.checkError(respuesta);
            return;
          }
          this.totalItems = respuesta.data.totalItems;

          let datosAMostrar = this.transformarADatosAMostrar(respuesta.data.data);
          if (!generarReporte) {
            this.listaDeAccionessistemaDto = datosAMostrar;
            this.dataSource = datosAMostrar;

            console.log(this.dataSource);
            this.cdRef.detectChanges();
          } else {
            //Generando el reporte
            let fechaInicio = new Date(this.paginacionAuditoriasAccionesRequest.fechaInicio);
            let fechaFin = new Date(this.paginacionAuditoriasAccionesRequest.fechaFin);

            let nombreArchivo = "auditorias_" + this.funcionesUtils.getOnlyDate(fechaInicio) +
              "_" + this.funcionesUtils.getOnlyDate(fechaFin) + ".csv";

            let llavesKeyLabels = Object.keys(this.keyLabelsTable);
            let headerMapping: HeaderMapping[] = llavesKeyLabels.map(
              (llave: string) => {
                let header: HeaderMapping = {
                  key: llave,
                  label: this.keyLabelsTable[llave]
                }

                return header;
              }
            );

            jsonToCsvExport({ data: datosAMostrar, filename: nombreArchivo, headers: headerMapping });

          }



        },
        error: (error: any) => {
          load?.close();
          this.auditoriAccionUsuarioSistemaService.checkError(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.page = pageEvent.pageIndex;
    this.size = pageEvent.pageSize;

    this.refresh();
  }

  private transformarADatosAMostrar(list: AuditoriaAccionesSistemaDTO[]): any[] {
    let listaFinal: any[] = [];
    list.forEach(
      (auditoriaAccionesSistemaDTO: AuditoriaAccionesSistemaDTO, index: number) => {
        let datosRequest = auditoriaAccionesSistemaDTO.auditoriaServicioRestDTO;
        let dato: any = {
          numero: this.totalItems - (index + (this.page * this.size)),
          fechaInicioAccion: this.getLocalDate(auditoriaAccionesSistemaDTO.fechaInicioAccion),
          fechaFinAccion: this.getLocalDate(auditoriaAccionesSistemaDTO.fechaFinAccion),
          nombreAccion: auditoriaAccionesSistemaDTO.nombreAccion,

          accept: datosRequest?.accept,
          acceptLanguage: datosRequest?.acceptLanguage,
          contentLength: datosRequest?.contentLength,
          contentType: datosRequest?.contentType,

          headersJson: datosRequest?.headersJson,
          jsonRequest: datosRequest?.jsonRequest,
          jsonResponse: datosRequest?.jsonResponse,
          origin: datosRequest?.origin,
          platform: datosRequest?.platform,
          referer: datosRequest?.referer,
          tipoDeMetodo: datosRequest?.tipoDeMetodo,
          url: datosRequest?.url,
          userAgent: datosRequest?.userAgent,

          nombreMenu: auditoriaAccionesSistemaDTO.nombreMenu,
          modulo: auditoriaAccionesSistemaDTO.modulo,
          descripcion: auditoriaAccionesSistemaDTO.descripcion,

          nombreRol: auditoriaAccionesSistemaDTO.nombreRol,
          nombreUsuarioQueRealizaLaAccion: auditoriaAccionesSistemaDTO.nombreUsuarioQueRealizaLaAccion,
          userNameUsuarioQueRealizaLaAccion: auditoriaAccionesSistemaDTO.userNameUsuarioQueRealizaLaAccion,
          emailUsuarioQueRealizaLaAccion: auditoriaAccionesSistemaDTO.emailUsuarioQueRealizaLaAccion
        }

        listaFinal.push(dato);
      }
    );

    return listaFinal;

  }

}
