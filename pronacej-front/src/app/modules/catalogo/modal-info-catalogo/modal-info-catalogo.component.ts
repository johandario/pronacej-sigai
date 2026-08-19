import { Component, Input } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { CatalogoService } from '../catalogo.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-modal-info-catalogo',
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    FormsModule,
    MatInputModule
  ],
  templateUrl: './modal-info-catalogo.component.html',
  styleUrl: './modal-info-catalogo.component.scss'
})
export class ModalInfoCatalogoComponent {

  @Input() declare titulo: string;
  @Input() declare catalogoPadre: CatalogoDTO;
  
  // VARIABLE LOCAL SIMPLE
  nemonicoMenu = etiquetasModel.MENU_CATALOGOS;

  formCatalogo: FormGroup;

  constructor(private dialogRef: MatDialogRef<ModalInfoCatalogoComponent>,
    private formBuilder: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService
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

  cancelar() {
    this.dialogRef.close(false);
  }

  getError(key: string) {
    let errors = this.formCatalogo.get(key)?.errors;
    return errors ? errors[0] : "";
  }

  crearCatalogo() {
    let catalogoDTOCrear = this.crearCatalogoDesdeElform();
    catalogoDTOCrear.tokenIdentificadorPadre = this.catalogoPadre?.tokenIdentificador;
    let load = this.dialogMensajeService.mensajeLoading("Creando el catalogo..");

    let confirm = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás a punto de crear el catalogo: " + catalogoDTOCrear.nombre,
      "Deseas continuar?"
    );

    confirm.afterClosed().subscribe(
      {
        next: (response: any) => {
          if (response == "confirmed") {
            this.catalogoService.crearCatalogo(catalogoDTOCrear, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<CatalogoDTO>) => {
                  load.close();

                  if (!response.exito) {
                    this.catalogoService.checkError(response);
                    return;
                  }

                  this.dialogRef.close(true);
                },
                error: (error: any) => {
                  load.close();
                  this.catalogoService.checkError(error);
                }
              }
            );
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
}