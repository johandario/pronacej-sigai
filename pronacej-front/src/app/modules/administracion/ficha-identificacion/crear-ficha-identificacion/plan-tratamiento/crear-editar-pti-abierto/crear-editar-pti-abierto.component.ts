import { Component } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PtiAbiertoLibertadComponent } from './pti-abierto-libertad/pti-abierto-libertad.component';
import { PtiAbiertoComunidadComponent } from './pti-abierto-comunidad/pti-abierto-comunidad.component';
import { PtiAbiertoAmonestacionComponent } from './pti-abierto-amonestacion/pti-abierto-amonestacion.component';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { ExpedienteMatrizDetalleDTO, ExpedienteMatrizDTO, ExpedienteMatrizMedidaDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { environment } from 'environments/environment';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { provideNativeDateAdapter } from '@angular/material/core';
import { catchError, concatMap, iif, Observable, tap, throwError } from 'rxjs';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { PlanTratamientoIndDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-crear-editar-pti-abierto',
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
    RouterLink,
    PtiAbiertoLibertadComponent,
    PtiAbiertoComunidadComponent,
    PtiAbiertoAmonestacionComponent
  ],  
  templateUrl: './crear-editar-pti-abierto.component.html',
  styleUrl: './crear-editar-pti-abierto.component.scss'
})
export class CrearEditarPtiAbiertoComponent {
  estadoEditar: boolean;
  estadoVisualizar: boolean = false;
 
  listaExpedientes: ExpedienteMatrizDTO[] = [];
  expedienteSeleccionado: ExpedienteMatrizDTO;
  detalleSeleccionado: ExpedienteMatrizDetalleDTO;
  medidaSeleccionada: ExpedienteMatrizMedidaDTO;

  planTratamiento: PlanTratamientoIndDTO;

  uuid_fp!: string;

  tipoAbierto: string = '';

  expedienteSeleccionFormGroup = this.fb.group({
      expediente: [null as ExpedienteMatrizDTO, Validators.required],
      detalle: [null as ExpedienteMatrizDetalleDTO, Validators.required],
      medida: [null as ExpedienteMatrizMedidaDTO, Validators.required],      
    })
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_PTI;
  

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private router: Router,
    public funcionesUtils: FuncionesUtils,
    private expedienteMatrizService: ExpedienteMatrizService, 
    private dialogMensajeService: DialogMensajeService,  
    private planTratamientoService: PlanTratamientoService,
    public dialog: MatDialog,
  ) {}  

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp']; //Obtener token de Ficha de Identificación
    this.cargarDatos(); 
  }

  cargarDatos(): void {      

    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerParametrosDeConsulta().pipe(      
      concatMap(() =>
        iif(
          () => this.estadoEditar, 
          this.obtenerPlanTratamiento(),          
          this.obtenerExpedientes(),
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
      return this.planTratamientoService.obtenerPlanTratamientoPorId(this.route.snapshot.queryParams['numDoc'], this.nemonicoMenu).pipe(
        tap((item) => {
          this.planTratamiento = item.data;    
          if (this.planTratamiento) {
            this.tipoAbierto = this.planTratamiento.tipoAbierto;
          }
        }),
        catchError(err => {
          this.planTratamientoService.checkError(err);
          return throwError(() => err); 
        })
      );
    }  

  obtenerExpedientes() : Observable<any> {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = 25;
    paginacionRequest.page = 0;
    paginacionRequest.tokenIdentificador = this.uuid_fp;
    
    return this.expedienteMatrizService.obtenerExpedientesValidos(paginacionRequest, this.nemonicoMenu).pipe(
      tap((item) => {
        this.listaExpedientes = item.data.data;        
      }),
      catchError(err => {
        this.expedienteMatrizService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  seleccionarExpediente(event: any) {
    this.expedienteSeleccionado = event.value;
  }

  seleccionarDetalle(event: any) {
    this.detalleSeleccionado = event.value;
  }

  seleccionarMedida(event: any) {
    this.medidaSeleccionada = event.value;
    const tipo = this.medidaSeleccionada.medida.nemonico;
    if (tipo.includes('LIBERTAD')) {
      this.tipoAbierto = 'Libertad Restringida/Libertad Asistida';
    } else if (tipo.includes('COMUNIDAD')) {
      this.tipoAbierto = 'Prestación de Servicios a la Comunidad';
    } else if (tipo.includes('AMONESTACION')) {
      this.tipoAbierto = 'Amonestación o Semilibertad';
    }
  }
  
}
