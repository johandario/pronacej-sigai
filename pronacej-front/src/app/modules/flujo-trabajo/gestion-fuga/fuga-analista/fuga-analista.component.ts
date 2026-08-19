import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormsModule, UntypedFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GestionFugaService } from '../gestion-fuga.service';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter, DateAdapter } from '@angular/material/core';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { CUSTOM_DATE_FORMATS, FuncionesUtils, CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { FichaPrincipalDocumentoDTO } from 'app/core/model/request/ia/FichaPrincipalDocumentoDTO.model';
import { FichaPrincipalDocumentoService } from 'app/modules/administracion/services/fichaPrincipalDocumento.service';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { environment } from 'environments/environment';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { catchError, forkJoin, map, Observable, startWith, tap, throwError, concatMap, iif, of } from 'rxjs';
import { CommonModule, Location } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuillModule } from 'ngx-quill';
import { ValidatorFn } from '@iplab/ngx-file-upload';
import { HttpClient } from '@angular/common/http';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { InstanciaProcesoDTO, TareaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { ProcesoDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { FlujoTrabajoService } from '../../flujo-trabajo.service';



export function noWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (typeof value === 'string') {
      const isWhitespace = value.trim().length === 0;
      const isValid = !isWhitespace;
      return isValid ? null : { whitespace: true };
    }
    return null; // Si el valor no es un string, no marca error
  };
}

export function noSpecialCharactersValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value || '';
    if (typeof value !== 'string') {
      return null; // No validar si el valor no es una cadena
    }
    const hasSpecialCharacters = /[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s]/.test(value);
    return hasSpecialCharacters ? { specialCharacters: true } : null;
  };
}




