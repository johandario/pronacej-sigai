import { HttpClient } from '@angular/common/http';
import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Route, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { InformeFinalAsistenciaDTO } from 'app/core/model/both/informeFinalAsistenciaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PlanAsistenciaPostEgresoDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { InformeFinalAsistenciaService } from 'app/modules/seguridad/services/informeFinalAsistencia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-informe-final-asistencia',
  standalone: true,
  imports: [
    TablaListaComponent,
  ],
  templateUrl: './informe-final-asistencia.component.html',
  styleUrl: './informe-final-asistencia.component.scss'
})
export class InformeFinalAsistenciaComponent implements OnInit, OnChanges {
  @Input() planAsistencia!: PlanAsistenciaPostEgresoDTO;

  listaPlanes: InformeFinalAsistenciaDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  uuid_fp!: string;

  base64Image: string | null = null;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fecCreacion: "Fecha de creación",
    fecInicio: "Fecha de inicio",
    fecFin: "Fecha de fin",
    // nombreEstado: "Estado",
  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private informeFinalAsistenciaService: InformeFinalAsistenciaService,
    private dialogMensajeService: DialogMensajeService,
    private http: HttpClient,
    private funcionesUtils: FuncionesUtils,
    private fichaIdentificacionService: FichaIdentificacionService,
    private pdfService: PdfService,
  ) {

  }

  ngOnInit(): void {
    // this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    // this.obtenerInformes();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['planAsistencia']) {
      // this.uuid_fp = this.planAsistencia.tokenIdentificador;
      this.route.queryParams.subscribe(params => {
        this.uuid_fp = params['tokenPlan'];
        this.obtenerInformes();
      })
    }
  }

  obtenerInformes() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.informeFinalAsistenciaService.obtenerInformes(this.paginacionRequest, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<InformeFinalAsistenciaDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.informeFinalAsistenciaService.checkError(response);
            return;
          }

          this.listaPlanes = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.informeFinalAsistenciaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.informeFinalAsistenciaService.obtenerInformes(this.paginacionRequest, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<InformeFinalAsistenciaDTO>>) => {

          if (!response.exito) {
            this.informeFinalAsistenciaService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.informeFinalAsistenciaService.checkError(error);
        }
      }
    );
  }

  verInforme(informeFinal: InformeFinalAsistenciaDTO) {
    this.router.navigate(['crear-editar-informe-final'], {
      relativeTo: this.route,
      state: {
        visualizar: true,
        informeFinal: informeFinal,
        plan: this.planAsistencia
      }
    });
  }

  agregarInforme() {
    this.router.navigate(['crear-editar-informe-final'], {
      relativeTo: this.route,
      state: {
        plan: this.planAsistencia
      }
    });
  }

  editarInforme(informeFinal: InformeFinalAsistenciaDTO) {
    this.router.navigate(['crear-editar-informe-final'], {
      relativeTo: this.route,
      state: {
        editar: true,
        informeFinal: informeFinal,
        plan: this.planAsistencia
      }
    });
  }




  eliminarInforme(informeFinal: InformeFinalAsistenciaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro seleccionado, esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.informeFinalAsistenciaService.eliminarInforme(informeFinal, '').subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerInformes();
                },
                error: (error: any) => {
                  load.close();

                  this.informeFinalAsistenciaService.checkError(error);
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

    this.obtenerInformes();
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

    this.obtenerInformes();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerInformes();
  }

  generarPDF(informeFinal: InformeFinalAsistenciaDTO) {
    this.loadImageAsBase64();

    let tablaFormateada: any[] = [];
    for (let item of informeFinal.detalle) {
      let elemento = {
        Áreas: item.area.nombre,
        Objetivo_general: item.objetivoGeneral || '',
        Objetivo_especifico: item.objetivoEspecifico || '',
        Actividades: item.descripcionActividad || '',
        Logros: item.logro || '',
        Dificultades: item.dificultad || '',
      }
      tablaFormateada.push(elemento);
    }

    let tabla = new TablaPlantilla();
    tabla.encabezados = ['Áreas', 'Objetivo general', 'Objetivo específico', 'Actividades', 'Logros', 'Dificultades'];
    tabla.filas = tablaFormateada;

    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(informeFinal.idFichaIdentificacion, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_INFORME_FINAL_ASISTENCIA;
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[TITULO-PLANTILLA]": 'Informe Final de Asistencia Post Egreso',
            "[TITULO-INFORME]": 'Informe Final de Asistencia Post Egreso',
            "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
            "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
            "[CENTRO]": fichaIdentificacion.centroIngreso,
            "[ADOLESCENTE]": nombreAdolescente,
            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
            "[FECHA-INICIO]": this.formatFecha(informeFinal.fechaInicio.toString()),
            "[FECHA-FIN]": this.formatFecha(informeFinal.fechaFin.toString()),
            "[TABLA-AREAS]": JSON.stringify(tabla),
          }
          this.pdfService.generarPdf(request, '').subscribe({
            next: (response: RespuestaPorDefecto<string>) => {

              if (!response.exito) {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                );
                return;
              }

              console.log(response);

              const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

              const pwa = window.open(url);

            },
            error: (error: any) => {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
              );
            }
          });

        },
        error: (error: any) => {
          this.fichaIdentificacionService.checkError(error);
        }
      }
    );
  }

  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
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

