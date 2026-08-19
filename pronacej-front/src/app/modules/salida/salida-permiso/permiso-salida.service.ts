import { Injectable } from '@angular/core';
import { PermisoSalidaDTO } from 'app/core/model/both/salida/PermisoSalidaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class PermisoSalidaService {
    private readonly path = '/permiso-salida';

    constructor(private readonly backendService: BackendService) {}

    obtenerPermisoSalidas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<PermisoSalidaDTO>>> {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerPermisoSalidasPorTokenID(
        tokenIdentificador: String,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<PermisoSalidaDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            nemonicoMenu
        );
    }

    crearEditarPermisoSalidas(
        fuga: PermisoSalidaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<PermisoSalidaDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    eliminarPermisoSalidas(
        traslado: PermisoSalidaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    obtenerlistadoPorToken(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu
    ): Observable<RespuestaPorDefecto<PaginacionResponse<PermisoSalidaDTO>>> {
        let endPoint = this.path + '/listado/token';
        console.log(endPoint);
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /**
     * Obtener eventos de fuga por ID de Ficha de Identificación
     * @param idFichaIdentificacion ID de la ficha de identificación
     * @returns Observable con la lista de eventos relacionados
     */
    obtenerPermisosPorFichaIdentificacion(
        idFichaIdentificacion: number,
    ): Observable<RespuestaPorDefecto<PermisoSalidaDTO[]>> {
        const endPoint = `${this.path}/buscar-por-ficha`;
        console.log(idFichaIdentificacion);
        return this.backendService.postFinal(
            endPoint,
            {
                idFichaIdentificacion,
            },
        );
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

    /**
     * Obtener director por jerarquía
     * @param nombreJerarquia Nombre de la jerarquía
     * @returns Observable con los datos del director
     */
    obtenerDirectorPorJerarquia(dataJerarquia: {
        nombreJerarquia: string;
    }): Observable<
        RespuestaPorDefecto<{ nombres: string; apellidos: string }>
    > {
        const endPoint = `${this.path}/por-jerarquia`;
        return this.backendService.postFinal(endPoint, dataJerarquia, '');
    }

        /**
     * Obtener director por ID de departamento
     * @param idDepartamento ID del departamento
     * @returns Observable con los datos del director
     */
    obtenerDirector(data: {
            idDepartamento: number;
        }): Observable<RespuestaPorDefecto<{ nombres: string; apellidos: string }>> {
            const endPoint = `${this.path}/directores`;
            return this.backendService.postFinal(endPoint, data, '');
    }

    actualizarPermisoSalida(tokenIdentificador: string, estadoEvento: any) {
        let endPointBuscar = `${this.path}/buscar`;
        let endPointActualizar = `${this.path}/crear`;
        return new Observable(
            (subscriber: Subscriber<RespuestaPorDefecto<PermisoSalidaDTO>>) => {
                if (!estadoEvento) {
                    console.error(
                        ' Error: `estadoEvento` no ha sido cargado aún.'
                    );
                    subscriber.error(
                        'El estado de permiso no está disponible.'
                    );
                    return;
                }
                this.backendService
                    .getFinal<
                        RespuestaPorDefecto<PermisoSalidaDTO>
                    >(endPointBuscar, { ID: tokenIdentificador }, '')
                    .subscribe({
                        next: (
                            respuesta: RespuestaPorDefecto<PermisoSalidaDTO>
                        ) => {
                            let permisoSalida = respuesta.data;
                            permisoSalida.estadoEvento = estadoEvento;
                            permisoSalida.isComplete = true;
                            permisoSalida.esEdicion = true;
                            this.backendService
                                .postFinal<
                                    RespuestaPorDefecto<PermisoSalidaDTO>
                                >(endPointActualizar, permisoSalida)
                                .subscribe({
                                    next: (
                                        bodyEncriptadoActualizado: RespuestaPorDefecto<PermisoSalidaDTO>
                                    ) => {
                                        subscriber.next(
                                            bodyEncriptadoActualizado
                                        );
                                        subscriber.complete();
                                    },
                                    error: (error: any) => {
                                        console.error(
                                            ' Error al actualizar permiso de salida:',
                                            error
                                        );
                                        subscriber.error(error);
                                        subscriber.complete();
                                    },
                                });
                        },
                        error: (error: any) => {
                            console.error(
                                ' Error al buscar permiso de salida:',
                                error
                            );
                            subscriber.error(error);
                            subscriber.complete();
                        },
                    });
            }
        );
    }
}
