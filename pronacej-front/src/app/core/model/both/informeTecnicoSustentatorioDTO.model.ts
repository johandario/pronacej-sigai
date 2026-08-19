import { CampoDTO } from "./campoDTO.model";

export class InformeTecnicoSustentatorioDTO extends CampoDTO {
    declare motivo: string;
    declare criteriosSeleccion: string;
    declare analisisPsicologico: string;
    declare analisisSocial: string;
    declare analisisConductual: string;
    declare analisisFamiliar: string;
    declare propuestaActividadFormativa: string;
    declare importanciaParticipacionAdolescente: string;
    declare objetivosConseguir: string;
    declare duracion: number;
    declare conclusiones: string;
    declare recomendaciones: string;
    declare tokenIdentificadorEstado: string;

    // Campos adicionales para la tabla
    declare nombreCompletoUsuarioCreacion: string;

    // Campo auxiliar para control de visualización
    declare esVisualizacion?: boolean;
}