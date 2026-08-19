import { Component, OnInit, ViewChild, ElementRef, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { CommonModule, Location, registerLocaleData } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { RelacionEgresoService } from 'app/modules/administracion/services/relacionEgreso.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { ReforzamientoDTO } from 'app/core/model/both/salida/ReforzamientoDTO.model';
import { ActivatedRoute } from '@angular/router';
import { SesionReforzamientoDTO } from 'app/core/model/both/salida/SesionReforzamientoDTO.model';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { v4 as uuidv4 } from 'uuid';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { PageEvent } from '@angular/material/paginator';
import { EvaluacionDocumentoRequest } from 'app/core/model/request/general/EvaluacionDocumentoRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { MatDialog } from '@angular/material/dialog';
import { PopupInformeComponent } from 'app/core/components/popup-informe/popup-informe.component';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PdfService } from 'app/core/services/pdf.service';

@Component({
  selector: 'app-rela-adol-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatExpansionModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatProgressSpinnerModule,
    SubidaDeDocumentosComponent,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './rela-adol-crear-editar.component.html',
  styleUrl: './rela-adol-crear-editar.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ]
})
export class RelaAdolCrearEditarComponent implements OnInit, AfterViewInit {
  @ViewChild("subidaDeDocumentosComp") subidaDeDocumentosComp: SubidaDeDocumentosComponent;
  @ViewChild('constanciaInput') constanciaInput: ElementRef<HTMLInputElement>;
  nemonicoMenu = etiquetasModel.MENU_ACTIVIDADES_REFORZAMIENTO;

  uuid_fp: string;
  item: ReforzamientoDTO;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  sesionesReforzamiento: any[] = [];
  constancias: File[] = [];

  crearActividadReforzamientoForm: FormGroup;
  crearSesionForm: FormGroup;
  listadoSesiones: SesionReforzamientoDTO[] = [];
  dataSource: CdkTableDataSourceInput<SesionReforzamientoDTO>;
  displayedColumns: string[] = ['fecha', 'tipo', 'responsable', 'observaciones', 'archivo'];
  esEdicion: boolean = false;
  esVisualizacion: boolean = false;

  constanciaFile: File | null = null; // Variable para almacenar el archivo

  sesionesSeleccionadas: any[] = [];

  @ViewChild('documentosComp')
  tablaDocumentos: DocumentosSubidosTablaComponent;

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private route: ActivatedRoute,
    private cdRef: ChangeDetectorRef,
    public dialog: MatDialog,
    public funcionesUtils: FuncionesUtils,
    private pdfService: PdfService,
    private fichaService: FichaIdentificacionService,
    private relacionEgresoService: RelacionEgresoService,
    private dialogMensajeService: DialogMensajeService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.item = history.state.item;
    this.esVisualizacion = history.state.esVisualizacion;

