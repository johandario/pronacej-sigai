import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NotificacionDTO } from 'app/core/model/both/ia/notificacionDTO.model';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatExpansionModule } from '@angular/material/expansion';
import { SubidaDeDocumentosComponent } from '../../documentos/subida-de-documentos/subida-de-documentos.component';
import { TipoDeDocumento } from '../../documentos/modelos/TipoDeDocumento.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { NotificacionService } from 'app/core/services/notificacion.service';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { CommonModule, Location, ɵNullViewportScroller } from '@angular/common';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { CUSTOM_DATE_FORMATS, CustomDateAdapter, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { ActivatedRoute } from '@angular/router';
import { QuillModule } from 'ngx-quill';
import { map, Observable, startWith } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { formatDate } from '@angular/common';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { PageEvent } from '@angular/material/paginator';
import { DocumentosSubidosTablaComponent } from '../../documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { InformacionUbicacionDTO } from 'app/core/model/both/InformacionUbicacionDTO.model';

@Component({
  selector: 'app-envio-notificacion',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatChipsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    QuillModule,
    MatExpansionModule,
    SubidaDeDocumentosComponent,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './envio-notificacion.component.html',
  styleUrl: './envio-notificacion.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
  ]
})
export class EnvioNotificacionComponent implements OnInit, AfterViewInit {
  public notificacionEmailDTO: NotificacionDTO = new NotificacionDTO();
  @Input() remitente = "desarrollo@latinus.net";
  @Input() tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  @Input() requiereIngresoDeDocumentos = false;
  @Input({ required: true }) declare nemonicoMenu: string;

  @Output() envioDeCorreoExitoso = new EventEmitter<NotificacionDTO>();

  titulo: string = "Nueva Notificación";
  listaPrev: boolean = false;
  esVisualizar: boolean = false;
  item: NotificacionDTO;
  uuid_fp: string;

  formGroup: FormGroup;
  correoForm: FormGroup;
  funcionarioActivo: FuncionarioDTO;
  adolescentes: FichaIdentificacionDTO[] = [];
  adolescenteSeleccionado: FichaIdentificacionDTO;
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();

  personasRelacionadas: PersonaRelacionadaDTO[] = [];
  personasFiltradas: { nombres: string; valorInformacionUbicacion: string }[] = [];
  destinatariosSeleccionados: string[] = [];

  tiposNotificacion: CatalogoDTO[] = [];
  mediosNotificacion: CatalogoDTO[] = [];

  etiquetasModel = etiquetasModel;

  @ViewChild("subidaDeDocumentosComp") subidaDeDocumentosComp: SubidaDeDocumentosComponent;

  @ViewChild('tablaDocumentosComp')
  tablaDocumentosComp: DocumentosSubidosTablaComponent;

  constructor(private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private fichaService: FichaIdentificacionService,
    private funcionarioService: FuncionarioService,
    private personaService: DatosFamiliaresService,
    private notificacionEmailService: NotificacionService,
    public funcionesUtils: FuncionesUtils,
    private route: ActivatedRoute,
    private location: Location,
  ) {
    this.formGroup = this.fb.group(
      {
        adolescente: [null, [Validators.required]],
        destinatario: [null, [Validators.required]],
        medio: [null, [Validators.required]],
        tipo: [null, [Validators.required]],
        mensaje: [null, [Validators.required]],
        asunto: [null, [Validators.required]],
        fechaEntrega: [{ value: '', disabled: true }],
        entregado: [{ value: '', disabled: true }],
        observacionesEntrega: [{ value: '', disabled: true }]
      }
    );

    this.correoForm = this.fb.group({
      correos: [null, [Validators.required, Validators.email, this.emailDomainValidator()]]
    });
  }

