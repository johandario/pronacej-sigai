import { Injectable } from '@angular/core';
import etiquetasModel from 'app/core/etiquetas.model';
import { ParametroDelSistemaDTO } from 'app/core/model/both/parametroDelSistemaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Buffer } from 'buffer';
import { Observable, Subscriber } from 'rxjs';
declare var grecaptcha: any;

@Injectable({
    providedIn: 'root',
})
export class RecaptchaV3Service {
    private siteKey: string = '';
    private tokenEmpresa = localStorage.getItem(etiquetasModel.LS_TOKEN_EMPRESA)
        ? localStorage.getItem(etiquetasModel.LS_TOKEN_EMPRESA)
        : null;

    constructor(private backendService: BackendService) {
        this.obternerCredenciales();
    }

    /**
     * ejecuta una operación de recapthca v3 con la verificación del servicio activo
     *
     * @param action string accion que va a realizar el usuario
     *
     * @returns Observable<string>
     */
    execute(action: string): Observable<string> {
        const observable = new Observable((subscriber: Subscriber<string>) => {
            this.backendService
                .getParam(
                    etiquetasModel.PARAM_RECAPTCHA_V3_ACTIVO,
                    this.tokenEmpresa
                )
                .subscribe({
                    next: (response: RespuestaPorDefecto<string>) => {
                        if (!response.exito) {
                            console.error(response.mensaje);
                            return;
                        }

                        let paramDTOString = Buffer.from(
                            response.data,
                            'base64'
                        ).toString('binary');

                        let paramDTO: ParametroDelSistemaDTO =
                            JSON.parse(paramDTOString);
                        let recaptchaActivoV3 = paramDTO.valor == 'true';
                        if (recaptchaActivoV3) {
                            grecaptcha.ready(() => {
                                grecaptcha
                                    .execute(this.siteKey, {
                                        action: action,
                                    })
                                    .then((token: string) => {
                                        subscriber.next(token);
                                        subscriber.complete();
                                    })
                                    .catch((ex: any) => {
                                        console.error(ex);
                                        subscriber.error(ex);
                                        subscriber.complete();
                                    });
                            });
                        } else {
                            console.warn('Recapt v3 desactivado');
                            subscriber.next('Recaptcha v3 fake desactivado');
                            subscriber.complete();
                        }
                    },
                    error: (error: any) => {
                        subscriber.error(error);
                        this.backendService.checkError(error, false);
                        subscriber.complete();
                    },
                });
        });

        return observable;
    }

    /**
     * Obteniend la key del site para el uso del recapthaV3
     *
     *
     * @returns void
     */
    private obternerCredenciales(): void {
        this.backendService
            .getParam(
                etiquetasModel.PARAM_RECAPTCHA_V3_SITE_KEY,
                this.tokenEmpresa
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<string>) => {
                    if (!response.exito) {
                        console.error(response.mensaje);
                        return;
                    }

                    let paramDTOString = Buffer.from(
                        response.data,
                        'base64'
                    ).toString('binary');

                    let paramDTO: ParametroDelSistemaDTO =
                        JSON.parse(paramDTOString);
                    this.siteKey = paramDTO.valor;

                    var recaptchaScript = document.createElement('script');
                    recaptchaScript.setAttribute(
                        'src',
                        'https://www.google.com/recaptcha/api.js?render=' +
                        this.siteKey
                    );
                    document.body.appendChild(recaptchaScript);
                },
                error: (error: any) => {
                    this.backendService.checkError(error, false);
                },
            });
    }
}
