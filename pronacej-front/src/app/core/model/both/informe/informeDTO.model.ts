import { CampoDTO } from "../campoDTO.model";
import { InformeDocumentoDTO } from "./informeDocumentoDTO.model";
import { ValorInformeDTO } from "./valorInformeDTO.model";

export class InformeDTO extends CampoDTO {
    idInforme?: number;
    fechaRegistro?: Date;
    asignado?: string;
    tipo?: string;
    impreso?: boolean;
    firmado?: boolean;
    idFichaIdentificacion?: number;
    tokenFichaIdentificacion?: string;
    idPlantillaInforme?: number;
    nemonicoPlantillaInforme?: string;
    idInformePadre?: number;
    valores: ValorInformeDTO[];
    informeDocumentoDTO: InformeDocumentoDTO;
  }
  