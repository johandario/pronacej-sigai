import { Injectable } from "@angular/core";
import { BackendService } from "./backend.service";
import { Observable, Subscriber } from "rxjs";
import { RespuestaPorDefecto } from "../model/response/RespuestaPorDefecto.model";

@Injectable(
    {
        providedIn: "root"
    }
)
export class UtilsService {

    private path = "/utils";

    constructor(private backendService: BackendService) { }

    actualizarCarpetasAlfresco(nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + "/actualizarCarpetasAlfresco";
        return new Observable(
            (susbscriber: Subscriber<RespuestaPorDefecto<Boolean>>) => {

                this.backendService.getJsonGeneralBodyEncriptado2(endPoint,
                    nemonicoMenu
                ).subscribe(
                    {
                        next: (response: RespuestaPorDefecto<Boolean>) => {
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

    data(dni: string): Observable<string> {
        let endPoint = this.path + "/data";
        console.log(endPoint, dni);
        
        return new Observable((subscriber: Subscriber<string>) => {
            this.backendService.getJsonGeneralBodyEncriptado2(endPoint, { dni }).subscribe({
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
}