import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { LaboralDTO } from 'app/core/model/both/LaboralDTO.model';

@Component({
  selector: 'app-md-regi-labo',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule, 
  ],
  templateUrl: './md-regi-labo.component.html',
  styleUrl: './md-regi-labo.component.scss'
})
export class MdRegiLaboComponent implements OnInit {
  
  formularioRegistroLaboral: FormGroup;
  listaOcupacionesLaborales: CatalogoDTO[] = [];
  listaModalidadesLaborales: CatalogoDTO[] = [];
  listaRendimientosLaborales: CatalogoDTO[] = [];

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiLaboComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) {
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.listaOcupacionesLaborales = this.datos.listaOcupacionesLaborales;
    this.listaModalidadesLaborales = this.datos.listaModalidadesLaborales;
    this.listaRendimientosLaborales = this.datos.listaRendimientosLaborales;

    if (this.datos?.fila) {
      this.formularioRegistroLaboral.patchValue({
        experienciaLaboral: this.datos.fila.experienciaLaboral,
        ocupacionLaboral: this.datos.fila.tokenIdentificadorOcupacionLaboral,
        modalidadLaboral: this.datos.fila.tokenIdentificadorModalidadLaboral,
      });
    }
  }

  /**
   * Construye el formulario con las validaciones correspondientes
   */
  private construirFormulario(): void {
    this.formularioRegistroLaboral = this.constructorFormulario.group({
      experienciaLaboral: ['', [Validators.required, this.validarNoEspacios()]],
      ocupacionLaboral: ['0', [Validators.required, this.validarSeleccion()]],
      modalidadLaboral: ['0', [Validators.required, this.validarSeleccion()]],
    });
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
        return control.value.trim().length === 0 ? { 'soloEspacios': true } : null;
      }
      
      return null;
    };
  }

  /**
   * Valida que se haya seleccionado una opción válida en el select
   * @returns Validador personalizado
   */
  validarSeleccion(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      // Verificar que no sea el valor por defecto "0"
      return control.value === '0' ? { 'seleccionInvalida': true } : null;
    };
  }

  /**
   * Agrega o actualiza un registro laboral
   */
  guardarRegistro(): void {
    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioRegistroLaboral.controls).forEach(key => {
      const control = this.formularioRegistroLaboral.get(key);
      control.markAsTouched();
      control.markAsDirty();
      control.updateValueAndValidity();
    });
    
    if (this.formularioRegistroLaboral.valid) {
      // Verificar espacios en blanco
      let tieneEspaciosEnBlanco = false;
      
      Object.keys(this.formularioRegistroLaboral.controls).forEach(clave => {
        const control = this.formularioRegistroLaboral.get(clave);
        const valor = control.value;
        
        if (typeof valor === 'string' && valor !== null && valor !== undefined) {
          if (valor.trim().length === 0 && valor.length > 0) {
            tieneEspaciosEnBlanco = true;
            control.setErrors({ 'soloEspacios': true });
          }
        }
      });
      
      if (tieneEspaciosEnBlanco) {
        return;
      }
      
      const valoresFormulario = this.formularioRegistroLaboral.value;
      let registroLaboral = new LaboralDTO();
      
      registroLaboral.tokenIdentificador = this.datos?.fila ? this.datos.fila.tokenIdentificador : "0";
      registroLaboral.experienciaLaboral = valoresFormulario.experienciaLaboral.trim();
      registroLaboral.tokenIdentificadorOcupacionLaboral = valoresFormulario.ocupacionLaboral;
      registroLaboral.tokenIdentificadorModalidadLaboral = valoresFormulario.modalidadLaboral;

      this.referenciaDialogo.close(registroLaboral);
    }
  }
}