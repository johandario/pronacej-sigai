import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { SeguimientoActividadOcupacionalDTO } from 'app/core/model/both/SeguimientoActividadOcupacional.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EvaluacionConductualService } from 'app/modules/seguridad/services/evaluacionConductual.service';
import { getEspPaginatorIntl } from 'app/app.component';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';

@Component({
  selector: 'app-crear-editar-seguimiento-actividad-ocupacional',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    CommonModule,
    MatSlideToggleModule,
    MatDatepickerModule],
  templateUrl: './crear-editar-seguimiento-actividad-ocupacional.component.html',
  styleUrl: './crear-editar-seguimiento-actividad-ocupacional.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
})
export class CrearEditarSeguimientoActividadOcupacionalComponent {

  seguimientoActividadForm: FormGroup;

  constructor(private fb: FormBuilder,
    public dialogRef: MatDialogRef<CrearEditarSeguimientoActividadOcupacionalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private evaluacionConductualService: EvaluacionConductualService,
    private dialogMensajeService: DialogMensajeService,
    private dateAdapter: DateAdapter<any>,) {
    this.dateAdapter.setLocale('es');

  }

  ngOnInit(): void {
    this.seguimientoActividadForm = this.fb.group({
      actividad: [null, [Validators.required]],
      fechaActividad: [null, [Validators.required]],
      vigente: [false],
      observaciones: [null]
    });
    console.log('data', this.data);
    if (this.data.informacion) {
      this.seguimientoActividadForm.patchValue({
        actividad: this.data.informacion.actividad,
        fechaActividad: new Date(this.data.informacion.fechaActividad),
        vigente: this.data.informacion.vigente,
        observaciones: this.data.informacion.observaciones
      });
    }
  }

  guardarSeguimiento() {

    if (this.seguimientoActividadForm.valid) {
      let seguimiento = new SeguimientoActividadOcupacionalDTO();
      seguimiento.actividad = this.seguimientoActividadForm.get('actividad').value;
      seguimiento.fechaActividad = this.seguimientoActividadForm.get('fechaActividad').value;
      seguimiento.vigente = this.seguimientoActividadForm.get('vigente').value;
      seguimiento.observaciones = this.seguimientoActividadForm.get('observaciones').value;
      seguimiento.tokenIdentificadorActividadOcupacional = this.data.actividadOcupacional;
      if (this.data.informacion) {
        seguimiento.tokenIdentificador = this.data.informacion.tokenIdentificador;
        seguimiento.esEdicion = true;
      }

      this.seguimientoActividadForm.disable();
      this.evaluacionConductualService.crearSeguimientoActividadOcupacional(seguimiento)
        .subscribe({
          next: response => {
            // Manejar la respuesta y cerrar el modal si es exitoso
            this.dialogMensajeService.mensajeExitoso(
              response.titulo,
              response.mensaje
            );
            this.dialogRef.close(response);
          },
          error: error => {
            this.evaluacionConductualService.checkError(error);
            this.seguimientoActividadForm.enable();
            console.error(error);
          }
        });
    }

  }

  cerrar() {
    this.dialogRef.close(false);
  }

  ejecutarAccion(): void {
    if (this.seguimientoActividadForm.invalid) return;

    const formValues = this.seguimientoActividadForm.value;
    this.seguimientoActividadForm.disable();

    const actividad: SeguimientoActividadOcupacionalDTO = {
      ...this.data?.actividad,
      subactividad: formValues.subactividad,
      frecuencia: formValues.frecuencia,
      esEdicion: this.data.actividad?.idActividadIntervencion ? true : false,
      tokenActividadOcupacional: this.data.tokenActividadOcupacional
    };

  }

  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.seguimientoActividadForm.get(controlName).setValue(fecha);
    }
  }

}


