import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { catchError, forkJoin, Observable, tap, throwError } from 'rxjs';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter,DateAdapter } from '@angular/material/core';
import { InformeFinalAsistenciaDetalleDTO, InformeFinalAsistenciaDTO } from 'app/core/model/both/informeFinalAsistenciaDTO.model';
import { InformeFinalAsistenciaService } from 'app/modules/seguridad/services/informeFinalAsistencia.service';
import { ModalInformeFinalAsistenciaComponent } from './modal/modal-informe-final-asistencia.component';
import { Location } from '@angular/common';
import { PlanAsistenciaPostEgresoDetalleDTO, PlanAsistenciaPostEgresoDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { CUSTOM_DATE_FORMATS,CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-crear-editar-informe-final-asistencia',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatButtonModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatInputModule,
    MatIconModule,
    MatPaginatorModule,
    MatTableModule,
    ReactiveFormsModule,
  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter()
  ],  
  templateUrl: './crear-editar-informe-final-asistencia.component.html',
  styleUrl: './crear-editar-informe-final-asistencia.component.scss'
})
export class CrearEditarInformeFinalAsistenciaComponent implements OnInit {

  dimensiones: CatalogoDTO[];
  estados: CatalogoDTO[];

  dataSource: MatTableDataSource<InformeFinalAsistenciaDetalleDTO>;
  @ViewChild('paginator') paginator: MatPaginator;
    

  informeFinalAsistencia: InformeFinalAsistenciaDTO;

  planAsistencia: PlanAsistenciaPostEgresoDTO;

  detallesEntrante: PlanAsistenciaPostEgresoDetalleDTO[] = [];

  displayedColumns: string[] = [
    'acciones',
    'area',
    'objetivoGeneral',
    'objetivoEspecifico',
    'actividades',
    'descripcionActividad',
    'logro',
    'dificultad',
  ];

  uuid_fp: string;

  fechasFormGroup = this.fb.group({
    fechaInicio: [new Date as Date, Validators.required],
    fechaFin: [new Date as Date, Validators.required],    
  })
  esVisualizar: boolean = false;


  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private catalogoService: CatalogoService,
    private informeFinalAsistenciaService: InformeFinalAsistenciaService,
    private dialogMensajeService: DialogMensajeService,
    private location: Location,
    public dialog: MatDialog,    
  ) {
    
  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    if (history.state.visualizar) {
      this.esVisualizar = true;
    }
    this.obtenerCatalogos().subscribe(result => {
      if (history.state.plan) {
        this.planAsistencia = history.state.plan;
      }

      if (history.state.informeFinal) {
        this.informeFinalAsistencia = history.state.informeFinal;
        this.informeFinalAsistencia.esEdicion = true;
        this.fechasFormGroup.controls['fechaInicio'].setValue(new Date(this.informeFinalAsistencia.fechaInicio));
        this.fechasFormGroup.controls['fechaFin'].setValue(new Date(this.informeFinalAsistencia.fechaFin));
        if (this.esVisualizar) {
          this.fechasFormGroup.disable(); 
        }
      } 
      
      else {
        this.informeFinalAsistencia = new InformeFinalAsistenciaDTO;

        this.detallesEntrante = this.planAsistencia.planDetalle;

        this.fechasFormGroup.controls['fechaInicio'].setValue(new Date(this.planAsistencia.fechaInicio));
        this.fechasFormGroup.controls['fechaFin'].setValue(new Date(this.planAsistencia.fechaFin));
        this.fechasFormGroup.controls['fechaInicio'].disable();
        this.fechasFormGroup.controls['fechaFin'].disable();
        for (let planDetalle of this.planAsistencia.planDetalle) {
          let detalle = new InformeFinalAsistenciaDetalleDTO;
          detalle.area = planDetalle.area;
          detalle.objetivoEspecifico = planDetalle.objetivoEspecifico;
          detalle.objetivoGeneral = planDetalle.objetivoGeneral;  
          detalle.actividades = planDetalle.actividades;        
          this.informeFinalAsistencia.detalle.push(detalle);
        }          
      }
      this.dataSource = new MatTableDataSource(this.informeFinalAsistencia.detalle);
      this.dataSource.paginator = this.paginator;     

    });
  }

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'PLAN_ASISTENCIA_POST_EGRESO_AREAS',  
      'ESTADOS_PLAN_ASISTENCIA',
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, ''));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.dimensiones = results[0]?.data;
        this.estados = results[1]?.data;
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  agregarFila() {
    const dialogRef = this.dialog.open(ModalInformeFinalAsistenciaComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensiones, detallePlan: this.detallesEntrante },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: InformeFinalAsistenciaDetalleDTO) => {
      if (result) {        
        this.informeFinalAsistencia.detalle.push(result);
        this.dataSource = new MatTableDataSource(this.informeFinalAsistencia.detalle);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  editarFila(fila: InformeFinalAsistenciaDetalleDTO, index: number) {
    const dialogRef = this.dialog.open(ModalInformeFinalAsistenciaComponent, {
      disableClose: true,
      data: { fila: fila, dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: InformeFinalAsistenciaDetalleDTO) => {
      if (result) {        
        this.informeFinalAsistencia.detalle[index] = result;
        this.dataSource = new MatTableDataSource(this.informeFinalAsistencia.detalle);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  guardarPlanAsistencia() {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'Se guardará el nuevo registro con la información ingresada.',
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            Object.assign(this.informeFinalAsistencia, this.fechasFormGroup.value);              
            this.informeFinalAsistencia.tokenFichaIdenticacion = this.uuid_fp;
            this.informeFinalAsistencia.tokenPlanAsistencia = this.planAsistencia.tokenIdentificador;
            this.informeFinalAsistencia.estado = this.estados.find(estado => estado.nemonico === 'ESTADO_PLAN_ASISTENCIA_ACTIVO');
            console.log(this.informeFinalAsistencia);
            this.informeFinalAsistenciaService.crearInforme(this.informeFinalAsistencia, '').subscribe({
              next: (response: RespuestaPorDefecto<InformeFinalAsistenciaDTO>) => {

                if (!response.exito) {
                  this.informeFinalAsistenciaService.checkError(response);

                  return;
                }
                this.location.back();                
              },
              error: (error: any) => {
                this.informeFinalAsistenciaService.checkError(error);
              }
            })
          }
        }
      }
    );
  }

  cancelar() {
    this.location.back();
  }

  onFechaManual(event: any, controlName: string) {
    const valorIngresado = event.target.value;
    if (valorIngresado) {
        const partes = valorIngresado.split('/');
        if (partes.length === 3) {
            const fechaConvertida = new Date(`${partes[2]}-${partes[1]}-${partes[0]}`);
            if (!isNaN(fechaConvertida.getTime())) {
                this.fechasFormGroup.patchValue({ [controlName]: fechaConvertida });
                this.fechasFormGroup.get(controlName)?.updateValueAndValidity();
                console.log(`Fecha manual válida (${controlName}):`, fechaConvertida);
            } else {
                console.warn(`Fecha ingresada no válida en ${controlName}`);
                this.fechasFormGroup.get(controlName)?.setErrors({ invalid: true });
            }
        }
    }
}

}
