import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { ActividadIntervencionSeguimientoDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { CommonModule } from '@angular/common';
import { DateAdapter, provideNativeDateAdapter } from '@angular/material/core';


@Component({
  selector: 'app-modal-crear-ficha-seg-ab-pti',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatDatepickerModule,
    CommonModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './modal-crear-ficha-seg-ab-pti.component.html',
  styleUrl: './modal-crear-ficha-seg-ab-pti.component.scss'
})
export class ModalCrearFichaSegAbPtiComponent {
  fila: ActividadIntervencionSeguimientoDTO = new ActividadIntervencionSeguimientoDTO;

  registroFormGroup = this.fb.group({
    fecha: [null as Date, Validators.required],
    hora: [null as string, Validators.required],
    descripcion: [null as string, Validators.required],
  });

  constructor(
    private dateAdapter: DateAdapter<any>,
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModalCrearFichaSegAbPtiComponent>,
    private dialogMensajeService: DialogMensajeService,
    @Inject(MAT_DIALOG_DATA) public data: { fila?: ActividadIntervencionSeguimientoDTO, visualizar?: boolean },
  ) {
    this.dateAdapter.setLocale('es');    
  }

  ngOnInit(): void {
    if (this.data.visualizar) {
      this.registroFormGroup.disable();
    } 

    if (this.data.fila) {
      this.registroFormGroup.patchValue(this.data.fila);
      this.registroFormGroup.controls['fecha'].setValue(new Date(this.data.fila.fecha));
    } else {
      this.fila = this.registroFormGroup.value;
    }
  }

  agregar() {
    if (this.data.fila) {
      this.fila = this.data.fila;      
    }
    Object.assign(this.fila, this.registroFormGroup.value); 
    this.dialogRef.close(this.fila);    
  }
}
