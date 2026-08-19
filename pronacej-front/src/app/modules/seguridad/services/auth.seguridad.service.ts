import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { AdministrarMenuRolRequest } from 'app/core/model/request/administrarMenuRolRequest.model';
import { LoginRequest } from 'app/core/model/request/loginRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { LoginResponse } from 'app/core/model/response/LoginResponse.model';
import { LoginResponseFuse } from 'app/core/model/response/LoginResponseFuse.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class AuthSerguridadServicio {
    private path = '/auth';

    constructor(
        private backendService: BackendService,
        private router: Router,
        private dialogMensajeService: DialogMensajeService
    ) {
        //this.backendService.actualizarClaves();
    }

    /**
     * Verifica los errores al verificar permisos de una pantalla
     *
     * @param response RespuestaPorDefecto<MenuDTO> Respuesta del verificar permiso
     *
     * @returns no retorna
     */
    verificarErrorPermisoPantalla(response: RespuestaPorDefecto<MenuDTO>) {
        if (response.logOut) {
            this.router.navigateByUrl('/sign-out');
            return;
        }

        if (response.sinAcceso) {
            this.router.navigateByUrl('/sin-permisos');
            return;
        }

        this.router.navigateByUrl('/falta-verificacion');
    }

    /**
     * Verifica los permisos de una pantalla
     *
     * @param clavePantalla string con la clave o nemonico de la pantalla
     *
     * @returns Promise<RespuestaPorDefecto<MenuDTO>>
     */
    verificarPermisosPantallaConServicio(
        clavePantalla: string
    ): Promise<RespuestaPorDefecto<MenuDTO>> {
        const promise = new Promise<RespuestaPorDefecto<MenuDTO>>((resol) => {
            let load = this.dialogMensajeService.mensajeLoading(
                'Verificando permisos..'
            );
            this.verificarPermisoPantalla(clavePantalla).subscribe({
                next: (response: RespuestaPorDefecto<MenuDTO>) => {
                    load.close();
                    console.log(response);
                    if (response.codigoEstado == 403) {
                        this.verificarErrorPermisoPantalla(response);
                    }
                    resol(response);
                },
                error: (error: any) => {
                    load.close();
                    this.verificarErrorPermisoPantalla(error);
                },
            });
        });

        return promise;
    }

    /**
     * Verifica los permisos de una pantalla
     *
     * @param clavePantalla string con la clave o nemonico de la pantalla
     *
     * @returns Observable<RespuestaPorDefecto<MenuDTO>>
     */
    verificarPermisoPantalla(clavePantalla: string) {
        let endPoint = this.path + '/verificarPermisos';
        return this.backendService.postFinal<RespuestaPorDefecto<MenuDTO>>(
            endPoint,
            clavePantalla,
            ''
        );
    }

    /**
     * Logea a un usuario con sus credenciales en el sistema
     *
     * @para loginRequest LoginRequest objeto login request
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<LoginResponseFuse>
     */
    login(
        loginRequest: LoginRequest,
        nemonicoMenu: string
    ): Observable<LoginResponseFuse> {
        let endPoint = this.path + '/login';

        return new Observable((subscriber: Subscriber<LoginResponseFuse>) => {
            this.backendService
                .postFinalRSA<
                    RespuestaPorDefecto<LoginResponse>
                >(endPoint, loginRequest, nemonicoMenu)
                .subscribe({
                    next: (response: RespuestaPorDefecto<LoginResponse>) => {
                        if (response.exito) {
                            let data = response.data;
                            if (
                                etiquetasModel.LOGIN_CAMBIO_DE_CONTRASENIA ==
                                data.estado
                            ) {
                                this.router.navigateByUrl(
                                    '/olvido-de-contrasenia/resetear?token=' +
                                    data.userDataResponse
                                        .tokenReseteoContrasenia +
                                    '&contraseniaExpirada=true'
                                );

                                this.dialogMensajeService.mensajeError(
                                    'Tu inicio de sesión fue exitoso, pero' +
                                    ' debes de cambiar tu actual contraseña por que esta ya caduco'
                                );
                                subscriber.complete();
                            }

                            localStorage.setItem(
                                etiquetasModel.LS_TOKEN_EMPRESA,
                                response.data.tokenIdentificadorEmpresa
                            );
                        }
                        let loginResponseFuse = new LoginResponseFuse();
                        loginResponseFuse.accessToken = response.data?.jwt;
                        loginResponseFuse.message = response.mensaje;
                        loginResponseFuse.success = response.exito;
                        loginResponseFuse.tokenType = 'bearer';
                        loginResponseFuse.user =
                            response.data?.userDataResponse;
                        loginResponseFuse.estado = response.data?.estado ?? '';
                        loginResponseFuse.listaJerarquias = response.data?.listaJerarquias ?? [];
                        loginResponseFuse.tokenIdentificadorJerarquia = response.data?.tokenIdentificadorJerarquia ?? '';
                        loginResponseFuse.tokenIdentificadorEmpresa = response.data?.tokenIdentificadorEmpresa ?? '';
                        loginResponseFuse.tokenIdentificadorRolJerarquia = response.data?.tokenIdentificadorRolJerarquia ?? '';
                        loginResponseFuse.tokenPermisos = response.data?.tokenPermisos ?? '';

                        subscriber.next(loginResponseFuse);
                    },
                    error: (error: any) => {
                        subscriber.error(error);
                        this.backendService.checkError(error, false);
                    },
                });
        });
    }

    /**
     * Verifica un jwt valido
     *
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<LoginResponseFuse>
     */
    verificarJWT(nemonicoMenu = ''): Observable<LoginResponseFuse> {
        let endPoint = this.path + '/verificarJWT';
        return new Observable((subscriber: Subscriber<LoginResponseFuse>) => {
            this.backendService
                .getFinal<
                    RespuestaPorDefecto<LoginResponse>
                >(endPoint, {}, nemonicoMenu)
                .subscribe({
                    next: (response: RespuestaPorDefecto<LoginResponse>) => {
                        let loginResponseFuse = new LoginResponseFuse();

                        if (response.exito) {
                            localStorage.setItem(
                                etiquetasModel.LS_TOKEN_EMPRESA,
                                response.data.tokenIdentificadorEmpresa
                            );
                        }
                        loginResponseFuse.accessToken = response.data?.jwt;
                        loginResponseFuse.message = response.mensaje;
                        loginResponseFuse.success = response.exito;
                        loginResponseFuse.tokenType = 'bearer';
                        loginResponseFuse.user =
                            response.data?.userDataResponse;
                        subscriber.next(loginResponseFuse);
                        subscriber.complete();
                    },
                    error: (error: any) => {
                        subscriber.error(error);
                        subscriber.complete();
                    },
                });
        });
    }

    /**
     * Crea un usuario en el sistema con los datos enviados en el request
     *
     * @param creacionDeUsuarioSistema CreacionDeUsuarioSistema datos del usuario a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<CreacionDeUsuarioSistema>>
     */
    crearUsuario(
        creacionDeUsuarioSistema: CreacionDeUsuarioSistema,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<CreacionDeUsuarioSistema>> {
        let endPoint = this.path + '/crearUsuario';
        return this.backendService.postFinal<
            RespuestaPorDefecto<CreacionDeUsuarioSistema>
        >(endPoint, creacionDeUsuarioSistema, nemonicoMenu);
    }

    /**
     * Crea un usuario en el sistema con los datos enviados en el request
     *
     * @param paginacionRequest PaginacionRequest datos de paginacion
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<CreacionDeUsuarioSistema>>
     */
    obtenerUsuariosValidos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<CreacionDeUsuarioSistema>>
    > {
        let endPoint = this.path + '/obtenerUsuariosValidos';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<CreacionDeUsuarioSistema>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    /**
     * Obtener usuarios válidos
     *
     * @param paginacionRequest PaginacionRequest datos de paginacion
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<UsuarioSistemaDTO>>
     */
    obtenerUsuariosActivos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<UsuarioSistemaDTO>>> {
        let endPoint = this.path + '/obtenerUsuariosValidosActivos';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<UsuarioSistemaDTO>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    /**
     * Elimina un usuario del sistema a traves de su token de identificacion
     *
     * @param creacionDeUsuarioSistema CreacionDeUsuarioSistema datos del usuario a eliminar lo importante es que tenga el token de identificacion
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<CreacionDeUsuarioSistema>>
     */ eliminarUsuario(
        creacionDeUsuarioSistema: CreacionDeUsuarioSistema,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarUsuario';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            creacionDeUsuarioSistema,
            nemonicoMenu
        );
    }

    /**
     * Bloquea un usuario del sistema a traves de su token de identificacion
     *
     * @param creacionDeUsuarioSistema CreacionDeUsuarioSistema datos del usuario a eliminar lo importante es que tenga el token de identificacion
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<CreacionDeUsuarioSistema>>
     */
    bloquearUsuario(
        creacionDeUsuarioSistema: CreacionDeUsuarioSistema,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/bloquearUsuario';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            creacionDeUsuarioSistema,
            nemonicoMenu
        );
    }

    crearRol(
        creacionDeRol: CreacionDeRol,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<CreacionDeRol>> {
        let endPoint = this.path + '/crearRol';
        return this.backendService.postFinal<
            RespuestaPorDefecto<CreacionDeRol>
        >(endPoint, creacionDeRol, nemonicoMenu);
    }

    obtenerRolesValidos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>> {
        let endPoint = this.path + '/obtenerRolesValidos';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearMenu(
        menuDTO: MenuDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<MenuDTO>> {
        let endPoint = this.path + '/crearMenu';
        return this.backendService.postFinal<RespuestaPorDefecto<MenuDTO>>(
            endPoint,
            menuDTO,
            nemonicoMenu
        );
    }

    obtenerMenusValidos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<MenuDTO>>> {
        let endPoint = this.path + '/obtenerMenusValidos';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<MenuDTO>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    eliminarRol(creacionDeRol: CreacionDeRol, nemonicoMenu = '') {
        let endPoint = this.path + '/eliminarRol';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            creacionDeRol,
            nemonicoMenu
        );
    }

    bloquearRol(
        creacionDeRol: CreacionDeRol,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/bloquearRol';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            creacionDeRol,
            nemonicoMenu
        );
    }

    crearRelacionMenusRol(
        administrarMenuRolRequest: AdministrarMenuRolRequest,
        nemonicoMenu = ''
    ) {
        let endPoint = this.path + '/crearRelacionMenusRol';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            administrarMenuRolRequest,
            nemonicoMenu
        );
    }

    obtenerMenusAccesiblesPorRol(
        creacionDeRol: CreacionDeRol,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<MenuDTO[]>> {
        let endPoint = this.path + '/obtenerMenusAccesiblesPorRol';
        return this.backendService.postFinal<RespuestaPorDefecto<MenuDTO[]>>(
            endPoint,
            creacionDeRol,
            nemonicoMenu
        );
    }

    eliminarMenu(
        menuDTO: MenuDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarMenu';
        return this.backendService.postFinal(endPoint, menuDTO, nemonicoMenu);
    }

    obtenerRolesValidosPorValor(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CreacionDeRol>>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    
    /**
     * Logea a un usuario con sus credenciales en el sistema
     *
     * @para loginRequest LoginRequest objeto login request
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<LoginResponseFuse>
     */
    cambioJerarquia(
        loginRequest: any,
        nemonicoMenu: string
    ): Observable<LoginResponseFuse> {
        let endPoint = this.path + '/cambioJerarquia';

        return new Observable((subscriber: Subscriber<LoginResponseFuse>) => {
            this.backendService
                .postFinal<
                    RespuestaPorDefecto<LoginResponse>
                >(endPoint, loginRequest, nemonicoMenu)
                .subscribe({
                    next: (response: RespuestaPorDefecto<LoginResponse>) => {
                        if (response.exito) {
                            let data = response.data;


                            localStorage.setItem(
                                etiquetasModel.LS_TOKEN_EMPRESA,
                                response.data.tokenIdentificadorEmpresa
                            );
                        }
                        let loginResponseFuse = new LoginResponseFuse();
                        loginResponseFuse.accessToken = response.data?.jwt;
                        loginResponseFuse.message = response.mensaje;
                        loginResponseFuse.success = response.exito;
                        loginResponseFuse.tokenType = 'bearer';
                        loginResponseFuse.user =
                            response.data?.userDataResponse;
                        loginResponseFuse.estado = response.data?.estado ?? '';
                        loginResponseFuse.listaJerarquias = response.data?.listaJerarquias ?? [];
                        loginResponseFuse.tokenIdentificadorJerarquia = response.data?.tokenIdentificadorJerarquia ?? '';
                        loginResponseFuse.tokenIdentificadorEmpresa = response.data?.tokenIdentificadorEmpresa ?? '';
                        loginResponseFuse.tokenIdentificadorRolJerarquia = response.data?.tokenIdentificadorRolJerarquia ?? '';
                        loginResponseFuse.tokenPermisos = response.data?.tokenPermisos ?? '';

                        subscriber.next(loginResponseFuse);
                    },
                    error: (error: any) => {
                        subscriber.error(error);
                        this.backendService.checkError(error, false);
                    },
                });
        });
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
