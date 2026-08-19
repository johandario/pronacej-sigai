import { ChangeDetectorRef, Component,Input,OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormsModule, UntypedFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter,DateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { catchError, forkJoin, map, Observable, of, startWith, Subject, takeUntil } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { ActividadSalidaDTO } from 'app/core/model/both/salida/RegistroSalidaDTO.model';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service'; 
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { CUSTOM_DATE_FORMATS, FuncionesUtils,CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PdfService } from 'app/core/services/pdf.service';
import { PermisoSalidaDTO } from 'app/core/model/both/salida/PermisoSalidaDTO.model';
import { PermisoSalidaService } from '../../salida-permiso/permiso-salida.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { HttpClient } from '@angular/common/http';
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
    const hasSpecialCharacters = /[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s]/.test(value);
    return hasSpecialCharacters ? { specialCharacters: true } : null;
  };
}

@Component({
  selector: 'app-crear-editar-registro-salida',
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
    MatPaginatorModule,
    MatTableModule,
    MatCardModule,
    RouterLink],
  templateUrl: './crear-editar-registro-salida.component.html',
  styleUrl: './crear-editar-registro-salida.component.scss',
  providers: [ 
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class CrearEditarRegistroSalidaComponent implements OnInit {

  isLoading: boolean =  false;
  tokenID: string;
  fuga: PermisoSalidaDTO = new PermisoSalidaDTO();
  estado: string = '';
  horaISO: string
  fechaISO: string
  horaRegresoISO: string
  fechaRegresoISO: string
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();
  uuid_fp: any;
  actividades: ActividadSalidaDTO[] = [];
  displayedColumns: string[] = ['indice', 'acciones', 'actividad']; 
  catalogosTipoSalida: CatalogoDTO[] = [];
  catalogosMotivoSalida: CatalogoDTO[] = [];
  centrosOrigenFiltrado: Observable<JerarquiaDTO[]>;
  centrosOrigen: JerarquiaDTO[];
  dataSource: any[] = [];
  user: User;
  catalogosFrecuenciaSalida: CatalogoDTO[] = [];
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
  funcionarioActivo: FuncionarioDTO;
  uuid_adolescente: string;
  uuid_registro: string;
  param : string;
  base64Image: string | null = null;
  tokenJerarquia: any
  jerarquia: any;
  tokenFilter: any
  mostrarOtrosSalida: boolean = false;


  informeSucesosForm = this.fb.group({
    fechaSalida: ['', Validators.required],
    horaSalida: ['', Validators.required],
    usuarioSalida: ['', Validators.required],
    nroDocumento: ['', Validators.required],
    observaciones: ['', Validators.required],
    tipoSalida: ['', Validators.required],
    tipoSalidaLugar: ['', Validators.required],
    fechaRegreso: ['', Validators.required],
    actividades: this.fb.array([], [Validators.required]),
    actividad: new FormControl('', [Validators.maxLength(100)]),
    tokenFichaIdentificacion: ['', Validators.required],
    centroSalida: ['', Validators.required],
    frecuenciaSalida: ['', Validators.required],
    horaRegreso: ['', Validators.required],
    nombreDirector: ['', Validators.required],
    otrosSalida: [''],
  },
  { validator: this.validarFechas } 
);
  
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PERMISO_SALIDA;
  adolescentes: FichaIdentificacionDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  esVisualizar: boolean = false;
  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private router: Router, private route: ActivatedRoute,
    private salidaService: PermisoSalidaService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private catalogoService: CatalogoService,
    private jerarquiaService: JerarquiaService,
    private _userService: UserService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private cdr: ChangeDetectorRef,
    private funcionarioService: FuncionarioService,
    private http: HttpClient,   
  ) {}

  ngOnInit(): void {
    this.estadoInicial()
    this.obtenerTokenDepartamento().then(() => {
      this.obtenerJerarquias();
    });
    this.informeSucesosForm.get('frecuenciaSalida')?.valueChanges.subscribe((value: any) => {
      this.mostrarOtrosSalida = value?.nemonico === 'FRECUENCIA_OTROS';
    });
    this.informeSucesosForm.get('fechaSalida')?.valueChanges.subscribe(() => {
      this.validarFechas(this.informeSucesosForm);
    });
    this.informeSucesosForm.get('fechaRegreso')?.valueChanges.subscribe(() => {
      this.validarFechas(this.informeSucesosForm);
    });
    this.obtenerFuncionario().then(() => {
      if (this.funcionarioActivo?.tokenIdentificadorDepartamento) {
          this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
              next: (adolescentesResponse) => {
                  if (adolescentesResponse.exito) {
                      this.adolescentes = adolescentesResponse.data;
                      const adolescenteSeleccionado = this.adolescentes.find(
                          (adolescente) => adolescente.tokenIdentificador === this.tokenID
                      );
                      if (adolescenteSeleccionado) {
                          this.informeSucesosForm.patchValue({
                              tokenFichaIdentificacion: adolescenteSeleccionado.idFichaIdentificacion,
                          });
                          this.personaControl.setValue(adolescenteSeleccionado);
                           this.informeSucesosForm.patchValue({
                          nroDocumento: adolescenteSeleccionado.numeroIdentificacion,
                          });
                      }
                      this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
                          startWith(''),
                          map((value) =>
                              typeof value === 'string' ? value : this.getNombreCompleto(value)
                          ),
                          map((name) =>
                              name ? this._filter(name) : this.adolescentes.slice()
                          )
                      );
                  } else {
                      console.error('Error al cargar adolescentes:', adolescentesResponse);
                  }
              },
              error: (error) => {
                  console.error('Error al cargar adolescentes:', error);
              },
          });
      } else {
          console.warn('El funcionario no tiene un tokenIdentificadorDepartamento definido.');
      }
  });
    forkJoin({
        tipoSalidaCatalog: this.getCatalogoTipoSalida(),
        frecuenciaSalidaCatalog: this.getCatalogoFrecuenciaSalida()
    }).subscribe({
        next: ({ tipoSalidaCatalog, frecuenciaSalidaCatalog }) => {
           
            if (!tipoSalidaCatalog || tipoSalidaCatalog.length === 0) {
            }
            if (!frecuenciaSalidaCatalog || frecuenciaSalidaCatalog.length === 0) {
            }

            this.catalogosTipoSalida = tipoSalidaCatalog || [];
            this.catalogosFrecuenciaSalida = frecuenciaSalidaCatalog || [];
            
            
            if (this.uuid_registro) {
              this.cargarDatosIniciales();
          }
        },
        error: (error) => {
            console.error('Error al cargar catálogos:', error);
        },
    });
    this.route.params.subscribe((params) => {
        this.tokenID = params['uuid'];
        this.uuid_adolescente = this.tokenID;
    });
    this.route.queryParams.subscribe((queryParams) => {
        this.uuid_fp = queryParams['uuid_fp'];
        this.uuid_registro = this.uuid_fp;
        const mode = queryParams['mode'];
        this.esVisualizar = mode === 'ver';
    });
    this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
            if (!response.exito) {
                return;
            }
            this.funcionarioActivo = response.data;
            console.log(this.funcionarioActivo);
            
            
            this.informeSucesosForm.patchValue({
                centroSalida: this.funcionarioActivo.departamento,
            });
          
            const nombreJerarquia = this.funcionarioActivo.departamento;
            // const dataJerarquia = { nombreJerarquia: nombreJerarquia }; 
            const dataJerarquia = { idDepartamento: this.funcionarioActivo?.idDepartamento };
            console.log(dataJerarquia);
            

            if (!nombreJerarquia || nombreJerarquia.trim() === "") {
                console.error(" Error: El nombre de la jerarquía es obligatorio antes de hacer la petición.");
                return;
            }
            console.log(dataJerarquia);
            
            this.salidaService.obtenerDirector(dataJerarquia).subscribe({
                next: (respuesta) => {
                  console.log(respuesta);
                  
                  const nombreCompleto = `${respuesta.data.nombres} ${respuesta.data.apellidos}`;
                    this.informeSucesosForm.patchValue({
                      nombreDirector: nombreCompleto
                  });
                },
                error: (err) => {
                    console.error(" Error al obtener el director:", err);
                }
            });
        },
        error: (error: any) => {
        }
    });
    this._userService.user$
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe((user: User) => {
            this.user = user;
            if (this.user?.name) {
                this.informeSucesosForm.patchValue({
                    usuarioSalida: this.user.name,
                });
                this.cdr.detectChanges();
            }
        });
        if (this.esVisualizar) {
            this.informeSucesosForm.disable();
        } else {
            this.informeSucesosForm.enable();
            
        }
}

  
  

  cargarDatosIniciales(): void {
      forkJoin([
        this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento),
        this.salidaService.obtenerPermisoSalidasPorTokenID(this.uuid_adolescente,this.nemonicoMenu),
      ]).subscribe(([adolescentesResponse, fugaResponse]) => {
        if (adolescentesResponse.exito) {
          this.adolescentes = adolescentesResponse.data;
          this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
            startWith(''),
            map((value) =>
              typeof value === 'string' ? value : this.getNombreCompleto(value)
            ),
            map((name) =>
              name ? this._filter(name) : this.adolescentes.slice()
            )
          );
        } else {
          console.error('Error al cargar adolescentes');
        }
        if (fugaResponse) {
          this.fuga = fugaResponse.data;
          console.log(this.fuga);
          
          if (this.fuga.actividades && this.fuga.actividades.length > 0) {
            this.actividadesForm.clear();
            this.fuga.actividades.forEach((actividad, index) => {
              const nuevaActividad = this.fb.group({
                indice: [index + 1], 
                descripcion: [actividad.descripcion, Validators.required],
              });
              this.actividadesForm.push(nuevaActividad);
            });
            this.actualizarNumeracion(); 
            this.dataSource = this.actividadesForm.controls.map((control) => control.value);
          }
          setTimeout(() => {
            if (this.fuga?.isComplete) {
                this.informeSucesosForm.disable();
            } else {
                this.informeSucesosForm.enable();
            }
            this.cdr.detectChanges(); 
        }, 0);
      
          this.informeSucesosForm.patchValue(fugaResponse.data);
          const seleccionado = this.adolescentes.find(
            (adolescente) =>
              adolescente.idFichaIdentificacion === Number(this.fuga.tokenFichaIdentificacion)
          );
          if (seleccionado) {
            this.personaControl.setValue(seleccionado);
            this.informeSucesosForm.patchValue({
            nroDocumento: seleccionado.numeroIdentificacion,
            });
          } else {
            console.error(
              'No se encontró ningún adolescente con idFichaIdentificacion igual a',
              this.fuga.tokenFichaIdentificacion
            );
          }
    
          if (this.fuga.fechaHoraSalida) {
            const fecha = new Date(this.fuga.fechaHoraSalida);
            let fechaSalida = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
            this.horaISO = fecha.toTimeString().split(' ')[0].slice(0, 5);
            this.informeSucesosForm.patchValue({
              fechaSalida: fechaSalida,
              horaSalida: this.horaISO,
            });
          }
          if (this.fuga.fechaHoraRegreso) {
            const fechaRegreso = new Date(this.fuga.fechaHoraRegreso);
            let fechaRegresoISO = new Date(fechaRegreso.getFullYear(), fechaRegreso.getMonth(), fechaRegreso.getDate());
            this.horaRegresoISO = fechaRegreso.toTimeString().split(' ')[0].slice(0, 5);
            this.informeSucesosForm.patchValue({
              fechaRegreso: fechaRegresoISO,
              horaRegreso: this.horaRegresoISO,
            });
          }
         
         
          const tipoSeleccionado = this.catalogosTipoSalida.find(
            (tipo) => tipo.tokenIdentificador === this.fuga.tipoSalida?.tokenIdentificador
          );
          const frecuenciaSeleccionado = this.catalogosFrecuenciaSalida.find(
            (tipo) => tipo.tokenIdentificador === this.fuga.frecuenciaSalida?.tokenIdentificador
          );
          this.informeSucesosForm.patchValue({
            tipoSalida: tipoSeleccionado || null,
            usuarioSalida: this.user.name,
            frecuenciaSalida: frecuenciaSeleccionado || null,
          });
        }
      });
    // }
   
  }
  
  cancelar(){
    if(this.uuid_adolescente && this.uuid_registro){
      this.param = this.uuid_registro
    }
   else{
    this.param= this.uuid_adolescente
   }
   this.router.navigate([`/gestion-adolescente/ficha-identificacion/crear-editar/salidas/${this.param}`]);
  }

  guardarFuga() {
    if (this.uuid_adolescente && this.uuid_registro) {
      this.param = this.uuid_registro;
    } else {
      this.param = this.uuid_adolescente;
    }
  
    this.informeSucesosForm.markAllAsTouched();
    const idFicha = this.informeSucesosForm.get('tokenFichaIdentificacion')?.value;
    this.fichaIdentificacionService
      .obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento)
      .subscribe({
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }
          const ficha = response.data.find(f => f.idFichaIdentificacion === idFicha);
          console.log(ficha);
          
          if (ficha.permisoTemporal === true) {
            this.dialogMensajeService.mensajeError('Ya existe un permiso de salida activo.');
            return;
          }
          this.route.queryParams.subscribe(() => {
            let ref = this.dialogMensajeService.mensajeConConfirmacion(
              'Se creará un permiso de salida',
              '¿Deseas continuar?'
            );
            ref.afterClosed().subscribe({
              next: (resp: 'confirmed' | 'cancelled') => {
                if (resp === 'confirmed') {
                  Object.assign(this.fuga, this.informeSucesosForm.value);
  
 // Actividades originales del backend (ya guardadas antes)
const originales = this.fuga.actividades?.map(a => a.descripcion.trim().toLowerCase()) || [];

// Actividades actuales del formulario (pueden venir duplicadas por error)
const actividadesSet = new Set<string>();
const actividadesFiltradas: ActividadSalidaDTO[] = [];

this.actividadesForm.controls.forEach(control => {
  const raw = control.get('descripcion')?.value;
  const desc = (raw || '').trim().toLowerCase();
  if (desc) actividadesFiltradas.push({ descripcion: desc });
});

// Verifica si el usuario repitió alguna actividad en el formulario
const repes = actividadesFiltradas.map(a => a.descripcion);
const repeticiones = repes.filter((item, i) => repes.indexOf(item) !== i);
if (repeticiones.length > 0) {
  this.dialogMensajeService.mensajeError(`No se puede guardar porque se repite la actividad "${repeticiones[0]}" más de una vez.`);
  return;
}

// Si las actividades no han cambiado (siguen siendo las mismas), no hacer nada
const actuales = actividadesFiltradas.map(a => a.descripcion);
const sonIguales = actuales.length === originales.length &&
  actuales.every(desc => originales.includes(desc));

if (!sonIguales) {
  // Guardar solo si hubo cambios
  this.fuga.actividades = actividadesFiltradas;
}
  
                  const fechaSeleccionada = this.informeSucesosForm.value.fechaSalida;
                  const horaSeleccionada = this.informeSucesosForm.value.horaSalida;
                  const fechaRegresoSeleccionada = this.informeSucesosForm.value.fechaRegreso;
                  const horaRegresoSeleccionada = this.informeSucesosForm.value.horaRegreso;
  
                  if (fechaSeleccionada && horaSeleccionada) {
                    const [hora, minutos] = horaSeleccionada.split(':');
                    const [horaRegreso, minutosRegreso] = horaRegresoSeleccionada.split(':');
                    const fechaHoraCombinada = new Date(fechaSeleccionada);
                    const fechaHoraCombinadaRegreso = new Date(fechaRegresoSeleccionada);
                    fechaHoraCombinada.setHours(Number(hora), Number(minutos));
                    fechaHoraCombinadaRegreso.setHours(Number(horaRegreso), Number(minutosRegreso));
                    this.fuga.fechaHoraSalida = fechaHoraCombinada.toISOString();
                    this.fuga.fechaHoraRegreso = fechaHoraCombinadaRegreso.toISOString();
                  }
  
                  this.salidaService.crearEditarPermisoSalidas(this.fuga, this.nemonicoMenu).subscribe({
                    next: (response: RespuestaPorDefecto<PermisoSalidaDTO>) => {
                      if (!response.exito) {
                        this.salidaService.checkError(response);
                        return;
                      }
                      this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                      this.router.navigate([
                        `/gestion-adolescente/ficha-identificacion/crear-editar/salidas/${this.param}`,
                      ]);
                    },
                    error: (error: any) => {
                      console.error('Error al guardar:', error);
                      this.salidaService.checkError(error);
                    },
                  });
                }
              },
            });
          });
        },
        error: () => {
          this.dialogMensajeService.mensajeError(
            'Ocurrió un error al verificar si ya existe un permiso activo.'
          );
        },
      });
  }
  
  
  
  
  private _filter(name: string): FichaIdentificacionDTO[] {
    const filterValue = name.toLowerCase();
    return this.adolescentes.filter(adolescente =>
      this.getNombreCompleto(adolescente).toLowerCase().includes(filterValue)
    );
  }
  
  
  cargarAdolescentes() {
      this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }
          this.adolescentes = response.data;  
          this.adolescentes.sort((a, b) => 
            a.apellidoPaterno.toLowerCase().localeCompare(b.apellidoPaterno.toLowerCase())
          );
      
          this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
            startWith(''),
            map(value => {
              return typeof value === 'string' ? value : this.getNombreCompleto(value);
            }),
            map(name => {
              const filtered = name ? this._filter(name) : this.adolescentes.slice();
              return filtered;
            })
          );
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      });
    }
  
  
  
  
    getNombreCompleto(adolescente: FichaIdentificacionDTO): string {
      return `${adolescente.nombres} ${adolescente.apellidoPaterno} ${adolescente.apellidoMaterno}`;
    }
    
  
    onAdolescenteSeleccionado(event: any): void {
      const seleccionado: FichaIdentificacionDTO = event.option.value;
      if (seleccionado) {
        this.personaControl.setValue(seleccionado);
        this.informeSucesosForm.patchValue({
          tokenFichaIdentificacion: seleccionado.idFichaIdentificacion, 
        });
      }
    }

 
  
  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select(); // Selecciona todo el texto
  }
  
  displayFn = (adolescente: FichaIdentificacionDTO | null): string => {
    return adolescente ? this.getNombreCompleto(adolescente) : '';
  };
  
  
  
  cambioAdolescente(adolescente: FichaIdentificacionDTO): void {
    if (adolescente) {
      this.informeSucesosForm.patchValue({
        tokenFichaIdentificacion: adolescente.idFichaIdentificacion,
        nroDocumento: adolescente.numeroIdentificacion,
      });
    }
  }

  validarHora(): Validators {
    return (control: FormControl): { [key: string]: any } | null => {
      const hora = control.value;
      if (!hora) {
        return null; 
      }

      const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
      if (!horaRegex.test(hora)) {
        return { invalid: true };
      }
      return null; 
    };
  }
  
  
  validarHoraInput(event: Event): void {
    const input = (event.target as HTMLInputElement);
    const hora = input.value;
    const horaRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
    const isValid = horaRegex.test(hora);
    if (!isValid) {
      this.informeSucesosForm.controls['horaSalida'].setErrors({ invalid: true });
    } else {
      this.informeSucesosForm.controls['horaSalida'].setErrors(null);
    }
  }

  // getCatalogoTipoSalida(): Observable<CatalogoDTO[]> {
  //   return this.catalogoService.obtenerHijos('TIPO_SALIDA', this.nemonicoMenu).pipe(
  //     map(responseCatalog => responseCatalog.data || []),
      
  //     catchError(error => {
  //       console.error('Error al obtener tipo salida:', error);
  //       return of([]); 
  //     })
  //   );
  // }

  getCatalogoTipoSalida(): Observable<CatalogoDTO[]> {
  return this.catalogoService.obtenerHijos('TIPO_SALIDA', this.nemonicoMenu).pipe(
    map(responseCatalog => {
      console.log('Respuesta cruda del catálogo TIPO_SALIDA:', responseCatalog);
      return responseCatalog.data || [];
    }),
    catchError(error => {
      console.error('Error al obtener tipo salida:', error);
      return of([]);
    })
  );
}
  
  getCatalogoMotivoSalida(): Observable<CatalogoDTO[]> {
    return this.catalogoService.obtenerHijos('MOTIVO_SALIDA', this.nemonicoMenu).pipe(
      map(responseCatalog => responseCatalog.data || []),
      catchError(error => {
        console.error('Error al obtener motivo salida:', error);
        return of([]); 
      })
    );
  }

  

  displayFnCentro(option: JerarquiaDTO): string {
    return option ? option.nombre : ''; 
  }
  

  getCentros(): Observable<JerarquiaDTO[]> {
    return this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre('CJDR', this.nemonicoMenu).pipe(
      map(responseCatalog => responseCatalog.data || []),
      catchError(error => {
        console.error('Error al obtener centros:', error);
        return of([]); 
      })
    );
  }
  get actividadesForm() {
    return this.informeSucesosForm.get('actividades') as FormArray;
  }

