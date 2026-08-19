import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import {
    InstanciaProcesoDTO,
    TareaDTO,
} from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { ProcesoDTO } from 'app/core/model/both/flujo/ProcesoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FlujoTrabajoService {
    private readonly path = '/motor-flujo';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de procesos
     */
    obtenerProcesos(
        paginacionRequest: PaginacionRequest,
        nemonico: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<ProcesoDTO>>> {
        let endPoint = this.path + '/proceso/lista';

        return this.backendService.postFinal(endPoint, paginacionRequest, nemonico);
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerProcesoPorTokenID(
        tokenIdentificador: String,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<ProcesoDTO>> {
        let endPoint = this.path + '/proceso/buscar';
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            nemonicoMenu
        );
    }

    /**
     * Crear o editar Proceso
     */
    crearEditarProceso(
        proceso: ProcesoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<ProcesoDTO>> {
        let endPoint = this.path + '/proceso/crear';
        return this.backendService.postFinal(endPoint, proceso, nemonico);
    }

    /**
     * Eliminar Proceso
     */
    eliminarProceso(
        proceso: ProcesoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<ProcesoDTO>> {
        let endPoint = this.path + '/proceso/eliminar';
        return this.backendService.postFinal(endPoint, proceso, nemonico);
    }

    /**
     * Crear o editar Proceso
     */
    crearInstanciaProcesoPorProceso(
        proceso: ProcesoDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<InstanciaProcesoDTO>> {
        let endPoint = this.path + '/instancia-proceso/crear';
        return this.backendService.postFinal(endPoint, proceso, nemonico);
    }

    /**
     * Eliminar instancia proceso por tarea relacionada
     */
    eliminarInstanciaProcesoPorTarea(
        proceso: TareaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<InstanciaProcesoDTO>> {
        let endPoint = this.path + '/instancia-proceso/eliminar-por-tarea';
        return this.backendService.postFinal(endPoint, proceso, nemonico);
    }

    /*
     * Obtener lista de tareas
     */
    obtenerTareas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TareaDTO>>> {
        let endPoint = this.path + '/tareas/lista';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Obtener lista de tareas enviadas
     */
    obtenerTareasEnviadas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TareaDTO>>> {
        let endPoint = this.path + '/tareas/lista-enviadas';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    // obtenerTareasEnviadasPorTipo(
    //     paginacionRequest: PaginacionRequest,
    //     tipo: string
    // ) {
    //     let endPoint = this.path + '/tareas/lista-enviadas/tipo';

    //     return new Observable(
    //         (
    //             subscriber: Subscriber<
    //                 RespuestaPorDefecto<PaginacionResponse<TareaDTO>>
    //             >
    //         ) => {
    //             this.backendService
    //                 .postJsonGeneralBodyEncriptadoParam(
    //                     endPoint,
    //                     paginacionRequest,
    //                     { tipo: tipo }
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

     /*
     * Obtener lista de tareas enviadas
     */
     obtenerTareasEnviadasPorTipo(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TareaDTO>>> {
        let endPoint = this.path + '/tareas/lista-enviadas/tipo';

        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Obtener lista de tareas enviadas
     */
    obtenerTareasRecibidas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TareaDTO>>> {
        let endPoint = this.path + '/tareas/lista-recibidas';

        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Obtener lista de tareas borrador creadas
     */
    obtenerTareasBorrador(
        paginacionRequest: PaginacionRequest,
        nemonico: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<TareaDTO>>> {
        let endPoint = this.path + '/tareas/lista-borrador';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonico);
    }

    obtenerTiposTareasEnviadas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<string[]>> {
        let endPoint = this.path + '/tareas/tipos';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /**
     * Crear o editar Proceso
     */
    obtenerTareasFlujoPorTarea(
        tarea: TareaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<TareaDTO[]>> {
        let endPoint = this.path + '/tareas/tareaPorTarea';
        return this.backendService.postFinal(endPoint, tarea, nemonico);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
