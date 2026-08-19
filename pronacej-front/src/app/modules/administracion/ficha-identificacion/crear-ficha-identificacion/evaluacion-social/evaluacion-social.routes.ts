import { Routes } from '@angular/router';
import { EvaluacionSocialComponent } from './evaluacion-social.component';
import { EvaluacionDomiciliariaCrearEditarComponent } from '../evaluacion-domiciliaria/evaluacion-domiciliaria-crear-editar/evaluacion-domiciliaria-crear-editar.component';
import { EvalSeguSociCrearEditarComponent } from '../eval-segu-soci/eval-segu-crear-editar/eval-segu-crear-editar.component';
import { SeguEducCrearEditarComponent } from '../segu-educ-labo-otro/segu-educ-crear-editar/segu-educ-crear-editar.component';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: EvaluacionSocialComponent
            },
            {
                path: 'crear-editar-evaluacion-domiciliaria',
                component: EvaluacionDomiciliariaCrearEditarComponent
            },
            {
                path: 'crear-editar-seguimiento',
                component: EvalSeguSociCrearEditarComponent
            },
            {
                path: 'crear-editar-seguimiento-educativo-laboral-otros',
                component: SeguEducCrearEditarComponent
            }
        ]
    },
] as Routes;