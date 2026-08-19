import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DatosFamiliaresDTO } from 'app/core/model/both/datosFamiliaresDTO.model';
import { DireccionPersonaRelacionadaDTO } from 'app/core/model/both/DireccionPersonaRelacionada.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { DatosFamiliaresDocumentoDTO } from 'app/core/model/request/ia/DatosFamiliaresDocumentoDTO.model';
import { DatosFamiliaresDocumentosRequest } from 'app/core/model/request/ia/DatosFamiliaresDocumentosRequest.model';
import { PaginacionPersonasRelacionadasRequest } from 'app/core/model/request/paginacionPersonaRelacionadaRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class DatosFamiliaresService {
    private path = '/personaRelacionada';
    private pathDatos = '/datosFamiliares';

    constructor(private backendService: BackendService) {}

    /**
     *
     * Obtiene las personas relacionadas a un usuario por medio de su tokenIdentificador
     *
     * @param paginacionPersonasRelacionadasRequest objeto que contiene los parametros necesarios para la consulta
     *
     * @returns Observable<Navigation>
     */

    obtenerPersonasRelacionadas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>
    > {
        let endPoint = 
            this.path + '/obtenerPersonasRelacionadasPaginado';
        return this.backendService.postFinal(
            endPoint, 
            paginacionRequest, 
            nemonicoMenu
        );
    }

    /**
     *
     * Obtiene las personas relacionadas a un usuario por medio de su tokenIdentificador
     *
     * @param paginacionPersonasRelacionadasRequest objeto que contiene los parametros necesarios para la consulta
     *
     * @returns Observable<Navigation>
     */

    obtenerPersonasRelacionadasPorEvaluacionSocial(
        paginacionRequest: PaginacionPersonasRelacionadasRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<PersonaRelacionadaDTO>>
    > {
        let endPoint =
            this.path + '/obtenerPersonasRelacionadasPorEvaluacionSocial';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una ficha de identificacion en el sistema con los datos enviados en el request
     *
     * @param personaRelacionadaDTO PersonaRelacionadaDTO datos de la persona relacionada a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>>
     */
    crearPersonaRelacionada(
        personaRelacionadaDTO: PersonaRelacionadaDTO,
        nemonicoMenu: string,
        enfermo: boolean = false
    ): Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>> {
        let endPoint =
            this.path +
            (enfermo ? '/editarPersonaEnfermo' : '/crearPersonaRelacionada');
        return this.backendService.postFinal(
            endPoint,
            personaRelacionadaDTO,
            nemonicoMenu
        );
    }

    /**
     * Encuentra una ficha de identtificacion en el sistema dado su token identificador
     *
     * @param tokenIdentificador tokenIdentificador de la persona relacionada
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>>
     */
    obtenerPersonaRelacionada(
        tokenIdentificador: String,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>> {
        let endPoint = this.path + '/obtenerPersonaRelacionada';
        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }

    /**
     * Elimina una ficha de identtificacion en el sistema con los datos enviados en el request
     *
     * @param PersonaRelacionadaDTO PersonaRelacionadaDTO datos de la persona relacionada a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacionDTO>>
     */
    eliminarFichaIdentificacion(
        personaRelacionadaDTO: PersonaRelacionadaDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarPersonaRelacionada';
        return this.backendService.postFinal(
            endPoint,
            personaRelacionadaDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una persona relacionada a una situación educativa/laboral en el sistema con los datos enviados en el request
     *
     * @param PersonaRelacionadaDTO datos de la persona relacionada a una situación educativa/laboral a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>>
     */
    eliminarPersonaRelacionadaPorSituacionEconomicaSocial(
        personaRelacionadaDTO: PersonaRelacionadaDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint =
            this.path +
            '/eliminarPersonaRelacionadaPorSituacionEconomicaSocial';
        return this.backendService.postFinal(
            endPoint,
            personaRelacionadaDTO,
            nemonicoMenu
        );
    }

    /**
     * Crea una ficha de identificacion en el sistema con los datos enviados en el request
     *
     * @param direccionPersonaRelacionada DireccionPersonaRelacionada datos de la direccion a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>>
     */
    crearDireccionPersonaRelacionada(
        direccionPersonaRelacionada: DireccionPersonaRelacionadaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>> {
        let endPoint = this.path + '/crearDireccionPersonaRelacionada';
        return this.backendService.postFinal(
            endPoint,
            direccionPersonaRelacionada,
            nemonicoMenu
        );
    }

    /**
     *
     * Obtiene las personas relacionadas a un usuario por medio de su tokenIdentificador
     *
     * @param idPersonaRelacionada id de la persona relacionada de la cual se obtendran las direcciones
     *
     * @returns Observable<Navigation>
     */

    obtenerDireccionesRelacionadas(
        idPersonaRelacionada: number,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<DireccionPersonaRelacionadaDTO>>
    > {
        let endPoint = this.path + '/obtenerDireccionesRelacionadas';
        return this.backendService.postFinal(
            endPoint,
            idPersonaRelacionada,
            nemonicoMenu
        );
    }

    /**
     * Elimina una ficha de identtificacion en el sistema con los datos enviados en el request
     *
     * @param DireccionPersonaRelacionadaDTO DireccionPersonaRelacionadaDTO datos de la direccion a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacionDTO>>
     */
    eliminarDireccionRelacionada(
        direccionDTO: DireccionPersonaRelacionadaDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarDireccionRelacionada';
        return this.backendService.postFinal(
            endPoint,
            direccionDTO,
            nemonicoMenu
        );
    }

    /**
     * Crea un objeto DatosPersonales en el sistema con los datos enviados en el request
     *
     * @param datosPersonalesDTO DatosFamiliaresDTO datos de la persona relacionada a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PersonaRelacionadaDTO>>
     */
    crearDatosPersonales(
        datosPersonalesDTO: DatosFamiliaresDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DatosFamiliaresDTO>> {
        let endPoint = this.pathDatos + '/crearDatosFamiliares';
        return this.backendService.postFinal(
            endPoint,
            datosPersonalesDTO,
            nemonicoMenu
        );
    }

    /**
     * Encuentra datosPersonales de la fichaIndetificacion que se detalla por medio de el tokenIdentificador
     *
     * @param tokenIdentificador tokenIdentificador de la ficha
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<DatosFamiliaresDTO>>
     */
    obtenerDatosPersonales(
        tokenIdentificador: String,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<DatosFamiliaresDTO>> {
        let endPoint = this.pathDatos + '/obtenerDatosFamiliares';
        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }

    /**
     * Obtiene la lista de personas relacionadas del adolescente especificado
     *
     * @param FichaIdentificacionDTO FichaIdentificacionDTO
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<List<PersonaRelacionadaDTO>>
     */
    obtenerPersonasRelacionadasPorIdFicha(
        fichaIdentificacionDTO: FichaIdentificacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PersonaRelacionadaDTO[]>> {
        let endPoint = this.path + '/obtenerPersonasRelacionadasPorIdFicha';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionDTO,
            nemonicoMenu
        );
    }

    /**
     * Obtiene la lista de personas relacionadas del adolescente especificado
     *
     * @param FichaIdentificacionDTO FichaIdentificacionDTO
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<List<PersonaRelacionadaDTO>>
     */
    obtenerPersonasRelacionadasPorTokenFicha(
        fichaIdentificacionDTO: FichaIdentificacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PersonaRelacionadaDTO[]>> {
        let endPoint = this.path + '/obtenerPersonasRelacionadasPorTokenFicha';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionDTO,
            nemonicoMenu
        );
    }

    /**
     * Sube un documento y lo asocia al registro de datos familiares
     *
     * @param archivo File archivo a subir
     * @param datosFamiliaresDocumentoDTO DatosFamiliaresDocumentoDTO datos del documento a subir
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<DocumentoDTO>>
     */
    subirDocumento(
        archivo: File,
        datosFamiliaresDocumentoDTO: DatosFamiliaresDocumentoDTO,
        nemonicoMenu: string
    ): Observable<any> {
        let endPoint = this.pathDatos + '/subirDocumento';
        let formData = new FormData();
        formData.append('documento', archivo);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(datosFamiliaresDocumentoDTO)
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

    /**
     * Elimina la relación entre un documento y los datos familiares
     *
     * @param datosFamiliaresDocumentoDTO DatosFamiliaresDocumentoDTO datos del documento a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<DatosFamiliaresDocumentoDTO>>
     */
    eliminarDocumento(
        datosFamiliaresDocumentoDTO: DatosFamiliaresDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DatosFamiliaresDocumentoDTO>> {
        let endPoint = this.pathDatos + '/eliminarDocumento';
        return this.backendService.postFinal(
            endPoint,
            datosFamiliaresDocumentoDTO,
            nemonicoMenu
        );
    }

    /**
     * Obtiene la lista paginada de documentos asociados a los datos familiares
     *
     * @param request DatosFamiliaresDocumentosRequest parámetros de paginación y búsqueda
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>>
     */
    obtenerDocumentos(
        request: DatosFamiliaresDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.pathDatos + '/obtenerDocumentos';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }

    /**
     * Mueve documentos de un identificador de persona a otro
     *
     * @param tokens objeto con propiedades origen y destino
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    moverDocumentos(
        tokens: { origen: string; destino: string },
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.pathDatos + '/moverDocumentos';
        return this.backendService.postFinal(endPoint, tokens, nemonicoMenu);
    }

    /**
     * Sube un documento y lo asocia al registro de datos familiares
     *
     * @param archivo File archivo a subir
     * @param datosFamiliaresDocumentoDTO DatosFamiliaresDocumentoDTO datos del documento a subir
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<DocumentoDTO>>
     */
    subirDocumentoFichaPsicosocial(
        archivo: File,
        datosFamiliaresDocumentoDTO: DatosFamiliaresDocumentoDTO,
        nemonicoMenu: string
    ): Observable<any> {
        let endPoint = this.pathDatos + '/subirDocumentoFichaPsicosocial';
        let formData = new FormData();
        formData.append('documento', archivo);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(datosFamiliaresDocumentoDTO)
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

    /**
     * Obtiene la lista paginada de documentos asociados a los datos familiares
     *
     * @param request DatosFamiliaresDocumentosRequest parámetros de paginación y búsqueda
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>>
     */
    obtenerDocumentosFichaPsicosocial(
        request: DatosFamiliaresDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.pathDatos + '/obtenerDocumentosFichaPsicosocial';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }

    /**
     * Busca personas relacionadas por número de documento en el sistema.
     * 
     * @param numeroDocumento El número de documento a buscar
     * @param nemonicoMenu Nemónico del menú (opcional)
     * @returns Observable con la respuesta que contiene las personas relacionadas encontradas
     */
    buscarPersonaRelacionadaPorNumeroDocumento(
        numeroDocumento: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PersonaRelacionadaDTO[]>> {
        const endPoint = this.path + '/buscarPorNumeroDocumento';
        return this.backendService.postFinal(
            endPoint,
            numeroDocumento,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
