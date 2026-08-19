import { Injectable } from '@angular/core';
import { InformeVisitasDTO } from 'app/core/model/both/informeVisitasDTO.model';
import { InformeVisitasPorPersonaDTO } from 'app/core/model/both/informeVisitasPorPersonaDTO.model';
import { SuspensionVisitasDTO } from 'app/core/model/both/suspensionVisitasDTO.model';
import { SuspensionVisitasPorPersonaDTO } from 'app/core/model/both/suspensionVisitasPorPersonaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformeVisitasService {
    private path = '/informe-visitas';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene los informes de visitas disponibles para el sistema con paginación
     *
     * @param paginacionRequest Datos para la paginación
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<InformeVisitasDTO>>>
     */
    obtenerInformesVisitasPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<InformeVisitasDTO>>> {
        let endPoint = this.path + '/obtenerInformesVisitasPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obtiene las suspensiones de visitas disponibles para el sistema con paginación
     *
     * @param paginacionRequest Datos para la paginación
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<SuspensionVisitasDTO>>>
     */
    obtenerSuspensionVisitasPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SuspensionVisitasDTO>>
    > {
        let endPoint = this.path + '/obtenerSuspensionVisitasPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea o actualiza informes de visitas en el sistema
     *
     * @param informeVisitasPorPersonaDTO datos de los informes de visitas a crear o actualizar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<InformeVisitasDTO>>
     */
    crearInformeVisitas(
        informeVisitasPorPersonaDTO: InformeVisitasPorPersonaDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<InformeVisitasDTO>> {
        let endPoint = this.path + '/crearInformeVisitas';
        return this.backendService.postFinal(
            endPoint,
            {
                ...informeVisitasPorPersonaDTO,
                nemonicoMenu,
            },
            nemonicoMenu
        );
    }

    /**
     * Crea o actualiza suspensiones de visitas en el sistema
     *
     * @param suspensionVisitasPorPersonaDTO datos de las suspensiones de visitas a crear o actualizar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<SuspensionVisitasDTO>>
     */
    crearSuspensionVisitas(
        suspensionVisitasPorPersonaDTO: SuspensionVisitasPorPersonaDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<SuspensionVisitasDTO>> {
        let endPoint = this.path + '/crearSuspensionVisitas';
        return this.backendService.postFinal(
            endPoint,
            {
                ...suspensionVisitasPorPersonaDTO,
                nemonicoMenu,
            },
            nemonicoMenu
        );
    }

    /**
     * Elimina un informe de visitas del sistema
     *
     * @param informeVisitasDTO datos del informe de visitas a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarInformeVisitas(
        informeVisitasDTO: InformeVisitasDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarInformeVisitas';
        return this.backendService.postFinal(
            endPoint,
            informeVisitasDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una suspensión de visitas del sistema
     *
     * @param suspensionVisitasDTO datos de la suspensión de visitas a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarSuspensionVisitas(
        suspensionVisitasDTO: SuspensionVisitasDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarSuspensionVisitas';
        return this.backendService.postFinal(
            endPoint,
            suspensionVisitasDTO,
            nemonicoMenu
        );
    }

    /**
     * Maneja los errores del servicio
     *
     * @param error Error a manejar
     * @param mostrarError Indica si se debe mostrar el error
     *
     * @returns Resultado del manejo del error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
