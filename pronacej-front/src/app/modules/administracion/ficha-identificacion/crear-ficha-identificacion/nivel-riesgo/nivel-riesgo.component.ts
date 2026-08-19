import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import { EncuestaPdfService } from 'app/modules/general/services/encuestaPdf.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-nivel-riesgo',
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
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './nivel-riesgo.component.html',
  styleUrl: './nivel-riesgo.component.scss',
  providers: [
    { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() },
  ],
})
export class NivelRiesgoComponent implements OnInit {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_NIVEL_RIESGO;
  tituloPantalla: string = "Valoración de Nivel de Riesgo";

  uuid_fp: string;
  tokenEncuesta: string;

  listaNivelRiesgo: EncabezadoDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    fechaCompletacion: "Fecha de finalización",
    nombre: "Nombre",
    descripcion: "Descripción"
  };

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private encuestaService: EncuestaService,
    private encuestaPdfService: EncuestaPdfService,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_VALORACION_DE_NIVEL_DE_RIESGO"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.cargarTokenEncuesta();
    this.obtenerNivelRiesgo();
  }

  cargarTokenEncuesta() {
    let encuestaDTO = new EncuestaDTO();
    encuestaDTO.nemonicoCategoria = etiquetasModel.CATEGORIA_RIESGO;

    this.encuestaService.obtenerEncuestas(encuestaDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO[]>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          // Buscar la primera encuesta de nivel de riesgo disponible
          var encuestaNivelRiesgo = response.data.find(x => x.nemonicoCategoria == etiquetasModel.CATEGORIA_RIESGO);

          if (!encuestaNivelRiesgo) {
            this.dialogMensajeService.mensajeError(
              'La evaluación de nivel de riesgo no se encuentra configurada. Por favor contacte a su administrador.'
            );
            return;
          }

          this.tokenEncuesta = encuestaNivelRiesgo.tokenIdentificador;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  obtenerNivelRiesgo() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorFichaIdentificacion(this.paginacionRequest, etiquetasModel.CATEGORIA_RIESGO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaNivelRiesgo = response.data.data;
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

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.encuestaService.obtenerEvaluacionesPorFichaIdentificacion(this.paginacionRequest, etiquetasModel.CATEGORIA_RIESGO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  eliminarNivelRiesgo(encabezadoDTO: EncabezadoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la evaluación de nivel de riesgo? También se eliminarán los seguimientos que se hayan realizado sobre esta. Esta operación es irreversible",
      "¿Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la evaluación de nivel de riesgo...");
            this.encuestaService.eliminarEvaluacion(encabezadoDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar la evaluación de nivel de riesgo. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerNivelRiesgo();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al eliminar la evaluación de nivel de riesgo. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  verEditarNivelRiesgo(encabezadoDTO: EncabezadoDTO) {
    this.router.navigate(['editar'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncabezado: encabezadoDTO.tokenIdentificador,
        completada: encabezadoDTO.completada,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'riesgo' // ✅ Especifica que es evaluación de nivel de riesgo
      }
    });
  }

  agregarNivelRiesgo() {
    this.router.navigate(['crear'], {
      relativeTo: this.route,
      state: {
        listaPrev: "true",
        tokenEncuesta: this.tokenEncuesta,
        uuid_fp: this.uuid_fp,
        tipoEvaluacion: 'riesgo' // ✅ Especifica que es evaluación de nivel de riesgo
      }
    });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerNivelRiesgo();
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

    this.obtenerNivelRiesgo();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerNivelRiesgo();
  }

  subirDocumento(encabezadoDTO: EncabezadoDTO) {
    // Abrir el popup y pasar la lista de documentos
    const dialogRef = this.dialog.open(EvaluacionDocumentoComponent, {
      width: '1200px',
      height: '700px',
      data: { 
        encabezado: encabezadoDTO, 
        nemonicoMenu: this.nemonicoMenu, 
        nemonicoCarpeta: etiquetasModel.CARPETA_NIVEL_RIESGO 
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
        nemonicoMenu: this.nemonicoMenu
      }
    });
  }

  async imprimirEvaluacion(encabezado: EncabezadoDTO) {
    let encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.tokenIdentificador = encabezado.tokenIdentificador

    let encuesta = new EncuestaDTO();

    let load = this.dialogMensajeService.mensajeLoading("Generando evaluación de nivel de riesgo...");
    this.encuestaService.obtenerEvaluacionPorTokenEncabezado(encabezadoDTO, this.nemonicoMenu).subscribe(
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
}

export function getEspPaginatorIntl() {
  const paginatorIntl = new MatPaginatorIntl();

  paginatorIntl.itemsPerPageLabel = 'Elementos por página:';
  paginatorIntl.firstPageLabel = 'Ir al inicio';
  paginatorIntl.nextPageLabel = 'Siguiente';
  paginatorIntl.previousPageLabel = 'Anterior';
  paginatorIntl.lastPageLabel = 'Ir al final';

  paginatorIntl.getRangeLabel = (page: number, pageSize: number, length: number) => {
    if (length === 0 || pageSize === 0) {
      return `0 / ${length}`;
    }
    length = Math.max(length, 0);
    const startIndex = page * pageSize;
    const endIndex = startIndex < length ? Math.min(startIndex + pageSize, length) : startIndex + pageSize;
    return `${startIndex + 1} - ${endIndex} de ${length}`;
  };

  return paginatorIntl;
}