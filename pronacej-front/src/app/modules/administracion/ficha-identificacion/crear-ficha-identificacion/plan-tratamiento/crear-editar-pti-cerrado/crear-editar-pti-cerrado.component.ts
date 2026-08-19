import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
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
import { ModalEditaIntervComponent } from './modal-edita-interv/modal-edita-interv.component';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DateAdapter, provideNativeDateAdapter } from '@angular/material/core';
import { CommonModule, Location } from '@angular/common';
import { catchError, concatMap, forkJoin, iif, Observable, of, tap, throwError } from 'rxjs';
import etiquetasModel from 'app/core/etiquetas.model';


@Component({
  selector: 'app-crear-editar-pti-cerrado',
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
    CommonModule
  ], 
  providers: [provideNativeDateAdapter()],   
  templateUrl: './crear-editar-pti-cerrado.component.html',
  styleUrl: './crear-editar-pti-cerrado.component.scss'
})
export class CrearEditarPtiCerradoComponent {
  estadoEditar: boolean = false;
  estadoVisualizar: boolean = false;
  estadoActivo: boolean = false;
  estadoFinalizado: boolean = false;
  selectedIndex: number = 0;

  displayedColumns: string[] = ['dimensiones', 'factoresRiesgo', 'factoresProtectores'];
  dataSourceEspecif: MatTableDataSource<PlanTratamientoIndEspecifDTO>;

  displayedColumns2: string[] = ['acciones', 'dimension', 'objetivo', 'actividadPrograma', 'equipoResponsable', 'tiempoEstimado'];
  dataSourceIntervObjetivos: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceIntervNoCriminogenos: MatTableDataSource<PlanTratamientoIndIntervDTO>;
  dataSourceIntervDiferenciada: MatTableDataSource<PlanTratamientoIndIntervDTO>;

  ingresoPlanTratamientoFormGroup = this.fb.group({
    instTecnicas: [null],
    factRiesgoNoCrimin: [null],
    valRiesgo: [null],
    hipotExplicativa: [null],
    intensidadIntervTrat: [null],
  })

  dimensionIntervObjetivoFormControl = new FormControl(null);

  filaEditada: PlanTratamientoIndIntervDTO = null;

  dimensiones: CatalogoDTO[];

  planTratamiento: PlanTratamientoIndDTO = new PlanTratamientoIndDTO;

  uuid_fp: string;

  base64Image: string | null = null;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_GESTION_PTI;
  

  @ViewChild('paginatorIntervObjetivos') paginatorIntervObjetivos: MatPaginator;
  @ViewChild('paginatorIntervNoCriminogenos') paginatorIntervNoCriminogenos: MatPaginator;
  @ViewChild('paginatorIntervDiferenciada') paginatorIntervDiferenciada: MatPaginator;


