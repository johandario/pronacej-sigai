import { ChangeDetectorRef, Component,Input,OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormsModule, UntypedFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink} from '@angular/router';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter,DateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { Observable, Subject, takeUntil,  } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { ContactoAdolescenteService } from '../contacto.service';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { User } from 'app/core/user/user.types';
import { ContactoAdolescenteDTO } from 'app/core/model/both/ContactoAdolescenteDTO.model';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { CUSTOM_DATE_FORMATS , FuncionesUtils,CustomDateAdapter} from 'app/core/utils/funcionesUtils.model';
import { UserService } from 'app/core/user/user.service';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
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

@Component({
  selector: 'app-crear-contacto',
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
    MatPaginatorModule,
    MatTableModule,
    MatCardModule,
    RouterLink,
  ],
  templateUrl: './crear-contacto.component.html',
  styleUrl: './crear-contacto.component.scss',
  providers: [ 
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class CrearContactoComponent implements OnInit{
  isLoading: boolean =  false;
  tokenID: string;
  fuga: ContactoAdolescenteDTO = new ContactoAdolescenteDTO();
  estado: string = '';
  horaISO: string
  
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();
  uuid_fp: any;
  dataSource: any[] = [];
  user: User;
  modalidades: string[] = ['Virtual', 'Presencial'];
  proceso: ContactoAdolescenteDTO | null = null
  actualDate: any;
  base64Image: string | null = null;

  informeSucesosForm = this.fb.group({
    fechaRegistro: ['', Validators.required],
    horaRegistro: ['', Validators.required],
    usuarioResponsable: ['', Validators.required],
    modalidadEntrevista: ['', Validators.required],
    observaciones: ['', [Validators.required, noWhitespaceValidator()]],
    actividades: ['', [Validators.required, noWhitespaceValidator()]],
   
  

  });
  
  @Input({ required: true }) declare nemonicoMenu: string;
  adolescentes: FichaIdentificacionDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  esVisualizar: boolean = false;
   private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private router: Router, private route: ActivatedRoute,
    private salidaService: ContactoAdolescenteService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private _userService: UserService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,   
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
  ) {}

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    const proceso = history.state?.proceso;
    this.actualDate = new Date();
    const currentHour = this.actualDate.getHours().toString().padStart(2, '0'); 
    const currentMinutes = this.actualDate.getMinutes().toString().padStart(2, '0'); 
    const formattedTime = `${currentHour}:${currentMinutes}`; 
    this.informeSucesosForm.patchValue({
      fechaRegistro: this.actualDate,
      horaRegistro: formattedTime,
    });
      this._userService.user$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((user: User) => {
                this.user = user;
                if (this.user?.name) {
                    this.informeSucesosForm.patchValue({
                      usuarioResponsable: this.user.name,
                    });
                    this.cdr.detectChanges();
                }
            });
    if (proceso) {
        this.fuga = proceso;
        this.proceso = proceso
        this.informeSucesosForm.patchValue(proceso);
        const fecha = new Date(this.fuga.fechaRegistro);
        let fechaDerivacionFormateada: Date | null = null;
        fechaDerivacionFormateada = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
        this.horaISO = fecha.toTimeString().split(' ')[0].slice(0, 5);
        this.informeSucesosForm.patchValue({
          fechaRegistro: fechaDerivacionFormateada,
          horaRegistro: this.horaISO
        });
        this.informeSucesosForm.disable();
    } else {
        console.error(" No se recibió el objeto proceso en el componente destino");
    }
    }

  
 
  
  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    if (this.informeSucesosForm.invalid) {
      this.dialogMensajeService.mensajeError('Por favor, completa todos los campos obligatorios antes de guardar.');
      return;
    }
    this.route.queryParams.subscribe(() => {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de gestión de contacto',
        "¿Deseas continuar?"
      );
      ref.afterClosed().subscribe({
        next: (resp: "confirmed" | "cancelled") => {
          if (resp === "confirmed") {
            Object.assign(this.fuga, this.informeSucesosForm.value);
            const fechaSeleccionada = this.informeSucesosForm.value.fechaRegistro;
            const horaSeleccionada = this.informeSucesosForm.value.horaRegistro;
            if (fechaSeleccionada && horaSeleccionada) {
              const [hora, minutos] = horaSeleccionada.split(':');
              const fechaHoraCombinada = new Date(fechaSeleccionada);
              fechaHoraCombinada.setHours(Number(hora), Number(minutos));
              this.fuga.fechaRegistro = fechaHoraCombinada.toISOString(); // Convertir a formato ISO
            }
            this.fuga.tokenFichaIdentificacion = this.uuid_fp;
            this.salidaService.crearEditarContacto(this.fuga, '').subscribe({
              next: (response: RespuestaPorDefecto<ContactoAdolescenteDTO>) => {
                if (!response.exito) {
                  this.salidaService.checkError(response);
                  return;
                }
                this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`]);
              },
              error: (error: any) => {
                console.error('Error al guardar:', error);
                this.salidaService.checkError(error);
              },
            });
          }
        },
      });
    });
  }
  

  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select(); 
  }
  


  validarHora(): Validators {
    return (control: FormControl): { [key: string]: any } | null => {
      const hora = control.value;
      if (!hora) {
        return null; 
      }
      const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
      if (!horaRegex.test(hora)) {
        return { invalid: true }; 
      }
      return null;
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

  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.informeSucesosForm.get(controlName).setValue(fecha);
    }
  }
  

  onFechaManual(event: any) {
    const valorIngresado = event.target.value;
    if (valorIngresado) {
        const partes = valorIngresado.split('/');
        if (partes.length === 3) {
            const fechaConvertida = new Date(`${partes[2]}-${partes[1]}-${partes[0]}`);
            if (!isNaN(fechaConvertida.getTime())) {
                this.informeSucesosForm.patchValue({ fechaRegistro: fechaConvertida });
                this.informeSucesosForm.get('fechaRegistro')?.updateValueAndValidity();
                console.log("Fecha manual válida, establecida en el formulario:", fechaConvertida);
            } else {
                console.warn("Fecha ingresada no válida");
                this.informeSucesosForm.get('fechaRegistro')?.setErrors({ invalid: true });
            }
        }
    }
}

arrayBufferToBase64(buffer: ArrayBuffer): string {
  const binary = String.fromCharCode(...new Uint8Array(buffer));
  return window.btoa(binary);
}

formatFecha(fecha: string): string {
  const date = new Date(fecha);
  return date.toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  });
}

formatHora(fecha: string): string {
  const date = new Date(fecha);
  return date.toLocaleTimeString('es-ES');
}

loadImageAsBase64() {
  this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
    .subscribe((data: ArrayBuffer) => {
      const base64String = this.arrayBufferToBase64(data);
      this.base64Image = `data:image/png;base64,${base64String}`;
    });
}

pruebaPdf() {
  this.loadImageAsBase64();
  const fechaRegistro = this.formatFecha((new Date).toString())
  const horaRegistro= this.formatHora((new Date).toString())
  const titulopantala= "Informe de gestión de contacto con adolescente"
  Object.assign(this.fuga, this.informeSucesosForm.value);
  const user= this.user.name;
  
  
  let request = new GeneracionPdfRequest();
  request.nemonico = etiquetasModel.FORMULARIO_GESTION_CONTACTO;
  request.variables = {
    "[IMG_BASE64]": this.base64Image,
    "[TITULO-PLANTILLA]":titulopantala, 
    "[TITULO-INFORME]": titulopantala,
    "[FECHA-REGISTRO]": fechaRegistro,
    "[HORA-REGISTRO]": horaRegistro,
    "[USUARIO-RESPONSABLE]": user,
    "[MODALIDAD-ENTREVISTA]": this.fuga.modalidadEntrevista,
    "[OBSERVACIONES]": this.fuga.observaciones,
    "[ACTIVIDADES]": this.fuga.actividades,
   
  }

  
  this.pdfService.generarPdf(request, '').subscribe({
    next: (response: RespuestaPorDefecto<string>) => {
      if (!response.exito) {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
        return;
      }
      const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
  const pwa = window.open(url);
    },
    error: (error: any) => {
      this.dialogMensajeService.mensajeError(
        'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
      );
    }
  });
}


}
