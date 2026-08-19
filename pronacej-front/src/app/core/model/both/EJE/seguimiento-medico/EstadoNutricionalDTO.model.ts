import { CatalogoDTO } from "../../catalogoDTO.model";

export class EstadoNutricionalDTO{
    declare tokenIdentificador? : string;
    declare tokenIdEvaluacionMedica: string;
    declare criterio: CatalogoDTO;
    declare grado: CatalogoDTO;
}