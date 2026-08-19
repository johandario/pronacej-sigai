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
import { catchError, forkJoin, map, Observable, of, startWith, Subject, combineLatestWith } from 'rxjs';
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
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { HttpClient } from '@angular/common/http';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { SancionDisciplinariaDTO } from 'app/core/model/both/ia/SancionDisciplinariaDTO.model';
import { SancionDisciplinariaService } from 'app/modules/administracion/services/sancionDisciplinaria.service';

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
  selector: 'app-crear-sanciones-disc',
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
    RouterLink,
    SubidaDeDocumentosComponent],
  templateUrl: './crear-sanciones-disc.component.html',
  styleUrl: './crear-sanciones-disc.component.scss',
   providers: [ 
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class CrearSancionesDiscComponent implements OnInit{

  isLoading: boolean =  false;
    tokenID: string;
    fuga: SancionDisciplinariaDTO = new SancionDisciplinariaDTO();
    estado: string = '';
    horaISO: string
    fechaISO: string
    adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
    personaControl = new FormControl();
    uuid_fp: any;
    catalogosTipoSalida: CatalogoDTO[] = [];
    catalogosMotivoSalida: CatalogoDTO[] = [];
    centrosOrigenFiltrado: Observable<JerarquiaDTO[]>;
    centrosOrigen: JerarquiaDTO[];
    dataSource: any[] = [];
  
    catalogosFrecuenciaSalida: CatalogoDTO[] = [];
    nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
    funcionarioActivo: FuncionarioDTO;
    uuid_adolescente: string;
    uuid_registro: string;
    param : string;
    isSaving: boolean = false;

    tokenJerarquia: any
    jerarquia: any;
    tokenFilter: any
    mostrarOtrosSalida: boolean = false;
    @Input() tiposDeDocumentosSistema: TipoDeDocumento[] = [];
    tipoCentro: string = 'CJDR';
    programaSeleccionado = false;
    listaProgramas: JerarquiaDTO[] = [];
    listaAmbientes: JerarquiaDTO[] = [];
    modoCreacion: boolean = false;

    informeSucesosForm = this.fb.group({
      fechaRegistro: ['', Validators.required],
      fechaFin: ['', Validators.required],
      fechaInicio: ['', Validators.required],
      nroResolucion: ['', Validators.required],
      falta: ['', Validators.required],
      sancion: ['', Validators.required],
      observacion: ['', Validators.required],
     programa: ['', Validators.required],
      fichaIdentificacion: ['', Validators.required],
      ambiente: [''],
      tipificacionFalta: ['', Validators.required],
      motivo: ['', Validators.required],
      horaRegistro: ['', Validators.required],
    
      
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
      private sancionService: SancionDisciplinariaService,
      private fb: UntypedFormBuilder,
      private dialogMensajeService: DialogMensajeService,
      private fichaIdentificacionService: FichaIdentificacionService,
      private catalogoService: CatalogoService,
      private jerarquiaService: JerarquiaService,
      private cdr: ChangeDetectorRef,
      private funcionarioService: FuncionarioService,
      private http: HttpClient,   
    ) {}


  ngOnInit(): void {
    this.verificarTiposDeDocumentos();
  // 1. Detectar segmentos y modo
  const urlSegmentos = this.router.url.split('/');
  const ultimoSegmento = urlSegmentos[urlSegmentos.length - 1];

  this.route.params.subscribe((params) => {
    this.tokenID = params['uuid']; // puede ser token de adolescente o sanción
  });

  this.route.queryParams.subscribe((queryParams) => {
    this.uuid_fp = queryParams['uuid_fp'];
    const mode = queryParams['mode'];

    this.esVisualizar = mode === 'ver' || ultimoSegmento === 'ver';
    this.modoCreacion = ultimoSegmento === 'crear'; // Detectamos modo crear por URL
  });

  this.obtenerTokenDepartamento().then(() => {
    this.obtenerJerarquias();

    forkJoin({
      motivoCatalog: this.getCatalogoMotivoSancion(),
      tipificacionCatalog: this.getCatalogoTipificacion(),
      frecuenciaCatalog: this.getCatalogoFrecuenciaSalida(),
    }).subscribe(({ motivoCatalog, tipificacionCatalog, frecuenciaCatalog }) => {
      this.catalogosMotivoSalida = motivoCatalog || [];
      this.catalogosTipoSalida = tipificacionCatalog || [];
      this.catalogosFrecuenciaSalida = frecuenciaCatalog || [];

      this.fichaIdentificacionService
        .obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento)
        .subscribe((response) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError('Error al cargar la lista de adolescentes.');
            return;
          }

          this.adolescentes = response.data;
          this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
            startWith(''),
            map((value) => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
            map((name) => (name ? this._filter(name) : this.adolescentes.slice()))
          );

          // ✅ MODO CREAR
          if (this.modoCreacion) {
            const seleccionado = this.adolescentes.find(a => a.tokenIdentificador === this.tokenID);
            if (seleccionado) {
              this.personaControl.setValue(seleccionado);
              this.informeSucesosForm.patchValue({
                fichaIdentificacion: seleccionado.idFichaIdentificacion,
                nroDocumento: seleccionado.numeroIdentificacion,
              });
            }
             const now = new Date();
            const horaActual = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');
            this.informeSucesosForm.patchValue({
              fechaRegistro: now,
              horaRegistro: horaActual,
            });
          }

          // ✅ MODO EDITAR
          if (!this.modoCreacion && this.tokenID) {
            this.sancionService
              .obtenerSancionPorToken(this.tokenID, this.nemonicoMenu)
              .subscribe((sancionResponse) => {
                if (!sancionResponse.exito || !sancionResponse.data) {
                  this.dialogMensajeService.mensajeError('No se pudo cargar la sanción.');
                  return;
                }

                this.fuga = sancionResponse.data;

                const seleccionadoEditar = this.adolescentes.find(
                  (a) => a.idFichaIdentificacion === Number(this.fuga.fichaIdentificacion?.idFichaIdentificacion)
                );

                if (seleccionadoEditar) {
                  this.personaControl.setValue(seleccionadoEditar);
                  this.informeSucesosForm.patchValue({
                    fichaIdentificacion: seleccionadoEditar.idFichaIdentificacion,
                    nroDocumento: seleccionadoEditar.numeroIdentificacion,
                  });
                }

                if (this.fuga.programa?.tokenIdentificador) {
                  this.cargarProgramas(this.fuga.centro?.tokenIdentificador);
                  setTimeout(() => {
                    this.cargarAmbientes(this.fuga.programa.tokenIdentificador);
                    this.programaSeleccionado = true;
                    this.informeSucesosForm.patchValue({
                      programa: this.fuga.programa.tokenIdentificador,
                      ambiente: this.fuga.ambiente?.tokenIdentificador || null,
                    });
                  }, 300);
                }

                this.informeSucesosForm.patchValue({
                  fechaInicio: this.fuga.fechaInicio ? new Date(this.fuga.fechaInicio) : '',
                  fechaFin: this.fuga.fechaFin ? new Date(this.fuga.fechaFin) : '',
                  fechaRegistro: this.fuga.fechaRegistro ? new Date(this.fuga.fechaRegistro) : '',
                  horaRegistro: this.fuga.fechaRegistro
                    ? new Date(this.fuga.fechaRegistro).toTimeString().split(' ')[0].slice(0, 5)
                    : '',
                  // motivoSancion: this.catalogosMotivoSalida.find(
                  //   (m) => m.tokenIdentificador === this.fuga.motivoSancion?.tokenIdentificador
                  // ),
                  tipificacionFalta: this.catalogosTipoSalida.find(
                    (t) => t.tokenIdentificador === this.fuga.tipificacionFalta?.tokenIdentificador
                  ),
                  nroResolucion: this.fuga.nroResolucion,
                  falta: this.fuga.falta,
                  sancion: this.fuga.sancion,
                  observacion: this.fuga.observacion,
                  motivo:this.fuga.motivo
                });
              });
          }

          if (this.esVisualizar) {
            this.informeSucesosForm.disable();
          } else {
            this.informeSucesosForm.enable();
          }
        });
    });
  });
}
   

  cargarCatalogosYProgramas() {
  forkJoin({
    motivoCatalog: this.getCatalogoMotivoSancion(),
    tipificacionCatalog: this.getCatalogoTipificacion(),
    frecuenciaCatalog: this.getCatalogoFrecuenciaSalida(),
  }).subscribe({
    next: ({ motivoCatalog, tipificacionCatalog, frecuenciaCatalog }) => {
      this.catalogosMotivoSalida = motivoCatalog || [];
      this.catalogosTipoSalida = tipificacionCatalog || [];
      this.catalogosFrecuenciaSalida = frecuenciaCatalog || [];

      if (this.fuga?.centro) {
        this.tipoCentro = this.detectarTipoCentro(this.fuga.centro);
        if (this.debeVerProgramaYAmbiente()) {
          this.cargarProgramas(this.fuga.centro.tokenIdentificador);
        }
      } else {
        this.obtenerCentroYProgramas();
      }
      console.log(this.uuid_fp);
      
      if (this.uuid_registro) {
        this.cargarDatosIniciales();
      }
    },
    error: (error) => {
      console.error('Error al cargar catálogos:', error);
    },
  });
}
    obtenerCentroYProgramas() {
  this.jerarquiaService.obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu).subscribe({
    next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
      if (!respuesta.exito) {
        this.jerarquiaService.checkError(respuesta);
        return;
      }
      this.fuga.centro = respuesta.data;
      this.tipoCentro = this.detectarTipoCentro(this.fuga.centro);
      if (this.debeVerProgramaYAmbiente()) {
        this.cargarProgramas(this.fuga.centro.tokenIdentificador);
      }
    },
    error: (error: any) => {
      this.jerarquiaService.checkError(error);
    },
  });
}

