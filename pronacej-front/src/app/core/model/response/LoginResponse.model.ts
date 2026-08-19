import { JerarquiaDTO } from "../both/jerarquiaDTO.model";
import { UserDataResponse } from "./UserDataResponse.model";

export class LoginResponse {
    declare estado: string;
    declare jwt: string;
    declare nombreRol: string;
    declare nombreEmpresa: string;
    declare userDataResponse: UserDataResponse;

    declare tokenIdentificadorEmpresa: string;
    listaJerarquias?: JerarquiaDTO[];
    declare tokenIdentificadorJerarquia: string;
    declare tokenIdentificadorRolJerarquia: string;
    declare tokenPermisos: string;
}