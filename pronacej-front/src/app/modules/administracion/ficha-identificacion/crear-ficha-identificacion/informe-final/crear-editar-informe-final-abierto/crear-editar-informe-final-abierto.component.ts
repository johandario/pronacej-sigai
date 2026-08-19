import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { InformeFinalAbiertoDTO, InformeFinalAbiertoMedidasDTO } from 'app/core/model/both/informeFinalAbiertoDTO.model';
import { ModalCrearInfFinalAbiertoComponent } from './modal-crear-inf-final-abierto/modal-crear-inf-final-abierto.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatDialog } from '@angular/material/dialog';
import { InformeFinalAbiertoService } from 'app/modules/seguridad/services/informeFinalAbierto.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CommonModule, Location } from '@angular/common';

@Component({
  selector: 'app-crear-editar-informe-final-abierto',
  standalone: true,
  imports: [
    MatStepperModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    CommonModule
  ],
  templateUrl: './crear-editar-informe-final-abierto.component.html',
  styleUrl: './crear-editar-informe-final-abierto.component.scss'
})
export class CrearEditarInformeFinalAbiertoComponent implements OnInit {

  dataSource: MatTableDataSource<any> = new MatTableDataSource();
  @ViewChild('paginator') paginator: MatPaginator;

  uuid_fp: string;

  displayedColumns: string[] = [
    'acciones',
    'medidas',
    'accion',
    'objetivo',
    'analisis',
  ];

  informeFinal: InformeFinalAbiertoDTO = new InformeFinalAbiertoDTO;
  esVisualizacion: boolean = false;

  evaluacionForm = this.fb.group({
    fortalecimientoDerechos: ['', Validators.required],
    area: ['', Validators.required],
    fortalecimientoFamiliar: ['', Validators.required],
    intervencion: ['', Validators.required],
    enfoque: ['', Validators.required],
    cultural: ['', Validators.required],
    responsabilidad: ['', Validators.required],
    conciencia: ['', Validators.required],
  });

  // evaluacionForm = this.fb.group({
  //   fortalecimientoDerechos: [null, Validators.required],
  //   area: [null, Validators.required],
  //   fortalecimientoFamiliar: [null, Validators.required],
  //   intervencion: [null, Validators.required],
  //   enfoque: [null, Validators.required],
  //   cultural: [null, Validators.required],
  //   responsabilidad: [null, Validators.required],
  //   conciencia: [null],    
  // });

  otrosForm = this.fb.group({
    valoracionRiesgo: ['', Validators.required],
    conclusionesRecomendaciones: ['', Validators.required],
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    public dialog: MatDialog,
    private informeFinalAbiertoService: InformeFinalAbiertoService,
    private _location: Location,
  ) {

  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    if (history.state.informe) {
      this.informeFinal = history.state.informe;

      if (this.informeFinal.completado) {
        this.esVisualizacion = true;
        this.evaluacionForm.disable();
        this.otrosForm.disable();
      }

      this.informeFinal.esEdicion = true;
      this.evaluacionForm.patchValue(this.informeFinal);
      this.otrosForm.patchValue(this.informeFinal);
      this.dataSource = new MatTableDataSource(this.informeFinal.medidasList);
    }
  }

  regresar() {
    this._location.back();
  }

  guardar() {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      'Se guardará el nuevo registro con la información ingresada.',
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            Object.assign(this.informeFinal, this.evaluacionForm.value);
            Object.assign(this.informeFinal, this.otrosForm.value);
            this.informeFinal.tokenFichaIdenticacion = this.uuid_fp;
            this.informeFinal.medidasList = this.dataSource.data;
            this.informeFinal.completado = true;

            this.informeFinalAbiertoService.crearInforme(this.informeFinal, '').subscribe({
              next: (response: RespuestaPorDefecto<InformeFinalAbiertoDTO>) => {

                if (!response.exito) {
                  this.dialogMensajeService.mensajeError("Hubo un problema al guardar el informe. " + response.mensaje);
                  return;
                }

                this.dialogMensajeService.mensajeExitoso(
                  'Guardar',
                  'Informe guardado correctamente.'
                ).afterClosed().subscribe(() => {
                  this.regresar();
                });

              },
              error: (error: any) => {
                this.informeFinalAbiertoService.checkError(error);
              }
            })
          }
        }
      }
    );
  }

  guardarBorrador() {

    Object.assign(this.informeFinal, this.evaluacionForm.value);
    Object.assign(this.informeFinal, this.otrosForm.value);
    this.informeFinal.tokenFichaIdenticacion = this.uuid_fp;
    this.informeFinal.medidasList = this.dataSource.data;
    this.informeFinal.completado = false;

    this.informeFinalAbiertoService.crearInforme(this.informeFinal, '').subscribe({
      next: (response: RespuestaPorDefecto<InformeFinalAbiertoDTO>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError("Hubo un problema al guardar el borrador de informe. " + response.mensaje);
          return;
        }

        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          'Borrador de informe guardado correctamente.'
        ).afterClosed().subscribe(() => {
          this.regresar();
        });

      },
      error: (error: any) => {
        this.informeFinalAbiertoService.checkError(error);
      }
    })
  }


  agregarFila() {
    const dialogRef = this.dialog.open(ModalCrearInfFinalAbiertoComponent, {
      disableClose: true,
      data: { fila: null },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: InformeFinalAbiertoMedidasDTO) => {
      if (result) {
        this.informeFinal.medidasList.push(result);
        this.dataSource = new MatTableDataSource(this.informeFinal.medidasList);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  editarFila(fila: InformeFinalAbiertoMedidasDTO, index: number) {
    const dialogRef = this.dialog.open(ModalCrearInfFinalAbiertoComponent, {
      disableClose: true,
      data: { fila: fila },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: InformeFinalAbiertoMedidasDTO) => {
      if (result) {
        this.informeFinal.medidasList[index] = result;
        this.dataSource = new MatTableDataSource(this.informeFinal.medidasList);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  verFila(fila: InformeFinalAbiertoMedidasDTO, index: number) {
    const dialogRef = this.dialog.open(ModalCrearInfFinalAbiertoComponent, {
      disableClose: true,
      data: { fila: fila, esVisualizacion: true },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: InformeFinalAbiertoMedidasDTO) => {
      if (result) {
        this.informeFinal.medidasList[index] = result;
        this.dataSource = new MatTableDataSource(this.informeFinal.medidasList);
        this.dataSource.paginator = this.paginator;
      }
    })
  }
}
