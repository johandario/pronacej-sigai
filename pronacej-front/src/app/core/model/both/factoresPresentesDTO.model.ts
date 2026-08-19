import { CampoDTO } from "./campoDTO.model";

export class FactoresPresentesDTO extends CampoDTO {
    // Identificador de la ficha principal
    declare tokenIdentificadorFichaIdentificacion: string;

    // Identificador de los factores presentes
    declare tokenIdentificadorFactoresPresentes: string;

    // Factores protectores
    declare factoresProtectores: string;

    // Factores de riesgo
    declare factoresRiesgo: string;
}