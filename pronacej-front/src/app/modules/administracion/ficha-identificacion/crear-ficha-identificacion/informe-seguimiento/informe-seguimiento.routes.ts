import { Routes } from '@angular/router';
import { InformeSeguimientoComponent } from './informe-seguimiento.component';
import { InformeSeguimientoCrearEditarComponent } from './informe-seguimiento-crear-editar/informe-seguimiento-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: InformeSeguimientoComponent
            },
            {
                path: 'crear-editar-informe-seguimiento',
                component: InformeSeguimientoCrearEditarComponent
            },
            {
                path: 'crear-editar-informe-seguimiento/:uuid',
                component: InformeSeguimientoCrearEditarComponent
            }
        ]
    },
    
] as Routes;
