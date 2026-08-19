import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { PlanTratamientoIndSeguiDocumentoRequest } from 'app/core/model/request/ia/PlanTratamientoIndSeguiDocumentoRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { environment } from 'environments/environment';
import { catchError, concatMap, map, Observable, tap, throwError } from 'rxjs';

@Component({
  selector: 'app-modal-mostrar-docs-segui-pti',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    DocumentosSubidosTablaComponent,
    MatButtonModule
  ],
  templateUrl: './modal-mostrar-docs-segui-pti.component.html',
  styleUrl: './modal-mostrar-docs-segui-pti.component.scss'
})
export class ModalMostrarDocsSeguiPtiComponent implements OnInit {
  
  @ViewChild("tablaDocumentos") tablaDocumentos: DocumentosSubidosTablaComponent;
  tiposDeDocumentosSistema: TipoDeDocumento[];    

  constructor(
    private dialogRef: MatDialogRef<ModalMostrarDocsSeguiPtiComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,     
    private planTratamientoService: PlanTratamientoService,     
    private dialogMensajeService: DialogMensajeService,   
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,         
  ) {
    console.log(data);
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos() : void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando documentos...');

    this.obtenerTiposDeDocumentos().pipe(
      concatMap(() => this.obtenerDocumentos())
    ).subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    this.obtenerDocumentos().subscribe();
  } 

  private obtenerDocumentos() : Observable<TipoDeDocumento[]> {    
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;
  
    let planTratamientoIndSeguiDocumentoRequest = new PlanTratamientoIndSeguiDocumentoRequest();
    planTratamientoIndSeguiDocumentoRequest.page = this.tablaDocumentos.page;
    planTratamientoIndSeguiDocumentoRequest.size = this.tablaDocumentos.pageSize;
    planTratamientoIndSeguiDocumentoRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
    planTratamientoIndSeguiDocumentoRequest.tokenIdentificadorSeguimiento = this.data.tokenIdentificador;
  
    return this.planTratamientoService
      .obtenerDocumentosSeguimiento(planTratamientoIndSeguiDocumentoRequest, '')
      .pipe(
        tap((response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }
  
          if (!response.exito) {
            this.planTratamientoService.checkError(response);
            throw new Error(response.mensaje); // Lanza error para interrumpir el flujo
          }
  
          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        }),
        catchError(error => {
          // this.planTratamientoService.checkError(error);
          return throwError(() => error); // Propaga el error
        }),
        map(() => void 0) // Devuelve void para indicar que no se necesita un valor de retorno
      );
  }

  private obtenerTiposDeDocumentos(): Observable<TipoDeDocumento[]> {
      return this.tipoDeIdentificacionTipoDeDocumentoService
        .obtenerTiposDeDocumentos(
          etiquetasModel.SECCION_FICHA_IDENT_PTI_SEGUIMIENTO, ''
        )
        .pipe(
          tap((response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
            if (!environment.production) {
              console.log(response);
            }
    
            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
              throw new Error(response.mensaje); // Lanza un error para que el observable maneje la interrupción
            }
    
            if (response.data.length === 0) {
              this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
              throw new Error("Tipos de documentos no configurados"); 
            }
    
            this.tiposDeDocumentosSistema = response.data.map(tipoArch => {
              let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
              let tipoDeDocumento = new TipoDeDocumento();
              tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
              tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
              tipoDeDocumento.requerido = tipoArch.requerido;
              tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
              tipoDeDocumento.nombre = catalogoTipoDoc.nombre;
    
              return tipoDeDocumento;
            });
          }),
          catchError(error => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
            return throwError(() => error); // Propaga el error al flujo de observables
          }),
          map(() => this.tiposDeDocumentosSistema) // Retorna la lista de tipos de documentos
        );
    }  

}
