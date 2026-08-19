import { Routes } from '@angular/router';
import { RegistroSalidaComponent } from 'app/modules/salida/registro-salida/registro-salida.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: RegistroSalidaComponent
            },
          
        ]  
    },

] as Routes;