import { CampoDTO } from "../campoDTO.model";
import { SeccionDTO } from "./seccionDTO.model";


export class EncuestaDTO extends CampoDTO {
    idEncuesta?: number;
    nombre: string;
    descripcion?: string;
    seccionesOrdenadas: boolean;
    idJerarquia: number;
    nemonico?: string;
    nemonicoCentro?: string;
    tipoCentro?: string;
    nemonicoCategoria?: string;
    categoria?: string;
    adolescente?: string;
    dniAdolescente?: string;
    edadAdolescente?: number;
    fechaNacimientoAdolescente?: Date;
    correlativo?: number;
    establecimiento?: string;
    fechaRegistro?: Date;
    evaluador?: string;
    fechaEvaluacion?: Date;
    secciones: SeccionDTO[];
    completada?: boolean;
    tokenIdentificadorValoracionFinal?: string;
    nombreValoracionFinal?: string;
    justificacionValoracion?: string;
    fechaValoracion?: Date;
  }
  