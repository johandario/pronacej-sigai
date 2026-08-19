import { Component, ViewChild, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { MatDialog } from '@angular/material/dialog';
import { MdRegiOrieComponent } from './orie-cons-crear-editar/md-regi-orie/md-regi-orie.component';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { ActivatedRoute } from '@angular/router';
import { OrientacionConsejeriaFamiliarService } from 'app/modules/seguridad/services/orientacionConsejeriaFamiliar.service';
import { OrientacionConsejeriaFamiliarDTO } from 'app/core/model/both/orientacionConsejeriaFamiliarDTO.model';
import { OrientacionConsejeriaPorPersonaDTO } from 'app/core/model/both/orientacionConsejeriaPorPersonaDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { TabService } from 'app/core/services/tab.service';

@Component({
  selector: 'app-orientacion-consejeria-familiar',
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
  templateUrl: './orientacion-consejeria-familiar.component.html',
  styleUrl: './orientacion-consejeria-familiar.component.scss'
})
export class OrientacionConsejeriaFamiliarComponent implements OnInit {
  // Variables de identificación
  identificadorFP: string;

  // Propiedad para el centro
  centro: JerarquiaDTO;
  nombreUsuarioActual: string = '';

  // Variable de control para evitar envíos duplicados
  estaProcesandoGuardado: boolean = false;

  // Variables de entidad
  formularioPersonaRelacionada: FormGroup;
  tituloPantalla = 'orientación y consejería familiar';

  // Variables de configuración
  nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_ORIENTACION_CONSEJERIA_FAMILIAR;

  // Variables de datos
  listaPersonasRelacionadas: PersonaRelacionadaDTO[] = [];
  datosPersonaRelacionada: MatTableDataSource<PersonaRelacionadaDTO>;
  listaOrientacionesConsejerias: OrientacionConsejeriaFamiliarDTO[] = [];
  listaOrientacionesConsejeriasOriginal: OrientacionConsejeriaFamiliarDTO[] = [];
  datosOrientacion = new MatTableDataSource<any>([]);
  
  // Lista de catálogos
  listaTiposIdentificacion: CatalogoDTO[] = [];

  // Configuración de columnas
  columnasPersonaRelacionada: string[] = ['nombreCompleto', 'tipoIdentificacion', 'numeroDocumento', 'parentesco'];
  columnasMostrarPersonaRelacionada: any = {
    'nombreCompleto': 'Nombres completos',
    'tipoIdentificacion': 'Tipo documento',
    'numeroDocumento': 'No. documento',
    'parentesco': 'Parentesco'
  };

  columnasOrientacionConsejeriaFamiliar: string[] = ['acciones', 'fecha', 'descripcion', 'nombreCompletoUsuarioCreacion'];
  columnasMostrarOrientacion: any = {
      'acciones': 'Acciones',
      'fecha': 'Fecha',
      'descripcion': 'Descripción',
      'nombreCompletoUsuarioCreacion': 'Usuario que registró'
  };

  @ViewChild('personaRelacionadaPag') paginadorPersonaRelacionada!: MatPaginator;
  @ViewChild('paginatorOrientacion') paginadorOrientacion!: MatPaginator;

  constructor(
    private constructorFormulario: FormBuilder,
    private servicioOrientacionConsejeriaFamiliar: OrientacionConsejeriaFamiliarService,
    private servicioMensajeDialogo: DialogMensajeService,
    private servicioDatosFamiliares: DatosFamiliaresService,
    private rutaActiva: ActivatedRoute,
    public dialogoMaterial: MatDialog,
    public utilidades: FuncionesUtils,
    private clienteHttp: HttpClient,
    private servicioPdf: PdfService,
    private servicioTab: TabService,
    private servicioFichaIdentificacion: FichaIdentificacionService,
    private servicioJerarquia: JerarquiaService,
    private servicioAutenticacion: AuthSerguridadServicio,
  ) {
    this.construirFormulario();
  }

  ngOnInit() {
    this.identificadorFP = this.rutaActiva.snapshot.params['uuid_fp'];
    this.cargarDatosCatalogo();
    this.obtenerTodasPersonasRelacionadas();
    this.cargarCentro();
    this.obtenerDatosUsuarioActual();
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
   * Obtiene los datos del usuario actual usando el servicio de autenticación
   */
  obtenerDatosUsuarioActual() {
    this.servicioAutenticacion.verificarJWT(this.nemonicoMenu).subscribe({
      next: (respuesta) => {
        if (respuesta.success) {
          // La respuesta contiene los datos del usuario actual
          const nombreCompleto = respuesta.user?.name || 'Usuario actual';
          this.nombreUsuarioActual = nombreCompleto;
        } else {
          console.error('Error al obtener usuario:', respuesta.message);
          this.nombreUsuarioActual = 'Usuario actual';
        }
      },
      error: (error) => {
        console.error('Error al obtener datos del usuario actual:', error);
        this.servicioAutenticacion.checkError(error, false);
        this.nombreUsuarioActual = 'Usuario actual';
      }
    });
  }
  
  /**
   * Carga los catálogos necesarios para el funcionamiento del componente
   */
  cargarDatosCatalogo() {
    this.utilidades.obtenerListaCatalogo('TIPO_DOCUMENTO_IDENTIFICACION', this.nemonicoMenu).subscribe({
      next: (datos) => {
        this.listaTiposIdentificacion = datos;
      },
      error: (error) => console.error('Error cargando tipos de documento:', error)
    });
  }

  /**
   * Construye el formulario principal del componente
   */
  construirFormulario() {
    this.formularioPersonaRelacionada = this.constructorFormulario.group({
      personaRelacionada: ['0']
    });
  }

  /**
   * Observa cambios en campos específicos del formulario
   */
  observadorCambioEnCampo(campo: string, evento: any) {
    if (campo === "personaRelacionada") {
      if (evento.value !== '0') {
        this.obtenerOrientacionesConsejerias();
      } else {
        this.datosOrientacion = new MatTableDataSource<any>([]);
        this.datosOrientacion.paginator = this.paginadorOrientacion;
        this.listaOrientacionesConsejerias = [];
        this.listaOrientacionesConsejeriasOriginal = [];
      }
    }
  }

  /**
   * Obtiene todas las personas relacionadas para mostrarlas en la tabla
   */
  obtenerTodasPersonasRelacionadas() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 5;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.identificadorFP;

    this.servicioDatosFamiliares.obtenerPersonasRelacionadas(solicitudPaginacion,this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>) => {
          if (!respuesta.exito) {
            this.servicioMensajeDialogo.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }

          this.listaPersonasRelacionadas = respuesta.data.data;
          this.datosPersonaRelacionada = new MatTableDataSource(this.listaPersonasRelacionadas);
          this.datosPersonaRelacionada.paginator = this.paginadorPersonaRelacionada;
        }
      });
  }

  /**
   * Obtiene las orientaciones y consejerías relacionadas con la persona seleccionada
   */
  obtenerOrientacionesConsejerias() {
    const solicitudPaginacion = new PaginacionRequest();
    solicitudPaginacion.size = 5;
    solicitudPaginacion.page = 0;
    solicitudPaginacion.tokenIdentificador = this.formularioPersonaRelacionada.get('personaRelacionada').value;

    this.servicioOrientacionConsejeriaFamiliar.obtenerOrientacionesConsejeriasFamiliaresPaginado(solicitudPaginacion, this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>>) => {
          if (!respuesta.exito) {
            this.servicioMensajeDialogo.mensajeErrorConTitulo(
              respuesta.titulo,
              respuesta.mensaje
            );
            return;
          }

          this.listaOrientacionesConsejerias = respuesta.data.data;
          // Guardar copia del estado original para detectar cambios
          this.listaOrientacionesConsejeriasOriginal = JSON.parse(JSON.stringify(respuesta.data.data));
          
          this.datosOrientacion = new MatTableDataSource(this.listaOrientacionesConsejerias);
          this.datosOrientacion.paginator = this.paginadorOrientacion;
        },
        error: (error: any) => {
          this.servicioOrientacionConsejeriaFamiliar.checkError(error);
        }
      });
  }

  /**
   * Verifica si hay cambios en las orientaciones usando FuncionesUtils
   */
  private hayOrientacionesModificadas(): boolean {
    const datosActuales = this.datosOrientacion.data;
    
    // Si las longitudes son diferentes, hay cambios
    if (datosActuales.length !== this.listaOrientacionesConsejeriasOriginal.length) {
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
      const original = this.listaOrientacionesConsejeriasOriginal.find(orig => 
        orig.tokenIdentificador === actual.tokenIdentificador
      );
      
      if (!original) {
        return true; // Registro no encontrado en original = nuevo
      }
      
      // Comparar campos relevantes usando FuncionesUtils
      if (this.utilidades.compararFechas(actual.fecha, original.fecha) ||
          (actual.descripcion || '') !== (original.descripcion || '')) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Abre el diálogo para agregar una nueva orientación/consejería
   */
  agregarFilaOrientacion() {
    const refDialogo = this.dialogoMaterial.open(MdRegiOrieComponent, {
      data: {},
      width: '800px',
      disableClose: true,
    });
  
    refDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        // Asignar el nombre del usuario actual al nuevo registro
        resultado.nombreCompletoUsuarioCreacion = this.nombreUsuarioActual;
        
        this.datosOrientacion.data.unshift(resultado);
        this.datosOrientacion = new MatTableDataSource(this.datosOrientacion.data);
        this.datosOrientacion.paginator = this.paginadorOrientacion;
      }
    });
  }

  /**
   * Abre el diálogo para editar una orientación/consejería existente
   */
  editarFilaOrientacion(fila: OrientacionConsejeriaFamiliarDTO, indice: number) {
    const refDialogo = this.dialogoMaterial.open(MdRegiOrieComponent, {
      data: {
        fila: fila,
      },
      width: '800px',
      disableClose: true,
    });
  
    refDialogo.afterClosed().subscribe(async (resultado) => {
      if (resultado) {
        // Mantener el nombre del usuario que creó el registro original
        resultado.nombreCompletoUsuarioCreacion = fila.nombreCompletoUsuarioCreacion || this.nombreUsuarioActual;
        this.datosOrientacion.data[indice] = resultado;
        this.datosOrientacion = new MatTableDataSource(this.datosOrientacion.data);
        this.datosOrientacion.paginator = this.paginadorOrientacion;
      }
    });
  }
  
  /**
   * Elimina una orientación/consejería de la lista o de la base de datos
   */
  eliminarFilaOrientacion(indice: number) {
    const elementoEliminar = this.datosOrientacion.data[indice];
    
    // Mostrar mensaje de confirmación
    const refConfirmacion = this.servicioMensajeDialogo.mensajeConConfirmacion(
      "¿Está seguro de eliminar esta orientación/consejería? Esta operación es irreversible",
      "¿Desea continuar?"
    );
  
    refConfirmacion.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          // Si es un registro nuevo (no está en BD todavía)
          if (elementoEliminar.tokenIdentificador === "0") {
            this.datosOrientacion.data.splice(indice, 1);
            this.datosOrientacion = new MatTableDataSource(this.datosOrientacion.data);
            this.datosOrientacion.paginator = this.paginadorOrientacion;
            this.servicioMensajeDialogo.mensajeExitoso("Registro eliminado", "El registro se ha eliminado correctamente");
          } 
          // Si es un registro de la BD
          else {
            const cargador = this.servicioMensajeDialogo.mensajeLoading("Eliminando orientación/consejería...");
            
            this.servicioOrientacionConsejeriaFamiliar.eliminarOrientacionConsejeriaFamiliar(elementoEliminar, this.nemonicoMenu).subscribe({
              next: (respuesta: RespuestaPorDefecto<boolean>) => {
                cargador.close();
                this.servicioMensajeDialogo.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
  
                if (respuesta.exito) {
                  this.obtenerOrientacionesConsejerias();
                }
              },
              error: (error: any) => {
                cargador.close();
                this.servicioOrientacionConsejeriaFamiliar.checkError(error);
              }
            });
          }
        }
      }
    });
  }

  /**
   * Guarda los cambios en orientaciones y consejerías
   */
  crearActualizar() {
    // Si ya está procesando una solicitud, ignorar clicks adicionales
    if (this.estaProcesandoGuardado) {
      return;
    }

    // Verificar si hay cambios
    if (!this.hayOrientacionesModificadas()) {
      this.servicioMensajeDialogo.mensajeAdvertencia("Sin cambios", "No se detectaron cambios para guardar");
      return;
    }

    // Activar el indicador de envío
    this.estaProcesandoGuardado = true;

    const orientacionConsejeriaPorPersonaDTO = new OrientacionConsejeriaPorPersonaDTO();
    orientacionConsejeriaPorPersonaDTO.listaOrientacionesConsejerias = this.datosOrientacion.data;
    orientacionConsejeriaPorPersonaDTO.tokenIdentificadorPersonaRelacionada = this.formularioPersonaRelacionada.get('personaRelacionada').value;

    const dialogoCarga = this.servicioMensajeDialogo.mensajeLoading("Guardando orientación y consejería familiar...");

    this.servicioOrientacionConsejeriaFamiliar.crearOrientacionConsejeriaFamiliar(orientacionConsejeriaPorPersonaDTO, this.nemonicoMenu).subscribe({
      next: (respuesta: RespuestaPorDefecto<OrientacionConsejeriaFamiliarDTO>) => {
        dialogoCarga.close();
        // Restablecer el estado al finalizar, sin importar el resultado
        this.estaProcesandoGuardado = false;

        if (!respuesta.exito) {
          this.servicioOrientacionConsejeriaFamiliar.checkError(respuesta);
          return;
        }
        this.servicioMensajeDialogo.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
        this.obtenerOrientacionesConsejerias();
        this.servicioTab.cambiarTab(1);
      },
      error: (error: any) => {
        dialogoCarga.close();
        // Restablecer el estado en caso de error
        this.estaProcesandoGuardado = false;
        this.servicioOrientacionConsejeriaFamiliar.checkError(error);
      }
    });
  }

  /**
   * Genera e imprime la ficha de orientación y consejería familiar en formato PDF
   * Este método recopila todos los datos necesarios para generar un PDF con la información
   * de las orientaciones y consejerías para una persona relacionada específica
   */
  imprimirFicha() {
    // 1. Verificar que haya una persona relacionada seleccionada
    const personaRelacionadaSeleccionada = this.formularioPersonaRelacionada.get('personaRelacionada').value;
    if (personaRelacionadaSeleccionada === '0') {
      this.servicioMensajeDialogo.mensajeError('Debe seleccionar una persona relacionada para imprimir la ficha');
      return;
    }
  
    // 2. Mostrar diálogo de confirmación
    const refDialogo = this.servicioMensajeDialogo.mensajeConConfirmacion(
      `¿Está seguro de imprimir la ficha de orientación y consejería familiar?`,
      "¿Desea continuar?"
    );
  
    refDialogo.afterClosed().subscribe({
      next: (respuesta: "confirmed" | "cancelled") => {
        if (respuesta === "confirmed") {
          const dialogoCarga = this.servicioMensajeDialogo.mensajeLoading(`Preparando la impresión...`);
          
          // 3. Obtener los datos de la persona relacionada seleccionada
          const personaSeleccionada = this.listaPersonasRelacionadas.find(p => 
            p.tokenIdentificador === personaRelacionadaSeleccionada);
            
          if (!personaSeleccionada) {
            dialogoCarga.close();
            this.servicioMensajeDialogo.mensajeError('No se pudo encontrar la información de la persona seleccionada');
            return;
          }
  
          // 4. Cargar la imagen del logo como base64
          this.clienteHttp.get('images/logo/logo.png', { responseType: 'arraybuffer' })
            .subscribe({
              next: (datos: ArrayBuffer) => {
                const cadenaCaracteres = String.fromCharCode(...new Uint8Array(datos));
                const imagenBase64 = `data:image/png;base64,${window.btoa(cadenaCaracteres)}`;
                
                // 5. Obtener datos de la ficha de identificación del adolescente
                this.servicioFichaIdentificacion.obtenerFichaIdentificacionPorTokenIdentificador(this.identificadorFP, this.nemonicoMenu)
                  .subscribe({
                    next: (respuestaFicha: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
                      if (!respuestaFicha.exito) {
                        dialogoCarga.close();
                        this.servicioMensajeDialogo.mensajeError('Error al obtener la ficha de identificación');
                        return;
                      }
                      
                      const fichaIdentificacion = respuestaFicha.data;
                      const nombreCompleto = `${fichaIdentificacion.nombres || ''} ${fichaIdentificacion.apellidoPaterno || ''} ${fichaIdentificacion.apellidoMaterno || ''}`.trim();
                      const edadActual = fichaIdentificacion.fechaNacimiento ? this.utilidades.getEdad(fichaIdentificacion.fechaNacimiento).toString() : '';
                      const lugarFechaNacimiento = `${fichaIdentificacion.lugarNacimiento || ''} ${this.utilidades.formatearFecha(fichaIdentificacion.fechaNacimiento)}`;
                      
                      // 6. Crear la solicitud para generar el PDF
                      let solicitudPdf = new GeneracionPdfRequest();
                      solicitudPdf.nemonico = 'FORMULARIO_ORIENTACION_CONSEJERIA_FAMILIAR';
                      
                      // 7. Preparar variables con escapado HTML
                      solicitudPdf.variables = {
                        "[IMG_BASE64]": imagenBase64,
                        "[FECHA-REGISTRO]": this.utilidades.escaparHTML(this.utilidades.formatearFecha(new Date())),
                        "[HORA-REGISTRO]": this.utilidades.escaparHTML(new Date().toLocaleTimeString('es-ES')),
                        "[CENTRO]": this.utilidades.escaparHTML(this.centro?.nombre || 'Centro de rehabilitación'),
                        
                        "[NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(nombreCompleto),
                        "[DNI]": this.utilidades.escaparHTML(fichaIdentificacion.numeroDocumento || ''),
                        "[LUGAR-FECHA-NACIMIENTO]": this.utilidades.escaparHTML(lugarFechaNacimiento),
                        "[EDAD]": this.utilidades.escaparHTML(edadActual),
                        
                        "[PERSONA-RELACIONADA-NOMBRES-APELLIDOS]": this.utilidades.escaparHTML(this.obtenerNombreCompleto(personaSeleccionada)),
                        "[PERSONA-RELACIONADA-DNI]": this.utilidades.escaparHTML(personaSeleccionada.numeroDocumento || ''),
                        "[PERSONA-RELACIONADA-PARENTESCO]": this.utilidades.escaparHTML(personaSeleccionada.parentesco || 'No especificado'),
                        "[PERSONA-RELACIONADA-TIPO-DOCUMENTO]": this.utilidades.escaparHTML(this.obtenerNombreTipoDocumento(personaSeleccionada.tipoIdentificacion) || 'No especificado')
                      };
  
                      // 8. Generar tabla de orientaciones y consejerías
                      let tablaOrientaciones = new TablaPlantilla();
                      tablaOrientaciones.encabezados = ['Fecha', 'Descripción', 'Usuario que registró'];
                      
                      let filasOrientaciones: any[] = [];
                      if (this.datosOrientacion.data && this.datosOrientacion.data.length > 0) {
                        for (let orientacion of this.datosOrientacion.data) {
                          let fila = {
                            'Fecha': this.utilidades.escaparHTML(this.utilidades.formatearFecha(orientacion.fecha)),
                            'Descripción': this.utilidades.escaparHTML(orientacion.descripcion || 'No especificado'),
                            'Usuario que registró': this.utilidades.escaparHTML(orientacion.nombreCompletoUsuarioCreacion || this.nombreUsuarioActual || 'No especificado')
                          };
                          filasOrientaciones.push(fila);
                        }
                      } else {
                        filasOrientaciones.push({
                          'Fecha': '-',
                          'Descripción': 'No hay registros de orientación/consejería para esta persona',
                          'Usuario que registró': '-'
                        });
                      }
                      
                      tablaOrientaciones.filas = filasOrientaciones;
                      
                      solicitudPdf.variables["[TABLA_ORIENTACIONES]"] = JSON.stringify(tablaOrientaciones);
                      
                      // 9. Generar el PDF
                      this.servicioPdf.generarPdf(solicitudPdf, this.nemonicoMenu).subscribe({
                        next: (respuesta: RespuestaPorDefecto<string>) => {
                          dialogoCarga.close();
                          
                          if (!respuesta.exito) {
                            console.error('Error al generar PDF:', respuesta);
                            this.servicioMensajeDialogo.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                            return;
                          }
                          
                          // 10. Abrir el PDF en una nueva pestaña
                          const url = window.URL.createObjectURL(this.utilidades.getPdfBlob(respuesta.data));
                          window.open(url);
                        },
                        error: (error: any) => {
                          dialogoCarga.close();
                          console.error('Error al generar PDF:', error);
                          this.servicioMensajeDialogo.mensajeError('Hubo un problema al generar el PDF. Inténtalo de nuevo.');
                        }
                      });
                    },
                    error: (error: any) => {
                      dialogoCarga.close();
                      console.error('Error al obtener ficha:', error);
                      this.servicioMensajeDialogo.mensajeError('Error al obtener la ficha de identificación');
                    }
                  });
              },
              error: (error) => {
                dialogoCarga.close();
                console.error('Error al cargar imagen:', error);
                this.servicioMensajeDialogo.mensajeError('Error al cargar la imagen del logo');
              }
            });
        }
      }
    });
  }
  
  /**
   * Obtiene el nombre del tipo de documento a partir de su identificador
   * @param tipoDocumento Identificador del tipo de documento
   * @returns Nombre del tipo de documento o valor por defecto
   */
  obtenerNombreTipoDocumento(tipoDocumento: string): string {
    if (!tipoDocumento) {
      return 'No especificado';
    }

    // Intentar obtener nombre usando utilidades
    const nombrePorFuncion = this.utilidades.obtenerNombreCatalogoPorToken(tipoDocumento, this.listaTiposIdentificacion);
    
    // Si se obtuvo un nombre válido, retornarlo
    if (nombrePorFuncion && nombrePorFuncion !== 'Nombre no disponible') {
      return nombrePorFuncion;
    }
    
    // Buscar en la lista de tipos de identificación
    if (this.listaTiposIdentificacion && this.listaTiposIdentificacion.length > 0) {
      // Buscar en el primer nivel
      const catalogo = this.listaTiposIdentificacion.find(item => 
        item.tokenIdentificador === tipoDocumento || 
        item.nemonico === tipoDocumento);
      
      if (catalogo) {
        return catalogo.nombre;
      }
      
      // Buscar en los hijos 
      for (const padre of this.listaTiposIdentificacion) {
        if (padre.hijos && padre.hijos.length > 0) {
          const hijoEncontrado = padre.hijos.find(hijo => 
            hijo.tokenIdentificador === tipoDocumento || 
            hijo.nemonico === tipoDocumento);
          
          if (hijoEncontrado) {
            return hijoEncontrado.nombre;
          }
        }
      }
    }
    
    return tipoDocumento;
  }
  
  /**
   * Obtiene el nombre completo de una persona relacionada
   * @param persona Objeto PersonaRelacionadaDTO 
   * @returns String con el nombre completo
   */
  obtenerNombreCompleto(persona: PersonaRelacionadaDTO): string {
    if (!persona) {
      return 'No especificado';
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
}