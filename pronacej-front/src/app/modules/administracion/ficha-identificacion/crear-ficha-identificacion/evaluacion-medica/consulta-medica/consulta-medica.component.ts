import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { ConsultaAtencionIntegralDTO } from 'app/core/model/both/EJE/ConsultaAtencionIntegralDTO.model';
import { SnackbarService } from 'app/core/services/snackbar.service';
import { EvaluacionMedicaService } from '../evaluacion-medica.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import moment from 'moment';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { ActivatedRoute } from '@angular/router';
import { PdfService } from 'app/core/services/pdf.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { HttpClient } from '@angular/common/http';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-consulta-medica',
  standalone: true,
  imports: [
    MatProgressBarModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    CommonModule],
  templateUrl: './consulta-medica.component.html',
  styleUrl: './consulta-medica.component.scss'
})
export class ConsultaMedicaComponent implements OnInit {

  @Output() crear = new EventEmitter<void>();
  @Input() puedeEditar: boolean = false;
  @Input() puedeEliminar: boolean = false;

  tokenFichaMedica: string = '';
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  uuid_fp: string;

  isLoading: boolean = true;
  searchInputControl: UntypedFormControl = new UntypedFormControl();

  keyLabelsTable: any = {
    acciones: 'Acciones',
    fechaInicio: 'Fecha',
    motivoConsulta: 'Motivo consulta',
    estadoAnimo: 'Estado de ánimo',
  };

  consultas: ConsultaAtencionIntegralDTO[] = [];
  datasource = new MatTableDataSource<ConsultaAtencionIntegralDTO>([]);

  base64Image: string | null = null;
  centro: JerarquiaDTO;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION;

  constructor(
    public dialog: MatDialog,
    private _evaluacionMedicaService: EvaluacionMedicaService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly _fuseConfirmationService: FuseConfirmationService,
    private readonly customSnackbar: SnackbarService,
    private fichaIdentificacionService: FichaIdentificacionService,
    public funcionesUtils: FuncionesUtils,
    private route: ActivatedRoute,
    public pdfService: PdfService,
    private dialogMensajeService: DialogMensajeService,
    private http: HttpClient,
    private jerarquiaService: JerarquiaService,
  ) {

  }

  async ngOnInit(): Promise<void> {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.loadImageAsBase64();
    this.cargarCentro();
    console.log('entrando a la evaluacion medica progreso');
    this._evaluacionMedicaService.fichaMedica$.subscribe((ficha) => {
      if (ficha) {
        console.log('ficha medica', this.tokenFichaMedica);
        this.tokenFichaMedica = ficha;
        this.obtenerConsultasAtencion();
      } else {
        this.isLoading = false;
      }
    });
  }

  async obtenerConsultasAtencion() {
    this.isLoading = true;
    this.tokenFichaMedica = this._evaluacionMedicaService.getToken();
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.tokenIdentificador = this.tokenFichaMedica;
    this._evaluacionMedicaService
      .getConsultasByFichaMedica(paginacionRequest)
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<ConsultaAtencionIntegralDTO>>) => {
          console.log('consultas', response);
          if (!response.exito) {
            this._evaluacionMedicaService.checkError(response);
            return;
          }
          this.consultas = response.data.data;
          this.datasource.data = this.consultas;
          this.changeDetector.detectChanges();
          this.totalItems = response.data.totalItems;

        },
        error: (error: any) => {
          this._evaluacionMedicaService.checkError(error);
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
  }

