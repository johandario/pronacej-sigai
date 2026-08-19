import { Routes } from '@angular/router';
import { PlantillasVerComponent } from './plantillas/plantillas-ver/plantillas-ver.component';
import { PlantillasCrearEditarComponent } from './plantillas/plantillas-crear-editar/plantillas-crear-editar.component';
import { EncuestasVerComponent } from './encuestas/encuestas-ver/encuestas-ver.component';
import { ConstructorEncuestaComponent } from './encuestas/constructor-encuesta/constructor-encuesta.component';
import { InformesVerComponent } from './informes/informes-ver/informes-ver.component';
import { InformesCrearEditarComponent } from './informes/informes-crear-editar/informes-crear-editar.component';
import { PlantillasInformeVerComponent } from './informes/plantillas-informe-ver/plantillas-informe-ver.component';
import { PlantillasInformeCrearEditarComponent } from './informes/plantillas-informe-crear-editar/plantillas-informe-crear-editar.component';
import { PlantillasInformeVisualizarComponent } from './informes/plantillas-informe-visualizar/plantillas-informe-visualizar.component';
import { AlertasVerComponent } from './alertas/alertas-ver/alertas-ver.component';
import { AlertasCrearEditarComponent } from './alertas/alertas-crear-editar/alertas-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: "encuestas",
                component: EncuestasVerComponent
            },
            {
                path: "encuestas/crear",
                component: ConstructorEncuestaComponent
            },
            {
                path: "encuestas/editar",
                component: ConstructorEncuestaComponent
            },
            {
                path: "plantillas",
                component: PlantillasVerComponent
            },
            {
                path: "plantillas/crear",
                component: PlantillasCrearEditarComponent

            },
            {
                path: "plantillas/editar",
                component: PlantillasCrearEditarComponent

            },
            {
                path: "plantillas/visualizar",
                component: PlantillasCrearEditarComponent

            },
            {
                path: "informes",
                component: InformesVerComponent
            },
            {
                path: "informes/crear",
                component: InformesCrearEditarComponent
            },
            {
                path: "informes/editar",
                component: InformesCrearEditarComponent
            },
            {
                path: "informes/plantillas",
                component: PlantillasInformeVerComponent
            },
            {
                path: "informes/plantillas/crear",
                component: PlantillasInformeCrearEditarComponent
            },
            {
                path: "informes/plantillas/editar",
                component: PlantillasInformeCrearEditarComponent
            },
            {
                path: "informes/plantillas/visualizar",
                component: PlantillasInformeVisualizarComponent
            },
            {
                path: "alertas",
                component: AlertasVerComponent
            },
            {
                path: "alertas/crear-editar",
                component: AlertasCrearEditarComponent
            },
        ],
    }
] as Routes;