detectarTipoCentro(centro: JerarquiaDTO): string {
  if (centro?.jerarquiaPadre?.nemonico === 'SOA') {
    return 'SOA';
  } else if (
    centro?.nemonico === 'UAPISE' ||
    (centro?.nombre && centro.nombre.includes('UAPISE')) ||
    centro?.jerarquiaPadre?.nemonico === 'UAPISE'
  ) {
    return 'UAPICE';
  }
  return 'CJDR';
}

cargarProgramas(tokenCentro: string) {
  this.jerarquiaService.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenCentro).subscribe({
    next: (respuesta: RespuestaPorDefecto<JerarquiaDTO[]>) => {
      if (respuesta.exito) {
        this.listaProgramas = respuesta.data;
      }
    },
    error: (error) => this.jerarquiaService.checkError(error),
  });
}
  
    
  cargarDatosIniciales(): void {
  forkJoin([
    this.fichaIdentificacionService.obtenerNombresFichas(
      this.nemonicoMenu,
      this.funcionarioActivo.tokenIdentificadorDepartamento
    ),
    this.sancionService.obtenerSancionPorToken(
      this.tokenID,
      this.nemonicoMenu
    ),
  ]).subscribe(([adolescentesResponse, sancionResponse]) => {
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

    if (sancionResponse?.exito && sancionResponse.data) {
      this.fuga = sancionResponse.data;
      console.log(this.fuga);
      

      const seleccionado = this.adolescentes.find(
        (adolescente) =>
          adolescente.idFichaIdentificacion ===
          Number(this.fuga.fichaIdentificacion?.idFichaIdentificacion)
      );

      if (seleccionado) {
        this.personaControl.setValue(seleccionado);
        this.informeSucesosForm.patchValue({
          fichaIdentificacion: seleccionado.idFichaIdentificacion,
        });
      }

      this.informeSucesosForm.patchValue({
        ...this.fuga,
        motivo:this.fuga.motivo,
        tipificacionFalta: this.catalogosTipoSalida.find(
          (c) => c.tokenIdentificador === this.fuga.tipificacionFalta?.tokenIdentificador
        ),
      });

      if (this.fuga.fechaRegistro) {
        const fecha = new Date(this.fuga.fechaRegistro);
        const horaStr = fecha.toTimeString().split(' ')[0].slice(0, 5);
        this.informeSucesosForm.patchValue({
          fechaRegistro: fecha,
          horaRegistro: horaStr,
        });
      }
    }
  });
}

    cancelar() {
      const seleccionado: FichaIdentificacionDTO = this.personaControl.value;
      if (seleccionado && seleccionado.tokenIdentificador) {
        this.router.navigate([
          `/gestion-adolescente/ficha-identificacion/crear-editar/sancionesDisciplinarias/${seleccionado.tokenIdentificador}`,
        ]);
      } else {
        this.dialogMensajeService.mensajeError('No se pudo determinar la ficha del adolescente.');
      }
    }
   
  
