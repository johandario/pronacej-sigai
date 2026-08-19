import { RolDTO } from "./seguridad/rolDTO.model";

export class CreacionDeRol extends RolDTO {

    declare tokenRelacion: String;
    bloqueadoRelacion = false;

}