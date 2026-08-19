import { Component, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { InformeTecnicoSustentatorioDTO } from 'app/core/model/both/informeTecnicoSustentatorioDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { InformeTecnicoSustentatorioService } from 'app/modules/seguridad/services/informeTecnicoSustentatorio.service';
import { environment } from 'environments/environment';
import { Sort } from '@angular/material/sort';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';

@Component({
  selector: 'app-informe-tecnico-sustentatorio',
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
  templateUrl: './informe-tecnico-sustentatorio.component.html',
  styleUrl: './informe-tecnico-sustentatorio.component.scss'
})
export class InformeTecnicoSustentatorioComponent {
  // Identificadores
  identificadorFichaPrincipal: string;
  tituloPantalla: string = "informe técnico sustentatorio";

  // Declaración de la variable centro
  centro: JerarquiaDTO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME_TECNICO_SUSTENTATORIO;

  // Datos y paginación
  listaInformesTecnicos: InformeTecnicoSustentatorioDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  // Configuración de columnas
  columnasTabla: any = {
      numero: "No.",
      acciones: "Acciones",
      fechaCreacion: "Fecha de creación",
      motivo: "Motivo",
      duracionMostrar: "Duración",
      criteriosSeleccion: "Criterios de selección"
  };

  constructor(
    private servicioInformeTecnico: InformeTecnicoSustentatorioService,
    private servicioMensajes: DialogMensajeService,
    private servicioJerarquia: JerarquiaService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private servicioPdf: PdfService,
    private http: HttpClient,
  ) { }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarCentro();
    this.obtenerInformesTecnicos();
  }

  /**
   * Carga información del centro al que pertenece el usuario
   */
  cargarCentro() {
    this.servicioJerarquia
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.servicioJerarquia.checkError(respuesta);
            return;
          }
          this.centro = respuesta.data;

          if (!environment.production) {
            console.log('Centro cargado:', this.centro);
          }
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        }
      });
  }

  visualizarInformeTecnico(informeTecnicoDTO: InformeTecnicoSustentatorioDTO) {
    informeTecnicoDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-informe-tecnico-sustentatorio'], {
      state: { informeTecnicoDTO },
      relativeTo: this.rutaActiva
    });
  }

  editarInformeTecnico(informeTecnicoDTO: InformeTecnicoSustentatorioDTO) {
    this.enrutador.navigate(['crear-editar-informe-tecnico-sustentatorio'], {
      state: { informeTecnicoDTO },
      relativeTo: this.rutaActiva
    });
  }

  eliminarInformeTecnico(informeTecnicoDTO: InformeTecnicoSustentatorioDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar el informe técnico? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando el informe técnico...");

          this.servicioInformeTecnico.eliminarInformeTecnico(informeTecnicoDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;
              this.obtenerInformesTecnicos();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioInformeTecnico.checkError(error);
            }
          });
        }
      }
    });
  }

  agregarInformeTecnico() {
    this.enrutador.navigate(['crear-editar-informe-tecnico-sustentatorio'], {
      relativeTo: this.rutaActiva
    });
  }

  crearSeguimiento(informeTecnico: InformeTecnicoSustentatorioDTO) {
    this.enrutador.navigate(['crear-editar-informe-seguimiento'], {
      relativeTo: this.rutaActiva,
      state: {
        informeTecnicoDTO: informeTecnico
      }
    });
  }

  /**
   * Obtiene la lista paginada de informes técnicos con filtrado mejorado para duraciones
   */
  obtenerInformesTecnicos() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original
    const filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Si hay filtro, intentar manejarlo en el frontend para duraciones
    if (filtroOriginal) {
      // No enviar el filtro al backend, traer todos los datos para filtrar localmente
      solicitudPaginacion.filter = '';
    } else {
      solicitudPaginacion.filter = '';
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort && this.solicitudPaginacion?.direction) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioInformeTecnico.obtenerInformesTecnicosPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeTecnicoSustentatorioDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // Mapear los datos para añadir duracionMostrar
        let datos = respuesta.data.data.map((informe: any) => ({
          ...informe,
          duracionMostrar: this.utilidades.convertirDecimalATiempo(informe.duracion),
          fechaCreacion: informe.fechaCreacion ? this.utilidades.formatearFechaSinHora(informe.fechaCreacion) : ''
        }));

        // Si hay filtro, aplicar filtrado manual
        if (filtroOriginal) {
          // Filtrar por todos los campos (texto, duración, fecha)
          const datosFiltrados = datos.filter(item => {
            // 1. Filtrar por duración como número
            const duracionStr = item.duracion?.toString() || '';
            if (duracionStr.includes(filtroOriginal)) {
              return true;
            }

            // 2. Filtrar por duración mostrada (formato hora)
            const duracionMostrar = item.duracionMostrar || '';
            if (duracionMostrar.includes(filtroOriginal)) {
              return true;
            }

            // 3. Intentar filtrar por número redondeado si el filtro es un número
            if (/^\d+$/.test(filtroOriginal)) {
              const numeroFiltro = parseInt(filtroOriginal, 10);
              const duracionRedondeada = Math.floor(item.duracion);
              if (duracionRedondeada === numeroFiltro) {
                return true;
              }
            }

            // 4. Filtrar por otros campos de texto
            for (const key in item) {
              if (typeof item[key] === 'string' &&
                key !== 'duracionMostrar' &&
                item[key].toLowerCase().includes(filtroOriginal.toLowerCase())) {
                return true;
              }
            }

            return false;
          });

          datos = datosFiltrados;
          this.paginacion.totalItems = datosFiltrados.length;
        } else {
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        this.listaInformesTecnicos = datos;
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
        this.servicioMensajes.mensajeError('Error al cargar los informes técnicos');
      }
    });
  }

  descargarExcelCompleto() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original
    const filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Si hay filtro, intentar manejarlo en el frontend para duraciones
    if (filtroOriginal) {
      // No enviar el filtro al backend, traer todos los datos para filtrar localmente
      solicitudPaginacion.filter = '';
    } else {
      solicitudPaginacion.filter = '';
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort && this.solicitudPaginacion?.direction) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioInformeTecnico.obtenerInformesTecnicosPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeTecnicoSustentatorioDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // Mapear los datos para añadir duracionMostrar
        let datos = respuesta.data.data.map(informe => ({
          ...informe,
          duracionMostrar: this.utilidades.convertirDecimalATiempo(informe.duracion)
        }));

        // Si hay filtro, aplicar filtrado manual
        if (filtroOriginal) {
          // Filtrar por todos los campos (texto, duración, fecha)
          const datosFiltrados = datos.filter(item => {
            // 1. Filtrar por duración como número
            const duracionStr = item.duracion?.toString() || '';
            if (duracionStr.includes(filtroOriginal)) {
              return true;
            }

            // 2. Filtrar por duración mostrada (formato hora)
            const duracionMostrar = item.duracionMostrar || '';
            if (duracionMostrar.includes(filtroOriginal)) {
              return true;
            }

            // 3. Intentar filtrar por número redondeado si el filtro es un número
            if (/^\d+$/.test(filtroOriginal)) {
              const numeroFiltro = parseInt(filtroOriginal, 10);
              const duracionRedondeada = Math.floor(item.duracion);
              if (duracionRedondeada === numeroFiltro) {
                return true;
              }
            }

            // 4. Filtrar por otros campos de texto
            for (const key in item) {
              if (typeof item[key] === 'string' &&
                key !== 'duracionMostrar' &&
                item[key].toLowerCase().includes(filtroOriginal.toLowerCase())) {
                return true;
              }
            }

            return false;
          });

          datos = datosFiltrados;
        }

        this.tablaComponent.exportXLSX(datos);
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
        this.servicioMensajes.mensajeError('Error al cargar los informes técnicos');
      }
    });
  }

  /**
   * Genera e imprime el informe técnico sustentatorio en formato PDF
   */
  imprimirInformeTecnico(informeTecnicoDTO: InformeTecnicoSustentatorioDTO) {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe técnico sustentatorio?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión del informe técnico sustentatorio...");

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

                      // 5. Preparar los datos personales del adolescente
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

                      // 6. Crear solicitud para el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_TECNICO_SUSTENTATORIO';

                      // 7. Incluir todas las variables para el PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        // Datos de cabecera
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'Centro no especificado'),

                        // Datos del adolescente
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),

                        // Datos del informe
                        "[MOTIVO]": this.utilidades.escaparHTML(informeTecnicoDTO.motivo || 'No especificado'),
                        "[CRITERIOS-SELECCION]": this.utilidades.escaparHTML(informeTecnicoDTO.criteriosSeleccion || 'No especificado'),
                        "[DURACION]": this.utilidades.escaparHTML(this.utilidades.convertirDecimalATiempo(informeTecnicoDTO.duracion) || 'No especificado'),

                        // Análisis multidisciplinario
                        "[ANALISIS-PSICOLOGICO]": this.utilidades.escaparHTML(informeTecnicoDTO.analisisPsicologico || 'No especificado'),
                        "[ANALISIS-SOCIAL]": this.utilidades.escaparHTML(informeTecnicoDTO.analisisSocial || 'No especificado'),
                        "[ANALISIS-CONDUCTUAL]": this.utilidades.escaparHTML(informeTecnicoDTO.analisisConductual || 'No especificado'),
                        "[ANALISIS-FAMILIAR]": this.utilidades.escaparHTML(informeTecnicoDTO.analisisFamiliar || 'No especificado'),

                        // Propuesta y objetivos
                        "[PROPUESTA-ACTIVIDAD]": this.utilidades.escaparHTML(informeTecnicoDTO.propuestaActividadFormativa || 'No especificado'),
                        "[IMPORTANCIA-PARTICIPACION]": this.utilidades.escaparHTML(informeTecnicoDTO.importanciaParticipacionAdolescente || 'No especificado'),
                        "[OBJETIVOS-CONSEGUIR]": this.utilidades.escaparHTML(informeTecnicoDTO.objetivosConseguir || 'No especificado'),

                        // Conclusiones y recomendaciones
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(informeTecnicoDTO.conclusiones || 'No especificado'),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(informeTecnicoDTO.recomendaciones || 'No especificado')
                      };

                      // 8. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, etiquetasModel.NEMONICO_MENU_INFORME_TECNICO_SUSTENTATORIO)
                        .subscribe({
                          next: (respuesta: RespuestaPorDefecto<string>) => {
                            dialogoCarga.close();
                            if (!respuesta.exito) {
                              console.error('Error al generar PDF:', respuesta);
                              this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                              return;
                            }

                            // 9. Abrir el PDF en una nueva pestaña
                            const url = window.URL.createObjectURL(this.utilidades.getPdfBlob(respuesta.data));
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
                      console.error('Error al obtener ficha de identificación:', error);
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

  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerInformesTecnicos();
  }

  manejarEventoOrdenamiento(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerInformesTecnicos();
  }

  manejarEventoBusqueda(filtro: string) {
    this.solicitudPaginacion.filter = filtro;

    // Al cambiar el filtro, volver a la primera página
    this.paginacion.pageIndex = 0;

    this.obtenerInformesTecnicos();
  }
}