import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { ReseteoDePasswordDTO } from 'app/core/model/both/seguridad/ReseteoDePasswordDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { ReseteoDeContraseniaService } from 'app/modules/seguridad/services/reseteoContrasenia.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-olvido-contrasenia-cancelar',
  standalone: true,
  imports: [],
  templateUrl: './olvido-contrasenia-cancelar.component.html',
  styleUrl: './olvido-contrasenia-cancelar.component.scss'
})
export class OlvidoContraseniaCancelarComponent implements OnInit {
  tokenIdentificador: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_REESTABLECER_CONTRASENIA_CANCELAR;

  constructor(private reseteoDeContraseniaService: ReseteoDeContraseniaService,
    private activatedRoute: ActivatedRoute,
    private dialogMensajeService: DialogMensajeService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.tokenIdentificador = this.activatedRoute.snapshot.queryParamMap.get("token");
    if (!this.tokenIdentificador || this.tokenIdentificador == "" || this.tokenIdentificador.length == 0) {
      this.dialogMensajeService.mensajeError("Url inválida");
      this.regresarAlLogin();
      return;
    }

    this.cancelarReseteo();
  }

  private regresarAlLogin() {
    this.router.navigate(
      ["/sign-in"]
    );
  }

  cancelarReseteo() {
    let reseteoDePasswordDTO = new ReseteoDePasswordDTO();
    reseteoDePasswordDTO.tokenIdentificador = this.tokenIdentificador;

    this.reseteoDeContraseniaService.cancelarReseteoDePassword(
      reseteoDePasswordDTO,
      this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<ReseteoDePasswordDTO>) => {
          if (!environment.production) {
            console.log(response);
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
  }
}
