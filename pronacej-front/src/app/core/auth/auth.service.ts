import { inject, Injectable } from '@angular/core';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { UserService } from 'app/core/user/user.service';
import { catchError, Observable, of, switchMap, tap, throwError } from 'rxjs';
import etiquetasModel from '../etiquetas.model';
import { LoginRequest } from '../model/request/loginRequest.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { LoginResponseFuse } from '../model/response/LoginResponseFuse.model';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { JerarquiaDTO } from '../model/both/jerarquiaDTO.model';
import { DialogSeleccionarJerarquiaComponent } from '../components/dialog-seleccionar-jerarquia/dialog-seleccionar-jerarquia.component';

@Injectable({ providedIn: 'root' })
export class AuthService {
    public internvalList: NodeJS.Timeout[] = [];

    private _authenticated: boolean = false;
    private _userService = inject(UserService);
    private authSerguridadServicio = inject(AuthSerguridadServicio);
    private _httpClient = inject(HttpClient);
    private dialog = inject(MatDialog);

    public nombreEmpresa: string;
    public nombreRol: string;

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------
    /**
     * Setter & getter for access token
     */
    set accessToken(token: string) {
        localStorage.setItem(etiquetasModel.LS_TOKEN_DE_ACCESO, token);
    }

    get accessToken(): string {
        return localStorage.getItem(etiquetasModel.LS_TOKEN_DE_ACCESO) ?? '';
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Forgot password
     *
     * @param email
     */
    forgotPassword(email: string): Observable<any> {
        return this._httpClient.post('api/auth/forgot-password', email);
    }

    /**
     * Reset password
     *
     * @param password
     */
    resetPassword(password: string): Observable<any> {
        return this._httpClient.post('api/auth/reset-password', password);
    }

    /**
     * Sign in
     *
     * @param credentials
     */
    signIn(loginRequest: LoginRequest, nemonicoMenu: string): Observable<any> {
        // Throw error, if the user is already logged in
        if (this._authenticated) {
            return throwError('User is already logged in.');
        }

        return this.authSerguridadServicio.login(loginRequest, nemonicoMenu);

        // return this.authSerguridadServicio.login(loginRequest, nemonicoMenu).pipe(
        //     switchMap((response: LoginResponseFuse) => {
        //         if (!response.success) return of(response);
        //         const necesitaSeleccionEmpresa = response.estado === "LOGIN_SELECCION_DE_JERARQUIA";
        //         const jerarquias = response.listaJerarquias || [];

        //         if (necesitaSeleccionEmpresa && jerarquias.length > 1) {
        //             return this.dialog.open(DialogSeleccionarJerarquiaComponent, {
        //                 width: '400px',
        //                 data: jerarquias,
        //                 disableClose: true
        //             }).afterClosed().pipe(
        //                 switchMap((jerarquiaSeleccionada: JerarquiaDTO) => {
        //                     if (!jerarquiaSeleccionada) {
        //                         return throwError('Debe seleccionar una jerarquia.');
        //                     }

        //                     // Reintentar login pero con tokenIdentificadorEmpresa
        //                     const nuevoLoginRequest = { ...loginRequest, tokenIdentificadorJerarquia: jerarquiaSeleccionada.tokenIdentificador };
        //                     return this.authSerguridadServicio.login(nuevoLoginRequest, nemonicoMenu);
        //                 }),
        //                 tap((loginResponseFinal) => {
        //                     if (!loginResponseFinal.success) {
        //                         throw new Error('Falló el login con la empresa seleccionada');
        //                     }
        //                     // Guardar token, usuario, etc
        //                     this.finalizarLogin(loginResponseFinal);
        //                 })
        //             );
        //         }

        //         this.finalizarLogin(response);
        //         return of(response);

        //         // if (response.success && !response.user?.tokenReseteoContrasenia) {
        //         //     // Store the access token in the local storage
        //         //     this.accessToken = response.accessToken;

        //         //     // Set the authenticated flag to true
        //         //     this._authenticated = true;

        //         //     // Store the user on the user service
        //         //     this._userService.user = response.user;

        //         //     // Return a new observable with the response
        //         // }

        //         // return of(response);
        //     })
        // );;
    }

    /**
     * Sign in using the access token
     */
    signInUsingToken(): Observable<any> {
        // Sign in using the token
        return this.authSerguridadServicio.verificarJWT().pipe(
            catchError(() =>
                // Return false
                of(false)
            ),
            switchMap((response: any) => {
                // Replace the access token with the new one if it's available on
                // the response object.
                //
                // This is an added optional step for better security. Once you sign
                // in using the token, you should generate a new one on the server
                // side and attach it to the response object. Then the following
                // piece of code can replace the token with the refreshed one.
                if (response.accessToken) {
                    this.accessToken = response.accessToken;
                }

                // Set the authenticated flag to true
                this._authenticated = true;

                // Store the user on the user service
                this._userService.user = response.user;

                // Return true
                return of(true);
            })
        );;
        /*
        return this._httpClient
            .post('api/auth/sign-in-with-token', {
                accessToken: this.accessToken,
            })
            .pipe(
                catchError(() =>
                    // Return false
                    of(false)
                ),
                switchMap((response: any) => {
                    alert(JSON.stringify(response))
                    // Replace the access token with the new one if it's available on
                    // the response object.
                    //
                    // This is an added optional step for better security. Once you sign
                    // in using the token, you should generate a new one on the server
                    // side and attach it to the response object. Then the following
                    // piece of code can replace the token with the refreshed one.
                    if (response.accessToken) {
                        this.accessToken = response.accessToken;
                    }

                    // Set the authenticated flag to true
                    this._authenticated = true;

                    // Store the user on the user service
                    this._userService.user = response.user;

                    // Return true
                    return of(true);
                })
            );
            */
    }

    /**
     * Sign out
     */
    signOut(): Observable<any> {
        // Remove the access token from the local storage
        localStorage.removeItem(etiquetasModel.LS_TOKEN_DE_ACCESO);
        localStorage.removeItem(etiquetasModel.LS_TOKEN_EMPRESA);
        localStorage.removeItem(etiquetasModel.LS_TOKEN_JERARQUIA);
        localStorage.removeItem(etiquetasModel.LS_TOKEN_ROL_JERARQUIA);
        // Set the authenticated flag to false
        this._authenticated = false;

        // Return the observable
        return of(true);
    }

    /**
     * Sign up
     *
     * @param user
     */
    signUp(user: {
        name: string;
        email: string;
        password: string;
        company: string;
    }): Observable<any> {
        return this._httpClient.post('api/auth/sign-up', user);
    }

    /**
     * Unlock session
     *
     * @param credentials
     */
    unlockSession(credentials: {
        email: string;
        password: string;
    }): Observable<any> {
        return this._httpClient.post('api/auth/unlock-session', credentials);

    }

    /**
     * Check the authentication status
     */
    check(): Observable<boolean> {
        // Check if the user is logged in
        if (this._authenticated) {
            return of(true);
        }

        // Check the access token availability
        if (!this.accessToken) {
            return of(false);
        }

        // Check the access token expire date
        if (AuthUtils.isTokenExpired(this.accessToken)) {
            return of(false);
        }

        // If the access token exists, and it didn't expire, sign in using it
        return this.signInUsingToken();
    }

    finalizarLogin(response: LoginResponseFuse): void {
        console.log('finalizarLogin', response);
        localStorage.setItem(etiquetasModel.LS_TOKEN_JERARQUIA, response.tokenIdentificadorJerarquia);
        localStorage.setItem(etiquetasModel.LS_TOKEN_EMPRESA, response.tokenIdentificadorEmpresa);
        localStorage.setItem(etiquetasModel.LS_TOKEN_ROL_JERARQUIA, response.tokenIdentificadorRolJerarquia);

        this._userService.user = response.user;
        this._authenticated = true;
        this.accessToken = response.accessToken;
    }

    finalizarCambioJerarquia(response: LoginResponseFuse): void {
        console.log('finalizarLogin', response);
        localStorage.setItem(etiquetasModel.LS_TOKEN_JERARQUIA, response.tokenIdentificadorJerarquia);
        localStorage.setItem(etiquetasModel.LS_TOKEN_EMPRESA, response.tokenIdentificadorEmpresa);
        localStorage.setItem(etiquetasModel.LS_TOKEN_ROL_JERARQUIA, response.tokenIdentificadorRolJerarquia);

        // this._userService.user = response.user;
        this._authenticated = true;
        this.accessToken = response.accessToken;
    }

    checkError(error: any, mostrarError = true) {
        return this.authSerguridadServicio.checkError(error, mostrarError);
    }
}
