import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { ActividadOcupacionalDTO } from 'app/core/model/both/ActividadOcupacional.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { EvaluacionSeguimientoEducativoLaboralDTO } from 'app/core/model/both/evaluacionSeguimientoEducativoLaboralDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SeguimientoService } from 'app/modules/administracion/services/seguimiento.service';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import { EvaluacionConductualService } from 'app/modules/seguridad/services/evaluacionConductual.service';
import { EvaluacionSeguimientoEducativoLaboralService } from 'app/modules/seguridad/services/evaluacionSeguimiento.service';
import { InstitucionService } from 'app/modules/institucion/institucion.service';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';
import { MatDialog } from '@angular/material/dialog';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { EncuestaPdfService } from 'app/modules/general/services/encuestaPdf.service';
import { HttpClient } from '@angular/common/http';
import { SeguimientoActividadOcupacionalDTO } from 'app/core/model/both/SeguimientoActividadOcupacional.model';
import { environment } from 'environments/environment';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { PdfService } from 'app/core/services/pdf.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';

@Component({
  selector: 'app-evaluacion-conductual-ver',
  standalone: true,
  imports: [
    MatTableModule,
    MatTabsModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatInputModule,
    CommonModule,
    FormsModule,
    TablaListaComponent,
    MatSortModule,
    MatFormFieldModule,
    TablaDatosComponent,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './evaluacion-conductual-ver.component.html',
  styleUrl: './evaluacion-conductual-ver.component.scss'
})
export class EvaluacionConductualVerComponent implements OnInit {
  nemonicoMenuEvaluacionConductual = etiquetasModel.NEMONICO_MENU_EVALUACION_CONDUCTUAL;
  nemonicoMenuActividadSocioRecreativa = etiquetasModel.NEMONICO_MENU_ACTIVIDAD_SOCIO_RECREATIVA;
  nemonicoMenuEvaluacionEducativaLaboral = etiquetasModel.NEMONICO_MENU_EVALUACION_EDUCATIVA_LABORAL;
  nemonicoEncuesta = etiquetasModel.ENCUESTA_EVALUACION_CONDUCTUAL;

  uuid_fp: string;

  selectedTabIndex = 0;

  listaEvaluaciones: EncabezadoDTO[] = [];
  listaEvaluacionesSeguimiento: EvaluacionSeguimientoEducativoLaboralDTO[] = [];
  listaInstituciones: RegistroInstitucionDTO[] = [];

  @ViewChild('tablaConductual') tablaConductualComponent: TablaListaComponent<any>;
  @ViewChild('tablaActividad') tablaActividadComponent: TablaListaComponent<any>;
  @ViewChild('tablaEvaluacion') tablaEvaluacionComponent: TablaDatosComponent<any>;

  tokenEncuesta: string;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  listaSeguimientoActividadOcupacional: SeguimientoActividadOcupacionalDTO[] = [];

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    fechaCompletacion: "Fecha de finalización",
    nombre: "Nombre",
    descripcion: "Descripción"
  };

  paginacionSocial: Paginacion = new Paginacion();
  keyLabelsTableSocial: any = {
    numero: "No.",
    acciones: "Acciones",
    institucionEducativaLaboral: "Institución",
    tipoEntidad: "Tipo de entidad",
    fechaCreacion: "Fecha registro",
    nombreCompletoUsuarioCreacion: "Usuario que registró",
  };

  paginacionRequestActividadOcupacional: PaginacionRequest = new PaginacionRequest();
  paginacionActividadOcupacional: Paginacion = new Paginacion();
  paginacionRequestEvaluacionEducativa: PaginacionRequest = new PaginacionRequest();


  listaActividades: ActividadOcupacionalDTO[] = [];
  keyLabelsActividad: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaInicio: "Fecha de inicio",
    nombrePrograma: "Programa",
    numeroDocumento: "Número documento",
  };

  centro: JerarquiaDTO;

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private encuestaService: EncuestaService,
    private seguimientoService: SeguimientoService,
    private evaluacionConductualService: EvaluacionConductualService,
    private evaluacionSeguimientoService: EvaluacionSeguimientoEducativoLaboralService,
    private encuestaPdfService: EncuestaPdfService,
    private institucionService: InstitucionService,
    private http: HttpClient,
    public funcionesUtils: FuncionesUtils,
    private jerarquiaService: JerarquiaService,
    public pdfService: PdfService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private fichaIdentificacionService: FichaIdentificacionService,
    private catalogoService: CatalogoService,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_EVALUACION_CONDUCTUAL"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    const savedIndex = sessionStorage.getItem('selectedConductualTabIndex');
    if (savedIndex !== null) {
      this.selectedTabIndex = +savedIndex;
    }

    this.cargarTokenEncuesta();
    this.cargarInstituciones();
    this.obtenerEvaluaciones();
    this.obtenerActividadesOcupacionales();
    this.cargarCentro();
  }

  onTabChange(event: MatTabChangeEvent) {
    this.selectedTabIndex = event.index;
    sessionStorage.setItem('selectedConductualTabIndex', event.index.toString());
  }

  cargarInstituciones() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = 100;
    paginacionRequest.page = 0;

    this.institucionService.obtenerRegistroInstituciones(paginacionRequest, this.nemonicoMenuEvaluacionConductual).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
        if (response.exito) {
          this.listaInstituciones = response.data.data;
          this.obtenerListaEvaluacionSeguimiento();
        }
      },
      error: (error: any) => {
        console.error('Error cargando instituciones:', error);
      }
    });
  }

  //#region Evaluaciones
  obtenerEvaluaciones() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(this.paginacionRequest, this.nemonicoEncuesta, this.nemonicoMenuEvaluacionConductual).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaEvaluaciones = response.data.data;
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

  descargarExcelCompletoConductual() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(this.paginacionRequest, this.nemonicoEncuesta, this.nemonicoMenuEvaluacionConductual).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaConductualComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  async imprimirEvaluacion(encabezado: EncabezadoDTO) {

    let encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.tokenIdentificador = encabezado.tokenIdentificador

    let encuesta = new EncuestaDTO();

    let load = this.dialogMensajeService.mensajeLoading("Enviando evaluación...");
    this.encuestaService.obtenerEvaluacionPorTokenEncabezado(encabezadoDTO, this.nemonicoMenuEvaluacionConductual).subscribe(
      {
        next: async (response: RespuestaPorDefecto<EncuestaDTO>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          encuesta = response.data;

          const base64 = await this.encuestaPdfService.generarPDF(encabezado, encuesta, true);
          load.close();

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  verEditarEvaluacion(encabezadoDTO: EncabezadoDTO) {
    console.log(encabezadoDTO);
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        completada: encabezadoDTO.completada,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  agregarEvaluacion() {
    this.router.navigate(['crear-editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncuesta: this.tokenEncuesta,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  eliminarEvaluacion(encabezadoDTO: EncabezadoDTO) {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la evaluación conductual? También se eliminará los seguimientos que se hayan realizado sobre esta. Esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la evaluación conductual..");
            this.encuestaService.eliminarEvaluacion(encabezadoDTO, this.nemonicoMenuEvaluacionConductual).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar la evaluación conductual. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerEvaluaciones();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al guardar la evaluación conductual. Inténtalo de nuevo.'
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
  //#endregion

  realizarSeguimiento(encabezadoDTO: EncabezadoDTO) {
    this.router.navigate(['seguimiento'], {
      relativeTo: this.route,
      state: {
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        centro: this.centro,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  cargarTokenEncuesta() {
    let encuestaDTO = new EncuestaDTO();

    this.encuestaService.obtenerEncuestas(encuestaDTO, this.nemonicoMenuEvaluacionConductual).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO[]>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }
          var encuestaConductual = response.data.find(x => x.nemonico == this.nemonicoEncuesta);

          this.tokenEncuesta = encuestaConductual.tokenIdentificador;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  obtenerActividadesOcupacionales() {
    this.paginacionRequestActividadOcupacional.size = this.paginacionActividadOcupacional.pageSize;
    this.paginacionRequestActividadOcupacional.page = this.paginacionActividadOcupacional.pageIndex;
    this.paginacionRequestActividadOcupacional.tokenIdentificador = this.uuid_fp;

    this.evaluacionConductualService.listarActividadesOcupacionales(this.paginacionRequestActividadOcupacional, this.nemonicoMenuActividadSocioRecreativa).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ActividadOcupacionalDTO>>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaActividades = response.data.data;
          this.paginacionActividadOcupacional.totalItems = response.data.totalItems;
          this.listaActividades.forEach(actividad => {
            actividad.nombrePrograma = actividad.programa ? actividad.programa.nombre : null;
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

  descargarExcelCompletoActividad() {
    this.paginacionRequestActividadOcupacional.size = 100000;
    this.paginacionRequestActividadOcupacional.page = 0;
    this.paginacionRequestActividadOcupacional.tokenIdentificador = this.uuid_fp;

    this.evaluacionConductualService.listarActividadesOcupacionales(this.paginacionRequestActividadOcupacional, this.nemonicoMenuActividadSocioRecreativa).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ActividadOcupacionalDTO>>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          let listaActividades = response.data.data;
          listaActividades.forEach(actividad => {
            actividad.nombrePrograma = actividad.programa ? actividad.programa.nombre : null;
          });

          this.tablaActividadComponent.exportXLSX(listaActividades);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  obtenerListaEvaluacionSeguimiento() {
    this.paginacionRequestEvaluacionEducativa.size = this.paginacionSocial.pageSize;
    this.paginacionRequestEvaluacionEducativa.page = this.paginacionSocial.pageIndex;
    this.paginacionRequestEvaluacionEducativa.tokenIdentificador = this.uuid_fp;

    this.evaluacionSeguimientoService.obtenerEvaluacionesSeguimientoPaginado(
      this.paginacionRequestEvaluacionEducativa,
      this.nemonicoMenuEvaluacionEducativaLaboral
    ).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }
        this.listaEvaluacionesSeguimiento = response.data.data.map(evaluacion => {
          const institucion = this.listaInstituciones.find(
            inst => inst.tokenIdentificador === evaluacion.tokenIdentificadorInstitucion
          );
          return {
            ...evaluacion,
            institucionEducativaLaboral: institucion?.nombreOrganizacion ? institucion?.nombreOrganizacion : evaluacion.nombreInstitucionOtros
              ? evaluacion.nombreInstitucionOtros : 'No especificado',
            tipoEntidad: institucion?.tipoOrganizacion?.nombre || 'No especificado'
          };
        });
        this.paginacionSocial.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        console.log(error);
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  descargarExcelCompletoEvaluacion() {
    this.paginacionRequestEvaluacionEducativa.size = 100000;
    this.paginacionRequestEvaluacionEducativa.page = 0;
    this.paginacionRequestEvaluacionEducativa.tokenIdentificador = this.uuid_fp;

    this.evaluacionSeguimientoService.obtenerEvaluacionesSeguimientoPaginado(
      this.paginacionRequestEvaluacionEducativa,
      this.nemonicoMenuEvaluacionEducativaLaboral
    ).subscribe({
      next: (response: RespuestaPorDefecto<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        let listaEvaluacionesSeguimiento = response.data.data.map(evaluacion => {
          const institucion = this.listaInstituciones.find(
            inst => inst.tokenIdentificador === evaluacion.tokenIdentificadorInstitucion
          );
          return {
            ...evaluacion,
            institucionEducativaLaboral: institucion?.nombreOrganizacion ? institucion?.nombreOrganizacion : evaluacion.nombreInstitucionOtros
              ? evaluacion.nombreInstitucionOtros : 'No especificado',
            tipoEntidad: institucion?.tipoOrganizacion?.nombre || 'No especificado'
          };
        });

        this.tablaEvaluacionComponent.exportXLSX(listaEvaluacionesSeguimiento);
      },
      error: (error: any) => {
        console.log(error);
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  crearActividadOcupacional() {
    this.router.navigate(['actividad-ocupacional/crear-editar'], {
      relativeTo: this.route,
      state: {
        uuid: this.uuid_fp,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  editarActividadOcupacional(conductualDTO: ActividadOcupacionalDTO) {
    this.router.navigate(['actividad-ocupacional/crear-editar'], {
      relativeTo: this.route,
      state: {
        item: conductualDTO,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  visualizarActividadOcupacional(conductualDTO: ActividadOcupacionalDTO) {
    this.router.navigate(['actividad-ocupacional/crear-editar'], {
      relativeTo: this.route,
      state: { 
        conductualDTO: { ...conductualDTO, esVisualizacion: true },
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  handlePageEventActividad(pageEvent: PageEvent) {
    this.paginacionActividadOcupacional.pageSize = pageEvent.pageSize;
    this.paginacionActividadOcupacional.pageIndex = pageEvent.pageIndex;

    this.obtenerActividadesOcupacionales();
  }

  visualizarEvaluacionSeguimiento(evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO) {
    this.router.navigate(['evaluacion-seguimiento-social/crear-editar'], {
      relativeTo: this.route,
      state: { 
        evaluacionSeguimientoDTO: { ...evaluacionSeguimientoDTO, esVisualizacion: true },
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  editarEvaluacionSeguimiento(evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO) {
    this.router.navigate(['evaluacion-seguimiento-social/crear-editar'], {
      relativeTo: this.route,
      state: { 
        evaluacionSeguimientoDTO,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  agregarEvaluacionSeguimiento() {
    this.router.navigate(['evaluacion-seguimiento-social/crear-editar'], {
      relativeTo: this.route,
      state: { 
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'conductual' // ✅ Especifica que es evaluación conductual
      }
    });
  }

  eliminarEvaluacionSeguimiento(evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la evaluación educativa/laboral? Esta operación es irreversible",
      "¿Deseas continuar?"
    );

    ref.afterClosed().subscribe({
      next: (resp: "confirmed" | "cancelled") => {
        if (resp == "confirmed") {
          let load = this.dialogMensajeService.mensajeLoading("Eliminando evaluación educativa/laboral...");
          this.evaluacionSeguimientoService.eliminarEvaluacionSeguimiento(evaluacionSeguimientoDTO).subscribe({
            next: (resp: RespuestaPorDefecto<boolean>) => {
              load.close();
              if (!resp.exito) {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al eliminar el registro. Inténtalo de nuevo.'
                );
                return;
              }
              this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
              this.obtenerListaEvaluacionSeguimiento();
            },
            error: (error: any) => {
              load.close();
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al eliminar el registro. Inténtalo de nuevo.'
              );
            }
          });
        }
      }
    });
  }

  handlePageEventSocial(pageEvent: PageEvent) {
    this.paginacionSocial.pageSize = pageEvent.pageSize;
    this.paginacionSocial.pageIndex = pageEvent.pageIndex;
    this.obtenerListaEvaluacionSeguimiento();
  }

  subirDocumento(encabezadoDTO: EncabezadoDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(EvaluacionDocumentoComponent, {
      width: '1200px',
      height: '700px',
      data: { 
        encabezado: encabezadoDTO, 
        nemonicoMenu: this.nemonicoMenuEvaluacionConductual, 
        nemonicoCarpeta: etiquetasModel.CARPETA_CONDUCTUAL 
      }
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
        nemonicoMenu: this.nemonicoMenuEvaluacionConductual
      }
    });
  }

  handleSearchEventActividades(filter: string) {
    this.paginacionRequestActividadOcupacional.filter = filter;

    this.obtenerActividadesOcupacionales();
  }

  handleSearchEventEduCativa(filter: string) {
    // Asegura que el filtro se aplique correctamente
    this.paginacionRequestEvaluacionEducativa.filter = filter;
    // Reinicia la página al aplicar un nuevo filtro
    this.paginacionRequestEvaluacionEducativa.page = 0;
    this.paginacionSocial.pageIndex = 0;

    // Llama al servicio para obtener datos filtrados
    this.obtenerListaEvaluacionSeguimiento();
  }

  async imprimirFicha(item: ActividadOcupacionalDTO) {
    try {
      // Mostrar diálogo de carga
      const dialogoCarga = this.dialogMensajeService.mensajeLoading('Generando PDF...');

      // Obtener imagen del logo para incluir en el PDF
      this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
        .subscribe({
          next: async (datos: ArrayBuffer) => {
            // Convertir imagen a base64 para incluirla en el PDF
            const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
            const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

            try {
              // Obtener datos de cabecera de la ficha de identificación
              const datosCabecera = await this.obtenerDatosCabecera();

              // Crear tabla de personas relacionadas para el PDF utilizando TablaPlantilla
              // Solo incluimos las columnas: Nombre completo, Parentesco, Identificación
              let tablaPersonasRelacionadas = new TablaPlantilla();
              tablaPersonasRelacionadas.encabezados = [
                'Actividad', 'Observaciones', 'Fecha'
              ];

              await this.obtenerSeguimientos(item.tokenIdentificador);

              // Mapear los datos de las personas relacionadas a la tabla con solo las columnas requeridas
              tablaPersonasRelacionadas.filas = this.listaSeguimientoActividadOcupacional.map(persona => {


                return {
                  'Actividad': persona.actividad,
                  'Observaciones': persona.observaciones,
                  'Fecha': this.funcionesUtils.formatearFecha(persona.fechaActividad),
                };
              });

              // Si no hay datos, agregar una fila con mensaje
              if (tablaPersonasRelacionadas.filas.length === 0) {
                tablaPersonasRelacionadas.filas.push({
                  'Actividad': '-',
                  'Observaciones': 'No hay seguimientos registrados',
                  'Fecha': '-'
                });
              }

              // Configurar solicitud para la generación del PDF
              const solicitudPdf = new GeneracionPdfRequest();
              solicitudPdf.nemonico = 'FORMULARIO_ACTIVIDAD_SOCIORECREATIVA';

              // Preparar variables para el PDF
              solicitudPdf.variables = {
                // Datos de cabecera
                ...datosCabecera,
                "[IMG_BASE64]": imagenBase64,
                "[FECHA-INGRESO]": this.funcionesUtils.formatearFecha(new Date()),
                "[HORA-INGRESO]": new Date().toLocaleTimeString('es-ES'),
                "[CENTRO]": this.centro?.nombre || 'No especificado',
                "[TITULO-INFORME]": 'Informe de Seguimiento de Actividades Socio Recreativas',
                "[TITULO-PLANTILLA]": 'Informe de Seguimiento de Actividades Socio Recreativas',


                // Tabla de personas relacionadas (serializada como JSON)
                "[TABLA-SEGUIMIENTO-ACTIVIDADES]": JSON.stringify(tablaPersonasRelacionadas),

                // Información familiar general
                // "[PROGRAMA]": item.programa?.nombre || 'No especificado',
                // "[AMBIENTE]": item.ambiente?.nombre || '',
                //    ...(this.centro?.jerarquiaPadre?.nemonico === 'CJDR' ? {
                //   "[PROGRAMA]": '',
                //   "[AMBIENTE]": ''
                // } : {
                //   "[PROGRAMA]": item.programa?.nombre || 'No especificado',
                //   "[AMBIENTE]": item.ambiente?.nombre || ''
                // }),

                // Condicionales de visibilidad
                "[OCULTAR_PROGRAMA]": this.centro?.jerarquiaPadre?.nemonico === 'UAPISE' ? 'display:none;' : '',
                "[OCULTAR_AMBIENTE]": this.centro?.jerarquiaPadre?.nemonico === 'UAPISE' ? 'display:none;' : '',

                // Información
                "[PROGRAMA]": item.programa?.nombre || 'No especificado',
                "[AMBIENTE]": item.ambiente?.nombre || '',
                "[OBJETIVOS]": item.objetivoActividad || 'No especificado',
                "[DOCUMENTO-APROBACION]": item.documentoAprobacion || 'No especificado',
                "[NUMERO-DOCUMENTO]": item.numeroDocumento || 'No especificado',
                "[ESTADO-ACTIVIDAD]": item.estadoActividadOcupacional?.nombre || 'No especificado',
                "[FECHA-INICIO]": this.funcionesUtils.formatearFecha(item.fechaInicio) || 'No especificado',

              };

              // Generar el PDF usando el servicio
              this.pdfService.generarPdf(solicitudPdf, this.nemonicoMenuEvaluacionEducativaLaboral).subscribe({
                next: (respuesta: RespuestaPorDefecto<string>) => {
                  dialogoCarga.close();

                  if (!respuesta.exito) {
                    this.dialogMensajeService.mensajeError(
                      'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                    );
                    return;
                  }

                  // Abrir el PDF en una nueva ventana
                  const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
                  window.open(url);
                },
                error: (error: any) => {
                  dialogoCarga.close();
                  console.error('Error al generar PDF:', error);
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                  );
                }
              });
            } catch (error) {
              dialogoCarga.close();
              console.error('Error al procesar datos:', error);
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al procesar los datos. Inténtalo de nuevo.'
              );
            }
          },
          error: (error) => {
            dialogoCarga.close();
            console.error('Error al cargar imagen:', error);
            this.dialogMensajeService.mensajeError(
              'Error al cargar la imagen del logo.'
            );
          }
        });
    } catch (error) {
      console.error('Error al imprimir ficha:', error);
      this.dialogMensajeService.mensajeError(
        'Hubo un problema al generar el PDF.'
      );
    }
  }

  /**
   * Imprime la evaluación y seguimiento educativo/laboral
   * @param evaluacionSeguimiento Evaluación a imprimir
   */
  async imprimirEvaluacionSeguimiento(evaluacionSeguimiento: EvaluacionSeguimientoEducativoLaboralDTO) {
    try {
      // 1. Mostrar diálogo de confirmación
      const refDialogo = this.dialogMensajeService.mensajeConConfirmacion(
        "¿Está seguro de imprimir la evaluación y seguimiento educativo/laboral?",
        "¿Desea continuar?"
      );

      refDialogo.afterClosed().subscribe({
        next: async (respuesta: "confirmed" | "cancelled") => {
          if (respuesta == "confirmed") {
            // 2. Mostrar diálogo de carga
            const dialogoCarga = this.dialogMensajeService.mensajeLoading("Preparando la impresión...");

            try {
              // 3. Cargar la imagen como base64
              const datos = await this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' }).toPromise();
              const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
              const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

              // 4. Obtener datos de la ficha de identificación
              const respuestaFicha = await this.fichaIdentificacionService
                .obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenuEvaluacionEducativaLaboral)
                .toPromise();

              if (!respuestaFicha.exito) {
                dialogoCarga.close();
                this.dialogMensajeService.mensajeError('Error al obtener la ficha de identificación');
                return;
              }

              const fichaIdentificacion = respuestaFicha.data;
              const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
              const edadActual = fichaIdentificacion.fechaNacimiento ? this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
              const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

              // 5. Obtener recomendaciones y comentarios
              let paginacionRequest = new PaginacionRequest();
              paginacionRequest.tokenIdentificador = evaluacionSeguimiento.tokenIdentificador;
              paginacionRequest.page = 0;
              paginacionRequest.size = 100;

              const respuestaRecomendaciones = await this.evaluacionSeguimientoService
                .obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(paginacionRequest)
                .toPromise();

              let listaRecomendaciones = [];
              if (respuestaRecomendaciones.exito) {
                listaRecomendaciones = respuestaRecomendaciones.data.data;
              }

              // 6. Crear tabla de recomendaciones
              let tablaRecomendaciones = new TablaPlantilla();
              tablaRecomendaciones.encabezados = ['Fecha', 'Comentario'];

              if (listaRecomendaciones && listaRecomendaciones.length > 0) {
                tablaRecomendaciones.filas = listaRecomendaciones.map(recomendacion => ({
                  'Fecha': recomendacion.fecha ? this.funcionesUtils.formatearFecha(recomendacion.fecha) : 'No especificado',
                  'Comentario': recomendacion.comentario || 'No especificado'
                }));
              } else {
                tablaRecomendaciones.filas = [{
                  'Fecha': '',
                  'Comentario': 'No hay recomendaciones registradas'
                }];
              }

              // 7. Obtener información de institución
              let nombreInstitucion = evaluacionSeguimiento.nombreInstitucionOtros || '';
              let tipoEntidad = 'No especificado';

              if (evaluacionSeguimiento.tokenIdentificadorInstitucion &&
                evaluacionSeguimiento.tokenIdentificadorInstitucion !== '0' &&
                evaluacionSeguimiento.tokenIdentificadorInstitucion !== '1') {

                const institucion = this.listaInstituciones.find(
                  inst => inst.tokenIdentificador === evaluacionSeguimiento.tokenIdentificadorInstitucion
                );

                if (institucion) {
                  nombreInstitucion = institucion.nombreOrganizacion || '';
                  tipoEntidad = institucion.tipoOrganizacion?.nombre || '';
                }
              }

              // 8. Obtener tipo de evaluación y medio de verificación
              const respuestaTipos = await this.funcionesUtils.obtenerListaCatalogo('TIPOS_EVALUACION_SEGUIMIENTO', this.nemonicoMenuEvaluacionEducativaLaboral).toPromise();
              const respuestaMedios = await this.funcionesUtils.obtenerListaCatalogo('MEDIOS_VERIFICACION', this.nemonicoMenuEvaluacionEducativaLaboral).toPromise();

              const tipoEvaluacion = respuestaTipos.find(
                tipo => tipo.tokenIdentificador === evaluacionSeguimiento.tokenIdentificadorTipoEvaluacionSeguimiento
              )?.nombre || 'No especificado';

              const medioVerificacion = respuestaMedios.find(
                medio => medio.tokenIdentificador === evaluacionSeguimiento.tokenIdentificadorMedioVerificacion
              )?.nombre || 'No especificado';

              // 9. Crear la solicitud para generar el PDF
              let solicitudPdf = new GeneracionPdfRequest();
              solicitudPdf.nemonico = 'FORMULARIO_EVALUACION_SEGUIMIENTO_EDUCATIVO_LABORAL';

              // 10. Configurar las variables para el PDF
              solicitudPdf.variables = {
                "[IMG_BASE64]": imagenBase64,
                "[FECHA_REGISTRO]": this.funcionesUtils.formatearFecha(new Date()),
                "[HORA_REGISTRO]": new Date().toLocaleTimeString('es-ES'),
                "[CENTRO]": this.centro?.nombre || fichaIdentificacion.centroIngreso || '',
                "[NOMBRES-APELLIDOS]": nombreCompleto,
                "[DNI]": fichaIdentificacion.numeroDocumento || '',
                "[LUGAR-FECHA-NACIMIENTO]": lugarFechaNacimiento,
                "[EDAD]": edadActual,
                "[TIPO_EVALUACION]": tipoEvaluacion,
                "[MEDIO_VERIFICACION]": medioVerificacion,
                "[INSTITUCION]": nombreInstitucion,
                "[TIPO_ENTIDAD]": tipoEntidad,
                "[FECHA_INICIO]": evaluacionSeguimiento.fechaInicio ? this.funcionesUtils.formatearFecha(evaluacionSeguimiento.fechaInicio) : 'No especificado',
                "[FECHA_FIN]": evaluacionSeguimiento.fechaFin ? this.funcionesUtils.formatearFecha(evaluacionSeguimiento.fechaFin) : 'No especificado',
                "[RESULTADO_SEGUIMIENTO]": evaluacionSeguimiento.resultadoSeguimiento || 'No especificado',
                "[TABLA_RECOMENDACIONES]": JSON.stringify(tablaRecomendaciones)
              };

              // 11. Generar y mostrar el PDF
              this.pdfService.generarPdf(solicitudPdf, this.nemonicoMenuEvaluacionEducativaLaboral).subscribe({
                next: (respuesta: RespuestaPorDefecto<string>) => {
                  dialogoCarga.close();

                  if (!respuesta.exito) {
                    console.error('Error al generar PDF:', respuesta);
                    this.dialogMensajeService.mensajeError(
                      'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                    );
                    return;
                  }

                  // Abrir el PDF en una nueva ventana
                  const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
                  window.open(url);
                },
                error: (error: any) => {
                  dialogoCarga.close();
                  console.error('Error al generar PDF:', error);
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                  );
                }
              });
            } catch (error) {
              dialogoCarga.close();
              console.error('Error al procesar datos:', error);
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al procesar los datos. Inténtalo de nuevo.'
              );
            }
          }
        }
      });
    } catch (error) {
      console.error('Error al iniciar impresión:', error);
      this.dialogMensajeService.mensajeError(
        'Hubo un problema al iniciar el proceso de impresión.'
      );
    }
  }

  async obtenerSeguimientos(tokenIdentificador: string): Promise<void> {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = 100;
    paginacionRequest.page = 0;
    paginacionRequest.tokenIdentificador = tokenIdentificador;

    return new Promise<void>((resolve, reject) => {
      this.evaluacionConductualService
        .obtenerSeguimientosPorActividadOcupacional(paginacionRequest)
        .subscribe({
          next: (response) => {
            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(
                response.titulo,
                response.mensaje
              );
              return reject('Error en la respuesta del servicio');
            }

            this.listaSeguimientoActividadOcupacional = response.data.data;
            resolve(); // ✅ resuelve cuando termina de obtener datos
          },
          error: (error) => {
            console.log(error);
            reject(error); // ❌ rechaza si hay error
          },
        });
    });
  }


  cargarCentro() {
    this.jerarquiaService
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenuEvaluacionConductual)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!environment.production) {
            console.log(respuesta.data);
          }
          if (!respuesta.exito) {
            this.jerarquiaService.checkError(respuesta);
            return;
          }

          this.centro = respuesta.data;
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
  }

  private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
    return new Promise((resolve, reject) => {
      this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenuEvaluacionEducativaLaboral).subscribe({
        next: async (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            reject('Error al obtener la ficha de identificación');
            return;
          }

          const fichaIdentificacion = response.data;

          var datosCabeceraCatalogo = null;

          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


          const datosCabecera = {
            "[ADOLESCENTE]": nombreAdolescente,
            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
          };

          resolve(datosCabecera);
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }
}