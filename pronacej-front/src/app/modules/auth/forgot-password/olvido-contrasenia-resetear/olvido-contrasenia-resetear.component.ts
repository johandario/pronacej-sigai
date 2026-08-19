import { Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { ActivatedRoute, Router } from '@angular/router';
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
  selector: 'app-olvido-contrasenia-resetear',
  standalone: true,
  imports: [
    IngresoContraseniaComponent,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './olvido-contrasenia-resetear.component.html',
  styleUrl: './olvido-contrasenia-resetear.component.scss'
})
export class OlvidoContraseniaResetearComponent implements OnInit {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_REESTABLECER_CONTRASENIA_REESTABLECER;
  contraseniaExpirada = false;
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

    let querysMap = this.activatedRoute.snapshot.queryParamMap;
    this.tokenIdentificador = querysMap.get("token");
    this.contraseniaExpirada = querysMap.get("contraseniaExpirada") === "true";

    if (!this.tokenIdentificador || this.tokenIdentificador == "" || this.tokenIdentificador.length == 0) {
      this.dialogMensajeService.mensajeError("Url inválida");
      this.regresarAlLogin();
    }

    this.validarReseteo();
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

    this.consultaAlBackend = true;
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
                this.consultaAlBackend = false;

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
                this.consultaAlBackend = false;

                this.reseteoDeContraseniaService.checkError(error);
              }
            }
          );
        },
        error: (error: any) => {
          this.consultaAlBackend = false;
          this.dialogMensajeService.mensajeError(
            etiquetasModel.MENSAJE_RECAPTCHAV3_ERROR
          );

          console.error(error);
        }
      }
    );


  }
}
