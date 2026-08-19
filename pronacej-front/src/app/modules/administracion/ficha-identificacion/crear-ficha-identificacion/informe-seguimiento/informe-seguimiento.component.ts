import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformeSeguimientoPIIDTO } from 'app/core/model/both/informeSeguimientoPIIDTO.model';
import { environment } from 'environments/environment';
import { InformeSeguimientoPIIService } from 'app/modules/seguridad/services/informeSeguimiento.service';
import { InformeTecnicoSustentatorioDTO } from 'app/core/model/both/informeTecnicoSustentatorioDTO.model';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { Sort } from '@angular/material/sort';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-informe-seguimiento',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    TablaDatosComponent
  ],
  templateUrl: './informe-seguimiento.component.html',
  styleUrl: './informe-seguimiento.component.scss'
})
export class InformeSeguimientoComponent implements OnInit {
  // Identificadores
  identificadorFichaPrincipal: string;
  informeTecnicoDTO: InformeTecnicoSustentatorioDTO;
  tituloPantalla: string = "Informe seguimiento";

  // Datos y paginación
  listaInformesSeguimiento: InformeSeguimientoPIIDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME_SEGUIMIENTO_PII;
  listaNivelesRiesgo: CatalogoDTO[] = [];
  filtroOriginal: string = '';
  esFiltroDeFecha: boolean = false;

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  // CAMBIO: Agregar fecha de creación a las columnas
  columnasTabla: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    motivoIngreso: "Motivo de ingreso",
    nivelRiesgo: "Nivel de riesgo"
  };

  constructor(
    private servicioInformeSeguimiento: InformeSeguimientoPIIService,
    private servicioMensajes: DialogMensajeService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public funcionesUtils: FuncionesUtils,
    private http: HttpClient,
    private servicioPdf: PdfService,
    private servicioFichaIdentificacion: FichaIdentificacionService
  ) { }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.informeTecnicoDTO = history.state.informeTecnicoDTO;
    this.cargarDatosCatalogo();
  }

  /**
   * Carga los catálogos necesarios para el componente
   */
  cargarDatosCatalogo() {
    this.funcionesUtils.obtenerListaCatalogo('NIVEL_RIESGO', this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaNivelesRiesgo = data;
        this.obtenerInformesSeguimiento();
      },
      error: (error) => {
        console.error('Error cargando niveles de riesgo:', error);
        this.obtenerInformesSeguimiento();
      }
    });
  }

  /**
   * Visualiza los detalles de un informe de seguimiento
   * @param informeSeguimientoDTO Informe a visualizar
   */
  visualizarInformeSeguimiento(informeSeguimientoDTO: InformeSeguimientoPIIDTO) {
    informeSeguimientoDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-informe-seguimiento'], {
      state: { informeSeguimientoDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Edita un informe de seguimiento existente
   * @param informeSeguimientoDTO Informe a editar
   */
  editarInformeSeguimiento(informeSeguimientoDTO: InformeSeguimientoPIIDTO) {
    informeSeguimientoDTO.esVisualizacion = false;
    this.enrutador.navigate(['crear-editar-informe-seguimiento'], {
      state: { informeSeguimientoDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Elimina un informe de seguimiento previa confirmación
   * @param informeSeguimientoDTO Informe a eliminar
   */
  eliminarInformeSeguimiento(informeSeguimientoDTO: InformeSeguimientoPIIDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Estás seguro de eliminar el informe? Esta operación es irreversible",
      "¿Deseas continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando el informe...");

          this.servicioInformeSeguimiento.eliminarInformeSeguimiento(
            informeSeguimientoDTO,
            etiquetasModel.NEMONICO_MENU_INFORME_SEGUIMIENTO_PII
          ).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              if (!respuesta.exito) {
                this.servicioInformeSeguimiento.checkError(respuesta);
                return;
              }
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              this.obtenerInformesSeguimiento();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioInformeSeguimiento.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Navega a la pantalla de creación de informe de egreso
   * @param informeSeguimiento Informe de seguimiento base para el egreso
   */
  crearInformeEgreso(informeSeguimiento: InformeSeguimientoPIIDTO) {
    this.enrutador.navigate(['crear-editar-informe-egreso'], {
      relativeTo: this.rutaActiva,
      state: {
        informeSeguimientoDTO: informeSeguimiento
      }
    });
  }

  /**
   * Imprime un informe de seguimiento específico
   * @param informeSeguimientoDTO Informe a imprimir
   */
  imprimirInforme(informeSeguimientoDTO: InformeSeguimientoPIIDTO) {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de seguimiento PII?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión del informe de seguimiento PII...");

          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.identificadorFichaPrincipal, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }

                      // 5. Preparar datos del adolescente
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

                      // 6. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_SEGUIMIENTO_PII';

                      // 7. Obtener el nombre del nivel de riesgo
                      let nombreNivelRiesgo = 'No especificado';

                      // Obtener nombre del nivel de riesgo desde el catálogo
                      if (informeSeguimientoDTO.tokenIdentificadorNivelRiesgo) {
                        nombreNivelRiesgo = this.funcionesUtils.obtenerNombreCatalogoPorToken(
                          informeSeguimientoDTO.tokenIdentificadorNivelRiesgo,
                          this.listaNivelesRiesgo
                        ) || 'No especificado';
                      }

                      // 8. Incluir las variables para el PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.funcionesUtils.escaparHTML(this.funcionesUtils.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.funcionesUtils.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.funcionesUtils.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                        "[NOMBRES-APELLIDOS]": this.funcionesUtils.escaparHTML(nombreCompleto),
                        "[DNI]": this.funcionesUtils.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.funcionesUtils.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.funcionesUtils.escaparHTML(edadActual),
                        "[MOTIVO-INGRESO]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.motivoIngreso || 'No especificado'),
                        "[ANTECEDENTES-ORGANICIDAD]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.antecedentesOrganicidad || 'No especificado'),
                        "[TECNICAS-UTILIZADAS]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.tecnicasUtilizadas || 'No especificado'),
                        "[OBSERVACION-CONDUCTUAL]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.observacionConductual || 'No especificado'),
                        "[EVALUACION-PLAN-PSICOLOGICA]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.evaluacionPlanPsicologica || 'No especificado'),
                        "[EVALUACION-PLAN-SOCIAL]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.evaluacionPlanSocial || 'No especificado'),
                        "[EVALUACION-PLAN-CONDUCTUAL]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.evaluacionPlanConductual || 'No especificado'),
                        "[EVALUACION-PLAN-FAMILIAR]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.evaluacionPlanFamiliar || 'No especificado'),
                        "[EVALUACION-PLAN-EDUCATIVA]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.evaluacionPlanEducativa || 'No especificado'),
                        "[EVALUACION-PLAN-LABORAL]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.evaluacionPlanLaboral || 'No especificado'),
                        "[NIVEL-RIESGO]": this.funcionesUtils.escaparHTML(nombreNivelRiesgo),
                        "[CONCLUSIONES]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.conclusiones || 'No especificado'),
                        "[RECOMENDACIONES]": this.funcionesUtils.escaparHTML(informeSeguimientoDTO.recomendaciones || 'No especificado')
                      };

                      // 9. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }

                          // 10. Abrir el PDF en una nueva pestaña
                          const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
                          window.open(url);
                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al generar PDF:', error);
                          this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                        }
                      });
                    },
                    error: (error: any) => {
                      dialogoCarga.close();
                      console.error('Error al obtener ficha:', error);
                      this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                    }
                  });
              },
              error: (error) => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.servicioMensajes.mensajeError('Error al cargar la imagen del logo');
              }
            });
        }
      }
    });
  }

  /**
   * Obtiene la lista paginada de informes de seguimiento
   * Implementa el filtrado híbrido para textos y fechas
   */
  obtenerInformesSeguimiento() {
    // Crear la solicitud de paginación
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original
    this.filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Verificar si es un filtro de fecha o de nivel de riesgo
    this.esFiltroDeFecha = this.funcionesUtils.esPosibleFiltroFecha(this.filtroOriginal);
    const esFiltroNivelRiesgo = this.esFiltroNivelRiesgo(this.filtroOriginal);

    // Configurar filtro para el backend
    if (this.esFiltroDeFecha || esFiltroNivelRiesgo) {
      // Si es fecha o nivel de riesgo, no enviar filtro al backend
      solicitudPaginacion.filter = '';
    } else {
      // Si no es fecha ni nivel de riesgo, enviar el filtro al backend para búsqueda de texto
      solicitudPaginacion.filter = this.filtroOriginal;
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Mostrar diálogo de carga
    const dialogoCarga = this.servicioMensajes.mensajeLoading("Cargando informes de seguimiento...");

    // Llamar al servicio para obtener los datos
    this.servicioInformeSeguimiento.obtenerInformesSeguimientoPaginado(
      solicitudPaginacion,
      etiquetasModel.NEMONICO_MENU_INFORME_SEGUIMIENTO_PII
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeSeguimientoPIIDTO>>) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        if (!environment.production) {
          console.log('Respuesta de obtenerInformesSeguimientoPaginado:', respuesta);
        }

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // CAMBIO: Transformar los datos usando type assertion y formatear fecha
        let datosTransformados = respuesta.data.data.map((informe: any) => ({
          ...informe,
          nivelRiesgo: this.funcionesUtils.obtenerNombreCatalogoPorToken(
            informe.tokenIdentificadorNivelRiesgo,
            this.listaNivelesRiesgo
          ) || 'No especificado',
          // NUEVO: Formatear la fecha para que se muestre solo DD-MM-YYYY
          fechaCreacion: informe.fechaCreacion ? this.funcionesUtils.formatearFechaSinHora(informe.fechaCreacion) : '',
          // Mantener fecha original para filtrado
          fechaCreacionOriginal: informe.fechaCreacion
        }));

        // Aplicar filtro personalizado en el frontend si es necesario
        if (this.esFiltroDeFecha && this.filtroOriginal) {
          // Filtro por fecha usando la fecha original
          datosTransformados = this.funcionesUtils.filtrarPorFecha(
            datosTransformados,
            this.filtroOriginal,
            'fechaCreacionOriginal'
          );

          // Actualizar paginación con el total de elementos filtrados
          this.paginacion.totalItems = datosTransformados.length;
        } else if (esFiltroNivelRiesgo && this.filtroOriginal) {
          // Filtro por nivel de riesgo
          const filtroLowerCase = this.filtroOriginal.toLowerCase();
          datosTransformados = datosTransformados.filter(item =>
            item.nivelRiesgo && item.nivelRiesgo.toLowerCase().includes(filtroLowerCase)
          );

          // Actualizar paginación con el total de elementos filtrados
          this.paginacion.totalItems = datosTransformados.length;
        } else {
          // Si no hay filtro especial, usar el total del backend
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        // Actualizar la lista de informes
        this.listaInformesSeguimiento = datosTransformados;
      },
      error: (error: any) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        console.error('Error al obtener informes de seguimiento:', error);
        this.servicioMensajes.mensajeError('Error al obtener los informes de seguimiento');
        this.servicioInformeSeguimiento.checkError(error);
      }
    });
  }

  descargarExcelCompleto() {
    // Crear la solicitud de paginación
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original
    this.filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Verificar si es un filtro de fecha o de nivel de riesgo
    this.esFiltroDeFecha = this.funcionesUtils.esPosibleFiltroFecha(this.filtroOriginal);
    const esFiltroNivelRiesgo = this.esFiltroNivelRiesgo(this.filtroOriginal);

    // Configurar filtro para el backend
    if (this.esFiltroDeFecha || esFiltroNivelRiesgo) {
      // Si es fecha o nivel de riesgo, no enviar filtro al backend
      solicitudPaginacion.filter = '';
    } else {
      // Si no es fecha ni nivel de riesgo, enviar el filtro al backend para búsqueda de texto
      solicitudPaginacion.filter = this.filtroOriginal;
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Mostrar diálogo de carga
    const dialogoCarga = this.servicioMensajes.mensajeLoading("Cargando informes de seguimiento...");

    // Llamar al servicio para obtener los datos
    this.servicioInformeSeguimiento.obtenerInformesSeguimientoPaginado(
      solicitudPaginacion,
      etiquetasModel.NEMONICO_MENU_INFORME_SEGUIMIENTO_PII
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeSeguimientoPIIDTO>>) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // CAMBIO: Transformar los datos usando type assertion y formatear fecha
        let datosTransformados = respuesta.data.data.map((informe: any) => ({
          ...informe,
          nivelRiesgo: this.funcionesUtils.obtenerNombreCatalogoPorToken(
            informe.tokenIdentificadorNivelRiesgo,
            this.listaNivelesRiesgo
          ) || 'No especificado',
          // NUEVO: Formatear la fecha para que se muestre solo DD-MM-YYYY
          fechaCreacion: informe.fechaCreacion ? this.funcionesUtils.formatearFechaSinHora(informe.fechaCreacion) : '',
          // Mantener fecha original para filtrado
          fechaCreacionOriginal: informe.fechaCreacion
        }));

        // Aplicar filtro personalizado en el frontend si es necesario
        if (this.esFiltroDeFecha && this.filtroOriginal) {
          // Filtro por fecha usando la fecha original
          datosTransformados = this.funcionesUtils.filtrarPorFecha(
            datosTransformados,
            this.filtroOriginal,
            'fechaCreacionOriginal'
          );
        } else if (esFiltroNivelRiesgo && this.filtroOriginal) {
          // Filtro por nivel de riesgo
          const filtroLowerCase = this.filtroOriginal.toLowerCase();
          datosTransformados = datosTransformados.filter(item =>
            item.nivelRiesgo && item.nivelRiesgo.toLowerCase().includes(filtroLowerCase)
          );
        }

        this.tablaComponent.exportXLSX(datosTransformados);
      },
      error: (error: any) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        console.error('Error al obtener informes de seguimiento:', error);
        this.servicioMensajes.mensajeError('Error al obtener los informes de seguimiento');
        this.servicioInformeSeguimiento.checkError(error);
      }
    });
  }

  /**
   * Determina si un texto es un filtro de nivel de riesgo
   * @param filtro Texto a analizar
   */
  esFiltroNivelRiesgo(filtro: string): boolean {
    if (!filtro) return false;

    const nivelesRiesgo = ['alto', 'medio', 'bajo'];
    return nivelesRiesgo.some(nivel =>
      filtro.toLowerCase().includes(nivel)
    );
  }

  /**
   * Maneja el evento de cambio de página
   * @param eventoPaginacion Evento con la información de paginación
   */
  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerInformesSeguimiento();
  }

  /**
   * Maneja el evento de ordenamiento de columnas
   * @param evento Información de ordenamiento
   */
  manejarEventoOrdenamiento(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerInformesSeguimiento();
  }

  /**
   * Maneja el evento de búsqueda en la tabla
   * @param filtro Texto de búsqueda
   */
  manejarEventoBusqueda(filtro: string) {
    // Guardar el filtro en la solicitud de paginación
    this.solicitudPaginacion.filter = filtro;

    // Verificar qué tipo de filtro es
    this.filtroOriginal = filtro;
    this.esFiltroDeFecha = this.funcionesUtils.esPosibleFiltroFecha(filtro);
    const esFiltroNivelRiesgo = this.esFiltroNivelRiesgo(filtro);

    // Indicar en consola para depuración
    if (this.esFiltroDeFecha) {
      console.log('Aplicando filtro de fecha:', filtro);
    } else if (esFiltroNivelRiesgo) {
      console.log('Aplicando filtro de nivel de riesgo:', filtro);
    } else {
      console.log('Aplicando filtro de texto:', filtro);
    }

    // Resetear a la primera página cuando se busca
    this.paginacion.pageIndex = 0;

    // Llamar al método principal para obtener datos con el filtro aplicado
    this.obtenerInformesSeguimiento();
  }
}