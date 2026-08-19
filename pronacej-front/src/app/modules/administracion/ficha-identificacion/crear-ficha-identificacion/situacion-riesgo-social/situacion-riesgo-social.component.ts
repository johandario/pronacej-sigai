import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { SituacionRiesgoSocialDTO } from 'app/core/model/both/situacionRiesgoSocialDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { SituacionRiesgoSocialService } from 'app/modules/seguridad/services/situacionRiesgoSocial.service';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { Sort } from '@angular/material/sort';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-situacion-riesgo-social',
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
  templateUrl: './situacion-riesgo-social.component.html',
  styleUrl: './situacion-riesgo-social.component.scss'
})
export class SituacionRiesgoSocialComponent implements OnInit {
  // Identificadores principales
  identificadorFichaPrincipal: string;
  tituloPantalla: string = "situación de riesgo social";
  centro: JerarquiaDTO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SITUACION_RIESGO_SOCIAL;

  // Gestión de datos y paginación
  listaSituacionesRiesgoSocial: SituacionRiesgoSocialDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  // Configuración de columnas para la tabla
  columnasTabla: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    usuarioRegistro: "Usuario que registró"
  };

  constructor(
    private servicioSituacionRiesgoSocial: SituacionRiesgoSocialService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioMensajes: DialogMensajeService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private servicioPdf: PdfService,
    private servicioJerarquia: JerarquiaService,
    private http: HttpClient,
  ) { }

  ngOnInit(): void {
    // Inicialización del componente
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarCentro();
    this.obtenerListaSituacionesRiesgoSocial();
  }

  // Carga la información del centro de rehabilitación
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
          this.ajustarTituloSegunCentro();
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  ajustarTituloSegunCentro() {
    this.tituloPantalla = 'Situación de riesgo social';
  }

  // Acciones CRUD para Situación de Riesgo Social
  visualizarSituacionRiesgoSocial(situacionRiesgoSocialDTO: SituacionRiesgoSocialDTO) {
    situacionRiesgoSocialDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-situacion-riesgo-social'], {
      state: { situacionRiesgoSocialDTO },
      relativeTo: this.rutaActiva
    });
  }

  editarSituacionRiesgoSocial(situacionRiesgoSocialDTO: SituacionRiesgoSocialDTO) {
    this.enrutador.navigate(['crear-editar-situacion-riesgo-social'], {
      state: { situacionRiesgoSocialDTO },
      relativeTo: this.rutaActiva
    });
  }

  eliminarSituacionRiesgoSocial(situacionRiesgoSocialDTO: SituacionRiesgoSocialDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar la situación de riesgo social? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando la situación de riesgo social...");

          this.servicioSituacionRiesgoSocial.eliminarSituacionRiesgoSocial(situacionRiesgoSocialDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;

              this.obtenerListaSituacionesRiesgoSocial();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioSituacionRiesgoSocial.checkError(error);
            }
          });
        }
      }
    });
  }

  // Navegación para agregar nuevo registro
  agregarSituacionRiesgoSocial() {
    this.enrutador.navigate(['crear-editar-situacion-riesgo-social'], { relativeTo: this.rutaActiva });
  }

  /**
   * Obtiene la lista paginada de situaciones de riesgo social
   * Con manejo mejorado de errores de ordenamiento
   */
  obtenerListaSituacionesRiesgoSocial() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original para usarlo después si es una fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si es un filtro de fecha, enviamos vacío al backend para traer todos los datos
    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = '';
    } else if (this.solicitudPaginacion?.filter) {
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Configurar ordenamiento si existe
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioSituacionRiesgoSocial.obtenerSituacionesRiesgoSocialPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SituacionRiesgoSocialDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          this.listaSituacionesRiesgoSocial = [];
          this.paginacion.totalItems = 0;
          return;
        }

        let datos = respuesta.data.data.map(situacion => ({
          ...situacion,
          usuarioRegistro: situacion.nombreCompletoUsuarioCreacion
        }));

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaCreacion');
          this.paginacion.totalItems = datos.length;
        } else {
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        this.listaSituacionesRiesgoSocial = datos;
      },
      error: (error: any) => {
        console.error('Error al obtener situaciones de riesgo social:', error);
        
        // Limpiar la lista en caso de error
        this.listaSituacionesRiesgoSocial = [];
        this.paginacion.totalItems = 0;
        
        // Manejo de errores más específico
        if (error?.status === 400) {
          if (error?.error?.mensaje) {
            this.servicioMensajes.mensajeError(`Error en la solicitud: ${error.error.mensaje}`);
          } else {
            this.servicioMensajes.mensajeError('Error en la solicitud. Verifique los parámetros de búsqueda u ordenamiento.');
          }
        } else if (error?.status === 500) {
          this.servicioMensajes.mensajeError('Error interno del servidor. Contacte al administrador del sistema.');
        } else if (error?.status === 0) {
          this.servicioMensajes.mensajeError('Error de conexión. Verifique su conexión a internet.');
        } else if (error?.status === 401) {
          this.servicioMensajes.mensajeError('Sesión expirada. Por favor, inicie sesión nuevamente.');
        } else if (error?.status === 403) {
          this.servicioMensajes.mensajeError('No tiene permisos para realizar esta operación.');
        } else {
          const mensajeError = error?.error?.mensaje || error?.message || 'Error desconocido';
          this.servicioMensajes.mensajeError(`Error al obtener situaciones de riesgo social: ${mensajeError}`);
        }
      }
    });
  }

  /**
   * Descarga Excel completo con el mismo ordenamiento y filtros aplicados en la tabla
   */
  descargarExcelCompleto() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000; // Máximo número de registros
    solicitudPaginacion.page = 0; // Primera página
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original para usarlo después si es una fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si es un filtro de fecha, enviamos vacío al backend para traer todos los datos
    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = '';
    } else if (this.solicitudPaginacion?.filter) {
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Configurar ordenamiento (mantener el mismo orden que en la tabla)
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Mostrar diálogo de carga
    const dialogoCarga = this.servicioMensajes.mensajeLoading('Preparando descarga de Excel...');

    this.servicioSituacionRiesgoSocial.obtenerSituacionesRiesgoSocialPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SituacionRiesgoSocialDTO>>) => {
        dialogoCarga.close();

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(
            respuesta.titulo || 'Error al obtener datos',
            respuesta.mensaje || 'No se pudieron obtener los datos para la exportación'
          );
          return;
        }

        let datos = respuesta.data.data.map(situacion => ({
          ...situacion,
          usuarioRegistro: situacion.nombreCompletoUsuarioCreacion
        }));

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaCreacion');
        }

        // Validar que hay datos para exportar
        if (datos.length === 0) {
          this.servicioMensajes.mensajeAdvertencia(
            'Sin datos para exportar',
            'No hay situaciones de riesgo social que cumplan con los criterios de búsqueda actuales.'
          );
          return;
        }

        // Realizar la exportación
        try {
          this.tablaComponent.exportXLSX(datos);
          
          // Mensaje de éxito
          this.servicioMensajes.mensajeExitoso(
            'Exportación exitosa',
            `Se exportaron ${datos.length} situaciones de riesgo social a Excel.`
          );
          
          if (!environment.production) {
            console.log(`Excel generado con ${datos.length} registros`);
          }
          
        } catch (exportError) {
          console.error('Error al generar el archivo Excel:', exportError);
          this.servicioMensajes.mensajeError('Error al generar el archivo Excel. Por favor, intente nuevamente.');
        }
      },
      error: (error: any) => {
        dialogoCarga.close();
        console.error('Error al obtener situaciones de riesgo social para Excel:', error);
        
        // Mensaje de error más específico según el tipo de error
        if (error?.status === 400) {
          if (error?.error?.mensaje) {
            this.servicioMensajes.mensajeError(`Error en la solicitud: ${error.error.mensaje}`);
          } else {
            this.servicioMensajes.mensajeError('Error en la solicitud. Verifique los parámetros de búsqueda u ordenamiento.');
          }
        } else if (error?.status === 500) {
          this.servicioMensajes.mensajeError('Error interno del servidor. Contacte al administrador del sistema.');
        } else if (error?.status === 0) {
          this.servicioMensajes.mensajeError('Error de conexión. Verifique su conexión a internet.');
        } else if (error?.status === 401) {
          this.servicioMensajes.mensajeError('Sesión expirada. Por favor, inicie sesión nuevamente.');
        } else if (error?.status === 403) {
          this.servicioMensajes.mensajeError('No tiene permisos para exportar estos datos.');
        } else if (error?.status === 413) {
          this.servicioMensajes.mensajeError('La cantidad de datos es demasiado grande para exportar. Intente aplicar filtros para reducir los resultados.');
        } else if (error?.status === 504) {
          this.servicioMensajes.mensajeError('La solicitud tardó demasiado tiempo. Intente aplicar filtros para reducir los resultados.');
        } else {
          const mensajeError = error?.error?.mensaje || error?.message || 'Error desconocido';
          this.servicioMensajes.mensajeError(`Error al preparar la descarga de Excel: ${mensajeError}`);
        }
      }
    });
  }

  // Métodos para manejar eventos de paginación, ordenamiento y búsqueda
  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerListaSituacionesRiesgoSocial();
  }

  /**
   * Maneja eventos de ordenamiento de columnas con validación mejorada
   * @param evento Evento con información de ordenamiento
   */
  manejarEventoOrdenamiento(evento: Sort) {
    if (!environment.production) {
      console.log('Evento ordenamiento situación de riesgo social:', evento);
    }
    
    // Inicializar solicitudPaginacion si no existe
    if (!this.solicitudPaginacion) {
      this.solicitudPaginacion = new PaginacionRequest();
    }
    
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
      
      // Log adicional para campo complejo
      if (!environment.production && evento.active === 'usuarioRegistro') {
        console.log(`Ordenando por campo complejo: ${evento.active} ${evento.direction}`);
      }
    } else {
      // Si no hay dirección, limpiar el ordenamiento
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    
    // Reiniciar a la primera página cuando se cambia el ordenamiento
    this.paginacion.pageIndex = 0;
    
    // Llamar al método de obtención con manejo de errores
    this.obtenerListaSituacionesRiesgoSocial();
  }

  /**
   * Alternativa: Manejo de ordenamiento con fallback en caso de error
   */
  manejarEventoOrdenamientoConFallback(evento: Sort) {
    if (!environment.production) {
      console.log('Evento ordenamiento con fallback:', evento);
    }
    
    // Inicializar solicitudPaginacion si no existe
    if (!this.solicitudPaginacion) {
      this.solicitudPaginacion = new PaginacionRequest();
    }
    
    const ordenamientoAnterior = {
      sort: this.solicitudPaginacion.sort,
      direction: this.solicitudPaginacion.direction
    };
    
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    
    // Reiniciar a la primera página cuando se cambia el ordenamiento
    this.paginacion.pageIndex = 0;
    
    // Mostrar loading
    const dialogoCarga = this.servicioMensajes.mensajeLoading('Aplicando ordenamiento...');
    
    this.servicioSituacionRiesgoSocial.obtenerSituacionesRiesgoSocialPaginado(
      this.construirSolicitudPaginacion(),
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta) => {
        dialogoCarga.close();
        this.procesarRespuestaSituaciones(respuesta);
      },
      error: (error) => {
        dialogoCarga.close();
        console.error('Error en ordenamiento:', error);
        
        // Restaurar ordenamiento anterior
        this.solicitudPaginacion.sort = ordenamientoAnterior.sort;
        this.solicitudPaginacion.direction = ordenamientoAnterior.direction;
        
        // Mensaje específico según el campo
        if (evento.active === 'usuarioRegistro') {
          this.servicioMensajes.mensajeAdvertencia(
            'Ordenamiento no disponible',
            'El ordenamiento por usuario que registró no está disponible temporalmente. Se mantuvo el ordenamiento anterior.'
          );
        } else {
          this.servicioMensajes.mensajeError(`Error al ordenar por ${this.columnasTabla[evento.active] || evento.active}.`);
        }
        
        // Recargar con ordenamiento anterior
        this.obtenerListaSituacionesRiesgoSocial();
      }
    });
  }

