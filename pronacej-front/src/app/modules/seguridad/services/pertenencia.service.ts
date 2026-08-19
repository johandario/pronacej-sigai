import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { PertenenciaDTO } from 'app/core/model/both/pertenenciaDTO.model';
import { PertenenciaDocumentoDTO } from 'app/core/model/request/ia/PertenenciaDocumentoDTO.model';
import { PertenenciaDocumentosRequest } from 'app/core/model/request/ia/PertenenciaDocumentosRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class PertenenciaService {
    private path = '/pertenencias';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerPertenencias(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<PertenenciaDTO>>> {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerPertenenciaPorId(
        idPertenencia: number,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PertenenciaDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { param: idPertenencia },
            nemonicoMenu
        );
    }

    crearPertenencia(
        pertenencia: PertenenciaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PertenenciaDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            pertenencia,
            nemonicoMenu
        );
    }

    eliminarPertenencia(
        pertenencia: PertenenciaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            pertenencia,
            nemonicoMenu
        );
    }

    subirDocumento(
        file: File,
        pertenenciaDocumentoDTO: PertenenciaDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(pertenenciaDocumentoDTO)
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
        pertenenciaDocumentosRequest: PertenenciaDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(
            endPoint,
            pertenenciaDocumentosRequest,
            nemonicoMenu
        );
    }

    eliminarDocumento(
        pertenenciaDocumentoDTO: PertenenciaDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PertenenciaDocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumento';
        return this.backendService.postFinal(
            endPoint,
            pertenenciaDocumentoDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
