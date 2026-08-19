import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { EvaluacionDocumentoRequest } from 'app/core/model/request/general/EvaluacionDocumentoRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EncuestaService {
    private path = '/encuesta';

    constructor(private backendService: BackendService) {}

    obtenerListaEncuestas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<EncuestaDTO>>> {
        let endPoint = this.path + '/obtenerListaEncuestas';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerEncuestas(
        encuestaDTO: EncuestaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EncuestaDTO[]>> {
        let endPoint = this.path + '/obtenerEncuestas';
        return this.backendService.postFinal(
            endPoint,
            encuestaDTO,
            nemonicoMenu
        );
    }

    obtenerEvaluacionesPorFichaIdentificacion(
        paginacionRequest: PaginacionRequest,
        nemonicoCategoria: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>> {
        let endPoint =
            this.path +
            '/obtenerEvaluacionesPorFichaIdentificacion?nemonicoCategoria=' +
            nemonicoCategoria;
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerEvaluacionesPorNemonicoEncuesta(
        paginacionRequest: PaginacionRequest,
        nemonicoEncuesta: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>> {
        let endPoint =
            this.path +
            '/obtenerEvaluacionesPorNemonicoEncuesta?nemonicoEncuesta=' +
            nemonicoEncuesta;
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerEvaluacionesPorNemonicoCategoria(
        paginacionRequest: PaginacionRequest,
        nemonicoCentro: string,
        nemonicosCategoria: string[],
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<EncabezadoDTO>>> {
        const listaSerializada = nemonicosCategoria.join(',');
        const endPoint =
            `${this.path}/obtenerEvaluacionesPorNemonicoCategoria` +
            `?nemonicoCentro=${nemonicoCentro}` +
            `&nemonicosCategoria=${encodeURIComponent(listaSerializada)}`;
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerEncuestaPorTokenEncuesta(
        encabezadoDTO: EncabezadoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EncuestaDTO>> {
        let endPoint = this.path + '/obtenerEncuestaPorTokenEncuesta';
        return this.backendService.postFinal(
            endPoint,
            encabezadoDTO,
            nemonicoMenu
        );
    }

    obtenerEvaluacionPorTokenEncabezado(
        encabezadoDTO: EncabezadoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EncuestaDTO>> {
        let endPoint = this.path + '/obtenerEvaluacionPorTokenEncabezado';
        return this.backendService.postFinal(
            endPoint,
            encabezadoDTO,
            nemonicoMenu
        );
    }

    crearEvaluacion(
        encabezadoDTO: EncabezadoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/crearEvaluacion';
        return this.backendService.postFinal(
            endPoint,
            encabezadoDTO,
            nemonicoMenu
        );
    }

    crearEncuesta(
        encuestaDTO: EncuestaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EncuestaDTO>> {
        let endPoint = this.path + '/crearEncuesta';
        return this.backendService.postFinal(
            endPoint,
            encuestaDTO,
            nemonicoMenu
        );
    }

    actualizarEncuesta(
        encuestaDTO: EncuestaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EncuestaDTO>> {
        let endPoint = this.path + '/actualizarEncuesta';
        return this.backendService.postFinal(
            endPoint,
            encuestaDTO,
            nemonicoMenu
        );
    }

    eliminarEncuesta(
        encuestaDTO: EncuestaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/removerEncuesta';
        return this.backendService.postFinal(
            endPoint,
            encuestaDTO,
            nemonicoMenu
        );
    }

    eliminarEvaluacion(
        encabezadoDTO: EncabezadoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/removerEvaluacion';
        return this.backendService.postFinal(
            endPoint,
            encabezadoDTO,
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
        encabezadoDTO: EncabezadoDTO,
        files: File[],
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/subirDocumento';

        let formData = new FormData();
        if (files != null) {
            for (let file of files) {
                formData.append('documentos', file);
            }
        }

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                this.backendService
                    .crearBodyEncriptado(encabezadoDTO)
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
        request: EvaluacionDocumentoRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }
}
