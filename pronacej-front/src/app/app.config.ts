import { provideHttpClient } from '@angular/common/http';
import { APP_INITIALIZER, ApplicationConfig, inject } from '@angular/core';
import { LuxonDateAdapter } from '@angular/material-luxon-adapter';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { provideAnimations } from '@angular/platform-browser/animations';
import {
    PreloadAllModules,
    provideRouter,
    withHashLocation,
    withInMemoryScrolling,
    withPreloading,
} from '@angular/router';
import { provideFuse } from '@fuse';
import { TranslocoService, provideTransloco } from '@ngneat/transloco';
import { appRoutes } from 'app/app.routes';
import { provideAuth } from 'app/core/auth/auth.provider';
import { provideIcons } from 'app/core/icons/icons.provider';
import { mockApiServices } from 'app/mock-api';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import etiquetasModel from './core/etiquetas.model';
import { RespuestaPorDefecto } from './core/model/response/RespuestaPorDefecto.model';
import { BackendService } from './core/services/backend.service';
import { DialogMensajeService } from './core/services/dialog-mensaje.service';
import { TranslocoHttpLoader } from './core/transloco/transloco.http-loader';
import { provideToastr } from 'ngx-toastr';

export function initBackendService() {
    return async () => {
        let url =
            environment.URL_SERVICIOS +
            '/parametro-del-sistema/obtenerValorApp?nemonico=' +
            etiquetasModel.NEMONICO_AES_CLAVE;
        let jwt = localStorage.getItem(etiquetasModel.LS_TOKEN_DE_ACCESO);
        let tokenEmpresa = localStorage.getItem(
            etiquetasModel.LS_TOKEN_EMPRESA
        );
        let configFecth = fetch(url, {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + (jwt ? jwt : ''),
                tokenIdentificadorEmpresa: tokenEmpresa ? tokenEmpresa : '',
            },

        });

        let response = await configFecth;
        let jsonResponse =
            (await response.json()) as RespuestaPorDefecto<string>;

        if (!jsonResponse.exito) {
            console.error(jsonResponse.mensaje);
            return;
        }
        //BackendService.claveAes = atob(jsonResponse.data);
        BackendService.claveAes = jsonResponse.data
        return;
    };
}

export const appConfig: ApplicationConfig = {
    providers: [
        provideAnimations(),
        provideToastr(),
        provideHttpClient(),
        provideRouter(
            appRoutes,
            withPreloading(PreloadAllModules),
            withInMemoryScrolling({ scrollPositionRestoration: 'enabled' }),
            withHashLocation()
        ),

        // Material Date Adapter
        {
            provide: DateAdapter,
            useClass: LuxonDateAdapter,
        },
        {
            provide: MAT_DATE_FORMATS,
            useValue: {
                parse: {
                    dateInput: 'D',
                },
                display: {
                    dateInput: 'DDD',
                    monthYearLabel: 'LLL yyyy',
                    dateA11yLabel: 'DD',
                    monthYearA11yLabel: 'LLLL yyyy',
                },
            },
        },

        // Transloco Config
        provideTransloco({
            config: {
                availableLangs: [
                    {
                        id: 'en',
                        label: 'English',
                    },
                    {
                        id: 'tr',
                        label: 'Turkish',
                    },
                ],
                defaultLang: 'en',
                fallbackLang: 'en',
                reRenderOnLangChange: true,
                prodMode: true,
            },
            loader: TranslocoHttpLoader,
        }),
        {
            // Preload the default language before the app starts to prevent empty/jumping content
            provide: APP_INITIALIZER,
            useFactory: () => {
                const translocoService = inject(TranslocoService);
                const defaultLang = translocoService.getDefaultLang();
                translocoService.setActiveLang(defaultLang);

                return () => firstValueFrom(translocoService.load(defaultLang));
            },
            multi: true,
        },

        // Fuse
        provideAuth(),
        provideIcons(),
        provideFuse({
            mockApi: {
                delay: 0,
                services: mockApiServices,
            },
            fuse: {
                layout: 'dense',
                scheme: 'light',
                screens: {
                    sm: '600px',
                    md: '960px',
                    lg: '1280px',
                    xl: '1440px',
                },
                theme: 'theme-default',
                themes: [
                    {
                        id: 'theme-default',
                        name: 'Default',
                    },
                    {
                        id: 'theme-brand',
                        name: 'Brand',
                    },
                    {
                        id: 'theme-teal',
                        name: 'Teal',
                    },
                    {
                        id: 'theme-rose',
                        name: 'Rose',
                    },
                    {
                        id: 'theme-purple',
                        name: 'Purple',
                    },
                    {
                        id: 'theme-amber',
                        name: 'Amber',
                    },
                ],
            },
        }),

        //Services
        DialogMensajeService,
        BackendService,
        {
            provide: APP_INITIALIZER,
            deps: [BackendService],
            multi: true,
            useFactory: initBackendService,
        },
    ],
};
