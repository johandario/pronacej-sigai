import { Injectable } from '@angular/core';
import { ActualizarDatosDeSeguridadDTO } from 'app/core/model/both/ActualizarDatosDeSeguridadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class UsuarioSistemaEmpresaRolService {
    private path = '/usuario-sistema-empresa-rol';
    constructor(private backendService: BackendService) {}

    /**
     * Obten las configuraciones de seguridad de un usuario del sistema
     *
     * @param nemonicoMenu string nemonico del menu donde se realiza la operación
     *
     * @returns Observable<RespuestaPorDefecto<ActualizarDatosDeSeguridadDTO>>
     */
    obtenerInformacionDeSeguridad(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ActualizarDatosDeSeguridadDTO>> {
        let endPoint = this.path + '/obtenerInformacionDeSeguridad';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