@Component({
  selector: 'app-fuga-analista',
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
    QuillModule],
  templateUrl: './fuga-analista.component.html',
  styleUrl: './fuga-analista.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class FugaAnalistaComponent implements OnInit {


  isLoading: boolean = false;
  tokenID: string;
  fuga: GestionFugaDTO = new GestionFugaDTO();
  estado: string = '';
  horaISO: string
  fechaISO: string
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();
  uuid_fp: any;
  base64Image: string | null = null;
  adolescente: string
  actualDate: any;
  funcionarioActivo: FuncionarioDTO;
  tokenJerarquia: any
  jerarquia: any;
  tokenFilter: any
  nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;
  informeSucesosForm = this.fb.group({
    fechaFuga: ['', Validators.required],
    horaFuga: ['', [Validators.required, this.validarHora()]],
    presenciaDe: ['', Validators.required],
    descripcionHechos: ['', Validators.required],
    tokenFichaIdentificacion: ['', Validators.required],
    numeroIdentificacion: [''],
  });
  @Input({ required: true }) declare nemonicoMenu: string;
  adolescentes: FichaIdentificacionDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  esVisualizar: boolean = false;
  dataAdolescente: any
  tareaEntrante: TareaDTO = new TareaDTO;
  instancia: any
  tareaSaliente: TareaDTO = new TareaDTO;
  listaTareas: TareaDTO[];
  nombreAdolescente: any
  presenciDe: any
  hechos: any
  numeroIdentificacion: any

  proceso: ProcesoDTO;
  esNuevo: boolean = false;
  esBorrador: boolean = false;
  fichaIdentifacion: FichaIdentificacionDTO

  constructor(
    private fichaPrincipalDocumentoService: FichaPrincipalDocumentoService,
    private router: Router, private route: ActivatedRoute,
    private gestionFugaService: GestionFugaService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    private http: HttpClient,
    private catalogoService: CatalogoService,
    private funcionarioService: FuncionarioService,
    private jerarquiaService: JerarquiaService,
    private flujoTrabajoService: FlujoTrabajoService,
    private _location: Location,

  ) { }

  ngOnInit(): void {

    this.proceso = history.state.proceso;

    if (this.proceso) {
      this.esNuevo = true;
    }

    if (history.state.tareaEntrante && history.state.listaTareas) {
      this.tareaEntrante = history.state.tareaEntrante;
      this.listaTareas = history.state.listaTareas;
    }

    if (this.tareaEntrante.estado == "Borrador")
      this.esBorrador = true;
    

    this.route.queryParams.subscribe(params => {
      this.tokenID = params['instancia'];
      
      console.log(this.estado);
      
      console.log('✅ tokenID desde queryParams:', this.tokenID);


      this.cargarDatos();
      this.estadoInicial()
      this.obtenerTokenDepartamento().then(() => {

        this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
          
          this.jerarquia = data.data.filter(j => j.nombre === this.tokenJerarquia);
          if (this.jerarquia.length > 0) {
            this.fuga.centro = this.jerarquia[0];
            console.log(this.fuga.centro)
          } else {
            this.fuga.centro = null;
          }

          this.obtenerJerarquias();
          if (this.tokenID) {
           
            forkJoin([
              this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu),
              this.gestionFugaService.obtenerFugasPorTokenID(this.tokenID, this.nemonicoMenu)
            ]).subscribe(([adolescentesResponse, fugaResponse]) => {
              // Manejar respuesta de adolescentes
              if (adolescentesResponse.exito) {
                this.adolescentes = adolescentesResponse.data;
                this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
                  startWith(''),
                  map(value => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
                  map(name => (name ? this._filter(name) : this.adolescentes.slice()))
                );
              } else {
                console.error('Error al cargar adolescentes');
              }
              if (fugaResponse.data) {
                this.fuga = fugaResponse.data;
                this.estado ='Completada';
                if (!this.esBorrador) {
                  this.informeSucesosForm.disable();
                  this.personaControl.disable();
                }
                this.informeSucesosForm.patchValue(fugaResponse.data);
                const seleccionado = this.adolescentes.find(
                  adolescente => adolescente.idFichaIdentificacion === Number(this.fuga.tokenFichaIdentificacion)
                );
  
                if (seleccionado) {
                  this.personaControl.setValue(seleccionado);
                  this.dataAdolescente = seleccionado
                  this.adolescente = this.getNombreCompleto(seleccionado);
                  this.numeroIdentificacion = this.fuga.numeroIdentificacion
                  this.informeSucesosForm.patchValue({
                    numeroIdentificacion: seleccionado.numeroIdentificacion
                    ,
                  });
                } else {
                  console.error('No se encontró ningún adolescente con idFichaIdentificacion igual a', this.fuga.tokenFichaIdentificacion);
                }
                if (this.fuga.fechaFuga) {
                  console.log(this.fuga.numeroIdentificacion);
  
                  const fecha = new Date(this.fuga.fechaFuga);
                  this.fechaISO = fecha.toISOString().split('T')[0];
                  this.horaISO = fecha.toTimeString().split(' ')[0].slice(0, 5);
                  this.nombreAdolescente = this.fuga.nombreAdolescente
                  this.presenciDe = this.fuga.presenciaDe,
                    this.hechos = this.fuga.descripcionHechos,
                    this.numeroIdentificacion = this.fuga.numeroIdentificacion
                  this.informeSucesosForm.patchValue({
                    fechaFuga: this.fechaISO,
                    horaFuga: this.horaISO
                  });
                }
                this.route.queryParams.subscribe(params => {
  
                  // if (this.estado === 'Completada') {
                  //   this.informeSucesosForm.disable();
                  //   this.personaControl.disable();
                  // }
                });
              }
            });
          } else {
            this.cargarAdolescentes();
          }

        });

      });
    });


    this.tokenID = this.route.snapshot.params['tokenID'];


    this.personaControl = new FormControl(null, [
      Validators.required,
      noSpecialCharactersValidator(),
    ]);

    // 6. Establecer hora y fecha actual
    this.actualDate = new Date();

    const currentHour = this.actualDate.getHours().toString().padStart(2, '0');
    const currentMinutes = this.actualDate.getMinutes().toString().padStart(2, '0');
    const formattedTime = `${currentHour}:${currentMinutes}`;
    this.informeSucesosForm.patchValue({
      fechaFuga: this.actualDate,
      horaFuga: formattedTime,
    });

    // 7. Estado inicial y jerarquía
    this.estadoInicial();
    this.obtenerTokenDepartamento().then(() => {
      this.obtenerJerarquias();
    });
  }



  cargarDatos(): void {
    //Existe el parámetro de URL tokenID?
    // Si: Cargo traslado
    // No: tengo el formulario para poder llenar
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerParametrosDeConsulta().pipe(
      concatMap(() =>
        iif(
          () => this.tokenID ? true : false,
          this.obtenerFuga(),
          of(null),
        )
      )
    ).subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });
  }

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        this.tokenID = params['instancia'];
        console.log(this.tokenID);


      })
    );
  }

  obtenerFuga(): Observable<any> {
    return this.gestionFugaService.obtenerFugasPorTokenID(this.tokenID, this.nemonicoMenu).pipe(
      tap((response) => {
        this.informeSucesosForm.patchValue(response.data);
        this.fuga = response.data;
        if (this.fuga.fechaInformeApoderado != null && this.fuga.fechaInformeApoderado != undefined) {
          const fecha = new Date(this.fuga.fechaInformeApoderado);
          this.fechaISO = fecha.toISOString().split('T')[0];
        }
        this.nombreAdolescente = this.fuga.nombreAdolescente
        this.presenciDe = this.fuga.presenciaDe,
          this.hechos = this.fuga.descripcionHechos,
          this.numeroIdentificacion = this.fuga.numeroIdentificacion
        console.log(this.fuga);
        // this.trasladoCargado = true;              
      }),
      catchError(err => {
        this.gestionFugaService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  cancelar() {
    // this.router.navigate([`/flujo-trabajo/bandeja-entrada`]);
    this._location.back();
  }

  guardarFuga() {
    this.informeSucesosForm.markAllAsTouched();
    this.personaControl.markAsTouched();

    const tokenFichaIdentificacion = this.informeSucesosForm.get('tokenFichaIdentificacion')?.value;
    if (!tokenFichaIdentificacion || this.personaControl.invalid || this.informeSucesosForm.invalid) {
      this.dialogMensajeService.mensajeError('Por favor, completa correctamente todos los campos obligatorios.');
      return;
    }

    this.route.queryParams.subscribe(params => {
      const tokenProceso = params['proceso'];
      if (tokenProceso) {
        this.fuga.tokenProceso = tokenProceso;
      } else {
        this.fuga.tokenInstancia = this.tokenID;
      }

      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de fuga',
        "¿Deseas continuar?"
      );

      ref.afterClosed().subscribe(resp => {
        if (resp === "confirmed") {

          this.route.queryParams.subscribe(params => {
            let ref = this.dialogMensajeService.mensajeConConfirmacion(
              'Se creará un registro de fuga',
              "Deseas continuar?"
            );

            ref.afterClosed().subscribe(
              {
                next: (resp: "confirmed" | "cancelled") => {
                  if (resp == "confirmed") {
                    if (this.esNuevo) {
                      this.crearInstancia();
                    }
                    else {
                      this.crearEditarFuga();
                    }
                  }
                }
              }
            );
          })


        }
      });
    });
  }

  guardarBorrador() {
   
    this.personaControl.markAsTouched();

    if (this.personaControl.invalid) {
      this.dialogMensajeService.mensajeError('Es necesario seleccionar un adolescente.');
      return;
    }

    this.esBorrador = true;

    if (this.esNuevo) {
      this.crearInstancia();
    }
    else {
      this.crearEditarBorrador();
    }
  }

  crearInstancia() {
    this.flujoTrabajoService.crearInstanciaProcesoPorProceso(this.proceso, '').subscribe(
      {
        next: (response: RespuestaPorDefecto<InstanciaProcesoDTO>) => {

          if (!response.exito) {
            this.flujoTrabajoService.checkError(response);
            return;
          }

          if (response.data) {
            console.log(response.data);
            this.tareaEntrante = response.data.tareas.find(t => t.orden == 1);

            if (this.esBorrador)
              this.crearEditarBorrador();
            else
              this.crearEditarFuga();
          }
        },
        error: (error: any) => {
          this.flujoTrabajoService.checkError(error);
        }
      }
    );
  }

  crearEditarFuga() {
    Object.assign(this.fuga, this.informeSucesosForm.value);
    if (this.fuga.fechaFuga && this.informeSucesosForm.value.horaFuga) {
      const fecha = new Date(this.fuga.fechaFuga);
      const [hora, minutos] = this.informeSucesosForm.value.horaFuga.split(':');
      fecha.setHours(Number(hora), Number(minutos));
      this.fuga.fechaFuga = fecha.toISOString();
    }
    this.fuga.html = `
    <br><strong>Fecha de la fuga: </strong>${this.fuga.fechaFuga}<br>
   <strong>Adolescente: </strong>${this.adolescente} <strong>Cédula:</strong> ${this.numeroIdentificacion}<br><br>
    <strong>Presencia de: </strong>${this.fuga.presenciaDe}<br><br>
    <strong>Narrar los hechos: </strong>${this.fuga.descripcionHechos}<br>  `;

    const tareaEventoFuga = {
      eventoFuga: this.fuga,
      tarea: this.tareaEntrante
    };
 
    
    this.gestionFugaService.crearEditarFuga(tareaEventoFuga, '').subscribe({
      next: (response: RespuestaPorDefecto<GestionFugaDTO>) => {
        if (!response.exito) {
          this.gestionFugaService.checkError(response);
          return;
        }
        this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
        this.router.navigate([`/flujo-trabajo/bandeja-salida`]);
      },
      error: (error: any) => {
        this.gestionFugaService.checkError(error);
      }
    });
  }

  crearEditarBorrador() {
    Object.assign(this.fuga, this.informeSucesosForm.value);
    if (this.fuga.fechaFuga && this.informeSucesosForm.value.horaFuga) {
      const fecha = new Date(this.fuga.fechaFuga);
      const [hora, minutos] = this.informeSucesosForm.value.horaFuga.split(':');
      fecha.setHours(Number(hora), Number(minutos));
      this.fuga.fechaFuga = fecha.toISOString();
    }

    const tareaEventoFuga = {
      eventoFuga: this.fuga,
      tarea: this.tareaEntrante
    };

    this.gestionFugaService.guardarBorrador(tareaEventoFuga, '').subscribe({
      next: (response: RespuestaPorDefecto<GestionFugaDTO>) => {
        if (!response.exito) {
          this.gestionFugaService.checkError(response);
          return;
        }
        this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
        this.router.navigate([`/flujo-trabajo/bandeja-borrador`]);
      },
      error: (error: any) => {
        this.gestionFugaService.checkError(error);
      }
    });
  }

  private _filter(name: string): FichaIdentificacionDTO[] {
    const filterValue = name.toLowerCase();
    return this.adolescentes.filter(adolescente =>
      this.getNombreCompleto(adolescente).toLowerCase().includes(filterValue)
    );
  }

  

  cargarAdolescentes() {

    
    this.fichaIdentificacionService.obtenerNombresFichas(this.nemonicoMenu, this.fuga.centro.tokenIdentificador).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }
        // this.adolescentes = response.data;
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

  getNombreCompleto(adolescente: FichaIdentificacionDTO): string {
    this.adolescente = `${adolescente.nombres} ${adolescente.apellidoPaterno} ${adolescente.apellidoMaterno}`
    return `${adolescente.nombres} ${adolescente.apellidoPaterno} ${adolescente.apellidoMaterno}`;
  }


  onAdolescenteSeleccionado(event: any): void {
    const seleccionado: FichaIdentificacionDTO = event.option.value;
    this.dataAdolescente = seleccionado
    if (seleccionado) {
      this.personaControl.setValue(seleccionado);
      this.informeSucesosForm.patchValue({
        tokenFichaIdentificacion: seleccionado.idFichaIdentificacion,
        numeroIdentificacion: seleccionado.numeroIdentificacion
      });
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
    setTimeout(() => {
    const fechaRegistro = this.formatFecha((new Date).toString())
    const horaRegistro = this.formatHora((new Date).toString())
    const titulopantala = "Informe de fuga"
    const fechaFuga = this.formatFecha(String(this.fuga.fechaFuga))
    Object.assign(this.fuga, this.informeSucesosForm.value);
    console.log(this.fuga);


    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_FUGA_ANALISTA;
    request.variables = {
      "[IMG_BASE64]": this.base64Image,
      "[TITULO-PLANTILLA]": titulopantala,
      "[TITULO-INFORME]": titulopantala,
      "[FECHA_REGISTRO]": fechaRegistro,
      "[HORA_REGISTRO]": horaRegistro,
      "[DESCRIPCION-HECHOS]": this.fuga.descripcionHechos,
      "[PRESENCIA-DE]": this.fuga.presenciaDe,
      "[HORA-FUGA]": this.horaISO,
      "[FECHA-FUGA]": fechaFuga,
      "[CENTRO]": this.funcionarioActivo.departamento,
      "[FECHA-INGRESO]": this.dataAdolescente.fechaIngreso,
      "[NUMERO-IDENTIFICACION]": this.dataAdolescente.numeroIdentificacion,
      "[NOMBRE-ADOLESCENTE]": this.fuga.nombreAdolescente,
      "[EDAD]": `${this.funcionesUtils.getEdad(String(this.fuga.fechaNacimiento))}`
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
  }, 500); 
  }

  obtenerFichaIdentificacion(): Observable<any> {
   
    
    return this.fichaIdentificacionService
    .obtenerFichaIdentificacionPorTokenIdentificador(this.fuga.tokenFichaIdentificacion, this.nemonicoMenu)
    .pipe(
        tap((response) => {
          console.log("Ficha de Identificación cargada:", response.data);
          this.fichaIdentifacion = response.data
        }),
        catchError((error) => {
          console.error("Error al obtener ficha de identificación:", error);
          return of(null); 
        })
    );
  }

  

  subirArchivosEvent(documentos: DocumentoSubido[]) {
    if (documentos && documentos.length > 0) {
      if (!this.fuga.tokenFichaIdentificacion) {
        this.dialogMensajeService.mensajeError("No existe un id de ficha de identificación válido");
        return;
      }
      for (let documentoSubido of documentos) {
        let fichaPrincipalDocumentoDTO = new FichaPrincipalDocumentoDTO();
        fichaPrincipalDocumentoDTO.tokenIdentificadorFichaPrincipal = this.fuga.tokenFichaIdentificacion;
        //fichaPrincipalDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;  //comentado por pruebas en subida de documentos en la seccion de ficha principal
        let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
          documentoSubido.documento.name
        );
        this.fichaPrincipalDocumentoService.subirDocumento(
          documentoSubido.documento,
          fichaPrincipalDocumentoDTO,
          this.nemonicoMenu
        ).subscribe(
          {
            next: (response: RespuestaPorDefecto<DocumentoDTO>) => {

              load.close();
              if (!response.exito) {
                this.fichaIdentificacionService.checkError(response);
                return;
              }
              //Refrescar la tabla de documentos
              // this.obtenerDocumentos();
            },
            error: (error: any) => {
              load.close();
              this.fichaIdentificacionService.checkError(error);
            }
          }
        );
      }
    } else {
      this.dialogMensajeService.mensajeError("No se obtenieron documentos para ser subidos");
    }
  }

  private obtenerTiposDeDocumentos() {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(etiquetasModel.SECCION_FICHA_IDENT_FICHA_PRINCIPAL,
      this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {
            if (!environment.production) {

            }
            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
              return;
            }
            let tiposArchivos = response.data;
            if (tiposArchivos.length == 0) {
              this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
              return;
            }

            this.tiposDeDocumentosSistema =
              tiposArchivos.map(
                (tipoArch) => {
                  let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
                  let tipoDeDocumento = new TipoDeDocumento();
                  tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
                  tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
                  tipoDeDocumento.requerido = tipoArch.requerido;
                  tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
                  tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

                  return tipoDeDocumento;
                }
              );
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
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
      this.personaControl.setValue(adolescente);
      this.personaControl.updateValueAndValidity();
      console.log(adolescente);

      this.informeSucesosForm.patchValue({
        tokenFichaIdentificacion: adolescente.idFichaIdentificacion,
        numeroIdentificacion: adolescente.numeroIdentificacion
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
      this.informeSucesosForm.controls['horaFuga'].setErrors({ invalid: true });
    } else {
      this.informeSucesosForm.controls['horaFuga'].setErrors(null);
    }


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
      this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenuinicio).subscribe({
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          if (!response.exito) {
            resolve();
            return;
          }
          this.funcionarioActivo = response.data;
          console.log('funcionarioActivo',this.funcionarioActivo);

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
        console.log(this.fuga.centro)
      } else {
        this.fuga.centro = null;
      }
    });
  }





}
