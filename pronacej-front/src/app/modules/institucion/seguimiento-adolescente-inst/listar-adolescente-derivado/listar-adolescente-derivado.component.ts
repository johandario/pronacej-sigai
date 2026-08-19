import { Component, OnInit, ViewChild } from '@angular/core';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { AdolescenteDerivadoInstService } from '../adolescente-derividado.service';
import { AdolescDerivadoInstDTO } from 'app/core/model/both/salida/AdolescDerivadoInstDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { TabService } from 'app/core/services/tab.service';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { catchError, Observable, Subject, takeUntil, tap, of } from 'rxjs';
import { User } from 'app/core/user/user.types';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-listar-adolescente-derivado',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './listar-adolescente-derivado.component.html',
  styleUrl: './listar-adolescente-derivado.component.scss'
})
export class ListarAdolescenteDerivadoComponent implements OnInit {
  tituloPantalla: string = "";

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  listaProcesos: AdolescDerivadoInstDTO[] = [];
  uuid_fp!: string;
  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  base64Image: string | null = null;
  user: User;
  funcionarioActivo: FuncionarioDTO;
  fichaIdentifacion: FichaIdentificacionDTO
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_DERIVADO_INSTITUCIONES;


  private _unsubscribeAll: Subject<any> = new Subject<any>();
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    idAdolescenteDerivado: "No.",
    acciones: "Acciones",
    estado: "Estado",
    fechaCreacion: "Fecha creación",
    personaResponsable: "Persona responsable",
    nombreInstitucion: "Nombre institución",
    departamento: "Departamento",
    servicio: "Servicio a brindar",
    tiempoServicio: "Tiempo de servicio",
    fechaDerivacion: "Fecha derivación",


  };

  constructor(
    private router: Router,
    private dialogMensajeService: DialogMensajeService,
    private salidaService: AdolescenteDerivadoInstService,
    private route: ActivatedRoute,
    private servicioTab: TabService,
    private http: HttpClient,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private funcionarioService: FuncionarioService,
    private fichaIdentificacionService: FichaIdentificacionService,

  ) { }

  // ngOnInit(): void {
  //   this.uuid_fp = this.route.snapshot.params['uuid_fp'];
  //    registerLocaleData(localeEs, 'es-ES');
  //   this.obtenerProcesos();
  // }

  async ngOnInit(): Promise<void> {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    registerLocaleData(localeEs, 'es-ES');
    await this.obtenerTokenDepartamento();
    this.obtenerFichaIdentificacion().subscribe({
      next: () => {
      },
      error: (err) => {
        console.error('Error al cargar ficha de identificación:', err);
        this.obtenerProcesos();
      }
    });
    this.obtenerProcesos();
  }


  obtenerProcesos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    console.log(this.paginacionRequest);

    this.salidaService.obtenerRegistroSalidas(this.paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<AdolescDerivadoInstDTO>>) => {
          if (!response.exito) {
            this.salidaService.checkError(response);
            return;
          }
          this.listaProcesos = response.data.data
          console.log(this.listaProcesos);

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
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;


    this.salidaService.obtenerRegistroSalidas(this.paginacionRequest).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<AdolescDerivadoInstDTO>>) => {
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
    this.servicioTab.cambiarTab(0);
    this.router.navigate(['crear-derivado-institucion'], {
      relativeTo: this.route,
      state: {
        crear: true,
        tokenAdolescente: this.uuid_fp
      }
    });
  }

  editarProceso(proceso: AdolescDerivadoInstDTO) {
    this.servicioTab.cambiarTab(0);
    this.router.navigate([`crear-editar-derivado-institucion/${proceso.tokenIdentificador}`], {
      relativeTo: this.route,
      state: {
        editar: true,
        proceso: proceso,
        tokenAdolescente: this.uuid_fp
      }
    });
  }

  verProceso(proceso: AdolescDerivadoInstDTO) {
    this.router.navigate([`crear-editar-derivado-institucion/${proceso.tokenIdentificador}`], {
      relativeTo: this.route,
      state: {
        visualizar: true,
        proceso: proceso,
        tokenAdolescente: this.uuid_fp
      }
    });
  }

  eliminarProceso(gestionFugaDTO: AdolescDerivadoInstDTO) {
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
            this.salidaService.eliminarSalida(gestionFugaDTO, "").subscribe(
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
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
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

  generarPDF(proceso: any) {
    if (!proceso) {
      console.error('No se recibió proceso para generar PDF');
      return;
    }
    console.log(proceso);

    this.loadImageAsBase64();
    setTimeout(() => {
      const fechaActual = this.formatFecha((new Date()).toString());
      const horaActual = this.formatHora((new Date()).toString());
      const request = new GeneracionPdfRequest();
      request.nemonico = etiquetasModel.FORMULARIO_ADOLESCENTE_DERIVADO;
      const nombreAdolescente = `${this.fichaIdentifacion.nombres ?? ''} ${this.fichaIdentifacion.apellidoPaterno ?? ''} ${this.fichaIdentifacion.apellidoMaterno ?? ''}`.trim();
      const fechaNacimientoFormateada = this.formatFecha(this.fichaIdentifacion.fechaNacimiento?.toString());
      const institucion = proceso.institucion.nombreOrganizacion;

      request.variables = {
        "[IMG_BASE64]": this.base64Image,
        "[TITULO-PLANTILLA]": "Informe de adolescente derivado a institución",
        "[TITULO-INFORME]": "Informe de adolescente derivado a institución",
        "[FECHA-REGISTRO]": fechaActual,
        "[HORA-REGISTRO]": horaActual,
        "[FECHA-DERIVACION]": this.formatFecha(proceso.fechaDerivacion),
        "[PERSONA-RESPONSABLE]": proceso.personaResponsable,
        "[ESTADO]": proceso.estado,
        "[NOMBRE-INSTITUCION]": institucion,
        "[DEPARTAMENTO]": proceso.departamento,
        "[TIEMPO-SERVICIO]": proceso.tiempoServicio,
        "[SERVICIO-BRINDAR]": proceso.servicio,
        "[CENTRO]": this.funcionarioActivo.departamento,
        "[NOMBRE-ADOLESCENTE]": nombreAdolescente,
        "[FECHA-NACIMIENTO]": fechaNacimientoFormateada,
        "[LUGAR-NACIMIENTO]": this.fichaIdentifacion.lugarNacimiento,
        "[NUMERO-DOCUMENTO]": this.fichaIdentifacion.numeroDocumento,
        "[EDAD]": `${this.funcionesUtils.getEdad(this.fichaIdentifacion.fechaNacimiento)}`
      };

      this.pdfService.generarPdf(request, '').subscribe({
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


  obtenerTokenDepartamento(): Promise<void> {
    return new Promise((resolve) => {
      this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenuinicio).subscribe({
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

}
