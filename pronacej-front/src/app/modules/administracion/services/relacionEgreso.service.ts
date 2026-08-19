import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { ReforzamientoDTO } from 'app/core/model/both/salida/ReforzamientoDTO.model';
import { RelacionEgresoDTO } from 'app/core/model/both/salida/RelacionEgresoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class RelacionEgresoService {
    private path = '/relacionEgreso';

    constructor(private backendService: BackendService) {}

    obtenerAdolescentes(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<RelacionEgresoDTO>>> {
        let endPoint = this.path + '/obtenerAdolescentes';

        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerReforzamientos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<ReforzamientoDTO>>> {
        let endPoint = this.path + '/obtenerReforzamientos';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerReforzamientoPorToken(
        reforzamientoDTO: ReforzamientoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ReforzamientoDTO>> {
        let endPoint = this.path + '/obtenerReforzamientoPorToken';
        return this.backendService.postFinal(
            endPoint,
            reforzamientoDTO,
            nemonicoMenu
        );
    }

    crearReforzamiento(
        files: File[],
        constancias: File[],
        reforzamientoDTO: ReforzamientoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/crearReforzamiento';

        let formData = new FormData();
        if (files != null) {
            for (let file of files) {
                formData.append('documentos', file);
            }
        }
        if (constancias != null) {
            for (let constancia of constancias) {
                formData.append('constancias', constancia);
            }
        }

        return new Observable(
            (susbscriber: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                this.backendService
                    .crearBodyEncriptado(reforzamientoDTO)
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
                                            RespuestaPorDefecto<Boolean>
                                        >(bodyEncriptado);
                                    susbscriber.next(resp);
                                    susbscriber.complete();
                                },
                                error: (error: any) => {
                                    susbscriber.error(error);
                                    susbscriber.complete();
                                },
                            });
                    })
                    .catch((error: any) => {
                        susbscriber.error(error);
                        susbscriber.complete();
                    });
            }
        );
    }

    actualizarReforzamiento(
        constancias: File[],
        reforzamientoDTO: ReforzamientoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/actualizarReforzamiento';

        let formData = new FormData();
        if (constancias != null) {
            for (let constancia of constancias) {
                formData.append('constancias', constancia);
            }
        }

        return new Observable(
            (susbscriber: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                this.backendService
                    .crearBodyEncriptado(reforzamientoDTO)
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
                                            RespuestaPorDefecto<Boolean>
                                        >(bodyEncriptado);
                                    susbscriber.next(resp);
                                    susbscriber.complete();
                                },
                                error: (error: any) => {
                                    susbscriber.error(error);
                                    susbscriber.complete();
                                },
                            });
                    })
                    .catch((error: any) => {
                        susbscriber.error(error);
                        susbscriber.complete();
                    });
            }
        );
    }

    removerReforzamiento(
        reforzamientoDTO: ReforzamientoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/removerReforzamiento';
        return this.backendService.postFinal(
            endPoint,
            reforzamientoDTO,
            nemonicoMenu
        );
    }

    obtenerDocumentos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }
}
