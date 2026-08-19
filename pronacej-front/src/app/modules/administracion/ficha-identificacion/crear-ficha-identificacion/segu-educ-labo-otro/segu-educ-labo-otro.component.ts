import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { SeguimientoEducativoLaboralOtrosDTO } from 'app/core/model/both/ia/seguimientoEducativoLaboralOtrosDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SeguimientoEducativoLaboralOtrosService } from 'app/modules/seguridad/services/seguimientoEducativoLaboralOtros.service';
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
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';
import { MatDialog } from '@angular/material/dialog';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';

@Component({
  selector: 'app-segu-educ-labo-otro',
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
  templateUrl: './segu-educ-labo-otro.component.html',
  styleUrl: './segu-educ-labo-otro.component.scss'
})
export class SeguEducLaboOtroComponent implements OnInit {
  // Identificadores
  identificadorFichaPrincipal: string;
  tituloPantalla: string = "seguimiento educativo/laboral/otros";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_EDUCATIVO_LABORAL;

  // Datos y paginación
  listaSeguimientos: SeguimientoEducativoLaboralOtrosDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();
  centro: JerarquiaDTO;

  // Catálogos
  listaTiposSeguimiento: CatalogoDTO[] = [];

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  // Configuración de columnas
  columnasMostrar: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaSeguimiento: "Fecha",
    "institucionVisitada": "Institución",
    "personaEntrevistada": "Persona entrevistada",
    fechaCreacion: "Fecha registro",
    nombreCompletoUsuarioCreacion: "Usuario que registró"
  };

  constructor(
    private servicioSeguimientoEducativo: SeguimientoEducativoLaboralOtrosService,
    private servicioMensajes: DialogMensajeService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private http: HttpClient,
    private servicioPdf: PdfService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioJerarquia: JerarquiaService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    // Obtener el identificador de la ficha principal de los parámetros de la ruta
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    // Cargar la lista de seguimientos al inicializar el componente
    this.obtenerListaSeguimientos();
    // Cargar información del centro
    this.cargarCentro();
    // Cargar datos de catálogos
    this.cargarDatosCatalogo();
  }

  /**
   * Carga los catálogos necesarios para el componente
   */
  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('TIPO_SEGUIMIENTO_EDUCATIVO_LABORAL', this.nemonicoMenu)
      .subscribe({
        next: (datos) => this.listaTiposSeguimiento = datos,
        error: (error) => console.error('Error al cargar tipos de seguimiento:', error)
      });
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
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  /**
   * Navega a la visualización de un seguimiento
   */
  visualizarSeguimiento(seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO) {
    seguimientoDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-seguimiento-educativo-laboral-otros'], {
      state: { seguimientoEducativoDTO: seguimientoDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Navega a la edición de un seguimiento
   */
  editarSeguimiento(seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO) {
    this.enrutador.navigate(['crear-editar-seguimiento-educativo-laboral-otros'], {
      state: { seguimientoEducativoDTO: seguimientoDTO },
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Elimina un seguimiento previo confirmación
   */
  eliminarSeguimiento(seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar el seguimiento educativo/laboral/otros? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Eliminando seguimiento...");
          this.servicioSeguimientoEducativo.eliminarSeguimiento(seguimientoDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;
              this.obtenerListaSeguimientos();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioSeguimientoEducativo.checkError(error);
            }
          });
        }
      }
    });
  }

  /**
   * Navega a la creación de un nuevo seguimiento
   */
  agregarSeguimiento() {
    this.enrutador.navigate(['crear-editar-seguimiento-educativo-laboral-otros'], {
      relativeTo: this.rutaActiva
    });
  }

  /**
   * Obtiene la lista paginada de seguimientos
   * Implementa el filtrado híbrido: texto en backend, fechas en frontend
   */
  obtenerListaSeguimientos() {
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

    this.servicioSeguimientoEducativo.obtenerSeguimientosPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>>) => {
        if (!environment.production) {
          console.log(respuesta);
        }

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // Obtener datos
        let datos = respuesta.data.data;

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaSeguimiento');
          this.paginacion.totalItems = datos.length;
        } else {
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        this.listaSeguimientos = datos;
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
        this.servicioMensajes.mensajeError('Error al cargar los seguimientos');
      }
    });
  }

  descargarExcelCompleto() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000;
    solicitudPaginacion.page = 0;
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

    this.servicioSeguimientoEducativo.obtenerSeguimientosPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>>) => {

        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // Obtener datos
        let datos = respuesta.data.data;

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal)
          datos = this.utilidades.filtrarPorFecha(datos, filtroOriginal, 'fechaSeguimiento');

        this.tablaComponent.exportXLSX(datos);
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
        this.servicioMensajes.mensajeError('Error al cargar los seguimientos');
      }
    });
  }

  /**
   * Maneja el evento de cambio de página
   */
  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerListaSeguimientos();
  }

  /**
   * Maneja el evento de ordenamiento de columnas
   */
  manejarEventoOrdenamiento(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerListaSeguimientos();
  }

  /**
   * Maneja el evento de búsqueda
   * Restablece la paginación cuando se aplica un nuevo filtro
   */
  manejarEventoBusqueda(filtro: string) {
    this.solicitudPaginacion.filter = filtro;
    // Al cambiar el filtro, volver a la primera página
    this.paginacion.pageIndex = 0;
    this.obtenerListaSeguimientos();
  }

  /**
   * Genera e imprime un PDF con los detalles del seguimiento
   */
  imprimirSeguimiento(seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO) {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      `¿Está seguro de imprimir el seguimiento educativo/laboral/otros?`,
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioMensajes.mensajeLoading(`Preparando la impresión del seguimiento...`);

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

                      // 5. Encontrar el tipo de seguimiento seleccionado
                      const tipoSeguimientoSeleccionado = this.listaTiposSeguimiento.find(t =>
                        t.tokenIdentificador === seguimientoDTO.tokenIdentificadorTipoSeguimientoSocial);

                      // 6. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SEGUIMIENTO_EDUCATIVO_LABORAL';

                      // 7. Preparar variables para la plantilla PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || ''),
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[ADOLESCENTE-NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[ADOLESCENTE-DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),

                        // Datos del seguimiento educativo/laboral/otros - APLICANDO escaparHTML a todos los valores
                        "[FECHA-SEGUIMIENTO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(seguimientoDTO.fechaSeguimiento)),
                        "[TIPO-SEGUIMIENTO]": this.utilidades.escaparHTML(tipoSeguimientoSeleccionado?.nombre || 'No especificado'),
                        "[INSTITUCION-VISITADA]": this.utilidades.escaparHTML(seguimientoDTO.institucionVisitada || 'No especificado'),
                        "[PERSONA-ENTREVISTADA]": this.utilidades.escaparHTML(seguimientoDTO.personaEntrevistada || 'No especificado'),
                        "[DIRECCION]": this.utilidades.escaparHTML(seguimientoDTO.direccion || 'No especificado'),
                        "[MEDIO-VERIFICACION]": this.utilidades.escaparHTML(seguimientoDTO.medioVerificacion || 'No especificado'),
                        "[RESULTADO-SEGUIMIENTO]": this.utilidades.escaparHTML(seguimientoDTO.resultadoSeguimiento || 'No especificado'),
                        "[SUGERENCIAS-RECOMENDACIONES]": this.utilidades.escaparHTML(seguimientoDTO.sugerenciasRecomendaciones || 'No especificado')
                      };

                      // 8. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
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

  subirDocumento(seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO) {
    // Agrega este código temporalmente al método subirDocumento de evaluación domiciliaria
    console.log('Estado completo al subir documento evaluación domiciliaria:', {
      item: seguimientoDTO,
      nemonicoMenu: etiquetasModel.NEMONICO_MENU_SITUACION_EDUCATIVA_LABORAL,
      nemonicoCarpeta: etiquetasModel.CARPETA_SEGUIMIENTO_EDUCATIVO,
      tipoServicio: 'seguimientoEducativo',
      seccionTipoDocumento: etiquetasModel.SECCION_FICHA_IDENT_EVALUACIONES
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
        nemonicoCarpeta: etiquetasModel.CARPETA_SEGUIMIENTO_EDUCATIVO,
        seccionTipoDocumento: etiquetasModel.SECCION_FICHA_IDENT_EVALUACIONES,
        tipoServicio: 'seguimientoEducativo',
      }
    });
  }

  verDocumentos(seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(PopupDocumentosComponent, {
      width: '1000px',
      height: '500px',
      data: {
        tokenItem: seguimientoDTO.tokenIdentificador,
        tipoServicio: "SEGUIMIENTO_EDUCATIVO_LABORAL",
        nemonicoMenu: etiquetasModel.NEMONICO_MENU_SITUACION_EDUCATIVA_LABORAL
      }
    });
  }
}
