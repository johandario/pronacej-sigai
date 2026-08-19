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
export class FichaIdentificacionCarpetaService {

    private path = "/ficha-principal-carpeta";

    constructor(
        private backendService: BackendService
    ) { }

    obtenerInformacionDeCarpeta(fichaIdentificacionCarpetaRequest: FichaIdentificacionCarpetaRequest,
        nemonicoMenu :string
    ): Observable<RespuestaPorDefecto<ContenidoCarpetaResponse>> {
        let endPoint = this.path + "/obtenerInformacionDeCarpeta";
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionCarpetaRequest,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true):Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}