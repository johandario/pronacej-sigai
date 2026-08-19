import { Routes } from '@angular/router';
import { InformeVisitasComponent } from './informe-visitas.component';

export default [
    {
        path: '',
        component: InformeVisitasComponent,
        data: {
            title: 'Orientación y Consejería Familiar'
        }
    },

] as Routes;
