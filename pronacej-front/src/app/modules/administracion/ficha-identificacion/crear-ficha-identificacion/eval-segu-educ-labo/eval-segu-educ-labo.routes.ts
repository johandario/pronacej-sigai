import { Routes } from '@angular/router';
import { EvalSeguEducLaboComponent } from './eval-segu-educ-labo.component';
import { EvalSeguCrearEditarComponent } from './eval-segu-crear-editar/eval-segu-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: EvalSeguEducLaboComponent
            },
            {
                path: 'crear-editar',
                component: EvalSeguCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: EvalSeguCrearEditarComponent
            }
        ]
    },
    
] as Routes;
