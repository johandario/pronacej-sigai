import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatDialog } from '@angular/material/dialog';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { EvaluacionConductualDTO } from 'app/core/model/both/evaluacionConductualDTO.model';
import { SituPersCaraPersDTO } from 'app/core/model/both/situPersCaraPersDTO.model';
import { CondHistViolDTO } from 'app/core/model/both/condHistViolDTO.model';
import { MdRegiSituComponent } from './md-regi-situ/md-regi-situ.component';
import { MdRegiCondComponent } from './md-regi-cond/md-regi-cond.component';
import { EvaluacionConductualService } from 'app/modules/seguridad/services/evaluacionConductual.service';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';

@Component({
  selector: 'app-administrar-ec',
  standalone: true,
  imports: [
    MatButtonModule,
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule,
  ],
  templateUrl: './administrar-ec.component.html',
  styleUrl: './administrar-ec.component.scss'
})
export class AdministrarEcComponent implements OnInit {

  uuid_fp: string;
  uuid_ec: string;

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  evaluacionConductualDTO: EvaluacionConductualDTO;

  listaSituPersCaraPers: SituPersCaraPersDTO[] = [];
  listaCondHistViol: CondHistViolDTO[] = [];
  situPersCaraPersDS: MatTableDataSource<SituPersCaraPersDTO>;
  condHistViolDS: MatTableDataSource<CondHistViolDTO>;

  tituloPantalla = "Evaluación Conductual";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_EVALUACION_CONDUCTUAL;
  esEdicion = false;
  esVisualizacion = false;

  columnasSituPersCaraPers: string[] = [
    'criterio',
    'comentario',
  ];

  columnasConcdHistViol: string[] = [
    'criterio',
    'comentario',
  ];

