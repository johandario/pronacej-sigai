import { Injectable } from '@angular/core';
import { TrabajoLaboralDTO } from 'app/core/model/both/TrabajoLaboralDTO.model';
import { TrabajoLaboralEstadisticoDTO } from 'app/core/model/both/TrabajoLaboralEstadisticoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class TrabajoLaboralService {
    private path = '/trabajo-laboral';

    constructor(private backendService: BackendService) {}

    obtenerListaTrabajoLaboral(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TrabajoLaboralDTO>>> {
        const endPoint = this.path + '/lista';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearTrabajoLaboral(
        trabajoLaboralDTO: TrabajoLaboralDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<TrabajoLaboralDTO>> {
        const endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            trabajoLaboralDTO,
            nemonicoMenu
        );
    }

    eliminarTrabajoLaboral(
        trabajoLaboralDTO: TrabajoLaboralDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        const endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            trabajoLaboralDTO,
            nemonicoMenu
        );
    }

    obtenerTrabajoLaboral(
        trabajoLaboralDTO: TrabajoLaboralDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<TrabajoLaboralDTO>> {
        const endPoint = this.path + '/obtener';
        return this.backendService.postFinal(
            endPoint,
            trabajoLaboralDTO,
            nemonicoMenu
        );
    }

    obtenerCantidadTrabajoActivo(
        nemonico: string
        ): Observable<RespuestaPorDefecto<number>> {
        let endPoint = this.path + '/cantidadTrabajoActivo';
        return this.backendService.postFinal(endPoint, {}, nemonico);
    }

    obtenerEstadisticasTrabajoLaboral(nemonico: string
        ): Observable<RespuestaPorDefecto<TrabajoLaboralEstadisticoDTO[]>> {
        let endPoint = this.path + '/estadisticasTrabajoLaboral';
        return this.backendService.postFinal(endPoint, {}, nemonico);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}