import { inject } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    CanActivateFn,
    Router,
    RouterStateSnapshot,
    Routes,
} from '@angular/router';
import { catchError, map, of, throwError } from 'rxjs';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { UbicacionComponent } from 'app/modules/ubicacion/ubicacion.component';
import { UbicacionBrowserService } from 'app/modules/ubicacion/services/ubicacion-browser.service';
import { UbicacionDetailsComponent } from 'app/modules/ubicacion/details/details.component';
import { UbicacionListComponent } from 'app/modules/ubicacion/list/list.component';
import { FuncionarioService } from '../seguridad/services/funcionario.service';

const redirectNoAdminToCentroGuard: CanActivateFn = () => {
    const funcionarioService = inject(FuncionarioService);
    const router = inject(Router);

    return funcionarioService
        .obtenerFuncionarioDelUsuario(etiquetasModel.NEMONICO_MENU_UBICACION)
        .pipe(
            map((response: RespuestaPorDefecto<FuncionarioDTO>) => {
                const esAdministrador = !!(response.exito && response.data?.cargoSuperRol);

                if (esAdministrador) {
                    return true;
                }

                const tokenCentro =
                    localStorage.getItem(etiquetasModel.LS_TOKEN_JERARQUIA) || '';

                if (!tokenCentro) {
                    return true;
                }

                return router.createUrlTree([
                    '/configuracion/ubicacion/centros',
                    tokenCentro,
                ]);
            }),
            catchError(() => of(true))
        );
};

const levelResolver = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
) => {
    const service = inject(UbicacionBrowserService);
    const router = inject(Router);

    return service.loadLevel(route).pipe(
        catchError((error) => {
            const parentUrl = state.url.split('/').slice(0, -1).join('/');
            router.navigateByUrl(parentUrl || '/configuracion/ubicacion');
            service.showError(error);
            return throwError(() => error);
        })
    );
};

const itemResolver = (route: ActivatedRouteSnapshot) => {
    const service = inject(UbicacionBrowserService);
    const id = route.paramMap.get('id');

    if (!id) {
        return throwError(() => new Error('Identificador invalido'));
    }

    return service.getUbicacionById(id);
};

const canDeactivateUbicacionDetails = (
    component: UbicacionDetailsComponent,
    _currentRoute: ActivatedRouteSnapshot,
    _currentState: RouterStateSnapshot,
    nextState: RouterStateSnapshot
) => {
    if (!nextState.url.includes('/configuracion/ubicacion')) {
        return true;
    }

    if (nextState.url.includes('/details/')) {
        return true;
    }

    return component.closeDrawer().then(() => true);
};

export default [
    {
        path: '',
        component: UbicacionComponent,
        children: [
            {
                path: '',
                component: UbicacionListComponent,
                canActivate: [redirectNoAdminToCentroGuard],
                resolve: {
                    level: levelResolver,
                },
                children: [
                    {
                        path: 'details/new',
                        component: UbicacionDetailsComponent,
                        canDeactivate: [canDeactivateUbicacionDetails],
                    },
                    {
                        path: 'details/:id',
                        component: UbicacionDetailsComponent,
                        resolve: {
                            item: itemResolver,
                        },
                        canDeactivate: [canDeactivateUbicacionDetails],
                    },
                ],
            },
            {
                path: 'centros/:centroId',
                component: UbicacionListComponent,
                resolve: {
                    level: levelResolver,
                },
                children: [
                    {
                        path: 'details/new',
                        component: UbicacionDetailsComponent,
                        canDeactivate: [canDeactivateUbicacionDetails],
                    },
                    {
                        path: 'details/:id',
                        component: UbicacionDetailsComponent,
                        resolve: {
                            item: itemResolver,
                        },
                        canDeactivate: [canDeactivateUbicacionDetails],
                    },
                ],
            },
            {
                path: 'ubicaciones/:ubicacionId',
                component: UbicacionListComponent,
                resolve: {
                    level: levelResolver,
                },
                children: [
                    {
                        path: 'details/new',
                        component: UbicacionDetailsComponent,
                        canDeactivate: [canDeactivateUbicacionDetails],
                    },
                    {
                        path: 'details/:id',
                        component: UbicacionDetailsComponent,
                        resolve: {
                            item: itemResolver,
                        },
                        canDeactivate: [canDeactivateUbicacionDetails],
                    },
                ],
            },
        ],
    },
] as Routes;
