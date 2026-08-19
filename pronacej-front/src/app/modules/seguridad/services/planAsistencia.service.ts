import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import {
    DetalleFichaAsistenciaPostEgresoDTO,
    FichaAsistenciaPostEgresoDTO,
} from 'app/core/model/both/FichaAsistenciaPostEgreso.model';
import { PlanAsistenciaPostEgresoDTO } from 'app/core/model/both/planAsistenciaPostEgresoDTO';
import { FichaAsistenciaPostEgresoDocumentoDTO } from 'app/core/model/request/ia/FichaAsistenciaPostEgresoDocumentoDTO.model';
import { FichaAsistenciaPostEgresoDocumentosRequest } from 'app/core/model/request/ia/FichaAsistenciaPostEgresoDocumentosRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class PlanAsistenciaService {
    private path = '/planes-asistencia-post-egreso';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerPlanesAsistencia(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<PlanAsistenciaPostEgresoDTO>>
    > {
        let endPoint = this.path + '/lista';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerPlanAsistenciaPorToken(
        tokenIdentificadorPlanAsistencia: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PlanAsistenciaPostEgresoDTO>> {
        let endPoint = this.path + '/buscar-por-token';
        return this.backendService.getFinal(
            endPoint,
            { param: tokenIdentificadorPlanAsistencia },
            nemonicoMenu
        );
    }

    crearPlanAsistencia(
        planTratamiento: PlanAsistenciaPostEgresoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PlanAsistenciaPostEgresoDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            planTratamiento,
            nemonicoMenu
        );
    }

    eliminarPlanAsistencia(
        planTratamiento: PlanAsistenciaPostEgresoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            planTratamiento,
            nemonicoMenu
        );
    }

    crearFichaAsistenciaPostEgreso(
        fichaDTO: FichaAsistenciaPostEgresoDTO
    ): Observable<RespuestaPorDefecto<FichaAsistenciaPostEgresoDTO>> {
        let endPoint = `${this.path}/crearFichaAsistenciaPostEgreso`;
        return this.backendService.postFinal(endPoint, fichaDTO, '');
    }

    obtenerFichasAsistenciaPostEgreso(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<FichaAsistenciaPostEgresoDTO>>
    > {
        let endPoint = `${this.path}/obtenerFichasAsistenciaPostEgreso`;
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    eliminarFichaAsistenciaPostEgreso(
        fichaDTO: FichaAsistenciaPostEgresoDTO
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = `${this.path}/eliminarFichaAsistenciaPostEgreso`;
        return this.backendService.postFinal(endPoint, fichaDTO, '');
    }

    crearOEditarDetalleFichaAsistencia(
        detalleDTO: DetalleFichaAsistenciaPostEgresoDTO
    ): Observable<RespuestaPorDefecto<DetalleFichaAsistenciaPostEgresoDTO>> {
        let endPoint = `${this.path}/crearOEditarDetalleFichaAsistencia`;
        return this.backendService.postFinal(endPoint, detalleDTO, '');
    }

    obtenerDetallesPorFichaAsistencia(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO>
        >
    > {
        let endPoint = `${this.path}/obtenerDetallesPorFichaAsistencia`;
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    eliminarDetalleFichaAsistencia(
        detalleDTO: DetalleFichaAsistenciaPostEgresoDTO
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = `${this.path}/eliminarDetalleFichaAsistencia`;
        return this.backendService.postFinal(endPoint, detalleDTO, '');
    }

    subirDocumento(
        file: File,
        fichaAsistenciaPostEgresoDocumentoDTO: FichaAsistenciaPostEgresoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumentoFichaAsistencia';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(fichaAsistenciaPostEgresoDocumentoDTO)
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
        fichaAsistenciaPostEgresoDocumentosRequest: FichaAsistenciaPostEgresoDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentosFichaAsistencia';
        return this.backendService.postFinal(
            endPoint,
            fichaAsistenciaPostEgresoDocumentosRequest,
            nemonicoMenu
        );
    }

    eliminarDocumento(
        fichaAsistenciaPostEgresoDocumentoDTO: FichaAsistenciaPostEgresoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaAsistenciaPostEgresoDocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumentoFichaAsistencia';
        return this.backendService.postFinal(
            endPoint,
            fichaAsistenciaPostEgresoDocumentoDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
