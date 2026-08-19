import { Injectable } from "@angular/core";
import { BackendService } from "./backend.service";
import { NotificacionDTO } from "../model/both/ia/notificacionDTO.model";
import { Observable, Subscriber } from "rxjs";
import { RespuestaPorDefecto } from "../model/response/RespuestaPorDefecto.model";
import { BodyEncriptado } from "../model/both/bodyEncriptado.model";
import { GeneracionPdfRequest } from "../model/request/GeneracionPdfRequest.model";

@Injectable(
    {
        providedIn: "root"
    }
)
export class PdfService {

    private path = "/utils";

    constructor(private backendService: BackendService) { }

    generarPdf(generacionPdfRequest: GeneracionPdfRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<string>> {
        let endPoint = this.path + "/generarPdfFormulario";
        return new Observable(
            (susbscriber: Subscriber<RespuestaPorDefecto<string>>) => {

                this.backendService.postJsonGeneralBodyEncriptado2(endPoint,
                    generacionPdfRequest, nemonicoMenu
                ).subscribe(
                    {
                        next: (response: RespuestaPorDefecto<string>) => {
                            let resp = response;
                            susbscriber.next(resp);
                            susbscriber.complete();
                        },
                        error: (error: any) => {
                            susbscriber.error(error);
                            susbscriber.complete();
                        }
                    }
                );
            }
        );
    }

    dataSunat(ruc: string): Observable<string> {
        let endPoint = this.path + "/dataSunat";
        console.log(endPoint, ruc);
        
        return new Observable((subscriber: Subscriber<string>) => {
            this.backendService.getJsonGeneralBodyEncriptado2(endPoint, { ruc }).subscribe({
                next: (response: string) => {
                    subscriber.next(response);
                    subscriber.complete();
                },
                error: (error: any) => {
                    subscriber.error(error);
                    subscriber.complete();
                },
            });
        });
    }

    checkError(error: any, mostrarError = true) {
        this.backendService.checkError(error, mostrarError);
    }
}