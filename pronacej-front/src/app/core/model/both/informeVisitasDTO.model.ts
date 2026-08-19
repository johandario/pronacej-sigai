import { CampoDTO } from "./campoDTO.model";

export class InformeVisitasDTO extends CampoDTO {
    declare tokenIdentificadorPersonaRelacionada: string;
    declare tokenIdentificadorTipoAutorizacion: string;
    declare fechaInicio: Date;
    declare fechaFin: Date;
    declare causalesRestriccion: string;
    declare observaciones: string;
    declare tokenIdentificadorFichaPrincipal: string;
}