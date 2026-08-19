import { Route } from '@angular/router';
import { RegistroEstadisticoComponent } from './registro-estadistico/registro-estadistico.component';
import { AdolescentesExternadosComponent } from './adolescentes-externados/adolescentes-externados.component';
import { InformacionAdolescentesComponent } from './informacion-adolescentes/informacion-adolescentes.component';


let route: Route[] = [{
    path: '', 
    children: [
        {
            path: 'registro-estadistico',
            component: RegistroEstadisticoComponent,
        },
        {
            path: 'adolescentes-externados',
            component: AdolescentesExternadosComponent,
        },        
        {
            path: 'informacion-adolescentes',
            component: InformacionAdolescentesComponent,
        },        
    ],
}];

export default route;