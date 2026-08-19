import { CampoDTO } from "../campoDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";

export class NotificacionDTO extends CampoDTO {
    declare remitente: string;

    declare destinatarios: string;

    declare cuerpo: string;

    declare asunto: string;

    declare tipo: string;

    declare medio: string;

    declare adolescente: number;

    declare observacionesEntrega?: string;

    declare entregado?: string;

    declare fechaEntrega?: Date;

    declare documentoDTOList: DocumentoDTO[];
}