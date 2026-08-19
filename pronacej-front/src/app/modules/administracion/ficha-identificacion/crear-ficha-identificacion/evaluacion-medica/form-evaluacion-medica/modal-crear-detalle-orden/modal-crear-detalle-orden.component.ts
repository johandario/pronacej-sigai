import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { EspecialidadProductoDTO, EspecialidadProductoRequest } from 'app/core/model/both/EJE/especialidadProductoDTO.model';
import { OrdenMedicaDetalleDTO } from 'app/core/model/both/EJE/ordenMedicaDTO.model';
import { EspecialidadProductoService } from 'app/core/services/especialidad-producto.service';
import { autocompleteObjectValidator } from 'app/core/utils/CustomValidators.validator';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { debounceTime, distinctUntilChanged, Observable, startWith, switchMap, map } from 'rxjs';

@Component({
  selector: 'app-modal-crear-detalle-orden',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatAutocompleteModule,
    CommonModule
  ],
  templateUrl: './modal-crear-detalle-orden.component.html',
  styleUrl: './modal-crear-detalle-orden.component.scss'
})
export class ModalCrearDetalleOrdenComponent {
  especialidadProductos: EspecialidadProductoDTO[] = [];
  especialidadProductosFiltrado: Observable<EspecialidadProductoDTO[]>;

  ordenMedicaDetalleForm = this.fb.group({
    tokenIdentificador: [null],
    especialidadProducto: [null as EspecialidadProductoDTO, [autocompleteObjectValidator(), Validators.required]],
  });

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModalCrearDetalleOrdenComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public funcionesUtils: FuncionesUtils,
    private especialidadProductoService: EspecialidadProductoService
  ) { }

  async ngOnInit(): Promise<void> {
    await this.cargarData();
    if (this.data.informacion) {
      const info = this.data.informacion as OrdenMedicaDetalleDTO;
      this.ordenMedicaDetalleForm.patchValue({
        tokenIdentificador: info.tokenIdentificador,
        especialidadProducto: info.especialidadProducto,
      });
    }
  }

  async cargarData() {
    this.obtenerEspecialidadProductos();
  }

  obtenerEspecialidadProductos() {
    let request: EspecialidadProductoRequest = {
      valor: '',
    }

    if (this.data.informacion && this.data.informacion.especialidadProducto) {
      request.valor = this.data.informacion.especialidadProducto.nombre;
    }

    this.especialidadProductoService.obtenerEspecialidadProductos(request).subscribe({
      next: (response) => {
        this.especialidadProductos = response.data;   

        const controlEspecialidadProducto = this.ordenMedicaDetalleForm.get('especialidadProducto');

        if (this.data.informacion && this.data.informacion.especialidadProducto) {
            const especialidadProducto = this.especialidadProductos.find(
            c => c.tokenIdentificador === this.data.informacion.especialidadProducto.tokenIdentificador
            );

            controlEspecialidadProducto
            .setValue(especialidadProducto);
        }        
        
        this.especialidadProductosFiltrado = controlEspecialidadProducto.valueChanges.pipe(
            startWith(''),
            debounceTime(300),
            distinctUntilChanged(),
            switchMap((value: string | EspecialidadProductoDTO) => {
            const texto = typeof value === 'string' ? value : value?.producto;

            // request = new ClasificacionEnfermedadRequest();
            request.valor = texto || request.valor;

            return this.especialidadProductoService
                .obtenerEspecialidadProductos(request);
            }),
            map(response => response.data || [])
        );
      },
      error: (error) => console.error('Error cargando especialidad productos:', error)
    });
  }

  registrarDetalle() {
    if (this.ordenMedicaDetalleForm.valid) {
      let detalle = new OrdenMedicaDetalleDTO();
      detalle.tokenIdentificador = this.ordenMedicaDetalleForm.get('tokenIdentificador').value;
      detalle.especialidadProducto = this.ordenMedicaDetalleForm.get('especialidadProducto').value;     
      this.dialogRef.close(detalle);
    }
  }

  displayFnEspecialidadProducto(option: EspecialidadProductoDTO): string {
      return option && option.especialidad && option.producto
      ? `${option.especialidad} - ${option.producto}`
      : '';
  }

  onSeleccionAutocomplete(event: MatAutocompleteSelectedEvent) {
    const especialidadProductoSeleccionado: EspecialidadProductoDTO = event.option.value;
    this.ordenMedicaDetalleForm.get('especialidadProducto').setValue(especialidadProductoSeleccionado);
  }

  cerrar() {
    this.dialogRef.close(false);
  }
}
