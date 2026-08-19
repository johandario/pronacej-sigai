import { Routes } from '@angular/router';
import { InformeFinalComponent } from './informe-final.component';
import { EvaluacionComponent } from 'app/core/components/evaluacion/evaluacion.component';
import { EvaluacionDocumentoComponent } from 'app/modules/general/evaluacion-documento/evaluacion-documento.component';
import { CrearEditarInformeFinalAbiertoComponent } from './crear-editar-informe-final-abierto/crear-editar-informe-final-abierto.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: InformeFinalComponent
            },
            {
                path: 'crear-editar',
                component: EvaluacionComponent
            },
            {
                path: 'crear-editar-informe-final-abierto',
                component: CrearEditarInformeFinalAbiertoComponent
            },
            {
                path: 'subir-documento',
                component: EvaluacionDocumentoComponent
            }
        ]
    },

] as Routes;
