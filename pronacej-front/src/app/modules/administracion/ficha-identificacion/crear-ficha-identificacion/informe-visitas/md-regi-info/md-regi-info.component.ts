import { CommonModule, registerLocaleData } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule, MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import localeEs from '@angular/common/locales/es';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import etiquetasModel from 'app/core/etiquetas.model';

registerLocaleData(localeEs);

@Component({
  selector: 'app-md-regi-info',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './md-regi-info.component.html'
})
export class MdRegiInfoComponent implements OnInit {
  // Variables de formulario
  formularioRegistro: FormGroup;
  fechaInicio: Date = new Date();
  fechaFin: Date = new Date();

  // Variables de catálogo
  listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
  listaTiposAutorizacion: CatalogoDTO[] = [];
  mostrarCausalesRestriccion: boolean = false;
  
  // Variable para controlar visualización
  esVisualizacion: boolean = false;
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_INFORME_VISITAS;
  

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiInfoComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
    private adaptadorFecha: DateAdapter<any>,
    public utilidades: FuncionesUtils
  ) {
    this.adaptadorFecha.setLocale('es');
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.cargarCatalogos();
    
    // Verificar si es solo visualización
    if (this.datos?.fila?.esVisualizacion) {
      this.esVisualizacion = true;
    }
    
    if (this.datos?.fila) {
      this.cargarDatosEdicion();
      
      // Si es visualización, deshabilitar el formulario
      if (this.esVisualizacion) {
        this.formularioRegistro.disable();
      }
    } else {
      // Inicializar fechas para un nuevo registro
      this.fechaInicio = new Date();
      this.fechaFin = new Date();
      this.formularioRegistro.patchValue({
        fechaInicio: this.fechaInicio,
        fechaFin: this.fechaFin
      });
    }
    
    // Suscripción para mostrar/ocultar causales de restricción según el tipo
    this.formularioRegistro.get('tokenIdentificadorTipoAutorizacion').valueChanges.subscribe(valor => {
      const tipoRestriccion = this.listaTiposAutorizacion.find(t => t.nemonico === 'RESTRICCION_VISITAS');
      this.mostrarCausalesRestriccion = tipoRestriccion && valor === tipoRestriccion.tokenIdentificador;
      
      if (this.mostrarCausalesRestriccion) {
        this.formularioRegistro.get('causalesRestriccion').setValidators([this.validarNoEspacios()]);
      } else {
        this.formularioRegistro.get('causalesRestriccion').clearValidators();
      }
      
      this.formularioRegistro.get('causalesRestriccion').updateValueAndValidity();
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
        return control.value.trim().length === 0 && control.value.length > 0 ? { 'soloEspacios': true } : null;
      }
      
      return null;
    };
  }

  /**
   * Valida que los selects tengan un valor válido diferente de '0'
   * @returns Validador personalizado
   */
  validarSeleccion(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (!control.value || control.value === '0') {
        return { 'valorInvalido': true };
      }
      return null;
    };
  }

  /**
   * Valida que la fecha fin no sea anterior a la fecha inicio
   * @returns Validador personalizado
   */
  validarFechas(): ValidatorFn {
    return (formGroup: AbstractControl): { [key: string]: any } | null => {
      const fechaInicio = formGroup.get('fechaInicio')?.value;
      const fechaFin = formGroup.get('fechaFin')?.value;
      
      if (fechaInicio && fechaFin) {
        // Asegurarse de usar objetos Date para la comparación
        const fechaInicioDate = new Date(fechaInicio);
        const fechaFinDate = new Date(fechaFin);
        
        if (fechaFinDate < fechaInicioDate) {
          // Establecer el error directamente en el control fechaFin
          const controlFechaFin = formGroup.get('fechaFin');
          if (controlFechaFin) {
            controlFechaFin.setErrors({ 'fechaInvalida': true });
          }
          return { 'fechaInvalida': true };
        } else {
          // Si no hay error, asegurarse de limpiar el error específico de fechaInvalida
          // pero mantener otros errores que pueda tener el control
          const controlFechaFin = formGroup.get('fechaFin');
          if (controlFechaFin && controlFechaFin.errors) {
            const errores = { ...controlFechaFin.errors };
            if (errores['fechaInvalida']) {
              delete errores['fechaInvalida'];
            }
            
            if (Object.keys(errores).length === 0) {
              controlFechaFin.setErrors(null);
            } else {
              controlFechaFin.setErrors(errores);
            }
          }
        }
      }
      
      return null;
    };
  }

  construirFormulario() {
    this.formularioRegistro = this.constructorFormulario.group({
      tokenIdentificadorPersonaRelacionada: ['0', [Validators.required, this.validarSeleccion()]],
      tokenIdentificadorTipoAutorizacion: ['0', [Validators.required, this.validarSeleccion()]],
      fechaInicio: [new Date(), [Validators.required]],
      fechaFin: [new Date(), [Validators.required]],
      causalesRestriccion: ['', [this.validarNoEspacios()]],
      observaciones: ['', [Validators.required, this.validarNoEspacios()]]
    }, { validators: this.validarFechas() });
  }

  // Método para validar si el formulario tiene selecciones válidas
  formularioValido(): boolean {
    const personaRelacionada = this.formularioRegistro.get('tokenIdentificadorPersonaRelacionada').value;
    const tipoAutorizacion = this.formularioRegistro.get('tokenIdentificadorTipoAutorizacion').value;
    
    return personaRelacionada !== '0' && tipoAutorizacion !== '0';
  }

  // Método actualizado para actualizar la fecha cuando cambia en el datepicker
  actualizarFecha(evento: MatDatepickerInputEvent<Date>, nombreControl: string) {
    if (evento.value) {
      const fecha = evento.value;
      this.formularioRegistro.get(nombreControl).setValue(fecha);
      
      if (nombreControl === 'fechaInicio') {
        this.fechaInicio = fecha;
        
        // Validar si la nueva fecha de inicio es posterior a la fecha fin actual
        const fechaFin = this.formularioRegistro.get('fechaFin').value;
        if (fechaFin && fecha > new Date(fechaFin)) {
          // Si es posterior, actualizar la fecha fin para que sea igual a la fecha inicio
          this.formularioRegistro.get('fechaFin').setValue(fecha);
          this.fechaFin = new Date(fecha);
        }
      } else if (nombreControl === 'fechaFin') {
        this.fechaFin = fecha;
        
        // Validar si la nueva fecha fin es anterior a la fecha inicio actual
        const fechaInicio = this.formularioRegistro.get('fechaInicio').value;
        if (fechaInicio && fecha < new Date(fechaInicio)) {
          // Si ponemos un mensaje de error que se oculte después de unos segundos
          this.formularioRegistro.get('fechaFin').setErrors({ 'fechaInvalida': true });
        }
      }
      
      // Siempre actualizar la validez del formulario
      this.formularioRegistro.updateValueAndValidity();
    }
  }

  cargarCatalogos() {
    this.listaPersonasRelacionadas = this.datos.listaPersonasRelacionadas;
    this.listaTiposAutorizacion = this.datos.listaTiposAutorizacion;
  }

  cargarDatosEdicion() {
    const tipoRestriccion = this.listaTiposAutorizacion.find(t => t.nemonico === 'RESTRICCION_VISITAS');
    this.mostrarCausalesRestriccion = tipoRestriccion && 
                                    this.datos.fila.tokenIdentificadorTipoAutorizacion === tipoRestriccion.tokenIdentificador;
    
    this.fechaInicio = this.datos.fila.fechaInicio ? new Date(this.datos.fila.fechaInicio) : new Date();
    this.fechaFin = this.datos.fila.fechaFin ? new Date(this.datos.fila.fechaFin) : new Date();
    
    this.formularioRegistro.patchValue({
      tokenIdentificadorPersonaRelacionada: this.datos.fila.tokenIdentificadorPersonaRelacionada,
      tokenIdentificadorTipoAutorizacion: this.datos.fila.tokenIdentificadorTipoAutorizacion,
      fechaInicio: this.fechaInicio,
      fechaFin: this.fechaFin,
      causalesRestriccion: this.datos.fila.causalesRestriccion || '',
      observaciones: this.datos.fila.observaciones || ''
    });
  }

  agregarRegistro() {
    // Si es modo visualización, solo cerrar el diálogo
    if (this.esVisualizacion) {
      this.referenciaDialogo.close();
      return;
    }
    
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
    
    if (this.formularioRegistro.invalid || !this.formularioValido()) {
      if (tieneEspaciosEnBlanco) {
        return; // Los mensajes de error ya se muestran en el formulario
      }
      return;
    }
    
    if (this.formularioRegistro.valid && this.formularioValido()) {
      const valoresFormulario = this.formularioRegistro.value;
      const tokenIdentificador = this.datos?.fila ? this.datos.fila.tokenIdentificador : "0";

      // Usar las utilidades para formatear las fechas correctamente
      const fechaInicioFormateada = valoresFormulario.fechaInicio instanceof Date ? 
                                  valoresFormulario.fechaInicio : 
                                  this.utilidades.parseManualDate(valoresFormulario.fechaInicio);
                                  
      const fechaFinFormateada = valoresFormulario.fechaFin instanceof Date ? 
                                valoresFormulario.fechaFin : 
                                this.utilidades.parseManualDate(valoresFormulario.fechaFin);

      // Limpiar espacios en blanco de los campos de texto
      const observacionesLimpias = valoresFormulario.observaciones ? valoresFormulario.observaciones.trim() : '';
      const causalesRestriccionLimpias = valoresFormulario.causalesRestriccion ? valoresFormulario.causalesRestriccion.trim() : '';

      const informe = {
        tokenIdentificador: tokenIdentificador,
        tokenIdentificadorPersonaRelacionada: valoresFormulario.tokenIdentificadorPersonaRelacionada,
        tokenIdentificadorTipoAutorizacion: valoresFormulario.tokenIdentificadorTipoAutorizacion,
        fechaInicio: fechaInicioFormateada,
        fechaFin: fechaFinFormateada,
        causalesRestriccion: causalesRestriccionLimpias,
        observaciones: observacionesLimpias
      };

      this.referenciaDialogo.close(informe);
    }
  }
}