import { Component, Inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { PlanTratamientoIndSeguiAbiertoDocumentoDTO } from 'app/core/model/request/ia/PlanTratamientoIndSeguiAbiertoDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { environment } from 'environments/environment';
import { catchError, map, Observable, tap, throwError } from 'rxjs';

@Component({
  selector: 'app-modal-subir-docs-ficha-seg-ab-pti',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule,
    SubidaDeDocumentosComponent,
  ],
  templateUrl: './modal-subir-docs-ficha-seg-ab-pti.component.html',
  styleUrl: './modal-subir-docs-ficha-seg-ab-pti.component.scss'
})
export class ModalSubirDocsFichaSegAbPtiComponent implements OnInit {

  tiposDeDocumentosSistema: TipoDeDocumento[];  

  constructor(
    private dialogRef: MatDialogRef<ModalSubirDocsFichaSegAbPtiComponent>,   
    @Inject(MAT_DIALOG_DATA) public data: any,    
    private planTratamientoService: PlanTratamientoService,
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
        let planTratamientoIndSeguiAbiertoDocumentoDTO = new PlanTratamientoIndSeguiAbiertoDocumentoDTO();
        planTratamientoIndSeguiAbiertoDocumentoDTO.tokenIdentificadorFichaSeguimientoAbierto = this.data.tokenIdentificador;
        planTratamientoIndSeguiAbiertoDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

        let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
          documentoSubido.documento.name
        );
        this.planTratamientoService.subirDocumentoFichaSeguimiento(
          documentoSubido.documento,
          planTratamientoIndSeguiAbiertoDocumentoDTO,
          ''
        ).subscribe(
          {
            next: (response: RespuestaPorDefecto<DocumentoDTO>) => {

              load.close();
              if (!response.exito) {
                this.planTratamientoService.checkError(response);
                return;
              }              
            },
            error: (error: any) => {
              load.close();
              this.planTratamientoService.checkError(error);
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
        etiquetasModel.SECCION_FICHA_IDENT_PTI_FICHA_SEGUIMIENTO_ABIERTO, ''
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
