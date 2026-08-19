import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from 'app/core/auth/auth.service';
import { of, switchMap } from 'rxjs';
import { AuthUtils } from 'app/core/auth/auth.utils';
import etiquetasModel from 'app/core/etiquetas.model';
import { environment } from 'environments/environment';

export const AuthGuard: CanActivateFn | CanActivateChildFn = (route, state) => {
    const router: Router = inject(Router);
    let authService = inject(AuthService);

    // Check the authentication status
    return authService
        .check()
        .pipe(
            switchMap((authenticated) => {
                // If the user is not authenticated...
                // Redirect to the sign-in page with a redirectUrl param
                const redirectURL =
                    state.url === '/sign-out'
                        ? ''
                        : `redirectURL=${state.url}`;
                let urlF = `sign-in?${redirectURL}`;
                const urlTree = router.parseUrl(urlF);

                checkSesionInterval(authService, router, urlF);

                if (!authenticated) {
                    return of(urlTree);
                }

                // Allow the access
                return of(true);
            })
        );
};

function checkSesionInterval(authService: AuthService, router: Router,
    urlF: string
) {
    let idInterval = setInterval(
        () => {

            const authenticated2 = !AuthUtils.isTokenExpired(
                localStorage.getItem(etiquetasModel.LS_TOKEN_DE_ACCESO)
            );

            if (!environment.production) {
                console.log("authenticated2: " + authenticated2)
            }

            if (!authenticated2) {
                authService.signOut().subscribe(
                    {
                        next: () => {
                            for (let id of authService.internvalList) {
                                clearInterval(id);
                            }
                            router.navigateByUrl(urlF);
                        }
                    }
                );
            }
        },
        1000
    );

    authService.internvalList.push(idInterval);
}
