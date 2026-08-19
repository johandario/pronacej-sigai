import { Injectable } from '@angular/core';
import { ActualizacionDatosUsuarioRequest } from 'app/core/model/request/ActualizacionDatosUsuarioRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { User } from 'app/core/user/user.types';
import { environment } from 'environments/environment';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class UsuarioSistemaService {
    private path = '/usuario-sistema';

    constructor(
        private backendService: BackendService,
        private dialogMensajeService: DialogMensajeService
    ) {
        //this.backendService.actualizarClaves();
    }

    /**
     * Obtiene datos de un usuario que ya esta logeado en el sistema
     *
     * @param mostrarError default true
     *
     * @returns Observable<User>
     */
    obtenerDataDelUsuarioLogeado(mostrarError = true): Observable<User> {
        let endPoint = this.path + '/obtenerDataDelUsuarioLogeado';

        return new Observable((subscriber: Subscriber<User>) => {
            this.backendService
                .getFinal<RespuestaPorDefecto<User>>(endPoint, {}, '')
                .subscribe({
                    next: (response: RespuestaPorDefecto<User>) => {
                        if (!environment.production) {
                            console.log(response);
                        }

                        if (!response.exito) {
                            this.dialogMensajeService.mensajeError(
                                response.mensaje
                            );
                            subscriber.error(response);
                        } else {
                            subscriber.next(response.data);
                        }
                    },
                    error: (error: any) => {
                        this.backendService.checkError(error, mostrarError);
                        subscriber.error(error);
                    },
                    complete: () => {
                        subscriber.complete();
                    },
                });
        });
    }

    /**
     * Obtiene datos de un usuario que ya esta logeado en el sistema
     *
     * @param nemonicoMenu default true
     *
     * @returns Observable<User>
     */
    actualizarDatos(
        actualizacionDatosUsuarioRequest: ActualizacionDatosUsuarioRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<User>> {
        let endPoint = this.path + '/actualizarDatos';

        return this.backendService.postFinal(
            endPoint,
            actualizacionDatosUsuarioRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
