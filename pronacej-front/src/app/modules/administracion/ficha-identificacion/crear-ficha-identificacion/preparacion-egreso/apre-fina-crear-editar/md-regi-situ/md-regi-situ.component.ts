import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SituacionActualAdolescenteDTO } from 'app/core/model/both/situacionActualAdolescenteDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-md-regi-situ',
  standalone: true,
  imports: [
    CommonModule,
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
  ],
  templateUrl: './md-regi-situ.component.html',
  styleUrl: './md-regi-situ.component.scss'
})
export class MdRegiSituComponent implements OnInit {
  // Formulario para registro de situaciones
  formularioRegistro = this.constructorFormulario.group({
    tokenIdentificadorTipoArea: ['0', [Validators.required]],
    tokenIdentificadorTipoSituacion: ['0', [Validators.required]],
    descripcion: ['', [Validators.required, this.validarNoEspacios()]],
    observacion: ['', [this.validarNoEspacios()]],
  });

  // Catálogos para las listas desplegables
  listaTiposArea: CatalogoDTO[] = [];
  listaTiposSituacion: CatalogoDTO[] = [];

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiSituComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { 
    // Inicialización de catálogos desde los datos de entrada
    if (datos?.listaTiposArea) {
      this.listaTiposArea = datos.listaTiposArea;
    }
    if (datos?.listaTiposSituacion) {
      this.listaTiposSituacion = datos.listaTiposSituacion;
    }
  }

  /**
   * Inicializa el formulario con datos si es modo edición
   */
  ngOnInit(): void {
    if (this.datos?.fila) {
      this.formularioRegistro.patchValue({
        tokenIdentificadorTipoArea: this.datos.fila.tokenIdentificadorTipoArea,
        tokenIdentificadorTipoSituacion: this.datos.fila.tokenIdentificadorTipoSituacion,
        descripcion: this.datos.fila.descripcion,
        observacion: this.datos.fila.observacion,
      });
    }
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   * @returns Validador personalizado
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      // Si el valor es nulo o undefined, no hay error de espacios
      if (control.value === null || control.value === undefined) {
        return null;
      }
      
      // Si es string, verificar que no sea solo espacios
      if (typeof control.value === 'string') {
        return control.value.trim().length === 0 && control.value.length > 0 ? { 'soloEspacios': true } : null;
      }
      
      return null;
    };
  }

  /**
   * Registra la situación y cierra el diálogo
   */
  registrarSituacion() {
    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioRegistro.controls).forEach(key => {
      const control = this.formularioRegistro.get(key);
      control.markAsTouched();
    });
    
    // Verificar si hay campos con error de "soloEspacios"
    let tieneEspaciosEnBlanco = false;
    Object.keys(this.formularioRegistro.controls).forEach(key => {
      const control = this.formularioRegistro.get(key);
      if (control.errors && control.errors['soloEspacios']) {
        tieneEspaciosEnBlanco = true;
      }
    });
    
    if (this.formularioRegistro.invalid) {
      return;
    }
    
    const valoresFormulario = this.formularioRegistro.value;
    let situacion = new SituacionActualAdolescenteDTO();
   
    if (this.datos?.fila) {
      // Modo edición: mantener el identificador existente
      situacion.tokenIdentificador = this.datos.fila.tokenIdentificador;
    } else {
      // Modo nuevo registro: asignar identificador temporal
      situacion.tokenIdentificador = "0";
    }
   
    // Asignar valores del formulario al DTO
    situacion.tokenIdentificadorTipoArea = valoresFormulario.tokenIdentificadorTipoArea;
    situacion.tokenIdentificadorTipoSituacion = valoresFormulario.tokenIdentificadorTipoSituacion;
    
    // Limpiar espacios en blanco de los campos de texto
    situacion.descripcion = valoresFormulario.descripcion?.trim();
    situacion.observacion = valoresFormulario.observacion?.trim();

    // Cerrar el diálogo y devolver el objeto con los valores
    this.referenciaDialogo.close(situacion);
  }
}