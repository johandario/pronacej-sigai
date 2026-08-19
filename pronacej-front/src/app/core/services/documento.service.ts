import { Injectable } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';
import { DocumentoDTO } from '../model/both/DocumentoDTO.model';
import { BodyEncriptado } from '../model/both/bodyEncriptado.model';
import { FormDataRequest } from '../model/internos/FormDataRequest.model';
import { RespuestaPorDefecto } from '../model/response/RespuestaPorDefecto.model';
import { BackendService } from './backend.service';

@Injectable({
    providedIn: 'root',
})
export class DocumentoService {
    private path = '/documento';

    constructor(private backendService: BackendService) {}

    subirDocumento(
        file: File,
        documentoDTO: DocumentoDTO,
        menuNemonico: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let formDataRequest = new FormDataRequest<DocumentoDTO>();
        formDataRequest.data = {
            clave: 'documento',
            valor: file,
        };

        formDataRequest.body = {
            clave: 'body',
            valor: documentoDTO,
        };

        let endPoint = this.path + '/subir-documento';
        let observable = new Observable<RespuestaPorDefecto<DocumentoDTO>>(
            (sub: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .postFormDataBodyEncriptado(
                        endPoint,
                        formDataRequest,
                        menuNemonico
                    )
                    .subscribe({
                        next: async (bodyEncriptado: BodyEncriptado) => {
                            let resp =
                                await this.backendService.desencriptarBdyEncriptado<
                                    RespuestaPorDefecto<DocumentoDTO>
                                >(bodyEncriptado);
                            sub.next(resp);
                            sub.complete();
                        },
                        error: (error: any) => {
                            sub.error(error);
                            sub.complete();
                        },
                    });
            }
        );
        return observable;
    }

    /**
     * Actualiza un documento
     *
     * @para file File
     * @param documentoDTO DocumentoDTO
     * @param menuNemonico String nemonico del menu
     *
     * @return Observable<RespuestaPorDefecto<DocumentoDTO>>
     */
    actualizarDocumento(
        file: File,
        documentoDTO: DocumentoDTO,
        menuNemonico: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let formData = new FormData();
        formData.append('documento', file);

        let endPoint = this.path + '/actualizar-documento';
        let observable = new Observable<RespuestaPorDefecto<DocumentoDTO>>(
            (sub: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(documentoDTO)
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));
                        this.backendService
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                menuNemonico
                            )
                            .subscribe({
                                next: async (
                                    bodyEncriptado: BodyEncriptado
                                ) => {
                                    let resp =
                                        await this.backendService.desencriptarBdyEncriptado<
                                            RespuestaPorDefecto<DocumentoDTO>
                                        >(bodyEncriptado);
                                    sub.next(resp);
                                    sub.complete();
                                },
                                error: (error: any) => {
                                    sub.error(error);
                                    sub.complete();
                                },
                            });
                    })
                    .catch((error: any) => {
                        sub.error(error);
                        sub.complete();
                    });
            }
        );
        return observable;
    }

    /**
     * Obten un documento fisico directament del sistema
     *
     * @para tokenIdentificadorDocumento string token identificador unico del documento
     * @param nemonicoMenu string nemonico del menu
     *
     * @return RespuestaPorDefecto<CatalogoDTO>
     */
    obtenerDocumento(
        tokenIdentificadorDocumento: string,
        nemonicoMenu: string
    ): Observable<ArrayBuffer> {
        let endPoint = this.path + '/obtenerDocumento';

        return new Observable((subs: Subscriber<ArrayBuffer>) => {
            this.backendService
                .getBlobGeneralBodyEncriptado(
                    endPoint,
                    {
                        tokenIdentificadorDocumento:
                            tokenIdentificadorDocumento,
                    },
                    nemonicoMenu
                )
                .subscribe({
                    next: (arrayBuffer: ArrayBuffer) => {
                        subs.next(arrayBuffer);
                        subs.complete();
                    },
                    error: (error: any) => {
                        let arrayBuffer = error?.error as ArrayBuffer;
                        if (arrayBuffer && arrayBuffer.byteLength > 0) {
                            var enc = new TextDecoder('utf-8');
                            var arr = new Uint8Array(arrayBuffer);
                            let stringBodyEncriptado = enc.decode(arr);
                            let bodyEncriptado = JSON.parse(
                                stringBodyEncriptado
                            ) as BodyEncriptado;
                            let resp =
                                this.backendService.desencriptarBdyEncriptado<
                                    RespuestaPorDefecto<any>
                                >(bodyEncriptado);
                            subs.error(resp);
                        } else {
                            subs.error(error);
                        }
                        subs.complete();
                    },
                });
        });
    }

    /**
     * elimina un documento del sistema
     *
     * @para tokenIdentificador string token identificador unico del documento
     * @param nemonicoMenu string nemonico del menu
     *
     * @return RespuestaPorDefecto<CatalogoDTO>
     */
    eliminarDocumento(
        tokenIdentificador: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumento';
        let documentoDTO = new DocumentoDTO();
        documentoDTO.tokenIdentificador = tokenIdentificador;
        return this.backendService.postFinal(
            endPoint,
            documentoDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrar = true): Promise<string> {
        return await this.backendService.checkError(error, mostrar);
    }
}
