import { Injectable } from '@angular/core';
import { ApreciacionFinalTratamientoDTO } from 'app/core/model/both/apreciacionFinalTratamientoDTO.model';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class ApreciacionFinalTratamientoService {
    private path = '/apreciacion-final-tratamiento';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene las apreciaciones finales del tratamiento disponibles de manera paginada
     *
     * @param paginacionRequest PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<ApreciacionFinalTratamientoDTO>>>
     */
    obtenerApreciacionesFinalesPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ApreciacionFinalTratamientoDTO>>
    > {
        let endPoint = this.path + '/obtenerApreciacionesFinalesPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una apreciación final del tratamiento con los datos enviados en el request
     *
     * @param apreciacionFinalDTO ApreciacionFinalTratamientoDTO datos de la apreciación final a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<ApreciacionFinalTratamientoDTO>>
     */
    crearApreciacionFinal(
        apreciacionFinalDTO: ApreciacionFinalTratamientoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ApreciacionFinalTratamientoDTO>> {
        let endPoint = this.path + '/crearApreciacionFinal';
        return this.backendService.postFinal(
            endPoint,
            apreciacionFinalDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una apreciación final del tratamiento
     *
     * @param apreciacionFinalDTO ApreciacionFinalTratamientoDTO datos de la apreciación final a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarApreciacionFinal(
        apreciacionFinalDTO: ApreciacionFinalTratamientoDTO,
        nemonicoMenu = ''
    ):Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarApreciacionFinal';

        return this.backendService.postFinal(
            endPoint,
            apreciacionFinalDTO,
            nemonicoMenu
        );
    }

    /**
     * Verifica y maneja los errores generales del servicio
     *
     * @param error any error a verificar
     * @param mostrarError boolean indica si se debe mostrar el error
     * @returns any
     */
    async checkError(error: any, mostrarError = true):Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
