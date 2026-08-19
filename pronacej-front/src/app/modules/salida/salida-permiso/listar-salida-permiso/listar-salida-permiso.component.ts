import { Component, OnInit, ViewChild } from '@angular/core';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { SalidaService } from '../../salida.service';
import { RegistroSalidaDTO } from 'app/core/model/both/salida/RegistroSalidaDTO.model';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { catchError, Observable, Subject, takeUntil, tap, of } from 'rxjs';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';

@Component({
  selector: 'app-listar-salida-permiso',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './listar-salida-permiso.component.html',
  styleUrl: './listar-salida-permiso.component.scss'
})
export class ListarSalidaPermisoComponent implements OnInit {
  tituloPantalla: string = "Registros de salida";

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  listaProcesos: RegistroSalidaDTO[] = [];
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  paginacion: Paginacion = new Paginacion();
  funcionarioActivo: FuncionarioDTO;
  fichaIdentifacion: FichaIdentificacionDTO
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_REGISTRO_SALIDA;
  base64Image: string | null = null;
  tokenFilter: any
  jerarquia: any;
  tokenJerarquia: any

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    idRegistroSalida: "No.",
    acciones: "Acciones",
    nroDocumento: "Nro. de salida",
    nombreAdolescente: "Nombre adolescente",
    fechaHoraSalida: "Fecha salida",
    nombreMotivoSalida: "Motivo de salida",

  };

  private _unsubscribeAll: Subject<any> = new Subject<any>();
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
  

  constructor(
    private router: Router,
    private dialogMensajeService: DialogMensajeService,
    private salidaService: SalidaService,
    private http: HttpClient,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private funcionarioService: FuncionarioService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private jerarquiaService: JerarquiaService,
  ) { }

  // ngOnInit(): void {
  //   registerLocaleData(localeEs, 'es-ES');
  //   this.obtenerProcesos();
  // }

  async ngOnInit(): Promise<void> {
    registerLocaleData(localeEs, 'es-ES');
    await this.obtenerTokenDepartamento();
    this.obtenerTokenDepartamento().then(() => {
      this.obtenerJerarquias().then(() => {
        this.obtenerProcesos();
      });
    });
  }

  obtenerProcesos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.filter = this.paginacionRequest.filter;
    this.paginacionRequest.tokenIdentificador = this.tokenFilter
    console.log(this.paginacionRequest);

    this.salidaService.obtenerRegistroSalidas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroSalidaDTO>>) => {
          if (!response.exito) {
            this.salidaService.checkError(response);
            return;
          }
          this.listaProcesos = response.data.data.map(item => ({
            ...item,
          }));
          console.log(this.listaProcesos);

          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.salidaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.tokenFilter

    this.salidaService.obtenerRegistroSalidas(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroSalidaDTO>>) => {
          if (!response.exito) {
            this.salidaService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.salidaService.checkError(error);
        }
      }
    );
  }

  agregarProceso() {
    this.router.navigate(['/salida/registro-salida/crear-editar']);
  }

  editarProceso(proceso: RegistroSalidaDTO) {
    this.router.navigate(['/salida/registro-salida/crear-editar'], { queryParams: { ID: proceso.tokenIdentificador } })
  }

  eliminarProceso(gestionFugaDTO: RegistroSalidaDTO) {
    console.log(gestionFugaDTO);

    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar este registro, esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el permiso..");
            this.salidaService.eliminarSalida(gestionFugaDTO, this.nemonicoMenu).subscribe(
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

                  // this.gestionFugaService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;
    this.obtenerProcesos();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerProcesos();

  }

  refrescar() {
    this.obtenerProcesos()
  }

  visualizar(proceso: RegistroSalidaDTO) {
    this.router.navigate(['/salida/registro-salida/crear-editar'], { queryParams: { ID: proceso.tokenIdentificador, mode: 'ver' } });
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


  formatFechaDate(fecha: string): string {
    const date = new Date(fecha);
    const dia = date.getDate().toString().padStart(2, '0');
    const mes = (date.getMonth() + 1).toString().padStart(2, '0');
    const anio = date.getFullYear();
    return `${dia}/${mes}/${anio}`;
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

  generarPDF(proceso: RegistroSalidaDTO) {
    if (!proceso || !proceso.tokenIdentificadorAdolescente) {
      console.error('No se recibió proceso válido para generar PDF');
      return;
    }
    this.obtenerFichaIdentificacion(proceso.tokenIdentificadorAdolescente).subscribe({
      next: (data) => {
        if (!data) {
          this.dialogMensajeService.mensajeError('No se pudo cargar la ficha de identificación.');
          return;
        }
        this.fichaIdentifacion = data.data;
        this.loadImageAsBase64();

        setTimeout(() => {
          const fechaActual = this.formatFecha((new Date()).toString());
          const horaActual = this.formatHora((new Date()).toString());
          const request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_REGISTRO_SALIDA;
          const nombreAdolescente = `${this.fichaIdentifacion.nombres ?? ''} ${this.fichaIdentifacion.apellidoPaterno ?? ''} ${this.fichaIdentifacion.apellidoMaterno ?? ''}`.trim();
          const fechaNacimientoFormateada = this.formatFecha(this.fichaIdentifacion.fechaNacimiento?.toString());
          const titulo = 'Informe de registro de salida'
          // Crear tabla según motivo de salida
          let tablaDetalleSalida = new TablaPlantilla();
          tablaDetalleSalida.encabezados = [];
          tablaDetalleSalida.filas = [];
          const motivo = proceso.motivoSalida?.nemonico;
          if (motivo === 'SALIDA_TRASLADO' && proceso.traslado) {
            console.log(proceso.traslado);

            tablaDetalleSalida.encabezados = ['No', 'N° Traslado', 'Análisis', 'Fecha'];
            tablaDetalleSalida.filas.push({
              No: '1',
              'N° Traslado': proceso.traslado.numTraslado ?? '-',
              'Análisis': proceso.traslado.analisis ?? '-',
              'Fecha': this.formatFechaDate(proceso.traslado.instanciaProcesoDTO?.fechaCreacion?.toString() ?? '')
            });
          } else if (motivo === 'SALIDA_TEMPORAL' && proceso.permisoSalida) {
            tablaDetalleSalida.encabezados = ['No', 'N° Documento', 'Observaciones', 'Fecha'];
            tablaDetalleSalida.filas.push({
              No: '1',
              'N° Documento': proceso.permisoSalida.nroDocumento ?? '-',
              'Observaciones': proceso.permisoSalida.observaciones ?? '-',
              'Fecha': this.formatFechaDate(proceso.permisoSalida.fechaHoraSalida?.toString() ?? '')
            });
          } else if (motivo === 'SALIDA_EXTERNAMIENTO' && proceso.externamiento) {
            tablaDetalleSalida.encabezados = ['No', 'N° Documento', 'Autorizado por', 'Observaciones', 'Fecha'];
            tablaDetalleSalida.filas.push({
              No: '1',
              'N° Documento': proceso.externamiento.numeroDocumento ?? '-',
              'Autorizado por': proceso.externamiento.autorizacion ?? '-',
              'Observaciones': proceso.externamiento.observaciones ?? '-',
              'Fecha': this.formatFechaDate(proceso.externamiento.fechaRegistro?.toString() ?? '')
            });
          }
          else if (motivo === 'SALIDA_FUGA' && proceso.eventoFuga) {
            tablaDetalleSalida.encabezados = ['No', 'N° Documento', 'Observaciones', 'Fecha'];
            tablaDetalleSalida.filas.push({
              No: '1',
              'N° Documento': proceso.eventoFuga.numFuga ?? '-',
              'Observaciones': proceso.eventoFuga.asunto ?? '-',
              'Fecha': this.formatFechaDate(proceso.eventoFuga.fechaFuga?.toString() ?? '')
            });
          }
          else if (motivo === 'SALIDA_INFORME_FINAL' && proceso.informeFinalAbierto) {
            tablaDetalleSalida.encabezados = ['No', 'N° Documento', 'Observaciones', 'Fecha'];
            tablaDetalleSalida.filas.push({
              No: '1',
              'N° Documento': proceso.informeFinalAbierto.idInformeFinalAbierto ?? '-',
              'Observaciones': proceso.informeFinalAbierto.conclusionesRecomendaciones ?? '-',
              'Fecha': this.formatFechaDate(proceso.informeFinalAbierto.fechaCreacion?.toString() ?? '')
            });
          }
          else {
            tablaDetalleSalida.encabezados = ['No', 'Detalle'];
            tablaDetalleSalida.filas.push({
              No: '1',
              'Detalle': 'No hay información específica para este tipo de salida'
            });
          }
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[TITULO-PLANTILLA]": titulo,
            "[TITULO-INFORME]": titulo,
            "[FECHA-REGISTRO]": fechaActual,
            "[HORA-REGISTRO]": horaActual,
            "[NRO-DOCUMENTO]": proceso.nroDocumento,
            "[OBSERVACIONES]": proceso.observaciones,
            "[FECHA-SALIDA]": this.formatFecha(proceso.fechaHoraSalida?.toString()),
            "[CENTRO]": this.funcionarioActivo.departamento,
            "[NOMBRE-ADOLESCENTE]": nombreAdolescente,
            "[FECHA-NACIMIENTO]": fechaNacimientoFormateada,
            "[LUGAR-NACIMIENTO]": this.fichaIdentifacion.lugarNacimiento,
            "[NUMERO-DOCUMENTO]": this.fichaIdentifacion.numeroDocumento,
            "[EDAD]": `${this.funcionesUtils.getEdad(this.fichaIdentifacion.fechaNacimiento)}`,
            "[TABLA-DETALLE-SALIDA]": JSON.stringify(tablaDetalleSalida),
          };

          this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<string>) => {
              if (!response.exito) {
                this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF.');
                return;
              }
              const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
              window.open(url);
            },
            error: (error: any) => {
              this.dialogMensajeService.mensajeError('Hubo un problema al generar el PDF.');
              console.error('Error al generar PDF:', error);
            }
          });
        }, 500);
      }
    });
  }



  obtenerTokenDepartamento(): Promise<void> {
    return new Promise((resolve) => {
      this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          if (!response.exito) {
            resolve();
            return;
          }
          this.funcionarioActivo = response.data;
          this.tokenJerarquia = this.funcionarioActivo.departamento;
          console.log(this.tokenJerarquia);

          console.log(this.funcionarioActivo);
          resolve();
        },
        error: (error: any) => {
          console.error('Error al obtener el departamento:', error);
          resolve();
        }
      });
    });
  }


  obtenerFichaIdentificacion(tokenAdolescente: string): Observable<any> {
    return this.fichaIdentificacionService
      .obtenerFichaIdentificacionPorTokenIdentificador(tokenAdolescente, this.nemonicoMenu)
      .pipe(
        tap((response) => {
        }),
        catchError((error) => {
          console.error("Error al obtener ficha de identificación:", error);
          return of(null);
        }),
        tap((res) => res?.data)
      );
  }


  obtenerJerarquias(): Promise<void> {
    return new Promise((resolve) => {
      this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
        const idDepartamento = this.funcionarioActivo?.tokenIdentificadorDepartamento;
        this.jerarquia = data.data.filter(j => j.tokenIdentificador === idDepartamento);

        if (this.jerarquia.length > 0) {


          this.tokenFilter = this.jerarquia[0].tokenIdentificador;
          console.log(this.tokenFilter);

        }


        resolve();
      });
    });
  }

}
