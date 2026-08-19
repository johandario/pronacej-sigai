import { Routes } from '@angular/router';
import { FichaUbicacionCrearEditarComponent } from './ficha-ubicacion-crear-editar/ficha-ubicacion-crear-editar.component';
import { FichaUbicacionComponent } from './ficha-ubicacion.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: FichaUbicacionComponent
            },
            {
                path: 'crear-editar',
                component: FichaUbicacionCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: FichaUbicacionCrearEditarComponent
            },
        ]
    },
] as Routes;