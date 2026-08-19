import { CampoDTO } from "../campoDTO.model";
import { CampoInformeDTO } from "./campoInformeDTO.model";

export class PlantillaInformeDTO extends CampoDTO {
    idPlantillaInforme?: number;
    nombre: string;
    descripcion: string;
    nemonico?: string;
    nemonicoCentro?: string;
    tipoCentro?: string;
    campos: CampoInformeDTO[];
  }
  