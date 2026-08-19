import { Routes } from '@angular/router';
import { SituacionEducativaLaboralComponent } from './situacion-educativa-laboral.component';
import { SituacionCrearEditarComponent } from './situacion-crear-editar/situacion-crear-editar.component';


export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: SituacionEducativaLaboralComponent
            },
            {
                path: 'crear-editar',
                component: SituacionCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: SituacionCrearEditarComponent
            }
        ]
    },
    
] as Routes;
