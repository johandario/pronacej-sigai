import { Component, Input, OnChanges, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { FichaMedicaDocumentoRequest } from 'app/core/model/request/ia/FichaMedicaDocumentoRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { environment } from 'environments/environment';
import { Observable, catchError, concatMap, map, tap, throwError } from 'rxjs';
import { EvaluacionMedicaService } from '../evaluacion-medica.service';
import { HistClinModalSubirDocsComponent } from './hist-clin-modal-subir-docs/hist-clin-modal-subir-docs.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-hist-clin-gestion-docs',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    DocumentosSubidosTablaComponent,
    MatProgressBarModule
  ],
  templateUrl: './hist-clin-gestion-docs.component.html',
  styleUrl: './hist-clin-gestion-docs.component.scss'
})
export class HistClinGestionDocsComponent implements OnChanges {
  @Input() tokenIdentificadorFichaMedica: string;
  isLoading: boolean = false;
  
  @ViewChild("tablaDocumentos") tablaDocumentos: DocumentosSubidosTablaComponent;
  tiposDeDocumentosSistema: TipoDeDocumento[];

  constructor(
    private dialogMensajeService: DialogMensajeService, 
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,   
    private fichaMedicaService: EvaluacionMedicaService,
    private dialog: MatDialog,
  ) { }

  ngOnChanges(): void {
    if (this.tokenIdentificadorFichaMedica) {
      this.cargarDatos();
    }
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

  abrirCargaDeDocumentos() {
    const dialogRef = this.dialog.open(HistClinModalSubirDocsComponent, {
      width: '80%',
      data: this.tokenIdentificadorFichaMedica
    });     

    dialogRef.afterClosed().subscribe(() => this.obtenerDocumentos().subscribe());
    
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    this.obtenerDocumentos().subscribe();
  } 

  buscarDocumentos(textoBuscar: string) {
    this.tablaDocumentos.textoBuscar = textoBuscar;    
    this.obtenerDocumentos().subscribe();
  }

  private obtenerDocumentos() : Observable<TipoDeDocumento[]> {        
  
    let planTratamientoIndSeguiDocumentoRequest = new FichaMedicaDocumentoRequest();
    planTratamientoIndSeguiDocumentoRequest.page = this.tablaDocumentos.page;
    planTratamientoIndSeguiDocumentoRequest.size = this.tablaDocumentos.pageSize;
    planTratamientoIndSeguiDocumentoRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
    planTratamientoIndSeguiDocumentoRequest.tokenIdentificadorFichaMedica = this.tokenIdentificadorFichaMedica;
  
    return this.fichaMedicaService
      .obtenerDocumentos(planTratamientoIndSeguiDocumentoRequest, '')
      .pipe(
        tap((response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }
  
          if (!response.exito) {
            this.fichaMedicaService.checkError(response);
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
        etiquetasModel.SECCION_FICHA_HISTORIA_CLINICA, ''
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
