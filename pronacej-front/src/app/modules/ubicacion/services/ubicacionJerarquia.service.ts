import { Injectable } from '@angular/core';
import { UbicacionJerarquiaDTO } from '../../../core/model/both/ubicacionJerarquiaDTO.model';
import { PaginacionRequest } from '../../../core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from '../../../core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from '../../../core/model/response/RespuestaPorDefecto.model';
import { BackendService } from '../../../core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class UbicacionJerarquiaService {
    private path = '/ubicacion-jerarquia';

    constructor(private backendService: BackendService) {}

    obtenerListaPaginada(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<UbicacionJerarquiaDTO>>> {
        const endPoint = this.path + '/obtenerListaPaginada';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerListaCompleta(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO[]>> {
        const endPoint = this.path + '/obtenerListaCompleta';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerPorTokenIdentificador(
        tokenIdentificador: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO>> {
        const endPoint = this.path + '/obtenerPorTokenIdentificador';
        return this.backendService.getFinal(
            endPoint,
            { tokenIdentificador },
            nemonicoMenu
        );
    }

    obtenerHijosPorTokenIdentificadorPadre(
        tokenIdentificadorPadre: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO[]>> {
        const endPoint =
            this.path + '/obtenerHijosPorTokenIdentificadorPadre';
        return this.backendService.getFinal(
            endPoint,
            { tokenIdentificadorPadre },
            nemonicoMenu
        );
    }

    obtenerPorTokenIdentificadorJerarquiaCentro(
        tokenIdentificadorCentro: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO[]>> {
        const endPoint =
            this.path + '/obtenerPorTokenIdentificadorJerarquiaCentro';
        return this.backendService.getFinal(
            endPoint,
            { tokenIdentificadorCentro },
            nemonicoMenu
        );
    }

    crearEditar(
        ubicacionJerarquiaDTO: UbicacionJerarquiaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO>> {
        const endPoint = this.path + '/crearEditar';
        return this.backendService.postFinal(
            endPoint,
            ubicacionJerarquiaDTO,
            nemonicoMenu
        );
    }

    eliminar(
        ubicacionJerarquiaDTO: UbicacionJerarquiaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO>> {
        const endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            ubicacionJerarquiaDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
