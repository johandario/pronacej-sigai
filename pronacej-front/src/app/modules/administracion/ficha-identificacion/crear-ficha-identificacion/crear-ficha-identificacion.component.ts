import { ChangeDetectorRef, Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDrawer, MatSidenavModule } from '@angular/material/sidenav';
import { FuseMediaWatcherService } from '@fuse/services/media-watcher/media-watcher.service';
import { catchError, Observable, Subject, takeUntil, tap, throwError } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { DatosGeneralesComponent } from './datos-generales/datos-generales.component';
import { FichaIngresoComponent } from "./ficha-ingreso/ficha-ingreso.component";
import { ActivatedRoute, Router, RouterModule, UrlSegment } from '@angular/router';
import { EntregaRecepcionUniformesArticulosComponent } from './entrega-recepcion-uniformes-articulos/entrega-recepcion-uniformes-articulos.component';
import { EvaluacionMedicaComponent } from './evaluacion-medica/evaluacion-medica.component';
import { PlanTratamientoComponent } from './plan-tratamiento/plan-tratamiento.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FichaIdentificacionService } from '../../services/fichaIdentificacion.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { HistorialDeFotosFichaIdentificacionDTO } from 'app/core/model/both/ia/HistorialDeFotosFichaIdentificacionDTO.model';
import { HistorialDeFotosFichaIdentificacionRequest } from 'app/core/model/request/ia/HistorialDeFotosFichaIdentificacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { HistorialDeFotosFichaIdentificacionService } from '../../services/HistorialDeFotosFichaIdentificacionService.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoService } from 'app/core/services/documento.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuseCardComponent } from '@fuse/components/card/card.component';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { ExpedienteMatrizDetalleDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { MenuService } from 'app/modules/seguridad/services/menu.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-crear-ficha-identificacion',
  standalone: true,
  imports: [
    MatDrawer,
    CommonModule,
    MatIcon,
    MatSidenavModule,
    DatosGeneralesComponent,
    RouterModule,
    FichaIngresoComponent,
    EvaluacionMedicaComponent,
    EntregaRecepcionUniformesArticulosComponent,
    PlanTratamientoComponent,
    FuseCardComponent
  ],
  templateUrl: './crear-ficha-identificacion.component.html',
  styleUrl: './crear-ficha-identificacion.component.scss'
})

export class CrearFichaIdentificacionComponent {

  @ViewChild('drawer') drawer: MatDrawer;
  drawerMode: 'over' | 'side' = 'side';
  drawerOpened: boolean = true;
  selectedPanel: string = 'account';
  private _unsubscribeAll: Subject<any> = new Subject<any>();
  @ViewChild(FuseCardComponent) fuseCard: FuseCardComponent;

  fotografia: boolean = true;

  paneles: any[] = [];
  declare panelSeleccionado: any;

  uuid_fp: string; // Variable que almacena el uuid de la ficha principal para redirigir en las demas opciones

  @Output() completoOperacion = new EventEmitter<boolean>();

  fichaIdentificacionDTO: FichaIdentificacionDTO;
  dataList: HistorialDeFotosFichaIdentificacionDTO[] = [];
  fotoPerfil: string;

  centro: JerarquiaDTO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

  idPanelDocumentacion = "idPanelDocumentacion";
  funcionarioActivo: FuncionarioDTO;

  currentFace: 'front' | 'back' = 'front';

  expedienteDetalle: ExpedienteMatrizDetalleDTO;
  fechaSalida: string;
  diasParaSalida: number;
  ingreso: FichaIngresoDTO = new FichaIngresoDTO;

