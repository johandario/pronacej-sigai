import { CampoDTO } from "./campoDTO.model";
import { RecomendacionComentarioPorEvalSeguDTO } from "./recomendacionComentarioPorEvalSeguDTO.model";

export class EvaluacionSeguimientoEducativoLaboralDTO extends CampoDTO {
    declare tokenIdentificadorFichaIdentificacion: string;
    
    // Campos principales
    declare tokenIdentificadorTipoEvaluacionSeguimiento: string;
    declare fechaInicio: Date;
    declare fechaFin: Date;
    declare tokenIdentificadorInstitucion: string;
    declare tokenIdentificadorMedioVerificacion: string;
    declare resultadoSeguimiento: string;
    
    declare listaRecomendacionesComentarios: RecomendacionComentarioPorEvalSeguDTO[];
    declare nombreCompletoUsuarioCreacion: string;
    declare esVisualizacion?: boolean;

    declare nombreInstitucionOtros: string;
}