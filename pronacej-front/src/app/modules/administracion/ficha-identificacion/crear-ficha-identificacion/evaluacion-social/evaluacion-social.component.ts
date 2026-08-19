import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
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
import { EvaluacionDomiciliariaComponent } from '../evaluacion-domiciliaria/evaluacion-domiciliaria.component';
import { OrientacionConsejeriaFamiliarComponent } from '../orientacion-consejeria-familiar/orientacion-consejeria-familiar.component';
import { EvalSeguSociComponent } from '../eval-segu-soci/eval-segu-soci.component';
import { InformeVisitasComponent } from '../informe-visitas/informe-visitas.component';
import { TabService } from 'app/core/services/tab.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { environment } from 'environments/environment';
import etiquetasModel from 'app/core/etiquetas.model';
import { SeguEducLaboOtroComponent } from '../segu-educ-labo-otro/segu-educ-labo-otro.component';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { ActivatedRoute, Router } from '@angular/router';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EvaluacionDomiciliariaService } from 'app/modules/seguridad/services/EvaluacionDomiciliaria.service';
import { EvaluacionDomiciliariaDocumentosRequest } from 'app/core/model/request/ia/EvaluacionDomiciliariaDocumentosRequest.model';
import { EvaluacionDomiciliariaDTO } from 'app/core/model/both/EvaluacionDomiciliariaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-evaluacion-social',
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
    EvaluacionDomiciliariaComponent,
    OrientacionConsejeriaFamiliarComponent,
    EvalSeguSociComponent,
    SeguEducLaboOtroComponent,
    InformeVisitasComponent,
  ],
  templateUrl: './evaluacion-social.component.html',
  styleUrl: './evaluacion-social.component.scss'
})
export class EvaluacionSocialComponent implements OnInit {
  tituloPantalla = 'Evaluación domiciliaria';
  indiceTabSeleccionado: number = 0;
  centro: JerarquiaDTO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA;
  
  identificadorFichaPrincipal: string;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  evaluacionesDomiciliarias: EvaluacionDomiciliariaDTO[] = [];
  cargandoDocumentos: boolean = false;

  @ViewChild('documentosComp')
  tablaDocumentos: DocumentosSubidosTablaComponent;

