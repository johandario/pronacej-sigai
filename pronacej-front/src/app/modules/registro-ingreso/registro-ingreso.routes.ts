import { Route } from '@angular/router';
import { RegistroIngresoComponent } from './registro-ingreso-crear/registro-ingreso.component';
import { ListadoRegistrosIngresoComponent } from './listado-registros-ingreso/listado-registros-ingreso.component';

let route: Route[] = [
    {
        path: '',
        component: ListadoRegistrosIngresoComponent,
    },
    {
        path: "crear-editar",
        component: RegistroIngresoComponent,
    },
    {
        path: "crear-editar/:uuid_fp",
        component: RegistroIngresoComponent
    },
];

export default route;