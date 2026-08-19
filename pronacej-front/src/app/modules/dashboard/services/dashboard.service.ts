import { Injectable } from '@angular/core';
import { DashboardCentroDTO } from 'app/core/model/both/DashboardCentroDTO.model';
import { DashboardEstadisticasDTO } from 'app/core/model/both/DashboardEstadisticasDTO.model';
import { DashboardRequest } from 'app/core/model/both/DashboardRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class DashboardService {
    private path = '/dashboard';

    constructor(private backendService: BackendService) {}

    obtenerCentros(
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<DashboardCentroDTO[]>> {
        const endPoint = this.path + '/centros';
        return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerEstadisticas(
        dashboardRequest: DashboardRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<DashboardEstadisticasDTO>> {
        const endPoint = this.path + '/estadisticas';
        return this.backendService.postFinal(endPoint, dashboardRequest, nemonicoMenu);
    }
}