import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { EvaluacionDomiciliariaDTO } from 'app/core/model/both/EvaluacionDomiciliariaDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { EvaluacionDomiciliariaService } from 'app/modules/seguridad/services/EvaluacionDomiciliaria.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { Sort } from '@angular/material/sort';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { HttpClient } from '@angular/common/http';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { MatDialog } from '@angular/material/dialog';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';

@Component({
  selector: 'app-evaluacion-domiciliaria',
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
  templateUrl: './evaluacion-domiciliaria.component.html',
  styleUrl: './evaluacion-domiciliaria.component.scss'
})
export class EvaluacionDomiciliariaComponent implements OnInit {
  // Identificadores del contexto actual
  identificadorFichaPrincipal: string;
  tituloPantalla: string;
  centro: JerarquiaDTO;
  medioCerrado = false;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA;

  // Datos y configuración de paginación
  listaEvaluacionesDomiciliarias: EvaluacionDomiciliariaDTO[] = [];
  listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  // Configuración de columnas para la tabla
  columnasTabla: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaEntrevista: "Fecha de entrevista",
    personaEntrevistada: "Persona entrevistada",
    duracionVista: "Duración de la visita",
    visitaRealizada: "Visita realizada"
  };

  constructor(
    private servicioEvaluacionDomiciliaria: EvaluacionDomiciliariaService,
    private servicioDatosFamiliares: DatosFamiliaresService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioMensajes: DialogMensajeService,
    private dialog: MatDialog,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    public servicioPdf: PdfService,
    private servicioJerarquia: JerarquiaService,
    private http: HttpClient,
  ) { }

  /**
   * Inicializa el componente cargando datos necesarios
   * Configura identificadores y carga datos del centro del usuario logueado
   */
  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarCentroUsuarioLogueado();
    this.obtenerTodasPersonasRelacionadas().then(() => {
      this.obtenerEvaluacionesDomiciliarias();
    });
  }

  /**
   * Obtiene todas las personas relacionadas para el selector de personas entrevistadas
   * @returns Promise que se resuelve cuando se cargan las personas relacionadas
   */
  obtenerTodasPersonasRelacionadas(): Promise<void> {
    return new Promise((resolve) => {
      const solicitudPaginacion = new PaginacionRequest();
      solicitudPaginacion.size = 100;
      solicitudPaginacion.page = 0;
      solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

      this.servicioDatosFamiliares.obtenerPersonasRelacionadas(solicitudPaginacion,this.nemonicoMenu)
        .subscribe({
          next: (respuesta: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
            if (!respuesta.exito) {
              this.servicioMensajes.mensajeErrorConTitulo(
                respuesta.titulo,
                respuesta.mensaje
              );
              resolve();
              return;
            }

            this.listaPersonasRelacionadas = respuesta.data.data;

            // Agregar la opción "Otros" al final de la lista
            const personaOtros = new PersonaRelacionadaDTO();
            personaOtros.tokenIdentificador = "OTROS";
            personaOtros.nombres = "Otros";
            this.listaPersonasRelacionadas.push(personaOtros);

            resolve();
          },
          error: (error) => {
            console.error('Error cargando personas relacionadas:', error);
            resolve();
          }
        });
    });
  }

  /**
   * Carga la información del centro del usuario logueado
   * En el sistema multijerárquico, cada usuario pertenece a una jerarquía específica
   */
  cargarCentroUsuarioLogueado() {
    this.servicioJerarquia
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.servicioJerarquia.checkError(respuesta);
            return;
          }

          this.centro = respuesta.data;
          this.ajustarTituloSegunCentroUsuario();
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  /**
   * Ajusta el título de la pantalla según el tipo de centro del usuario logueado
   * SOA: "evaluación de visita domiciliaria", otros: "visita domiciliaria"
   */
  ajustarTituloSegunCentroUsuario() {
    const esSOA = this.centro?.jerarquiaPadre?.nemonico === 'SOA';
    this.tituloPantalla = esSOA ? 'evaluación de visita domiciliaria' : 'visita domiciliaria';
    this.medioCerrado = !esSOA;
  }

  /**
   * Determina si el centro del usuario logueado no es de tipo SOA
   * @returns true si no es SOA, false si es SOA
   */
  get noEsSOA(): boolean {
    return this.centro?.jerarquiaPadre?.nemonico !== 'SOA';
  }

  /**
   * Obtiene el texto descriptivo del tipo de evaluación según el centro del usuario
   * @returns Texto para usar en mensajes según el tipo de centro
   */
  get textoTipoEvaluacion(): string {
    return this.noEsSOA ? 'evaluación domiciliaria' : 'evaluación de visita domiciliaria';
  }

  /**
   * Navega a la vista de visualización de una evaluación domiciliaria específica
   * @param evaluacionDomiciliariaDTO Evaluación a visualizar
   */
  visualizarEvaluacionDomiciliaria(evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO) {
    evaluacionDomiciliariaDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-evaluacion-domiciliaria'], {
      state: { evaluacionDomiciliariaDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Navega a la vista de edición de una evaluación domiciliaria específica
   * @param evaluacionDomiciliariaDTO Evaluación a editar
   */
  editarEvaluacionDomiciliaria(evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO) {
    this.enrutador.navigate(['crear-editar-evaluacion-domiciliaria'], {
      state: { evaluacionDomiciliariaDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Elimina una evaluación domiciliaria después de confirmar la acción
   * @param evaluacionDomiciliariaDTO Evaluación a eliminar
   */
  eliminarEvaluacionDomiciliaria(evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de eliminar la ${this.textoTipoEvaluacion}? Esta operación es irreversible`,
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Eliminando la ${this.textoTipoEvaluacion}...`);
          this.servicioEvaluacionDomiciliaria.eliminarEvaluacionDomiciliaria(evaluacionDomiciliariaDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;
              this.obtenerEvaluacionesDomiciliarias();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioEvaluacionDomiciliaria.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Navega a la vista de creación de una nueva evaluación domiciliaria
   */
  agregarEvaluacionDomiciliaria() {
    this.enrutador.navigate(['crear-editar-evaluacion-domiciliaria'], {
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Obtiene y formatea las evaluaciones domiciliarias para mostrar en el listado
   * Aplica formato correcto a fechas (solo día-mes-año) y duración de visita
   */
  obtenerEvaluacionesDomiciliarias() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Gestión del filtro para detectar si es un filtro de fecha
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

    this.servicioEvaluacionDomiciliaria.obtenerEvaluacionesDomiciliariasPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionDomiciliariaDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          this.listaEvaluacionesDomiciliarias = [];
          this.paginacion.totalItems = 0;
          return;
        }

        // Convertir los datos aplicando formato correcto a fechas y duración
        let datos = respuesta.data.data.map(evaluacion => {
          // Crear copia del objeto y agregar campos formateados
          const evaluacionFormateada = { ...evaluacion } as any;
          
          // Convertir fecha a objeto Date y formatear consistentemente
          evaluacionFormateada.fechaEntrevista = this.utilidades.formatearFecha(this.convertirFechaSegura(evaluacion.fechaEntrevista));
          
          // Usar la misma lógica que el reporte para duración
          evaluacionFormateada.duracionVista = this.formatearDuracionVisita(evaluacion.duracionVista);
          
          // Determinar nombre de persona entrevistada
          evaluacionFormateada.personaEntrevistada = this.obtenerNombrePersonaEntrevistada(
            evaluacion.tokenIdentificadorPersonaRelacionada,
            evaluacion.otraPersonaRelacionada
          );
          
          // Formatear texto de visita realizada
          evaluacionFormateada.visitaRealizada = evaluacion.visitaRealizada ? 'Sí' : 'No';
          
          return evaluacionFormateada;
        });

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaEntrevista');
          this.paginacion.totalItems = datos.length;
        } else {
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        this.listaEvaluacionesDomiciliarias = datos;
      },
      error: (error: any) => {
        console.error('Error al obtener evaluaciones domiciliarias:', error);
        
        // Limpiar la lista en caso de error
        this.listaEvaluacionesDomiciliarias = [];
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
      }
    });
  }

  /**
   * Descarga Excel completo aplicando el mismo formato de fechas y duración del listado
   */
  descargarExcelCompleto() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000; // Máximo número de registros
    solicitudPaginacion.page = 0; // Primera página
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Gestión del filtro para detectar si es un filtro de fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = '';
    } else if (this.solicitudPaginacion?.filter) {
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Configuración de ordenamiento (mantener el mismo orden que en la tabla)
    if (this.solicitudPaginacion?.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Mostrar diálogo de carga
    const dialogoCarga = this.servicioMensajes.mensajeLoading('Preparando descarga de Excel...');

    this.servicioEvaluacionDomiciliaria.obtenerEvaluacionesDomiciliariasPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionDomiciliariaDTO>>) => {
        dialogoCarga.close();

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(
            respuesta.titulo || 'Error al obtener datos', 
            respuesta.mensaje || 'No se pudieron obtener los datos para la exportación'
          );
          return;
        }

        // Aplicar el mismo formato que en el listado para consistencia
        let datos = respuesta.data.data.map(evaluacion => {
          // Crear copia del objeto y aplicar formatos usando FuncionesUtils
          const evaluacionFormateada = { ...evaluacion } as any;
          
          // Convertir fecha a objeto Date y formatear consistentemente
          evaluacionFormateada.fechaEntrevista = this.utilidades.formatearFecha(this.convertirFechaSegura(evaluacion.fechaEntrevista));
          evaluacionFormateada.fechaRegistro = this.utilidades.formatearFecha(this.convertirFechaSegura(evaluacion.fechaRegistro));
          
          // Usar la misma lógica que el reporte para duración
          evaluacionFormateada.duracionVista = this.formatearDuracionVisita(evaluacion.duracionVista);
          
          // Determinar nombre de persona entrevistada
          evaluacionFormateada.personaEntrevistada = this.obtenerNombrePersonaEntrevistada(
            evaluacion.tokenIdentificadorPersonaRelacionada,
            evaluacion.otraPersonaRelacionada
          );
          
          // Formatear texto de visita realizada
          evaluacionFormateada.visitaRealizada = evaluacion.visitaRealizada ? 'Sí' : 'No';
          
          return evaluacionFormateada;
        });

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaEntrevista');
        }

        // Validar que hay datos para exportar
        if (datos.length === 0) {
          this.servicioMensajes.mensajeAdvertencia(
            'Sin datos para exportar', 
            'No hay evaluaciones domiciliarias que cumplan con los criterios de búsqueda actuales.'
          );
          return;
        }

        // Realizar la exportación
        try {
          this.tablaComponent.exportXLSX(datos);
          
          // Mensaje de éxito
          this.servicioMensajes.mensajeExitoso(
            'Exportación exitosa', 
            `Se exportaron ${datos.length} evaluaciones domiciliarias a Excel.`
          );
          
        } catch (exportError) {
          console.error('Error al generar el archivo Excel:', exportError);
          this.servicioMensajes.mensajeError('Error al generar el archivo Excel. Por favor, intente nuevamente.');
        }
      },
      error: (error: any) => {
        dialogoCarga.close();
        console.error('Error al obtener evaluaciones domiciliarias para Excel:', error);
        
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

  /**
   * Formatea la duración de visita manejando tanto strings como numbers
   * @param duracion Puede ser string "HH:MM" o number decimal
   * @returns String formateado como "HH:MM"
   */
  private formatearDuracionVisita(duracion: any): string {
    try {
      // Si es null o undefined
      if (duracion === null || duracion === undefined) {
        return 'No especificado';
      }

      // Si es string (formato "HH:MM"), devolverlo directamente
      if (typeof duracion === 'string') {
        const duracionLimpia = duracion.trim();
        // Verificar si tiene formato HH:MM
        if (/^\d{1,2}:\d{2}$/.test(duracionLimpia)) {
          return duracionLimpia;
        }
        
        // Si es string pero no formato HH:MM, intentar convertir a number
        const numeroConvertido = parseFloat(duracionLimpia);
        if (!isNaN(numeroConvertido)) {
          return this.utilidades.convertirDecimalATiempo(numeroConvertido);
        }
        
        return duracionLimpia; // Devolver tal como está si no se puede procesar
      }

      // Si es number, usar convertirDecimalATiempo
      if (typeof duracion === 'number') {
        return this.utilidades.convertirDecimalATiempo(duracion);
      }

      // Para cualquier otro caso, intentar conversión
      return this.utilidades.convertirDecimalATiempo(Number(duracion));
      
    } catch (error) {
      console.error('Error al formatear duración:', duracion, error);
      return 'Error formato';
    }
  }

  /**
   * Convierte una fecha de manera segura manejando diferentes formatos
   * @param fecha Fecha en formato string, Date o null/undefined
   * @returns Objeto Date válido o fecha actual si hay error
   */
  private convertirFechaSegura(fecha: any): Date {
    if (!fecha) {
      return new Date();
    }

    try {
      // Si ya es un objeto Date
      if (fecha instanceof Date) {
        return isNaN(fecha.getTime()) ? new Date() : fecha;
      }

      // Si es string, intentar parsearlo
      if (typeof fecha === 'string') {
        // Limpiar el string
        const fechaLimpia = fecha.trim();
        
        // Manejar formato ISO con timezone (YYYY-MM-DDTHH:mm:ss.sssZ)
        if (fechaLimpia.includes('T') || fechaLimpia.includes('Z')) {
          const fechaParseada = new Date(fechaLimpia);
          if (!isNaN(fechaParseada.getTime())) {
            return fechaParseada;
          }
        }
        
        // Manejar formatos DD-MM-YYYY o DD/MM/YYYY
        const partesGuion = fechaLimpia.split('-');
        const partesSlash = fechaLimpia.split('/');
        
        if (partesGuion.length === 3 && partesGuion[0].length <= 2) {
          // Formato DD-MM-YYYY
          const dia = parseInt(partesGuion[0], 10);
          const mes = parseInt(partesGuion[1], 10) - 1; // Los meses en Date van de 0-11
          const año = parseInt(partesGuion[2], 10);
          const fechaFormateada = new Date(año, mes, dia);
          if (!isNaN(fechaFormateada.getTime())) {
            return fechaFormateada;
          }
        }
        
        if (partesSlash.length === 3 && partesSlash[0].length <= 2) {
          // Formato DD/MM/YYYY
          const dia = parseInt(partesSlash[0], 10);
          const mes = parseInt(partesSlash[1], 10) - 1; // Los meses en Date van de 0-11
          const año = parseInt(partesSlash[2], 10);
          const fechaFormateada = new Date(año, mes, dia);
          if (!isNaN(fechaFormateada.getTime())) {
            return fechaFormateada;
          }
        }
        
        // Fallback: intentar conversión directa del string
        const fechaDirecta = new Date(fechaLimpia);
        if (!isNaN(fechaDirecta.getTime())) {
          return fechaDirecta;
        }
      }

      // Si es número (timestamp)
      if (typeof fecha === 'number') {
        const fechaTimestamp = new Date(fecha);
        return isNaN(fechaTimestamp.getTime()) ? new Date() : fechaTimestamp;
      }

      // Fallback final: intentar conversión directa
      const fechaConvertida = new Date(fecha);
      return isNaN(fechaConvertida.getTime()) ? new Date() : fechaConvertida;
      
    } catch (error) {
      console.error('Error al convertir fecha:', fecha, error);
      return new Date();
    }
  }

  /**
   * Evalúa si la visita fue realizada manejando diferentes tipos de datos
   * @param valor Valor que puede ser boolean, string, number, etc.
   * @returns true si la visita fue realizada, false en caso contrario
   */
  private evaluarVisitaRealizada(valor: any): boolean {
    if (valor === null || valor === undefined) {
      return false;
    }

    // Si es boolean, usar directamente
    if (typeof valor === 'boolean') {
      return valor;
    }

    // Si es string, evaluar diferentes posibilidades
    if (typeof valor === 'string') {
      const valorLimpio = valor.trim().toLowerCase();
      return valorLimpio === 'true' || valorLimpio === 's' || valorLimpio === 'si' || valorLimpio === 'sí' || valorLimpio === '1';
    }

    // Si es número, considerar 1 como true, 0 como false
    if (typeof valor === 'number') {
      return valor === 1;
    }

    // Para cualquier otro caso, convertir a boolean
    return Boolean(valor);
  }

  /**
   * Determina el nombre de la persona entrevistada según los datos disponibles
   * @param tokenIdentificador ID de la persona relacionada
   * @param otraPersona Valor del campo otraPersonaRelacionada
   * @returns Nombre de la persona entrevistada
   */
  obtenerNombrePersonaEntrevistada(tokenIdentificador: string, otraPersona: string): string {
    // Primero verificar si hay un valor en otraPersonaRelacionada
    if (otraPersona && otraPersona.trim() !== '') {
      return otraPersona;
    }

    // Si hay un tokenIdentificador, buscar la persona relacionada
    if (tokenIdentificador) {
      const personaRelacionada = this.listaPersonasRelacionadas.find(
        p => p.tokenIdentificador === tokenIdentificador
      );

      if (personaRelacionada) {
        return this.obtenerNombreCompleto(personaRelacionada);
      }
    }

    return 'No especificado';
  }

  /**
   * Maneja eventos de paginación actualizando el índice y tamaño de página
   * @param eventoPaginacion Evento con nueva configuración de paginación
   */
  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerEvaluacionesDomiciliarias();
  }

  /**
   * Maneja eventos de ordenamiento configurando columna y dirección
   * @param evento Evento con información de ordenamiento
   */
  manejarEventoOrdenamiento(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerEvaluacionesDomiciliarias();
  }

  /**
   * Maneja eventos de búsqueda aplicando filtro y reiniciando paginación
   * @param filtro Texto a buscar en los registros
   */
  manejarEventoBusqueda(filtro: string) {
    this.solicitudPaginacion.filter = filtro;
    this.paginacion.pageIndex = 0;
    this.obtenerEvaluacionesDomiciliarias();
  }

  /**
   * Abre diálogo para subir documentos asociados a una evaluación domiciliaria
   * @param evaluacionDomiciliariaDTO Evaluación a la que se asociará el documento
   */
  subirDocumento(evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO) {
    // Validar que la evaluación tenga un identificador válido
    if (!evaluacionDomiciliariaDTO || !evaluacionDomiciliariaDTO.tokenIdentificador) {
      this.servicioMensajes.mensajeError('No se puede subir el documento sin una evaluación válida.');
      return;
    }

    const dialogRef = this.dialog.open(SubidaDocumentoGenericoComponent, {
      width: '1200px',
      height: '700px',
      data: {
        item: evaluacionDomiciliariaDTO,
        nemonicoMenu: etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA,
        nemonicoCarpeta: etiquetasModel.CARPETA_DOMICILIARIO,
        tipoServicio: 'evaluacionDomiciliaria',
        seccionTipoDocumento: etiquetasModel.SECCION_FICHA_IDENT_EVALUACIONES
      }
    });
  }

  /**
   * Abre diálogo para visualizar documentos asociados a una evaluación domiciliaria
   * @param evaluacionDomiciliariaDTO Evaluación cuyos documentos se quieren ver
   */
  verDocumentos(evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO) {
    const dialogRef = this.dialog.open(PopupDocumentosComponent, {
      width: '1000px',
      height: '500px',
      data: {
        tokenItem: evaluacionDomiciliariaDTO.tokenIdentificador,
        tipoServicio: "EVALUACION_DOMICILIARIA",
        nemonicoMenu: this.nemonicoMenu
      }
    });
  }

  /**
   * Construye el nombre completo de una persona relacionada combinando sus componentes
   * @param persona Objeto PersonaRelacionadaDTO 
   * @returns String con el nombre completo
   */
  obtenerNombreCompleto(persona: PersonaRelacionadaDTO): string {
    if (!persona) {
      return 'No especificado';
    }

    // Si tiene el campo nombres que ya contiene el nombre completo, usarlo
    if (persona.nombres) {
      return persona.nombres;
    }

    // Si no tiene nombres pero tiene los componentes individuales
    const partes: string[] = [];

    if (persona.primerNombre) partes.push(persona.primerNombre);
    if (persona.segundoNombre) partes.push(persona.segundoNombre);
    if (persona.apellidoPaterno) partes.push(persona.apellidoPaterno);
    if (persona.apellidoMaterno) partes.push(persona.apellidoMaterno);

    // Si aún no hay partes, usar los campos alternativos
    if (partes.length === 0) {
      if (persona.primerApellido) partes.push(persona.primerApellido);
      if (persona.segundoApellido) partes.push(persona.segundoApellido);
    }

    return partes.length > 0 ? partes.join(' ') : 'No especificado';
  }

  /**
   * Genera e imprime la ficha de evaluación domiciliaria en formato PDF
   * Para la impresión, usa el centro específico de la evaluación (no del usuario)
   * @param evaluacionDomiciliariaDTO Evaluación a imprimir
   */
  imprimirEvaluacionDomiciliaria(evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de imprimir la ${this.textoTipoEvaluacion}?`,
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Preparando la impresión de ${this.textoTipoEvaluacion}...`);

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
                      
                      // Obtener persona entrevistada usando la misma lógica que crear-editar
                      const personaEntrevistada = this.obtenerNombrePersonaEntrevistada(
                        evaluacionDomiciliariaDTO.tokenIdentificadorPersonaRelacionada,
                        evaluacionDomiciliariaDTO.otraPersonaRelacionada
                      );

                      // Variables de control de visualización usando evaluación robusta
                      const visitaRealizada = this.evaluarVisitaRealizada(evaluacionDomiciliariaDTO.visitaRealizada);
                      const mostrarMotivoNoVisita = !visitaRealizada; // Solo mostrar cuando NO se realizó la visita

                      // Para la impresión, usar el centro específico de la evaluación
                      const centroEvaluacion = evaluacionDomiciliariaDTO.centro;
                      const esMedioCerrado = centroEvaluacion?.jerarquiaPadre?.nemonico !== 'SOA';

                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_EVALUACION_DOMICILIARIA';

                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        // Usar el nombre del centro de la evaluación específica
                        "[CENTRO]": this.utilidades.escaparHTML(centroEvaluacion?.nombre || this.centro?.nombre || 'Centro de rehabilitación'),
                        // Convertir fecha a objeto Date y formatear consistentemente
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(this.convertirFechaSegura(evaluacionDomiciliariaDTO.fechaRegistro))),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[FECHA-ENTREVISTA]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(this.convertirFechaSegura(evaluacionDomiciliariaDTO.fechaEntrevista))),
                        // Manejar duracionVista como string o number
                        "[DURACION-VISITA]": this.utilidades.escaparHTML(
                          evaluacionDomiciliariaDTO.duracionVista !== null && evaluacionDomiciliariaDTO.duracionVista !== undefined 
                            ? this.formatearDuracionVisita(evaluacionDomiciliariaDTO.duracionVista)
                            : 'No especificado'
                        ),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[PERSONA-ENTREVISTADA]": this.utilidades.escaparHTML(personaEntrevistada),
                        // Usar evaluación robusta para determinar el texto correcto
                        "[VISITA-REALIZADA]": visitaRealizada ? 'Sí' : 'No',
                        // Control correcto de visualización del motivo
                        "[DISPLAY-MOTIVO-NO-VISITA]": mostrarMotivoNoVisita ? 'table-row' : 'none',
                        "[MOTIVO-NO-VISITA]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.motivoNoVisita || ''),
                        "[OBJETIVO-GENERAL]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.objetivoGeneral || ''),
                        "[DESARROLLO-VISITA]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.desarrolloVisitaDomiciliaria || ''),
                        "[CONCLUSIONES]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.conclusiones || ''),
                        "[RECOMENDACIONES]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.recomendaciones || ''),
                        // Usar el tipo de centro de la evaluación específica para el PDF
                        "[DISPLAY-MC]": esMedioCerrado ? 'block' : 'none',
                        "[DISPLAY-MA]": !esMedioCerrado ? 'block' : 'none',
                        // Campos Medio Cerrado
                        "[DINAMICA-FAMILIAR-DISFUNCIONAL]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.dinamicaFamiliarDisfuncional || ''),
                        "[CARACTERISTICAS-ENTORNO-SOCIAL-MC]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.caracteristicasEntornoSocialMC || ''),
                        "[FACTORES-PROTECTORES]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.factoresProtectores || ''),
                        // Campos Medio Abierto
                        "[FACTORES-RIESGO-FAMILIA]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.factoresRiesgoFamilia || ''),
                        "[FACTORES-RIESGO-SOCIAL]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.factoresRiesgoSocial || ''),
                        "[FACTORES-PROTECTORES-FAMILIA]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.factoresProtectoresFamilia || ''),
                        "[FACTORES-PROTECTORES-SOCIAL]": this.utilidades.escaparHTML(evaluacionDomiciliariaDTO.factoresProtectoresSocial || '')
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
