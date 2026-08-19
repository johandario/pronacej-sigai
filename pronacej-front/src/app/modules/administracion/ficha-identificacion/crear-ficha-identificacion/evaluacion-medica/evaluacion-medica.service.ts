import { Injectable } from '@angular/core';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { ConsultaAtencionIntegralDTO } from 'app/core/model/both/EJE/ConsultaAtencionIntegralDTO.model';
import { EvaluacionMedicaProgresoDTO } from 'app/core/model/both/EJE/EvaluacionMedicaProgresoDTO.model';
import { DiagnosticoDTO } from 'app/core/model/both/EJE/seguimiento-medico/DiagnosticoDTO.model';
import { EstadoNutricionalDTO } from 'app/core/model/both/EJE/seguimiento-medico/EstadoNutricionalDTO.model';
import { EvaluacionMedicaDTO } from 'app/core/model/both/EJE/seguimiento-medico/EvaluacionMedicaDTO.model';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { CriterioEvaluacionMedicaSeguimientoDTO } from 'app/core/model/both/criterioEvaluacionMedicaSeguimientoDTO.model';
import { FichaMedicaEnfermedadDTO } from 'app/core/model/both/fichaMedicaEnfermedadDTO.model';
import { AntecedenteFamiliarDTO } from 'app/core/model/both/ia/ficha-medica/AntecedenteFamiliarDTO.model';
import { FichaMedicaDTO } from 'app/core/model/both/ia/ficha-medica/FichaMedicaDTO.model';
import { IngresoCentroJuvenilDTO } from 'app/core/model/both/ia/ficha-medica/IngresoCentroJuvenilDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { EvaluacionMedicaProgresoDocumentoDTO } from 'app/core/model/request/ia/EvaluacionMedicaProgresoDocumentoDTO.model';
import { EvaluacionMedicaProgresoDocumentosRequest } from 'app/core/model/request/ia/EvaluacionMedicaProgresoDocumentosRequest.model';
import { FichaMedicaDocumentoDTO } from 'app/core/model/request/ia/FichaMedicaDocumentoDTO.model';
import { FichaMedicaDocumentoRequest } from 'app/core/model/request/ia/FichaMedicaDocumentoRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { BehaviorSubject, Observable, Subscriber } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EvaluacionMedicaService {
    private readonly path = '/evaluacionMedica';

    constructor(private readonly backendService: BackendService) {}

    private fichaMedicaSubject = new BehaviorSubject<string | null>(null);
    fichaMedica$: Observable<string | null> =
        this.fichaMedicaSubject.asObservable();

    setToken(ficha: string) {
        this.fichaMedicaSubject.next(ficha);
    }

    getToken(): string | null {
        return this.fichaMedicaSubject.value;
    }

    private evaluacionMedicaSubject = new BehaviorSubject<string | null>(null);
    evaluacionMedica$: Observable<string | null> =
        this.evaluacionMedicaSubject.asObservable();

    setTokenEvaluacionMedica(ficha: string) {
        this.evaluacionMedicaSubject.next(ficha);
    }

    getTokenEvaluacionMedica(): string | null {
        return this.evaluacionMedicaSubject.value;
    }

    private evaluacionProgresoSubject = new BehaviorSubject<string | null>(
        null
    ); // Inicialmente null
    evaluacionProgreso$ = this.evaluacionProgresoSubject.asObservable();

    setTokenEvaluacionMedicaProgreso(ficha: string) {
        this.evaluacionProgresoSubject.next(ficha);
    }

    getTokenEvaluacionMedicaProgreso(): string | null {
        return this.evaluacionProgresoSubject.value;
    }

    private vistaDoctorSubject = new BehaviorSubject<Boolean | null>(null); // Inicialmente null
    vistaDoctorSubject$ = this.vistaDoctorSubject.asObservable();

    setVistaDoctorProgreso(vista: Boolean) {
        this.vistaDoctorSubject.next(vista);
    }

    getVistaDoctorProgreso(): Boolean | null {
        return this.vistaDoctorSubject.value;
    }

    private consultaAtencionSubject = new BehaviorSubject<string | null>(null); // Inicialmente null
    consultaAtencionSubject$ = this.consultaAtencionSubject.asObservable();

    private consultaAtencionSoloLecturaSubject = new BehaviorSubject<boolean>(
        false
    );
    consultaAtencionSoloLectura$ =
        this.consultaAtencionSoloLecturaSubject.asObservable();

    setTokenConsultaAtencion(ficha: string) {
        this.consultaAtencionSubject.next(ficha);
    }

    getTokenConsultaAtencion(): string | null {
        return this.consultaAtencionSubject.value;
    }

    setConsultaAtencionSoloLectura(soloLectura: boolean): void {
        this.consultaAtencionSoloLecturaSubject.next(soloLectura);
    }

    getConsultaAtencionSoloLectura(): boolean {
        return this.consultaAtencionSoloLecturaSubject.value;
    }

    /**
     * Obtener ficha médica por token id ficha identificación
     * @params paginacionRequest objeto paginacion personalizado para enviar token id o nombre para las busquedas
     * @returns retorna un objeto FichaMedicaDTO
     */
    getFichaMedicaByFichaIden(
        fichaIdentificacion: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaMedicaDTO>> {
        let endPoint = this.path + '/obtenerFichaMedicaPorFichaIdentificacion';

        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacion,
            nemonicoMenu
        );
    }

    /**
     * Crear ficha medica
     * @params fichaMedica objeto DTO que contiene los datos para crear una ficha medica
     * @returns retorna un objeto FichaMedicaDTO
     */
    postFichaMedica(
        fichaMedica: FichaMedicaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaMedicaDTO>> {
        let endPoint = this.path + '/crearFichaMedica';
        return this.backendService.postFinal(
            endPoint,
            fichaMedica,
            nemonicoMenu
        );
    }

    // Antecedentes familiares

    /**
     * Obtener antecedentes familiares por el token id de la ficha medica
     * @params paginacionRequest objeto de paginacion personalizaco que contiene el id de la ficha medica
     * @returns retorna una lista de objetos AntecedenteFamiliarDTO
     */
    getAntecedenteFamiliarByFichaMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<AntecedenteFamiliarDTO>>
    > {
        let endPoint =
            this.path + '/obtenerAntecedentesFamiliaresPorFichaMedica';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /**
     * Crea un nuevo antecedente familiar
     * @params antecedenteFamiliar objeto DTO que contiene los datos para crear un antecedente
     * @returns retorna un objeto AntecedenteFamiliarDTO
     */
    postAntecedenteFamiliar(
        antecedenteFamiliar: AntecedenteFamiliarDTO
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<AntecedenteFamiliarDTO>>
    > {
        let endPoint = this.path + '/crearAntecedenteFamiliar';
        return this.backendService.postFinal(endPoint, antecedenteFamiliar, '');
    }

    /**
     * Actualiza un antecedente familiar
     * @params antecedenteFamiliar objeto DTO que contiene los datos para crear el antecedente
     * @returns retorna un objeto AntecedenteFamiliarDTO
     */
    updateAntecedenteFamiliar(
        antecedenteFamiliar: AntecedenteFamiliarDTO
    ): Observable<RespuestaPorDefecto<AntecedenteFamiliarDTO>> {
        let endPoint = this.path + '/actualizarAntecedenteFamiliar';
        return this.backendService.postFinal(endPoint, antecedenteFamiliar, '');
    }

    /**
     * Elimina un antecedente familiar
     * @params antecedenteFamiliar objeto DTO que contiene los datos para eliminar el antecedente
     * @returns retorna un objeto booleano
     */
    deleteAntecedenteFamiliar(
        antecedenteFamiliar: AntecedenteFamiliarDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarAntecedenteFamiliar';
        return this.backendService.postFinal(endPoint, antecedenteFamiliar, '');
    }

    /**
     * Obtener ingresos a centros por el token id de la ficha medica
     * @params paginacionRequest objeto de paginacion personalizado para pasar parametros
     * @returns retorna un objeto paginacion con IngresoCentroJuvenilDTO
     */
    getIngresoCentroJuvenilByFichaMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<IngresoCentroJuvenilDTO>>
    > {
        let endPoint = this.path + '/obtenerCentrosJuvenilesPorFichaMedica';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /**
     * Crear ingreso a centro juvenil
     * @params ingresoCentro objeto DTO que contiene los datos para crear un ingreso a centro juvenil
     * @returns retorna un objeto IngresoCentroJuvenilDTO
     */
    postIngresoCentroJuvenil(
        ingresoCentro: IngresoCentroJuvenilDTO
    ): Observable<RespuestaPorDefecto<IngresoCentroJuvenilDTO>> {
        let endPoint = this.path + '/crearCentroJuvenil';
        return this.backendService.postFinal(endPoint, ingresoCentro, '');
    }

    /**
     * Eliminar ingreso a centro juvenil
     * @params ingresoCentro objeto DTO que contiene los datos para editar un ingreso a centro juvenil
     * @returns retorna un objeto IngresoCentroJuvenilDTO
     */
    updateIngresoCentroJuvenil(
        ingresoCentro: IngresoCentroJuvenilDTO
    ): Observable<RespuestaPorDefecto<IngresoCentroJuvenilDTO>> {
        let endPoint = this.path + '/actualizarCentroJuvenil';
        return this.backendService.postFinal(endPoint, ingresoCentro, '');
    }

    /**
     * Eliminar ingreso a centro juvenil
     * @params ingresoCentro objeto DTO que contiene los datos para eliminar un ingreso a centro juvenil
     * @returns retorna un objeto booleano
     */
    deleteIngresoCentroJuvenil(
        ingresoCentro: IngresoCentroJuvenilDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarCentroJuvenil';
        return this.backendService.postFinal(endPoint, ingresoCentro, '');
    }

    //Seguimiento médico
    getEvaluacionMedicaByTokenId(
        tokenIdentificador: string
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaDTO>> {
        let endPoint = this.path + '/obtenerEvaluacionMedicaPorTokenId';
        return this.backendService.postFinal(endPoint, tokenIdentificador, '');
    }

    getEvaluacionMedicaByFichaMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EvaluacionMedicaDTO>>
    > {
        let endPoint = this.path + '/obtenerEvaluacionMedicaPorFichaMedica';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    getDiagnosticosByEvaluacionMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DiagnosticoDTO>>> {
        let endPoint = this.path + '/obtenerDiagnosticoPorEvaluacionMedica';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    getEstadoNutricionalByEvaluacionMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EstadoNutricionalDTO>>
    > {
        let endPoint =
            this.path + '/obtenerEstadoNutricionalPorEvaluacionMedica';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    postEvaluacionMedica(
        evaluacionMedica: EvaluacionMedicaDTO
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaDTO>> {
        let endPoint = this.path + '/crearEvaluacionMedica';
        return this.backendService.postFinal(endPoint, evaluacionMedica, '');
    }

    postDiagnostico(
        diagnostico: DiagnosticoDTO
    ): Observable<RespuestaPorDefecto<DiagnosticoDTO>> {
        let endPoint = this.path + '/crearDiagnostico';
        return this.backendService.postFinal(endPoint, diagnostico, '');
    }

    postEstadoNutricional(
        estadoNutricional: EstadoNutricionalDTO
    ): Observable<RespuestaPorDefecto<EstadoNutricionalDTO>> {
        let endPoint = this.path + '/crearEstadoNuticional';
        return this.backendService.postFinal(endPoint, estadoNutricional, '');
    }

    updateEvaluacionMedica(
        evaluacionMedica: EvaluacionMedicaDTO
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaDTO>> {
        let endPoint = this.path + '/actualizarEvaluacionMedica';
        return this.backendService.postFinal(endPoint, evaluacionMedica, '');
    }

    updateDiagnostico(
        diagnostico: DiagnosticoDTO
    ): Observable<RespuestaPorDefecto<DiagnosticoDTO>> {
        let endPoint = this.path + '/actualizarDiagnostico';
        return this.backendService.postFinal(endPoint, diagnostico, '');
    }

    updateEstadoNutricional(
        estadoNutricional: EstadoNutricionalDTO
    ): Observable<RespuestaPorDefecto<EstadoNutricionalDTO>> {
        let endPoint = this.path + '/actualizarCentroJuvenil';
        return this.backendService.postFinal(endPoint, estadoNutricional, '');
    }

    deleteEvaluacionMedica(
        evaluacionMedica: EvaluacionMedicaDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarEvaluacionMedica';
        return this.backendService.postFinal(endPoint, evaluacionMedica, '');
    }

    deleteDiagnostico(
        diagnostico: DiagnosticoDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarDiagnostico';
        return this.backendService.postFinal(endPoint, diagnostico, '');
    }

    deleteEstadoNutricional(
        estadoNutricional: EstadoNutricionalDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarEstadoNutricional';
        return this.backendService.postFinal(endPoint, estadoNutricional, '');
    }

    /**
     * Obtener enfermedades asociadas por el token id de la ficha medica
     * @params paginacionRequest objeto de paginacion personalizaco que contiene el tokenIdentificador de la ficha medica
     * @returns retorna una lista de objetos FichaMedicaEnfermedadDTO
     */
    getEnfermedadesAsociadasFicha(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<FichaMedicaEnfermedadDTO>>
    > {
        let endPoint = this.path + '/obtenerEnfermedadesRelacionadas';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /**
     * Obtener criterios asociados a la ficha seguimiento
     * @params paginacionRequest objeto de paginacion personalizaco que contiene el tokenIdentificador de la ficha medica
     * @returns retorna una lista de objetos FichaMedicaEnfermedadDTO
     */
    getCriteriosAsociadosSeguimiento(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>
        >
    > {
        let endPoint = this.path + '/obtenerCriteriosEvaluacionRelacionados';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    getEvaluacionMedicaProgresoByFichaMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<EvaluacionMedicaProgresoDTO>>
    > {
        let endPoint =
            this.path + '/obtenerEvaluacionMedicaProgresoPorFichaMedica';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    postEvaluacionMedicaProgreso(
        evaluacionMedica: EvaluacionMedicaProgresoDTO
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>> {
        let endPoint = this.path + '/crearEvaluacionMedicaProgreso';
        return this.backendService.postFinal(endPoint, evaluacionMedica, '');
    }

    getEvaluacionMedicaProgresoByTokenId(
        tokenIdentificador: string
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>> {
        let endPoint = this.path + '/obtenerEvaluacionMedicaProgresoPorTokenId';
        return this.backendService.postFinal(endPoint, tokenIdentificador, '');
    }

    deleteEvaluacionMedicaProgreso(
        evaluacionMedica: EvaluacionMedicaProgresoDTO
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarEvaluacionMedicaProgreso';
        return this.backendService.postFinal(endPoint, evaluacionMedica, '');
    }

    updateEvaluacionMedicaProgreso(
        evaluacionMedica: EvaluacionMedicaProgresoDTO
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaProgresoDTO>> {
        let endPoint = this.path + '/actualizarEvaluacionMedicaProgreso';
        return this.backendService.postFinal(endPoint, evaluacionMedica, '');
    }

    subirDocumentoEvaluacionMedicaProgreso(
        file: File,
        evalMedDocDTO: EvaluacionMedicaProgresoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumentoEvaluacionMedicaProgreso';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(evalMedDocDTO)
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

    obtenerDocumentosEvaluacionMedicaProgreso(
        request: EvaluacionMedicaProgresoDocumentosRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentosEvaluacionMedicaProgreso';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }

    eliminarDocumentoEvaluacionMedicaProgreso(
        evalMedDocDTO: EvaluacionMedicaProgresoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EvaluacionMedicaProgresoDocumentoDTO>> {
        let endPoint = this.path + '/eliminarDocumentoEvaluacionMedicaProgreso';
        return this.backendService.postFinal(
            endPoint,
            evalMedDocDTO,
            nemonicoMenu
        );
    }

    getConsultasByFichaMedica(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ConsultaAtencionIntegralDTO>>
    > {
        let endPoint = this.path + '/listarConsultasAtencionPorFicha';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    postConsultaAtencion(
        consulta: ConsultaAtencionIntegralDTO
    ): Observable<RespuestaPorDefecto<ConsultaAtencionIntegralDTO>> {
        let endPoint = this.path + '/crearConsultaAtencion';

        return this.backendService.postFinal(endPoint, consulta, '');
    }

    getConsultaByTokenId(
        tokenIdentificador: string
    ): Observable<RespuestaPorDefecto<ConsultaAtencionIntegralDTO>> {
        let endPoint = this.path + '/buscarConsultaAtencionPorToken';
        return this.backendService.postFinal(endPoint, tokenIdentificador, '');
    }

    deleteConsultaAtencion(
        consulta: ConsultaAtencionIntegralDTO
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarConsultaAtencion';
        return this.backendService.postFinal(endPoint, consulta, '');
    }

    subirDocumento(
        file: File,
        fichaMedicaDocumentoDTO: FichaMedicaDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(fichaMedicaDocumentoDTO)
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
        fichaMedicaDocumentosRequest: FichaMedicaDocumentoRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(
            endPoint,
            fichaMedicaDocumentosRequest,
            nemonicoMenu
        );
    }

    eliminarDocumento(
        pertenenciaDocumentoDTO: FichaMedicaDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaMedicaDocumentoDTO>> {
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
