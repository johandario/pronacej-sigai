import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import { EvaluacionDomiciliariaService } from 'app/modules/seguridad/services/EvaluacionDomiciliaria.service';
import { EvaluacionDocumentoDTO } from 'app/core/model/both/encuesta/evaluacionDocumentoDTO.model';
import { EvaluacionDomiciliariaDocumentoDTO } from 'app/core/model/request/ia/EvaluacionDomiciliariaDocumentoDTO.model';
import { DatosFamiliaresDocumentoDTO } from 'app/core/model/request/ia/DatosFamiliaresDocumentoDTO.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { SeguimientoEducativoDTO } from 'app/core/model/both/ia/seguimientoEducativoDTO.model';
import { SeguimientoEducativoLaboralOtrosDTO } from 'app/core/model/both/ia/seguimientoEducativoLaboralOtrosDTO.model';
import { EvaluacionSeguimientoEducativoLaboralService } from 'app/modules/seguridad/services/evaluacionSeguimiento.service';
import { SeguimientoEducativoLaboralOtrosService } from 'app/modules/seguridad/services/seguimientoEducativoLaboralOtros.service';
import { SeguimientoSocialService } from 'app/modules/administracion/services/seguimientoSocial.service';
import { SancionDisciplinariaService } from 'app/modules/administracion/services/sancionDisciplinaria.service';
import { InformeFinalAbiertoDTO } from 'app/core/model/both/informeFinalAbiertoDTO.model';
import { InformeFinalAbiertoService } from 'app/modules/seguridad/services/informeFinalAbierto.service';
import { SancionDisciplinariaDTO } from 'app/core/model/both/ia/SancionDisciplinariaDTO.model';

@Component({
  selector: 'app-subida-documento-generico',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    SubidaDeDocumentosComponent
  ],
  templateUrl: './subida-documento-generico.component.html',
  styleUrl: './subida-documento-generico.component.scss'
})
export class SubidaDocumentoGenericoComponent implements OnInit {
  // Parámetros recibidos desde la navegación
  nemonicoMenu: string;
  nemonicoCarpeta: string;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  item: any; // El objeto para el cual se subirá el documento
  tipoServicio: string; // Define el tipo de servicio: 'encuesta', 'evaluacionDomiciliaria', etc.
  seccionTipoDocumento: string = etiquetasModel.SECCION_FICHA_IDENT_EVALUACIONES;
  tituloPantalla: string = 'Subir documento';

  @ViewChild(SubidaDeDocumentosComponent)
  componenteSubidaDocumentos: SubidaDeDocumentosComponent;

  constructor(
    public dialogRef: MatDialogRef<SubidaDocumentoGenericoComponent>,
    private dialogMensajeService: DialogMensajeService,
    private encuestaService: EncuestaService,
    private evaluacionDomiciliariaService: EvaluacionDomiciliariaService,
    private seguimientoEducativoService: SeguimientoEducativoLaboralOtrosService,
    private seguimientoSocialService: SeguimientoSocialService,
    private datosFamiliaresService: DatosFamiliaresService,
    private informeFinalAbiertoService: InformeFinalAbiertoService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    private sancionService: SancionDisciplinariaService,
    @Inject(MAT_DIALOG_DATA) public data: {
      item: any,
      nemonicoMenu: string,
      nemonicoCarpeta: string,
      tipoServicio: string,
      seccionTipoDocumento: string,
      tituloPantalla: string
    }
  ) {

    this.item = data.item;
    this.nemonicoMenu = data.nemonicoMenu;
    this.nemonicoCarpeta = data.nemonicoCarpeta;
    this.tipoServicio = data.tipoServicio || 'encuesta';
    this.seccionTipoDocumento = data.seccionTipoDocumento || etiquetasModel.SECCION_FICHA_IDENT_EVALUACIONES;
    this.tituloPantalla = data.tituloPantalla || 'Subir documento';;
  }

  ngOnInit() {
    this.obtenerTiposDeDocumentos();
  }

