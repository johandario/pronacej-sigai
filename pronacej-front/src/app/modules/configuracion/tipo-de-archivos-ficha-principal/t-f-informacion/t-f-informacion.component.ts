import { AfterViewInit, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card/card.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { environment } from 'environments/environment';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-t-f-informacion',
  standalone: true,
  imports: [
    FuseCardComponent,
    MatChipsModule,
    MatIconModule,
    MatButtonModule,
    ReactiveFormsModule,
    FormsModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule
  ],
  templateUrl: './t-f-informacion.component.html',
  styleUrl: './t-f-informacion.component.scss'
})
export class TFInformacionComponent implements OnInit, AfterViewInit {

  private tokenSeccionFichaPrincipal: string;
  tipoDeIdentificacionTipoDeDocumentoList: FichaIdentificacionTipoDeDocumentoDTO[];
  tiposDeDocumentos: CatalogoDTO[];

  tiposDeDocumentosASeleccionar: CatalogoDTO[] = [];

  @Output() descendenciaEvent = new EventEmitter<CatalogoDTO[]>();
  @Output() catalogoActualEvent = new EventEmitter<CatalogoDTO>();


  formSeccionTipoDeDocumento: FormGroup;
  tipoDeIdentificacionTipoDeDocumentoEnUso: FichaIdentificacionTipoDeDocumentoDTO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_CONFIGURACION_TIPO_DE_ARCHIVOS_FICHA_PRINCIPAL;

  constructor(
    private activatedRoute: ActivatedRoute,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    private catalogoService: CatalogoService,
    private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService
  ) {

    this.formSeccionTipoDeDocumento = this.fb.group(
      {
        tokenSeccion: [null, [Validators.required]],
        tokenIdentificadorTipoDeDocumento: [null, [Validators.required]],
        esRequerido: [false]
      }
    );
  }

  ngAfterViewInit(): void {

  }

  private actualizarToken() {
    this.tokenSeccionFichaPrincipal = this.activatedRoute.snapshot.paramMap.get("tokenSeccion");
    this.formSeccionTipoDeDocumento.get("tokenSeccion").setValue(this.tokenSeccionFichaPrincipal);
  }

  ngOnInit(): void {
    this.actualizarToken();
    this.obtenerTiposDeDocumentos();

    this.obtenerDescendencia(this.tokenSeccionFichaPrincipal);

    this.obtenerCatalogo(this.tokenSeccionFichaPrincipal);

  }

