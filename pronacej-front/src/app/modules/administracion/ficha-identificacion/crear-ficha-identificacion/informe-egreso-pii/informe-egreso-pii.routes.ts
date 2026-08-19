import { Routes } from '@angular/router';
import { InformeEgresoPiiComponent } from './informe-egreso-pii.component';
import { InformeEgresoPiiCrearEditarComponent } from './informe-egreso-pii-crear-editar/informe-egreso-pii-crear-editar.component';

export default [
    {
        path: '',
        component: InformeEgresoPiiComponent
    },
    {
        path: 'crear-editar-informe-egreso',
        component: InformeEgresoPiiCrearEditarComponent
    },
    {
        path: 'crear-editar-informe-egreso/:uuid',
        component: InformeEgresoPiiCrearEditarComponent
    },
] as Routes;