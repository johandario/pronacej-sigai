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
import { EvaluacionSeguimientoEducativoLaboralDTO } from 'app/core/model/both/evaluacionSeguimientoEducativoLaboralDTO.model';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { InstitucionService } from 'app/modules/institucion/institucion.service';
import { EvaluacionSeguimientoEducativoLaboralService } from 'app/modules/seguridad/services/evaluacionSeguimiento.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-eval-segu-educ-labo',
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
  templateUrl: './eval-segu-educ-labo.component.html',
  styleUrl: './eval-segu-educ-labo.component.scss'
})
export class EvalSeguEducLaboComponent implements OnInit {
  uuid_fp: string;
  pagina = 0;
  tamanosPagina = [5, 10, 15, 20];
  tamano = this.tamanosPagina[0];
  totalElementos = 0;

  tituloPantalla = "evaluación educativa/laboral";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_EVALUACION_EDUCATIVA_LABORAL;

  listaInstituciones: RegistroInstitucionDTO[] = [];
  listaEvaluacionesSeguimiento: EvaluacionSeguimientoEducativoLaboralDTO[] = [];
  dataSource: CdkTableDataSourceInput<EvaluacionSeguimientoEducativoLaboralDTO & { nombreInstitucion?: string, tipoEntidad?: string }>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombreInstitucion: "Institución",
    tipoEntidad: "Tipo de entidad",
    fechaRegistro: "Fecha registro",
    usuarioRegistro: "Usuario que registró",
  };

  constructor(
    private evaluacionSeguimientoService: EvaluacionSeguimientoEducativoLaboralService,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private institucionService: InstitucionService,
    public funcionesUtils: FuncionesUtils,
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.cargarInstituciones();
    this.obtenerListaEvaluacionSeguimiento();
  }

  obtenerClaves() {
    return Object.keys(this.keyLabelsTable);
  }

  visualizarEvaluacionSeguimiento(evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO) {
    evaluacionSeguimientoDTO.esVisualizacion = true;
    this.router.navigate(['crear-editar'], {
      state: { evaluacionSeguimientoDTO },
      relativeTo: this.route
    });
  }

  editarEvaluacionSeguimiento(evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO) {
    this.router.navigate(['crear-editar'], {
      state: { evaluacionSeguimientoDTO },
      relativeTo: this.route
    });
  }

  eliminarEvaluacionSeguimiento(evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la evaluación y seguimiento? Esta operación es irreversible",
      "¿Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let cargando = this.dialogMensajeService.mensajeLoading("Eliminando evaluación y seguimiento...");
            this.evaluacionSeguimientoService.eliminarEvaluacionSeguimiento(evaluacionSeguimientoDTO).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  cargando.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerListaEvaluacionSeguimiento();
                },
                error: (error: any) => {
                  cargando.close();
                  this.evaluacionSeguimientoService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  agregarEvaluacionSeguimiento() {
    this.router.navigate(['crear-editar'], { relativeTo: this.route });
  }

  cargarInstituciones() {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100;
    solicitudPaginacion.page = 0;

    this.institucionService.obtenerRegistroInstituciones(solicitudPaginacion, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
        if (respuesta.exito) {
          this.listaInstituciones = respuesta.data.data;
          if (this.listaEvaluacionesSeguimiento.length > 0) {
            this.actualizarFuenteDeDatos();
          }
        }
      },
      error: (error: any) => {
        console.error('Error cargando instituciones:', error);
      }
    });
  }

  obtenerListaEvaluacionSeguimiento() {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.tamano;
    solicitudPaginacion.page = this.pagina;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    this.evaluacionSeguimientoService.obtenerEvaluacionesSeguimientoPaginado(
      solicitudPaginacion,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>>) => {
        if (!environment.production) {
          console.log(respuesta);
        }

        if (!respuesta.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        this.listaEvaluacionesSeguimiento = respuesta.data.data;
        this.totalElementos = respuesta.data.totalItems;

        if (this.listaInstituciones.length > 0) {
          this.actualizarFuenteDeDatos();
        }
      },
      error: (error: any) => {
        console.log(error);
      }
    });
  }

  private actualizarFuenteDeDatos() {
    const datosConNombres = this.listaEvaluacionesSeguimiento.map(evaluacion => {
      const institucion = this.listaInstituciones.find(
        inst => inst.tokenIdentificador === evaluacion.tokenIdentificadorInstitucion
      );

      return {
        ...evaluacion,
        nombreInstitucion: institucion?.nombreOrganizacion || 'No especificado',
        tipoEntidad: institucion?.tipoOrganizacion?.nombre || 'No especificado'
      };
    });

    this.dataSource = datosConNombres;
  }

  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.tamano = eventoPaginacion.pageSize;
    this.pagina = eventoPaginacion.pageIndex;
    this.obtenerListaEvaluacionSeguimiento();
  }
}