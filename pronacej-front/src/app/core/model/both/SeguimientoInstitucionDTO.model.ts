import { CampoDTO } from "./campoDTO.model";
import { RegistroInstitucionDTO } from "./RegistroInstitucionDTO.model";


export class SeguimientoInstitucionDTO extends CampoDTO {
    idSeguimientoInstitucion: number;
    fechaRegistro: string | Date;
    numeroDoc: string;
    estado: string;
    fecha: string | Date;
    personaEntrevistada: string;
    fortalezas: string;
    debilidades: string;
    cumpleObjetivo: boolean;
    personaResponsable: string;
    registroInstitucion: RegistroInstitucionDTO;
    
 
}

