import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-modal-edita-interv',
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
  templateUrl: './modal-edita-interv.component.html',
  styleUrl: './modal-edita-interv.component.scss',
  encapsulation: ViewEncapsulation.None,  
})
export class ModalEditaIntervComponent implements OnInit {
  dimensiones: CatalogoDTO[];

  ingresoPlanTratamientoFormGroup = this.fb.group({
    dimension: [null],
    objetivo: [null],
    actividadPrograma: [null],
    equipoResponsable: [null],
    tiempoEstimado: [null,  [Validators.required, Validators.max(9999), Validators.min(0)]],
    numAtencionIndividual: [null],
    numAtencionGrupal: [null]
  })

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_PTI;

  constructor(
    private fb: FormBuilder,
    private cd: ChangeDetectorRef,
    private catalogoService: CatalogoService,
    public dialogRef: MatDialogRef<ModalEditaIntervComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.dimensiones = this.data.dimensiones;
    if (this.data.fila) {
      this.ingresoPlanTratamientoFormGroup.patchValue(this.data.fila);
      const dimesionEncontrada = this.dimensiones.filter(dimension => dimension.nemonico == this.data.fila.dimension.nemonico);
      this.ingresoPlanTratamientoFormGroup.controls['dimension'].setValue(dimesionEncontrada[0]);
    }    
    if (this.data.visualizar) {
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
