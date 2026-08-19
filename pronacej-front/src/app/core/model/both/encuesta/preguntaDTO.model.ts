import { ContestacionDTO } from "./contestacionDTO.model";
import { RespuestaDTO } from "./respuestaDTO.model";

export class PreguntaDTO {
    idPregunta?: number;
    texto: string;
    categoria: string;
    orden?: number;
    requerido: boolean;
    respuestasOrdenadas: boolean;
    tieneObservaciones: boolean;
    permiteDocumentos: boolean;
    respuestas?: RespuestaDTO[];
    contestaciones?: ContestacionDTO[];
  }
  