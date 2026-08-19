import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, MatPaginatorIntl, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { EvaluacionMedicaDTO } from 'app/core/model/both/EJE/seguimiento-medico/EvaluacionMedicaDTO.model';
import { CustomPaginatorIntl } from 'app/core/services/custom-paginator-intl.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EvaluacionMedicaService } from '../evaluacion-medica.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import moment from 'moment';
import { SnackbarService } from 'app/core/services/snackbar.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-seguimiento-evaluacion-medica',
  standalone: true,
  imports: [
    MatProgressBarModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    CommonModule
  ],
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl },
  ],
  templateUrl: './seguimiento-evaluacion-medica.component.html',
  styleUrl: './seguimiento-evaluacion-medica.component.scss'
})
export class SeguimientoEvaluacionMedicaComponent implements OnInit {
  private permisosService = inject(PermisoRolUsuarioService);
  permisos: Record<string, Record<string, boolean>> = {};

  constructor(
    public dialog: MatDialog,
    private router: Router, private route: ActivatedRoute,
    private _evaluacionMedicaService: EvaluacionMedicaService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly _fuseConfirmationService: FuseConfirmationService,
    private readonly customSnackbar: SnackbarService,
    private readonly accionesSheet: MatBottomSheet,
    private readonly dialogMensajeService: DialogMensajeService,
  ) {}

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tokenFichaMedica: string = "";

  isLoading: boolean = true;
  searchInputControl: UntypedFormControl = new UntypedFormControl();

  keyLabelsTable: any = {
    acciones: "Acciones",
    numReferencia: "Número de referencia",
    fecha: "Fecha",
  };
  
  evaluaciones: EvaluacionMedicaDTO[] = [];
  datasource = new MatTableDataSource<EvaluacionMedicaDTO>([]);


  async ngOnInit(): Promise<void> {
    //await this.obtenerEvaluacionMedica();
    await this.obtenerPermisos();
    this._evaluacionMedicaService.fichaMedica$.subscribe((ficha) => {
      if (ficha) {
        this.tokenFichaMedica = ficha;
        this.obtenerEvaluacionMedica();  // Solo llama a este método cuando la ficha médica está disponible
      }
    });
  }

  /*
  * Método para dirigirse a la pantalla de creación de evaluación médica
  */
  abrirFormulario(){
    this.router.navigate(['../crear'], { relativeTo: this.route });
  }

  editarEvaluacion(token?: string) {
    this.router.navigate(['../crear'], {
      relativeTo: this.route,
      queryParams: { token: token || null }
    });
  }

  verEvaluacion(token?: string) {
    this.router.navigate(['../visualizar'], {
      relativeTo: this.route,
      queryParams: { token: token || null }
    });
  }
  

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }
  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerEvaluacionMedica();
  }

  async obtenerEvaluacionMedica() {
    this.isLoading = true;
    this.tokenFichaMedica = this._evaluacionMedicaService.getToken();
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.tokenFichaMedica;
    this._evaluacionMedicaService.getEvaluacionMedicaByFichaMedica(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EvaluacionMedicaDTO>>) => {

          if (!response.exito) {
            this._evaluacionMedicaService.checkError(response);
            return;
          }
          this.evaluaciones = response.data.data;
          this.datasource.data = this.evaluaciones;
          this.changeDetector.detectChanges();
          this.totalItems = response.data.totalItems;
          console.log(this.evaluaciones);
          
        },
        error: (error: any) => {
          this._evaluacionMedicaService.checkError(error);
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      }
    );
  }

  eliminarEvaluacion(evaluacion: EvaluacionMedicaDTO){
    const confirmation = this._fuseConfirmationService.open({
      title: 'Eliminar registro',
      message:
          '¿Estás seguro de eliminar este registro?',
      actions: {
          confirm: {
              label: 'Eliminar',
          },
          cancel: {
              label: 'Cancelar'
          }
      },
  });

  confirmation.afterClosed().subscribe((result) => {
      if (result === 'confirmed') {
          this._evaluacionMedicaService.deleteEvaluacionMedica(evaluacion).subscribe({
              next: (response) => {
                  this.obtenerEvaluacionMedica();

                  this.customSnackbar.show('Evaluación eliminada con exito', 'Cerrar', "success");
              },
              error: (err) => {
                  this.customSnackbar.show('No se pudo eliminar', 'Cerrar', "error");
              }
          });
      }
  });
  }

  getFormatedDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  obtenerPermisos() {    
      const acciones = [
          etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR,
          etiquetasModel.ACCIONES_MENU_PERMISO_ELIMINAR
      ];

      const modulos = [
          'MENU_HC_PLAN_ATENCION_ADOLESCENTE'          
      ];

      modulos.forEach(modulo => {
          const [editar, eliminar] =
          this.permisosService.hasPermissionArray(modulo, ...acciones);

          this.permisos[modulo] = {
              editar,
              eliminar
          };
      });   
  }

  tienePermiso(modulo: string, accion: string): boolean {
      return this.permisos[modulo]?.[accion] ?? false;
  }
}
