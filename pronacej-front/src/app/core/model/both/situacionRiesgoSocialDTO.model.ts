import { StringUnitLength } from "luxon";
import { CampoDTO } from "./campoDTO.model";

export class SituacionRiesgoSocialDTO extends CampoDTO {

    declare tokenIdentificadorFichaIdentificacion: string;
    declare anteDeliFami: string;
    declare primManiInfrAdol: string;
    declare evasionHogar: boolean;
    declare estadoSaludGeneral: string;
    declare problemasLegales: string;
    declare observaciones: string;

    declare nombreCompletoUsuarioCreacion: string;
    declare esVisualizacion?: boolean;
}