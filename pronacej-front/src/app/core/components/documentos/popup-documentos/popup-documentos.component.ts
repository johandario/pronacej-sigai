import { AfterViewInit, Component, EventEmitter, Inject, OnInit, Output, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { DocumentosSubidosTablaComponent } from '../documentos-subidos-tabla/documentos-subidos-tabla.component';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { CommonModule } from '@angular/common';
import { TipoDeDocumento } from '../modelos/TipoDeDocumento.model';
import { PageEvent } from '@angular/material/paginator';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import { EvaluacionDocumentoRequest } from 'app/core/model/request/general/EvaluacionDocumentoRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { ActivatedRoute } from '@angular/router';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatButtonModule } from '@angular/material/button';
import { EvaluacionDomiciliariaDocumentosRequest } from 'app/core/model/request/ia/EvaluacionDomiciliariaDocumentosRequest.model';
import { EvaluacionDomiciliariaService } from 'app/modules/seguridad/services/EvaluacionDomiciliaria.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { SeguimientoEducativoLaboralOtrosService } from 'app/modules/seguridad/services/seguimientoEducativoLaboralOtros.service';
import { EvaluacionSeguimientoEducativoLaboralService } from 'app/modules/seguridad/services/evaluacionSeguimiento.service';
import { SeguimientoSocialService } from 'app/modules/administracion/services/seguimientoSocial.service';
import { InformeFinalAbiertoService } from 'app/modules/seguridad/services/informeFinalAbierto.service';
import { SancionDisciplinariaService } from 'app/modules/administracion/services/sancionDisciplinaria.service';

@Component({
  selector: 'app-popup-documentos',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './popup-documentos.component.html',
  styleUrl: './popup-documentos.component.scss'
})
export class PopupDocumentosComponent implements OnInit, AfterViewInit {

  documentosPaginados: PaginacionResponse<DocumentoDTO> = new PaginacionResponse();
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  nemonicoMenu: string = "";
  tokeniItem: string = "";
  tipoServicio: string = "";

  uuid_fp: string;

  @ViewChild('documentosComp')
  tablaDocumentos: DocumentosSubidosTablaComponent;

  constructor(
    public dialogRef: MatDialogRef<PopupDocumentosComponent>,
    private dialogMensajeService: DialogMensajeService,
    private servicioEvaluacionDomiciliaria: EvaluacionDomiciliariaService,
    private seguimientoEducativoService: SeguimientoEducativoLaboralOtrosService,
    private seguimientoSocialService: SeguimientoSocialService,
    private informeFinalAbiertoService: InformeFinalAbiertoService,
    private sancionService: SancionDisciplinariaService,
    private encuestaService: EncuestaService,
    private route: ActivatedRoute,
    @Inject(MAT_DIALOG_DATA) public data: { tokenItem: string, tipoServicio: string, nemonicoMenu: string }
  ) {
    this.tokeniItem = data.tokenItem;
    this.nemonicoMenu = data.nemonicoMenu;
    this.tipoServicio = data.tipoServicio;
  }

  ngOnInit() {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      switch (this.tipoServicio) {
        case "EVALUACIONES":
          this.obtenerDocumentosEvaluacion();
          break;
        case "EVALUACION_DOMICILIARIA":
          this.obtenerDocumentosDomiciliaria();
          break;
        case "SEGUIMIENTO_EDUCATIVO_LABORAL":
          this.obtenerDocumentosSeguimiento();
          break;
        case "SEGUIMIENTO_SOCIAL":
          this.obtenerDocumentosSeguimientoSocial();
          break;
        case "SANCION_DISCIPLINARIA":
          this.obtenerDocumentosSancionDisciplinaria();
          break;
        case "INFORME_FINAL_SOA":
          this.obtenerDocumentosInformeFinal();
          break;
      }
    });
  }

  obtenerDocumentosEvaluacion() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let evaluacionDocumentoRequest = new EvaluacionDocumentoRequest();
    evaluacionDocumentoRequest.page = page;
    evaluacionDocumentoRequest.size = pageSize;
    evaluacionDocumentoRequest.tokenEvaluacion = this.tokeniItem;
    evaluacionDocumentoRequest.tokenIdentificador = this.uuid_fp;
    evaluacionDocumentoRequest.nemonicoCarpeta = etiquetasModel.CARPETA_NIVEL_RIESGO;

    this.encuestaService.obtenerDocumentos(
      evaluacionDocumentoRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          console.log(response);
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  obtenerDocumentosDomiciliaria() {
    const pagina = this.tablaDocumentos.page || 0;
    const tamañoPagina = this.tablaDocumentos.pageSize || 10;

    // Iteramos por cada evaluación para obtener sus documentos
    let solicitudDocumentos = new EvaluacionDomiciliariaDocumentosRequest();
    solicitudDocumentos.page = pagina;
    solicitudDocumentos.size = tamañoPagina; // Obtener todos los documentos
    solicitudDocumentos.tokenIdentificadorEvaluacionDomiciliaria = this.tokeniItem;

    this.servicioEvaluacionDomiciliaria.obtenerDocumentos(
      solicitudDocumentos,
      etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA
    ).subscribe({
      next: (respuesta) => {

        if (!respuesta.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
          return;
        }

        if (respuesta.data?.data) {
          this.tablaDocumentos.actualizarTabla(
            respuesta.data.data,
            respuesta.data.totalItems
          );
        }

      },
      error: (error) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
        );
      }
    });
  }

  obtenerDocumentosSeguimiento() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = page;
    paginacionRequest.size = pageSize;
    paginacionRequest.tokenIdentificador = this.tokeniItem;

    this.seguimientoEducativoService.obtenerDocumentos(
      paginacionRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. ' + response.mensaje
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  obtenerDocumentosSeguimientoSocial() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = page;
    paginacionRequest.size = pageSize;
    paginacionRequest.tokenIdentificador = this.tokeniItem;

    this.seguimientoSocialService.obtenerDocumentos(
      paginacionRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. ' + response.mensaje
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  obtenerDocumentosSancionDisciplinaria() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = page;
    paginacionRequest.size = pageSize;
    paginacionRequest.tokenIdentificador = this.tokeniItem;

    this.sancionService.obtenerDocumentos(
      paginacionRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. ' + response.mensaje
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  obtenerDocumentosInformeFinal() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = page;
    paginacionRequest.size = pageSize;
    paginacionRequest.tokenIdentificador = this.tokeniItem;

    this.informeFinalAbiertoService.obtenerDocumentos(
      paginacionRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. ' + response.mensaje
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    switch (this.tipoServicio) {
      case "EVALUACIONES":
        this.obtenerDocumentosEvaluacion();
        break;
      case "EVALUACION_DOMICILIARIA":
        this.obtenerDocumentosDomiciliaria();
        break;
      case "INFORME_FINAL_SOA":
        this.obtenerDocumentosInformeFinal();
        break;
      case "SANCION_DISCIPLINARIA":
        this.obtenerDocumentosSancionDisciplinaria();
        break;
    }
  }

  cerrar() {
    this.dialogRef.close();
  }
}
