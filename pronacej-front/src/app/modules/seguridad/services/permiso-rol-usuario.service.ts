import { Injectable } from "@angular/core";
import { PermisoRolUsuarioDTO, PermisoRolUsuarioNombresDTO, PermissionMap } from "app/core/model/both/permisoRolUsuario.model";
import { PaginacionRequest } from "app/core/model/request/PaginacionRequest.model";
import { PaginacionResponse } from "app/core/model/response/PaginacionResponse.model";
import { RespuestaPorDefecto } from "app/core/model/response/RespuestaPorDefecto.model";
import { BackendService } from "app/core/services/backend.service";
import { environment } from "environments/environment";
import { map, Observable, ReplaySubject, tap } from "rxjs";

@Injectable(
    {
        providedIn: "root"
    }
)
export class PermisoRolUsuarioService {

    private path = "/permisoRolUsuario";

    private _permisos$ = new ReplaySubject<PermisoRolUsuarioDTO>(1);

    private _permissionMap$ = new ReplaySubject<PermissionMap>(1);
    private permissionMap?: PermissionMap;

    constructor(
        private backendService: BackendService
    ) { }

    // Observable público (solo lectura)
    get permisos$(): Observable<PermisoRolUsuarioDTO> {
        return this._permisos$.asObservable();
    }

    obtenerPermisos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<PermisoRolUsuarioNombresDTO>>> {
        let endPoint = this.path + '/obtenerPermisos';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerPermisosPorToken(
        token: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PermisoRolUsuarioDTO>> {
        let endPoint = this.path + '/obtenerPermisosPorToken';
        return this.backendService.getFinal(
            endPoint,
            { token },
            nemonicoMenu
        );
    }    

    crearEditarPermisos(
        permisoRolUsuario: PermisoRolUsuarioDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PermisoRolUsuarioDTO>> {
        let endPoint = this.path + '/crearEditarPermisos';
        return this.backendService.postFinal(
            endPoint,
            permisoRolUsuario,
            nemonicoMenu
        );
    }

    eliminarPermisos(
        permisoRolUsuario: PermisoRolUsuarioNombresDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PermisoRolUsuarioNombresDTO>> {
        let endPoint = this.path + '/eliminarPermisos';
        return this.backendService.postFinal(
            endPoint,
            permisoRolUsuario,
            nemonicoMenu
        );
    }

    obtenerPermisosUsuario(
        nemonicoMenu: string,
        uuid: string = ''
    ): Observable<RespuestaPorDefecto<PermisoRolUsuarioDTO>> {
        const endPoint = this.path + '/obtenerPermisosUsuario';

        return this.backendService.getFinal<RespuestaPorDefecto<PermisoRolUsuarioDTO>>(endPoint, { uuid }, nemonicoMenu).pipe(
            // tap((permisos) => this._permisos$.next(permisos.data))
            tap((respuesta) => {
                if (!environment.production) {
                    console.log('PermisosMenuGuard - tienePermiso:', respuesta.exito);
                }

                const permisos = respuesta.data;

                this._permisos$.next(permisos);

                const map = this.buildPermissionMap(permisos);
                this.permissionMap = map;
                this._permissionMap$.next(map);
            })
        );
    }

    limpiarPermisos(): void {
        this._permisos$.complete();
        this._permissionMap$.complete();

        this._permisos$ = new ReplaySubject<PermisoRolUsuarioDTO>(1);
        this._permissionMap$ = new ReplaySubject<PermissionMap>(1);

        this.permissionMap = undefined;
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }

    /** Para directivas */
    hasPermission$(
        nemonicoMenu: string,
        nemonicoAccion: string
    ): Observable<boolean> {
        return this._permissionMap$.pipe(
            map(map => !!map[nemonicoMenu]?.[nemonicoAccion])
        );
    }

    /** Para guards o lógica puntual */
    hasPermission(
        nemonicoMenu: string,
        nemonicoAccion: string
    ): boolean {
        return !!this.permissionMap?.[nemonicoMenu]?.[nemonicoAccion];
    }

    hasPermissionArray(
        nemonicoMenu: string,
        ...nemonicosAccion: string[]
    ): boolean[] {
        return nemonicosAccion.map(accion => !!this.permissionMap?.[nemonicoMenu]?.[accion]);
    }

    private buildPermissionMap(
        permisos: PermisoRolUsuarioDTO
    ): PermissionMap {

        const map: PermissionMap = {};

        permisos?.menus.forEach(menu => {
            if (!menu.nemonicoMenu) {
                return;
            }

            map[menu.nemonicoMenu] = {};

            menu.acciones.forEach(accion => {
                if (!accion.nemonicoCatalogoAccion) {
                    return;
                }

                map[menu.nemonicoMenu][accion.nemonicoCatalogoAccion] = accion.activo;
            });
        });

        return map;
    }
}