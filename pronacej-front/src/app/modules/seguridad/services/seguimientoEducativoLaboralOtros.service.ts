import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { SeguimientoEducativoDTO } from 'app/core/model/both/ia/seguimientoEducativoDTO.model';
import { SeguimientoEducativoLaboralOtrosDTO } from 'app/core/model/both/ia/seguimientoEducativoLaboralOtrosDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class SeguimientoEducativoLaboralOtrosService {
    private ruta = '/seguimiento-educativo-laboral-otros';

    constructor(private servicioBackend: BackendService) {}

    /**
     * Obtiene los seguimientos educativos/laborales/otros disponibles para el sistema de manera paginada
     * @param solicitudPaginacion PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>>>
     */
    obtenerSeguimientosPaginado(
        solicitudPaginacion: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>
        >
    > {
        const endpoint = this.ruta + '/obtenerSeguimientosPaginado';
        return this.servicioBackend.postFinal(
            endpoint,
            solicitudPaginacion,
            nemonicoMenu
        );
    }

    /**
     * Crea un seguimiento educativo/laboral/otros en el sistema con los datos enviados en el request
     * @param seguimientoDTO SeguimientoEducativoLaboralOtrosDTO datos del seguimiento a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     * @returns Observable<RespuestaPorDefecto<SeguimientoEducativoLaboralOtrosDTO>>
     */
    crearSeguimiento(
        seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<SeguimientoEducativoLaboralOtrosDTO>> {
        const endpoint = this.ruta + '/crearSeguimiento';
        return this.servicioBackend.postFinal(
            endpoint,
            seguimientoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un seguimiento educativo/laboral/otros en el sistema
     * @param seguimientoDTO SeguimientoEducativoLaboralOtrosDTO datos del seguimiento a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarSeguimiento(
        seguimientoDTO: SeguimientoEducativoLaboralOtrosDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        const endpoint = this.ruta + '/eliminarSeguimiento';
        return this.servicioBackend.postFinal(
            endpoint,
            seguimientoDTO,
            nemonicoMenu
        );
    }

    /**
     * Sube documentos asociados a un seguimiento educativo laboral otros
     * Utiliza el DTO genérico SeguimientoEducativoDTO para compatibilidad con el backend
     * @param seguimientoDTO SeguimientoEducativoDTO con token del seguimiento y lista de documentos
     * @param files File[] archivos a subir
     * @param nemonicoMenu string nemonico del menú
     * @returns Observable<RespuestaPorDefecto<Boolean>>
     */
    subirDocumentos(
        seguimientoDTO: SeguimientoEducativoDTO,
        files: File[],
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.ruta + '/subirDocumentos';

        let formData = new FormData();
        if (files != null) {
            for (let file of files) {
                formData.append('documentos', file);
            }
        }

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                this.servicioBackend
                    .crearBodyEncriptado(seguimientoDTO)
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));

                        this.servicioBackend
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                nemonicoMenu
                            )
                            .subscribe({
                                next: async (body: BodyEncriptado) => {
                                    let resp =
                                        await this.servicioBackend.desencriptarBdyEncriptado<
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

    /**
     * Obtiene los documentos asociados a un seguimiento educativo laboral otros
     * @param request PaginacionRequest con el token del seguimiento
     * @param nemonicoMenu string nemonico del menú
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>>
     */
    obtenerDocumentos(
        request: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.ruta + '/obtenerDocumentos';
        return this.servicioBackend.postFinal(endPoint, request, nemonicoMenu);
    }

    /**
     * Verifica y maneja los errores generales del servicio
     * @param error any error a verificar
     * @param mostrarError boolean indica si se debe mostrar el error
     * @returns any
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.servicioBackend.checkError(error, mostrarError);
    }
}
