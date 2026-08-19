import { Injectable } from '@angular/core';
import { InformeSeguimientoPIIDTO } from 'app/core/model/both/informeSeguimientoPIIDTO.model';
import { InstrumentoEvaluacionDTO } from 'app/core/model/both/instrumentoEvaluacionDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformeSeguimientoPIIService {
    private path = '/informe-seguimiento';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene los informes de seguimiento disponibles para el sistema de manera paginada
     * @param paginacionRequest Datos de paginación
     * @param nemonicoMenu string nemonico del menú
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<InformeSeguimientoPIIDTO>>>
     */
    obtenerInformesSeguimientoPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<InformeSeguimientoPIIDTO>>
    > {
        let endPoint = this.path + '/obtenerInformesSeguimientoPaginado';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea un informe de seguimiento en el sistema con los datos enviados
     * @param informeSeguimientoDTO datos del informe a crear
     * @param nemonicoMenu string nemonico del menú
     * @returns Observable<RespuestaPorDefecto<InformeSeguimientoPIIDTO>>
     */
    crearInformeSeguimiento(
        informeSeguimientoDTO: InformeSeguimientoPIIDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<InformeSeguimientoPIIDTO>> {
        let endPoint = this.path + '/crearInformeSeguimiento';
        return this.backendService.postFinal(
            endPoint,
            informeSeguimientoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un informe de seguimiento del sistema
     * @param informeSeguimientoDTO datos del informe a eliminar
     * @param nemonicoMenu string nemonico del menú
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarInformeSeguimiento(
        informeSeguimientoDTO: InformeSeguimientoPIIDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarInformeSeguimiento';
        return this.backendService.postFinal(
            endPoint,
            informeSeguimientoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un instrumento de evaluación asociado a un informe de seguimiento
     * @param informeSeguimientoDTO datos del instrumento a eliminar
     * @param nemonicoMenu string nemonico del menú
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarInstrumentoPorInformeSeguimiento(
        instrumentoEvaluacionDTO: InstrumentoEvaluacionDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarInstrumentoPorInformeSeguimiento';
        return this.backendService.postFinal(
            endPoint,
            instrumentoEvaluacionDTO,
            nemonicoMenu
        );
    }

    /**
     * Verifica y maneja los errores del servicio
     * @param error error a verificar
     * @param mostrarError boolean indica si se debe mostrar el error
     * @returns resultado del checkError del backendService
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
