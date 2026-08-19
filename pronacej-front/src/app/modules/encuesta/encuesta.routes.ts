import { Routes } from '@angular/router';
import { FormularioDinamicoComponent } from './formulario-dinamico/formulario-dinamico.component';
import { EncuestaClienteComponent } from './encuesta-cliente/encuesta-cliente.component';

export default [
    {
        path: 'encuesta',
        children: [
            {
                path: "formulario-dinamico",
                component: FormularioDinamicoComponent
            },
            {
                path: "encuesta-cliente",
                component: EncuestaClienteComponent
            }
        ],
    }
] as Routes;
