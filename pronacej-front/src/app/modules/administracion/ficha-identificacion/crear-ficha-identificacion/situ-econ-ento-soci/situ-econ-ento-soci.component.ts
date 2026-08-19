import { Component, ViewChild } from '@angular/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { EvaluacionSocialDTO } from 'app/core/model/both/EvaluacionSocialDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { EvaluacionSocialService } from 'app/modules/seguridad/services/evaluacionSocial.service';
import { environment } from 'environments/environment';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { Sort } from '@angular/material/sort';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { PaginacionPersonasRelacionadasRequest } from 'app/core/model/request/paginacionPersonaRelacionadaRequest.model';
import { firstValueFrom } from 'rxjs';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { EvaluacionSocialArtefactoService } from 'app/modules/seguridad/services/evaluacionSocialArtefactoService';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-situ-econ-ento-soci',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    TablaDatosComponent
  ],
  templateUrl: './situ-econ-ento-soci.component.html',
  styleUrl: './situ-econ-ento-soci.component.scss'
})
export class SituEconEntoSociComponent {

  uuid_fp: string;
  base64Image: string | null = null;

  tituloPantalla: string = "situación económica y entorno social";
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SITUACION_ECONOMICA_ENTORNO_SOCIAL;

  listaEvaluacionesSociales: EvaluacionSocialDTO[] = [];
  paginacion: Paginacion = new Paginacion();
  solicitudPaginacion: PaginacionRequest = new PaginacionRequest();

  // Listas para catálogos
  listaArtefactosCatalogo: CatalogoDTO[] = [];
  listaZonasViviendas: CatalogoDTO[] = [];
  listaMaterialesParedVivienda: CatalogoDTO[] = [];
  listaMaterialesPisoVivienda: CatalogoDTO[] = [];
  listaMaterialesTechoVivienda: CatalogoDTO[] = [];
  listaTiposAbastecimientoAgua: CatalogoDTO[] = [];
  listaTiposVivienda: CatalogoDTO[] = [];
  listaTiposAlumbrado: CatalogoDTO[] = [];
  listaCombustibleCocinar: CatalogoDTO[] = [];
  listaTiposDesague: CatalogoDTO[] = [];
  listaTenencias: CatalogoDTO[] = [];

  @ViewChild('tabla') tablaComponent: TablaDatosComponent<any>;

  columnasTabla: any = {
    numero: "No.",
    acciones: "Acciones",
    fechaCreacion: "Fecha de creación",
    usuarioRegistro: "Usuario que registró",
    grupoAmical: "Grupo amical",
    factorRiesgoMedio: "Factores de riesgo",
  };

  constructor(
    private servicioEvaluacionSocial: EvaluacionSocialService,
    private servicioMensajes: DialogMensajeService,
    private servicioDatosFamiliares: DatosFamiliaresService,
    private servicioEvaluacionSocialArtefacto: EvaluacionSocialArtefactoService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private enrutador: Router,
    private rutaActiva: ActivatedRoute,
    public utilidades: FuncionesUtils,
    private http: HttpClient,
    private servicioPdf: PdfService
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarDatosCatalogo();
    this.obtenerListaSituEconEntoSoci();
  }