  obtenerCatalogo(tokenIdentificador: string) {
    this.catalogoService.obtenerCatalogo(tokenIdentificador, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }

          this.catalogoActualEvent.emit(response.data);
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  obtenerDescendencia(tokenUltimoHijo: string) {
    this.catalogoService.obtenerDescendencia(tokenUltimoHijo, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }

          this.descendenciaEvent.emit(response.data);
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  obtenerTiposDeDocumentos() {
    this.catalogoService.obtenerHijos(
      etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }

          this.tiposDeDocumentos = response.data;
          this.obtenerPorSeccionFichaPrincipal(this.tokenSeccionFichaPrincipal);

        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }


  obtenerPorSeccionFichaPrincipal(tokenSeccionFichaPrincipal: string) {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerPorSeccionFichaPrincipal(
      tokenSeccionFichaPrincipal, this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
          this.actualizarToken();

          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(response);
            return;
          }

          this.tipoDeIdentificacionTipoDeDocumentoList = response.data;

          this.verificarTiposDeDocumentos();
        },
        error: (error: any) => {
          this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
        }
      }
    );
  }


  private verificarTiposDeDocumentos() {
    let tipoSDocUsados = this.tipoDeIdentificacionTipoDeDocumentoList.map(
      (tipo) => tipo.tipoArchivoSistemaDTO
    );

    this.tiposDeDocumentosASeleccionar = this.tiposDeDocumentos?.filter(
      (tipo) => {
        return !tipoSDocUsados?.find(
          (tipo2) => tipo2.tokenIdentificador == tipo.tokenIdentificador
        );
      }
    );
  }

  accion(accion: "editar" | "crear" | "cancelar") {
    if (accion == "cancelar") {
      this.resetar();

    } else {
      if (this.formSeccionTipoDeDocumento.invalid) {
        this.dialogMensajeService.mensajeError("Verifica la información requerida antes de continuar");
        return;
      }

      let objetoDTO: FichaIdentificacionTipoDeDocumentoDTO;
      if (this.tipoDeIdentificacionTipoDeDocumentoEnUso) {
        objetoDTO = this.tipoDeIdentificacionTipoDeDocumentoEnUso;
      } else {
        objetoDTO = new FichaIdentificacionTipoDeDocumentoDTO();
        objetoDTO.seccionFichaDeIdentificacionDTO = new CatalogoDTO();
        objetoDTO.seccionFichaDeIdentificacionDTO.tokenIdentificador = this.tokenSeccionFichaPrincipal;
      }

      objetoDTO.requerido = this.formSeccionTipoDeDocumento.get("esRequerido").value;
      objetoDTO.tipoArchivoSistemaDTO = this.tiposDeDocumentos.find(
        (cat) => cat.tokenIdentificador == this.formSeccionTipoDeDocumento.get("tokenIdentificadorTipoDeDocumento").value
      );

      let observable: Observable<RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO>>;

      if (accion == "crear") {
        observable = this.tipoDeIdentificacionTipoDeDocumentoService.crear(objetoDTO, this.nemonicoMenu);
      } else {
        observable = this.tipoDeIdentificacionTipoDeDocumentoService.editar(objetoDTO, this.nemonicoMenu);
      }

      observable.subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO>) => {
            if (!environment.production) {
              console.log(response);
            }

            if (!response.exito) {
              this.tipoDeIdentificacionTipoDeDocumentoService.checkError(response);
              return;
            }

            this.dialogMensajeService.mensajeExitoso(
              response.titulo,
              response.mensaje
            );

            this.resetar();
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );

    }
  }


  resetar() {
    this.obtenerTiposDeDocumentos();
    this.formSeccionTipoDeDocumento.reset();
    this.tipoDeIdentificacionTipoDeDocumentoEnUso = null;
  }

  editar(tipoDeIdentificacionTipoDeDocumento: FichaIdentificacionTipoDeDocumentoDTO) {
    this.tipoDeIdentificacionTipoDeDocumentoEnUso = tipoDeIdentificacionTipoDeDocumento;
    this.formSeccionTipoDeDocumento.get("tokenIdentificadorTipoDeDocumento").setValue(
      this.tipoDeIdentificacionTipoDeDocumentoEnUso.tipoArchivoSistemaDTO.tokenIdentificador
    );
    this.formSeccionTipoDeDocumento.get("esRequerido").setValue(
      this.tipoDeIdentificacionTipoDeDocumentoEnUso.requerido
    );

    this.tiposDeDocumentosASeleccionar = this.tiposDeDocumentos;
  }

  remover(tipoDeIdentificacionTipoDeDocumento: FichaIdentificacionTipoDeDocumentoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás a punto de eliminar: " + tipoDeIdentificacionTipoDeDocumento.tipoArchivoSistemaDTO.nombre,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.removerApi(tipoDeIdentificacionTipoDeDocumento);
          }
        }
      }
    );
  }

  private removerApi(tipoDeIdentificacionTipoDeDocumento: FichaIdentificacionTipoDeDocumentoDTO) {
    this.tipoDeIdentificacionTipoDeDocumentoService.eliminar(
      tipoDeIdentificacionTipoDeDocumento,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(response);
            return;
          }

          this.dialogMensajeService.mensajeExitoso(response.titulo,
            response.mensaje
          );

          this.obtenerTiposDeDocumentos();
        },
        error: (error: any) => {
          this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
        }
      }
    );
  }
}
