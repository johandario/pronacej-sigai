import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { SituacionEducativaLaboralOcioDTO } from 'app/core/model/both/SituacionEducativaLaboralOcioDTO.model';

@Component({
  selector: 'app-md-regi-situ',
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
  templateUrl: './md-regi-situ.component.html',
  styleUrl: './md-regi-situ.component.scss'
})
export class MdRegiSituComponent implements OnInit {
  
  formularioRegistroSituacion: FormGroup;
  
  listaSituacionesEducativas: CatalogoDTO[] = [];
  listaModalidadesEducativas: CatalogoDTO[] = [];
  listaRendimientosEducativos: CatalogoDTO[] = [];
  listaModalidadesEstudio: CatalogoDTO[] = [];
  listaNivelesEBR: CatalogoDTO[] = [];
  listaNivelesSuperior: CatalogoDTO[] = [];
  listaNivelesEBA: CatalogoDTO[] = [];

  mostrarNivelEBR: boolean = false;
  mostrarNivelSuperior: boolean = false;
  mostrarNivelEBA: boolean = false;
  
  formularioEnviandose: boolean = false;

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiSituComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
    private detectorCambios: ChangeDetectorRef
  ) {
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.listaSituacionesEducativas = this.datos.listaSituacionesEducativas;
    this.listaModalidadesEducativas = this.datos.listaModalidadesEducativas;
    this.listaRendimientosEducativos = this.datos.listaRendimientosEducativos;
    this.listaModalidadesEstudio = this.datos.listaModalidadesEstudio;
    this.listaNivelesEBR = this.datos.listaNivelesEBR;
    this.listaNivelesSuperior = this.datos.listaNivelesSuperior;
    this.listaNivelesEBA = this.datos.listaNivelesEBA;

    if (this.datos?.fila) {
      // Pre-cargar datos existentes
      this.construirFormulario(); // Reconstruir el formulario para evitar problemas
      
      setTimeout(() => {
        this.formularioRegistroSituacion.patchValue({
          situacionEducativa: this.datos.fila.tokenIdentificadorSituacionEducativa,
          centroEstudios: this.datos.fila.centroEstudios,
          rendimientoEducativo: this.datos.fila.tokenIdentificadorRendimientoEducativo,
          modalidadEducativa: this.datos.fila.tokenIdentificadorModalidadEducativa,
          modalidadEstudio: this.datos.fila.tokenIdentificadorModalidadEstudio,
        });
  
        // Actualizar campos de nivel basado en la modalidad guardada
        if (this.datos.fila.tokenIdentificadorModalidadEstudio) {
          this.actualizarCamposNivel(this.datos.fila.tokenIdentificadorModalidadEstudio);
          
          // Aplicar valores de nivel después de mostrar los campos correspondientes
          setTimeout(() => {
            const parche = {};
            
            if (this.mostrarNivelEBR && this.datos.fila.tokenIdentificadorNivelEBR) {
              parche['nivelEBR'] = this.datos.fila.tokenIdentificadorNivelEBR;
            }
            
            if (this.mostrarNivelSuperior && this.datos.fila.tokenIdentificadorNivelSuperior) {
              parche['nivelSuperior'] = this.datos.fila.tokenIdentificadorNivelSuperior;
            }
            
            if (this.mostrarNivelEBA && this.datos.fila.tokenIdentificadorNivelEBA) {
              parche['nivelEBA'] = this.datos.fila.tokenIdentificadorNivelEBA;
            }
            
            this.formularioRegistroSituacion.patchValue(parche);
            this.detectorCambios.detectChanges();
          }, 100);
        }
      }, 0);
    }

    // Suscribirse a cambios en modalidad de estudio
    this.formularioRegistroSituacion.get('modalidadEstudio')?.valueChanges.subscribe(value => {
      if (value && value !== '0') {
        this.actualizarCamposNivel(value);
      }
    });
  }

  /**
   * Construye el formulario con validaciones
   */
  private construirFormulario(): void {
    this.formularioRegistroSituacion = this.constructorFormulario.group({
      situacionEducativa: ['0', [Validators.required, this.validarSeleccion()]],
      centroEstudios: ['', [Validators.required, this.validarNoEspacios()]],
      rendimientoEducativo: ['0', [Validators.required, this.validarSeleccion()]],
      modalidadEducativa: ['0', [Validators.required, this.validarSeleccion()]],
      modalidadEstudio: ['0', [Validators.required, this.validarSeleccion()]],
      nivelEBR: ['0'],
      nivelSuperior: ['0'],
      nivelEBA: ['0']
    });
  }

  /**
   * Actualiza los campos de nivel según la modalidad de estudio seleccionada
   * @param tokenModalidadEstudio Token de la modalidad de estudio seleccionada
   */
  private actualizarCamposNivel(tokenModalidadEstudio: string): void {
    // Resetear todos los flags
    this.mostrarNivelEBR = false;
    this.mostrarNivelSuperior = false;
    this.mostrarNivelEBA = false;

    // Resetear validadores
    this.formularioRegistroSituacion.get('nivelEBR')?.setValidators([]);
    this.formularioRegistroSituacion.get('nivelSuperior')?.setValidators([]);
    this.formularioRegistroSituacion.get('nivelEBA')?.setValidators([]);
    
    // Resetear valores solo si no está enviando el formulario
    if (!this.formularioEnviandose) {
      this.formularioRegistroSituacion.get('nivelEBR')?.setValue('0');
      this.formularioRegistroSituacion.get('nivelSuperior')?.setValue('0');
      this.formularioRegistroSituacion.get('nivelEBA')?.setValue('0');
    }

    // Encontrar la modalidad seleccionada
    const modalidadSeleccionada = this.listaModalidadesEstudio.find(
      modalidad => modalidad.tokenIdentificador === tokenModalidadEstudio
    );

    if (modalidadSeleccionada) {
      // Actualizar según la modalidad seleccionada usando el nemónico
      switch (modalidadSeleccionada.nemonico) {
        case 'MODALIDAD_ESTUDIO_EBR':
          this.mostrarNivelEBR = true;
          this.formularioRegistroSituacion.get('nivelEBR')?.setValidators([Validators.required, this.validarSeleccion()]);
          break;
        case 'MODALIDAD_ESTUDIO_SUPERIOR':
          this.mostrarNivelSuperior = true;
          this.formularioRegistroSituacion.get('nivelSuperior')?.setValidators([Validators.required, this.validarSeleccion()]);
          break;
        case 'MODALIDAD_ESTUDIO_EBA':
          this.mostrarNivelEBA = true;
          this.formularioRegistroSituacion.get('nivelEBA')?.setValidators([Validators.required, this.validarSeleccion()]);
          break;
      }
    }

    // Actualizar validadores
    this.formularioRegistroSituacion.get('nivelEBR')?.updateValueAndValidity();
    this.formularioRegistroSituacion.get('nivelSuperior')?.updateValueAndValidity();
    this.formularioRegistroSituacion.get('nivelEBA')?.updateValueAndValidity();
    
    // Forzar detección de cambios
    this.detectorCambios.detectChanges();
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
   * Verifica si el formulario cumple con todas las validaciones
   */
  obtenerErroresFormulario(): string[] {
    const errores = [];
    const controles = this.formularioRegistroSituacion.controls;
    
    // Verificar campo por campo
    if (controles.situacionEducativa.invalid) {
      errores.push('Debe seleccionar una situación educativa válida');
    }
    
    if (controles.centroEstudios.invalid) {
      errores.push('El centro de estudios es obligatorio y no puede contener solo espacios');
    }
    
    if (controles.rendimientoEducativo.invalid) {
      errores.push('Debe seleccionar un rendimiento educativo válido');
    }
    
    if (controles.modalidadEducativa.invalid) {
      errores.push('Debe seleccionar una modalidad educativa válida');
    }
    
    if (controles.modalidadEstudio.invalid) {
      errores.push('Debe seleccionar una modalidad de estudio válida');
    }
    
    // Verificar campos dinámicos solo si están visibles
    if (this.mostrarNivelEBR && controles.nivelEBR.invalid) {
      errores.push('Debe seleccionar un nivel EBR válido');
    }
    
    if (this.mostrarNivelSuperior && controles.nivelSuperior.invalid) {
      errores.push('Debe seleccionar un nivel Superior válido');
    }
    
    if (this.mostrarNivelEBA && controles.nivelEBA.invalid) {
      errores.push('Debe seleccionar un nivel EBA válido');
    }
    
    return errores;
  }

  /**
   * Agrega o actualiza un registro de situación educativa
   */
  guardarRegistro(): void {
    // Evitar envíos múltiples
    if (this.formularioEnviandose) {
      return;
    }
    
    this.formularioEnviandose = true;
    
    // Marcar todos los campos como tocados para activar validaciones
    Object.keys(this.formularioRegistroSituacion.controls).forEach(key => {
      const control = this.formularioRegistroSituacion.get(key);
      control.markAsTouched();
      control.markAsDirty();
      control.updateValueAndValidity();
    });
    
    // Verificación de errores
    const errores = this.obtenerErroresFormulario();
    if (errores.length > 0) {
      this.formularioEnviandose = false;
      return;
    }
    
    // Capturar todos los valores antes de cualquier posible cambio
    const valoresFormulario = { ...this.formularioRegistroSituacion.value };
    
    // Verificar espacios en blanco
    if (typeof valoresFormulario.centroEstudios === 'string' && 
        valoresFormulario.centroEstudios.trim().length === 0 && 
        valoresFormulario.centroEstudios.length > 0) {
      this.formularioRegistroSituacion.get('centroEstudios').setErrors({ 'soloEspacios': true });
      this.formularioEnviandose = false;
      return;
    }
    
    // Crear el objeto DTO
    let situacion = new SituacionEducativaLaboralOcioDTO();
    
    situacion.tokenIdentificador = this.datos?.fila ? this.datos.fila.tokenIdentificador : "0";
    situacion.tokenIdentificadorFichaIdentificacion = this.datos.uuid_fp;
    situacion.centroEstudios = valoresFormulario.centroEstudios.trim();
    situacion.tokenIdentificadorSituacionEducativa = valoresFormulario.situacionEducativa;
    situacion.tokenIdentificadorRendimientoEducativo = valoresFormulario.rendimientoEducativo;
    situacion.tokenIdentificadorModalidadEducativa = valoresFormulario.modalidadEducativa;
    situacion.tokenIdentificadorModalidadEstudio = valoresFormulario.modalidadEstudio;
    
    // Asignar niveles según la modalidad
    if (this.mostrarNivelEBR) {
      situacion.tokenIdentificadorNivelEBR = valoresFormulario.nivelEBR;
    }
    
    if (this.mostrarNivelSuperior) {
      situacion.tokenIdentificadorNivelSuperior = valoresFormulario.nivelSuperior;
    }
    
    if (this.mostrarNivelEBA) {
      situacion.tokenIdentificadorNivelEBA = valoresFormulario.nivelEBA;
    }

    this.referenciaDialogo.close(situacion);
  }
}