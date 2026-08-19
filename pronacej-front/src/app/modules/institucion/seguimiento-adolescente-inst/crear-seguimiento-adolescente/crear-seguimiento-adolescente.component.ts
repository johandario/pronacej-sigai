import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormsModule, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter, DateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { MatRadioModule } from '@angular/material/radio';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { SeguimientoAdolescInstDTO } from 'app/core/model/both/salida/SeguimientoAdolcInstDTO.model';
import { SeguimientoAdoelscInstService } from '../seguimiento-adolesc.service';
import { CUSTOM_DATE_FORMATS, FuncionesUtils, CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { TabService } from 'app/core/services/tab.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-crear-seguimiento-adolescente',
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
    MatRadioModule
  ],
  templateUrl: './crear-seguimiento-adolescente.component.html',
  styleUrl: './crear-seguimiento-adolescente.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class CrearSeguimientoAdolescenteComponent implements OnInit {

  isLoading: boolean = false;
  tokenID: string;
  fuga: SeguimientoAdolescInstDTO = new SeguimientoAdolescInstDTO();
  estado: string = '';
  horaISO: string
  fechaISO: string
  uuid_fp: string;
  listaProcesos: any;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  base64Image: string | null = null;
  proceso:any


  actualDate: any;
  modalidades: string[] = ['Virtual', 'Presencial'];
  informeSucesosForm = this.fb.group({
    fechaSeguimiento: [null, Validators.required],
    horaSeguimiento: ['', Validators.required],
    observacion: ['', Validators.required],
    recomendacion: ['', Validators.required],
    medioEntrevista: ['', Validators.required],
    resultadoEntrevista: ['', Validators.required],

  });

  @Input({ required: true }) declare nemonicoMenu: string;
  adolescentes: FichaIdentificacionDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  esVisualizar: boolean = false;
  nroDocumento: string
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  paginacion: Paginacion = new Paginacion();



  constructor(
    private router: Router, private route: ActivatedRoute,
    private salidaService: SeguimientoAdoelscInstService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private servicioTab: TabService,
    private http: HttpClient,   
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,

  ) { }

  ngOnInit(): void {
    this.actualDate = new Date();
    const currentHour = this.actualDate.getHours().toString().padStart(2, '0');
    const currentMinutes = this.actualDate.getMinutes().toString().padStart(2, '0');
    const formattedTime = `${currentHour}:${currentMinutes}`;
    this.informeSucesosForm.patchValue({
      fechaSeguimiento: this.actualDate,
      horaSeguimiento: formattedTime,
    });
    const proceso = history.state?.proceso;
    this.uuid_fp = history.state.tokenAdolescente
    if (!this.uuid_fp) {
      this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    }

    if (proceso && history.state.hasOwnProperty('editar')) {
      this.fuga = proceso;
      this.proceso = proceso
      if (this.fuga.fechaSeguimiento) {
        let fechaHoraString: string;
        if (this.fuga.fechaSeguimiento instanceof Date) {
          fechaHoraString = this.fuga.fechaSeguimiento.toISOString();
        } else {
          fechaHoraString = this.fuga.fechaSeguimiento;
        }
        const fechaLocal = new Date(fechaHoraString);
        this.informeSucesosForm.get('fechaSeguimiento')?.setValue(fechaLocal);
        const hora = fechaHoraString.split('T')[1].substring(0, 5);
        this.informeSucesosForm.get('horaSeguimiento')?.setValue(hora);
      }
      this.informeSucesosForm.patchValue({
        observacion: this.fuga.observacion,
        recomendacion: this.fuga.recomendacion,
        medioEntrevista: this.fuga.medioEntrevista,
        resultadoEntrevista: this.fuga.resultadoEntrevista,
      });
    }
    if (proceso && history.state.hasOwnProperty('crear')) {

      this.fuga.adolescenteDerivadoInst = proceso

    }

  }


  cancelar() {
    this.servicioTab.cambiarTab(1);
    const url = `/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}/crear-editar-derivado-institucion/${this.fuga.adolescenteDerivadoInst.tokenIdentificador}`;
    this.router.navigate([url], {
      state: {
        editar: true,
        proceso: this.fuga.adolescenteDerivadoInst,
        tokenAdolescente: this.uuid_fp
      }
    });
  }

  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    if (this.informeSucesosForm.invalid) {
      this.dialogMensajeService.mensajeError('Por favor, completa todos los campos obligatorios antes de guardar.');
      return;
    }
    this.route.queryParams.subscribe(() => {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un seguimiento',
        "¿Deseas continuar?"
      );
      ref.afterClosed().subscribe({
        next: (resp: "confirmed" | "cancelled") => {
          if (resp === "confirmed") {
            Object.assign(this.fuga, this.informeSucesosForm.value);
            const fechaSeleccionada = this.informeSucesosForm.value.fechaSeguimiento;
            const horaSeleccionada = this.informeSucesosForm.value.horaSeguimiento;
            if (fechaSeleccionada && horaSeleccionada) {
              const [hora, minutos] = horaSeleccionada.split(':');
              const fechaHoraCombinada = new Date(fechaSeleccionada);
              fechaHoraCombinada.setHours(Number(hora), Number(minutos));
              this.fuga.fechaSeguimiento = fechaHoraCombinada.toISOString();
            }
            this.salidaService.crearEditarSalida(this.fuga, '').subscribe({
              next: (response: RespuestaPorDefecto<SeguimientoAdolescInstDTO>) => {
                if (!response.exito) {
                  this.salidaService.checkError(response);
                  return;
                }
                const url = `/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}/crear-editar-derivado-institucion/${this.fuga.adolescenteDerivadoInst.tokenIdentificador}`;
                this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                this.router.navigate([url], {
                  state: {
                    editar: true,
                    proceso: this.fuga.adolescenteDerivadoInst,
                    tokenAdolescente: this.uuid_fp
                  }
                });

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


  getCatalogoMotivoSalida(): Observable<CatalogoDTO[]> {
    return this.catalogoService.obtenerHijos('MOTIVO_SALIDA', '').pipe(
      map(responseCatalog => responseCatalog.data || []),
      catchError(error => {
        console.error('Error al obtener motivo salida:', error);
        return of([]);
      })
    );
  }



  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.informeSucesosForm.get(controlName).setValue(fecha);
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
    const titulopantala= "Informe de seguimiento de adolescente derivados"
    Object.assign(this.fuga, this.informeSucesosForm.value);
    
    
    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_FUGA_ANALISTA;
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]":titulopantala, 
      "[TITULO-INFORME]": titulopantala,
      "[FECHA-REGISTRO]": fechaRegistro,
      "[HORA-REGISTRO]": horaRegistro,
      "[MODALIDAD-ENTREVISTA]": this.fuga.medioEntrevista,
      "[RESULTADO-ENTREVISTA]": this.fuga.resultadoEntrevista,
      "[RECOMENDACION]": this.fuga.recomendacion,
      "[FECHA-SEGUIMIENTO]": this.fuga.fechaSeguimiento? new Date(this.fuga.fechaSeguimiento).toISOString().split('T')[0]
      : "Fecha no disponible",
     
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
