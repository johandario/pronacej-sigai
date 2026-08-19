import { Injectable } from '@angular/core';
import { ReseteoDePasswordDTO } from 'app/core/model/both/seguridad/ReseteoDePasswordDTO.model';
import { ReseteoDeContraseniaRequest } from 'app/core/model/request/ReseteoDeContraseniaRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class ReseteoDeContraseniaService {
    private path = '/reseteo-password';

    constructor(private backendService: BackendService) {}

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }

    /**
     * Crea un proceso de reseteo de contraseña
     *
     * @param reseteoDeContraseniaRequest ReseteoDeContraseniaRequest
     * @param nemonicoMenu string nemonico del menu
     *
     * @returns Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>>
     */
    empezar(
        reseteoDeContraseniaRequest: ReseteoDeContraseniaRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>> {
        let endPoint = this.path + '/empezar';

        return this.backendService.postFinal(
            endPoint,
            reseteoDeContraseniaRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea un proceso de reseteo de contraseña
     *
     * @param reseteoDePasswordDTO ReseteoDePasswordDTO
     * @param nemonicoMenu string nemonico del menu
     *
     * @returns Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>>
     */
    resetearPassword(
        reseteoDePasswordDTO: ReseteoDePasswordDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>> {
        let endPoint = this.path + '/resetearPassword';
        return this.backendService.postFinal(
            endPoint,
            reseteoDePasswordDTO,
            nemonicoMenu
        );
    }

    /**
     * Cancela un proceso de reseteo de password
     *
     * @param reseteoDePasswordDTO ReseteoDePasswordDTO
     * @param nemonicoMenu string nemonico del menu
     *
     * @returns Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>>
     */
    cancelarReseteoDePassword(
        reseteoDePasswordDTO: ReseteoDePasswordDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>> {
        let endPoint = this.path + '/cancelarReseteoDePassword';
        return this.backendService.postFinal(
            endPoint,
            reseteoDePasswordDTO,
            nemonicoMenu
        );
    }

    /**
     * Verifica un proceso de reseteo de password
     *
     * @param reseteoDePasswordDTO ReseteoDePasswordDTO
     * @param nemonicoMenu string nemonico del menu
     *
     * @returns Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>>
     */
    verificarReseteoDePassword(
        reseteoDePasswordDTO: ReseteoDePasswordDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ReseteoDePasswordDTO>> {
        let endPoint = this.path + '/verificarReseteoDePassword';
        return this.backendService.postFinal(
            endPoint,
            reseteoDePasswordDTO,
            nemonicoMenu
        );
    }
}
