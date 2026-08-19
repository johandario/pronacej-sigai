import { Injectable } from '@angular/core';
import { InformacionUbicacionDTO } from 'app/core/model/both/InformacionUbicacionDTO.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformacionUbicacionService {
    private path = '/informacionUbicacion';

    constructor(private backendService: BackendService) {}

    /**
     * Crea una ficha de identificacion en el sistema con los datos enviados en el request
     *
     * @param inforacionUbicacionDTO InforacionUbicacionDTO datos de la informacion ubicacion a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>>
     */
    crearInformacionUbicacion(
        inforacionUbicacionDTO: InformacionUbicacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<InformacionUbicacionDTO>> {
        let endPoint = this.path + '/crearInformacionUbicacion';
        return this.backendService.postFinal(
            endPoint,
            inforacionUbicacionDTO,
            nemonicoMenu
        );
    }

    /**
     *
     * Obtiene la informacion de ubicaciones de la persona relacionada por medio de id
     *
     * @param idPersonaRelacionada id de la persona relacionada de la cual se obtendran las direcciones
     *
     * @returns Observable<Navigation>
     */

    obtenerInformacionUbicacionesRelacionadas(
        idPersonaRelacionada: number,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<InformacionUbicacionDTO>>
    > {
        let endPoint = this.path + '/obtenerInformacionUbicacionesPersona';
        return this.backendService.postFinal(
            endPoint,
            idPersonaRelacionada,
            nemonicoMenu
        );
    }

    /**
     * Elimina una informacion ubicacion en el sistema con los datos enviados en el request
     *
     * @param informacionUbicacionDTO InformacionUbicacionDTO datos de la informacion ubicacion a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<InformacionUbicacionDTO>>
     */
    eliminarInformacionUbicacion(
        informacionUbicacionDTO: InformacionUbicacionDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarInformacionUbicacion';
        return this.backendService.postFinal(
            endPoint,
            informacionUbicacionDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
