import { Injectable } from '@angular/core';
import { SeguimientoAdolescInstDTO } from 'app/core/model/both/salida/SeguimientoAdolcInstDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SeguimientoAdoelscInstService {
    private readonly path = '/seguimiento-adolescente';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerRegistroSalidas(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SeguimientoAdolescInstDTO>>
    > {
        let endPoint = this.path + '/lista';
        console.log(endPoint);

        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerSalidasPorTokenID(
        tokenIdentificador: String
    ): Observable<RespuestaPorDefecto<SeguimientoAdolescInstDTO>> {
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
    crearEditarSalida(
        fuga: SeguimientoAdolescInstDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<SeguimientoAdolescInstDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Eliminar Fuga
     */
    eliminarSalida(
        traslado: SeguimientoAdolescInstDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    obtenerlistadoPorToken(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SeguimientoAdolescInstDTO>>
    > {
        let endPoint = this.path + '/listado/token';
        console.log(endPoint);
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
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
