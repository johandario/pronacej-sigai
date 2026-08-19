import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatDialog } from '@angular/material/dialog';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaUbicacionDTO } from 'app/core/model/both/fichaUbicacion.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';
import { JerarquiasPorNemonicosPadreRequest } from 'app/core/model/request/JerarquiasPorNemonicosPadreRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { FichaUbicacionService } from 'app/modules/seguridad/services/fichaUbicacion.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { UbicacionJerarquiaService } from 'app/modules/ubicacion/services/ubicacionJerarquia.service';
import { UBICACION_TIPO_CELDA } from 'app/modules/ubicacion/ubicacion.types';
import { Observable, map } from 'rxjs';
import { CeldaSelectorModalComponent } from './celda-selector-modal/celda-selector-modal.component';

@Component({
  selector: 'app-ficha-ubicacion-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatSelectModule,
    MatSlideToggleModule
  ],
  templateUrl: './ficha-ubicacion-crear-editar.component.html',
  styleUrl: './ficha-ubicacion-crear-editar.component.scss',
  providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    ],
})
export class FichaUbicacionCrearEditarComponent implements OnInit {

  formulario: FormGroup;
  uuid_fp!: string;
  tokenRegistro?: string;
  esVisualizacion = false;
  cargando = false;

  centros: JerarquiaDTO[] = [];
  ubicacionesJerarquicas: UbicacionJerarquiaDTO[] = [];
  celdasDisponibles: Array<{ nombre: string; ubicacionJerarquia: UbicacionJerarquiaDTO }> = [];
  nombreCeldaSeleccionada = '';

  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACIONES_JERARQUICAS;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private fichaUbicacionService: FichaUbicacionService,
    private jerarquiaService: JerarquiaService,
    private ubicacionJerarquiaService: UbicacionJerarquiaService
  ) {
    this.formulario = this.fb.group({
      fechaIngreso: [null, Validators.required],
      horaIngreso: [null, Validators.required],
      centroDestino: [null, Validators.required],
      ubicacionActual: [false],
      numeroCama: [null],
      atencionPrioritaria: [false],
      ingresoExpediente: [false],
      observaciones: [''],
      ubicacionJerarquia: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    this.cargarCentros(this.nemonicoMenu).subscribe(centros => {
      this.centros = centros;
    });

    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.route.queryParams.subscribe(params => {
      this.tokenRegistro = params['token'];
      this.esVisualizacion = params['state'] === 'show';

      if (this.tokenRegistro) {
        this.obtenerRegistro();
      } else if (this.esVisualizacion) {
        this.formulario.disable();
      }
    });
  }

  obtenerRegistro() {
    if (!this.tokenRegistro) {
      return;
    }

    this.cargando = true;
    this.fichaUbicacionService.obtenerPorTokenIdentificador(this.tokenRegistro, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<FichaUbicacionDTO>) => {
        this.cargando = false;

        if (!response.exito) {
          this.fichaUbicacionService.checkError(response);
          return;
        }

        const fichaUbicacion = response.data;
        const fechaIngreso = this.parsearFecha(fichaUbicacion.fechaIngreso);
        const centroDestino = fichaUbicacion.centro || null;
        const ubicacionJerarquia = fichaUbicacion.ubicacionJerarquia || null;

        this.formulario.patchValue({
          fechaIngreso,
          horaIngreso: this.extraerHora(fechaIngreso),
          centroDestino,
          ubicacionActual: !!fichaUbicacion.ubicacionActual,
          numeroCama: fichaUbicacion.numeroCama,
          atencionPrioritaria: !!fichaUbicacion.atencionPrioritaria,
          ingresoExpediente: !!fichaUbicacion.ingresoExpediente,
          observaciones: fichaUbicacion.observaciones || '',
          ubicacionJerarquia,
        });

        if (centroDestino?.tokenIdentificador) {
          this.cargarCeldasCentro(centroDestino.tokenIdentificador, ubicacionJerarquia, this.esVisualizacion);
          return;
        }

        if (this.esVisualizacion) {
          this.formulario.disable();
        }
      },
      error: (error: any) => {
        this.cargando = false;
        this.fichaUbicacionService.checkError(error);
      }
    });
  }

  guardar() {
    if (this.esVisualizacion || this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const values = this.formulario.getRawValue();
    const fechaIngreso = this.combinarFechaHora(values.fechaIngreso, values.horaIngreso);

    if (!fechaIngreso) {
      this.formulario.get('fechaIngreso')?.setErrors({ required: true });
      this.formulario.get('horaIngreso')?.setErrors({ required: true });
      this.formulario.markAllAsTouched();
      return;
    }

    const fichaUbicacion = new FichaUbicacionDTO();

    fichaUbicacion.tokenIdentificador = this.tokenRegistro;
    fichaUbicacion.tokenIdentificadorFichaIdentificacion = this.uuid_fp;
    fichaUbicacion.fechaIngreso = fechaIngreso;
    fichaUbicacion.ubicacionActual = !!values.ubicacionActual;
    fichaUbicacion.numeroCama = values.numeroCama;
    fichaUbicacion.atencionPrioritaria = !!values.atencionPrioritaria;
    fichaUbicacion.ingresoExpediente = !!values.ingresoExpediente;
    fichaUbicacion.observaciones = values.observaciones;
    fichaUbicacion.ubicacionJerarquia = values.ubicacionJerarquia || null;
    fichaUbicacion.centro = values.centroDestino || null;

    this.cargando = true;
    this.fichaUbicacionService.crearEditar(fichaUbicacion, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<FichaUbicacionDTO>) => {
        this.cargando = false;
        this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);

        if (!response.exito) {
          return;
        }

        this.volver();
      },
      error: (error: any) => {
        this.cargando = false;
        this.fichaUbicacionService.checkError(error);
      }
    });
  }

  volver() {
    this.router.navigate(['../'], { relativeTo: this.route });
  }

  private parsearFecha(fecha?: Date): Date | null {
    if (!fecha) {
      return null;
    }

    const parsedDate = new Date(fecha);
    if (Number.isNaN(parsedDate.getTime())) {
      return null;
    }

    return parsedDate;
  }

  private extraerHora(fecha: Date | null): string | null {
    if (!fecha) {
      return null;
    }

    const horas = `${fecha.getHours()}`.padStart(2, '0');
    const minutos = `${fecha.getMinutes()}`.padStart(2, '0');

    return `${horas}:${minutos}`;
  }

  private combinarFechaHora(fecha: Date | null, hora: string | null): Date | null {
    if (!fecha || !hora) {
      return null;
    }

    const [horasStr, minutosStr] = hora.split(':');
    const horas = Number(horasStr);
    const minutos = Number(minutosStr);

    if (
      Number.isNaN(horas) ||
      Number.isNaN(minutos) ||
      horas < 0 ||
      horas > 23 ||
      minutos < 0 ||
      minutos > 59
    ) {
      return null;
    }

    const fechaCombinada = new Date(fecha);
    fechaCombinada.setHours(horas, minutos, 0, 0);

    return fechaCombinada;
  }

  private cargarCentros(nemonicoMenu: string): Observable<any> {
      const request = new JerarquiasPorNemonicosPadreRequest();
      request.nemonicosPadre = ['CJDR'];

      return this.jerarquiaService
          .obtenerJerarquiasPorNemonicoPadreLista(request, nemonicoMenu)
          .pipe(
              map(
                  (
                      response: RespuestaPorDefecto<
                          Record<string, JerarquiaDTO[]>
                      >
                  ) => {
                  if (!response.exito) {
                      throw new Error(response.mensaje || 'No se pudieron cargar los centros');
                  }

                  const centros = Object.values(response.data || {}).flat();
                  
                  return centros;
                  }
              )
          );
  }

  getDireccionCentroSeleccionado(): string {
    const centroSeleccionado: JerarquiaDTO = this.formulario.get('centroDestino')?.value;
    if (!centroSeleccionado || !centroSeleccionado.direccion || centroSeleccionado.direccion.trim().length === 0) {
      return '';
    }
    return 'Dirección: ' + centroSeleccionado.direccion;
  }

  private construirNombreCelda(
    ubicacion: UbicacionJerarquiaDTO,
    ubicacionesPorToken: Map<string, UbicacionJerarquiaDTO>
  ): string {

      const ruta: string[] = [];

      let actual: UbicacionJerarquiaDTO | undefined = ubicacion;

      while (actual) {

          ruta.unshift(actual.nombre);

          const tokenPadre =
              actual.ubicacionJerarquiaPadre?.tokenIdentificador;

          actual = tokenPadre
              ? ubicacionesPorToken.get(tokenPadre)
              : undefined;
      }

      return ruta.join(' / ');
  }

  getCantidadCeldasDisponibles(): string {
    if (!this.formulario.get('centroDestino')?.value) {
      return 'Seleccione un centro para habilitar el selector';
    }

    const cantCeldas = this.celdasDisponibles.length;
    return `Existen ${cantCeldas} celdas en el centro seleccionado`;
  }

  getCeldaSeleccionada(): string {
    return this.nombreCeldaSeleccionada;
  }

  private filtrarYProcesarCeldas(
    ubicaciones: UbicacionJerarquiaDTO[]
  ): void {

    const ubicacionesPorToken = new Map(
      ubicaciones.map(u => [u.tokenIdentificador, u])
    );

    this.celdasDisponibles = ubicaciones
      .filter(
        u => u.jerarquiaTipo?.nemonico === UBICACION_TIPO_CELDA
      )
      .map(u => {

        const ruta = this.obtenerRutaJerarquia(
          u,
          ubicacionesPorToken
        );

        return {
          nombre: ruta.join(' / '),
          ruta,
          ubicacionJerarquia: u
        };
      })
      .sort((a, b) => this.compararRutas(a.ruta, b.ruta))
      .map(({ ruta, ...rest }) => rest);
  }

  private obtenerRutaJerarquia(
    ubicacion: UbicacionJerarquiaDTO,
    ubicacionesPorToken: Map<string, UbicacionJerarquiaDTO>
  ): string[] {

    const ruta: string[] = [];

    let actual: UbicacionJerarquiaDTO | undefined = ubicacion;

    while (actual) {

      ruta.unshift(actual.nombre);

      const tokenPadre =
        actual.ubicacionJerarquiaPadre?.tokenIdentificador;

      actual = tokenPadre
        ? ubicacionesPorToken.get(tokenPadre)
        : undefined;
    }

    return ruta;
  }

  private compararRutas(
    rutaA: string[],
    rutaB: string[]
  ): number {

    const max = Math.max(
      rutaA.length,
      rutaB.length
    );

    for (let i = 0; i < max; i++) {

      const valorA = rutaA[i] ?? '';
      const valorB = rutaB[i] ?? '';

      const comparacion = valorA.localeCompare(
        valorB,
        'es',
        { sensitivity: 'base' }
      );

      if (comparacion !== 0) {
        return comparacion;
      }
    }

    return 0;
  }

  compararJerarquias = (a: JerarquiaDTO | null, b: JerarquiaDTO | null): boolean => {
    if (!a && !b) {
      return true;
    }

    if (!a || !b) {
      return false;
    }

    return a.tokenIdentificador === b.tokenIdentificador;
  };

  compararUbicaciones = (a: UbicacionJerarquiaDTO | null, b: UbicacionJerarquiaDTO | null): boolean => {
    if (!a && !b) {
      return true;
    }

    if (!a || !b) {
      return false;
    }

    return a.tokenIdentificador === b.tokenIdentificador;
  };

  private obtenerUbicacionDesdeCeldas(ubicacionJerarquia: UbicacionJerarquiaDTO | null): UbicacionJerarquiaDTO | null {
    if (!ubicacionJerarquia?.tokenIdentificador) {
      return null;
    }

    const celda = this.celdasDisponibles.find(c => c.ubicacionJerarquia.tokenIdentificador === ubicacionJerarquia.tokenIdentificador);
    return celda?.ubicacionJerarquia || null;
  }

  abrirSelectorCelda(): void {
    if (this.esVisualizacion || !this.formulario.get('centroDestino')?.value) {
      return;
    }

    const tokenSeleccionado = this.formulario.get('ubicacionJerarquia')?.value?.tokenIdentificador || null;

    const dialogRef = this.dialog.open(CeldaSelectorModalComponent, {
      width: '920px',
      maxWidth: '96vw',
      disableClose: true,
      data: {
        ubicaciones: this.ubicacionesJerarquicas,
        tokenSeleccionado
      }
    });

    dialogRef.afterClosed().subscribe((ubicacionSeleccionada: UbicacionJerarquiaDTO | null) => {
      if (!ubicacionSeleccionada) {
        return;
      }

      const ubicacionEncontrada = this.obtenerUbicacionDesdeCeldas(ubicacionSeleccionada);
      if (!ubicacionEncontrada) {
        return;
      }

      this.formulario.get('ubicacionJerarquia')?.setValue(ubicacionEncontrada);
      this.actualizarNombreCeldaSeleccionada(ubicacionEncontrada);
    });
  }

  private actualizarNombreCeldaSeleccionada(ubicacionJerarquia: UbicacionJerarquiaDTO | null): void {
    if (!ubicacionJerarquia?.tokenIdentificador) {
      this.nombreCeldaSeleccionada = '';
      return;
    }

    const celda = this.celdasDisponibles.find(c => c.ubicacionJerarquia.tokenIdentificador === ubicacionJerarquia.tokenIdentificador);
    this.nombreCeldaSeleccionada = celda?.nombre || ubicacionJerarquia.nombre || '';
  }

  private cargarCeldasCentro(
    tokenCentro: string,
    ubicacionJerarquiaSeleccionada?: UbicacionJerarquiaDTO | null,
    deshabilitarFormularioDespuesDeCargar: boolean = false
  ): void {
    this.formulario.get('ubicacionJerarquia')?.setValue(null);
    this.nombreCeldaSeleccionada = '';

    this.ubicacionJerarquiaService.obtenerPorTokenIdentificadorJerarquiaCentro(tokenCentro, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<UbicacionJerarquiaDTO[]>) => {
        if (!response.exito) {
          this.ubicacionJerarquiaService.checkError(response);
          if (deshabilitarFormularioDespuesDeCargar) {
            this.formulario.disable();
          }
          return;
        }

        this.ubicacionesJerarquicas = response.data || [];
        this.filtrarYProcesarCeldas(response.data || []);

        const ubicacionEncontrada = this.obtenerUbicacionDesdeCeldas(ubicacionJerarquiaSeleccionada || null);
        if (ubicacionEncontrada) {
          this.formulario.get('ubicacionJerarquia')?.setValue(ubicacionEncontrada);
          this.actualizarNombreCeldaSeleccionada(ubicacionEncontrada);
        }

        if (deshabilitarFormularioDespuesDeCargar) {
          this.formulario.disable();
        }
      },
      error: (error: any) => {
        this.ubicacionJerarquiaService.checkError(error);
        if (deshabilitarFormularioDespuesDeCargar) {
          this.formulario.disable();
        }
      }
    });
  }

  cambiarSelectCentro(event: MatSelectChange) {
    const centroSeleccionado: JerarquiaDTO = event.value;

    if (!centroSeleccionado?.tokenIdentificador) {
      this.ubicacionesJerarquicas = [];
      this.celdasDisponibles = [];
      this.formulario.get('ubicacionJerarquia')?.setValue(null);
      this.nombreCeldaSeleccionada = '';
      return;
    }

    this.cargarCeldasCentro(centroSeleccionado.tokenIdentificador);
  }
}
