import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { InformeEgresoPIIDTO } from 'app/core/model/both/informeEgresoPIIDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { InformeEgresoPIIService } from 'app/modules/seguridad/services/informeEgresoPII.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { PdfService } from 'app/core/services/pdf.service';
import { environment } from 'environments/environment';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { Sort } from '@angular/material/sort';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-informe-egreso-pii',
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
  templateUrl: './informe-egreso-pii.component.html',
  styleUrl: './informe-egreso-pii.component.scss'
})
export class InformeEgresoPiiComponent implements OnInit {
  // Identificadores
  identificadorFichaPrincipal: string;
  identificadorInformeSeguimiento: string;
  tituloPantalla: string = "informe de egreso PII";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_INFORME_EGRESO_PII;

  // Datos y paginación
  listaInformesEgreso: InformeEgresoPIIDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  // Configuración de columnas para la tabla
  columnasTabla: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    motivoIngresoPII: "Motivo de ingreso",
    conclusiones: "Conclusiones",
    nombreCompletoUsuarioCreacion: "Usuario que registró"
  };

  constructor(
    private servicioInformeEgreso: InformeEgresoPIIService,
    private servicioMensajes: DialogMensajeService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private http: HttpClient,
    private servicioPdf: PdfService
  ) { }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.identificadorInformeSeguimiento = this.rutaActiva.snapshot.params['uuid_is'];
    this.obtenerInformesEgreso();
  }

  /**
   * Visualiza el informe de egreso seleccionado
   * @param informeEgresoDTO Informe a visualizar
   */
  visualizarInformeEgreso(informeEgresoDTO: InformeEgresoPIIDTO) {
    informeEgresoDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-informe-egreso'], {
      state: { informeEgresoDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Navega al componente de edición con el informe seleccionado
   * @param informeEgresoDTO Informe a editar
   */
  editarInformeEgreso(informeEgresoDTO: InformeEgresoPIIDTO) {
    this.enrutador.navigate(['crear-editar-informe-egreso'], {
      state: { informeEgresoDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Navega al componente de creación de un nuevo informe
   */
  agregarInformeEgreso() {
    this.enrutador.navigate(['crear-editar-informe-egreso'], {
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Elimina el informe de egreso seleccionado previa confirmación
   * @param informeEgresoDTO Informe a eliminar
   */
  eliminarInformeEgreso(informeEgresoDTO: InformeEgresoPIIDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar el informe de egreso? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando informe de egreso...");

          this.servicioInformeEgreso.eliminarInformeEgreso(informeEgresoDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;
              this.obtenerInformesEgreso();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioInformeEgreso.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Genera e imprime el informe de egreso PII en formato PDF
   * @param informeEgresoDTO Informe a imprimir
   */
  imprimir(informeEgresoDTO: InformeEgresoPIIDTO) {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de egreso PII?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión del informe de egreso PII...");

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

                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

                      // 5. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_EGRESO_PII';

                      // 6. Incluir las variables para el PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(informeEgresoDTO.fechaCreacion || new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[MOTIVO-INGRESO-PII]": this.utilidades.escaparHTML(informeEgresoDTO.motivoIngresoPII || ''),
                        "[DESCRIPCION-PSICOLOGICA]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionPsicologicaPlanTratamiento || ''),
                        "[DESCRIPCION-SOCIAL]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionSocialPlanTratamiento || ''),
                        "[DESCRIPCION-CONDUCTUAL]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionConductualPlanTratamiento || ''),
                        "[DESCRIPCION-FAMILIAR]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionFamiliarPlanTratamiento || ''),
                        "[DESCRIPCION-NIVEL-RIESGO]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionNivelRiesgoPlanTratamiento || ''),
                        "[EVOLUCION-PSICOLOGICA]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionEvolucionPsicologicaPlanTratamiento || ''),
                        "[EVOLUCION-SOCIAL]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionEvolucionSocialPlanTratamiento || ''),
                        "[EVOLUCION-CONDUCTUAL]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionEvolucionConductualPlanTratamiento || ''),
                        "[EVOLUCION-FAMILIAR]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionEvolucionFamiliarPlanTratamiento || ''),
                        "[EVOLUCION-NIVEL-RIESGO]": this.utilidades.escaparHTML(informeEgresoDTO.descripcionEvolucionNivelRiesgoPlanTratamiento || ''),
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(informeEgresoDTO.conclusiones || ''),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(informeEgresoDTO.recomendaciones || '')
                      };

                      // 7. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }

                          // 8. Abrir el PDF en una nueva pestaña
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

  /**
   * Obtiene la lista paginada de informes de egreso
   * Implementa el filtrado híbrido para textos, fechas y usuarios
   */
  obtenerInformesEgreso() {
    // Crear la solicitud de paginación
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original
    const filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Verificar si es un filtro de fecha
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Configurar filtro para el backend
    if (esFiltroDeFecha) {
      // Si es fecha, no enviar filtro al backend
      solicitudPaginacion.filter = '';
    } else {
      // Si no es fecha, enviar el filtro al backend para búsqueda de texto
      solicitudPaginacion.filter = filtroOriginal;
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Mostrar diálogo de carga (opcional)
    const dialogoCarga = this.servicioMensajes.mensajeLoading("Cargando informes de egreso...");

    // Llamada al servicio de backend
    this.servicioInformeEgreso.obtenerInformesEgresoPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeEgresoPIIDTO>>) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        if (!environment.production) {
          console.log('Respuesta de obtenerInformesEgresoPaginado:', respuesta);
        }

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          this.listaInformesEgreso = [];
          this.paginacion.totalItems = 0;
          return;
        }

        // Formatear fechas usando formatearFechaSinHora para mostrar solo DD-MM-YYYY
        let datosTransformados = respuesta.data.data.map((informe: any) => ({
          ...informe,
          // Formatear la fecha para que se muestre solo DD-MM-YYYY
          fechaCreacion: informe.fechaCreacion ? this.utilidades.formatearFechaSinHora(informe.fechaCreacion) : '',
          // Mantener fecha original para filtrado
          fechaCreacionOriginal: informe.fechaCreacion
        }));

        // Aplicar filtro de fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datosTransformados = this.utilidades.filtrarPorFecha(
            datosTransformados,
            filtroOriginal,
            'fechaCreacionOriginal'
          );

          // Actualizar paginación con el total de elementos filtrados
          this.paginacion.totalItems = datosTransformados.length;
        } else {
          // Si no es filtro de fecha, usar el total del backend
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        // Actualizar la lista de informes
        this.listaInformesEgreso = datosTransformados;

        if (!environment.production) {
          console.log('Datos transformados:', datosTransformados);
          console.log('Total de elementos:', this.paginacion.totalItems);
        }
      },
      error: (error: any) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        console.error('Error al obtener informes de egreso:', error);
        
        // Limpiar la lista en caso de error
        this.listaInformesEgreso = [];
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
          this.servicioMensajes.mensajeError(`Error al obtener datos: ${mensajeError}`);
        }

        if (this.servicioInformeEgreso.checkError) {
          this.servicioInformeEgreso.checkError(error);
        }
      }
    });
  }

  /**
   * Descarga Excel completo aplicando el mismo formato de fechas del listado
   */
  descargarExcelCompleto() {
    // Crear la solicitud de paginación
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000; // Máximo número de registros
    solicitudPaginacion.page = 0; // Primera página
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Guardar el filtro original
    const filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Verificar si es un filtro de fecha
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Configurar filtro para el backend
    if (esFiltroDeFecha) {
      // Si es fecha, no enviar filtro al backend
      solicitudPaginacion.filter = '';
    } else {
      // Si no es fecha, enviar el filtro al backend para búsqueda de texto
      solicitudPaginacion.filter = filtroOriginal;
    }

    // Aplicar ordenamiento si existe (mantener el mismo orden que en la tabla)
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Mostrar diálogo de carga
    const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando descarga de Excel...");

    // Llamada al servicio de backend
    this.servicioInformeEgreso.obtenerInformesEgresoPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeEgresoPIIDTO>>) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(
            respuesta.titulo || 'Error al obtener datos', 
            respuesta.mensaje || 'No se pudieron obtener los datos para la exportación'
          );
          return;
        }

        // Aplicar el mismo formato que en el listado para consistencia
        let datosTransformados = respuesta.data.data.map((informe: any) => ({
          ...informe,
          // Formatear la fecha para que se muestre solo DD-MM-YYYY
          fechaCreacion: informe.fechaCreacion ? this.utilidades.formatearFechaSinHora(informe.fechaCreacion) : '',
          // Mantener fecha original para filtrado
          fechaCreacionOriginal: informe.fechaCreacion
        }));

        // Aplicar filtro de fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datosTransformados = this.utilidades.filtrarPorFecha(
            datosTransformados,
            filtroOriginal,
            'fechaCreacionOriginal'
          );
        }

        // Validar que hay datos para exportar
        if (datosTransformados.length === 0) {
          this.servicioMensajes.mensajeAdvertencia(
            'Sin datos para exportar', 
            'No hay informes de egreso que cumplan con los criterios de búsqueda actuales.'
          );
          return;
        }

        // Realizar la exportación
        try {
          this.tablaComponent.exportXLSX(datosTransformados);
          
          // Mensaje de éxito
          this.servicioMensajes.mensajeExitoso(
            'Exportación exitosa', 
            `Se exportaron ${datosTransformados.length} informes de egreso a Excel.`
          );
          
          if (!environment.production) {
            console.log(`Excel generado con ${datosTransformados.length} registros`);
          }
          
        } catch (exportError) {
          console.error('Error al generar el archivo Excel:', exportError);
          this.servicioMensajes.mensajeError('Error al generar el archivo Excel. Por favor, intente nuevamente.');
        }
      },
      error: (error: any) => {
        // Cerrar diálogo de carga
        dialogoCarga.close();
        console.error('Error al obtener informes de egreso para Excel:', error);
        
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

        if (this.servicioInformeEgreso.checkError) {
          this.servicioInformeEgreso.checkError(error);
        }
      }
    });
  }

  /**
   * Maneja el evento de cambio de página
   * @param eventoPaginacion Evento de paginación
   */
  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerInformesEgreso();
  }

  /**
   * Maneja eventos de ordenamiento de columnas con validación mejorada
   * @param evento Evento con información de ordenamiento
   */
  manejarEventoOrdenamiento(evento: Sort) {
    if (!environment.production) {
      console.log('Evento ordenamiento informe egreso PII:', evento);
    }
    
    // Inicializar solicitudPaginacion si no existe
    if (!this.solicitudPaginacion) {
      this.solicitudPaginacion = new PaginacionRequest();
    }
    
    // Lista de campos que usan ordenamiento especial (opcional para debugging)
    const camposOrdenamientoEspecial = ['nombreCompletoUsuarioCreacion'];
    
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
      
      // Log adicional para campos con ordenamiento especial
      if (!environment.production && camposOrdenamientoEspecial.includes(evento.active)) {
        console.log(`Ordenando por campo con ordenamiento especial: ${evento.active} ${evento.direction}`);
      }
    } else {
      // Si no hay dirección, limpiar el ordenamiento
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    
    // Reiniciar a la primera página cuando se cambia el ordenamiento
    this.paginacion.pageIndex = 0;
    
    // Llamar al método de obtención con manejo de errores
    this.obtenerInformesEgreso();
  }

  /**
   * Maneja el evento de búsqueda
   * @param filtro Texto de búsqueda
   */
  manejarEventoBusqueda(filtro: string) {
    // Guardar el filtro en la solicitud de paginación
    this.solicitudPaginacion.filter = filtro;

    // Verificar si parece un filtro de fecha para optimizar la búsqueda
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtro);

    // Mostrar indicación de tipo de filtro en consola para depuración
    if (!environment.production) {
      if (esFiltroDeFecha) {
        console.log('Aplicando filtro de fecha:', filtro);
      } else {
        console.log('Aplicando filtro de texto:', filtro);
      }
    }

    // Resetear a la primera página cuando se busca
    this.paginacion.pageIndex = 0;

    // Llamar al método principal para obtener datos con el filtro aplicado
    this.obtenerInformesEgreso();
  }
}