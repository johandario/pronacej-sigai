import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { EvaluacionMedicaService } from '../evaluacion-medica.service';
import { ActivatedRoute, Router } from '@angular/router';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { EvaluacionMedicaDTO } from 'app/core/model/both/EJE/seguimiento-medico/EvaluacionMedicaDTO.model';
import { DiagnosticoDTO } from 'app/core/model/both/EJE/seguimiento-medico/DiagnosticoDTO.model';
import { EstadoNutricionalDTO } from 'app/core/model/both/EJE/seguimiento-medico/EstadoNutricionalDTO.model';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import moment from 'moment';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-ver-evaluacion-medica',
  standalone: true,
  imports: [
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './ver-evaluacion-medica.component.html',
  styleUrl: './ver-evaluacion-medica.component.scss'
})
export class VerEvaluacionMedicaComponent implements OnInit {

  tokenEdicion: string;
  isLoading: boolean;
  evaluacionMedica: EvaluacionMedicaDTO;

  pageDiagnostico = 0;
  listSizeDiagnostico = [5, 10, 15, 20];
  sizeDiagnostico = this.listSizeDiagnostico[0];
  totalItemsDiagnostico = 0;

  pageEstadoNutricional = 0;
  listSizeEstadoNutricional = [5, 10, 15, 20];
  sizeEstadoNutricional = this.listSizeEstadoNutricional[0];
  totalItemsEstadoNutricional = 0;

  diagnosticos: DiagnosticoDTO[] = [];
  estadosNutricion: EstadoNutricionalDTO[] = [];
  dataSourceDiagnostico = new MatTableDataSource<DiagnosticoDTO>([]);
  dataSourceEstadoNutricional = new MatTableDataSource<EstadoNutricionalDTO>([]);

  keyLabelsDiagnostico: any = {
    codDiagnostico: "Código diagnostico",
    diagnostico: "Diagnostico",
    tipoDiagnostico: 'Tipo',
    tratamiento: 'Tratamiento',
    indicaciones: 'Indicaciones',
    examenes: 'Examenes',
    medicamentos: 'Medicamentos'
  };

  keyLabelsNutricion: any = {
    criterio: "Criterio",
    grado: "Grado",
  };

  constructor(
    private _evaluacionMedicaService: EvaluacionMedicaService,
    private readonly changeDetector: ChangeDetectorRef,
    private router: Router, private route: ActivatedRoute,
  ){}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.tokenEdicion = params['token'];
      if (this.tokenEdicion) {
        // Llama aquí a una función para cargar los datos del formulario para edición
        this.obtenerEvaluacionMedica(this.tokenEdicion);
      }
    });
  }


  obtenerEvaluacionMedica(tokenId: string) {
    this.isLoading = true;
    this._evaluacionMedicaService.getEvaluacionMedicaByTokenId(tokenId).subscribe(
      {
        next: (response: RespuestaPorDefecto<EvaluacionMedicaDTO>) => {

          if (!response.exito) {
            this._evaluacionMedicaService.checkError(response);
            return;
          }
          this.evaluacionMedica = response.data;

          if(this.evaluacionMedica){
            this.changeDetector.detectChanges();
          }
        },
        error: (error: any) => {
          this._evaluacionMedicaService.checkError(error);
          this.isLoading = false;
        },
        complete: () => {
          this.obtenerDiagnosticos();
          this.obtenerEstadoNutricional();
          this.isLoading = false;
        }
      }
    );
  }

  async obtenerDiagnosticos() {
    this.isLoading = true;
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.sizeDiagnostico;
    paginacionRequest.page = this.pageDiagnostico;
    paginacionRequest.tokenIdentificador = this.evaluacionMedica.tokenIdentificador;

    this._evaluacionMedicaService.getDiagnosticosByEvaluacionMedica(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<DiagnosticoDTO>>) => {

          if (!response.exito) {
            this._evaluacionMedicaService.checkError(response);
            return;
          }

          this.diagnosticos = response.data.data;
          console.log(this.diagnosticos);
          this.dataSourceDiagnostico.data = this.diagnosticos;
          this.isLoading = false;
          this.changeDetector.detectChanges();
          this.totalItemsDiagnostico = response.data.totalItems;
        },
        error: (error: any) => {
          this._evaluacionMedicaService.checkError(error);
          this.isLoading = false;
        }
      }
    );
  }

  async obtenerEstadoNutricional() {
    this.isLoading = true;
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.sizeEstadoNutricional;
    paginacionRequest.page = this.pageEstadoNutricional;
    paginacionRequest.tokenIdentificador = this.evaluacionMedica.tokenIdentificador;

    this._evaluacionMedicaService.getEstadoNutricionalByEvaluacionMedica(paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<EstadoNutricionalDTO>>) => {

          if (!response.exito) {
            this._evaluacionMedicaService.checkError(response);
            return;
          }

          this.estadosNutricion = response.data.data;
          this.dataSourceEstadoNutricional.data = this.estadosNutricion;
          this.isLoading = false;
          this.changeDetector.detectChanges();
          this.totalItemsEstadoNutricional = response.data.totalItems;
        },
        error: (error: any) => {
          this._evaluacionMedicaService.checkError(error);
          this.isLoading = false;
        }
      }
    );
  }



  getKeysDiagnostico() {
    return Object.keys(this.keyLabelsDiagnostico);
  }

  getKeysNutricion() {
    return Object.keys(this.keyLabelsNutricion);
  }


  handlePageEventDiagnostico(pageEvent: PageEvent) {
    this.sizeDiagnostico = pageEvent.pageSize;
    this.pageDiagnostico = pageEvent.pageIndex;
    this.obtenerDiagnosticos();
  }

  handlePageEventNutricion(pageEvent: PageEvent) {
    this.sizeEstadoNutricional = pageEvent.pageSize;
    this.pageEstadoNutricional = pageEvent.pageIndex;
    this.obtenerEstadoNutricional();
  }

  getFormatedDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  atras() {
    this.router.navigate(['../seguimiento'], { relativeTo: this.route });
  }
}
