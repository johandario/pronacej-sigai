import { Component, EventEmitter, inject, OnInit, Output, ViewChild } from '@angular/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { ExpedienteMatrizDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { environment } from 'environments/environment';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { ActaExternamientoDTO } from 'app/core/model/both/ia/actaExternamientoDTO.model';
import { ActaExternamientoService } from 'app/modules/administracion/services/actaExternamiento.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { HttpClient } from '@angular/common/http';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { catchError, Observable, tap, throwError, of, firstValueFrom } from 'rxjs';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';

@Component({
  selector: 'app-visualizar-expediente-matriz',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTabsModule,
    MatTooltipModule,
    TablaListaComponent
  ],
  templateUrl: './visualizar-expediente-matriz.component.html',
  styleUrl: './visualizar-expediente-matriz.component.scss'
})

export class VisualizarExpedienteMatrizComponent implements OnInit {

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  mostrarActaExternamiento: boolean = false;

  tituloPantalla: string = "Expedientes Legales";

  listaExpedientes: ExpedienteMatrizDTO[] = [];
  paginacionExpediente: Paginacion = new Paginacion();
  paginacionExpedienteRequest: PaginacionRequest = new PaginacionRequest();

  listaActas: ActaExternamientoDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  uuid_fp!: string;
  fichaIdentifacion: FichaIdentificacionDTO

  @ViewChild('tablaActas') tablaActasComponent: TablaListaComponent<any>;
  @ViewChild('tablaExpedientes') tablaExpedientesComponent: TablaListaComponent<any>;

