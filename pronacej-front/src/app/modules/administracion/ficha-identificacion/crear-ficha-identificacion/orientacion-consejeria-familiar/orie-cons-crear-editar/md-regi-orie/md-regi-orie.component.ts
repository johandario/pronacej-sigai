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

registerLocaleData(localeEs);

@Component({
  selector: 'app-md-regi-orie',
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
  templateUrl: './md-regi-orie.component.html',
  styleUrl: './md-regi-orie.component.scss'
})
export class MdRegiOrieComponent implements OnInit {
  fechaActual: Date = new Date();

  formularioAgregarRegistro = this.constructorFormulario.group({
    descripcion: ['', [Validators.required, this.validarNoEspacios()]],
    fecha: [new Date(), [Validators.required]],
    hora: ['', [Validators.required]]
  });

  constructor(
    private constructorFormulario: FormBuilder,
    private refDialogo: MatDialogRef<MdRegiOrieComponent>,
    public utilidades: FuncionesUtils,
    @Inject(MAT_DIALOG_DATA) public datos: any,
    private adaptadorFecha: DateAdapter<any>
  ) { 
    this.adaptadorFecha.setLocale('es');
  }

  ngOnInit(): void {
    if (this.datos?.fila) {
      const fecha = new Date(this.datos.fila.fecha);
      this.fechaActual = fecha;
      
      this.formularioAgregarRegistro.patchValue({
        descripcion: this.datos.fila.descripcion,
        fecha: fecha,
        hora: `${fecha.getHours().toString().padStart(2, '0')}:${fecha.getMinutes().toString().padStart(2, '0')}`
      });
    } else {
      const ahora = new Date();
      this.formularioAgregarRegistro.patchValue({
        fecha: ahora,
        hora: `${ahora.getHours().toString().padStart(2, '0')}:${ahora.getMinutes().toString().padStart(2, '0')}`
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

  actualizarFecha(evento: MatDatepickerInputEvent<Date>, nombreControl: string) {
    if (evento.value) {
      const fecha = evento.value;
      this.formularioAgregarRegistro.get(nombreControl).setValue(fecha);
    }
  }

  agregarRegistro() {
    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioAgregarRegistro.controls).forEach(key => {
      const control = this.formularioAgregarRegistro.get(key);
      control.markAsTouched();
    });
    
    // Verificar si hay campos con error de "soloEspacios"
    let tieneEspaciosEnBlanco = false;
    Object.keys(this.formularioAgregarRegistro.controls).forEach(key => {
      const control = this.formularioAgregarRegistro.get(key);
      if (control.errors && control.errors['soloEspacios']) {
        tieneEspaciosEnBlanco = true;
      }
    });
    
    if (this.formularioAgregarRegistro.invalid) {
      return;
    }
    
    const valoresFormulario = this.formularioAgregarRegistro.value;
    let orientacion: any = {};
    
    if (this.datos?.fila) {
      orientacion.tokenIdentificador = this.datos.fila.tokenIdentificador;
    } else {
      orientacion.tokenIdentificador = "0";
    }
    
    // Manejar la fecha correctamente, ya sea desde el datepicker o entrada manual
    let fechaFinal: Date;
    const valorFecha = valoresFormulario.fecha;
    
    if (valorFecha instanceof Date) {
      fechaFinal = new Date(valorFecha);
    } else if (typeof valorFecha === 'string') {
      const fechaAnalizada = this.utilidades.parseManualDate(valorFecha);
      if (!fechaAnalizada) {
        // Si la fecha no se puede analizar, usar la fecha actual
        fechaFinal = new Date();
      } else {
        fechaFinal = fechaAnalizada;
      }
    } else {
      fechaFinal = new Date();
    }
    
    // Agregar la hora seleccionada
    const [horas, minutos] = (valoresFormulario.hora || '00:00').split(':');
    fechaFinal.setHours(parseInt(horas), parseInt(minutos));
    
    // Limpiar espacios en blanco de la descripción
    orientacion.descripcion = valoresFormulario.descripcion?.trim();
    orientacion.fecha = fechaFinal;

    this.refDialogo.close(orientacion);
  }
}