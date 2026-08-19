import { Injectable } from '@angular/core';
import { SeguimientoConductualDTO } from 'app/core/model/both/ia/seguimientoConductualDTO.model';
import { SeguimientoPsicologicoDTO } from 'app/core/model/both/ia/seguimientoPsicologicoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class SeguimientoService {
    private path = '/seguimiento';

    constructor(private backendService: BackendService) {}

    obtenerSeguimientosPsicologicos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SeguimientoPsicologicoDTO>>
    > {
        let endPoint = this.path + '/obtenerSeguimientosPsicologicos';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearSeguimientoPsicologico(
        psicologicoDTO: SeguimientoPsicologicoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/crearSeguimientoPsicologico';
        return this.backendService.postFinal(
            endPoint,
            psicologicoDTO,
            nemonicoMenu
        );
    }

    actualizarSeguimientoPsicologico(
        psicologicoDTO: SeguimientoPsicologicoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/actualizarSeguimientoPsicologico';
        return this.backendService.postFinal(
            endPoint,
            psicologicoDTO,
            nemonicoMenu
        );
    }

    eliminarSeguimientoPsicologico(
        psicologicoDTO: SeguimientoPsicologicoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarSeguimientoPsicologico';
        return this.backendService.postFinal(
            endPoint,
            psicologicoDTO,
            nemonicoMenu
        );
    }

    obtenerSeguimientosConductuales(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SeguimientoConductualDTO>>
    > {
        let endPoint = this.path + '/obtenerSeguimientosConductuales';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearSeguimientoConductual(
        conductualDTO: SeguimientoConductualDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/crearSeguimientoConductual';

        return this.backendService.postFinal(
            endPoint,
            conductualDTO,
            nemonicoMenu
        );
    }

    actualizarSeguimientoConductual(
        conductualDTO: SeguimientoConductualDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/actualizarSeguimientoConductual';
        return this.backendService.postFinal(
            endPoint,
            conductualDTO,
            nemonicoMenu
        );
    }

    eliminarSeguimientoConductual(
        conductualDTO: SeguimientoConductualDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarSeguimientoConductual';
        return this.backendService.postFinal(
            endPoint,
            conductualDTO,
            nemonicoMenu
        );
    }
}
