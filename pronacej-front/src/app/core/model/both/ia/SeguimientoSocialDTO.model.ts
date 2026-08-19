import { CampoDTO } from "../campoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class SeguimientoSocialDTO extends CampoDTO {
    declare tokenEvaluacion: string;
    declare fecha: Date;
    declare nemonicoTipoActividadSocial?: string;
    declare descripcionSocial: string;
    declare accionesAdoptadas: string;
    declare comentarios: string;
    declare centro: JerarquiaDTO;
    declare programa: JerarquiaDTO;
    declare ambiente: JerarquiaDTO;
    
    declare nombreCompletoUsuarioCreacion: string;
    declare esVisualizacion?: boolean;
}
