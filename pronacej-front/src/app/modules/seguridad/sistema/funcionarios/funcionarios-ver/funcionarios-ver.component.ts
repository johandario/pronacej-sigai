import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { environment } from 'environments/environment';
import moment from 'moment';

@Component({
  selector: 'app-funcionarios-ver',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    TablaListaComponent,
  ],
  templateUrl: './funcionarios-ver.component.html',
  styleUrl: './funcionarios-ver.component.scss'
})
export class FuncionariosVerComponent {

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FUNCIONARIO;

  listaDeFuncionarios: FuncionarioDTO[] = [];
  dataSource: CdkTableDataSourceInput<FuncionarioDTO>;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;
  @Output() editarFuncionarioEvent = new EventEmitter<FuncionarioDTO>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombres: "Nombres",
    apellidos: "Apellidos",
    email: "Email",
    numeroDeCelular: "No. de celular",
    numeroDeDocumento: "No. documento",
    cargo: "Cargo",
    departamento: "Departamento",
    fechaCreacion: "Fecha de creación"
  };

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private funcionarioService: FuncionarioService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.obtenerFuncionarios();
  }

  getLocalDate(date: Date) {
    return moment(date).format("DD-MM-YYYY HH:mm:ss");
  }


  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(funcionarioDTO: FuncionarioDTO) {
    let action = (funcionarioDTO.bloqueadoRelacion ? "Desbloquear" : "Bloquear");
    let ref = this.accionesSheet.open(AccionesUsuarioComponent,
      {
        data: {
          mostrar: true,
          textAccion: action,
          keyAccion: action
        }
      }
    );

    ref.afterDismissed().subscribe(
      {
        next: (result: "editar" | "eliminar" | "Desbloquear" | "Bloquear") => {
          if (result == "editar") {
            this.editarFuncionario(funcionarioDTO);

          } else if (result == "eliminar") {
            this.eliminarFuncionario(funcionarioDTO);
          }
          else if (result == "Desbloquear" || result == "Bloquear") {
            this.bloquearODesbloquearFuncionario(funcionarioDTO);
          }
        }
      }
    );
  }

  eliminarFuncionario(funcionarioDTO: FuncionarioDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + funcionarioDTO.nombres + "\" esta operación es irreversible",
      "¿Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el funcionario..");
            this.funcionarioService.eliminarFuncionario(funcionarioDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerFuncionarios();
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

  bloquearODesbloquearFuncionario(funcionarioDTO: FuncionarioDTO) {
    let text = (funcionarioDTO.bloqueadoRelacion ? "\"desbloquear\"" : "\"bloquear\"");
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de " + text +
      " a: \"" + funcionarioDTO.nombres + "\".",
      "¿Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let text2 = (funcionarioDTO.bloqueadoRelacion ? "\"Desbloqueando\"" : "\"Bloqueando\"");

            let load = this.dialogMensajeService.mensajeLoading(text2 + " el usuario..");

            funcionarioDTO.bloqueadoRelacion = !funcionarioDTO.bloqueadoRelacion
            // this.funcionarioService.bloquearFuncionario(funcionarioDTO, this.nemonicoMenu).subscribe(
            //   {
            //     next: (resp: RespuestaPorDefecto<boolean>) => {
            //       load.close();
            //       this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

            //       if (!resp.exito) {
            //         this.authSerguridadServicio.checkError(resp);
            //         return;
            //       }

            //       this.obtenerFuncionarios();
            //     },
            //     error: (error: any) => {
            //       load.close();
            //       this.authSerguridadServicio.checkError(error);
            //     }
            //   }
            // );
          }
        }
      }
    );
  }

  obtenerFuncionarios() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.funcionarioService.obtenerFuncionariosValidos(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }

          this.listaDeFuncionarios = response.data.data;
          this.dataSource = this.listaDeFuncionarios;
          this.totalItems = response.data.totalItems;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;

    if (this.paginacionRequest.filter) {
      this.funcionarioService.obtenerFuncionariosPorValor(this.paginacionRequest, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>) => {
            if (!response.exito) {
              this.authSerguridadServicio.checkError(response);
              return;
            }

            this.tablaComponent.exportXLSX(response.data.data);
          },
          error: (error: any) => {
            this.authSerguridadServicio.checkError(error);
          }
        }
      );
    }
    else {
      this.funcionarioService.obtenerFuncionariosValidos(this.paginacionRequest, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>) => {

            if (!response.exito) {
              this.authSerguridadServicio.checkError(response);
              return;
            }

            this.tablaComponent.exportXLSX(response.data.data);
          },
          error: (error: any) => {
            this.authSerguridadServicio.checkError(error);
          }
        }
      );
    }
  }

  obtenerFuncionariosPorValor() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.funcionarioService.obtenerFuncionariosPorValor(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);
            return;
          }

          this.listaDeFuncionarios = response.data.data;
          this.dataSource = this.listaDeFuncionarios;
          this.totalItems = response.data.totalItems;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  agregarFuncionario() {
    this.router.navigate(['/flujos/crear-funcionario-usuario']);
    // this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarFuncionario(funcionarioDTO: FuncionarioDTO) {
    this.router.navigate(['editar'], { state: { item: funcionarioDTO }, relativeTo: this.route });
  }

  verFuncionario(funcionarioDTO: FuncionarioDTO) {
    funcionarioDTO.esVisualizacion = true;
    this.router.navigate(['editar'], { state: { item: funcionarioDTO }, relativeTo: this.route });
  }

  handlePageEvent(event: PageEvent) {
    this.paginacion.pageSize = event.pageSize;
    this.paginacion.pageIndex = event.pageIndex;
    this.obtenerFuncionarios();
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
    this.obtenerFuncionarios();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerFuncionariosPorValor();
  }
}
