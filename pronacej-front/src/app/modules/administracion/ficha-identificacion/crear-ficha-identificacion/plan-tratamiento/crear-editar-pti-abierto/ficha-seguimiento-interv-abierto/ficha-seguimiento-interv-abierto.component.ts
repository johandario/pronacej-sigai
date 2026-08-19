import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { PlanTratamientoIndIntervDTO, PlanTratamientoIndSeguiAbiertoDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { ModalCrearFichaSegAbPtiComponent } from './modal-crear-ficha-seg-ab-pti/modal-crear-ficha-seg-ab-pti.component';
import { MatDialog } from '@angular/material/dialog';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { MatTooltip } from '@angular/material/tooltip';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { Location } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PdfService } from 'app/core/services/pdf.service';
import { ModalMostrarDocsFichaSegAbPtiComponent } from './modal-mostrar-docs-ficha-seg-ab-pti/modal-mostrar-docs-ficha-seg-ab-pti.component';
import { ModalSubirDocsFichaSegAbPtiComponent } from './modal-subir-docs-ficha-seg-ab-pti/modal-subir-docs-ficha-seg-ab-pti.component';

@Component({
  selector: 'app-ficha-seguimiento-interv-abierto',
  standalone: true,
  imports: [
    MatExpansionModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatTableModule,
    MatTooltip,
  ],
  templateUrl: './ficha-seguimiento-interv-abierto.component.html',
  styleUrl: './ficha-seguimiento-interv-abierto.component.scss'
})
export class FichaSeguimientoIntervAbiertoComponent implements OnInit {

  intervencion: PlanTratamientoIndIntervDTO;

  @ViewChild('paginator') paginator: MatPaginator;
  dataSource: MatTableDataSource<any>;  

  listaSeguimientos: PlanTratamientoIndSeguiAbiertoDTO[];
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  paginacion: Paginacion = new Paginacion();  

  columnas: string[] = [
    'acciones', 
    'fecha', 
    'hora', 
    'descripcion', 
    'subidaDocumentos'
  ];

  base64Image: string | null = null;

  constructor(
    private router: Router,
    private planTratamientoService: PlanTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    private location: Location,
    private http: HttpClient,  
    private pdfService: PdfService,
    public dialog: MatDialog,    
    public funcionesUtils: FuncionesUtils,
  ) {

  }

  ngOnInit(): void {
    this.loadImageAsBase64();
    if (history.state.interv) {
      this.intervencion = history.state.interv;
      console.log(this.intervencion);
    }
    this.obtenerFichasSeguimiento();
  }

  obtenerFichasSeguimiento() {
    this.paginacionRequest.size = 50;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.intervencion.tokenIdentificador;
    
    this.planTratamientoService.obtenerFichasSeguimientoAbierto(this.paginacionRequest, '').subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>>) => {
            if (!environment.production) {
              console.log(response);
            }
  
            if (!response.exito) {
              this.planTratamientoService.checkError(response);
              return;
            }
            this.listaSeguimientos = response.data.data;
            this.dataSource = new MatTableDataSource(this.listaSeguimientos);
            this.dataSource.paginator = this.paginator;
            this.paginacion.totalItems = response.data.totalItems;
          },
          error: (error: any) => {
            this.planTratamientoService.checkError(error);
          }
        }
      );
  }

  agregarRegistro() {
    const dialogRef = this.dialog.open(ModalCrearFichaSegAbPtiComponent, {
      disableClose: true,
      data: { fila: null },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        let fichaSeguimiento = new PlanTratamientoIndSeguiAbiertoDTO();          
        Object.assign(fichaSeguimiento, result);
        fichaSeguimiento.tokenPtiInterv = this.intervencion.tokenIdentificador;
        this.planTratamientoService.crearFichaSeguimientoAbierto(fichaSeguimiento, '').subscribe(
          item => this.obtenerFichasSeguimiento()
        );
      }
    })
  }  

  editarRegistro(seguimiento: PlanTratamientoIndSeguiAbiertoDTO) {
    const dialogRef = this.dialog.open(ModalCrearFichaSegAbPtiComponent, {
      disableClose: true,
      data: { fila: seguimiento },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {      
        let fichaSeguimiento = new PlanTratamientoIndSeguiAbiertoDTO();          
        Object.assign(fichaSeguimiento, result);
        fichaSeguimiento.tokenPtiInterv = this.intervencion.tokenIdentificador;
        fichaSeguimiento.esEdicion = true;  
        this.planTratamientoService.crearFichaSeguimientoAbierto(fichaSeguimiento, '').subscribe(
          item => this.obtenerFichasSeguimiento()
        );
      }
    })
  }  

  eliminarRegistro(seguimiento: PlanTratamientoIndSeguiAbiertoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro, esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.planTratamientoService.eliminarFichaSeguimientoAbierto(seguimiento, '').subscribe(
              {
                next: (resp: RespuestaPorDefecto<PlanTratamientoIndSeguiAbiertoDTO>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerFichasSeguimiento();
                },
                error: (error: any) => {
                  load.close();

                  this.planTratamientoService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  volver() {
    this.location.back();
  }

  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatHora(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }

  pruebaPdf() {

    let tablaFormateada: any[] = [];
    for (let ficha of this.listaSeguimientos) {
      let elemento = {
        fecha: this.formatFecha(ficha.fecha.toString()),
        hora: ficha.hora || '',
        descripcion: ficha.descripcion || '',
      }
      tablaFormateada.push(elemento);
    }

    let tablaFactores = new TablaPlantilla();
    tablaFactores.encabezados = ['Fecha', 'Hora', 'Descripción'];
    tablaFactores.filas = tablaFormateada; 

    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_PTI_FICHA_SEGUIMIENTO_ABIERTO;
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]": 'Ficha de seguimiento de intervención individual',
      "[TITULO-INFORME]": 'Ficha de seguimiento de intervención individual',
      "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
      "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
      // "[CENTRO]": fichaIdentificacion.centroIngreso,
      // "[ADOLESCENTE]": nombreAdolescente,
      // "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
      // "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
      "[TABLA-SEGUIMIENTO]": JSON.stringify(tablaFactores),
    }
    this.pdfService.generarPdf(request, '').subscribe({
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

  abrirDocumentosCargados(ficha: PlanTratamientoIndSeguiAbiertoDTO) {
    this.dialog.open(ModalMostrarDocsFichaSegAbPtiComponent, {
      width: '80%',
      data: ficha
    });     
  }
  
  abrirCargaDeDocumentos(ficha: PlanTratamientoIndSeguiAbiertoDTO) {
    this.dialog.open(ModalSubirDocsFichaSegAbPtiComponent, {
      width: '80%',
      data: ficha
    });     
  }
}
