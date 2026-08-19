import { Component,Input,OnInit } from '@angular/core';
import {  FormControl, FormsModule, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter,DateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { catchError, forkJoin, map, Observable, of, startWith } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { SalidaService } from '../../salida.service';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service'; 
import { RegistroSalidaDTO } from 'app/core/model/both/salida/RegistroSalidaDTO.model';
import { PdfService } from 'app/core/services/pdf.service';
import { TrasladoService } from 'app/modules/flujo-trabajo/traslado/traslado.service';
import { GestionFugaService } from 'app/modules/flujo-trabajo/gestion-fuga/gestion-fuga.service';
import { MatRadioModule } from '@angular/material/radio';
import { CUSTOM_DATE_FORMATS, FuncionesUtils,CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { PermisoSalidaService } from '../permiso-salida.service';
import { ActaExternamientoService } from 'app/modules/administracion/services/actaExternamiento.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { ActaExternamientoDTO } from 'app/core/model/both/ia/actaExternamientoDTO.model';
import { HttpClient } from '@angular/common/http';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { ChangeDetectorRef } from '@angular/core';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { PermisoSalidaDTO } from 'app/core/model/both/salida/PermisoSalidaDTO.model';
import { InformeFinalAbiertoDTO } from 'app/core/model/both/informeFinalAbiertoDTO.model';
import { InformeFinalAbiertoService } from 'app/modules/seguridad/services/informeFinalAbierto.service';


@Component({
  selector: 'app-crear-permiso-salida',
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
    MatRadioModule
  ],
  templateUrl: './crear-permiso-salida.component.html',
  styleUrl: './crear-permiso-salida.component.scss',
  providers: [ 
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class CrearPermisoSalidaComponent implements OnInit{
  isLoading: boolean =  false;
  tokenID: string;
  fuga: RegistroSalidaDTO = new RegistroSalidaDTO();
  horaISO: string
  fechaISO: string
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();
  uuid_fp: number;
  uuid_adolescente: string;
  catalogosMotivoSalida: CatalogoDTO[] = [];
  dataSource: any[] = [];
  listaProcesos: any;
  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;
  motivoSalidaSeleccionado: string | null = null;
  registrosDataSource = new MatTableDataSource<any>([]);
  displayedColumns: string[] = ['noDocumento', 'fecha', 'centro','observacion','select'];
  selectedRegistro: any = null;
  dataSourceTraslado: any[] = [];
  dataSourcePermisoSalida: any[] = [];
  dataSourceExternamiento: any[] = [];
  dataSourceInformeFinal: any[] = [];
  actualDate: any;
  centro: JerarquiaDTO;
  base64Image: string | null = null;
  estadoEnCursoPermisosSalida: any
  estadoEnProcesoTraslado: any
  informeSucesosForm = this.fb.group({
    fechaSalida: ['', Validators.required],
    horaSalida: ['', Validators.required],
    observaciones: ['', Validators.required],
    motivoSalida: ['', Validators.required],
    tokenFichaIdentificacion: ['', Validators.required],
    numeroIdentificacion: ['', Validators.required],

  });
  data: PermisoSalidaDTO = new PermisoSalidaDTO();
  listaInformes: InformeFinalAbiertoDTO[] = [];
  
  
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_REGISTRO_SALIDA;
  adolescentes: FichaIdentificacionDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  esVisualizar: boolean = false;
  nroDocumento: string
  paginacionRequest: PaginacionRequest = new PaginacionRequest();
  paginacion: Paginacion = new Paginacion();
  listaActas: ActaExternamientoDTO[] = [];
  funcionarioActivo: FuncionarioDTO;
  
  

  constructor(
    private router: Router, private route: ActivatedRoute,
    private salidaService: SalidaService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private catalogoService: CatalogoService,
    private fugaService: GestionFugaService,
    private trasladoService: TrasladoService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private permisoSalida: PermisoSalidaService,
    private actaService: ActaExternamientoService,
    private http: HttpClient, 
    private funcionarioService: FuncionarioService,
    private cdr: ChangeDetectorRef,
    private jerarquiaService: JerarquiaService,
    private informeFinalAbiertoService: InformeFinalAbiertoService,
  ) {}

  ngOnInit(): void {
    this.estadoEnCurso()
    this.estadoEnProceso()
    this.obtenerJerarquia();
    //  this.obtenerFuncionario().then(() => {
    //     this.obtenerJerarquias();
    //   });
    this.route.queryParams.subscribe((params) => {
        this.tokenID = params['ID'];
        const mode = params['mode'];
        this.esVisualizar = mode === 'ver';
       
        this.obtenerFuncionario().then(() => {
            // this.cargarCentro(); 
             this.getCatalogoMotivoSalida().subscribe({
            next: (motivoSalidaCatalog) => {
            
                this.catalogosMotivoSalida = motivoSalidaCatalog || [];
            },
            error: (error) => {
                console.error('Error al obtener los catálogos:', error);
            }
        });

            if (!this.tokenID) {
                this.actualDate = new Date();
                const currentHour = this.actualDate.getHours().toString().padStart(2, '0'); 
                const currentMinutes = this.actualDate.getMinutes().toString().padStart(2, '0'); 
                const formattedTime = `${currentHour}:${currentMinutes}`; 
                this.informeSucesosForm.patchValue({
                    fechaSalida: this.actualDate,
                    horaSalida: formattedTime,
                });

            } else {
                forkJoin({
                    adolescentesData: this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo?.tokenIdentificadorDepartamento),
                    fugaData: this.salidaService.obtenerSalidasPorTokenID(this.tokenID, this.nemonicoMenu),
                }).subscribe({
                    next: ({ adolescentesData, fugaData }) => {
                        if (adolescentesData.exito) {
                            this.adolescentes = adolescentesData.data;
                            
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
                            console.error('Error al cargar adolescentes:', adolescentesData);
                        }
                        if (fugaData) {
                            this.fuga = fugaData.data;
                            console.log(this.fuga);
                            
                            this.nroDocumento = this.fuga.nroDocumento;
                            this.informeSucesosForm.patchValue(fugaData.data);
                            const seleccionado = this.adolescentes.find(
                                (adolescente) => adolescente.idFichaIdentificacion === Number(this.fuga.tokenFichaIdentificacion)
                            );
                            if (seleccionado) {
                                this.personaControl.setValue(seleccionado);
                                this.informeSucesosForm.patchValue({
                                  numeroIdentificacion: seleccionado.numeroIdentificacion
                                  ,
                              });
                            } else {
                                console.warn('No se encontró el adolescente:', this.fuga.tokenFichaIdentificacion);
                            }
                            if (this.fuga.fechaHoraSalida) {
                                let fechaHoraString: string;
                                if (this.fuga.fechaHoraSalida instanceof Date) {
                                    fechaHoraString = this.fuga.fechaHoraSalida.toISOString();
                                } else {
                                    fechaHoraString = this.fuga.fechaHoraSalida;
                                }
                                const fechaUTC = new Date(fechaHoraString);
                                const fechaLocal = new Date(
                                    fechaUTC.getUTCFullYear(),
                                    fechaUTC.getUTCMonth(),
                                    fechaUTC.getUTCDate()
                                );
                                this.informeSucesosForm.patchValue({ fechaSalida: fechaLocal });

                                const hora = fechaHoraString.split('T')[1].substring(0, 5);
                                this.informeSucesosForm.patchValue({ horaSalida: hora });
                            }
                            setTimeout(() => {
                              console.log(this.catalogosMotivoSalida);
                              
                              const motivoSeleccionado = this.catalogosMotivoSalida.find(
                                (motivo) => motivo.tokenIdentificador === this.fuga.motivoSalida?.tokenIdentificador
                            );
                            if (motivoSeleccionado) {
                              console.log(motivoSeleccionado);
                              
                                this.informeSucesosForm.controls['motivoSalida'].setValue(motivoSeleccionado);
                                this.motivoSalidaSeleccionado = motivoSeleccionado.nemonico;
                                switch (this.motivoSalidaSeleccionado) {
                                    case 'SALIDA_TEMPORAL':
                                        if (this.fuga.permisoSalida) {
                                            // this.uuid_fp = this.fuga.tokenFichaIdentificacion;
                                            const permiso = this.fuga.permisoSalida;
                                            const registro = {
                                                noDocumento: permiso.nroDocumento || permiso.idRegistroSalida || 'Sin documento',
                                                fecha: permiso.fechaHoraSalida ? new Date(permiso.fechaHoraSalida) : 'Sin fecha',
                                                id: permiso.idRegistroSalida,
                                                tokenIdentificador: permiso.tokenIdentificador,
                                                observacion: permiso.observaciones || 'Sin observación',
                                            };
                                            this.registrosDataSource.data = [registro];
                                        }
                                        break;
                                        case 'SALIDA_FUGA':
                                          if (this.fuga.eventoFuga) {
                                              const evento = this.fuga.eventoFuga; 
                                              const registro = {
                                                fecha: evento.fechaFuga ? new Date(evento.fechaFuga) : 'Sin fecha',
                                                id: evento.idFuga,
                                                tokenIdentificador: evento.tokenIdentificador,
                                                observacion: evento.asunto || 'Sin observación',
                                                noDocumento: evento.numFuga|| evento.idFuga || 'Sin documento',
                                              };
                                              this.registrosDataSource.data = [registro];
                                          }
                                        break;
                                        case 'SALIDA_TRASLADO':
                                          if (this.fuga.traslado) {
                                              const evento = this.fuga.traslado; 
                                              const registro = {
                                                fecha: evento.instanciaProcesoDTO
                                                ?.fechaCreacion ? new Date(evento.instanciaProcesoDTO
                                                  .fechaCreacion) : null, 
                                                id: evento.idTraslado,
                                                tokenIdentificador: evento.tokenIdentificador,
                                                observacion: evento.analisis || 'Sin observación',
                                                noDocumento: evento.numTraslado|| evento.idTraslado || 'Sin documento',
                                                centro: evento.centroDestino?.nombre|| 'Sin centro'
                                              };
                                              this.registrosDataSource.data = [registro];
                                        }
                                        break;
                                        case 'SALIDA_EXTERNAMIENTO':
                                          if (this.fuga.externamiento) {
                                              const evento = this.fuga.externamiento; 
                                              const registro = {
                                                fecha: evento.fechaRegistro ? new Date(evento.fechaRegistro) : 'Sin fecha',
                                                tokenIdentificador: evento.tokenIdentificador,
                                                observacion: evento.observaciones || 'Sin observación',
                                                noDocumento: evento.numeroDocumento || 'Sin documento',
                                              };
                                              this.registrosDataSource.data = [registro];
                                        }
                                        break;
                                        case 'SALIDA_INFORME_FINAL':
                                          if (this.fuga.informeFinalAbierto) {
                                              const evento = this.fuga.informeFinalAbierto; 
                                              const registro = {
                                                fecha: evento.fechaCreacion ? new Date(evento.fechaCreacion) : 'Sin fecha',
                                                tokenIdentificador: evento.tokenIdentificador,
                                                observacion: evento.conclusionesRecomendaciones || 'Sin observación',
                                                noDocumento: evento.idInformeFinalAbierto || 'Sin documento',
                                              };
                                              this.registrosDataSource.data = [registro];
                                        }
                                        break;

                                    
                                }
                            
                                this.displayedColumns = ['noDocumento', 'fecha', 'observacion'];
                                if (this.motivoSalidaSeleccionado === 'SALIDA_TRASLADO') {
                                    this.displayedColumns.push('centro');
                                }
                                this.displayedColumns.push('select');
                            
                                this.cdr.detectChanges();
                            }
                            
                              else {
                                  console.warn("⚠️ No se encontró el motivo de salida en catalogosMotivoSalida.");
                              }
                          }


                          , 100); 
                            if (this.esVisualizar) {
                                this.informeSucesosForm.disable();
                            } else {
                                this.informeSucesosForm.enable();
                                
                            }
                        }
                    },
                    error: (error) => {
                        console.error('Error al cargar datos:', error);
                    },
                });
            }
        });
    });
}


  
  cancelar() {
    this.router.navigate([`/salida/registro-salida`]);
  }
  
  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    const tokenFichaIdentificacion = this.informeSucesosForm.get('tokenFichaIdentificacion')?.value;
    if (!tokenFichaIdentificacion) {
      this.dialogMensajeService.mensajeError('Por favor, selecciona un adolescente válido.');
      return;
    }
    if (this.personaControl.invalid) {
      this.dialogMensajeService.mensajeError('Por favor, selecciona un adolescente válido.');
      return;
    }
    if (!this.selectedRegistro) {
      this.dialogMensajeService.mensajeError('Debe seleccionar un registro antes de continuar.');
      return;
    }
    this.route.queryParams.subscribe(() => {
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de salida',
        "¿Deseas continuar?"
      );
      ref.afterClosed().subscribe({
        next: (resp: "confirmed" | "cancelled") => {
          if (resp === "confirmed") {
            Object.assign(this.fuga, this.informeSucesosForm.value);
            const fechaSeleccionada = this.informeSucesosForm.value.fechaSalida;
            const horaSeleccionada = this.informeSucesosForm.value.horaSalida;
            if (fechaSeleccionada && horaSeleccionada) {
              const [hora, minutos] = horaSeleccionada.split(':');
              const fechaHoraCombinada = new Date(fechaSeleccionada);
              fechaHoraCombinada.setHours(Number(hora), Number(minutos));
              this.fuga.fechaHoraSalida = fechaHoraCombinada.toISOString(); 
            }
            this.nroDocumento = this.fuga.nroDocumento
            console.log(this.fuga);
            
            this.salidaService.crearEditarSalida(this.fuga, this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<RegistroSalidaDTO>) => {
                if (!response.exito) {
                  this.salidaService.checkError(response);
                  return;
                }
                this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                if (this.motivoSalidaSeleccionado === 'SALIDA_TEMPORAL' && this.fuga.permisoSalida) {
                      this.estadoEnCurso();
                      setTimeout(() => {  
                        this.permisoSalida.actualizarPermisoSalida(this.fuga.permisoSalida.tokenIdentificador, this.estadoEnCursoPermisosSalida)

                              .subscribe({
                                  next: (responseUpdate) => {
                                  },
                                  error: (error) => {
                                      console.error(" Error al actualizar registro de salida:", error);
                                  }
                              });
                      }, 500);
                 
              }
                this.router.navigate([`/salida/registro-salida`]);
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
  }
  
  
  private _filter(name: string): FichaIdentificacionDTO[] {
    const filterValue = name.toLowerCase();
    return this.adolescentes.filter(adolescente =>
      this.getNombreCompleto(adolescente).toLowerCase().includes(filterValue)
    );
    
  }
  
  
  cargarAdolescentes() {
    if (!this.funcionarioActivo || !this.funcionarioActivo.tokenIdentificadorDepartamento) {
      console.warn('Intento de cargar adolescentes antes de que el funcionario esté definido.');
      return;
    }
      this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }
          this.adolescentes = (response.data || []).filter(ficha => ficha.tieneProceso !== true);
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


    obtenerFuncionario(): Promise<void> {
      return new Promise((resolve, reject) => {
          this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe({
              next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
                  if (!response.exito) {
                      reject('No se pudo obtener el funcionario.');
                      return;
                  }
                  this.funcionarioActivo = response.data;
                  console.log(this.funcionarioActivo);
                  
                  
                  // this.tokenJerarquia = this.funcionarioActivo.departamento;
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
    inputElement.select(); 
  }
  
  displayFn = (adolescente: FichaIdentificacionDTO | null): string => {
    return adolescente ? this.getNombreCompleto(adolescente) : '';
  };
  
  
  cambioAdolescente(adolescente: FichaIdentificacionDTO): void {
    if (adolescente) {
      this.informeSucesosForm.patchValue({
        numeroIdentificacion: adolescente.numeroIdentificacion
        ,
    });
      this.informeSucesosForm.patchValue({
        tokenFichaIdentificacion: adolescente.idFichaIdentificacion 
      });
      this.uuid_fp = adolescente.idFichaIdentificacion
      this.uuid_adolescente = adolescente.tokenIdentificador
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

  
  getCatalogoMotivoSalida(): Observable<CatalogoDTO[]> {
    if (!this.centro) {
        console.warn("Centro no inicializado, no se pueden obtener motivos de salida.");
        return of([]);
    }
    return this.catalogoService.obtenerHijos('MOTIVO_SALIDA', '').pipe(
        map(responseCatalog => {
            if (!responseCatalog.data) {
                return [];
            }
            if (this.centro.jerarquiaPadre?.nemonico === "CJDR") {
                return responseCatalog.data.filter(motivo => motivo.nemonico !== "SALIDA_INFORME_FINAL");
            }
            if (this.centro.jerarquiaPadre?.nemonico === "SOA") {
                return responseCatalog.data.filter(motivo => motivo.nemonico === "SALIDA_INFORME_FINAL");
            }
            return responseCatalog.data;
        }),
        catchError(error => {
            console.error('Error al obtener motivo salida:', error);
            return of([]);
        })
    );
}






// cargarEventosDeFuga(idFichaIdentificacion: number): void {
//   this.isLoading = true; 
//   this.fugaService.obtenerFugasPorFichaIdentificacion(Math.floor(idFichaIdentificacion)).subscribe({
//     next: (respuesta) => {
//       this.isLoading = false; 
//       if (respuesta.exito) {
//         this.dataSource = (respuesta.data || []).filter(evento => !evento.isComplete);
//         console.log(this.dataSource);
        
//       } else {
//         this.dialogMensajeService.mensajeError(respuesta.mensaje);
//       }
//     },
//     error: (error) => {
//       this.isLoading = false; 
//       console.error('Error al cargar eventos de fuga:', error);
//       this.dialogMensajeService.mensajeError(
//         'No se pudo cargar la lista de eventos de fuga. Intente nuevamente.'
//       );
//     },
//   });
// }

cargarEventosDeFuga(idFichaIdentificacion: number): void {
  this.isLoading = true;
  this.fugaService.obtenerFugasPorFichaIdentificacionJson(Math.floor(idFichaIdentificacion)).subscribe({
    next: (respuesta) => {
      this.isLoading = false;
      
      if (respuesta.exito) {
        const eventos = (respuesta.data || []).filter(evento => !evento.isComplete);
        // if (eventos.length === 0) {
        //   this.dialogMensajeService.mensajeError('No existen eventos de fuga disponibles.');
        //   this.registrosDataSource.data = []; // Limpia la tabla por si tenía datos anteriores
        //   return;
        // }
        const datosTransformados = eventos.map(evento => ({
          noDocumento: evento.numFuga || evento.idFuga || 'Sin documento',
          fecha: evento.fechaFuga || evento.fechaCreacion || 'Sin fecha',
          id: evento.idFuga,
          tokenIdentificador: evento.tokenIdentificador,
          observacion: evento.asunto || 'Sin observación',
        }));
        this.registrosDataSource.data = datosTransformados;
        this.cdr.detectChanges();
      } else {
        this.dialogMensajeService.mensajeError(respuesta.mensaje);
      }
    },
    error: () => {
      this.isLoading = false;
      this.dialogMensajeService.mensajeError('No se pudo cargar la lista de eventos de fuga.');
    },
  });
}

  
  // cargartraslados(idFichaIdentificacion: number): void {
  //   this.isLoading = true;
  //   this.trasladoService.obtenerListadoTrasladosPorAdolescente(Math.floor(idFichaIdentificacion)).subscribe({
  //     next: (respuesta) => {
  //       console.log(respuesta);
        
  //       this.isLoading = false;
  //       if (respuesta.exito) {
  //         this.dataSourceTraslado = Array.from(respuesta.data || []).filter(
  //           traslado =>
  //             traslado.completado === true &&
  //             traslado.trasladoAdolescentes?.some(
  //               adol => adol.isComplete === false && adol.completado === true
  //             )
  //         );
  
  //         console.log(this.dataSourceTraslado);
          
  //       } else {
  //         this.dialogMensajeService.mensajeError( respuesta.mensaje);
  //       }
  //     },
  //     error: (error) => {
  //       this.isLoading = false; 
  //       console.error('Error al cargar eventos de fuga:', error);
  //       this.dialogMensajeService.mensajeError(
         
  //         'No se pudo cargar la lista de traslados. Intente nuevamente.'
  //       );
  //     },
  //   });
  // }

  cargartraslados(idFichaIdentificacion: number): void {
    this.isLoading = true;
    this.trasladoService.obtenerListadoTrasladosPorAdolescente(Math.floor(idFichaIdentificacion)).subscribe({
      next: (respuesta) => {
        this.isLoading = false;
        if (respuesta.exito) {
          this.dataSourceTraslado = Array.from(respuesta.data || []).filter(
            traslado =>
              traslado.completado === true &&
              traslado.trasladoAdolescentes?.some(
                adol => adol.isComplete === false && adol.completado === true
              )
          );
          const datosTransformados = this.dataSourceTraslado.map(traslado => ({
            noDocumento: traslado.numTraslado || traslado.id || 'Sin documento',
            fecha: traslado.instanciaProcesoDTO?.fechaCreacion ? new Date(traslado.instanciaProcesoDTO.fechaCreacion) : null,
            id: traslado.idTraslado,
            tokenIdentificador: traslado.tokenIdentificador,
            observacion: traslado.analisis || 'Sin observación',
            centro: traslado.centroDestino?.nombre || 'Sin centro'
          }));
          this.registrosDataSource.data = datosTransformados;
          this.cdr.detectChanges();
        } else {
          this.dialogMensajeService.mensajeError(respuesta.mensaje);
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.dialogMensajeService.mensajeError('No se pudo cargar la lista de traslados.');
      }
    });
  }
  
  
//   cargarPermisoSalida(idFichaIdentificacion: number): void {
//     this.isLoading = true;
//     this.permisoSalida.obtenerPermisosPorFichaIdentificacion(Math.floor(idFichaIdentificacion)).subscribe({
//       next: (respuesta) => {
//         this.isLoading = false;
//         if (respuesta.exito) {
//           this.dataSourcePermisoSalida = (respuesta.data || []).filter(permiso => !permiso.isComplete);
//           console.log(this.dataSourcePermisoSalida);
          
//         } else {
//           this.dialogMensajeService.mensajeError(respuesta.mensaje);
//         }
//       },
//       error: (error) => {
//         this.isLoading = false; 
//         console.error('Error al cargar permisos de salida:', error);
//         this.dialogMensajeService.mensajeError(
//           'No se pudo cargar la lista de permisos de salida. Intente nuevamente.'
//         );
//       },
//     });
// }

cargarPermisoSalida(idFichaIdentificacion: number): void {
  this.isLoading = true;
  this.permisoSalida.obtenerPermisosPorFichaIdentificacion(Math.floor(idFichaIdentificacion)).subscribe({
    next: (respuesta) => {
      this.isLoading = false;
      if (respuesta.exito) {
        const permisos = (respuesta.data || []).filter(permiso => !permiso.isComplete);
        const datosTransformados = permisos.map(permiso => ({
          noDocumento: permiso.nroDocumento || permiso.idRegistroSalida || 'Sin documento',
          fecha: permiso.fechaHoraSalida || permiso.fechaCreacion || 'Sin fecha',
          id: permiso.idRegistroSalida,
          tokenIdentificador: permiso.tokenIdentificador,
          observacion: permiso.observaciones || 'Sin observación',
        }));
        this.registrosDataSource.data = datosTransformados;
        this.cdr.detectChanges();
      } else {
        this.dialogMensajeService.mensajeError(respuesta.mensaje);
      }
    },
    error: () => {
      this.isLoading = false;
      this.dialogMensajeService.mensajeError('No se pudo cargar la lista de permisos de salida.');
    },
  });
}



  //  obtenerActas() {
  //     this.paginacionRequest.size = this.paginacion.pageSize;
  //     this.paginacionRequest.page = this.paginacion.pageIndex;
  //     this.paginacionRequest.tokenIdentificador = this.uuid_adolescente;
    
  //     this.actaService.obtenerActasExternamiento(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe(
  //       {
  //         next: (response: RespuestaPorDefecto<PaginacionResponse<ActaExternamientoDTO>>) => {
  //           if (!response.exito) {
  //             this.dialogMensajeService.mensajeError(
  //               'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
  //             );
  //             return;
  //           }
  
  //           const actasIncompletas = (response.data.data || []).filter(acta => acta.isComplete === false);

  //           this.listaActas = actasIncompletas;
  //           this.dataSourceExternamiento = actasIncompletas;
            
  //           this.paginacion.totalItems = response.data.totalItems;
  
  //         },
  //         error: (error: any) => {
  //           this.dialogMensajeService.mensajeError(
  //             'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
  //           );
  //         }
  //       }
  //     );
  //   }

  obtenerActas() {
    this.paginacionRequest.size = this.paginacion.pageSize;
    this.paginacionRequest.page = this.paginacion.pageIndex;
    this.paginacionRequest.tokenIdentificador = this.uuid_adolescente;
    this.actaService.obtenerActasExternamiento(this.paginacionRequest, etiquetasModel.NEMONICO_MENU_ACTA_EXTERNAMIENTO).subscribe({
      next: (response) => {
        console.log(response);
        
        if (!response.exito) {
          this.dialogMensajeService.mensajeError('Error al recuperar las actas.');
          return;
        }
        
        const actas = (response.data?.data || []).filter(acta => !acta.isComplete);
         if (actas.length === 0) {
        this.dialogMensajeService.mensajeError('No existen actas de externamiento para el adolescente.');
      }
        const datosTransformados = actas.map(acta => ({
          noDocumento: acta.numeroDocumento || acta.idActaExternamiento || 'Sin documento',
          fecha: acta.fechaRegistro || acta.fechaCreacion || 'Sin fecha',
          id: acta.idActaExternamiento,
          tokenIdentificador: acta.tokenIdentificador,
          observacion: acta.observaciones || 'Sin observación',
          autorizacion: acta.autorizacion || 'Sin autorización',
        }));
        this.dataSourceExternamiento = actas;
        this.registrosDataSource.data = datosTransformados;
        this.paginacion.totalItems = response.data.totalItems;
        this.cdr.detectChanges();
      },
      error: () => {
        this.dialogMensajeService.mensajeError('No se pudo recuperar las actas.');
      }
    });
  }
  

  // onMotivoSalidaChange(motivoSeleccionado: CatalogoDTO): void {
  //   this.motivoSalidaSeleccionado = motivoSeleccionado?.nemonico || null;
  //   this.displayedColumns = ['noDocumento', 'fecha'];
  // if (this.motivoSalidaSeleccionado === 'SALIDA_TRASLADO') {
  //   this.displayedColumns.push('centro');
  // }
  // this.displayedColumns.push('observacion', 'select');

  // this.registrosDataSource.data = []; 
  //   if (motivoSeleccionado && motivoSeleccionado.nemonico) {
  //     switch (motivoSeleccionado.nemonico) {
  //       case 'SALIDA_FUGA':
  //         if (this.uuid_fp) {
  //           this.cargarEventosDeFuga(this.uuid_fp); 
  //           setTimeout(() => {
  //             if (this.dataSource && Array.isArray(this.dataSource)) {
  //               const datosTransformados = this.dataSource.map(evento => ({
  //                 noDocumento: evento.numFuga || evento.id || 'Sin documento',
  //                 fecha: evento.fechaFuga || evento.fecha || 'Sin fecha',
  //                 id: evento.idFuga,
  //                 tokenIdentificador: evento.tokenIdentificador,
  //                 observacion: evento.asunto || 'Sin observación',
  //               }));
  //               this.registrosDataSource.data = datosTransformados; 
  //               this.cdr.detectChanges(); 
  //             } else {
  //               console.warn('No se encontraron eventos de fuga para mostrar.');
  //             }
  //           }, 500); 
  //         } else {
  //           console.warn('No hay un UUID válido para cargar eventos de fuga.');
  //         }
  //         break;
    
  //         case 'SALIDA_TRASLADO':
        
  //         if (this.uuid_fp) {
  //           this.cargartraslados(this.uuid_fp);
  //           setTimeout(() => {
  //             if (this.dataSourceTraslado && Array.isArray(this.dataSourceTraslado)) {
  //               const datosTransformados = this.dataSourceTraslado.map(traslado => ({
  //                 noDocumento: traslado.numTraslado || traslado.id || 'Sin documento',
  //                 fecha: traslado.instanciaProcesoDTO?.fechaCreacion ? new Date(traslado.instanciaProcesoDTO.fechaCreacion) : null, 
  //                 id: traslado.idTraslado,
  //                 tokenIdentificador: traslado.tokenIdentificador,
  //                 observacion: traslado.analisis || 'Sin observación',
  //                 centro: traslado.centroDestino?.nombre|| 'Sin centro'

  //               }));
  //               this.registrosDataSource.data = datosTransformados;
  //               this.cdr.detectChanges(); 
  //             } else {
  //               console.warn('No se encontraron traslados para mostrar.');
  //             }
  //           }, 500); 
  //         } else {
  //           console.warn('No hay un UUID válido para cargar traslados.');
  //         }
  //         break;
  
  //         case 'SALIDA_TEMPORAL':
  //           if (this.uuid_fp) {
  //             this.cargarPermisoSalida(this.uuid_fp);
  //             setTimeout(() => {
  //               if (this.dataSourcePermisoSalida && Array.isArray(this.dataSourcePermisoSalida)) {
  //                 const datosTransformados = this.dataSourcePermisoSalida.map(permiso => ({
  //                   noDocumento: permiso.nroDocumento || permiso.id || 'Sin documento',
  //                   fecha: permiso.fechaHoraSalida || permiso.fecha || 'Sin fecha',
  //                   id: permiso.idPermisoSalida,
  //                   tokenIdentificador: permiso.tokenIdentificador,
  //                   observacion: permiso.observaciones || 'Sin observación',
  //                 }));
  //                 this.registrosDataSource.data = datosTransformados;
  //                 this.cdr.detectChanges(); 
  //               } else {
  //                 console.warn('No se encontraron permisos para mostrar.');
  //               }
  //             }, 500); 
  //           } else {
  //             console.warn('No hay un UUID válido para cargar permisos.');
  //           }
  //         break;
        

  //         case 'SALIDA_EXTERNAMIENTO':
  //           if (this.uuid_adolescente) {
  //             this.obtenerActas();
  //             setTimeout(() => {
  //               if (this.dataSourceExternamiento.length>0 && Array.isArray(this.dataSourceExternamiento)) {
  //                 const datosTransformados = this.dataSourceExternamiento.map(externamiento => ({
  //                   noDocumento: externamiento.numeroDocumento || externamiento.idActaExternamiento || 'Sin documento',
  //                   fecha: externamiento.fechaRegistro || externamiento.fechaCreacion || 'Sin fecha',
  //                   id: externamiento.idActaExternamiento,
  //                   tokenIdentificador: externamiento.tokenIdentificador,
  //                   observacion: externamiento.observaciones || 'Sin observación',
  //                   autorizacion: externamiento.autorizacion || 'Sin autorización',
  //                 }));
  //                 this.registrosDataSource.data = datosTransformados;
  //                 this.cdr.detectChanges(); 
  //               } else {
  //                 this.dialogMensajeService.mensajeError("No se encontraron actas de externamiento para mostrar.");
  //               }
  //             }, 500); 
  //           } 
  //           else {
  //             console.warn('No hay un UUID válido para cargar actas de externamiento.');
  //           }
  //         break;
  //         case 'SALIDA_EXTERNAMIENTO':
  //           if (this.uuid_adolescente) {
  //             this.obtenerActas();
  //           } else {
  //             console.warn('No hay un UUID válido para cargar actas de externamiento.');
  //           }
  //           break;

  //         case 'SALIDA_INFORME_FINAL':
  //           if (this.uuid_adolescente) {
  //             this.obtenerInformes();
  //             setTimeout(() => {
  //               if (this.dataSourceInformeFinal.length>0 && Array.isArray(this.dataSourceInformeFinal)) {
  //                 const datosTransformados = this.dataSourceInformeFinal.map(informe => ({
  //                   noDocumento: informe.numeroDocumento || informe.idInformeFinalAbierto || 'Sin documento',
  //                   fecha: informe.fechaCreacion || informe.fechaCreacion || 'Sin fecha',
  //                   id: informe.idInformeFinalAbierto,
  //                   tokenIdentificador: informe.tokenIdentificador,
  //                   observacion: informe.conclusionesRecomendaciones || 'Sin observación',
  //                 }));
  //                 this.registrosDataSource.data = datosTransformados;
  //                 this.cdr.detectChanges(); 
  //               } else {
  //                 this.dialogMensajeService.mensajeError("No se encontraron informes finales para mostrar.");
  //               }
  //             }, 500); 
  //           } 
  //           else {
  //             console.warn('No hay un UUID válido para cargar actas de externamiento.');
  //           }
  //         break;



  //     }
  //   } else {
  //     console.warn('No se seleccionó un motivo válido.');
  //   }
  // }
  
  onMotivoSalidaChange(motivoSeleccionado: CatalogoDTO): void {
    this.motivoSalidaSeleccionado = motivoSeleccionado?.nemonico || null;
    this.displayedColumns = ['noDocumento', 'fecha'];
    if (this.motivoSalidaSeleccionado === 'SALIDA_TRASLADO') {
      this.displayedColumns.push('centro');
    }
    this.displayedColumns.push('observacion', 'select');
    this.registrosDataSource.data = [];
    switch (this.motivoSalidaSeleccionado) {
      case 'SALIDA_FUGA':
        if (this.uuid_fp) {
          this.cargarEventosDeFuga(this.uuid_fp);
        } else {
          console.warn('No hay un UUID válido para cargar eventos de fuga.');
        }
        break;
  
      case 'SALIDA_TRASLADO':
        if (this.uuid_fp) {
          this.cargartraslados(this.uuid_fp);
        } else {
          console.warn('No hay un UUID válido para cargar traslados.');
        }
        break;
  
      case 'SALIDA_TEMPORAL':
        if (this.uuid_fp) {
          this.cargarPermisoSalida(this.uuid_fp);
        } else {
          console.warn('No hay un UUID válido para cargar permisos.');
        }
        break;
  
      case 'SALIDA_EXTERNAMIENTO':
        if (this.uuid_adolescente) {
          this.obtenerActas();
        } else {
          console.warn('No hay un UUID válido para cargar actas de externamiento.');
        }
        break;
  
      case 'SALIDA_INFORME_FINAL':
        if (this.uuid_adolescente) {
          this.obtenerInformes();
        } else {
          console.warn('No hay un UUID válido para cargar informes.');
        }
        break;
  
      default:
        console.warn('Motivo de salida no reconocido.');
    }
  }
  
  
  
  seleccionarRegistro(registro: any): void {
    this.selectedRegistro = registro;
    this.nroDocumento = this.selectedRegistro.noDocumento; 
    this.fuga.nroDocumento = this.nroDocumento
    switch (this.motivoSalidaSeleccionado) {
      case 'SALIDA_FUGA':
        this.fuga.eventoFuga = {
          idFuga: registro.id,
          tokenIdentificador: registro.tokenIdentificador,
        } as GestionFugaDTO;
        break;
      case 'SALIDA_TRASLADO':
        this.fuga.traslado = {
          idTraslado: registro.id,
          tokenIdentificador: this.selectedRegistro.tokenIdentificador,
        } as TrasladoDTO;
        break;

        case 'SALIDA_TEMPORAL':
          this.fuga.permisoSalida = {
            idRegistroSalida: registro.id,
            tokenIdentificador: registro.tokenIdentificador,
            isComplete: true,
            estadoEvento: this.estadoEnCursoPermisosSalida,
        } as PermisoSalidaDTO;
          break;
      case 'SALIDA_EXTERNAMIENTO':
        this.fuga.externamiento = {
          idActaExternamiento: registro.id,
          tokenIdentificador: registro.tokenIdentificador,
        } as ActaExternamientoDTO;
        break;

        case 'SALIDA_INFORME_FINAL':
          this.fuga.informeFinalAbierto = {
            idInformeFinalAbierto: registro.id,
            tokenIdentificador: registro.tokenIdentificador,
          } as InformeFinalAbiertoDTO;
          break;

      default:
        console.warn("Motivo de salida no reconocido:", this.motivoSalidaSeleccionado);
        break;
    }
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

  pruebaPdf() {
  this.loadImageAsBase64();
  const fechaRegistro = this.formatFecha((new Date).toString())
  const horaRegistro= this.formatHora((new Date).toString())
  const titulopantala= "Informe de registro de salida"
    Object.assign(this.fuga, this.informeSucesosForm.value);
    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_PERMISO_SALIDA;
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]":titulopantala, 
      "[TITULO-INFORME]": titulopantala,
      "[FECHA_REGISTRO]": fechaRegistro,
      "[HORA_REGISTRO]": horaRegistro,
      "[OBSERVACIONES]": this.fuga.observaciones,
      "[NRO-DOCUMENTO]": this.fuga.nroDocumento,
      "[HORA-SALIDA]": this.horaISO,
      "[FECHA-SALIDA]": this.fechaISO,
    }
    
    this.pdfService.generarPdf(request, '').subscribe({
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


//   cargarCentro() {
//     this.jerarquiaService
//         .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
//         .subscribe({
//             next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
//                 if (!respuesta.exito) {
//                     this.jerarquiaService.checkError(respuesta);
//                     return;
//                 }
//                 this.centro = respuesta.data;
//                 this.getCatalogoMotivoSalida().subscribe({
//                     next: (motivoSalidaCatalog) => {
//                         this.catalogosMotivoSalida = motivoSalidaCatalog || [];
//                     },
//                     error: (error) => {
//                         console.error('Error al obtener los catálogos:', error);
//                     }
//                 });
//             },
//             error: (error: any) => {
//                 this.jerarquiaService.checkError(error);
//             },
//         });
// }



  
getCatalogoMotivoSalidaEspecifico(nemonico: string): void {
  this.catalogoService.obtenerHijos('MOTIVO_SALIDA', '').subscribe({
      next: (response) => {
          this.catalogosMotivoSalida = response.data.filter(motivo => motivo.nemonico === nemonico);
          // Forzar la actualización de la vista
          this.cdr.detectChanges();
      },
      error: (error) => {
          console.error(' Error al obtener motivo específico:', error);
      }
  });
}

  
 
//   actualizarPermisoSalida(permiso: PermisoSalidaDTO) {
//     permiso.isComplete = true;  
//     permiso.estadoEvento = this.estadoEnCursoPermisosSalida
//     console.log(permiso);
    
//     this.permisoSalida.crearEditarPermisoSalidas(permiso, '').subscribe({
//         next: (response: RespuestaPorDefecto<PermisoSalidaDTO>) => {
//             if (!response.exito) {
//                 this.dialogMensajeService.mensajeError('Error al actualizar permiso de salida.');
//                 return;
//             }
//         },
//         error: (error: any) => {
//             console.error('Error al actualizar permiso de salida:', error);
//             this.dialogMensajeService.mensajeError('No se pudo actualizar el permiso de salida.');
//         }
//     });
// }

     actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
            if (event.value) {
                const fecha = event.value;
                this.informeSucesosForm.get(controlName).setValue(fecha);
            }
        }

     estadoEnCurso() {
        this.catalogoService.obtenerCatalogoPorNemonico(
          etiquetasModel.NEMONICO_ESTADO_SALIDA_CURSO,
          this.nemonicoMenu
        ).subscribe(
          {
            next: (respuesta: RespuestaPorDefecto<CatalogoDTO>) => {
              if (!respuesta.exito) {
                this.catalogoService.checkError(respuesta);
                return;
              }
              this.estadoEnCursoPermisosSalida = respuesta.data 
            },
            error: (error: any) => {
              this.catalogoService.checkError(error);
            }
          }
        );
     
    }


    estadoEnProceso() {
      this.catalogoService.obtenerCatalogoPorNemonico(
        etiquetasModel.NEMONICO_ESTADO_SALIDA_PROCESO,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (respuesta: RespuestaPorDefecto<CatalogoDTO>) => {
            if (!respuesta.exito) {
              this.catalogoService.checkError(respuesta);
              return;
            }
            this.estadoEnProcesoTraslado = respuesta.data
            
           
          },
          error: (error: any) => {
            this.catalogoService.checkError(error);
          }
        }
      );
   
  }


  obtenerInformes() {
      this.paginacionRequest.size = 100;
      this.paginacionRequest.page = this.paginacion.pageIndex;
      this.paginacionRequest.tokenIdentificador = this.uuid_adolescente;
  
      this.informeFinalAbiertoService.obtenerInformes(this.paginacionRequest, '').subscribe(
        {
          next: (response: RespuestaPorDefecto<PaginacionResponse<InformeFinalAbiertoDTO>>) => {
  
            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
              );
              return;
            }
            const data = response.data.data;
            this.dataSourceInformeFinal = data.filter(item => item.completado);
              if (this.dataSourceInformeFinal.length === 0) {
                this.dialogMensajeService.mensajeError('No existe informes finales para el adolescente.');
              }
            const datosTransformados = this.dataSourceInformeFinal.map(acta => ({
            noDocumento: acta.numeroDocumento || acta.idInformeFinalAbierto || 'Sin documento',
            fecha: acta.fechaFinalizacion || acta.fechaCreacion || 'Sin fecha',
            id: acta.idInformeFinalAbierto,
            tokenIdentificador: acta.tokenIdentificador,
            observacion: acta.conclusionesRecomendaciones || 'Sin observación',
           }));
            // this.dataSourceExternamiento = actas;
            this.registrosDataSource.data = datosTransformados;
            this.cdr.detectChanges();
          },
          error: (error: any) => {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
          }
        }
      );
    }


//     obtenerJerarquias() {
//     this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
//     const idDepartamento = this.funcionarioActivo?.tokenIdentificadorDepartamento;
//     this.jerarquia = data.data.filter(j => j.tokenIdentificador === idDepartamento);
//     console.log(this.jerarquia);
    
//     if (this.jerarquia.length > 0) {
//       this.fuga.centroSalida = this.jerarquia[0];
//     } else {
//       this.fuga.centroSalida = null;
//       console.warn('No se encontró jerarquía con token:', idDepartamento);
//     }
//   });
// }

obtenerJerarquia(): void {
  this.jerarquiaService
    .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
    .subscribe({
      next: (response: RespuestaPorDefecto<any>) => {
        if (!response.exito || !response.data) {
          this.dialogMensajeService.mensajeError('No se pudo obtener la jerarquía del usuario.');
          return;
        }
        this.fuga.centroSalida = response.data
        this.centro = response.data
        this.getCatalogoMotivoSalida().subscribe({
                    next: (motivoSalidaCatalog) => {
                        this.catalogosMotivoSalida = motivoSalidaCatalog || [];
                    },
                    error: (error) => {
                        console.error('Error al obtener los catálogos:', error);
                    }
                });
        console.log(response.data);
       
      },
      error: (err) => {
        console.error('Error al obtener jerarquía por documento:', err);
        this.dialogMensajeService.mensajeError('Error al obtener la jerarquía del usuario.');
      },
    });
}

}
