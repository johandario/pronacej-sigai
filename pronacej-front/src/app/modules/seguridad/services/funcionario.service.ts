import { Injectable } from '@angular/core';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class FuncionarioService {
    private path = '/funcionario';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerFuncionariosValidos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>> {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerFuncionariosSinPaginacion(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FuncionarioDTO[]>> {
        let endPoint = this.path + '/listaSinPaginacion';
        return this.backendService.getFinal(
            endPoint,
            nemonicoMenu
        );
    }

    crearFuncionario(
        creacionDeFuncionario: FuncionarioDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FuncionarioDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            creacionDeFuncionario,
            nemonicoMenu
        );
    }

    eliminarFuncionario(
        creacionDeFuncionario: FuncionarioDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            creacionDeFuncionario,
            nemonicoMenu
        );
    }

    obtenerFuncionarioDelUsuario(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FuncionarioDTO>> {
        let endPoint = this.path + '/obtenerFuncionarioDelUsuario';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerFuncionariosPorValor(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<FuncionarioDTO>>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerJerarquiasFuncionarioDelUsuario(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO[]>> {
        let endPoint = this.path + '/obtenerJerarquiasFuncionarioDelUsuario';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
