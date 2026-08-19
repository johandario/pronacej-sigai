import { Routes } from '@angular/router';
import { NivelRiesgoComponent } from './nivel-riesgo.component';
import { NivelRiesgoCrearEditarComponent } from './nivel-riesgo-crear-editar/nivel-riesgo-crear-editar.component';
import { EvaluacionComponent } from 'app/core/components/evaluacion/evaluacion.component';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: NivelRiesgoComponent
            },
            {
                path: 'crear',
                component: NivelRiesgoCrearEditarComponent
            },
            {
                path: 'editar',
                component: EvaluacionComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: NivelRiesgoCrearEditarComponent
            }
        ]
    },
    
] as Routes;
