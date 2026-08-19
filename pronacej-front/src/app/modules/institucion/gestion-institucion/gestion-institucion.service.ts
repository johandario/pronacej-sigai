import { Injectable } from '@angular/core';
import { SeguimientoInstitucionDTO } from 'app/core/model/both/SeguimientoInstitucionDTO.model';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SeguimientoInstitucionService {
    private readonly path = '/institucion-seguimiento';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerRegistroInstituciones(
        paginacionRequest: PaginacionRequest,
        nemonico: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SeguimientoInstitucionDTO>>
    > {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerInstitucionesPorTokenID(
        tokenIdentificador: String
    ): Observable<RespuestaPorDefecto<SeguimientoInstitucionDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            ''
        );
    }

    /**
     * Crear o editar Proceso
     */
    crearEditarInstitucion(
        fuga: SeguimientoInstitucionDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<SeguimientoInstitucionDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Eliminar Fuga
     */
    eliminarInstitucion(traslado: SeguimientoInstitucionDTO, nemonico: string) {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Manejar errores
     * @param error Error recibido
     * @param mostrarError Indica si se debe mostrar el error
     * @returns Retorna el mensaje de error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
