import { Component, Input, OnInit, ViewChild } from '@angular/core';
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
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PlanTratamientoIndDTO, PlanTratamientoIndEspecifDTO, PlanTratamientoIndIntervDTO } from 'app/core/model/both/planTratamientoIndDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PlanTratamientoService } from 'app/modules/seguridad/services/planTratamiento.service';
import { ModalEditaIntervAbiertoComponent } from '../modal-edita-interv-abierto/modal-edita-interv-abierto.component';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { catchError, concatMap, elementAt, forkJoin, iif, Observable, of, tap, throwError } from 'rxjs';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { CommonModule, Location } from '@angular/common';
import { ExpedienteMatrizDetalleDTO } from 'app/core/model/both/expedienteMatrizDTO.model';

@Component({
  selector: 'app-pti-abierto-libertad',
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
      RouterLink,
      MatTooltipModule,
      MatPaginatorModule,
      MatCardModule,
      CommonModule
  ],
  templateUrl: './pti-abierto-libertad.component.html',
  styleUrl: './pti-abierto-libertad.component.scss'
})
export class PtiAbiertoLibertadComponent {
  @Input() detalleEntrante: ExpedienteMatrizDetalleDTO = null;

  estadoEditar: boolean;
  estadoVisualizar: boolean = false;
  estadoActivo: boolean = false;
  estadoFinalizado: boolean = false;
  selectedIndex: number = 0;
  
  columnasMatrizPti: string[] = [
    'acciones', 
    'dimension', 
    'objetivo', 
    'actividadPrograma', 
    'tiempoEstimado', 
    'modalidad', 
    'frecuencia', 
    'equipoResponsable'];
    
  columnasControlAsistencia: string[] = ['acciones', 'atencion', 'horario', 'lugar'];

  columnasMedidas: string[] = [
    'acciones', 
    'dimension', 
    'objetivo', 
    'actividadPrograma', 
    'equipoResponsable',
    'tiempoEstimado', 
    'lugar',
    'numAtencionGrupal',
    'modalidad', 
    'frecuencia', 
  ];

  dataSourceMatrizPti: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceControlAsistencia: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceMedidas: MatTableDataSource<PlanTratamientoIndIntervDTO>;

  ingresoPlanTratamientoFormGroup = this.fb.group({
    instTecnicas: [null] 
  })

  filaEditada: PlanTratamientoIndIntervDTO = null;

  dimensiones: CatalogoDTO[];
  modalidades: CatalogoDTO[];
  componentesMatriz: CatalogoDTO[];
  controles: CatalogoDTO[];
  frecuencias: CatalogoDTO[];
  estados: CatalogoDTO[];

  planTratamiento: PlanTratamientoIndDTO = new PlanTratamientoIndDTO;

  uuid_fp: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_PTI;
  

