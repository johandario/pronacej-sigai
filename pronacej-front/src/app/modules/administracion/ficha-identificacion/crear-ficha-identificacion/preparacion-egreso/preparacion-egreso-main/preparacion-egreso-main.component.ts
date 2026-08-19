import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { ApreciacionFinalTratamientoDTO } from 'app/core/model/both/apreciacionFinalTratamientoDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { ReforzamientoDTO } from 'app/core/model/both/salida/ReforzamientoDTO.model';
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
import { RelacionEgresoService } from 'app/modules/administracion/services/relacionEgreso.service';
import { ApreciacionFinalTratamientoService } from 'app/modules/seguridad/services/apreciacionFinalTratamiento.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-preparacion-egreso-main',
  standalone: true,
  imports: [
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    TablaDatosComponent
  ],
  templateUrl: './preparacion-egreso-main.component.html',
  styleUrl: './preparacion-egreso-main.component.scss'
})
export class PreparacionEgresoMainComponent {
  uuid_fp: string;

  tituloPantallaReforzamiento = "actividad de reforzamiento";

  listaReforzamientos: any[] = [];

  selectedTabIndex = 0;

  @ViewChild('tablaActividades') tablaActividadesComponent: TablaDatosComponent<any>;
  @ViewChild('tablaApreciaciones') tablaApreciacionesComponent: TablaDatosComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    planVida: "Plan de vida",
    numeroSesiones: "Número de sesiones",
    fechaUltimaSesionFormateada: "Fecha de última sesión",
    tipoUltimaSesion: "Tipo de última sesión",
    responsableUltimaSesion: "Responsable de última sesión",
    observacionesUltimaSesion: "Observaciones de última sesión"
  };

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();


  tituloPantallaApreciacion: string = "apreciación final del tratamiento";

  nemonicoMenuActividadReforzamiento = etiquetasModel.MENU_ACTIVIDADES_REFORZAMIENTO;
  nemonicoMenuApreciacionFinalTratamiento = etiquetasModel.NEMONICO_MENU_APRECIACION_FINAL;

  // Configuración de columnas
  columnasTabla: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaRegistro: "Fecha registro",
    nombreCompletoUsuarioCreacion: "Usuario que registró"
  };

  // Datos y paginación
  listaApreciacionesFinales: ApreciacionFinalTratamientoDTO[] = [];
  paginacionApreciacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  constructor(
    private servicioApreciacionFinal: ApreciacionFinalTratamientoService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private relacionEgresoService: RelacionEgresoService,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private pdfService: PdfService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    public funcionesUtils: FuncionesUtils,
    private authSerguridadServicio: AuthSerguridadServicio
  ) { }


  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_PREPARACION_PARA_EL_EGRESO"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    const savedIndex = sessionStorage.getItem('selectedTabIndex');
    if (savedIndex !== null) {
      this.selectedTabIndex = +savedIndex;
    }

    this.obtenerReforzamientos();
    this.obtenerListaApreciacionesFinales();
  }

  onTabChange(event: MatTabChangeEvent) {
    this.selectedTabIndex = event.index;
    sessionStorage.setItem('selectedTabIndex', event.index.toString());
  }

  eliminarReforzamiento(reforzamientoDTO: ReforzamientoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el reforzamiento? Esta operación es irreversible",
      "¿Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el funcionario..");
            this.relacionEgresoService.removerReforzamiento(reforzamientoDTO, this.nemonicoMenuActividadReforzamiento).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerReforzamientos();
                },
                error: (error: any) => {
                  load.close();

                  this.authSerguridadServicio.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerReforzamientos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp

    this.relacionEgresoService.obtenerReforzamientos(this.paginacionRequest, this.nemonicoMenuActividadReforzamiento).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ReforzamientoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }          

          this.listaReforzamientos = response.data.data;
          this.listaReforzamientos.forEach(
            (item: ReforzamientoDTO) => {
              if (item.fechaCreacion) item.fechaCreacion = new Date(item.fechaCreacion);
              // if (item.fechaUltimaSesion) item.fechaUltimaSesion = new Date(item.fechaUltimaSesion);
            });
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  descargarExcelCompletoActividades() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp

    this.relacionEgresoService.obtenerReforzamientos(this.paginacionRequest, this.nemonicoMenuActividadReforzamiento).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<any>>) => {

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }

          this.tablaActividadesComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  agregarReforzamiento() {
    this.router.navigate(['reforzamiento/crear-editar'], { relativeTo: this.route });
  }

  editarReforzamiento(reforzamientoDTO: ReforzamientoDTO) {
    this.router.navigate(['reforzamiento/crear-editar'], { state: { item: reforzamientoDTO }, relativeTo: this.route });
  }

  verReforzamiento(reforzamientoDTO: ReforzamientoDTO) {
    this.router.navigate(['reforzamiento/crear-editar'], { state: { item: reforzamientoDTO, esVisualizacion: true }, relativeTo: this.route });
  }

  imprimirReforzamiento(reforzamientoDTO: ReforzamientoDTO) {
    console.log(reforzamientoDTO);
    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(reforzamientoDTO.idFichaIdentificacion, this.nemonicoMenuActividadReforzamiento).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
        if (!response.exito) {
          return;
        }

        this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
          .subscribe((data: ArrayBuffer) => {
            const base64String = this.funcionesUtils.arrayBufferToBase64(data);
            const base64Image = `data:image/png;base64,${base64String}`;


            const fichaDTO = response.data;

            // Construir los nuevos campos dinámicamente
            const adolescente = `${fichaDTO.nombres || ''} ${fichaDTO.apellidoPaterno} ${fichaDTO.apellidoMaterno}`;

            const actualDate = new Date();

            const lugarFechaNacimiento = `${fichaDTO.lugarNacimiento || ''}, ${this.funcionesUtils.formatearFecha(fichaDTO.fechaNacimiento)}`;
            const edadActual = this.funcionesUtils.getEdad(fichaDTO.fechaNacimiento).toString() || 'N/A';
            const direccion = fichaDTO.direccion || 'N/A';

            //Obtener grado de instrucción desde catálogos
            this.catalogoService.obtenerCatalogoPorNemonico(fichaDTO.modalidadEstudio, '').subscribe({
              next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
                const catalogoModalidadEstudio = respuestaCatalogo.data;
                const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

                this.relacionEgresoService.obtenerReforzamientoPorToken(reforzamientoDTO, etiquetasModel.MENU_ACTIVIDADES_REFORZAMIENTO).subscribe({
                  next: (response: RespuestaPorDefecto<ReforzamientoDTO>) => {

                    if (!response.exito) {
                      this.dialogMensajeService.mensajeError(
                        'Hubo un problema al obtener el reforzamiento. ' + response.mensaje
                      );
                      return;
                    }
                    else {

                      reforzamientoDTO = response.data;

                      let tablaSesiones = new TablaPlantilla();
                      tablaSesiones.encabezados = [
                        'Tipo', 'Responsable', 'Fecha', 'Observaciones'
                      ];

                      tablaSesiones.filas = reforzamientoDTO.sesiones.map(sesion => {
                        return {
                          'Tipo': sesion.nombretipoSesion,
                          'Responsable': sesion.nombreResponsable,
                          'Fecha': this.funcionesUtils.formatearFecha(sesion.fechaSesion),
                          'Observaciones': sesion.observaciones,
                        };
                      });


                      let request = new GeneracionPdfRequest();
                      request.nemonico = etiquetasModel.FORMULARIO_ACTIVIDAD_REFORZAMIENTO;
                      request.variables = {
                        "[IMG_BASE64]": base64Image,
                        "[FECHA_REGISTRO]": this.funcionesUtils.formatearFecha(actualDate.toString()),
                        "[HORA_REGISTRO]": this.funcionesUtils.formatearHora(actualDate.toString()),
                        "[ADOLESCENTE]": adolescente,
                        "[LUGAR_FECHA_NACIMIENTO]": lugarFechaNacimiento,
                        "[CENTRO]": fichaDTO.centroIngreso,
                        "[EDAD_ACTUAL]": edadActual,
                        "[GRADO_INSTRUCCION]": gradoInstruccion,
                        "[DIRECCION]": direccion,
                        "[PLAN_VIDA]": reforzamientoDTO.planVida ? "Sí" : "No",
                        "[TABLA_SESIONES]": JSON.stringify(tablaSesiones)
                      }

                      this.pdfService.generarPdf(request, etiquetasModel.MENU_ACTIVIDADES_REFORZAMIENTO).subscribe({
                        next: (response: RespuestaPorDefecto<string>) => {

                          if (!response.exito) {
                            this.dialogMensajeService.mensajeError(
                              'Hubo un problema al recuperar los registros. ' + response.mensaje
                            );
                            return;
                          }

                          const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

                          const pwa = window.open(url);
                        },
                        error: (error: any) => {
                          this.dialogMensajeService.mensajeError(
                            'Hubo un problema al generar el archivo. Inténtalo de nuevo.'
                          );
                        }
                      });
                    }
                  },
                  error: (error) => {
                    this.dialogMensajeService.mensajeError("Ocurrió un error al obtener el reforzamiento. Inténtalo de nuevo.");
                  }
                });
              },
              error: (error: any) => {
                console.error('Error al obtener el catálogo:', error);
                this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
              }
            });
          });
      },
      error: (error: any) => {
        this.fichaIdentificacionService.checkError(error);
      }
    });
  }

  handlePageEvent(event: PageEvent) {
    this.paginacion.pageSize = event.pageSize;
    this.paginacion.pageIndex = event.pageIndex;
    this.obtenerReforzamientos();
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
    this.obtenerReforzamientos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerReforzamientos();
  }

  visualizarApreciacionFinal(apreciacionFinalDTO: ApreciacionFinalTratamientoDTO) {
    apreciacionFinalDTO.esVisualizacion = true;
    this.router.navigate(['apreciacion/crear-editar'], {
      state: { apreciacionFinalDTO },
      relativeTo: this.route
    });
  }

  editarApreciacionFinal(apreciacionFinalDTO: ApreciacionFinalTratamientoDTO) {
    this.router.navigate(['apreciacion/crear-editar'], {
      state: { apreciacionFinalDTO },
      relativeTo: this.route
    });
  }

  eliminarApreciacionFinal(apreciacionFinalDTO: ApreciacionFinalTratamientoDTO) {
    const refDialogo = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Está seguro de eliminar la apreciación final? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.dialogMensajeService.mensajeLoading("Eliminando apreciación final...");
          this.servicioApreciacionFinal.eliminarApreciacionFinal(apreciacionFinalDTO, this.nemonicoMenuApreciacionFinalTratamiento).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              dialogoCarga.close();
              this.dialogMensajeService.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

              if (!respuesta.exito) return;
              this.obtenerListaApreciacionesFinales();
            },
            error: (error: any) => {
              dialogoCarga.close();
              this.servicioApreciacionFinal.checkError(error);
            }
          });
        }
      }
    });
  }

  agregarApreciacionFinal() {
    this.router.navigate(['apreciacion/crear-editar'], { relativeTo: this.route });
  }

  obtenerListaApreciacionesFinales() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.paginacionApreciacion.pageSize;
    solicitudPaginacion.page = this.paginacionApreciacion.pageIndex;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    // Gestión del filtro para detectar si es un filtro de fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.funcionesUtils.esPosibleFiltroFecha(filtroOriginal);

    // Si es filtro de fecha, no enviarlo al backend
    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = '';
    } else if (this.solicitudPaginacion.filter) {
      // Si no es un filtro de fecha, usar el filtro original
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioApreciacionFinal.obtenerApreciacionesFinalesPaginado(
      solicitudPaginacion,
      this.nemonicoMenuApreciacionFinalTratamiento
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<ApreciacionFinalTratamientoDTO>>) => {
        if (!environment.production) {
          console.log(respuesta);
        }

        if (!respuesta.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        let datos = respuesta.data.data;

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal) {
          datos = this.funcionesUtils.filtrarPorFecha(datos, filtroOriginal, 'fechaRegistro');
          this.paginacionApreciacion.totalItems = datos.length;
        } else {
          this.paginacionApreciacion.totalItems = respuesta.data.totalItems;
        }

        this.listaApreciacionesFinales = datos;
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
      }
    });
  }

  descargarExcelCompletoApreciaciones() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100000;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    // Gestión del filtro para detectar si es un filtro de fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';
    const esFiltroDeFecha = this.funcionesUtils.esPosibleFiltroFecha(filtroOriginal);

    // Si es filtro de fecha, no enviarlo al backend
    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = '';
    } else if (this.solicitudPaginacion.filter) {
      // Si no es un filtro de fecha, usar el filtro original
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Aplicar ordenamiento si existe
    if (this.solicitudPaginacion.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioApreciacionFinal.obtenerApreciacionesFinalesPaginado(
      solicitudPaginacion,
      this.nemonicoMenuApreciacionFinalTratamiento
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<ApreciacionFinalTratamientoDTO>>) => {

        if (!respuesta.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        let datos = respuesta.data.data;

        // Aplicar filtrado por fecha en el frontend si es necesario
        if (esFiltroDeFecha && filtroOriginal)
          datos = this.funcionesUtils.filtrarPorFecha(datos, filtroOriginal, 'fechaRegistro');

        this.tablaApreciacionesComponent.exportXLSX(datos);
      },
      error: (error: any) => {
        console.error('Error al obtener listado:', error);
      }
    });
  }

  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacionApreciacion.pageSize = eventoPaginacion.pageSize;
    this.paginacionApreciacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerListaApreciacionesFinales();
  }

  manejarEventoOrdenamiento(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerListaApreciacionesFinales();
  }

  manejarEventoBusqueda(filtro: string) {
    // Resetear a la primera página cuando se aplica un nuevo filtro
    this.paginacionApreciacion.pageIndex = 0;

    // Asignar el filtro a la solicitud de paginación
    this.solicitudPaginacion.filter = filtro;

    // Actualizar sort y direction si están definidos
    if (this.solicitudPaginacion.sort) {
      this.solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      this.solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    // Realizar la solicitud con el filtro aplicado
    this.obtenerListaApreciacionesFinales();
  }
}
