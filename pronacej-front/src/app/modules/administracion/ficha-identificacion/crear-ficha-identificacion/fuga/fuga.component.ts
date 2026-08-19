import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component, OnInit } from '@angular/core';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';

import { AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { GestionFugaService } from 'app/modules/flujo-trabajo/gestion-fuga/gestion-fuga.service';
import { environment } from 'environments/environment';
import moment from 'moment';

@Component({
  selector: 'app-fuga',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './fuga.component.html',
  styleUrl: './fuga.component.scss',
  providers: [
    { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() },    
  ],
})
export class FugaComponent implements OnInit {

  uuid_fp: string;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "Gestión de fuga";

  listaFuga: GestionFugaDTO[] = [];
  dataSource: CdkTableDataSourceInput<GestionFugaDTO>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaFuga: "Fecha de Fuga",
  };

  constructor(
    private gestionFugaService: GestionFugaService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerFugas();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }



  activarAcciones(gestionFugaDTO: GestionFugaDTO) {
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
            // Aqui se va a redirijir a otra pantalla que es un componente crear-editar pero necesito que al abrir ese componente
            // editando se le pase la variable fugaDto y se guarde en una variable de ese componente
            this.router.navigate(['crear-editar'], { state: { GestionFugaDTO }, relativeTo: this.route });
          } else if (result == "eliminar") {
            this.eliminarFuga(gestionFugaDTO);
          }
        }
      }
    );
  }

  editarFichaIngreso(gestionFugaDTO: GestionFugaDTO) {
    this.router.navigate(['crear-editar'], { state: { gestionFugaDTO }, relativeTo: this.route });
  }

  eliminarFuga(gestionFugaDTO: GestionFugaDTO) {
    console.log(gestionFugaDTO);
    
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar a: \"" + gestionFugaDTO.tokenIdentificador + "\" esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la fuga..");
            this.gestionFugaService.eliminarFuga(gestionFugaDTO,"").subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerFugas();
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

  agregarFuga() {
  this.router.navigate(['/flujo-trabajo/fuga/fuga-analista']);
}


  obtenerFugas(){
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.uuid_fp;
    this.gestionFugaService.obtenerFugas(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<GestionFugaDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }
          this.listaFuga = response.data.data;
          this.dataSource = this.listaFuga;
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
    this.obtenerFugas();
  }


}


export function getEspPaginatorIntl() {
  const paginatorIntl = new MatPaginatorIntl();
  
  paginatorIntl.itemsPerPageLabel = 'Elementos por página:';
  paginatorIntl.firstPageLabel = 'Ir al inicio';
  paginatorIntl.nextPageLabel = 'Siguiente';
  paginatorIntl.previousPageLabel = 'Anterior';
  paginatorIntl.lastPageLabel = 'Ir al final';

  paginatorIntl.getRangeLabel = (page: number, pageSize: number, length: number) =>  {
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
