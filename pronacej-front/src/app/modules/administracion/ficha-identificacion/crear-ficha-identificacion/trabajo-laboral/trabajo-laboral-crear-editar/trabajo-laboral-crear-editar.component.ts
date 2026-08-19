import { CommonModule } from '@angular/common';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { TrabajoLaboralDTO } from 'app/core/model/both/TrabajoLaboralDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TrabajoLaboralService } from 'app/modules/seguridad/services/trabajoLaboral.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { InstitucionService } from 'app/modules/institucion/institucion.service';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-trabajo-laboral-crear-editar',
  standalone: true,
  imports: [
  CommonModule,
  ReactiveFormsModule,
  MatFormFieldModule,
  MatInputModule,
  MatDatepickerModule,
  MatNativeDateModule,
  MatButtonModule
],
  templateUrl: './trabajo-laboral-crear-editar.component.html',
  styleUrl: './trabajo-laboral-crear-editar.component.scss'
})
export class TrabajoLaboralCrearEditarComponent implements OnInit {

  @Input() uuid_fp!: string;
  @Input() modo: 'crear' | 'editar' | 'ver' = 'crear';
  @Input() trabajoLaboral: TrabajoLaboralDTO;

  @Output() cerrar = new EventEmitter<void>();
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;




  soloLectura = false;
  trabajoLaboralForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private trabajoLaboralService: TrabajoLaboralService, 
    private institucionService:InstitucionService
  ) {}

  ngOnInit(): void {
    this.soloLectura = this.modo === 'ver';
    this.crearFormulario();
    this.escucharCambioRuc();
    if (this.trabajoLaboral) {
      this.cargarFormulario(this.trabajoLaboral);
    }
    if (this.soloLectura) {
      this.trabajoLaboralForm.disable();
    }
  }
  
  crearFormulario(): void {
  this.trabajoLaboralForm = this.fb.group({
    fechaIngresoLaboral: [null, Validators.required],
    cargoLaboral: [null, Validators.required],

    idRegistroInstitucion: [null],
    tokenRegistroInstitucion: [null, Validators.required],
    nombreOrganizacion: [null, Validators.required],
    ruc: [null, Validators.required]
  });
}

  cargarFormulario(data: TrabajoLaboralDTO): void {
  this.trabajoLaboralForm.patchValue({
    fechaIngresoLaboral: data.fechaIngresoLaboral ? new Date(data.fechaIngresoLaboral) : null,
    cargoLaboral: data.cargoLaboral,

    idRegistroInstitucion: data.registroInstitucion?.idRegistroInstitucion,
    tokenRegistroInstitucion: data.registroInstitucion?.tokenIdentificador,
    nombreOrganizacion: data.registroInstitucion?.nombreOrganizacion,
    ruc: data.registroInstitucion?.ruc
  }, { emitEvent: false });
}

  guardar(): void {
  if (!this.uuid_fp) {
    this.dialogMensajeService.mensajeError('No se encontró la ficha de identificación asociada.');
    return;
  }

  if (this.trabajoLaboralForm.invalid) {
    this.trabajoLaboralForm.markAllAsTouched();
    this.dialogMensajeService.mensajeError('Debe completar los campos obligatorios.');
    return;
  }

  const form = this.trabajoLaboralForm.getRawValue();

  const request: TrabajoLaboralDTO = {
    idTrabajoLaboral: this.trabajoLaboral?.idTrabajoLaboral,
    tokenIdentificador: this.trabajoLaboral?.tokenIdentificador,

    fechaIngresoLaboral: form.fechaIngresoLaboral,
    cargoLaboral: form.cargoLaboral,
    tokenFichaIdentificacion: this.uuid_fp,

    registroInstitucion: {
      idRegistroInstitucion: form.idRegistroInstitucion,
      tokenIdentificador: form.tokenRegistroInstitucion,
      nombreOrganizacion: form.nombreOrganizacion,
      ruc: form.ruc
    } as RegistroInstitucionDTO
  };

  const load = this.dialogMensajeService.mensajeLoading(
    this.modo === 'editar' ? 'Actualizando trabajo laboral...' : 'Guardando trabajo laboral...'
  );

  console.log('========== REQUEST TRABAJO LABORAL ==========');
  console.log('modo:', this.modo);
  console.log('uuid_fp:', this.uuid_fp);
  console.log('form:', form);
  console.log('request:', request);

  this.trabajoLaboralService.crearTrabajoLaboral(
    request,
    this.nemonicoMenu
  ).subscribe({
    next: (response: RespuestaPorDefecto<TrabajoLaboralDTO>) => {
      load.close();

      if (!response.exito) {
        this.dialogMensajeService.mensajeError(response.mensaje);
        return;
      }

      this.dialogMensajeService.mensajeExitoso(
        response.titulo,
        response.mensaje
      );

      this.regresar();
    },
    error: (error: any) => {
      load.close();
      this.trabajoLaboralService.checkError(error);
    }
  });
}

  buscarInstitucionPorRuc(ruc: string): void {
    this.institucionService.obtenerInstitucionPorRuc(
      ruc,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<RegistroInstitucionDTO>) => {

        if (!response.exito || !response.data) {
          this.trabajoLaboralForm.patchValue({
            idRegistroInstitucion: null,
            tokenRegistroInstitucion: null,
            nombreOrganizacion: null
          }, { emitEvent: false });

          this.dialogMensajeService.mensajeError(
            response.mensaje || 'No se encontró una institución con el RUC ingresado.'
          );
          return;
        }

        const institucion = response.data;

        this.trabajoLaboralForm.patchValue({
          idRegistroInstitucion: institucion.idRegistroInstitucion,
          tokenRegistroInstitucion: institucion.tokenIdentificador,
          nombreOrganizacion: institucion.nombreOrganizacion,
          ruc: institucion.ruc
        }, { emitEvent: false });
      },
      error: (error: any) => {
        this.trabajoLaboralService.checkError(error);
      }
    });
  }

  escucharCambioRuc(): void {
      this.trabajoLaboralForm.get('ruc')?.valueChanges
        .pipe(
          debounceTime(600),
          distinctUntilChanged()
        )
        .subscribe((ruc: string) => {
          if (!ruc || ruc.length < 10) {
            this.trabajoLaboralForm.patchValue({
              idRegistroInstitucion: null,
              tokenRegistroInstitucion: null,
              nombreOrganizacion: null
            }, { emitEvent: false });

            return;
          }
          this.buscarInstitucionPorRuc(ruc);
        });
    }

  regresar(): void {
    this.cerrar.emit();
  }
}