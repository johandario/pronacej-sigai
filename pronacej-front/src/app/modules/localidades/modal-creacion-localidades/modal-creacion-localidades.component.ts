import { Component, Input } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { LocalidadService } from 'app/modules/seguridad/services/localidad.service';

@Component({
  selector: 'app-modal-creacion-localidades',
  standalone: true,
  imports: [MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    FormsModule,
    MatInputModule],
  templateUrl: './modal-creacion-localidades.component.html',
  styleUrl: './modal-creacion-localidades.component.scss'
})
export class ModalCreacionLocalidadesComponent {

  @Input() declare titulo: string;
  @Input() declare catalogoPadre: LocalidadDTO;
  @Input() declare tipoLocalidad: string;

  nemonicoMenu = etiquetasModel.MENU_LOCALIDADES;
  formCatalogo: FormGroup;
  isSubmitting = false;

  constructor(private dialogRef: MatDialogRef<ModalCreacionLocalidadesComponent>,
    private formBuilder: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private localidadService: LocalidadService,) {

    this.formCatalogo = this.formBuilder.group(
      {
        nombre: [null, [Validators.required]],
        nemonico: [null, [Validators.required]],
        ubigeo: [null, [Validators.minLength(2), Validators.maxLength(2),Validators.required]]
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



    crearLocalidad() {
  if (this.isSubmitting || this.formCatalogo.invalid) return;
  this.isSubmitting = true;

  let catalogoDTOCrear = this.crearLocalidadDesdeElform();
  catalogoDTOCrear.tokenIdentificadorPadre = this.catalogoPadre?.tokenIdentificador;
  catalogoDTOCrear.tipoLocalidad = this.tipoLocalidad;

  const inputUbigeo = (catalogoDTOCrear.ubigeo || '').trim();
  const padreUbigeo = this.catalogoPadre?.ubigeo?.trim();
  catalogoDTOCrear.ubigeo = padreUbigeo ? padreUbigeo + inputUbigeo : inputUbigeo;

  const load = this.dialogMensajeService.mensajeLoading("Creando el catálogo...");

  this.dialogMensajeService.mensajeConConfirmacion(
    "Estás a punto de crear el catálogo: " + catalogoDTOCrear.nombre,
    "¿Deseas continuar?"
  ).afterClosed().subscribe((response: any) => {
    if (response === "confirmed") {
      this.localidadService.crearLocalidad(catalogoDTOCrear, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<LocalidadDTO>) => {
          load.close();
          this.isSubmitting = false;
          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }
          this.dialogRef.close(true);
        },
        error: (error: any) => {
          load.close();
          this.isSubmitting = false;
          this.catalogoService.checkError(error);
        }
      });
    } else {
      load.close();
      this.isSubmitting = false;
    }
  });
}

  private crearLocalidadDesdeElform() {
    let calogoDTO = new LocalidadDTO();
    let values = this.formCatalogo.value;
    Object.keys(values).forEach(
      (key) => {
        calogoDTO[key] = this.formCatalogo.get(key).value;
      }
    );

    return calogoDTO;
  }

  soloNumero(event: KeyboardEvent): void {
    const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete'];
    const isNumberKey = event.key >= '0' && event.key <= '9';

    if (!isNumberKey && !allowedKeys.includes(event.key)) {
      event.preventDefault();
    }
  }
}
