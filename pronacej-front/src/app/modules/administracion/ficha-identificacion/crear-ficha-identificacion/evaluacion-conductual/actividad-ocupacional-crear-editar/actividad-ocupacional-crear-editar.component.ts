import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule, Location } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTable, MatTableModule } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { ActividadOcupacionalDTO } from 'app/core/model/both/ActividadOcupacional.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { SeguimientoActividadOcupacionalDTO } from 'app/core/model/both/SeguimientoActividadOcupacional.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SeguimientoService } from 'app/modules/administracion/services/seguimiento.service';
import { EvaluacionConductualService } from 'app/modules/seguridad/services/evaluacionConductual.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import { CrearEditarSeguimientoActividadOcupacionalComponent } from './crear-editar-seguimiento-actividad-ocupacional/crear-editar-seguimiento-actividad-ocupacional.component';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { TabService } from 'app/core/services/tab.service';
import { HttpClient } from '@angular/common/http';
import etiquetasModel from 'app/core/etiquetas.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { PdfService } from 'app/core/services/pdf.service';

@Component({
  selector: 'app-actividad-ocupacional-crear-editar',
  standalone: true,
  imports: [CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatRadioModule,
    MatSelectModule,
    MatLabel,
    MatInputModule,
    MatButtonModule,
    MatExpansionModule,
    MatTableModule,
    MatIconModule,
    MatPaginatorModule,
    MatSlideToggleModule],
  templateUrl: './actividad-ocupacional-crear-editar.component.html',
  styleUrl: './actividad-ocupacional-crear-editar.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ],
})
export class ActividadOcupacionalCrearEditarComponent {

  esEdicion: boolean = false;
  token: string;
  item: ActividadOcupacionalDTO;

  actividadOcupacionalForm: FormGroup;

  
  // Variable para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ACTIVIDAD_SOCIO_RECREATIVA;

  tiposActividadOcupacional: CatalogoDTO[] = [];
  tiposDocumentoAprobacion: CatalogoDTO[] = [];
  estadosActividadOcupacional: CatalogoDTO[] = [];
  tiposPrograma: CatalogoDTO[] = [];
  uuid_fp: string;
  centro: JerarquiaDTO;
  programas: JerarquiaDTO[] = [];
  ambientes: JerarquiaDTO[] = [];

  listSizeSeguimientoActividadOcupacional = [5, 10, 15, 20];
  pageSeguimientoActividadOcupacional = 0;
  sizeSeguimientoActividadOcupacional = this.listSizeSeguimientoActividadOcupacional[0];
  totalItemsSeguimientoActividadOcupacional = 0;
  listaSeguimientoActividadOcupacional: SeguimientoActividadOcupacionalDTO[] = [];
  dataSourceSeguimientoActividadOcupacional: CdkTableDataSourceInput<SeguimientoActividadOcupacionalDTO>;

  @ViewChild('tablaSeguimiento')
  tableSeguimiento: MatTable<SeguimientoActividadOcupacionalDTO>;

  keyLabelsTableEnfermedadFicha: any = {
    acciones: 'Acciones',
    actividad: 'Actividad',
    observaciones: 'Observaciones',
    fechaActividad: 'Fecha Actividad',
  };

  constructor(
    private fb: FormBuilder,
    private seguimientoService: SeguimientoService,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils,
    private location: Location,
    private evaluacionConductualService: EvaluacionConductualService,
    private route: ActivatedRoute,
    private dateAdapter: DateAdapter<any>,
    private jerarquiaService: JerarquiaService,
    public dialog: MatDialog,
    private tabService: TabService,
    private http: HttpClient,
    private fichaIdentificacionService: FichaIdentificacionService,
    public pdfService: PdfService,
  ) {
    this.dateAdapter.setLocale('es');
    this.actividadOcupacionalForm = this.fb.group({
      // tipoPrograma: ['', Validators.required],
      tipoActividadOcupacional: ['', Validators.required],
      // tipoDocumentoAprobacion: ['', Validators.required],
      estadoActividadOcupacional: ['', Validators.required],
      fechaInicio: ['', Validators.required],
      numeroDocumento: ['', Validators.required],
      objetivoActividad: ['', Validators.required],
      programa: [JerarquiaDTO, Validators.required],
      ambiente: [JerarquiaDTO,],
      documentoAprobacion: ['', Validators.required],

    });
  }


