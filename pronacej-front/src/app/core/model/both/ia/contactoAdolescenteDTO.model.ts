import { CampoDTO } from "../campoDTO.model";
import { CatalogoDTO } from "../catalogoDTO.model";

export class ContactoAdolescenteDTO extends CampoDTO {
    declare fechaHora: Date;
    declare tokenIdentificadorModalidadEntrevista: string;
    declare descripcionActividad: string;
    declare observacionesSugerencias: string;
    
    declare tokenIdentificadorEtapa: string;
    declare tokenIdentificadorTipoContacto: string; 
    declare tokenIdentificadorFichaIdentificacion: string;
    
    declare nombreCompletoUsuarioCreacion: string;
    declare esVisualizacion?: boolean;
 }