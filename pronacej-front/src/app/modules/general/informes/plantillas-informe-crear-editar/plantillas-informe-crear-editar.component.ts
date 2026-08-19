import { CommonModule, Location } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { CampoInformeDTO } from 'app/core/model/both/informe/campoInformeDTO.model';
import { PlantillaInformeDTO } from 'app/core/model/both/informe/plantillaInformeDTO.model';
import { InformeService } from '../../services/informe.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-plantillas-informe-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatTableModule,
    MatLabel,
    MatExpansionModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule
  ],
  templateUrl: './plantillas-informe-crear-editar.component.html',
  styleUrl: './plantillas-informe-crear-editar.component.scss'
})
export class PlantillasInformeCrearEditarComponent {
  esEdicion: boolean = false;
  item: PlantillaInformeDTO;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PLANTILLAS_INFORMES;
  titulo: String = "Informe";

  plantillaForm: FormGroup;
  displayedColumns: string[] = ['etiqueta', 'tipo', 'acciones'];
  dataSource: MatTableDataSource<any>;

  listaTiposCampo: CatalogoDTO[];
  listaTiposCentro: CatalogoDTO[];

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private informeService: InformeService,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils
  ) { }

  ngOnInit() {
    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.TIPO_CAMPO, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaTiposCampo = data;
      },
      error: (error) => console.error('Error cargando tipos de campo:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.TIPO_CENTRO, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaTiposCentro = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });

    this.item = history.state.item;

    if (this.item)
      this.esEdicion = true;

    if (this.esEdicion)
      this.populateFromDTO(this.item)
    else {
      this.plantillaForm = this.fb.group({
        nombre: ['', Validators.required],
        descripcion: [''],
        tipoCentro: ['', Validators.required],
        campos: this.fb.array([
          this.nuevoCampo()
        ])
      });

      this.dataSource = new MatTableDataSource(this.listaCampos.controls);
    }
  }

  guardar() {
    if (this.esEdicion)
      this.editarPlantilla();
    else
      this.crearPlantilla();
  }

  crearPlantilla() {

    let plantillaDTO = new PlantillaInformeDTO();

    plantillaDTO = this.transformToDTO();

    this.informeService.crearPlantilla(plantillaDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          'Plantilla guardada correctamente.'
        ).afterClosed().subscribe(() => {
          this.plantillaForm.reset();
          this.location.back();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar la plantilla. Inténtalo de nuevo.'
        );
      }
    });
  }

  editarPlantilla() {
    let plantillaDTO = new PlantillaInformeDTO();

    plantillaDTO = this.transformToDTO();

    plantillaDTO.tokenIdentificador = this.item.tokenIdentificador;

    this.informeService.actualizarPlantilla(plantillaDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Editar',
          'Plantilla actualizada correctamente.'
        ).afterClosed().subscribe(() => {
          this.plantillaForm.reset();
          this.location.back();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar la plantilla. Inténtalo de nuevo.'
        );
      }
    });
  }

  cancelar() {
    this.plantillaForm.reset();

    this.location.back();
  }

  nuevoCampo() {
    let newCampoFormGroup = this.fb.group({
      etiqueta: ['', Validators.required],
      tipo: ['', Validators.required]
    });

    return newCampoFormGroup;
  }

  agregarCampo() {
    this.listaCampos.push(this.nuevoCampo());
    this.dataSource.data = this.listaCampos.controls;
  }

  eliminarCampo(index: number) {
    this.listaCampos.removeAt(index);
    this.dataSource.data = this.listaCampos.controls;
  }

  get listaCamposArray() {
    return this.plantillaForm.get('campos')['controls'];
  }

  get listaCampos() {
    return this.plantillaForm.get('campos') as FormArray;
  }

  cargarCampos(idPlantilla: number): Promise<CampoInformeDTO[]> {
    let plantillaDTO = new PlantillaInformeDTO();
    plantillaDTO.idPlantillaInforme = idPlantilla;

    return new Promise((resolve, reject) => {
      this.informeService.obtenerCamposPorPlantilla(plantillaDTO, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<CampoInformeDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            reject('Error al obtener los campos');
          }

          resolve(response.data);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          reject(error);
        }
      });
    });
  }


  transformToDTO(): PlantillaInformeDTO {
    const formValue = this.plantillaForm.value;

    return {
      nombre: formValue.nombre,
      descripcion: formValue.descripcion,
      nemonicoCentro: formValue.tipoCentro,
      campos: formValue.campos.map((campo: CampoInformeDTO) => ({
        etiqueta: campo.etiqueta,
        tipo: campo.tipo
      }))
    };
  }

  async populateFromDTO(plantillaInformeDTO: PlantillaInformeDTO) {
    // Asigna valores a los controles principales
    this.plantillaForm = this.fb.group({
      nombre: plantillaInformeDTO.nombre,
      descripcion: plantillaInformeDTO.descripcion,
      tipoCentro: plantillaInformeDTO.nemonicoCentro,
      campos: this.fb.array([]) // Inicializa el FormArray vacío
    });

    // Espera a que los campos se carguen antes de continuar
    const campos = await this.cargarCampos(plantillaInformeDTO.idPlantillaInforme);

    // Agrega cada campo en el FormArray 'campos'
    campos.forEach((campo) => {
      const campoFormGroup = this.fb.group({
        etiqueta: [campo.etiqueta],
        tipo: [campo.tipo]
      });
      (this.plantillaForm.get('campos') as FormArray).push(campoFormGroup);
    });

    this.dataSource = new MatTableDataSource(this.listaCampos.controls);
  }
}
