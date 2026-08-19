import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { EvaluacionDomiciliariaDTO } from 'app/core/model/both/EvaluacionDomiciliariaDTO.model';
import { EvaluacionDomiciliariaDocumentoDTO } from 'app/core/model/request/ia/EvaluacionDomiciliariaDocumentoDTO.model';
import { EvaluacionDomiciliariaDocumentosRequest } from 'app/core/model/request/ia/EvaluacionDomiciliariaDocumentosRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EvaluacionDomiciliariaService {
    private path = '/evaluacion-domiciliaria';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene las evaluaciones domiciliarias disponibles para el sistema
     * En el sistema multi-jerárquico, el backend filtra automáticamente por la jerarquía del usuario
     *
     * @param paginacionRequest Parámetros de paginación y filtrado
     * @param nemonicoMenu Nemónico del menú del sistema
     *
     * @returns Observable con la respuesta paginada de evaluaciones domiciliarias
     */
    obtenerEvaluacionesDomiciliariasPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EvaluacionDomiciliariaDTO>>
    > {
        let endPoint = this.path + '/obtenerEvaluacionesDomiciliariasPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una evaluación domiciliaria en el sistema con los datos enviados en el request
     * En el sistema multi-jerárquico, el backend asigna automáticamente la jerarquía del usuario
     *
     * @param evaluacionDomiciliariaDTO Datos de la evaluación domiciliaria a crear
     * @param nemonicoMenu Nemónico del menú del sistema
     *
     * @returns Observable con la respuesta de creación
     */
    crearEvaluacionDomiciliaria(
        evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<EvaluacionDomiciliariaDTO>> {
        let endPoint = this.path + '/crearEvaluacionDomiciliaria';
        return this.backendService.postFinal(
            endPoint,
            evaluacionDomiciliariaDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una evaluación domiciliaria en el sistema con los datos enviados en el request
     * En el sistema multi-jerárquico, el backend valida que el usuario tenga permisos sobre la evaluación
     *
     * @param evaluacionDomiciliariaDTO Datos de la evaluación domiciliaria a eliminar
     * @param nemonicoMenu Nemónico del menú del sistema
     *
     * @returns Observable con la respuesta de eliminación
     */
    eliminarEvaluacionDomiciliaria(
        evaluacionDomiciliariaDTO: EvaluacionDomiciliariaDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarEvaluacionDomiciliaria';
        return this.backendService.postFinal(
            endPoint,
            evaluacionDomiciliariaDTO,
            nemonicoMenu
        );
    }

    /**
     * Sube un documento y lo asocia al registro de evaluación domiciliaria
     *
     * @param file Archivo a subir
     * @param evaluacionDomiciliariaDocumentoDTO DTO con la información para la relación entre documento y evaluación domiciliaria
     * @param nemonicoMenu Nemónico del menú del sistema
     *
     * @returns Observable con la respuesta del documento subido
     */
    subirDocumento(
        file: File,
        evaluacionDomiciliariaDocumentoDTO: EvaluacionDomiciliariaDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(evaluacionDomiciliariaDocumentoDTO)
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));
                        this.backendService
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                nemonicoMenu
                            )
                            .subscribe({
                                next: async (
                                    bodyEncriptado: BodyEncriptado
                                ) => {
                                    let resp =
                                        await this.backendService.desencriptarBdyEncriptado<
                                            RespuestaPorDefecto<DocumentoDTO>
                                        >(bodyEncriptado);
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

    /**
     * Obtiene la lista de documentos asociados a una evaluación domiciliaria
     *
     * @param evaluacionDomiciliariaDocumentosRequest Request con parámetros de paginación y búsqueda
     * @param nemonicoMenu Nemónico del menú del sistema
     *
     * @returns Observable con la respuesta paginada de documentos
     */
    obtenerDocumentos(
        evaluacionDomiciliariaDocumentosRequest: EvaluacionDomiciliariaDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(
            endPoint,
            evaluacionDomiciliariaDocumentosRequest,
            nemonicoMenu
        );
    }

    /**
     * Elimina la relación entre un documento y una evaluación domiciliaria
     *
     * @param evaluacionDomiciliariaDocumentoDTO DTO con la información de la relación a eliminar
     * @param nemonicoMenu Nemónico del menú del sistema
     *
     * @returns Observable con la respuesta de eliminación
     */
    eliminarDocumento(
        evaluacionDomiciliariaDocumentoDTO: EvaluacionDomiciliariaDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EvaluacionDomiciliariaDocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumento';
        return this.backendService.postFinal(
            endPoint,
            evaluacionDomiciliariaDocumentoDTO,
            nemonicoMenu
        );
    }

    /**
     * Maneja los errores del servicio
     *
     * @param error Error a manejar
     * @param mostrarError Indica si se debe mostrar el error
     *
     * @returns Resultado del manejo del error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}