  @ViewChild('situPersCaraPersPag') situPersCaraPersPag: MatPaginator;
  @ViewChild('condHistViolPag') condHistViolPag: MatPaginator;

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private evaluacionConductualService: EvaluacionConductualService,
    private router: Router,
    private route: ActivatedRoute,
    public matDialog: MatDialog,
    public funcionesUtils: FuncionesUtils,
    public pdfService: PdfService,
  ) { } 


  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.evaluacionConductualDTO = history.state.evaluacionConductualDTO;
    if (this.evaluacionConductualDTO) {
      this.esVisualizacion = this.evaluacionConductualDTO.esVisualizacion;
      this.actualizarColumnas();
      

      this.empezarEdicion(this.evaluacionConductualDTO);
      this.obtenerSituPersCaraPers();
      this.obtenerCondHistViol();
    }
  }

  private actualizarColumnas(): void {
    const columnasBase = [
      'criterio',
      'comentario',
    ];
    
    if (!this.esVisualizacion) {
        this.columnasSituPersCaraPers = ['acciones', ...columnasBase];
        this.columnasConcdHistViol = ['acciones', ...columnasBase];
    } else {
        this.columnasSituPersCaraPers = [...columnasBase];
        this.columnasConcdHistViol = [...columnasBase];
    }
  }

  agregarFilaSituPersCaraPers() {
    const dialogRef = this.matDialog.open(MdRegiSituComponent, {
      data: {
      },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaSituPersCaraPers.unshift(resultado);
        this.situPersCaraPersDS = new MatTableDataSource(this.listaSituPersCaraPers);
        this.situPersCaraPersDS.paginator = this.situPersCaraPersPag;
      }
    });
  }

  agregarFilaCondHistViol() {
    const dialogRef = this.matDialog.open(MdRegiCondComponent, {
      data: { 
      },
      width: '600px'
    }); 
  
    dialogRef.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaCondHistViol.unshift(resultado);
        this.condHistViolDS = new MatTableDataSource(this.listaCondHistViol);
        this.condHistViolDS.paginator = this.condHistViolPag;
      }
    });
  }

  eliminarFilaSituPersCaraPers(indice: number) {
    const elementoEliminar = this.listaSituPersCaraPers[indice];

    if (elementoEliminar.tokenIdentificador === "0") {
      this.listaSituPersCaraPers.splice(indice, 1);
      this.situPersCaraPersDS = new MatTableDataSource(this.listaSituPersCaraPers);
      this.situPersCaraPersDS.paginator = this.situPersCaraPersPag;
    }else {
      this.eliminarSituPersCaraPers(elementoEliminar);
    }
  }

  eliminarFilaCondHistViol(indice: number) {
    const elementoEliminar = this.listaCondHistViol[indice];
    if (elementoEliminar.tokenIdentificador === "0") {
      this.listaCondHistViol.splice(indice, 1);
      this.condHistViolDS = new MatTableDataSource(this.listaCondHistViol);
      this.condHistViolDS.paginator = this.condHistViolPag;
    }else {
      this.eliminarCondHistViol(elementoEliminar);
    }
  }

  obtenerSituPersCaraPers() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.uuid_ec;

    this.evaluacionConductualService.obtenerSituPersCaraPersPaginado(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SituPersCaraPersDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }
          this.listaSituPersCaraPers = response.data.data;
          this.situPersCaraPersDS = new MatTableDataSource(this.listaSituPersCaraPers);
          this.situPersCaraPersDS.paginator = this.situPersCaraPersPag;
        },
        error: (error: any) => {
          console.log(error);
        },
    });
  }

  obtenerCondHistViol() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.uuid_ec;

    this.evaluacionConductualService.obtenerCondHistViolPaginado(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<CondHistViolDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }
          this.listaCondHistViol = response.data.data;
          this.condHistViolDS = new MatTableDataSource(this.listaCondHistViol);
          this.condHistViolDS.paginator = this.condHistViolPag;
        },
        error: (error: any) => {
          console.log(error);
        },
    });
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.evaluacionConductualDTO = null;

    this.router.navigate(['../'], { relativeTo: this.route });
  }

  eliminarSituPersCaraPers(situPersCaraPersDTO: SituPersCaraPersDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + situPersCaraPersDTO.tokenIdentificador + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la situación personal..");
            this.evaluacionConductualService.eliminarSituPersCaraPers(situPersCaraPersDTO).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerSituPersCaraPers();
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

  eliminarCondHistViol(condHistViolDTO: CondHistViolDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + condHistViolDTO.tokenIdentificador + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la conducta e historia de violencia..");
            this.evaluacionConductualService.eliminarCondHistViol(condHistViolDTO).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerCondHistViol();
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

  empezarEdicion(evaluacionConductualEditar: EvaluacionConductualDTO) {
    this.esEdicion = true;
    this.evaluacionConductualDTO = evaluacionConductualEditar;
    this.uuid_ec = evaluacionConductualEditar.tokenIdentificador;
  }
  
  crearActualizar() {
    let evaluacionConductual = new EvaluacionConductualDTO();
    evaluacionConductual.listaSituPersCaraPers = this.listaSituPersCaraPers;
    evaluacionConductual.listaCondHistViolDTO = this.listaCondHistViol;

    evaluacionConductual.tokenIdentificadorFichaIdentificacion = this.uuid_fp;
    evaluacionConductual.tokenIdentificador = this.evaluacionConductualDTO?.tokenIdentificador;
    evaluacionConductual.esEdicion = this.esEdicion;

    this.evaluacionConductualService.crearEvaluacionConductual(evaluacionConductual, etiquetasModel.NEMONICO_MENU_EVALUACION_CONDUCTUAL).subscribe(
      {
        next: (response: RespuestaPorDefecto<EvaluacionConductualDTO>) => {
          if (!response.exito) {
            this.evaluacionConductualService.checkError(response);
            return;
          }
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: (error: any) => {
          this.evaluacionConductualService.checkError(error);
        }
      }
    );

  }

  imprimirFicha() {
    let request = new GeneracionPdfRequest();
    request.nemonico = this.nemonicoMenu;
    request.variables = {
      "[TABLA-SITUACION-PERSONAL]": 'Aqui debería ir la tabla de situación personal',
      "[TABLA-CONDUCTA]": 'Aqui debería ir la tabla de conducta',
    }
    this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<string>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        console.log(response);

        const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

        const pwa = window.open(url);
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

}