  ngOnInit(): void {
    this.token = history.state.token;
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.cargarCentro();

    this.funcionesUtils.obtenerListaCatalogo('PROGRAMA_ACTIVIDAD_OCUPACIONAL', '').subscribe({
      next: (data) => {
        this.tiposPrograma = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });
    this.funcionesUtils.obtenerListaCatalogo('ESTADO_ACTIVIDAD_OCUPACIONAL', '').subscribe({
      next: (data) => {
        this.estadosActividadOcupacional = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });
    this.funcionesUtils.obtenerListaCatalogo('TIPO_ACTIVIDAD_OCUPACIONAL', '').subscribe({
      next: (data) => {
        this.tiposActividadOcupacional = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });
    this.funcionesUtils.obtenerListaCatalogo('TIPO_DOCUMENTO_APROBADO', '').subscribe({
      next: (data) => {
        this.tiposDocumentoAprobacion = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });

    this.item = history.state.conductualDTO;
    console.log('item conductualDTO', this.item);
    if (this.item) {
      this.esEdicion = true;
      // this.actividadOcupacionalForm.controls.tipoPrograma.setValue(this.item.tipoPrograma);
      this.actividadOcupacionalForm.controls.tipoActividadOcupacional.setValue(this.item.tipoActividadOcupacional);
      // this.actividadOcupacionalForm.controls.tipoDocumentoAprobacion.setValue(this.item.tipoDocumentoAprobacion);
      // this.actividadOcupacionalForm.get("tipoDocumentoAprobacion")?.setValue(this.item.tipoDocumentoAprobacion);
      if (this.item.fechaInicio) {
        let fechaInicio = new Date(this.item.fechaInicio);
        this.actividadOcupacionalForm.get("fechaInicio")?.setValue(fechaInicio);
      }
      this.actividadOcupacionalForm.controls.numeroDocumento.setValue(this.item.numeroDocumento);
      this.actividadOcupacionalForm.controls.objetivoActividad.setValue(this.item.objetivoActividad);
      this.actividadOcupacionalForm.controls.estadoActividadOcupacional.setValue(this.item.estadoActividadOcupacional);
      this.actividadOcupacionalForm.controls.programa.setValue(this.item.programa);
      if (this.item?.ambiente) {
        this.actividadOcupacionalForm.controls.ambiente.setValue(this.item?.ambiente);
      }
      this.actividadOcupacionalForm.controls.documentoAprobacion.setValue(this.item.documentoAprobacion);


      this.obtenerSeguimientos(this.item.tokenIdentificador);

      if (this.item.esVisualizacion) {
        this.actividadOcupacionalForm.disable();
      }
    }



    this.actividadOcupacionalForm.get('programa')?.valueChanges.subscribe((programa: JerarquiaDTO) => {
      if (programa) {
        this.cargarCentros(false, programa.tokenIdentificador);
      } else {
        this.ambientes = []; // Resetear ambientes si no hay selección
        this.actividadOcupacionalForm.get('ambiente')?.setValue(null);
      }
    });
  }

  /**
   * Guarda o actualiza la actividad ocupacional
   */
  guardar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }

    if (this.actividadOcupacionalForm.invalid) {
      this.dialogMensajeService.mensajeErrorConTitulo("Error", "Complete los campos obligatorios");
      return;
    }

    // Establecer bandera de procesamiento
    this.estaProcesandoGuardado = true;

    const actividadDTO: ActividadOcupacionalDTO = {
      ...this.actividadOcupacionalForm.value,
      esEdicion: this.esEdicion,
      tokenIdentificador: this.esEdicion ? this.item?.tokenIdentificador : undefined,
      tokenFichaIdentificacion: this.uuid_fp
    };

    this.evaluacionConductualService.crearOActualizarActividadOcupacional(actividadDTO, this.nemonicoMenu).subscribe({
      next: (response) => {
        // Restablecer bandera de procesamiento
        this.estaProcesandoGuardado = false;
        
        if (response.exito) {
          this.dialogMensajeService.mensajeExitoso("Guardar", response.mensaje).afterClosed().subscribe(() => {
            this.tabService.cambiarTab(1);
            this.location.back();
          });
        } else {
          this.dialogMensajeService.mensajeErrorConTitulo("Error", response.mensaje);
        }
      },
      error: (err) => {
        // Restablecer bandera de procesamiento en caso de error
        this.estaProcesandoGuardado = false;
        this.dialogMensajeService.mensajeError("Hubo un problema al guardar el registro. Inténtalo de nuevo.");
      }
    });
  }

  cancelar() {
    this.tabService.cambiarTab(1);
    this.location.back();
  }

  validarSoloNumeros(event: KeyboardEvent): boolean {
    const charCode = event.which ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }

  compararCatalogos(o1: CatalogoDTO, o2: CatalogoDTO): boolean {
    return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
  }

  compararJerarquia(o1: JerarquiaDTO, o2: JerarquiaDTO): boolean {
    return o1 && o2 ? o1.tokenIdentificador === o2.tokenIdentificador : o1 === o2;
  }

  cargarCentro() {
    this.jerarquiaService
      .obtenerJerarquiaPorNumeroDeDocumento('')
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!environment.production) {
            console.log(respuesta.data);
          }
          if (!respuesta.exito) {
            this.jerarquiaService.checkError(respuesta);
            return;
          }

          this.centro = respuesta.data;
          if (this.centro.jerarquiaPadre.nemonico == 'UAPISE') {
            this.actividadOcupacionalForm.get('programa')?.clearValidators();
            this.actividadOcupacionalForm.get('programa')?.updateValueAndValidity();
          } else {
            if (this.centro.nemonico != 'SOA' && this.centro.nemonico != 'CJDR') {
              this.cargarCentros(true);
            }
          }

        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
  }

  cargarCentros(programa: Boolean, tokenPadre: string = ''): void {

    this.jerarquiaService
      .obtenerJerarquiasPorTokenPadre('', '', tokenPadre ? tokenPadre : this.centro.tokenIdentificador)
      .subscribe({
        next: (resp: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (resp.exito) {
            if (programa) {
              this.programas = resp.data;
              if (this.esEdicion) {
                this.cargarCentros(false, this.item.programa.tokenIdentificador);
              }
            } else {
              this.ambientes = resp.data;
            }
            console.log('Centros cargados:', resp.data);
          } else {
            console.warn('Ocurrió un problema al cargar los centros:', resp.mensaje);
          }
        },
        error: (error: any) => {
          console.error('Error al cargar los centros:', error);
        }
      });
  }

  async obtenerSeguimientos(tokenIdentificador: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.sizeSeguimientoActividadOcupacional;
    paginacionRequest.page = this.pageSeguimientoActividadOcupacional;
    paginacionRequest.tokenIdentificador = tokenIdentificador;

    this.evaluacionConductualService
      .obtenerSeguimientosPorActividadOcupacional(paginacionRequest)
      .subscribe({
        next: (
          response: RespuestaPorDefecto<
            PaginacionResponse<SeguimientoActividadOcupacionalDTO>
          >
        ) => {
          if (!environment.production) {
            console.log('respuesta enfermedades ficha', response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }
          this.listaSeguimientoActividadOcupacional = response.data.data;
          this.dataSourceSeguimientoActividadOcupacional = this.listaSeguimientoActividadOcupacional;
          this.totalItemsSeguimientoActividadOcupacional = response.data.totalItems;
          // this.tableSeguimiento.renderRows();
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  aniadirFilaInformacion() {
    const dialogRef = this.dialog.open(
      CrearEditarSeguimientoActividadOcupacionalComponent,
      {
        data: {
          uuid_fp: this.uuid_fp,
          actividadOcupacional: this.item.tokenIdentificador
        },
        width: '600px',
        disableClose: true
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Al cerrar el modal con resultado, recargar la tabla con los nuevos datos.
        this.obtenerSeguimientos(this.item.tokenIdentificador);
      }
    });
  }

  getKeysSeguimiento() {
    return Object.keys(this.keyLabelsTableEnfermedadFicha);
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.sizeSeguimientoActividadOcupacional = pageEvent.pageSize;
    this.pageSeguimientoActividadOcupacional = pageEvent.pageIndex;
    this.obtenerSeguimientos(this.item.tokenIdentificador);
  }

  editarFilaInformacion(informacion: SeguimientoActividadOcupacionalDTO) {
    const dialogRef = this.dialog.open(
      CrearEditarSeguimientoActividadOcupacionalComponent,
      {
        data: {
          informacion: informacion,
          uuid_fp: this.uuid_fp,
          actividadOcupacional: this.item.tokenIdentificador
        },
        width: '600px',
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Al cerrar el modal con resultado, recargar la tabla con los nuevos datos.
        this.obtenerSeguimientos(this.item.tokenIdentificador);
      }
    });
  }

  eliminarActividadIntervencion(actividad: SeguimientoActividadOcupacionalDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Está seguro de eliminar el registro?",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.evaluacionConductualService.eliminarSeguimientoActividadOcupacional(actividad).subscribe({
              next: (response) => {
                if (response.exito) {
                  this.obtenerSeguimientos(this.item.tokenIdentificador);
                } else {
                  this.dialogMensajeService.mensajeErrorConTitulo(
                    response.titulo,
                    response.mensaje
                  )
                }
              },
              error: (error) => {
                this.evaluacionConductualService.checkError(error);
                console.error('Error en la solicitud:', error);
              }
            });
          }
        }
      }
    )


  }

  soloAlfanumerico(event: KeyboardEvent): void {
    const allowedKeys = [
      'Backspace',
      'ArrowLeft',
      'ArrowRight',
      'Tab',
      'Delete'
    ];

    const isNumberKey = event.key >= '0' && event.key <= '9';
    const isLetterKey = (event.key >= 'a' && event.key <= 'z') || (event.key >= 'A' && event.key <= 'Z');

    if (!isNumberKey && !isLetterKey && !allowedKeys.includes(event.key)) {
      event.preventDefault();
    }
  }

  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.actividadOcupacionalForm.get(controlName).setValue(fecha);
    }
  }

  async imprimirFicha() {
    try {
      // Mostrar diálogo de carga
      const dialogoCarga = this.dialogMensajeService.mensajeLoading('Generando PDF...');

      // Obtener imagen del logo para incluir en el PDF
      this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
        .subscribe({
          next: async (datos: ArrayBuffer) => {
            // Convertir imagen a base64 para incluirla en el PDF
            const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
            const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

            try {
              // Obtener datos de cabecera de la ficha de identificación
              // const datosCabecera = await this.obtenerDatosCabecera();

              // Crear tabla de personas relacionadas para el PDF utilizando TablaPlantilla
              // Solo incluimos las columnas: Nombre completo, Parentesco, Identificación
              let tablaPersonasRelacionadas = new TablaPlantilla();
              tablaPersonasRelacionadas.encabezados = [
                'Actividad', 'Observaciones', 'Fecha'
              ];

              // Mapear los datos de las personas relacionadas a la tabla con solo las columnas requeridas
              tablaPersonasRelacionadas.filas = this.listaSeguimientoActividadOcupacional.map(persona => {


                return {
                  'Actividad': persona.actividad,
                  'Observaciones': persona.observaciones,
                  'Fecha': this.funcionesUtils.formatearFecha(persona.fechaActividad),
                };
              });

              // Si no hay datos, agregar una fila con mensaje
              if (tablaPersonasRelacionadas.filas.length === 0) {
                tablaPersonasRelacionadas.filas.push({
                  'Actividad': '-',
                  'Observaciones': 'No hay seguimientos registrados',
                  'Fecha': '-'
                });
              }

              // Configurar solicitud para la generación del PDF
              const solicitudPdf = new GeneracionPdfRequest();
              solicitudPdf.nemonico = 'FORMULARIO_ACTIVIDAD_SOCIORECREATIVA';

              // Preparar variables para el PDF
              solicitudPdf.variables = {
                // Datos de cabecera
                "[IMG_BASE64]": imagenBase64,
                "[FECHA-INGRESO]": this.funcionesUtils.formatearFecha(new Date()),
                "[HORA-INGRESO]": new Date().toLocaleTimeString('es-ES'),
                "[CENTRO]": this.centro?.nombre || 'No especificado',
                "[TITULO-INFORME]": 'Informe de Seguimiento de Actividades Socio Recreativas',
                "[TITULO-PLANTILLA]": 'Informe de Seguimiento de Actividades Socio Recreativas',


                // Tabla de personas relacionadas (serializada como JSON)
                "[TABLA-SEGUIMIENTO-ACTIVIDADES]": JSON.stringify(tablaPersonasRelacionadas),

                // Información familiar general
                "[PROGRAMA]": this.item.programa?.nombre || 'No especificado',
                "[AMBIENTE]": this.item.ambiente?.nombre || 'No especificado',
                "[OBJETIVOS]": this.item.tipoActividadOcupacional?.nombre || 'No especificado',
                "[DOCUMENTO-APROBACION]": this.item.documentoAprobacion || 'No especificado',
                "[NUMERO-DOCUMENTO]": this.item.numeroDocumento || 'No especificado',
                "[ESTADO-ACTIVIDAD]": this.item.estadoActividadOcupacional?.nombre || 'No especificado',
                "[FECHA-INICIO]": this.funcionesUtils.formatearFecha(this.item.fechaInicio) || 'No especificado',

              };

              // Generar el PDF usando el servicio
              this.pdfService.generarPdf(solicitudPdf, etiquetasModel.NEMONICO_MENU_SITUACION_FAMILIAR).subscribe({
                next: (respuesta: RespuestaPorDefecto<string>) => {
                  dialogoCarga.close();

                  if (!respuesta.exito) {
                    this.dialogMensajeService.mensajeError(
                      'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                    );
                    return;
                  }

                  // Abrir el PDF en una nueva ventana
                  const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(respuesta.data));
                  window.open(url);
                },
                error: (error: any) => {
                  dialogoCarga.close();
                  console.error('Error al generar PDF:', error);
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
                  );
                }
              });
            } catch (error) {
              dialogoCarga.close();
              console.error('Error al procesar datos:', error);
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al procesar los datos. Inténtalo de nuevo.'
              );
            }
          },
          error: (error) => {
            dialogoCarga.close();
            console.error('Error al cargar imagen:', error);
            this.dialogMensajeService.mensajeError(
              'Error al cargar la imagen del logo.'
            );
          }
        });
    } catch (error) {
      console.error('Error al imprimir ficha:', error);
      this.dialogMensajeService.mensajeError(
        'Hubo un problema al generar el PDF.'
      );
    }
  }

  private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
    return new Promise((resolve, reject) => {
      this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            reject('Error al obtener la ficha de identificación');
            return;
          }

          const fichaIdentificacion = response.data;
          const datosCabecera = {
            "[NOMBRES-APELLIDOS]": `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim(),
            "[DNI]": fichaIdentificacion.numeroDocumento || '',
            "[LUGAR-FECHA-NACIMIENTO]": `${fichaIdentificacion.lugarNacimiento || ''} ${this.funcionesUtils.formatearFecha(fichaIdentificacion.fechaNacimiento)}`,
            "[EDAD]": `${this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento)}`
          };

          resolve(datosCabecera);
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }
}


