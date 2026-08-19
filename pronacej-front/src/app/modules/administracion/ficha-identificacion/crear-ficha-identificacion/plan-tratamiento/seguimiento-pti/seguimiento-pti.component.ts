import { Component, OnInit, ViewChild } from '@angular/core';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PlanTratamientoIndDTO, PlanTratamientoIndSeguiDTO, PlanTratamientoSeguimientoDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ExpedienteMatrizDetalleDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { MatDialog } from '@angular/material/dialog';
import { ModalMostrarDocsSeguiPtiComponent } from './modal-mostrar-docs-segui-pti/modal-mostrar-docs-segui-pti.component';
import { ModalSubirDocsSeguiPtiComponent } from './modal-subir-docs-segui-pti/modal-subir-docs-segui-pti.component';


  function serializarTabla(tabla: TablaPlantilla): string {
  if (!tabla || !Array.isArray(tabla.encabezados) || !Array.isArray(tabla.filas)) {
    return JSON.stringify({
      encabezados: [],
      filas: [{ 'Sin datos': 'No hay registros disponibles' }]
    });
  }
  return JSON.stringify(tabla);
}

@Component({
  selector: 'app-seguimiento-pti',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    TablaListaComponent,
  ],
  templateUrl: './seguimiento-pti.component.html',
  styleUrl: './seguimiento-pti.component.scss'
})