  cargarDatosCatalogo() {
    const catalogos = [
      { clave: 'ARTEFACTOS_VIVIENDA', lista: 'listaArtefactosCatalogo' },
      { clave: 'ZONA_VIVIENDA', lista: 'listaZonasViviendas' },
      { clave: 'MATERIAL_PARED', lista: 'listaMaterialesParedVivienda' },
      { clave: 'MATERIAL_TECHO', lista: 'listaMaterialesTechoVivienda' },
      { clave: 'MATERIAL_PISO', lista: 'listaMaterialesPisoVivienda' },
      { clave: 'ABASTECIMIENTO_AGUA', lista: 'listaTiposAbastecimientoAgua' },
      { clave: 'TIPOS_VIVIENDA', lista: 'listaTiposVivienda' },
      { clave: 'TIPO_ALUMBRADO', lista: 'listaTiposAlumbrado' },
      { clave: 'COMBUSTIBLE_COCINA', lista: 'listaCombustibleCocinar' },
      { clave: 'DESAGUE_VIVIENDA', lista: 'listaTiposDesague' },
      { clave: 'TENENCIA_VIVIENDA', lista: 'listaTenencias' },
    ];

    catalogos.forEach(catalogo => {
      this.utilidades.obtenerListaCatalogo(catalogo.clave, this.nemonicoMenu).subscribe({
        next: (data) => {
          this[catalogo.lista] = data;
          console.log(`Catálogo ${catalogo.clave}:`, {
            nombreLista: catalogo.lista,
            datos: data,
            cantidad: data.length
          });
        },
        error: (error) => {
          console.error(`Error cargando ${catalogo.clave}:`, error);
        }
      });
    });
  }

  visualizarSituEconEntoSoci(evaluacionSocialDTO: EvaluacionSocialDTO) {
    evaluacionSocialDTO.esVisualizacion = true;
    this.enrutador.navigate(['crear-editar-situ-econ'], {
      state: { evaluacionSocialDTO },
      relativeTo: this.rutaActiva
    });
  }

  editarSituEconEntoSoci(evaluacionSocialDTO: EvaluacionSocialDTO) {
    this.enrutador.navigate(['crear-editar-situ-econ'], {
      state: { evaluacionSocialDTO },
      relativeTo: this.rutaActiva
    });
  }

  imprimir(evaluacionSocialDTO: EvaluacionSocialDTO) {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir la situación económica y entorno social?",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión...");

          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;

                const promesaPersonas = firstValueFrom(
                  this.servicioDatosFamiliares.obtenerPersonasRelacionadasPorEvaluacionSocial({
                    tokenIdentificador: evaluacionSocialDTO.tokenIdentificador,
                    page: 0,
                    size: 100
                  } as PaginacionPersonasRelacionadasRequest)
                );

