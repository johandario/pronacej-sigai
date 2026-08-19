import { Component, Inject } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ActividadIntervencionDTO, PlanTratamientoIndSeguiDetalleDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { List } from 'lodash';
import { Observable, map, catchError, of, forkJoin, tap, throwError } from 'rxjs';

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
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-modal-seg-pti',
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
  templateUrl: './modal-seg-pti.component.html',
  styleUrl: './modal-seg-pti.component.scss'
})
export class ModalSegPtiComponent {
  registroFormGroup = this.fb.group({
    frecuenciaParticipacion: [null],
    situacionActual: [null],
    actitud: [null],
    aprovechamiento: [null],
  });

  situaciones: CatalogoDTO[] = [];
  frecuenciasParticipacion: CatalogoDTO[] = [];
  actitudes: CatalogoDTO[] = [];

  numDoc: string;
  numIntr: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_SEGUIMIENTO_PTI;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModalSegPtiComponent>,
    private planTratamientoService: PlanTratamientoService,
    @Inject(MAT_DIALOG_DATA) public data: { fila?: PlanTratamientoIndSeguiDetalleDTO, estado?: string },
    private router: Router,
    private catalogoService: CatalogoService,
    private dialogMensajeService: DialogMensajeService,
    private route: ActivatedRoute,
  ) {
    if (data.fila) {
      this.registroFormGroup.patchValue(data.fila);
    }
    if (data.estado) {
      this.registroFormGroup.disable();
    }
  }

  ngOnInit() {
    this.obtenerCatalogos().subscribe();
  }

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'PTI_SEG_CJDR_SITUACION_ACTUAL', 
      'PTI_SEG_CJDR_FRECUENCIA_PARTICIPACION',
      'PTI_SEG_CJDR_ACTITUD_PARTICIPACION',      
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.situaciones = results[0]?.data;
        this.frecuenciasParticipacion = results[1]?.data;
        this.actitudes = results[2]?.data;
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  cancelar(): void {
    this.dialogRef.close();
  }

  ejecutarAccion(): void {
    Object.assign(this.data.fila, this.registroFormGroup.value);
    this.dialogRef.close(this.data.fila);
  }

  compararCatalogos(o1: any, o2: any): boolean {
    return o1 && o2 ? o1.nemonico === o2.nemonico : o1 === o2;
  }
}
