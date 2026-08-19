import { CampoDTO } from "./campoDTO.model";
import { SituacionActualAdolescenteDTO } from "./situacionActualAdolescenteDTO.model";
import { FactoresPresentesDTO } from "./factoresPresentesDTO.model";

export class ApreciacionFinalTratamientoDTO extends CampoDTO {
    // Identificador de la ficha principal
    declare tokenIdentificadorFichaIdentificacion: string;

    // Fecha de registro de la apreciación
    declare fechaRegistro: Date;

    // Nombre completo del usuario que creó el registro
    declare nombreCompletoUsuarioCreacion: string;

    // Lista de situaciones del adolescente
    declare listaSituaciones: SituacionActualAdolescenteDTO[];

    // Lista de factores presentes
    declare listaFactoresPresentes: FactoresPresentesDTO[];

    // Bandera para modo visualización
    declare esVisualizacion?: boolean;
}