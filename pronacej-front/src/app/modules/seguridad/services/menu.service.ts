import { Injectable } from "@angular/core";
import { FuseNavigationItem } from "@fuse/components/navigation";
import { BodyEncriptado } from "app/core/model/both/bodyEncriptado.model";
import { MenuDTO } from "app/core/model/both/seguridad/MenuDTO.model";
import { NavigationFuseResponse } from "app/core/model/response/NavigationFuseResponse.model";
import { RespuestaPorDefecto } from "app/core/model/response/RespuestaPorDefecto.model";
import { Navigation } from "app/core/navigation/navigation.types";
import { BackendService } from "app/core/services/backend.service";
import { environment } from "environments/environment";
import { Observable, Subscriber, tap } from "rxjs";

@Injectable(
    {
        providedIn: "root"
    }
)
export class MenuService {

    private path = "/menu";

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    /**
  * Obten los menu disponibles para el sistema 
  *
  * 
  * @returns Observable<Navigation>
  */
    obtenerMenu(): Observable<Navigation> {
        let endPoint = this.path + "/obtenerMenu";
        return new Observable(
            (subscr: Subscriber<Navigation>) => {
                this.backendService.getFinal<RespuestaPorDefecto<NavigationFuseResponse>>(endPoint, {}).subscribe(
                    {
                        next: (resp: RespuestaPorDefecto<NavigationFuseResponse>) => {
                            if (!environment.production) {
                                console.log("MenuService.obtenerMenu", resp);
                            }

                            if (!resp.exito) {
                                this.backendService.checkError(resp);
                                subscr.complete();
                            }
                            let menuCompact = resp.data.compact as FuseNavigationItem[];
                            let menuDefault = resp.data.porDefecto as FuseNavigationItem[];

                            let navigation: Navigation = {
                                compact: menuCompact,
                                default: menuDefault,
                                futuristic: menuDefault,
                                horizontal: menuDefault
                            };
                            subscr.next(navigation);
                            subscr.complete();

                        },
                        error: (error: any) => {
                            this.backendService.checkError(error)
                            subscr.error(error);
                            subscr.complete();

                        }
                    }
                );
            }
        );
    }

    /**
* Obten los menu disponibles para el sistema 
*
* @param nemonicoMenu string nemonico de un menu del sistema
*  
* @returns Observable<Navigation>
*/
    obtenerTodosLosMenu(nemonicoMenu: string): Observable<RespuestaPorDefecto<MenuDTO[]>> {
        let endPoint = this.path + "/obtenerTodosLosMenu";
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerMenusPermisos(nemonicoMenu: string): Observable<RespuestaPorDefecto<MenuDTO[]>> {
        let endPoint = this.path + "/obtenerMenusPermisos";
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerMenusPorEmpresa(nemonicoMenu: string): Observable<RespuestaPorDefecto<MenuDTO[]>> {
        let endPoint = this.path + "/obtenerMenusPorEmpresa";
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerMenusPadres(nemonicoMenu: string): Observable<RespuestaPorDefecto<MenuDTO[]>> {
        let endPoint = this.path + "/obtenerMenusPadres";
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}