/**
 * Construye la solicitud de paginación actual
 */
private construirSolicitudPaginacion(): PaginacionRequest {
  const solicitudPaginacion = new PaginacionRequest();
  solicitudPaginacion.size = this.paginacion.pageSize;
  solicitudPaginacion.page = this.paginacion.pageIndex;
  solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

  // Gestión del filtro
  const filtroOriginal = this.solicitudPaginacion?.filter || '';
  const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

  if (esFiltroDeFecha) {
    solicitudPaginacion.filter = '';
  } else if (this.solicitudPaginacion?.filter) {
    solicitudPaginacion.filter = this.solicitudPaginacion.filter;
  }

  // Configuración de ordenamiento
  if (this.solicitudPaginacion?.sort) {
    solicitudPaginacion.sort = this.solicitudPaginacion.sort;
    solicitudPaginacion.direction = this.solicitudPaginacion.direction;
  }

  return solicitudPaginacion;
}

/**
 * Procesa la respuesta de situaciones de manera centralizada
 */
private procesarRespuestaSituaciones(respuesta: RespuestaPorDefecto<PaginacionResponse<SituacionRiesgoSocialDTO>>) {
  if (!respuesta.exito) {
    this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
    this.listaSituacionesRiesgoSocial = [];
    this.paginacion.totalItems = 0;
    return;
  }

  let datos = respuesta.data.data.map(situacion => ({
    ...situacion,
    usuarioRegistro: situacion.nombreCompletoUsuarioCreacion
  }));

  // Aplicar filtrado por fecha si es necesario
  const filtroOriginal = this.solicitudPaginacion?.filter || '';
  const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);
  
  if (esFiltroDeFecha && filtroOriginal) {
    datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaCreacion');
    this.paginacion.totalItems = datos.length;
  } else {
    this.paginacion.totalItems = respuesta.data.totalItems;
  }

  this.listaSituacionesRiesgoSocial = datos;
}

  manejarEventoBusqueda(filtro: string) {
    this.solicitudPaginacion.filter = filtro;
    this.obtenerListaSituacionesRiesgoSocial();
  }

  // Método para imprimir situación de riesgo social (generación de PDF)
  imprimirSituacionRiesgoSocial(situacionRiesgoSocialDTO: SituacionRiesgoSocialDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir la situación de riesgo social?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión de situación de riesgo social...");

          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.identificadorFichaPrincipal, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }

                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SITUACION_RIESGO_SOCIAL';

                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'No especificado'),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto || 'No especificado'),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || 'No especificado'),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento || 'No especificado'),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual || 'No especificado'),
                        "[ANTECEDENTES-DELICTIVOS-FAMILIARES]": this.utilidades.escaparHTML(situacionRiesgoSocialDTO.anteDeliFami || 'No proporcionado'),
                        "[PRIMERA-MANIFESTACION-INFRACCION]": this.utilidades.escaparHTML(situacionRiesgoSocialDTO.primManiInfrAdol || 'No proporcionado'),
                        "[EVASION-HOGAR]": situacionRiesgoSocialDTO.evasionHogar ? 'Sí' : 'No',
                        "[ESTADO-SALUD-GENERAL]": this.utilidades.escaparHTML(situacionRiesgoSocialDTO.estadoSaludGeneral || 'No proporcionado'),
                        "[PROBLEMAS-LEGALES]": this.utilidades.escaparHTML(situacionRiesgoSocialDTO.problemasLegales || 'No proporcionado'),
                        "[OBSERVACIONES]": this.utilidades.escaparHTML(situacionRiesgoSocialDTO.observaciones || 'No proporcionado')
                      };

                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }

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
}