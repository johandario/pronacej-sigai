import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';

// Modelos
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { InformeVisitasDTO } from 'app/core/model/both/informeVisitasDTO.model';
import { InformeVisitasPorPersonaDTO } from 'app/core/model/both/informeVisitasPorPersonaDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { SuspensionVisitasDTO } from 'app/core/model/both/suspensionVisitasDTO.model';
import { SuspensionVisitasPorPersonaDTO } from 'app/core/model/both/suspensionVisitasPorPersonaDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import etiquetasModel from 'app/core/etiquetas.model';

// Servicios
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformeVisitasService } from 'app/modules/seguridad/services/informeVisitas.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { PdfService } from 'app/core/services/pdf.service';
import { HttpClient } from '@angular/common/http';
import { TabService } from 'app/core/services/tab.service';

// Componentes
import { MdRegiInfoComponent } from './md-regi-info/md-regi-info.component';
import { MdRegiSuspComponent } from './md-regi-susp/md-regi-susp.component';

@Component({
  selector: 'app-informe-visitas',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    ReactiveFormsModule
  ],
  templateUrl: './informe-visitas.component.html',
  styleUrl: './informe-visitas.component.scss'
})
export class InformeVisitasComponent {
  // Variables de identificación
  uuid_fp: string;

  // Variables de entidad
  formularioSuspensionVisitas: FormGroup;
  tituloPantalla = 'Autorización de visitantes';
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_INFORME_VISITAS;

  // Variable de control para evitar envíos duplicados
  estaProcesandoGuardado: boolean = false;

