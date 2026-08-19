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
    secciones: SeccionDTO[];
  }
  