import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaAsistenciaPostEgresoDTO } from 'app/core/model/both/FichaAsistenciaPostEgreso.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { PlanAsistenciaPostEgresoDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { FichaAsistenciaPostEgresoDocumentoDTO } from 'app/core/model/request/ia/FichaAsistenciaPostEgresoDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { PlanAsistenciaService } from 'app/modules/seguridad/services/planAsistencia.service';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-modal-seguimiento-subir-doc',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatDialogContent,
    MatDialogActions,
    MatIconModule,
    MatButtonModule,
    SubidaDeDocumentosComponent
  ],
  templateUrl: './modal-seguimiento-subir-doc.component.html',
  styleUrl: './modal-seguimiento-subir-doc.component.scss'
})
export class ModalSeguimientoSubirDocComponent implements OnInit {
    tiposDeDocumentosSistema: TipoDeDocumento[] = [];  

    seccion: string = etiquetasModel.SECCION_FICHA_IDENT_POST_EGRESO;
    etiquetasModel = etiquetasModel;
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_PLAN_SEGUIMIENTO;

    constructor(
      private catalogoService: CatalogoService,
      private planAsistenciaService: PlanAsistenciaService,
      private dialogMensajeService: DialogMensajeService,
      private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
      public dialogRef: MatDialogRef<ModalSeguimientoSubirDocComponent>,      
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {

    }

    ngOnInit(): void {
      const load = this.dialogMensajeService.mensajeLoading('Cargando tipos de documentos...');

      this.obtenerTiposDeDocumentos().subscribe(() => load.close());;
    }

    subirArchivosEvent(documentos: DocumentoSubido[]) {
      if (documentos && documentos.length > 0) {
          
        for (let documentoSubido of documentos) {
          let fichaAsistenciaPostEgresoDocumentoDTO = new FichaAsistenciaPostEgresoDocumentoDTO();
          fichaAsistenciaPostEgresoDocumentoDTO.tokenIdentificadorFichaAsistenciaPostEgreso = this.data.tokenIdentificador;
          fichaAsistenciaPostEgresoDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;
  
          let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
            documentoSubido.documento.name
          );
          this.planAsistenciaService.subirDocumento(
            documentoSubido.documento,
            fichaAsistenciaPostEgresoDocumentoDTO,
            ''
          ).subscribe(
            {
              next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
  
                load.close();
                if (!response.exito) {
                  this.planAsistenciaService.checkError(response);
                  return;
                }              
              },
              error: (error: any) => {
                load.close();
                this.planAsistenciaService.checkError(error);
              }
            }
          );
        }
        // this.dialogRef.close();
      } else {
        this.dialogMensajeService.mensajeError("No se obtuvieron documentos para ser subidos");
      }
    }

    // subirDocumento(documentos: DocumentoSubido[]) {
    //   console.log(documentos);
      
    //   if (documentos && documentos.length > 0) {
    //     let completadas = 0;
    //     const total = documentos.length;
        
    //     for (let documentoSubido of documentos) {
    //       let fichaAsistenciaPostEgresoDocumentoDTO = new FichaAsistenciaPostEgresoDocumentoDTO();
    //       console.log(this.data);
          
    //       fichaAsistenciaPostEgresoDocumentoDTO.tokenIdentificadorFichaAsistenciaPostEgreso = this.data.fichaAsistencia.tokenIdentificador;
    //       fichaAsistenciaPostEgresoDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;
        
    //       // VALIDACIÓN EXTRA
    //       console.log('Documento subido tipo:', documentoSubido.documentoDTO?.tipoDocumentoSistema?.nemonico);
        
    //       if (!documentoSubido.documentoDTO.tipoDocumentoSistema) {
    //         documentoSubido.documentoDTO.tipoDocumentoSistema = {} as any;
    //       }
    //       documentoSubido.documentoDTO.tipoDocumentoSistema.nemonico = 'SEGUIMIENTO';
        
    //       let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " + documentoSubido.documento.name);
        
    //       this.planAsistenciaService.subirDocumento(
    //         documentoSubido.documento,
    //         fichaAsistenciaPostEgresoDocumentoDTO,
    //         ''
    //       ).subscribe(
    //         {
    //           next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
    //             load.close();
    //             if (!response.exito) {
    //               this.planAsistenciaService.checkError(response);
    //               return;
    //             }
    //             completadas++;
    //             if (completadas === total) {
    //               this.dialogRef.close(); 
    //             }
    //           },
    //           error: (error: any) => {
    //             load.close();
    //             this.planAsistenciaService.checkError(error);
    //           }
    //         }
    //       );
    //     }
        
    //   } else {
    //     this.dialogMensajeService.mensajeError("No se obtuvieron documentos para ser subidos");
    //   }
    // }

    private obtenerTiposDeDocumentos(): Observable<TipoDeDocumento[]> {
      return this.tipoDeIdentificacionTipoDeDocumentoService
        .obtenerTiposDeDocumentos(
          etiquetasModel.SECCION_FICHA_IDENT_POST_EGRESO, ''
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
