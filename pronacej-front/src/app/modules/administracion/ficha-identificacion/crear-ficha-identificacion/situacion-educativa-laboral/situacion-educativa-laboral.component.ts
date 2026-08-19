import { Component, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatDialog } from '@angular/material/dialog';
import { LaboralDTO } from 'app/core/model/both/LaboralDTO.model';
import { MdRegiSituComponent } from './situacion-crear-editar/md-regi-situ/md-regi-situ.component';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MdRegiLaboComponent } from './situacion-crear-editar/md-regi-labo/md-regi-labo.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { SituacionEducativaLaboralOcioDTO } from 'app/core/model/both/SituacionEducativaLaboralOcioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { SituacionEducativaLaboralService } from 'app/modules/seguridad/services/situacionEducativaLaboral.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { environment } from 'environments/environment';
import { SituacionEducativaLaboralDTO } from 'app/core/model/both/SituacionEducativaLaboralDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PdfService } from 'app/core/services/pdf.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AreasSituacionEducativaLaboralOcioDTO } from 'app/core/model/both/areasSituacionEducativaLaboralOcioDTO.model';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { TabService } from 'app/core/services/tab.service';

@Component({
  selector: 'app-situacion-educativa-laboral',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatExpansionModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './situacion-educativa-laboral.component.html',
  styleUrl: './situacion-educativa-laboral.component.scss'
})
export class SituacionEducativaLaboralComponent implements OnInit {

  // Variables de identificación
  identificadorFichaPrincipal: string;

  // Propiedad para el centro
  centro: JerarquiaDTO;

  // Variables de paginación
  paginaActual = 0;
  opcionesTamanoPagina = [5, 10, 15, 20];
  tamanoPaginaSeleccionado = this.opcionesTamanoPagina[0];
  totalRegistros = 0;

  // Variables para controlar el estado de procesamiento
  estaProcesandoGuardado: boolean = false;

  // Formulario
  formularioAreaSituEducLaboOcio: FormGroup;
  entidadSituacionEducativaLaboral: SituacionEducativaLaboralDTO;

  // Configuración
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_SITUACION_EDUCATIVA_LABORAL;

  // Datos
  listaSituEducLaboOcio: SituacionEducativaLaboralOcioDTO[] = [];
  listaLaboral: LaboralDTO[] = [];
  tablaDataSituEducLaboOcio: MatTableDataSource<SituacionEducativaLaboralOcioDTO>;
  tablaDataLaboral: MatTableDataSource<LaboralDTO>;

  // Catálogos
  listaSituacionesEducativas: CatalogoDTO[] = [];
  listaModalidadesEducativas: CatalogoDTO[] = [];
  listaRendimientosEducativos: CatalogoDTO[] = [];
  listaOcupacionesLaborales: CatalogoDTO[] = [];
  listaModalidadesLaborales: CatalogoDTO[] = [];
  listaRendimientosLaborales: CatalogoDTO[] = [];
  listaModalidadesEstudio: CatalogoDTO[] = [];
  listaNivelesEBR: CatalogoDTO[] = [];
  listaNivelesSuperior: CatalogoDTO[] = [];
  listaNivelesEBA: CatalogoDTO[] = [];

  // Configuración de columnas
  columnasSituacionEducativaLaboralOcio: string[] = [
    'acciones',
    'modalidadEducativa',
    'rendimientoEducativo',
    'modalidadEstudio',
  ];

  columnasLaboral: string[] = [
    'acciones',
    'experienciaLaboral', 
    'ocupacionLaboral',
    'modalidadLaboral',
  ];

