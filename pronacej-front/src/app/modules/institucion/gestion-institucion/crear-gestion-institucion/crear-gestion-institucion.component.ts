import { Component ,Input,OnInit} from '@angular/core';
import { AbstractControl, FormControl, FormsModule, UntypedFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter ,DateAdapter} from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { SeguimientoInstitucionService } from '../gestion-institucion.service';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { SeguimientoInstitucionDTO } from 'app/core/model/both/SeguimientoInstitucionDTO.model';
import { CUSTOM_DATE_FORMATS, FuncionesUtils,CustomDateAdapter  } from 'app/core/utils/funcionesUtils.model';
import { TabService } from 'app/core/services/tab.service';
import etiquetasModel from 'app/core/etiquetas.model';

export function noWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (typeof value === 'string') {
      const isWhitespace = value.trim().length === 0;
      const isValid = !isWhitespace;
      return isValid ? null : { whitespace: true };
    }
    return null; 
  };
}

export function noSpecialCharactersValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value || '';
    if (typeof value !== 'string') {
      return null; // No validar si el valor no es una cadena
    }
    const hasSpecialCharacters = /[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s]/.test(value);
    return hasSpecialCharacters ? { specialCharacters: true } : null;
  };
}

@Component({
  selector: 'app-crear-gestion-institucion',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatButtonModule,
    FormsModule,
    SubidaDeDocumentosComponent,
    MatExpansionModule,
    CommonModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatIconModule,
    QuillModule,
    RouterLink
  ],
  templateUrl: './crear-gestion-institucion.component.html',
  styleUrl: './crear-gestion-institucion.component.scss',
  providers: [ 
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
     { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class CrearGestionInstitucionComponent implements OnInit{
  isLoading: boolean =  false;
    tokenID: string;
    fuga: SeguimientoInstitucionDTO = new SeguimientoInstitucionDTO();
    estado: string
    horaISO: string
    fechaRegistroISO: string
    fechaISO: string
    uuid_fp: any;
    estadoList: string[] = ['Completo', 'Pendiente'];
    actualDate: any;
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_INSTITUCION;
    

    
  
    informeSucesosForm = this.fb.group({
      fechaRegistro: ['', Validators.required],
      horaRegistro: ['', Validators.required],
      numeroDoc: ['', Validators.required],
      estado: ['', Validators.required],
      fecha: ['', Validators.required],
      personaEntrevistada: ['', [Validators.required, noWhitespaceValidator()]],
      fortalezas: ['', [Validators.required, noWhitespaceValidator()]],
      debilidades: ['', [Validators.required, noWhitespaceValidator()]],
      cumpleObjetivo: ['', Validators.required],
      personaResponsable: ['', [Validators.required, noWhitespaceValidator()]],
      registroInstitucion: [''],
    });
   
    constructor(
      private router: Router, private route: ActivatedRoute,
      private seguimientoService: SeguimientoInstitucionService,
      private fb: UntypedFormBuilder,
      private dialogMensajeService: DialogMensajeService,
      private servicioTab: TabService,
    
  
    ) {}
  
    ngOnInit(): void {
      this.actualDate = new Date();
        const currentHour = this.actualDate.getHours().toString().padStart(2, '0'); 
        const currentMinutes = this.actualDate.getMinutes().toString().padStart(2, '0'); 
        const formattedTime = `${currentHour}:${currentMinutes}`; 
        this.informeSucesosForm.patchValue({
          fechaRegistro: this.actualDate,
          horaRegistro: formattedTime,
        });
        const proceso = history.state?.proceso;
        if (proceso && history.state.hasOwnProperty('editar')) {
          this.fuga= proceso
          if (this.fuga.fechaRegistro || this.fuga.fecha) {
            let fechaHoraString: string;
            let fechaString: string;
            if (this.fuga.fechaRegistro instanceof Date ) {
              fechaHoraString = this.fuga.fechaRegistro.toISOString();
            } else {
              fechaHoraString = this.fuga.fechaRegistro;
            
            }
            if (this.fuga.fecha instanceof Date ) {
              fechaString = this.fuga.fecha.toISOString();
              
            } else {
              fechaString = this.fuga.fecha;
            }
            const fechaLocal = new Date(fechaHoraString);
            const fecha = new Date(fechaString);
            this.informeSucesosForm.get('fechaRegistro')?.setValue(fechaLocal);
            const hora = fechaHoraString.split('T')[1].substring(0, 5);
            this.informeSucesosForm.get('horaRegistro')?.setValue(hora);
            this.informeSucesosForm.get('fecha')?.setValue(fecha);
            this.informeSucesosForm.patchValue({
              numeroDoc: this.fuga.numeroDoc,
              estado: this.fuga.estado,
              personaEntrevistada: this.fuga.personaEntrevistada,
              fortalezas: this.fuga.fortalezas,
              debilidades: this.fuga.debilidades,
              cumpleObjetivo: this.fuga.cumpleObjetivo,
              personaResponsable: this.fuga.personaResponsable,

            });
          }
          if (history.state.editar) {
            this.informeSucesosForm.enable();
        } else {
            this.informeSucesosForm.disable();
            
        }   
        }
        if(proceso && history.state.hasOwnProperty('crear')){
          this.fuga.registroInstitucion = proceso
        }
      
    }
    
  
    
    
    cancelar() {
      this.servicioTab.cambiarTab(1);
      this.router.navigate([`/institucion/registro-institucion/crear-editar/${this.fuga.registroInstitucion.tokenIdentificador}`], {
        state: {
          editar: true,  
          proceso: this.fuga.registroInstitucion
        }
      });
    }
    
  
    
  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    console.log(this.fuga.registroInstitucion);
  
    let errores = [];
    Object.keys(this.informeSucesosForm.controls).forEach(key => {
      const control = this.informeSucesosForm.get(key);
      if (control?.errors) {
        console.log(`${key}:`, {
          Valor: control.value,
          Validez: control.valid,
          Errores: control.errors
        });
        if (control.errors['required']) {
          errores.push(`El campo "${key}" es obligatorio.`);
        }
        if (control.errors['specialCharacters']) {
          errores.push(`El campo "${key}" no permite caracteres especiales.`);
        }
        if (control.errors['whitespace']) {
          errores.push(`El campo "${key}" no puede contener solo espacios en blanco.`);
        }
      }
    });
  
    if (errores.length > 0) {
      this.dialogMensajeService.mensajeError(errores.join('\n'));
      return;
    }
  
    if (!this.fuga.registroInstitucion) {
      this.dialogMensajeService.mensajeError('El registro de institución no está configurado.');
      return;
    }
  
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'Se creará un registro de seguimiento',
      "¿Deseas continuar?"
    );
  
    ref.afterClosed().subscribe({
      next: (resp: "confirmed" | "cancelled") => {
        if (resp === "confirmed") {
          const datosFormulario = this.informeSucesosForm.value;
          this.fuga = {
            ...this.fuga,
            ...datosFormulario,
            registroInstitucion: this.fuga.registroInstitucion
          };
  
          this.seguimientoService.crearEditarInstitucion(this.fuga, this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<SeguimientoInstitucionDTO>) => {
              if (!response.exito) {
                this.seguimientoService.checkError(response);
                return;
              }
              this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
              this.servicioTab.cambiarTab(1);
              this.router.navigate([`/institucion/registro-institucion/crear-editar/${this.fuga.registroInstitucion.tokenIdentificador}`], {
                state: {
                  editar: true,  
                  proceso: this.fuga.registroInstitucion
                }
              });
            },
            error: (error: any) => {
              console.error('Error al guardar:', error);
              this.seguimientoService.checkError(error);
            },
          });
        }
      }
    });
  }
  
    
    
    
    // editarProceso(proceso: SeguimientoInstitucionDTO) {
    //     this.router.navigate(['/flujo-trabajo/admin-procesos/crear-editar'], {queryParams: {ID: proceso.tokenIdentificador}})
    //   }
    
    
    onInputFocus(): void {
      const inputElement = (document.activeElement as HTMLInputElement);
      inputElement.select(); // Selecciona todo el texto
    }
    
    validarHora(): Validators {
      return (control: FormControl): { [key: string]: any } | null => {
        const hora = control.value;
        if (!hora) {
          return null; // Si está vacío, no valida nada (se encarga Validators.required).
        }
        // Validación del formato de hora (HH:mm)
        const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
        if (!horaRegex.test(hora)) {
          return { invalid: true }; // Marca el error como 'invalid'
        }
        return null; // Sin errores
      };
    }
  
    validarHoraInput(event: Event): void {
      const input = (event.target as HTMLInputElement);
      const hora = input.value;
      const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
      const isValid = horaRegex.test(hora);
      if (!isValid) {
        this.informeSucesosForm.controls['horaSalida'].setErrors({ invalid: true });
      } else {
        this.informeSucesosForm.controls['horaSalida'].setErrors(null);
      }
    
    
    }

    soloNumeros(event: KeyboardEvent) {
      const charCode = event.key.charCodeAt(0);
      if (charCode < 48 || charCode > 57) {
        event.preventDefault(); 
      }
    }
    
     actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
      if (event.value) {
          const fecha = event.value;
          this.informeSucesosForm.get(controlName).setValue(fecha);
      }
    }


    onFechaManual(event: any, controlName: string) {
      const valorIngresado = event.target.value;
      if (valorIngresado) {
          const partes = valorIngresado.split('/');
          if (partes.length === 3) {
              const fechaConvertida = new Date(`${partes[2]}-${partes[1]}-${partes[0]}`);
              if (!isNaN(fechaConvertida.getTime())) {
                  this.informeSucesosForm.patchValue({ [controlName]: fechaConvertida });
                  this.informeSucesosForm.get(controlName)?.updateValueAndValidity();
                  console.log(`Fecha manual válida (${controlName}):`, fechaConvertida);
              } else {
                  console.warn(`Fecha ingresada no válida en ${controlName}`);
                  this.informeSucesosForm.get(controlName)?.setErrors({ invalid: true });
              }
          }
      }
  }

}
