import { Injectable } from '@angular/core';
import { AlertaDTO } from 'app/core/model/both/AlertaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class AlertaService {
    private path = '/alerta';

    constructor(private backendService: BackendService) {}

    obtenerListaAlertas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<AlertaDTO>>> {
        let endPoint = this.path + '/obtenerListaAlertas';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerAlertas(
        alertaDTO: AlertaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<AlertaDTO[]>> {
        let endPoint = this.path + '/obtenerAlertas';
        return this.backendService.postFinal(endPoint, alertaDTO, nemonicoMenu);
    }

    crearAlerta(
        alertaDTO: AlertaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/crearAlerta';
        return this.backendService.postFinal(endPoint, alertaDTO, nemonicoMenu);
    }

    actualizarAlerta(
        alertaDTO: AlertaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/actualizarAlerta';
        return this.backendService.postFinal(endPoint, alertaDTO, nemonicoMenu);
    }

    removerAlerta(
        alertaDTO: AlertaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/removerAlerta';
        return this.backendService.postFinal(endPoint, alertaDTO, nemonicoMenu);
    }
}
