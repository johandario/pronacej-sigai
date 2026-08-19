import { CampoDTO } from "../campoDTO.model";

export class UsuarioSistemaEmpresaRolDTO extends CampoDTO {

    declare tokenIdentificadorEmpresa: string;
    declare tokenIdentificadorRol: string;
    declare tokenIdentificadorUsuario: string;

}