import { CampoDTO } from "../campoDTO.model";

export class RelacionEgresoDTO extends CampoDTO {
    numExpediente: string;
    nombres: string;
    apellidoPaterno: string;
    apellidoMaterno: string;
    tipoDocumento: string;
    numDocumento: string;
    tokenExpediente: string;
    tokenFichaIdentificacion: string;
  }
  