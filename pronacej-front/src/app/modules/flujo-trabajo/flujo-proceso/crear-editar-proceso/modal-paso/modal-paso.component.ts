import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PasoDTO, PasoUsuarioDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { map, Observable, startWith } from 'rxjs';

@Component({
  selector: 'app-modal-paso',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatAutocompleteModule,
    CommonModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './modal-paso.component.html',
  styleUrl: './modal-paso.component.scss'
})
export class ModalPasoComponent implements OnInit {
  pasoFormGroup = this.fb.group({
    nombre: ['', Validators.required],
    url: ['', Validators.required],
    porcentajeAvance: [null],
    requiereNotificacionCorreo: [false],
    pasoSalto: [null],
    usuario: [null]
  });
  
  usuarios: UsuarioSistemaDTO[];
  paso: PasoDTO = new PasoDTO;

  usuariosFiltrados: Observable<UsuarioSistemaDTO[]>;   
  usuariosSeleccionados: UsuarioSistemaDTO[] = [];

  isCondicional: Boolean = false;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModalPasoComponent>,
    private dialogMensajeService: DialogMensajeService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.pasoFormGroup.controls['url'].disable();

    if (this.data.paso) {
      this.paso = this.data.paso; 
      this.pasoFormGroup.patchValue(this.paso);
      if (this.paso.pasoUsuarioList.length > 0) {
        for (let pasoUsuario of this.paso.pasoUsuarioList) {
          this.usuariosSeleccionados.push(pasoUsuario.usuarioSistema);
        }
      }
    } 
    if (this.data.usuarios) {
      this.usuarios = this.data.usuarios.data;
      if (this.data.paso) {
        if (this.paso.pasoUsuarioList.length > 0) {
          for (let pasoUsuario of this.paso.pasoUsuarioList) {
            this.usuarios = this.usuarios.filter(usuario => usuario.tokenIdentificador !== pasoUsuario.usuarioSistema.tokenIdentificador);
          }
        }
      }
      this.usuariosFiltrados = this.pasoFormGroup.controls['usuario'].valueChanges.pipe(
        startWith(''),
        map(value => typeof value === 'string' ? this._filterUsuarios(value) : this.usuarios),
      ); 

      this.usuarios.sort((a,b) =>  a.apellidos.toLowerCase().localeCompare(b.apellidos.toLowerCase()))

    }
  } 

  displayFn(option: UsuarioSistemaDTO): string {
    return option && option.apellidos && option.nombres
      ? `${option.apellidos} ${option.nombres}`
      : '';
  }

  agregarUsuario() {
    const usuarioSeleccionado = this.pasoFormGroup.controls['usuario'].value;
    if (usuarioSeleccionado) {
      this.usuariosSeleccionados.push(usuarioSeleccionado);
      this.pasoFormGroup.controls['usuario'].reset();
      this.usuarios = this.usuarios.filter(usuario => usuario.tokenIdentificador !== usuarioSeleccionado.tokenIdentificador);
      this.usuarios.sort((a,b) =>  a.apellidos.toLowerCase().localeCompare(b.apellidos.toLowerCase()))
      this.usuariosFiltrados = this.pasoFormGroup.controls['usuario'].valueChanges.pipe(
        startWith(''),
        map(value => typeof value === 'string' ? this._filterUsuarios(value) : this.usuarios),
      );       
    }
  }

  eliminarUsuario(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el registro seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            const eliminado = this.usuariosSeleccionados.splice(index, 1);
            this.usuarios.push(eliminado[0]);
            this.usuarios.sort((a,b) =>  a.apellidos.toLowerCase().localeCompare(b.apellidos.toLowerCase()))
            this.usuariosFiltrados = this.pasoFormGroup.controls['usuario'].valueChanges.pipe(
              startWith(''),
              map(value => typeof value === 'string' ? this._filterUsuarios(value) : this.usuarios),
            );  
          }
        }
      }
    )
  }

  private _filterUsuarios(value: string): UsuarioSistemaDTO[] {
    const filterValue = value.toLowerCase();

    return this.usuarios.filter(option => {
      const nombreCompleto = `${option.apellidos} ${option.nombres}`.toLowerCase(); 
      return nombreCompleto.includes(filterValue)
    })
  }

  guardarCambios() {
    if (this.usuariosSeleccionados.length > 0) {
      let pasoUsuarioList: PasoUsuarioDTO[] = [];
      for (let usuario of this.usuariosSeleccionados) {
        let pasoUsuario = new PasoUsuarioDTO;
        pasoUsuario.usuarioSistema = usuario;
        const pasoUsuarioExistente = this.paso?.pasoUsuarioList.find(pasoUsuario => pasoUsuario.usuarioSistema.tokenIdentificador === usuario.tokenIdentificador);
        if (pasoUsuarioExistente) {
          pasoUsuarioExistente.usuarioSistema = usuario;
          pasoUsuarioList.push(pasoUsuarioExistente);
        } else {
          pasoUsuarioList.push(pasoUsuario);
        }
      }
      this.paso.pasoUsuarioList = pasoUsuarioList;
    } else {
      this.paso.pasoUsuarioList = [];
    }        
    Object.assign(this.paso, this.pasoFormGroup.value);
    if (!this.paso.pasoSalto) {
      this.paso.pasoSalto = null;
      this.paso.pasoSubsanacion = null;
    }
    this.dialogRef.close(this.paso);
  }
}