  constructor(
    private _fuseMediaWatcherService: FuseMediaWatcherService,
    private _changeDetectorRef: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private historialDeFotosFichaIdentificacionService: HistorialDeFotosFichaIdentificacionService,
    private documentoService: DocumentoService,
    private funcionesUtils: FuncionesUtils,
    private jerarquiaService: JerarquiaService,
    private funcionarioService: FuncionarioService,
    private expdienteMatrizService: ExpedienteMatrizService,
    private fichaIngresoService: FichaIngresoService,
    private authSeguridadService: AuthSerguridadServicio,
  ) {
    this.paneles = [
      {
        id: 'fichaPrincipal',
        icon: 'heroicons_outline:user-circle',
        title: 'Ficha principal',
        mostrar: true
      },
      {
        id: 'fichaDeIngreso',
        icon: 'heroicons_outline:bell',
        title: 'Ficha ingreso',
        mostrar: false
      },
      // {
      //   id: 'fichaJudicial',
      //   icon: 'heroicons_outline:bell',
      //   title: 'Ficha Judicial',
      //   mostrar: false
      // },
      {
        id: 'expediente',
        icon: 'heroicons_outline:lock-closed',
        title: 'Expedientes legales',
        mostrar: false
      },
      {
        id: 'entregaRecepcionUniformesArticulos',
        icon: 'heroicons_outline:arrows-up-down',
        title: 'Entrega/Retiro de pertenencias',
        mostrar: false
      },
      {
        id: 'fichaPsicosocial',
        icon: 'heroicons_outline:clipboard-document-list',
        title: 'Ficha psicosocial',
        mostrar: false
      },      
      {
        id: 'evaluacionSocial',
        icon: 'heroicons_outline:document-check',
        title: 'Evaluación social',
        mostrar: false
      },
      {
        id: 'evaluacionMedica',
        icon: 'heroicons_outline:heart',
        title: 'Historia clínica',
        mostrar: false
      },
      {
        id: 'historiaClinica',
        icon: 'heroicons_outline:heart',
        title: 'Evaluación de salud',
        mostrar: false
      },
      {
        id: 'evaluacionPsicologica',
        icon: 'heroicons_outline:clipboard-document-list',
        title: 'Evaluaciones psicológicas',
        mostrar: false
      },
      {
        id: 'nivelDeRiesgo',
        icon: 'heroicons_outline:clipboard-document-list',
        title: 'Valoración de nivel de riesgo',
        mostrar: false
      },
      {
        id: 'evaluacionConductual',
        icon: 'heroicons_outline:document-check',
        title: 'Evaluación conductual',
        mostrar: false
      },
      {
        id: 'planTratamiento',
        icon: 'heroicons_outline:clipboard-document-list',
        title: 'Plan de tratamiento individual',
        mostrar: false
      },
      {
        id: 'informes',
        icon: 'heroicons_outline:clipboard',
        title: 'Informes',
        mostrar: false
      },
      {
        id: 'salidas',
        icon: 'heroicons_outline:bell',
        title: 'Permiso de salida',
        mostrar: false
      },
      {
        id: 'sancionesDisciplinarias',
        icon: 'heroicons_outline:document-text',
        title: 'Sanciones disciplinarias',
        mostrar: false
      },
      {
        id: 'programaIntervencionRepentina',
        icon: 'heroicons_outline:clipboard',
        title: 'Programa de intervención intensiva',
        mostrar: false
      },
      {
        id: 'notificaciones',
        icon: 'heroicons_outline:bell',
        title: 'Notificaciones',
        mostrar: false
      },
      {
        id: 'preparacionEgreso',
        icon: 'heroicons_outline:clipboard',
        title: 'Preparación para el Egreso',
        mostrar: false
      },
      {
        id: 'informeFinal',
        icon: 'heroicons_outline:document-text',
        title: 'Informe final',
        mostrar: false
      },
      {
        id: 'postEgreso',
        icon: 'heroicons_outline:arrow-top-right-on-square',
        title: 'Post egreso',
        mostrar: false
      }, 
      {
        id: 'registroVisitas',
        icon: 'heroicons_outline:document-text',
        title: 'Registro de visitas',
        mostrar: true
      }, 
      {
        id: 'controlVisitas',
        icon: 'heroicons_outline:document-check',
        title: 'Control de visitas',
        mostrar: true
      }, 
      {
        id: 'trasladosFinalizados',
        icon: 'heroicons_outline:arrow-top-right-on-square',
        title: 'Flujos',
        mostrar: false
      },
      {
        id: 'fichaUbicacion',
        icon: 'heroicons_outline:building-office-2',
        title: 'Ubicaciones',
        mostrar: false
      },
      {
        id: this.idPanelDocumentacion,
        icon: "heroicons_outline:folder",
        title: 'Documentos',
        mostrar: false
      }
      // {
      //   id: 'contacto',
      //   icon: 'heroicons_outline:bell',
      //   title: 'Contacto',
      //   mostrar: false
      // },
    ];
  }


