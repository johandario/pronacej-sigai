import { CampoDTO } from "../campoDTO.model";
import { CatalogoDTO } from "../catalogoDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";
import { FichaIdentificacionDTO } from "../fichaIdentificacionDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class SancionDisciplinariaDTO extends CampoDTO {
    declare fechaRegistro: Date;
    declare fechaFin: Date;
    declare fechaInicio: Date;
    declare nroResolucion?: string;
    declare falta: string;
    declare sancion: string;
    declare observacion: string;
    declare centro: JerarquiaDTO;
    declare programa: JerarquiaDTO;
    declare ambiente: JerarquiaDTO;
    declare tipificacionFalta?:CatalogoDTO
    declare motivo?:string
    declare fichaIdentificacion: FichaIdentificacionDTO
    declare tokenIdentificador: string
    documentoDTOList: DocumentoDTO[] = [];
    
   
}