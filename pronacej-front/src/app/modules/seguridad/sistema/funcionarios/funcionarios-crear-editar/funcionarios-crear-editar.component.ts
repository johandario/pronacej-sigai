import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormArray, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { environment } from 'environments/environment.development';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { Subscription, debounceTime, distinctUntilChanged, filter, forkJoin } from 'rxjs';
import { CargosJerarquiaDTO } from 'app/core/model/both/cargosJerarquiaDTO.model';
import { CargosJerarquiaService } from 'app/modules/seguridad/services/cargosJerarquia.service';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FuncionarioJerarquiaRolDTO } from 'app/core/model/both/seguridad/FuncionarioJerarquiaRolDTO.model';
import { UtilsService } from 'app/core/services/utils.service';

@Component({
  selector: 'app-funcionarios-crear-editar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    CommonModule,
    MatFormFieldModule,
  ],
  templateUrl: './funcionarios-crear-editar.component.html',
  styleUrl: './funcionarios-crear-editar.component.scss'
})
export class FuncionariosCrearEditarComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FUNCIONARIO;

  @Input() esFlujo: boolean = false;

  funcionarioEdicion: FuncionarioDTO;
  crearFuncionarioForm: FormGroup;
  item: FuncionarioDTO;

  esEdicion = false;
  esVisualizacion = false;
  tokenCargo: string;

  listaTiposDocumentos: CatalogoDTO[] = [];

  listaDepartamentos: JerarquiaDTO[] = [];

  listaCargosOriginal: CargosJerarquiaDTO[] = [
  ];
  listRoles: RolDTO[] = [];

  listaCargos: CargosJerarquiaDTO[] = [];
  private subscriptionReniec = new Subscription();
  private documentoSeleccionado: CatalogoDTO | null = null;

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();
  @Output() avanzarPaso = new EventEmitter<FuncionarioDTO>();


  rolesPorJerarquia: Record<string, RolDTO[]> = {};
  cargosPorJerarquia: Record<string, CargosJerarquiaDTO[]> = {};

  get asignaciones(): FormArray {
    return this.crearFuncionarioForm.get('asignaciones') as FormArray;
  }

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private funcionarioService: FuncionarioService,
    private cargosJerarquiaService: CargosJerarquiaService,
    private dialogMensajeService: DialogMensajeService,
    private location: Location,
    private router: Router,
    private catalogoService: CatalogoService,
    private jerarquiaService: JerarquiaService,
    private rolService: RolService,
    private utilsService: UtilsService,
  ) {
    this.construirForm();
  }

  async ngOnInit(): Promise<void> {
    await this.obtenerTiposDocumentoIdentificacion();
    await this.cargarDatosCentros();
    await this.cargarDatosCargosJerarquias();
    this.obtenerRoles();
    this.configurarAutofillReniec();

    this.item = history.state.item;

    if (this.item) {
      this.esVisualizacion = this.item.esVisualizacion;
      if (this.esVisualizacion) {
        this.crearFuncionarioForm.disable();
      }
      // Si existe el objeto, significa que estamos en modo edición
      this.esEdicion = true;
      this.empezarEdicion(this.item);
    }
    // this.crearFuncionarioForm.get('departamento')?.valueChanges.subscribe(tokenIdentificadorDepartamento => {
    //   this.filtrarCargos(tokenIdentificadorDepartamento);
    // });
  }

  async cargarDatosCentros(): Promise<void> {
    return new Promise((resolve) => {
      forkJoin({
        departamentosAbiertos: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("SOA", this.nemonicoMenu),
        departamentosCerrados: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("CJDR", this.nemonicoMenu),
        departamentosUapise: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("UAPISE", this.nemonicoMenu)
      }).subscribe({
        next: ({ departamentosAbiertos, departamentosCerrados, departamentosUapise }) => {
          if (!departamentosAbiertos.exito || !departamentosCerrados.exito || !departamentosUapise.exito) {
            this.jerarquiaService.checkError(departamentosAbiertos || departamentosCerrados || departamentosUapise);
            resolve();
            return;
          }

          if (!environment.production) {
            console.log(departamentosAbiertos.data, departamentosCerrados.data, departamentosUapise.data);
          }

          // Combinar resultados
          this.listaDepartamentos = [...departamentosAbiertos.data, ...departamentosCerrados.data, ...departamentosUapise.data];
          this.listaDepartamentos.sort((a, b) =>
            a.nombre.localeCompare(b.nombre)
          );
          resolve();
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
          resolve();
        }
      });
    });
  }

  async cargarDatosCargosJerarquias(): Promise<void> {
    return new Promise((resolve) => {
      this.cargosJerarquiaService.obtenerCargosJerarquias(
        this.nemonicoMenu
      ).subscribe({
        next: (respuesta: RespuestaPorDefecto<CargosJerarquiaDTO[]>) => {
          if (!respuesta.exito) {
            this.jerarquiaService.checkError(respuesta);
            resolve();
            return;
          }

          if (!environment.production) {
            console.log(respuesta.data);
          }

          this.listaCargosOriginal = respuesta.data;
          this.listaCargosOriginal.sort((a, b) =>
            a.nombre.localeCompare(b.nombre)
          );
          resolve();
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
          resolve();
        }
      });
    });
  }

  filtrarCargos(departamentoId: number) {
    this.listaCargos = this.listaCargosOriginal.filter(cargo => cargo.idJerarquia == departamentoId);
    // if (this.esEdicion) {
    //   this.crearFuncionarioForm.get("cargo")?.setValue(this.tokenCargo);
    // }
  }

  // filtrarCargos(tokenIdentificadorDepartamento: string) {
  //   this.listaCargos = this.listaCargosOriginal.filter(cargo => cargo.tokenIdentificador === tokenIdentificadorDepartamento);
  // }

  async obtenerTiposDocumentoIdentificacion(): Promise<void> {
    return new Promise((resolve) => {
      this.catalogoService.obtenerHijos("TIPO_DOCUMENTO_IDENTIFICACION", this.nemonicoMenu).subscribe({
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            resolve();
            return;
          }
          console.log(response);
          this.listaTiposDocumentos = response.data;
          resolve();
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
          resolve();
        }
      });
    });
  }

  construirForm() {
    this.crearFuncionarioForm = this.fb.group(
      {
        tipoDeDocumento: [0, [Validators.required]],
        numeroDeDocumento: ["", [Validators.required, this.noWhitespaceValidator]],
        nombres: ["", [Validators.required, this.noWhitespaceValidator]],
        apellidos: ["", [Validators.required, this.noWhitespaceValidator]],
        email: ["", [Validators.required, this.noWhitespaceValidator, Validators.email]],
        // departamento: [0, [Validators.required]],
        jerarquias: [[], [Validators.required]],
        cargo: [0, [Validators.required]],
        telefono: ["", [Validators.required, this.noWhitespaceValidator]],
        numeroDeCelular: ["", [Validators.required, this.noWhitespaceValidator]],
        // rol: [0, [Validators.required]],
        // asignaciones: this.fb.array(
        //   [],
        //   [Validators.minLength(1)]
        // )
      }
    );
  }

  private obtenerValor(key: string) {
    return this.crearFuncionarioForm.get(key)?.value;
  }

  private esDocumentoDni(): boolean {
    return this.documentoSeleccionado?.nemonico === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI';
  }

  private configurarAutofillReniec(): void {
    const numeroDeDocumentoControl = this.crearFuncionarioForm.get('numeroDeDocumento');
    const tipoDeDocumentoControl = this.crearFuncionarioForm.get('tipoDeDocumento');

    if (!numeroDeDocumentoControl || !tipoDeDocumentoControl) {
      return;
    }

    const tipoDocumentoSub = tipoDeDocumentoControl.valueChanges.subscribe((tipoDocumento) => {
      this.documentoSeleccionado = this.listaTiposDocumentos.find(
        (documento) => documento.tokenIdentificador === tipoDocumento
      ) || null;

      if (tipoDocumento === 'TIPO_DOCUMENTO_IDENTIFICACION_DNI') {
        numeroDeDocumentoControl.setValidators([
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(8),
          Validators.pattern(/^\d+$/),
          this.noWhitespaceValidator,
        ]);
      } else {
        numeroDeDocumentoControl.setValidators([
          Validators.required,
          this.noWhitespaceValidator,
        ]);
      }

      numeroDeDocumentoControl.updateValueAndValidity({ emitEvent: false });
    });

    const numeroDocumentoSub = numeroDeDocumentoControl.valueChanges.pipe(
      filter((value): value is string => !!value),
      debounceTime(700),
      distinctUntilChanged(),
      filter((value) => this.esDocumentoDni() && value.length === 8)
    ).subscribe((numeroIdentificacion) => {
      this.consultarReniec(numeroIdentificacion);
    });

    this.subscriptionReniec.add(tipoDocumentoSub);
    this.subscriptionReniec.add(numeroDocumentoSub);
  }

  private consultarReniec(numeroIdentificacion: string): void {
    this.utilsService.data(numeroIdentificacion).subscribe({
      next: (resp: any) => {
        const reniecData = Array.isArray(resp?.row) ? resp.row[0] : resp?.row;

        if (!reniecData) {
          return;
        }

        const apellidos = [reniecData.apellido_paterno, reniecData.apellido_materno]
          .filter((valor: string) => !!valor)
          .join(' ')
          .trim();

        if (reniecData.nombres) {
          this.crearFuncionarioForm.get('nombres')?.setValue(reniecData.nombres);
        }

        if (apellidos) {
          this.crearFuncionarioForm.get('apellidos')?.setValue(apellidos);
        }
      },
      error: (error: any) => {
        console.error('Error al consultar RENIEC:', error);
      },
    });
  }

  onDepartamentoChange(event: any) {
    // Resetear el cargo
    this.crearFuncionarioForm.get("cargo")?.setValue(null);
    // Filtrar la lista de cargos
    this.filtrarCargos(event.value);
  }

  empezarEdicion(funcionarioDTOEditar: FuncionarioDTO) {
    this.esEdicion = true;
    this.funcionarioEdicion = funcionarioDTOEditar;
    console.log(funcionarioDTOEditar);

    // Primero filtramos los cargos según el departamento
    this.filtrarCargos(funcionarioDTOEditar.idDepartamento);

    // Luego establecemos todos los valores del formulario
    this.crearFuncionarioForm.patchValue({
      cargo: funcionarioDTOEditar.tokenIdentificadorCargo,
      // departamento: funcionarioDTOEditar.idDepartamento,
      nombres: funcionarioDTOEditar.nombres,
      apellidos: funcionarioDTOEditar.apellidos,
      tipoDeDocumento: funcionarioDTOEditar.tokenIdentificadorTipoDeDocumento,
      numeroDeDocumento: funcionarioDTOEditar.numeroDeDocumento,
      email: funcionarioDTOEditar.email,
      telefono: funcionarioDTOEditar.telefono,
      numeroDeCelular: funcionarioDTOEditar.numeroDeCelular
    });

    this.crearFuncionarioForm.controls.numeroDeDocumento.disable();
    this.crearFuncionarioForm.controls.email.disable();

    // this.asignaciones.clear();

    const jerTs: string[] = (funcionarioDTOEditar.asignaciones || [])
      .map(a => a.tokenIdentificadorJerarquia);

    // 3) Y los meto directamente en el control 'jerarquias'
    this.crearFuncionarioForm.get('jerarquias')!.setValue(jerTs);


  }

  cancelar() {
    this.crearFuncionarioForm.reset();
    this.funcionarioEdicion = null;
    this.location.back();
  }

  ejecutarAccion() {

    if (this.crearFuncionarioForm.invalid) {
      const controls = this.crearFuncionarioForm.controls;
      Object.keys(controls).forEach(name => {
        if (controls[name].invalid) {
          console.log(`Control: ${name}, errores:`, controls[name].errors);
        }
      });
      return;
    }

    if (this.obtenerValor("apellidos") == ' ' || this.obtenerValor("nombres") == ' ') {
      this.crearFuncionarioForm.get("nombres")?.reset();
      this.crearFuncionarioForm.get("apellidos")?.reset();
      return;
    }

    console.log(this.crearFuncionarioForm.value.asignaciones);

    this.crearFuncionarioForm.disable();

    let funcionarioCreacion = new FuncionarioDTO();
    funcionarioCreacion.apellidos = this.obtenerValor("apellidos");
    funcionarioCreacion.nombres = this.obtenerValor("nombres");
    funcionarioCreacion.email = this.obtenerValor("email");
    funcionarioCreacion.telefono = this.obtenerValor("telefono");
    funcionarioCreacion.tokenIdentificadorTipoDeDocumento = this.obtenerValor("tipoDeDocumento");
    funcionarioCreacion.numeroDeDocumento = this.obtenerValor("numeroDeDocumento");
    funcionarioCreacion.numeroDeCelular = this.obtenerValor("numeroDeCelular");
    funcionarioCreacion.tokenIdentificadorCargo = this.obtenerValor("cargo");
    funcionarioCreacion.idDepartamento = this.obtenerValor("departamento");
    funcionarioCreacion.esEdicion = this.esEdicion;

    funcionarioCreacion.asignaciones = (this.crearFuncionarioForm.value.jerarquias as string[])
      .map(tokJer => {
        const dto = new FuncionarioJerarquiaRolDTO();
        dto.tokenIdentificadorJerarquia = tokJer;
        return dto;
      });

    this.funcionarioService.crearFuncionario(funcionarioCreacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          this.crearFuncionarioForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.funcionarioService.checkError(response);

            return;
          }
          if (!this.esFlujo) {
            this.cancelarEdicion();
            this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          } else {
            let ref = this.dialogMensajeService.mensajeConConfirmacion(
              response.titulo,
              response.mensaje + ".<br>¿Desea crear un usuario asociado al funcionario?"
            );

            ref.afterClosed().subscribe(
              {
                next: (resp: "confirmed" | "cancelled") => {
                  if (resp == "confirmed") {
                    // NO MOVERME DEL ROUTER Y COMUNICARSE CON EL COMPONENTE PADRE DEL MAT STEPPER PARA QUE CONTINUE AL PASO SIGUIENTE
                    this.avanzarPaso.emit(funcionarioCreacion);
                  } else {
                    this.cancelarEdicion();
                  }
                }
              }
            );

          }

        },
        error: (error: any) => {
          this.funcionarioService.checkError(error);
          this.crearFuncionarioForm.enable();
        }
      }
    );

  }


  cancelarEdicion() {
    this.esEdicion = false;
    this.crearFuncionarioForm.reset();
    // this.fichaIngresoDTO = null;

    if (!this.esFlujo) {
      this.router.navigate(['../'], { relativeTo: this.route });
    } else {
      this.router.navigate(['/seguridad/sistema/funcionarios']);
    }


  }

  ngOnDestroy(): void {
    this.subscriptionReniec.unsubscribe();
  }

  limpiarCaracteresEspeciales(event: any) {
    let valor: string = event.target.value;
    valor = valor.trim()
    valor = valor.replace(/[^a-zA-Z0-9]/g, '');
    event.target.value = valor.toUpperCase();
  }

  limpiarCaracteresEspecialesConEspacio(event: any) {
    let valor: string = event.target.value;
    valor = valor.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ ]/g, '');
    valor = valor.replace(/\s+/g, ' ');
    event.target.value = valor;
  }

  limpiarEspaciosBlanco(event: any) {
    let valor: string = event.target.value;
    event.target.value = valor.trim();
  }

  validarCaracteresCorreo(event: any) {
    let valor = event.target.value;
    // valor = valor.trim();
    valor = valor.replace(/[^a-zA-Z0-9@._-]/g, '');
    event.target.value = valor;
  }

  validarNumeros(event: any) {
    let valor = event.target.value;
    valor = valor.trim()
    valor = valor.replace(/[^0-9]/g, '');
    event.target.value = valor;
  }

  public noWhitespaceValidator(control: FormControl) {
    return (control.value || '').trim().length ? null : { 'whitespace': true };
  }

  obtenerRoles() {
    this.rolService.obtenerRoles(this.nemonicoMenu).subscribe(
      {
        next: (resp: RespuestaPorDefecto<RolDTO[]>) => {
          if (!environment.production) {
            console.log(resp);
          }

          if (!resp.exito) {
            this.rolService.checkError(resp);
            return;
          }

          this.listRoles = resp.data;
        },
        error: (error: any) => {
          this.rolService.checkError(error);
        }
      }
    );
  }

  addAsignacion(init?: {
    idJerarquia?: string,
    idRol?: string,
    // idCargo?: string
  }) {
    const grupo = this.fb.group({
      idJerarquia: [init?.idJerarquia || null, Validators.required],
      idRol: [init?.idRol || null, Validators.required],
      // idCargo: [init?.idCargo || null, Validators.required],
    });
    this.asignaciones.push(grupo);
  }

  removeAsignacion(i: number) {
    this.asignaciones.removeAt(i);
  }

  private getSelectedJerarquias(): string[] {
    return this.asignaciones.controls
      .map(ctrl => ctrl.get('idJerarquia')!.value as string)
      .filter(val => !!val);
  }

  getJerarquiasDisponibles(i: number): JerarquiaDTO[] {
    const seleccionadas = this.getSelectedJerarquias();
    const actual = this.asignaciones.at(i).get('idJerarquia')!.value;
    return this.listaDepartamentos.filter(d =>
      // siempre muestro la opción si coincide con la que ya tengo en esta fila...
      d.tokenIdentificador === actual ||
      // …o si NO ha sido seleccionada en ninguna otra fila
      !seleccionadas.includes(d.tokenIdentificador)
    );
  }



}