  constructor(
    private servicioTab: TabService,
    private servicioJerarquia: JerarquiaService,
    private servicioEvaluacionDomiciliaria: EvaluacionDomiciliariaService,
    private servicioMensajes: DialogMensajeService,
    private rutaActiva: ActivatedRoute,
    public router: Router,  // No traducir este componente, porque probocará que no se seleccione en el menu latera
    private authSerguridadServicio: AuthSerguridadServicio,
  ) {}

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_EVALUACION_SOCIAL"
    );
    this.servicioTab.tabIndex$.subscribe(indice => {
      this.indiceTabSeleccionado = indice;
    });
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarCentro();
  }

  ngAfterViewInit(): void {
    if (this.indiceTabSeleccionado === 4 || this.indiceTabSeleccionado === 3) {
      // Si estamos en la pestaña de documentos
      this.obtenerEvaluacionesDomiciliarias();
    }
  }

  cargarCentro() {
    this.servicioJerarquia
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.servicioJerarquia.checkError(respuesta);
            return;
          }

          if (!environment.production) {
            console.log('centro cargado:', respuesta.data);
          }

          this.centro = respuesta.data;
        },
        error: (error: any) => {
          this.servicioJerarquia.checkError(error);
        },
      });
  }

  get noEsSOA(): boolean {
    return this.centro?.jerarquiaPadre?.nemonico !== 'SOA';
  }

  alCambiarTab(evento: any): void {
    const indiceTab = evento.index;
    
    if (this.noEsSOA) {
      switch (indiceTab) {
        case 0:
          this.tituloPantalla = 'Evaluación domiciliaria';
          break;
        case 1:
          this.tituloPantalla = 'Orientación y consejería familiar';
          break;
        case 2:
          this.tituloPantalla = 'Evaluación de seguridad social';
          break;
        case 3:
          this.tituloPantalla = 'Autorización de visitantes';
          break;
        case 4:
          this.tituloPantalla = 'Documentos';
          this.obtenerEvaluacionesDomiciliarias();
          break;
        default:
          this.tituloPantalla = '';
      }
    } else {
      switch (indiceTab) {
        case 0:
          this.tituloPantalla = 'Evaluación de visita domiciliaria';
          break;
        case 1:
          this.tituloPantalla = 'Orientación y consejería familiar';
          break;
        case 2:
          this.tituloPantalla = 'Seguimiento educativo/laboral/otros';
          break;
        case 3:
          this.tituloPantalla = 'Documentos';
          this.obtenerEvaluacionesDomiciliarias();
          break;
        default:
          this.tituloPantalla = '';
      }
    }
  }

  cambiarPestana(indice: number) {
    this.indiceTabSeleccionado = indice;
  }

  // Obtener las evaluaciones domiciliarias para luego obtener sus documentos
  obtenerEvaluacionesDomiciliarias() {
    this.cargandoDocumentos = true;
    
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100; // Un número suficientemente grande para obtener todas
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    this.servicioEvaluacionDomiciliaria.obtenerEvaluacionesDomiciliariasPaginado(
      solicitudPaginacion, 
      etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA
    ).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionDomiciliariaDTO>>) => {
        if (respuesta.exito && respuesta.data.data.length > 0) {
          this.evaluacionesDomiciliarias = respuesta.data.data;
          // Una vez que tenemos las evaluaciones, obtenemos los documentos
          this.obtenerDocumentos();
        } else {
          this.cargandoDocumentos = false;
          // No hay evaluaciones domiciliarias, por lo que no habrá documentos
          if (this.tablaDocumentos) {
            this.tablaDocumentos.actualizarTabla([], 0);
          }
        }
      },
      error: (error: any) => {
        this.cargandoDocumentos = false;
        this.servicioMensajes.mensajeError(
          'Hubo un problema al recuperar las evaluaciones domiciliarias.'
        );
      }
    });
  }

  obtenerDocumentos() {
    if (!this.tablaDocumentos) {
      this.cargandoDocumentos = false;
      return;
    }
    
    // Si no hay evaluaciones, no hay documentos que mostrar
    if (this.evaluacionesDomiciliarias.length === 0) {
      this.tablaDocumentos.actualizarTabla([], 0);
      this.cargandoDocumentos = false;
      return;
    }
    
    const pagina = this.tablaDocumentos.page || 0;
    const tamañoPagina = this.tablaDocumentos.pageSize || 10;

    // Crear un arreglo para almacenar todos los documentos
    let todosLosDocumentos: DocumentoDTO[] = [];
    let evaluacionesCompletadas = 0;
    
    // Iteramos por cada evaluación para obtener sus documentos
    this.evaluacionesDomiciliarias.forEach(evaluacion => {
      let solicitudDocumentos = new EvaluacionDomiciliariaDocumentosRequest();
      solicitudDocumentos.page = 0;
      solicitudDocumentos.size = 100; // Obtener todos los documentos
      solicitudDocumentos.tokenIdentificadorEvaluacionDomiciliaria = evaluacion.tokenIdentificador;
      
      this.servicioEvaluacionDomiciliaria.obtenerDocumentos(
        solicitudDocumentos,
        etiquetasModel.NEMONICO_MENU_EVALUACION_DOMICILIARIA
      ).subscribe({
        next: (respuesta) => {
          evaluacionesCompletadas++;
          
          if (respuesta.exito && respuesta.data?.data) {
            // Agregamos los documentos encontrados al arreglo
            todosLosDocumentos = [...todosLosDocumentos, ...respuesta.data.data];
          }
          
          // Si esta es la última evaluación, actualizamos la tabla
          if (evaluacionesCompletadas === this.evaluacionesDomiciliarias.length) {
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
          evaluacionesCompletadas++;
          // Incluso si hay error, seguimos con el proceso
          if (evaluacionesCompletadas === this.evaluacionesDomiciliarias.length) {
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
    this.obtenerDocumentos();
  }
}
