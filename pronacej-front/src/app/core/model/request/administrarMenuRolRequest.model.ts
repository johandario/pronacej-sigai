import { CreacionDeRol } from "../both/CreacionDeRol.model";
import { MenuDTO } from "../both/seguridad/MenuDTO.model";

export class AdministrarMenuRolRequest {
    declare rol: CreacionDeRol;
    declare listaMenus: MenuDTO[];
}