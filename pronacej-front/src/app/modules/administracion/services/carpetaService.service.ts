import { Injectable } from "@angular/core";
import { BodyEncriptado } from "app/core/model/both/bodyEncriptado.model";
import { FichaIdentificacionCarpetaRequest } from "app/core/model/request/ia/FichaIdentificacionCarpetaRequest.model";
import { ContenidoCarpetaResponse } from "app/core/model/response/ia/ContenidoCarpetaResponse.model";
import { RespuestaPorDefecto } from "app/core/model/response/RespuestaPorDefecto.model";
import { BackendService } from "app/core/services/backend.service";
import { Observable, Subscriber } from "rxjs";

@Injectable(
    {
        providedIn: "root"
    }
)
export class CarpetaService {
    private path = "/carpeta";

    constructor(
        private backendService: BackendService
    ) { }

    obtenerCarpetaDesdeFichaPrincipal(fichaIdentificacionCarpetaRequest: FichaIdentificacionCarpetaRequest,
        nemonicoMenu : string
    ): Observable<RespuestaPorDefecto<ContenidoCarpetaResponse>> {
        let endPoint = this.path + "/obtenerCarpetaDesdeFichaPrincipal";
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionCarpetaRequest,
            nemonicoMenu
        );
    }

    checkError(error: any, mostrarError = true) {
        this.backendService.checkError(error, mostrarError);
    }
}