  constructor(
    private dateAdapter: DateAdapter<any>,        
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private router: Router,
    private planTratamientoService: PlanTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    public dialog: MatDialog,
    private location: Location,
  ) {
    this.dateAdapter.setLocale('es');
  }  

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.cargarDatos();
  }

  cargarDatos(): void {        
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerIndiceTabs().pipe(    
      concatMap(() => this.obtenerCatalogos()),  
      concatMap(() => this.obtenerParametrosDeConsulta()),
      concatMap(() =>
        iif(
          () => this.estadoEditar, 
          this.obtenerPlanTratamiento(),          
          of(null),
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

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const numDoc = params['numDoc'];
        if (numDoc) {
          const state = params['state'];
          if (state) {
            this.estadoVisualizar = true;
            this.ingresoPlanTratamientoFormGroup.controls['valRiesgo'].disable();
          }
          this.estadoEditar = true;         
        } else {
          this.ingresoPlanTratamientoFormGroup.markAllAsTouched();
          this.planTratamiento = new PlanTratamientoIndDTO();
          this.planTratamiento.especFactores = [];
          this.planTratamiento.intervObjetivos = [];
          this.planTratamiento.intervNoCriminogenos = [];
          this.planTratamiento.intervDiferenciada = [];
          this.dataSourceIntervObjetivos = new MatTableDataSource(this.planTratamiento.intervObjetivos);
          this.dataSourceIntervNoCriminogenos = new MatTableDataSource(this.planTratamiento.intervNoCriminogenos);
          this.dataSourceIntervDiferenciada = new MatTableDataSource(this.planTratamiento.intervDiferenciada);
          for (let dimension of this.dimensiones) {
            let factor = new PlanTratamientoIndEspecifDTO;
            factor.dimension = dimension;
            this.planTratamiento.especFactores.push(factor);
          }          
          this.dataSourceEspecif = new MatTableDataSource(this.planTratamiento.especFactores);
        }
      })
    );
  }

  obtenerPlanTratamiento(): Observable<any> {
    return this.planTratamientoService.obtenerPlanTratamientoPorId(this.route.snapshot.queryParams['numDoc'], this.nemonicoMenu).pipe(
      tap((item) => {
        this.planTratamiento = item.data;
        if (this.planTratamiento.completada) {
          this.estadoVisualizar = true;
          this.ingresoPlanTratamientoFormGroup.controls['valRiesgo'].disable();
        }

        this.planTratamiento.esEdicion = true;
        this.ingresoPlanTratamientoFormGroup.patchValue(this.planTratamiento);
        this.dataSourceEspecif = new MatTableDataSource(this.planTratamiento.especFactores);
        this.dataSourceIntervObjetivos = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervNoCriminogenos = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervDiferenciada = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);

        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;     

        if (this.planTratamiento.estado.nemonico === etiquetasModel.NEMONICO_ESTADO_PTI_ACTIVO) {
          this.estadoActivo = true;
          this.ingresoPlanTratamientoFormGroup.controls['valRiesgo'].disable();
        } else if (this.planTratamiento.estado.nemonico === etiquetasModel.NEMONICO_ESTADO_PTI_FINALIZADO) {
          this.estadoFinalizado = true;
          this.ingresoPlanTratamientoFormGroup.controls['valRiesgo'].disable();
        }
      }),
      catchError(err => {
        this.planTratamientoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }  

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      'DIMENSIONES_PLAN_TRATAMIENTO',  
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.dimensiones = results[0]?.data;
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  aniadirFilaIntervObjetivos() {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result: PlanTratamientoIndIntervDTO) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos.unshift(result);
        this.dataSourceIntervObjetivos = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  }

  editarFilaIntervObjetivos(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { fila: fila, dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervObjetivos[index] = result;
        this.dataSourceIntervObjetivos = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
        this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
      }
    })
  }

  eliminarItemIntervObjetivos(index: number) {    
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervObjetivos.splice(index, 1);
            this.dataSourceIntervObjetivos = this.actualizarDataSource(this.planTratamiento.intervObjetivos);
            this.dataSourceIntervObjetivos.paginator = this.paginatorIntervObjetivos;
          }
        }
      }
    )    
  }

  aniadirFilaIntervNoCriminogenos() {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervNoCriminogenos.unshift(result);
        this.dataSourceIntervNoCriminogenos = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  }

  editarFilaIntervNoCriminogenos(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { fila: fila, dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervNoCriminogenos[index] = result;
        this.dataSourceIntervNoCriminogenos = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
        this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
      }
    })
  }

  eliminarItemIntervNoCriminogenos(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervNoCriminogenos.splice(index, 1);
            this.dataSourceIntervNoCriminogenos = this.actualizarDataSource(this.planTratamiento.intervNoCriminogenos);
            this.dataSourceIntervNoCriminogenos.paginator = this.paginatorIntervNoCriminogenos;
          }
        }
      }
    )    
  }

  aniadirFilaIntervDiferenciada() {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervDiferenciada.unshift(result);
        this.dataSourceIntervDiferenciada = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
      }
    })
  }

  editarFilaIntervDiferenciada(fila: PlanTratamientoIndIntervDTO, index: number) {
    const dialogRef = this.dialog.open(ModalEditaIntervComponent, {
      disableClose: true,
      data: { fila: fila, dimensiones: this.dimensiones },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        result.activo = true;
        this.planTratamiento.intervDiferenciada[index] = result;
        this.dataSourceIntervDiferenciada = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);
        this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
      }
    })
  }

  eliminarItemIntervDiferenciada(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.planTratamiento.intervDiferenciada.splice(index, 1);
            this.dataSourceIntervDiferenciada = this.actualizarDataSource(this.planTratamiento.intervDiferenciada);
            this.dataSourceIntervDiferenciada.paginator = this.paginatorIntervDiferenciada;
          }
        }
      }
    )    
  }

  mostrarFilaInterv(fila: PlanTratamientoIndIntervDTO) {
    this.dialog.open(ModalEditaIntervComponent, {
      data: {fila: fila, dimensiones: this.dimensiones, visualizar: true},
      width: '600px'
    });     
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

  irARegistroActividad() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/registro-actividad`], { queryParams: { numDoc: this.planTratamiento.idPlanTratamiento } })
  }  

  irAReajuste() {
    if (this.ingresoPlanTratamientoFormGroup.invalid) {
      let ref = this.dialogMensajeService.mensajeAdvertencia(
        `Existen datos sin guardar.`,
        `Verifique que el formulario de la pestaña "Antecedentes" se encuentre lleno.`
      );
      return;
    }

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            Object.assign(this.planTratamiento, this.ingresoPlanTratamientoFormGroup.value);  
            this.planTratamiento.tipoCentro = 'CJDR';
            
            this.planTratamiento.tokenPadre = this.uuid_fp;

            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe({
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
            })
          }
        }
      }
    );
  }

  irAMostrarReajuste() {
    
    if (this.ingresoPlanTratamientoFormGroup.invalid) {
      let ref = this.dialogMensajeService.mensajeAdvertencia(
        `Existen datos sin guardar.`,
        `Verifique que el formulario de la pestaña "Antecedentes" se encuentre lleno.`
      );
      return;
    }

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
              Object.assign(this.planTratamiento, this.ingresoPlanTratamientoFormGroup.value);  
              this.planTratamiento.tipoCentro = 'CJDR';
              
              this.planTratamiento.tokenPadre = this.uuid_fp;
  
              this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe({
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
              })
            }
          }
        }
      );
    }

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

  sePuedeHacerReajuste() : boolean{
    const valor = this.estadoActivo && !this.estadoVisualizar;
    // const valor = (this.existeAlMenosUnDatoValido(this.planTratamiento.intervObjetivos) || 
    //               this.existeAlMenosUnDatoValido(this.planTratamiento.intervNoCriminogenos) || 
    //               this.existeAlMenosUnDatoValido(this.planTratamiento.intervDiferenciada)) &&                   
    //               !this.estadoVisualizar;
    return valor;
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

  guardar(esActivo?: boolean) {
    if (this.ingresoPlanTratamientoFormGroup.invalid) {
      let ref = this.dialogMensajeService.mensajeAdvertencia(
        `Existen datos sin guardar.`,
        `Verifique que el formulario de la pestaña "Antecedentes" se encuentre lleno.`
      );
      return;
    }

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            Object.assign(this.planTratamiento, this.ingresoPlanTratamientoFormGroup.value);  
            this.planTratamiento.tipoCentro = 'CJDR';
            this.planTratamiento.tokenPadre = this.uuid_fp;
            
            if (esActivo) {
              this.planTratamiento.esActivo = esActivo;
            }

            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {

                if (!response.exito) {
                  this.planTratamientoService.checkError(response);

                  return;
                }
                if (this.selectedIndex == 2) {
                  this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                  this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}`]);
                }
              },
              error: (error: any) => {
                this.planTratamientoService.checkError(error);
              }
            })
          }
        }
      }
    );
  }

  guardarSinSalir() {
    if (this.ingresoPlanTratamientoFormGroup.invalid) {
      let ref = this.dialogMensajeService.mensajeAdvertencia(
        `Existen datos sin guardar.`,
        `Verifique que el formulario de la pestaña "Antecedentes" se encuentre lleno.`
      );
      return;
    }

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            Object.assign(this.planTratamiento, this.ingresoPlanTratamientoFormGroup.value);  
            this.planTratamiento.tipoCentro = 'CJDR';
            
            this.planTratamiento.tokenPadre = this.uuid_fp;

            this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {

                if (!response.exito) {
                  this.planTratamientoService.checkError(response);

                  return;
                }

                if (this.selectedIndex < 2) {
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
            })
          }
        }
      }
    );
  }

  irASubactividades(fila: PlanTratamientoIndIntervDTO) {
    if (!fila.idPlanTratIndInterv) {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        `Es necesario guardar el PTI antes de definir subactividades.`,
        "Deseas continuar?"
      );
  
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              Object.assign(this.planTratamiento, this.ingresoPlanTratamientoFormGroup.value);  
              this.planTratamiento.tipoCentro = 'CJDR';
              
              this.planTratamiento.tokenPadre = this.uuid_fp;
              console.log(this.planTratamiento);
  
              this.planTratamientoService.crearPlanTratamiento(this.planTratamiento, this.nemonicoMenu).subscribe({
                next: (response: RespuestaPorDefecto<PlanTratamientoIndDTO>) => {
  
                  if (!response.exito) {
                    this.planTratamientoService.checkError(response);
  
                    return;
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
              })
            }
          }
        }
      );
    } else {
      console.log('intervencion dif', fila);
      this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/planTratamiento/${this.uuid_fp}/subactividades`], { queryParams: 
        {numDoc: this.planTratamiento.idPlanTratamiento, numInterv: fila.idPlanTratIndInterv } })
    }
  }

  cambiarTab(event: MatTabChangeEvent) {
    if (event.index !== 0 && this.ingresoPlanTratamientoFormGroup.invalid) {
      this.selectedIndex = 0;
      if (this.ingresoPlanTratamientoFormGroup.invalid) {
        let ref = this.dialogMensajeService.mensajeAdvertencia(
          `Existen datos sin guardar.`,
          `Verifique que el formulario de la pestaña "Antecedentes" se encuentre lleno.`
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

  regresar() {
    this.location.back();
  }

  esEstadoBorrador() : boolean {
    const condicion = !this.estadoActivo && !this.estadoFinalizado;
    return condicion;
  }

}
