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
  selector: 'app-md-regi-situ',
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
  templateUrl: './md-regi-situ.component.html',
  styleUrl: './md-regi-situ.component.scss'
})
export class MdRegiSituComponent {

  agregarRegistroForm = this.formBuilder.group({
    criterio: [null, ],
    comentario: [null, ],
  });

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<MdRegiSituComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { }

  agregarRegistro() {
    const formValues = this.agregarRegistroForm.value;
    let situPersCaraPers = new CondHistViolDTO();
    situPersCaraPers.tokenIdentificador = "0";
    situPersCaraPers.criterio = formValues.criterio;
    situPersCaraPers.comentario = formValues.comentario;

    this.dialogRef.close(situPersCaraPers);
  }

}
