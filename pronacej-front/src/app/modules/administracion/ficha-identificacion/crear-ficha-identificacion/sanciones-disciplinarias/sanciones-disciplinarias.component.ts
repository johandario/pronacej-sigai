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
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { SancionDisciplinariaService } from 'app/modules/administracion/services/sancionDisciplinaria.service';
import { SancionDisciplinariaDTO } from 'app/core/model/both/ia/SancionDisciplinariaDTO.model';
import { PermisoSalidaDTO } from 'app/core/model/both/salida/PermisoSalidaDTO.model';
import { catchError, Observable, of, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { PopupDocumentosComponent } from 'app/core/components/documentos/popup-documentos/popup-documentos.component';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';
import { MatDialog } from '@angular/material/dialog';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';



@Component({
  selector: 'app-sanciones-disciplinarias',
  standalone: true,
  imports: [TablaListaComponent],
  templateUrl: './sanciones-disciplinarias.component.html',
  styleUrl: './sanciones-disciplinarias.component.scss'
})
export class SancionesDisciplinariasComponent implements OnInit{
  tituloPantalla: string = "";
  
    page = 0;
    listSize = [5, 10, 15, 20];
    size = this.listSize[0];
    totalItems = 0;
    uuid_fp: string;
  
    listaProcesos: SancionDisciplinariaDTO[] = [];
    paginacionRequest: PaginacionRequest = new PaginacionRequest();
    paginacion: Paginacion = new Paginacion();
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_SANCIONES_DISCIPLINARIAS;
    base64Image: string | null = null;
    fichaIdentifacion: FichaIdentificacionDTO
    funcionarioActivo: FuncionarioDTO;
    nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
  
    @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;
  
    keyLabelsTable: any = {
      idSancionDisciplinaria: "No.",
      acciones: "Acciones",
      fechaRegistro: "Fecha de registro",
      nombreAdolescente: "Nombre adolescente",
      nroResolucion: "Nro. resolución",
      motivo: "Motivo de la sanción",
      nombreTipificacion: "Tipifición de la falta",
      sancion: "Sanción",
      
  
    };
  
    constructor(
      private router: Router,
      private dialogMensajeService: DialogMensajeService,
      private route: ActivatedRoute,
      private authSerguridadServicio: AuthSerguridadServicio,
      private sancionDisciplinariaService: SancionDisciplinariaService,
      private http: HttpClient,
      private pdfService: PdfService,
      private funcionesUtils: FuncionesUtils,
      private fichaIdentificacionService: FichaIdentificacionService,
      private funcionarioService: FuncionarioService,
      private dialog: MatDialog,
      private servicioMensajes: DialogMensajeService,
    ) { }

    async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_PERMISO_DE_SALIDA"
    );
    
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];
    this.obtenerFichaIdentificacion().subscribe({
      next: () => {
      },
      error: (err) => {
        console.error('Error al cargar ficha de identificación:', err);
        this.obtenerProcesos();
      }
    });
    registerLocaleData(localeEs, 'es-ES');
    this.obtenerProcesos();
    await this.obtenerTokenDepartamento();
  }


  obtenerProcesos() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.filter = this.paginacionRequest.filter;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    console.log(this.paginacionRequest);
    
    this.sancionDisciplinariaService.obtenerSancionesPorTokenFicha(this.paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SancionDisciplinariaDTO>>) => {
          if (!response.exito) {
            this.sancionDisciplinariaService.checkError(response);
            return;
          }
          this.listaProcesos = response.data.data
          console.log(this.listaProcesos);

          this.paginacion.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.sancionDisciplinariaService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.tokenIdentificador = this.uuid_fp;
    
    this.sancionDisciplinariaService.obtenerSancionesPorTokenFicha(this.paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SancionDisciplinariaDTO>>) => {
          if (!response.exito) {
            this.sancionDisciplinariaService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.sancionDisciplinariaService.checkError(error);
        }
      }
    );
  }

    agregarProceso() {
  const uuid = this.uuid_fp;
  this.router.navigate([
    '/gestion-adolescente/ficha-identificacion/crear-editar/sancionesDisciplinarias',
    uuid,
    'crear'
  ]);
}

  


  
  editarProceso(proceso: SancionDisciplinariaDTO) {
    console.log(this.uuid_fp);
    
    this.router.navigate(
      ['/gestion-adolescente/ficha-identificacion/crear-editar/sancionesDisciplinarias/ver', proceso.tokenIdentificador],
      {
        queryParams: { uuid_fp: this.uuid_fp }
      }
    );
    console.log(this.uuid_fp);
    console.log(proceso);
    
    
  }


  eliminarProceso(sancionDisciplinariaDTO: SancionDisciplinariaDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar este registro, esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando..");
            console.log(sancionDisciplinariaDTO);
            
            this.sancionDisciplinariaService.eliminarSancion(sancionDisciplinariaDTO, this.nemonicoMenu).subscribe(
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

  visualizar(proceso: PermisoSalidaDTO) {
    this.router.navigate(
      ['/gestion-adolescente/ficha-identificacion/crear-editar/sancionesDisciplinarias/ver', proceso.tokenIdentificador],
      {
        queryParams: { uuid_fp: this.uuid_fp, mode: 'ver' }
      }
    );

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

  if (!this.fichaIdentifacion) {
    this.dialogMensajeService.mensajeError('No se pudo cargar la ficha de identificación del adolescente.');
    console.error('Ficha de identificación no cargada aún');
    return;
  }

  this.loadImageAsBase64();

  setTimeout(() => {
    const fechaActual = this.formatFecha((new Date()).toString());
    const horaActual = this.formatHora((new Date()).toString());
    const request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_SANCION_DISCIPLINARIA;

    const nombreAdolescente = `${this.fichaIdentifacion.nombres ?? ''} ${this.fichaIdentifacion.apellidoPaterno ?? ''} ${this.fichaIdentifacion.apellidoMaterno ?? ''}`.trim();
    const fechaNacimientoFormateada = this.formatFecha(this.fichaIdentifacion.fechaNacimiento?.toString());
    const titulopantalal= 'Sanciones disciplinarias'
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]": titulopantalal,
      "[TITULO-INFORME]":titulopantalal,
      "[FECHA-REGISTRO]": fechaActual,
      "[HORA-REGISTRO]": horaActual,
      "[OBSERVACIONES]": proceso.observacion,
      "[SANCION]": proceso.sancion,
      "[AMBIENTE]": proceso.ambiente?.nombre ?? '',
      "[FALTA]": proceso.falta,
      "[PROGRAMA]": proceso.programa?.nombre ?? '',
      "[RESOLUCION]": proceso.nroResolucion,
      "[MOTIVO]": proceso.motivo,
      "[TIPIFICACION]": proceso.tipificacionFalta?.nombre ?? '',
      "[FECHA-INICIO]": this.formatFecha(proceso.fechaInicio?.toString()),
      "[FECHA-FIN]": this.formatFecha(proceso.fechaFin?.toString()),
      "[FECHA-REGISTROSANCION]": this.formatFecha(proceso.fechaRegistro?.toString()),
      "[CENTRO]": this.funcionarioActivo?.departamento ?? '',
      "[NOMBRE-ADOLESCENTE]": nombreAdolescente,
      "[NUMERO-DOCUMENTO]": this.fichaIdentifacion.numeroDocumento,
      "[EDAD]": `${this.funcionesUtils.getEdad(this.fichaIdentifacion.fechaNacimiento)}`
    };
    console.log('📄 Generando PDF con los siguientes datos:', {
  fichaIdentifacion: {
    nombres: this.fichaIdentifacion?.nombres,
    apellidoPaterno: this.fichaIdentifacion?.apellidoPaterno,
    apellidoMaterno: this.fichaIdentifacion?.apellidoMaterno,
    fechaNacimiento: this.fichaIdentifacion?.fechaNacimiento,
    numeroDocumento: this.fichaIdentifacion?.numeroDocumento,
  },
  funcionario: {
    departamento: this.funcionarioActivo?.departamento,
  },
  proceso: {
    observacion: proceso?.observacion,
    sancion: proceso?.sancion,
    ambiente: proceso?.ambiente?.nombre,
    falta: proceso?.falta,
    programa: proceso?.programa?.nombre,
    nroResolucion: proceso?.nroResolucion,
    motivo: proceso?.motivo,
    tipificacionFalta: proceso?.tipificacionFalta?.nombre,
    fechaInicio: proceso?.fechaInicio,
    fechaFin: proceso?.fechaFin,
    fechaRegistro: proceso?.fechaRegistro,
  },
  variablesFinales: request.variables,
  base64Image: this.base64Image ? '✅ Imagen cargada' : '❌ Imagen NO cargada'
});
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


     
      subirDocumento(sancionDTO: SancionDisciplinariaDTO) {
        
          console.log('Estado completo al subir documento SANCIONES:', {
            item: sancionDTO,
            nemonicoMenu: etiquetasModel.NEMONICO_MENU_SANCIONES_DISCIPLINARIAS,
            nemonicoCarpeta: etiquetasModel.CARPETA_SANCIONES_DISCIPLINARIA,
            tipoServicio: 'sancionDisciplinaria'
          });
      
          // Verifica que evaluacionDomiciliariaDTO tenga un tokenIdentificador
          // if (!sancionDTO || !sancionDTO.tokenIdentificador) {
          //   this.servicioMensajes.mensajeError('No se puede subir el documento sin una sancion valida.');
          //   return;
          // }
      
          // Abrir el popup y pasar la lista de documentos
          const dialogRef = this.dialog.open(SubidaDocumentoGenericoComponent, {
            width: '1200px',
            height: '700px',
            data: {
              item: sancionDTO,
              nemonicoMenu: etiquetasModel.NEMONICO_MENU_SANCIONES_DISCIPLINARIAS,
              nemonicoCarpeta: etiquetasModel.CARPETA_SANCIONES_DISCIPLINARIA,
              tipoServicio: 'sancionDisciplinaria',
              seccionTipoDocumento: etiquetasModel.SECCION_FICHA_IDENT_SANCION_DISCIPLINARIA
            }
          });
        }

        verDocumentos(sancionDTO: SancionDisciplinariaDTO) {
            // Abrir el popup y pasar la lista de documentos
            const dialogRef = this.dialog.open(PopupDocumentosComponent, {
              width: '1000px',
              height: '500px',
              data: {
                tokenItem: sancionDTO.tokenIdentificador,
                tipoServicio: "SANCION_DISCIPLINARIA",
                nemonicoMenu: etiquetasModel.NEMONICO_MENU_SANCIONES_DISCIPLINARIAS
              }
            });
          }


         
}
