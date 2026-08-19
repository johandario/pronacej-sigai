import { CampoDTO } from "./campoDTO.model";
import { JerarquiaDTO } from "./jerarquiaDTO.model";

export class EvaluacionDomiciliariaDTO extends CampoDTO {

    declare tokenIdentificadorFichaIdentificacion: string;
    declare centro: JerarquiaDTO;
    declare fechaRegistro: Date;
    declare fechaEntrevista: Date;
    // declare personaEntrevistada: string;
    declare duracionVista: number;
    declare visitaRealizada: boolean;
    declare motivoNoVisita: string;
    declare objetivoGeneral: string;
    declare desarrolloVisitaDomiciliaria: string;
    declare caracteristicasDomicilioVisitado: string;
    declare conclusiones: string;
    declare recomendaciones: string;
    declare tokenIdentificadorPersonaRelacionada: string;
    declare otraPersonaRelacionada: string;
    
    // Campos medio cerrado
    declare dinamicaFamiliarDisfuncional: string;
    declare caracteristicasEntornoSocialMC: string;
    declare factoresProtectores: string;
    
    // Campos medio abierto
    declare factoresRiesgoFamilia: string;
    declare factoresRiesgoSocial: string;
    declare factoresProtectoresFamilia: string;
    declare factoresProtectoresSocial: string;

    declare esVisualizacion?: boolean;
}
