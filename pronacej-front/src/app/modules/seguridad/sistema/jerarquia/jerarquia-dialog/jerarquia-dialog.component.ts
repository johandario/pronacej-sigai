import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule, AbstractControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-jerarquia-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatLabel,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    ReactiveFormsModule],
  templateUrl: './jerarquia-dialog.component.html',
  styleUrl: './jerarquia-dialog.component.scss'
})
export class JerarquiaDialogComponent {

  form: FormGroup;
  type: Number;
  jerarquias: JerarquiaDTO[];
  jerarquia: JerarquiaDTO;
  nodeId: Number;
  title: string;
  selectDisabled: boolean;
  listaTipoGenero: CatalogoDTO[] = [];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<JerarquiaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public funcionesUtils: FuncionesUtils,
    private dialogMensajeService: DialogMensajeService
  ) {
    this.cargarCatalogos();

    this.form = this.fb.group({
      idJerarquiaPadre: ['', Validators.required],
      nombre: ['', [Validators.required, Validators.maxLength(100), this.noSpecialCharsValidator]],
      empresa: ['', [Validators.required, this.nonNegativeValidator]],
      direccion: ['', []],
      genero: [null, []],
    });

    this.type = data.type;
    this.jerarquias = data.list;
    this.nodeId = data.id;
    this.selectDisabled = false;

    switch (this.type) {
      //Agregar jerarquia
      case 0:
        this.title = 'Agregar Jerarquía';
        break;

      //Agregar hijo
      case 1:
        this.title = 'Agregar Hijo';
        this.selectDisabled = true;
        this.toggleControls();
        this.form.controls.idJerarquiaPadre.setValue(this.nodeId);
        this.jerarquia = this.jerarquias.find(x => x.id == this.nodeId);
        this.form.controls.empresa.setValue(this.jerarquia.empresa);
        // this.form.controls.direccion.setValue(this.jerarquia.direccion);
        // this.form.controls.genero.setValue(this.jerarquia.tokenIdentificadorGenero);
        break;

      //Actualizar nodo
      case 2:
        this.title = 'Actualizar Jerarquía';
        this.jerarquia = this.jerarquias.find(x => x.id == this.nodeId);
        this.form.controls.idJerarquiaPadre.setValue(this.jerarquia.idJerarquiaPadre);
        this.form.controls.nombre.setValue(this.jerarquia.nombre);
        this.form.controls.empresa.setValue(this.jerarquia.empresa);
        this.form.controls.direccion.setValue(this.jerarquia.direccion);
        this.form.controls.genero.setValue(this.jerarquia.tokenIdentificadorGenero);
        break;

    }
  }

  
  async cargarCatalogos(): Promise<void> {
    let load = this.dialogMensajeService.mensajeLoading("Cargando data...");
    const catalogos$ = forkJoin({      
      tipoGenero: this.funcionesUtils.obtenerListaCatalogo('TIPO_SEXO', etiquetasModel.NEMONICO_MENU_JERARQUIA),      
    });

    catalogos$.subscribe({
      next: (result) => {
        this.listaTipoGenero = result.tipoGenero;
        load.close();
      },
      error: (err) => {
        console.error('Error al cargar los catálogos:', err);
        load.close();
      }
    });
  }

  noSpecialCharsValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const value = control.value || '';

    // Permitir letras, números y espacios, pero no solo espacios
    const hasSpecialChars = /[^a-zA-Z0-9\sñÑáéíóúÁÉÍÓÚüÜ]/.test(value);  // Verifica caracteres especiales
    const onlySpaces = value.trim().length === 0;          // Verifica si solo tiene espacios

    if (hasSpecialChars) {
      return { 'specialChars': true };  // Si tiene caracteres especiales, retorna error
    }

    if (onlySpaces) {
      return { 'onlySpaces': true };    // Si son solo espacios, retorna error
    }

    return null; // Si es válido, retorna null
  }

  nonNegativeValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const isNegative = control.value < 0;
    return isNegative ? { 'negativeValue': true } : null;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.dialogRef.close(this.form.getRawValue());

      console.log(this.form.getRawValue());
    }
  }

  toggleControls(): void {
    if (this.selectDisabled) {
      this.form.controls['idJerarquiaPadre'].disable();
      this.form.controls['empresa'].disable();
    } else {
      this.form.controls['nombre'].enable();
      this.form.controls['empresa'].enable();
    }
  }
}
