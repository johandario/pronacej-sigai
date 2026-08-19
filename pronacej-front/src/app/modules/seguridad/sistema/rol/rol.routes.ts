import { Routes } from '@angular/router';
import { RolComponent } from './rol/rol.component';

export default [
    {
        path: '',
        component: RolComponent,
    },
    {
        path: 'crear',
        component: RolComponent,
    },
    {
        path: 'editar',
        component: RolComponent,
    },
] as Routes;
