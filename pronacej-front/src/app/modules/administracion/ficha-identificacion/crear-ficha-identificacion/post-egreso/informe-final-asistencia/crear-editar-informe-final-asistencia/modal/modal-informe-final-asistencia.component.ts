import { Component, Inject, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { InformeFinalAsistenciaDetalleDTO } from 'app/core/model/both/informeFinalAsistenciaDTO.model';
import { PlanAsistenciaPostEgresoDetalleDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-modal-informe-final-asistencia',
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
  templateUrl: './modal-informe-final-asistencia.component.html',
  styleUrl: './modal-informe-final-asistencia.component.scss',
  encapsulation: ViewEncapsulation.None,  
  
})
export class ModalInformeFinalAsistenciaComponent {

  dimensiones: CatalogoDTO[] = [];  

  detallePlan: PlanAsistenciaPostEgresoDetalleDTO[] = [];
  detalleSeleccionado: PlanAsistenciaPostEgresoDetalleDTO = new PlanAsistenciaPostEgresoDetalleDTO;
  mostrarControlDetalle: boolean = false;
  
  ingresoFormGroup = this.fb.group({
    detalle: [null],
    area: [null],    
    objetivoGeneral: [null],
    objetivoEspecifico: [''],
    actividades: [''],
    descripcionActividad: [''],
    logro: [null],
    dificultad: [null],
  });

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModalInformeFinalAsistenciaComponent>,
    public funcionesUtils: FuncionesUtils,    
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.ingresoFormGroup.controls['objetivoGeneral'].disable();
    this.ingresoFormGroup.controls['objetivoEspecifico'].disable();
    this.ingresoFormGroup.controls['actividades'].disable();
    this.dimensiones = data.dimensiones;
    if (data.fila) {
      this.ingresoFormGroup.patchValue(data.fila);
    } 

    if (data.detallePlan) {
      this.detallePlan = data.detallePlan;
      this.mostrarControlDetalle = true;
    } else {
     this.ingresoFormGroup.controls['area'].disable();
    }
    // else if (data.plan) {
    //   this.ingresoFormGroup.controls['area'].setValue(data.plan.area);
    //   this.ingresoFormGroup.controls['objetivoGeneral'].setValue(data.plan.objetivoGeneral);
    //   this.ingresoFormGroup.controls['objetivoEspecifico'].disable(data.plan.objetivoEspecifico);    
    // }
  }

  seleccionarDetalle(event: any) {
    if (event) {
      this.ingresoFormGroup.controls['area'].setValue(event.value.area);
      this.ingresoFormGroup.controls['objetivoGeneral'].setValue(event.value.objetivoGeneral);
      this.ingresoFormGroup.controls['objetivoEspecifico'].setValue(event.value.objetivoEspecifico);
      this.ingresoFormGroup.controls['actividades'].setValue(event.value.actividades);
    }
  }

  guardar() {
    if (!this.data.fila) {
      this.data.fila = new InformeFinalAsistenciaDetalleDTO;
      this.detalleSeleccionado = this.ingresoFormGroup.controls['detalle'].value;
      this.data.fila.objetivoGeneral = this.detalleSeleccionado.objetivoGeneral;
      this.data.fila.objetivoEspecifico = this.detalleSeleccionado.objetivoEspecifico;
      this.data.fila.actividades = this.detalleSeleccionado.actividades;
    }
    Object.assign(this.data.fila, this.ingresoFormGroup.value);
    this.dialogRef.close(this.data.fila);
  }

}