  @ViewChild('paginatorMatrizPti') paginatorMatrizPti: MatPaginator;
  @ViewChild('paginatorControlAsistencia') paginatorControlAsistencia: MatPaginator;
  @ViewChild('paginatorMedidas') paginatorMedidas: MatPaginator;


  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private router: Router,
    private planTratamientoService: PlanTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    public dialog: MatDialog,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private location: Location
  ) { }  

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    
    this.cargarDatos();
  }

  cargarDatos(): void {      
      this.uuid_fp = this.route.snapshot.params['uuid_fp']; //Obtener token de Ficha de Identificación
  
      const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');
  
      this.obtenerIndiceTabs().pipe(
        concatMap(() => this.obtenerCatalogos()),
        concatMap(() => this.obtenerParametrosDeConsulta()),
        concatMap(() =>
          iif(
            () => this.estadoEditar, 
            this.obtenerPlanTratamiento(),          
            of(null)
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

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'DIMENSIONES_PLAN_TRATAMIENTO_SOA',
      'PTI_SOA_MODALIDAD',
      'PTI_SOA_MATRIZ_COMPONENTES',    
      'PTI_SOA_CONTROL_ASISTENCIA',    
      'PTI_SOA_FRECUENCIA',
      'ESTADOS_PTI'
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.dimensiones = results[0]?.data; 
        this.modalidades = results[1]?.data; 
        this.componentesMatriz = results[2]?.data; 
        this.controles = results[3]?.data; 
        this.frecuencias = results[4]?.data; 
        this.estados = results[5]?.data; 
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
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
        } else {
          this.planTratamiento = new PlanTratamientoIndDTO(); 
          this.planTratamiento.especFactores = [];
          this.planTratamiento.intervObjetivos = [];
          this.planTratamiento.intervNoCriminogenos = [];
          this.planTratamiento.intervDiferenciada = [];
          this.dataSourceMatrizPti = new MatTableDataSource(this.planTratamiento.intervObjetivos);
          this.dataSourceControlAsistencia = new MatTableDataSource(this.planTratamiento.intervNoCriminogenos);
          this.dataSourceMedidas = new MatTableDataSource(this.planTratamiento.intervDiferenciada);
          this.dimensiones.forEach(dimension => {
            let factor = new PlanTratamientoIndEspecifDTO;
            factor.dimension = dimension;
            factor.comentario = '';
            this.planTratamiento.especFactores.push(factor);
          });
          // this.componentesMatriz.forEach(componente => {
          //   let registro = new PlanTratamientoIndIntervDTO;
          //   registro.dimension = componente;
          //   this.planTratamiento.intervObjetivos.push(registro);
          // })   
          // this.controles.forEach(control => {
          //   let registro = new PlanTratamientoIndIntervDTO;
          //   registro.dimension = control;
          //   this.planTratamiento.intervNoCriminogenos.push(registro);
          // })         
          this.dataSourceMatrizPti.paginator = this.paginatorMatrizPti;
          this.dataSourceControlAsistencia.paginator = this.paginatorControlAsistencia;

          if (this.detalleEntrante) {
            for (let medidaAccesoria of this.detalleEntrante.medidasAccesorias) {
              this.planTratamiento.medidasAccesorias.push(medidaAccesoria.medida);
            }
          }

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
        this.ingresoPlanTratamientoFormGroup.patchValue(this.planTratamiento);   
        this.planTratamiento.esEdicion = true;
        this.dataSourceMatrizPti = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceControlAsistencia = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceMedidas = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);

        this.dataSourceMatrizPti.paginator = this.paginatorMatrizPti;
        this.dataSourceControlAsistencia.paginator = this.paginatorControlAsistencia;
        this.dataSourceMedidas.paginator = this.paginatorMedidas;  

        if (this.planTratamiento.estado.nemonico === etiquetasModel.NEMONICO_ESTADO_PTI_ACTIVO) {
          this.estadoActivo = true;
        } else if (this.planTratamiento.estado.nemonico === etiquetasModel.NEMONICO_ESTADO_PTI_FINALIZADO) {
          this.estadoFinalizado = true;
        }

      }),
      catchError(err => {
        this.planTratamientoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }  

  aniadirFilaMatrizPti() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: { dimensiones: this.componentesMatriz, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-pti' },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos.unshift(result);
        this.dataSourceMatrizPti = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceMatrizPti.paginator = this.paginatorMatrizPti;
      }
    })
  } 

  editarFilaMatrizPti(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {fila: fila, dimensiones: this.componentesMatriz, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-pti'},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos[index] = result;
        this.dataSourceMatrizPti = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceMatrizPti.paginator = this.paginatorMatrizPti;
      }
    })
  }  

  mostrarItemMatrizPti(fila: PlanTratamientoIndIntervDTO) {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.componentesMatriz, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-pti', visualizar: true},
      width: '600px'
    }); 
  }  

  eliminarItemMatrizPti(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervObjetivos.splice(index, 1);
            this.dataSourceMatrizPti = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
            this.dataSourceMatrizPti.paginator = this.paginatorMatrizPti;
          }
        }
      }
    )        
  }

  aniadirFilaControlAsistencia() {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: { dimensiones: this.controles, tipo: 'control-asistencia' },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervNoCriminogenos.unshift(result);
        this.dataSourceControlAsistencia = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceControlAsistencia.paginator = this.paginatorControlAsistencia;
      }
    })
  } 

  editarFilaControlAsistencia(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {fila: fila, dimensiones: this.controles, tipo: 'control-asistencia'},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervNoCriminogenos[index] = result;
        this.dataSourceControlAsistencia = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceControlAsistencia.paginator = this.paginatorControlAsistencia;
      }
    })
  }  

  eliminarFilaControlAsistencia(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervNoCriminogenos.splice(index, 1);
            this.dataSourceControlAsistencia = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
            this.dataSourceControlAsistencia.paginator = this.paginatorControlAsistencia;
          }
        }
      }
    )    
  }

  aniadirFilaMedidas() {    
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: { dimensiones: this.planTratamiento.medidasAccesorias, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-cumplimiento' },
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervDiferenciada.unshift(result);
        this.dataSourceMedidas = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceMedidas.paginator = this.paginatorMedidas;
      }
    })
  } 

  editarFilaMedidas(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      disableClose: true,
      data: {fila: fila, dimensiones: this.planTratamiento.medidasAccesorias, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-cumplimiento'},
      width: '600px'
    }); 

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervDiferenciada[index] = result;
        this.dataSourceMedidas = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceMedidas.paginator = this.paginatorMedidas;
      }
    })
  }  

  mostrarFilaMedidas(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervAbiertoComponent, {
      data: {fila: fila, dimensiones: this.planTratamiento.medidasAccesorias, frecuencias: this.frecuencias, modalidades: this.modalidades, tipo: 'matriz-cumplimiento', visualizar: true},
      width: '600px'
    }); 
  }   

  eliminarItemMedidas(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervDiferenciada.splice(index, 1);
            this.dataSourceMedidas = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);
            this.dataSourceMedidas.paginator = this.paginatorMedidas;
          }
        }
      }
    )   
   
  }  

  asignarIndice(event: any) {
    this.selectedIndex = event;

    const queryParams: Params = { tabIndex: event };
    
    this.router.navigate(
      [], 
      {
        relativeTo: this.route,
        queryParams, 
        queryParamsHandling: 'merge',
      }
    );
  }

  generarPDF() {
    this.planTratamiento.instTecnicas = this.ingresoPlanTratamientoFormGroup.controls['instTecnicas'].value;

    // TABLA EVALUACIÓN
    let registrosTablaEvaluacion: any[] = [];
    for (let item of this.planTratamiento.especFactores) {
      let elemento =  {
        Nombre: item.dimension.nombre,
        Detalle: item.comentario
      }
      registrosTablaEvaluacion.push(elemento);
    }

    let tablaEvaluacion = new TablaPlantilla();
    tablaEvaluacion.encabezados = ['Nombre', 'Detalle'];
    tablaEvaluacion.filas = registrosTablaEvaluacion;

    // TABLA MATRIZ PTI
    let registrosTablaMatrizPti: any[] = [];
    for (let item of this.planTratamiento.intervObjetivos) {
      let elemento =  {
        Componentes: item.dimension.nombre,
        Objetivo: item.objetivo,
        Actividades_Programas: item.actividadPrograma,
        Periodo_ejecucion: item.tiempoEstimado,
        Modalidad: item.modalidad.nombre,
        Frecuencia: item.frecuencia.nombre,
        Responsable: item.equipoResponsable,
      }
      registrosTablaMatrizPti.push(elemento);
    }

    let tablaMatrizPti = new TablaPlantilla();
    tablaMatrizPti.encabezados = ['Componentes', 'Objetivo', 'Actividades_Programas', 'Periodo_ejecucion', 'Modalidad', 'Frecuencia', 'Responsable'];
    tablaMatrizPti.filas = registrosTablaMatrizPti;

    // TABLA CONTROL ACTIVIDADES
    let registrosTablaControlActividades: any[] = [];
    for (let item of this.planTratamiento.intervNoCriminogenos) {
      let elemento =  {
        Atencion: item.dimension.nombre,
        Horario: item.tiempoEstimado,
        Lugar: item.lugar,        
      }
      registrosTablaControlActividades.push(elemento);
    }

    let tablaControlActividades = new TablaPlantilla();
    tablaControlActividades.encabezados = ['Atencion', 'Horario', 'Lugar'];
    tablaControlActividades.filas = registrosTablaControlActividades;

    // TABLA CUMPLIMIENTO MEDIDAS ACCESORIAS
    let registrosTablaMedidasAccesorias: any[] = [];
    for (let item of this.planTratamiento.intervDiferenciada) {
      let elemento =  {
        Medida_accesoria: item.dimension.nombre,
        Objetivo: item.objetivo,
        Actividades_Programas: item.actividadPrograma,
        Responsable: item.equipoResponsable,
        Periodo_ejecucion: item.tiempoEstimado,
        Lugar: item.lugar,
        Sesiones: item.numAtencionGrupal,
        Modalidad: item.modalidad.nombre,
        Frecuencia: item.frecuencia.nombre,
      }
      registrosTablaMedidasAccesorias.push(elemento);
    }

    let tablaMedidasAccesorias = new TablaPlantilla();
    tablaMedidasAccesorias.encabezados = ['Medida_accesoria', 'Objetivo', 'Actividades_Programas', 'Responsable', 'Periodo_ejecucion', 'Lugar', 'Sesiones', 'Modalidad', 'Frecuencia'];
    tablaMedidasAccesorias.filas = registrosTablaMedidasAccesorias;


    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_PTI_ABIERTO_LIBERTAD;
    request.variables = {
      "[NOMBRE-ADOLESCENTE]": '',
      "[EDAD-ADOLESCENTE]": '',
      "[DNI-ADOLESCENTE]": '',
      "[TELEFONO-ADOLESCENTE]": '',
      "[DIRECCION-ADOLESCENTE]": '',
      "[JUZGADO-PROCEDENCIA]": '',
      "[NUM-EXPEDIENTE]": '',
      "[INFRACCION-PENAL]": '',
      "[MEDIDA-SOCIOEDUCATIVA]": '',
      "[MEDIDAS-ACCESORIAS]": '',
      "[DURACION-MEDIDA]": '',
      "[FECHA-INICIO-CUMPLIMIENTO]": '',
      "[TECNICAS-RECOJO-INFORMACION]": this.planTratamiento.instTecnicas,
      "[TABLA-EVALUACION]": JSON.stringify(tablaEvaluacion),
      "[TABLA-MATRIZ-PTI]": JSON.stringify(tablaMatrizPti),
      "[TABLA-CONTROL-ASISTENCIA]": JSON.stringify(tablaControlActividades),
      "[TABLA-MATRIZ-CUMPLIMIENTO-MEDIDAS]": JSON.stringify(tablaMedidasAccesorias),
    }

    this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<string>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        console.log(response);

        const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

        const pwa = window.open(url);

      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });

  }

  guardar(esActivo?: boolean) {       

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {

            this.planTratamiento.instTecnicas = this.ingresoPlanTratamientoFormGroup.controls['instTecnicas'].value;
            this.planTratamiento.tokenPadre = this.uuid_fp;
            this.planTratamiento.tipoCentro = 'SOA';
            this.planTratamiento.tipoAbierto = 'Libertad Restringida/Libertad Asistida';    
            this.planTratamiento.tokenExpedienteMatrizDetalle = this.detalleEntrante?.tokenIdentificador;        

            if (esActivo) {
              this.planTratamiento.esActivo = esActivo;
            }

            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
                  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
        
                    return;
                  }      
                  if (this.selectedIndex == 1) {
                    this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}`]);
                  }
                },
                error: (error: any) => {
                  this.planTratamientoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }

  guardarSinSalir() {       

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {

            this.planTratamiento.instTecnicas = this.ingresoPlanTratamientoFormGroup.controls['instTecnicas'].value;
            this.planTratamiento.tokenPadre = this.uuid_fp;
            this.planTratamiento.tipoCentro = 'SOA';
            this.planTratamiento.tipoAbierto = 'Libertad Restringida/Libertad Asistida';
            this.planTratamiento.tokenExpedienteMatrizDetalle = this.detalleEntrante?.tokenIdentificador;            
            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
                  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
        
                    return;
                  }      

                  if (this.selectedIndex < 1) {
                    this.selectedIndex++;
                  }
                  const queryParams: Params = { numDoc: response.data.idPlanTratamiento,  tabIndex: this.selectedIndex };
  
                  this.router.navigate(
                    [], 
                    {
                      relativeTo: this.route,
                      queryParams, 
                      queryParamsHandling: 'merge',
                    }
                  );
                },
                error: (error: any) => {
                  this.planTratamientoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }

  existeAlMenosUnDatoValido(detalle: PlanTratamientoIndIntervDTO[]) : boolean {
    const itemsEncontrados = detalle?.filter(item => item.tokenIdentificador && item.reajuste);
    // const itemsEncontrados = dataSource?.data.filter(item => item.tokenIdentificador && item.activo);
    if (itemsEncontrados?.length > 0) {
      return true;
    } else {
      return false;
    }
  }

  obtenerCantInactivos(detalle: PlanTratamientoIndIntervDTO[]) : number {
    const itemsEncontrados = detalle?.filter(item => !item.activo);
    // const itemsEncontrados = dataSource?.data.filter(item => item.tokenIdentificador && item.activo);
    if (itemsEncontrados?.length > 0) {
      return itemsEncontrados?.length;
    } else {
      return 0;
    }
  }

  sePuedeHacerReajuste() : boolean{
    const valor = this.estadoActivo && !this.estadoVisualizar;
    // const valor = (this.existeAlMenosUnDatoValido(this.planTratamiento.intervObjetivos) || 
    //               this.existeAlMenosUnDatoValido(this.planTratamiento.intervDiferenciada)) &&                   
    //               !this.estadoVisualizar;
    return valor;
  }

  irAReajuste() {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {

            this.planTratamiento.instTecnicas = this.ingresoPlanTratamientoFormGroup.controls['instTecnicas'].value;
            this.planTratamiento.tokenPadre = this.uuid_fp;
            this.planTratamiento.tipoCentro = 'SOA';
            this.planTratamiento.tipoAbierto = 'Libertad Restringida/Libertad Asistida';   
            this.planTratamiento.tokenExpedienteMatrizDetalle = this.detalleEntrante?.tokenIdentificador;         
            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe(
              {
                next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
                  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
        
                    return;
                  }      

                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/reajuste`], {queryParams: {numDoc : this.planTratamiento.idPlanTratamiento}})

                },
                error: (error: any) => {
                  this.planTratamientoService.checkError(error);
                }
              }
            )
          }
        }
      }
    );
  }

  irAMostrarReajuste() {
    if (this.estadoFinalizado) {
      this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/reajuste`], {queryParams: {numDoc : this.planTratamiento.idPlanTratamiento, state: 'visualizar'}})
    } else {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
        "Deseas continuar?"
      );
  
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
  
              this.planTratamiento.instTecnicas = this.ingresoPlanTratamientoFormGroup.controls['instTecnicas'].value;
              this.planTratamiento.tokenPadre = this.uuid_fp;
              this.planTratamiento.tipoCentro = 'SOA';
              this.planTratamiento.tipoAbierto = 'Libertad Restringida/Libertad Asistida';       
              this.planTratamiento.tokenExpedienteMatrizDetalle = this.detalleEntrante?.tokenIdentificador;     
              this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe(
                {
                  next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
                    
                    if (!response.exito) {
                      this.planTratamientoService.checkError(response);
          
                      return;
                    }      
  
                    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/reajuste`], {queryParams: {numDoc : this.planTratamiento.idPlanTratamiento, state: 'visualizar'}})
  
                  },
                  error: (error: any) => {
                    this.planTratamientoService.checkError(error);
                  }
                }
              )
            }
          }
        }
      );
    }

  }

  obtenerIndiceTabs() : Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tabIndex = params['tabIndex'];
        if (tabIndex) {
          this.selectedIndex = parseInt(tabIndex);
        }
      })
    );
  }

  actualizarDataSource(lista: PlanTratamientoIndIntervDTO[]) : MatTableDataSource<PlanTratamientoIndIntervDTO> {
    const listaTemporal: PlanTratamientoIndIntervDTO[] = [];
    for (let item of lista) {
      if (item.activo) {
        listaTemporal.push(item);
      }
    }
    return new MatTableDataSource(listaTemporal);
  } 

  cambiarTab(event: MatTabChangeEvent) {
    if (event.index !== 0 && this.ingresoPlanTratamientoFormGroup.invalid) {
      this.selectedIndex = 0;
      if (this.ingresoPlanTratamientoFormGroup.invalid) {
        let ref = this.dialogMensajeService.mensajeAdvertencia(
          `Existen datos sin guardar.`,
          `Verifique que el formulario de la pestaña "Generales" se encuentre lleno.`
        );
      }
    } else if (!this.ingresoPlanTratamientoFormGroup.invalid) {
      let queryParams;
      if (this.planTratamiento.idPlanTratamiento) {
        queryParams = `numDoc=${this.planTratamiento.idPlanTratamiento}&tabIndex=${event.index}`;
        this.location.replaceState(`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/crear-editar?${queryParams}`);
      }
    }
  }

  irAFichaSeguimiento(fila: PlanTratamientoIndIntervDTO) {
    if (!fila.idPlanTratIndInterv) {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        `Es necesario guardar el PTI antes de crear una ficha de seguimiento.`,
        "Deseas continuar?"
      );
  
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              Object.assign(this.planTratamiento, this.ingresoPlanTratamientoFormGroup.value);  
              this.planTratamiento.tipoCentro = 'SOA';
              this.planTratamiento.tipoAbierto = 'Amonestación o Semilibertad';
              this.planTratamiento.tokenPadre = this.uuid_fp;
              this.planTratamiento.tokenExpedienteMatrizDetalle = this.detalleEntrante?.tokenIdentificador;
              this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe({
                next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
  
                    return;
                  }                
  
                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/ficha-seguimiento-abierto`], 
                    {
                      state: {
                        interv: fila
                      }
                    }
                  )
  
                },
                error: (error: any) => {
                  this.planTratamientoService.checkError(error);
                }
              })
            }
          }
        }
      );
    } else {
      this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/ficha-seguimiento-abierto`], 
        {
          state: {
            interv: fila
          }
        }
      )
    }
  }

  esEstadoBorrador() : boolean {
    const condicion = !this.estadoActivo && !this.estadoFinalizado;
    return condicion;
  }
}
