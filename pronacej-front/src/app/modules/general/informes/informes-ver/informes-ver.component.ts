import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { InformeDTO } from 'app/core/model/both/informe/informeDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { InformeService } from 'app/modules/general/services/informe.service';
import { HttpClient } from '@angular/common/http';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-informes-ver',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    TablaDatosComponent
  ],
  templateUrl: './informes-ver.component.html',
  styleUrl: './informes-ver.component.scss'
})
export class InformesVerComponent {

  esEdicion: boolean = false;
  uuid_fp: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORMES;
  titulo: string = "informe";
  listaInformes: InformeDTO[] = [];
  base64Image: string | null = null;

  paginacion: Paginacion = new Paginacion();
  paginacionRequest: PaginacionRequest = new PaginacionRequest();

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;
  @Output() editarInformeEvent = new EventEmitter<InformeDTO>();

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaRegistro: "Fecha de registro",
    asignado: "Asignado a",
    tipo: "Tipo de informe",
    firmado: "Firmado"
  };

  constructor(
    private informeService: InformeService,
    private dialogMensajeService: DialogMensajeService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private catalogoService: CatalogoService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) { }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_INFORMES"
    );
    this.uuid_fp = this.route.snapshot.paramMap.get('uuid_fp');

    if (this.uuid_fp) {
      this.esEdicion = true;
      this.obtenerInformesPorToken();
    }
    else {
      this.esEdicion = false;
      this.obtenerInformes();
    }
  }

  eliminarInforme(informeDTO: InformeDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar el informe? esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando el usuario..");
            this.informeService.eliminarInforme(informeDTO, this.nemonicoMenu).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  if (this.uuid_fp)
                    this.obtenerInformesPorToken();
                  else
                    this.obtenerInformes();
                },
                error: (error: any) => {
                  load.close();

                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
                  );
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerInformes() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;

    this.informeService.obtenerInformes(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<InformeDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaInformes = response.data.data;
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

  obtenerInformesPorToken() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.informeService.obtenerInformesPorToken(this.paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<InformeDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaInformes = response.data.data;
          this.listaInformes.forEach(informe => {
            if (informe.fechaRegistro) informe.fechaRegistro = new Date(informe.fechaRegistro);
          })
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

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;

    if (this.esEdicion) {
      this.paginacionRequest.tokenIdentificador = this.uuid_fp;

      this.informeService.obtenerInformesPorToken(this.paginacionRequest, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<InformeDTO>>) => {

            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
              );
              return;
            }

            this.tablaComponent.exportXLSX(response.data.data);
          },
          error: (error: any) => {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
          }
        }
      );
    }
    else {
      this.informeService.obtenerInformes(this.paginacionRequest, this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<InformeDTO>>) => {

            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
              );
              return;
            }

            this.tablaComponent.exportXLSX(response.data.data);
          },
          error: (error: any) => {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
          }
        }
      );
    }
  }

  refrescar() {
    if (this.uuid_fp)
      this.obtenerInformesPorToken();
    else
      this.obtenerInformes();
  }

  agregarInforme() {
    this.router.navigate(['crear'], { state: { listaPrev: "true" }, relativeTo: this.route });
  }

  editarInforme(informeDTO: InformeDTO) {
    this.router.navigate(['editar'], { state: { item: informeDTO, listaPrev: "true" }, relativeTo: this.route });
  }

  verPlantillas() {
    this.router.navigate(['plantillas'], { state: { informePrev: "true" }, relativeTo: this.route });
  }

  confirmarImpresion(informeDTO: InformeDTO): void {
    this.loadImageAsBase64();
    if (!informeDTO.impreso) {
      this.dialogMensajeService.mensajeConConfirmacion(
        'Confirmar Impresión', 'Está seguro de realizar la impresión? No se podrá editar el informe después.'
      ).afterClosed().subscribe((result) => {
        if (result == "confirmed") {
          this.actualizarEstadoImpreso(informeDTO);
          this.imprimir(informeDTO);
        }
      });
    }
    else
      this.imprimir(informeDTO);
  }

  async imprimir(informeDTO: InformeDTO) {
    this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(informeDTO.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaDTO = response.data;

          // Construir los nuevos campos dinámicamente
          const lugarFechaNacimiento = `${fichaDTO.lugarNacimiento || ''}, ${this.formatFecha(fichaDTO.fechaNacimiento)}`;
          const edadActual = this.funcionesUtils.getEdad(fichaDTO.fechaNacimiento).toString() || 'N/A';
          const direccion = fichaDTO.direccion || 'N/A';

          //Obtener grado de instrucción desde catálogos
          this.catalogoService.obtenerCatalogoPorNemonico(fichaDTO.modalidadEstudio, '').subscribe({
            next: (respuestaCatalogo: RespuestaPorDefecto<CatalogoDTO>) => {
              const catalogoModalidadEstudio = respuestaCatalogo.data;
              const gradoInstruccion = catalogoModalidadEstudio?.nombre || '';

              let request = new GeneracionPdfRequest();
              request.nemonico = etiquetasModel.FORMULARIO_INFORME;
              request.variables = {
                "[TITULO-PLANTILLA]": informeDTO.tipo,
                "[IMG_BASE64]": this.base64Image,
                "[FECHA_REGISTRO]": this.formatFecha(informeDTO.fechaRegistro.toString()),
                "[HORA_REGISTRO]": this.formatHora(informeDTO.fechaRegistro.toString()),
                "[TITULO-INFORME]": informeDTO.tipo,
                "[ADOLESCENTE]": informeDTO.asignado,
                "[LUGAR_FECHA_NACIMIENTO]": lugarFechaNacimiento,
                "[CENTRO]": fichaDTO.centroIngreso,
                "[EDAD_ACTUAL]": edadActual,
                "[GRADO_INSTRUCCION]": gradoInstruccion,
                "[DIRECCION]": direccion,
                "[INFORME]": informeDTO.idInforme.toString(),
              }
              this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
                next: (response: RespuestaPorDefecto<string>) => {

                  if (!response.exito) {
                    this.dialogMensajeService.mensajeError(
                      'Hubo un problema al general el pdf. ' + response.mensaje
                    );
                    return;
                  }

                  const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

                  const pwa = window.open(url);
                },
                error: (error: any) => {
                  this.dialogMensajeService.mensajeError(
                    'Hubo un problema al general el pdf. Inténtalo de nuevo.'
                  );
                }
              });
            },
            error: (error: any) => {
              console.error('Error al obtener el catálogo:', error);
              this.dialogMensajeService.mensajeError('Error al obtener el catálogo');
            }
          });
        },
        error: (error: any) => {
          this.fichaIdentificacionService.checkError(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.paginacion.pageSize = pageEvent.pageSize;
    this.paginacion.pageIndex = pageEvent.pageIndex;

    if (this.esEdicion)
      this.obtenerInformesPorToken();
    else
      this.obtenerInformes();
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

    if (this.esEdicion)
      this.obtenerInformesPorToken();
    else
      this.obtenerInformes();
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;

    if (this.esEdicion)
      this.obtenerInformesPorToken();
    else
      this.obtenerInformes();
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

  actualizarEstadoImpreso(informeDTO: InformeDTO) {
    informeDTO.impreso = true;

    this.informeService.actualizarInforme(informeDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<InformeDTO>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al actualizar el informe. Inténtalo de nuevo.'
          );
        }

        return;
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar el informe. Inténtalo de nuevo.'
        );
      }
    });
  }

  // Función para cargar la imagen como base64
  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  // Función para convertir el ArrayBuffer a base64
  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }
}
