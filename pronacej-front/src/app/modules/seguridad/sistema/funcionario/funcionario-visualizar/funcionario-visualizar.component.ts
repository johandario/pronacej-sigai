import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { environment } from 'environments/environment';
import moment from "moment";
import { FuncionarioAccionesComponent } from '../funcionario-acciones/funcionario-acciones.component';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSort, MatSortModule } from '@angular/material/sort';


@Component({
  selector: 'app-funcionario-visualizar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatSortModule 
  ],
  templateUrl: './funcionario-visualizar.component.html',
  styleUrl: './funcionario-visualizar.component.scss'
})
export class FuncionarioVisualizarComponent implements OnInit, OnDestroy {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;

  listaDeFuncionarios: FuncionarioDTO[] = [];
  dataSource: MatTableDataSource<FuncionarioDTO>;

  @Output() editarFuncionarioEvent = new EventEmitter<FuncionarioDTO>();
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;


  busquedaFormControl = new FormControl('');

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombres: "Nombres",
    apellidos: "Apellidos",
    email: "Email",
    numeroDeCelular: "No. de celular",
    numeroDeDocumento: "No. documento",
    telefono: "Teléfono",
    fechaCreacion: "Fecha de creación"
  };

  constructor(
    private funcionarioService: FuncionarioService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet
  ) { 
    
  }

  ngOnInit(): void {
    this.obtenerFuncionarios();
  }

  ngOnDestroy(): void {
    this.busquedaFormControl = new FormControl('');
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(creacionDeFuncionario: FuncionarioDTO) {
    let action = (creacionDeFuncionario.bloqueadoRelacion ? "Desbloquear" : "Bloquear");
    let ref = this.accionesSheet.open(FuncionarioAccionesComponent,
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
            this.editarFuncionarioEvent.emit(creacionDeFuncionario);

          } else if (result == "eliminar") {
            this.eliminarFuncionario(creacionDeFuncionario);
          }
          else if (result == "Desbloquear" || result == "Bloquear") {
            this.bloquearODesbloquearUsuario(creacionDeFuncionario);
          }
        }
      }
    );
  }

  eliminarFuncionario(creacionDeFuncionario: FuncionarioDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + creacionDeFuncionario.nombres + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el usuario..");
            this.funcionarioService.eliminarFuncionario(creacionDeFuncionario, this.nemonicoMenu).subscribe(
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

                  this.funcionarioService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  bloquearODesbloquearUsuario(creacionDeUsuarioSistema: FuncionarioDTO) {
  //   let text = (creacionDeUsuarioSistema.bloqueadoRelacion ? "\"desbloquear\"" : "\"bloquear\"");
  //   let ref = this.dialogMensajeService.mensajeConConfirmacion(
  //     "Estás seguro de " + text +
  //     " a: \"" + creacionDeUsuarioSistema.nombres + "\".",
  //     "Deseas continuar?"
  //   );

  //   ref.afterClosed().subscribe(
  //     {
  //       next: (resp: "confirmed" | "cancelled") => {
  //         if (resp == "confirmed") {
  //           let text2 = (creacionDeUsuarioSistema.bloqueadoRelacion ? "\"Desbloqueando\"" : "\"Bloqueando\"");

  //           let load = this.dialogMensajeService.mensajeLoading(text2 + " el usuario..");

  //           creacionDeUsuarioSistema.bloqueadoRelacion = !creacionDeUsuarioSistema.bloqueadoRelacion
  //           this.authSerguridadServicio.bloquearUsuario(creacionDeUsuarioSistema, this.nemonicoMenu).subscribe(
  //             {
  //               next: (resp: RespuestaPorDefecto<boolean>) => {
  //                 load.close();
  //                 this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

  //                 if (!resp.exito) {
  //                   this.authSerguridadServicio.checkError(resp);
  //                   return;
  //                 }

  //                 this.obtenerUsuarios();
  //               },
  //               error: (error: any) => {
  //                 load.close();
  //                 this.authSerguridadServicio.checkError(error);
  //               }
  //             }
  //           );
  //         }
  //       }
  //     }
  //   );
  }

  // obtenerFuncionarios() {
  //   let paginacionRequest = new PaginacionRequest();
  //   paginacionRequest.size = this.size;
  //   paginacionRequest.page = this.page;

  //   this.funcionarioService.obtenerFuncionariosValidos(paginacionRequest, etiquetasModel.NEMONICO_MENU_FUNCIONARIO).subscribe(
  //     {
  //       next: (response: RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>) => {
  //         if (!environment.production) {
  //           console.log(response);
  //         }

  //         if (!response.exito) {
  //           this.funcionarioService.checkError(response);
  //           return;
  //         }

  //         this.listaDeFuncionarios = response.data.data;
  //         this.dataSource = this.listaDeFuncionarios;
  //         this.totalItems = response.data.totalItems;
  //       },
  //       error: (error: any) => {
  //         this.funcionarioService.checkError(error);
  //       }
  //     }
  //   );
  // }

  busquedaFuncionarios() {
    this.paginator.firstPage();
    this.obtenerFuncionarios();
  }

  obtenerFuncionarios() {
    const valor = this.busquedaFormControl.value;    
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;    

    // this.funcionarioService.obtenerFuncionariosValidosPorValor(valor, paginacionRequest, etiquetasModel.NEMONICO_MENU_FUNCIONARIO).subscribe(
    //   {
    //     next: (response: RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>) => {
    //       if (!environment.production) {
    //         console.log(response);
    //       }

    //       if (!response.exito) {
    //         this.funcionarioService.checkError(response);
    //         return;
    //       }

    //       this.listaDeFuncionarios = response.data.data;
    //       this.dataSource = new MatTableDataSource(this.listaDeFuncionarios)
    //       // this.dataSource = this.listaDeFuncionarios;
    //       this.totalItems = response.data.totalItems;
    //       this.dataSource.sort = this.sort;
    //     },
    //     error: (error: any) => {
    //       this.funcionarioService.checkError(error);
    //     }
    //   }
    // );
  }

  handlePageEvent(pageEvent: PageEvent) {    
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;        
    this.obtenerFuncionarios();
  }
}
