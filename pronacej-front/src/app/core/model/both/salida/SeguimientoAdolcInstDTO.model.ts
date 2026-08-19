import { CampoDTO } from "../campoDTO.model";
import { AdolescDerivadoInstDTO } from "./AdolescDerivadoInstDTO.model";

export class SeguimientoAdolescInstDTO extends CampoDTO{

    idAdolescenteSeguimiento: number;
    tokenFichaIdentificacion: string;
    fechaSeguimiento: string | Date;
    medioEntrevista?: string
    resultadoEntrevista?: string;
    recomendacion?: string;
    observacion?: string; 
    adolescenteDerivadoInst?: AdolescDerivadoInstDTO;
  
}

