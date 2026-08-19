import { CampoDTO } from "./campoDTO.model";

export class RecomendacionComentarioPorEvalSeguDTO extends CampoDTO {
    declare tokenIdentificadorEvaluacionSeguimiento: string;
    declare comentario: string;
    declare fecha: Date;
}