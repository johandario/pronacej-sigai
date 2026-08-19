import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, ActivatedRoute } from '@angular/router';
import { FichaIdentificacionCarpetaRequest } from 'app/core/model/request/ia/FichaIdentificacionCarpetaRequest.model';
import { ContenidoCarpetaResponse } from 'app/core/model/response/ia/ContenidoCarpetaResponse.model';
import { RutaContenidoCarpetaResponse } from 'app/core/model/response/ia/RutaContenidoCarpetaResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { CarpetaService } from 'app/modules/administracion/services/carpetaService.service';
import { FichaIdentificacionCarpetaService } from 'app/modules/administracion/services/fichaIdentificacionCarpeta.service';
import { environment } from 'environments/environment';
import { DetallesArchivoComponent } from '../detalles-archivo/detalles-archivo.component';
import { MatDialog } from '@angular/material/dialog';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-administrador-de-archivos',
  standalone: true,
  imports: [
    MatIconModule,
    MatTooltipModule
  ],
  templateUrl: './administrador-de-archivos.component.html',
  styleUrl: './administrador-de-archivos.component.scss',
})
export class AdministradorDeArchivosComponent implements OnInit {
  @HostListener('window:popstate', ['$event'])
  onPopState(event) {
    this.contenidoCarpetaResponse = null;
    this.cargarData();
  }

  private tokenFicha: string;
  public contenidoCarpetaResponse: ContenidoCarpetaResponse;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_DOCUMENTOS;

  constructor(public router: Router,
    private activatedRoute: ActivatedRoute,
    private fichaIdentificacionCarpetaService: FichaIdentificacionCarpetaService,
    private dialogMensajeService: DialogMensajeService,
    private carpetaService: CarpetaService,
    private changeDetectorRef: ChangeDetectorRef,
    private matDialog: MatDialog,
    private authSerguridadServicio: AuthSerguridadServicio,) {
  }

  
  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_DOCUMENTOS"
    );
    if (!this.contenidoCarpetaResponse) {
      this.cargarData();
    }
  }

  private cargarData() {
    let mapParam = this.activatedRoute.snapshot.paramMap
    let parentMapParam = this.activatedRoute.parent.snapshot.paramMap;

    let tokenCarpeta = mapParam.get("token_carpeta");;
    this.tokenFicha = parentMapParam.get("uuid_fp");

    if (tokenCarpeta) {
      this.obterInformacionDeCarpetaConCarpetaService(tokenCarpeta);
    } else {
      this.obtenerInformacionDeCarpeta(tokenCarpeta);
    }
  }

  obterInformacionDeCarpetaConCarpetaService(tokenCarpeta: string) {
    let load = this.dialogMensajeService.mensajeLoading("Cargando carpetas y documentos");

    let fichaIdentificacionCarpetaRequest = new FichaIdentificacionCarpetaRequest();
    fichaIdentificacionCarpetaRequest.tokenIdentificadorFichaPrincipal = this.tokenFicha;
    fichaIdentificacionCarpetaRequest.tokenIdentificadorCarpeta = tokenCarpeta;

    this.carpetaService.obtenerCarpetaDesdeFichaPrincipal(
      fichaIdentificacionCarpetaRequest, this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<ContenidoCarpetaResponse>) => {

          if (!environment.production) {
            console.log(response);
          }
          load.close();
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(response.mensaje);
            return;
          }

          this.contenidoCarpetaResponse = response.data;

          this.changeDetectorRef.detectChanges();
        },
        error: (error: any) => {
          load.close();
          this.carpetaService.checkError(error);
        }
      }
    );
  }

  obtenerInformacionDeCarpeta(tokenFichaCarpeta: string) {
    let load = this.dialogMensajeService.mensajeLoading("Cargando carpetas y documentos");

    let fichaIdentificacionCarpetaRequest = new FichaIdentificacionCarpetaRequest();
    fichaIdentificacionCarpetaRequest.tokenIdentificadorFichaPrincipal = this.tokenFicha;
    fichaIdentificacionCarpetaRequest.tokenIdentificadorFichaPrincipalCarpeta = tokenFichaCarpeta;

    this.fichaIdentificacionCarpetaService.obtenerInformacionDeCarpeta(
      fichaIdentificacionCarpetaRequest, this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<ContenidoCarpetaResponse>) => {

          if (!environment.production) {
            console.log(response);
          }
          load.close();
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(response.mensaje);
            return;
          }

          this.contenidoCarpetaResponse = response.data;
          this.changeDetectorRef.detectChanges();

        },
        error: (error: any) => {
          load.close();
          this.fichaIdentificacionCarpetaService.checkError(error);
        }
      }
    );
  }

  getType(type: string) {
    if (!type) {
      return type;
    }

    let typeSplit = type.split("/");
    if (typeSplit && typeSplit.length > 0) {
      return typeSplit[typeSplit.length - 1].toLocaleUpperCase();
    } else {
      return type.toLocaleUpperCase();
    }
  }


  regresar(ruta: RutaContenidoCarpetaResponse, index: number) {
    let rutaF = ruta.tokenCarpeta;
    let path: string = "../" + this.tokenFicha;

    if (rutaF && rutaF != "" && index != 0) {
      path = "../" + this.tokenFicha + "/" + rutaF;
    }
    this.navigate(path);
  }

  clickFolder(contenidoCarpetaResponse: ContenidoCarpetaResponse) {
    let ruta = "../" + this.tokenFicha + "/" + contenidoCarpetaResponse.tokenIdentificadorCarpeta;
    this.navigate(ruta);
  }

  private navigate(ruta: string) {
    this.router.navigate(
      [
        ruta
      ],
      {
        relativeTo: this.activatedRoute.parent
      }
    ).then(
      (valor: boolean) => {
        if (valor) {
          this.cargarData();
        }
      }
    );
  }


  //detalles
  clickNodo(contenidoCarpetaResponse: ContenidoCarpetaResponse) {
    let ref = this.matDialog.open(
      DetallesArchivoComponent,
      {
        hasBackdrop: true,
        panelClass: ["w-full"]
      }
    );

    ref.componentInstance.contenidoCarpetaResponse = contenidoCarpetaResponse;

    ref.afterClosed().subscribe(
      {
        next: (response: any) => {
          console.log(response);
        }
      }
    );
  }

}