    if (this.item)
      this.esEdicion = true;

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.NEMONICO_SESIONES_REFORZAMIENTO, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.sesionesReforzamiento = data;
      },
      error: (error) => console.error('Error cargando tipos de notificación:', error)
    });

    this.initializeForms();
    this.obtenerTiposDeDocumentos();

    if (this.esEdicion)
      this.obtenerReforzamiento();

    if (this.esVisualizacion)
      this.crearActividadReforzamientoForm.disable();
  }

  ngAfterViewInit(): void {
    if (this.esEdicion)
      this.obtenerDocumentos();
  }

  private initializeForms(): void {
    // Inicializar el formulario principal
    this.crearActividadReforzamientoForm = this.fb.group({
      planVida: [false],
    });

    // Inicializar el formulario de sesiones
    this.crearSesionForm = this.fb.group({
      fechaSesion: ['', Validators.required],
      nombreResponsable: ['', Validators.required],
      constancia: [null, Validators.required],
      observaciones: ['']
    });
  }

  onConstanciaSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];

    if (file) {
      if (this.isValidImageType(file)) {
        this.constanciaFile = file;
        this.crearSesionForm.get('constancia').setValue('Cargado'); // Solo para validación
      } else {
        console.error('Solo se permiten archivos de imagen');
        this.crearSesionForm.get('constancia').setErrors({ invalidType: true });
      }
    }
  }

  private isValidImageType(file: File): boolean {
    const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
    return validTypes.includes(file.type);
  }

  // Método para agregar sesión
  agregarSesion(): void {

    if (this.sesionesSeleccionadas.length === 0) {
      this.dialogMensajeService.mensajeErrorConTitulo("Error de validación", "Se debe seleccionar al menos una sesión.");
      return;
    }

    const fechaIngresada = this.crearSesionForm.get('fechaSesion').value;
    const sesionesHoy = this.listadoSesiones.filter(s => {
      // Convertir las fechas a cadenas con formato adecuado
      return new Date(s.fechaSesion).toISOString().split('T')[0] === new Date(fechaIngresada).toISOString().split('T')[0];
    });

    if (sesionesHoy.length + this.sesionesSeleccionadas.length > 2) {
      this.dialogMensajeService.mensajeErrorConTitulo("Error de validación", "No se puede guardar más de 2 sesiones por día.");
      return;
    }

    if (this.crearSesionForm.valid) {
      this.sesionesSeleccionadas.forEach(sesion => {

        const nuevaSesion: SesionReforzamientoDTO = {
          fechaSesion: this.crearSesionForm.get('fechaSesion').value,
          nemonicoTipoSesion: sesion.nemonico,
          nombretipoSesion: sesion.nombre,
          nombreResponsable: this.crearSesionForm.get('nombreResponsable').value,
          archivo: this.constanciaFile.name,
          observaciones: this.crearSesionForm.get('observaciones').value,
        };

        nuevaSesion.documentoDTO = this.getDocumentoDTO(this.constanciaFile, nuevaSesion);

        this.listadoSesiones.push(nuevaSesion);

        this.constancias.push(this.constanciaFile);
      });

      // Limpiar el archivo de constancia en el input HTML
      this.constanciaInput.nativeElement.value = '';

      // Limpiar el archivo de constancia
      this.constanciaFile = null;

      // Resetear el formulario de sesiones
      this.crearSesionForm.reset();

      // Ordenar las sesiones por nombre (nombretipoSesion)
      this.listadoSesiones.sort((a, b) => {
        if (a.nombretipoSesion < b.nombretipoSesion) {
          return -1; // a va antes
        }
        if (a.nombretipoSesion > b.nombretipoSesion) {
          return 1; // b va antes
        }
        return 0; // Son iguales
      });

      // Se actualiza la sesión habilitada solo si no está habilitada
      // this.sesionesSeleccionadas.forEach(sesion => {
      //   const index = this.sesionesReforzamiento.findIndex(s => s.nemonico === sesion.nemonicoTipoSesion);
      //   if (index !== -1 && index < this.sesionesReforzamiento.length - 1) {
      //     const siguienteSesion = this.sesionesReforzamiento[index + 1];
      //     if (!siguienteSesion.habilitada) {
      //       siguienteSesion.habilitada = true;
      //     }
      //   }
      // });

      this.sesionesSeleccionadas = [];

      // Actualizar el dataSource
      this.dataSource = [...this.listadoSesiones];
      this.cdRef.detectChanges();
    }
  }

  isTipoSesionDisabled(sesion: any): boolean {
    const sesionYaIngresada = this.listadoSesiones.some(existingSesion => existingSesion.nemonicoTipoSesion === sesion.nemonico);
    const sesionYaEscogida = this.sesionesSeleccionadas.some(sesionEscogida => sesionEscogida.nemonico === sesion.nemonico);
    return sesionYaIngresada || (!sesionYaEscogida && this.sesionesSeleccionadas.length >= 2);
  }

  getTituloArchivos() {
    let loguitud = this.subidaDeDocumentosComp?.documentosSubidos?.length;
    return "Carga de archivos" + (loguitud > 0 ? " (archivos cargados " + loguitud + ")" : "");
  }

  guardar() {
    if (this.esEdicion)
      this.actualizarReforzamiento();
    else
      this.guardarReforzamiento();
  }

  // Métodos de acción principal
  guardarReforzamiento(): void {
    if (this.crearActividadReforzamientoForm.invalid) {
      this.crearActividadReforzamientoForm.markAllAsTouched();
      this.dialogMensajeService.mensajeError("Verifica la información requerida antes de continuar");
      return;
    }

    if (this.listadoSesiones.length == 0) {
      this.crearActividadReforzamientoForm.markAllAsTouched();
      this.dialogMensajeService.mensajeError("Debes de ingresar por lo menos 1 sesión");
      return;
    }

    let docSubidos = this.subidaDeDocumentosComp?.documentosSubidos ?? null;
    if (!docSubidos || docSubidos?.length < 1) {
      this.dialogMensajeService.mensajeError("Debes de cargar al menos el acta de consentimiento y la entrega firmada para continuar");
      return;
    }

    const tieneActaConsentimiento = docSubidos.some(doc =>
      doc.documentoDTO.tipoDocumentoSistema?.nemonico === etiquetasModel.TIPO_DOCUMENTO_ACTA_CONSENTIMIENTO
    );
    if (!tieneActaConsentimiento) {
      this.dialogMensajeService.mensajeError("Debes cargar el acta de consentimiento para continuar");
      return;
    }

    const tieneEntregaFirmada = docSubidos.some(doc =>
      doc.documentoDTO.tipoDocumentoSistema?.nemonico === etiquetasModel.TIPO_DOCUMENTO_CARTILLA_INFORMATIVA
    );
    if (!tieneEntregaFirmada) {
      this.dialogMensajeService.mensajeError("Debes cargar la entrega firmada para continuar");
      return;
    }

    if (this.subidaDeDocumentosComp?.documentosCargados?.length > 0) {
      this.dialogMensajeService.mensajeError("Hay archivos por cargar");
      return;
    }

    let files = docSubidos?.map(doc => doc.documento) ?? null;

    // Crear objeto ReforzamientoDTO con los datos del formulario
    let reforzamientoDTO: ReforzamientoDTO = {
      planVida: this.crearActividadReforzamientoForm.controls.planVida.value,
      tokenFichaIdentificacion: this.uuid_fp,
      sesiones: this.listadoSesiones,
      reforzamientoDocumentoDTO: {
        documentoDTOList: docSubidos?.map(doc => doc.documentoDTO)
      }
    };

    // Enviar al backend
    let load = this.dialogMensajeService.mensajeLoading("Guardando el reforzamiento..");
    this.relacionEgresoService.crearReforzamiento(files, this.constancias, reforzamientoDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<Boolean>) => {
        load.close();
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al guardar el reforzamiento. ' + response.mensaje
          );
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje).afterClosed().subscribe(() => {
            this.cancelar();
          });
        }
      },
      error: (error) => {
        this.dialogMensajeService.mensajeError("Ocurrió un error al guardar el reforzamiento. Inténtalo de nuevo.");
      }
    });
  }

  actualizarReforzamiento(): void {
    // Crear objeto ReforzamientoDTO con los datos del formulario
    let reforzamientoDTO: ReforzamientoDTO = {
      planVida: this.crearActividadReforzamientoForm.controls.planVida.value,
      tokenFichaIdentificacion: this.uuid_fp,
      tokenIdentificador: this.item.tokenIdentificador,
      sesiones: this.listadoSesiones
    };

    // Enviar al backend
    let load = this.dialogMensajeService.mensajeLoading("Guardando el reforzamiento..");
    this.relacionEgresoService.actualizarReforzamiento(this.constancias, reforzamientoDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<Boolean>) => {
        load.close();
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al guardar el reforzamiento. ' + response.mensaje
          );
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje).afterClosed().subscribe(() => {
            this.cancelar();
          });
        }
      },
      error: (error) => {
        this.dialogMensajeService.mensajeError("Ocurrió un error al guardar el reforzamiento. Inténtalo de nuevo.");
      }
    });
  }

  cancelar(): void {
    this.location.back();
  }

  obtenerReforzamiento() {
    this.relacionEgresoService.obtenerReforzamientoPorToken(this.item, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<ReforzamientoDTO>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al obtener el reforzamiento. ' + response.mensaje
          );
          return;
        }
        else {
          this.item = response.data;

          // Cargar los datos en los formularios
          this.crearActividadReforzamientoForm.get('planVida').setValue(this.item.planVida);

          // Cargar las sesiones en el listado
          this.listadoSesiones = this.item.sesiones;

          // Recalcular el estado de habilitación de las sesiones
          // this.sesionesReforzamiento.forEach((sesion, index) => {
          //   const sesionIndex = this.listadoSesiones.findIndex(existingSesion => existingSesion.nemonicoTipoSesion === sesion.nemonico);
          //   if (sesionIndex !== -1) {
          //     // Si la sesión ya existe en el listado, habilitar la siguiente sesión si corresponde
          //     if (index < this.sesionesReforzamiento.length - 1) {
          //       const siguienteSesion = this.sesionesReforzamiento[index + 1];
          //       if (!siguienteSesion.habilitada) {
          //         siguienteSesion.habilitada = true;
          //       }
          //     }
          //   }
          // });

          // Actualizar el dataSource
          this.dataSource = this.listadoSesiones;
          this.cdRef.detectChanges();
        }
      },
      error: (error) => {
        this.dialogMensajeService.mensajeError("Ocurrió un error al obtener el reforzamiento. Inténtalo de nuevo.");
      }
    });
  }

  obtenerTiposDeDocumentos() {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(etiquetasModel.SECCION_FICHA_IDENT_ACTIVIDADES_REFORZAMIENTO,
      '').subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {

            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
              return;
            }

            let tiposArchivos = response.data;

            if (tiposArchivos.length == 0) {
              this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
              return;
            }

            this.tiposDeDocumentosSistema =
              tiposArchivos.map(
                (tipoArch) => {
                  let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
                  let tipoDeDocumento = new TipoDeDocumento();
                  tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
                  tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
                  tipoDeDocumento.requerido = tipoArch.requerido;
                  tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
                  tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

                  return tipoDeDocumento;
                }
              );
            console.log('tipos de documento', this.tiposDeDocumentosSistema);
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
  }

  generarActa() {
    this.fichaService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaDTO = response.data;

          // Construir los nuevos campos dinámicamente
          const edadActual = this.funcionesUtils.getEdad(fichaDTO.fechaNacimiento).toString() || 'N/A';

          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_ACTA_CONSENTIMIENTO;
          request.variables = {
            "[TITULO-PLANTILLA]": "Acta de Consentimiento Informado",
            "[ADOLESCENTE]": fichaDTO.nombres + " " + fichaDTO.apellidoPaterno + " " + (fichaDTO.apellidoMaterno || ""),
            "[EDAD]": edadActual,
            "[TIPO-DOC-ADOLESCENTE]": fichaDTO.nombreTipoDocumento,
            "[NOMBRE-CJDR-SOA]": fichaDTO.centroIngreso,
            "[NUMERO-DOCUMENTO-ADOLESCENTE]": fichaDTO.numeroDocumento,
            "[FECHA]": this.formatFecha(new Date()),
          }
          this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
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
                'Hubo un problema al generar el archivo. Inténtalo de nuevo.'
              );
            }
          });

        },
        error: (error: any) => {
          this.fichaService.checkError(error);
        }
      }
    );
    // Abrir el popup y pasar la lista de documentos
    // const dialogRef = this.dialog.open(PopupInformeComponent, {
    //   width: '1000px',
    //   height: '350px',
    //   data: { nemonicoInforme: etiquetasModel.PLANTILLA_ACTA_DE_CONSENTIMIENTO, uuid_fp: this.uuid_fp }
    // });
  }

  getDocumentoDTO(file: File, sesion: SesionReforzamientoDTO) {
    let descripcion = "Constancia de " + sesion.nombretipoSesion;

    let documentoDTO = new DocumentoDTO();
    documentoDTO.tokenIdentificador = uuidv4();
    documentoDTO.mimeType = file.type;
    documentoDTO.nombre = sesion.nombretipoSesion;
    documentoDTO.tamanioBytes = file.size;
    documentoDTO.fechaCreacion = new Date(file.lastModified);
    documentoDTO.descripcion = descripcion;

    return documentoDTO;
  }

  obtenerDocumentos() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let documentosPaginacionRequest = new PaginacionRequest();
    documentosPaginacionRequest.page = page;
    documentosPaginacionRequest.size = pageSize;
    documentosPaginacionRequest.tokenIdentificador = this.item.tokenIdentificador;

    this.relacionEgresoService.obtenerDocumentos(
      documentosPaginacionRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<
            PaginacionResponse<DocumentoDTO>
          >
        ) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    this.obtenerDocumentos();
  }

  validarMaximoSesiones() {
    if (this.sesionesSeleccionadas.length > 2) {
      this.sesionesSeleccionadas.pop(); // Remueve la última selección si supera el límite
      alert("Solo puedes seleccionar un máximo de 2 sesiones.");
    }
    else
      this.sesionesSeleccionadas.push();
  }

  formatFecha(fecha: any): string {
    // Si fecha no es una instancia de Date, convertirla
    const date = new Date(fecha);
    if (isNaN(date.getTime())) {
      return ''; // Retorna vacío si la fecha no es válida
    }

    const day = date.getDate().toString().padStart(2, '0'); // Asegura que el día tenga 2 dígitos
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Meses en JavaScript son 0-indexados
    const year = date.getFullYear();

    return `${day}-${month}-${year}`;
  }
}