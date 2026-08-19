import { Routes } from '@angular/router';
import { EstudiosComponent } from './estudios.component';
import { EstudiosCrearEditarComponent } from './estudios-crear-editar/estudios-crear-editar.component';

export default [
  {
    path: '',
    component: EstudiosComponent
  },
  {
    path: 'crear',
    component: EstudiosCrearEditarComponent
  },
  {
    path: 'editar',
    component: EstudiosCrearEditarComponent
  },
  {
    path: 'ver',
    component: EstudiosCrearEditarComponent
  }
] as Routes;