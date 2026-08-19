import { CampoDTO } from "../campoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class SeguimientoEducativoLaboralOtrosDTO extends CampoDTO {
    // Identificadores
    declare tokenIdentificadorEvaluacionSeguimiento: string;

    // Información de la institución
    declare institucionVisitada: string;
    declare personaEntrevistada: string;
    declare direccion: string;

    // Información del seguimiento
    declare fechaSeguimiento: Date;
    declare medioVerificacion: string;
    declare resultadoSeguimiento: string;
    declare sugerenciasRecomendaciones: string;

    // Campos añadidos
    declare centro: JerarquiaDTO;
    declare programa: JerarquiaDTO;
    declare ambiente: JerarquiaDTO;
    declare tokenIdentificadorTipoSeguimientoSocial: string;

    declare esVisualizacion?: boolean;
}
