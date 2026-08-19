import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIngresoDTO } from 'app/core/model/both/FichaIngresoDTO.model';
import { FichaIngresoDocumentoDTO } from 'app/core/model/request/ia/FichaIngresoDocumentoDTO.model';
import { FichaIngresoDocumentosRequest } from 'app/core/model/request/ia/FichaIngresoDocumentosRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subject, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class FichaIngresoService {
    private path = '/ficha-ingreso';

    constructor(private backendService: BackendService) {}

    /**
     * Obten las fichas de ingreso disponibles para el sistema
     *
     * @param nemonicoMenu string nemonico de una ficha de ingreso del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerFichasIngresoPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<FichaIngresoDTO>>> {
        let endPoint = this.path + '/obtenerFichasIngresoPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una ficha de ingreso en el sistema con los datos enviados en el request
     *
     * @param FichaIngresoDTO datos de la ficha de ingreso a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIngresoDTO>>
     */
    crearFichaIngreso(
        fichaIngresoDTO: FichaIngresoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<FichaIngresoDTO>> {
        let endPoint = this.path + '/crearFichaIngreso';
        return this.backendService.postFinal(
            endPoint,
            fichaIngresoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una ficha de ingreso en el sistema con los datos enviados en el request
     *
     * @param FichaIngresoDTO datos de la ficha de ingreso a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIngresoDTO>>
     */
    eliminarFichaIngreso(
        fichaIngresoDTO: FichaIngresoDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarFichaIngreso';
        return this.backendService.postFinal(
            endPoint,
            fichaIngresoDTO,
            nemonicoMenu
        );
    }

    /**
     * Obten las fichas de ingreso disponibles para el sistema
     *
     * @param nemonicoMenu string nemonico de una ficha de ingreso del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerUltimaFichaValidaPorTokenFichaIdentificacion(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<FichaIngresoDTO>> {
        let endPoint = this.path + '/obtenerIngresoPorFichaPrincipal';
        console.log(paginacionRequest);
        
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    subirDocumentoFichaIngreso(
        file: File,
        fichaIngresoDocumentoDTO: FichaIngresoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumentoFichaIngreso';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(fichaIngresoDocumentoDTO)
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

    obtenerDocumentosFichaIngreso(
        fichaIngresoDocumentosRequest: FichaIngresoDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentosFichaIngreso';
        return this.backendService.postFinal(
            endPoint,
            fichaIngresoDocumentosRequest,
            nemonicoMenu
        );
    }

    eliminarDocumentoFichaIngreso(
        fichaIngresoDocumentoDTO: FichaIngresoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaIngresoDocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumentoFichaIngreso';

        return this.backendService.postFinal(
            endPoint,
            fichaIngresoDocumentoDTO,
            nemonicoMenu
        );
    }

    obtenerTodosDocumentosFichaIngreso(
        fichaIngresoDocumentosRequest: FichaIngresoDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerTodosDocumentosFichaIngreso';
        return this.backendService.postFinal(
            endPoint,
            fichaIngresoDocumentosRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }

      public actualizarFichaIngreso$ = new Subject<void>();
}
