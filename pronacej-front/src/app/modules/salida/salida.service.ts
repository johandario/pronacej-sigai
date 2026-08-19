import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { RegistroSalidaDTO } from 'app/core/model/both/salida/RegistroSalidaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SalidaService {
    private readonly path = '/registro-salida';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerRegistroSalidas(
        paginacionRequest: PaginacionRequest,
        nemonico: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<RegistroSalidaDTO>>> {
        let endPoint = this.path + '/lista';

        return this.backendService.postFinal(endPoint, paginacionRequest, nemonico);
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerSalidasPorTokenID(
        tokenIdentificador: String,
        nemonicoMenu: string,
    ): Observable<RespuestaPorDefecto<RegistroSalidaDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            nemonicoMenu
        );
    }

    /**
     * Crear o editar Proceso
     */
    crearEditarSalida(
        fuga: RegistroSalidaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<RegistroSalidaDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Eliminar Fuga
     */
    eliminarSalida(
        traslado: RegistroSalidaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    obtenerlistadoPorToken(
        paginacionRequest: PaginacionRequest
    ): Observable<RespuestaPorDefecto<PaginacionResponse<RegistroSalidaDTO>>> {
        let endPoint = this.path + '/listado/token';
        console.log(endPoint);
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    obtenerlistadoPorTokenCompletos(
        paginacionRequest: PaginacionRequest
    ): Observable<RespuestaPorDefecto<PaginacionResponse<RegistroSalidaDTO>>> {
        let endPoint = this.path + '/listadoCompletos/token';
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