  ngOnInit(): void {
    this.listaPrev = history.state.listaPrev;

    this.item = history.state.item;

    if (this.item)
      this.esVisualizar = true;

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.TIPO_NOTIFICACION, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.tiposNotificacion = data;
        if (this.esVisualizar) {
          this.formGroup.controls.tipo.setValue(this.item.tipo);
          this.formGroup.controls.tipo.disable();
        }
      },
      error: (error) => console.error('Error cargando tipos de notificación:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.MEDIO_NOTIFICACION, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.mediosNotificacion = data;
        if (this.esVisualizar) {
          this.formGroup.controls.medio.setValue(this.item.medio);
          this.formGroup.controls.medio.disable();
        }
      },
      error: (error) => console.error('Error cargando medios de notificación:', error)
    });

    this.uuid_fp = this.route.snapshot.paramMap.get('uuid_fp');

    this.obtenerFuncionario();
    this.verificarTiposDeDocumentos();

    this.formGroup.controls.destinatario.disable();
    this.formGroup.controls.asunto.disable();

    if (this.esVisualizar) {
      this.titulo = "Ver Notificación";
      this.nemonicoMenu = etiquetasModel.NEMONICO_MENU_NOTIFICACIONES;
      this.visualizarNotificacion();
    }

    // Sincroniza el FormControl del formulario principal con el de autocompletar
    this.personaControl.valueChanges.subscribe(value => {
      const personaSeleccionada = typeof value === 'string' ? null : value;
      this.formGroup.patchValue({ adolescente: personaSeleccionada?.idFichaIdentificacion || '' });
      this.adolescenteSeleccionado = personaSeleccionada;
    });

    this.suscribirCambiosMedio();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.esVisualizar)
        this.obtenerDocumentos();
    });
  }

  obtenerDocumentos() {
    let page = this.tablaDocumentosComp.page;
    let pageSize = this.tablaDocumentosComp.pageSize;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = page;
    paginacionRequest.size = pageSize;
    paginacionRequest.tokenIdentificador = this.item.tokenIdentificador;
    console.log(paginacionRequest);
    this.notificacionEmailService.obtenerDocumentos(
      paginacionRequest,
      etiquetasModel.NEMONICO_MENU_NOTIFICACIONES_DOCUMENTACION
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          console.log(response);
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentosComp.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  suscribirCambiosMedio(): void {
    this.formGroup.get('medio')?.valueChanges.subscribe((medio) => {
      if (medio === 'MEDIO_FISICO') {
        // Habilitar los campos
        this.formGroup.get('fechaEntrega')?.enable();
        this.formGroup.get('entregado')?.enable();
        this.formGroup.get('observacionesEntrega')?.enable();
      } else {
        // Setear valores por defecto
        //const fechaActual = formatDate(new Date(), 'dd-MM-yyyy', 'es');
        this.formGroup.get('fechaEntrega')?.setValue(new Date());
        this.formGroup.get('entregado')?.setValue(this.destinatariosSeleccionados.join(",") || '');
        this.formGroup.get('observacionesEntrega')?.setValue("Medio: " + this.mediosNotificacion.find(x => x.nemonico == medio)?.descripcion);

        // Deshabilitar los campos
        this.formGroup.get('fechaEntrega')?.disable();
        this.formGroup.get('entregado')?.disable();
        this.formGroup.get('observacionesEntrega')?.disable();
      }
    });
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

  cambioAdolescente(adolescente: FichaIdentificacionDTO): void {
    this.formGroup.patchValue({ adolescente: adolescente.idFichaIdentificacion });
    this.personasFiltradas = [];
    this.destinatariosSeleccionados = [];
    this.formGroup.controls.destinatario.disable();
    this.cargarPersonasRelacionadas();
  }

  getTituloArchivos() {
    let loguitud = this.subidaDeDocumentosComp?.documentosSubidos?.length;
    return "Carga de archivos" + (loguitud > 0 ? " (archivos cargados " + loguitud + ")" : "");
  }

  enviarCorreo() {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      this.dialogMensajeService.mensajeError("Verifica la información requerida antes de continuar");
      return;
    }


    if (!this.destinatariosSeleccionados || this.destinatariosSeleccionados?.length < 1) {
      this.dialogMensajeService.mensajeError("Debes de ingresar al menos 1 destinatario para continuar");
      return;
    }

    let docSubidos = this.subidaDeDocumentosComp?.documentosSubidos ?? null;
    if (this.requiereIngresoDeDocumentos && (!docSubidos || docSubidos?.length < 1)) {
      this.dialogMensajeService.mensajeError("Debes de cargar al menos 1 archivo para continuar");
      return;
    }

    if (this.subidaDeDocumentosComp?.documentosCargados?.length > 0) {
      this.dialogMensajeService.mensajeError("Hay archivos por cargar");
      return;
    }

    let refConf;

    if (this.formGroup.get("medio").value == 'MEDIO_CORREO')
      refConf = this.dialogMensajeService.mensajeConConfirmacion(
        "Estás seguro de enviar el correo electrónico",
        "Estás a punto de enviar un correo a: " + this.destinatariosSeleccionados
      );
    else if (this.formGroup.get("medio").value == 'MEDIO_FISICO')
      refConf = this.dialogMensajeService.mensajeConConfirmacion(
        "Estás seguro de enviar la notificación?",
        "Esto no enviará un correo electrónico, solo dejará constancia"
      );
    else
      refConf = this.dialogMensajeService.mensajeConConfirmacion(
        "Estás seguro de enviar la notificación?",
        "Estás a punto de enviar una notificación a: " + this.destinatariosSeleccionados
      );

    refConf.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Enviando el correo..");
            let files = docSubidos?.map(doc => doc.documento) ?? null;
            let notificacionDTO = new NotificacionDTO();

            notificacionDTO.asunto = this.formGroup.get("asunto").value;
            notificacionDTO.cuerpo = this.formGroup.get("mensaje").value;
            notificacionDTO.medio = this.formGroup.get("medio").value;
            notificacionDTO.tipo = this.formGroup.get("tipo").value;
            notificacionDTO.adolescente = this.formGroup.get("adolescente").value;
            notificacionDTO.fechaEntrega = this.formGroup.get("fechaEntrega").value;
            notificacionDTO.entregado = this.formGroup.get("entregado").value;
            notificacionDTO.observacionesEntrega = this.formGroup.get("observacionesEntrega").value;
            notificacionDTO.destinatarios = this.destinatariosSeleccionados.join(",");
            notificacionDTO.documentoDTOList = docSubidos?.map(doc => doc.documentoDTO);
            notificacionDTO.remitente = this.remitente;
            this.notificacionEmailService.enviarNotificacion(files,
              notificacionDTO, etiquetasModel.NEMONICO_MENU_NOTIFICACIONES_DOCUMENTACION
            ).subscribe(
              {
                next: (resp: RespuestaPorDefecto<NotificacionDTO>) => {
                  load.close();
                  if (!resp.exito) {
                    this.notificacionEmailService.checkError(resp);
                    return;
                  }
                  else {
                    this.dialogMensajeService.mensajeExitoso(
                      'Envío de Notificación',
                      'Se ha enviado la notificación exitosamente.'
                    ).afterClosed().subscribe(() => {
                      this.borrarDatos();

                      if (this.uuid_fp)
                        this.location.back();
                    });
                  }
                },
                error: (error: any) => {
                  load.close();
                  // this.notificacionEmailService.checkError(error);
                }
              }
            );
          }
        }
      }
    );

  }

  cargarAdolescentes() {
    this.fichaService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.adolescentes = response.data;

        // Inicializar filtro
        this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
          startWith(''),
          map(value => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
          map(name => (name ? this._filter(name) : this.adolescentes.slice()))
        );

        if (this.esVisualizar) {
          console.log(this.item);
          let adolescente = this.adolescentes.find(x => x.idFichaIdentificacion == this.item.adolescente);
          this.personaControl.setValue(adolescente);
          this.personaControl.disable();
          this.formGroup.controls.adolescente.setValue(this.item.adolescente);
          this.formGroup.controls.adolescente.disable();
        }
        else {

          if (this.uuid_fp) {
            let adolescente = this.adolescentes.find(x => x.tokenIdentificador == this.uuid_fp);
            this.personaControl.setValue(adolescente);
            this.personaControl.disable();
            this.formGroup.controls.adolescente.setValue(adolescente.idFichaIdentificacion);

            this.cargarPersonasRelacionadas();
          }
          else
            this.formGroup.controls.medio.disable();
        }

      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  cargarAdolescenteGuardado() {
    this.fichaService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.adolescentes = []
        this.adolescentes.push(response.data);

        // Inicializar filtro
        this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
          startWith(''),
          map(value => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
          map(name => (name ? this._filter(name) : this.adolescentes.slice()))
        );

        if (this.esVisualizar) {
          console.log(this.item);
          let adolescente = this.adolescentes.find(x => x.idFichaIdentificacion == this.item.adolescente);
          this.personaControl.setValue(adolescente);
          this.personaControl.disable();
          this.formGroup.controls.adolescente.setValue(this.item.adolescente);
          this.formGroup.controls.adolescente.disable();
        }
        else {

          if (this.uuid_fp) {
            let adolescente = this.adolescentes.find(x => x.tokenIdentificador == this.uuid_fp);
            this.personaControl.setValue(adolescente);
            this.personaControl.disable();
            this.formGroup.controls.adolescente.setValue(adolescente.idFichaIdentificacion);

            this.cargarPersonasRelacionadas();
          }
          else
            this.formGroup.controls.medio.disable();
        }

      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  cargarPersonasRelacionadas() {
    let fichaDTO = new FichaIdentificacionDTO();
    fichaDTO.idFichaIdentificacion = this.formGroup.controls.adolescente.value;
    this.personaService.obtenerPersonasRelacionadasPorIdFicha(fichaDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<PersonaRelacionadaDTO[]>) => {
        console.log(this.adolescenteSeleccionado);
        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo('Advertencia',
            response.mensaje
          );
          if (!this.adolescenteSeleccionado.email)
            return;
        }

        this.personasRelacionadas = response.data;

        if (this.adolescenteSeleccionado.email) {
          let informacionUbicaciones: InformacionUbicacionDTO[] = [];
          let informacionUbicacion = new InformacionUbicacionDTO();

          informacionUbicacion.tipoInformacionUbicacion = etiquetasModel.INFORMACION_PERSONAL_CORREO;
          informacionUbicacion.valor = this.adolescenteSeleccionado.email;

          informacionUbicaciones.push(informacionUbicacion);

          let personaRelacionadaDTO = new PersonaRelacionadaDTO();
          personaRelacionadaDTO.nombres = this.getNombreCompleto(this.adolescenteSeleccionado);
          personaRelacionadaDTO.informacionUbicaciones = informacionUbicaciones;

          if (!this.personasRelacionadas)
            this.personasRelacionadas = [];

          this.personasRelacionadas.push(personaRelacionadaDTO);

        }

        console.log(this.personasRelacionadas);

        this.formGroup.controls.medio.enable();

        if (this.formGroup.controls.medio.value)
          this.filterDestinatarios();
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  filterDestinatarios() {
    if (!this.formGroup.controls.medio.value) {
      this.personasFiltradas = [];
      return;
    }

    this.destinatariosSeleccionados = [];

    this.formGroup.controls.destinatario.enable();

    let medio = this.mediosNotificacion.find(m => m.nemonico == this.formGroup.controls.medio.value);

    this.personasFiltradas = this.personasRelacionadas
      .flatMap((p) => {
        const informacion = p.informacionUbicaciones?.find(
          (iu) => iu.tipoInformacionUbicacion === medio.nombre
        );
        if (informacion) {
          return {
            nombres: `${p.nombres}`.trim(),
            valorInformacionUbicacion: informacion.valor
          };
        }
        return null;
      })
      .filter(Boolean) as { nombres: string; valorInformacionUbicacion: string }[];

    if (this.personasFiltradas.length == 0)
      this.dialogMensajeService.mensajeError(
        "No hay personas relacionadas con información de contacto de tipo: " + medio.descripcion
      );
  }

  cambioTipo(event: any) {
    if (event.value == etiquetasModel.NOTIFICACION_OTROS) {
      this.formGroup.controls.asunto.setValue("");
      this.formGroup.controls.asunto.enable();
    }
    else
      this.formGroup.controls.asunto.setValue(this.tiposNotificacion.find(x => x.nemonico == event.value).descripcion);

    this.formGroup.controls.asunto.setValue(this.formGroup.controls.asunto.value + (this.adolescenteSeleccionado.centroIngreso ? (" - " + this.adolescenteSeleccionado.centroIngreso) : '') + " - " + this.getNombreCompleto(this.adolescenteSeleccionado));
  }

  addDestinatario(destinatario: { nombres: string; valorInformacionUbicacion: string }) {
    if (destinatario.nombres != "otros")
      if (destinatario.valorInformacionUbicacion)
        if (!this.destinatariosSeleccionados.includes(destinatario.valorInformacionUbicacion))
          this.destinatariosSeleccionados.push(destinatario.valorInformacionUbicacion);

    if (this.formGroup.get('medio').value != 'MEDIO_FISICO') {
      this.formGroup.get('entregado')?.setValue(this.destinatariosSeleccionados.join(",") || '');
    }
  }

  removeDestinatario(destinatario: string) {
    this.destinatariosSeleccionados = this.destinatariosSeleccionados.filter((d) => d !== destinatario);

    if (this.formGroup.get('medio').value != 'MEDIO_FISICO') {
      this.formGroup.get('entregado')?.setValue(this.destinatariosSeleccionados.join(",") || '');
    }
  }

  displayFn = (adolescente: FichaIdentificacionDTO): string => {
    return adolescente ? this.getNombreCompleto(adolescente) : '';
  }

  private _filter(name: string): any[] {
    const filterValue = name.toLowerCase();
    return this.adolescentes.filter(adolescente =>
      this.getNombreCompleto(adolescente).toLowerCase().includes(filterValue)
    );
  }

  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select(); // Selecciona todo el texto
  }

  getNombreCompleto(adolescente: FichaIdentificacionDTO): string {
    return `${adolescente.nombres} ${adolescente.apellidoPaterno} ${adolescente.apellidoMaterno}`;
  }

  ingresarCorreos() {
    if (this.correoForm.controls.correos) {
      this.correoForm.controls.correos.value.split(',').forEach((correo) => {
        this.destinatariosSeleccionados.push(correo);
      });
      this.correoForm.controls.correos.setValue('');
    }

    if (this.formGroup.get('medio').value != 'MEDIO_FISICO') {
      this.formGroup.get('entregado')?.setValue(this.destinatariosSeleccionados.join(",") || '');
    }
  }

  borrarDatos() {
    const persona = this.formGroup.controls.adolescente.value;

    this.formGroup.reset();
    this.personaControl.setValue("");
    this.formGroup.controls.medio.disable();
    this.formGroup.controls.destinatario.disable();
    this.destinatariosSeleccionados = [];
    this.subidaDeDocumentosComp?.borrarDatos();

    if (this.uuid_fp) {
      this.formGroup.controls.adolescente.setValue(persona);
      this.formGroup.controls.adolescente.disable();
      this.formGroup.controls.medio.enable();
    }
  }

  regresar() {
    this.location.back();
  }

  visualizarNotificacion() {
    this.formGroup.controls.adolescente.setValue(this.item.adolescente);
    this.formGroup.controls.adolescente.disable();
    this.formGroup.controls.asunto.setValue(this.item.asunto);
    this.formGroup.controls.asunto.disable();
    this.formGroup.controls.mensaje.setValue(this.item.cuerpo);
    this.formGroup.controls.mensaje.disable();
    this.formGroup.controls.destinatario.disable();

    this.item.destinatarios.split(',').forEach((destinatario, index) => {
      this.destinatariosSeleccionados.push(destinatario);
    });
  }

  obtenerFuncionario() {
    this.funcionarioService.obtenerFuncionarioDelUsuario(etiquetasModel.NEMONICO_MENU_NOTIFICACIONES_DOCUMENTACION).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {

          if (!response.exito) {
            return;
          }

          this.funcionarioActivo = response.data;
          if (this.esVisualizar) {
            this.cargarAdolescenteGuardado();
          } else {
            this.cargarAdolescentes();
          }
        },
        error: (error: any) => {
          console.log('Hubo un problema al recuperar el funcionario activo. Inténtalo de nuevo.');
        }
      }
    );
  }

  emailDomainValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null; // Si el campo está vacío, no hay error

      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

      return emailRegex.test(control.value) ? null : { invalidEmailDomain: true };
    };
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentosComp.page = event.pageIndex;
    this.tablaDocumentosComp.pageSize = event.pageSize;

    this.obtenerDocumentos();
  }
}
