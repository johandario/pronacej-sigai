import { CatalogoDTO } from "../../catalogoDTO.model";

export class AntecedenteFamiliarDTO{
    declare tokenIdentificador?: string;
    declare tokenIdFichaIdentificacion: string;
    declare enfermedad: CatalogoDTO;
    declare parentesco: CatalogoDTO;
}