import { Injectable } from '@angular/core';
import { ActividadOcupacionalDTO } from 'app/core/model/both/ActividadOcupacional.model';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { CondHistViolDTO } from 'app/core/model/both/condHistViolDTO.model';
import { EvaluacionConductualDTO } from 'app/core/model/both/evaluacionConductualDTO.model';
import { SeguimientoActividadOcupacionalDTO } from 'app/core/model/both/SeguimientoActividadOcupacional.model';
import { SituPersCaraPersDTO } from 'app/core/model/both/situPersCaraPersDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EvaluacionConductualService {
    private path = '/evaluacion-conductual';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene evaluaciones conductuales de manera paginada.
     *
     * @param paginacionRequest PaginacionRequest con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta paginada
     */
    obtenerEvaluacionesConductualesPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EvaluacionConductualDTO>>
    > {
        const endPoint = this.path + '/obtenerEvaluacionesConductualesPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obtiene las situaciones personales y características personales de manera paginada.
     *
     * @param paginacionRequest PaginacionRequest con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta paginada
     */
    obtenerSituPersCaraPersPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SituPersCaraPersDTO>>
    > {
        const endPoint = this.path + '/obtenerSituPersCaraPersPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obtiene las conductas e historias de violencias de manera paginada.
     *
     * @param paginacionRequest PaginacionRequest con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta paginada
     */
    obtenerCondHistViolPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CondHistViolDTO>>> {
        const endPoint = this.path + '/obtenerCondHistViolPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una evaluación conductual.
     *
     * @param evaluacionConductualDTO Datos de la evaluación a crear
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta
     */
    crearEvaluacionConductual(
        evaluacionConductualDTO: EvaluacionConductualDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<EvaluacionConductualDTO>> {
        const endPoint = this.path + '/crearEvaluacionConductual';
        return this.backendService.postFinal(
            endPoint,
            evaluacionConductualDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una evaluación conductual general.
     *
     * @param evaluacionConductualDTO Datos de la evaluación conductual a eliminar
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta
     */
    eliminarEvaluacionConductual(
        evaluacionConductualDTO: EvaluacionConductualDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        const endPoint = this.path + '/eliminarEvaluacionConductual';
        return this.backendService.postFinal(
            endPoint,
            evaluacionConductualDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una situación personal y características personales.
     *
     * @param evaluacionConductualDTO Datos de la situación personal a eliminar
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta
     */
    eliminarSituPersCaraPers(
        situPersCaraPersDTO: SituPersCaraPersDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        const endPoint = this.path + '/eliminarSituPersCaraPers';
        return this.backendService.postFinal(
            endPoint,
            situPersCaraPersDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una conducta e historia de violencia.
     *
     * @param evaluacionConductualDTO Datos de la situación personal a eliminar
     * @param nemonicoMenu string nemonico del menú en el sistema
     * @returns Observable con la respuesta
     */
    eliminarCondHistViol(
        condHistViolDTO: CondHistViolDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        const endPoint = this.path + '/eliminarCondHistViol';
        return this.backendService.postFinal(
            endPoint,
            condHistViolDTO,
            nemonicoMenu
        );
    }

    crearOActualizarActividadOcupacional(
        actividad: ActividadOcupacionalDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ActividadOcupacionalDTO>> {
        let endPoint = this.path + '/crearOActualizarActividadOcupacional';
        return this.backendService.postFinal(endPoint, actividad, nemonicoMenu);
    }

    listarActividadesOcupacionales(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ActividadOcupacionalDTO>>
    > {
        let endPoint = this.path + '/listarActividadesOcupacionales';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    eliminarActividadOcupacional(
        tokenIdentificador: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ActividadOcupacionalDTO>> {
        let endPoint = this.path + '/eliminarActividadOcupacional';
        // Suponemos que el backend espera el tokenIdentificador como parte del cuerpo de la solicitud
        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }

    obtenerActividadOcupacionalPorToken(
        tokenIdentificador: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ActividadOcupacionalDTO>> {
        let endPoint = this.path + '/obtenerActividadOcupacional';
        // Enviar tokenIdentificador como cuerpo de la solicitud
        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }

    crearSeguimientoActividadOcupacional(
        seguimientoDTO: SeguimientoActividadOcupacionalDTO
    ): Observable<RespuestaPorDefecto<SeguimientoActividadOcupacionalDTO>> {
        let endPoint = `${this.path}/crearOEditarSeguimientoActividadOcupacional`;
        return this.backendService.postFinal(endPoint, seguimientoDTO, '');
    }

    obtenerSeguimientosPorActividadOcupacional(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<SeguimientoActividadOcupacionalDTO>
        >
    > {
        let endPoint = `${this.path}/listarSeguimientosPorActividad`;
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    eliminarSeguimientoActividadOcupacional(
        seguimientoDTO: SeguimientoActividadOcupacionalDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = `${this.path}/eliminarSeguimientoActividadOcupacional`;
        return this.backendService.postFinal(endPoint, seguimientoDTO, '');
    }

    /**
     * Verifica y maneja errores en las peticiones.
     *
     * @param error Error recibido
     * @param mostrarError Si debe mostrar o no el error al usuario
     * @returns Observable con el manejo del error
     */
    async checkError(error: any, mostrarError = true):Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