  getFormatedDate(date: Date) {
    return moment(date, 'YYYY-MM-DDTHH:mm:ssZ').toDate().toLocaleString();
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerConsultasAtencion();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  eliminarConsultaAtencion(consulta: ConsultaAtencionIntegralDTO) {
    const confirmation = this._fuseConfirmationService.open({
      title: 'Eliminar registro',
      message:
        '¿Estás seguro de eliminar este registro?',
      actions: {
        confirm: {
          label: 'Eliminar',
        },
        cancel: {
          label: 'Cancelar'
        }
      },
    });

    confirmation.afterClosed().subscribe((result) => {
      if (result === 'confirmed') {
        this._evaluacionMedicaService.deleteConsultaAtencion(consulta).subscribe({
          next: (response) => {
            this.obtenerConsultasAtencion();

            this.customSnackbar.show('Evaluación eliminada con exito', 'Cerrar', "success");
          },
          error: (err) => {
            this.customSnackbar.show('No se pudo eliminar', 'Cerrar', "error");
          }
        });
      }
    });
  }

  crearObjeto() {
    this._evaluacionMedicaService.setConsultaAtencionSoloLectura(false);
    this._evaluacionMedicaService.setTokenConsultaAtencion(null);
    this.crear.emit();
  }

  editarConsultaAtencion(id: string) {
    this._evaluacionMedicaService.setConsultaAtencionSoloLectura(false);
    this._evaluacionMedicaService.setTokenConsultaAtencion(id);
    this.crear.emit();
  }

  verConsultaAtencion(id: string) {
    this._evaluacionMedicaService.setConsultaAtencionSoloLectura(true);
    this._evaluacionMedicaService.setTokenConsultaAtencion(id);
    this.crear.emit();
  }

  async obtenerConsultaAtencion(token: string) {
    this.isLoading = true;
    this._evaluacionMedicaService
      .getConsultaByTokenId(
        token
      ).subscribe({
        next: async (response: RespuestaPorDefecto<ConsultaAtencionIntegralDTO>
        ) => {
          if (!response.exito) {
            this._evaluacionMedicaService.checkError(response);
            return;
          }
          const dialogoCarga = this.dialogMensajeService.mensajeLoading('Generando PDF...');
          const consultaAtencion = response.data;
          const solicitudPdf = new GeneracionPdfRequest();
          const datosCabecera = await this.obtenerDatosCabecera();
          solicitudPdf.nemonico = 'FORMULARIO_CONSULTA_MEDICA';
          console.log('consultaAtencion', consultaAtencion);
          console.log('centro', this.centro);

          solicitudPdf.variables = {
            ...datosCabecera,
            "[IMG_BASE64]": this.base64Image,
            "[FECHA-INGRESO]": this.funcionesUtils.formatearFecha(new Date()),
            "[HORA-INGRESO]": new Date().toLocaleTimeString('es-ES'),
            "[CENTRO]": this.centro?.nombre || 'No especificado',
            "[TITULO-INFORME]": 'Consulta Médica',
            "[TITULO-PLANTILLA]": 'Consulta Médica',
            "[MOTIVO-CONSULTA]": consultaAtencion?.motivoConsulta || 'No especificado',
            "[TIEMPO-ENFERMEDAD]": consultaAtencion?.tiempoEnfermedad || 'No especificado',
            "[ESTADO-ANIMO]": consultaAtencion?.estadoDeAnimo || 'No especificado',
            "[FORMA-INICIO]": consultaAtencion?.formaDeInicio || 'No especificado',
            "[SED]": consultaAtencion?.sed ? 'Sí' : 'No',
            "[SUENO]": consultaAtencion?.sueno ? 'Sí' : 'No',
            "[APETITO]": consultaAtencion?.apetito ? 'Sí' : 'No',
            "[DEPOSICIONES]": consultaAtencion?.deposiciones || 'No especificado',
            "[ORINA]": consultaAtencion?.orina || 'No especificado',
            "[FIEBRE15]": consultaAtencion?.fiebre15dias || 'No especificado',
            "[TOS15]": consultaAtencion?.tos15dias || 'No especificado',
            "[SECRECION]": consultaAtencion?.secrecionGenitales || 'No especificado',
            "[PERDIDA-PESO]": consultaAtencion?.perdidaPeso || 'No especificado',
            "[PESO]": consultaAtencion?.peso || 'No especificado',
            "[TALLA]": consultaAtencion?.talla || 'No especificado',
            "[IMC]": consultaAtencion?.imc || 'No especificado',
            "[PRESION]": consultaAtencion?.presion || 'No especificado',
            "[TEMPERATURA]": consultaAtencion?.temperatura || 'No especificado',
            "[FECHA-PROXIMA-CITA]": consultaAtencion?.fechaProximaCita
              ? new Date(consultaAtencion.fechaProximaCita).toLocaleDateString('es-ES')
              : 'No especificado',
            "[DIAGNOSTICO]": consultaAtencion?.diagnostico || 'No especificado',
            "[TRATAMIENTO]": consultaAtencion?.tratamiento || 'No especificado',
            "[OBSERVACIONES]": consultaAtencion?.observaciones || 'No especificado',
            "[EXAMENES-AUXILIARES]": consultaAtencion?.examenesAuxiliares || 'No especificado'
          };

          this.pdfService.generarPdf(solicitudPdf, etiquetasModel.NEMONICO_MENU_EVALUACION_MEDICA).subscribe({
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


        }, error: (error: any) => {
          this._evaluacionMedicaService.checkError(error);
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
  }

  private obtenerDatosCabecera(): Promise<{ [key: string]: string }> {
    return new Promise((resolve, reject) => {
      this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
        next: async (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            reject('Error al obtener la ficha de identificación');
            return;
          }

          const fichaIdentificacion = response.data;

          var datosCabeceraCatalogo = null;

          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


          const datosCabecera = {
            "[ADOLESCENTE]": nombreAdolescente,
            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
          };

          resolve(datosCabecera);
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }

  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
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
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
  }
}
