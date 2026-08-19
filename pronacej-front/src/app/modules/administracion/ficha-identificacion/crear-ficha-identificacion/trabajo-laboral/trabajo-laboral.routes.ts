import { Routes } from '@angular/router';
import { TrabajoLaboralComponent } from './trabajo-laboral.component';
import { TrabajoLaboralCrearEditarComponent } from './trabajo-laboral-crear-editar/trabajo-laboral-crear-editar.component';

export default [
    {
        path: '',
        children: [
             {

                path: '',
                component: TrabajoLaboralComponent
            },
            {
                path: 'crear',
                component: TrabajoLaboralCrearEditarComponent
            },
            {
                path: 'editar',
                component: TrabajoLaboralCrearEditarComponent
            },
            {
                path: 'ver',
                component: TrabajoLaboralCrearEditarComponent
            }
        ]
    },

] as Routes;
