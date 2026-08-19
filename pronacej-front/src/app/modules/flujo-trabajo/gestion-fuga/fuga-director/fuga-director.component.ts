import { Component,OnInit } from '@angular/core';
import { FormsModule, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { ActivatedRoute, Router } from '@angular/router';
import { GestionFugaService } from '../gestion-fuga.service';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter,DateAdapter } from '@angular/material/core';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { HttpClient } from '@angular/common/http';
import { TareaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { CommonModule,Location } from '@angular/common';
import { NativeDateAdapter } from '@angular/material/core';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';



@Component({
  selector: 'app-fuga-director',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatButtonModule,
    CommonModule,
  FormsModule],
  templateUrl: './fuga-director.component.html',
  styleUrl: './fuga-director.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class FugaDirectorComponent implements OnInit  {

  isLoading: boolean =  false;
  tokenID: string;
  fuga: GestionFugaDTO = new GestionFugaDTO();
  estado: string = '';
  base64Image: string | null = null;
  tareaEntrante: TareaDTO = new TareaDTO;
  fechaISO: string
  funcionarioActivo: FuncionarioDTO;
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
  

  modeloOficioForm = this.fb.group({
    fechaInformeDirector: ['', Validators.required],
    dirigidoA: ['', Validators.required],
    de: ['', Validators.required],
    asunto: ['', Validators.required],
    descripcion: ['', Validators.required],
    accionesRealizadas: ['', Validators.required],
  });

  constructor(
    private router: Router, private route: ActivatedRoute,
    private gestionFugaService: GestionFugaService,
    private dialogMensajeService: DialogMensajeService,
    private fb: UntypedFormBuilder,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private http: HttpClient,   
    private _location: Location,
    private funcionarioService: FuncionarioService,


  ) {}

  ngOnInit(): void {
    // 1. Cargar tarea si llega desde el estado
    if (history.state.tareaEntrante && history.state.listaTareas) {
      this.tareaEntrante = history.state.tareaEntrante;
     
    }
    this.obtenerTokenDepartamento();
    this.route.queryParams.subscribe((params) => {
      this.tokenID = params['instancia'] || this.route.snapshot.params['tokenID'];
      this.estado = params['estado'];
      if (this.tokenID) {
        this.gestionFugaService.obtenerFugasPorTokenID(this.tokenID, this.nemonicoMenuinicio).subscribe((result) => {
          this.fuga = result.data;
          const fecha = new Date(this.fuga.fechaInformeDirector);
          this.fechaISO = fecha.toISOString().split('T')[0];
  
          // Si tiene descripción previa
          if (this.fuga.descripcionHechos) {
            this.modeloOficioForm.patchValue({
              descripcion: this.fuga.descripcionHechos,
            });
          }
  
          // Patch al resto de valores
          this.modeloOficioForm.patchValue({
            fechaInformeDirector: this.fuga.fechaInformeDirector
              ? new Date(this.fuga.fechaInformeDirector)
              : null,
            dirigidoA: this.fuga.dirigidoA,
            de: this.fuga.de,
            asunto: this.fuga.asunto,
            accionesRealizadas: this.fuga.accionesRealizadas,
          });
  
          // (Opcional) Si el estado es 'Completada', puedes desactivar campos
          // if (this.estado === 'Completada') {
          //   this.modeloOficioForm.disable();
          // }
        });
      }
    });
  }
  
  cancelar() {
    this._location.back();
    // this.router.navigate([`/flujo-trabajo/bandeja-entrada`]);
  }

  guardarFuga(){
    this.route.queryParams.subscribe(params => {
      const tokenInstancia = params['instancia'];
      if (tokenInstancia) {
        this.fuga.tokenInstancia = tokenInstancia;
      } else {
        this.fuga.tokenInstancia = this.tokenID;
      }
      this.modeloOficioForm.markAllAsTouched(); 
      if (this.modeloOficioForm.invalid) {
        this.dialogMensajeService.mensajeError('Por favor, completa todos los campos obligatorios antes de guardar.');
        return;
      }
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de fuga',
        "Deseas continuar?"
      );
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") { 
              Object.assign(this.fuga, this.modeloOficioForm.value);
              const tareaEventoFuga = {
                eventoFuga: this.fuga,
                tarea: this.tareaEntrante
              };
      
              
              this.gestionFugaService.crearEditarFuga(tareaEventoFuga, '').subscribe(
                {
                  next: (response: RespuestaPorDefecto<GestionFugaDTO>) => {
                    
                    if (!response.exito) {
                      this.gestionFugaService.checkError(response);
          
                      return;
                    }                  
                    this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);     
                    this.router.navigate([`/flujo-trabajo/bandeja-salida`])
             
                  },
                  error: (error: any) => {
                    this.gestionFugaService.checkError(error);
                  }
                }
              )
            }
          }
        }
      );
    })
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
    setTimeout(() => {
    const fechaRegistro = this.formatFecha((new Date).toString())
    const horaRegistro= this.formatHora((new Date).toString())
    const titulopantala= "Informe de fuga"
    Object.assign(this.fuga, this.modeloOficioForm.value);
    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_FUGA_DIRECTOR;
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]": titulopantala,
      "[TITULO-INFORME]": titulopantala,
      "[FECHA_REGISTRO]": fechaRegistro,
      "[HORA_REGISTRO]": horaRegistro,
      "[DESCRIPCION]": this.fuga.descripcionHechos,
      "[ACCIONES-REALIZADAS]": this.fuga.accionesRealizadas,
      "[DIRIGIDO-A]": this.fuga.dirigidoA,
      "[DE]": this.fuga.de,
      "[ASUNTO]": String(this.fuga.asunto),
      "[FECHA-INFORME]": this.fuga.fechaInformeDirector? new Date(this.fuga.fechaInformeDirector).toISOString().split('T')[0]: "Fecha no disponible", 
      "[NUMERO-IDENTIFICACION]": this.fuga.numeroIdentificacion,
      "[NOMBRE-ADOLESCENTE]": this.fuga.nombreAdolescente,
      "[EDAD]": `${this.funcionesUtils.getEdad(String(this.fuga.fechaNacimiento))}`,
      "[CENTRO]": this.funcionarioActivo.departamento,
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
  }, 500); 
  }

  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.modeloOficioForm.get(controlName).setValue(fecha);
    }
  }

  // validarFechaManual(event: FocusEvent, controlName: string): void {
  //   const input = event.target as HTMLInputElement;
  //   const valor = input.value;
  
  //   if (valor) {
  //     const partes = valor.split('/');
  //     if (partes.length === 3) {
  //       const [dia, mes, anio] = partes.map(Number);
  //       const fecha = new Date(anio, mes - 1, dia);
  //       if (!isNaN(fecha.getTime())) {
  //         this.modeloOficioForm.get(controlName)?.setValue(fecha);
  //       }
  //     }
  //   }
  // }
  

  validarFechaManual(event: FocusEvent, controlName: string): void {
    const input = event.target as HTMLInputElement;
    const valor = input.value?.trim();
    
    if (valor) {
      const partes = valor.split('/');
      if (partes.length === 3) {
        const [diaStr, mesStr, anioStr] = partes;
        const dia = Number(diaStr);
        const mes = Number(mesStr);
        const anio = Number(anioStr);
  
        if (!isNaN(dia) && !isNaN(mes) && !isNaN(anio)) {
          // Crea la fecha en formato UTC para evitar desfases
          const fecha = new Date(Date.UTC(anio, mes - 1, dia));
          // Validar que los componentes correspondan (evitar que 31/02 pase)
          if (fecha.getUTCFullYear() === anio && 
              fecha.getUTCMonth() === mes - 1 && 
              fecha.getUTCDate() === dia) {
            this.modeloOficioForm.get(controlName)?.setValue(fecha);
          } else {
            this.modeloOficioForm.get(controlName)?.setValue(null);
          }
        } else {
          this.modeloOficioForm.get(controlName)?.setValue(null);
        }
      } else {
        this.modeloOficioForm.get(controlName)?.setValue(null);
      }
    }
  }
  


  onFechaManual(event: any) {
    const valorIngresado = event.target.value;
    if (valorIngresado) {
        const partes = valorIngresado.split('/');
        if (partes.length === 3) {
          const fechaConvertida = new Date(Number(partes[2]), Number(partes[1]) - 1, Number(partes[0]));

            if (!isNaN(fechaConvertida.getTime())) {
                this.modeloOficioForm.patchValue({ fechaInformeDirector: fechaConvertida });
                this.modeloOficioForm.get('fechaInformeDirector')?.updateValueAndValidity();
            } else {
                console.warn("Fecha ingresada no válida");
                this.modeloOficioForm.get('fechaInformeDirector')?.setErrors({ invalid: true });
            }
        }
    }
}

obtenerTokenDepartamento(): Promise<void> {
    return new Promise((resolve) => {
      this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenuinicio).subscribe({
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          if (!response.exito) {
            resolve();
            return;
          }
          this.funcionarioActivo = response.data;
          console.log(this.funcionarioActivo);
          resolve();
        },
        error: (error: any) => {
          console.error('Error al obtener el departamento:', error);
          resolve();
        }
      });
    });
  }
  

}
