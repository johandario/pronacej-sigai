import { Injectable } from '@angular/core';
import { AreasSituacionEducativaLaboralOcioDTO } from 'app/core/model/both/areasSituacionEducativaLaboralOcioDTO.model';
import { LaboralDTO } from 'app/core/model/both/LaboralDTO.model';
import { SituacionEducativaLaboralDTO } from 'app/core/model/both/SituacionEducativaLaboralDTO.model';
import { SituacionEducativaLaboralOcioDTO } from 'app/core/model/both/SituacionEducativaLaboralOcioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class SituacionEducativaLaboralService {
    private path = '/situacion-educativa-laboral';

    constructor(private backendService: BackendService) {}

    /**
     * Obten las areas de la situación educativa laborale y de ocio para el sistema
     *
     * @param nemonicoMenu string nemonico del área de la situacion educativa laboral del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerAreasSituacionEducativaLaboralOcio(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<AreasSituacionEducativaLaboralOcioDTO>> {
        const endPoint =
            this.path + '/obtenerAreasSituacionEducativaLaboralOcio';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obten las situaciones educativas laborales y de ocio para el sistema
     *
     * @param nemonicoMenu string nemonico de una situacion educativa laboral del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerSituacionesEducativasLaboralesOcioPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<SituacionEducativaLaboralOcioDTO>
        >
    > {
        let endPoint =
            this.path + '/obtenerSituacionesEducativasLaboralesOcioPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obten los laborales y de ocio para el sistema
     *
     * @param nemonicoMenu string nemonico de un laboral del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerLaboralesPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<LaboralDTO>>> {
        let endPoint = this.path + '/obtenerLaboralesPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una situación educativa laboral en el sistema con los datos enviados en el request
     *
     * @param SituacionEducativaLaboralDTO datos de la situación educativa laboral a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<SituacionEducativaLaboralDTO>>
     */
    crearSituacionEducativaLaboral(
        situacionEducativaLaboralDTO: SituacionEducativaLaboralDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<SituacionEducativaLaboralDTO>> {
        let endPoint = this.path + '/crearSituacionEducativaLaboral';
        return this.backendService.postFinal(
            endPoint,
            situacionEducativaLaboralDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una situación educativa/laboral/ocio en el sistema con los datos enviados en el request
     *
     * @param SituacionEducativaLaboralOcioDTO datos de la situación educativa/laboral/ocio a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<SituacionEducativaLaboralOcioDTO>>
     */
    eliminarSituacionEducativaLaboralOcio(
        situacionEducativaLaboralOcioDTO: SituacionEducativaLaboralOcioDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarSituacionEducativaLaboralOcio';
        return this.backendService.postFinal(
            endPoint,
            situacionEducativaLaboralOcioDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un laboral en el sistema con los datos enviados en el request
     *
     * @param LaboralDTO datos del laboral a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<LaboralDTO>>
     */
    eliminarLaboral(
        laboral: LaboralDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarLaboral';

        return this.backendService.postFinal(endPoint, laboral, nemonicoMenu);
    }

    /**
     * Verifica y maneja errores en las peticiones.
     *
     * @param error Error recibido
     * @param mostrarError Si debe mostrar o no el error al usuario
     * @returns Observable con el manejo del error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
