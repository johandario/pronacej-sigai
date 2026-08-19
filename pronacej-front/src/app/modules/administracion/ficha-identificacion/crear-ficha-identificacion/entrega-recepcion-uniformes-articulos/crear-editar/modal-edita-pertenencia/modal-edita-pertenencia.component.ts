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
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-modal-edita-pertenencia',
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
  templateUrl: './modal-edita-pertenencia.component.html',
  styleUrl: './modal-edita-pertenencia.component.scss'
})
export class ModalEditaPertenenciaComponent {
  tipos: CatalogoDTO[] = [];
  estados: CatalogoDTO[] = [];

  detallePertenenciaFormGroup = this.fb.group({
    tipo: [null as CatalogoDTO, Validators.required],
    nombre: [null, Validators.required],
    cantidad: [null, [Validators.required, Validators.max(999999), Validators.min(0)]],    
    estado: [null as CatalogoDTO, Validators.required],   
    observacion: [null]
  });

  constructor(
    private fb: FormBuilder,
    private catalogoService: CatalogoService,
    public dialogRef: MatDialogRef<ModalEditaPertenenciaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    const tiposRequest = this.catalogoService.obtenerHijos('TIPOS_PERTENENCIAS','');
    const estadosRequest = this.catalogoService.obtenerHijos('ESTADOS_PERTENENCIAS','');

    forkJoin([tiposRequest, estadosRequest]).subscribe(([tiposResponse, estadosResponse]) => {
      this.tipos = tiposResponse.data;
      this.estados = estadosResponse.data;
      if (this.data) {
        this.detallePertenenciaFormGroup.patchValue(this.data);
        const tipoEncontrado = this.tipos.find(tipo => tipo.nemonico == this.data.tipo.nemonico);
        this.detallePertenenciaFormGroup.controls['tipo'].setValue(tipoEncontrado);
        
        const estadoEntonctrado = this.estados.find(estado => estado.nemonico == this.data.estado.nemonico);
        this.detallePertenenciaFormGroup.controls['estado'].setValue(estadoEntonctrado);
      }
    });

    // this.catalogoService.obtenerHijos('TIPOS_PERTENENCIAS', '').subscribe(response => {
    //   this.tipos = response.data;
    //   if (this.data) {
    //     this.detallePertenenciaFormGroup.patchValue(this.data);
    //     const tipoEncontrado = this.tipos.filter(tipo => tipo.nemonico == this.data.tipo.nemonico);
    //     this.detallePertenenciaFormGroup.controls['tipo'].setValue(tipoEncontrado[0]);
    //     this.catalogoService.obtenerHijos(this.data.tipo.nemonico, '').subscribe(response => {
    //       this.nombres = response.data;
    //       const nombreEncontrado = this.nombres.filter(nombre => nombre.nemonico == this.data.nombre.nemonico);
    //       this.detallePertenenciaFormGroup.controls['nombre'].setValue(nombreEncontrado[0]);
    //     });
    //   }
    // });
    
  }

  // obtenerNombres(event: any) {
  //   this.detallePertenenciaFormGroup.controls['nombre'].reset();
  //   this.catalogoService.obtenerHijos(event.value.nemonico, '').subscribe(response => {
  //     this.nombres = response.data;
  //   });
  // }

  aniadirFila() {
    this.data = this.detallePertenenciaFormGroup.getRawValue();
    this.dialogRef.close(this.data);
  }

  prevenirInputNumberInvalido(event: KeyboardEvent): void {
    const invalidKeys = ['+', '-', 'e', 'E'];
    if (invalidKeys.includes(event.key)) {
      event.preventDefault();
    }
  }
}
