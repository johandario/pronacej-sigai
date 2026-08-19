import { Component, Inject, ViewEncapsulation } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ActividadIntervencionDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { List } from 'lodash';
import { Observable, map, catchError, of } from 'rxjs';

import { CatalogoService } from 'app/modules/catalogo/catalogo.service';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { CommonModule } from '@angular/common';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { getEspPaginatorIntl } from 'app/app.component';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-actividad-intervencion-dialog',
  standalone: true,
  imports: [MatFormFieldModule,
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
    MatDatepickerModule,
    CommonModule],
  // providers: [{ provide: MAT_DATE_LOCALE, useValue: 'en-GB' },
  // { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() },
  // provideNativeDateAdapter(),],
  providers: [
          { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
          { provide: DateAdapter, useClass: CustomDateAdapter },
          { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
        ],
  templateUrl: './actividad-intervencion-dialog.component.html',
  styleUrl: './actividad-intervencion-dialog.component.scss',
  encapsulation: ViewEncapsulation.None,    
})
export class ActividadIntervencionDialogComponent {

  registroFormGroup: FormGroup;
  listaTipoFrecuencia: CatalogoDTO[] = [];
  numDoc: string;
  numIntr: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ActividadIntervencionDialogComponent>,
    private planTratamientoService: PlanTratamientoService,
    @Inject(MAT_DIALOG_DATA) public data: { actividad?: ActividadIntervencionDTO, visualizar?: boolean },
    private router: Router,
    private _catalogoService: CatalogoService,
    private dialogMensajeService: DialogMensajeService,
    private route: ActivatedRoute,
    private dateAdapter: DateAdapter<any>,

  ) {
    // Inicializar el formulario con validadores
    this.registroFormGroup = this.fb.group({
      subactividad: [null, Validators.required],
      frecuencia: [null, Validators.required],
      fechaInicio: [null, Validators.required],
      horaInicio: [null, Validators.required],
      fechaFin: [null, Validators.required],
      horaFin: [null, Validators.required]
    });

    this.dateAdapter.setLocale('es');
  }

  async ngOnInit(): Promise<void> {
    if (this.data?.visualizar) {
      this.registroFormGroup.disable();
    }

    this.route.queryParams.subscribe(params => {
      const numInterv = params['numInterv'];
      const numDoc = params['numDoc'];
      this.numIntr = numInterv;
      this.numDoc = numDoc;
    });
    await this.getCatalogoFrecuencia();
    if (this.data?.actividad) {
      console.log('actividad a editar', this.data.actividad);
      const act = this.data.actividad;
      this.registroFormGroup.patchValue({
        subactividad: act.subactividad,
        frecuencia: act.frecuencia,
        fechaInicio: new Date(act.fechaInicio),
        horaInicio: this.formatearHora(act.fechaInicio), // Si hora se guarda en fechaInicio como tiempo
        fechaFin: new Date(act.fechaFin),
        horaFin: this.formatearHora(act.fechaFin)
      });
      // if (act.frecuencia) {
      //   this.registroFormGroup.get('frecuencia')?.setValue(this.listaTipoFrecuencia.find(frecuencia => frecuencia.nemonico == act.frecuencia.nemonico));
      // }
    }
  }

  async getCatalogoFrecuencia() {
    this.getCatalogos('FRECUENCIA').subscribe(
      (catalogos: CatalogoDTO[]) => {
        this.listaTipoFrecuencia = catalogos;
      }
    );
  }

  getCatalogos(nemonico: string): Observable<CatalogoDTO[]> {
    return this._catalogoService.getCatalogosPorNemonicPadre(nemonico,this.nemonicoMenu).pipe(
      map((response: RespuestaPorDefecto<List<CatalogoDTO>>) => {
        if (!response.exito) {
          this._catalogoService.checkError(response);
          return [];
        }
        return Array.from(response.data);
      }),
      catchError((error: any) => {
        this._catalogoService.checkError(error);
        return of([]);
      })
    );
  }

  formatearHora(fecha: Date | undefined): string {
    if (!fecha) return '';
    const d = new Date(fecha);
    return d.toISOString().substring(11, 16); // Formato HH:mm
  }

  cancelar(): void {
    this.dialogRef.close();
  }

  ejecutarAccion(): void {
    if (this.registroFormGroup.invalid) return;

    const formValues = this.registroFormGroup.value;
    // this.registroFormGroup.disable();

    const fecha: Date = this.registroFormGroup.get('fechaInicio')?.value;
    const hora: string = this.registroFormGroup.get('horaInicio')?.value;

    const [hours, minutes] = hora.split(':').map(Number);
    const fechaHora = new Date(fecha);
    fechaHora.setHours(hours);
    fechaHora.setMinutes(minutes);
    fechaHora.setSeconds(0);
    fechaHora.setMilliseconds(0);

    const fechaFin: Date = this.registroFormGroup.get('fechaFin')?.value;
    const horaFin: string = this.registroFormGroup.get('horaFin')?.value;

    const [hoursF, minutesF] = horaFin.split(':').map(Number);
    const fechaHoraFin = new Date(fechaFin);
    fechaHoraFin.setHours(hoursF);
    fechaHoraFin.setMinutes(minutesF);
    fechaHoraFin.setSeconds(0);
    fechaHoraFin.setMilliseconds(0);
    const actividad: ActividadIntervencionDTO = {
      ...this.data?.actividad,
      subactividad: formValues.subactividad,
      frecuencia: formValues.frecuencia,
      fechaInicio: fechaHora,
      fechaFin: fechaHoraFin,
      idPlanTratIndInterv: Number(this.numIntr),
      esEdicion: this.data.actividad?.idActividadIntervencion ? true : false
    };


    const fechaInicio = actividad.fechaInicio;
    const fechaFinal = actividad.fechaFin;

    if (fechaInicio.getTime() <= fechaFinal.getTime()) {
      this.planTratamientoService.crearActualizarActividad(actividad, 'nemonicoMenu')
      .subscribe({
        next: response => {
          // Manejar la respuesta y cerrar el modal si es exitoso
          this.dialogMensajeService.mensajeExitoso(
            response.titulo,
            response.mensaje
          );
          this.dialogRef.close(response);
        },
        error: error => {
          this.planTratamientoService.checkError(error);
          this.registroFormGroup.enable();
          console.error(error);
        }
      });
    } else {
      this.dialogMensajeService.mensajeAdvertencia("Fecha incorrecta", "La fecha y hora de fin no puede ser menor a la fecha y hora de inicio")
    }

    
  }

  compararCatalogos(o1: any, o2: any): boolean {
    return o1 && o2 ? o1.nemonico === o2.nemonico : o1 === o2;
  }

}
