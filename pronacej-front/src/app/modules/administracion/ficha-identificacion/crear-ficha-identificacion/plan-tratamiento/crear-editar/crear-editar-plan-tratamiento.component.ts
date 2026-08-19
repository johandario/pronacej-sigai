import { Component, OnInit, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PlanTratamientoIndDTO, PlanTratamientoIndEspecifDTO, PlanTratamientoIndIntervDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { ModalEditaIntervComponent } from '../crear-editar-pti-cerrado/modal-edita-interv/modal-edita-interv.component';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CrearEditarPtiAbiertoComponent } from '../crear-editar-pti-abierto/crear-editar-pti-abierto.component';
import { CrearEditarPtiCerradoComponent } from '../crear-editar-pti-cerrado/crear-editar-pti-cerrado.component';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { catchError, concatMap, iif, Observable, tap, throwError } from 'rxjs';

@Component({
  selector: 'app-crear-editar-plan-tratamiento',
  standalone: true,
  imports: [
    MatTabsModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatRadioModule,
    MatSlideToggleModule,
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatCardModule, 
    CrearEditarPtiAbiertoComponent,
    CrearEditarPtiCerradoComponent
  ],
  templateUrl: './crear-editar-plan-tratamiento.component.html',
  styleUrl: './crear-editar-plan-tratamiento.component.scss'
})
export class CrearEditarPlanTratamientoComponent implements OnInit {
  estadoEditar: boolean = false;
  estadoVisualizar: boolean = false;

  ingreso: FichaIngresoDTO;
  tipoCentro: string = '';
  planTratamiento: PlanTratamientoIndDTO;

  constructor(
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private fichaIngresoService: FichaIngresoService,
    private planTratamientoService: PlanTratamientoService,
    public dialog: MatDialog,
    public router: Router,
  ) {}  

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {      
    const uuid_fp = this.route.snapshot.params['uuid_fp'];

    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerParametrosDeConsulta().pipe(
      concatMap(() =>
        iif(
          () => this.estadoEditar, 
          this.obtenerPlanTratamiento(),          
          this.obtenerFichaIngresoValida(uuid_fp),
        )
      )
    ).subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });
  }

  obtenerParametrosDeConsulta(): Observable<any> {
      return new Observable((observer) => {
        this.route.queryParams.subscribe((params) => {
          const numDoc = params['numDoc'];
          if (numDoc) {
            const state = params['state'];
            if (state) {
              this.estadoVisualizar = true;               
            }
            this.estadoEditar = true;        
          } 
          observer.next();
          observer.complete();
        });
      });
  }

  obtenerPlanTratamiento(): Observable<any> {
    return this.planTratamientoService.obtenerPlanTratamientoPorId(this.route.snapshot.queryParams['numDoc'], '').pipe(
      tap((item) => {
        this.planTratamiento = item.data;    
        if (this.planTratamiento) {
          this.tipoCentro = this.planTratamiento.tipoCentro;
        }
      }),
      catchError(err => {
        this.planTratamientoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }  

  obtenerFichaIngresoValida(tokenFichaIdentificacion: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;
    
    return this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, '').pipe(
      tap((response) => {
        this.ingreso = response.data;
        console.log(this.ingreso);
        if (this.ingreso && this.ingreso?.centro.nombre.includes('CJDR')) {
          this.tipoCentro = 'CJDR';
        } else if (this.ingreso && this.ingreso?.centro.nombre.includes('SOA')) {
          this.tipoCentro = 'SOA';
        }
      }),
      catchError(err => {
        this.fichaIngresoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

}
