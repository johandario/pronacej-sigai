import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { SeguimientoService } from 'app/modules/administracion/services/seguimiento.service';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { MatDialog } from '@angular/material/dialog';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';
import { EncuestaPdfService } from 'app/modules/general/services/encuestaPdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-evaluacion-psicologica-ver',
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
    SubidaDeDocumentosComponent,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './evaluacion-psicologica-ver.component.html',
  styleUrl: './evaluacion-psicologica-ver.component.scss'
})
export class EvaluacionPsicologicaVerComponent implements OnInit {

  nemonicoMenuEvaluacionPsicologica = etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOLOGICA;
  nemonicoMenuPruebasPsicologicas = etiquetasModel.NEMONICO_MENU_PRUEBAS_PSICOLOGICAS;
  nemonicoEncuesta: string;
  nemonicoCentro: string;

  uuid_fp: string;

  selectedTabIndex = 0;

  tiposDeDocumentosSistema: TipoDeDocumento[] = [];

  listaEvaluaciones: EncabezadoDTO[] = [];
  listaPruebas: EncabezadoDTO[] = [];
  tokenEncuesta: string;
  fichaIngreso: FichaIngresoDTO;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tablaEvaluaciones') tablaEvaluacionesComponent: TablaListaComponent<any>;
  @ViewChild('tablaPruebas') tablaPruebasComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    fechaCompletacion: "Fecha de finalización",
    nombre: "Nombre",
    descripcion: "Descripción"
  };

  paginacionPruebas: Paginacion = new Paginacion();
  paginacionRequestPruebas: PaginacionRequest = new PaginacionRequest();

  @ViewChild('documentosComp')
  tablaDocumentos: DocumentosSubidosTablaComponent;

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private encuestaService: EncuestaService,
    private fichaIngresoService: FichaIngresoService,
    private encuestaPdfService: EncuestaPdfService,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_EVALUACIONES_PSICOLOGICAS"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    const savedIndex = sessionStorage.getItem('selectedPsicologicoTabIndex');
    if (savedIndex !== null) {
      this.selectedTabIndex = +savedIndex;
    }

    this.obtenerFichaIngresoValida();
  }

  onTabChange(event: MatTabChangeEvent) {
    this.selectedTabIndex = event.index;
    sessionStorage.setItem('selectedPsicologicoTabIndex', event.index.toString());
  }

  /**
   * Obtiene el nemónico de menú correcto basado en el tab activo
   * @returns string - El nemónico de menú correspondiente
   */
  private obtenerNemonicoMenuActual(): string {
    return this.selectedTabIndex === 0 
      ? this.nemonicoMenuEvaluacionPsicologica 
      : this.nemonicoMenuPruebasPsicologicas;
  }

  /**
   * Obtiene el tipo de evaluación actual basado en el tab activo
   * @returns string - El tipo de evaluación
   */
  private obtenerTipoEvaluacionActual(): string {
    return this.selectedTabIndex === 0 ? 'psicologica' : 'prueba';
  }

  /**
   * Obtiene la carpeta correspondiente basada en el tab activo
   * @returns string - El nemónico de la carpeta
   */
  private obtenerCarpetaActual(): string {
    return etiquetasModel.CARPETA_PSICOLOGICO; // Ambos usan la misma carpeta
  }

  subirDocumento(encabezadoDTO: EncabezadoDTO) {
    const nemonicoMenuActual = this.obtenerNemonicoMenuActual();
    const carpetaActual = this.obtenerCarpetaActual();
    
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(EvaluacionDocumentoComponent, {
      width: '1200px',
      height: '700px',
      data: { 
        encabezado: encabezadoDTO, 
        nemonicoMenu: nemonicoMenuActual, 
        nemonicoCarpeta: carpetaActual 
      }
    });
  }

  verDocumentos(encabezadoDTO: EncabezadoDTO) {
    const nemonicoMenuActual = this.obtenerNemonicoMenuActual();
    
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(PopupDocumentosComponent, {
      width: '1000px',
      height: '500px',
      data: {
        tokenItem: encabezadoDTO.tokenIdentificador,
        tipoServicio: "EVALUACIONES",
        nemonicoMenu: nemonicoMenuActual
      }
    });
  }

  async imprimirEvaluacion(encabezado: EncabezadoDTO) {
    const nemonicoMenuActual = this.obtenerNemonicoMenuActual();

    let encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.tokenIdentificador = encabezado.tokenIdentificador

    let encuesta = new EncuestaDTO();

    let load = this.dialogMensajeService.mensajeLoading("Enviando evaluación...");
    this.encuestaService.obtenerEvaluacionPorTokenEncabezado(encabezadoDTO, nemonicoMenuActual).subscribe(
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

  //#region Evaluaciones
  obtenerEvaluaciones() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(this.paginacionRequest, this.nemonicoEncuesta, this.nemonicoMenuEvaluacionPsicologica).subscribe(
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

  descargarExcelCompletoEvaluaciones() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(this.paginacionRequest, this.nemonicoEncuesta, this.nemonicoMenuEvaluacionPsicologica).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaEvaluacionesComponent.exportXLSX(response.data.data);

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
    this.router.navigate(['evaluacion/crear-editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        completada: encabezadoDTO.completada,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'psicologica' // Especifica que es evaluación psicológica
      }
    });
  }

  agregarEvaluacion() {
    this.router.navigate(['evaluacion/crear-editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncuesta: this.tokenEncuesta,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'psicologica' // Especifica que es evaluación psicológica
      }
    });
  }

  eliminarEvaluacion(encabezadoDTO: EncabezadoDTO) {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la evaluación psicológica? También se eliminará los seguimientos que se hayan realizado sobre esta. Esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la evaluación psicológica..");
            this.encuestaService.eliminarEvaluacion(encabezadoDTO, this.nemonicoMenuEvaluacionPsicologica).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar la evaluación psicológica. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerEvaluaciones();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al guardar la evaluación psicológica. Inténtalo de nuevo.'
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

  //#region Pruebas Psicológicas
  obtenerPruebas() {
    this.paginacionRequestPruebas.size = this.paginacionPruebas.pageSize;
    this.paginacionRequestPruebas.page = this.paginacionPruebas.pageIndex;
    this.paginacionRequestPruebas.tokenIdentificador = this.uuid_fp;

    const listaCategorias = [etiquetasModel.CATEGORIA_PRUEBA_PSICOLOGICA]

    this.encuestaService.obtenerEvaluacionesPorNemonicoCategoria(this.paginacionRequestPruebas, this.nemonicoCentro, listaCategorias, this.nemonicoMenuPruebasPsicologicas).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaPruebas = response.data.data;
          this.paginacionPruebas.totalItems = response.data.totalItems;

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  descargarExcelCompletoPruebas() {
    this.paginacionRequestPruebas.size = 100000;
    this.paginacionRequestPruebas.page = 0;
    this.paginacionRequestPruebas.tokenIdentificador = this.uuid_fp;

    const listaCategorias = [etiquetasModel.CATEGORIA_PRUEBA_PSICOLOGICA]

    this.encuestaService.obtenerEvaluacionesPorNemonicoCategoria(this.paginacionRequestPruebas, this.nemonicoCentro, listaCategorias, this.nemonicoMenuPruebasPsicologicas).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaPruebasComponent.exportXLSX(response.data.data);

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  agregarPrueba() {
    this.router.navigate(['prueba/crear'], {
      relativeTo: this.route,
      state: {
        tipoEvaluacion: 'prueba' // ✅ Cambiado de 'psicologica' a 'prueba'
      }
    });
  }

  verEditarPrueba(encabezadoDTO: EncabezadoDTO) {
    this.router.navigate(['prueba/editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        completada: encabezadoDTO.completada,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'prueba' // ✅ Cambiado de 'psicologica' a 'prueba'
      }
    });
  }

  eliminarPrueba(encabezadoDTO: EncabezadoDTO) {

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la prueba psicológica? esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la prueba psicológica..");
            this.encuestaService.eliminarEvaluacion(encabezadoDTO, this.nemonicoMenuPruebasPsicologicas).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar la prueba psicológica. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerPruebas();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al eliminar la prueba psicológica. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEventPrueba(pageEvent: PageEvent) {
    this.paginacionPruebas.pageSize = pageEvent.pageSize;
    this.paginacionPruebas.pageIndex = pageEvent.pageIndex;

    this.obtenerPruebas();
  }

  handleSortEventPrueba(event: Sort) {
    if (event.direction) {
      this.paginacionRequestPruebas.sort = event.active;
      this.paginacionRequestPruebas.direction = event.direction;
    }
    else {
      this.paginacionRequestPruebas.sort = null;
      this.paginacionRequestPruebas.direction = null;
    }

    this.obtenerPruebas();
  }

  handleSearchEventPrueba(filter: string) {
    this.paginacionRequestPruebas.filter = filter;

    this.obtenerPruebas();
  }
  //#endregion

  realizarSeguimiento(encabezadoDTO: EncabezadoDTO) {
    const tipoEvaluacionActual = this.obtenerTipoEvaluacionActual();
    
    this.router.navigate(['seguimiento'], {
      relativeTo: this.route,
      state: {
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        centro: this.fichaIngreso.centro,
        tipoEvaluacion: tipoEvaluacionActual // Usar el tipo correcto basado en el tab activo
      }
    });
  }

  cargarTokenEncuesta() {
    let encuestaDTO = new EncuestaDTO();

    this.encuestaService.obtenerEncuestas(encuestaDTO, this.nemonicoMenuEvaluacionPsicologica).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO[]>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          var encuestaPsicologica = response.data.find(x => x.nemonico == this.nemonicoEncuesta);

          if (!encuestaPsicologica) {
            this.dialogMensajeService.mensajeError(
              'La evaluación psicológica no se encuentra configurada. Por favor contacte a su administrador.'
            );
            return;
          }

          this.tokenEncuesta = encuestaPsicologica.tokenIdentificador;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  obtenerFichaIngresoValida() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = this.uuid_fp;

    return this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIngresoDTO>) => {

          if (!response.exito) {
            console.log(response);
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar la ficha de ingreso. Inténtalo de nuevo.'
            );
            return;
          }
          this.fichaIngreso = response.data;

          if (response.data?.centro.nombre.includes('CJDR')) {
            this.nemonicoEncuesta = etiquetasModel.ENCUESTA_EVALUACION_PSICOLOGICA_CJDR;
            this.nemonicoCentro = etiquetasModel.TIPO_CENTRO_CJDR;
          } else if (response.data?.centro.nombre.includes('SOA')) {
            this.nemonicoEncuesta = etiquetasModel.ENCUESTA_EVALUACION_PSICOLOGICA_SOA;
            this.nemonicoCentro = etiquetasModel.TIPO_CENTRO_SOA;
          } else if (response.data?.centro.nombre.includes('UAPISE')) {
            this.nemonicoEncuesta = etiquetasModel.ENCUESTA_EVALUACION_PSICOLOGICA_UAPISE;
            this.nemonicoCentro = etiquetasModel.TIPO_CENTRO_UAPISE;
          }

          this.cargarTokenEncuesta();
          this.obtenerEvaluaciones();
          this.obtenerPruebas();
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar la ficha de ingreso. Inténtalo de nuevo.'
          );
        }
      });
  }
}