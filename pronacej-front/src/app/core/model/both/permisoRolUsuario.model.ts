import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";
import { FuncionarioDTO } from "./seguridad/FuncionarioDTO.model";
import { RolDTO } from "./seguridad/rolDTO.model";

export class PermisoRolUsuarioNombresDTO {
    nombreFuncionario?: string;
    tipoAsignacion?: string;
    tipoPermiso?: string;
    nombreRoles?: string;
    fechaCreacion?: Date;
    tokenIdentificador?: string;
}

export class PermisoRolUsuarioDTO extends CampoDTO {
    fechaCreacionTexto?: string;
    funcionario?: FuncionarioDTO;
    roles?: RolDTO[];
    tipoPermiso?: CatalogoDTO;
    tipoAsignacion?: CatalogoDTO;
    menus: PermisoRolUsuarioMenuDTO[] = [];
}

export class PermisoRolUsuarioMenuDTO extends CampoDTO {
    tokenMenu: string;
    nemonicoMenu!: string;
    acciones: PermisoRolUsuarioMenuAccionDTO[] = [];
}

export class PermisoRolUsuarioMenuAccionDTO extends CampoDTO {
    tokenCatalogoAccion: string;
    nemonicoCatalogoAccion!: string;
    activo: boolean;
}

export interface PermissionMap {
    [nemonicoMenu: string]: {
        [nemonicoAccion: string]: boolean;
    };
}