import { Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ActividadIntervencionDTO, ActividadIntervencionSeguimientoDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
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
  selector: 'app-modal-seg-subact-pti',
  standalone: true,
  imports: [
    MatFormFieldModule,
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
    CommonModule
  ],
  templateUrl: './modal-seg-subact-pti.component.html',
  styleUrl: './modal-seg-subact-pti.component.scss',
 providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
  encapsulation: ViewEncapsulation.None,  
  
})
export class ModalSegSubactPtiComponent implements OnInit {
  fila: ActividadIntervencionSeguimientoDTO = new ActividadIntervencionSeguimientoDTO;

  registroFormGroup = this.fb.group({
    fecha: [null as Date, Validators.required],
    horaInicio: [null as string, Validators.required],
    horaFin: [null as string, Validators.required],
    observaciones: [null as string, Validators.required],
  });

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_PTI;

  constructor(
    private dateAdapter: DateAdapter<any>,
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModalSegSubactPtiComponent>,
    private dialogMensajeService: DialogMensajeService,
    @Inject(MAT_DIALOG_DATA) public data: { fila?: ActividadIntervencionSeguimientoDTO, visualizar?: boolean },
  ) {
    this.dateAdapter.setLocale('es');    
  }

  ngOnInit(): void {
    if (this.data.visualizar) {
      this.registroFormGroup.disable();
    } 

    if (this.data.fila) {
      console.log(this.data.fila);
      this.registroFormGroup.patchValue(this.data.fila);
      this.registroFormGroup.controls['fecha'].setValue(new Date(this.data.fila.fecha));
    } else {
      this.fila = this.registroFormGroup.value;
    }
  }

  agregar() {
    Object.assign(this.fila, this.registroFormGroup.value);

    const horaInicio = (this.fila.horaInicio).split(":")[0] + (this.fila.horaInicio).split(":")[1];
    const horaFin = (this.fila.horaFin).split(":")[0] + (this.fila.horaFin).split(":")[1];

    if (horaInicio <= horaFin) {
      this.dialogRef.close(this.fila);
    } else {
      this.dialogMensajeService.mensajeAdvertencia("Error en ingreso de hora", "La hora de fin no puede ser mayor a la hora de inicio");
    }
  }
}
