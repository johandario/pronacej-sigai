import { CommonModule, Location } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { SeguimientoPsicologicoDTO } from 'app/core/model/both/ia/seguimientoPsicologicoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SeguimientoService } from 'app/modules/administracion/services/seguimiento.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { catchError, map, Observable, of } from 'rxjs';

@Component({
  selector: 'app-seguimiento-psicologico-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatLabel,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './seguimiento-psicologico-crear-editar.component.html',
  styleUrl: './seguimiento-psicologico-crear-editar.component.scss'
})
export class SeguimientoPsicologicoCrearEditarComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOLOGICA;

  esEdicion: boolean = false;
  esVisualizacion: boolean = false;
  tokenEncabezado: string;
  item: SeguimientoPsicologicoDTO;
  centro: JerarquiaDTO;

  seguimientoForm: FormGroup;

  listaProgramas: JerarquiaDTO[] = [];
  listaAmbientes: JerarquiaDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private seguimientoService: SeguimientoService,
    private dialogMensajeService: DialogMensajeService,
    private jerarquiaService: JerarquiaService,
    private location: Location,
  ) { }

  ngOnInit(): void {
    this.tokenEncabezado = history.state.tokenEncabezado;
    this.centro = history.state.centro;
    this.esVisualizacion = history.state.esVisualizacion;

    this.seguimientoForm = this.fb.group({
      programa: [JerarquiaDTO],
      ambiente: [JerarquiaDTO],
      intervencionConcejeria: ['', [Validators.required]],
      accionesRealizar: ['', [Validators.required]],
      comentariosObservaciones: [''],
    });

    this.item = history.state.item;

    if (this.item) {
      this.esEdicion = true;
      this.seguimientoForm.controls.intervencionConcejeria.setValue(this.item.intervencionConcejeria);
      this.seguimientoForm.controls.accionesRealizar.setValue(this.item.accionesRealizar);
      this.seguimientoForm.controls.comentariosObservaciones.setValue(this.item.comentariosObservaciones);
    }

    if (this.centro.nombre.includes('CJDR')) {
      this.seguimientoForm.controls.programa.setValidators(Validators.required);
      this.seguimientoForm.controls.ambiente.setValidators(Validators.required);

      this.cargarJerarquias(this.centro.tokenIdentificador).subscribe(data => {
        this.listaProgramas = data;

        if (this.esEdicion)
          this.seguimientoForm.controls.programa.setValue(this.item.programa);

        if (this.esVisualizacion)
          this.cargarAmbientes(this.item.programa);
      });
    }

    if (this.esVisualizacion)
      this.seguimientoForm.disable();
  }

  cargarAmbientes(programa: JerarquiaDTO) {
    this.cargarJerarquias(programa.tokenIdentificador).subscribe(data => {
      this.listaAmbientes = data;

      if (this.esEdicion)
        this.seguimientoForm.controls.ambiente.setValue(this.item.ambiente);
    });
  }

  guardar() {
    if (this.esEdicion)
      this.editarSeguimiento();
    else
      this.crearSeguimiento();
  }

  cancelar() {
    this.location.back();
  }

  crearSeguimiento() {
    let psicologicoDTO = new SeguimientoPsicologicoDTO();

    psicologicoDTO.tokenEvaluacion = this.tokenEncabezado;
    psicologicoDTO.intervencionConcejeria = this.seguimientoForm.controls.intervencionConcejeria.value;
    psicologicoDTO.accionesRealizar = this.seguimientoForm.controls.accionesRealizar.value;
    psicologicoDTO.comentariosObservaciones = this.seguimientoForm.controls.comentariosObservaciones.value;
    psicologicoDTO.programa = this.seguimientoForm.controls.programa.value;
    psicologicoDTO.ambiente = this.seguimientoForm.controls.ambiente.value;

    this.seguimientoService.crearSeguimientoPsicologico(psicologicoDTO, this.nemonicoMenu).subscribe({
      next: (resp: RespuestaPorDefecto<Boolean>) => {
        if (!resp.exito) {
          this.dialogMensajeService.mensajeError(
            'Error al guardar el registro'
          )
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(
            'Guardar',
            'Registro guardado correctamente.'
          ).afterClosed().subscribe(() => {
            this.location.back();
          });
        }
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  editarSeguimiento() {
    let psicologicoDTO = this.item;

    psicologicoDTO.intervencionConcejeria = this.seguimientoForm.controls.intervencionConcejeria.value;
    psicologicoDTO.accionesRealizar = this.seguimientoForm.controls.accionesRealizar.value;
    psicologicoDTO.comentariosObservaciones = this.seguimientoForm.controls.comentariosObservaciones.value;
    psicologicoDTO.programa = this.seguimientoForm.controls.programa.value;
    psicologicoDTO.ambiente = this.seguimientoForm.controls.ambiente.value;

    this.seguimientoService.actualizarSeguimientoPsicologico(psicologicoDTO, this.nemonicoMenu).subscribe({
      next: (resp: RespuestaPorDefecto<Boolean>) => {
        if (!resp.exito) {
          this.dialogMensajeService.mensajeError(
            'Error al actualizar el registro'
          )
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(
            'Editar',
            'Registro actualizado correctamente.'
          ).afterClosed().subscribe(() => {
            this.location.back();
          });
        }
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  cargarJerarquias(tokenPadre: string): Observable<JerarquiaDTO[]> {
    return this.jerarquiaService.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenPadre)
      .pipe(
        map((resp: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (resp.exito) {
            return resp.data;
          } else {
            console.warn('Ocurrió un problema al cargar los centros:', resp.mensaje);
            return [];
          }
        }),
        catchError(error => {
          console.error('Error al cargar los centros:', error);
          return of([]); // Retorna un array vacío en caso de error
        })
      );
  }

  compararJerarquia(o1: JerarquiaDTO, o2: JerarquiaDTO): boolean {
    return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
  }
}