  @ViewChild('situEconLaboOcioPag') paginadorSituEducLaboOcio: MatPaginator;
  @ViewChild('laboralPag') paginadorLaboral: MatPaginator;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioMensajes: DialogMensajeService,
    private servicioSituacionEducativaLaboral: SituacionEducativaLaboralService,
    private servicioJerarquia: JerarquiaService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private rutaActiva: ActivatedRoute,
    public dialogoModal: MatDialog,
    public utilidades: FuncionesUtils,
    private servicioTab: TabService,
    private servicioPdf: PdfService,
    private http: HttpClient,
  ) {
    this.construirFormulario();
  }

  ngOnInit(): void {
    this.identificadorFichaPrincipal = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarDatosCatalogo();
    this.cargarCentro();
    this.cargarAreasSituacionEducativaLaboralOcio();
    this.obtenerSituacionesEducativasLaboralesOcio();
    this.obtenerLaborales();
  }

  /**
 * Carga información del centro al que pertenece el usuario
 */
cargarCentro() {
  this.servicioJerarquia
    .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
    .subscribe({
      next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
        if (!respuesta.exito) {
          this.servicioJerarquia.checkError(respuesta);
          return;
        }
        this.centro = respuesta.data;
      },
      error: (error: any) => {
        this.servicioJerarquia.checkError(error);
      }
    });
}

  /**
   * Valida que el campo no contenga solo espacios en blanco
   * @returns Validador personalizado
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      // Si el valor es nulo o undefined, no hay error de espacios
      if (control.value === null || control.value === undefined) {
        return null;
      }
      
      // Si es string, verificar que no sea solo espacios
      if (typeof control.value === 'string') {
        return control.value.trim().length === 0 ? { 'soloEspacios': true } : null;
      }
      
      return null;
    };
  }

  /**
   * Carga los datos de catálogos necesarios para el componente
   */
  cargarDatosCatalogo(): void {
    this.cargarCatalogo('SITUACION_EDUCATIVA', 'listaSituacionesEducativas');
    this.cargarCatalogo('GRADO_INSTRUCCION', 'listaGradosInstruccion');
    this.cargarCatalogo('MODALIDAD_EDUCATIVA', 'listaModalidadesEducativas');
    this.cargarCatalogo('RENDIMIENTO_EDUCATIVO', 'listaRendimientosEducativos');
    this.cargarCatalogo('OCUPACION', 'listaOcupacionesLaborales');
    this.cargarCatalogo('MODALIDAD_LABORAL', 'listaModalidadesLaborales');
    this.cargarCatalogo('RENDIMIENTO_LABORAL', 'listaRendimientosLaborales');
    this.cargarCatalogo('MODALIDAD_ESTUDIO', 'listaModalidadesEstudio');
    this.cargarCatalogo('NIVEL_EBR', 'listaNivelesEBR');
    this.cargarCatalogo('NIVEL_SUPERIOR', 'listaNivelesSuperior');
    this.cargarCatalogo('NIVEL_EBA', 'listaNivelesEBA');
  }

  /**
   * Carga un catálogo específico
   * @param catalogoNombre Nombre del catálogo a cargar
   * @param listaNombre Nombre de la propiedad donde se guardará el catálogo
   */
  private cargarCatalogo(catalogoNombre: string, listaNombre: string): void {
    this.utilidades.obtenerListaCatalogo(catalogoNombre, this.nemonicoMenu).subscribe({
      next: (data) => this[listaNombre] = data,
      error: (error) => console.error(`Error cargando ${listaNombre}:`, error)
    });
  }

  /**
   * Abre el diálogo para agregar una nueva situación educativa/laboral/ocio
   */
  agregarFilaSituEducLaboOcio(): void {
    const referenciaDialogo = this.dialogoModal.open(MdRegiSituComponent, {
      data: {
        uuid_fp: this.identificadorFichaPrincipal,
        listaSituacionesEducativas: this.listaSituacionesEducativas,
        listaModalidadesEducativas: this.listaModalidadesEducativas,
        listaRendimientosEducativos: this.listaRendimientosEducativos,
        listaModalidadesEstudio: this.listaModalidadesEstudio,
        listaNivelesEBR: this.listaNivelesEBR,
        listaNivelesSuperior: this.listaNivelesSuperior,
        listaNivelesEBA: this.listaNivelesEBA
      },
      width: '600px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaSituEducLaboOcio.unshift(resultado);
        this.tablaDataSituEducLaboOcio = new MatTableDataSource(this.listaSituEducLaboOcio);
        this.tablaDataSituEducLaboOcio.paginator = this.paginadorSituEducLaboOcio;
      }
    });
  }

  /**
   * Abre el diálogo para agregar una nueva situación laboral
   */
  agregarFilaLaboral(): void {
    const referenciaDialogo = this.dialogoModal.open(MdRegiLaboComponent, {
      data: {
        uuid_fp: this.identificadorFichaPrincipal,
        listaOcupacionesLaborales: this.listaOcupacionesLaborales,
        listaModalidadesLaborales: this.listaModalidadesLaborales,
        listaRendimientosLaborales: this.listaRendimientosLaborales,
      },
      width: '600px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaLaboral.unshift(resultado);
        this.tablaDataLaboral = new MatTableDataSource(this.listaLaboral);
        this.tablaDataLaboral.paginator = this.paginadorLaboral;
      }
    });
  }

  /**
   * Abre el diálogo para editar una situación educativa/laboral/ocio existente
   * @param fila Fila a editar
   * @param indice Índice de la fila en la lista
   */
  editarFilaSituEducLaboOcio(fila: SituacionEducativaLaboralOcioDTO, indice: number): void {
    const referenciaDialogo = this.dialogoModal.open(MdRegiSituComponent, {
      data: {
        fila: fila,
        uuid_fp: this.identificadorFichaPrincipal,
        listaSituacionesEducativas: this.listaSituacionesEducativas,
        listaModalidadesEducativas: this.listaModalidadesEducativas,
        listaRendimientosEducativos: this.listaRendimientosEducativos,
        listaModalidadesEstudio: this.listaModalidadesEstudio,
        listaNivelesEBR: this.listaNivelesEBR,
        listaNivelesSuperior: this.listaNivelesSuperior,
        listaNivelesEBA: this.listaNivelesEBA
      },
      width: '600px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaSituEducLaboOcio[indice] = resultado;
        this.tablaDataSituEducLaboOcio = new MatTableDataSource(this.listaSituEducLaboOcio);
        this.tablaDataSituEducLaboOcio.paginator = this.paginadorSituEducLaboOcio;
      }
    });
  }

  /**
   * Abre el diálogo para editar una situación laboral existente
   * @param documento Fila a editar
   * @param indice Índice de la fila en la lista
   */
  editarFilaLaboral(documento: any, indice: number): void {
    const referenciaDialogo = this.dialogoModal.open(MdRegiLaboComponent, {
      data: {
        fila: documento,
        uuid_fp: this.identificadorFichaPrincipal,
        listaOcupacionesLaborales: this.listaOcupacionesLaborales,
        listaModalidadesLaborales: this.listaModalidadesLaborales,
        listaRendimientosLaborales: this.listaRendimientosLaborales, 
      },
      width: '600px',
      disableClose: true,
    });

    referenciaDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaLaboral[indice] = resultado;
        this.tablaDataLaboral = new MatTableDataSource(this.listaLaboral);
        this.tablaDataLaboral.paginator = this.paginadorLaboral;
      }
    });
  }

  /**
   * Elimina una situación educativa/laboral/ocio
   * @param indice Índice de la fila a eliminar
   */
  eliminarFilaSituEducLaboOcio(indice: number): void {
    const elementoEliminar = this.listaSituEducLaboOcio[indice];
    
    // Mostrar mensaje de confirmación siempre
    const refConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la situación educativa/laboral? Esta operación es irreversible",
      "¿Deseas continuar?"
    );
  
    refConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si el token es "0", es un registro local
          if (elementoEliminar.tokenIdentificador === "0") {
            this.listaSituEducLaboOcio.splice(indice, 1);
            this.tablaDataSituEducLaboOcio = new MatTableDataSource(this.listaSituEducLaboOcio);
            this.tablaDataSituEducLaboOcio.paginator = this.paginadorSituEducLaboOcio;
            this.servicioMensajes.mensajeExitoso("Registro eliminado", "La situación educativa/laboral se ha eliminado correctamente");
          } 
          // Si no es "0", hay que eliminarlo de la BD
          else {
            const cargador = this.servicioMensajes.mensajeLoading("Eliminando la situación educativa/laboral...");
            
            this.servicioSituacionEducativaLaboral.eliminarSituacionEducativaLaboralOcio(elementoEliminar, this.nemonicoMenu).subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                cargador.close();
                this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
      
                if (respuesta.exito) {
                  this.obtenerSituacionesEducativasLaboralesOcio();
                }
              },
              error: (error: any) => {
                cargador.close();
                this.servicioSituacionEducativaLaboral.checkError(error);
              }
            });
          }
        }
      }
    });
  }

  /**
   * Elimina una situación laboral
   * @param indice Índice de la fila a eliminar
   */
  eliminarFilaLaboral(indice: number): void {
    const elementoEliminar = this.listaLaboral[indice];
    
    // Mostrar mensaje de confirmación siempre
    const refConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la situación laboral? Esta operación es irreversible",
      "¿Deseas continuar?"
    );
  
    refConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si el token es "0", es un registro local
          if (elementoEliminar.tokenIdentificador === "0") {
            this.listaLaboral.splice(indice, 1);
            this.tablaDataLaboral = new MatTableDataSource(this.listaLaboral);
            this.tablaDataLaboral.paginator = this.paginadorLaboral;
            this.servicioMensajes.mensajeExitoso("Registro eliminado", "La situación laboral se ha eliminado correctamente");
          } 
          // Si no es "0", hay que eliminarlo de la BD
          else {
            const cargador = this.servicioMensajes.mensajeLoading("Eliminando la situación laboral...");
            
            this.servicioSituacionEducativaLaboral.eliminarLaboral(elementoEliminar, this.nemonicoMenu).subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                cargador.close();
                this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
      
                if (respuesta.exito) {
                  this.obtenerLaborales(); 
                }
              },
              error: (error: any) => {
                cargador.close();
                this.servicioSituacionEducativaLaboral.checkError(error);
              }
            });
          }
        }
      }
    });
  }

  /**
   * Construye el formulario con validaciones
   */
  construirFormulario(): void {
    this.formularioAreaSituEducLaboOcio = this.constructorFormulario.group({
      actitudEstudios: ['', [this.validarNoEspacios()]],
      desarrolloEducativo: ['', [this.validarNoEspacios()]],
      interesesVocacionales: ['', [this.validarNoEspacios()]], 
      observacionesEducativas: ['', [this.validarNoEspacios()]],
      actitudEmpleo: ['', [this.validarNoEspacios()]],
      capacitacionesEmpleabilidad: ['', [this.validarNoEspacios()]],
      observacionesLaborales: ['', [this.validarNoEspacios()]],
      pasatiempos: ['', [this.validarNoEspacios()]],
      talentos: ['', [this.validarNoEspacios()]],
      participacionGrupal: ['', [this.validarNoEspacios()]],
      usoTiempo: ['', [this.validarNoEspacios()]],
      observacionesOcio: ['', [this.validarNoEspacios()]]
    });
  }

  /**
   * Carga las áreas de situación educativa/laboral/ocio del servidor
   */
  cargarAreasSituacionEducativaLaboralOcio(): void {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;

    this.servicioSituacionEducativaLaboral.obtenerAreasSituacionEducativaLaboralOcio(solicitudPaginacion, this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<AreasSituacionEducativaLaboralOcioDTO>) => {
          if (!respuesta.exito) {
            this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
            return;
          }
          
          if (respuesta.data) {
            this.formularioAreaSituEducLaboOcio.patchValue(respuesta.data);
          }
        },
        error: (error: any) => {
          this.servicioSituacionEducativaLaboral.checkError(error);
        }
      });
  }

  /**
   * Obtiene las situaciones educativas/laborales/ocio del servidor
   */
  obtenerSituacionesEducativasLaboralesOcio(): void {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.tamanoPaginaSeleccionado;
    solicitudPaginacion.page = this.paginaActual;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;
  
    this.servicioSituacionEducativaLaboral.obtenerSituacionesEducativasLaboralesOcioPaginado(solicitudPaginacion, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<SituacionEducativaLaboralOcioDTO>>) => {
        if (!environment.production) {
          console.log(respuesta);
        }
  
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }
        
        this.listaSituEducLaboOcio = respuesta.data.data;
        this.tablaDataSituEducLaboOcio = new MatTableDataSource(this.listaSituEducLaboOcio);
        this.tablaDataSituEducLaboOcio.paginator = this.paginadorSituEducLaboOcio;
      },
      error: (error: any) => {
        console.log(error);
      }
    });
  }
  
  /**
   * Obtiene las situaciones laborales del servidor
   */
  obtenerLaborales(): void {
    let solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = this.tamanoPaginaSeleccionado;
    solicitudPaginacion.page = this.paginaActual;
    solicitudPaginacion.tokenIdentificador = this.identificadorFichaPrincipal;
  
    this.servicioSituacionEducativaLaboral.obtenerLaboralesPaginado(solicitudPaginacion, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<PaginacionResponse<LaboralDTO>>) => {
        if (!environment.production) {
          console.log(respuesta);
        }
  
        if (!respuesta.exito) {
          this.servicioMensajes.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
          return;
        }
  
        this.listaLaboral = respuesta.data.data;
        this.tablaDataLaboral = new MatTableDataSource(this.listaLaboral);
        this.tablaDataLaboral.paginator = this.paginadorLaboral;
      },
      error: (error: any) => {
        console.log(error);
      }
    });
  }
  
  /**
   * Elimina una situación educativa/laboral/ocio del servidor
   * @param situacionEducativaLaboralOcioDTO Situación a eliminar
   */
  eliminarSituacionEducativaLaboralOcio(situacionEducativaLaboralOcioDTO: SituacionEducativaLaboralOcioDTO): void {
    const referenciaConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la situación educativa/laboral? Esta operación es irreversible",
      "¿Deseas continuar?"
    );
  
    referenciaConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const cargador = this.servicioMensajes.mensajeLoading("Eliminando la situación educativa/laboral...");
          
          this.servicioSituacionEducativaLaboral.eliminarSituacionEducativaLaboralOcio(situacionEducativaLaboralOcioDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              cargador.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
  
              if (respuesta.exito) {
                this.obtenerSituacionesEducativasLaboralesOcio();
              }
            },
            error: (error: any) => {
              cargador.close();
              this.servicioSituacionEducativaLaboral.checkError(error);
            }
          });
        }
      }
    });
  }
  
  /**
   * Elimina una situación laboral del servidor
   * @param laboralDTO Situación laboral a eliminar
   */
  eliminarLaboral(laboralDTO: LaboralDTO): void {
    const referenciaConfirmacion = this.servicioMensajes.mensajeConConfirmacion(
      "¿Estás seguro de eliminar la situación laboral? Esta operación es irreversible",
      "¿Deseas continuar?"
    );
  
    referenciaConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const cargador = this.servicioMensajes.mensajeLoading("Eliminando la situación laboral...");
          
          this.servicioSituacionEducativaLaboral.eliminarLaboral(laboralDTO, this.nemonicoMenu).subscribe({
            next: (respuesta: RespuestaPorDefecto<boolean>) => {
              cargador.close();
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
  
              if (respuesta.exito) {
                this.obtenerLaborales(); 
              }
            },
            error: (error: any) => {
              cargador.close();
              this.servicioSituacionEducativaLaboral.checkError(error);
            }
          });
        }
      }
    });
  }
  
  /**
   * Limpia todos los campos del formulario sin afectar a las tablas ni hacer enrutamiento
   */
  cancelarEdicion(): void {
    // Obtener los campos a limpiar
    const camposFormulario = [
      'actitudEstudios',
      'desarrolloEducativo',
      'interesesVocacionales',
      'observacionesEducativas',
      'actitudEmpleo',
      'capacitacionesEmpleabilidad',
      'observacionesLaborales',
      'pasatiempos',
      'talentos',
      'participacionGrupal',
      'usoTiempo',
      'observacionesOcio'
    ];
    
    // Crear un objeto para resetear los valores
    const valoresReset = {};
    
    // Asignar valor vacío a cada campo
    camposFormulario.forEach(campo => {
      valoresReset[campo] = '';
    });
    
    // Usar patchValue para actualizar solo los campos específicos
    this.formularioAreaSituEducLaboOcio.patchValue(valoresReset);
    
    // Restablecer el estado del formulario
    this.formularioAreaSituEducLaboOcio.markAsPristine();
    this.formularioAreaSituEducLaboOcio.markAsUntouched();
    
    // Mostrar mensaje informativo (opcional)
    this.servicioMensajes.mensajeExitoso(
      'Formulario limpiado', 
      'Se han limpiado todos los campos del formulario'
    );
  }
  
  /**
   * Crea o actualiza la situación educativa/laboral/ocio
   * Valida que exista al menos un registro en cada tabla antes de guardar
   */
  guardarRegistro(): void {
      // Si ya está procesando una solicitud, ignorar clicks adicionales
      if (this.estaProcesandoGuardado) {
          return;
      }
      
      // Validar que existan registros en ambas tablas
      if (!this.listaSituEducLaboOcio || this.listaSituEducLaboOcio.length === 0) {
          this.servicioMensajes.mensajeError('Debe agregar al menos un registro en la tabla de Situación Educativa antes de guardar.');
          return;
      }
      
      if (!this.listaLaboral || this.listaLaboral.length === 0) {
          this.servicioMensajes.mensajeError('Debe agregar al menos un registro en la tabla de Situación Laboral antes de guardar.');
          return;
      }
      
      // Marcar todos los campos como tocados para activar validaciones
      Object.keys(this.formularioAreaSituEducLaboOcio.controls).forEach(key => {
          const control = this.formularioAreaSituEducLaboOcio.get(key);
          control.markAsTouched();
          control.markAsDirty();
          control.updateValueAndValidity();
          this.servicioTab.cambiarTab(2);
      });
      
      // Verificar espacios en blanco en todos los campos de texto
      let tieneEspaciosEnBlanco = false;
      
      Object.keys(this.formularioAreaSituEducLaboOcio.controls).forEach(key => {
          const control = this.formularioAreaSituEducLaboOcio.get(key);
          const valor = control.value;
          
          if (typeof valor === 'string' && valor !== null && valor !== undefined) {
              if (valor.trim().length === 0 && valor.length > 0) {
                  tieneEspaciosEnBlanco = true;
                  control.setErrors({ 'soloEspacios': true });
              }
          }
      });
      
      if (this.formularioAreaSituEducLaboOcio.invalid || tieneEspaciosEnBlanco) {
          this.servicioMensajes.mensajeError('Por favor, corrija los campos marcados en rojo antes de guardar.');
          return;
      }
      
      // Establecer bandera de procesamiento
      this.estaProcesandoGuardado = true;

      let situacionEducativaLaboralDTO = new SituacionEducativaLaboralDTO();

      // Crear instancia de AreasSituacionEducativaLaboralOcioDTO
      const areasDTO = new AreasSituacionEducativaLaboralOcioDTO();
      
      // Obtener valores del formulario
      const valoresFormulario = this.formularioAreaSituEducLaboOcio.getRawValue();
      
      // Asignar propiedades a la instancia de AreasSituacionEducativaLaboralOcioDTO
      areasDTO.tokenIdentificadorFichaIdentificacion = this.identificadorFichaPrincipal;
      areasDTO.actitudEstudios = valoresFormulario.actitudEstudios?.trim() || '';
      areasDTO.desarrolloEducativo = valoresFormulario.desarrolloEducativo?.trim() || '';
      areasDTO.interesesVocacionales = valoresFormulario.interesesVocacionales?.trim() || '';
      areasDTO.observacionesEducativas = valoresFormulario.observacionesEducativas?.trim() || '';
      areasDTO.actitudEmpleo = valoresFormulario.actitudEmpleo?.trim() || '';
      areasDTO.capacitacionesEmpleabilidad = valoresFormulario.capacitacionesEmpleabilidad?.trim() || '';
      areasDTO.observacionesLaborales = valoresFormulario.observacionesLaborales?.trim() || '';
      areasDTO.pasatiempos = valoresFormulario.pasatiempos?.trim() || '';
      areasDTO.talentos = valoresFormulario.talentos?.trim() || '';
      areasDTO.participacionGrupal = valoresFormulario.participacionGrupal?.trim() || '';
      areasDTO.usoTiempo = valoresFormulario.usoTiempo?.trim() || '';
      areasDTO.observacionesOcio = valoresFormulario.observacionesOcio?.trim() || '';
      
      // Asignar el objeto DTO y las listas de registros
      situacionEducativaLaboralDTO.areas = areasDTO;
      situacionEducativaLaboralDTO.listaSituEducLaboOcio = this.listaSituEducLaboOcio;
      situacionEducativaLaboralDTO.listaLaboral = this.listaLaboral;
      situacionEducativaLaboralDTO.tokenIdentificadorFichaIdentificacion = this.identificadorFichaPrincipal;
      situacionEducativaLaboralDTO.fechaCreacion = new Date();

      const cargador = this.servicioMensajes.mensajeLoading("Guardando información...");
      this.servicioSituacionEducativaLaboral.crearSituacionEducativaLaboral(situacionEducativaLaboralDTO, this.nemonicoMenu).subscribe({
          next: (respuesta: RespuestaPorDefecto<SituacionEducativaLaboralDTO>) => {
              cargador.close();
              // Restablecer bandera de procesamiento
              this.estaProcesandoGuardado = false;
              
              if (!respuesta.exito) {
                  this.servicioSituacionEducativaLaboral.checkError(respuesta);
                  return;
              }
              
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
              this.obtenerSituacionesEducativasLaboralesOcio();
              this.obtenerLaborales();
          },
          error: (error: any) => {
              cargador.close();
              // Restablecer bandera de procesamiento en caso de error
              this.estaProcesandoGuardado = false;
              this.servicioSituacionEducativaLaboral.checkError(error);
          }
      });
  }

  /**
   * Genera e imprime la ficha de situación educativa laboral ocio en formato PDF
   */
  imprimirFicha(): void {
    const refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de imprimir la situación educativa, laboral y ocio?",
      "¿Desea continuar?"
    );
  
    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          const dialogoCarga = this.servicioMensajes.mensajeLoading("Preparando la impresión de situación educativa, laboral y ocio...");
          
          this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.identificadorFichaPrincipal, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioMensajes.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }
                      
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_SITUACION_EDUCATIVA_LABORAL_OCIO';
                      
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || "Centro de rehabilitación"),
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        "[ACTITUD-ESTUDIOS]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('actitudEstudios')?.value || 'No especificado'),
                        "[DESARROLLO-EDUCATIVO]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('desarrolloEducativo')?.value || 'No especificado'),
                        "[INTERESES-VOCACIONALES]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('interesesVocacionales')?.value || 'No especificado'),
                        "[OBSERVACIONES-EDUCATIVAS]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('observacionesEducativas')?.value || 'No especificado'),
                        "[ACTITUD-EMPLEO]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('actitudEmpleo')?.value || 'No especificado'),
                        "[CAPACITACIONES-EMPLEABILIDAD]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('capacitacionesEmpleabilidad')?.value || 'No especificado'),
                        "[OBSERVACIONES-LABORALES]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('observacionesLaborales')?.value || 'No especificado'),
                        "[PASATIEMPOS]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('pasatiempos')?.value || 'No especificado'),
                        "[TALENTOS]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('talentos')?.value || 'No especificado'),
                        "[PARTICIPACION-GRUPAL]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('participacionGrupal')?.value || 'No especificado'),
                        "[USO-TIEMPO]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('usoTiempo')?.value || 'No especificado'),
                        "[OBSERVACIONES-OCIO]": this.utilidades.escaparHTML(this.formularioAreaSituEducLaboOcio.get('observacionesOcio')?.value || 'No especificado')
                      };
                      
                      let tablaSituacionEducativa = new TablaPlantilla();
                      tablaSituacionEducativa.encabezados = [
                        'Modalidad educativa',
                        'Rendimiento educativo', 
                        'Modalidad estudio'
                      ];
                      
                      let filasSituacionEducativa: any[] = [];
                      if (this.listaSituEducLaboOcio && this.listaSituEducLaboOcio.length > 0) {
                        for (let item of this.listaSituEducLaboOcio) {
                          let fila = {
                            'Modalidad educativa': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(item.tokenIdentificadorModalidadEducativa, this.listaModalidadesEducativas) || "No especificado"),
                            'Rendimiento educativo': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(item.tokenIdentificadorRendimientoEducativo, this.listaRendimientosEducativos) || "No especificado"),
                            'Modalidad estudio': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(item.tokenIdentificadorModalidadEstudio, this.listaModalidadesEstudio) || "No especificado")
                          };
                          filasSituacionEducativa.push(fila);
                        }
                      } else {
                        filasSituacionEducativa.push({
                          'Modalidad educativa': '-',
                          'Rendimiento educativo': 'No hay registros educativos disponibles',
                          'Modalidad estudio': '-'
                        });
                      }
                      tablaSituacionEducativa.filas = filasSituacionEducativa;
                      
                      let tablaLaboral = new TablaPlantilla();
                      tablaLaboral.encabezados = [
                        'Experiencia laboral',
                        'Ocupación laboral',
                        'Modalidad laboral'
                      ];
                      
                      let filasLaboral: any[] = [];
                      if (this.listaLaboral && this.listaLaboral.length > 0) {
                        for (let lab of this.listaLaboral) {
                          let fila = {
                            'Experiencia laboral': this.utilidades.escaparHTML(lab.experienciaLaboral || "No especificado"),
                            'Ocupación laboral': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(lab.tokenIdentificadorOcupacionLaboral, this.listaOcupacionesLaborales) || "No especificado"),
                            'Modalidad laboral': this.utilidades.escaparHTML(this.utilidades.obtenerNombreCatalogoPorToken(lab.tokenIdentificadorModalidadLaboral, this.listaModalidadesLaborales) || "No especificado")
                          };
                          filasLaboral.push(fila);
                        }
                      } else {
                        filasLaboral.push({
                          'Experiencia laboral': '-',
                          'Ocupación laboral': 'No hay registros laborales disponibles',
                          'Modalidad laboral': '-'
                        });
                      }
                      tablaLaboral.filas = filasLaboral;
                      
                      solicitudPdf.variables["[TABLA_SITUACION_EDUCATIVA]"] = JSON.stringify(tablaSituacionEducativa);
                      solicitudPdf.variables["[TABLA_LABORAL]"] = JSON.stringify(tablaLaboral);
                      
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
}