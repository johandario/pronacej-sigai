import { HttpClient } from '@angular/common/http';
import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { Router, ActivatedRoute } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaAsistenciaPostEgresoDTO } from 'app/core/model/both/FichaAsistenciaPostEgreso.model';
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
  selector: 'app-asistencia-seguimiento-post-egreso',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './asistencia-seguimiento-post-egreso.component.html',
  styleUrl: './asistencia-seguimiento-post-egreso.component.scss'
})
export class AsistenciaSeguimientoPostEgresoComponent implements OnInit, OnChanges {
  @Input() planAsistencia!: PlanAsistenciaPostEgresoDTO;

  listaAsistencias: FichaAsistenciaPostEgresoDTO[] = [];
  keyLabelsActividad: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de inicio",
    nombreFormato: "Formato",
  };

  base64Image: string | null = null;

  uuid_fp!: string;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  constructor(private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private planAsistenciaService: PlanAsistenciaService,
    private http: HttpClient,
    private fichaIdentificacionService: FichaIdentificacionService,
    private funcionesUtils: FuncionesUtils,
    private pdfService: PdfService,
  ) {

  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['planAsistencia']) {
      // this.uuid_fp = this.planAsistencia.tokenIdentificador;
      this.route.queryParams.subscribe(params => {
        this.uuid_fp = params['tokenPlan'];
        this.obtenerAsistencias();
      })
    }
  }

  obtenerAsistencias() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    this.paginacionRequest.filter = this.paginacionRequest.filter;

    this.planAsistenciaService.obtenerFichasAsistenciaPostEgreso(this.paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaAsistenciaPostEgresoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.planAsistenciaService.checkError(response);
            return;
          }

          this.listaAsistencias = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
          this.listaAsistencias.forEach(actividad => {
            actividad.nombreFormato = actividad.tipoFormato ? actividad.tipoFormato.nombre : null;
          });
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

    this.planAsistenciaService.obtenerFichasAsistenciaPostEgreso(this.paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaAsistenciaPostEgresoDTO>>) => {

          if (!response.exito) {
            this.planAsistenciaService.checkError(response);
            return;
          }

          let listaAsistencias = response.data.data;
          listaAsistencias.forEach(actividad => {
            actividad.nombreFormato = actividad.tipoFormato ? actividad.tipoFormato.nombre : null;
          });

          this.tablaComponent.exportXLSX(listaAsistencias);
        },
        error: (error: any) => {
          this.planAsistenciaService.checkError(error);
        }
      }
    );
  }

  eliminarAsistencia(plan: FichaAsistenciaPostEgresoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro seleccionado, esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.planAsistenciaService.eliminarFichaAsistenciaPostEgreso(plan).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerAsistencias();
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

    this.obtenerAsistencias();
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

    this.obtenerAsistencias();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerAsistencias();
  }

  agregarFichaAsistencia() {
    this.router.navigate(['crear-editar-ficha-asistencia'], {
      relativeTo: this.route,
      state: {
        plan: this.planAsistencia
      }
    });
  }

  editarFIchaAsistencia(ficha: FichaAsistenciaPostEgresoDTO) {
    this.router.navigate(['crear-editar-ficha-asistencia'], {
      relativeTo: this.route,
      state: {
        editar: true,
        item: ficha,
        plan: this.planAsistencia
      }
    });
  }

  verFIchaAsistencia(ficha: FichaAsistenciaPostEgresoDTO) {
    this.router.navigate(['crear-editar-ficha-asistencia'], {
      relativeTo: this.route,
      state: {
        visualizar: true,
        item: ficha,
        plan: this.planAsistencia
      }
    });
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

  generarPDF(fichaAsistencia: FichaAsistenciaPostEgresoDTO) {
    this.loadImageAsBase64();

    let tablaFormateada: any[] = [];
    let tabla = new TablaPlantilla();
    if (fichaAsistencia.tipoFormato.nemonico === 'NO_CONTINUAR_PROGRAMA_SEGUIMIENTO' || fichaAsistencia.tipoFormato.nemonico === 'ACTIVIDADES_PREVIAS_CULMINAR') {
      for (let item of fichaAsistencia.detalleFichaAsistenciaPostEgresos) {
        let elemento = {
          fechaDetalle: this.funcionesUtils.getLocalDate(item.fechaDetalle),
          modalidadDeEntrevista: item.modalidadDeEntrevista.nombre,
          observaciones: item.observaciones,
          descripcionActividad: item.descripcionActividad,
        }
        tablaFormateada.push(elemento);
      }
      tabla.encabezados = [
        'Fecha',
        'Modalidad',
        'Observaciones',
        'Descripcion actividad',
      ];
      tabla.filas = tablaFormateada;
    } else if (fichaAsistencia.tipoFormato.nemonico === 'ACTIVIDAD_SEGUIMIENTO_PASPE') {
      for (let item of fichaAsistencia.detalleFichaAsistenciaPostEgresos) {
        let elemento = {
          fechaDetalle: this.funcionesUtils.getLocalDate(item.fechaDetalle),
          modalidadDeEntrevista: item.modalidadDeEntrevista.nombre,
          personaEntrevistada: item.personaEntrevistada.nombre,
          observaciones: item.observaciones,
          descripcionActividad: item.descripcionActividad,
        }
        tablaFormateada.push(elemento);
      }
      tabla.encabezados = [
        'Fecha',
        'Modalidad',
        'Persona entrevistada',
        'Observaciones',
        'Descripcion actividad',
      ];
      tabla.filas = tablaFormateada;
    } else {
      for (let item of fichaAsistencia.detalleFichaAsistenciaPostEgresos) {
        let elemento = {
          fechaDetalle: this.funcionesUtils.getLocalDate(item.fechaDetalle),
          modalidadDeEntrevista: item.modalidadDeEntrevista.nombre,
          personaEntrevistada: item.personaEntrevistada.nombre,
          observaciones: item.observaciones,
          descripcionActividad: item.descripcionActividad,
          motivo: item.motivo.nombre,
        }
        tablaFormateada.push(elemento);
      }
      tabla.encabezados = [
        'Fecha',
        'Modalidad',
        'Persona entrevistada',
        'Observaciones',
        'Descripcion actividad',
        'motivo'
      ];
      tabla.filas = tablaFormateada;
    }

    console.log("FichaID:", fichaAsistencia.idFichaIdentificacion);
    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(fichaAsistencia.idFichaIdentificacion, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          console.log(tabla);
          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_PLAN_ASISTENCIA_SEGUIMIENTO;
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[TITULO-PLANTILLA]": 'Ficha de asistencia/Seguimiento',
            "[TITULO-INFORME]": 'Ficha de asistencia/Seguimiento',
            "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
            "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
            "[CENTRO]": fichaIdentificacion.centroIngreso,
            "[ADOLESCENTE]": nombreAdolescente,
            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
            "[ESTADO_ACTIVIDAD_SOCIO_EDUCATIVA]": fichaAsistencia.tipoFormato.nombre,
            "[AREA_INTERVENCION]": fichaAsistencia.planAsistenciaPostEgresoDetalle.area.nombre,
            "[TABLA-ACTIVIDADES]": JSON.stringify(tabla),
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
}
