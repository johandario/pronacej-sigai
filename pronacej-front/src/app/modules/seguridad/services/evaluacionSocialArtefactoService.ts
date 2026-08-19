import { Injectable } from '@angular/core';
import { EvaluacionSocialArtefactoDTO } from 'app/core/model/both/EvaluacionSocialArtefactoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EvaluacionSocialArtefactoService {
    private path = '/evaluacion-social-artefacto';

    constructor(private backendService: BackendService) {}

    /**
     * Obten los artefactos por evaluación social disponibles para el sistema
     *
     * @param nemonicoMenu string nemonico de un artefacto perteneciente a una evaluación social del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerArtefactosPorEvaluacionSocialPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EvaluacionSocialArtefactoDTO>>
    > {
        let endPoint =
            this.path + '/obtenerArtefactosPorEvaluacionSocialPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea un artefacto perteneciente a una evaluación social en el sistema con los datos enviados en el request
     *
     * @param EvaluacionSocialArtefactoDTO datos del artefacto perteneciente a una evaluación social a crear
     * @param nemonicoMenu string nemonico artefacto perteneciente a una evaluación social del sistema
     *
     * @returns Observable<RespuestaPorDefecto<EvaluacionSocialArtefactoDTO>>
     */
    crearArtefactoPorEvaluacionSocial(
        evaluacionSocialArtefactoDTO: EvaluacionSocialArtefactoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<EvaluacionSocialArtefactoDTO>> {
        let endPoint = this.path + '/crearArtefactoPorEvaluacionSocial';
        return this.backendService.postFinal(
            endPoint,
            evaluacionSocialArtefactoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un artefacto perteneciente a una evaluación social en el sistema con los datos enviados en el request
     *
     * @param EvaluacionSocialArtefactoDTO datos del artefacto perteneciente a una evaluación social a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<EvaluacionSocialArtefactoDTO>>
     */
    eliminarArtefactoPorEvaluacionSocial(
        evaluacionSocialArtefactoDTO: EvaluacionSocialArtefactoDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarArtefactoPorEvaluacionSocial';
        return this.backendService.postFinal(
            endPoint,
            evaluacionSocialArtefactoDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true):Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
