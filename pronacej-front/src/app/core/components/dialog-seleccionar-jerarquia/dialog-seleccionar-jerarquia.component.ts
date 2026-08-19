import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';

@Component({
  selector: 'app-dialog-seleccionar-jerarquia',
  standalone: true,
  imports: [CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButton,
    MatDialogModule],
  templateUrl: './dialog-seleccionar-jerarquia.component.html',
  styleUrl: './dialog-seleccionar-jerarquia.component.scss'
})
export class DialogSeleccionarJerarquiaComponent {

  seleccionada: JerarquiaDTO;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: JerarquiaDTO[],
    private dialogRef: MatDialogRef<DialogSeleccionarJerarquiaComponent>
  ) { 
    console.log('selecionar jerarquia',data);
    this.data.sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  aceptar(): void {
    this.dialogRef.close(this.seleccionada);
  }

  cerrar(): void {
    this.dialogRef.close();
  }
}
