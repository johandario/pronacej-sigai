import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AlertaService } from '../../services/alerta.service';
import { CommonModule, Location } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { map, Observable, startWith } from 'rxjs';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { AlertaDTO } from 'app/core/model/both/AlertaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MetadataService } from 'app/core/services/metadata.service';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-alertas-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatButtonModule,
    MatSlideToggleModule
  ],
  templateUrl: './alertas-crear-editar.component.html',
  styleUrl: './alertas-crear-editar.component.scss'
})
export class AlertasCrearEditarComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ALERTAS;

  alertaForm: FormGroup;
  esEdicion = false;
  esVisualizacion = false;

  item: AlertaDTO;
  tokenCentro: string;

  listaTablas: String[] = [];
  tablasFiltradas: Observable<String[]>;
  listaCampos: String[] = [];
  tablaControl = new FormControl();

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private alertaService: AlertaService,
    private metadataService: MetadataService,
    private dialogMensajeService: DialogMensajeService) { }

  ngOnInit(): void {

    this.item = history.state.item;
    this.tokenCentro = history.state.tokenCentro;
    this.esVisualizacion = history.state.esVisualizacion;

    this.alertaForm = this.fb.group({
      tabla: [null, Validators.required],
      campo: [{ value: null, disabled: true }, Validators.required],
      prioridad: ['', Validators.required],
      activo: [true, Validators.required],  // Por defecto true
      unidadTiempo: ['', Validators.required],
      tiempo: [null, [Validators.required, Validators.min(1)]],
      ruta: [null],
      mensaje: ['', Validators.required],
      descripcion: ['', Validators.required]
    });

    this.alertaForm.controls.tabla.value

    if (this.item)
      this.esEdicion = true;

    this.cargarTablas();

    // Sincroniza el FormControl del formulario principal con el de autocompletar
    this.tablaControl.valueChanges.subscribe(value => {
      const tablaSeleccionada = value;
      this.alertaForm.patchValue({ tabla: tablaSeleccionada || '' });
      if (value == '')
        this.alertaForm.controls.campo.disable();
    });

    if (this.esEdicion || this.esVisualizacion) {
      this.tablaControl.setValue(this.item.tabla);
      this.obtenerCampos(this.item.tabla);

      this.alertaForm.controls.prioridad.setValue(this.item.prioridad);
      this.alertaForm.controls.activo.setValue(this.item.activo);
      this.alertaForm.controls.unidadTiempo.setValue(this.item.unidadTiempo);
      this.alertaForm.controls.tiempo.setValue(this.item.tiempo);
      this.alertaForm.controls.ruta.setValue(this.item.ruta);
      this.alertaForm.controls.mensaje.setValue(this.item.mensaje);
      this.alertaForm.controls.descripcion.setValue(this.item.descripcion);

      this.alertaForm.controls.campo.setValue(this.item.campo);
    }

    if (this.esVisualizacion) {
      this.tablaControl.disable();
      this.alertaForm.disable();
    }
  }

  guardar() {
    if (this.esEdicion)
      this.actualizarAlerta();
    else
      this.guardarAlerta();
  }

  guardarAlerta() {
    if (this.alertaForm.valid) {
      let alertaDTO: AlertaDTO = {
        descripcion: this.alertaForm.controls.descripcion.value,
        mensaje: this.alertaForm.controls.mensaje.value,
        ruta: this.alertaForm.controls.ruta.value,
        tabla: this.alertaForm.controls.tabla.value,
        campo: this.alertaForm.controls.campo.value,
        prioridad: this.alertaForm.controls.prioridad.value,
        unidadTiempo: this.alertaForm.controls.unidadTiempo.value,
        tiempo: this.alertaForm.controls.tiempo.value,
        activo: this.alertaForm.controls.activo.value,
        tokenCentro: this.tokenCentro,
      };

      // Enviar al backend
      this.alertaService.crearAlerta(alertaDTO, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<Boolean>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al guardar la alerta. ' + response.mensaje
            );
            return;
          }
          else {
            this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje).afterClosed().subscribe(() => {
              this.cancelar();
            });
          }
        },
        error: (error) => {
          this.dialogMensajeService.mensajeError("Ocurrió un error al guardar la alerta. Inténtalo de nuevo.");
        }
      });
    }
  }

  actualizarAlerta() {
    if (this.alertaForm.valid) {
      let alertaDTO: AlertaDTO = {
        descripcion: this.alertaForm.controls.descripcion.value,
        mensaje: this.alertaForm.controls.mensaje.value,
        ruta: this.alertaForm.controls.ruta.value,
        tabla: this.alertaForm.controls.tabla.value,
        campo: this.alertaForm.controls.campo.value,
        prioridad: this.alertaForm.controls.prioridad.value,
        unidadTiempo: this.alertaForm.controls.unidadTiempo.value,
        tiempo: this.alertaForm.controls.tiempo.value,
        activo: this.alertaForm.controls.activo.value,
        tokenIdentificador: this.item.tokenIdentificador
      };

      // Enviar al backend
      this.alertaService.actualizarAlerta(alertaDTO, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<Boolean>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al guardar la alerta. ' + response.mensaje
            );
            return;
          }
          else {
            this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje).afterClosed().subscribe(() => {
              this.cancelar();
            });
          }
        },
        error: (error) => {
          this.dialogMensajeService.mensajeError("Ocurrió un error al guardar la alerta. Inténtalo de nuevo.");
        }
      });
    }
  }

  onTableChange(event: MatSelectChange) {
    this.obtenerCampos(event.value);
  }

  obtenerCampos(tabla: String) {
    this.metadataService.obtenerCampos(tabla).subscribe(
      {
        next: (response: RespuestaPorDefecto<String[]>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al obtener los campos. ' + response.mensaje
            );
            return;
          }

          this.listaCampos = response.data;
          this.alertaForm.controls.campo.enable();

          if (this.esVisualizacion)
            this.alertaForm.controls.campo.disable();
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError("Ocurrió un error al obtener los campos. Inténtalo de nuevo.");
        }
      }
    );
  }

  cargarTablas() {
    this.metadataService.obtenerTablas().subscribe(
      {
        next: (response: RespuestaPorDefecto<String[]>) => {
          console.log(response);
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al obtener las tablas. ' + response.mensaje
            );
            return;
          }

          this.listaTablas = response.data;

          // Inicializar filtro
          this.tablasFiltradas = this.tablaControl.valueChanges.pipe(
            startWith(''),
            //map(value => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
            map(name => (name ? this._filter(name) : this.listaTablas.slice()))
          );

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError("Ocurrió un error al obtener las tablas. Inténtalo de nuevo.");
        }
      }
    );
  }

  cancelar() {
    this.location.back();
  }

  cambioTabla(tablaSeleccionada: String): void {
    // Cuando seleccionas una opción, sincroniza el ID en el formulario principal
    this.alertaForm.patchValue({ tabla: tablaSeleccionada });
    this.obtenerCampos(tablaSeleccionada);
  }

  private _filter(name: string): any[] {
    const filterValue = name.toLowerCase();
    return this.listaTablas.filter(tabla =>
      tabla.toLowerCase().includes(filterValue)
    );
  }

  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select(); // Selecciona todo el texto
  }
}
