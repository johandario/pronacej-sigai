import { UsuarioSistemaDTO } from "./seguridad/usuarioSistemaDTO.model";

export class CreacionDeUsuarioSistema extends UsuarioSistemaDTO {
    declare nombreRol: string;
    declare tokenIdentificadorRol: string;

    declare tokenRelacion: String;
    bloqueadoRelacion = false;

}