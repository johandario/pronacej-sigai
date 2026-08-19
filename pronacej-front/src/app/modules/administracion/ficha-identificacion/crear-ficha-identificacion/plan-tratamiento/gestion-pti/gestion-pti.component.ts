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
import { PlanTratamientoIndDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { EstadoBorrador, TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { HttpClient } from '@angular/common/http';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { PdfService } from 'app/core/services/pdf.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ExpedienteMatrizDetalleDTO } from 'app/core/model/both/expedienteMatrizDTO.model';

@Component({
  selector: 'app-gestion-pti',
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
    TablaListaComponent
  ],
  templateUrl: './gestion-pti.component.html',
  styleUrl: './gestion-pti.component.scss'
})
export class GestionPtiComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "Plan de tratamiento individual";

  listaPlanes: PlanTratamientoIndDTO[] = [];
  dataSource: MatTableDataSource<PlanTratamientoIndDTO>;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  uuid_fp: string;

  base64Image: string | null = null;

  estadoBorrador: EstadoBorrador = {
    prop1: 'estado',
    prop2: 'nemonico',
    nemonico: etiquetasModel.NEMONICO_ESTADO_PTI_BORRADOR
  }

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_PTI;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    // idPlanTratamiento: "Núm. Registro",
    // version: "Versión",
    // reajuste: "Reajuste",
    // fechaReajuste: "Fecha de Reajuste",    
    tipoCentro: "Centro",
    tipoAbierto: "Tipo",
    valRiesgo: "Valoración de riesgo",
    reajuste: "Reajuste",
    nombreEstado: "Estado",
  };

  constructor(
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private planTratamientoService: PlanTratamientoService,
    private http: HttpClient,
    private fichaIdentificacionService: FichaIdentificacionService,
    private pdfService: PdfService,
    public funcionesUtils: FuncionesUtils,
    private catalogoService: CatalogoService,
    private expedienteMatrizService: ExpedienteMatrizService
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerPlanes();
  }

  obtenerPlanes() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.planTratamientoService.obtenerPlanesTratamiento(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.planTratamientoService.checkError(response);
            return;
          }
          this.listaPlanes = this.obtenerReajustes(response.data.data);
          // this.dataSource = new MatTableDataSource(this.listaPlanes);
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

    this.planTratamientoService.obtenerPlanesTratamiento(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndDTO>>) => {

          if (!response.exito) {
            this.planTratamientoService.checkError(response);
            return;
          }

          let listaPlanes = this.obtenerReajustes(response.data.data);
          this.tablaComponent.exportXLSX(listaPlanes);
        },
        error: (error: any) => {
          this.planTratamientoService.checkError(error);
        }
      }
    );
  }

  // getLocalDate(date: Date) {
  //   return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  // }

  // getKeys() {
  //   return Object.keys(this.keyLabelsTable);
  // }

  // handlePageEvent(pageEvent: PageEvent) {
  //   this.size = pageEvent.pageSize;
  //   this.page = pageEvent.pageIndex;
  // }

  verPlan(planTratamiento: PlanTratamientoIndDTO) {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar`], { queryParams: { numDoc: planTratamiento.idPlanTratamiento, state: 'show' } })
  }

  editarPlan(planTratamiento: PlanTratamientoIndDTO) {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar`], { queryParams: { numDoc: planTratamiento.idPlanTratamiento } })
  }

  agregarPlan() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar`])
  }

  eliminarPlan(planTratamiento: PlanTratamientoIndDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro: \"" + planTratamiento.idPlanTratamiento + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.planTratamientoService.eliminarPlanTratamiento(planTratamiento, this.nemonicoMenu).subscribe(
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

                  this.planTratamientoService.checkError(error);
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

  obtenerReajustes(planes: PlanTratamientoIndDTO[]): PlanTratamientoIndDTO[] {
    for (let plan of planes) {
      plan = this.existeReajuste(plan);
    }
    return planes;
  }

  existeReajuste(plan: PlanTratamientoIndDTO): PlanTratamientoIndDTO {
    if (plan.intervObjetivos.length > 0) {
      for (let interv of plan.intervObjetivos) {
        if (interv.reajuste) {
          plan.reajuste = 'Si';
          return plan;
        }
      }
    }

    if (plan.intervNoCriminogenos.length > 0) {
      for (let interv of plan.intervNoCriminogenos) {
        if (interv.reajuste) {
          plan.reajuste = 'Si';
          return plan;
        }
      }
    }

    if (plan.intervDiferenciada.length > 0) {
      for (let interv of plan.intervDiferenciada) {
        if (interv.reajuste) {
          plan.reajuste = 'Si';
          return plan;
        }
      }
    }

    plan.reajuste = 'No';
    return plan;
  }

  generarPDF(plan: PlanTratamientoIndDTO) {
    this.loadImageAsBase64();

    if (plan.tipoCentro == 'CJDR') {
      this.generarPDFMedioCerrado(plan);
    } else if (plan.tipoCentro == 'SOA') {
      if (plan.tipoAbierto == 'Libertad Restringida/Libertad Asistida') {
        this.generarPDFMedioAbiertoLibertad(plan);
      } else if (plan.tipoAbierto == 'Prestación de Servicios a la Comunidad') {
        this.generarPDFMedioAbiertoComunidad(plan);
      } else if (plan.tipoAbierto == 'Amonestación o Semilibertad') {
        this.generarPDFMedioAbiertoAmonestacion(plan);
      }
    }
  }

  generarPDFMedioCerrado(plan: PlanTratamientoIndDTO) {
    // TABLA FACTORES
    let tablaFormateada: any[] = [];
    for (let factores of plan.especFactores) {
      let elemento = {
        Dimensiones: factores.dimension.nombre,
        Factores_de_riesgo: factores.factorRiesgo || '',
        Factores_protectores: factores.factorProtector || '',
      }
      tablaFormateada.push(elemento);
    }

    let tablaFactores = new TablaPlantilla();
    tablaFactores.encabezados = ['Dimensiones', 'Factores de riesgo', 'Factores protectores'];
    tablaFactores.filas = tablaFormateada;

    // TABLA OBJETIVOS
    tablaFormateada = [];
    for (let interv of plan.intervObjetivos) {
      if (interv.activo) {
        let elemento = {
          Dimensiones: interv.dimension.nombre,
          Objetivos: interv.objetivo,
          Actividad: interv.actividadPrograma,
          Responsable: interv.equipoResponsable,
          Tiempo_estimado: interv.tiempoEstimado,
        }
        tablaFormateada.push(elemento);
      }
    }

    let tablaOjetivos = new TablaPlantilla();
    tablaOjetivos.encabezados = ['Dimensiones', 'Objetivos', 'Actividad', 'Responsable', 'Tiempo estimado'];
    tablaOjetivos.filas = tablaFormateada;

    // TABLA NO CRIMINÓGENO
    tablaFormateada = [];
    for (let interv of plan.intervNoCriminogenos) {
      if (interv.activo) {
        let elemento = {
          Dimensiones: interv.dimension.nombre,
          Objetivos: interv.objetivo,
          Actividad: interv.actividadPrograma,
          Responsable: interv.equipoResponsable,
          Tiempo_estimado: interv.tiempoEstimado,
        }
        tablaFormateada.push(elemento);
      }
    }

    let tablaNoCriminogeno = new TablaPlantilla();
    tablaNoCriminogeno.encabezados = ['Dimensiones', 'Objetivos', 'Actividad', 'Responsable', 'Tiempo estimado'];
    tablaNoCriminogeno.filas = tablaFormateada;

    // TABLA INTERV DIFERENCIADA
    tablaFormateada = [];
    for (let interv of plan.intervDiferenciada) {
      if (interv.activo) {
        let elemento = {
          Dimensiones: interv.dimension.nombre,
          Objetivos: interv.objetivo,
          Actividad: interv.actividadPrograma,
          Responsable: interv.equipoResponsable,
          Tiempo_estimado: interv.tiempoEstimado,
        }
        tablaFormateada.push(elemento);
      }
    }

    let tablaDiferenciada = new TablaPlantilla();
    tablaDiferenciada.encabezados = ['Dimensiones', 'Objetivos', 'Actividad', 'Responsable', 'Tiempo estimado'];
    tablaDiferenciada.filas = tablaFormateada;

    // TABLA OBJETIVOS REAJUSTE
    tablaFormateada = [];
    for (let interv of plan.intervObjetivos) {
      if (interv.activo && interv.reajuste) {
        let elemento = {
          Dimensiones: interv.dimension.nombre,
          Actividad: interv.actividadPrograma,
          Reajuste: interv.reajuste ? 'Si' : 'No',
          Fecha_reajuste: interv.fechaReajuste ? this.funcionesUtils.getLocalDate(interv.fechaReajuste) : 'N/A',
          Motivo: interv.fundamentacionReajuste ? interv.fundamentacionReajuste : 'N/A'
        }
        tablaFormateada.push(elemento);
      }
    }

    let tablaOjetivosReajuste = new TablaPlantilla();
    tablaOjetivosReajuste.encabezados = ['Dimensiones', 'Actividad', 'Reajuste', 'Fecha de reajuste', 'Motivo'];
    tablaOjetivosReajuste.filas = tablaFormateada;

    // TABLA NO CRIMINÓGENO REAJUSTE
    tablaFormateada = [];
    for (let interv of plan.intervNoCriminogenos) {
      if (interv.activo && interv.reajuste) {
        let elemento = {
          Dimensiones: interv.dimension.nombre,
          Actividad: interv.actividadPrograma,
          Reajuste: interv.reajuste ? 'Si' : 'No',
          Fecha_reajuste: interv.fechaReajuste ? this.funcionesUtils.getLocalDate(interv.fechaReajuste) : 'N/A',
          Motivo: interv.fundamentacionReajuste ? interv.fundamentacionReajuste : 'N/A'
        }
        tablaFormateada.push(elemento);
      }
    }

    let tablaNoCriminogenoReajuste = new TablaPlantilla();
    tablaNoCriminogenoReajuste.encabezados = ['Dimensiones', 'Actividad', 'Reajuste', 'Fecha de reajuste', 'Motivo'];
    tablaNoCriminogenoReajuste.filas = tablaFormateada;

    // TABLA INTERV DIFERENCIADA REAJUSTE
    tablaFormateada = [];
    for (let interv of plan.intervDiferenciada) {
      if (interv.activo && interv.reajuste) {
        let elemento = {
          Dimensiones: interv.dimension.nombre,
          Actividad: interv.actividadPrograma,
          Reajuste: interv.reajuste ? 'Si' : 'No',
          Fecha_reajuste: interv.fechaReajuste ? this.funcionesUtils.getLocalDate(interv.fechaReajuste) : 'N/A',
          Motivo: interv.fundamentacionReajuste ? interv.fundamentacionReajuste : 'N/A'
        }
        tablaFormateada.push(elemento);
      }
    }

    let tablaDiferenciadaReajuste = new TablaPlantilla();
    tablaDiferenciadaReajuste.encabezados = ['Dimensiones', 'Actividad', 'Reajuste', 'Fecha de reajuste', 'Motivo'];
    tablaDiferenciadaReajuste.filas = tablaFormateada;

    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(plan.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          console.log(fichaIdentificacion);
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';
          const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''}, ${this.formatFecha(fichaIdentificacion.fechaNacimiento)}`;
          const direccion = fichaIdentificacion.direccion || 'N/A';

          //Obtener grado de instrucción desde catálogos
          this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
            next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
              console.log(respuestaCatalogo);
              const catalogoModalidadEstudio = respuestaCatalogo.data;
              const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

              // 6. Obtener último detalle de expediente matriz
              this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                  console.log(respuestaDetalleExpediente);
                  
                  const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                  console.log(detalleExpediente);
                  
                  const juzgadoProcedencia = detalleExpediente?.organoJurisdiccional || '';
                  const numExpediente = detalleExpediente?.numExpediente || '';
                  const infraccion = detalleExpediente?.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                  const fechaSentencia = detalleExpediente?.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaCreacion) : '';
                  const tipoMedida = detalleExpediente?.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                  const duracionMedida = `${detalleExpediente?.tiempoMedSocEduDias ? detalleExpediente?.tiempoMedSocEduDias : 0} días, ${detalleExpediente?.tiempoMedSocEduMeses ? detalleExpediente?.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente?.tiempoMedSocEduAnios ? detalleExpediente?.tiempoMedSocEduAnios : 0} años.`;
                  const inicioMedida = detalleExpediente?.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaInicioMedida) : '';
                  const finMedida = detalleExpediente?.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaFinMedida) : '';
                  const numExpJudicial = detalleExpediente?.numExpedienteJudicial || '';
                  let request = new GeneracionPdfRequest();
                  request.nemonico = etiquetasModel.FORMULARIO_PTI_CERRADO;
                  request.variables = {
                    "[IMG_BASE64]": this.base64Image,
                    "[TITULO-PLANTILLA]": 'Plan de Tratamiento Individual',
                    "[TITULO-INFORME]": 'Plan de Tratamiento Individual - Medio Cerrado',
                    "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
                    "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
                    "[CENTRO]": fichaIdentificacion.centroIngreso,
                    "[ADOLESCENTE]": nombreAdolescente,
                    "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
                    "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                    "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
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
                    "[INSTRUMENTOS]": plan.instTecnicas,
                    "[TABLA-FACTORES]": JSON.stringify(tablaFactores),
                    "[FACTORES]": plan.factRiesgoNoCrimin,
                    "[VAL-RIESGO]": plan.valRiesgo,
                    "[HIP-EXPLICATIVA]": plan.hipotExplicativa,
                    "[INTENSIDAD-INTERV]": plan.intensidadIntervTrat,
                    "[TABLA-OBJETIVOS]": JSON.stringify(tablaOjetivos),
                    "[TABLA-NO-CRIMINOGENO]": JSON.stringify(tablaNoCriminogeno),
                    "[TABLA-INTERV-DIF]": JSON.stringify(tablaDiferenciada),
                    "[TABLA-REAJUSTE-OBJETIVOS]": JSON.stringify(tablaOjetivosReajuste),
                    "[TABLA-REAJUSTE-NO-CRIMINOGENO]": JSON.stringify(tablaNoCriminogenoReajuste),
                    "[TABLA-REAJUSTE-INTERV-DIF]": JSON.stringify(tablaDiferenciadaReajuste),
                    "[NUM-EXPJUDICIAL]": numExpJudicial,
                  }
                  this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
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
                  this.expedienteMatrizService.checkError(error);
                }
              });
            },
            error: (error: any) => {
              this.catalogoService.checkError(error);
            }
          });

        },
        error: (error: any) => {
          this.fichaIdentificacionService.checkError(error);
        }
      }
    );
  }

  generarPDFMedioAbiertoLibertad(plan: PlanTratamientoIndDTO) {
    // TABLA EVALUACIÓN
    let registrosTablaEvaluacion: any[] = [];
    for (let item of plan.especFactores) {
      let elemento = {
        Nombre: item.dimension.nombre,
        Detalle: item.comentario
      }
      registrosTablaEvaluacion.push(elemento);
    }

    let tablaEvaluacion = new TablaPlantilla();
    tablaEvaluacion.encabezados = ['Nombre', 'Detalle'];
    tablaEvaluacion.filas = registrosTablaEvaluacion;

    // TABLA MATRIZ PTI
    let registrosTablaMatrizPti: any[] = [];
    for (let item of plan.intervObjetivos) {
      if (item.activo) {
        let elemento = {
          Componentes: item.dimension.nombre,
          Objetivo: item.objetivo,
          Actividades: item.actividadPrograma,
          Periodo: item.tiempoEstimado,
          Modalidad: item.modalidad.nombre,
          Frecuencia: item.frecuencia.nombre,
          Responsable: item.equipoResponsable,
        }
        registrosTablaMatrizPti.push(elemento);
      }
    }

    let tablaMatrizPti = new TablaPlantilla();
    tablaMatrizPti.encabezados = ['Componentes', 'Objetivo', 'Actividades', 'Periodo', 'Modalidad', 'Frecuencia', 'Responsable'];
    tablaMatrizPti.filas = registrosTablaMatrizPti;

    // TABLA CONTROL ACTIVIDADES
    let registrosTablaControlActividades: any[] = [];
    for (let item of plan.intervNoCriminogenos) {
      if (item.activo) {
        let elemento = {
          Atencion: item.dimension.nombre,
          Horario: item.tiempoEstimado,
          Lugar: item.lugar,
        }
        registrosTablaControlActividades.push(elemento);
      }
    }

    let tablaControlActividades = new TablaPlantilla();
    tablaControlActividades.encabezados = ['Atención', 'Horario', 'Lugar'];
    tablaControlActividades.filas = registrosTablaControlActividades;

    // TABLA CUMPLIMIENTO MEDIDAS ACCESORIAS
    let registrosTablaMedidasAccesorias: any[] = [];
    for (let item of plan.intervDiferenciada) {
      if (item.activo) {
        let elemento = {
          Medida_accesoria: item.dimension?.nombre || '',
          Objetivo: item.objetivo,
          Actividades: item.actividadPrograma,
          Responsable: item.equipoResponsable,
          Periodo: item.tiempoEstimado,
          Lugar: item.lugar,
          Sesiones: item.numAtencionGrupal,
          Modalidad: item.modalidad.nombre,
          Frecuencia: item.frecuencia.nombre,
        }
        registrosTablaMedidasAccesorias.push(elemento);
      }
    }

    let tablaMedidasAccesorias = new TablaPlantilla();
    tablaMedidasAccesorias.encabezados = ['Medida accesoria', 'Objetivo', 'Actividades', 'Responsable', 'Periodo', 'Lugar', 'Sesiones', 'Modalidad', 'Frecuencia'];
    tablaMedidasAccesorias.filas = registrosTablaMedidasAccesorias;


    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(plan.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          console.log(fichaIdentificacion);
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';
          const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''}, ${this.formatFecha(fichaIdentificacion.fechaNacimiento)}`;
          const direccion = fichaIdentificacion.direccion || 'N/A';

          //Obtener grado de instrucción desde catálogos
          this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
            next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
              console.log(respuestaCatalogo);
              const catalogoModalidadEstudio = respuestaCatalogo.data;
              const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

              // 6. Obtener último detalle de expediente matriz
              this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                  const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                  const juzgadoProcedencia = detalleExpediente?.organoJurisdiccional || '';
                  const numExpediente = detalleExpediente?.numExpediente || '';
                  const infraccion = detalleExpediente?.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                  const fechaSentencia = detalleExpediente?.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaCreacion) : '';
                  const tipoMedida = detalleExpediente?.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                  const duracionMedida = `${detalleExpediente?.tiempoMedSocEduDias ? detalleExpediente?.tiempoMedSocEduDias : 0} días, ${detalleExpediente?.tiempoMedSocEduMeses ? detalleExpediente?.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente?.tiempoMedSocEduAnios ? detalleExpediente?.tiempoMedSocEduAnios : 0} años.`;
                  const inicioMedida = detalleExpediente?.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaInicioMedida) : '';
                  const finMedida = detalleExpediente?.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaFinMedida) : '';
                  const numExpJudicial = detalleExpediente?.numExpedienteJudicial || '';
                  let request = new GeneracionPdfRequest();
                  request.nemonico = etiquetasModel.FORMULARIO_PTI_ABIERTO_LIBERTAD;
                  request.variables = {
                    "[IMG_BASE64]": this.base64Image,
                    "[TITULO-PLANTILLA]": 'Plan de Tratamiento Individual',
                    "[TITULO-INFORME]": 'Plan de Tratamiento Individual - Medio Abierto - Libertad Restringida/Libertad Asistida',
                    "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
                    "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
                    "[CENTRO]": fichaIdentificacion.centroIngreso,
                    "[ADOLESCENTE]": nombreAdolescente,
                    "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
                    "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                    "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
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
                    "[TECNICAS-RECOJO-INFORMACION]": plan.instTecnicas,
                    "[TABLA-EVALUACION]": JSON.stringify(tablaEvaluacion),
                    "[TABLA-MATRIZ-PTI]": JSON.stringify(tablaMatrizPti),
                    "[TABLA-CONTROL-ASISTENCIA]": JSON.stringify(tablaControlActividades),
                    "[TABLA-MATRIZ-CUMPLIMIENTO-MEDIDAS]": JSON.stringify(tablaMedidasAccesorias),
                    "[NUM-EXPJUDICIAL]": numExpJudicial,

                  }
                  this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
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
                  this.expedienteMatrizService.checkError(error);
                }
              });
            },
            error: (error: any) => {
              this.catalogoService.checkError(error);
            }
          });

        },
        error: (error: any) => {
          this.fichaIdentificacionService.checkError(error);
        }
      }
    );
  }

  generarPDFMedioAbiertoComunidad(plan: PlanTratamientoIndDTO) {
    // TABLA EVALUACIÓN
    let registrosTablaEvaluacion: any[] = [];
    for (let item of plan.especFactores) {
      let elemento = {
        Nombre: item.dimension.nombre,
        Detalle: item.comentario
      }
      registrosTablaEvaluacion.push(elemento);
    }

    let tablaEvaluacion = new TablaPlantilla();
    tablaEvaluacion.encabezados = ['Nombre', 'Detalle'];
    tablaEvaluacion.filas = registrosTablaEvaluacion;

    // TABLA PSC
    let registrosTablaComunidad: any[] = [];
    for (let item of plan.ejecMedidas) {
      let elemento = {
        Nombre: item.dimension.nombre,
        Detalle: item.comentario
      }
      registrosTablaComunidad.push(elemento);
    }

    let tablaComunidad = new TablaPlantilla();
    tablaComunidad.encabezados = ['Nombre', 'Detalle'];
    tablaComunidad.filas = registrosTablaComunidad;


    // TABLA UNIDAD RECEPTORA
    let registrosTablaUnidad: any[] = [];
    for (let item of plan.unidadReceptora) {
      let elemento = {
        Nombre: item.dimension.nombre,
        Detalle: item.comentario
      }
      registrosTablaUnidad.push(elemento);
    }

    let tablaUnidadReceptora = new TablaPlantilla();
    tablaUnidadReceptora.encabezados = ['Nombre', 'Detalle'];
    tablaUnidadReceptora.filas = registrosTablaUnidad;

    // TABLA FASE PREPARACIÓN
    let registrosTablaFasePreparacion: any[] = [];
    for (let item of plan.intervNoCriminogenos) {
      if (item.activo) {
        let elemento = {
          Componentes: item.dimension.nombre,
          Objetivo: item.objetivo,
          Actividad: item.actividadPrograma,
          No_sesiones: item.numAtencionGrupal,
          Horario: item.tiempoEstimado,

        }
        registrosTablaFasePreparacion.push(elemento);
      }
    }

    let tablaFasePreparacion = new TablaPlantilla();
    tablaFasePreparacion.encabezados = ['Componentes', 'Objetivo', 'Actividad', 'No_sesiones', 'Horario'];
    tablaFasePreparacion.filas = registrosTablaFasePreparacion;

    // TABLA FASE EJECUCIÓN
    let registrosTablaFaseEjecucion: any[] = [];
    for (let item of plan.intervMedidas) {
      if (item.activo) {
        let elemento = {
          Componentes: item.dimension.nombre,
          Objetivo: item.objetivo,
          Actividad: item.actividadPrograma,
          No_sesiones: item.numAtencionGrupal,
          Horario: item.tiempoEstimado,

        }
        registrosTablaFaseEjecucion.push(elemento);
      }
    }

    let tablaFaseEjecucion = new TablaPlantilla();
    tablaFaseEjecucion.encabezados = ['Componentes', 'Objetivo', 'Actividad', 'No_sesiones', 'Horario'];
    tablaFaseEjecucion.filas = registrosTablaFaseEjecucion;

    // TABLA MATRIZ PTI
    let registrosTablaMatrizPti: any[] = [];
    for (let item of plan.intervObjetivos) {
      if (item.activo) {
        let elemento = {
          Componentes: item.dimension.nombre,
          Objetivo: item.objetivo,
          Actividades: item.actividadPrograma,
          Periodo: item.tiempoEstimado,
          // Modalidad: item.modalidad.nombre,
          // Frecuencia: item.frecuencia.nombre,
          Responsable: item.equipoResponsable,
        }
        registrosTablaMatrizPti.push(elemento);
      }
    }

    let tablaMatrizPti = new TablaPlantilla();
    //tablaMatrizPti.encabezados = ['Componentes', 'Objetivo', 'Actividades', 'Periodo', 'Modalidad', 'Frecuencia', 'Responsable'];
    tablaMatrizPti.encabezados = ['Componentes', 'Objetivo', 'Actividades', 'Periodo', 'Responsable'];
    tablaMatrizPti.filas = registrosTablaMatrizPti;

    // TABLA CUMPLIMIENTO MEDIDAS ACCESORIAS
    let registrosTablaMedidasAccesorias: any[] = [];
    for (let item of plan.intervDiferenciada) {
      if (item.activo) {
        let elemento = {
          Medida_accesoria: item.dimension?.nombre || '',
          Objetivo: item.objetivo,
          Actividades: item.actividadPrograma,
          Responsable: item.equipoResponsable,
          Periodo: item.tiempoEstimado,
          Lugar: item.lugar,
          Sesiones: item.numAtencionGrupal,
          Modalidad: item.modalidad.nombre,
          Frecuencia: item.frecuencia.nombre,
        }
        registrosTablaMedidasAccesorias.push(elemento);
      }
    }

    let tablaMedidasAccesorias = new TablaPlantilla();
    tablaMedidasAccesorias.encabezados = ['Medida accesoria', 'Objetivo', 'Actividades', 'Responsable', 'Periodo', 'Lugar', 'Sesiones', 'Modalidad', 'Frecuencia'];
    tablaMedidasAccesorias.filas = registrosTablaMedidasAccesorias;


    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(plan.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          console.log(fichaIdentificacion);
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';
          const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''}, ${this.formatFecha(fichaIdentificacion.fechaNacimiento)}`;
          const direccion = fichaIdentificacion.direccion || 'N/A';

          //Obtener grado de instrucción desde catálogos
          this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
            next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
              console.log(respuestaCatalogo);
              const catalogoModalidadEstudio = respuestaCatalogo.data;
              const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

              // 6. Obtener último detalle de expediente matriz
              this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                  const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                  const juzgadoProcedencia = detalleExpediente?.organoJurisdiccional || '';
                  const numExpediente = detalleExpediente?.numExpediente || '';
                  const infraccion = detalleExpediente?.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                  const fechaSentencia = detalleExpediente?.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaCreacion) : '';
                  const tipoMedida = detalleExpediente?.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                  const duracionMedida = `${detalleExpediente?.tiempoMedSocEduDias ? detalleExpediente?.tiempoMedSocEduDias : 0} días, ${detalleExpediente?.tiempoMedSocEduMeses ? detalleExpediente?.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente?.tiempoMedSocEduAnios ? detalleExpediente?.tiempoMedSocEduAnios : 0} años.`;
                  const inicioMedida = detalleExpediente?.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaInicioMedida) : '';
                  const finMedida = detalleExpediente?.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaFinMedida) : '';
                  const numExpJudicial = detalleExpediente?.numExpedienteJudicial || '';
                  let request = new GeneracionPdfRequest();
                  request.nemonico = etiquetasModel.FORMULARIO_PTI_ABIERTO_COMUNIDAD;
                  request.variables = {
                    "[IMG_BASE64]": this.base64Image,
                    "[TITULO-PLANTILLA]": 'Plan de Tratamiento Individual',
                    "[TITULO-INFORME]": 'Plan de Tratamiento Individual - Medio Abierto - Prestación de Servicios a la Comunidad',
                    "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
                    "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
                    "[CENTRO]": fichaIdentificacion.centroIngreso,
                    "[ADOLESCENTE]": nombreAdolescente,
                    "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
                    "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                    "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
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
                    "[TECNICAS-RECOJO-INFORMACION]": plan.instTecnicas,
                    "[TABLA-EVALUACION]": JSON.stringify(tablaEvaluacion),
                    "[TABLA-PSC]": JSON.stringify(tablaComunidad),
                    "[TABLA-UNIDAD]": JSON.stringify(tablaUnidadReceptora),
                    "[TABLA-FASE-PREPARACION]": JSON.stringify(tablaFasePreparacion),
                    "[TABLA-FASE-EJECUCION]": JSON.stringify(tablaFaseEjecucion),
                    "[TABLA-MATRIZ-PTI]": JSON.stringify(tablaMatrizPti),
                    "[TABLA-MATRIZ-CUMPLIMIENTO-MEDIDAS]": JSON.stringify(tablaMedidasAccesorias),
                    "[NUM-EXPJUDICIAL]": numExpJudicial,
                  }
                  this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
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
                  this.expedienteMatrizService.checkError(error);
                }
              });
            },
            error: (error: any) => {
              this.catalogoService.checkError(error);
            }
          });

        },
        error: (error: any) => {
          this.fichaIdentificacionService.checkError(error);
        }
      }
    );
  }

  generarPDFMedioAbiertoAmonestacion(plan: PlanTratamientoIndDTO) {
    // TABLA EVALUACIÓN
    let registrosTablaEvaluacion: any[] = [];
    for (let item of plan.especFactores) {
      let elemento = {
        Nombre: item.dimension.nombre,
        Detalle: item.comentario
      }
      registrosTablaEvaluacion.push(elemento);
    }

    let tablaEvaluacion = new TablaPlantilla();
    tablaEvaluacion.encabezados = ['Nombre', 'Detalle'];
    tablaEvaluacion.filas = registrosTablaEvaluacion;

    // TABLA MATRIZ PTI
    let registrosTablaMatrizPti: any[] = [];
    for (let item of plan.intervObjetivos) {
      if (item.activo) {
        let elemento = {
          Componentes: item.dimension.nombre,
          Objetivo: item.objetivo,
          Actividades: item.actividadPrograma,
          Periodo: item.tiempoEstimado,
          // Modalidad: item.modalidad?.nombre,
          // Frecuencia: item.frecuencia?.nombre,
          Responsable: item.equipoResponsable,
        }
        registrosTablaMatrizPti.push(elemento);
      }
    }

    let tablaMatrizPti = new TablaPlantilla();
    // tablaMatrizPti.encabezados = ['Componentes', 'Objetivo', 'Actividades', 'Periodo', 'Modalidad', 'Frecuencia', 'Responsable'];
    tablaMatrizPti.encabezados = ['Componentes', 'Objetivo', 'Actividades', 'Periodo', 'Responsable'];
    tablaMatrizPti.filas = registrosTablaMatrizPti;

    // TABLA CONTROL ACTIVIDADES
    let registrosTablaControlActividades: any[] = [];
    for (let item of plan.intervNoCriminogenos) {
      let elemento = {
        Atencion: item.dimension.nombre,
        Horario: item.tiempoEstimado,
        Lugar: item.lugar,
      }
      registrosTablaControlActividades.push(elemento);
    }

    let tablaControlActividades = new TablaPlantilla();
    tablaControlActividades.encabezados = ['Atención', 'Horario', 'Lugar'];
    tablaControlActividades.filas = registrosTablaControlActividades;

    // TABLA CUMPLIMIENTO MEDIDAS ACCESORIAS
    let registrosTablaMedidasAccesorias: any[] = [];
    for (let item of plan.intervDiferenciada) {
      if (item.activo) {
        let elemento = {
          Medida_accesoria: item.dimension?.nombre || '',
          Objetivo: item.objetivo,
          Actividades: item.actividadPrograma,
          Responsable: item.equipoResponsable,
          Periodo: item.tiempoEstimado,
          Lugar: item.lugar,
          Sesiones: item.numAtencionGrupal,
          Modalidad: item.modalidad.nombre,
          Frecuencia: item.frecuencia.nombre,
        }
        registrosTablaMedidasAccesorias.push(elemento);
      }
    }

    let tablaMedidasAccesorias = new TablaPlantilla();
    tablaMedidasAccesorias.encabezados = ['Medida accesoria', 'Objetivo', 'Actividades', 'Responsable', 'Periodo', 'Lugar', 'Sesiones', 'Modalidad', 'Frecuencia'];
    tablaMedidasAccesorias.filas = registrosTablaMedidasAccesorias;


    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(plan.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          console.log(fichaIdentificacion);
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';
          const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''}, ${this.formatFecha(fichaIdentificacion.fechaNacimiento)}`;
          const direccion = fichaIdentificacion.direccion || 'N/A';

          //Obtener grado de instrucción desde catálogos
          this.catalogoService.obtenerCatalogoPorNemonico(fichaIdentificacion.modalidadEstudio, this.nemonicoMenu).subscribe({
            next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
              console.log(respuestaCatalogo);
              const catalogoModalidadEstudio = respuestaCatalogo.data;
              const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

              // 6. Obtener último detalle de expediente matriz
              this.expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(this.nemonicoMenu, this.uuid_fp).subscribe({
                next: (respuestaDetalleExpediente: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
                  const detalleExpediente: ExpedienteMatrizDetalleDTO = respuestaDetalleExpediente.data;
                  const juzgadoProcedencia = detalleExpediente?.organoJurisdiccional || '';
                  const numExpediente = detalleExpediente?.numExpediente || '';
                  const infraccion = detalleExpediente?.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                  const fechaSentencia = detalleExpediente?.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaCreacion) : '';
                  const tipoMedida = detalleExpediente?.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                  const duracionMedida = `${detalleExpediente?.tiempoMedSocEduDias ? detalleExpediente?.tiempoMedSocEduDias : 0} días, ${detalleExpediente?.tiempoMedSocEduMeses ? detalleExpediente?.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente?.tiempoMedSocEduAnios ? detalleExpediente?.tiempoMedSocEduAnios : 0} años.`;
                  const inicioMedida = detalleExpediente?.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaInicioMedida) : '';
                  const finMedida = detalleExpediente?.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaFinMedida) : '';
                  const numExpJudicial = detalleExpediente?.numExpedienteJudicial || '';
                  let request = new GeneracionPdfRequest();
                  request.nemonico = etiquetasModel.FORMULARIO_PTI_ABIERTO_AMONESTACION;
                  request.variables = {
                    "[IMG_BASE64]": this.base64Image,
                    "[TITULO-PLANTILLA]": 'Plan de Tratamiento Individual',
                    "[TITULO-INFORME]": 'Plan de Tratamiento Individual - Medio Abierto - Amonestación o Semilibertad',
                    "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
                    "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
                    "[CENTRO]": fichaIdentificacion.centroIngreso,
                    "[ADOLESCENTE]": nombreAdolescente,
                    "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
                    "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                    "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
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
                    "[TECNICAS-RECOJO-INFORMACION]": plan.instTecnicas,
                    "[TABLA-EVALUACION]": JSON.stringify(tablaEvaluacion),
                    "[TABLA-MATRIZ-PTI]": JSON.stringify(tablaMatrizPti),
                    "[TABLA-MATRIZ-CUMPLIMIENTO-MEDIDAS]": JSON.stringify(tablaMedidasAccesorias),
                    "[NUM-EXPJUDICIAL]": numExpJudicial,
                  }
                  this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
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
                  this.expedienteMatrizService.checkError(error);
                }
              });
            },
            error: (error: any) => {
              this.catalogoService.checkError(error);
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
