import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormField } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { PlanTratamientoIndIntervDTO, PlanTratamientoSeguimientoDTO } from 'app/core/model/both/planTratamientoIndDTO.model';

@Component({
  selector: 'app-modal-edita-registro',
  standalone: true,
  imports: [
    MatFormField,
    ReactiveFormsModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule
  ],
  providers: [
    provideNativeDateAdapter(),
  ],
  templateUrl: './modal-edita-registro.component.html',
  styleUrl: './modal-edita-registro.component.scss'
})
export class ModalEditaRegistroComponent implements OnInit {
  intervDiferenciada: PlanTratamientoIndIntervDTO[]; 

  registroFormGroup = this.fb.group({
    actividad: [null],
    fecha: [new Date()],
    horaInicio: [null],
    horaFin: [null],
    observaciones: [null],
  })

  constructor(
    private fb: FormBuilder,
    private dateAdapter: DateAdapter<any>,
    public dialogRef: MatDialogRef<ModalEditaRegistroComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.dateAdapter.setLocale('es');
  }

  ngOnInit(): void {
    this.intervDiferenciada = this.data.intervDiferenciada;
    if (this.data.fila) {
      this.registroFormGroup.patchValue(this.data.fila);
      const actividadEncontrada = this.intervDiferenciada.find(interv => interv.tokenIdentificador == this.data.fila.actividad.tokenIdentificador);
      if (actividadEncontrada) {
        this.registroFormGroup.controls['actividad'].setValue(actividadEncontrada)           
      }
      this.registroFormGroup.controls['fecha'].setValue(new Date(this.data.fila.fecha));
    }
  }

  aniadirFila() {    ;
    this.dialogRef.close(this.registroFormGroup.getRawValue());
  } 

}
