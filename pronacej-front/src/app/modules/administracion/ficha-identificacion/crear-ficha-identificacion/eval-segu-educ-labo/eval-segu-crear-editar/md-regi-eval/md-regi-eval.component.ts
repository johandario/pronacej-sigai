import { CommonModule, registerLocaleData } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule, MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import localeEs from '@angular/common/locales/es';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { RecomendacionComentarioPorEvalSeguDTO } from 'app/core/model/both/recomendacionComentarioPorEvalSeguDTO.model';

registerLocaleData(localeEs);

/**
 * Componente modal para agregar o editar recomendaciones y comentarios
 */
@Component({
  selector: 'app-md-regi-eval',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './md-regi-eval.component.html',
  styleUrl: './md-regi-eval.component.scss'
})
export class MdRegiEvalComponent implements OnInit {
  // Fecha actual para el formulario
  fechaActual: Date = new Date();

  // Formulario de registro
  formularioRecomendacion: FormGroup;

  constructor(
    private constructorFormulario: FormBuilder,
    private refDialogo: MatDialogRef<MdRegiEvalComponent>,
    public utilidades: FuncionesUtils,
    @Inject(MAT_DIALOG_DATA) public datos: any,
    private adaptadorFecha: DateAdapter<any>
  ) { 
    this.adaptadorFecha.setLocale('es');
    this.construirFormulario();
  }

  ngOnInit(): void {
    if (this.datos?.fila) {
      const fecha = new Date(this.datos.fila.fecha);
      this.fechaActual = fecha;
      
      this.formularioRecomendacion.patchValue({
        comentario: this.datos.fila.comentario,
        fecha: fecha
      });
    } else {
      const ahora = new Date();
      this.formularioRecomendacion.patchValue({
        fecha: ahora
      });
    }
  }

  /**
   * Construye el formulario con validadores
   */
  private construirFormulario() {
    this.formularioRecomendacion = this.constructorFormulario.group({
      comentario: ['', [Validators.required, this.validarNoEspacios()]],
      fecha: [new Date(), [Validators.required]]
    });
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (control.value === null || control.value === undefined) {
        return null;
      }
      
      if (typeof control.value === 'string') {
        return control.value.trim().length === 0 && control.value.length > 0 ? { 'soloEspacios': true } : null;
      }
      
      return null;
    };
  }

  /**
   * Actualiza la fecha en el formulario
   */
  actualizarFecha(evento: MatDatepickerInputEvent<Date>, nombreControl: string) {
    if (evento.value) {
      const fecha = evento.value;
      this.formularioRecomendacion.get(nombreControl).setValue(fecha);
    }
  }

  /**
   * Guarda la recomendación y cierra el diálogo
   */
  guardarRecomendacion() {
    // Marcar todos los campos como tocados para validar
    Object.keys(this.formularioRecomendacion.controls).forEach(key => {
      const control = this.formularioRecomendacion.get(key);
      control.markAsTouched();
    });
    
    // Verificar espacios en blanco
    let tieneEspaciosEnBlanco = false;
    Object.keys(this.formularioRecomendacion.controls).forEach(key => {
      const control = this.formularioRecomendacion.get(key);
      if (control.errors && control.errors['soloEspacios']) {
        tieneEspaciosEnBlanco = true;
      }
    });
    
    if (this.formularioRecomendacion.invalid) {
      return;
    }
    
    const valoresFormulario = this.formularioRecomendacion.value;
    let recomendacion = new RecomendacionComentarioPorEvalSeguDTO();
    
    if (this.datos?.fila) {
      recomendacion.tokenIdentificador = this.datos.fila.tokenIdentificador;
    } else {
      recomendacion.tokenIdentificador = "0";
    }
    
    // Limpiar espacios en blanco del comentario
    recomendacion.comentario = valoresFormulario.comentario?.trim();
    recomendacion.fecha = valoresFormulario.fecha;

    this.refDialogo.close(recomendacion);
  }
}