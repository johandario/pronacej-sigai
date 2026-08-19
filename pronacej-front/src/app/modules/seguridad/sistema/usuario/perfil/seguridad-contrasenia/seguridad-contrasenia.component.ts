import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, UntypedFormGroup, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { IngresoContraseniaComponent } from 'app/core/components/ingreso-contrasenia/ingreso-contrasenia.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { ActualizarDatosDeSeguridadDTO } from 'app/core/model/both/ActualizarDatosDeSeguridadDTO.model';
import { ParametroDelSistemaDTO } from 'app/core/model/both/parametroDelSistemaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PasswordUsuarioSistemaService } from 'app/modules/seguridad/services/passwordUsuarioSistema.service';
import { RecaptchaV3Service } from 'app/modules/seguridad/services/recaptchav3.service';
import { UsuarioSistemaEmpresaRolService } from 'app/modules/seguridad/services/usuarioSistemaEmpresaRol.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-seguridad-contrasenia',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSlideToggleModule,
    MatButtonModule,
    IngresoContraseniaComponent
  ],
  templateUrl: './seguridad-contrasenia.component.html',
  styleUrl: './seguridad-contrasenia.component.scss'
})
export class SeguridadContraseniaComponent implements OnInit, AfterViewInit {

  contraseniaForm: UntypedFormGroup;
  numeroDeDias: number = 30;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PERFIL_USUARIO;

  @ViewChild("ingresoContraseniaComp") ingresoContraseniaComp: IngresoContraseniaComponent;
  @Input({ required: true }) declare tokenIdentificadorUsuario: string;

  constructor(private _formBuilder: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private recaptchaV3Service: RecaptchaV3Service,
    private passwordUsuarioSistemaService: PasswordUsuarioSistemaService,
    private usuarioSistemaEmpresaRolService: UsuarioSistemaEmpresaRolService
  ) {
    // Create the form
    this.contraseniaForm = this._formBuilder.group(
      {
        contraseniaActual: [null, []],
        twoStep: [true],
        askPasswordChange: [false],
      }
    );
  }

  ngAfterViewInit(): void {
    /*
    this.ingresoContraseniaComp.obtenerCondicionesDeContrasenia(
      (listaRequisitos: ParametroDelSistemaDTO[]) => {
        let paramNDias = listaRequisitos.find(
          (value) => value.nemonico == etiquetasModel.PARAM_REGLA_CONTRASENIA_CAMBIO_CADA_N_DIAS
        );
        this.numeroDeDias = paramNDias ? +paramNDias.valor : 30;
      }
    );
    */
  }

  ngOnInit(): void {
    this.obtenerConfiguracionesDeSeguridad();

  }

  /**
    * Obten las configuraciones de seguridad de un usuario del sistema
    * 
    * @returns void
    */
  obtenerConfiguracionesDeSeguridad() {
    this.usuarioSistemaEmpresaRolService.obtenerInformacionDeSeguridad(
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<ActualizarDatosDeSeguridadDTO>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.usuarioSistemaEmpresaRolService.checkError(response);
            return;
          }

          let data = response.data;
          this.contraseniaForm.get("twoStep")?.setValue(data.habilitar2DoFactorDeAutenticacion);
          this.contraseniaForm.get("askPasswordChange")?.setValue(data.cambioDeContraseniaCadaNDias);

          this.numeroDeDias = data.diasExpiracionContrasenia;

        },
        error: (error: any) => {
          this.usuarioSistemaEmpresaRolService.checkError(error);
        }
      }
    );
  }

  /**
* Guarda la información de seguridad del usuario 
**  
* @returns void
*/
  guardarDatosDeSeguridad() {

    let datosContraseniaCambio = this.ingresoContraseniaComp.obtenerContrasenia();
    if (datosContraseniaCambio.contrasenia) {
      if (datosContraseniaCambio.contrasenia != datosContraseniaCambio.contraseniaRepetida) {
        this.dialogMensajeService.mensajeError("La contraseña nueva no coincide con la confirmada");
        return;
      }

      if (!datosContraseniaCambio.valida) {
        this.dialogMensajeService.mensajeError("La contraseña nueva no cumple con los requerimientos de seguridad");
        return;
      }
    }

    this.recaptchaV3Service.execute(
      etiquetasModel.ACCION_ACTUALIZAR_DATOS_USUARIO_DEL_SISTEMA
    ).subscribe(
      {
        next: (tokenRecapthaV3: string) => {
          let actualizarDatosDeSeguridadRequest = new ActualizarDatosDeSeguridadDTO();
          actualizarDatosDeSeguridadRequest.tokenIdentificador = this.tokenIdentificadorUsuario;
          actualizarDatosDeSeguridadRequest.cambioDeContraseniaCadaNDias = this.contraseniaForm.get("askPasswordChange").value;
          actualizarDatosDeSeguridadRequest.habilitar2DoFactorDeAutenticacion = this.contraseniaForm.get("twoStep").value;
          actualizarDatosDeSeguridadRequest.recaptchaV3 = tokenRecapthaV3;
          actualizarDatosDeSeguridadRequest.password = datosContraseniaCambio.contrasenia;
          actualizarDatosDeSeguridadRequest.passwordActual = this.contraseniaForm.get("contraseniaActual").value;
          actualizarDatosDeSeguridadRequest.passwordConfirm = datosContraseniaCambio.contraseniaRepetida;

          this.passwordUsuarioSistemaService.actualizarDatosDeSeguridad(
            actualizarDatosDeSeguridadRequest, etiquetasModel.NEMONICO_MENU_PERFIL_USUARIO
          ).subscribe(
            {
              next: (response: RespuestaPorDefecto<boolean>) => {
                if (!environment.production) {
                  console.log(response);
                }
                if (!response.exito) {
                  this.passwordUsuarioSistemaService.checkError(response);
                  return;
                }

                this.dialogMensajeService.mensajeExitoso(
                  response.titulo, response.mensaje
                );
              },
              error: (error: any) => {
                this.passwordUsuarioSistemaService.checkError(error);
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
