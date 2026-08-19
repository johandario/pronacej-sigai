import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter,MAT_DATE_FORMATS } from '@angular/material/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogModule } from '@angular/material/dialog';
import { DetalleFichaAsistenciaPostEgresoDTO } from 'app/core/model/both/FichaAsistenciaPostEgreso.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';

import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CustomDateAdapter, FuncionesUtils,CUSTOM_DATE_FORMATS } from 'app/core/utils/funcionesUtils.model';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';


@Component({
  selector: 'app-modal-crear-editar-detalle-asistencia',
  standalone: true,
  imports: [MatButtonModule,
    MatExpansionModule,
    MatTableModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogModule,  
  ],
  templateUrl: './modal-crear-editar-detalle-asistencia.component.html',
  styleUrl: './modal-crear-editar-detalle-asistencia.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class ModalCrearEditarDetalleAsistenciaComponent {

  detalleActividadForm: FormGroup;

  tiposModalidadEntrevista: CatalogoDTO[] = [];
  tiposMotivoEntrevista: CatalogoDTO[] = [];
  tiposParentescoEntrevista: CatalogoDTO[] = [];

  tipoFormato: CatalogoDTO;

  constructor(private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModalCrearEditarDetalleAsistenciaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogMensajeService: DialogMensajeService,
    private dateAdapter: DateAdapter<any>,
    public funcionesUtils: FuncionesUtils,
  ) {
      this.dateAdapter.setLocale('es');
  }

  ngOnInit(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');
    this.tipoFormato = this.data.tipoFormato;
    this.detalleActividadForm = this.fb.group({
      fecha: [null, [Validators.required]],
      hora: [null, [Validators.required]],
      modalidadEntrevista: [null, [Validators.required]],
      personaEntrevistada: [null, [Validators.required]],
      descripcionActividad: [null, [Validators.required]],
      observaciones: [null, [Validators.required]],
      motivo: [null, [Validators.required]],
    });
    if (this.data.informacion) {
      console.log('data', this.data);
      const detalle = this.data.informacion as DetalleFichaAsistenciaPostEgresoDTO;

      this.detalleActividadForm.patchValue({
        fecha: new Date(detalle.fechaDetalle),
        hora: this.formatearHora(detalle.fechaDetalle),
        modalidadEntrevista: detalle.modalidadDeEntrevista,
        personaEntrevistada: detalle?.personaEntrevistada,
        descripcionActividad: detalle.descripcionActividad,
        observaciones: detalle.observaciones,
        motivo: detalle.motivo
      });
    }

    this.funcionesUtils.obtenerListaCatalogo('MODALIDAD_ENTREVISTA', '').subscribe({
      next: (data) => {
        this.tiposModalidadEntrevista = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo('PARENTESCO', '').subscribe({
      next: (data) => {
        this.tiposParentescoEntrevista = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo('MOTIVO_FICHA_ASISTENCIA', '').subscribe({
      next: (data) => {
        this.tiposMotivoEntrevista = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });
    console.log('tipo formato', this.tipoFormato);
    if (this.tipoFormato.nemonico == "NO_CONTINUAR_PROGRAMA_SEGUIMIENTO" || this.tipoFormato.nemonico == "ACTIVIDADES_PREVIAS_CULMINAR") {
      this.detalleActividadForm.get('personaEntrevistada').clearValidators();
      this.detalleActividadForm.get('motivo').clearValidators();

      this.detalleActividadForm.get('personaEntrevistada').updateValueAndValidity();
      this.detalleActividadForm.get('motivo').updateValueAndValidity();

      this.detalleActividadForm.get('personaEntrevistada').disable();
      this.detalleActividadForm.get('motivo').disable();

    } else if (this.tipoFormato.nemonico == "ACTIVIDAD_SEGUIMIENTO_PASPE") {

      this.detalleActividadForm.get('motivo').clearValidators();
      this.detalleActividadForm.get('motivo').updateValueAndValidity();
      this.detalleActividadForm.get('motivo').disable();

    }
    load.close();
  }


  formatearHora(fecha: Date | undefined): string {
    if (!fecha) return '';
    const d = new Date(fecha);
    return d.toISOString().substring(11, 16); // Formato HH:mm
  }

  compararCatalogos(o1: CatalogoDTO, o2: CatalogoDTO): boolean {
    return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
  }

  cerrar() {
    this.dialogRef.close(false);
  }

  guardarDetalleo() {

    if (this.detalleActividadForm.valid) {
      let detalle = new DetalleFichaAsistenciaPostEgresoDTO();
      if (this.data.informacion) {
        Object.assign(detalle, this.data.informacion);
      } 
      const fecha: Date = this.detalleActividadForm.get('fecha')?.value;
      const hora: string = this.detalleActividadForm.get('hora')?.value;

      const [hours, minutes] = hora.split(':').map(Number);
      const fechaHora = new Date(fecha);
      fechaHora.setHours(hours);
      fechaHora.setMinutes(minutes);
      fechaHora.setSeconds(0);
      fechaHora.setMilliseconds(0);
      detalle.modalidadDeEntrevista = this.detalleActividadForm.get('modalidadEntrevista').value;
      detalle.fechaDetalle = fechaHora;
      detalle.personaEntrevistada = this.detalleActividadForm.get('personaEntrevistada').value;
      detalle.observaciones = this.detalleActividadForm.get('observaciones').value;
      detalle.descripcionActividad = this.detalleActividadForm.get('descripcionActividad').value;
      detalle.motivo = this.detalleActividadForm.get('motivo').value;

      detalle.tokenIdentificadorFichaAsistenciaPostEgreso = this.data.asistenciaSeguimiento;
      if (this.data.informacion) {
        detalle.tokenIdentificador = this.data.informacion.tokenIdentificador;
        detalle.esEdicion = true;
      }
      console.log(detalle)
      this.dialogRef.close(detalle);
      // this.detalleActividadForm.disable();
      // this.planAsistenciaService.crearOEditarDetalleFichaAsistencia(detalle)
      //   .subscribe({
      //     next: response => {
      //       // Manejar la respuesta y cerrar el modal si es exitoso
      //       this.dialogMensajeService.mensajeExitoso(
      //         response.titulo,
      //         response.mensaje
      //       );
      //       this.dialogRef.close(response);
      //     },
      //     error: error => {
      //       this.planAsistenciaService.checkError(error);
      //       this.detalleActividadForm.enable();
      //       console.error(error);
      //     }
      //   });
    }

  }

  validarFormato() {

  }

  validarFormatoFecha(control) {
    if (!control?.value) return null;

    const regex = /^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/(19|20)\d{2}$/;
    return regex.test(control.value) ? null : { invalidFormat: true };
  }

  // Evitar que se escriban caracteres no permitidos
  validarInput(event: any) {
    const regex = /[0-9/]/;
    if (!regex.test(event.key) && event.key !== 'Backspace' && event.key !== 'Tab') {
      event.preventDefault();
    }
  }


  onFechaManual(event: any) {
    const valorIngresado = event.target.value;
    if (valorIngresado) {
        const partes = valorIngresado.split('/');
        if (partes.length === 3) {
            const fechaConvertida = new Date(`${partes[2]}-${partes[1]}-${partes[0]}`);
            if (!isNaN(fechaConvertida.getTime())) {
                this.detalleActividadForm.patchValue({ fecha: fechaConvertida });
                this.detalleActividadForm.get('fecha')?.updateValueAndValidity();
                console.log("Fecha manual válida, establecida en el formulario:", fechaConvertida);
            } else {
                console.warn("Fecha ingresada no válida");
                this.detalleActividadForm.get('fecha')?.setErrors({ invalid: true });
            }
        }
    }
}

}
