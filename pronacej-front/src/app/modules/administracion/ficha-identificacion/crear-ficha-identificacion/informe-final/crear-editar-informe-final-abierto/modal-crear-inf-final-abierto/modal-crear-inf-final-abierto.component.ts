import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { InformeFinalAbiertoMedidasDTO } from 'app/core/model/both/informeFinalAbiertoDTO.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-modal-crear-inf-final-abierto',
  standalone: true,
  imports: [
    CommonModule,
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
    MatSelectModule
  ],
  templateUrl: './modal-crear-inf-final-abierto.component.html',
  styleUrl: './modal-crear-inf-final-abierto.component.scss'
})
export class ModalCrearInfFinalAbiertoComponent {
  dimensiones: CatalogoDTO[] = [];
  esVisualizacion: boolean = false;

  ingresoFormGroup = this.fb.group({
    medidaAccesoria: [null, Validators.required],
    accion: [null, Validators.required],
    objetivo: [null, Validators.required],
    analisisCualitativo: [null, Validators.required],
  });

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModalCrearInfFinalAbiertoComponent>,
    public funcionesUtils: FuncionesUtils,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    if (data.fila) {
      this.ingresoFormGroup.patchValue(data.fila);
    }

    if (data.esVisualizacion)
      this.esVisualizacion = data.esVisualizacion;

    if (this.esVisualizacion)
      this.ingresoFormGroup.disable();
  }

  guardar() {
    if (!this.data.fila) {
      this.data.fila = new InformeFinalAbiertoMedidasDTO;
    }
    Object.assign(this.data.fila, this.ingresoFormGroup.value);
    this.dialogRef.close(this.data.fila);
  }
}
