import { AfterViewInit, Component, EventEmitter, LOCALE_ID, OnInit, Output, ViewChild } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { AccionesExpedienteMatrizComponent } from '../../expediente-matriz/acciones-expediente-matriz/acciones-expediente-matriz.component';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import moment from 'moment';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { PertenenciaService } from 'app/modules/seguridad/services/pertenencia.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PertenenciaDTO } from 'app/core/model/both/pertenenciaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { HttpClient } from '@angular/common/http';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { PdfService } from 'app/core/services/pdf.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-visualizar-recepcion-entrega',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    TablaListaComponent
  ],
  templateUrl: './visualizar-recepcion-entrega.component.html',
  styleUrl: './visualizar-recepcion-entrega.component.scss'
})
export class VisualizarRecepcionEntregaComponent implements OnInit {
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "Entrega/Retiro de pertenencias";

  listaRegistros: PertenenciaDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  uuid_fp: string;

  base64Image: string | null = null;
  nemonicoMenu = etiquetasModel.NEMONICO_ENTREGA_RETIRO_PERTENENCIAS;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;
  @Output() estadoEditarEnviado = new EventEmitter<boolean>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    articulosRetirados: "Items retirados",
    articulosEntregados: "Items entregados",
    articulosRetiradosSalida: "Items retirados a la salida",
    // numArticulosRetirados: "Items retirados",
    // numArticulosEntregados: "Items entregados",
    // numArticulosRetiradosSalida: "Items retirados a la salida",
    // idPertenencia: "Número de registro",
    // estado: "Estado",
    // tipoRegistro: "Tipo",
  };

  constructor(
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private pertenenciaService: PertenenciaService,
    private http: HttpClient,
    private funcionesUtils: FuncionesUtils,
    private fichaIdentificacionService: FichaIdentificacionService,
    private pdfService: PdfService,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_ENTREGA_RETIRO_DE_PERTENENCIAS"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerPertenencias();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  getLocalDate(date: Date) {
    return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
  }

  visualizarPertenencia(registro: PertenenciaDTO) {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/entregaRecepcionUniformesArticulos/${this.uuid_fp}/crear-editar`], { queryParams: { numDoc: registro.idPertenencia, state: 'show' } });
  }

  agregarPertenencia() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/entregaRecepcionUniformesArticulos/${this.uuid_fp}/crear-editar`]);
  }

  editarPertenencia(registro: PertenenciaDTO) {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/entregaRecepcionUniformesArticulos/${this.uuid_fp}/crear-editar`], { queryParams: { numDoc: registro.idPertenencia } })
  }

  obtenerPertenencias() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.pertenenciaService.obtenerPertenencias(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PertenenciaDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.pertenenciaService.checkError(response);
            return;
          }

          this.listaRegistros = response.data.data;
          this.listaRegistros.map(registro => {
            registro.fechaCreacion = new Date(registro.fechaCreacion);
          });
          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.pertenenciaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.pertenenciaService.obtenerPertenencias(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<PertenenciaDTO>>) => {

          if (!response.exito) {
            this.pertenenciaService.checkError(response);
            return;
          }
          response.data.data.map(registro => {
            registro.fechaCreacion = new Date(registro.fechaCreacion);
          });
          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.pertenenciaService.checkError(error);
        }
      }
    );
  }

  eliminarPertenencia(registro: PertenenciaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el registro: \"" + registro.idPertenencia + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el registro..");
            this.pertenenciaService.eliminarPertenencia(registro, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerPertenencias();
                },
                error: (error: any) => {
                  load.close();

                  this.pertenenciaService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  handlePageEvent(event: PageEvent) {
    this.paginacion.pageSize = event.pageSize;
    this.paginacion.pageIndex = event.pageIndex;
    this.obtenerPertenencias();
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
    this.obtenerPertenencias();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerPertenencias();
  }

  generarPDF(registro: PertenenciaDTO) {
    this.loadImageAsBase64();

    let elementosEgresoEnviar: any[] = [];
    let numero = 1;
    for (let egreso of registro.detalleEgresos) {
      let elemento = {
        No: (numero++).toString(),
        Nombre: egreso.nombre,
        Tipo: egreso.tipo.nombre,
        Estado: egreso.estado.nombre,
        Cantidad: egreso.cantidad.toString()
      }
      elementosEgresoEnviar.push(elemento);
    }

    let tablaEgreso = new TablaPlantilla();
    tablaEgreso.encabezados = ['No', 'Nombre', 'Tipo', 'Estado', 'Cantidad'];
    tablaEgreso.filas = elementosEgresoEnviar;

    let elementosIngresoEnviar: any[] = [];
    numero = 1;
    for (let ingreso of registro.detalleIngresos) {
      let elemento = {
        No: (numero++).toString(),
        Nombre: ingreso.nombre,
        Tipo: ingreso.tipo.nombre,
        Estado: ingreso.estado.nombre,
        Cantidad: ingreso.cantidad.toString()
      }
      elementosIngresoEnviar.push(elemento);
    }

    let tablaIngreso = new TablaPlantilla();
    tablaIngreso.encabezados = ['No', 'Nombre', 'Tipo', 'Estado', 'Cantidad'];
    tablaIngreso.filas = elementosIngresoEnviar;

    console.log(registro);
    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(registro.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaIdentificacion: FichaIdentificacionDTO = response.data;
          console.log(fichaIdentificacion);
          const nombreAdolescente = `${fichaIdentificacion?.nombres} ${fichaIdentificacion?.apellidoPaterno} ${fichaIdentificacion?.apellidoMaterno}`;
          const edadActual = this.funcionesUtils.getEdad(fichaIdentificacion.fechaNacimiento).toString() || 'N/A';
          const numDocumento = fichaIdentificacion.numeroDocumento || 'N/A';


          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_PERTENENCIAS_INGRESO;
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[TITULO-PLANTILLA]": 'Acta de documentos y artículos personales',
            "[TITULO-INFORME]": 'Anexo 14 - Acta de documentos y artículos personales - Ingreso al Centro',
            "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
            "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
            "[CENTRO]": fichaIdentificacion.centroIngreso,
            "[ADOLESCENTE]": nombreAdolescente,
            "[EDAD_ACTUAL_ADOLESCENTE]": edadActual,
            "[IDENTIFICACION_ADOLESCENTE]": numDocumento,
            "[TABLA-RETIRO-PERT]": JSON.stringify(tablaIngreso),
            "[COMENTARIO-RETIRO]": registro?.comentarioIngresos,
            "[TABLA-ENTREGA-PERT]": JSON.stringify(tablaEgreso),
            "[COMENTARIO-ENTREGA]": registro?.comentarioEgresos
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

            },
            error: (error: any) => {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
              );
            }
          });

        },
        error: (error: any) => {
          this.fichaIdentificacionService.checkError(error);
        }
      }
    );


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

}

