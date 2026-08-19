import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { ActividadIntervencionDTO, ActividadIntervencionSeguimientoDTO, PlanTratamientoIndIntervDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { Location } from '@angular/common';
import { getEspPaginatorIntl } from 'app/app.component';
import moment from 'moment';

export const CUSTOM_DATE_FORMATS = {
  parse: {
    dateInput: 'DD/MM/YYYY',
  },
  display: {
    dateInput: 'DD/MM/YYYY',
    monthYearLabel: 'MMMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY',
  },
};

@Component({
  selector: 'app-actividad-diferenciada-seguimiento',
  standalone: true,
  imports: [MatStepperModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatRadioModule,
    MatPaginatorModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatTabsModule,
    CommonModule],
  templateUrl: './actividad-diferenciada-seguimiento.component.html',
  styleUrl: './actividad-diferenciada-seguimiento.component.scss',
  providers: [{ provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
  { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl(), },
  provideNativeDateAdapter(),
  { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }],
})
export class ActividadDiferenciadaSeguimientoComponent {

  uuid_fp: string;
  numDoc: string;
  numIntr: string;
  numActividad: string;

  mostrarFormulario: boolean = false;
  registroFormGroup: FormGroup;

  editingSeguimiento: ActividadIntervencionSeguimientoDTO | null = null;

  intervDiferenciada: PlanTratamientoIndIntervDTO;

  displayedColumnsSeguimientos: string[] = ['acciones', 'fecha', 'horaInicio', 'horaFin', 'observaciones'];
  dataSourceSeguimientos = new MatTableDataSource<ActividadIntervencionSeguimientoDTO>([]);
  estadoVisualizar = false;
  size: number = 10;
  page: number = 0;
  idActividadIntervencion!: number; // Asegúrate de asignar este valor adecuadamente
  totalItems: number = 0;
  nemonicoMenu: string = '';
  actividadInterv: ActividadIntervencionDTO;

  @ViewChild('paginatorSeguimientos') paginator!: MatPaginator;

  constructor(
    private fb: FormBuilder,
    private dateAdapter: DateAdapter<any>,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private dialogMensajeService: DialogMensajeService,
    private planTratamientoService: PlanTratamientoService,
    private router: Router,
    private _catalogoService: CatalogoService,
    private _location: Location,
  ) {
    this.dateAdapter.setLocale('es');
    this.registroFormGroup = this.fb.group({
      fechaInicio: [null, Validators.required],
      horaInicio: [null, Validators.required],
      horaFin: [null, Validators.required],
      observaciones: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.route.queryParams.subscribe(params => {
      const numInterv = params['numInterv'];
      const numDoc = params['numDoc'];
      this.numDoc = numDoc;
      if (numInterv) {
        this.numIntr = numInterv;
        this.numActividad = params['numActividad'];
        this.obtenerSeguimientosActividad();
        this.obtenerActividad();
        this.obtenerIntervencion(numInterv);
      }
    });
  }

  obtenerSeguimientosActividad() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.filter = this.numActividad;

    this.planTratamientoService
      .obtenerSeguimientosActividad(paginacionRequest, this.nemonicoMenu)
      .subscribe({
        next: (response: RespuestaPorDefecto<PaginacionResponse<ActividadIntervencionSeguimientoDTO>>) => {
          if (!response.exito) {
            this.planTratamientoService.checkError(response);

            return;
          }
          this.dataSourceSeguimientos.data = response.data.data;
          this.totalItems = response.data.totalItems;
          this.dataSourceSeguimientos.paginator = this.paginator;
        },
        error: (error: any) => {
          console.log(error);
        }
      });
  }

  toggleFormulario(): void {
    this.mostrarFormulario = !this.mostrarFormulario;
    if (this.mostrarFormulario) {
      this.registroFormGroup.reset();
    }
  }

  ejecutarCrearSeguimiento(): void {
    if (this.registroFormGroup.invalid) return;

    const formValues = this.registroFormGroup.value;


    const nuevoSeguimiento: ActividadIntervencionSeguimientoDTO = {
      idActividadIntervencionSeguimiento: this.editingSeguimiento?.idActividadIntervencionSeguimiento,
      idActividadIntervencion: Number(this.numActividad),
      fecha: formValues.fechaInicio,
      horaInicio: formValues.horaInicio,
      horaFin: formValues.horaFin,
      observaciones: formValues.observaciones,
      esEdicion: !!this.editingSeguimiento
    };

    this.planTratamientoService
      .crearActualizarSeguimientoActividad(nuevoSeguimiento, this.nemonicoMenu)
      .subscribe({
        next: response => {
          // En caso de éxito, ocultar formulario y recargar la tabla
          this.editingSeguimiento = null;
          this.mostrarFormulario = false;
          this.registroFormGroup.reset();
          this.obtenerSeguimientosActividad();
        },
        error: error => {
          this.planTratamientoService.checkError(error);
          console.error(error);

        }
      });
  }

  editarSeguimiento(seguimiento: ActividadIntervencionSeguimientoDTO): void {
    // Establecer el seguimiento que se va a editar
    this.editingSeguimiento = seguimiento;

    // Mostrar el formulario
    this.mostrarFormulario = true;

    // Rellenar el formulario con los valores del seguimiento seleccionado
    this.registroFormGroup.patchValue({
      fechaInicio: seguimiento.fecha,
      horaInicio: seguimiento.horaInicio,
      horaFin: seguimiento.horaFin,
      observaciones: seguimiento.observaciones
    });
  }

  cancelarEdicion(): void {
    // Reiniciar el formulario y estado de edición sin guardar
    this.editingSeguimiento = null;
    this.dataSourceSeguimientos.paginator = this.paginator;
    this.mostrarFormulario = false;
    this.registroFormGroup.reset();
  }

  obtenerActividad() {
    this.planTratamientoService.obtenerActividadPorId(Number(this.numActividad), 'nemonicoMenu').subscribe({
      next: (response) => {
        if (response.exito) {
          this.actividadInterv = response.data;
        } else {
          console.error('Error:', response.mensaje);
        }
      },
      error: (error) => {
        this.planTratamientoService.checkError(error);
        console.error('Error en la solicitud:', error);
      }
    });

  }

  obtenerIntervencion(numInterv: string) {
    this.planTratamientoService.obtenerIntervencion(numInterv).subscribe({
      next: (item) => {
        if (item.exito) {
          this.intervDiferenciada = item.data;
        } else {
          console.error('Error:', item.mensaje);
        }
      },
      error: (error) => {
        this.planTratamientoService.checkError(error);
        console.error('Error en la solicitud:', error);
      }
    })
  }

  salir() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/consolidar-intervencion`], {
      queryParams:
        { numDoc: this.numDoc, numInterv: this.numIntr }
    })
  }

  volver() {
    this._location.back();
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").format("YYYY/MM/DD");
  }
}
