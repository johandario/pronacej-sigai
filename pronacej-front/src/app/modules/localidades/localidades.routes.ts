import { Routes } from '@angular/router';
import { LocalidadesComponent } from './localidades.component';
import { InformacionLocalidadComponent } from './informacion-localidad/informacion-localidad.component';

export default [
    {
        path: '',
        component: LocalidadesComponent,
        children: [
            {
                path: ':token_localidad',
                component: InformacionLocalidadComponent,
            },
            // {
            //     path: 'nueva',
            //     component: InformacionLocalidadComponent
            // },
        ],
    },
] as Routes;