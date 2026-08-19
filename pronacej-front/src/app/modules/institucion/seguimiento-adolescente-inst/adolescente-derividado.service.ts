import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { AdolescDerivadoInstDTO } from 'app/core/model/both/salida/AdolescDerivadoInstDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdolescenteDerivadoInstService {
    private readonly path = '/adolescente-derivado';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerRegistroSalidas(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<AdolescDerivadoInstDTO>>
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
    ): Observable<RespuestaPorDefecto<AdolescDerivadoInstDTO>> {
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
        fuga: AdolescDerivadoInstDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<AdolescDerivadoInstDTO>> {
        let endPoint = this.path + '/crear';
        console.log(endPoint);
        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Eliminar Fuga
     */
    eliminarSalida(
        traslado: AdolescDerivadoInstDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    obtenerlistadoPorToken(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<AdolescDerivadoInstDTO>>
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
