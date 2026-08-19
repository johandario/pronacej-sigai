import { Routes } from '@angular/router';
import { SeguEducLaboOtroComponent } from './segu-educ-labo-otro.component';
import { SeguEducCrearEditarComponent } from './segu-educ-crear-editar/segu-educ-crear-editar.component';



export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: SeguEducLaboOtroComponent
            },
            {
                path: 'crear-editar-seguimiento-educativo-laboral-otros',
                component: SeguEducCrearEditarComponent
            },
            {
                path: 'crear-editar-seguimiento-educativo-laboral-otros/:uuid',
                component: SeguEducCrearEditarComponent
            }
        ]
    },
    
] as Routes;
