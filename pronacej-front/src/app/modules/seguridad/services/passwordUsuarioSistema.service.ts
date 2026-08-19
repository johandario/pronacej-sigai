import { Injectable } from '@angular/core';
import { ActualizarDatosDeSeguridadDTO } from 'app/core/model/both/ActualizarDatosDeSeguridadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class PasswordUsuarioSistemaService {
    private path = '/password-usuario-sistema';

    constructor(private backendService: BackendService) {}

    /**
     * Actualiza las opciones de seguridad de un usuario logeado en el sistema
     *
     * @param actualizarDatosDeSeguridadRequest ActualizarDatosDeSeguridadDTO datos del request a enviar
     * @param nemonicoMenu string nemonico del menu donde se realiza la operación
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    actualizarDatosDeSeguridad(
        actualizarDatosDeSeguridadRequest: ActualizarDatosDeSeguridadDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/actualizarDatosDeSeguridad';
        return this.backendService.postFinal(
            endPoint,
            actualizarDatosDeSeguridadRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
