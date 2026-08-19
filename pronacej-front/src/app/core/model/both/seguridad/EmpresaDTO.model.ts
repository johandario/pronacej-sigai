import { CampoDTO } from "../campoDTO.model";

export class EmpresaDTO extends CampoDTO {


    declare nombre: string;
    declare descripcion: string;

    declare nombreCorto: string;

    declare urlPagina: string;

    declare urlLogo: string;

    declare colorPrimarioHex: string;
    declare colorSecundarioHex;

    declare userNameAlfresco: string;
    declare constraseniaAlfresco: string;

}