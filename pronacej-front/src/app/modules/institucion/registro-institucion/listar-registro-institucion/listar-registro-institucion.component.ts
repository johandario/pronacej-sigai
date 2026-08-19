import { Component, OnInit, ViewChild } from '@angular/core';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InstitucionService } from '../../institucion.service';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-listar-registro-institucion',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './listar-registro-institucion.component.html',
  styleUrl: './listar-registro-institucion.component.scss'
})
export class ListarRegistroInstitucionComponent implements OnInit {
  tituloPantalla: string = "Registro de instituciones";
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  uuid_fp!: string;
  listaProcesos: RegistroInstitucionDTO[] = [];
  funcionarioActivo: FuncionarioDTO;
  tokenJerarquia: any
  jerarquia: any;
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INSTITUCION;
  tokenFilter: any
  tokenFilterJerarquia: any

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    idRegistroInstitucion: "No.",
    acciones: "Acciones",
    ruc: "RUC",
    nombreOrganizacion: "Nombre organización",
    nombreDirector: "Nombre director",
    direccion: "Dirección",
    finalidadInstitucion: "Finalidad",
    estado: "Estado",
  };

  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  paginacion: Paginacion = new Paginacion();


  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private institucionService: InstitucionService,
    private funcionarioService: FuncionarioService,
    private jerarquiaService: JerarquiaService,
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    registerLocaleData(localeEs, 'es-ES');
    this.obtenerJerarquia();
   
  }



  obtenerProcesos() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.filter = this.paginacionRequest.filter;
    paginacionRequest.tokenIdentificador = this.tokenFilterJerarquia
    this.institucionService.obtenerRegistroInstituciones(paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            return;
          }
          this.listaProcesos = response.data.data;
          console.log(this.listaProcesos);
          this.paginacion.totalItems = response.data.totalItems;

        },
        error: (error: any) => {
          this.institucionService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.tokenFilter

    this.institucionService.obtenerRegistroInstituciones(this.paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.institucionService.checkError(error);
        }
      }
    );
  }

  agregarProceso() {
    this.router.navigate(['/institucion/registro-institucion/crear']);
  }

  editarProceso(proceso: RegistroInstitucionDTO) {
    this.router.navigate([`/institucion/registro-institucion/crear-editar/${proceso.tokenIdentificador}`], {
      state: {
        editar: true,
        proceso: proceso
      }
    });
  }

  visualizar(proceso: RegistroInstitucionDTO) {
    this.router.navigate([`/institucion/registro-institucion/crear-editar/${proceso.tokenIdentificador}`], {
      state: {
        editar: false,
        proceso: proceso
      }
    });
  }


  eliminarProceso(gestionFugaDTO: RegistroInstitucionDTO) {
    console.log(gestionFugaDTO);
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar este registro, esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la fuga..");
            this.institucionService.eliminarInstitucion(gestionFugaDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerProcesos();
                },
                error: (error: any) => {
                  load.close();

                  // this.gestionFugaService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerProcesos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerProcesos();

  }

  refrescar() {
    this.obtenerProcesos()
  }




  obtenerJerarquia(): void {
  this.jerarquiaService
    .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
    .subscribe({
      next: (response: RespuestaPorDefecto<any>) => {
        if (!response.exito || !response.data) {
          this.dialogMensajeService.mensajeError('No se pudo obtener la jerarquía del usuario.');
          return;
        }
        this.tokenFilterJerarquia = response.data.tokenIdentificador;
        this.obtenerProcesos();
      },
      error: (err) => {
        console.error('Error al obtener jerarquía por documento:', err);
        this.dialogMensajeService.mensajeError('Error al obtener la jerarquía del usuario.');
      },
    });
}

}