export class SeguimientoPtiComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  listaSeguimientos: PlanTratamientoIndSeguiDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  dataSource: MatTableDataSource<PlanTratamientoIndSeguiDTO>;

  uuid_fp: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_PTI;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de Creación",
    // idPlanTratamientoIndSegui: "Núm. Registro",
    fecInicio: "Fecha Inicio",
    fecFin: "Fecha Fin",
    observaciones: "Observaciones",
    recomendaciones: "Recomendaciones",
    // version: "Versión",
    // reajuste: "Reajuste",
    // fechaReajuste: "Fecha de Reajuste",    

  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private planTratamientoService: PlanTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    private http: HttpClient,
    private servicioPdf: PdfService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private catalogoService: CatalogoService,
    private expedienteMatrizService: ExpedienteMatrizService,
    public funcionesUtils: FuncionesUtils,
    private dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.obtenerSeguimientos();
  }

  // getKeys() {
  //   return Object.keys(this.keyLabelsTable);
  // }

  // handlePageEvent(pageEvent: PageEvent) {
  //   this.size = pageEvent.pageSize;
  //   this.page = pageEvent.pageIndex;
  // }

  obtenerSeguimientos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.planTratamientoService.obtenerSeguimientosPlanesTratamiento(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndSeguiDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.planTratamientoService.checkError(response);
            return;
          }
          this.listaSeguimientos = response.data.data;
          // this.dataSource = new MatTableDataSource(this.listaSeguimientos);
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.planTratamientoService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.planTratamientoService.obtenerSeguimientosPlanesTratamiento(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndSeguiDTO>>) => {

          if (!response.exito) {
            this.planTratamientoService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.planTratamientoService.checkError(error);
        }
      }
    );
  }

  eliminarSeguimiento(seguimiento: PlanTratamientoIndSeguiDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro: \"" + seguimiento.idPlanTratamientoIndSegui + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.planTratamientoService.eliminarSeguimientoPlanTratamiento(seguimiento, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerSeguimientos();
                },
                error: (error: any) => {
                  load.close();

                  this.planTratamientoService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  verSeguimiento(seguimiento: PlanTratamientoIndSeguiDTO) {
    this.router.navigate(['seguimiento/crear-editar-cerrado'], {
      relativeTo: this.route,
      state: {
        seguimiento: seguimiento,
      }
    });
  }

  editarSeguimiento(seguimiento: PlanTratamientoIndSeguiDTO) {
    this.router.navigate(['seguimiento/crear-editar-cerrado'], {
      relativeTo: this.route,
      state: {
        seguimiento: seguimiento,
        editar: true
      }
    });
  }

  agregarSeguimiento() {
    this.router.navigate(['seguimiento/crear-editar-cerrado'], {
      relativeTo: this.route,
    });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerSeguimientos();
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

    this.obtenerSeguimientos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerSeguimientos();
  }

  /**
   * Genera un PDF para el seguimiento PTI seleccionado
   * @param seguimiento El objeto de seguimiento a imprimir
   */
  generarPDF(seguimiento: PlanTratamientoIndSeguiDTO) {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de seguimiento PTI?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.dialogMensajeService.mensajeLoading("Preparando la impresión del informe de seguimiento PTI...");

          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }

                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      const direccion = fichaIdentificacion.direccion || '';

                      // 5. Obtener grado de instrucción desde catálogos
                      this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
                        next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
                          const catalogoModalidadEstudio = respuestaCatalogo.data;
                          const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

                          // 6. Obtener último detalle de expediente matriz
                          this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                            next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                              const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                              const juzgadoProcedencia = detalleExpediente?.organoJurisdiccional || '';
                              const numOficio = detalleExpediente?.numResolucion || '';
                              const numExpediente = detalleExpediente?.numExpediente || '';
                              const infraccion = detalleExpediente?.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                              const fechaSentencia = detalleExpediente?.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaCreacion) : '';
                              const tipoMedida = detalleExpediente?.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                              const duracionMedida = `${detalleExpediente?.tiempoMedSocEduDias ? detalleExpediente?.tiempoMedSocEduDias : 0} días, ${detalleExpediente?.tiempoMedSocEduMeses ? detalleExpediente?.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente?.tiempoMedSocEduAnios ? detalleExpediente?.tiempoMedSocEduAnios : 0} años.`;
                              const inicioMedida = detalleExpediente?.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaInicioMedida) : '';
                              const finMedida = detalleExpediente?.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaFinMedida) : '';
                              const numExpJudicial = detalleExpediente?.numExpedienteJudicial || '';
                              // 7. Obtener datos del plan de tratamiento para determinar el tipo de centro
                              this.planTratamientoService.obtenerPlanTratamientoPorId(seguimiento.idPlanTratamiento, this.nemonicoMenu)
                                .subscribe({
                                  next: (respuestaPlan: RespuestaPorDefecto<any>) => {
                                    if (!respuestaPlan.exito) {
                                      dialogoCarga.close();
                                      this.dialogMensajeService.mensajeError('Error al obtener datos del plan de tratamiento');
                                      return;
                                    }

                                    const planTratamiento = respuestaPlan.data;
                                    const esCentroAbierto = planTratamiento.tipoCentro === 'SOA';
                                    const esServicioComunidad = esCentroAbierto && planTratamiento.tipoAbierto === 'Prestación de Servicios a la Comunidad';

                                    if (esCentroAbierto) {
                                      // 6A. Procesamiento para SOA
                                      // Matriz PTI
                                      let tablaMatrizPTI = new TablaPlantilla();
                                      tablaMatrizPTI.encabezados = [
                                        'Componente',
                                        'Objetivo',
                                        'Actividad o Programa',
                                        'Indicador (D)',
                                        'Indicador (EP)',
                                        'Indicador (L)',
                                        'Análisis'
                                      ];

                                      tablaMatrizPTI.filas = seguimiento.intervObjetivos && seguimiento.intervObjetivos.length > 0
                                        ? seguimiento.intervObjetivos.map(objetivo => ({
                                          'Componente': objetivo.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                          'Objetivo': objetivo.planTratamientoIndInterv?.objetivo || 'No especificado',
                                          'Actividad o Programa': objetivo.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                          'Indicador (D)': objetivo.indicadorDeficiente ? 'X' : '',
                                          'Indicador (EP)': objetivo.indicadorEnProceso ? 'X' : '',
                                          'Indicador (L)': objetivo.indicadorLogrado ? 'X' : '',
                                          'Análisis': objetivo.analisis || 'No especificado'
                                        }))
                                        : [{
                                          'Componente': 'No hay registros disponibles',
                                          'Objetivo': 'No hay registros disponibles',
                                          'Actividad o Programa': '-',
                                          'Indicador (D)': '-',
                                          'Indicador (EP)': '-',
                                          'Indicador (L)': '-',
                                          'Análisis': '-'
                                        }];

                                      // Matriz de cumplimiento de medidas accesorias
                                      let tablaMedidasAccesorias = new TablaPlantilla();
                                      tablaMedidasAccesorias.encabezados = [
                                        'Componente',
                                        'Objetivo',
                                        'Actividad o Programa',
                                        'Indicador (D)',
                                        'Indicador (EP)',
                                        'Indicador (L)',
                                        'Análisis'
                                      ];

                                      tablaMedidasAccesorias.filas = seguimiento.intervDiferenciada && seguimiento.intervDiferenciada.length > 0
                                        ? seguimiento.intervDiferenciada.map(intervencion => ({
                                          'Componente': intervencion.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                          'Objetivo': intervencion.planTratamientoIndInterv?.objetivo || 'No especificado',
                                          'Actividad o Programa': intervencion.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                          'Indicador (D)': intervencion.indicadorDeficiente ? 'X' : '',
                                          'Indicador (EP)': intervencion.indicadorEnProceso ? 'X' : '',
                                          'Indicador (L)': intervencion.indicadorLogrado ? 'X' : '',
                                          'Análisis': intervencion.analisis || 'No especificado'
                                        }))
                                        : [{
                                          'Componente': 'No hay registros disponibles',
                                          'Objetivo': 'No hay registros disponibles',
                                          'Actividad o Programa': '-',
                                          'Indicador (D)': '-',
                                          'Indicador (EP)': '-',
                                          'Indicador (L)': '-',
                                          'Análisis': '-'
                                        }];

                                      // Si hay tablas de comunidad, preparamos sus datos
                                      let tablaFasePreparacion = null;
                                      let tablaFaseEjecucion = null;

                                      if (esServicioComunidad) {
                                        tablaFasePreparacion = new TablaPlantilla();
                                        tablaFasePreparacion.encabezados = [
                                          'Componente',
                                          'Objetivo',
                                          'Actividad o Programa',
                                          'Indicador (D)',
                                          'Indicador (EP)',
                                          'Indicador (L)',
                                          'Análisis'
                                        ];

                                        tablaFasePreparacion.filas = seguimiento.intervNoCriminogenos && seguimiento.intervNoCriminogenos.length > 0
                                          ? seguimiento.intervNoCriminogenos.map(item => ({
                                            'Componente': item.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                            'Objetivo': item.planTratamientoIndInterv?.objetivo || 'No especificado',
                                            'Actividad o Programa': item.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                            'Indicador (D)': item.indicadorDeficiente ? 'X' : '',
                                            'Indicador (EP)': item.indicadorEnProceso ? 'X' : '',
                                            'Indicador (L)': item.indicadorLogrado ? 'X' : '',
                                            'Análisis': item.analisis || 'No especificado'
                                          }))
                                          : [{
                                            'Componente': 'No hay registros disponibles',
                                            'Objetivo': 'No hay registros disponibles',
                                            'Actividad o Programa': '-',
                                            'Indicador (D)': '-',
                                            'Indicador (EP)': '-',
                                            'Indicador (L)': '-',
                                            'Análisis': '-'
                                          }];

                                        tablaFaseEjecucion = new TablaPlantilla();
                                        tablaFaseEjecucion.encabezados = [
                                          'Componente',
                                          'Objetivo',
                                          'Actividad o Programa',
                                          'Indicador (D)',
                                          'Indicador (EP)',
                                          'Indicador (L)',
                                          'Análisis'
                                        ];

                                        tablaFaseEjecucion.filas = seguimiento.intervMedidas && seguimiento.intervMedidas.length > 0
                                          ? seguimiento.intervMedidas.map(item => ({
                                            'Componente': item.planTratamientoIndInterv?.dimension?.nombre || 'No especificado',
                                            'Objetivo': item.planTratamientoIndInterv?.objetivo || 'No especificado',
                                            'Actividad o Programa': item.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                            'Indicador (D)': item.indicadorDeficiente ? 'X' : '',
                                            'Indicador (EP)': item.indicadorEnProceso ? 'X' : '',
                                            'Indicador (L)': item.indicadorLogrado ? 'X' : '',
                                            'Análisis': item.analisis || 'No especificado'
                                          }))
                                          : [{
                                            'Componente': 'No hay registros disponibles',
                                            'Objetivo': 'No hay registros disponibles',
                                            'Actividad o Programa': '-',
                                            'Indicador (D)': '-',
                                            'Indicador (EP)': '-',
                                            'Indicador (L)': '-',
                                            'Análisis': '-'
                                          }];
                                      }

                                      // Crear la solicitud para generar el PDF
                                      let solicitudPdf = new GeneracionPdfRequest();
                                      solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_PTI_SOA';

                                      // Incluir las variables para el PDF
                                      solicitudPdf.variables = {
                                        "[IMG_BASE64]": imagenBase64,
                                        "[FECHA-REGISTRO]": this.funcionesUtils.formatearFecha(new Date()),
                                        "[HORA-REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                                        "[CENTRO]": fichaIdentificacion.centroIngreso || '',
                                        "[NOMBRES-APELLIDOS]": nombreCompleto,
                                        "[DNI]": fichaIdentificacion.numeroDocumento || '',
                                        "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
                                        "[EDAD]": edadActual,
                                        "[GRADO-INSTRUCCION]": gradoInstruccion,
                                        "[DIRECCION]": direccion,
                                        "[JUZGADO-PROCEDENCIA]": juzgadoProcedencia,
                                        "[NUM-OFICIO-JUZGADO]": numOficio,
                                        "[INFRACCION]": infraccion,
                                        "[FECHA-SENTENCIA]": fechaSentencia,
                                        "[TIPO-MEDIDA]": tipoMedida,
                                        "[DURACION-MEDIDA]": duracionMedida,
                                        "[FECHA-INICIO-MEDIDA]": inicioMedida,
                                        "[FECHA-FINALIZACION]": finMedida,
                                        "[FECHA-INICIO]": this.funcionesUtils.formatearFecha(seguimiento.fechaInicio) || 'No especificado',
                                        "[FECHA-FIN]": this.funcionesUtils.formatearFecha(seguimiento.fechaFin) || 'No especificado',
                                        "[TABLA-MATRIZ-PTI]": JSON.stringify(tablaMatrizPTI),
                                        "[TABLA-MEDIDAS-ACCESORIAS]": JSON.stringify(tablaMedidasAccesorias),
                                        "[RECOMENDACIONES]": seguimiento.recomendaciones || 'No se registraron recomendaciones',
                                        "[MOSTRAR-COMUNIDAD]": esServicioComunidad ? "block" : "none",
                                         "[NUM-EXPJUDICIAL]": numExpJudicial,
                                      };

                                      // Agregar variables condicionales para comunidad
                                      if (esServicioComunidad) {
                                        solicitudPdf.variables["[TABLA-FASE-PREPARACION]"] = JSON.stringify(tablaFasePreparacion);
                                        solicitudPdf.variables["[TABLA-FASE-EJECUCION]"] = JSON.stringify(tablaFaseEjecucion);
                                      } else {
                                        let tablaVacia = new TablaPlantilla;
                                        solicitudPdf.variables["[TABLA-FASE-PREPARACION]"] = JSON.stringify(tablaVacia);
                                        solicitudPdf.variables["[TABLA-FASE-EJECUCION]"] = JSON.stringify(tablaVacia);
                                      }

                                      // Generar PDF
                                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                                        next: (respuestaPdf) => this.manejarRespuestaPdf(respuestaPdf, dialogoCarga),
                                        error: (error) => this.manejarErrorPdf(error, dialogoCarga)
                                      });

                                    } else {
                                      // 6B. Procesamiento para CJDR
                                      // Crear tabla de objetivos de intervención
                                      let tablaObjetivosIntervencion = new TablaPlantilla();
                                      tablaObjetivosIntervencion.encabezados = [
                                        'Actividad o Programa',
                                        'Situación Actual',
                                        'Frecuencia',
                                        'Actitud',
                                        'Aprovechamiento'
                                      ];

                                      tablaObjetivosIntervencion.filas = seguimiento.intervObjetivos && seguimiento.intervObjetivos.length > 0
                                        ? seguimiento.intervObjetivos.map(objetivo => ({
                                          'Actividad o Programa': objetivo.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                          'Situación Actual': objetivo.situacionActual?.nombre || 'No especificado',
                                          'Frecuencia': objetivo.frecuenciaParticipacion?.nombre || 'No especificado',
                                          'Actitud': objetivo.actitud?.nombre || 'No especificado',
                                          'Aprovechamiento': objetivo.aprovechamiento?.nombre || 'No especificado'
                                        }))
                                        : [{
                                          'Actividad o Programa': 'No hay registros disponibles',
                                          'Situación Actual': '-',
                                          'Frecuencia': '-',
                                          'Actitud': '-',
                                          'Aprovechamiento': '-'
                                        }];

                                      // Crear tabla de factores no criminógenos
                                      let tablaFactoresNoCriminogenos = new TablaPlantilla();
                                      tablaFactoresNoCriminogenos.encabezados = [
                                        'Actividad o Programa',
                                        'Situación Actual',
                                        'Frecuencia',
                                        'Actitud',
                                        'Aprovechamiento'
                                      ];

                                      tablaFactoresNoCriminogenos.filas = seguimiento.intervNoCriminogenos && seguimiento.intervNoCriminogenos.length > 0
                                        ? seguimiento.intervNoCriminogenos.map(factor => ({
                                          'Actividad o Programa': factor.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                          'Situación Actual': factor.situacionActual?.nombre || 'No especificado',
                                          'Frecuencia': factor.frecuenciaParticipacion?.nombre || 'No especificado',
                                          'Actitud': factor.actitud?.nombre || 'No especificado',
                                          'Aprovechamiento': factor.aprovechamiento?.nombre || 'No especificado'
                                        }))
                                        : [{
                                          'Actividad o Programa': 'No hay registros disponibles',
                                          'Situación Actual': '-',
                                          'Frecuencia': '-',
                                          'Actitud': '-',
                                          'Aprovechamiento': '-'
                                        }];

                                      // Crear tabla de intervención diferenciada
                                      let tablaIntervencionDiferenciada = new TablaPlantilla();
                                      tablaIntervencionDiferenciada.encabezados = [
                                        'Actividad o Programa',
                                        'Situación Actual',
                                        'Frecuencia',
                                        'Actitud',
                                        'Aprovechamiento'
                                      ];

                                      tablaIntervencionDiferenciada.filas = seguimiento.intervDiferenciada && seguimiento.intervDiferenciada.length > 0
                                        ? seguimiento.intervDiferenciada.map(intervencion => ({
                                          'Actividad o Programa': intervencion.planTratamientoIndInterv?.actividadPrograma || 'No especificado',
                                          'Situación Actual': intervencion.situacionActual?.nombre || 'No especificado',
                                          'Frecuencia': intervencion.frecuenciaParticipacion?.nombre || 'No especificado',
                                          'Actitud': intervencion.actitud?.nombre || 'No especificado',
                                          'Aprovechamiento': intervencion.aprovechamiento?.nombre || 'No especificado'
                                        }))
                                        : [{
                                          'Actividad o Programa': 'No hay registros disponibles',
                                          'Situación Actual': '-',
                                          'Frecuencia': '-',
                                          'Actitud': '-',
                                          'Aprovechamiento': '-'
                                        }];

                                      // Crear la solicitud para generar el PDF
                                      let solicitudPdf = new GeneracionPdfRequest();
                                      solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_PTI';
                                      console.log(tablaObjetivosIntervencion,tablaFactoresNoCriminogenos,tablaIntervencionDiferenciada);
                                      

                                      //Incluir las variables para el PDF
                                      solicitudPdf.variables = {
                                        "[IMG_BASE64]": imagenBase64,
                                        "[FECHA-REGISTRO]": this.funcionesUtils.formatearFecha(new Date()),
                                        "[HORA-REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                                        "[CENTRO]": fichaIdentificacion.centroIngreso || '',
                                        "[NOMBRES-APELLIDOS]": nombreCompleto,
                                        "[DNI]": fichaIdentificacion.numeroDocumento || '',
                                        "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
                                        "[EDAD]": edadActual,
                                        "[DIRECCION]": direccion,
                                        "[GRADO-INSTRUCCION]": gradoInstruccion,
                                        "[JUZGADO-PROCEDENCIA]": juzgadoProcedencia,
                                        "[NUM_EXPEDIENTE]": numExpediente,
                                        "[INFRACCION]": infraccion,
                                        "[FECHA-SENTENCIA]": fechaSentencia,
                                        "[TIPO-MEDIDA]": tipoMedida,
                                        "[DURACION-MEDIDA]": duracionMedida,
                                        "[FECHA-INICIO-MEDIDA]": inicioMedida,
                                        "[FECHA-FINALIZACION]": finMedida,
                                        "[TIPO-PERIODO]": seguimiento.periodoTiempo?.nombre || 'No especificado',
                                        "[FECHA-INICIO]": this.funcionesUtils.formatearFecha(seguimiento.fechaInicio) || 'No especificado',
                                        "[FECHA-FIN]": this.funcionesUtils.formatearFecha(seguimiento.fechaFin) || 'No especificado',
                                        "[PROGRAMA]": seguimiento.programa || 'No especificado',
                                        "[RESUMEN]": seguimiento.resumen || 'No especificado',
                                        "[ESTADO-SALUD]": seguimiento.estadoSalud || 'No especificado',
                                        // "[TABLA-OBJETIVOS-INTERVENCION]": JSON.stringify(tablaObjetivosIntervencion),
                                        // "[TABLA-FACTORES-NO-CRIMINOGENOS]": JSON.stringify(tablaFactoresNoCriminogenos),
                                        // "[TABLA-INTERVENCION-DIFERENCIADA]": JSON.stringify(tablaIntervencionDiferenciada),
                                        "[TABLA-OBJETIVOS-INTERVENCION]": serializarTabla(tablaObjetivosIntervencion),
                                        "[TABLA-FACTORES-NO-CRIMINOGENOS]": serializarTabla(tablaFactoresNoCriminogenos),
                                        "[TABLA-INTERVENCION-DIFERENCIADA]": serializarTabla(tablaIntervencionDiferenciada),
                                        "[OBSERVACIONES]": seguimiento.observaciones || 'No se registraron observaciones',
                                        "[RECOMENDACIONES]": seguimiento.recomendaciones || 'No se registraron recomendaciones',
                                         "[NUM-EXPJUDICIAL]": numExpJudicial,
                                      };

                                      //Generar PDF
                                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                                        next: (respuestaPdf) => this.manejarRespuestaPdf(respuestaPdf, dialogoCarga),
                                        error: (error) => this.manejarErrorPdf(error, dialogoCarga)
                                      });
                                    }
                                  },
                                  error: (error: any) => {
                                    dialogoCarga.close();
                                    console.error('Error al obtener plan de tratamiento:', error);
                                    this.dialogMensajeService.mensajeError('Error al obtener los datos del plan de tratamiento');
                                  }
                                });
                            },
                            error: (error: any) => {
                              dialogoCarga.close();
                              console.error('Error al obtener el detalle del último expediente:', error);
                              this.dialogMensajeService.mensajeError('Error al obtener el detalle del último expediente');
                            }
                          });


                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al obtener catálogo:', error);
                          this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
                        }
                      });


                    },
                    error: (error: any) => {
                      dialogoCarga.close();
                      console.error('Error al obtener ficha:', error);
                      this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
                    }
                  });
              },
              error: (error) => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.dialogMensajeService.mensajeError('Error al cargar la imagen del logo');
              }
            });
        }
      }
    });
  }

  abrirDocumentosCargados(seguimiento: PlanTratamientoIndSeguiDTO) {
    this.dialog.open(ModalMostrarDocsSeguiPtiComponent, {
      width: '80%',
      data: seguimiento
    });     
  }
  
  abrirCargaDeDocumentos(seguimiento: PlanTratamientoIndSeguiDTO) {
    this.dialog.open(ModalSubirDocsSeguiPtiComponent, {
      width: '80%',
      data: seguimiento
    });     
  }

  /**
   * Maneja la respuesta exitosa del servicio de generación de PDF
   * @param respuesta Respuesta del servicio
   * @param dialogoCarga Referencia al diálogo de carga para cerrarlo
   */
  private manejarRespuestaPdf(respuesta: RespuestaPorDefecto<string>, dialogoCarga: any) {
    dialogoCarga.close();
    if (!respuesta.exito) {
      console.error('Error al generar PDF:', respuesta);
      this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
      return;
    }

    // Abrir el PDF en una nueva pestaña
    const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
    window.open(url);
  }

  /**
   * Maneja errores en la generación del PDF
   * @param error Error producido
   * @param dialogoCarga Referencia al diálogo de carga para cerrarlo
   */
  private manejarErrorPdf(error: any, dialogoCarga: any) {
    dialogoCarga.close();
    console.error('Error al generar PDF:', error);
    this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
  }
}


