import { Injectable } from "@angular/core";
import { AdolescenteExternadoDTO } from "app/core/model/both/ReportesDTO.model";
import { ExportacionRequest } from "app/core/model/request/ExportacionRequest.model";
import { PaginacionRequest } from "app/core/model/request/PaginacionRequest.model";
import { PaginacionResponse } from "app/core/model/response/PaginacionResponse.model";
import { RespuestaPorDefecto } from "app/core/model/response/RespuestaPorDefecto.model";
import { BackendService } from "app/core/services/backend.service";
import { environment } from "environments/environment";
import { Observable } from "rxjs";

export interface ExportacionEstadoDTO {
    jobId: string;
    estado: string;
    loteActual: number;
    totalLotes: number;
    registrosProcesados: number;
    tokenDescarga?: string;
    mensajeError?: string;
    esPropio: boolean;
    totalAdolescentesSolicitados: number;
    totalSeccionesSolicitadas: number;
    tamanoBytes?: number;
    tamanoOriginalBytes?: number;
}

export interface ExportacionJobIniciadoDTO {
    jobId: string;
}

@Injectable(
    {
        providedIn: "root"
    }
)
export class ReporteService {

    private path = "/reporte";    

    constructor(
        private backendService: BackendService
    ) { }
    
    obtenerAdolescentesExternados(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<AdolescenteExternadoDTO>>> {
        let endPoint = this.path + '/obtenerAdolescentesExternados';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    iniciarExportacionAdolescentes(
        exportacionRequest: ExportacionRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ExportacionJobIniciadoDTO>> {
        return this.backendService.postFinal(
            this.path + '/exportarAdolescentes/iniciar',
            exportacionRequest,
            nemonicoMenu
        );
    }

    consultarEstadoExportacionAdolescentes(
        jobId: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ExportacionEstadoDTO>> {
        return this.backendService.postFinal(
            this.path + '/exportarAdolescentes/estado',
            { jobId },
            nemonicoMenu
        );
    }

    listarExportacionesAdolescentes(
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ExportacionEstadoDTO[]>> {
        return this.backendService.postFinal(
            this.path + '/exportarAdolescentes/listar',
            {},
            nemonicoMenu
        );
    }

    cancelarExportacionAdolescentes(
        jobId: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<void>> {
        return this.backendService.postFinal(
            this.path + '/exportarAdolescentes/cancelar',
            { jobId },
            nemonicoMenu
        );
    }

    descartarExportacionAdolescentes(
        jobId: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<void>> {
        return this.backendService.postFinal(
            this.path + '/exportarAdolescentes/descartar',
            { jobId },
            nemonicoMenu
        );
    }

    construirUrlDescargaExportacion(token: string): string {
        const base = (environment as any).URL_SERVICIOS || (environment as any).urlServicios || '';
        return `${base}${this.path}/exportarAdolescentes/descargar?token=${encodeURIComponent(token)}`;
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }    
}
