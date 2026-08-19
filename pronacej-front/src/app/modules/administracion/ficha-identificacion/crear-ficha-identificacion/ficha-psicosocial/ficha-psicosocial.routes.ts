import { Routes } from '@angular/router';
import { FichaPsicosocialComponent } from './ficha-psicosocial.component';
import { SituEconCrearEditarComponent } from '../situ-econ-ento-soci/situ-econ-crear-editar/situ-econ-crear-editar.component';
import { SituacionRiesgoSocialCrearEditarComponent } from '../situacion-riesgo-social/situacion-riesgo-social-crear-editar/situacion-riesgo-social-crear-editar.component';
import { EvaluacionDomiciliariaCrearEditarComponent } from '../evaluacion-domiciliaria/evaluacion-domiciliaria-crear-editar/evaluacion-domiciliaria-crear-editar.component';
import { SubidaDocumentoGenericoComponent } from 'app/core/components/documentos/subida-documento-generico/subida-documento-generico.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: FichaPsicosocialComponent
            },
            {
                path: 'crear-editar-situ-econ',
                component: SituEconCrearEditarComponent
            },
            {
                path: 'crear-editar-situacion-riesgo-social',
                component: SituacionRiesgoSocialCrearEditarComponent
            },
            {
                path: 'crear-editar-evaluacion-domiciliaria',
                component: EvaluacionDomiciliariaCrearEditarComponent
            },
            {
                path: 'subir-documento',
                component: SubidaDocumentoGenericoComponent
            },
        ]
    },
] as Routes;