//   agregarActividad(): void {
//     const actividadControl = this.informeSucesosForm.get('actividad');
//     const actividadDescripcion = actividadControl?.value?.trim(); 
//     if (!actividadDescripcion) {
//         this.dialogMensajeService.mensajeError("Por favor, ingrese una actividad válida antes de agregarla.");
//         return; 
//     }
//     const nuevaActividad = this.fb.group({
//         indice: [this.actividadesForm.length + 1],
//         descripcion: [actividadDescripcion, Validators.required],
//     });
//     this.actividadesForm.push(nuevaActividad);
//     this.actualizarNumeracion();
//     this.dataSource = this.actividadesForm.controls.map((control) => control.value); 
//     actividadControl?.reset(); 
// }

agregarActividad(): void {
  const actividadControl = this.informeSucesosForm.get('actividad');
  const actividadDescripcion = actividadControl?.value?.trim();

  if (!actividadDescripcion) {
    this.dialogMensajeService.mensajeError("Por favor, ingrese una actividad válida antes de agregarla.");
    return;
  }

  // Verificar si ya existe una actividad con la misma descripción
  const yaExiste = this.actividadesForm.controls.some(control =>
    control.get('descripcion')?.value.trim().toLowerCase() === actividadDescripcion.toLowerCase()
  );

  if (yaExiste) {
    this.dialogMensajeService.mensajeError("Esta actividad ya ha sido agregada.");
    return;
  }

  const nuevaActividad = this.fb.group({
    indice: [this.actividadesForm.length + 1],
    descripcion: [actividadDescripcion, Validators.required],
  });

  this.actividadesForm.push(nuevaActividad);
  this.actualizarNumeracion();
  this.dataSource = this.actividadesForm.controls.map((control) => control.value);
  actividadControl?.reset();
}


  actualizarNumeracion(): void {
    this.actividadesForm.controls.forEach((control, index) => {
        control.get('indice')?.setValue(index + 1); 
    });
    this.dataSource = this.actividadesForm.controls.map(control => control.value);
}

 

  eliminarActividad(index: number): void {
    this.dialogMensajeService.mensajeConConfirmacion(
      'Confirmación',
      '¿Seguro que desea eliminar esta actividad?'
    ).afterClosed().subscribe((resp: "confirmed" | "cancelled") => {
      if (resp === "confirmed") {
        this.actividadesForm.removeAt(index);
        this.actualizarNumeracion();
        this.dataSource = this.actividadesForm.controls.map((control) => control.value);
      }
    });
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

  
  pruebaPdf() {
    this.loadImageAsBase64();
      Object.assign(this.fuga, this.informeSucesosForm.value);
      let request = new GeneracionPdfRequest();
      request.nemonico = etiquetasModel.FORMULARIO_REGISTRO_SALIDA;
      request.variables = {
        "[IMG_BASE64]": this.base64Image,
        "[TITULO-PLANTILLA]": 'Informe de permiso de salida',
        "[TITULO-INFORME]": 'Informe de permiso de salida',
        "[FECHA_REGISTRO]": this.formatFecha((new Date).toString()),
        "[HORA_REGISTRO]": this.formatHora((new Date).toString()),
        "[OBSERVACIONES]": this.fuga.observaciones,
        "[NRO-DOCUMENTO]": this.fuga.nroDocumento,
        "[HORA-SALIDA]": this.horaISO,
        "[FECHA-SALIDA]": this.fechaISO,
        "[USARIO-SALIDA]": this.fuga.usuarioSalida,
        "[LUGAR-SALIDA]": this.fuga.tipoSalidaLugar,
      }
      this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<string>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }
          const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
      const pwa = window.open(url);
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      });
    }

    getCatalogoFrecuenciaSalida(): Observable<CatalogoDTO[]> {
      return this.catalogoService.obtenerHijos('FRECUENCIA_SALIDA', this.nemonicoMenu).pipe(
        map(responseCatalog => responseCatalog.data || []),
        catchError(error => {
          console.error('Error al obtener motivo salida:', error);
          return of([]);
        })
      );
    }


    validarFechas(formGroup: AbstractControl): void {
      const fechaSalidaControl = formGroup.get('fechaSalida');
      const fechaRegresoControl = formGroup.get('fechaRegreso');
      if (!fechaSalidaControl || !fechaRegresoControl) return;
      const fechaSalida = fechaSalidaControl.value;
      const fechaRegreso = fechaRegresoControl.value;
      if (fechaSalida && fechaRegreso) {
        const inicio = new Date(fechaSalida).setHours(0, 0, 0, 0);
        const fin = new Date(fechaRegreso).setHours(0, 0, 0, 0);
    
        if (inicio > fin) {
          fechaRegresoControl.setErrors({ fechaInvalida: true });
        } else {
          fechaRegresoControl.setErrors(null); 
        }
      }
    }
    
    obtenerFuncionario(): Promise<void> {
      return new Promise((resolve, reject) => {
          this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
                  if (!response.exito) {
                      reject('No se pudo obtener el funcionario.');
                      return;
                  }
                  this.funcionarioActivo = response.data;
                  if (this.funcionarioActivo.tokenIdentificadorDepartamento) {
                      this.cargarAdolescentes();
                  } else {
                      console.warn('El funcionario no tiene un tokenIdentificadorDepartamento definido.');
                  }
                  resolve();
              },
              error: (error: any) => {
                  console.log('Hubo un problema al recuperar el funcionario activo. Inténtalo de nuevo.');
                  reject(error);
              }
          });
      });
    }

    actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
      if (event.value) {
        const fecha = event.value;
        this.informeSucesosForm.get(controlName).setValue(fecha);
      }
    }

    estadoInicial() {
      this.catalogoService.obtenerCatalogoPorNemonico(
      etiquetasModel.NEMONICO_ESTADO_SALIDA_ACTIVO,
      this.nemonicoMenu
          ).subscribe(
            {
              next: (respuesta: RespuestaPorDefecto<CatalogoDTO>) => {
                if (!respuesta.exito) {
                  this.catalogoService.checkError(respuesta);
                  return;
                }
                let estado = respuesta.data 
                console.log(estado);
                
                this.fuga.estadoEvento = estado
              },
              error: (error: any) => {
                this.catalogoService.checkError(error);
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
              console.log('funcionario', this.funcionarioActivo);
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
          console.log('jeraruiqas', data.data);
          this.jerarquia = data.data.filter(j => j.nombre === this.tokenJerarquia);
          if (this.jerarquia.length > 0) {
            this.fuga.centro = this.jerarquia[0];
          } else {
            this.fuga.centro = null; 
          }
        });
      }


  

}