//   guardarFuga() {
//   this.informeSucesosForm.markAllAsTouched();
//   if (this.informeSucesosForm.invalid) {
//     this.dialogMensajeService.mensajeError('Por favor, completa todos los campos obligatorios antes de guardar.');
//     return;
//   }

//  const seleccionado: FichaIdentificacionDTO = this.personaControl.value;
//  this.param = seleccionado.tokenIdentificador;
//  if (!seleccionado || !seleccionado.tokenIdentificador) {
//   this.dialogMensajeService.mensajeError('No se encontró la ficha de identificación.');
//   return;
// }

//   this.fichaIdentificacionService
//     .obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento)
//     .subscribe({
//       next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {
//         if (!response.exito) {
//           this.dialogMensajeService.mensajeError('Hubo un problema al recuperar los registros.');
//           return;
//         }

     
//         const ref = this.dialogMensajeService.mensajeConConfirmacion(
//           'Se creará una sanción disciplinaria',
//           '¿Deseas continuar?'
//         );

//         ref.afterClosed().subscribe({
//           next: (resp: 'confirmed' | 'cancelled') => {
//             if (resp !== 'confirmed') return;

//             const formValue = this.informeSucesosForm.getRawValue();

//             // Combinar fechaRegistro + horaRegistro
//             const fecha = formValue.fechaRegistro;
//             const hora = formValue.horaRegistro;
//             const [hh, mm] = hora.split(':');
//             const fechaRegistroCompleta = new Date(fecha);
//             fechaRegistroCompleta.setHours(+hh);
//             fechaRegistroCompleta.setMinutes(+mm);
//             fechaRegistroCompleta.setSeconds(0);
//             fechaRegistroCompleta.setMilliseconds(0);

