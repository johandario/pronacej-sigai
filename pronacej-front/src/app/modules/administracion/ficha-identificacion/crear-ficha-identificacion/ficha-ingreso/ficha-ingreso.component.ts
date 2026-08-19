import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import { PermisoDirective } from 'app/core/directives/permiso.directive';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import { environment } from 'environments/environment';
import moment from 'moment';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-ficha-ingreso',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatInputModule,
    CommonModule,
    FormsModule,
    MatSortModule,
    PermisoDirective
  ],
  templateUrl: './ficha-ingreso.component.html',
  styleUrl: './ficha-ingreso.component.scss',
  providers: [
    { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() },
  ],
})
export class FichaIngresoComponent implements OnInit {

  uuid_fp: string;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "ficha de ingreso";
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_INGRESO;
  
  etiquetaEditar = etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR;

  listaFichasIngreso: FichaIngresoDTO[] = [];
  dataSource: CdkTableDataSourceInput<FichaIngresoDTO>;

  keyLabelsTable: any = {
    acciones: "Acciones",
    fechaIngreso: "Fecha de Ingreso",
    centro: "Centro",
    nombreSeguro: "Seguro",
    lesiones: "Lesiones",
    moretones: "Moretones",
    piercing: "Piercing",
    tatuajes: "Tatuajes",
    observaciones: "Observaciones",

  };

  filter: string = '';

  fichaIngresosPaginacion: PaginacionRequest = new PaginacionRequest();
  centro: JerarquiaDTO;

  constructor(
    private fichaIngresoService: FichaIngresoService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private jerarquiaService: JerarquiaService,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_FICHA_INGRESO"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerFichasIngreso();
    this.cargarCentro();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(fichaIngresoDTO: FichaIngresoDTO) {
    let ref = this.accionesSheet.open(AccionesUsuarioComponent,
      {
        data: {
          mostrar: false,
          textAccion: "",
          keyAccion: "",
        }
      }
    );

    ref.afterDismissed().subscribe(
      {
        next: (result: "editar" | "eliminar" | "Desbloquear" | "Bloquear") => {
          if (result == "editar") {
            this.router.navigate(['crear-editar'], { state: { fichaIngresoDTO }, relativeTo: this.route });
          } else if (result == "eliminar") {
            this.eliminarFichaIngreso(fichaIngresoDTO);
          }
        }
      }
    );
  }

  visualizarFichaIngreso(fichaIngresoDTO: FichaIngresoDTO) {
    fichaIngresoDTO.esVisualizacion = true;
    this.router.navigate(['crear-editar'], { state: { fichaIngresoDTO }, relativeTo: this.route });
  }

  editarFichaIngreso(fichaIngresoDTO: FichaIngresoDTO) {
    this.router.navigate(['crear-editar'], { state: { fichaIngresoDTO }, relativeTo: this.route });
  }

  eliminarFichaIngreso(fichaIngresoDTO: FichaIngresoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la ficha de ingreso, esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la ficha de ingreso..");
            this.fichaIngresoService.eliminarFichaIngreso(fichaIngresoDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerFichasIngreso();
                },
                error: (error: any) => {
                  load.close();

                  this.fichaIngresoService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  agregarFichaIngreso() {
    this.router.navigate(['crear-editar'], { relativeTo: this.route, state: null });
  }

  obtenerFichasIngreso() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.uuid_fp;
    paginacionRequest.direction = this.fichaIngresosPaginacion.direction;
    if (this.filter.length > 0) {
      paginacionRequest.filter = this.filter;
      paginacionRequest.page = 0;
      this.page = 0;
    }

    this.fichaIngresoService.obtenerFichasIngresoPaginado(paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaIngresoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaFichasIngreso = response.data.data;
          this.dataSource = this.listaFichasIngreso;
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
    this.obtenerFichasIngreso();
  }

  onXLSX() {
    // Primero, transformamos los datos para que las claves se mapeen a los títulos definidos en keyLabels
    const transformedData = this.listaFichasIngreso.map((item, i) => {
      const transformedItem: any = {};

      // Recorremos las claves de keyLabels y excluimos 'acciones'
      Object.keys(this.keyLabelsTable).forEach(key => {
        if (key !== 'acciones') { // Excluir 'acciones'
          const label = this.keyLabelsTable[key] || key; // Si no hay un label, usamos la clave original

          // Manejo de campos especiales
          if (key === 'numero') {
            transformedItem[label] = this.totalItems - (i + (this.page * this.size));
          } else if (key.includes('fecha')) {
            transformedItem[label] = this.getLocalDate(item[key]);
          } else if (key.includes('tipoCentro')) {
            transformedItem[label] = item.centro.jerarquiaPadre.nemonico == "SOA" ? "Medio Abierto" : "Medio Cerrado";
          } else if (key.includes('centro')) {
            transformedItem[label] = item.centro.nombre;
          } else if (key.includes('ubigeo')) {
            transformedItem[label] = item.centro.ubigeo;
          }else if (key.includes('lesiones')) {
            transformedItem[label] = item.lesiones? "Si" : "No";
          }else if (key.includes('moretones')) {
            transformedItem[label] = item.moretones? "Si" : "No";
          }else if (key.includes('piercing')) {
            transformedItem[label] = item.piercing? "Si" : "No";
          }else if (key.includes('tatuajes')) {
            transformedItem[label] = item.tatuajes? "Si" : "No";
          } else {
            transformedItem[label] = item[key];
          }
        }
      });

      return transformedItem;
    });
    // Creamos la hoja de trabajo con los datos transformados, excluyendo la columna 'acciones'
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(transformedData, { header: Object.values(this.keyLabelsTable).filter(label => label !== 'Acciones') as string[] });

    // Si deseas personalizar las columnas, por ejemplo, establecer un ancho fijo:
    const wscols = [
      { wch: 10 },
      { wch: 15 },
      { wch: 20 },
      { wch: 10 }
    ];
    ws['!cols'] = wscols;

    // Creamos el libro de trabajo
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Datos'); // Aquí "Datos" es el nombre de la hoja

    // Generamos el archivo XLSX
    const fileName = 'datos.xlsx'; // El nombre del archivo de salida
    XLSX.writeFile(wb, fileName); // Descarga el archivo
  }

  refrescar() {
    this.filter = '';
    this.page = 0;
    this.obtenerFichasIngreso();
  }

  onSortChange(evento: Sort) {
    if (evento.direction) {
      this.fichaIngresosPaginacion.sort = evento.active;
      this.fichaIngresosPaginacion.direction = evento.direction;
    } else {
      this.fichaIngresosPaginacion.sort = null;
      this.fichaIngresosPaginacion.direction = null;
    }
    this.obtenerFichasIngreso();
  }

  cargarCentro() {
    this.jerarquiaService
      .obtenerJerarquiaPorNumeroDeDocumento(etiquetasModel.NEMONICO_MENU_FICHA_INGRESO)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.jerarquiaService.checkError(respuesta);
            return;
          }

          if (!environment.production) {
            console.log(respuesta.data);
          }

          this.centro = respuesta.data;
          console.log('centro', this.centro);
          if (this.centro.jerarquiaPadre.nemonico == "SOA") {
            delete this.keyLabelsTable['nombreSeguro'];
            delete this.keyLabelsTable['lesiones'];
            delete this.keyLabelsTable['moretones'];
            delete this.keyLabelsTable['piercing'];
            delete this.keyLabelsTable['tatuajes'];
            this.keyLabelsTable["responsableInscripcion"] = "Responsable inscripción";

          }
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
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
  }
    ;

  return paginatorIntl;
}