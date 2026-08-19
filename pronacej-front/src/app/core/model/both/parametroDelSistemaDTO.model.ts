import { CatalogoDTO } from "./catalogoDTO.model";

export class ParametroDelSistemaDTO extends CatalogoDTO {
    declare valor: string;
    declare valorExterno: string;

    declare hijos2: ParametroDelSistemaDTO;
}