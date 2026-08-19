import { CommonModule, Location } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { SeguimientoConductualDTO } from 'app/core/model/both/ia/seguimientoConductualDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TabService } from 'app/core/services/tab.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { SeguimientoService } from 'app/modules/administracion/services/seguimiento.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import { catchError, map, Observable, of } from 'rxjs';

@Component({
  selector: 'app-seguimiento-conductual-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatIconModule,
    MatRadioModule,
    MatSelectModule,
    MatLabel,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './seguimiento-conductual-crear-editar.component.html',
  styleUrl: './seguimiento-conductual-crear-editar.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ]
})
export class SeguimientoConductualCrearEditarComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOLOGICA;

  esEdicion: boolean = false;
  esVisualizacion: boolean = false;
  tokenEncabezado: string;
  item: SeguimientoConductualDTO;
  centro: JerarquiaDTO;

  tiposConducta: CatalogoDTO[] = [];

  seguimientoForm: FormGroup;

  programas: JerarquiaDTO[] = [];
  ambientes: JerarquiaDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private seguimientoService: SeguimientoService,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils,
    private location: Location,
    private jerarquiaService: JerarquiaService,
    private tabService: TabService,
  ) { }

  ngOnInit(): void {
    this.tokenEncabezado = history.state.tokenEncabezado;
    this.centro = history.state.centro;
    this.esVisualizacion = history.state.esVisualizacion;

    this.seguimientoForm = this.fb.group({
      estable: [null, Validators.required],
      periodoDesde: ['', Validators.required],
      periodoHasta: ['', Validators.required],
      tipoConducta: ['', Validators.required],
      descripcionConducta: ['', [Validators.required]],
      accionesAdoptadas: ['', [Validators.required]],
      programa: [JerarquiaDTO],
      ambiente: [JerarquiaDTO],
    });

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.TIPO_CONDUCTA, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.tiposConducta = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });

    this.item = history.state.item;

    if (this.item) {
      this.esEdicion = true;
      this.seguimientoForm.controls.estable.setValue(this.item.estable);
      this.seguimientoForm.controls.periodoDesde.setValue(new Date(this.item.periodoDesde));
      this.seguimientoForm.controls.periodoHasta.setValue(new Date(this.item.periodoHasta));
      this.seguimientoForm.controls.tipoConducta.setValue(this.item.nemonicoTipoConducta);
      this.seguimientoForm.controls.descripcionConducta.setValue(this.item.descripcionConducta);
      this.seguimientoForm.controls.accionesAdoptadas.setValue(this.item.accionesAdoptadas);
    }

    if (this.centro.nombre.includes('CJDR')) {
      this.seguimientoForm.controls.programa.setValidators(Validators.required);
      this.seguimientoForm.controls.ambiente.setValidators(Validators.required);

      this.cargarJerarquias(this.centro.tokenIdentificador).subscribe(data => {
        this.programas = data;

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
      this.ambientes = data;

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
    this.tabService.cambiarTab(1);
    this.location.back();
  }

  crearSeguimiento() {
    let conductualDTO = new SeguimientoConductualDTO();

    conductualDTO.tokenEvaluacion = this.tokenEncabezado;
    conductualDTO.estable = this.seguimientoForm.controls.estable.value;
    conductualDTO.periodoDesde = this.seguimientoForm.controls.periodoDesde.value;
    conductualDTO.periodoHasta = this.seguimientoForm.controls.periodoHasta.value;
    conductualDTO.nemonicoTipoConducta = this.seguimientoForm.controls.tipoConducta.value;
    conductualDTO.descripcionConducta = this.seguimientoForm.controls.descripcionConducta.value;
    conductualDTO.accionesAdoptadas = this.seguimientoForm.controls.accionesAdoptadas.value;
    conductualDTO.programa = this.seguimientoForm.controls.programa.value;
    conductualDTO.ambiente = this.seguimientoForm.controls.ambiente.value;

    this.seguimientoService.crearSeguimientoConductual(conductualDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          'Registro guardado correctamente.'
        ).afterClosed().subscribe(() => {
          this.tabService.cambiarTab(1);
          this.location.back();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  editarSeguimiento() {
    let conductualDTO = this.item;

    conductualDTO.estable = this.seguimientoForm.controls.estable.value;
    conductualDTO.periodoDesde = this.seguimientoForm.controls.periodoDesde.value;
    conductualDTO.periodoHasta = this.seguimientoForm.controls.periodoHasta.value;
    conductualDTO.nemonicoTipoConducta = this.seguimientoForm.controls.tipoConducta.value;
    conductualDTO.descripcionConducta = this.seguimientoForm.controls.descripcionConducta.value;
    conductualDTO.accionesAdoptadas = this.seguimientoForm.controls.accionesAdoptadas.value;
    conductualDTO.programa = this.seguimientoForm.controls.programa.value;
    conductualDTO.ambiente = this.seguimientoForm.controls.ambiente.value;

    this.seguimientoService.actualizarSeguimientoConductual(conductualDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Editar',
          'Registro actualizado correctamente.'
        ).afterClosed().subscribe(() => {
          this.tabService.cambiarTab(1);
          this.location.back();
        });
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
