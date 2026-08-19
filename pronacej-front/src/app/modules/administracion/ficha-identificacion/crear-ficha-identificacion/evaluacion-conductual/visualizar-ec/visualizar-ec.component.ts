import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component, OnInit } from '@angular/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { EvaluacionConductualDTO } from 'app/core/model/both/evaluacionConductualDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TabService } from 'app/core/services/tab.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { EvaluacionConductualService } from 'app/modules/seguridad/services/evaluacionConductual.service';
import { EvaluacionSocialService } from 'app/modules/seguridad/services/evaluacionSocial.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-visualizar-ec',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
  ],
  templateUrl: './visualizar-ec.component.html',
  styleUrl: './visualizar-ec.component.scss'
})
export class VisualizarEcComponent implements OnInit {

  uuid_fp: string;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "Evaluación Conductual";

  listaEvaluacionesConductuales: EvaluacionConductualDTO[] = [];
  dataSource: CdkTableDataSourceInput<EvaluacionConductualDTO>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaRegistro: "Fecha Registro",
    usuarioRegistro: "Usuario que Registró",
  };

  constructor(
    private evaluacionConductualService: EvaluacionConductualService,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    public funcionesUtils: FuncionesUtils,
    private tabService: TabService,
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerEvaluacionesConductuales();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  editarEvaluacionConductual(evaluacionConductualDTO: EvaluacionConductualDTO) {
    this.router.navigate(['crear-editar'], { state: { evaluacionConductualDTO }, relativeTo: this.route });
  }

  visualizarEvaluacionConductual(evaluacionConductualDTO: EvaluacionConductualDTO) {
    evaluacionConductualDTO.esVisualizacion = true;
    this.router.navigate(['crear-editar'], { state: { evaluacionConductualDTO }, relativeTo: this.route });
  }

  eliminarEvaluacionConductual(evaluacionConductualDTO: EvaluacionConductualDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + evaluacionConductualDTO.tokenIdentificador + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la evaluación conductual..");
            this.evaluacionConductualService.eliminarEvaluacionConductual(evaluacionConductualDTO).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerEvaluacionesConductuales();
                },
                error: (error: any) => {
                  load.close();

                  this.evaluacionConductualService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  agregarEvaluacionConductual() {
    this.router.navigate(['crear-editar'], { relativeTo: this.route });
  }

  obtenerEvaluacionesConductuales() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.evaluacionConductualService.obtenerEvaluacionesConductualesPaginado(paginacionRequest, etiquetasModel.NEMONICO_MENU_EVALUACION_CONDUCTUAL).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EvaluacionConductualDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaEvaluacionesConductuales = response.data.data;
          this.dataSource = this.listaEvaluacionesConductuales;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          console.log(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerEvaluacionesConductuales();
  }

}
