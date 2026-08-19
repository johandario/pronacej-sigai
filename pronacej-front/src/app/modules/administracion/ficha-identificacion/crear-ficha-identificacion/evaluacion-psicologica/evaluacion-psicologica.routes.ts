import { Routes } from '@angular/router';
import { EvaluacionPsicologicaVerComponent } from './evaluacion-psicologica-ver/evaluacion-psicologica-ver.component';
import { EvaluacionComponent } from 'app/core/components/evaluacion/evaluacion.component';
import { SeguimientoPsicologicoCrearEditarComponent } from './seguimiento-psicologico-crear-editar/seguimiento-psicologico-crear-editar.component';
import { PruebasPsicologicasComponent } from './pruebas-psicologicas/pruebas-psicologicas.component';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';
import { SeguimientoPsicologicoVerComponent } from './seguimiento-psicologico-ver/seguimiento-psicologico-ver.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: EvaluacionPsicologicaVerComponent
            },
            {
                path: 'evaluacion/crear-editar',
                component: EvaluacionComponent
            },
            {
                path: 'evaluacion/crear-editar/:uuid',
                component: EvaluacionComponent
            },
            {
                path: 'prueba/crear',
                component: PruebasPsicologicasComponent
            },
            {
                path: 'prueba/editar',
                component: EvaluacionComponent
            },
            {
                path: 'seguimiento',
                component: SeguimientoPsicologicoVerComponent
            },
            {
                path: 'seguimiento/crear-editar',
                component: SeguimientoPsicologicoCrearEditarComponent
            },
            {
                path: 'seguimiento/crear-editar/:uuid',
                component: SeguimientoPsicologicoCrearEditarComponent
            }
        ]
    },

] as Routes;
