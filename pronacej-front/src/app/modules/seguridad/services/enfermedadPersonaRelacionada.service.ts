import { Injectable } from '@angular/core';
import { PersonaRelacionadaEnfermedadDTO } from 'app/core/model/both/personaRelacionadaEnfermedadDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EnfermedadPersonaRelacionadaService {
    private path = '/personaRelacionadaEnfermedad';

    constructor(private backendService: BackendService) {}

    /**
     *
     * Obtiene las personas relacionadas a un usuario por medio de su tokenIdentificador
     *
     * @param paginacionRequest objeto que contiene los parametros necesarios para la consulta
     *
     * @returns Observable<Navigation>
     */

    obtenerEnfermedadPersonasRelacionadas(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaEnfermedadDTO>>
    > {
        let endPoint = this.path + '/obtenerPersonaRelacionadaEnfermedad';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }
}
