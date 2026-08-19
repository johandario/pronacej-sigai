import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { DatosFamiliaresComponent } from '../datos-familiares/datos-familiares.component';
import { SituacionEducativaLaboralComponent } from '../situacion-educativa-laboral/situacion-educativa-laboral.component';
import { SituEconEntoSociComponent } from '../situ-econ-ento-soci/situ-econ-ento-soci.component';
import { SituacionRiesgoSocialComponent } from '../situacion-riesgo-social/situacion-riesgo-social.component';
import { TabService } from 'app/core/services/tab.service';
import { ActivatedRoute, Router } from '@angular/router';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { DatosFamiliaresDocumentosRequest } from 'app/core/model/request/ia/DatosFamiliaresDocumentosRequest.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { environment } from 'environments/environment';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { DatosFamiliaresDocumentoDTO } from 'app/core/model/request/ia/DatosFamiliaresDocumentoDTO.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-ficha-psicosocial',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDatepickerModule,
    MatBottomSheetModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatIconModule,
    DatosFamiliaresComponent,
    SituacionEducativaLaboralComponent,
    SituEconEntoSociComponent,
    SituacionRiesgoSocialComponent,
    DocumentosSubidosTablaComponent,
    SubidaDeDocumentosComponent
  ],
  templateUrl: './ficha-psicosocial.component.html',
  styleUrl: './ficha-psicosocial.component.scss'
})
export class FichaPsicosocialComponent {
  tituloPantalla = 'Composición Familiar';
  indiceTabSeleccionado: number = 0;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR;
  nemonicoMenuSituacionFamiliar = etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR;

  identificadorFichaPrincipal: string;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  personasRelacionadas: PersonaRelacionadaDTO[] = [];
  cargandoDocumentos: boolean = false;

  @ViewChild('documentosComp')
  tablaDocumentos: DocumentosSubidosTablaComponent;

