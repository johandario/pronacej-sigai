import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormControl, FormsModule, UntypedFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter, DateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { forkJoin, Observable } from 'rxjs';
import { CommonModule, Location } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { MatRadioModule } from '@angular/material/radio';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { ActaExternamientoDTO } from 'app/core/model/both/ia/actaExternamientoDTO.model';
import { MatTabsModule } from '@angular/material/tabs';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { InstitucionService } from '../../institucion.service';
import { AdolescenteDerivadoInstService } from '../adolescente-derividado.service';
import { AdolescDerivadoInstDTO } from 'app/core/model/both/salida/AdolescDerivadoInstDTO.model';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { SeguimientoAdoelscInstService } from '../seguimiento-adolesc.service';
import { SeguimientoAdolescInstDTO } from 'app/core/model/both/salida/SeguimientoAdolcInstDTO.model';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { Sort } from '@angular/material/sort';
import { CUSTOM_DATE_FORMATS, FuncionesUtils, CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { TabService } from 'app/core/services/tab.service';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';


export function noWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (typeof value === 'string') {
      const isWhitespace = value.trim().length === 0;
      const isValid = !isWhitespace;
      return isValid ? null : { whitespace: true };
    }
    return null;
  };
}


@Component({
  selector: 'app-gest-adolescente-derivado',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatButtonModule,
    FormsModule,
    SubidaDeDocumentosComponent,
    MatExpansionModule,
    CommonModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatIconModule,
    QuillModule,
    MatPaginatorModule,
    MatTableModule,
    MatCardModule,
    MatRadioModule,
    MatTabsModule,
    TablaListaComponent,
    RouterLink,
    CommonModule
  ],
  templateUrl: './gest-adolescente-derivado.component.html',
  styleUrl: './gest-adolescente-derivado.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
  ],
})
export class GestAdolescenteDerivadoComponent implements OnInit {
  isLoading: boolean = false;
  tokenID: string;
  fuga: AdolescDerivadoInstDTO = new AdolescDerivadoInstDTO();
  horaISO: string
  fechaISO: string
  horaISODerivacion: string
  fechaISODerivacion: any
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();
  uuid_fp: string;
  uuid_adolescente: string;
  catalogosMotivoSalida: CatalogoDTO[] = [];
  dataSource: any[] = [];
  listaProcesos: SeguimientoAdolescInstDTO[] = [];
  listaInstitucion: RegistroInstitucionDTO[] = [];
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  motivoSalidaSeleccionado: string | null = null;
  registrosDataSource = new MatTableDataSource<any>([]);
  displayedColumns: string[] = ['noDocumento', 'fecha', 'select'];
  selectedRegistro: any = null;
  dataSourceTraslado: any[] = [];
  dataSourcePermisoSalida: any[] = [];
  dataSourceExternamiento: any[] = [];
  estado: string[] = ['Aprobado', 'Pendiente'];
  actualDate: any;
  centro: JerarquiaDTO;
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  proceso: any
  indiceTabSeleccionado: number = 0;
  base64Image: string | null = null;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    idAdolescenteSeguimiento: "No.",
    acciones: "Acciones",
    medioEntrevista: "Modalidad entrevista",
    observacion: "Observación",
    recomendacion: "Recomendación",
    resultadoEntrevista: "Resultado de entrevista",
    fechaSeguimiento: "Fecha de seguimiento",

  };

  informeSucesosForm = this.fb.group({
    fechaRegistro: ['', Validators.required],
    horaRegistro: ['', Validators.required],
    estado: ['', Validators.required],
    fechaDerivacion: ['', Validators.required],
    horaDerivacion: ['', Validators.required],
    departamento: ['', [Validators.required, noWhitespaceValidator()]],
    tiempoServicio: ['', [Validators.required, noWhitespaceValidator()]],
    servicio: ['', [Validators.required, noWhitespaceValidator()]],
    personaResponsable: ['', [Validators.required, noWhitespaceValidator()]],
    institucion: ['', Validators.required],

  });

  @Input({ required: true }) declare nemonicoMenu: string;
  adolescentes: FichaIdentificacionDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  esVisualizar: boolean = false;
  nroDocumento: string
  paginacion: Paginacion = new Paginacion();
  listaActas: ActaExternamientoDTO[] = [];


  constructor(
    private router: Router, private route: ActivatedRoute,
    private adolescenteService: AdolescenteDerivadoInstService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private institucionService: InstitucionService,
    private seguimientoAdoelscente: SeguimientoAdoelscInstService,
    private jerarquiaService: JerarquiaService,
    private servicioTab: TabService,
    private location: Location,
    private http: HttpClient,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
  ) { }

  ngOnInit(): void {
    this.servicioTab.tabIndex$.subscribe(indice => {
      this.indiceTabSeleccionado = indice;
    });
    this.cargarCentro();
    this.uuid_fp = history.state.tokenAdolescente;
    this.proceso = history.state?.proceso;
    this.actualDate = new Date();
    const currentHour = this.actualDate.getHours().toString().padStart(2, '0');
    const currentMinutes = this.actualDate.getMinutes().toString().padStart(2, '0');
    const formattedTime = `${currentHour}:${currentMinutes}`;
    this.informeSucesosForm.patchValue({
      fechaRegistro: this.actualDate,
      horaRegistro: formattedTime,
    });

    this.fuga.tokenFichaIdentificacion = this.uuid_fp;

    forkJoin([this.obtenerInstituciones(), this.obtenerProcesos()]).subscribe({
      next: () => {
        if (this.proceso && history.state.hasOwnProperty('editar')) {
          this.inicializarFormulario();
          this.fuga = this.proceso;
        } else if (this.proceso && history.state.hasOwnProperty('visualizar')) {
          this.esVisualizar = history.state.visualizar ?? false;
          this.inicializarFormulario();
          this.informeSucesosForm.disable();
          this.fuga = this.proceso;
        }
      },
      error: (error) => {
        console.error("Error en carga inicial:", error);
      }
    });
  }

  inicializarFormulario() {
    let proceso = this.proceso;
    let fechaRegistroFormateada: Date | null = null;
    let horaISO: string | null = null;
    let fechaDerivacionFormateada: Date | null = null;
    let horaISODerivacion: string | null = null;

    if (proceso.fechaRegistro) {
      const fecha = new Date(proceso.fechaRegistro);
      fechaRegistroFormateada = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
      horaISO = fecha.getHours().toString().padStart(2, '0') + ':' + fecha.getMinutes().toString().padStart(2, '0');
    }

    if (proceso.fechaDerivacion) {
      const fechaDerivacion = new Date(proceso.fechaDerivacion);
      fechaDerivacionFormateada = new Date(fechaDerivacion.getFullYear(), fechaDerivacion.getMonth(), fechaDerivacion.getDate());
      horaISODerivacion = fechaDerivacion.getHours().toString().padStart(2, '0') + ':' + fechaDerivacion.getMinutes().toString().padStart(2, '0');
      this.fechaISODerivacion = fechaDerivacionFormateada
    }
    const institucionSeleccionada = this.listaInstitucion.find(inst =>
      inst.idRegistroInstitucion === proceso.institucion?.idRegistroInstitucion
    );

    if (institucionSeleccionada) {
      this.informeSucesosForm.get('institucion')?.setValue(institucionSeleccionada);
    }
    this.informeSucesosForm.patchValue({
      fechaRegistro: fechaRegistroFormateada,
      horaRegistro: horaISO,
      fechaDerivacion: fechaDerivacionFormateada,
      horaDerivacion: horaISODerivacion,
      departamento: proceso.departamento,
      tiempoServicio: proceso.tiempoServicio,
      servicio: proceso.servicio,
      personaResponsable: proceso.personaResponsable,
      estado: proceso.estado
    });
  }

  cancelar() {
    // if (!this.uuid_fp) {
    //   console.error("Error: uuid_fp no está definido.");
    //   return;
    // }
    this.servicioTab.cambiarTab(2);
    const url = `/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`;
    this.router.navigate([url], {
      state: {
        tokenAdolescente: this.uuid_fp
      }
    });
  }

  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    let errores = [];
    Object.keys(this.informeSucesosForm.controls).forEach(key => {
      const control = this.informeSucesosForm.get(key);
      if (control?.errors) {
        console.log(`${key}:`, {
          Valor: control.value,
          Validez: control.valid,
          Errores: control.errors
        });
        if (control.errors['required']) {
          errores.push(`El campo "${key}" es obligatorio.`);
        }
        if (control.errors['specialCharacters']) {
          errores.push(`El campo "${key}" no permite caracteres especiales.`);
        }
        if (control.errors['whitespace']) {
          errores.push(`El campo "${key}" no puede contener solo espacios en blanco.`);
        }
      }
    });

    if (errores.length > 0) {
      this.dialogMensajeService.mensajeError(errores.join('\n'));
      return;
    }
    const esEdicion = !!this.fuga.tokenIdentificador;

    this.route.queryParams.subscribe(() => {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        esEdicion ? 'Se actualizará el registro del adolescente derivado a institución' : 'Se creará un registro de adolescente derivado a institución',
        "¿Deseas continuar?"
      );

      ref.afterClosed().subscribe({
        next: (resp: "confirmed" | "cancelled") => {
          if (resp === "confirmed") {
            Object.assign(this.fuga, this.informeSucesosForm.value);
            const fechaSeleccionada = this.informeSucesosForm.value.fechaRegistro;
            const horaSeleccionada = this.informeSucesosForm.value.horaRegistro;
            const fechaSeleccionadaDerivacion = this.informeSucesosForm.value.fechaDerivacion;
            const horaSeleccionadaDerivacion = this.informeSucesosForm.value.horaDerivacion;

            if (fechaSeleccionada && horaSeleccionada) {
              const [hora, minutos] = horaSeleccionada.split(':');
              const [horaDerivada, minutosDerivados] = horaSeleccionadaDerivacion.split(':');

              const fechaHoraCombinada = new Date(fechaSeleccionada);
              fechaHoraCombinada.setHours(Number(hora), Number(minutos));

              const fechaHoraCombinadaDerivada = new Date(fechaSeleccionadaDerivacion);
              fechaHoraCombinadaDerivada.setHours(Number(horaDerivada), Number(minutosDerivados));

              this.fuga.fechaRegistro = fechaHoraCombinada.toISOString();
              this.fuga.fechaDerivacion = fechaHoraCombinadaDerivada.toISOString();
            }

            this.fuga.tokenFichaIdentificacion = this.uuid_fp;
            this.adolescenteService.crearEditarSalida(this.fuga, esEdicion ? this.fuga.tokenIdentificador : '').subscribe({
              next: (response: RespuestaPorDefecto<AdolescDerivadoInstDTO>) => {
                if (!response.exito) {
                  this.adolescenteService.checkError(response);
                  return;
                }
                this.dialogMensajeService.mensajeExitoso(
                  esEdicion ? 'Registro actualizado con éxito' : 'Registro creado con éxito',
                  response.mensaje
                );
                this.servicioTab.cambiarTab(2);
                // this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`]);
                this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`], { queryParams: { tabIndex: 2 } });
              },
              error: (error: any) => {
                console.error('Error al guardar:', error);
                this.adolescenteService.checkError(error);
              },
            });
          }
        },
      });
    });
  }

  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select();
  }

  validarHora(): Validators {
    return (control: FormControl): { [key: string]: any } | null => {
      const hora = control.value;
      if (!hora) {
        return null;
      }
      const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
      if (!horaRegex.test(hora)) {
        return { invalid: true };
      }
      return null;
    };
  }

  validarHoraInput(event: Event): void {
    const input = (event.target as HTMLInputElement);
    const hora = input.value;
    const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
    const isValid = horaRegex.test(hora);
    if (!isValid) {
      this.informeSucesosForm.controls['horaSalida'].setErrors({ invalid: true });
    } else {
      this.informeSucesosForm.controls['horaSalida'].setErrors(null);
    }
  }

  obtenerProcesos(): Observable<void> {
    return new Observable(observer => {
      this.paginacionRequest.size = this.size;
      this.paginacionRequest.page = this.page;
      this.paginacionRequest.filter = this.paginacionRequest.filter;
      this.paginacionRequest.tokenIdentificador = this.proceso?.tokenIdentificador;

      if (this.proceso) {
        this.seguimientoAdoelscente.obtenerRegistroSalidas(this.paginacionRequest).subscribe({
          next: (response: RespuestaPorDefecto<PaginacionResponse<SeguimientoAdolescInstDTO>>) => {
            if (!response.exito) {
              this.institucionService.checkError(response);
              observer.error(response);
              return;
            }

            this.listaProcesos = response.data.data;
            observer.next();
            observer.complete();
          },
          error: (error: any) => {
            this.institucionService.checkError(error);
            observer.error(error);
          }
        });
      }

    });
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.proceso?.tokenIdentificador;

    if (this.proceso) {
      this.seguimientoAdoelscente.obtenerRegistroSalidas(this.paginacionRequest).subscribe({
        next: (response: RespuestaPorDefecto<PaginacionResponse<SeguimientoAdolescInstDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.institucionService.checkError(error);
        }
      });
    }
  }


  agregarSeguimiento() {
    this.router.navigate(['crear-editar-seguimiento-institucion'], {
      relativeTo: this.route,
      state: {
        crear: true,
        proceso: this.proceso,
        tokenAdolescente: this.fuga.tokenFichaIdentificacion
      }
    });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
  }


  obtenerInstituciones(): Observable<void> {
    return new Observable(observer => {
      let paginacionRequest = new PaginacionRequest();
      paginacionRequest.size = 1000;
      paginacionRequest.page = this.page;

      this.institucionService.obtenerRegistroInstituciones(paginacionRequest, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            observer.error(response);
            return;
          }

          console.log('instituciones', response);

          if (!response.data || !response.data.data || response.data.data.length === 0) {
            console.warn("devolvió una lista vacía.");
          } else {
            let filtroFinalidad = this.centro?.jerarquiaPadre?.nemonico === "SOA" ? "SOA" : "CJDR";
            this.listaInstitucion = response.data.data.filter(inst =>
              inst.estado === "Activo" && inst.finalidadInstitucion === filtroFinalidad
            );
          }

          observer.next();
          observer.complete();
        },
        error: (error: any) => {
          console.error("Error al obtener instituciones:", error);
          this.institucionService.checkError(error);
          observer.error(error);
        }
      });
    });
  }





  compareInstituciones(o1: RegistroInstitucionDTO, o2: RegistroInstitucionDTO): boolean {
    return o1 && o2 ? o1.idRegistroInstitucion === o2.idRegistroInstitucion : o1 === o2;
  }


  eliminarProceso(gestionFugaDTO: SeguimientoAdolescInstDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar este registro, esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la fuga..");
            this.seguimientoAdoelscente.eliminarSalida(gestionFugaDTO, "").subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);
                  if (!resp.exito) {
                    return;
                  }
                  this.obtenerProcesos();
                },
                error: (error: any) => {
                  load.close();
                }
              }
            );
          }
        }
      }
    );
  }

  editarProceso(proceso: SeguimientoAdolescInstDTO) {
    this.router.navigate(['crear-editar-seguimiento-institucion'], {
      relativeTo: this.route,
      state: {
        editar: true,
        proceso: proceso,
        tokenAdolescente: this.fuga.tokenFichaIdentificacion
      }
    });

    // this.router.navigate(['crear-editar-seguimiento-institucion'], {queryParams: {ID: proceso.tokenIdentificador}})
  }

  cargarCentro() {
    this.jerarquiaService
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!respuesta.exito) {
            this.jerarquiaService.checkError(respuesta);
            return;
          }
          this.centro = respuesta.data;
          if (this.centro.jerarquiaPadre?.nemonico === "CJDR") {
          } else if (this.centro.jerarquiaPadre?.nemonico === "SOA") {

          } else {
            this.catalogosMotivoSalida = [];
          }
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
  }

  handleSortEvent(event: Sort) {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    }
    else {
      this.paginacionRequest.sort = null;
      this.paginacionRequest.direction = null;
    }
    this.obtenerProcesos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerProcesos();
  }

  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.informeSucesosForm.get(controlName).setValue(fecha);
    }
  }

  cambiarPestana(indice: number) {
    this.indiceTabSeleccionado = indice;
  }

  regresar() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/postEgreso/${this.uuid_fp}`], { queryParams: { tabIndex: 2 } });
  }

  pruebaPdf() {
    this.loadImageAsBase64();
    const fechaRegistro = this.formatFecha((new Date).toString())
    const horaRegistro = this.formatHora((new Date).toString())
    const titulopantala = "Informe de adolescente derivado a institución"
    Object.assign(this.fuga, this.informeSucesosForm.value);
    const institucion = this.fuga.institucion.nombreOrganizacion;

    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_ADOLESCENTE_DERIVADO;
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]": titulopantala,
      "[TITULO-INFORME]": titulopantala,
      "[FECHA-REGISTRO]": this.actualDate,
      "[FECHA-DERIVACION]": this.fechaISODerivacion,
      "[HORA-REGISTRO]": horaRegistro,
      "[PERSONA-RESPONSABLE]": this.fuga.personaResponsable,
      "[ESTADO]": this.fuga.estado,
      "[NOMBRE-INSTITUCION]": institucion,
      "[DEPARTAMENTO]": this.fuga.departamento,
      "[TIEMPO-SERVICIO]": this.fuga.tiempoServicio,
      "[SERVICIO-BRINDAR]": this.fuga.servicio,

    }


    this.pdfService.generarPdf(request, '').subscribe({
      next: (response: RespuestaPorDefecto<string>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }
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

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatHora(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }

  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

}
