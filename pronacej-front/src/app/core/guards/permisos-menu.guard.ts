import { inject } from '@angular/core';
import {
  CanActivateFn,
  CanActivateChildFn,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router
} from '@angular/router';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { DialogMensajeService } from '../services/dialog-mensaje.service';
import { environment } from 'environments/environment';

export const PermisosMenuGuard: CanActivateFn | CanActivateChildFn =
  (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> => {

    const permisosService = inject(PermisoRolUsuarioService);

    // Puedes obtener el nemónico desde la ruta
    const nemonicoMenu = '';

    // if (!nemonicoMenu) {
    //   // Para pruebas: si no hay nemónico, permites el acceso
    //   return of(true);
    // }

    return permisosService.obtenerPermisosUsuario(nemonicoMenu).pipe(
      map((tienePermiso) => {
        if (!environment.production) {
          console.log('PermisosMenuGuard - tienePermiso:', tienePermiso.exito);
        }
        
        return true;
        
      }),
      catchError((error) => {
        console.error('Error validando permisos', error);
        return of(false);
      })
    );
  };