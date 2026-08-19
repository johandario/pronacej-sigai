import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DetalleRecetaDTO } from 'app/core/model/both/EJE/detalleRecetaDTO.model';
import { MedicamentoDTO, MedicamentoRequest } from 'app/core/model/both/EJE/medicamentoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MedicamentoService } from 'app/core/services/medicamento.service';
import { autocompleteObjectValidator } from 'app/core/utils/CustomValidators.validator';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { debounceTime, distinctUntilChanged, Observable, startWith, switchMap, map } from 'rxjs';

@Component({
  selector: 'app-modal-crear-detalle-receta',
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
  templateUrl: './modal-crear-detalle-receta.component.html',
  styleUrl: './modal-crear-detalle-receta.component.scss'
})
export class ModalCrearDetalleRecetaComponent {
  medicamentos: MedicamentoDTO[] = [];
  medicamentosFiltrado: Observable<MedicamentoDTO[]>;

  listaFormasFarmaceuticas: CatalogoDTO[] = [];

  detalleRecetaForm = this.fb.group({
    tokenIdentificador: [null],
    medicamento: [''],
    medicamentoCompleto: [null as MedicamentoDTO, [autocompleteObjectValidator(), Validators.required]],
    dosis: [null],
    frecuencia: [null],
    indicaciones: [null],
    concentracion: [null],
    formaFarmaceutica: [null] // Aquí guardamos el tokenIdentificador del catálogo seleccionado
  });

  constructor(
    private fb: FormBuilder,
    private cd: ChangeDetectorRef,
    private catalogoService: CatalogoService,
    public dialogRef: MatDialogRef<ModalCrearDetalleRecetaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils,
    private medicamentoService: MedicamentoService
  ) { }

  async ngOnInit(): Promise<void> {
    await this.cargarCatalogos();
    if (this.data.informacion) {
      const info = this.data.informacion as DetalleRecetaDTO;
      this.detalleRecetaForm.patchValue({
        tokenIdentificador: info.tokenIdentificador,
        medicamento: info.medicamento,
        dosis: info.dosis,
        frecuencia: info.frecuencia,
        indicaciones: info.indicaciones,
        concentracion: info.concentracion,
        formaFarmaceutica: info.formaFarmaceutica?.tokenIdentificador
      });
    }
  }

  async cargarCatalogos() {
    // Suponiendo que FORMA_FARMACEUTICA es el nemónico del catálogo padre.
    this.funcionesUtils.obtenerListaCatalogo('FORMA_FARMACEUTICA', '').subscribe({
      next: (data) => (this.listaFormasFarmaceuticas = data),
      error: (error) => console.error('Error cargando formas farmacéuticas:', error),
    });

    this.obtenerMedicamentos();
  }

  obtenerMedicamentos() {
    let request: MedicamentoRequest = {
      valor: '',
    }

    if (this.data.informacion && this.data.informacion.medicamentoCompleto) {
      request.valor = this.data.informacion.medicamentoCompleto.nombre;
    }

    this.medicamentoService.obtenerMedicamentos(request).subscribe({
      next: (response) => {
        this.medicamentos = response.data;   

        const controlMedicamentoCompleto = this.detalleRecetaForm.get('medicamentoCompleto');

        if (this.data.informacion && this.data.informacion.medicamentoCompleto) {
            const medicamentoCompleto = this.medicamentos.find(
            c => c.tokenIdentificador === this.data.informacion.medicamentoCompleto.tokenIdentificador
            );

            controlMedicamentoCompleto
            .setValue(medicamentoCompleto);
        }        
        
        this.medicamentosFiltrado = controlMedicamentoCompleto.valueChanges.pipe(
            startWith(''),
            debounceTime(300),
            distinctUntilChanged(),
            switchMap((value: string | MedicamentoDTO) => {
            const texto = typeof value === 'string' ? value : value?.nombre;

            // request = new ClasificacionEnfermedadRequest();
            request.valor = texto || request.valor;

            return this.medicamentoService
                .obtenerMedicamentos(request);
            }),
            map(response => response.data || [])
        );
      },
      error: (error) => console.error('Error cargando medicamentos:', error)
    });
  }

  registrarDetalle() {
    if (this.detalleRecetaForm.valid) {
      let detalle = new DetalleRecetaDTO();
      detalle.tokenIdentificador = this.detalleRecetaForm.get('tokenIdentificador').value;
      detalle.medicamento = this.detalleRecetaForm.get('medicamento').value;
      detalle.dosis = this.detalleRecetaForm.get('dosis').value;
      detalle.frecuencia = this.detalleRecetaForm.get('frecuencia').value;
      detalle.indicaciones = this.detalleRecetaForm.get('indicaciones').value;
      detalle.concentracion = this.detalleRecetaForm.get('concentracion').value;
      detalle.medicamentoCompleto = this.detalleRecetaForm.get('medicamentoCompleto').value;

      const formaToken = this.detalleRecetaForm.get('formaFarmaceutica').value;
      if (formaToken) {
        const formaSeleccionada = this.listaFormasFarmaceuticas.find(x => x.tokenIdentificador === formaToken);
        detalle.formaFarmaceutica = formaSeleccionada;
      }

      this.dialogRef.close(detalle);
    }
  }

  displayFnMedicamento(option: MedicamentoDTO): string {
      return option && option.nombre && option.presentacion
      ? `${option.nombre} | ${option.presentacion}`
      : '';
  }

  onSeleccionAutocomplete(event: MatAutocompleteSelectedEvent) {
    const medicamentoSeleccionado: MedicamentoDTO = event.option.value;
    this.detalleRecetaForm.get('concentracion').setValue(medicamentoSeleccionado.concentracion);
  }

  cerrar() {
    this.dialogRef.close(false);
  }
}
