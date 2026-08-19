import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { FactoresPresentesDTO } from 'app/core/model/both/factoresPresentesDTO.model';

@Component({
  selector: 'app-md-regi-fact',
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
  ],
  templateUrl: './md-regi-fact.component.html',
  styleUrl: './md-regi-fact.component.scss'
})
export class MdRegiFactComponent implements OnInit {
  // Formulario para registro de factores
  formularioRegistro = this.constructorFormulario.group({
    factoresProtectores: ['', [Validators.required, this.validarNoEspacios()]],
    factoresRiesgo: ['', [Validators.required, this.validarNoEspacios()]],
  });

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiFactComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { }

  /**
   * Inicializa el formulario con datos si es modo edición
   */
  ngOnInit(): void {
    if (this.datos?.fila) {
      this.formularioRegistro.patchValue({
        factoresProtectores: this.datos.fila.factoresProtectores,
        factoresRiesgo: this.datos.fila.factoresRiesgo,
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
   * Registra los factores y cierra el diálogo
   */
  registrarFactores() {
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
    let factor = new FactoresPresentesDTO();
   
    if (this.datos?.fila) {
      // Modo edición: mantener el identificador existente
      factor.tokenIdentificador = this.datos.fila.tokenIdentificador;
    } else {
      // Modo nuevo registro: asignar identificador temporal
      factor.tokenIdentificador = "0";
    }
   
    // Limpiar espacios en blanco de los campos de texto
    factor.factoresProtectores = valoresFormulario.factoresProtectores?.trim();
    factor.factoresRiesgo = valoresFormulario.factoresRiesgo?.trim();

    // Cerrar el diálogo y devolver el objeto con los valores
    this.referenciaDialogo.close(factor);
  }
}