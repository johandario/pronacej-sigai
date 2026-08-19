import { CampoDTO } from "./campoDTO.model";

export class SituacionActualAdolescenteDTO extends CampoDTO {
    // Identificador de la ficha principal
    declare tokenIdentificadorFichaIdentificacion: string;

    // Identificador de los factores presentes
    declare tokenIdentificadorFactoresPresentes: string;

    // Identificador del tipo de área
    declare tokenIdentificadorTipoArea: string;

    // Identificador del tipo de situación
    declare tokenIdentificadorTipoSituacion: string;

    // Descripción detallada de la situación
    declare descripcion: string;

    // Observaciones adicionales
    declare observacion: string;
}