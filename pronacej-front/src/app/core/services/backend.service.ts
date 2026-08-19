import { HttpClient } from '@angular/common/http';
import { inject, Injectable, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { environment } from 'environments/environment';
import { Observable, Subscriber } from 'rxjs';
import etiquetasModel from '../etiquetas.model';
import { BodyEncriptado } from '../model/both/bodyEncriptado.model';
import { FormDataRequest } from '../model/internos/FormDataRequest.model';
import { ErrorSpringBootResponse } from '../model/response/errorSpringBootResponse.model';
import { RespuestaPorDefecto } from '../model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from './dialog-mensaje.service';
import { ParametroDelSistemaDTO } from '../model/both/parametroDelSistemaDTO.model';
import { Buffer } from 'buffer';
import * as pako from 'pako';

@Injectable({
    providedIn: 'root',
})
export class BackendService implements OnInit {
    private path = environment.URL_SERVICIOS;

    public static claveAes: string = '';

    private httpClient = inject(HttpClient);
    private dialogMensajeService = inject(DialogMensajeService);

    constructor(private router: Router) {
        console.log(BackendService.claveAes);
    }

    async ngOnInit(): Promise<void> { }

    getParam(nemonico: string, tokenEmpresa: string) {
        let url = this.path + '/parametro-del-sistema/obtenerParam2';
        return this.httpClient.get(url, {
            params: {
                nemonico: nemonico ? nemonico : '',
                tokenIdentificadorEmpresa: tokenEmpresa ? tokenEmpresa : '',
            },
        });
    }

    /**
     * Crea un consumo post con un body encriptando
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param jwt string json web token a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    private async postJsonGeneralBodyEncriptado(
        endPoint: string,
        body: any,
        nemonicoMenu = ''
    ): Promise<Observable<Object>> {
        let url = this.path + endPoint;

        return this.httpClient.post(url, await this.crearBodyEncriptado(body), {
            headers: this.buildHeader(nemonicoMenu),
        });
    }

    /**
     * Crea un consumo post con un body comprimido encriptando
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param jwt string json web token a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    private async postJsonGeneralBodyEncriptadoComprimido(
        endPoint: string,
        body: any,
        nemonicoMenu = ''
    ): Promise<Observable<Object>> {
        let url = this.path + endPoint;

        return this.httpClient.post(url, await this.crearBodyEncriptadoComprimido(body), {
            headers: this.buildHeader(nemonicoMenu),
        });
    }

    /**
     * Crea un consumo post con un body encriptando
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param jwt string json web token a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    private async postJsonGeneralBodyEncriptadoRSA(
        endPoint: string,
        body: any,
        nemonicoMenu = ''
    ): Promise<Observable<Object>> {
        let url = this.path + endPoint;

        return this.httpClient.post(url, await this.crearBodyEncriptadoRSA(body), {
            headers: this.buildHeader(nemonicoMenu),
        });
    }

    postJsonGeneralBodyEncriptado2(
        endPoint: string,
        body: any,
        nemonicoMenu = ''
    ): Observable<Object> {
        return new Observable((subscriber: Subscriber<Object>) => {
            this.crearBodyEncriptado(body)
                .then((bodyEncriptado) => {
                    let url = this.path + endPoint;
                    this.httpClient
                        .post(url, bodyEncriptado, {
                            headers: this.buildHeader(nemonicoMenu),
                        })
                        .subscribe({
                            next: (object: Object) => {
                                subscriber.next(object);
                                subscriber.complete();
                            },
                            error: (error: any) => {
                                subscriber.error(error);
                                subscriber.complete();
                            },
                        });
                })
                .catch((error: any) => {
                    console.error(error);
                    subscriber.error(error);
                    subscriber.complete();
                });
        });
    }

    postBlobGeneralBodyEncriptado(
        endPoint: string,
        body: any,
        nemonicoMenu = ''
    ): Observable<ArrayBuffer> {
        return new Observable((subscriber: Subscriber<ArrayBuffer>) => {
            this.crearBodyEncriptado(body)
                .then((bodyEncriptado) => {
                    let url = this.path + endPoint;
                    this.httpClient
                        .post(url, bodyEncriptado, {
                            headers: this.buildHeader(nemonicoMenu),
                            responseType: 'arraybuffer',
                        })
                        .subscribe({
                            next: (response: ArrayBuffer) => {
                                subscriber.next(response);
                                subscriber.complete();
                            },
                            error: (error: any) => {
                                subscriber.error(error);
                                subscriber.complete();
                            },
                        });
                })
                .catch((error: any) => {
                    console.error(error);
                    subscriber.error(error);
                    subscriber.complete();
                });
        });
    }

    /**
     * Crea un consumo post con un body form Data con un valor del body encriptado
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param formDataRequest FormDataRequest<T> body a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<BodyEncriptado>
     */
    postFormDataBodyEncriptado<T>(
        endPoint: string,
        formDataRequest: FormDataRequest<T>,
        nemonicoMenu = ''
    ): Observable<BodyEncriptado> {
        let url = this.path + endPoint;
        let formData = new FormData();
        formData.append(formDataRequest.data.clave, formDataRequest.data.valor);

        return new Observable((subscriber: Subscriber<BodyEncriptado>) => {
            this.crearBodyEncriptado(formDataRequest.body.valor)
                .then((bodyEncriptado) => {
                    formData.append(
                        formDataRequest.body.clave,
                        JSON.stringify(bodyEncriptado)
                    );
                    this.httpClient
                        .post<BodyEncriptado>(url, formData, {
                            headers: this.buildHeader(nemonicoMenu),
                        })
                        .subscribe({
                            next: (response: BodyEncriptado) => {
                                subscriber.next(response);
                                subscriber.complete();
                            },
                            error: (error: any) => {
                                subscriber.error(error);
                                subscriber.complete();
                            },
                        });
                })
                .catch((error: any) => {
                    console.error(error);
                    subscriber.error(error);
                    subscriber.complete();
                });
        });
    }

    /**
     * Crea un consumo post con un body form Data con un valor del body encriptado
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param formData FormData body a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<BodyEncriptado>
     */
    postFormDataBodyEncriptado2(
        endPoint: string,
        formData: FormData,
        nemonicoMenu = ''
    ): Observable<BodyEncriptado> {
        let url = this.path + endPoint;

        return this.httpClient.post<BodyEncriptado>(url, formData, {
            headers: this.buildHeader(nemonicoMenu),
        });
    }

    /**
     * Crea un consumo post con un body encriptando y un grupo de parámetros de consulta
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param params any grupo de parámetros a consultar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    postJsonGeneralBodyEncriptadoParam(
        endPoint: string,
        body: any,
        params: any = {},
        nemonicoMenu = ''
    ): Observable<Object> {
        let url = this.path + endPoint;
        let bodyEncriptado = new BodyEncriptado();
        let bodyFinalString = JSON.stringify(body);
        bodyEncriptado.body = bodyFinalString;
        bodyEncriptado.llave = new Date().getTime() + 'Latinus';

        bodyEncriptado.encriptarData(BackendService.claveAes);

        return this.httpClient.post(url, bodyEncriptado, {
            headers: this.buildHeader(nemonicoMenu),
            params: params,
        });
    }

    /**
     * Crea un consumo post final con un body encriptando y un grupo de parámetros de consulta
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    postFinal<T>(
        endPoint: string,
        body: any,
        nemonicoMenu?: string
    ): Observable<T> {
        return new Observable((subs: Subscriber<T>) => {
            this.postJsonGeneralBodyEncriptado(endPoint, body, nemonicoMenu)
                .then((observer: Observable<Object>) => {
                    observer.subscribe({
                        next: async (bodyEncriptado: BodyEncriptado) => {
                            // Emitimos la respuesta al suscriptor
                            subs.next(
                                // Desencriptamos la respuesta
                                await this.desencriptarBdyEncriptado<T>(
                                    bodyEncriptado
                                )
                            );
                            subs.complete();
                        },
                        error: (error: any) => {
                            subs.error(error);
                            subs.complete();
                        },
                    });
                })
                .catch((error: any) => {
                    subs.error(error);
                    this.checkError(error, false);
                    subs.complete();
                });
        });
    }

    /**
     * Crea un consumo post final con un body comprimido encriptando y un grupo de parámetros de consulta
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    postFinalCompressed<T>(
        endPoint: string,
        body: any,
        nemonicoMenu?: string
    ): Observable<T> {
        return new Observable((subs: Subscriber<T>) => {
            this.postJsonGeneralBodyEncriptadoComprimido(endPoint, body, nemonicoMenu)
                .then((observer: Observable<Object>) => {
                    observer.subscribe({
                        next: async (bodyEncriptado: BodyEncriptado) => {
                            // Emitimos la respuesta al suscriptor
                            subs.next(
                                // Desencriptamos la respuesta
                                await this.desencriptarBdyEncriptado<T>(
                                    bodyEncriptado
                                )
                            );
                            subs.complete();
                        },
                        error: (error: any) => {
                            subs.error(error);
                            subs.complete();
                        },
                    });
                })
                .catch((error: any) => {
                    subs.error(error);
                    this.checkError(error, false);
                    subs.complete();
                });
        });
    }

    /**
     * Crea un consumo post final con un body encriptando con RSA y un grupo de parámetros de consulta
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param body any body a enviar
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    postFinalRSA<T>(
        endPoint: string,
        body: any,
        nemonicoMenu?: string
    ): Observable<T> {
        return new Observable((subs: Subscriber<T>) => {
            this.postJsonGeneralBodyEncriptadoRSA(endPoint, body, nemonicoMenu)
                .then((observer: Observable<T>) => {
                    observer.subscribe({
                        next: (response: T) => {
                            /*
                            // Emitimos la respuesta al suscriptor
                            subs.next(
                                // Desencriptamos la respuesta
                                await this.desencriptarBdyEncriptado<T>(
                                    bodyEncriptado
                                )
                            );
                            */

                            subs.next(response);
                            subs.complete();
                        },
                        error: (error: any) => {
                            subs.error(error);
                            subs.complete();
                        },
                    });
                })
                .catch((error: any) => {
                    subs.error(error);
                    this.checkError(error, false);
                    subs.complete();
                });
        });
    }

    /**
     * Crea un consumo get final
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param params any {}
     * @param nemonicoMenu string identificador del menu
     *
     * @return Observable<Object>
     */
    getFinal<T>(
        endPoint: string,
        params: any = {},
        nemonicoMenu?: string
    ): Observable<T> {
        return new Observable((subs: Subscriber<T>) => {
            this.getJsonGeneralBodyEncriptado(
                endPoint,
                params,
                nemonicoMenu
            ).subscribe({
                next: async (bodyEncriptado: BodyEncriptado) => {
                    // Emitimos la respuesta al suscriptor
                    subs.next(
                        // Desencriptamos la respuesta
                        await this.desencriptarBdyEncriptado(bodyEncriptado)
                    );
                    subs.complete();
                },
                error: (error: any) => {
                    subs.error(error);
                    subs.complete();
                },
            });
        });
    }

    async crearBodyEncriptado(body: any): Promise<BodyEncriptado> {
        let bodyEncriptado = new BodyEncriptado();
        let bodyFinalString = JSON.stringify(body);
        bodyEncriptado.body = bodyFinalString;
        bodyEncriptado.llave = new Date().getTime() + '-Latinus';

        await bodyEncriptado.encriptarData(BackendService.claveAes);
        return bodyEncriptado;
    }

    async crearBodyEncriptadoComprimido(body: any): Promise<BodyEncriptado> {
        let bodyEncriptado = new BodyEncriptado();
        //let bodyFinalString = JSON.stringify(body);
        const compressed = pako.gzip(JSON.stringify(body), { level: 9 });
        const bodyFinalString = btoa(String.fromCharCode(...compressed));
        bodyEncriptado.body = bodyFinalString;

        bodyEncriptado.llave = new Date().getTime() + '-Latinus';

        await bodyEncriptado.encriptarData(BackendService.claveAes);
        return bodyEncriptado;
    }

    async crearBodyEncriptadoRSA(body: any): Promise<BodyEncriptado> {
        let bodyEncriptado = new BodyEncriptado();
        let bodyFinalString = JSON.stringify(body);
        bodyEncriptado.body = bodyFinalString;

        let param = await this.getParametroDelsistema(etiquetasModel.NEMONICO_RSA_PUBLICA_FRONT_END);

        await bodyEncriptado.encriptarDataRSA(
            param.valor
        );
        return bodyEncriptado;
    }

    async getParametroDelsistema(nemonico: string, tokenEmpresa = "") {
        let promise = new Promise<ParametroDelSistemaDTO>((resolve, reject) => {
            this.getParam(nemonico, tokenEmpresa).subscribe({
                next: (response: RespuestaPorDefecto<string>) => {
                    if (!response.exito) {
                        console.error(response.mensaje);
                        return;
                    }
                    let paramDTOString = Buffer.from(
                        response.data,
                        'base64'
                    ).toString('binary');

                    resolve(JSON.parse(paramDTOString) as ParametroDelSistemaDTO);
                },
                error: (error: any) => {
                    reject(error);
                }
            });
        });

        return promise;

    }

    //Se asume que la clave ya se tiene
    async desencriptarBdyEncriptado<T>(
        bodyEncriptado: BodyEncriptado
    ): Promise<T> {
        let body = new BodyEncriptado();
        body.body = bodyEncriptado.body;
        body.llave = bodyEncriptado.llave;

        return await body.desencriptarData<T>(BackendService.claveAes);
    }

    /**
     * Crea un consumo get
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param params any params a enviar en la url
     * @param tokenMenu string nemonico menu
     * @param tokenAccion string nemonico de accion del usuario
     *
     * @return Observable<Object>
     */
    private getJsonGeneralBodyEncriptado(
        endPoint: string,
        params: any = {},
        nemonicoMenu = ''
    ): Observable<any> {
        let url = this.path + endPoint;

        return this.httpClient.get(url, {
            headers: this.buildHeader(nemonicoMenu),
            params: params,
        });
    }

    getJsonGeneralBodyEncriptado2(
        endPoint: string,
        params: any = {},
        nemonicoMenu = ''
    ): Observable<any> {
        return this.getJsonGeneralBodyEncriptado(
            endPoint,
            params,
            nemonicoMenu
        );
    }

    /**
     * Crea un consumo get
     *
     * @param endPoint string wnsPoint de consumo al backend
     * @param params any params a enviar en la url
     * @param tokenMenu string nemonico menu
     * @param tokenAccion string nemonico de accion del usuario
     *
     * @return Observable<Object>
     */
    getBlobGeneralBodyEncriptado(
        endPoint: string,
        params: any = {},
        nemonicoMenu = ''
    ): Observable<ArrayBuffer> {
        let url = this.path + endPoint;

        return this.httpClient.get(url, {
            headers: this.buildHeader(nemonicoMenu),
            params: params,
            responseType: 'arraybuffer',
        });
    }

    private buildHeader(nemonicoMenu: string) {
        let jwt = localStorage.getItem(etiquetasModel.LS_TOKEN_DE_ACCESO);
        let tokenEmpresa = localStorage.getItem(
            etiquetasModel.LS_TOKEN_EMPRESA
        );
        return {
            nemonicoMenu: nemonicoMenu ? nemonicoMenu : '',
            Authorization: 'Bearer ' + (jwt ? jwt : ''),
            tokenIdentificadorEmpresa: tokenEmpresa ? tokenEmpresa : '',
        };
    }

    /**
     * Chequea el error enviado por el backend
     *
     * @param error error ocurrido enviado por el backend
     * @param mostrarError default true
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        let errorSpring = error?.error as ErrorSpringBootResponse;
        let response = error as BodyEncriptado;
        let response3 = error.error as BodyEncriptado;
        let response2 = error as RespuestaPorDefecto<any>;

        if (!environment.production) {
            console.error(error);
        }

        let mensaje = null;
        if (errorSpring?.error) {
            mensaje = errorSpring.error;
        } else if (response?.logOut) {
            mensaje = 'Tu sesión ha expirado';
        } else if (response2.logOut) {
            mensaje = 'Tu sesión ha expirado (app)';
        } else if (response2?.mensaje) {
            mensaje = response2.mensaje;
        } else if (response3?.body && response3?.llave) {
            try {
                let responseDecryt =
                    await this.desencriptarBdyEncriptado<
                        RespuestaPorDefecto<any>
                    >(response3);
                mensaje = responseDecryt?.mensaje;
            } catch (ex: any) {
                console.error(ex);
                mensaje = ex.toString();
            }
        } else if (
            !errorSpring?.error &&
            !response?.body &&
            !response2.titulo
        ) {
            this.router.navigateByUrl('/app-en-mantenimiento');
        } else {
            mensaje = etiquetasModel.MENSAJE_SERVICIO_NO_DISPONIBLE;
        }

        let ref: MatDialogRef<any>;
        if (mostrarError) {
            let titulo = response2?.titulo;
            //this.fuseServiceDialog.cerrarTodo();
            if (titulo) {
                ref = this.dialogMensajeService.mensajeErrorConTitulo(
                    titulo,
                    mensaje
                );
            } else {
                ref = this.dialogMensajeService.mensajeError(mensaje);
            }
        }

        if (response?.logOut || response2.logOut) {
            localStorage.removeItem(etiquetasModel.LS_TOKEN_DE_ACCESO);
            localStorage.removeItem(etiquetasModel.LS_TOKEN_EMPRESA);
            if (!environment.production) {
                console.log('Se cerro la sesión');
            }

            ref.afterClosed().subscribe({
                complete: () => {
                    location.reload();
                },
            });
        }

        return mensaje;
    }
}
