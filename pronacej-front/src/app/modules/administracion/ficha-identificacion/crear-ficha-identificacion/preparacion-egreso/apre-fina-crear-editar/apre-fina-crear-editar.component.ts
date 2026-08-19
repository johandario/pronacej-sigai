import { CommonModule, Location } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { ApreciacionFinalTratamientoDTO } from 'app/core/model/both/apreciacionFinalTratamientoDTO.model';
import { SituacionActualAdolescenteDTO } from 'app/core/model/both/situacionActualAdolescenteDTO.model';
import { FactoresPresentesDTO } from 'app/core/model/both/factoresPresentesDTO.model';
import { ApreciacionFinalTratamientoService } from 'app/modules/seguridad/services/apreciacionFinalTratamiento.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { MdRegiSituComponent } from './md-regi-situ/md-regi-situ.component';
import { MdRegiFactComponent } from './md-regi-fact/md-regi-fact.component';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-apre-fina-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatExpansionModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatIconModule,
  ],
  templateUrl: './apre-fina-crear-editar.component.html',
  styleUrl: './apre-fina-crear-editar.component.scss'
})
export class ApreFinaCrearEditarComponent implements OnInit {
  // Variables de identificación
  uuidFichaIdentificacion: string;
  uuidApreciacionFinal: string;

  // Variables de entidad
  apreciacionFinalDTO: ApreciacionFinalTratamientoDTO;
  tituloPantalla = "apreciación final del tratamiento";

  // Variables de estado
  esEdicion = false;
  esVisualizacion = false;
  estaGuardando = false;
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_APRECIACION_FINAL;

  // Variables de datos para situaciones
  listaSituaciones: SituacionActualAdolescenteDTO[] = [];
  fuenteDatosSituaciones: MatTableDataSource<SituacionActualAdolescenteDTO>;
  columnasSituaciones = ['numero', 'acciones', 'tipoArea', 'tipoSituacion', 'descripcion', 'observacion'];

  // Variables de datos para factores
  listaFactores: FactoresPresentesDTO[] = [];
  fuenteDatosFactores: MatTableDataSource<FactoresPresentesDTO>;
  columnasFactores = ['numero', 'acciones', 'factoresProtectores', 'factoresRiesgo'];

  // Variables de catálogos
  listaTiposArea: CatalogoDTO[] = [];
  listaTiposSituacion: CatalogoDTO[] = [];

  // Referencias a paginadores
  @ViewChild('situacionesPag') paginadorSituaciones: MatPaginator;
  @ViewChild('factoresPag') paginadorFactores: MatPaginator;

  constructor(
    private servicioMensajes: DialogMensajeService,
    private servicioApreciacionFinal: ApreciacionFinalTratamientoService,
    private dialogMensajeService: DialogMensajeService,
    private enrutador: Router,
    private ruta: ActivatedRoute,
    private ubicacion: Location,
    public dialogoMaterial: MatDialog,
    public utilidades: FuncionesUtils,
  ) { }

  /**
   * Inicializa el componente cargando datos necesarios
   */
  ngOnInit(): void {
    this.uuidFichaIdentificacion = this.ruta.snapshot.params['uuid_fp'];
    this.apreciacionFinalDTO = history.state.apreciacionFinalDTO;
    this.cargarDatosCatalogo();

    if (this.apreciacionFinalDTO) {
      this.esVisualizacion = this.apreciacionFinalDTO.esVisualizacion;
      this.empezarEdicion(this.apreciacionFinalDTO);
    }
  }

  /**
   * Carga los catálogos necesarios para el formulario
   */
  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('TIPO_AREA_SITUACION', this.nemonicoMenu).subscribe({
      next: (datos) => this.listaTiposArea = datos,
      error: (error) => console.error('Error al cargar los tipos de área:', error)
    });

