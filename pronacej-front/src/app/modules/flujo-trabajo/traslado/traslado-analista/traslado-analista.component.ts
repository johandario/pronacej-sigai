import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TrasladoService } from '../traslado.service';
import { TrasladoAdolescenteDTO, TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, Location } from '@angular/common'
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MatIconModule } from '@angular/material/icon';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { catchError, concatMap, forkJoin, iif, map, Observable, of, startWith, tap, throwError } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { InstanciaProcesoDTO, TareaDTO, TareaTrasladoDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { FlujoTrabajoService } from '../../flujo-trabajo.service';
import { ProcesoDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';

@Component({
  selector: 'app-traslado-analista',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatAutocompleteModule,
    CommonModule,
  ],
  templateUrl: './traslado-analista.component.html',
  styleUrl: './traslado-analista.component.scss'
})
export class TrasladoAnalistaComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_FLUJO_BORRADORES_TRASLADOS;
  funcionarioActivo: FuncionarioDTO;

  tokenID: string;
  tokenTarea: string;
  tokenTareaSeleccionada: string;

  estado: string = '';
  traslado: TrasladoDTO = new TrasladoDTO();

  habilitarAgregar: Boolean = false;
  datosCargados: boolean = false;

  centrosOrigen: JerarquiaDTO[];
  centrosDestino: JerarquiaDTO[];
  centroOrigen: JerarquiaDTO;

  centrosOrigenFiltrado: Observable<JerarquiaDTO[]>;
  centrosDestinoFiltrado: Observable<JerarquiaDTO[]>;
  adolescentesFiltrado: Observable<FichaIdentificacionDTO[]>;
  motivosTrasladoFiltrado: Observable<CatalogoDTO[]>;

  tareaEntrante: TareaDTO = new TareaDTO;
  tareaSaliente: TareaDTO = new TareaDTO;
  listaTareas: TareaDTO[];

  adolescentes: FichaIdentificacionDTO[];
  motivosTraslado: CatalogoDTO[];
  estadoEvento: any;
  trasladoCargado: boolean = false;

  proceso: ProcesoDTO;
  esNuevo: boolean = false;
  esBorrador: boolean = false;

  displayedColumns: string[] = ['acciones', 'nombre', 'dni'];
  @ViewChild('paginatorAdolescente') paginatorAdolescente: MatPaginator;

  dataSource = new MatTableDataSource<any>;

  informeFormGroup = this.fb.group({
    centroOrigen: [null as JerarquiaDTO, Validators.required],
    centroDestino: [null as JerarquiaDTO, Validators.required],
    adolescente: [null],
    motivoTraslado: [null as CatalogoDTO, Validators.required],
    antecedentes: ['', Validators.required],
    analisis: [''],
    conclusiones: [''],
    recomendaciones: [''],
  })

  constructor(
    private fb: FormBuilder,
    private trasladoService: TrasladoService,
    private dialogMensajeService: DialogMensajeService,
    private route: ActivatedRoute,
    private router: Router,
    private _location: Location,
    private pdfService: PdfService,
    public funcionesUtils: FuncionesUtils,
    private jerarquiaService: JerarquiaService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private catalogoService: CatalogoService,
    private funcionarioService: FuncionarioService,
    private flujoTrabajoService: FlujoTrabajoService
  ) { }

  ngOnInit(): void {

    this.proceso = history.state.proceso;

    if (this.proceso) {
      this.esNuevo = true;
    }

    //obtengo tarea entrante del step seleccionado y lista de tareas del flujo
    if (history.state.tareaEntrante && history.state.listaTareas) {
      console.log(history);

      this.tareaEntrante = history.state.tareaEntrante;
      this.listaTareas = history.state.listaTareas;
      //asignar tareaEntrante en base a la lista para validar la vista
      //asignar tareaSaliente para enviar en la petición de guardado de traslado
    }

    this.cargarDatos();
  }

  cargarDatos(): void {
    //Existe el parámetro de URL tokenID?
    // Si: Cargo traslado
    // No: tengo el formulario para poder llenar
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.estadoInicial();

    this.obtenerParametrosDeConsulta().pipe(
      concatMap(() => this.obtenerCatalogos()),
      concatMap(() => this.obtenerJerarquias()),
      concatMap(() => this.obtenerFuncionario()),
      concatMap(() => this.obtenerAdolescentes()),
      concatMap(() =>
        iif(
          () => this.tokenID ? true : false,
          this.obtenerTraslado(),
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

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tokenTarea = params['tokenTarea'];
        if (tokenTarea) {
          this.tareaSaliente = this.listaTareas.find(tarea => tarea.tokenIdentificador === tokenTarea);
        }
        const tokenID = this.route.snapshot.params['tokenID'];
        if (tokenID) {
          this.tokenID = tokenID;
        }
      })
    );
  }

  obtenerCatalogos(): Observable<any> {
    const nemonicosCatalogos = [
      'MOTIVOS_TRASLADO',
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));

    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.motivosTraslado = results[0]?.data;

        this.motivosTrasladoFiltrado = this.informeFormGroup.controls['motivoTraslado'].valueChanges.pipe(
          startWith(''),
          map(value => typeof value === 'string' ? this._filterMotivosTraslado(value) : this.motivosTraslado),
        );
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerJerarquias(): Observable<any> {
    const nemonicosJerarquias = [
      'CJDR',
    ];

    const solicitudes = nemonicosJerarquias.map(solicitud => this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre(solicitud, this.nemonicoMenu));

    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.centrosOrigen = results[0]?.data;

        this.centrosOrigenFiltrado = this.informeFormGroup.controls['centroOrigen'].valueChanges.pipe(
          startWith(''),
          map(value => typeof value === 'string' ? this._filterCentrosOrigen(value) : this.centrosOrigen),
        );
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerFuncionario(): Observable<any> {
    return this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).pipe(
      tap((response) => {
        this.funcionarioActivo = response.data;
        console.log(this.funcionarioActivo);
        

        const centroOrigen = this.centrosOrigen.find(centro => centro.nombre == this.funcionarioActivo.departamento);
        this.centroOrigen = centroOrigen;
        this.informeFormGroup.controls['centroOrigen'].setValue(centroOrigen);

        this.informeFormGroup.controls['centroOrigen'].disable();

        this.centrosDestino = [...this.centrosOrigen];
        this.centrosDestino = this.centrosDestino.filter(centro => centro.nombre != centroOrigen.nombre);

        this.centrosDestinoFiltrado = this.informeFormGroup.controls['centroDestino'].valueChanges.pipe(
          startWith(''),
          map(value => typeof value === 'string' ? this._filterCentrosDestino(value) : this.centrosDestino),
        );

      }),
      catchError(err => {
        this.funcionarioService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerTraslado(): Observable<any> {
    return this.trasladoService.obtenerTrasladoPorTokenID(this.tokenID, this.nemonicoMenu).pipe(
      tap((response) => {
        this.informeFormGroup.patchValue(response.data);
        this.traslado = response.data;
        this.trasladoCargado = true;
        this.dataSource = new MatTableDataSource();
        for (let traslado of this.traslado.trasladoAdolescentes) {
          this.dataSource.data.push(traslado.fichaIdentificacion);
        }
        this.dataSource.data.sort((a, b) => a.apellidoPaterno.toLowerCase().localeCompare(b.apellidoPaterno.toLowerCase()));
        this.dataSource.paginator = this.paginatorAdolescente;
      }),
      catchError(err => {
        this.trasladoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerAdolescentes(): Observable<any> {
    return this.fichaIdentificacionService.obtenerNombresFichas(null, this.centroOrigen.tokenIdentificador).pipe(
      tap((response) => {
        // this.adolescentes = response.data;
        this.adolescentes = (response.data || []).filter(ficha => ficha.tieneProceso !== true);
        console.log(this.adolescentes);
        this.datosCargados = true;

        this.adolescentesFiltrado = this.informeFormGroup.controls['adolescente'].valueChanges.pipe(
          startWith(''),
          map(value => typeof value === 'string' ? this._filterAdolescentes(value) : this.adolescentes),
        );

        this.adolescentes.sort((a, b) => a.apellidoPaterno.toLowerCase().localeCompare(b.apellidoPaterno.toLowerCase()));

      }),
      catchError(err => {
        this.fichaIdentificacionService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  private _filterMotivosTraslado(value: string): CatalogoDTO[] {
    const filterValue = value.toLowerCase();

    return this.motivosTraslado.filter(option => option.nombre.toLowerCase().includes(filterValue))
  }

  private _filterCentrosOrigen(value: string): JerarquiaDTO[] {
    const filterValue = value.toLowerCase();

    return this.centrosOrigen.filter(option => option.nombre.toLowerCase().includes(filterValue))
  }

  private _filterCentrosDestino(value: string): JerarquiaDTO[] {
    const filterValue = value.toLowerCase();

    return this.centrosDestino.filter(option => option.nombre.toLowerCase().includes(filterValue))
  }

  private _filterAdolescentes(value: string): FichaIdentificacionDTO[] {
    const filterValue = value.toLowerCase();

    return this.adolescentes.filter(option => {
      const nombreCompleto = `${option.apellidoPaterno} ${option.apellidoMaterno} ${option.nombres}`.toLowerCase();
      return nombreCompleto.includes(filterValue)
    })
  }

  displayFn(option: JerarquiaDTO): string {
    return option && option.nombre ? option.nombre : '';
  }

  displayFnMotivosTraslado(option: CatalogoDTO): string {
    return option && option.nombre ? option.nombre : '';
  }

  displayFnAdolescente(option: FichaIdentificacionDTO): string {
    return option && option.apellidoPaterno && option.apellidoMaterno && option.nombres
      ? `${option.apellidoPaterno} ${option.apellidoMaterno} ${option.nombres}`
      : '';
  }

  obtenerCentrosDestino(event: any) {
    this.informeFormGroup.controls['centroDestino'].reset();
    const centroSelecionado: JerarquiaDTO = event.option.value;
    this.centrosDestino = [...this.centrosOrigen];
    this.centrosDestino = this.centrosDestino.filter(centro => centro.nombre != centroSelecionado.nombre);

    this.centrosDestinoFiltrado = this.informeFormGroup.controls['centroDestino'].valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? this._filterCentrosDestino(value) : this.centrosDestino),
    );
  }

  cancelar() {
    // this.router.navigate([`/flujo-trabajo/bandeja-entrada`]);
    this._location.back();
  }

  esFormularioInvalido(): boolean {
    const datosInvalidos = this.informeFormGroup.invalid
      || this.dataSource.data.length < 1;
    return datosInvalidos;
  }

  guardarTraslado() {
    this.route.queryParams.subscribe(params => {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de traslado',
        "Deseas continuar?"
      );

      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              if (this.esNuevo) {
                this.crearInstancia();
              }
              else {
                this.crearEditarTraslado();
              }
            }
          }
        }
      );
    })
  }

  guardarBorrador() {

    this.esBorrador = true;

    if (this.esNuevo) {
      this.crearInstancia();
    }
    else {
      this.crearEditarBorrador();
    }
  }

  crearInstancia() {
    this.flujoTrabajoService.crearInstanciaProcesoPorProceso(this.proceso, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<InstanciaProcesoDTO>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }

          if (response.data) {
            this.tareaEntrante = response.data.tareas.find(t => t.orden == 1);

            if (this.esBorrador)
              this.crearEditarBorrador();
            else
              this.crearEditarTraslado();
          }
        },
        error: (error: any) => {
          this.flujoTrabajoService.checkError(error);
        }
      }
    );
  }

  crearEditarTraslado() {
    const centroOrigen = this.centrosOrigen.find(centro => centro.nombre == this.funcionarioActivo.departamento);
    this.traslado.centroOrigen = centroOrigen;

    Object.assign(this.traslado, this.informeFormGroup.value);
    this.traslado.trasladoAdolescentes = [];
    for (let item of this.dataSource.data) {
      let tras = new TrasladoAdolescenteDTO;
      tras.fichaIdentificacion = item;
      tras.estadoEvento = this.estadoEvento
      tras.isComplete = false;
      this.traslado.trasladoAdolescentes.push(tras);
    }

    // HTML DE DETALLE DE CORREO
    this.traslado.html = `<br>
                          <strong>Centro de origen:</strong>${this.traslado.centroOrigen.nombre}<br>
                          <strong>Centro de destino:</strong>${this.traslado.centroDestino.nombre}<br>
                          <strong>Motivo de traslado:</strong>${this.traslado.motivoTraslado.nombre}<br>
                          <br>
                          <strong>Adolescentes:</strong>
                          <ul>
                          `;
    for (let adolescente of this.traslado.trasladoAdolescentes) {
      const nombreAdolescente = `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}`;
      const numeroIdentificacion = adolescente.fichaIdentificacion.numeroIdentificacion
      const htmlNombre = `<li>${nombreAdolescente} - ${numeroIdentificacion}</li>`;
      this.traslado.html += htmlNombre;
    }
    this.traslado.html += `</ul>
                          <br>
                          <br>
                          ${this.funcionarioActivo.nombres} ${this.funcionarioActivo.apellidos}<br>
                          ${this.funcionarioActivo.cargo}<br>
                          ${this.funcionarioActivo.departamento}
                          `;

    let tareaTraslado = new TareaTrasladoDTO;
    tareaTraslado.traslado = this.traslado;
    tareaTraslado.tarea = this.tareaEntrante;
    this.traslado.usuarioCreaTraslado = `${this.funcionarioActivo?.nombres ?? ''} ${this.funcionarioActivo?.apellidos ?? ''}`.trim();
    this.trasladoService.crearEditarTraslado(tareaTraslado, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<TrasladoDTO>) => {

          if (!response.exito) {
            this.trasladoService.checkError(response);

            return;
          }
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.router.navigate([`/flujo-trabajo/bandeja-salida`])

        },
        error: (error: any) => {
          this.trasladoService.checkError(error);
        }
      }
    )
  }

  crearEditarBorrador() {
    const centroOrigen = this.centrosOrigen.find(centro => centro.nombre == this.funcionarioActivo.departamento);
    this.traslado.centroOrigen = centroOrigen;

    Object.assign(this.traslado, this.informeFormGroup.value);

    this.traslado.trasladoAdolescentes = [];

    for (let item of this.dataSource.data) {
      let tras = new TrasladoAdolescenteDTO;
      tras.fichaIdentificacion = item;
      tras.estadoEvento = this.estadoEvento
      tras.isComplete = false;
      this.traslado.trasladoAdolescentes.push(tras);
    }

    let tareaTraslado = new TareaTrasladoDTO;
    tareaTraslado.traslado = this.traslado;
    tareaTraslado.tarea = this.tareaEntrante;

    this.trasladoService.guardarBorrador(tareaTraslado, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<TrasladoDTO>) => {

          if (!response.exito) {
            this.trasladoService.checkError(response);

            return;
          }
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.router.navigate([`/flujo-trabajo/bandeja-borrador`])

        },
        error: (error: any) => {
          this.trasladoService.checkError(error);
        }
      }
    )
  }

  agregarAdolescente() {
    const adolescente = this.informeFormGroup.controls['adolescente'].value;
    this.dataSource.data.unshift(adolescente);
    this.dataSource.paginator = this.paginatorAdolescente;
    this.informeFormGroup.controls['adolescente'].reset();
    this.adolescentes = this.adolescentes.filter(item => item.tokenIdentificador !== adolescente.tokenIdentificador);
    this.adolescentes.sort((a, b) => a.apellidoPaterno.toLowerCase().localeCompare(b.apellidoPaterno.toLowerCase()));
    this.adolescentesFiltrado = this.informeFormGroup.controls['adolescente'].valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? this._filterAdolescentes(value) : this.adolescentes),
    );
    this.habilitarAgregar = false;
  }

  eliminarAdolescente(index: number) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Se eliminará el registro seleccionado de la lista",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            const eliminado = this.dataSource.data.splice(index, 1);
            this.dataSource = new MatTableDataSource(this.dataSource.data);
            this.adolescentes.push(eliminado[0]);
            this.adolescentes.sort((a, b) => a.apellidoPaterno.toLowerCase().localeCompare(b.apellidoPaterno.toLowerCase()));
            this.adolescentesFiltrado = this.informeFormGroup.controls['adolescente'].valueChanges.pipe(
              startWith(''),
              map(value => typeof value === 'string' ? this._filterAdolescentes(value) : this.adolescentes),
            );
          }
        }
      }
    )

  }

  async generarPdf() {
    Object.assign(this.traslado, this.informeFormGroup.value);

    let listaAdolescentes: string = '';

    for (let adolescente of this.traslado.trasladoAdolescentes) {
      listaAdolescentes += `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}, `;
    }

    let tablaAdolescentes = new TablaPlantilla();
    tablaAdolescentes.encabezados = [
      'Nombre', 'DNI'
    ];

    tablaAdolescentes.filas = this.traslado.trasladoAdolescentes.map(adolescente => {
      console.log(adolescente);
      return {
        'Nombre': `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}`,
        'DNI': adolescente.fichaIdentificacion.numeroIdentificacion ?? ""
      };
    });

    const fechaActual = new Date();

    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_TRASLADO;
    request.variables = {
      "[TITULO-PLANTILLA]": "Informe de traslado",
      "[IMG_BASE64]": await this.funcionesUtils.obtenerLogoPdf(),
      "[TITULO-INFORME]": this.tareaEntrante.paso.nombre,
      "[FECHA]": this.funcionesUtils.formatearFecha(fechaActual),
      "[HORA]": this.funcionesUtils.formatearHora(fechaActual),
      "[CENTRO]": this.traslado.centroOrigen.nombre,
      "[CENTRO-ORIGEN]": this.traslado.centroOrigen.nombre,
      "[CENTRO-DESTINO]": this.traslado.centroDestino.nombre,
      "[TIPO-TRASLADO]": this.traslado.motivoTraslado.nombre,
      "[TABLA-ADOLESCENTES]": JSON.stringify(tablaAdolescentes),
      "[ANTECEDENTES]": this.traslado.antecedentes,
      "[ANALISIS]": this.traslado.analisis,
      "[CONCLUSIONES]": this.traslado.conclusiones,
      "[RECOMENDACIONES]": this.traslado.recomendaciones
    }
    this.pdfService.generarPdf(request, '').subscribe({
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

        // // Crear un enlace y disparar la descarga
        // const a = document.createElement('a');
        // a.href = url;
        // a.download = 'archivo.pdf'; // Nombre del archivo
        // document.body.appendChild(a);
        // a.click();

        // // Limpiar la URL y remover el enlace
        // window.URL.revokeObjectURL(url);
        // document.body.removeChild(a);
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  estadoInicial() {
    this.catalogoService.obtenerCatalogoPorNemonico(
      etiquetasModel.NEMONICO_ESTADO_SALIDA_ACTIVO,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (respuesta: RespuestaPorDefecto<CatalogoDTO>) => {
          if (!respuesta.exito) {
            this.catalogoService.checkError(respuesta);
            return;
          }
          this.estadoEvento = respuesta.data

        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );

  }

}