//             // Construir objeto final
//             this.fuga = {
//               ...this.fuga,
//               ...formValue,
//               fechaRegistro: fechaRegistroCompleta,
//               esEdicion: !!this.fuga?.tokenIdentificador,
//               fichaIdentificacion: seleccionado,
//               centro: this.fuga.centro,
//               programa: this.listaProgramas.find(p => p.tokenIdentificador === formValue.programa) || null,
//               ambiente: this.listaAmbientes.find(a => a.tokenIdentificador === formValue.ambiente) || null,
//               motivo: formValue.motivo,
//               tipificacionFalta: this.catalogosTipoSalida.find(c => c.tokenIdentificador === (formValue.tipificacionFalta?.tokenIdentificador || formValue.tipificacionFalta)) || null,
//             };

//             console.log(this.fuga);
            
//             this.sancionService.crearSancion(this.fuga, this.nemonicoMenu).subscribe({
//               next: (response: RespuestaPorDefecto<SancionDisciplinariaDTO>) => {
//                 if (!response.exito) {
//                   this.sancionService.checkError(response);
//                   return;
//                 }
//                 this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
//                 this.router.navigate([
//                   `/gestion-adolescente/ficha-identificacion/crear-editar/sancionesDisciplinarias/${this.param}`,
//                 ]);
//               },
//               error: (error: any) => {
//                 console.error('Error al guardar:', error);
//                 this.sancionService.checkError(error);
//               },
//             });
//           }
//         });
//       },
//       error: () => {
//         this.dialogMensajeService.mensajeError(
//           'Ocurrió un error al verificar si ya existe un permiso activo.'
//         );
//       },
//     });
// }

    guardarFuga() {
  if (this.isSaving) return; // evita clicks múltiples

  this.informeSucesosForm.markAllAsTouched();
  if (this.informeSucesosForm.invalid) {
    this.dialogMensajeService.mensajeError('Por favor, completa todos los campos obligatorios antes de guardar.');
    return;
  }

  const seleccionado: FichaIdentificacionDTO = this.personaControl.value;
  this.param = seleccionado.tokenIdentificador;
  if (!seleccionado || !seleccionado.tokenIdentificador) {
    this.dialogMensajeService.mensajeError('No se encontró la ficha de identificación.');
    return;
  }

  this.fichaIdentificacionService
    .obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento)
    .subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError('Hubo un problema al recuperar los registros.');
          return;
        }

        const ref = this.dialogMensajeService.mensajeConConfirmacion(
          'Se creará una sanción disciplinaria',
          '¿Deseas continuar?'
        );

        ref.afterClosed().subscribe({
          next: (resp: 'confirmed' | 'cancelled') => {
            if (resp !== 'confirmed') return;

            this.isSaving = true; // desactiva guardado

            const formValue = this.informeSucesosForm.getRawValue();
            const fecha = formValue.fechaRegistro;
            const hora = formValue.horaRegistro;
            const [hh, mm] = hora.split(':');
            const fechaRegistroCompleta = new Date(fecha);
            fechaRegistroCompleta.setHours(+hh);
            fechaRegistroCompleta.setMinutes(+mm);
            fechaRegistroCompleta.setSeconds(0);
            fechaRegistroCompleta.setMilliseconds(0);

            this.fuga = {
              ...this.fuga,
              ...formValue,
              fechaRegistro: fechaRegistroCompleta,
              esEdicion: !!this.fuga?.tokenIdentificador,
              fichaIdentificacion: seleccionado,
              centro: this.fuga.centro,
              programa: this.listaProgramas.find(p => p.tokenIdentificador === formValue.programa) || null,
              ambiente: this.listaAmbientes.find(a => a.tokenIdentificador === formValue.ambiente) || null,
              motivo: formValue.motivo,
              tipificacionFalta: this.catalogosTipoSalida.find(c => c.tokenIdentificador === (formValue.tipificacionFalta?.tokenIdentificador || formValue.tipificacionFalta)) || null,
            };

            this.sancionService.crearSancion(this.fuga, this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<SancionDisciplinariaDTO>) => {
                this.isSaving = false;
                if (!response.exito) {
                  this.sancionService.checkError(response);
                  return;
                }
                this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                this.router.navigate([
                  `/gestion-adolescente/ficha-identificacion/crear-editar/sancionesDisciplinarias/${this.param}`,
                ]);
              },
              error: (error: any) => {
                this.isSaving = false;
                console.error('Error al guardar:', error);
                this.sancionService.checkError(error);
              },
            });
          },
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
  
    getCatalogoTipificacion(): Observable<CatalogoDTO[]> {
    return this.catalogoService.obtenerHijos('TIPIFICACION_FALTA', this.nemonicoMenu).pipe(
      map(responseCatalog => {
        console.log('Respuesta cruda del catálogo TIPIFICACION_FALTA:', responseCatalog);
        return responseCatalog.data || [];
      }),
      catchError(error => {
        console.error('Error al obtener tipificacion:', error);
        return of([]);
      })
    );
  }
    
    getCatalogoMotivoSancion(): Observable<CatalogoDTO[]> {
      return this.catalogoService.obtenerHijos('MOTIVO_SANCION', this.nemonicoMenu).pipe(
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

    debeVerProgramaYAmbiente(): boolean {
    return this.tipoCentro === 'CJDR';
    }


    observadorCambioEnCampo(campo: string, evento: any) {
    if (campo === 'programa') {
      const tokenPrograma = evento.value;
      if (tokenPrograma && tokenPrograma !== '0') {
        this.cargarAmbientes(tokenPrograma);
        this.programaSeleccionado = true;
      } else {
        this.programaSeleccionado = false;
      }
      this.informeSucesosForm.get('ambiente').setValue('0');
    }
  }

  cargarAmbientes(tokenPrograma: string) {
    this.jerarquiaService.obtenerJerarquiasPorTokenPadre('', this.nemonicoMenu, tokenPrograma)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (respuesta.exito) {
            this.listaAmbientes = respuesta.data;
          }
        },
        error: (error) => this.jerarquiaService.checkError(error)
      });
  }


  onFechaManual(event: any, controlName: string): void {
  const valorIngresado = event.target.value;
  if (valorIngresado) {
    const partes = valorIngresado.split('/');
    if (partes.length === 3) {
      const fechaConvertida = new Date(`${partes[2]}-${partes[1]}-${partes[0]}`);
      if (!isNaN(fechaConvertida.getTime())) {
        this.informeSucesosForm.patchValue({ [controlName]: fechaConvertida });
        this.informeSucesosForm.get(controlName)?.updateValueAndValidity();
        console.log(`Fecha válida (${controlName}):`, fechaConvertida);
      } else {
        console.warn(`Fecha inválida (${controlName})`);
        this.informeSucesosForm.get(controlName)?.setErrors({ invalid: true });
      }
    }
  }
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
          this.tipoCentro = this.detectarTipoCentro(this.fuga.centro);
          if (this.debeVerProgramaYAmbiente()) {
            this.cargarProgramas(this.fuga.centro.tokenIdentificador);
          }

        } else {
          this.fuga.centro = null;
        }
      });
      }

    validarFechas(formGroup: AbstractControl): void {
      const fechaInicioControl = formGroup.get('fechaInicio');
      const fechaFinControl = formGroup.get('fechaFin');
      if (!fechaInicioControl || !fechaFinControl) return;
      const fechaInicio = fechaInicioControl.value;
      const fechaFin = fechaFinControl.value;
      if (fechaInicio && fechaFin) {
        const inicio = new Date(fechaInicio).setHours(0, 0, 0, 0);
        const fin = new Date(fechaFin).setHours(0, 0, 0, 0);
        if (inicio > fin) {
          fechaFinControl.setErrors({ fechaInvalida: true });
        } else {
          fechaFinControl.setErrors(null); 
        }
      }
    }


    verificarTiposDeDocumentos() {
        if (this.tiposDeDocumentosSistema.length == 0) {
          this.catalogoService.obtenerHijos(
            etiquetasModel.NEMONICO_TIPO_DOCUMENTO_SANCIONES,
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
}
