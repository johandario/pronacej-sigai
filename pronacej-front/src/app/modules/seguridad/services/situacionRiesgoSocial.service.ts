import { Injectable } from '@angular/core';
import { SituacionRiesgoSocialDTO } from 'app/core/model/both/situacionRiesgoSocialDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class SituacionRiesgoSocialService {
    private path = '/situacion-riesgo-social';

    constructor(private backendService: BackendService) {}

    /**
     * Obten las situaciones de riesgo social para el sistema
     *
     * @param nemonicoMenu string nemónico de una situación de riesgo social del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerSituacionesRiesgoSocialPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SituacionRiesgoSocialDTO>>
    > {
        let endPoint = this.path + '/obtenerSituacionesRiesgoSocialPaginado';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una situación de riesgo social en el sistema con los datos enviados en el request
     *
     * @param SituacionRiesgoSocialDTO datos de la situación de riesgo social a crear
     * @param nemonicoMenu string nemónico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<SituacionRiesgoSocialDTO>>
     */
    crearSituacionRiesgoSocial(
        situacionRiesgoSocialDTO: SituacionRiesgoSocialDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<SituacionRiesgoSocialDTO>> {
        let endPoint = this.path + '/crearSituacionRiesgoSocial';

        return this.backendService.postFinal(
            endPoint,
            situacionRiesgoSocialDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una situación de riesgo social en el sistema con los datos enviados en el request
     *
     * @param SituacionRiesgoSocialDTO datos de la situación de riesgo social a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<SituacionRiesgoSocialDTO>>
     */
    eliminarSituacionRiesgoSocial(
        situacionRiesgoSocialDTO: SituacionRiesgoSocialDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarSituacionRiesgoSocial';
        return this.backendService.postFinal(
            endPoint,
            situacionRiesgoSocialDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
