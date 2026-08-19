import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { CondHistViolDTO } from 'app/core/model/both/condHistViolDTO.model';

@Component({
  selector: 'app-md-regi-cond',
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
    MatSelectModule,
    MatRadioModule,
  ],
  templateUrl: './md-regi-cond.component.html',
  styleUrl: './md-regi-cond.component.scss'
})
export class MdRegiCondComponent {

  agregarRegistroForm = this.formBuilder.group({
    criterio: [null, ],
    comentario: [null, ],
  });

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<MdRegiCondComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { }

  agregarRegistro() {
    const formValues = this.agregarRegistroForm.value;
    let condHistViol = new CondHistViolDTO();
    condHistViol.tokenIdentificador = "0";
    condHistViol.criterio = formValues.criterio;
    condHistViol.comentario = formValues.comentario;

    this.dialogRef.close(condHistViol);
  }

}
