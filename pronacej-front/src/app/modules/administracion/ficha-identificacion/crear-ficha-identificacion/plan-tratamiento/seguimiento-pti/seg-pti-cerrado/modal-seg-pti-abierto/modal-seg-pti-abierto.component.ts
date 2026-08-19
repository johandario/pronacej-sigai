import { Component, Inject } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ActividadIntervencionDTO, PlanTratamientoIndSeguiDetalleDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { List } from 'lodash';
import { Observable, map, catchError, of, forkJoin, tap, throwError } from 'rxjs';
import etiquetasModel from 'app/core/etiquetas.model';

import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { CommonModule } from '@angular/common';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { MatRadioModule } from '@angular/material/radio';

@Component({
  selector: 'app-modal-seg-pti-abierto',
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
      CommonModule,
      MatRadioModule
    ],
  templateUrl: './modal-seg-pti-abierto.component.html',
  styleUrl: './modal-seg-pti-abierto.component.scss'
})
export class ModalSegPtiAbiertoComponent {
registroFormGroup = this.fb.group({
    indicadores: [null],
    analisis: [null],
  });

  situaciones: CatalogoDTO[] = [];
  frecuenciasParticipacion: CatalogoDTO[] = [];
  actitudes: CatalogoDTO[] = [];

  numDoc: string;
  numIntr: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_PTI;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModalSegPtiAbiertoComponent>,
    private planTratamientoService: PlanTratamientoService,
    @Inject(MAT_DIALOG_DATA) public data: { fila?: PlanTratamientoIndSeguiDetalleDTO, estado?: string },
    private router: Router,
    private catalogoService: CatalogoService,
    private dialogMensajeService: DialogMensajeService,
    private route: ActivatedRoute,
  ) {
    console.log('data',data);
    if (data.fila) {
      this.registroFormGroup.patchValue(data.fila);
    }
    if (data.estado) {
      this.registroFormGroup.disable();
    }
  }

  ngOnInit() {

  }  

  cancelar(): void {
    this.dialogRef.close();
  }

  ejecutarAccion(): void {
    console.log('data',this.data);
    Object.assign(this.data.fila, this.registroFormGroup.value);
    const seleccionado = this.registroFormGroup.value.indicadores;
    if (seleccionado === 'indicadorDeficiente') {
      this.data.fila.indicadorDeficiente = true;
      this.data.fila.indicadorEnProceso = false;
      this.data.fila.indicadorLogrado = false;
    } else if (seleccionado === 'indicadorEnProceso') {
      this.data.fila.indicadorDeficiente = false;
      this.data.fila.indicadorEnProceso = true;
      this.data.fila.indicadorLogrado = false;
    } else if (seleccionado === 'indicadorLogrado') {
      this.data.fila.indicadorDeficiente = false;
      this.data.fila.indicadorEnProceso = false;
      this.data.fila.indicadorLogrado = true;
    }
    this.dialogRef.close(this.data.fila);
  }

  
}
