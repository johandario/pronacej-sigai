import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { MatSelectModule } from '@angular/material/select';
import { environment } from 'environments/environment';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-usuario-crear-editar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './usuario-crear-editar.component.html',
  styleUrl: './usuario-crear-editar.component.scss'
})
export class UsuarioCrearEditarComponent implements OnInit {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;

  crearUsuarioForm: FormGroup;
  usuarioEdicion: UsuarioSistemaDTO;

  esEdicion = false;

  listRoles: RolDTO[] = [];

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  constructor(
    private fb: FormBuilder,
    private rolService: RolService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService
  ) {
    this.construirForm();

  }

  ngOnInit(): void {
    this.obtenerRoles();
  }

  construirForm() {
    this.crearUsuarioForm = this.fb.group(
      {
        nombres: [null, [Validators.required]],
        apellidos: [null, [Validators.required]],
        userName: [null, [Validators.required]],
        email: [null, [Validators.required, Validators.email]],
        telefono: [null, [Validators.required]],
        numeroDeDocumento: [null, [Validators.required]],
        numeroDeCelular: [null, [Validators.required]],
        tokenIdentificadorRol: [null, [Validators.required]]
      }
    );
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
    this.crearUsuarioForm.get("numeroDeDocumento")?.setValue(creacionDeUsuarioSistemaEditar.numeroDeDocumento);
    this.crearUsuarioForm.get("numeroDeCelular")?.setValue(creacionDeUsuarioSistemaEditar.numeroDeCelular);
    this.crearUsuarioForm.get("tokenIdentificadorRol")?.setValue(creacionDeUsuarioSistemaEditar.tokenIdentificadorRol);
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.crearUsuarioForm.reset();
    this.usuarioEdicion = null;

    this.canceloEdicion.emit(true);
  }

  ejecutarAccion() {

    if (this.crearUsuarioForm.invalid) {
      return;
    }

    this.crearUsuarioForm.disable();

    let usuarioCreacion = new CreacionDeUsuarioSistema();
    usuarioCreacion.tokenIdentificadorRol = this.obtenerValor("tokenIdentificadorRol");
    usuarioCreacion.apellidos = this.obtenerValor("apellidos");
    usuarioCreacion.nombres = this.obtenerValor("nombres");
    usuarioCreacion.userName = this.obtenerValor("userName");
    usuarioCreacion.email = this.obtenerValor("email");
    usuarioCreacion.telefono = this.obtenerValor("telefono");
    usuarioCreacion.numeroDeDocumento = this.obtenerValor("numeroDeDocumento");
    usuarioCreacion.numeroDeCelular = this.obtenerValor("numeroDeCelular");
    usuarioCreacion.tokenIdentificador = this.usuarioEdicion?.tokenIdentificador;
    usuarioCreacion.esEdicion = this.esEdicion;

    this.authSerguridadServicio.crearUsuario(usuarioCreacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CreacionDeUsuarioSistema>) => {
          this.crearUsuarioForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.authSerguridadServicio.checkError(response);

            return;
          }
          this.cancelarEdicion();
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
          this.crearUsuarioForm.enable();
        }
      }
    );
  }

  obtenerRoles() {
    this.rolService.obtenerRoles(etiquetasModel.NEMONICO_MENU_USUARIO).subscribe(
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
}