  constructor(
    private servicioTab: TabService,
    private rutaActiva: ActivatedRoute,
    private servicioPersonaRelacionada: DatosFamiliaresService,
    private servicioTiposDocumento: TipoDeIdentificacionTipoDeDocumentoService,
    private servicioMensajes: DialogMensajeService,
    public router: Router,  // No traducir este componente, porque probocará que no se seleccione en el menu latera
    private authSerguridadServicio: AuthSerguridadServicio,    
  ) {

  }

  
  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_FICHA_PSICOSOCIAL"
    );
    this.servicioTab.tabIndex$.subscribe(indice => {
      this.indiceTabSeleccionado = indice;
    });

    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
  }

  ngAfterViewInit(): void {
    if (this.indiceTabSeleccionado === 4) {
      // Si estamos en la pestaña de documentos
      this.obtenerTiposDeDocumentos();
      this.obtenerPersonasRelacionadas();
    }
  }

  onTabChange(event: any): void {
    const tabIndex = event.index;

    switch (tabIndex) {
      case 0:
        this.tituloPantalla = 'Composición familiar';
        break;
      case 1:
        this.tituloPantalla = 'Situación económica/entorno social';
        break;
      case 2:
        this.tituloPantalla = 'Situación educativa/laboral/ocio';
        break;
      case 3:
        this.tituloPantalla = 'Situación de riesgo social';
        break;
      case 4:
        this.tituloPantalla = 'Documentos';
        this.obtenerTiposDeDocumentos();
        // this.obtenerPersonasRelacionadas();
        this.obtenerDocumentosFichaPsicosocial();
        break;
      default:
        this.tituloPantalla = '';
    }
  }

  cambiarPestana(indice: number) {
    this.indiceTabSeleccionado = indice;
  }

  obtenerTiposDeDocumentos() {
    this.servicioTiposDocumento.obtenerTiposDeDocumentos(
      etiquetasModel.SECCION_FICHA_IDENT_PERSONA_RELACIONADA,
      ''
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        let tiposArchivos = respuesta.data;

        if (tiposArchivos.length == 0) {
          this.servicioMensajes.mensajeError("No se ha configurado los tipos de documentos para esta sección");
          return;
        }

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
        this.servicioTiposDocumento.checkError(error);
      }
    });
  }

  obtenerPersonasRelacionadas() {
    this.cargandoDocumentos = true;

    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100; // Un número suficientemente grande para obtener todas
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    this.servicioPersonaRelacionada.obtenerPersonasRelacionadas(
      solicitudPaginacion
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
        if (respuesta.exito && respuesta.data.data.length > 0) {
          this.personasRelacionadas = respuesta.data.data;
          // Una vez que tenemos las personas, obtenemos los documentos
          this.obtenerDocumentos();
        } else {
          this.cargandoDocumentos = false;
          // No hay personas relacionadas, por lo que no habrá documentos
          if (this.tablaDocumentos) {
            this.tablaDocumentos.actualizarTabla([], 0);
          }
        }
      },
      error: (error: any) => {
        this.cargandoDocumentos = false;
        this.servicioMensajes.mensajeError(
          'Hubo un problema al recuperar las personas relacionadas.'
        );
      }
    });
  }

  obtenerDocumentos() {
    if (!this.tablaDocumentos) {
      this.cargandoDocumentos = false;
      return;
    }

    // Si no hay personas, no hay documentos que mostrar
    if (this.personasRelacionadas.length === 0) {
      this.tablaDocumentos.actualizarTabla([], 0);
      this.cargandoDocumentos = false;
      return;
    }

    const pagina = this.tablaDocumentos.page || 0;
    const tamañoPagina = this.tablaDocumentos.pageSize || 10;

    // Crear un arreglo para almacenar todos los documentos
    let todosLosDocumentos: DocumentoDTO[] = [];
    let personasCompletadas = 0;

    // Iteramos por cada persona para obtener sus documentos
    this.personasRelacionadas.forEach(persona => {
      let solicitudDocumentos = new DatosFamiliaresDocumentosRequest();
      solicitudDocumentos.page = 0;
      solicitudDocumentos.size = 100; // Obtener todos los documentos
      solicitudDocumentos.tokenIdentificadorDatosFamiliares = persona.tokenIdentificador;

      this.servicioPersonaRelacionada.obtenerDocumentos(
        solicitudDocumentos,
        etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR
      ).subscribe({
        next: (respuesta) => {
          personasCompletadas++;

          if (respuesta.exito && respuesta.data?.data) {
            // Agregamos los documentos encontrados al arreglo
            todosLosDocumentos = [...todosLosDocumentos, ...respuesta.data.data];
          }

          // Si esta es la última persona, actualizamos la tabla
          if (personasCompletadas === this.personasRelacionadas.length) {
            this.cargandoDocumentos = false;
            // Aplicamos paginación manual si es necesario
            const inicio = pagina * tamañoPagina;
            const fin = inicio + tamañoPagina;
            const documentosPaginados = todosLosDocumentos.slice(inicio, fin);

            this.tablaDocumentos.actualizarTabla(
              documentosPaginados,
              todosLosDocumentos.length
            );
          }
        },
        error: (error) => {
          personasCompletadas++;
          // Incluso si hay error, seguimos con el proceso
          if (personasCompletadas === this.personasRelacionadas.length) {
            this.cargandoDocumentos = false;
            this.tablaDocumentos.actualizarTabla(
              todosLosDocumentos,
              todosLosDocumentos.length
            );
          }
        }
      });
    });
  }

  manejarEventoPaginacionDocumentos(evento: PageEvent) {
    if (!this.tablaDocumentos) return;

    this.tablaDocumentos.page = evento.pageIndex;
    this.tablaDocumentos.pageSize = evento.pageSize;
    this.obtenerDocumentosFichaPsicosocial();
  }

  obtenerDocumentosFichaPsicosocial() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let fichaIngresoDocumentosRequest = new DatosFamiliaresDocumentosRequest();
    fichaIngresoDocumentosRequest.page = page;
    fichaIngresoDocumentosRequest.size = pageSize;
    fichaIngresoDocumentosRequest.textoBuscar =
      this.tablaDocumentos.textoBuscar;
    // fichaIngresoDocumentosRequest.tokenIdentificadorFichaIngreso =
    //     this.fichaIngresoDTO.tokenIdentificador;
    fichaIngresoDocumentosRequest.tokenFichaIdentificacion = this.identificadorFichaPrincipal;

    this.servicioPersonaRelacionada
      .obtenerDocumentosFichaPsicosocial(fichaIngresoDocumentosRequest, this.nemonicoMenuSituacionFamiliar)
      .subscribe({
        next: (
          response: RespuestaPorDefecto<
            PaginacionResponse<DocumentoDTO>
          >
        ) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.servicioPersonaRelacionada.checkError(response);
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.servicioPersonaRelacionada.checkError(error);
        },
      });
  }

  subirArchivosEvent(documentos: DocumentoSubido[]) {
    if (documentos && documentos.length > 0) {
      for (let documentoSubido of documentos) {
        let fichaIngresoDocumento = new DatosFamiliaresDocumentoDTO();
        // fichaIngresoDocumento.tokenIdentificadorFichaIngreso =
        //     this.fichaIngresoDTO.tokenIdentificador;
        fichaIngresoDocumento.tokenFichaIdentificacion = this.identificadorFichaPrincipal;
        fichaIngresoDocumento.documentoDTO =
          documentoSubido.documentoDTO;

        let load = this.servicioMensajes.mensajeLoading(
          'Subiendo el documento: ' + documentoSubido.documento.name
        );
        this.servicioPersonaRelacionada
          .subirDocumentoFichaPsicosocial(
            documentoSubido.documento,
            fichaIngresoDocumento,
            this.nemonicoMenuSituacionFamiliar
          )
          .subscribe({
            next: (response: RespuestaPorDefecto<DocumentoDTO>) => {
              load.close();
              if (!response.exito) {
                this.servicioPersonaRelacionada.checkError(response);
                return;
              }

              //Refrescar la tabla de documentos
              this.obtenerDocumentosFichaPsicosocial();
            },
            error: (error: any) => {
              load.close();
              this.servicioPersonaRelacionada.checkError(error);
            },
          });
      }
    } else {
      this.servicioMensajes.mensajeError(
        'No se obtenieron documentos para ser subidos'
      );
    }
  }
}
