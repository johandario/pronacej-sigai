import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { EvaluacionSeguimientoEducativoLaboralDTO } from 'app/core/model/both/evaluacionSeguimientoEducativoLaboralDTO.model';
import { SeguimientoEducativoDTO } from 'app/core/model/both/ia/seguimientoEducativoDTO.model';
import { RecomendacionComentarioPorEvalSeguDTO } from 'app/core/model/both/recomendacionComentarioPorEvalSeguDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EvaluacionSeguimientoEducativoLaboralService {
    private path = '/evaluacion-seguimiento-educativo-laboral';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene las evaluaciones de seguimiento educativo/laboral disponibles para el sistema de manera paginada
     *
     * @param paginacionRequest PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>>>
     */
    obtenerEvaluacionesSeguimientoPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>
        >
    > {
        let endPoint = this.path + '/obtenerEvaluacionesSeguimientoPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obtiene las recomendaciones y comentarios por evaluación de seguimiento disponibles para el sistema de manera paginada
     *
     * @param paginacionRequest PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>>>
     */
    obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>
        >
    > {
        let endPoint =
            this.path +
            '/obtenerRecomendacionesComentariosPorEvaluacionSeguimiento';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una evaluación de seguimiento educativo/laboral en el sistema con los datos enviados en el request
     *
     * @param evaluacionSeguimientoDTO EvaluacionSeguimientoEducativoLaboralDTO datos de la evaluación de seguimiento a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<EvaluacionSeguimientoEducativoLaboralDTO>>
     */
    crearEvaluacionSeguimiento(
        evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO,
        nemonicoMenu: string = ''
    ): Observable<
        RespuestaPorDefecto<EvaluacionSeguimientoEducativoLaboralDTO>
    > {
        let endPoint = this.path + '/crearEvaluacionSeguimiento';
        return this.backendService.postFinal(
            endPoint,
            evaluacionSeguimientoDTO,
            nemonicoMenu
        );
    }

    /**
     * Crea una recomendación y comentario para una evaluación de seguimiento en el sistema
     *
     * @param recomendacionComentarioDTO RecomendacionComentarioPorEvalSeguDTO datos de la recomendación a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<RecomendacionComentarioPorEvalSeguDTO>>
     */
    crearRecomendacionComentario(
        recomendacionComentarioDTO: RecomendacionComentarioPorEvalSeguDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<RecomendacionComentarioPorEvalSeguDTO>> {
        let endPoint = this.path + '/crearRecomendacionComentario';
        return this.backendService.postFinal(
            endPoint,
            recomendacionComentarioDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una evaluación de seguimiento educativo/laboral en el sistema
     *
     * @param evaluacionSeguimientoDTO EvaluacionSeguimientoEducativoLaboralDTO datos de la evaluación de seguimiento a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarEvaluacionSeguimiento(
        evaluacionSeguimientoDTO: EvaluacionSeguimientoEducativoLaboralDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarEvaluacionSeguimiento';
        return this.backendService.postFinal(
            endPoint,
            evaluacionSeguimientoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una recomendación y comentario de una evaluación de seguimiento en el sistema
     *
     * @param recomendacionComentarioDTO RecomendacionComentarioPorEvalSeguDTO datos de la recomendación a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarRecomendacionComentario(
        recomendacionComentarioDTO: RecomendacionComentarioPorEvalSeguDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarRecomendacionComentario';
        return this.backendService.postFinal(
            endPoint,
            recomendacionComentarioDTO,
            nemonicoMenu
        );
    }

    /**
     * Sube un informe firmado
     *
     * @param encabezadoDTO InformeDTO
     * @param archivo File
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<Boolean>>
     */
    subirDocumentos(
        seguimientoDTO: SeguimientoEducativoDTO,
        files: File[],
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/subirDocumentos';

        let formData = new FormData();
        if (files != null) {
            for (let file of files) {
                formData.append('documentos', file);
            }
        }

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                this.backendService
                    .crearBodyEncriptado(seguimientoDTO)
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));
                        this.backendService
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                nemonicoMenu
                            )
                            .subscribe({
                                next: async (body: BodyEncriptado) => {
                                    let resp =
                                        await this.backendService.desencriptarBdyEncriptado<
                                            RespuestaPorDefecto<Boolean>
                                        >(body);
                                    subs.next(resp);
                                    subs.complete();
                                },
                                error: (error: any) => {
                                    subs.error(error);
                                    subs.complete();
                                },
                            });
                    })
                    .catch((error: any) => {
                        subs.error(error);
                        subs.complete();
                    });
            }
        );
    }

    obtenerDocumentos(
        request: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';

        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }

    /**
     * Verifica y maneja los errores generales del servicio
     *
     * @param error any error a verificar
     * @param mostrarError boolean indica si se debe mostrar el error
     * @returns any
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
