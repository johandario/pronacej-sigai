import { CampoDTO } from "../campoDTO.model";
import { RegistroInstitucionDTO } from "../RegistroInstitucionDTO.model";


export class AdolescDerivadoInstDTO extends CampoDTO{

   
    idAdolescenteDerivado: number;
    tokenFichaIdentificacion: string;
    fechaRegistro: string | Date;
    fechaDerivacion?: string | Date;
    departamento?: string;
    tiempoServicio?: string;
    servicio?: string; 
    personaResponsable?: string;
    institucion?: RegistroInstitucionDTO;
    estado?: string; 
  
}

