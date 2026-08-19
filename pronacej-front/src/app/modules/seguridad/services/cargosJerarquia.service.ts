import { Injectable } from '@angular/core';
import { CargosJerarquiaDTO } from 'app/core/model/both/cargosJerarquiaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { catchError, Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class CargosJerarquiaService {
    private path = '/cargos-jerarquia';

    constructor(private backendService: BackendService) {}

    /**
     * Devuelve una lista CargosJerarquiaDTO
     *
     * @param nemonico string nemonico del cargo por jerarquia
     *
     * @return RespuestaPorDefecto<CargosJerarquiaDTO>
     */
    obtenerCargosJerarquias(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<CargosJerarquiaDTO[]>> {
        let endPoint = this.path + '/obtenerCargosJerarquias';
        return this.backendService.postFinal(
            endPoint,
            nemonicoMenu,
            nemonicoMenu
        );
    }

    /**
     * Devuelve una lista CargosJerarquiaDTO
     *
     * @param nemonico string nemonico del cargo por jerarquia
     *
     * @return RespuestaPorDefecto<CargosJerarquiaDTO>
     */
    obtenerCargosJerarquiasPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ) {
        let endPoint = this.path + '/obtenerCargosJerarquiasPaginado';

        // Asegurar que todos los campos requeridos estén presentes
        if (!paginacionRequest.direction) {
            paginacionRequest.direction = 'desc';
        }

        if (!paginacionRequest.sort) {
            paginacionRequest.sort = 'idCargosJerarquia';
        }

        // Verificar que page y size estén definidos
        if (
            paginacionRequest.page === undefined ||
            paginacionRequest.page === null
        ) {
            paginacionRequest.page = 0;
        }

        if (
            paginacionRequest.size === undefined ||
            paginacionRequest.size === null
        ) {
            paginacionRequest.size = 10;
        }

        console.log('Enviando petición:', JSON.stringify(paginacionRequest));
        return new Observable(
            (
                subscriber: Subscriber<
                    RespuestaPorDefecto<PaginacionResponse<CargosJerarquiaDTO>>
                >
            ) => {
                this.backendService
                    .postFinal<
                        RespuestaPorDefecto<
                            PaginacionResponse<CargosJerarquiaDTO>
                        >
                    >(endPoint, paginacionRequest, nemonicoMenu)
                    .pipe(
                        catchError((error) => {
                            console.error('Error en la petición HTTP:', error);
                            const respuestaError = new RespuestaPorDefecto<
                                PaginacionResponse<CargosJerarquiaDTO>
                            >();
                            respuestaError.exito = false;
                            respuestaError.titulo = 'Error de conexión';
                            respuestaError.mensaje =
                                'No se pudo establecer conexión con el servidor. Por favor, intente nuevamente.';
                            subscriber.next(respuestaError);
                            subscriber.complete();
                            return [];
                        })
                    )
                    .subscribe({
                        next: (
                            respuesta: RespuestaPorDefecto<
                                PaginacionResponse<CargosJerarquiaDTO>
                            >
                        ) => {
                            try {
                                subscriber.next(respuesta);
                                subscriber.complete();
                            } catch (error) {
                                console.error(
                                    'Error al desencriptar respuesta:',
                                    error
                                );
                                const respuestaError = new RespuestaPorDefecto<
                                    PaginacionResponse<CargosJerarquiaDTO>
                                >();
                                respuestaError.exito = false;
                                respuestaError.titulo = 'Error de formato';
                                respuestaError.mensaje =
                                    'Los datos recibidos no tienen el formato esperado. Contacte al administrador.';
                                subscriber.next(respuestaError);
                                subscriber.complete();
                            }
                        },
                        error: (error: any) => {
                            console.error('Error en la suscripción:', error);
                            subscriber.error(error);
                            subscriber.complete();
                        },
                    });
            }
        );
    }

    crearCargoJerarquia(
        cargosJerarquiaDTO: CargosJerarquiaDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<CargosJerarquiaDTO>> {
        let endPoint = this.path + '/crearCargoJerarquia';

        return this.backendService.postFinal(
            endPoint,
            cargosJerarquiaDTO,
            nemonicoMenu
        );
    }

    eliminarCargoJerarquia(
        cargosJerarquiaDTO: CargosJerarquiaDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarCargoJerarquia';
        return this.backendService.postFinal(
            endPoint,
            cargosJerarquiaDTO,
            nemonicoMenu
        );
    }

    obtenerCargosJerarquiaPorValor(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CargosJerarquiaDTO>>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrar = true): Promise<string> {
        return await this.backendService.checkError(error, mostrar);
    }
}
