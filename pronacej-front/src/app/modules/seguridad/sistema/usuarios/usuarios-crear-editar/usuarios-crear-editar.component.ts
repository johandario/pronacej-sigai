import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { environment } from 'environments/environment.development';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { forkJoin } from 'rxjs';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { FuncionarioJerarquiaRolDTO } from 'app/core/model/both/seguridad/FuncionarioJerarquiaRolDTO.model';

@Component({
  selector: 'app-usuarios-crear-editar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    CommonModule,
    MatPaginatorModule,
    MatTableModule
  ],
  templateUrl: './usuarios-crear-editar.component.html',
  styleUrl: './usuarios-crear-editar.component.scss'
})
export class UsuariosCrearEditarComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;

  @Input() esFlujo: boolean = false;

  private _funcionarioData: FuncionarioDTO | null = null;
  @Input()
  set funcionarioData(data: FuncionarioDTO | null) {
    this._funcionarioData = data;
    if (this.esFlujo && data) {
      // Llenar el formulario con los datos recibidos
      console.log("Entro como flujo");
      this.crearUsuarioForm.get("nombres")?.setValue(data.nombres);
      this.crearUsuarioForm.get("apellidos")?.setValue(data.apellidos);
      this.crearUsuarioForm.get("tipoDeDocumento")?.setValue(data.tokenIdentificadorTipoDeDocumento);
      this.crearUsuarioForm.get("numeroDeDocumento")?.setValue(data.numeroDeDocumento);
      this.crearUsuarioForm.get("email")?.setValue(data.email);
      this.crearUsuarioForm.get("telefono")?.setValue(data.telefono);
      this.crearUsuarioForm.get("numeroDeCelular")?.setValue(data.numeroDeCelular);
      console.log('objeto', data.asignaciones)
      // 1) Cargo en el datasource el array que ya vino del back
      const asigns = data.asignaciones || [];
      this.dataSourceAsignaciones.data = asigns;

      // 2) Si quieres paginación:
      setTimeout(() => this.dataSourceAsignaciones.paginator = this.paginator);
    }
  }
  get funcionarioData(): FuncionarioDTO | null {
    return this._funcionarioData;
  }

  crearUsuarioForm: FormGroup;
  usuarioEdicion: UsuarioSistemaDTO;
  item: CreacionDeUsuarioSistema;

  esEdicion = false;

  listRoles: RolDTO[] = [];
  listaTiposDocumentos: CatalogoDTO[] = [];

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  listaDepartamentos: JerarquiaDTO[] = [];

  dataSourceAsignaciones = new MatTableDataSource<FuncionarioJerarquiaRolDTO>();

  /** Columnas y sus etiquetas */
  keyLabelsTableAsignaciones: Record<string, string> = {
    jerarquia: 'Jerarquía',
    rol: 'Rol'
  };

  @ViewChild(MatPaginator) paginator: MatPaginator;


  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private rolService: RolService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private catalogoService: CatalogoService,
    private jerarquiaService: JerarquiaService,
  ) {
    this.construirForm();

  }

  ngOnInit(): void {
    this.obtenerTiposDocumentoIdentificacion();
    this.obtenerRoles();
    this.cargarDatosCentros();

    this.item = history.state.item;

    if (this.item) {
      // Si existe el objeto, significa que estamos en modo edición
      this.esEdicion = true;
      this.empezarEdicion(this.item);
    }
  }

  construirForm() {
    this.crearUsuarioForm = this.fb.group(
      {
        nombres: [null, [Validators.required, this.noSpacesValidator]],
        apellidos: [null, [Validators.required, this.noSpacesValidator]],
        userName: [null, [Validators.required, this.noSpacesValidator]],
        email: [null, [Validators.required, Validators.email, this.emailDomainValidator()]],
        telefono: [null, [Validators.required, this.noSpacesValidator]],
        tipoDeDocumento: [0, [Validators.required]],
        numeroDeDocumento: [null, [Validators.required, this.noSpacesValidator]],
        numeroDeCelular: [null, [Validators.required, this.noSpacesValidator]],
        // tokenIdentificadorRol: [null, [Validators.required, this.noSpacesValidator]]
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

  private obtenerValor(key: string) {
    return this.crearUsuarioForm.get(key)?.value;
  }

  empezarEdicion(creacionDeUsuarioSistemaEditar: CreacionDeUsuarioSistema) {
    this.esEdicion = true;
    this.usuarioEdicion = creacionDeUsuarioSistemaEditar;
    this.crearUsuarioForm.get("nombres")?.setValue(creacionDeUsuarioSistemaEditar.nombres);
    this.crearUsuarioForm.get("apellidos")?.setValue(creacionDeUsuarioSistemaEditar.apellidos);
    this.crearUsuarioForm.get("userName")?.setValue(creacionDeUsuarioSistemaEditar.userName);
    this.crearUsuarioForm.get("email")?.setValue(creacionDeUsuarioSistemaEditar.email);
    this.crearUsuarioForm.get("telefono")?.setValue(creacionDeUsuarioSistemaEditar.telefono);
    this.crearUsuarioForm.get("tipoDeDocumento")?.setValue(creacionDeUsuarioSistemaEditar.tokenIdentificadorTipoDeDocumento);
    this.crearUsuarioForm.get("numeroDeDocumento")?.setValue(creacionDeUsuarioSistemaEditar.numeroDeDocumento);
    this.crearUsuarioForm.get("numeroDeCelular")?.setValue(creacionDeUsuarioSistemaEditar.numeroDeCelular);
    // this.crearUsuarioForm.get("tokenIdentificadorRol")?.setValue(creacionDeUsuarioSistemaEditar.tokenIdentificadorRol);
    //this.crearUsuarioForm.get("userName")?.disable();
    //this.crearUsuarioForm.get("email")?.disable();
    this.crearUsuarioForm.get("tipoDeDocumento")?.disable();
    this.crearUsuarioForm.get("numeroDeDocumento")?.disable();
    console.log('objeto', creacionDeUsuarioSistemaEditar.asignaciones)
    // 1) Cargo en el datasource el array que ya vino del back
    const asigns = creacionDeUsuarioSistemaEditar.asignaciones || [];
    this.dataSourceAsignaciones.data = asigns;

    // 2) Si quieres paginación:
    setTimeout(() => this.dataSourceAsignaciones.paginator = this.paginator);
  }

  cancelar() {
    this.crearUsuarioForm.reset();
    this.usuarioEdicion = null;

    if (!this.esFlujo) {
      this.router.navigate(['../'], { relativeTo: this.route });
    } else {
      this.router.navigate(['/seguridad/sistema/funcionarios']);
    }
  }

  ejecutarAccion() {

    if (this.crearUsuarioForm.invalid) {
      return;
    }

    this.crearUsuarioForm.disable();

    let usuarioCreacion = new CreacionDeUsuarioSistema();
    // usuarioCreacion.tokenIdentificadorRol = this.obtenerValor("tokenIdentificadorRol");
    usuarioCreacion.apellidos = this.obtenerValor("apellidos");
    usuarioCreacion.nombres = this.obtenerValor("nombres");
    usuarioCreacion.userName = this.obtenerValor("userName");
    usuarioCreacion.email = this.obtenerValor("email");
    usuarioCreacion.telefono = this.obtenerValor("telefono");
    usuarioCreacion.tokenIdentificadorTipoDeDocumento = this.obtenerValor("tipoDeDocumento");
    usuarioCreacion.numeroDeDocumento = this.obtenerValor("numeroDeDocumento");
    usuarioCreacion.numeroDeCelular = this.obtenerValor("numeroDeCelular");
    usuarioCreacion.tokenIdentificador = this.usuarioEdicion?.tokenIdentificador;
    usuarioCreacion.esEdicion = this.esEdicion;

    usuarioCreacion.asignaciones = this.dataSourceAsignaciones.data.map(asig => {
      // si tu CreacionDeUsuarioSistema espera la misma forma
      // de FuncionarioJerarquiaRolDTO, puedes pasarlo directo:
      const dto = new FuncionarioJerarquiaRolDTO();
      dto.tokenIdentificadorJerarquia = asig.tokenIdentificadorJerarquia;
      dto.tokenIdentificadorRol = asig.tokenIdentificadorRol;
      return dto;
    });

    this.authSerguridadServicio.crearUsuario(usuarioCreacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CreacionDeUsuarioSistema>) => {
          this.crearUsuarioForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);

            return;
          }
          this.dialogMensajeService.mensajeExitoso(response.titulo, "Se completó el proceso de registro exitosamente. <br>\"La contraseña fue enviada al correo electrónico registrado.\"")
            .afterClosed().subscribe(result => {
              this.cancelar();
            });
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
          this.crearUsuarioForm.enable();
        }
      }
    );
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
          this.listRoles.sort((a, b) => a.nombre.localeCompare(b.nombre));
        },
        error: (error: any) => {
          this.rolService.checkError(error);
        }
      }
    );
  }

  obtenerTiposDocumentoIdentificacion() {
    this.catalogoService.obtenerHijos("TIPO_DOCUMENTO_IDENTIFICACION", this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaTiposDocumentos = response.data;
          console.log(this.listaTiposDocumentos);
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
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

  getKeyLabelsTableAsignaciones(): string[] {
    return Object.keys(this.keyLabelsTableAsignaciones);
  }

  getRolesDisponibles(): RolDTO[] {
    return this.listRoles;
  }

  onRolChange(asig: FuncionarioJerarquiaRolDTO, nuevoTokenRol: string) {
    asig.tokenIdentificadorRol = nuevoTokenRol;
  }
}
