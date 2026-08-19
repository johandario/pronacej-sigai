import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { HistoricoEntradaSalidaDTO } from 'app/core/model/both/HistoricoEntradaSalidaDTO.model';
import { HistoricoEntradaSalidaRequest } from 'app/core/model/request/HistoricoEntradaSalidaRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class HistoricoEntradaSalidaService {
    private path = '/historico-entrada-salida';

    constructor(private backendService: BackendService) {}

    obtenerHistoricoSalidaActivo(
        historicoRequest: HistoricoEntradaSalidaRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<HistoricoEntradaSalidaDTO>> {
        let endPoint = this.path + '/obtenerHistoricoSalidaActivo';
        return this.backendService.postFinal(
            endPoint,
            historicoRequest,
            nemonicoMenu
        );
    }
}
