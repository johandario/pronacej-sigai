import { Injectable } from '@angular/core';
import { EvaluacionSocialDTO } from 'app/core/model/both/EvaluacionSocialDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EvaluacionSocialService {
    private path = '/evaluacion-social';

    constructor(private backendService: BackendService) {}

    /**
     * Obten las evaluaciones sociales para el sistema
     *
     * @param nemonicoMenu string nemonico de una evaluacion social del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerEvaluacionesSocialesPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EvaluacionSocialDTO>>
    > {
        let endPoint = this.path + '/obtenerEvaluacionesSocialesPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una evaluación social en el sistema con los datos enviados en el request
     *
     * @param EvaluacionSocialDTO datos de la evaluación social a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<EvaluacionSocialDTO>>
     */
    crearEvaluacionSocial(
        evaluacionSocialDTO: EvaluacionSocialDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<EvaluacionSocialDTO>> {
        let endPoint = this.path + '/crearEvaluacionSocial';
        return this.backendService.postFinal(
            endPoint,
            evaluacionSocialDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una evaluación social en el sistema con los datos enviados en el request
     *
     * @param EvaluacionSocialDTO datos de la evaluación social a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<EvaluacionSocialDTO>>
     */
    eliminarEvaluacionSocial(
        evaluacionSocialDTO: EvaluacionSocialDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarEvaluacionSocial';
        return this.backendService.postFinal(
            endPoint,
            evaluacionSocialDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
