import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { InstrumentoEvaluacionDTO } from 'app/core/model/both/instrumentoEvaluacionDTO.model';

@Component({
  selector: 'app-md-regi-info',
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
  ],
  templateUrl: './md-regi-info.component.html',
  styleUrls: ['./md-regi-info.component.scss']
})
export class MdRegiInfoComponent implements OnInit {
  instrumentoEvaluacionDTOEditar: InstrumentoEvaluacionDTO;
  listaTiposInstrumento: CatalogoDTO[] = [];

  agregarRegistroForm = this.formBuilder.group({
    tipoInstrumento: ["0", [Validators.required]],
  });

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<MdRegiInfoComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { }

  ngOnInit(): void {
    this.listaTiposInstrumento = this.datos.listaTiposInstrumento;

    if (this.datos?.fila) {
      this.agregarRegistroForm.get('tipoInstrumento')?.setValue(this.datos.fila.tokenIdentificadorTipoInstrumento);
    }
  }

  agregarRegistro() {
    if (this.agregarRegistroForm.invalid) {
      return;
    }

    const formValues = this.agregarRegistroForm.value;
    let instrumento = new InstrumentoEvaluacionDTO();
    
    if (this.datos?.fila) {
      instrumento.tokenIdentificador = this.datos.fila.tokenIdentificador;
      instrumento.tokenIdentificadorInformeSeguimiento = this.datos.fila.tokenIdentificadorInformeSeguimiento;
    } else {
      instrumento.tokenIdentificador = "0";
    }
    
    instrumento.tokenIdentificadorTipoInstrumento = formValues.tipoInstrumento;

    this.dialogRef.close(instrumento);
  }
}