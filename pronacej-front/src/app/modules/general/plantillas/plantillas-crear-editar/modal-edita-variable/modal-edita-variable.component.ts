import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';

@Component({
  selector: 'app-modal-edita-variable',
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
  templateUrl: './modal-edita-variable.component.html',
  styleUrl: './modal-edita-variable.component.scss'
})
export class ModalEditaVariableComponent implements OnInit {
  dimensiones: CatalogoDTO[];

  crearVariableFormGroup = this.fb.group({
    nombre: ["", [Validators.required]],
    clave: ["", [Validators.required]],
  })

  constructor(
    private fb: FormBuilder,
    private cd: ChangeDetectorRef,
    private catalogoService: CatalogoService,
    public dialogRef: MatDialogRef<ModalEditaVariableComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    // this.dimensiones = this.data.dimensiones;
    if (this.data.fila) {
      this.crearVariableFormGroup.patchValue(this.data.fila);
      // const dimesionEncontrada = this.dimensiones.filter(dimension => dimension.nemonico == this.data.fila.dimension.nemonico);
      // this.crearVariableFormGroup.controls['dimension'].setValue(dimesionEncontrada[0]);
    }    
  }

  aniadirFila() {
    this.data.fila = this.crearVariableFormGroup.getRawValue();
    this.dialogRef.close(this.data.fila);
  }

}
