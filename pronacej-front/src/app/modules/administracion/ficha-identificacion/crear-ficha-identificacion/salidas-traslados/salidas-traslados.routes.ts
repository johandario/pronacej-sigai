import { Routes } from '@angular/router';
import { SalidasTrasladosComponent } from './salidas-traslados.component';
import { VistaSalidaTrasladosComponent } from './vista-salida-traslados/vista-salida-traslados.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: SalidasTrasladosComponent
            },

            {
                path: 'vista/:tokenID',
                component: VistaSalidaTrasladosComponent
            }

        ]
    },

] as Routes;