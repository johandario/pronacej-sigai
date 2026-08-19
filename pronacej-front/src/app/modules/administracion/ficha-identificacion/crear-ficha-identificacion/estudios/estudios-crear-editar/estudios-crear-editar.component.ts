import { CommonModule } from '@angular/common';
import { Component, OnInit,Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { EstudiosDTO } from 'app/core/model/both/EstudiosDTO.model';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EstudiosService } from 'app/modules/seguridad/services/EstudiosService.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';

@Component({
  selector: 'app-estudios-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  templateUrl: './estudios-crear-editar.component.html',
  styleUrl: './estudios-crear-editar.component.scss'
})
export class EstudiosCrearEditarComponent implements OnInit {
  @Input() uuid_fp!: string;
  @Input() estudios!: EstudiosDTO;
  @Input() modo: 'crear' | 'editar' | 'ver' = 'crear';
  @Output() cerrar = new EventEmitter<void>();

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

  estudiosForm!: FormGroup;

  // uuid_fp: string;
  // modo: 'crear' | 'editar' | 'ver' = 'crear';
  // estudios: EstudiosDTO;

  soloLectura = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private estudiosService: EstudiosService
  ) {}

  ngOnInit(): void {
    this.soloLectura = this.modo === 'ver';
    this.crearFormulario();
    if (this.estudios) {
      this.cargarFormulario(this.estudios);
    }
    if (this.soloLectura) {
      this.estudiosForm.disable();
    }
  }

  crearFormulario(): void {
    this.estudiosForm = this.fb.group({
      fechaInicioEstudios: [null, Validators.required],
      cicloAcademicoActual: [null, Validators.required],

      convenioPronacej: [false],
      independiente: [false],

      idRegistroInstitucion: [null],
      tokenRegistroInstitucion: [null, Validators.required],
      ruc: [null, Validators.required],
      nombreOrganizacion: [null, Validators.required]
    });
  }

  cargarFormulario(data: EstudiosDTO): void {
    this.estudiosForm.patchValue({
      fechaInicioEstudios: data.fechaInicioEstudios ? new Date(data.fechaInicioEstudios) : null,
      cicloAcademicoActual: data.cicloAcademicoActual,
      convenioPronacej: data.convenioPronacej === true,
      independiente: data.independiente === true,
      idRegistroInstitucion: data.registroInstitucion?.idRegistroInstitucion,
      tokenRegistroInstitucion: data.registroInstitucion?.tokenIdentificador,
      ruc: data.registroInstitucion?.ruc,
      nombreOrganizacion: data.registroInstitucion?.nombreOrganizacion
    });
  }

  consultarInstitucionPorRuc(): void {
    const ruc = this.estudiosForm.get('ruc')?.value;

    if (!ruc) {
      this.dialogMensajeService.mensajeError('Debe ingresar el RUC.');
      return;
    }

    const request: EstudiosDTO = {
      registroInstitucion: {
        ruc
      } as RegistroInstitucionDTO
    };

    const load = this.dialogMensajeService.mensajeLoading('Consultando institución...');

    this.estudiosService.consultarInstitucionPorRuc(
      request,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<EstudiosDTO>) => {
        load.close();

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(response.mensaje);
          this.estudiosForm.patchValue({
            idRegistroInstitucion: null,
            tokenRegistroInstitucion: null,
            nombreOrganizacion: null
          });
          return;
        }

        const institucion = response.data.registroInstitucion;

        this.estudiosForm.patchValue({
          idRegistroInstitucion: institucion?.idRegistroInstitucion,
          tokenRegistroInstitucion: institucion?.tokenIdentificador,
          nombreOrganizacion: institucion?.nombreOrganizacion,
          ruc: institucion?.ruc
        });
      },
      error: (error: any) => {
        load.close();
        this.estudiosService.checkError(error);
      }
    });
  }

  cambioConvenio(): void {
    const convenio = this.estudiosForm.get('convenioPronacej')?.value;

    if (convenio) {
      this.estudiosForm.patchValue({
        independiente: false
      });
    }
  }

  cambioIndependiente(): void {
    const independiente = this.estudiosForm.get('independiente')?.value;

    if (independiente) {
      this.estudiosForm.patchValue({
        convenioPronacej: false
      });
    }
  }

  guardar(): void {
    if (this.estudiosForm.invalid) {
      this.estudiosForm.markAllAsTouched();
      this.dialogMensajeService.mensajeError('Debe completar los campos obligatorios.');
      return;
    }

    const form = this.estudiosForm.getRawValue();

    const request: EstudiosDTO = {
      ...this.estudios,
      fechaInicioEstudios: form.fechaInicioEstudios,
      cicloAcademicoActual: form.cicloAcademicoActual,
      convenioPronacej: form.convenioPronacej,
      independiente: form.independiente,
      tokenFichaIdentificacion: this.uuid_fp,
      registroInstitucion: {
        idRegistroInstitucion: form.idRegistroInstitucion,
        tokenIdentificador: form.tokenRegistroInstitucion,
        nombreOrganizacion: form.nombreOrganizacion,
        ruc: form.ruc
      } as RegistroInstitucionDTO
    };

    const load = this.dialogMensajeService.mensajeLoading('Guardando estudios...');

    this.estudiosService.crearEstudios(
      request,
      this.nemonicoMenu
    ).subscribe({
      next: (response: RespuestaPorDefecto<EstudiosDTO>) => {
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
        this.estudiosService.checkError(error);
      }
    });
  }

  regresar(): void {
    this.cerrar.emit();

  }

  private formatearFecha(fecha: any): string | null {
    if (!fecha) return null;

    const date = new Date(fecha);
    const mes = String(date.getMonth() + 1).padStart(2, '0');
    const dia = String(date.getDate()).padStart(2, '0');

    return `${date.getFullYear()}-${mes}-${dia}`;
  }
}