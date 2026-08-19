import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDrawerContainer, MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';
import { ActivatedRoute } from '@angular/router';
import { NodeItem, TreeNgxComponent, TreeNgxModule } from 'app/core/components/tree-ngx';
import etiquetasModel from 'app/core/etiquetas.model';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { LocalidadService } from 'app/modules/seguridad/services/localidad.service';
import { environment } from 'environments/environment';
import { ModalCreacionLocalidadesComponent } from '../modal-creacion-localidades/modal-creacion-localidades.component';

@Component({
  selector: 'app-informacion-localidad',
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
  templateUrl: './informacion-localidad.component.html',
  styleUrl: './informacion-localidad.component.scss'
})
export class InformacionLocalidadComponent implements OnInit {

  @Input() catalogoActual: LocalidadDTO;
  @Input() declare titulo: string;

  @Output() editarEvent = new EventEmitter<boolean>();
  @Output() descendenciaEvent = new EventEmitter<LocalidadDTO[]>();
  @Output() obtencionCatalogoActualEvent = new EventEmitter<LocalidadDTO>();

  nemonicoMenu = etiquetasModel.MENU_LOCALIDADES;

  formCatalogo: FormGroup;

  esCatalogoCarpeta: Boolean = false;
  modoEdicion = true;
  isSubmitting = false;

  constructor(private localidadService: LocalidadService,
    private dialogMensajeService: DialogMensajeService,
    private fb: FormBuilder,
    private activatedRoute: ActivatedRoute,
    private matDialog: MatDialog,
  ) {
    console.log('entro al componente');

    this.formCatalogo = this.fb.group(
      {
        nombre: [null, [Validators.required]],
        nemonico: [null, [Validators.required]],
        ubigeo: [null,]
      }
    );

  }

  ngOnInit(): void {
    this.titulo= 'Localidades'
    this.activatedRoute.paramMap.subscribe(params => {
      const tokenIdentificador = params.get("token_localidad");
      if (tokenIdentificador) {
        console.log('token' + tokenIdentificador);
        this.obtenerInformacionLocalidad();
      }
    //   else {
    //   this.formCatalogo.enable(); 
    // }
    });
    console.log('entro al componente');
    // this.obtenerInformacionLocalidad();
  }

 

  getError(key: string) {
    let errors = this.formCatalogo.get(key)?.errors;
    return errors ? errors[0] : "";
  }

  // llenarDataConCatalogo(catalogo: LocalidadDTO) {
  //   let values = this.formCatalogo.value;
  //   Object.keys(values).forEach(
  //     (key) => {
  //       this.formCatalogo.get(key)?.setValue(
  //         catalogo[key]
  //       );
  //     }
  //   );
  // }

  llenarDataConCatalogo(catalogo: LocalidadDTO) {
  Object.keys(this.formCatalogo.controls).forEach(key => {
    const control = this.formCatalogo.get(key);
    if (!control) return;

    if (control.disabled) {
      control.enable();
      control.setValue(catalogo[key]);
      control.disable();
    } else {
      control.setValue(catalogo[key]);
    }
  });
}

  obtenerInformacionLocalidad() {
    console.log('componente informacion');
    let tokenIdentificador = this.activatedRoute.snapshot.paramMap.get("token_localidad");
    if (tokenIdentificador) {
      this.localidadService.obtenerLocalidadTokenIdentificador(tokenIdentificador, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<LocalidadDTO>) => {
            if (!environment.production) {
              console.log(response);
            }

            if (!response.exito) {
              this.localidadService.checkError(response);
              return;
            }

            this.catalogoActual = response.data;
            this.titulo = this.catalogoActual.nombre;

            if (this.catalogoActual.nemonico === etiquetasModel.NEMONICO_CARPETA_GESTION_ADOLESCENTE)
              this.esCatalogoCarpeta = true;

            this.llenarDataConCatalogo(this.catalogoActual);
            this.formCatalogo.enable(); 
            this.formCatalogo.get('ubigeo')?.disable(); // desactiva campo ubigeo en modo edición
            this.obtencionCatalogoActualEvent.emit(this.catalogoActual);

            this.obtenerDescendencia(this.catalogoActual.tokenIdentificador);

          },
          error: (error: any) => {
            this.localidadService.checkError(error);
          }
        }
      );
    }
  }

  obtenerDescendencia(tokenUltimoHijo: string) {
    this.localidadService.obtenerDescendencia(tokenUltimoHijo, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.localidadService.checkError(response);
            return;
          }

          this.descendenciaEvent.emit(response.data);
        },
        error: (error: any) => {
          this.localidadService.checkError(error);
        }
      }
    );
  }

    crearHijo() {
  let ref = this.matDialog.open(ModalCreacionLocalidadesComponent, {
    panelClass: ["w-full"]
  });

  console.log(this.catalogoActual);

  ref.componentInstance.titulo = "Crea un catálogo hijo de: " + this.catalogoActual?.nombre;
  ref.componentInstance.catalogoPadre = this.catalogoActual;

  
  if (this.catalogoActual?.tipoLocalidad === 'Departamento') {
    ref.componentInstance.tipoLocalidad = 'PROVINCIA';
  } else if (this.catalogoActual?.tipoLocalidad === 'Provincia') {
    ref.componentInstance.tipoLocalidad = 'DISTRITO';
  }

  ref.afterClosed().subscribe({
    next: (resp: boolean) => {
      if (resp) {
        this.editarEvent.emit(true);
      }
    }
  });
}

  guardarEdicion() {
  if (this.isSubmitting || this.formCatalogo.invalid) return;

  this.isSubmitting = true;

  const localidadEditada: LocalidadDTO = {
    ...this.catalogoActual,
    ...this.formCatalogo.value,
    esEdicion: true
  };

  this.dialogMensajeService
    .mensajeConConfirmacion('¿Está seguro de que desea editar esta localidad?', '')
    .afterClosed()
    .subscribe((confirmado: string) => {
      if (confirmado === 'confirmed') {
        this.localidadService.editarLocalidad(localidadEditada, this.nemonicoMenu).subscribe({
          next: (resp) => {
            this.isSubmitting = false;

            if (resp.exito) {
              this.dialogMensajeService.mensajeExitoso(
                'Guardar',
                `Localidad ${localidadEditada.nombre} editada correctamente.`
              );
              this.editarEvent.emit(true);
            } else {
              this.dialogMensajeService.mensajeConConfirmacion(resp.mensaje, '');
            }
          },
          error: () => {
            this.isSubmitting = false;
            this.dialogMensajeService.mensajeError(
              'Hubo un error al editar la localidad.'
            );
          }
        });
      } else {
        this.isSubmitting = false;
      }
    });
}

}