  // Variables de catálogo
  private listaInformesVisitasOriginal: InformeVisitasDTO[] = [];
  private listaSuspensionVisitasOriginal: SuspensionVisitasDTO[] = [];
  listaCausalesSuspension: CatalogoDTO[] = [];
  listaTiposAutorizacion: CatalogoDTO[] = [];
  listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];

  // Variables de datos
  fuenteDatosInformeVisitas = new MatTableDataSource<any>([]);
  fuenteDatosSuspensionVisitas = new MatTableDataSource<any>([]);
  listaInformesVisitas: InformeVisitasDTO[] = [];
  listaSuspensionVisitas: SuspensionVisitasDTO[] = [];

  // Configuración de columnas de autorización visitantes
  columnasInformeVisitas: string[] = [
    'acciones', 
    'personaRelacionada',
    'tipoAutorizacion',
    'fechaInicio', 
    'fechaFin',
    'causalesRestriccion',
    'observaciones'
  ];

  titulosColumnasInforme = {
    'personaRelacionada': 'Persona relacionada',
    'tipoAutorizacion': 'Tipo',
    'fechaInicio': 'Fecha inicio',
    'fechaFin': 'Fecha fin',
    'causalesRestriccion': 'Causales de restricción',
    'observaciones': 'Observaciones'
  };

  // Configuración de columnas de suspensión visitas
  columnasSuspensionVisitas: string[] = [
    'acciones',
    'causalesSuspension',
    'oficioDeSancion', // Nueva columna
    'fechaInicio',
    'fechaFin',
    'observaciones'
  ];

  titulosColumnasSuspension = {
    'causalesSuspension': 'Cometimiento de una infracción',
    'oficioDeSancion': 'N° oficio de sanción',
    'fechaInicio': 'Fecha inicio',
    'fechaFin': 'Fecha fin',
    'observaciones': 'Observaciones'
  };

  @ViewChild('informeVisitasPag') paginadorInformeVisitas!: MatPaginator;
  @ViewChild('suspensionVisitasPag') paginadorSuspensionVisitas!: MatPaginator;

  constructor(
    private constructorFormulario: FormBuilder,
    private router: Router,
    private ruta: ActivatedRoute,
    private servicioDialogoMensaje: DialogMensajeService,
    private servicioInformeVisitas: InformeVisitasService,
    private servicioDatosFamiliares: DatosFamiliaresService,
    private http: HttpClient,
    private utilidades: FuncionesUtils,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioPdf: PdfService,
    private servicioTab: TabService,
    public dialogoMaterial: MatDialog,
  ) {
    this.construirFormulario();
    // Hacer Array disponible en el template
    (window as any).Array = Array;
  }

  ngOnInit() {
    this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
    this.obtenerPersonasRelacionadas();
    this.cargarDatosCatalogo();
    this.obtenerInformesVisitas();
    this.obtenerSuspensionVisitas();
  }

  construirFormulario() {
    this.formularioSuspensionVisitas = this.constructorFormulario.group({
      causalesSuspension: ['0']
    });
  }

  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('CAUSALES_SUSPENSION', this.nemonicoMenu).subscribe({
      next: (datos) => this.listaCausalesSuspension = datos,
      error: (error) => console.error('Error al cargar cometimientos de un delito:', error)
    });

    this.utilidades.obtenerListaCatalogo('TIPOS_AUTORIZACION', this.nemonicoMenu).subscribe({
      next: (datos) => this.listaTiposAutorizacion = datos,
      error: (error) => console.error('Error al cargar tipos de autorización:', error)
    });
  }

  obtenerPersonasRelacionadas() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    this.servicioDatosFamiliares.obtenerPersonasRelacionadas(solicitudPaginacion,this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
          if (!respuesta.exito) {
            this.servicioDialogoMensaje.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }

          this.listaPersonasRelacionadas = respuesta.data.data;
        }
      });
  }

  /**
   * Obtiene el nombre completo de una persona relacionada
   * @param tokenIdentificador Identificador de la persona relacionada
   * @returns Nombre completo en formato similar al de OrientacionConsejeriaFamiliarComponent
   */
  obtenerNombrePersonaRelacionada(tokenIdentificador: string): string {
    const persona = this.listaPersonasRelacionadas.find(p => p.tokenIdentificador === tokenIdentificador);
    
    if (!persona) {
      return 'No encontrado';
    }
    
    // Si tiene el campo nombres que ya contiene el nombre completo, usarlo
    if (persona.nombres) {
      return persona.nombres;
    }
    
    // Si no tiene nombres pero tiene los componentes individuales
    const partes: string[] = [];
    
    if (persona.primerNombre) partes.push(persona.primerNombre);
    if (persona.segundoNombre) partes.push(persona.segundoNombre);
    if (persona.apellidoPaterno) partes.push(persona.apellidoPaterno);
    if (persona.apellidoMaterno) partes.push(persona.apellidoMaterno);
    
    // Si aún no hay partes, usar los campos alternativos
    if (partes.length === 0) {
      if (persona.primerApellido) partes.push(persona.primerApellido);
      if (persona.segundoApellido) partes.push(persona.segundoApellido);
    }
    
    return partes.length > 0 ? partes.join(' ') : 'No especificado';
  }

  /**
   * Obtiene el texto formateado para mostrar los cometimientos de infracciones seleccionados
   * @param elemento Elemento de suspensión de visitas
   * @returns Texto formateado con los cometimientos seleccionados
   */
  obtenerCausalesSuspensionTexto(elemento: SuspensionVisitasDTO): string {
    if (!elemento.cometimientosInfraccion || elemento.cometimientosInfraccion.length === 0) {
      return 'No especificado';
    }
    
    // Filtrar solo los cometimientos seleccionados
    const cometimientosSeleccionados = elemento.cometimientosInfraccion
      .filter(cometimiento => cometimiento.seleccionado);
    
    if (cometimientosSeleccionados.length === 0) {
      return 'No especificado';
    }
    
    // Obtener los nombres de los catálogos para los cometimientos seleccionados
    return cometimientosSeleccionados
      .map(cometimiento => this.utilidades.obtenerNombreCatalogoPorToken(
        cometimiento.tokenIdentificadorCausalSuspension, 
        this.listaCausalesSuspension
      ))
      .join(', ');
  }

  /**
   * Verifica si hay cambios en los informes de visitas usando FuncionesUtils
   */
  private hayInformesModificados(): boolean {
    const camposComparar: (keyof InformeVisitasDTO)[] = [
      'tokenIdentificadorPersonaRelacionada',
      'tokenIdentificadorTipoAutorizacion', 
      'fechaInicio',
      'fechaFin',
      'causalesRestriccion',
      'observaciones'
    ];
    
    return this.utilidades.hayArrayModificado(
      this.fuenteDatosInformeVisitas.data,
      this.listaInformesVisitasOriginal,
      'tokenIdentificador',
      camposComparar
    );
  }

  /**
   * Verifica si hay cambios en las suspensiones de visitas usando FuncionesUtils
   */
  private haySuspensionesModificadas(): boolean {
    const datosActuales = this.fuenteDatosSuspensionVisitas.data;
    
    // Si las longitudes son diferentes, hay cambios
    if (datosActuales.length !== this.listaSuspensionVisitasOriginal.length) {
      return true;
    }
    
    // Verificar si hay registros nuevos (tokenIdentificador === "0")
    const hayRegistrosNuevos = datosActuales.some(item => item.tokenIdentificador === "0");
    if (hayRegistrosNuevos) {
      return true;
    }
    
    // Verificar si hay cambios en registros existentes
    for (let i = 0; i < datosActuales.length; i++) {
      const actual = datosActuales[i];
      const original = this.listaSuspensionVisitasOriginal.find(orig => 
        orig.tokenIdentificador === actual.tokenIdentificador
      );
      
      if (!original) {
        return true; // Registro no encontrado en original = nuevo
      }
      
      // Comparar campos relevantes usando FuncionesUtils
      if (this.utilidades.compararFechas(actual.fechaInicio, original.fechaInicio) ||
          this.utilidades.compararFechas(actual.fechaFin, original.fechaFin) ||
          (actual.oficioDeSancion || '') !== (original.oficioDeSancion || '') ||
          (actual.observaciones || '') !== (original.observaciones || '') ||
          this.compararCometimientos(actual.cometimientosInfraccion, original.cometimientosInfraccion)) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Compara dos listas de cometimientos para detectar cambios específicos
   */
  private compararCometimientos(actual: any[], original: any[]): boolean {
    return this.utilidades.compararArrays(
      actual || [], 
      original || [], 
      (cometActual, cometOriginal) => 
        cometActual.tokenIdentificadorCausalSuspension === cometOriginal.tokenIdentificadorCausalSuspension &&
        cometActual.seleccionado === cometOriginal.seleccionado
    );
  }

  obtenerInformesVisitas() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    this.servicioInformeVisitas.obtenerInformesVisitasPaginado(solicitudPaginacion, this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<InformeVisitasDTO>>) => {
          if (!respuesta.exito) {
            this.servicioDialogoMensaje.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }

          this.listaInformesVisitas = respuesta.data.data;
          // Guardar copia del estado original
          this.listaInformesVisitasOriginal = JSON.parse(JSON.stringify(respuesta.data.data));
          
          this.fuenteDatosInformeVisitas = new MatTableDataSource(this.listaInformesVisitas);
          this.fuenteDatosInformeVisitas.paginator = this.paginadorInformeVisitas;
        },
        error: (error: any) => {
          this.servicioInformeVisitas.checkError(error);
        }
      });
  }

  obtenerSuspensionVisitas() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 100;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.uuid_fp;

    this.servicioInformeVisitas.obtenerSuspensionVisitasPaginado(solicitudPaginacion, this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SuspensionVisitasDTO>>) => {
          if (!respuesta.exito) {
            this.servicioDialogoMensaje.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }

          this.listaSuspensionVisitas = respuesta.data.data;
          // Guardar copia del estado original
          this.listaSuspensionVisitasOriginal = JSON.parse(JSON.stringify(respuesta.data.data));
          
          this.fuenteDatosSuspensionVisitas = new MatTableDataSource(this.listaSuspensionVisitas);
          this.fuenteDatosSuspensionVisitas.paginator = this.paginadorSuspensionVisitas;
        },
        error: (error: any) => {
          this.servicioInformeVisitas.checkError(error);
        }
      });
  }

  agregarFilaInforme() {
    const referenciaDialogo = this.dialogoMaterial.open(MdRegiInfoComponent, {
      data: {
        listaPersonasRelacionadas: this.listaPersonasRelacionadas,
        listaTiposAutorizacion: this.listaTiposAutorizacion,
      },
      width: '800px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.fuenteDatosInformeVisitas.data.unshift(resultado);
        this.fuenteDatosInformeVisitas = new MatTableDataSource(this.fuenteDatosInformeVisitas.data);
        this.fuenteDatosInformeVisitas.paginator = this.paginadorInformeVisitas;
      }
    });
  }

  visualizarFilaInforme(fila: InformeVisitasDTO) {
    const referenciaDialogo = this.dialogoMaterial.open(MdRegiInfoComponent, {
      data: {
        fila: { ...fila, esVisualizacion: true },
        listaPersonasRelacionadas: this.listaPersonasRelacionadas,
        listaTiposAutorizacion: this.listaTiposAutorizacion,
        esVisualizacion: true
      },
      width: '800px',
      disableClose: true,
    });
  }

  editarFilaInforme(fila: InformeVisitasDTO, indice: number) {
    const referenciaDialogo = this.dialogoMaterial.open(MdRegiInfoComponent, {
      data: {
        fila: fila,
        listaPersonasRelacionadas: this.listaPersonasRelacionadas,
        listaTiposAutorizacion: this.listaTiposAutorizacion
      },
      width: '800px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.fuenteDatosInformeVisitas.data[indice] = resultado;
        this.fuenteDatosInformeVisitas = new MatTableDataSource(this.fuenteDatosInformeVisitas.data);
        this.fuenteDatosInformeVisitas.paginator = this.paginadorInformeVisitas;
      }
    });
  }

  eliminarFilaInforme(indice: number) {
    const elementoEliminar = this.fuenteDatosInformeVisitas.data[indice];
    
    // Mostrar mensaje de confirmación en todos los casos
    const refDialogo = this.servicioDialogoMensaje.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta autorización de visitante? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si es un registro nuevo (no está en BD todavía)
          if (elementoEliminar.tokenIdentificador === "0") {
            this.fuenteDatosInformeVisitas.data.splice(indice, 1);
            this.fuenteDatosInformeVisitas = new MatTableDataSource(this.fuenteDatosInformeVisitas.data);
            this.fuenteDatosInformeVisitas.paginator = this.paginadorInformeVisitas;
            this.servicioDialogoMensaje.mensajeExitoso("Registro eliminado", "El registro se ha eliminado correctamente");
          } 
          // Si es un registro existente
          else {
            const dialogoCarga = this.servicioDialogoMensaje.mensajeLoading("Eliminando autorización de visitante...");
            
            this.servicioInformeVisitas.eliminarInformeVisitas(elementoEliminar, this.nemonicoMenu).subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                dialogoCarga.close();
                this.servicioDialogoMensaje.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

                if (respuesta.exito) {
                  this.obtenerInformesVisitas();
                }
              },
              error: (error: any) => {
                dialogoCarga.close();
                this.servicioInformeVisitas.checkError(error);
              }
            });
          }
        }
      }
    });
  }

  agregarFilaSuspension() {
    const referenciaDialogo = this.dialogoMaterial.open(MdRegiSuspComponent, {
      data: {
        listaCausalesSuspension: this.listaCausalesSuspension
      },
      width: '800px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        // Asegúrate de que los cometimientos estén inicializados si vienen vacíos
        if (!resultado.cometimientosInfraccion) {
          resultado.cometimientosInfraccion = [];
        }
        
        this.fuenteDatosSuspensionVisitas.data.unshift(resultado);
        this.fuenteDatosSuspensionVisitas = new MatTableDataSource(this.fuenteDatosSuspensionVisitas.data);
        this.fuenteDatosSuspensionVisitas.paginator = this.paginadorSuspensionVisitas;
      }
    });
  }

  visualizarFilaSuspension(fila: SuspensionVisitasDTO) {
    const referenciaDialogo = this.dialogoMaterial.open(MdRegiSuspComponent, {
      data: {
        fila: { ...fila, esVisualizacion: true },
        listaCausalesSuspension: this.listaCausalesSuspension,
        esVisualizacion: true
      },
      width: '800px',
      disableClose: true,
    });
  }

  editarFilaSuspension(fila: SuspensionVisitasDTO, indice: number) {
    const referenciaDialogo = this.dialogoMaterial.open(MdRegiSuspComponent, {
      data: {
        fila: fila,
        listaCausalesSuspension: this.listaCausalesSuspension
      },
      width: '800px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.fuenteDatosSuspensionVisitas.data[indice] = resultado;
        this.fuenteDatosSuspensionVisitas = new MatTableDataSource(this.fuenteDatosSuspensionVisitas.data);
        this.fuenteDatosSuspensionVisitas.paginator = this.paginadorSuspensionVisitas;
      }
    });
  }

  eliminarFilaSuspension(indice: number) {
    const elementoEliminar = this.fuenteDatosSuspensionVisitas.data[indice];
    
    // Mostrar mensaje de confirmación en todos los casos
    const refDialogo = this.servicioDialogoMensaje.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta suspensión de visita? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si es un registro nuevo (no está en BD todavía)
          if (elementoEliminar.tokenIdentificador === "0") {
            this.fuenteDatosSuspensionVisitas.data.splice(indice, 1);
            this.fuenteDatosSuspensionVisitas = new MatTableDataSource(this.fuenteDatosSuspensionVisitas.data);
            this.fuenteDatosSuspensionVisitas.paginator = this.paginadorSuspensionVisitas;
            this.servicioDialogoMensaje.mensajeExitoso("Registro eliminado", "El registro se ha eliminado correctamente");
          } 
          // Si es un registro existente
          else {
            const dialogoCarga = this.servicioDialogoMensaje.mensajeLoading("Eliminando suspensión de visita...");
            
            this.servicioInformeVisitas.eliminarSuspensionVisitas(elementoEliminar, this.nemonicoMenu).subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                dialogoCarga.close();
                this.servicioDialogoMensaje.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

                if (respuesta.exito) {
                  this.obtenerSuspensionVisitas();
                }
              },
              error: (error: any) => {
                dialogoCarga.close();
                this.servicioInformeVisitas.checkError(error);
              }
            });
          }
        }
      }
    });
  }

  crearActualizar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }
    
    // Verificar qué tablas tienen cambios
    const hayInformesModificados = this.hayInformesModificados();
    const haySuspensionesModificadas = this.haySuspensionesModificadas();
    
    // Si no hay cambios en ninguna tabla
    if (!hayInformesModificados && !haySuspensionesModificadas) {
      this.servicioDialogoMensaje.mensajeAdvertencia("Sin cambios", "No se detectaron cambios para guardar");
      return;
    }
    
    // Establecer bandera de procesamiento
    this.estaProcesandoGuardado = true;
    
    // Contador para manejar múltiples operaciones asíncronas
    let operacionesPendientes = 0;
    let operacionesCompletadas = 0;
    
    // Función para verificar si todas las operaciones terminaron
    const verificarFinalizacion = () => {
      operacionesCompletadas++;
      if (operacionesCompletadas >= operacionesPendientes) {
        this.estaProcesandoGuardado = false;
        this.servicioTab.cambiarTab(3);
      }
    };
    
    // Guardar informes solo si hay cambios
    if (hayInformesModificados) {
      operacionesPendientes++;
      this.guardarInformesVisitas(() => verificarFinalizacion());
    }
    
    // Guardar suspensiones solo si hay cambios
    if (haySuspensionesModificadas) {
      operacionesPendientes++;
      this.guardarSuspensionVisitas(() => verificarFinalizacion());
    }
    
    // Si no se incrementó el contador, no había cambios
    if (operacionesPendientes === 0) {
      this.estaProcesandoGuardado = false;
    }
  }

  guardarInformesVisitas(callback?: () => void) {
    const datosInformeVisitas = new InformeVisitasPorPersonaDTO();
    datosInformeVisitas.listaInformeVisitas = this.fuenteDatosInformeVisitas.data;
    datosInformeVisitas.tokenIdentificadorFichaPrincipal = this.uuid_fp;

    this.servicioInformeVisitas.crearInformeVisitas(datosInformeVisitas, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<InformeVisitasDTO>) => {
        if (!respuesta.exito) {
          this.servicioInformeVisitas.checkError(respuesta);
          this.estaProcesandoGuardado = false;
          return;
        }
        
        // Mostrar mensaje específico para informes
        this.servicioDialogoMensaje.mensajeExitoso(
          "Autorización de visitantes", 
          respuesta.mensaje
        );
        
        this.obtenerInformesVisitas();
        
        // Ejecutar callback si se proporciona
        if (callback) {
          callback();
        }
      },
      error: (error: any) => {
        this.servicioInformeVisitas.checkError(error);
        this.estaProcesandoGuardado = false;
        if (callback) {
          callback();
        }
      }
    });
  }

  guardarSuspensionVisitas(callback?: () => void) {
    const datosSuspensionVisitas = new SuspensionVisitasPorPersonaDTO();
    datosSuspensionVisitas.listaSuspensionVisitas = this.fuenteDatosSuspensionVisitas.data;
    datosSuspensionVisitas.tokenIdentificadorFichaPrincipal = this.uuid_fp;

    this.servicioInformeVisitas.crearSuspensionVisitas(datosSuspensionVisitas, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<SuspensionVisitasDTO>) => {
        if (!respuesta.exito) {
          this.servicioInformeVisitas.checkError(respuesta);
          this.estaProcesandoGuardado = false;
          return;
        }
        
        // Mostrar mensaje específico para suspensiones
        this.servicioDialogoMensaje.mensajeExitoso(
          "Suspensión de visitas", 
          respuesta.mensaje
        );
        
        this.obtenerSuspensionVisitas();
        
        // Ejecutar callback si se proporciona
        if (callback) {
          callback();
        }
      },
      error: (error: any) => {
        this.servicioInformeVisitas.checkError(error);
        this.estaProcesandoGuardado = false;
        if (callback) {
          callback();
        }
      }
    });
  }

  imprimirFicha() {
    // 1. Mostrar diálogo de confirmación
    const refDialogo = this.servicioDialogoMensaje.mensajeConConfirmacion(
      "¿Está seguro de imprimir el informe de visitas?",
      "¿Desea continuar?"
    );
  
    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          // 2. Mostrar diálogo de carga
          const dialogoCarga = this.servicioDialogoMensaje.mensajeLoading("Preparando la impresión del informe de visitas...");
          
          // 3. Cargar la imagen como base64
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // 4. Obtener datos de la ficha de identificación
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioDialogoMensaje.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }
                      
                      // 5. Preparar datos del adolescente
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      
                      // 6. Generar tabla de suspensión de visitas
                      let tablaSuspensionVisitas = new TablaPlantilla();
                      tablaSuspensionVisitas.encabezados = [
                        'Cometimiento de una infracción', 
                        'N° oficio de sanción', 
                        'Fecha inicio', 
                        'Fecha fin', 
                        'Observaciones'
                      ];
  
                      let filasSuspension: any[] = [];
                      if (this.fuenteDatosSuspensionVisitas.data && this.fuenteDatosSuspensionVisitas.data.length > 0) {
                        for (let suspension of this.fuenteDatosSuspensionVisitas.data) {
                          let fila = {
                            'Cometimiento de una infracción': this.utilidades.escaparHTML(this.obtenerCausalesSuspensionTexto(suspension)),
                            'N° oficio de sanción': this.utilidades.escaparHTML(suspension.oficioDeSancion || 'No especificado'),
                            'Fecha inicio': this.utilidades.escaparHTML(this.utilidades.formatearFecha(suspension.fechaInicio)),
                            'Fecha fin': this.utilidades.escaparHTML(this.utilidades.formatearFecha(suspension.fechaFin)),
                            'Observaciones': this.utilidades.escaparHTML(suspension.observaciones || '')
                          };
                          filasSuspension.push(fila);
                        }
                      }
                      tablaSuspensionVisitas.filas = filasSuspension;
  
                      // 7. Generar tabla de informes de visitas
                      let tablaInformeVisitas = new TablaPlantilla();
                      tablaInformeVisitas.encabezados = ['Persona relacionada', 'Tipo', 'Fecha inicio', 'Fecha fin', 'Causales de restricción', 'Observaciones'];
                      
                      let filasInforme: any[] = [];
                      if (this.fuenteDatosInformeVisitas.data && this.fuenteDatosInformeVisitas.data.length > 0) {
                        for (let informe of this.fuenteDatosInformeVisitas.data) {
                          let fila = {
                            'Persona relacionada': this.utilidades.escaparHTML(this.obtenerNombrePersonaRelacionada(informe.tokenIdentificadorPersonaRelacionada)),
                            'Tipo': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(informe.tokenIdentificadorTipoAutorizacion, this.listaTiposAutorizacion)),
                            'Fecha inicio': this.utilidades.escaparHTML(this.utilidades.formatearFecha(informe.fechaInicio)),
                            'Fecha fin': this.utilidades.escaparHTML(this.utilidades.formatearFecha(informe.fechaFin)),
                            'Causales de restricción': this.utilidades.escaparHTML(informe.causalesRestriccion || 'No presenta'),
                            'Observaciones': this.utilidades.escaparHTML(informe.observaciones || '')
                          };
                          filasInforme.push(fila);
                        }
                      }
                      tablaInformeVisitas.filas = filasInforme;
                      
                      // 8. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_INFORME_VISITAS';
                      
                      // 9. Incluir las variables para el PDF - APLICANDO escaparHTML a todos los valores
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA_REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA_REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(fichaIdentificacion.centroIngreso || ''),
                        "[NOMBRES_APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR_FECHA_NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[TABLA_SUSPENSION_VISITAS]": JSON.stringify(tablaSuspensionVisitas),
                        "[TABLA_INFORMES_VISITAS]": JSON.stringify(tablaInformeVisitas)
                      };
                      
                      // 10. Llamar al servicio para generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioDialogoMensaje.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }
                          
                          // 11. Abrir el PDF en una nueva pestaña
                          const url = window.URL.createObjectURL(this.utilidades.getPdfBlob(respuesta.data));
                          window.open(url);
                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al generar PDF:', error);
                          this.servicioDialogoMensaje.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                        }
                      });
                    },
                    error: (error: any) => {
                      dialogoCarga.close();
                      console.error('Error al obtener ficha:', error);
                      this.servicioDialogoMensaje.mensajeError('Error al obtener la ficha de identificación');
                    }
                  });
              },
              error: (error) => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.servicioDialogoMensaje.mensajeError('Error al cargar la imagen del logo');
              }
            });
        }
      }
    });
  }
}