import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EvaluacionSocialArtefactoDTO } from 'app/core/model/both/EvaluacionSocialArtefactoDTO.model';

@Component({
  selector: 'app-md-regi-arte',
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
  templateUrl: './md-regi-arte.component.html',
  styleUrl: './md-regi-arte.component.scss'
})
export class MdRegiArteComponent {
  // Variables
  artefactoParaEditar: EvaluacionSocialArtefactoDTO;
  listaArtefactosVivienda: CatalogoDTO[] = [];

  // Formulario
  formularioRegistroArtefacto = this.constructorFormulario.group({
    artefactosVivienda: ['0', [Validators.required, Validators.pattern(/^(?!0$).*$/)]],
    cantidad: [0, [Validators.required, Validators.min(0), Validators.max(999)]]
  });

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiArteComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { }

  ngOnInit(): void {
    this.listaArtefactosVivienda = this.datos.listaArtefactosVivienda;

    if (this.datos?.fila) {
      this.cargarDatosFormulario(this.datos.fila);
    }
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   * @returns Validador personalizado
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const esInvalido = control?.value && control?.value?.trim().length === 0;
      return esInvalido ? { 'soloEspacios': true } : null;
    };
  }

  /**
   * Carga los datos del artefacto en el formulario para su edición
   * @param artefacto El artefacto a editar
   */
  cargarDatosFormulario(artefacto: EvaluacionSocialArtefactoDTO): void {
    this.formularioRegistroArtefacto.get('artefactosVivienda')?.setValue(artefacto.tokenIdentificadorArtefactosVivienda);
    this.formularioRegistroArtefacto.get('cantidad')?.setValue(artefacto.cantidad);
  }

  /**
   * Registra o actualiza un artefacto
   */
  guardarArtefacto(): void {
    const valoresFormulario = this.formularioRegistroArtefacto.value;
    let artefactoRegistro = new EvaluacionSocialArtefactoDTO();
    
    // Si estamos editando, conservamos el identificador
    if (this.datos?.fila) {
      artefactoRegistro.tokenIdentificador = this.datos.fila.tokenIdentificador;
      artefactoRegistro.tokenIdentificadorEvaluacionSocial = this.datos.fila.tokenIdentificadorEvaluacionSocial;
    } else {
      artefactoRegistro.tokenIdentificador = "0";
      artefactoRegistro.tokenIdentificadorEvaluacionSocial = "0";
    }
    
    // Asignamos los valores del formulario
    artefactoRegistro.tokenIdentificadorArtefactosVivienda = valoresFormulario.artefactosVivienda;
    artefactoRegistro.cantidad = valoresFormulario.cantidad;

    // Cerramos el diálogo y devolvemos el artefacto
    this.referenciaDialogo.close(artefactoRegistro);
  }
}