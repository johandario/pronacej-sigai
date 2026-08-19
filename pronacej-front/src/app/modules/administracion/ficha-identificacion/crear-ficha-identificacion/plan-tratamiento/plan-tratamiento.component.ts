import { Component, OnInit } from '@angular/core';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { GestionPtiComponent } from './gestion-pti/gestion-pti.component';
import { SeguimientoPtiComponent } from './seguimiento-pti/seguimiento-pti.component';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { FichaIngresoService } from 'app/modules/seguridad/services/fichaIngreso.service';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-plan-tratamiento',
  standalone: true,
  imports: [
    MatTabsModule,
    GestionPtiComponent,
    SeguimientoPtiComponent
  ],
  templateUrl: './plan-tratamiento.component.html',
  styleUrl: './plan-tratamiento.component.scss'
})
export class PlanTratamientoComponent implements OnInit {
  selectedIndex: number = 0;
  tituloSeguimiento: string = '';
  uuid_fp!: string;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PLAN_TRATAMIENTO_INDIVIDUAL;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private fichaIngresoService: FichaIngresoService,    
    private authSerguridadServicio: AuthSerguridadServicio,    
  ) {

  }

  async ngOnInit(): Promise<void> {
    await this.authSerguridadServicio.verificarPermisosPantallaConServicio(
      "MENU_PLAN_DE_TRATAMIENTO_INDIVIDUAL"
    );
    this.uuid_fp = this.route.snapshot.params['uuid_fp'];

    this.obtenerIndiceTabs().subscribe( item => {
      this.obtenerFichaIngresoValida(this.uuid_fp).subscribe();
    });
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

  obtenerFichaIngresoValida(tokenFichaIdentificacion: string) {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = null;
    paginacionRequest.size = null;
    paginacionRequest.tokenIdentificador = tokenFichaIdentificacion;

    return this.fichaIngresoService.obtenerUltimaFichaValidaPorTokenFichaIdentificacion(paginacionRequest, this.nemonicoMenu).pipe(
      tap((response) => {
        let ingreso = response.data;
        if (ingreso && ingreso?.centro.nombre.includes('CJDR')) {
          this.tituloSeguimiento = 'Seguimiento periódico de PTI';
        } else if (ingreso && ingreso?.centro.nombre.includes('SOA')) {
          this.tituloSeguimiento = 'Seguimiento evolutivo de PTI';
        } 
      }),
      catchError(err => {
        this.fichaIngresoService.checkError(err);
        return throwError(() => err);
      })
    );
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
