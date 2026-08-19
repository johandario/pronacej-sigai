import { Routes } from '@angular/router';
import { SituacionRiesgoSocialComponent } from './situacion-riesgo-social.component';
import { SituacionRiesgoSocialCrearEditarComponent } from './situacion-riesgo-social-crear-editar/situacion-riesgo-social-crear-editar.component';


export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: SituacionRiesgoSocialComponent
            },
            {
                path: 'crear-editar-situacion-riesgo-social',
                component: SituacionRiesgoSocialCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: SituacionRiesgoSocialCrearEditarComponent
            }
        ]
    },
    
] as Routes;
