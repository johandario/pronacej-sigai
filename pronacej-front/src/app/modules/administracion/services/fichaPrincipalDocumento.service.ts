import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTOFichaPrincipal } from 'app/core/model/both/documento/documentoDTOFichaPrincipal.model';
import { FichaIdentificacionDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionDocumentoDTO.model';
import { FichaDeIdentificacionDocumentoDTO } from 'app/core/model/request/ia/FichaDeIdentificacionDocumentoDTO.model';
import { FichaPrincipalDocumentoDTO } from 'app/core/model/request/ia/FichaPrincipalDocumentoDTO.model';
import { FichaPrincipalDocumentosRequest } from 'app/core/model/request/ia/FichaPrincipalDocumentosRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class FichaPrincipalDocumentoService {
    private path = '/ficha-principal-documento';

    constructor(private backendService: BackendService) {}

    /**
     * Sube un documento al sistema y lo asocia a la ficha de identificacion principal
     *
     * @param file File archivo a subir
     * @param fichaPrincipalDocumentoDTO FichaPrincipalDocumentoDTO objeto ficha principal
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<DocumentoDTOFichaPrincipal>>
     */
    subirDocumento(
        file: File,
        fichaPrincipalDocumentoDTO: FichaPrincipalDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTOFichaPrincipal>> {
        let endPoint = this.path + '/subirDocumento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (
                subs: Subscriber<
                    RespuestaPorDefecto<DocumentoDTOFichaPrincipal>
                >
            ) => {
                this.backendService
                    .crearBodyEncriptado(fichaPrincipalDocumentoDTO)
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
                                            RespuestaPorDefecto<DocumentoDTOFichaPrincipal>
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
     * Obten todos los documentos de la ficha de indetificacion enviada por paginación
     *
     * @param fichaPrincipalDocumentosRequest FichaPrincipalDocumentosRequest objeto request para la peticion
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<Object>
     */
    obtenerDocumentos(
        fichaPrincipalDocumentosRequest: FichaPrincipalDocumentosRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<FichaDeIdentificacionDocumentoDTO>
        >
    > {
        let endPoint = this.path + '/obtenerDocumentos';

        return this.backendService.postFinal(
            endPoint,
            fichaPrincipalDocumentosRequest,
            nemonicoMenu
        );
    }

    /**
     * Elimina un documento asociado a una ficha de identificacion
     *
     * @param fichaIdentificacionDocumentoDTO FichaIdentificacionDocumentoDTO ficha de identificacion documento
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<FichaIdentificacionDocumentoDTO>>
     */
    eliminarDocumento(
        fichaIdentificacionDocumentoDTO: FichaIdentificacionDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaIdentificacionDocumentoDTO>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionDocumentoDTO,
            nemonicoMenu
        );
    }

    /**
     * Edita un documento al sistema y lo asocia a la ficha de identificacion principal
     *
     * @param file File archivo a subir
     * @param fichaDeIdentificacionDocumentoDTO FichaDeIdentificacionDocumentoDTO
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<DocumentoDTOFichaPrincipal>>
     */
    editarDocumento(
        file: File,
        fichaDeIdentificacionDocumentoDTO: FichaDeIdentificacionDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaDeIdentificacionDocumentoDTO>> {
        let endPoint = this.path + '/editarDocumento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (
                subs: Subscriber<
                    RespuestaPorDefecto<FichaDeIdentificacionDocumentoDTO>
                >
            ) => {
                this.backendService
                    .crearBodyEncriptado(fichaDeIdentificacionDocumentoDTO)
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
                                            RespuestaPorDefecto<FichaDeIdentificacionDocumentoDTO>
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

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
