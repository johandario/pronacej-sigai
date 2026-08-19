import { TextFieldModule } from '@angular/cdk/text-field';
import { Component, Input } from '@angular/core';
import { FormsModule, ReactiveFormsModule, UntypedFormGroup, UntypedFormBuilder, Validators, FormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { ActualizacionDatosUsuarioRequest } from 'app/core/model/request/ActualizacionDatosUsuarioRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { User } from 'app/core/user/user.types';
import { RecaptchaV3Service } from 'app/modules/seguridad/services/recaptchav3.service';
import { UsuarioSistemaService } from 'app/modules/seguridad/services/usuarioSistema.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-cuenta-usuario',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    TextFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatButtonModule,
  ], templateUrl: './cuenta-usuario.component.html',
  styleUrl: './cuenta-usuario.component.scss'
})
export class CuentaUsuarioComponent {
  accountForm: UntypedFormGroup;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PERFIL_USUARIO;
  @Input({ required: true }) declare tokenIdentificadorUsuario: string;

  datosUsuario: User;

  /**
   * Constructor
   */
  constructor(private _formBuilder: UntypedFormBuilder,
    private usuarioSistemaService: UsuarioSistemaService,
    private dialogMensajeService: DialogMensajeService,
    private recaptchaV3Service: RecaptchaV3Service
  ) { }

  /**
   * On init
   */
  ngOnInit(): void {
    // Create the form
    this.accountForm = this._formBuilder.group({
      name: new FormControl({ value: null, disabled: true }, []),
      username: [null, [Validators.required]],
      title: new FormControl({ value: null, disabled: true }, []),
      company: new FormControl({ value: null, disabled: true }, []),
      about: [null],
      email: [null, [Validators.email, Validators.required]],
      phone: [null, [Validators.required, Validators.pattern(etiquetasModel.REGEX_NUMBERS)]],
      country: ['usa'],
      language: ['english'],
    });

    this.obtenerData();
  }

  obtenerData() {
    this.usuarioSistemaService.obtenerDataDelUsuarioLogeado().subscribe(
      {
        next: (response: User) => {
          this.accountForm.get("name")?.setValue(response.name);
          this.accountForm.get("username")?.setValue(response.username);

          this.accountForm.get("title")?.setValue(response.rol);
          this.accountForm.get("company")?.setValue(response.empresa);
          this.accountForm.get("about")?.setValue(null);
          this.accountForm.get("email")?.setValue(response.email);

          this.accountForm.get("phone")?.setValue(response.telefono);

          this.datosUsuario = response;

        },
        error: (error: any) => {
          this.usuarioSistemaService.checkError(error);
        }
      }
    );
  }

  actualizarDatosDeUsuario() {

    if (!environment.production) {
      console.log(this.accountForm);
    }

    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      this.dialogMensajeService.mensajeError("Debes de llenar toda la información requerida para continuar");
      return;
    }

    this.recaptchaV3Service.execute(etiquetasModel.ACCION_ACTUALIZAR_DATOS_USUARIO_DEL_SISTEMA).subscribe(
      {
        next: (tokenRecaptchaV3: string) => {
          let request = new ActualizacionDatosUsuarioRequest();
          let userName = this.accountForm.get("username").value;
          let email = this.accountForm.get("email").value;
          let telefono = this.accountForm.get("phone").value;
          request.userName = this.datosUsuario.username != userName ? userName : null;
          request.email = this.datosUsuario.email != email ? email : null;
          request.telefono = this.datosUsuario.telefono != telefono ? telefono : null;
          request.tokenRecaptchaV3 = tokenRecaptchaV3;
          request.tokenIdentificador = this.tokenIdentificadorUsuario;
          request.esEdicion = true;

          this.usuarioSistemaService.actualizarDatos(
            request, this.nemonicoMenu
          ).subscribe(
            {
              next: (response: RespuestaPorDefecto<User>) => {

                if (!response.exito) {
                  this.usuarioSistemaService.checkError(response);
                  return;
                }

                this.dialogMensajeService.mensajeExitoso(
                  response.titulo, response.mensaje
                );
              },
              error: (error: any) => {
                this.usuarioSistemaService.checkError(error);
              }
            }
          );
        },
        error: (error: any) => {
          console.error(error);
          this.dialogMensajeService.mensajeError(etiquetasModel.MENSAJE_RECAPTCHAV3_ERROR);
        }
      }
    );



  }

}
