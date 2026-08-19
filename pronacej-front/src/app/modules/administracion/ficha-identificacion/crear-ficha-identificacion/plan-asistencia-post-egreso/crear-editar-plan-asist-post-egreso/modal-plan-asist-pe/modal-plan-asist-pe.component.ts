import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PlanAsistenciaPostEgresoDetalleDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-modal-plan-asist-pe',
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
      MatSelectModule 
    ],
  templateUrl: './modal-plan-asist-pe.component.html',
  styleUrl: './modal-plan-asist-pe.component.scss'
})
export class ModalPlanAsistPeComponent {  

  dimensiones: CatalogoDTO[] = [];

  ingresoFormGroup = this.fb.group({
    area: [null],
    factores: [null],
    objetivoGeneral: [null],
    objetivoEspecifico: [null],
    actividades: [null],
    institucion: [null],
    frecuencia: [null],
    indicador: [null],
  });

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModalPlanAsistPeComponent>,
    public funcionesUtils: FuncionesUtils,
    @Inject(MAT_DIALOG_DATA) public data: {fila: PlanAsistenciaPostEgresoDetalleDTO, dimensiones?: CatalogoDTO[] }
  ) {
    this.dimensiones = data.dimensiones;
    if (data.fila) {
      this.ingresoFormGroup.patchValue(data.fila);
    } 
  }

  guardar() {
    if (!this.data.fila) {
      this.data.fila = new PlanAsistenciaPostEgresoDetalleDTO;
    }
    Object.assign(this.data.fila, this.ingresoFormGroup.value);
    this.dialogRef.close(this.data.fila);
  }

}
