import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormsModule, UntypedFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { InstitucionService } from '../../institucion.service';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { MatTabsModule } from '@angular/material/tabs';
import { TablaListaComponent } from 'app/core/components/tabla-lista/tabla-lista.component';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { PageEvent } from '@angular/material/paginator';
import { SeguimientoInstitucionService } from '../../gestion-institucion/gestion-institucion.service';
import { SeguimientoInstitucionDTO } from 'app/core/model/both/SeguimientoInstitucionDTO.model';
import { PdfService } from 'app/core/services/pdf.service';
import { SeleccionarUbigeoComponent } from 'app/modules/administracion/ficha-identificacion/crear-ficha-identificacion/datos-generales/seleccionar-ubigeo/seleccionar-ubigeo.component';
import { MatDialog } from '@angular/material/dialog';
import { catchError, Observable, of, map, forkJoin, Subscription } from 'rxjs';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { ChangeDetectorRef } from '@angular/core';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { LocalidadService } from 'app/modules/seguridad/services/localidad.service';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { TabService } from 'app/core/services/tab.service';

export function noWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (typeof value === 'string') {
      const isWhitespace = value.trim().length === 0;
      const isValid = !isWhitespace;
      return isValid ? null : { whitespace: true };
    }
    return null;
  };
}

export function noSpecialCharactersValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value || '';
    if (typeof value !== 'string') {
      return null;
    }
    const hasInvalidCharacters = /[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/.test(value);
    return hasInvalidCharacters ? { specialCharacters: true } : null;
  };
}



@Component({
  selector: 'app-crear-registro-institucion',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatButtonModule,
    FormsModule,
    SubidaDeDocumentosComponent,
    MatExpansionModule,
    CommonModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatIconModule,
    QuillModule,
    MatTabsModule,
    TablaListaComponent
  ],
  templateUrl: './crear-registro-institucion.component.html',
  styleUrl: './crear-registro-institucion.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    provideNativeDateAdapter(),
  ],
})
export class CrearRegistroInstitucionComponent implements OnInit {
  isLoading: boolean = false;
  tokenID: string;
  fuga: RegistroInstitucionDTO = new RegistroInstitucionDTO();
  seguimiento: SeguimientoInstitucionDTO = new SeguimientoInstitucionDTO();
  estado: string = '';
  uuid_fp: any;
  catalogosTipoOrganizacion: CatalogoDTO[] = [];
  tituloPantalla: string = "Registro de instituciones";
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  @Input() tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  // finalidades: string[] = ['CJDR', 'SOA', 'UAPISE', 'Todas'];
  finalidades: string[] = [];
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  dataRuc: any
  etiquetasModel = etiquetasModel;
  estadoList: string[] = ['Activo', 'Inactivo'];
  proceso: any
  listaProcesos: SeguimientoInstitucionDTO[] = [];
  listaInstituciones: RegistroInstitucionDTO[] = [];
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_GESTION_INSTITUCION;
  funcionarioActivo: FuncionarioDTO;
  tokenJerarquia: any
  jerarquia: any;
  tokenFilter: any
  indiceTabSeleccionado: number = 0;

  @ViewChild('tabla') tablaComponent: TablaListaComponent<any>;

  keyLabelsTable: any = {
    idSeguimientoInstitucion: "No.",
    acciones: "Acciones",
    numeroDoc: "Número de documento",
    estado: "Estado",
    cumpleObjetivo: "Cumple objetivo",
    personaResponsable: "Persona responsable",
    fechaRegistro: "Fecha seguimiento",
  };

