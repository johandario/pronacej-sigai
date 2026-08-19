import { ChangeDetectorRef, Component, Inject, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { ModalEditaIntervComponent } from '../../crear-editar-pti-cerrado/modal-edita-interv/modal-edita-interv.component';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';


@Component({
  selector: 'app-modal-edita-interv-abierto',
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
  templateUrl: './modal-edita-interv-abierto.component.html',
  styleUrl: './modal-edita-interv-abierto.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class ModalEditaIntervAbiertoComponent {
  dimensiones: CatalogoDTO[];
  frecuencias: CatalogoDTO[];
  modalidades: CatalogoDTO[];

  tipo: string = '';

  ingresoPlanTratamientoFormGroup = this.fb.group({
    dimension: [null as CatalogoDTO],
    objetivo: [null],
    actividadPrograma: [null],
    equipoResponsable: [null],
    tiempoEstimado: [null],
    numAtencionIndividual: [null],
    numAtencionGrupal: [null],
    lugar: [null],
    modalidad: [null as CatalogoDTO],
    frecuencia: [null as CatalogoDTO],
    descripcion: [null],        
  })
  
  constructor(
    private fb: FormBuilder,
    public funcionesUtils: FuncionesUtils,
    public dialogRef: MatDialogRef<ModalEditaIntervComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { 
    this.dimensiones = data.dimensiones;
    this.frecuencias = data.frecuencias;
    this.modalidades = data.modalidades;
    this.tipo = data.tipo;
  }

  ngOnInit(): void {    
    if (this.data.tipo == 'matriz-pti' || this.data.tipo == 'control-asistencia' && this.data.fila) {
      // this.ingresoPlanTratamientoFormGroup.controls['dimension'].disable();
    } 
    if (this.data.fila) {
      this.ingresoPlanTratamientoFormGroup.patchValue(this.data.fila);
    }   
    if (this.data.visualizar)  {
      this.ingresoPlanTratamientoFormGroup.disable();
    }
  }

  aniadirFila() {
    this.data.fila = this.ingresoPlanTratamientoFormGroup.getRawValue();
    this.dialogRef.close(this.data.fila);
  }

  prevenirInputNumberInvalido(event: KeyboardEvent): void {
    const invalidKeys = ['+', '-', 'e', 'E'];
    if (invalidKeys.includes(event.key)) {
      event.preventDefault();
    }
  }
}
