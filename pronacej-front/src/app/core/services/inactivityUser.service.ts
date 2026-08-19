import { Injectable } from '@angular/core';
import { jwtDecode, JwtPayload } from 'jwt-decode';
import { Observable, Subscriber } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import etiquetasModel from '../etiquetas.model';
import { ParametroDelSistemaDTO } from '../model/both/parametroDelSistemaDTO.model';
import { RespuestaPorDefecto } from '../model/response/RespuestaPorDefecto.model';
import { ParametroDelSistemaService } from './parametroDelSistema.service';
import { MatDialog } from '@angular/material/dialog';

@Injectable({ providedIn: 'root' })
export class InactivityUserService {
    private time: NodeJS.Timeout;

    /**
     * Constructor
     */
    constructor(
        private authService: AuthService,
        private parametroDelSistemaService: ParametroDelSistemaService,
        private dialog: MatDialog
    ) {}

    public verificarInactividad() {
        let observable = new Observable((subs: Subscriber<boolean>) => {
            this.authService.check().subscribe({
                next: (logeado: boolean) => {
                    if (logeado) {
                        let minutosPorefecto = 5;
                        this.parametroDelSistemaService
                            .obtenerParametroDelSistema(
                                etiquetasModel.PARAM_TIEMPO_INACTIVIDAD
                            )
                            .subscribe({
                                next: (
                                    resp: RespuestaPorDefecto<ParametroDelSistemaDTO>
                                ) => {
                                    if (resp.exito) {
                                        minutosPorefecto = +resp.data.valor;
                                    } else {
                                        console.error(
                                            resp,
                                            'Se toma el tiempo por defecto: ' +
                                                minutosPorefecto
                                        );
                                    }

                                    this.inactivityTime(minutosPorefecto);
                                    subs.next(true);
                                    subs.complete();
                                },
                                error: (error: any) => {
                                    console.error(
                                        error,
                                        'Se toma el tiempo por defecto: ' +
                                            minutosPorefecto
                                    );
                                    this.inactivityTime(minutosPorefecto);
                                    subs.next(true);
                                    subs.complete();
                                },
                            });
                    } else {
                        subs.next(true);
                        subs.complete();
                    }
                },
                error: (error: any) => {
                    console.error(error);
                    subs.next(true);
                    subs.complete();
                },
                complete: () => {
                    subs.next(true);
                    subs.complete();
                },
            });
        });

        return observable;
    }

    private inactivityTime(tiempoInactividadEnMinutos: number) {
        document.onload = () => this.resetTimer(tiempoInactividadEnMinutos);
        document.onmousemove = () =>
            this.resetTimer(tiempoInactividadEnMinutos);
        document.onmousedown = () =>
            this.resetTimer(tiempoInactividadEnMinutos); // touchscreen presses
        document.ontouchstart = () =>
            this.resetTimer(tiempoInactividadEnMinutos);
        document.onclick = () => this.resetTimer(tiempoInactividadEnMinutos); // touchpad clicks
        document.onkeydown = () => this.resetTimer(tiempoInactividadEnMinutos); // onkeypress is deprectaed
        document.addEventListener(
            'scroll',
            () => this.resetTimer(tiempoInactividadEnMinutos),
            true
        );
    }

    private logout() {
        // Cerrar todos los Dialog de Material que se encuentren abiertos
        if (this.dialog) {
            this.dialog.closeAll();
        }
        let mensaje =
            'Su tiempo de sesión se ha caducado se lo enviara al login';
        alert(mensaje);
        const decoded = jwtDecode<JwtPayload>(
            localStorage.getItem(etiquetasModel.LS_TOKEN_DE_ACCESO)
        );
        if (decoded && decoded.sub) {
            localStorage.clear();
            setTimeout(() => {
                location.reload();
            }, 700);
        }
    }

    private resetTimer(tiempoInactividadEnMinutos: number) {
        clearTimeout(this.time);
        // this.time = setTimeout(
        //     this.logout,
        //     tiempoInactividadEnMinutos * 60 * 1000
        // );
        this.time = setTimeout(() => this.logout(), tiempoInactividadEnMinutos * 60 * 1000);
    }
}