  // Método llamado por el componente hijo cuando se seleccionan documentos para subir
  subirDocumento(documentos: DocumentoSubido[]) {
    if (documentos.length === 0) {
      this.dialogMensajeService.mensajeError('No se han seleccionado documentos para subir.');
      return;
    }

    let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento...");

    switch (this.tipoServicio) {
      case 'evaluacionDomiciliaria':
        this.subirDocumentoEvaluacionDomiciliaria(documentos[0], load);
        break;
      case 'seguimientoEducativo':
        this.subirDocumentoSeguimientoEducativo(documentos, load);
        break;
      case 'seguimientoSocial':
        this.subirDocumentoSeguimientoSocial(documentos, load);
        break;
      case 'sancionDisciplinaria':
        this.subirDocumentoSancionDisciplinaria(documentos, load);
        break;
      case 'personaRelacionada':
        this.subirDocumentoPersonaRelacionada(documentos[0], load);
        break;
      case 'informeFinalSOA':
        this.subirDocumentoInformeFinalSOA(documentos, load);
        break;
      case 'encuesta':
      default:
        this.subirDocumentoEncuesta(documentos, load);
        break;
    }
  }

  // Maneja la subida de documentos para el servicio de encuesta
  private subirDocumentoEncuesta(documentosSubidos: DocumentoSubido[], loadRef: any) {
    let files = documentosSubidos?.map(doc => doc.documento) ?? null;
    let encabezadoDTO: any = this.item;
    encabezadoDTO.evaluacionDocumentoDTO = new EvaluacionDocumentoDTO();
    encabezadoDTO.evaluacionDocumentoDTO.nemonicoCarpeta = this.nemonicoCarpeta;
    encabezadoDTO.evaluacionDocumentoDTO.documentoDTOList = documentosSubidos?.map(doc => doc.documentoDTO)

    this.encuestaService.subirDocumentos(
      encabezadoDTO,
      files,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<Boolean>) => {
        this.manejarRespuestaExitosa(respuesta, loadRef);
      },
      error: (error: any) => {
        this.manejarError(error, loadRef);
      }
    });
  }

  private subirDocumentoPersonaRelacionada(documentoSubido: DocumentoSubido, loadRef: any) {
    // Verificar que tenemos los datos necesarios
    if (!this.item || !this.item.tokenIdentificador) {
      loadRef.close();
      this.dialogMensajeService.mensajeError('No se encontró un identificador válido para la persona relacionada.');
      return;
    }

    // Crear el DTO con la información mínima necesaria
    const datosFamiliaresDocumentoDTO = new DatosFamiliaresDocumentoDTO();
    datosFamiliaresDocumentoDTO.tokenIdentificadorDatosFamiliares = this.item.tokenIdentificador;
    datosFamiliaresDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

    // Realizar la solicitud al backend
    this.datosFamiliaresService.subirDocumento(
      documentoSubido.documento,
      datosFamiliaresDocumentoDTO,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<DocumentoDTO>) => {
        console.log('Respuesta del servidor:', respuesta);
        this.manejarRespuestaExitosa(respuesta, loadRef);
      },
      error: (error: any) => {
        console.error('Error detallado al subir documento:', error);
        this.manejarError(error, loadRef);
      }
    });
  }

  // Maneja la subida de documentos para el servicio de evaluación domiciliaria
  private subirDocumentoEvaluacionDomiciliaria(documentoSubido: DocumentoSubido, loadRef: any) {
    const evaluacionDomiciliariaDocumentoDTO = new EvaluacionDomiciliariaDocumentoDTO();
    evaluacionDomiciliariaDocumentoDTO.tokenIdentificadorEvaluacionDomiciliaria = this.item.tokenIdentificador;
    evaluacionDomiciliariaDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

    this.evaluacionDomiciliariaService.subirDocumento(
      documentoSubido.documento,
      evaluacionDomiciliariaDocumentoDTO,
      this.nemonicoMenu
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<DocumentoDTO>) => {
        this.manejarRespuestaExitosa(respuesta, loadRef);
      },
      error: (error: any) => {
        this.manejarError(error, loadRef);
      }
    });
  }

  // Maneja la subida de documentos para el servicio de evaluación domiciliaria
  private subirDocumentoSeguimientoEducativo(documentosSubidos: DocumentoSubido[], loadRef: any) {

      if (documentosSubidos.length > 0) {

          let files = documentosSubidos?.map(doc => doc.documento) ?? null;

          const seguimientoDTO = new SeguimientoEducativoDTO();
          seguimientoDTO.tokenIdentificadorSeguimiento = this.item.tokenIdentificador;
          seguimientoDTO.documentoDTOList = documentosSubidos?.map(doc => doc.documentoDTO)

          this.seguimientoEducativoService.subirDocumentos(
              seguimientoDTO,
              files,
              this.nemonicoMenu
          ).subscribe(
              {
                  next: (respuesta: RespuestaPorDefecto<Boolean>) => {
                      this.manejarRespuestaExitosa(respuesta, loadRef);
                  },
                  error: (error: any) => {
                      this.manejarError(error, loadRef);
                  }
              }
          );
      } else {
          this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
      }
  }

  // Maneja la subida de documentos para el servicio de evaluación domiciliaria
  private subirDocumentoSeguimientoSocial(documentosSubidos: DocumentoSubido[], loadRef: any) {

    if (documentosSubidos.length > 0) {

      let files = documentosSubidos?.map(doc => doc.documento) ?? null;

      const seguimientoDTO = new SeguimientoEducativoDTO();
      seguimientoDTO.tokenIdentificadorSeguimiento = this.item.tokenIdentificador;
      seguimientoDTO.documentoDTOList = documentosSubidos?.map(doc => doc.documentoDTO)

      this.seguimientoSocialService.subirDocumentos(
        seguimientoDTO,
        files,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (respuesta: RespuestaPorDefecto<Boolean>) => {
            this.manejarRespuestaExitosa(respuesta, loadRef);
          },
          error: (error: any) => {
            this.manejarError(error, loadRef);
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
    }
  }


  // Maneja la subida de documentos para el servicio de evaluación domiciliaria
  private subirDocumentoSancionDisciplinaria(documentosSubidos: DocumentoSubido[], loadRef: any) {

    if (documentosSubidos.length > 0) {

      let files = documentosSubidos?.map(doc => doc.documento) ?? null;

      const sancionDisciplinariaDTO = new SancionDisciplinariaDTO();
      sancionDisciplinariaDTO.tokenIdentificador = this.item.tokenIdentificador;
      sancionDisciplinariaDTO.documentoDTOList = documentosSubidos?.map(doc => doc.documentoDTO)

      this.sancionService.subirDocumentos(
        sancionDisciplinariaDTO,
        files,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (respuesta: RespuestaPorDefecto<Boolean>) => {
            this.manejarRespuestaExitosa(respuesta, loadRef);
          },
          error: (error: any) => {
            this.manejarError(error, loadRef);
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
    }
  }


  private subirDocumentoInformeFinalSOA(documentosSubidos: DocumentoSubido[], loadRef: any) {

    if (documentosSubidos.length > 0) {

      let files = documentosSubidos?.map(doc => doc.documento) ?? null;

      const informeDTO = new InformeFinalAbiertoDTO();
      informeDTO.tokenIdentificador = this.item.tokenIdentificador;
      informeDTO.documentoDTOList = documentosSubidos?.map(doc => doc.documentoDTO)
      console.log(informeDTO);
      this.informeFinalAbiertoService.subirDocumentos(
        informeDTO,
        files,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (respuesta: RespuestaPorDefecto<Boolean>) => {
            this.manejarRespuestaExitosa(respuesta, loadRef);
          },
          error: (error: any) => {
            this.manejarError(error, loadRef);
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
    }
  }

  // Maneja respuestas exitosas del servidor
  private manejarRespuestaExitosa(respuesta: any, loadRef: any) {
    loadRef.close();

    if (!respuesta.exito) {
      this.dialogMensajeService.mensajeError(
        'Hubo un problema al subir el documento. ' + respuesta.mensaje
      );
      return;
    }

    this.dialogMensajeService.mensajeExitoso(
      'Documento subido',
      'Documento subido correctamente.'
    ).afterClosed().subscribe(() => {
      this.cerrar();
    });
  }

  // Maneja errores durante la subida
  private manejarError(error: any, loadRef: any) {
    loadRef.close();
    this.dialogMensajeService.mensajeError(
      'Hubo un problema al subir el documento. Inténtalo de nuevo.'
    );
  }

  // Obtiene los tipos de documentos disponibles para la sección especificada
  obtenerTiposDeDocumentos() {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(
      this.seccionTipoDocumento,
      ''
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
        if (!respuesta.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        let tiposArchivos = respuesta.data;

        if (tiposArchivos.length == 0) {
          this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
          return;
        }

        // Mapea los datos recibidos al formato esperado por el componente
        this.tiposDeDocumentosSistema =
          tiposArchivos.map(
            (tipoArch) => {
              let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
              let tipoDeDocumento = new TipoDeDocumento();
              tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
              tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
              tipoDeDocumento.requerido = tipoArch.requerido;
              tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
              tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

              return tipoDeDocumento;
            }
          );
      },
      error: (error: any) => {
        this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
      }
    });
  }

  cerrar() {
    this.dialogRef.close();
  }
}
