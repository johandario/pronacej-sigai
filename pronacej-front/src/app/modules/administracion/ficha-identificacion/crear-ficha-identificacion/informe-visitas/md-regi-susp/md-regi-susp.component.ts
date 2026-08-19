import { CommonModule, registerLocaleData } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule, MatDatepickerInputEvent } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import localeEs from '@angular/common/locales/es';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { CometimientoInfraccionDTO } from 'app/core/model/both/cometimientoInfraccionDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';

registerLocaleData(localeEs);

@Component({
  selector: 'app-md-regi-susp',
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
    MatNativeDateModule,
    MatCheckboxModule
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  templateUrl: './md-regi-susp.component.html'
})
export class MdRegiSuspComponent implements OnInit {
  // Variables de formulario
  formularioSuspension: FormGroup;
  fechaInicio: Date = new Date();
  fechaFin: Date = new Date();

  // Variables de catálogo
  listaCausalesSuspension: CatalogoDTO[] = [];
  
  // Variable para controlar visualización
  esVisualizacion: boolean = false;
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_INFORME_VISITAS;

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiSuspComponent>,
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
        this.formularioSuspension.disable();
      }
    } else {
      // Inicializar fechas para un nuevo registro
      this.fechaInicio = new Date();
      this.fechaFin = new Date();
      this.formularioSuspension.patchValue({
        fechaInicio: this.fechaInicio,
        fechaFin: this.fechaFin
      });

      // Para un nuevo registro, inicializar los checkboxes de cometimientos
      this.inicializarCometimientos();
    }
  }

  /**
   * Inicializa los controles de cometimientos de infracción
   */
  inicializarCometimientos() {
    // Limpiar el FormArray de cometimientos
    while (this.arregloDeCometimientos.length !== 0) {
      this.arregloDeCometimientos.removeAt(0);
    }
    
    // Agregar un FormGroup por cada causal en el catálogo
    this.listaCausalesSuspension.forEach(causal => {
      this.arregloDeCometimientos.push(
        this.constructorFormulario.group({
          tokenIdentificador: ['0'],
          tokenIdentificadorCausalSuspension: [causal.tokenIdentificador],
          seleccionado: [false],
          nombreCausal: [causal.nombre]
        })
      );
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
   * Valida que al menos un checkbox esté seleccionado
   */
  validarAlMenosUnCometimientoSeleccionado(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const formArray = control as FormArray;
      const seleccionados = formArray.controls
        .filter(control => control.get('seleccionado')?.value === true);
      
      return seleccionados.length === 0 ? { 'requiereSeleccion': true } : null;
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
    this.formularioSuspension = this.constructorFormulario.group({
      fechaInicio: [new Date(), [Validators.required]],
      fechaFin: [new Date(), [Validators.required]],
      oficioDeSancion: ['', [Validators.required, this.validarNoEspacios()]],
      observaciones: ['', [Validators.required, this.validarNoEspacios()]],
      cometimientos: this.constructorFormulario.array([], [this.validarAlMenosUnCometimientoSeleccionado()])
    }, { validators: this.validarFechas() });
  }

  // Getter para acceder al FormArray de cometimientos
  get arregloDeCometimientos() {
    return this.formularioSuspension.get('cometimientos') as FormArray;
  }

  // Método para validar si el formulario tiene selecciones válidas
  formularioValido(): boolean {
    // Verificar si hay al menos un cometimiento seleccionado
    return this.arregloDeCometimientos.controls.some(control => 
      control.get('seleccionado')?.value === true
    );
  }

  // Método actualizado para actualizar la fecha cuando cambia en el datepicker
  actualizarFecha(evento: MatDatepickerInputEvent<Date>, nombreControl: string) {
    if (evento.value) {
      const fecha = evento.value;
      this.formularioSuspension.get(nombreControl)?.setValue(fecha);
      
      if (nombreControl === 'fechaInicio') {
        this.fechaInicio = fecha;
        
        // Validar si la nueva fecha de inicio es posterior a la fecha fin actual
        const fechaFin = this.formularioSuspension.get('fechaFin')?.value;
        if (fechaFin && fecha > new Date(fechaFin)) {
          // Si es posterior, actualizar la fecha fin para que sea igual a la fecha inicio
          this.formularioSuspension.get('fechaFin')?.setValue(fecha);
          this.fechaFin = new Date(fecha);
        }
      } else if (nombreControl === 'fechaFin') {
        this.fechaFin = fecha;
        
        // Validar si la nueva fecha fin es anterior a la fecha inicio actual
        const fechaInicio = this.formularioSuspension.get('fechaInicio')?.value;
        if (fechaInicio && fecha < new Date(fechaInicio)) {
          // Si es anterior, marcar error directamente
          this.formularioSuspension.get('fechaFin')?.setErrors({ 'fechaInvalida': true });
        }
      }
      
      // Siempre actualizar la validez del formulario
      this.formularioSuspension.updateValueAndValidity();
    }
  }

  cargarCatalogos() {
    this.listaCausalesSuspension = this.datos.listaCausalesSuspension;
  }

  cargarDatosEdicion() {
    this.fechaInicio = this.datos.fila.fechaInicio ? new Date(this.datos.fila.fechaInicio) : new Date();
    this.fechaFin = this.datos.fila.fechaFin ? new Date(this.datos.fila.fechaFin) : new Date();
    
    this.formularioSuspension.patchValue({
      fechaInicio: this.fechaInicio,
      fechaFin: this.fechaFin,
      oficioDeSancion: this.datos.fila.oficioDeSancion || '',
      observaciones: this.datos.fila.observaciones || ''
    });

    // Cargar los cometimientos desde los datos de la fila
    this.cargarCometimientos();
  }

  /**
   * Carga los cometimientos desde los datos de la suspensión
   */
  cargarCometimientos() {
    // Limpiar el FormArray
    while (this.arregloDeCometimientos.length !== 0) {
      this.arregloDeCometimientos.removeAt(0);
    }

    // Crear un mapa de los cometimientos existentes para fácil acceso
    const mapaCometimientos = new Map();
    if (this.datos.fila.cometimientosInfraccion) {
      this.datos.fila.cometimientosInfraccion.forEach((cometimiento: CometimientoInfraccionDTO) => {
        mapaCometimientos.set(cometimiento.tokenIdentificadorCausalSuspension, cometimiento);
      });
    }
    
    // Agregar todos los catálogos, marcando los que estén seleccionados
    this.listaCausalesSuspension.forEach(causal => {
      const cometimientoExistente = mapaCometimientos.get(causal.tokenIdentificador);
      
      this.arregloDeCometimientos.push(
        this.constructorFormulario.group({
          tokenIdentificador: [cometimientoExistente ? cometimientoExistente.tokenIdentificador : '0'],
          tokenIdentificadorCausalSuspension: [causal.tokenIdentificador],
          seleccionado: [cometimientoExistente ? cometimientoExistente.seleccionado : false],
          nombreCausal: [causal.nombre]
        })
      );
    });
  }

  agregarSuspension() {
    // Si es modo visualización, solo cerrar el diálogo
    if (this.esVisualizacion) {
      this.referenciaDialogo.close();
      return;
    }
    
    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioSuspension.controls).forEach(key => {
      const control = this.formularioSuspension.get(key);
      if (control instanceof FormArray) {
        control.markAsTouched();
      } else {
        control?.markAsTouched();
      }
    });
    
    // Verificar si hay campos con error de "soloEspacios"
    let tieneEspaciosEnBlanco = false;
    Object.keys(this.formularioSuspension.controls).forEach(key => {
      const control = this.formularioSuspension.get(key);
      if (control?.errors && control.errors['soloEspacios']) {
        tieneEspaciosEnBlanco = true;
      }
    });
    
    if (this.formularioSuspension.invalid || !this.formularioValido()) {
      if (tieneEspaciosEnBlanco) {
        return; // Los mensajes de error ya se muestran en el formulario
      }
      return;
    }
    
    if (this.formularioSuspension.valid && this.formularioValido()) {
      const valoresFormulario = this.formularioSuspension.value;
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
      const oficioDeSancionLimpio = valoresFormulario.oficioDeSancion ? valoresFormulario.oficioDeSancion.trim() : '';

      // Convertir los cometimientos del formulario al formato DTO
      const cometimientosInfraccion: CometimientoInfraccionDTO[] = [];
      const tokenIdentificadorCausalesSuspensionSeleccionadas: string[] = [];
      
      valoresFormulario.cometimientos.forEach((item: any) => {
        if (item.seleccionado) {
          tokenIdentificadorCausalesSuspensionSeleccionadas.push(item.tokenIdentificadorCausalSuspension);
        }
        
        const cometimientoDTO = new CometimientoInfraccionDTO();
        cometimientoDTO.tokenIdentificador = item.tokenIdentificador;
        cometimientoDTO.tokenIdentificadorCausalSuspension = item.tokenIdentificadorCausalSuspension;
        cometimientoDTO.seleccionado = item.seleccionado;
        cometimientosInfraccion.push(cometimientoDTO);
      });

      const suspension = {
        tokenIdentificador: tokenIdentificador,
        fechaInicio: fechaInicioFormateada,
        fechaFin: fechaFinFormateada,
        oficioDeSancion: oficioDeSancionLimpio,
        observaciones: observacionesLimpias,
        cometimientosInfraccion: cometimientosInfraccion,
        tokenIdentificadorCausalesSuspensionSeleccionadas: tokenIdentificadorCausalesSuspensionSeleccionadas
      };

      this.referenciaDialogo.close(suspension);
    }
  }
}