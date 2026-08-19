import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { TareaTrasladoDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { List } from 'lodash';
import { Observable, Subscriber } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TrasladoService {
    private readonly path = '/traslados';

    constructor(private readonly backendService: BackendService) { }

    /*
     * Obtener lista de procesos
     */
    obtenerTraslados(
        paginacionRequest: PaginacionRequest
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TrasladoDTO>>> {
        let endPoint = this.path + '/lista';

        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /*
     * Obtener lista de traslados
     */
    obtenerTrasladosPorIdFicha(
        paginacionRequest: PaginacionRequest,
        idFichaIdentificacion: number
    ) {
        let endPoint = this.path + '/buscar-por-ficha';

        return new Observable(
            (
                subscriber: Subscriber<
                    RespuestaPorDefecto<PaginacionResponse<TrasladoDTO>>
                >
            ) => {
                this.backendService
                    .postJsonGeneralBodyEncriptadoParam(
                        endPoint,
                        paginacionRequest,
                        { ID: idFichaIdentificacion }
                    )
                    .subscribe({
                        next: async (bodyEncriptado: BodyEncriptado) => {
                            subscriber.next(
                                await this.backendService.desencriptarBdyEncriptado(
                                    bodyEncriptado
                                )
                            );
                            subscriber.complete();
                        },
                        error: (error: any) => {
                            subscriber.error(error);
                            subscriber.complete();
                        },
                    });
            }
        );
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerTrasladoPorTokenID(
        tokenIdentificador: String,
        nemonicoMenu: string,
    ): Observable<RespuestaPorDefecto<TrasladoDTO>> {
        let endPoint = this.path + '/buscar';
        console.log(endPoint);
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            nemonicoMenu
        );
    }

    /**
     * Crear o editar Proceso
     */
    crearEditarTraslado(
        traslado: TareaTrasladoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<TrasladoDTO>> {
        let endPoint = this.path + '/crear';

        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Guardar borrador
     */
    guardarBorrador(
        traslado: TareaTrasladoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<TrasladoDTO>> {
        let endPoint = this.path + '/guardarBorrador';

        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Eliminar Proceso
     */
    eliminarTraslado(
        traslado: TrasladoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<TrasladoDTO>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Rechazar Proceso
     */
    rechazarTraslado(
        traslado: TareaTrasladoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<TrasladoDTO>> {
        let endPoint = this.path + '/rechazar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Obtener lista de traslados por ID de ficha de identificación
     */
    obtenerListadoTrasladosPorAdolescente(
        idFichaIdentificacion: number
    ): Observable<RespuestaPorDefecto<List<TrasladoDTO>>> {
        const endPoint = this.path + '/buscar-traslados-por-ficha';
        const body = { idFichaIdentificacion };
        return this.backendService.postFinal(endPoint, body, '');
    }

    /*
     * Obtener lista de traslados por token identificador
     */
    obtenerTrasladosPorTokenFicha(
        paginacionRequest: PaginacionRequest
    ) {
        let endPoint = this.path + '/buscar-por-ficha-tokenIdentificador';

        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }
    // obtenerTrasladosPorTokenFicha(paginacionRequest: PaginacionRequest) {
    //     let endPoint = this.path + '/buscar-por-ficha-tokenIdentificador';

    //     return new Observable(
    //         (
    //             subscriber: Subscriber<
    //                 RespuestaPorDefecto<PaginacionResponse<TrasladoDTO>>
    //             >
    //         ) => {
    //             this.backendService
    //                 .postJsonGeneralBodyEncriptadoParam(
    //                     endPoint,
    //                     paginacionRequest
    //                 )
    //                 .subscribe({
    //                     next: async (bodyEncriptado: BodyEncriptado) => {
    //                         subscriber.next(
    //                             await this.backendService.desencriptarBdyEncriptado(
    //                                 bodyEncriptado
    //                             )
    //                         );
    //                         subscriber.complete();
    //                     },
    //                     error: (error: any) => {
    //                         subscriber.error(error);
    //                         subscriber.complete();
    //                     },
    //                 });
    //         }
    //     );
    // }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
