// programa-intervencion-intensiva.routes.ts
import { Routes } from '@angular/router';
import { ProgramaIntervencionIntensivaComponent } from './programa-intervencion-intensiva.component';
import { InformeTecnicoSustentatorioCrearEditarComponent } from '../informe-tecnico-sustentatorio/informe-tecnico-sustentatorio-crear-editar/informe-tecnico-sustentatorio-crear-editar.component';
import { InformeSeguimientoCrearEditarComponent } from '../informe-seguimiento/informe-seguimiento-crear-editar/informe-seguimiento-crear-editar.component';
import { InformeEgresoPiiCrearEditarComponent } from '../informe-egreso-pii/informe-egreso-pii-crear-editar/informe-egreso-pii-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: ProgramaIntervencionIntensivaComponent
            },
            {
                path: 'crear-editar-informe-tecnico-sustentatorio',
                component: InformeTecnicoSustentatorioCrearEditarComponent
            },
            {
                path: 'crear-editar-informe-seguimiento',
                component: InformeSeguimientoCrearEditarComponent
            },
            {
                path: 'crear-editar-informe-egreso',
                component: InformeEgresoPiiCrearEditarComponent
            }
        ]
    },
] as Routes;