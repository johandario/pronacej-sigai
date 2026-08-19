import { Routes } from '@angular/router';
import { AdminCatalogoComponent } from './admin-catalogo/admin-catalogo.component';
import { InformacionCatalogoComponent } from './informacion-catalogo/informacion-catalogo.component';

export default [
    {
        path: '',
        component: AdminCatalogoComponent,
        children: [
            {
                path: ':token_catalogo',
                component: InformacionCatalogoComponent,
            },
        ],
    },
] as Routes;
