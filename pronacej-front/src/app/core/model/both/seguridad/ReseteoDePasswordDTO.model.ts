import { CampoDTO } from "../campoDTO.model";
import { CatalogoDTO } from "../catalogoDTO.model";
import { EmpresaDTO } from "./EmpresaDTO.model";
import { PasswordUserSistemaDTO } from "./PasswordUserSistemaDTO.model";
import { UsuarioSistemaEmpresaRolDTO } from "./UsuarioSistemaEmpresaRolDTO.model";

export class ReseteoDePasswordDTO extends CampoDTO {


    declare estadoDTO: CatalogoDTO;

    declare usuarioSistemaEmpresaRolDTO: UsuarioSistemaEmpresaRolDTO;
    declare empresaDTO: EmpresaDTO;
    declare passwordUserSistemaDTO: PasswordUserSistemaDTO;

    declare recaptchaV3: string;

}