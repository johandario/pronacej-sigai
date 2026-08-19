import { Component, Inject, LOCALE_ID, ViewEncapsulation } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { DateAdapter, MatNativeDateModule } from '@angular/material/core';
import { CommonModule, Location } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { ActivatedRoute, Router } from '@angular/router';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { ActaExternamientoDTO } from 'app/core/model/both/ia/actaExternamientoDTO.model';
import { ActaExternamientoService } from 'app/modules/administracion/services/actaExternamiento.service';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { ActaExternamientoDocumentoDTO } from 'app/core/model/both/ia/actaExternamientoDocumentoDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatIconModule } from '@angular/material/icon';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { ExpedienteMatrizDTO } from 'app/core/model/both/expedienteMatrizDTO.model';
import { ExpedienteMatrizService } from 'app/modules/seguridad/services/expedienteMatriz.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-acta-externamiento-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatCardModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatNativeDateModule,
    SubidaDeDocumentosComponent
  ],
  templateUrl: './acta-externamiento-crear-editar.component.html',
  styleUrl: './acta-externamiento-crear-editar.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class ActaExternamientoCrearEditarComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO;

  uuid_fp: string;

  esEdicion: boolean = false;
  impreso: boolean = false;
  firmado: boolean = false;
  item: ActaExternamientoDTO;
  actaForm: FormGroup;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];

  listaExpedientes: ExpedienteMatrizDTO[];
  listaTiposDocumento: CatalogoDTO[];
  listaTiposAutorizacion: CatalogoDTO[];
  personasRelacionadas: PersonaRelacionadaDTO[] = [];
  esVisualizacion: boolean = false;

  loadingDialogRef: any;

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private router: Router,
    private route: ActivatedRoute,
    private catalogoService: CatalogoService,
    private dialogMensajeService: DialogMensajeService,
    private personaService: DatosFamiliaresService,
    private expedienteService: ExpedienteMatrizService,
    private actaService: ActaExternamientoService,
    private funcionesUtils: FuncionesUtils
  ) {
    this.actaForm = this.fb.group({
      expediente: ['', Validators.required],
      ingreso: ['', Validators.required],
      institucion: ['', Validators.required],
      autorizacion: ['', Validators.required],
      tipoDocumento: ['', Validators.required],
      numeroDocumento: ['', Validators.required],
      resolucion: ['', Validators.required],
      domicilio: ['', Validators.required],
      mandatoDetencion: [false, Validators.required],
      retiroSolo: [false, Validators.required],
      familiaresSeleccionados: [[]],
      familiares: this.fb.array([]),
      observaciones: ['']
    });
  }

  ngOnInit() {
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.AUTORIZACION_EXTERNAMIENTO, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaTiposAutorizacion = data;
      },
      error: (error) => console.error('Error cargando tipos de autorización:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.TIPO_DOCUMENTO_EXTERNAMIENTO, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaTiposDocumento = data;
      },
      error: (error) => console.error('Error cargando tipos de documento:', error)
    });

    this.actaForm.get('retiroSolo')?.valueChanges.subscribe(value => {
      this.actualizarValidacionesFamiliares(value);
    });

    this.item = history.state.item;

    if (history.state.esVisualizacion) {
      this.esVisualizacion = true;
      this.actaForm.disable();
    }

    if (this.item) {
      this.esEdicion = true;
      this.cargarCamposEdicion();
    }

    this.cargarExpedientes();
    this.cargarPersonasRelacionadas();
    this.verificarTiposDeDocumentos();
  }

  cargarCamposEdicion() {
    this.actaForm.controls.expediente.setValue(this.item.tokenExpedienteMatriz);
    this.actaForm.controls.ingreso.setValue(this.item.ingreso);
    this.actaForm.controls.institucion.setValue(this.item.institucion);
    this.actaForm.controls.autorizacion.setValue(this.item.autorizacion);
    this.actaForm.controls.tipoDocumento.setValue(this.item.nemonicoTipoDocumento);
    this.actaForm.controls.numeroDocumento.setValue(this.item.numeroDocumento);
    this.actaForm.controls.resolucion.setValue(this.item.resolucion);
    this.actaForm.controls.domicilio.setValue(this.item.domicilio);
    this.actaForm.controls.mandatoDetencion.setValue(this.item.mandatoDetencion);
    this.actaForm.controls.retiroSolo.setValue(this.item.retiroSolo);
    this.actaForm.controls.observaciones.setValue(this.item.observaciones);

    // Procesar familiares
    const familiaresNombres = this.item.familiares.split(';');
    const familiaresParentescos = this.item.parentescos.split(';');
    const familiaresIdentificaciones = this.item.identificaciones.split(';');
    const familiaresDirecciones = this.item.direcciones.split(';');
    const familiaresTelefonos = this.item.telefonos.split(';');

    // Reconstruir el FormArray de familiares
    const familiaresFormArray = this.actaForm.controls.familiares as FormArray;
    familiaresFormArray.clear(); // Limpiar el FormArray antes de llenarlo

    for (let i = 0; i < familiaresNombres.length; i++) {
      familiaresFormArray.push(
        this.fb.group({
          nombre: [familiaresNombres[i], [Validators.required]],
          parentesco: [familiaresParentescos[i], [Validators.required]],
          identificacion: [familiaresIdentificaciones[i], [Validators.required, this.noSpacesValidator]],
          direccion: [familiaresDirecciones[i], [Validators.required]],
          telefono: [familiaresTelefonos[i], [Validators.required, this.noSpacesValidator]]
        })
      );
    }

    // Manejo de estado del formulario si está impreso o firmado
    if (this.item.impreso) {
      this.impreso = true;
      this.actaForm.disable();
    }

    if (this.item.firmado)
      this.firmado = true;

    // Actualizar validaciones de familiares
    this.actualizarValidacionesFamiliares(this.item.retiroSolo);
  }

  noSpacesValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const value = control.value || '';

    const onlySpaces = value.trim().length === 0;          // Verifica si solo tiene espacios

    if (onlySpaces) {
      return { 'onlySpaces': true };    // Si son solo espacios, retorna error
    }

    return null; // Si es válido, retorna null
  }

  soloLetrasNumeros(event: KeyboardEvent) {
    const regex = /^[a-zA-Z0-9]$/; // Permite solo letras y números
    if (!regex.test(event.key)) {
      event.preventDefault(); // Impide la entrada de caracteres especiales
    }
  }

  soloNumeros(event: KeyboardEvent) {
    const charCode = event.key.charCodeAt(0);
    if (charCode < 48 || charCode > 57) {
      event.preventDefault(); // Impide que se ingresen caracteres no numéricos
    }
  }

  actualizarValidacionesFamiliares(retiroSolo: Boolean) {
    const familiaresControl = this.actaForm.get('familiaresSeleccionados');
    const familiaresFormArray = this.actaForm.get('familiares') as FormArray;

    // Si se retira solo, los familiares no son requeridos
    if (retiroSolo) {
      familiaresControl?.clearValidators();
      familiaresFormArray.clear();
    } else {
      familiaresControl?.setValidators([Validators.required]);
    }
    familiaresControl?.updateValueAndValidity();
  }

  guardar() {
    const dialogRef = this.dialogMensajeService.mensajeConConfirmacion(
      'Guardar',
      '¿Estás seguro de guardar el registro?'
    );

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'confirmed') {
        this.loadingDialogRef = this.dialogMensajeService.mensajeLoading('Guardando el registro...');

        if (this.esEdicion)
          this.editarActa();
        else
          this.crearActa();
      }
    });

  }

  onExpedienteChange(event: MatSelectChange) {
    const expedienteSeleccionado = this.listaExpedientes.find(x => x.tokenIdentificador == event.value);

    if (expedienteSeleccionado) {
      this.actaForm.controls.ingreso.setValue(expedienteSeleccionado.motivoIngreso);
      const detalles = expedienteSeleccionado.expedienteDetalle;
    }
  }

  crearActa() {

    let actaDTO = new ActaExternamientoDTO();

    actaDTO.tokenFichaIdentificacion = this.uuid_fp;
    actaDTO.tokenExpedienteMatriz = this.actaForm.controls.expediente.value;
    actaDTO.fechaRegistro = new Date();
    actaDTO.ingreso = this.actaForm.controls.ingreso.value;
    actaDTO.institucion = this.actaForm.controls.institucion.value;
    actaDTO.autorizacion = this.actaForm.controls.autorizacion.value;
    actaDTO.nemonicoTipoDocumento = this.actaForm.controls.tipoDocumento.value;
    actaDTO.numeroDocumento = this.actaForm.controls.numeroDocumento.value;
    actaDTO.resolucion = this.actaForm.controls.resolucion.value;
    actaDTO.domicilio = this.actaForm.controls.domicilio.value;
    actaDTO.mandatoDetencion = this.actaForm.controls.mandatoDetencion.value;
    actaDTO.retiroSolo = this.actaForm.controls.retiroSolo.value;
    actaDTO.observaciones = this.actaForm.controls.observaciones.value;

    // Procesar familiares
    const familiaresArray = this.actaForm.controls.familiares.value;

    // Crear los strings concatenados
    actaDTO.familiares = familiaresArray.map((f: any) => f.nombre).join(';');
    actaDTO.parentescos = familiaresArray.map((f: any) => f.parentesco).join(';');
    actaDTO.identificaciones = familiaresArray.map((f: any) => f.identificacion).join(';');
    actaDTO.direcciones = familiaresArray.map((f: any) => f.direccion).join(';');
    actaDTO.telefonos = familiaresArray.map((f: any) => f.telefono).join(';');

    this.actaService.crearActaExternamiento(actaDTO, this.nemonicoMenu)
    .pipe(
      finalize(() => {
        this.loadingDialogRef.close();
      })
    )
    .subscribe({
      next: (resp: RespuestaPorDefecto<Boolean>) => {
        if (!resp.exito) {
          this.dialogMensajeService.mensajeError(
            'Error al guardar el registro. ' + resp.mensaje
          )
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(
            'Guardar',
            'Registro guardado correctamente.'
          ).afterClosed().subscribe(() => {
            this.location.back();
          });
        }
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  editarActa() {
    const loadingDialog = this.dialogMensajeService.mensajeLoading('Guardando el registro...');

    let actaDTO = this.item;

    actaDTO.tokenExpedienteMatriz = this.actaForm.controls.expediente.value;
    actaDTO.ingreso = this.actaForm.controls.ingreso.value;
    actaDTO.institucion = this.actaForm.controls.institucion.value;
    actaDTO.autorizacion = this.actaForm.controls.autorizacion.value;
    actaDTO.nemonicoTipoDocumento = this.actaForm.controls.tipoDocumento.value;
    actaDTO.numeroDocumento = this.actaForm.controls.numeroDocumento.value;
    actaDTO.resolucion = this.actaForm.controls.resolucion.value;
    actaDTO.domicilio = this.actaForm.controls.domicilio.value;
    actaDTO.mandatoDetencion = this.actaForm.controls.mandatoDetencion.value;
    actaDTO.retiroSolo = this.actaForm.controls.retiroSolo.value;
    actaDTO.observaciones = this.actaForm.controls.observaciones.value;

    // Procesar familiares
    const familiaresArray = this.actaForm.controls.familiares.value;

    // Crear los strings concatenados
    actaDTO.familiares = familiaresArray.map((f: any) => f.nombre).join(';');
    actaDTO.parentescos = familiaresArray.map((f: any) => f.parentesco).join(';');
    actaDTO.identificaciones = familiaresArray.map((f: any) => f.identificacion).join(';');
    actaDTO.direcciones = familiaresArray.map((f: any) => f.direccion).join(';');
    actaDTO.telefonos = familiaresArray.map((f: any) => f.telefono).join(';');

    this.actaService.actualizarActaExternamiento(actaDTO, this.nemonicoMenu)
    .pipe(
      finalize(() => {
        this.loadingDialogRef.close();
      })
    )
    .subscribe({
      next: (resp: RespuestaPorDefecto<Boolean>) => {
        if (!resp.exito) {
          this.dialogMensajeService.mensajeError(
            'Error al actualizar el registro. ' + resp.mensaje
          )
          return;
        }
        else {
          this.dialogMensajeService.mensajeExitoso(
            'Editar',
            'Registro actualizado correctamente.'
          ).afterClosed().subscribe(() => {
            this.location.back();
          });
        }
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar el registro. Inténtalo de nuevo.'
        );
      },
      complete: () => {loadingDialog.close();}
    });
  }

  cancelar() {
    this.location.back();
  }

  cargarExpedientes() {
    let fichaDTO = new FichaIdentificacionDTO();
    fichaDTO.tokenIdentificador = this.uuid_fp;

    this.personaService.obtenerPersonasRelacionadasPorTokenFicha(fichaDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<PersonaRelacionadaDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            response.mensaje
          );
          return;
        }

        this.personasRelacionadas = response.data;
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = 100;
    paginacionRequest.page = 0;
    paginacionRequest.tokenIdentificador = this.uuid_fp;

    this.expedienteService.obtenerExpedientesValidos(paginacionRequest, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<ExpedienteMatrizDTO>>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              response.mensaje
            );
            return;
          }

          this.listaExpedientes = response.data.data;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los expedientes. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  cargarPersonasRelacionadas() {
    let fichaDTO = new FichaIdentificacionDTO();
    fichaDTO.tokenIdentificador = this.uuid_fp;

    this.personaService.obtenerPersonasRelacionadasPorTokenFicha(fichaDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<PersonaRelacionadaDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            response.mensaje
          );
          return;
        }

        this.personasRelacionadas = response.data;

        if (this.esEdicion) {
          const familiaresNombres = this.item.familiares.split(';');

          // Obtener los objetos familiares relacionados
          const familiaresSeleccionados = this.personasRelacionadas.filter(persona =>
            familiaresNombres.includes(persona.nombres)
          );

          // Asignar los objetos familiares seleccionados al select (formControl 'familiaresSeleccionados')
          this.actaForm.controls.familiaresSeleccionados.setValue(familiaresSeleccionados);
        }
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  subirActa(documentos: DocumentoSubido[]) {
    if (documentos.length > 1) {
      this.dialogMensajeService.mensajeError(
        'Solo 1 documento permitido.'
      );
      return;
    }
    let documentoSubido = documentos.at(0);

    //TODO: GUARDAR REGISTRO DE MANDATO ANTES DE SUBIR UN ARCHIVO
    if (documentoSubido) {

      let actaDTO = this.item;
      actaDTO.actaExternamientoDocumentoDTO = new ActaExternamientoDocumentoDTO();
      actaDTO.actaExternamientoDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

      let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
        documentoSubido.documentoDTO.nombre
      );
      this.actaService.subirActaFirmada(
        actaDTO,
        documentoSubido.documento,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (response: RespuestaPorDefecto<Boolean>) => {

            load.close();
            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al subir el documento. Inténtalo de nuevo.'
              );
              return;
            }

            this.dialogMensajeService.mensajeExitoso(
              'Subir',
              'Documento subido correctamente.'
            ).afterClosed().subscribe(() => {
              this.location.back();
            });
          },
          error: (error: any) => {
            load.close();
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al subir el documento. Inténtalo de nuevo.'
            );
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
    }
  }

  verificarTiposDeDocumentos() {
    if (this.tiposDeDocumentosSistema.length == 0) {
      this.catalogoService.obtenerCatalogoPorNemonico(
        etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (respuesta: RespuestaPorDefecto<CatalogoDTO>) => {
            if (!respuesta.exito) {
              this.catalogoService.checkError(respuesta);
              return;
            }

            let tipoDoc = respuesta.data as TipoDeDocumento;
            tipoDoc.requerido = tipoDoc.nemonico != etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS;

            this.tiposDeDocumentosSistema.push(tipoDoc);
          },
          error: (error: any) => {
            this.catalogoService.checkError(error);
          }
        }
      );
    }
  }

  get familiares(): FormArray {
    return this.actaForm.get('familiares') as FormArray;
  }

  eliminarFamiliar(index: number) {
    this.familiares.removeAt(index);
  }

  onFamiliaresSeleccionados(seleccionados: PersonaRelacionadaDTO[]) {
    const familiaresActuales = this.familiares.value.map((f: any) => f.nombre);

    // Agrega nuevos familiares seleccionados al FormArray
    seleccionados.forEach((familiar) => {
      if (!familiaresActuales.includes(familiar.nombres)) {
        const familiarGroup = this.fb.group({
          nombre: [familiar.nombres, Validators.required],
          parentesco: [familiar.tipoParentesco, Validators.required],
          identificacion: [familiar.numeroDocumento ? familiar.numeroDocumento : '', [Validators.required, this.noSpacesValidator]],
          direccion: ['', Validators.required],
          telefono: [familiar.telefono ? familiar.telefono : '', [Validators.required, this.noSpacesValidator]]
        });

        if (familiar.informacionUbicaciones) {
          const direccion = familiar.informacionUbicaciones.find(x => x.tipoInformacionUbicacion == etiquetasModel.INFORMACION_PERSONAL_DIRECCION);
          if (direccion)
            familiarGroup.controls.direccion.setValue(direccion.valor);

          // const telefono = familiar.informacionUbicaciones.find(x => x.tipoInformacionUbicacion == etiquetasModel.INFORMACION_PERSONAL_TELEFONO);
          // if (telefono)
          //   familiarGroup.controls.telefono.setValue(telefono.valor);
        }
        this.familiares.push(familiarGroup);
      }
    });

    // Elimina familiares deseleccionados
    this.familiares.controls.forEach((control, index) => {
      if (!seleccionados.find((f) => f.nombres === control.get('nombre')?.value)) {
        this.eliminarFamiliar(index);
      }
    });
  }

}
