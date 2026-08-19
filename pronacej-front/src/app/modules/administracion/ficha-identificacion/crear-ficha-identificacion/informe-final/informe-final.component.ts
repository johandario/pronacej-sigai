import { HttpClient } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { ExpedienteMatrizDetalleDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { InformeFinalAbiertoDTO } from 'app/core/model/both/informeFinalAbiertoDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { InformeFinalAbiertoService } from 'app/modules/seguridad/services/informeFinalAbierto.service';
import { catchError, tap, throwError } from 'rxjs';

@Component({
  selector: 'app-informe-final',
  standalone: true,
  imports: [
    MatTabsModule,
    TablaListaComponent,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './informe-final.component.html',
  styleUrl: './informe-final.component.scss'
})
export class InformeFinalComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME_FINAL;
  nemonicoEncuesta: string = '';

  mostrarInformeFinalCJDR: boolean = false;
  mostrarInformeFinalSOA: boolean = false;

  uuid_fp: string;

  listaEvaluaciones: EncabezadoDTO[] = [];
  tokenEncuesta: string;

  listaInformes: InformeFinalAbiertoDTO[] = [];

  paginacion: Paginacion = new Paginacion();

  paginacionInformes: Paginacion = new Paginacion();

  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  base64Image: string | null = null;
  evaluacion: any

  @ViewChild('tablaCJDR') tablaCJDRComponent: TablaListaComponent<any>;
  @ViewChild('tablaSOA') tablaSOAComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    nombre: "Nombre",
    descripcion: "Descripción"
  };

  keyLabelsTableInformes: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    fechaFinalizacion: "Fecha de finalización",
    conclusionesRecomendaciones: "Conclusiones/Recomendaciones",
  };

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private encuestaService: EncuestaService,
    private fichaIngresoService: FichaIngresoService,
    private catalogoService: CatalogoService,
    private expedienteMatrizService: ExpedienteMatrizService,
    private informeFinalAbiertoService: InformeFinalAbiertoService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private http: HttpClient,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) {

  }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_INFORME_FINAL"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];



    this.obtenerFichaIngresoValida(this.uuid_fp).subscribe(response => {
      this.cargarTokenEncuesta();
      this.obtenerEvaluaciones();
      this.obtenerInformes();
    });
  }

  obtenerEvaluaciones() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    this.paginacionRequest.filter = this.paginacionRequest.filter

    this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(this.paginacionRequest, this.nemonicoEncuesta, etiquetasModel.NEMONICO_MENU_ENCUESTA).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaEvaluaciones = response.data.data;
          console.log(this.listaEvaluaciones);

          this.paginacion.totalItems = response.data.totalItems;

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  descargarExcelCompletoCJDR() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(this.paginacionRequest, this.nemonicoEncuesta, etiquetasModel.NEMONICO_MENU_ENCUESTA).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaCJDRComponent.exportXLSX(response.data.data);

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  cargarTokenEncuesta() {
    let encuestaDTO = new EncuestaDTO();

    this.encuestaService.obtenerEncuestas(encuestaDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO[]>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }
          let encuestaInformeFinal = response.data.find(x => x.nemonico == this.nemonicoEncuesta);

          this.tokenEncuesta = encuestaInformeFinal.tokenIdentificador;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  obtenerFichaIngresoValida(tokenFichaIdentificacion: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;

    return this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, this.nemonicoMenu).pipe(
      tap((response) => {
        let ingreso = response.data;
        if (ingreso && ingreso?.centro.nombre.includes('CJDR')) {
          this.mostrarInformeFinalCJDR = true;
          this.nemonicoEncuesta = etiquetasModel.ENCUESTA_INFORME_FINAL_CJDR;
        } else if (ingreso && ingreso?.centro.nombre.includes('SOA')) {
          this.mostrarInformeFinalSOA = true;
          this.nemonicoEncuesta = etiquetasModel.ENCUESTA_INFORME_FINAL_SOA;
        }
      }),
      catchError(err => {
        this.fichaIngresoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  verEvaluacion(encabezadoDTO: EncabezadoDTO) {
    console.log(encabezadoDTO);

    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        uuid_fp: this.uuid_fp,
        completada: encabezadoDTO.completada,
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

  generarPDF(encabezadoDTO: EncabezadoDTO) {
    this.loadImageAsBase64();
    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_INFORME_FINAL;

    this.encuestaService.obtenerEncuestaPorTokenEncuesta(encabezadoDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'La evaluación no se encuentra configurada. Por favor contacte a su administrador.'
            ).afterClosed().subscribe();
            return;
          }
          this.evaluacion = response.data;
          let preguntasRespuestas: any[] = [];
          let numero = 1;

          for (let seccion of this.evaluacion.secciones) {
            for (let pregunta of seccion.preguntas) {
              let respuestaSeleccionada = pregunta.tieneObservaciones ? "Sí" : "No";

              let fila = {
                No: (numero++).toString(),
                Pregunta: pregunta.texto + (pregunta.contestaciones.length > 0 ? ` ${pregunta.contestaciones.map(c => c.contestacion).join(', ')}` : ''),
                Respuesta: respuestaSeleccionada
              };
              preguntasRespuestas.push(fila);
            }
          }

          this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, etiquetasModel.NEMONICO_MENU_INFORME_FINAL).subscribe({
            next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
              if (!response.exito) {
                return;
              }

              const fichaIdentificacion: FichaIdentificacionDTO = response.data;
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
                      const juzgadoProcedencia = detalleExpediente?.organoJurisdiccional || '';
                      const numExpediente = detalleExpediente?.numExpediente || '';
                      const infraccion = detalleExpediente?.expedienteDelitos?.[0]?.delitoGenerico?.nombre || '';
                      const fechaSentencia = detalleExpediente?.fechaCreacion ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaCreacion) : '';
                      const tipoMedida = detalleExpediente?.medidasSocioeducativas?.[0]?.medida?.nombre || '';
                      const duracionMedida = `${detalleExpediente?.tiempoMedSocEduDias ? detalleExpediente?.tiempoMedSocEduDias : 0} días, ${detalleExpediente?.tiempoMedSocEduMeses ? detalleExpediente?.tiempoMedSocEduMeses : 0} meses, ${detalleExpediente?.tiempoMedSocEduAnios ? detalleExpediente?.tiempoMedSocEduAnios : 0} años.`;
                      const inicioMedida = detalleExpediente?.fechaInicioMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaInicioMedida) : '';
                      const finMedida = detalleExpediente?.fechaFinMedida ? this.funcionesUtils.formatearFecha(detalleExpediente?.fechaFinMedida) : '';
                      const numExpJudicial = detalleExpediente?.numExpedienteJudicial || '';
                      let tablaEvaluacion = new TablaPlantilla();
                      tablaEvaluacion.encabezados = ['No', 'Pregunta', 'Respuesta'];
                      tablaEvaluacion.filas = preguntasRespuestas;

                      request.variables = {
                        "[IMG_BASE64]": this.base64Image,
                        "[TITULO-PLANTILLA]": "Informe final",
                        "[TITULO-INFORME]": this.evaluacion.nombre,
                        "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
                        "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
                        "[CENTRO]": 'CJDR',
                        "[NOMBRE]": encabezadoDTO.nombre,
                        "[DESCRIPCION]": encabezadoDTO.descripcion,
                        "[ADOLESCENTE]": nombreAdolescente,
                        "[DNI]": numDocumento,
                        "[GRADO_INSTRUCCION]": gradoInstruccion,
                        "[JUZGADO_PROCEDENCIA]": juzgadoProcedencia,
                        "[NUM_EXPEDIENTE]": numExpediente,
                        "[INFRACCION]": infraccion,
                        "[FECHA_SENTENCIA]": fechaSentencia,
                        "[TIPO_MEDIDA]": tipoMedida,
                        "[DURACION_MEDIDA]": duracionMedida,
                        "[FECHA_INICIO_MEDIDA]": inicioMedida,
                        "[FECHA_FINALIZACION]": finMedida,
                        "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                        "[LUGAR_FECHA_NACIMIENTO]": lugarFechaNacimiento,
                        "[EDAD_ACTUAL]": edadActual,
                        "[DIRECCION]": direccion,
                        "[TABLA-PREGUNTAS]": JSON.stringify(tablaEvaluacion),
                        "[NUM-EXPJUDICIAL]": numExpJudicial,
                      };

                      this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
                        next: (response: RespuestaPorDefecto<string>) => {
                          if (!response.exito) {
                            this.dialogMensajeService.mensajeError(
                              'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                            );
                            return;
                          }

                          console.log(response);
                          const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
                          window.open(url);
                        },
                        error: (error: any) => {
                          this.dialogMensajeService.mensajeError(
                            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                          );
                        }
                      });


                    },
                    error: (error: any) => {
                      console.error('Error al obtener el detalle del último expediente:', error);
                      this.dialogMensajeService.mensajeError('Error al obtener el detalle del último expediente');
                    }
                  });
                },
                error: (error: any) => {
                  console.error('Error al obtener el catálogo:', error);
                  this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
                }
              });
            },
            error: (error: any) => {
              console.error('Error al obtener la ficha de identificación:', error);
              this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
            }
          });
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  async generarPdfSoa(informe: InformeFinalAbiertoDTO) {
    const logo = await this.funcionesUtils.obtenerLogoPdf();
    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_INFORME_FINAL_SOA;

    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(informe.idFichaIdentificacion, etiquetasModel.NEMONICO_MENU_INFORME_FINAL).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
        if (!response.exito) {
          return;
        }

        const fichaIdentificacion: FichaIdentificacionDTO = response.data;
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
                let tablaMedidas = new TablaPlantilla();
                tablaMedidas.encabezados = ['Medidas accesorias', 'Acción', 'Objetivo', 'Análisis cualitativo'];

                tablaMedidas.filas = informe.medidasList.map(medida => {
                  return {
                    'MedidasAccesorias': medida.medidaAccesoria,
                    'Acción': medida.accion,
                    'Objetivo': medida.objetivo,
                    'AnálisisCualitativo': medida.analisisCualitativo,
                  };
                });

                request.variables = {
                  "[TITULO-PLANTILLA]": "Informe final",
                  "[IMG_BASE64]": logo,
                  "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
                  "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
                  "[CENTRO]": fichaIdentificacion.centroIngreso,
                  "[ADOLESCENTE]": nombreAdolescente,
                  "[DNI]": numDocumento,
                  "[GRADO_INSTRUCCION]": gradoInstruccion,
                  "[JUZGADO_PROCEDENCIA]": juzgadoProcedencia,
                  "[NUM_EXPEDIENTE]": numExpediente,
                  "[INFRACCION]": infraccion,
                  "[FECHA_SENTENCIA]": fechaSentencia,
                  "[TIPO_MEDIDA]": tipoMedida,
                  "[DURACION_MEDIDA]": duracionMedida,
                  "[FECHA_INICIO_MEDIDA]": inicioMedida,
                  "[FECHA_FINALIZACION]": finMedida,
                  "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
                  "[LUGAR_FECHA_NACIMIENTO]": lugarFechaNacimiento,
                  "[EDAD_ACTUAL]": edadActual,
                  "[DIRECCION]": direccion,
                  "[FORTALECIMIENTO_DERECHOS]": informe.fortalecimientoDerechos,
                  "[AREA]": informe.area,
                  "[FORTALECIMIENTO_FAMILIAR]": informe.fortalecimientoFamiliar,
                  "[INTERVENCION]": informe.intervencion,
                  "[ENFOQUE]": informe.enfoque,
                  "[CULTURAL]": informe.cultural,
                  "[RESPONSABILIDAD]": informe.responsabilidad,
                  "[CONCIENCIA]": informe.conciencia,
                  "[TABLA_MEDIDAS]": JSON.stringify(tablaMedidas),
                  "[VALORACION_RIESGO]": informe.valoracionRiesgo,
                  "[CONCLUSIONES_RECOMENDACIONES]": informe.conclusionesRecomendaciones,
                  "[NUM-EXPJUDICIAL]": numExpJudicial,
                };

                this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
                  next: (response: RespuestaPorDefecto<string>) => {
                    if (!response.exito) {
                      this.dialogMensajeService.mensajeError(
                        'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                      );
                      return;
                    }

                    console.log(response);
                    const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
                    window.open(url);
                  },
                  error: (error: any) => {
                    this.dialogMensajeService.mensajeError(
                      'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                    );
                  }
                });
              },
              error: (error: any) => {
                console.error('Error al obtener el detalle del último expediente:', error);
                this.dialogMensajeService.mensajeError('Error al obtener el detalle del último expediente');
              }
            });
          },
          error: (error: any) => {
            this.fichaIdentificacionService.checkError(error);
          }
        });
      },
      error: (error: any) => {
        console.error('Error al obtener catálogo:', error);
        this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
      }
    });
  }

  agregarEvaluacion() {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncuesta: this.tokenEncuesta,
        uuid_fp: this.uuid_fp
      }
    });
  }

  eliminarEvaluacion(encabezadoDTO: EncabezadoDTO) {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el informe? Esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la evaluación..");
            this.encuestaService.eliminarEvaluacion(encabezadoDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar la evaluación. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerEvaluaciones();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al guardar la evaluación. Inténtalo de nuevo.'
                  );
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

    this.obtenerEvaluaciones();
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

    this.obtenerEvaluaciones();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerEvaluaciones();
  }

  subirDocumento(encabezadoDTO: EncabezadoDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(EvaluacionDocumentoComponent, {
      width: '1200px',
      height: '700px',
      data: { encabezado: encabezadoDTO, nemonicoMenu: this.nemonicoMenu, nemonicoCarpeta: etiquetasModel.CARPETA_INFORME_FINAL, seccion: etiquetasModel.SECCION_FICHA_IDENT_INFORME_FINAL }
    });
  }

  verDocumentos(encabezadoDTO: EncabezadoDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(PopupDocumentosComponent, {
      width: '1000px',
      height: '500px',
      data: {
        tokenItem: encabezadoDTO.tokenIdentificador,
        tipoServicio: "EVALUACIONES",
        nemonicoMenu: this.nemonicoMenu
      }
    });
  }

  subirDocumentoSOA(informeDTO: InformeFinalAbiertoDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(SubidaDocumentoGenericoComponent, {
      width: '1200px',
      height: '700px',
      data: {
        item: informeDTO,
        nemonicoMenu: etiquetasModel.NEMONICO_MENU_INFORME_FINAL,
        nemonicoCarpeta: etiquetasModel.CARPETA_INFORME_FINAL,
        tipoServicio: 'informeFinalSOA',
        seccionTipoDocumento: etiquetasModel.SECCION_FICHA_IDENT_INFORME_FINAL
      }
    });
  }

  verDocumentosSOA(informeDTO: InformeFinalAbiertoDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(PopupDocumentosComponent, {
      width: '1000px',
      height: '500px',
      data: {
        tokenItem: informeDTO.tokenIdentificador,
        tipoServicio: "INFORME_FINAL_SOA",
        nemonicoMenu: this.nemonicoMenu
      }
    });
  }

  obtenerInformes() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.informeFinalAbiertoService.obtenerInformes(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<InformeFinalAbiertoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaInformes = response.data.data;
          console.log(this.listaInformes);

          this.paginacionInformes.totalItems = response.data.totalItems;

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  descargarExcelCompletoSOA() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.informeFinalAbiertoService.obtenerInformes(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<InformeFinalAbiertoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaSOAComponent.exportXLSX(response.data.data);

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  verInforme(informeDTO: InformeFinalAbiertoDTO) {
    this.router.navigate(['crear-editar-informe-final-abierto'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        informe: informeDTO,
        uuid_fp: this.uuid_fp,
        // completada: informeDTO.completada,
      }
    });
  }

  agregarInforme() {
    this.router.navigate(['crear-editar-informe-final-abierto'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncuesta: this.tokenEncuesta,
        uuid_fp: this.uuid_fp
      }
    });
  }

  eliminarInforme(informeDTO: InformeFinalAbiertoDTO) {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el informe? Esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el informe..");
            this.informeFinalAbiertoService.eliminarInforme(informeDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar el informe. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerInformes();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al eliminar el informe. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEventInforme(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerInformes();
  }

  handleSortEventInforme(event: Sort) {
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

  handleSearchEventInforme(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerInformes();
  }
}
