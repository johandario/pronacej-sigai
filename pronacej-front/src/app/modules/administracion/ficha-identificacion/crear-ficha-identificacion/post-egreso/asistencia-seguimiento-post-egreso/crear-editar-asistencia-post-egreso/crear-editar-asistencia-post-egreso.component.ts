import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DateAdapter } from '@angular/material/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DetalleFichaAsistenciaPostEgresoDTO, FichaAsistenciaPostEgresoDTO } from 'app/core/model/both/FichaAsistenciaPostEgreso.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { PlanAsistenciaService } from 'app/modules/seguridad/services/planAsistencia.service';
import { CommonModule, Location } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTable, MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { SeguimientoActividadOcupacionalDTO } from 'app/core/model/both/SeguimientoActividadOcupacional.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { environment } from 'environments/environment';
import { MatDialog } from '@angular/material/dialog';
import { ModalCrearEditarDetalleAsistenciaComponent } from '../modal-crear-editar-detalle-asistencia/modal-crear-editar-detalle-asistencia.component';
import { PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { PlanAsistenciaPostEgresoDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { ModalSeguimientoSubirDocComponent } from '../modal-seguimiento-subir-doc/modal-seguimiento-subir-doc.component';
import { CatalogoService } from 'app/core/services/catalogo.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaAsistenciaPostEgresoDocumentosRequest } from 'app/core/model/request/ia/FichaAsistenciaPostEgresoDocumentosRequest.model';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';

@Component({
  selector: 'app-crear-editar-asistencia-post-egreso',
  standalone: true,
  imports: [MatButtonModule,
    MatExpansionModule,
    MatTableModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './crear-editar-asistencia-post-egreso.component.html',
  styleUrl: './crear-editar-asistencia-post-egreso.component.scss'
})
export class CrearEditarAsistenciaPostEgresoComponent implements OnInit {

  asistenciaActividadForm: FormGroup;

  mostrarTablaRegistros: boolean = false;

  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  nemonicoMenu: string = "";  

  uuid_fp!: string;
  tiposFormato: CatalogoDTO[] = [];
  item: FichaAsistenciaPostEgresoDTO;
  esEdicion: boolean = false;
  planAsistencia: PlanAsistenciaPostEgresoDTO;

  listSize = [5, 10, 15, 20];
  page = 0;
  size = this.listSize[0];
  totalItems = 0;
  lista: DetalleFichaAsistenciaPostEgresoDTO[] = [];
  dataSource: MatTableDataSource<DetalleFichaAsistenciaPostEgresoDTO>;

  @ViewChild("tablaDocumentos") tablaDocumentos: DocumentosSubidosTablaComponent;


  @ViewChild('tabla')
  tableSeguimiento: MatTable<DetalleFichaAsistenciaPostEgresoDTO>;

  displayedColumns: string[];
  //  = {
  //   acciones: 'Acciones',
  //   fecha: 'Fecha y Hora',
  //   modalidadEntrevista: 'Modalidad',
  //   personaEntrevista: 'Persona Entrevistada',
  //   observaciones: 'Observaciones',
  //   descripcionActividad: 'Descripcion actividad',
  //   motivo: 'Motivo'
  // };
  esVisualizar: boolean = false;
  etiquetasModel = etiquetasModel;


  constructor(private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private planAsistenciaService: PlanAsistenciaService,
    private dateAdapter: DateAdapter<any>,
    public funcionesUtils: FuncionesUtils,
    private location: Location,
    public dialog: MatDialog,
    private catalogoService: CatalogoService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService
  ) {
    this.dateAdapter.setLocale('es');
    this.asistenciaActividadForm = this.fb.group({
      // tipoPrograma: ['', Validators.required],
      tipoFormato: ['', Validators.required],
      planAsistenciaPostEgresoDetalle: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');
    // this.verificarTiposDeDocumentos();
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.esVisualizar = history.state.visualizar || false;

    this.funcionesUtils.obtenerListaCatalogo('TIPO_FORMATO_SEGUMIENTO_EGRESO', '').subscribe({
      next: (data) => {
        this.tiposFormato = data;
        load.close();
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });
    this.item = history.state.item;
    if (this.item) {
      console.log(this.item);
      this.esEdicion = true;
      this.asistenciaActividadForm.controls.tipoFormato.setValue(this.item.tipoFormato);      
      this.asistenciaActividadForm.controls.planAsistenciaPostEgresoDetalle.setValue(this.item.planAsistenciaPostEgresoDetalle);   
      this.asistenciaActividadForm.disable();      
      this.lista = this.item.detalleFichaAsistenciaPostEgresos;
      this.dataSource = new MatTableDataSource(this.lista);
      this.mostrarTablaRegistros = true;
      if (
        this.item.tipoFormato.nemonico === 'NO_CONTINUAR_PROGRAMA_SEGUIMIENTO' || 
        this.item.tipoFormato.nemonico === 'ACTIVIDADES_PREVIAS_CULMINAR') 
      {
        this.displayedColumns = [
          'acciones',
          'fecha',
          'modalidadEntrevista',
          'observaciones',
          'descripcionActividad',
        ];
      } else if (this.item.tipoFormato.nemonico === 'ACTIVIDAD_SEGUIMIENTO_PASPE') {
        this.displayedColumns = [
          'acciones',
          'fecha',
          'modalidadEntrevista',
          'personaEntrevista',
          'observaciones',
          'descripcionActividad',
        ];        
      } else {
        this.displayedColumns = [
          'acciones',
          'fecha',
          'modalidadEntrevista',
          'personaEntrevista',
          'observaciones',
          'descripcionActividad',
          'motivo'
        ];        
      }
      this.obtenerTiposDeDocumentos().subscribe(() => {
        const load = this.dialogMensajeService.mensajeLoading('Obteniendo documentos subidos...');
        this.obtenerDocumentos().subscribe(() => load.close());
      });      
      // this.obtenerSeguimientos(this.item.tokenIdentificador);
    }

    if (history.state.plan) {
      this.planAsistencia = history.state.plan;
    }
  }

  guardar() {
    if (this.asistenciaActividadForm.invalid) {
      this.dialogMensajeService.mensajeErrorConTitulo("Error", "Complete los campos obligatorios");
      return;
    }
    const actividadDTO: FichaAsistenciaPostEgresoDTO = {
      ...this.asistenciaActividadForm.value,
      tokenIdentificadorFichaIdentificacion: this.uuid_fp,
      tokenPlanAsistencia: this.planAsistencia.tokenIdentificador,
      esEdicion: this.esEdicion,
      tokenIdentificador: this.esEdicion ? this.item?.tokenIdentificador : undefined,
      detalleFichaAsistenciaPostEgresos: this.dataSource.data,
      idPlanAsistenciaPostEgreso: this.planAsistencia.idPlanAsistenciaPostEgreso
    }    
    console.log(actividadDTO);

    this.planAsistenciaService.crearFichaAsistenciaPostEgreso(actividadDTO).subscribe({
      next: (response) => {
        // if (response.exito) {
        //   this.dialogMensajeService.mensajeExitoso("Guardar", response.mensaje).afterClosed().subscribe(() => {
        //     // this.location.back();
        //     this.item = response.data;
        //     this.asistenciaActividadForm.get('tipoFormato').disable();
        //   });
        // } else {
        //   this.dialogMensajeService.mensajeErrorConTitulo("Error", response.mensaje);
        // }
        if (!response.exito) {
          this.planAsistenciaService.checkError(response);

          return;
        }
        this.location.back();

      },
      error: (err) => {
        this.dialogMensajeService.mensajeError("Hubo un problema al guardar el registro. Inténtalo de nuevo.");
      }
    });
  }

  compararCatalogos(o1: CatalogoDTO, o2: CatalogoDTO): boolean {
    return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
  }

  cancelar() {
    this.location.back();
  }

  async obtenerSeguimientos(tokenIdentificador: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = tokenIdentificador;

    this.planAsistenciaService
      .obtenerDetallesPorFichaAsistencia(paginacionRequest)
      .subscribe({
        next: (
          response: RespuestaPorDefecto<
            PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO>
          >
        ) => {
          if (!environment.production) {
            console.log('respuesta ', response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }
          this.lista = response.data.data;
          // this.dataSource = this.lista;
          this.totalItems = response.data.totalItems;
          // this.tableSeguimiento.renderRows();
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  getKeysSeguimiento() {
    return Object.keys(this.displayedColumns);
  }

  aniadirFilaInformacion() {
    const dialogRef = this.dialog.open(
      ModalCrearEditarDetalleAsistenciaComponent,
      {
        data: {
          uuid_fp: this.uuid_fp,
          asistenciaSeguimiento: this.item?.tokenIdentificador,
          tipoFormato: this.asistenciaActividadForm.controls['tipoFormato'].value
        },
        width: '600px',
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.lista.push(result);
        this.dataSource = new MatTableDataSource(this.lista);
        // Al cerrar el modal con resultado, recargar la tabla con los nuevos datos.
        
        // this.obtenerSeguimientos(this.item.tokenIdentificador);
      }
    });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;    
    // this.obtenerSeguimientos(this.item.tokenIdentificador);
  }

  editarFilaInformacion(informacion: DetalleFichaAsistenciaPostEgresoDTO, index: number) {
    const dialogRef = this.dialog.open(
      ModalCrearEditarDetalleAsistenciaComponent,
      {
        data: {
          informacion: informacion,
          uuid_fp: this.uuid_fp,
          asistenciaSeguimiento: this.item?.tokenIdentificador,
          tipoFormato: this.item.tipoFormato
        },
        width: '600px',
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Al cerrar el modal con resultado, recargar la tabla con los nuevos datos.
        // this.obtenerSeguimientos(this.item.tokenIdentificador);
        this.lista[index] = result;
        this.dataSource = new MatTableDataSource(this.lista);
      }
    });
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    this.obtenerDocumentos().subscribe();
  }

  obtenerDocumentos() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;
  
    let fichaAsistenciaPostEgresoDocumentosRequest = new FichaAsistenciaPostEgresoDocumentosRequest();
    fichaAsistenciaPostEgresoDocumentosRequest.page = this.tablaDocumentos.page;
    fichaAsistenciaPostEgresoDocumentosRequest.size = this.tablaDocumentos.pageSize;
    fichaAsistenciaPostEgresoDocumentosRequest.textoBuscar = this.tablaDocumentos.textoBuscar;
    fichaAsistenciaPostEgresoDocumentosRequest.tokenIdentificadorFichaAsistenciaPostEgreso = this.item.tokenIdentificador;
  
    return this.planAsistenciaService
      .obtenerDocumentos(fichaAsistenciaPostEgresoDocumentosRequest, '')
      .pipe(
        tap((response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }
  
          if (!response.exito) {
            this.planAsistenciaService.checkError(response);
            throw new Error(response.mensaje); // Lanza error para interrumpir el flujo
          }
  
          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        }),
        catchError(error => {
          this.planAsistenciaService.checkError(error);
          return throwError(() => error); // Propaga el error
        }),
        map(() => void 0) // Devuelve void para indicar que no se necesita un valor de retorno
      );
  }

  agregarDocumento() {    
    const dialogRef = this.dialog.open(ModalSeguimientoSubirDocComponent, {
      width: '80%',
      data: this.item      
    });

    dialogRef.afterClosed().subscribe(async () => {
      this.obtenerTiposDeDocumentos().subscribe(() => {
        const load = this.dialogMensajeService.mensajeLoading('Obteniendo documentos subidos...');
        this.obtenerDocumentos().subscribe(() => load.close());
      });
    })

  }

  eliminarActividadIntervencion(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.lista.splice(index, 1);
            this.dataSource = new MatTableDataSource(this.lista);
            // this.planAsistenciaService.eliminarDetalleFichaAsistencia(actividad).subscribe({
            //   next: (response) => {
            //     if (response.exito) {
            //       this.obtenerSeguimientos(this.item.tokenIdentificador);
            //     } else {
            //       this.dialogMensajeService.mensajeErrorConTitulo(
            //         response.titulo,
            //         response.mensaje
            //       )
            //     }
            //   },
            //   error: (error) => {
            //     this.planAsistenciaService.checkError(error);
            //     console.error('Error en la solicitud:', error);
            //   }
            // });
          }
        }
      }
    )


  }

  finalizarSeleccion(event: any) {

    // let ref = this.dialogMensajeService.mensajeConConfirmacion(
    //   "Está seguro de las opciones escogidas?",
    //   "Deseas continuar?"
    // );

    // ref.afterClosed().subscribe(
    //   {
    //     next: (resp: "confirmed" | "cancelled") => {
    //       if (resp == "confirmed") {
            // this.asistenciaActividadForm.disable();
            this.mostrarTablaRegistros = true;
            if (
              this.asistenciaActividadForm.controls['tipoFormato'].value.nemonico === 'NO_CONTINUAR_PROGRAMA_SEGUIMIENTO' || 
              this.asistenciaActividadForm.controls['tipoFormato'].value.nemonico === 'ACTIVIDADES_PREVIAS_CULMINAR') 
            {
              this.displayedColumns = [
                'acciones',
                'fecha',
                'modalidadEntrevista',
                'observaciones',
                'descripcionActividad',
              ];
            } else if (this.asistenciaActividadForm.controls['tipoFormato'].value.nemonico === 'ACTIVIDAD_SEGUIMIENTO_PASPE') {
              this.displayedColumns = [
                'acciones',
                'fecha',
                'modalidadEntrevista',
                'personaEntrevista',
                'observaciones',
                'descripcionActividad',
              ];  
            } else {
              this.displayedColumns = [
                'acciones',
                'fecha',
                'modalidadEntrevista',
                'personaEntrevista',
                'observaciones',
                'descripcionActividad',
                'motivo'
              ]; 
            }
    //       }
    //     }
    //   }
    // )
  }


    // verificarTiposDeDocumentos() {
    //         if (this.tiposDeDocumentosSistema.length == 0) {
    //             this.catalogoService.obtenerHijos(
    //                 etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS,
    //                 this.nemonicoMenu
    //             ).subscribe(
    //                 {
    //                     next: (respuesta: RespuestaPorDefecto<CatalogoDTO[]>) => { 
    //                         if (!respuesta.exito) {
    //                             this.catalogoService.checkError(respuesta);
    //                             return;
    //                         }
    //                         respuesta.data.forEach((tipoDoc) => {
    //                             let documento: TipoDeDocumento = tipoDoc as TipoDeDocumento;
    //                             documento.requerido = documento.nemonico !== etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS;
    //                             this.tiposDeDocumentosSistema.push(documento);
    //                         });
    //                     },
    //                     error: (error: any) => {
    //                         this.catalogoService.checkError(error);
    //                     }
    //                 }
    //             );
    //         }
    //     }

  private obtenerTiposDeDocumentos(): Observable<TipoDeDocumento[]> {
    return this.tipoDeIdentificacionTipoDeDocumentoService
      .obtenerTiposDeDocumentos(
        etiquetasModel.SECCION_FICHA_IDENT_PTI_FICHA_SEGUIMIENTO_ABIERTO, ''
      )
      .pipe(
        tap((response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }
  
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            throw new Error(response.mensaje); // Lanza un error para que el observable maneje la interrupción
          }
  
          if (response.data.length === 0) {
            this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
            throw new Error("Tipos de documentos no configurados"); 
          }
  
          this.tiposDeDocumentosSistema = response.data.map(tipoArch => {
            let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
            let tipoDeDocumento = new TipoDeDocumento();
            tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
            tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
            tipoDeDocumento.requerido = tipoArch.requerido;
            tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
            tipoDeDocumento.nombre = catalogoTipoDoc.nombre;
  
            return tipoDeDocumento;
          });
        }),
        catchError(error => {
          this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          return throwError(() => error); // Propaga el error al flujo de observables
        }),
        map(() => this.tiposDeDocumentosSistema) // Retorna la lista de tipos de documentos
      );
  }  
        
}
