import { CampoDTO } from "../campoDTO.model";
import { FuncionarioJerarquiaRolDTO } from "./FuncionarioJerarquiaRolDTO.model";

export class UsuarioSistemaDTO extends CampoDTO {

    declare nombres: string;
    declare apellidos: string;
    declare userName: string;
    declare email: string;
    declare telefono: string;
    declare tokenIdentificadorTipoDeDocumento: string;
    declare numeroDeDocumento: string;
    declare numeroDeCelular: string;
    declare logo: string;
    declare password: string;
    declare fechaCreacion: Date;

    asignaciones: FuncionarioJerarquiaRolDTO[] = [];

}