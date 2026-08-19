import { inject, Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, Resolve } from "@angular/router";
import { PermisoRolUsuarioService } from "app/modules/seguridad/services/permiso-rol-usuario.service";
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class FichaResolver implements Resolve<any> {
  private permisoRolUsuarioService = inject(PermisoRolUsuarioService);  

  resolve(route: ActivatedRouteSnapshot) {    
    const uuid = route.paramMap.get('uuid_fp');
    const nemonicoMenu = route.data['nemonicoMenu'];

    return this.permisoRolUsuarioService.obtenerPermisosUsuario(nemonicoMenu, uuid);
  }
}
