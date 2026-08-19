import { Injectable } from "@angular/core";
import { AdolescenteExternadoDTO } from "app/core/model/both/ReportesDTO.model";
import { ExportacionRequest } from "app/core/model/request/ExportacionRequest.model";
import { PaginacionRequest } from "app/core/model/request/PaginacionRequest.model";
import { PaginacionResponse } from "app/core/model/response/PaginacionResponse.model";
import { RespuestaPorDefecto } from "app/core/model/response/RespuestaPorDefecto.model";
import { BackendService } from "app/core/services/backend.service";
import { Observable } from "rxjs";

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

    exportarAdolescentes(
        exportacionRequest: ExportacionRequest,
        nemonicoMenu: string = ''
    ): Observable<ArrayBuffer> {
        let endPoint = this.path + '/exportarAdolescentes';
        return this.backendService.postBlobGeneralBodyEncriptado(
            endPoint,
            exportacionRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }    
}