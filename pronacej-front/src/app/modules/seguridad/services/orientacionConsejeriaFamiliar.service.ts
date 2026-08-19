import { Injectable } from '@angular/core';
import { OrientacionConsejeriaFamiliarDTO } from 'app/core/model/both/orientacionConsejeriaFamiliarDTO.model';
import { OrientacionConsejeriaPorPersonaDTO } from 'app/core/model/both/orientacionConsejeriaPorPersonaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class OrientacionConsejeriaFamiliarService {
    private path = '/orientacion-consejeria-familiar';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene las orientaciones y consejerías familiares paginadas para el sistema
     *
     * @param paginacionRequest request con los datos de paginación
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>>>
     */
    obtenerOrientacionesConsejeriasFamiliaresPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<OrientacionConsejeriaFamiliarDTO>
        >
    > {
        let endPoint =
            this.path + '/obtenerOrientacionesConsejeriasFamiliaresPaginado';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea o edita orientaciones/consejerías familiares en el sistema con los datos enviados en el request
     *
     * @param orientacionPorPersonaDTO datos que contienen el tokenIdentificador de la persona relacionada y la lista de orientaciones/consejerías a crear o editar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<OrientacionConsejeriaFamiliarDTO>>
     */
    crearOrientacionConsejeriaFamiliar(
        orientacionPorPersonaDTO: OrientacionConsejeriaPorPersonaDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<OrientacionConsejeriaFamiliarDTO>> {
        let endPoint = this.path + '/crearOrientacionConsejeriaFamiliar';
        return this.backendService.postFinal(
            endPoint,
            orientacionPorPersonaDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una orientación/consejería familiar del sistema
     *
     * @param orientacionDTO datos de la orientación/consejería a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarOrientacionConsejeriaFamiliar(
        orientacionDTO: OrientacionConsejeriaFamiliarDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarOrientacionConsejeriaFamiliar';
        return this.backendService.postFinal(
            endPoint,
            orientacionDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
