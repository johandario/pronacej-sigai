import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { SeguimientoSocialDTO } from 'app/core/model/both/ia/SeguimientoSocialDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SeguimientoSocialService } from 'app/modules/administracion/services/seguimientoSocial.service';
import { environment } from 'environments/environment';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { Sort } from '@angular/material/sort';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { HttpClient } from '@angular/common/http';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PdfService } from 'app/core/services/pdf.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { MatDialog } from '@angular/material/dialog';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';

@Component({
  selector: 'app-eval-segu-soci',
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
  templateUrl: './eval-segu-soci.component.html',
  styleUrl: './eval-segu-soci.component.scss'
})
export class EvalSeguSociComponent implements OnInit {
  // Identificadores
  identificadorFichaPrincipal: string;
  tituloPantalla: string = "seguimiento social";

  // Tipo de centro
  tipoCentro: string = 'CJDR'; // Valor por defecto - CJDR, SOA o UAPICE

  // Configuración
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_SOCIAL;

  // Datos y paginación
  listaSeguimientosSociales: SeguimientoSocialDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();
  centro: JerarquiaDTO;

  // Listas para programas y ambientes
  listaProgramas: JerarquiaDTO[] = [];
  listaAmbientes: JerarquiaDTO[] = [];

  // Configuración de columnas (se configurará según el tipo de centro)
  columnasMostrar: any = {};

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  constructor(
    private servicioSeguimientoSocial: SeguimientoSocialService,
    private servicioMensajes: DialogMensajeService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private http: HttpClient,
    private servicioPdf: PdfService,
    private dialog: MatDialog,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioJerarquia: JerarquiaService
  ) { }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarCentro();
  }

  /**
   * Determina si se deben mostrar campos de programa y ambiente según el tipo de centro
   */
  debeVerProgramaYAmbiente(): boolean {
    return this.tipoCentro === 'CJDR';
  }

  /**
   * Configura las columnas a mostrar según el tipo de centro
   */
  configurarColumnas() {
    if (this.debeVerProgramaYAmbiente()) {
      // Si es un centro que muestra programa y ambiente (CJDR)
      this.columnasMostrar = {
        numero: "No.",
        acciones: "Acciones",
        fecha: "Fecha",
        "programa.nombre": "Programa",
        "ambiente.nombre": "Ambiente",
        fechaCreacion: "Fecha registro",
        nombreCompletoUsuarioCreacion: "Usuario que registró"
      };
    } else {
      // Si es un centro que NO muestra programa y ambiente (SOA, UAPICE)
      this.columnasMostrar = {
        numero: "No.",
        acciones: "Acciones",
        fecha: "Fecha",
        fechaCreacion: "Fecha registro",
        nombreCompletoUsuarioCreacion: "Usuario que registró"
      };
    }
  }

  /**
   * Carga la información del centro al que pertenece el usuario
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

          // Determinar el tipo de centro basado en el nemonico o nombre
          if (this.centro?.jerarquiaPadre?.nemonico === 'SOA') {
            this.tipoCentro = 'SOA';
          } else if (this.centro?.nemonico === 'UAPISE' ||
            (this.centro?.nombre && this.centro.nombre.includes('UAPISE')) ||
            (this.centro?.jerarquiaPadre?.nemonico === 'UAPISE')) {
            this.tipoCentro = 'UAPICE';
          } else {
            this.tipoCentro = 'CJDR'; // Por defecto
          }

          // Debug para verificar el tipo de centro
          if (!environment.production) {
            console.log('Tipo de centro detectado:', this.tipoCentro);
            console.log('Centro:', this.centro);
          }

          // Configurar columnas después de determinar el tipo de centro
          this.configurarColumnas();

          // Cargar programas solo si debe ver programa y ambiente
          if (this.debeVerProgramaYAmbiente()) {
            this.cargarProgramas(this.centro.tokenIdentificador);
          } else {
            // Si no debe ver programa y ambiente, directamente obtenemos la lista de seguimientos
            this.obtenerListaSeguimientoSocial();
          }
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  /**
   * Carga la lista de programas asociados al centro
   * @param tokenCentro Token identificador del centro
   */
  cargarProgramas(tokenCentro: string) {
    this.servicioJerarquia.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenCentro)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (respuesta.exito) {
            this.listaProgramas = respuesta.data;

            // Para cada programa, cargar sus ambientes
            this.cargarTodosLosAmbientes();

            // Una vez cargados los programas, obtener la lista de seguimientos
            this.obtenerListaSeguimientoSocial();
          }
        },
        error: (error) => this.servicioJerarquia.checkError(error)
      });
  }

  /**
   * Carga los ambientes para todos los programas disponibles
   */
  cargarTodosLosAmbientes() {
    this.listaAmbientes = []; // Reiniciar la lista de ambientes

    // Para cada programa, cargar sus ambientes asociados
    this.listaProgramas.forEach(programa => {
      this.cargarAmbientes(programa.tokenIdentificador);
    });
  }

  /**
   * Carga los ambientes asociados a un programa específico
   * @param tokenPrograma Token identificador del programa
   */
  cargarAmbientes(tokenPrograma: string) {
    this.servicioJerarquia.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenPrograma)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (respuesta.exito) {
            // Añadir los nuevos ambientes a la lista existente, sin duplicados
            respuesta.data.forEach(ambiente => {
              const ambienteExistente = this.listaAmbientes.find(a => a.tokenIdentificador === ambiente.tokenIdentificador);
              if (!ambienteExistente) {
                this.listaAmbientes.push(ambiente);
              }
            });
          }
        },
        error: (error) => this.servicioJerarquia.checkError(error)
      });
  }

  /**
   * Navega a la visualización de un seguimiento social
   */
  visualizarSeguimientoSocial(seguimientoSocialDTO: SeguimientoSocialDTO) {
    seguimientoSocialDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-seguimiento'], {
      state: {
        seguimientoSocialDTO,
        tipoCentro: this.tipoCentro,
        centro: this.centro
      },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Navega a la edición de un seguimiento social
   */
  editarSeguimientoSocial(seguimientoSocialDTO: SeguimientoSocialDTO) {
    this.enrutador.navigate(['crear-editar-seguimiento'], {
      state: {
        seguimientoSocialDTO,
        tipoCentro: this.tipoCentro,
        centro: this.centro
      },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Elimina un seguimiento social previo confirmación
   */
  eliminarSeguimientoSocial(seguimientoSocialDTO: SeguimientoSocialDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar el seguimiento social? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando seguimiento social...");
          this.servicioSeguimientoSocial.eliminarSeguimientoSocial(seguimientoSocialDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;
              this.obtenerListaSeguimientoSocial();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioSeguimientoSocial.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Navega a la creación de un nuevo seguimiento social
   */
  agregarSeguimientoSocial() {
    this.enrutador.navigate(['crear-editar-seguimiento'], {
      state: {
        tipoCentro: this.tipoCentro,
        centro: this.centro
      },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Obtiene la lista paginada de seguimientos sociales
   * Implementa el filtrado híbrido: texto en backend, fechas en frontend
   */
  obtenerListaSeguimientoSocial() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    // Gestión del filtro para detectar si es un filtro de fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si parece un filtro de fecha, no enviarlo al backend
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

    this.servicioSeguimientoSocial.obtenerSeguimientosSocialesPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SeguimientoSocialDTO>>) => {
        if (!environment.production) {
          console.log('Respuesta del servidor:', respuesta);
        }

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          this.listaSeguimientosSociales = [];
          this.paginacion.totalItems = 0;
          return;
        }

        // Obtener la lista de seguimientos sociales
        let datos = respuesta.data.data;

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fecha');
          this.paginacion.totalItems = datos.length;
        } else {
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        this.listaSeguimientosSociales = datos;

        // Completar la información de programas y ambientes solo si debe mostrarlos
        if (this.debeVerProgramaYAmbiente()) {
          this.completarInformacionSeguimientos();
        }

        if (!environment.production) {
          console.log('Datos procesados:', this.listaSeguimientosSociales);
          console.log('Total de elementos:', this.paginacion.totalItems);
        }
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
        
        // Limpiar la lista en caso de error
        this.listaSeguimientosSociales = [];
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
          this.servicioMensajes.mensajeError(`Error al cargar los seguimientos sociales: ${mensajeError}`);
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

    // Gestión del filtro para detectar si es un filtro de fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si parece un filtro de fecha, no enviarlo al backend
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

    this.servicioSeguimientoSocial.obtenerSeguimientosSocialesPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SeguimientoSocialDTO>>) => {
        dialogoCarga.close();

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(
            respuesta.titulo || 'Error al obtener datos',
            respuesta.mensaje || 'No se pudieron obtener los datos para la exportación'
          );
          return;
        }

        // Obtener la lista de seguimientos sociales
        let datos = respuesta.data.data;

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fecha');
        }

        // Completar información de programas y ambientes si es necesario
        this.listaSeguimientosSociales = datos;
        if (this.debeVerProgramaYAmbiente()) {
          this.completarInformacionSeguimientos();
          datos = this.listaSeguimientosSociales; // usar los datos con campos planos
        }

        // Validar que hay datos para exportar
        if (datos.length === 0) {
          this.servicioMensajes.mensajeAdvertencia(
            'Sin datos para exportar',
            'No hay seguimientos sociales que cumplan con los criterios de búsqueda actuales.'
          );
          return;
        }

        // Realizar la exportación
        try {
          this.tablaComponent.exportXLSX(datos);
          
          // Mensaje de éxito
          this.servicioMensajes.mensajeExitoso(
            'Exportación exitosa',
            `Se exportaron ${datos.length} seguimientos sociales a Excel.`
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
        console.error('Error al obtener seguimientos sociales para Excel:', error);
        
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
   * Completa la información de programas y ambientes en los seguimientos sociales
   * usando las listas precargadas
   */
  completarInformacionSeguimientos() {
    // Si no debe ver programa y ambiente, no es necesario completar esta información
    if (!this.debeVerProgramaYAmbiente()) return;

    // Para cada seguimiento, asignar la información completa del programa y ambiente
    this.listaSeguimientosSociales = this.listaSeguimientosSociales.map(seguimiento => {
      // Creamos una copia del seguimiento para no modificar el original
      const seguimientoCopia = { ...seguimiento };

      // Creamos propiedades planas para la tabla
      // Esto es clave: estamos creando propiedades "programa.nombre" y "ambiente.nombre" directamente en el objeto
      seguimientoCopia["programa.nombre"] = "No especificado";
      seguimientoCopia["ambiente.nombre"] = "No especificado";

      // Completar información del programa
      if (seguimientoCopia.programa && seguimientoCopia.programa.tokenIdentificador) {
        const programaCompleto = this.listaProgramas.find(
          p => p.tokenIdentificador === seguimientoCopia.programa.tokenIdentificador
        );

        if (programaCompleto) {
          // Asignar el programa completo
          seguimientoCopia.programa = programaCompleto;
          // Asignar la propiedad plana para la tabla
          seguimientoCopia["programa.nombre"] = programaCompleto.nombre;
        } else {
          // Si no encontramos el programa, asignamos un nombre temporal
          const nombreTemp = `Programa ${seguimientoCopia.programa.tokenIdentificador.substring(0, 8)}`;
          seguimientoCopia.programa.nombre = nombreTemp;
          seguimientoCopia["programa.nombre"] = nombreTemp;
        }
      }

      // Completar información del ambiente
      if (seguimientoCopia.ambiente && seguimientoCopia.ambiente.tokenIdentificador) {
        const ambienteCompleto = this.listaAmbientes.find(
          a => a.tokenIdentificador === seguimientoCopia.ambiente.tokenIdentificador
        );

        if (ambienteCompleto) {
          // Asignar el ambiente completo
          seguimientoCopia.ambiente = ambienteCompleto;
          // Asignar la propiedad plana para la tabla
          seguimientoCopia["ambiente.nombre"] = ambienteCompleto.nombre;
        } else {
          // Si no encontramos el ambiente, asignamos un nombre temporal
          const nombreTemp = `Ambiente ${seguimientoCopia.ambiente.tokenIdentificador.substring(0, 8)}`;
          seguimientoCopia.ambiente.nombre = nombreTemp;
          seguimientoCopia["ambiente.nombre"] = nombreTemp;
        }
      }

      return seguimientoCopia;
    });

    // Log para depuración (solo en entornos no productivos)
    if (!environment.production) {
      console.log('Seguimientos con información completa:', this.listaSeguimientosSociales);

      // Verificar explícitamente si tenemos las propiedades planas
      if (this.listaSeguimientosSociales.length > 0) {
        const primero = this.listaSeguimientosSociales[0];
        console.log('Propiedad programa.nombre:', primero["programa.nombre"]);
        console.log('Propiedad ambiente.nombre:', primero["ambiente.nombre"]);
      }
    }
  }

  /**
   * Maneja el evento de cambio de página
   */
  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerListaSeguimientoSocial();
  }

  /**
   * Maneja eventos de ordenamiento de columnas con validación mejorada
   * @param evento Evento con información de ordenamiento
   */
  manejarEventoOrdenamiento(evento: Sort) {
    if (!environment.production) {
      console.log('Evento ordenamiento seguimiento social:', evento);
    }
    
    // Inicializar solicitudPaginacion si no existe
    if (!this.solicitudPaginacion) {
      this.solicitudPaginacion = new PaginacionRequest();
    }
    
    // Lista de campos que pueden causar problemas (opcional para debugging)
    const camposComplejos = ['nombreCompletoUsuarioCreacion', 'programa.nombre', 'ambiente.nombre'];
    
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
      
      // Log adicional para campos complejos
      if (!environment.production && camposComplejos.includes(evento.active)) {
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
    this.obtenerListaSeguimientoSocial();
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
    
    this.servicioSeguimientoSocial.obtenerSeguimientosSocialesPaginado(
      this.construirSolicitudPaginacion(),
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta) => {
        dialogoCarga.close();
        this.procesarRespuestaSeguimientos(respuesta);
      },
      error: (error) => {
        dialogoCarga.close();
        console.error('Error en ordenamiento:', error);
        
        // Restaurar ordenamiento anterior
        this.solicitudPaginacion.sort = ordenamientoAnterior.sort;
        this.solicitudPaginacion.direction = ordenamientoAnterior.direction;
        
        // Mensajes específicos según el campo
        if (evento.active === 'nombreCompletoUsuarioCreacion') {
          this.servicioMensajes.mensajeAdvertencia(
            'Ordenamiento no disponible',
            'El ordenamiento por usuario que registró no está disponible temporalmente. Se mantuvo el ordenamiento anterior.'
          );
        } else if (evento.active === 'programa.nombre') {
          this.servicioMensajes.mensajeAdvertencia(
            'Ordenamiento no disponible', 
            'El ordenamiento por programa no está disponible temporalmente. Se mantuvo el ordenamiento anterior.'
          );
        } else if (evento.active === 'ambiente.nombre') {
          this.servicioMensajes.mensajeAdvertencia(
            'Ordenamiento no disponible',
            'El ordenamiento por ambiente no está disponible temporalmente. Se mantuvo el ordenamiento anterior.'
          );
        } else {
          this.servicioMensajes.mensajeError(`Error al ordenar por ${this.columnasMostrar[evento.active] || evento.active}.`);
        }
        
        // Recargar con ordenamiento anterior
        this.obtenerListaSeguimientoSocial();
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
   * Procesa la respuesta de seguimientos de manera centralizada
   */
  private procesarRespuestaSeguimientos(respuesta: RespuestaPorDefecto<PaginacionResponse<SeguimientoSocialDTO>>) {
    if (!respuesta.exito) {
      this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
      this.listaSeguimientosSociales = [];
      this.paginacion.totalItems = 0;
      return;
    }

    // Obtener la lista de seguimientos sociales
    let datos = respuesta.data.data;

    // Aplicar filtrado por fecha si es necesario
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);
    
    if (esFiltroDeFecha && filtroOriginal) {
      datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fecha');
      this.paginacion.totalItems = datos.length;
    } else {
      this.paginacion.totalItems = respuesta.data.totalItems;
    }

    this.listaSeguimientosSociales = datos;

    // Completar la información de programas y ambientes solo si debe mostrarlos
    if (this.debeVerProgramaYAmbiente()) {
      this.completarInformacionSeguimientos();
    }
  }

  /**
   * Maneja el evento de búsqueda
   * Restablece la paginación cuando se aplica un nuevo filtro
   */
  manejarEventoBusqueda(filtro: string) {
    this.solicitudPaginacion.filter = filtro;
    // Al cambiar el filtro, volver a la primera página
    this.paginacion.pageIndex = 0;
    this.obtenerListaSeguimientoSocial();
  }

  /**
   * Genera e imprime un PDF con los detalles del seguimiento social
   * @param seguimientoSocialDTO Objeto con los datos del seguimiento a imprimir
   */
  imprimirSeguimientoSocial(seguimientoSocialDTO: SeguimientoSocialDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de imprimir el seguimiento social?`,
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Preparando la impresión del seguimiento social...`);

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

                      let tipoActividadNombre = 'No especificado';

                      if (seguimientoSocialDTO.nemonicoTipoActividadSocial) {
                        tipoActividadNombre = this.convertirNemonicoANombre(seguimientoSocialDTO.nemonicoTipoActividadSocial);
                      }

                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_SOCIAL';

                      // Determinar modalidad según tipo de centro
                      let modalidad = 'Medio cerrado';
                      if (this.tipoCentro === 'SOA') {
                        modalidad = 'Medio abierto';
                      } else if (this.tipoCentro === 'UAPICE') {
                        modalidad = 'UAPICE';
                      }

                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'Centro de rehabilitación'),
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),

                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),

                        "[PROGRAMA]": this.utilidades.escaparHTML(seguimientoSocialDTO.programa?.nombre || 'No especificado'),
                        "[AMBIENTE]": this.utilidades.escaparHTML(seguimientoSocialDTO.ambiente?.nombre || 'No especificado'),
                        "[FECHA-SEGUIMIENTO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(seguimientoSocialDTO.fecha)),
                        "[TIPO-SEGUIMIENTO]": this.utilidades.escaparHTML(tipoActividadNombre),
                        "[MODALIDAD]": this.utilidades.escaparHTML(modalidad),

                        "[MOTIVO]": this.utilidades.escaparHTML(tipoActividadNombre),
                        "[DESCRIPCION]": this.utilidades.escaparHTML(seguimientoSocialDTO.descripcionSocial || 'No especificado'),
                        "[ACUERDOS-COMPROMISOS]": this.utilidades.escaparHTML(seguimientoSocialDTO.accionesAdoptadas || 'No especificado'),
                        "[OBSERVACIONES]": this.utilidades.escaparHTML(seguimientoSocialDTO.comentarios || 'No especificado'),

                        "[MOSTRAR_CJDR]": this.tipoCentro === 'CJDR' ? 'table-row-group' : 'none',
                        "[MOSTRAR_NO_CJDR]": this.tipoCentro === 'CJDR' ? 'none' : 'table-row-group'
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

  subirDocumento(seguimientoDTO: SeguimientoSocialDTO) {
    // Agrega este código temporalmente al método subirDocumento de evaluación domiciliaria
    console.log('Estado completo al subir documento evaluación domiciliaria:', {
      item: seguimientoDTO,
      nemonicoMenu: etiquetasModel.NEMONICO_MENU_SITUACION_EDUCATIVA_LABORAL,
      nemonicoCarpeta: etiquetasModel.CARPETA_SEGUIMIENTO_EDUCATIVO,
      tipoServicio: 'evaluacionSeguimientoEducativo'
    });

    // Verifica que evaluacionDomiciliariaDTO tenga un tokenIdentificador
    if (!seguimientoDTO || !seguimientoDTO.tokenIdentificador) {
      this.servicioMensajes.mensajeError('No se puede subir el documento sin una evaluación válida.');
      return;
    }

    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(SubidaDocumentoGenericoComponent, {
      width: '1200px',
      height: '700px',
      data: {
        item: seguimientoDTO,
        nemonicoMenu: etiquetasModel.NEMONICO_MENU_SITUACION_EDUCATIVA_LABORAL,
        nemonicoCarpeta: etiquetasModel.CARPETA_SEGUIMIENTO_SOCIAL,
        tipoServicio: 'seguimientoSocial',
      }
    });
  }

  verDocumentos(seguimientoDTO: SeguimientoSocialDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(PopupDocumentosComponent, {
      width: '1000px',
      height: '500px',
      data: {
        tokenItem: seguimientoDTO.tokenIdentificador,
        tipoServicio: "SEGUIMIENTO_SOCIAL",
        nemonicoMenu: etiquetasModel.NEMONICO_MENU_SITUACION_EDUCATIVA_LABORAL
      }
    });
  }

  /**
   * Convierte un nemónico a un formato legible como nombre
   * @param nemonico Nemónico a convertir
   * @returns Cadena formateada
   */
  private convertirNemonicoANombre(nemonico: string): string {
    if (!nemonico) return 'No especificado';

    // Reemplazar los guiones bajos por espacios
    let nombre = nemonico.replace(/_/g, ' ');

    // Convertir a minúsculas
    nombre = nombre.toLowerCase();

    // Convertir la primera letra de cada palabra a mayúsculas
    nombre = nombre.split(' ')
      .map(palabra => palabra.charAt(0).toUpperCase() + palabra.slice(1))
      .join(' ');

    return nombre;
  }
}
