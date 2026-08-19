import { Routes } from '@angular/router';
import { DatosFamiliaresComponent } from './datos-familiares.component';

export default [{
    path: '',
    children: [
        {
            path: '',
            component: DatosFamiliaresComponent
        },
        // {
        //     path: 'crear-editar',
        //     component: 
        // }
    ]
}] as Routes;