                const promesaArtefactos = firstValueFrom(
                  this.servicioEvaluacionSocialArtefacto.obtenerArtefactosPorEvaluacionSocialPaginado({
                    tokenIdentificador: evaluacionSocialDTO.tokenIdentificador,
                    page: 0,
                    size: 100
                  } as PaginacionRequest, this.nemonicoMenu)
                );

                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                  .subscribe({
                    next: async (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }

                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;

                      try {
                        const [respuestaPersonas, respuestaArtefactos] = await Promise.all([promesaPersonas, promesaArtefactos]);

                        let tablaPersonaRelacionada = new TablaPlantilla();
                        tablaPersonaRelacionada.encabezados = [
                          'Nombres',
                          'Parentesco',
                          'N° documento',
                          'Salario',
                          'N° hijos',
                          'Resp. económico'
                        ];

                        if (respuestaPersonas.exito) {
                          tablaPersonaRelacionada.filas = respuestaPersonas.data.data.map(persona => ({
                            'Nombres': this.utilidades.escaparHTML(persona.nombres || "Sin nombre"),
                            'Parentesco': this.utilidades.escaparHTML(persona.parentesco || "No especificado"),
                            'N° documento': this.utilidades.escaparHTML(persona.numeroDocumento || "No especificado"),
                            'Salario': this.utilidades.escaparHTML(persona.ingresoPromedio ? `S/ ${persona.ingresoPromedio}` : "S/ 0"),
                            'N° hijos': persona.numeroHijos ? Math.trunc(persona.numeroHijos).toString() : '0',
                            'Resp. económico': persona.esResponsableEconom ? 'Si' : 'No'
                          }));
                        }

                        let tablaArtefacto = new TablaPlantilla();
                        tablaArtefacto.encabezados = [
                          'Artefacto',
                          'Cantidad'
                        ];

                        if (respuestaArtefactos.exito) {
                          tablaArtefacto.filas = respuestaArtefactos.data.data.map(artefacto => ({
                            'Artefacto': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(artefacto.tokenIdentificadorArtefactosVivienda, this.listaArtefactosCatalogo) || 'No especificado'),
                            'Cantidad': Math.trunc(artefacto.cantidad).toString() || '0'
                          }));
                        }

                        let solicitudPdf = new GeneracionPdfRequest();
                        solicitudPdf.nemonico = 'FORMULARIO_SITUACION_ECONOMICA_ENTORNO_SOCIAL';

                        solicitudPdf.variables = {
                          "[IMG_BASE64]": imagenBase64,
                          "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                          "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                          "[CENTRO]": this.utilidades.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                          "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                          "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                          "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                          "[EDAD]": this.utilidades.escaparHTML(edadActual),
                          "[TABLA-PERSONA-RELACIONADA]": JSON.stringify(tablaPersonaRelacionada),
                          "[TABLA-ARTEFACTO]": JSON.stringify(tablaArtefacto),
                          "[ZONA-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorZonaVivienda, this.listaZonasViviendas) || ''),
                          "[MATERIAL-PARED-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorMaterialParedVivienda, this.listaMaterialesParedVivienda) || ''),
                          "[MATERIAL-PISO-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorMaterialPisoVivienda, this.listaMaterialesPisoVivienda) || ''),
                          "[MATERIAL-TECHO-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorMaterialTechoVivienda, this.listaMaterialesTechoVivienda) || ''),
                          "[TIPO-ABASTESIMIENTO-AGUA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorAbastecimientoAguaVivienda, this.listaTiposAbastecimientoAgua) || ''),
                          "[TIPO-VIVIENDA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorTipoVivienda, this.listaTiposVivienda) || ''),
                          "[TIPO-ALUMBRADO]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorTipoAlumbradoVivienda, this.listaTiposAlumbrado) || ''),
                          "[COMBUSTIBLE-COCINAR]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorCombustibleCocinarVivienda, this.listaCombustibleCocinar) || ''),
                          "[TIPO-DESAGUE]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorTipoDesagueVivienda, this.listaTiposDesague) || ''),
                          "[NUMERO-AMBIENTES]": this.utilidades.escaparHTML(evaluacionSocialDTO.numeroAmbientes?.toString() || ''),
                          "[TENENCIA]": this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(evaluacionSocialDTO.tokenIdentificadorTenencia, this.listaTenencias) || ''),
                          "[NUMERO-OCUPANTES]": this.utilidades.escaparHTML(evaluacionSocialDTO.numeroOcupantes?.toString() || ''),
                          "[NUMERO-HABITACIONES]": this.utilidades.escaparHTML(evaluacionSocialDTO.numeroHabitaciones?.toString() || ''),
                          "[NUMERO-DORMITORIOS]": this.utilidades.escaparHTML(evaluacionSocialDTO.numeroDormitorios?.toString() || ''),
                          "[GRUPO-AMICAL]": this.utilidades.escaparHTML(evaluacionSocialDTO.grupoAmical || ''),
                          "[FACTORES-RIESGO-MEDIO]": this.utilidades.escaparHTML(evaluacionSocialDTO.factorRiesgoMedio || ''),
                          "[AREA-ACADEMICO-LABORAL]": this.utilidades.escaparHTML(evaluacionSocialDTO.areaAcademicoLaboral || ''),
                          "[AREA-SOCIAL-RECREACIONAL]": this.utilidades.escaparHTML(evaluacionSocialDTO.areaSocialRecreacional || ''),
                          "[AREA-FAMILIAR-PAREJA]": this.utilidades.escaparHTML(evaluacionSocialDTO.areaFamiliarPareja || ''),
                          "[AREA-PERSONAL]": this.utilidades.escaparHTML(evaluacionSocialDTO.areaPersonal || '')
                        };

                        this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                          next: (respuesta: RespuestaPorDefecto<string>) => {
                            dialogoCarga.close();
                            if (!respuesta.exito) {
                              console.error('Error al generar PDF:', respuesta);
                              this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                              return;
                            }

                            const url = window.URL.createObjectURL(this.utilidades.getPdfBlob(respuesta.data));
                            window.open(url);
                          },
                          error: (error: any) => {
                            dialogoCarga.close();
                            console.error('Error al generar PDF:', error);
                            this.servicioMensajes.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                          }
                        });
                      } catch (error) {
                        dialogoCarga.close();
                        console.error('Error al procesar datos:', error);
                        this.servicioMensajes.mensajeError('Error al procesar los datos necesarios para el reporte.');
                      }
                    },
                    error: (error: any) => {
                      dialogoCarga.close();
                      console.error('Error al obtener ficha:', error);
                      this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                    }
                  });
              },
              error: (error) => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.servicioMensajes.mensajeError('Error al cargar la imagen del logo');
              }
            });
        }
      }
    });
  }

  eliminarSituEconEntoSoci(evaluacionSocialDTO: EvaluacionSocialDTO) {
    let ref = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar la situación económica/social? Esta operación es irreversible.",
      "¿Desea continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (respuesta: "confirmed" | "cancelled") => {
          if (respuesta == "confirmed") {
            let carga = this.servicioMensajes.mensajeLoading("Eliminando la situación económica/social..");
            this.servicioEvaluacionSocial.eliminarEvaluacionSocial(evaluacionSocialDTO, this.nemonicoMenu).subscribe(
              {
                next: (respuesta: RespuestaPorDefecto<boolean>) => {
                  carga.close();
                  this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

                  if (!respuesta.exito) {
                    return;
                  }

                  this.obtenerListaSituEconEntoSoci();
                },
                error: (error: any) => {
                  carga.close();
                  this.servicioEvaluacionSocial.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  agregarSituEconEntoSoci() {
    this.enrutador.navigate(['crear-editar-situ-econ'], { relativeTo: this.rutaActiva });
  }

  obtenerListaSituEconEntoSoci() {
    let solicitudPaginacion = new PaginacionRequest();

    // Asignamos los valores de paginación actual
    solicitudPaginacion.page = this.paginacion.pageIndex;
    solicitudPaginacion.size = this.paginacion.pageSize;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    // Guardamos el filtro original para usarlo después en el filtrado por fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Verificamos si el filtro parece un formato de fecha
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si parece un filtro de fecha, lo quitamos de la solicitud para el backend
    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = ''; // No enviamos el filtro de fecha al backend
    } else if (this.solicitudPaginacion && this.solicitudPaginacion.filter) {
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Incluimos la configuración de ordenamiento si existe
    if (this.solicitudPaginacion && this.solicitudPaginacion.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioEvaluacionSocial.obtenerEvaluacionesSocialesPaginado(solicitudPaginacion, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionSocialDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // Convertir los datos recibidos
        let evaluaciones = respuesta.data.data.map(evaluacion => {
          return {
            ...evaluacion,
            usuarioRegistro: evaluacion.nombreCompletoUsuarioCreacion || 'No especificado',
            grupoAmical: evaluacion.grupoAmical || 'No especificado',
            factorRiesgoMedio: evaluacion.factorRiesgoMedio || 'No especificado'
          } as EvaluacionSocialDTO;
        });

        // Si tenemos un filtro y parece ser una fecha, aplicamos el filtro manual
        if (esFiltroDeFecha && filtroOriginal) {
          evaluaciones = this.utilidades.filtrarPorFecha(evaluaciones, filtroOriginal, 'fechaCreacion');
          this.paginacion.totalItems = evaluaciones.length;
        } else {
          this.paginacion.totalItems = respuesta.data.totalItems;
        }

        this.listaEvaluacionesSociales = evaluaciones;
      },
      error: (error: any) => {
        console.error('Error al obtener evaluaciones sociales:', error);
        this.servicioMensajes.mensajeError('Ocurrió un error al obtener los datos. Por favor, inténtelo nuevamente.');
      }
    });
  }

  descargarExcelCompleto() {
    let solicitudPaginacion = new PaginacionRequest();

    // Asignamos los valores de paginación actual
    solicitudPaginacion.page = 0;
    solicitudPaginacion.size = 100000;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    // Guardamos el filtro original para usarlo después en el filtrado por fecha
    const filtroOriginal = this.solicitudPaginacion?.filter || '';

    // Verificamos si el filtro parece un formato de fecha
    const esFiltroDeFecha = this.utilidades.esPosibleFiltroFecha(filtroOriginal);

    // Si parece un filtro de fecha, lo quitamos de la solicitud para el backend
    if (esFiltroDeFecha) {
      solicitudPaginacion.filter = ''; // No enviamos el filtro de fecha al backend
    } else if (this.solicitudPaginacion && this.solicitudPaginacion.filter) {
      solicitudPaginacion.filter = this.solicitudPaginacion.filter;
    }

    // Incluimos la configuración de ordenamiento si existe
    if (this.solicitudPaginacion && this.solicitudPaginacion.sort) {
      solicitudPaginacion.sort = this.solicitudPaginacion.sort;
      solicitudPaginacion.direction = this.solicitudPaginacion.direction;
    }

    this.servicioEvaluacionSocial.obtenerEvaluacionesSocialesPaginado(solicitudPaginacion, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<EvaluacionSocialDTO>>) => {
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }

        // Convertir los datos recibidos
        let evaluaciones = respuesta.data.data.map(evaluacion => {
          return {
            ...evaluacion,
            usuarioRegistro: evaluacion.nombreCompletoUsuarioCreacion || 'No especificado',
            grupoAmical: evaluacion.grupoAmical || 'No especificado',
            factorRiesgoMedio: evaluacion.factorRiesgoMedio || 'No especificado'
          } as EvaluacionSocialDTO;
        });

        // Si tenemos un filtro y parece ser una fecha, aplicamos el filtro manual
        if (esFiltroDeFecha && filtroOriginal)
          evaluaciones = this.utilidades.filtrarPorFecha(evaluaciones, filtroOriginal, 'fechaCreacion');

        this.tablaComponent.exportXLSX(evaluaciones);
      },
      error: (error: any) => {
        console.error('Error al obtener evaluaciones sociales:', error);
        this.servicioMensajes.mensajeError('Ocurrió un error al obtener los datos. Por favor, inténtelo nuevamente.');
      }
    });
  }

  manejarEventoPaginacion(eventoPaginacion: PageEvent) {
    this.paginacion.pageSize = eventoPaginacion.pageSize;
    this.paginacion.pageIndex = eventoPaginacion.pageIndex;
    this.obtenerListaSituEconEntoSoci();
  }

  manejarEventoOrdenamiento(evento: Sort) {
    if (evento.direction) {
      this.solicitudPaginacion.sort = evento.active;
      this.solicitudPaginacion.direction = evento.direction;
    } else {
      this.solicitudPaginacion.sort = null;
      this.solicitudPaginacion.direction = null;
    }
    this.obtenerListaSituEconEntoSoci();
  }

  manejarEventoBusqueda(filtro: string) {
    this.solicitudPaginacion = this.solicitudPaginacion || new PaginacionRequest();
    this.solicitudPaginacion.filter = filtro;

    // Si parece un filtro de fecha, mostrar un indicador de carga personalizado
    if (this.utilidades.esPosibleFiltroFecha(filtro)) {
      const dialogoCarga = this.servicioMensajes.mensajeLoading("Filtrando por fecha...");

      // Simulamos un pequeño retraso para mostrar el indicador
      setTimeout(() => {
        this.obtenerListaSituEconEntoSoci();
        dialogoCarga.close();
      }, 300);
    } else {
      this.obtenerListaSituEconEntoSoci();
    }
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

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }
}
