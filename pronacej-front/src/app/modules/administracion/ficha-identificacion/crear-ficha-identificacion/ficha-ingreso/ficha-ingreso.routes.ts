import { Routes } from '@angular/router';
import { FichaIngresoComponent } from './ficha-ingreso.component';
import { FichaIngresoCrearEditarComponent } from './ficha-ingreso-crear-editar/ficha-ingreso-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: FichaIngresoComponent
            },
            {
                path: 'crear-editar',
                component: FichaIngresoCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: FichaIngresoCrearEditarComponent
            }
        ]
    },
    
] as Routes;