    this.utilidades.obtenerListaCatalogo('TIPO_SITUACION_ADOLESCENTE', this.nemonicoMenu).subscribe({
      next: (datos) => this.listaTiposSituacion = datos,
      error: (error) => console.error('Error al cargar los tipos de situación:', error)
    });
  }

  /**
   * Abre diálogo para agregar una nueva situación
   */
  agregarSituacion() {
    const refDialogo = this.dialogoMaterial.open(MdRegiSituComponent, {
      data: {
        listaTiposArea: this.listaTiposArea,
        listaTiposSituacion: this.listaTiposSituacion
      },
      width: '600px',
      disableClose: true,
    });

    refDialogo.afterClosed().subscribe(async (resultado: SituacionActualAdolescenteDTO) => {
      if (resultado) {
        this.listaSituaciones.unshift(resultado);
        this.actualizarFuenteDatosSituaciones();
      }
    });
  }

  /**
   * Abre diálogo para editar una situación existente
   * @param situacion Datos de la situación a editar
   * @param indice Posición en la lista de situaciones
   */
  editarSituacion(situacion: SituacionActualAdolescenteDTO, indice: number) {
    const refDialogo = this.dialogoMaterial.open(MdRegiSituComponent, {
      data: {
        fila: situacion,
        listaTiposArea: this.listaTiposArea,
        listaTiposSituacion: this.listaTiposSituacion
      },
      width: '600px',
      disableClose: true,
    });

    refDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaSituaciones[indice] = resultado;
        this.actualizarFuenteDatosSituaciones();
      }
    });
  }

  /**
   * Elimina una situación de la lista previa confirmación
   * @param indice Posición en la lista de situaciones
   */
  eliminarSituacion(indice: number) {
    let refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta situación? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          this.listaSituaciones.splice(indice, 1);
          this.actualizarFuenteDatosSituaciones();
        }
      }
    });
  }

  /**
   * Abre diálogo para agregar un nuevo factor
   */
  agregarFactor() {
    const refDialogo = this.dialogoMaterial.open(MdRegiFactComponent, {
      data: {},
      width: '600px',
      disableClose: true,
    });

    refDialogo.afterClosed().subscribe(async (resultado: FactoresPresentesDTO) => {
      if (resultado) {
        this.listaFactores.unshift(resultado);
        this.actualizarFuenteDatosFactores();
      }
    });
  }

  /**
   * Abre diálogo para editar un factor existente
   * @param factor Datos del factor a editar
   * @param indice Posición en la lista de factores
   */
  editarFactor(factor: FactoresPresentesDTO, indice: number) {
    const refDialogo = this.dialogoMaterial.open(MdRegiFactComponent, {
      data: { fila: factor },
      width: '600px',
      disableClose: true,
    });

    refDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        this.listaFactores[indice] = resultado;
        this.actualizarFuenteDatosFactores();
      }
    });
  }

  /**
   * Elimina un factor de la lista previa confirmación
   * @param indice Posición en la lista de factores
   */
  eliminarFactor(indice: number) {
    let refDialogo = this.servicioMensajes.mensajeConConfirmacion(
      "¿Está seguro de eliminar este factor? Esta operación es irreversible",
      "¿Desea continuar?"
    );

    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta == "confirmed") {
          this.listaFactores.splice(indice, 1);
          this.actualizarFuenteDatosFactores();
        }
      }
    });
  }

  /**
   * Actualiza la fuente de datos de la tabla de situaciones
   */
  actualizarFuenteDatosSituaciones() {
    this.fuenteDatosSituaciones = new MatTableDataSource(this.listaSituaciones);
    this.fuenteDatosSituaciones.paginator = this.paginadorSituaciones;
  }

  /**
   * Actualiza la fuente de datos de la tabla de factores
   */
  actualizarFuenteDatosFactores() {
    this.fuenteDatosFactores = new MatTableDataSource(this.listaFactores);
    this.fuenteDatosFactores.paginator = this.paginadorFactores;
  }

  /**
   * Inicia el modo edición con los datos proporcionados
   * @param apreciacionFinalEditar Datos de apreciación final para editar
   */
  empezarEdicion(apreciacionFinalEditar: ApreciacionFinalTratamientoDTO) {
    this.esEdicion = true;
    this.apreciacionFinalDTO = apreciacionFinalEditar;
    this.uuidApreciacionFinal = apreciacionFinalEditar.tokenIdentificador;

    // Cargar listas desde la entidad
    this.listaSituaciones = apreciacionFinalEditar.listaSituaciones || [];
    this.listaFactores = apreciacionFinalEditar.listaFactoresPresentes || [];

    // Actualizar tablas de datos
    this.actualizarFuenteDatosSituaciones();
    this.actualizarFuenteDatosFactores();
  }

  /**
   * Crea o actualiza la apreciación final según datos capturados
   */
  crearActualizar() {
      // Si ya está procesando una solicitud, ignorar clicks adicionales
      if (this.estaGuardando) {
          return;
      }
      
      // Validar que haya al menos una situación
      if (this.listaSituaciones.length === 0) {
          this.servicioMensajes.mensajeErrorConTitulo(
              'Error de validación',
              'Debe agregar al menos una situación'
          );
          return;
      }

      // Validar que haya al menos un factor
      if (this.listaFactores.length === 0) {
          this.servicioMensajes.mensajeErrorConTitulo(
              'Error de validación',
              'Debe agregar al menos un factor'
          );
          return;
      }

      // Establecer bandera de procesamiento
      this.estaGuardando = true;

      let apreciacionFinal = new ApreciacionFinalTratamientoDTO();

      // Asignar propiedades
      Object.assign(apreciacionFinal, {
          listaSituaciones: this.listaSituaciones,
          listaFactoresPresentes: this.listaFactores,
          tokenIdentificadorFichaIdentificacion: this.uuidFichaIdentificacion,
          tokenIdentificador: this.apreciacionFinalDTO?.tokenIdentificador,
          esEdicion: this.esEdicion
      });

      let load = this.dialogMensajeService.mensajeLoading("Guardando la apreciación final...");
      
      // Llamar al servicio para guardar
      this.servicioApreciacionFinal.crearApreciacionFinal(apreciacionFinal, this.nemonicoMenu).subscribe({
          next: (respuesta) => {
              // Restablecer bandera de procesamiento
              this.estaGuardando = false;
              load.close();
              
              if (!respuesta.exito) {
                  this.servicioApreciacionFinal.checkError(respuesta);
                  return;
              }
              
              this.servicioMensajes.mensajeExitoso(respuesta.titulo, respuesta.mensaje).afterClosed().subscribe(() => {
                  this.enrutador.navigate(['../../'], { relativeTo: this.ruta });
              });
          },
          error: (error: any) => {
              // Restablecer bandera de procesamiento en caso de error
              this.estaGuardando = false;
              load.close();
              this.servicioApreciacionFinal.checkError(error);
          }
      });
  }

  /**
   * Cancela la edición y regresa a la vista anterior
   */
  cancelarEdicion() {
    this.esEdicion = false;
    this.apreciacionFinalDTO = null;
    this.ubicacion.back();
  }
}