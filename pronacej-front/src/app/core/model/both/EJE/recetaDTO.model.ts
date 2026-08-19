import { CatalogoDTO } from "../catalogoDTO.model";
import { DetalleRecetaDTO } from "./detalleRecetaDTO.model";

export class RecetaDTO {
    declare tokenIdentificador?: string;
    declare tokenIdEvaluacionMedica?: string;
    declare numeroReceta?: string;
    declare fechaEmision?: Date;
    declare observaciones?: string;
    declare especialidad?: CatalogoDTO;
    declare detalles?: DetalleRecetaDTO[];
    declare tokenIdConsultaAtencionIntegral?: string;
}