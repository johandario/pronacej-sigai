import { CampoDTO } from "../campoDTO.model";

export class RolDTO extends CampoDTO {
    declare nombre: string;
    declare codigo: string;
    declare descripcion: string;
    declare esSuperRol: boolean;
    declare esRolPorDefecto: boolean;
    declare diasExpiracionPassword: number;
}