import { CampoDTO } from "./campoDTO.model";

export class AreasSituacionEducativaLaboralOcioDTO extends CampoDTO {

    declare tokenIdentificadorFichaIdentificacion: string;
    
    // Área Educativa
    declare actitudEstudios: string;
    declare desarrolloEducativo: string; 
    declare interesesVocacionales: string;
    declare observacionesEducativas: string;

    // Área Laboral
    declare actitudEmpleo: string;
    declare capacitacionesEmpleabilidad: string;
    declare observacionesLaborales: string;

    // Área Ocio
    declare pasatiempos: string;
    declare talentos: string;
    declare participacionGrupal: string;
    declare usoTiempo: string;
    declare observacionesOcio: string;
}