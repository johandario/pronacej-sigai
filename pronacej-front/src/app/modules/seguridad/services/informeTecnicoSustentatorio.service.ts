import { Injectable } from '@angular/core';
import { InformeTecnicoSustentatorioDTO } from 'app/core/model/both/informeTecnicoSustentatorioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformeTecnicoSustentatorioService {
    private path = '/informe-tecnico';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene los informes técnicos disponibles para el sistema
     *
     * @param paginacionRequest Datos de paginación
     * @param nemonicoMenu string nemonico de un informe técnico del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<InformeTecnicoSustentatorioDTO>>>
     */
    obtenerInformesTecnicosPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<InformeTecnicoSustentatorioDTO>>
    > {
        let endPoint = this.path + '/obtenerInformesTecnicosPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea un informe técnico en el sistema con los datos enviados en el request
     *
     * @param informeTecnicoDTO datos del informe técnico a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<InformeTecnicoSustentatorioDTO>>
     */
    crearInformeTecnico(
        informeTecnicoDTO: InformeTecnicoSustentatorioDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<InformeTecnicoSustentatorioDTO>> {
        let endPoint = this.path + '/crearInformeTecnico';
        return this.backendService.postFinal(
            endPoint,
            informeTecnicoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un informe técnico en el sistema con los datos enviados en el request
     *
     * @param informeTecnicoDTO datos del informe técnico a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarInformeTecnico(
        informeTecnicoDTO: InformeTecnicoSustentatorioDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarInformeTecnico';
        return this.backendService.postFinal(
            endPoint,
            informeTecnicoDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
