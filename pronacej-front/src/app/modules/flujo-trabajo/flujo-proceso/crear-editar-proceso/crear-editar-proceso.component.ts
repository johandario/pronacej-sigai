import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray } from '@angular/cdk/drag-drop';
import { ModalPasoComponent } from './modal-paso/modal-paso.component';
import { FlujoTrabajoService } from '../../flujo-trabajo.service';
import { PasoDTO, ProcesoDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { AuthService } from 'app/core/auth/auth.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { UsuarioSistemaEmpresaRolService } from 'app/modules/seguridad/services/usuarioSistemaEmpresaRol.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-crear-editar-proceso',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    RouterLink,
    CdkDropList, 
    CdkDrag
  ],
  templateUrl: './crear-editar-proceso.component.html',
  styleUrl: './crear-editar-proceso.component.scss'
})
export class CrearEditarProcesoComponent implements OnInit {
  estadoEditar: Boolean = false;

  proceso: ProcesoDTO = new ProcesoDTO;

  pasosRemovidos: PasoDTO[] = [];

  usuariosSistema: UsuarioSistemaDTO[];
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FLUJO_PROCESOS;
  
  pasosDataSource: MatTableDataSource<any>;
  displayedColumns: string[] = [
    'acciones',
    // 'idPaso', 
    'orden', 'nombre', 'url', 
    // 'porcentaje',
    // 'condicional',
    'notificacion',
    'opciones'
  ];  


  constructor(
    private route: ActivatedRoute,
    private flujoTrabajoService: FlujoTrabajoService,  
    public dialog: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private authSeguridadService: AuthSerguridadServicio
  ) {}

  ngOnInit(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.route.queryParams.subscribe(params => {
      let paginacionUsuarios = new PaginacionRequest;
      paginacionUsuarios.page = 0;  
      paginacionUsuarios.size = 1000;  
      this.authSeguridadService.obtenerUsuariosActivos(paginacionUsuarios, this.nemonicoMenu).subscribe( 
        {
          next: (response: RespuestaPorDefecto<any>) => {
                    
            if (!response.exito) {
              this.authSeguridadService.checkError(response);

              return;
            }                  
            this.usuariosSistema = response.data;

            if (params['ID']) {
              this.estadoEditar = true;
              this.flujoTrabajoService.obtenerProcesoPorTokenID(params['ID'], this.nemonicoMenu).subscribe(result => {
                this.proceso = result.data;
                this.proceso.esEdicion = true;
                this.proceso.pasos = this.proceso.pasos.sort((a, b) => (a.orden < b.orden ? -1 : 1));
                this.pasosDataSource = new MatTableDataSource(this.proceso.pasos);
              })
            } else {
              this.proceso.pasos = [];
              this.pasosDataSource = new MatTableDataSource(this.proceso.pasos);
            }
            
            load.close();

          },
          error: (error: any) => {
            this.authSeguridadService.checkError(error);
          }
        }
      )
      
    })
  }

  agregarPaso() {
    const dialogRef = this.dialog.open(ModalPasoComponent, {      
      data: { usuarios: this.usuariosSistema },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        let lista = [...this.pasosDataSource.data];
        lista.push(result);
        this.pasosDataSource = new MatTableDataSource(this.reasignarOrden(lista));
      }
    })
  }

  editarPaso(paso: PasoDTO, index: number) {
    let pasoTemp = {...paso}
    const dialogRef = this.dialog.open(ModalPasoComponent, {  
      data: { usuarios: this.usuariosSistema, paso: pasoTemp },    
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        let lista = [...this.pasosDataSource.data];
        lista[index] = result;
        this.pasosDataSource = new MatTableDataSource(this.reasignarOrden(lista));
      }
    })
  }

  eliminarPaso(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el registro seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let lista = [...this.pasosDataSource.data];
            let itemRemovido = lista.splice(index, 1);
            itemRemovido[0].removido = true;
            itemRemovido[0].orden = null;
            this.pasosRemovidos.push(itemRemovido[0]);
            this.pasosDataSource = new MatTableDataSource(this.reasignarOrden(lista));
          }
        }
      }
    )    
  }

  reasignarOrden(lista: PasoDTO[]) {    
    lista.forEach((item, index) => {
        item.orden = index + 1; 
    });
    return lista; 
  }

  subirPaso(index: number) {
    let lista = [...this.pasosDataSource.data];
    const anterior = lista[index - 1]
    const actual = lista[index]
    lista[index - 1] = actual;
    lista[index] = anterior;
    this.pasosDataSource = new MatTableDataSource(this.reasignarOrden(lista));
  }

  bajarPaso(index: number) {
    let lista = [...this.pasosDataSource.data];
    const actual = lista[index]
    const siguiente = lista[index + 1]
    lista[index + 1] = actual;
    lista[index] = siguiente;
    this.pasosDataSource = new MatTableDataSource(this.reasignarOrden(lista));
  }

  guardarCambios() {   
    if (!this.proceso.nombre?.trim() || !this.proceso.nemonico?.trim() || this.proceso.version == null) {
      this.dialogMensajeService.mensajeError(
        'Todos los campos son obligatorios.'
      );
      return;
    } 
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            
            this.proceso.pasos = this.pasosDataSource.data;
            //this.proceso.pasos = this.pasosDataSource.data.concat(this.pasosRemovidos);
            
            this.flujoTrabajoService.crearEditarProceso(this.proceso, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<any>) => {
                  
                  if (!response.exito) {
                    this.flujoTrabajoService.checkError(response);
        
                    return;
                  }                  
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                  this.router.navigate([`/flujo-trabajo/admin-procesos`])
                },
                error: (error: any) => {
                  this.flujoTrabajoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }

  guardarCambiosSinSalir() {   
    if (!this.proceso.nombre?.trim() || !this.proceso.nemonico?.trim() || this.proceso.version == null) {
      this.dialogMensajeService.mensajeError(
        'Todos los campos son obligatorios.'
      );
      return;
    } 
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            
            this.proceso.pasos = this.pasosDataSource.data;
            //this.proceso.pasos = this.pasosDataSource.data.concat(this.pasosRemovidos);
            this.flujoTrabajoService.crearEditarProceso(this.proceso, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<any>) => {
                  
                  if (!response.exito) {
                    this.flujoTrabajoService.checkError(response);
                    
                    return;
                  }                  
                  // this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                  this.router.navigate(['/flujo-trabajo/admin-procesos/crear-editar'], {queryParams: {ID: response.data.tokenIdentificador}});
                  this.ngOnInit();
                },
                error: (error: any) => {
                  this.flujoTrabajoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }


  drop(event: CdkDragDrop<any[]>) {
    const data = this.pasosDataSource.data;
    moveItemInArray(data, event.previousIndex, event.currentIndex);
    this.pasosDataSource.data = this.reasignarOrden(data);
  }

 
  validarNumeros(event: KeyboardEvent) {
    const key = event.key;
    if (!/^\d$/.test(key) && key !== 'Backspace' && key !== 'ArrowLeft' && key !== 'ArrowRight') {
      event.preventDefault();
    }
  }

  validarLetras(event: KeyboardEvent) {
    const key = event.key;
    // Permitir solo letras, espacio, retroceso, y teclas de navegación
    if (!/^[a-zA-Z\s]$/.test(key) && key !== 'Backspace' && key !== 'ArrowLeft' && key !== 'ArrowRight') {
      event.preventDefault();
    }
  }
  
  
  
}