  informeSucesosForm = this.fb.group({
    ruc: ['', Validators.required],
    nombreOrganizacion: [''],
    nombContactoOperacional: ['', Validators.required],
    nombreDirector: ['', Validators.required],
    direccion: [''],
    telefono: ['', Validators.required],
    email: ['', Validators.required],
    fax: ['', Validators.required],
    sitioWeb: ['', Validators.required],
    dni: ['', Validators.required],
    misionInstitucional: ['', [Validators.required, noSpecialCharactersValidator()]],
    objetivoInstitucional: ['', [Validators.required, noSpecialCharactersValidator()]],
    departamento: ['', [Validators.required, noSpecialCharactersValidator()]],
    servicios: ['', [Validators.required, noSpecialCharactersValidator()]],
    beneficios: ['', [Validators.required, noSpecialCharactersValidator()]],
    horariosServicios: ['', Validators.required],
    serviciosArticulados: ['', [Validators.required, noSpecialCharactersValidator()]],
    areaGeografica: ['', [Validators.required, noSpecialCharactersValidator()]],
    participacionEspaciosLocales: ['', [Validators.required, noSpecialCharactersValidator()]],
    otroSitioWeb: ['', Validators.required],
    tipoOrganizacion: ['', Validators.required],
    tieneConvenio: ['', Validators.required],
    codigoUbigeoUbicacion: [null,],
    rutaUbigeoDireccion: [null],
    finalidadInstitucion: ['', Validators.required],
    estado: ['', Validators.required],

  });
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INSTITUCION;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private institucionService: InstitucionService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private seguimientoInstitucionService: SeguimientoInstitucionService,
    private utilService: PdfService,
    public dialog: MatDialog,
    private catalogoService: CatalogoService,
    private cdr: ChangeDetectorRef,
    private localidadService: LocalidadService,
    private funcionarioService: FuncionarioService,
    private jerarquiaService: JerarquiaService,
    private tabService: TabService,
  ) { }

  ngOnInit(): void {
    this.tabService.tabIndex$.subscribe(indice => {
      this.indiceTabSeleccionado = indice;
    });
    this.verificarTiposDeDocumentos();
    this.obtenerInstituciones()
    // this.obtenerTokenDepartamento().then(() => {
    //   this.obtenerJerarquias();
    // });
    this.obtenerJerarquia();
    this.proceso = history.state?.proceso;
    this.route.queryParams.subscribe((params) => {
      if (this.proceso) {
        this.fuga = this.proceso;
        if (this.proceso.tipoOrganizacion) {
          this.informeSucesosForm.get('tipoOrganizacion').setValue(this.proceso.tipoOrganizacion);
        }
        const codigoUbigeo = this.proceso.codigoUbigeoUbicacion;
        if (codigoUbigeo) {
          this.localidadService.obtenerLocalidadUbigeo(codigoUbigeo,this.nemonicoMenu).subscribe({
            next: (resp: RespuestaPorDefecto<LocalidadDTO>) => {
              if (resp.exito) {
                this.informeSucesosForm.get('rutaUbigeoDireccion').setValue(resp.data.rutaUbigeo || null);
              } else {
                this.informeSucesosForm.get('rutaUbigeoDireccion').setValue(null);
              }
            },
            error: (error: any) => {
              console.error('Error al obtener el ubigeo:', error);
            },
          });
          if (history.state.editar) {
            this.informeSucesosForm.enable();
          } else {
            this.informeSucesosForm.disable();

          }
        }
        this.informeSucesosForm.patchValue(this.proceso);
        this.obtenerProcesos()
      }
      // else {
      //     console.error(" No se recibió el objeto proceso en el componente destino");
      // }
    });
    this.informeSucesosForm.get('codigoUbigeoUbicacion').valueChanges.subscribe((value) => {
      if (value && value.length % 2 === 0) {
        this.localidadService.obtenerLocalidadUbigeo(value,this.nemonicoMenu).subscribe({
          next: (resp: RespuestaPorDefecto<LocalidadDTO>) => {
            if (resp.exito) {
              this.informeSucesosForm.get('rutaUbigeoDireccion').setValue(resp.data.rutaUbigeo || null);
            } else {
              this.informeSucesosForm.get('rutaUbigeoDireccion').setValue(null);
            }
          },
          error: (error: any) => {
            console.error('Error al obtener el ubigeo:', error);
          },
        });
      }
    });
    this.getCatalogoTipoOrganizacion().subscribe({
      next: (catalogos: CatalogoDTO[]) => {
        this.catalogosTipoOrganizacion = catalogos;
        const selectedCatalogo = this.catalogosTipoOrganizacion.find(c => c.tokenIdentificador === this.informeSucesosForm.get('tipoOrganizacion').value?.tokenIdentificador);
        if (selectedCatalogo) {
          this.informeSucesosForm.get('tipoOrganizacion').setValue(selectedCatalogo);
        }
      },
      error: (error) => {
        console.error('Error al obtener catálogos de tipo organización:', error);
      }
    });
    this.informeSucesosForm.get('finalidadInstitucion')?.valueChanges.subscribe((valor: string) => {
      if (valor === 'Todas') {
        this.fuga.centro = null;
      } else if (!this.fuga.centro && this.jerarquia?.[0]) {
        this.fuga.centro = this.jerarquia[0];
      }
    });
  }



  cancelar() {
    this.router.navigate([`/institucion/registros`]);
  }

  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    let errores = [];
    Object.keys(this.informeSucesosForm.controls).forEach(key => {
      const control = this.informeSucesosForm.get(key);
      if (control?.errors) {
        console.log(`${key}:`, {
          Valor: control.value,
          Validez: control.valid,
          Errores: control.errors
        });
        if (control.errors['required']) {
          errores.push(`El campo "${key}" es obligatorio.`);
        }
        if (control.errors['specialCharacters']) {
          errores.push(`El campo "${key}" no permite caracteres especiales.`);
        }
      }
    });
    if (errores.length > 0) {
      this.dialogMensajeService.mensajeError(errores.join('\n'));
      return;
    }
    this.route.queryParams.subscribe(() => {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de institución',
        "¿Deseas continuar?"
      );
      ref.afterClosed().subscribe({
        next: (resp: "confirmed" | "cancelled") => {
          if (resp === "confirmed") {
            Object.assign(this.fuga, this.informeSucesosForm.value);
            this.institucionService.setRegistroInstitucionData(this.fuga);
            this.router.navigate(['/institucion/registro-institucion/crear-gestion']);
            this.institucionService.crearEditarInstitucion(this.fuga, this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<RegistroInstitucionDTO>) => {
                if (!response.exito) {
                  this.institucionService.checkError(response);
                  return;
                }
                this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                this.router.navigate([`/institucion/registros`]);
              },
              error: (error: any) => {
                this.institucionService.checkError(error);
              },
            });
          }
        },
      });
    });
  }


  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select(); // Selecciona todo el texto
  }


  obtenerProcesos() {
    this.paginacionRequest.size = this.size;
    this.paginacionRequest.page = this.page;
    this.paginacionRequest.filter = this.paginacionRequest.filter;
    this.paginacionRequest.tokenIdentificador = this.proceso.tokenIdentificador;
    this.seguimientoInstitucionService.obtenerRegistroInstituciones(this.paginacionRequest,this.nemonicoMenuinicio).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SeguimientoInstitucionDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            return;
          }
          this.listaProcesos = response.data.data;
        },
        error: (error: any) => {
          this.institucionService.checkError(error);
        }
      }
    );
  }

  descargarExcelCompleto() {
    this.paginacionRequest.size = 100000;
    this.paginacionRequest.page = 0;
    this.paginacionRequest.filter = this.paginacionRequest.filter;
    this.paginacionRequest.tokenIdentificador = this.proceso.tokenIdentificador;

    this.seguimientoInstitucionService.obtenerRegistroInstituciones(this.paginacionRequest, this.nemonicoMenuinicio).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<SeguimientoInstitucionDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            return;
          }

          this.tablaComponent.exportXLSX(response.data.data);
        },
        error: (error: any) => {
          this.institucionService.checkError(error);
        }
      }
    );
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
  }

  handleSearchEvent(filter: string) {
    this.paginacionRequest.filter = filter;
    this.obtenerProcesos();

  }

  refrescar() {
    this.obtenerProcesos()
  }




  editarSeguimiento(proceso: SeguimientoInstitucionDTO) {
    this.router.navigate(['/institucion/registro-institucion/crear-gestion'], {
      relativeTo: this.route,
      state: {
        editar: true,
        proceso: proceso
      }
    });
  }

  visualizar(proceso: SeguimientoInstitucionDTO) {
    this.router.navigate(['/institucion/registro-institucion/crear-gestion'], {
      relativeTo: this.route,
      state: {
        editar: false,
        proceso: proceso
      }
    });

  }


  eliminarSeguimiento(gestionFugaDTO: SeguimientoInstitucionDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar este registro, esta operación es irreversible",
      "Deseas continuar?"
    );
    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la fuga..");
            this.seguimientoInstitucionService.eliminarInstitucion(gestionFugaDTO, this.nemonicoMenuinicio).subscribe(
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

  agregarSeguimiento() {
    if (this.proceso) {
      this.router.navigate(['/institucion/registro-institucion/crear-gestion'], {
        relativeTo: this.route,
        state: {
          crear: true,
          proceso: this.proceso
        }
      });
    }

  }



  buscarRuc(): void {
    const ruc = this.informeSucesosForm.get('ruc')?.value;
    if (!ruc || ruc.trim() === '') {
      this.dialogMensajeService.mensajeError('Por favor, ingresa un RUC válido antes de buscar.');
      return;
    }
    const centroActual = this.tokenFilter;
    const rucExisteEnCentro = this.listaInstituciones.some(inst =>
      inst.ruc === ruc && inst.centro?.tokenIdentificador === centroActual
    );
    if (rucExisteEnCentro) {
      this.dialogMensajeService.mensajeError(`El RUC ${ruc} ya está registrado en este centro.`);
      return;
    }
    this.utilService.dataSunat(ruc).subscribe({
      next: (data: any) => {
        this.dataRuc = data;
        this.informeSucesosForm.patchValue({
          direccion: this.dataRuc.direccion || this.informeSucesosForm.get('direccion')?.value,
          nombreOrganizacion: this.dataRuc.razon_social || this.informeSucesosForm.get('nombreOrganizacion')?.value
        });
        this.dialogMensajeService.mensajeExitoso('Datos obtenidos correctamente', 'Éxito');
      },
      error: (error: any) => {
        console.error('Error al obtener datos de SUNAT:', error);
        this.dialogMensajeService.mensajeError('No se pudo obtener la información del RUC ingresado.');
      }
    });
  }




  soloNumero(event: KeyboardEvent): void {
    const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete'];
    const isNumberKey = event.key >= '0' && event.key <= '9';

    if (!isNumberKey && !allowedKeys.includes(event.key)) {
      event.preventDefault();
    }
  }

  abrirModalUbigeoActual() {
    const dialogRef = this.dialog.open(SeleccionarUbigeoComponent, {
      width: '600px',
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        this.informeSucesosForm.get('codigoUbigeoUbicacion').setValue(result);
        this.cdr.detectChanges();
      }
    });
  }



  getCatalogoTipoOrganizacion(): Observable<CatalogoDTO[]> {
    return this.catalogoService.obtenerHijos('TIPO_PRESTACION', this.nemonicoMenu).pipe(
      map(responseCatalog => {
        return responseCatalog.data || [];
      }),
      catchError(error => {
        console.error('Error al obtener motivo salida:', error);
        return of([]);
      })
    );
  }

  soloNumeros(event: KeyboardEvent) {
    const charCode = event.key.charCodeAt(0);
    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
    }
  }

  verificarTiposDeDocumentos() {
    if (this.tiposDeDocumentosSistema.length == 0) {
      this.catalogoService.obtenerHijos(
        etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (respuesta: RespuestaPorDefecto<CatalogoDTO[]>) => {
            if (!respuesta.exito) {
              this.catalogoService.checkError(respuesta);
              return;
            }
            respuesta.data.forEach((tipoDoc) => {
              let documento: TipoDeDocumento = tipoDoc as TipoDeDocumento;
              documento.requerido = documento.nemonico !== etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS;
              this.tiposDeDocumentosSistema.push(documento);
            });
          },
          error: (error: any) => {
            this.catalogoService.checkError(error);
          }
        }
      );
    }
  }


  obtenerInstituciones() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.filter = this.paginacionRequest.filter;
    this.institucionService.obtenerRegistroInstituciones(paginacionRequest,this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>) => {
          if (!response.exito) {
            this.institucionService.checkError(response);
            return;
          }
          this.listaInstituciones = response.data.data;

        },
        error: (error: any) => {
          this.institucionService.checkError(error);
        }
      }
    );
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
          this.tokenJerarquia = this.funcionarioActivo.departamento;
          this.tokenFilter = this.funcionarioActivo.tokenIdentificadorDepartamento
          resolve();
        },
        error: (error: any) => {
          console.error('Error al obtener el departamento:', error);
          resolve();
        }
      });
    });
  }

  obtenerJerarquias() {
    this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
      if (!this.tokenJerarquia) {
        console.warn("Token de jerarquía no definido aún.");
        return;
      }
      this.jerarquia = data.data.filter(j => j.nombre === this.tokenJerarquia);
      if (this.jerarquia.length > 0) {
        this.fuga.centro = this.jerarquia[0];
       
        this.filtrarFinalidades();


      } else {
        this.fuga.centro = null;
      }
    });
  }


  cambiarPestana(indice: number) {
    this.indiceTabSeleccionado = indice;
  }


  filtrarFinalidades() {
    const nombreCentro = this.fuga.centro?.nombre?.toUpperCase() || '';
    const posibles = ['CJDR', 'SOA', 'UAPISE'];
    const tipoFinalidad = posibles.find(prefijo => nombreCentro.startsWith(prefijo));
    this.finalidades = tipoFinalidad ? [tipoFinalidad, 'Todas'] : ['Todas'];
  }


  obtenerJerarquia(): void {
  this.jerarquiaService
    .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
    .subscribe({
      next: (response: RespuestaPorDefecto<any>) => {
        if (!response.exito || !response.data) {
          this.dialogMensajeService.mensajeError('No se pudo obtener la jerarquía del usuario.');
          return;
        }
        this.fuga.centro = response.data
        console.log(response.data);
        this.filtrarFinalidades()
       
      },
      error: (err) => {
        console.error('Error al obtener jerarquía por documento:', err);
        this.dialogMensajeService.mensajeError('Error al obtener la jerarquía del usuario.');
      },
    });
}




}
