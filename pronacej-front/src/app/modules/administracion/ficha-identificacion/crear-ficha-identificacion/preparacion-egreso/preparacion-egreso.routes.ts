import { Routes } from '@angular/router';
import { PreparacionEgresoMainComponent } from './preparacion-egreso-main/preparacion-egreso-main.component';
import { RelaAdolCrearEditarComponent } from './rela-adol-crear-editar/rela-adol-crear-editar.component';
import { ApreFinaCrearEditarComponent } from './apre-fina-crear-editar/apre-fina-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: PreparacionEgresoMainComponent
            },
            {
                path: 'reforzamiento/crear-editar',
                component: RelaAdolCrearEditarComponent
            },
            {
                path: 'reforzamiento/crear-editar/:uuid',
                component: RelaAdolCrearEditarComponent
            },
            {
                path: 'apreciacion/crear-editar',
                component: ApreFinaCrearEditarComponent
            },
            {
                path: 'apreciacion/crear-editar/:uuid',
                component: ApreFinaCrearEditarComponent
            }
        ]
    },

] as Routes;
