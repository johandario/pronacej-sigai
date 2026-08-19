import { Component, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { Router } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { RelacionEgresoService } from 'app/modules/administracion/services/relacionEgreso.service';
import { RelacionEgresoDTO } from 'app/core/model/both/salida/RelacionEgresoDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { Sort } from '@angular/material/sort';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';

@Component({
  selector: 'app-relacion-adolescente-egreso',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatTabsModule,
    FormsModule,
    MatSelectModule,
    MatDatepickerModule,
    MatInputModule,
    ReactiveFormsModule,
    TablaListaComponent
  ],
  templateUrl: './relacion-adolescente-egreso.component.html',
  styleUrl: './relacion-adolescente-egreso.component.scss'
})
export class RelacionAdolescentesEgresoComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_RELACION_ADOLESCENTES_EGRESO;

  listaAdolescentes: RelacionEgresoDTO[] = [];

  funcionarioActivo: FuncionarioDTO;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    numExpediente: "Número Expediente",
    nombres: "Nombres",
    apellidoPaterno: "Apellido Paterno",
    apellidoMaterno: "Apellido Materno",
    tipoDocumento: "Tipo Documento",
    numDocumento: "Número Documento"
  };

  constructor(
    private dialogMensajeService: DialogMensajeService,
    private relacionEgresoService: RelacionEgresoService,
    private funcionarioService: FuncionarioService,
    private router: Router,
    private formBuilder: FormBuilder,
  ) {
  }

  ngOnInit(): void {
    this.obtenerFuncionario();
  }

  obtenerFuncionario() {
    this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {

          if (!response.exito) {
            return;
          }

          this.funcionarioActivo = response.data;
          this.obtenerAdolescentes();
        },
        error: (error: any) => {
          console.log('Hubo un problema al recuperar los registros. Inténtalo de nuevo.');
        }
      }
    );
  }

  obtenerAdolescentes() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.funcionarioActivo.tokenIdentificadorDepartamento;

    this.relacionEgresoService.obtenerAdolescentes(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RelacionEgresoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaAdolescentes = response.data.data;
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

    this.relacionEgresoService.obtenerAdolescentes(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RelacionEgresoDTO>>) => {

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

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerAdolescentes();
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

    this.obtenerAdolescentes();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerAdolescentes();
  }

  irAPantalla(relacion: RelacionEgresoDTO) {
    // this.router.navigate([row.url], {queryParams: {estado: row.estado}});
    this.router.navigate(["gestion-adolescente/ficha-identificacion/crear-editar/preparacionEgreso/" + relacion.tokenFichaIdentificacion]);
  }
}
