import { Routes } from '@angular/router';
import { VisualizarExpedienteMatrizComponent } from './visualizar-expediente-matriz/visualizar-expediente-matriz.component';
import { CrearEditarExpedienteMatrizComponent } from './crear-editar-expediente-matriz/crear-editar-expediente-matriz.component';
import { CrearEditarRegistroLegalComponent } from './crear-editar-expediente-matriz/crear-editar-registro-legal/crear-editar-registro-legal.component';
import { ActaExternamientoCrearEditarComponent } from './acta-externamiento-crear-editar/acta-externamiento-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: VisualizarExpedienteMatrizComponent
            },
            {
                path: 'crear-editar',
                component: CrearEditarExpedienteMatrizComponent,                
            },
            // {
            //     path: 'crear-editar/:idExpediente',
            //     component: CrearEditarExpedienteMatrizComponent
            // },
            {
                path: 'crear-editar/registro',
                component: CrearEditarRegistroLegalComponent
            },
            {
                path: 'crear-editar/actaExternamiento',
                component: ActaExternamientoCrearEditarComponent
            },
        ]
    },
    
] as Routes;
