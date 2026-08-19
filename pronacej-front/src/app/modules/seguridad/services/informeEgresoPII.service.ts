import { Injectable } from '@angular/core';
import { InformeEgresoPIIDTO } from 'app/core/model/both/informeEgresoPIIDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformeEgresoPIIService {
    private path = '/informe-egreso-pii';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene los informes de egreso PII disponibles para el sistema de manera paginada
     *
     * @param paginacionRequest PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<InformeEgresoPIIDTO>>>
     */
    obtenerInformesEgresoPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<InformeEgresoPIIDTO>>
    > {
        let endPoint = this.path + '/obtenerInformesEgresoPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea un informe de egreso PII en el sistema con los datos enviados en el request
     *
     * @param informeEgresoDTO InformeEgresoPIIDTO datos del informe de egreso a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<InformeEgresoPIIDTO>>
     */
    crearInformeEgreso(
        informeEgresoDTO: InformeEgresoPIIDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<InformeEgresoPIIDTO>> {
        let endPoint = this.path + '/crearInformeEgreso';
        return this.backendService.postFinal(
            endPoint,
            informeEgresoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un informe de egreso PII en el sistema
     *
     * @param informeEgresoDTO InformeEgresoPIIDTO datos del informe de egreso a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarInformeEgreso(
        informeEgresoDTO: InformeEgresoPIIDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarInformeEgreso';
        return this.backendService.postFinal(
            endPoint,
            informeEgresoDTO,
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
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
