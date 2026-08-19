import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { environment } from 'environments/environment';
import { ModalInfoCatalogoComponent } from '../modal-info-catalogo/modal-info-catalogo.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { CatalogoService as CatalogoService2 } from '../catalogo.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { CommonModule } from '@angular/common';
import { UtilsService } from 'app/core/services/utils.service';

@Component({
  selector: 'app-informacion-catalogo',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  templateUrl: './informacion-catalogo.component.html',
  styleUrl: './informacion-catalogo.component.scss'
})
export class InformacionCatalogoComponent implements OnInit {
  @Input() catalogoActual: CatalogoDTO;

  maxLength = 255;

  @Input() declare titulo: string;

  @Output() eliminarEvent = new EventEmitter<boolean>();
  @Output() editarEvent = new EventEmitter<boolean>();
  @Output() descendenciaEvent = new EventEmitter<CatalogoDTO[]>();
  @Output() obtencionCatalogoActualEvent = new EventEmitter<CatalogoDTO>();

  // VARIABLE LOCAL SIMPLE
  nemonicoMenu = etiquetasModel.MENU_CATALOGOS;

  formCatalogo: FormGroup;

  esCatalogoCarpeta: Boolean = false;

  constructor(private catalogoService: CatalogoService,
    private activatedRoute: ActivatedRoute,
    private formBuilder: FormBuilder,
    private matDialog: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService2: CatalogoService2,
    private utilsService: UtilsService
  ) {

    this.formCatalogo = this.formBuilder.group(
      {
        nombre: [null, [Validators.required]],
        descripcion: [null],
        nemonico: [null, [Validators.required]],
        codigoExterno: [null,]
      }
    );
  }

  ngOnInit(): void {
    this.obtenerInformacionCatalogo();
  }

  getError(key: string) {
    let errors = this.formCatalogo.get(key)?.errors;
    return errors ? errors[0] : "";
  }

  llenarDataConCatalogo(catalogo: CatalogoDTO) {
    let values = this.formCatalogo.value;
    Object.keys(values).forEach(
      (key) => {
        this.formCatalogo.get(key)?.setValue(
          catalogo[key]
        );
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

  obtenerInformacionCatalogo() {
    let tokenIdentificador = this.activatedRoute.snapshot.paramMap.get("token_catalogo");

    if (tokenIdentificador) {
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

            this.catalogoActual = response.data;
            this.titulo = this.catalogoActual.nombre;

            if (this.catalogoActual.nemonico === etiquetasModel.NEMONICO_CARPETA_GESTION_ADOLESCENTE)
              this.esCatalogoCarpeta = true;
            else
              this.esCatalogoCarpeta = false;

            this.llenarDataConCatalogo(this.catalogoActual);
            this.obtencionCatalogoActualEvent.emit(this.catalogoActual);

            this.obtenerDescendencia(this.catalogoActual.tokenIdentificador);

          },
          error: (error: any) => {
            this.catalogoService.checkError(error);
          }
        }
      );
    }
  }

  crearHijo() {
    let ref = this.matDialog.open(ModalInfoCatalogoComponent,
      {
        panelClass: ["w-full"]
      }
    );

    ref.componentInstance.titulo = "Crea un catalogo hijo de: " + this.catalogoActual?.nombre;
    ref.componentInstance.catalogoPadre = this.catalogoActual;
    ref.componentInstance.nemonicoMenu = this.nemonicoMenu;

    ref.afterClosed().subscribe(
      {
        next: (resp: boolean) => {
          if (resp) {
            this.editarEvent.emit(true);
          }
        }
      }
    );
  }

  private crearCatalogoDesdeElform() {
    let calogoDTO = new CatalogoDTO();
    let values = this.formCatalogo.value;
    Object.keys(values).forEach(
      (key) => {
        calogoDTO[key] = this.formCatalogo.get(key).value;
      }
    );

    return calogoDTO;
  }

  accion(accion: "editar" | "eliminar") {
    let confirm = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás a punto de " + accion + " el catalogo: " + this.catalogoActual.nombre,
      "Deseas continuar?"
    );

    confirm.afterClosed().subscribe(
      {
        next: (response: any) => {
          if (response == "confirmed") {
            if (accion == "editar") {
              this.editarCatalogo();
            } else if (accion == "eliminar") {
              this.eliminarCatalogo();
            }
          }
        }
      }
    );
  }

  private eliminarCatalogo() {
    let catalogoDTOEditar = this.crearCatalogoDesdeElform();
    catalogoDTOEditar.tokenIdentificador = this.catalogoActual.tokenIdentificador;
    let load = this.dialogMensajeService.mensajeLoading("Eliminando el catalogo..");

    this.catalogoService2.eliminarCatalogo(
      catalogoDTOEditar,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO>) => {
          load.close();

          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService2.checkError(response);
            return;
          }

          this.eliminarEvent.emit(true);
        },
        error: (error: any) => {
          load.close();
          this.catalogoService2.checkError(error);
        }
      }
    );
  }


  private editarCatalogo() {
    let catalogoDTOEditar = this.crearCatalogoDesdeElform();
    catalogoDTOEditar.tokenIdentificador = this.catalogoActual.tokenIdentificador;

    this.editarCatalogoFunct(catalogoDTOEditar);
  }

  editarCatalogoFunct(catalogoDTOEditar: CatalogoDTO) {
    let load = this.dialogMensajeService.mensajeLoading("Modificando el catalogo..");
    
    this.catalogoService2.actualizarCatalogo(catalogoDTOEditar, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO>) => {
          load.close();
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService2.checkError(response);
            return;
          }

          this.editarEvent.emit(true);
        },
        error: (error: any) => {
          load.close();

          this.catalogoService2.checkError(error);
        }
      }
    );
  }

  actualizarAlfresco() {
    this.utilsService.actualizarCarpetasAlfresco(this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<Boolean>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al actualizar las carpetas. ' + response.mensaje
          );
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
        }
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar las carpetas. Inténtalo de nuevo.'
        );
      }
    });
  }

}