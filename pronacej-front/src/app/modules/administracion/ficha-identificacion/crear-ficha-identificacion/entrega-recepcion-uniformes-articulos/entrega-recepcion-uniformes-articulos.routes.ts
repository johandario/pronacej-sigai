import { Routes } from '@angular/router';
import { EntregaRecepcionUniformesArticulosComponent } from './entrega-recepcion-uniformes-articulos.component';
import { CrearEditarRecepcionEntregaComponent } from './crear-editar/crear-editar-recepcion-entrega.component';
import { VisualizarRecepcionEntregaComponent } from './visualizar/visualizar-recepcion-entrega.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: VisualizarRecepcionEntregaComponent,
            },
            {
                path: 'crear-editar',
                component: CrearEditarRecepcionEntregaComponent,
            }            
        ]
    },
] as Routes;