  @Output() estadoEditarEnviado = new EventEmitter<boolean>();
  base64Image: string | null = null;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_EXPEDIENTE_LEGAL;
  nemonicoMenuExternamiento = etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO;
  funcionarioActivo: FuncionarioDTO;
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    numExpediente: "No. Registro",
    tipoCentro: "Centro",
    numOficio: "No. Oficio",
    fecOficioTexto: "Fecha de oficio",
    // fechaOficio: "Fecha de oficio",
    observacion: "Observación",
    // estado: "Estado"
  };

  keyLabelsActas: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaRegistro: "Fecha de registro",
    autorizacion: "Autorización",
    tipoDocumento: "Tipo de documento",
    numeroExpedienteMatriz: "No. Registro",
    impreso: "Impreso",
    firmado: "Firmado"
  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private expedienteMatrizService: ExpedienteMatrizService,
    private actaService: ActaExternamientoService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private personaService: DatosFamiliaresService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private http: HttpClient,
    private fichaIngresoService: FichaIngresoService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private funcionarioService: FuncionarioService,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_EXPEDIENTES_LEGALES"
    );

    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    console.log(this.uuid_fp);
    await firstValueFrom(this.obtenerFichaIdentificacion());
    await this.obtenerTokenDepartamento();
    this.obtenerFichaIngresoValida(this.uuid_fp).subscribe(response => {
      this.obtenerExpedientes();
      this.obtenerActas();

    });
  }

  obtenerFichaIngresoValida(tokenFichaIdentificacion: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;

    return this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, '').pipe(
      tap((response) => {
        let ingreso = response.data;
        console.log(ingreso);
        if (ingreso && ingreso?.centro.nombre.includes('CJDR')) {
          this.mostrarActaExternamiento = true;
        }
      }),
      catchError(err => {
        this.fichaIngresoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerExpedientes() {
    this.paginacionExpedienteRequest.size = this.paginacionExpediente.pageSize;
    this.paginacionExpedienteRequest.page = this.paginacionExpediente.pageIndex;
    this.paginacionExpedienteRequest.tokenIdentificador = this.uuid_fp;

    this.expedienteMatrizService.obtenerExpedientesValidos(this.paginacionExpedienteRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ExpedienteMatrizDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.expedienteMatrizService.checkError(response);
            return;
          }

          this.listaExpedientes = response.data.data;
          this.totalItems = response.data.totalItems;
          this.paginacionExpediente.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.expedienteMatrizService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompletoExpedientes() {
    this.paginacionExpedienteRequest.size = 100000;
    this.paginacionExpedienteRequest.page = 0;
    this.paginacionExpedienteRequest.tokenIdentificador = this.uuid_fp;

    this.expedienteMatrizService.obtenerExpedientesValidos(this.paginacionExpedienteRequest, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ExpedienteMatrizDTO>>) => {

          if (!response.exito) {
            this.expedienteMatrizService.checkError(response);
            return;
          }

          this.tablaExpedientesComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.expedienteMatrizService.checkError(error);
        }
      }
    );
  }

  visualizarExpediente(expedienteMatriz: ExpedienteMatrizDTO) {
    this.router.navigate(
      [
        `/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}/crear-editar`
      ], 
      { 
        queryParams: { 
          numDoc: expedienteMatriz.numExpediente, 
          state: 'show' 
        } 
      }
    )
  }

  agregarExpediente() {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}/crear-editar`]);
  }

  eliminarExpediente(expedienteMatriz: ExpedienteMatrizDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el expediente: \"" + expedienteMatriz.numExpediente + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el expediente..");
            this.expedienteMatrizService.eliminarExpediente(expedienteMatriz, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerExpedientes();
                },
                error: (error: any) => {
                  load.close();

                  this.expedienteMatrizService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  editarExpediente(expedienteMatriz: ExpedienteMatrizDTO) {
    this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/expediente/${this.uuid_fp}/crear-editar`], { queryParams: { numDoc: expedienteMatriz.numExpediente, state: 'edit' } })
  }

  handlePageEvent(event: PageEvent) {
    this.paginacionExpediente.pageSize = event.pageSize;
    this.paginacionExpediente.pageIndex = event.pageIndex;
    this.obtenerExpedientes();
  }

  handleSortEvent(event: Sort) {
    if (event.direction) {
      this.paginacionExpedienteRequest.sort = event.active;
      this.paginacionExpedienteRequest.direction = event.direction;
    }
    else {
      this.paginacionExpedienteRequest.sort = null;
      this.paginacionExpedienteRequest.direction = null;
    }
    this.obtenerExpedientes();
  }

  handleSearchEvent(filter: string) {
    this.paginacionExpedienteRequest.filter = filter;
    this.obtenerExpedientes();
  }

  obtenerActas() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.actaService.obtenerActasExternamiento(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ActaExternamientoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaActas = response.data.data;
          this.paginacion.totalItems = response.data.totalItems;

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  descargarExcelCompletoActas() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.actaService.obtenerActasExternamiento(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ActaExternamientoDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.tablaActasComponent.exportXLSX(response.data.data);

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  agregarActa() {
    this.router.navigate(['crear-editar/actaExternamiento'], {
      relativeTo: this.route
    });
  }

  verActa(actaExternamientoDTO: ActaExternamientoDTO) {
    this.router.navigate(['crear-editar/actaExternamiento'], {
      relativeTo: this.route,
      state: {
        item: actaExternamientoDTO,
        esVisualizacion: true
      }
    });
  }

  editarActa(actaExternamientoDTO: ActaExternamientoDTO) {
    this.router.navigate(['crear-editar/actaExternamiento'], {
      relativeTo: this.route,
      state: {
        item: actaExternamientoDTO
      }
    });
  }

  eliminarActa(actaExternamientoDTO: ActaExternamientoDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el acta? esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el acta..");
            this.actaService.eliminarActaExternamiento(actaExternamientoDTO, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();

                  if (!resp.exito) {
                    this.dialogMensajeService.mensajeError("Hubo un problema al eliminar el registro. Inténtalo de nuevo.");
                    return;
                  }
                  else
                    this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  this.obtenerActas();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al eliminar el registro. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  confirmarImpresion(actaExternamientoDTO: ActaExternamientoDTO): void {
    if (!actaExternamientoDTO.impreso) {
      this.dialogMensajeService.mensajeConConfirmacion(
        'Confirmar Impresión', 'Está seguro de realizar la impresión? No se podrá editar el acta después.'
      ).afterClosed().subscribe((result) => {
        if (result == "confirmed") {
          this.actualizarEstadoImpreso(actaExternamientoDTO);
          this.imprimir(actaExternamientoDTO);
        }
      });
    }
    else
      this.imprimir(actaExternamientoDTO);
  }

  imprimir(actaExternamientoDTO: ActaExternamientoDTO) {
    this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(actaExternamientoDTO.tokenFichaIdentificacion, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
        if (!response.exito) {
          return;
        }

        this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
          .subscribe((data: ArrayBuffer) => {
            const base64String = this.arrayBufferToBase64(data);
            const base64Image = `data:image/png;base64,${base64String}`;


            const fichaDTO = response.data;

            // Construir los nuevos campos dinámicamente
            const adolescente = `${fichaDTO.nombres || ''} ${fichaDTO.apellidoPaterno} ${fichaDTO.apellidoMaterno}`;

            // Dividir los familiares en una lista usando split
            const familiaresNombres = actaExternamientoDTO.familiares.split(';');
            const familiaresParentescos = actaExternamientoDTO.parentescos.split(';');
            const familiaresIdentificaciones = actaExternamientoDTO.identificaciones.split(';');


            let request = new GeneracionPdfRequest();
            request.nemonico = etiquetasModel.FORMULARIO_ACTA_EXTERNAMIENTO;
            request.variables = {
              "[IMG_BASE64]": base64Image,
              "[AUTORIZACION]": actaExternamientoDTO.autorizacion,
              "[TIPO_DOCUMENTO]": actaExternamientoDTO.tipoDocumento,
              "[RESOLUCION]": actaExternamientoDTO.resolucion,
              "[NUMERO_EXPEDIENTE]": actaExternamientoDTO.numeroExpedienteMatriz,
              "[CENTRO]": fichaDTO.centroIngreso,
              "[ADOLESCENTE]": adolescente,
              "[IDENTIFICACION]": fichaDTO.dni ? fichaDTO.dni : fichaDTO.numeroDocumento,
              "[FAMILIARES]": familiaresNombres.join(', '),
              "[IDENTIFICACIONES_FAMILIARES]": familiaresIdentificaciones.join(', '),
              "[PARENTESCO]": familiaresParentescos.join(', '),
              "[DOMICILIO]": actaExternamientoDTO.domicilio,
              "[FECHA_SALIDA]": this.formatFecha(actaExternamientoDTO.fechaRegistro.toString()),
              "[HORA_SALIDA]": this.formatHora(actaExternamientoDTO.fechaRegistro.toString())
            }

            this.pdfService.generarPdf(request, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe({
              next: (response: RespuestaPorDefecto<string>) => {

                if (!response.exito) {
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al recuperar los registros. ' + response.mensaje
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
          });
      },
      error: (error: any) => {
        this.fichaIdentificacionService.checkError(error);
      }
    });
  }

  handlePageEventActas(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    this.obtenerActas();
  }

  handleSortEventActas(event: Sort) {
    if (event.direction) {
      this.paginacionRequest.sort = event.active;
      this.paginacionRequest.direction = event.direction;
    }
    else {
      this.paginacionRequest.sort = null;
      this.paginacionRequest.direction = null;
    }

    this.obtenerActas();
  }

  handleSearchEventActas(filter: string) {
    this.paginacionRequest.filter = filter;

    this.obtenerActas();
  }

  actualizarEstadoImpreso(actaExternamientoDTO: ActaExternamientoDTO) {
    actaExternamientoDTO.impreso = true;

    this.actaService.actualizarActaExternamiento(actaExternamientoDTO, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe({
      next: (response: RespuestaPorDefecto<Boolean>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al actualizar el acta. Inténtalo de nuevo.'
          );
        }
        return;
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar el acta. Inténtalo de nuevo.'
        );
      }
    });
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

  // Función para convertir el ArrayBuffer a base64
  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }



  generarPDF(proceso: any) {
    console.log(proceso);
    this.loadImageAsBase64().subscribe({
      next: (base64) => {
        let request = new GeneracionPdfRequest();
        request.nemonico = etiquetasModel.FORMULARIO_EXPEDIENTE_LEGAL_SOA;
        console.log(this.listaExpedientes);
        let filasTabla: any[] = [];
        let numero = 1;
        for (let detalle of proceso.expedienteDetalle) {
          let fila = {
            No: numero.toString(),
            Tipo: detalle.tipoRegistro?.nombre ?? '-',
            Situación: detalle.situacionJuridica?.nombre ?? '-',
            Variación: detalle.tipoVariacion?.nombre ?? '-',
            Resolución: detalle.numResolucion ?? '-',
            Fecha: this.formatFecha(detalle.fechaResolucion.toString()),
            Corte: detalle.corteJusticia?.nombre ?? '-',
            Instancia: detalle.instancia?.nombre ?? '-',
            Especialidad: detalle.especialidad?.nombre ?? '-',
            Órgano: detalle.organoJurisdiccional ?? '-',
            Monto: detalle.montoReparacion ?? '-',
          };
          filasTabla.push(fila);
          numero++;
        }
        let filasDelito: any[] = [];
        let numeroDelito = 1;
        for (let detalle of proceso.expedienteDetalle || []) {
          for (let delito of detalle.expedienteDelitos || []) {
            filasDelito.push({
              No: numeroDelito.toString(),
              'Delito específico': delito.delitoEspecifico?.nombre ?? '-',
              'Delito genérico': delito.delitoGenerico?.nombre ?? '-',
            });
            numeroDelito++;
          }
        }

        const tablaDelito = new TablaPlantilla();
        tablaDelito.encabezados = ['No', 'Delito específico', 'Delito genérico'];
        tablaDelito.filas = filasDelito;

        let tablaExpediente = new TablaPlantilla();
        tablaExpediente.encabezados = ['No', 'Tipo', 'Situación', 'Variación', 'Resolución', 'Fecha', 'Corte', 'Instancia', 'Especialidad', 'Órgano', 'Monto de reparación'];
        tablaExpediente.filas = filasTabla;
        const tituloPantalla = 'Informe expediente legal';
        const fecha = this.formatFecha((new Date()).toString());
        const hora = this.formatHora((new Date()).toString());
        const fechaOficion = new Date(proceso.fechaOficio).toLocaleDateString('es-ES') ?? 'Sin información';
        const nombreAdolescente = this.fichaIdentifacion ? `${this.fichaIdentifacion.nombres ?? ''} ${this.fichaIdentifacion.apellidoPaterno ?? ''} ${this.fichaIdentifacion.apellidoMaterno ?? ''}`.trim() : 'Sin nombre';
        const fechaNacimientoFormateada = this.formatFecha(this.fichaIdentifacion.fechaNacimiento?.toString());

        let filasDetalleMedidas: any[] = [];
        let numeroDetalle = 1;

        for (let detalle of proceso.expedienteDetalle || []) {
          filasDetalleMedidas.push({
            No: numeroDetalle.toString(),
            'N° Resolución': detalle.numResolucion ?? '-',
            'Fecha Resolución': this.formatFecha(detalle.fechaResolucion),
            'Inicio Medida': this.formatFecha(detalle.fechaInicioMedida),
            'Fin Medida': this.formatFecha(detalle.fechaFinMedida),
            'Decisión': detalle.decision ?? '-',
            'Juez': detalle.juez ?? '-',
            'Secretario': detalle.secretario ?? '-',
            'Variación Medida': detalle.variacionMedida?.nombre ?? '-',
          });
          numeroDetalle++;
        }
        const tablaDetalleMedidas = new TablaPlantilla();
        tablaDetalleMedidas.encabezados = [
          'No', 'N° Resolución', 'Fecha Resolución', 'Inicio Medida', 'Fin Medida',
          'Decisión', 'Juez', 'Secretario', 'Variación de medida'
        ];
        tablaDetalleMedidas.filas = filasDetalleMedidas;


        request.variables = {
          "[IMG-BASE64]": base64,
          "[TITULO-INFORME]": tituloPantalla,
          "[TITULO-PLANTILLA]": tituloPantalla,
          "[FECHA-REGISTRO]": fecha,
          "[HORA-REGISTRO]": hora,
          "[CENTRO]": this.funcionarioActivo.departamento,
          "[NUM-EXPEDIENTE]": proceso.numExpediente,
          "[NUM-OFICIO]": proceso.numOficio,
          "[NUM-EXPJUDICIAL]": proceso.numExpedienteJudicial,
          "[MOTIVO-INGRESO]": proceso.motivoIngreso,
          "[OBSERVACION]": proceso.observacion,
          "[FECHA-OFICIO]": fechaOficion,
          "[TABLA-EXPEDIENTE]": JSON.stringify(tablaExpediente),
          "[NOMBRE-ADOLESCENTE]": nombreAdolescente,
          "[FECHA-NACIMIENTO]": fechaNacimientoFormateada,
          "[LUGAR-NACIMIENTO]": this.fichaIdentifacion.lugarNacimiento,
          "[NUMERO-DOCUMENTO]": this.fichaIdentifacion.numeroDocumento,
          "[DIRECCION]": this.fichaIdentifacion.direccion,
          "[GRADO-INSTRUCCION]": this.fichaIdentifacion.modalidadEstudio,
          "[TABLA-DELITO]": JSON.stringify(tablaDelito),
          "[TABLA-DETALLE-MEDIDAS]": JSON.stringify(tablaDetalleMedidas),
          "[EDAD]": `${this.funcionesUtils.getEdad(this.fichaIdentifacion.fechaNacimiento)}`

          // "[FECHA-INICIOMEDIDA]": this.formatFecha(proceso.fechaInicioMedida),
          // "[FECHA-FINMEDIDA]": this.formatFecha(proceso.fechaFinMedida),
          // "[FECHA-RESOLUCION]": this.formatFecha(proceso.fechaResolucion),
          // "[DECISION]": proceso.decision,
          // "[JUEZ]": proceso.juez,
        };
        this.pdfService.generarPdf(request, '').subscribe({
          next: (response: RespuestaPorDefecto<string>) => {
            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al generar el PDF. Inténtalo de nuevo.'
              );
              return;
            }
            const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
            window.open(url);
          },
          error: () => {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
          }
        });
      },
      error: () => {
        this.dialogMensajeService.mensajeError('No se pudo cargar la imagen para el PDF.');
      }
    });
  }

  obtenerFichaIdentificacion(): Observable<any> {
    return this.fichaIdentificacionService
      .obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
      .pipe(
        tap((response) => {
          console.log("Ficha de Identificación cargada:", response.data);
          this.fichaIdentifacion = response.data
        }),
        catchError((error) => {
          console.error("Error al obtener ficha de identificación:", error);
          return of(null);
        })
      );
  }

  loadImageAsBase64(): Observable<string> {
    return new Observable((observer) => {
      this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
        .subscribe({
          next: (data: ArrayBuffer) => {
            const base64String = this.arrayBufferToBase64(data);
            this.base64Image = `data:image/png;base64,${base64String}`;
            observer.next(this.base64Image);
            observer.complete();
          },
          error: (err) => {
            console.error('Error al cargar imagen:', err);
            observer.error(err);
          }
        });
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
}

