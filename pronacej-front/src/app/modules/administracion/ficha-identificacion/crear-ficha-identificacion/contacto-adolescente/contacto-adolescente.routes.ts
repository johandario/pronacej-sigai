import { Routes } from '@angular/router';
import { ContactoAdolescenteComponent } from './contacto-adolescente.component';
import { ContactoAdolescenteCrearEditarComponent } from './contacto-adolescente-crear-editar/contacto-adolescente-crear-editar.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: ContactoAdolescenteComponent
            },
            {
                path: 'crear-editar',
                component: ContactoAdolescenteCrearEditarComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: ContactoAdolescenteCrearEditarComponent
            },
        ]
    },
    
] as Routes;
