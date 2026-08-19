import { state } from '@angular/animations';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Route, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
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
import { PlanAsistenciaService } from 'app/modules/seguridad/services/planAsistencia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-plan-asistencia-post-egreso',
  standalone: true,
  imports: [
    TablaListaComponent
  ],
  templateUrl: './plan-asistencia-post-egreso.component.html',
  styleUrl: './plan-asistencia-post-egreso.component.scss'
})
export class PlanAsistenciaPostEgresoComponent implements OnInit {

  listaPlanes: PlanAsistenciaPostEgresoDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_PLAN_ASISTENCIA_POST_EGRESO;

  uuid_fp!: string;

  base64Image: string | null = null;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    fecInicio: "Fecha de inicio",
    fecFin: "Fecha de fin",
    nombreEstado: "Estado",
  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private planAsistenciaService: PlanAsistenciaService,
    private dialogMensajeService: DialogMensajeService,
    private http: HttpClient,
    private funcionesUtils: FuncionesUtils,
    private fichaIdentificacionService: FichaIdentificacionService,
    private pdfService: PdfService,
  ) {

  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerPlanes();
  }

  obtenerPlanes() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.planAsistenciaService.obtenerPlanesAsistencia(this.paginacionRequest, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlanAsistenciaPostEgresoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.planAsistenciaService.checkError(response);
            return;
          }

          this.listaPlanes = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.planAsistenciaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.planAsistenciaService.obtenerPlanesAsistencia(this.paginacionRequest, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlanAsistenciaPostEgresoDTO>>) => {

          if (!response.exito) {
            this.planAsistenciaService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.planAsistenciaService.checkError(error);
        }
      }
    );
  }

  verPlan(plan: PlanAsistenciaPostEgresoDTO) {
    this.router.navigate(['crear-editar-plan-asistencia'], {
      relativeTo: this.route,
      queryParams: { tokenPlan: plan.tokenIdentificador, state: 'show' }
      // state: {
      //   visualizar: true,
      //   plan: plan
      // }      
    });
  }

  agregarPlan() {
    this.router.navigate(['crear-editar-plan-asistencia'], {
      relativeTo: this.route,
    });
  }

  editarPlan(plan: PlanAsistenciaPostEgresoDTO) {
    this.router.navigate(['crear-editar-plan-asistencia'], {
      relativeTo: this.route,
      queryParams: { tokenPlan: plan.tokenIdentificador }
      // state: {
      //   editar: true,
      //   plan: plan
      // }      
    });
  }

  visualizar(plan: PlanAsistenciaPostEgresoDTO) {
    this.router.navigate(['crear-editar-plan-asistencia'], { relativeTo: this.route, queryParams: { tokenPlan: plan.tokenIdentificador, mode: 'ver' } });
  }

  eliminarPlan(plan: PlanAsistenciaPostEgresoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro seleccionado, esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.planAsistenciaService.eliminarPlanAsistencia(plan, '').subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerPlanes();
                },
                error: (error: any) => {
                  load.close();

                  this.planAsistenciaService.checkError(error);
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

    this.obtenerPlanes();
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

    this.obtenerPlanes();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerPlanes();
  }

  generarPDF(plan: PlanAsistenciaPostEgresoDTO) {
    this.loadImageAsBase64();

    // TABLA FACTORES
    let tablaFormateada: any[] = [];
    for (let item of plan.planDetalle) {
      let elemento = {
        Áreas: item.area.nombre,
        Factores_de_riesgo_protectores: item.factores || '',
        Objetivo_general: item.objetivoGeneral || '',
        Objetivo_especifico: item.objetivoEspecifico || '',
        Actividades: item.actividades || '',
        Instituciones: item.institucion || '',
        Frecuencia: item.frecuencia || '',
        Registro_indicador: item.objetivoEspecifico || '',
      }
      tablaFormateada.push(elemento);
    }

    let tabla = new TablaPlantilla();
    tabla.encabezados = ['Áreas', 'Factores de riesgo/protectores', 'Objetivo general', 'Objetivo específico', 'Actividades', 'Instituciones donde participa', 'Frecuencia/tiempo de seguimiento', 'Registro/indicador de seguimiento'];
    tabla.filas = tablaFormateada;

    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(plan.idFichaIdentificacion, '').subscribe(
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
          request.nemonico = etiquetasModel.FORMULARIO_PLAN_ASISTENCIA;
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[TITULO-PLANTILLA]": 'Plan de Asistencia Post Egreso',
            "[TITULO-INFORME]": 'Plan de Asistencia Post Egreso',
            "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
            "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
            "[CENTRO]": fichaIdentificacion.centroIngreso,
            "[ADOLESCENTE]": nombreAdolescente,
            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
            "[FECHA-INICIO]": this.formatFecha(plan.fechaInicio.toString()),
            "[FECHA-FIN]": this.formatFecha(plan.fechaFin.toString()),
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

  handleCustomAction(event: { accion: string, data: PlanAsistenciaPostEgresoDTO }) {
    switch (event.accion) {
      case 'editarPlan':
        this.editarPlan(event.data);
        break;
      case 'generarPDF':
        this.generarPDF(event.data);
        break;
      case 'visualizar':
        this.visualizar(event.data);
        break;
      default:
        console.warn('Acción no reconocida:', event);
    }
  }


}
