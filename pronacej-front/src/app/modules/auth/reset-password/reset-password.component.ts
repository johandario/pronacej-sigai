import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import {
  FormsModule,
  NgForm,
  ReactiveFormsModule,
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { fuseAnimations } from '@fuse/animations';
import { IngresoContraseniaComponent } from 'app/core/components/ingreso-contrasenia/ingreso-contrasenia.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { PasswordUserSistemaDTO } from 'app/core/model/both/seguridad/PasswordUserSistemaDTO.model';
import { ReseteoDePasswordDTO } from 'app/core/model/both/seguridad/ReseteoDePasswordDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { RecaptchaV3Service } from 'app/modules/seguridad/services/recaptchav3.service';
import { ReseteoDeContraseniaService } from 'app/modules/seguridad/services/reseteoContrasenia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'auth-reset-password',
  templateUrl: './reset-password.component.html',
  encapsulation: ViewEncapsulation.None,
  animations: fuseAnimations,
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    IngresoContraseniaComponent
  ],
})
export class AuthResetPasswordComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_REESTABLECER_CONTRASENIA_REESTABLECER;
  consultaAlBackend = false;

  tokenIdentificador: string;

  @ViewChild("ingresoContraseniaComponent") ingresoContraseniaComponent: IngresoContraseniaComponent;

  constructor(private reseteoDeContraseniaService: ReseteoDeContraseniaService,
    private activatedRoute: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private recaptchaV3Service: RecaptchaV3Service
  ) { }

  ngOnInit(): void {
    this.tokenIdentificador = this.activatedRoute.snapshot.queryParamMap.get("token");

    if (!this.tokenIdentificador || this.tokenIdentificador == "" || this.tokenIdentificador.length == 0) {
      this.dialogMensajeService.mensajeError("Url inválida");
      this.regresarAlLogin();
    }

  }

  private regresarAlLogin() {
    this.router.navigate(
      ["/sign-in"]
    );
  }

  validarReseteo() {
    let reseteoDePasswordDTO = new ReseteoDePasswordDTO();
    reseteoDePasswordDTO.tokenIdentificador = this.tokenIdentificador;

    this.reseteoDeContraseniaService.verificarReseteoDePassword(reseteoDePasswordDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<ReseteoDePasswordDTO>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.reseteoDeContraseniaService.checkError(response);
            this.regresarAlLogin();
          }
        },
        error: (error: any) => {
          this.reseteoDeContraseniaService.checkError(error);
        }
      }
    );
  }

  resetearContrasenia() {

    let responsePass = this.ingresoContraseniaComponent.obtenerContrasenia();
    if (!responsePass.valida) {
      this.dialogMensajeService.mensajeError(
        "La contraseña no cumple con las reglas de seguridad"
      );
      return;
    }

    let passwordUserSistemaDTO = new PasswordUserSistemaDTO();
    passwordUserSistemaDTO.password = responsePass.contrasenia;
    passwordUserSistemaDTO.passwordEncrypt = responsePass.contraseniaRepetida;

    if (passwordUserSistemaDTO.password != passwordUserSistemaDTO.passwordEncrypt) {
      this.dialogMensajeService.mensajeError("Las contraseñas no coinciden");
      return;
    }

    this.recaptchaV3Service.execute(
      etiquetasModel.ACCION_EJECUCION_RESETEO_PASSWORD
    ).subscribe(
      {
        next: (tokenRecaptchaV3: string) => {

          let reseteoDePasswordDTO = new ReseteoDePasswordDTO();
          reseteoDePasswordDTO.tokenIdentificador = this.tokenIdentificador;



          reseteoDePasswordDTO.passwordUserSistemaDTO = passwordUserSistemaDTO;
          reseteoDePasswordDTO.recaptchaV3 = tokenRecaptchaV3;

          this.reseteoDeContraseniaService.resetearPassword(reseteoDePasswordDTO, this.nemonicoMenu).subscribe(
            {
              next: (response: RespuestaPorDefecto<ReseteoDePasswordDTO>) => {
                if (!environment.production) {
                  console.log(response);
                }

                if (!response.exito) {
                  this.reseteoDeContraseniaService.checkError(response);
                }

                this.dialogMensajeService.mensajeExitoso(
                  response.titulo,
                  response.mensaje
                );

                this.regresarAlLogin();
              },
              error: (error: any) => {
                this.reseteoDeContraseniaService.checkError(error);
              }
            }
          );
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            etiquetasModel.MENSAJE_RECAPTCHAV3_ERROR
          );

          console.error(error);
        }
      }
    );


  }
}
