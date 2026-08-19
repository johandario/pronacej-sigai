import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DelitoEstadisticaDTO } from 'app/core/model/both/DelitoEstadisticoDTO.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import {
    ExpedienteMatrizDetalleDTO,
    ExpedienteMatrizDTO,
} from 'app/core/model/both/expedienteMatrizDTO.model';
import { ReportesDTO } from 'app/core/model/both/ReportesDTO.model';
import { ExpedienteMatrizDetalleDocumentoDTO } from 'app/core/model/request/ia/ExpedienteMatrizDetalleDocumentoDTO.model';
import { ExpedienteMatrizDetalleDocumentosRequest } from 'app/core/model/request/ia/ExpedienteMatrizDetalleDocumentosRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class ExpedienteMatrizService {
    private path = '/expediente-matriz';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerExpedientesValidos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ExpedienteMatrizDTO>>
    > {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerExpedientesPorFicha(
        valor: string,
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/buscar-por-ficha';
        return new Observable(
            (
                subs: Subscriber<
                    RespuestaPorDefecto<PaginacionResponse<ExpedienteMatrizDTO>>
                >
            ) => {
                this.backendService
                    .postJsonGeneralBodyEncriptadoParam(
                        endPoint,
                        paginacionRequest,
                        { param: valor },
                        nemonicoMenu
                    )
                    .subscribe({
                        next: async (bodyEncriptado: BodyEncriptado) => {
                            subs.next(
                                await this.backendService.desencriptarBdyEncriptado(
                                    bodyEncriptado
                                )
                            );
                            subs.complete();
                        },
                        error: (error: any) => {
                            subs.error(error);
                            subs.complete();
                        },
                    });
            }
        );
    }

    obtenerExpedientePorNum(
        numExpediente: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ExpedienteMatrizDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { param: numExpediente },
            nemonicoMenu
        );
    }

    crearExpediente(
        expedienteMatriz: ExpedienteMatrizDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ExpedienteMatrizDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            expedienteMatriz,
            nemonicoMenu
        );
    }

    eliminarExpediente(
        expedienteMatriz: ExpedienteMatrizDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            expedienteMatriz,
            nemonicoMenu
        );
    }

    subirDocumento(
        file: File,
        expedienteDetalleDocumentoDTO: ExpedienteMatrizDetalleDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(expedienteDetalleDocumentoDTO)
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

    obtenerDocumentos(
        expedienteMatrizDetalleDocumentosRequest: ExpedienteMatrizDetalleDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(
            endPoint,
            expedienteMatrizDetalleDocumentosRequest,
            nemonicoMenu
        );
    }

    eliminarDocumento(
        expedienteMatrizDetalleDocumentoDTO: ExpedienteMatrizDetalleDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ExpedienteMatrizDetalleDocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumento';
        return this.backendService.postFinal(
            endPoint,
            expedienteMatrizDetalleDocumentoDTO,
            nemonicoMenu
        );
    }

    obtenerEstadisticasDelitos(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DelitoEstadisticaDTO[]>> {
        let endPoint = this.path + '/obtenerEstadisticasDelitos';
        return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerUltimoExpedienteDetalle(
        nemonicoMenu: string,
        tokenFichaIdentificacion: string
    ): Observable<RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>> {
        let endPoint = this.path + '/obtenerUltimoExpedienteDetalle';
        return this.backendService.postFinal(
            endPoint,
            tokenFichaIdentificacion,
            nemonicoMenu
        );
    }

    obtenerExpedienteCabeceraYDetalleActualPorFicha(
        nemonicoMenu: string,
        tokenFichaIdentificacion: string
    ): Observable<RespuestaPorDefecto<ExpedienteMatrizDetalleDTO>> {
        let endPoint = this.path + '/obtenerExpedienteCabeceraYDetalleActualPorFicha';
        return this.backendService.postFinal(
            endPoint,
            tokenFichaIdentificacion,
            nemonicoMenu
        );
    }

    obtenerEstadisticasDelitosFiltros(
        nemonicoMenu: string,
        reportesDTO: ReportesDTO
    ): Observable<RespuestaPorDefecto<DelitoEstadisticaDTO[]>> {
        let endPoint = this.path + '/obtenerEstadisticasDelitos';
        return this.backendService.postFinal(
            endPoint,
            reportesDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