  async ngOnInit() {
    // Escuchar el parámetro 'uuid_fp' desde cualquier ruta hija, en este caso se obtendra de datos generales que es la unica a la que se llama agregando el uuid

    this.comprobarMenusMedicos();

    this.route.firstChild?.paramMap.subscribe(params => {
      this.uuid_fp = params.get('uuid_fp');
      this.cargarCentroYProcesar();

    });
    // this.fichaIngresoService.actualizarFichaIngreso$
    //   .pipe(takeUntil(this._unsubscribeAll))
    //   .subscribe(() => {
    //     if (this.uuid_fp) {
    //       this.obtenerFichaIngresoValida(this.uuid_fp).subscribe();
    //     }
    //   });


    this.selectedPanel = "fichaPrincipal";
    this._fuseMediaWatcherService.onMediaChange$
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(({ matchingAliases }) => {

        if (matchingAliases.includes('lg')) {
          this.drawerMode = 'side';
          this.drawerOpened = true;
        }
        else {
          this.drawerMode = 'over';
          this.drawerOpened = false;
        }

        this._changeDetectorRef.markForCheck();
      });
    this.fichaIdentificacionService.datosFicha$.subscribe((actualizar) => {
      if (actualizar) {
        if (this.uuid_fp) {
          this.obtenerFichaIdentificacionPorToken(this.uuid_fp);
        }
      }
    });
    this.fichaIdentificacionService.fotoPerfil$.subscribe((actualizar) => {
      console.log('actualizando foto')
      if (actualizar) {
        if (this.uuid_fp) {
          this.obtenerFotoPerfil(this.uuid_fp);
          // this.obtenerFotoPerfil(this.uuid_fp);
        }
      }
    });
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  comprobarMenusMedicos() {    
    this.authSeguridadService.verificarPermisoPantalla(etiquetasModel.NEMONICO_MENU_HISTORIA_CLINICA).subscribe(response => {
      if (!response.sinAcceso) {
        this.paneles.find(x => x.id == 'evaluacionMedica').mostrar = true;
      }
    });

    this.authSeguridadService.verificarPermisoPantalla(etiquetasModel.NEMONICO_MENU_EVALUACION_SALUD).subscribe(response => {
      if (!response.sinAcceso) {
        this.paneles.find(x => x.id == 'historiaClinica').mostrar = true;
      }
    });
  }

  activateRoute(componentActivated: any) {
    let activatedRoute = componentActivated?.router as Router;
    let url = activatedRoute?.url;
    this.panelSeleccionado = this.paneles.find(
      (panel) => url?.includes(panel.id)
    );
    this.selectedPanel = this.panelSeleccionado?.id;

  }

  trackByFn(index: number, item: any): any {
    return item.id || index;
  }

  goToPanel(panelId: string): void {
    // Lista de rutas para redirección externa
    const opcionesExternas = ['registroVisitas', 'controlVisitas'];

    if (opcionesExternas.includes(panelId)) {
      // abrir url externa en nueva tab en caso de que coincida con lista de rutas
      const url = 'https://modulo.pronacej.gob.pe/siserv';
      window.open(url, '_blank');
      panelId = 'fichaPrincipal';
    }

    // Lógica común
    this.selectedPanel = panelId;
    this.panelSeleccionado = this.paneles.find(
      (panel) => panel.id == panelId
    );
    //console.log(this.panelSeleccionado);

    if (this.uuid_fp) {
      this.router.navigate([panelId, this.uuid_fp], { relativeTo: this.route }).then(
        (result) => {
          console.log(result);
        }
      );
    } else {
      this.router.navigate([panelId], { relativeTo: this.route });
    }
  }

  getPanelInfo(id: string): any {
    return this.paneles.find(panel => panel.id === id);
  }

  // obtenerFichaIdentificacionPorToken(tokenIdentificador: string) {
  //   // let load = this.dialogMensajeService.mensajeLoading("Obteniendo la ficha de identificación..");
  //   this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(tokenIdentificador, this.nemonicoMenu).subscribe(
  //     {
  //       next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
  //         // load.close();
  //         // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

  //         if (!resp.exito) {
  //           return;
  //         }

  //         this.fichaIdentificacionDTO = resp.data;
  //         // this._changeDetectorRef.detectChanges();
  //         console.log('ficha identificacion', this.fichaIdentificacionDTO);

  //         if (this.fichaIdentificacionDTO.tokenIdentificador) {
  //           this.paneles.find(
  //             (panel) => panel.id == this.idPanelDocumentacion
  //           )!.mostrar = true;
  //         }

  //         if (this.centro.jerarquiaPadre?.nemonico === 'SOA' || this.centro.jerarquiaPadre?.nemonico === 'UAPISE') {
  //           if (this.fichaIdentificacionDTO.cantExpedientes > 0 && this.fichaIdentificacionDTO.cantIngresos > 0) {
  //             this.paneles.forEach(x => {
  //               if (x.id !== 'evaluacionMedica' && x.id !== 'historiaClinica' && x.id !== 'salidas' && x.id !== 'sancionesDisciplinarias') {
  //                 x.mostrar = true;
  //               }
  //             })
  //             this.obtenerFuncionario();
  //           } else {
  //             this.paneles.find(x => x.id == 'fichaDeIngreso').mostrar = true;
  //             this.paneles.find(x => x.id == 'expediente').mostrar = true;
  //           }
  //         } else {
  //           if (this.fichaIdentificacionDTO.cantExpedientes > 0 && this.fichaIdentificacionDTO.cantIngresos > 0 && this.fichaIdentificacionDTO.cantPertenencias) {
  //             this.paneles.forEach(x => {
  //               this.paneles.forEach(x => {
  //                 if (x.id !== 'evaluacionMedica' && x.id !== 'historiaClinica') {
  //                   x.mostrar = true;
  //                 }
  //               })
  //             })
  //             this.obtenerFuncionario();
  //           } else {
  //             this.paneles.find(x => x.id == 'fichaDeIngreso').mostrar = true;
  //             this.paneles.find(x => x.id == 'expediente').mostrar = true;
  //             this.paneles.find(x => x.id == 'entregaRecepcionUniformesArticulos').mostrar = true;
  //           }
  //         }

  //         this.obtenerDetalleExpediente(this.uuid_fp);
  //       },
  //       error: (error: any) => {
  //         // load.close();

  //         this.fichaIdentificacionService.checkError(error);
  //       }
  //     }
  //   );
  // }

  obtenerFichaIdentificacionPorToken(tokenIdentificador: string) {
  this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(
    tokenIdentificador,
    this.nemonicoMenu
  ).subscribe({
    next: (resp: RespuestaPorDefecto<FichaIdentificacionDTO>) => {

      if (!resp.exito) {
        return;
      }

      this.fichaIdentificacionDTO = resp.data;
      console.log('ficha identificacion', this.fichaIdentificacionDTO);

      // Mostrar documentos si ya existe token
      if (this.fichaIdentificacionDTO.tokenIdentificador) {
        this.paneles.find(
          (panel) => panel.id === this.idPanelDocumentacion
        )!.mostrar = true;
      }

      // SOA y UAPISE: mostrar todas excepto las que no aplican
      if (
        this.centro.jerarquiaPadre?.nemonico === 'SOA' ||
        this.centro.jerarquiaPadre?.nemonico === 'UAPISE'
      ) {
        this.paneles.forEach(panel => {
          if (
            panel.id !== 'evaluacionMedica' &&
            panel.id !== 'historiaClinica' &&
            panel.id !== 'salidas' &&
            panel.id !== 'sancionesDisciplinarias'
          ) {
            panel.mostrar = true;
          }
        });

        this.obtenerFuncionario();

      } else {
        // Otros centros: mostrar todas excepto historia clínica/evaluación médica
        this.paneles.forEach(panel => {
          if (
            panel.id !== 'evaluacionMedica' &&
            panel.id !== 'historiaClinica'
          ) {
            panel.mostrar = true;
          }
        });

        this.obtenerFuncionario();
      }

      this.obtenerDetalleExpediente(this.uuid_fp);
    },
    error: (error: any) => {
      this.fichaIdentificacionService.checkError(error);
    }
  });
}

  obtenerFotos(tokenIdentificador: string) {
    let historialDeFotosFichaIdentificacionRequest = new HistorialDeFotosFichaIdentificacionRequest();
    historialDeFotosFichaIdentificacionRequest.page = 0;
    historialDeFotosFichaIdentificacionRequest.size = 5;
    historialDeFotosFichaIdentificacionRequest.tokenIdentificadorFichaDeIdentificacion = tokenIdentificador;

    this.historialDeFotosFichaIdentificacionService.obtener(historialDeFotosFichaIdentificacionRequest,
      etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>>) => {

          if (!response.exito) {
            this.historialDeFotosFichaIdentificacionService.checkError(response);
            return;
          }

          let paginacionResponse = response.data;
          this.dataList = paginacionResponse.data;
          if (this.dataList.length > 0) {
            this.documentoService.obtenerDocumento(
              this.dataList[0].documentoDTO.tokenIdentificador,
              etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            ).subscribe(
              {
                next: (response: ArrayBuffer) => {

                  let nombreArchivo = this.dataList[0].documentoDTO.nombre;
                  const blob = new Blob([response], { type: this.dataList[0].documentoDTO.mimeType });
                  let base64Encoded = 'data:image/png;base64,' + this.funcionesUtils.arrayBufferToBase64(response);
                  this.fotoPerfil = base64Encoded;

                },
                error: (error: any) => {

                  this.documentoService.checkError(error);
                }
              }
            );
          }
        },
        error: (error: any) => {
          this.historialDeFotosFichaIdentificacionService.checkError(error);
        }
      }
    );

  }

  obtenerApellidos() {
    let apellidos = "";
    apellidos = this.fichaIdentificacionDTO ? this.fichaIdentificacionDTO.apellidoPaterno : "" + " "
      + this.fichaIdentificacionDTO ? this.fichaIdentificacionDTO.apellidoMaterno : "";
    return apellidos
  }

  obtenerApellidoMaterno() {
    let apellidos = "";
    apellidos = this.fichaIdentificacionDTO ? this.fichaIdentificacionDTO.apellidoMaterno : "";
    return apellidos
  }

  obtenerFotoPerfil(tokenIdentificador: string) {
    let historialDeFotosFichaIdentificacionRequest = new HistorialDeFotosFichaIdentificacionRequest();
    historialDeFotosFichaIdentificacionRequest.page = 0;
    historialDeFotosFichaIdentificacionRequest.size = 5;
    historialDeFotosFichaIdentificacionRequest.tokenIdentificadorFichaDeIdentificacion = tokenIdentificador;

    this.historialDeFotosFichaIdentificacionService.obtenerFotoPerfil(this.uuid_fp,
      etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>) => {

          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.historialDeFotosFichaIdentificacionService.checkError(response);
            return;
          }

          let fotoPerfil = response.data;

          if (fotoPerfil.documentoDTO?.nombre) {
            this.documentoService.obtenerDocumento(
              fotoPerfil.documentoDTO.tokenIdentificador,
              etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION
            ).subscribe(
              {
                next: (response: ArrayBuffer) => {

                  // let nombreArchivo = this.dataList[0].documentoDTO.nombre;
                  const blob = new Blob([response], { type: fotoPerfil.documentoDTO.mimeType });
                  let base64Encoded = 'data:image/png;base64,' + this.funcionesUtils.arrayBufferToBase64(response);
                  this.fotoPerfil = base64Encoded;

                },
                error: (error: any) => {

                  this.documentoService.checkError(error);
                }
              }
            );
          }
        },
        error: (error: any) => {
          this.historialDeFotosFichaIdentificacionService.checkError(error);
        }
      }
    );

  }

  cargarCentro(): Observable<RespuestaPorDefecto<JerarquiaDTO>> {
    return this.jerarquiaService.obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu);
  }

  async cargarCentroYProcesar() {
    try {
      const respuesta = await this.cargarCentro().toPromise();
      if (!respuesta.exito) {
        this.jerarquiaService.checkError(respuesta);
        return;
      }

      this.centro = respuesta.data;
      console.log('centro', this.centro);

      if (this.centro.jerarquiaPadre?.nemonico === 'SOA') {
        this.paneles = this.paneles.filter(
          (item) =>
            item.id !== 'entregaRecepcionUniformesArticulos' &&
            item.id !== 'evaluacionMedica' && item.id !== 'evaluacionConductual'
            && item.id !== 'programaIntervencionRepentina'
        );
      } else if (this.centro.jerarquiaPadre?.nemonico === 'UAPISE') {
        this.paneles = this.paneles.filter(
          (item) =>
            item.id !== 'planTratamiento' &&
            item.id !== 'informeFinal' &&
            item.id !== 'entregaRecepcionUniformesArticulos' &&
            item.id !== 'salidas'
        );
      }
      else if (this.centro.jerarquiaPadre?.nemonico === 'UAPISE') {
        this.paneles = this.paneles.filter(
          (item) =>
            item.id !== 'planTratamiento' &&
            item.id !== 'informeFinal' &&
            item.id !== 'entregaRecepcionUniformesArticulos' &&
            item.id !== 'salidas'
        );
      }

      if (this.uuid_fp) {
        this.obtenerFichaIdentificacionPorToken(this.uuid_fp);
        this.obtenerFotoPerfil(this.uuid_fp);
        this.obtenerFichaIngresoValida();

      }
    } catch (error) {
      this.jerarquiaService.checkError(error);
    }
  }

  obtenerCondicionesMedico() {
    if (this.funcionarioActivo.cargo?.toLowerCase()?.includes('medico')) {
      this.paneles.find(x => x.id == 'evaluacionMedica').mostrar = true;
    } else {
      if (this.centro.jerarquiaPadre?.nemonico === 'SOA') {

      } else {
        this.paneles.find(x => x.id == 'historiaClinica').mostrar = true;
      }
    }
  }

  obtenerFuncionario() {
    this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {

          if (!response.exito) {
            return;
          }

          this.funcionarioActivo = response.data;
          // if (this.normalizarTexto(this.funcionarioActivo.cargo) === 'medico') {
          // if (this.funcionarioActivo.cargo?.toLowerCase()?.includes('medico')) {
          //   this.paneles.find(x => x.id == 'evaluacionMedica').mostrar = true;
          // } else {
          //   if (this.centro.jerarquiaPadre?.nemonico === 'SOA') {

          //   } else {
          //     this.paneles.find(x => x.id == 'historiaClinica').mostrar = true;
          //   }
          // }
        },
        error: (error: any) => {
          console.log('Hubo un problema al recuperar los registros. Inténtalo de nuevo.');
        }
      }
    );
  }

  cargarDatosFichaPPL(fuseCard: any) {
    fuseCard.face = 'back';

    // this.cargarDatosfichaPPL();
  }

  mostrarParteTrasera(): void {
    this.currentFace = 'back';
  }

  mostrarParteFrontal(): void {
    this.currentFace = 'front';
  }

  obtenerDetalleExpediente(tokenIdentificador: string) {
    console.log('ejecutando expeidnete detalle')
    this.expdienteMatrizService.obtenerUltimoExpedienteDetalle(this.nemonicoMenu, tokenIdentificador).subscribe(
      {
        next: (resp: RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>) => {
          // load.close();
          // this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

          if (!resp.exito) {
            return;
          }
          this.expedienteDetalle = resp.data;
          console.log('expedienteDetalle', this.expedienteDetalle);
          if (!this.expedienteDetalle.removido) {
            this.fechaSalida = this.expedienteDetalle.fechaFinMedida?.toString().split('T')[0];
            this.diasParaSalida = this.calcularDiasDiferencia(this.expedienteDetalle.fechaFinMedida);
          }
        },
        error: (error: any) => {
          // load.close();

          this.fichaIdentificacionService.checkError(error);
        }
      }
    )
  }

  calcularDiasDiferencia(fechaInicio: string | Date): number {
    const fechaActual = new Date();
    const milisegundosPorDia = 1000 * 60 * 60 * 24;

    fechaActual.setHours(0, 0, 0, 0);

    const fechaInicioDate = (typeof fechaInicio === 'string') ? new Date(fechaInicio) : fechaInicio;
    fechaInicioDate.setHours(0, 0, 0, 0);

    const diferenciaEnMilisegundos = fechaInicioDate.getTime() - fechaActual.getTime();
    const diferenciaEnDias = Math.floor(diferenciaEnMilisegundos / milisegundosPorDia);

    return diferenciaEnDias;
  }

  extraerHora(fecha: string): string {
    return new Date(fecha).toTimeString().slice(0, 5);
  }

  normalizarTexto(texto: string): string {
    return texto
      ?.toLowerCase()
      .normalize('NFD') // Descompone tildes
      .replace(/[\u0300-\u036f]/g, ''); // Elimina los signos diacríticos (tildes)
  }


  obtenerFichaIngresoValida(): void {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, '')
      .subscribe({
        next: (response) => {
          console.log(response);
          this.ingreso = response.data;
          console.log(this.ingreso);
        },
        error: (err) => {
          this.fichaIngresoService.checkError(err);
        }
      });
  }

}
