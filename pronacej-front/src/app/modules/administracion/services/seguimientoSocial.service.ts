import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { SeguimientoEducativoDTO } from 'app/core/model/both/ia/seguimientoEducativoDTO.model';
import { SeguimientoSocialDTO } from 'app/core/model/both/ia/SeguimientoSocialDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class SeguimientoSocialService {
    private path = '/seguimiento-social';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene los seguimientos sociales disponibles para el sistema de manera paginada
     *
     * @param paginacionRequest PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<SeguimientoSocialDTO>>>
     */
    obtenerSeguimientosSocialesPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<SeguimientoSocialDTO>>
    > {
        let endPoint = this.path + '/obtenerSeguimientosSocialesPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea un seguimiento social en el sistema con los datos enviados en el request
     *
     * @param seguimientoSocialDTO SeguimientoSocialDTO datos del seguimiento social a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<SeguimientoSocialDTO>>
     */
    crearSeguimientoSocial(
        seguimientoSocialDTO: SeguimientoSocialDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<SeguimientoSocialDTO>> {
        let endPoint = this.path + '/crearSeguimientoSocial';
        return this.backendService.postFinal(
            endPoint,
            seguimientoSocialDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un seguimiento social en el sistema
     *
     * @param seguimientoSocialDTO SeguimientoSocialDTO datos del seguimiento social a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarSeguimientoSocial(
        seguimientoSocialDTO: SeguimientoSocialDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarSeguimientoSocial';
        return this.backendService.postFinal(
            endPoint,
            seguimientoSocialDTO,
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
