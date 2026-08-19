import { Routes } from '@angular/router';
import { InformesCrearEditarComponent } from 'app/modules/general/informes/informes-crear-editar/informes-crear-editar.component';
import { InformesVerComponent } from 'app/modules/general/informes/informes-ver/informes-ver.component';
import { PlantillasInformeCrearEditarComponent } from 'app/modules/general/informes/plantillas-informe-crear-editar/plantillas-informe-crear-editar.component';
import { PlantillasInformeVerComponent } from 'app/modules/general/informes/plantillas-informe-ver/plantillas-informe-ver.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: InformesVerComponent
            },
            {
                path: 'plantillas',
                component: PlantillasInformeVerComponent
            },
            {
                path: 'plantillas/crear',
                component: PlantillasInformeCrearEditarComponent
            },
            {
                path: 'plantillas/editar',
                component: PlantillasInformeCrearEditarComponent
            },
            {
                path: 'crear',
                component: InformesCrearEditarComponent
            },
            {
                path: 'editar',
                component: InformesCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: InformesCrearEditarComponent
            }
        ]
    },

] as Routes;
