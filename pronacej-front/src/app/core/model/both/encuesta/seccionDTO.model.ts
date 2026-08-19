import { PreguntaDTO } from "./preguntaDTO.model";

export class SeccionDTO {
  idSeccion?: number;
  nombre: string;
  orden: number;
  preguntasOrdenadas: boolean;
  tienePuntuacion: boolean;
  preguntas?: PreguntaDTO[];
}