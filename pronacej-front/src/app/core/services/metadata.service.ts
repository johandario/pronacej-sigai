import { Injectable } from "@angular/core";
import { BackendService } from "./backend.service";
import { Observable, Subscriber } from "rxjs";
import { RespuestaPorDefecto } from "../model/response/RespuestaPorDefecto.model";
import { BodyEncriptado } from "../model/both/bodyEncriptado.model";

@Injectable(
    {
        providedIn: "root"
    }
)
export class MetadataService {

    private path = "/metadata";

    constructor(private backendService: BackendService) { }

    obtenerTablas(): Observable<RespuestaPorDefecto<String[]>> {
        let endPoint = this.path + "/obtenerTablas";

        return this.backendService.getFinal(endPoint, {}, "");
    }

    obtenerCampos(tabla: String): Observable<RespuestaPorDefecto<String[]>> {
        let endPoint = this.path + "/obtenerCampos";
        return this.backendService.getFinal(endPoint, {tabla}, "");
    }
}