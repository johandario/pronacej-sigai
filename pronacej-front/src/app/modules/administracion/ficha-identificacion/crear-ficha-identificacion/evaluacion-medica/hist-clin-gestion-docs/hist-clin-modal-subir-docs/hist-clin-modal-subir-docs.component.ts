import { Component, Inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { environment } from 'environments/environment';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { EvaluacionMedicaService } from '../../evaluacion-medica.service';
import { FichaMedicaDocumentoDTO } from 'app/core/model/request/ia/FichaMedicaDocumentoDTO.model';

@Component({
  selector: 'app-hist-clin-modal-subir-docs',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule,
    SubidaDeDocumentosComponent,
  ],
  templateUrl: './hist-clin-modal-subir-docs.component.html',
  styleUrl: './hist-clin-modal-subir-docs.component.scss'
})
export class HistClinModalSubirDocsComponent implements OnInit {
  tiposDeDocumentosSistema: TipoDeDocumento[];  

  constructor(
    private dialogRef: MatDialogRef<HistClinModalSubirDocsComponent>,   
    @Inject(MAT_DIALOG_DATA) public data: any,    
    private historiaClinicaService: EvaluacionMedicaService,
    private dialogMensajeService: DialogMensajeService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
  ) {
    console.log(data);
  }

  ngOnInit(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando tipos de documentos...');

    this.obtenerTiposDeDocumentos().subscribe(item => load.close());
  }
  
  subirArchivosEvent(documentos: DocumentoSubido[]) {
    if (documentos && documentos.length > 0) {
        
      for (let documentoSubido of documentos) {
        let fichaMedicaDocumentoDTO = new FichaMedicaDocumentoDTO();
        fichaMedicaDocumentoDTO.tokenIdentificadorFichaMedica = this.data;
        fichaMedicaDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

        let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
          documentoSubido.documento.name
        );
        this.historiaClinicaService.subirDocumento(
          documentoSubido.documento,
          fichaMedicaDocumentoDTO,
          ''
        ).subscribe(
          {
            next: (response: RespuestaPorDefecto<DocumentoDTO>) => {

              load.close();
              if (!response.exito) {
                this.historiaClinicaService.checkError(response);
                return;
              }              
            },
            error: (error: any) => {
              load.close();
              this.historiaClinicaService.checkError(error);
            }
          }
        );
      }
      // this.dialogRef.close();
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvieron documentos para ser subidos");
    }
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
