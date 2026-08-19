import { User } from "app/core/user/user.types";
import { JerarquiaDTO } from "../both/jerarquiaDTO.model";

export class LoginResponseFuse {
    declare accessToken: string;
    tokenType = "bearer";
    declare user: User;
    declare message: string;
    declare success: boolean;
    declare estado: string;
    listaJerarquias?: JerarquiaDTO[];
    declare tokenIdentificadorJerarquia?: string;
    declare tokenIdentificadorEmpresa: string;
    declare tokenIdentificadorRolJerarquia: string;
    declare tokenPermisos: string;
}