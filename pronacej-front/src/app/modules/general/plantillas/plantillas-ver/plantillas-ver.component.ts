import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import { PlantillaFormularioService } from '../../services/plantillaFormulario.service';
import { PlantillaFormularioDTO } from 'app/core/model/both/plantillaFormularioDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';

@Component({
  selector: 'app-plantillas-ver',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    TablaDatosComponent
  ],
  templateUrl: './plantillas-ver.component.html',
  styleUrl: './plantillas-ver.component.scss'
})
export class PlantillasVerComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PLANTILLAS;
  titulo: string = "Plantilla formulario";
  listaDePlantillas: PlantillaFormularioDTO[] = [];

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;
  @Output() editarUsuarioEvent = new EventEmitter<CreacionDeUsuarioSistema>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nemonico: "Nemónico",
    fechaCreacion: "Fecha de creación"
  };

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private plantillaFormularioService: PlantillaFormularioService,
  ) { }

  ngOnInit(): void {
    this.obtenerPlantillasFormulario();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(plantillaFormularioDTO: PlantillaFormularioDTO) {
    let action = "";
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
            this.editarPlantillaFormulario(plantillaFormularioDTO);

          } else if (result == "eliminar") {
            this.eliminarPlantillaFormulario(plantillaFormularioDTO);
          }
        }
      }
    );
  }

  eliminarPlantillaFormulario(plantillaFormularioDTO: PlantillaFormularioDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la plantilla formulario: \"" + plantillaFormularioDTO.nemonico + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando plantilla formulario..");
            this.plantillaFormularioService.eliminarPlantillaFormulario(plantillaFormularioDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerPlantillasFormulario();
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

  obtenerPlantillasFormulario() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.plantillaFormularioService.obtenerPlantillasFormulario(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlantillaFormularioDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.plantillaFormularioService.checkError(response);
            return;
          }

          this.listaDePlantillas = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.plantillaFormularioService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;

    this.plantillaFormularioService.obtenerPlantillasFormulario(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PlantillaFormularioDTO>>) => {

          if (!response.exito) {
            this.plantillaFormularioService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.plantillaFormularioService.checkError(error);
        }
      }
    );
  }

  agregarPlantillaFormulario() {
    this.router.navigate(['crear'], { relativeTo: this.route });
  }

  editarPlantillaFormulario(plantillaFormularioDTO: PlantillaFormularioDTO) {
    this.router.navigate(['editar'], { state: { item: plantillaFormularioDTO }, relativeTo: this.route });
  }

  visualizarPlantillaFormulario(plantillaFormularioDTO: PlantillaFormularioDTO) {
    this.router.navigate(['visualizar'], { state: { item: plantillaFormularioDTO, visualizar: true }, relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerPlantillasFormulario();
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

    this.obtenerPlantillasFormulario();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerPlantillasFormulario();
  }
}
