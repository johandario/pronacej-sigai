import { CatalogoDTO } from "../../catalogoDTO.model";

export class IngresoCentroJuvenilDTO{
    declare tokenIdentificador?: string;
    declare tokenIdFichaIdentificacion: string;
    declare centro: string;
    declare fechaIngreso: Date;
    declare fechaEgreso: Date;
    declare motivo: CatalogoDTO;
}