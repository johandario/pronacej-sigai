import { Routes } from '@angular/router';
import { SituEconEntoSociComponent } from './situ-econ-ento-soci.component';
import { SituEconCrearEditarComponent } from './situ-econ-crear-editar/situ-econ-crear-editar.component';


export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: SituEconEntoSociComponent
            },
            {
                path: 'crear-editar',
                component: SituEconCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: SituEconCrearEditarComponent
            }
        ]
    },
    
] as Routes;
