import { Component } from '@angular/core';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { PlanAsistenciaPostEgresoComponent } from '../plan-asistencia-post-egreso/plan-asistencia-post-egreso.component';
import { InformeFinalAsistenciaComponent } from './informe-final-asistencia/informe-final-asistencia.component';
import { AsistenciaSeguimientoPostEgresoComponent } from './asistencia-seguimiento-post-egreso/asistencia-seguimiento-post-egreso.component';
import { ListarContactoComponent } from 'app/modules/contacto/listar-contacto/listar-contacto.component';
import { ListarAdolescenteDerivadoComponent } from 'app/modules/institucion/seguimiento-adolescente-inst/listar-adolescente-derivado/listar-adolescente-derivado.component';
import { TabService } from 'app/core/services/tab.service';
import { Observable, tap } from 'rxjs';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';

@Component({
  selector: 'app-post-egreso',
  standalone: true,
  imports: [
    MatTabsModule,
    PlanAsistenciaPostEgresoComponent,
    InformeFinalAsistenciaComponent,
    AsistenciaSeguimientoPostEgresoComponent,
    ListarContactoComponent,
    ListarAdolescenteDerivadoComponent,
    
  ],
  templateUrl: './post-egreso.component.html',
  styleUrl: './post-egreso.component.scss'
})
export class PostEgresoComponent {
  selectedIndex: number = 0;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private tabService: TabService,
    private authSerguridadServicio: AuthSerguridadServicio,
  ) 
  {}

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_POST_EGRESO"
    );
    this.obtenerIndiceTabs().subscribe();
    // this.tabService.tabIndex$.subscribe(indice => {
    //   this.selectedIndex = indice;
    // });
  }

  obtenerIndiceTabs() : Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tabIndex = params['tabIndex'];
        if (tabIndex) {
          this.selectedIndex = parseInt(tabIndex);
        }
      })
    );
  }

  cambiarPestana(indice: number) {
    this.selectedIndex = indice;
  }

  cambiarTab(event: MatTabChangeEvent) {
      this.selectedIndex = event.index;
      const queryParams: Params = { tabIndex: this.selectedIndex };
      
      this.router.navigate(
        [], 
        {
          relativeTo: this.route,
          queryParams, 
          queryParamsHandling: 'merge',
        }
      );
    }

  
}
