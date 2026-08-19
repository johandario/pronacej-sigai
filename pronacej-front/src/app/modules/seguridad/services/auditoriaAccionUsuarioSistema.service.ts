import { Injectable } from '@angular/core';
import { AuditoriaAccionesSistemaDTO } from 'app/core/model/both/AuditoriaAccionesSistemaDTO.model';
import { PaginacionAuditoriasAccionesRequest } from 'app/core/model/request/PaginacionAuditoriasAccionesRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class AuditoriAccionUsuarioSistemaService {
    private path = '/auditoria-accion-sistema';

    constructor(private backendService: BackendService) {}

    /**
     * Obten las auditorias por filtros y paginadas
     *
     * @param paginacionAuditoriasAccionesRequest PaginacionAuditoriasAccionesRequest datos de la paginación
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<AuditoriaAccionesSistemaDTO>>>
     */
    obtenerPorFiltros(
        paginacionAuditoriasAccionesRequest: PaginacionAuditoriasAccionesRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<AuditoriaAccionesSistemaDTO>>
    > {
        let endPoint = this.path + '/obtenerPorFiltros';

        return this.backendService.postFinal(
            endPoint,
            paginacionAuditoriasAccionesRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
