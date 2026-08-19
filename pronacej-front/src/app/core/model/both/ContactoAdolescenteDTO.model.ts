import { CampoDTO } from "./campoDTO.model";

export class ContactoAdolescenteDTO extends CampoDTO{
    idContactoAdolescente: number;
    tokenFichaIdentificacion: string;
    fechaRegistro: string | Date;
    usuarioResponsable?: string;
    modalidadEntrevista?: string;
    observaciones?: string; 
    actividades?: string;
   
}


