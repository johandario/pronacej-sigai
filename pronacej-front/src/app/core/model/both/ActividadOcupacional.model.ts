import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";
import { JerarquiaDTO } from "./jerarquiaDTO.model";


export interface ActividadOcupacionalDTO extends CampoDTO {
  fechaInicio?: Date;
  tipoActividadOcupacional?: CatalogoDTO;
  tipoDocumentoAprobacion?: CatalogoDTO;
  estadoActividadOcupacional?: CatalogoDTO;
  tipoPrograma?: CatalogoDTO;
  objetivoActividad?: string;
  numeroDocumento?: string;
  tokenFichaIdentificacion?: string;
  nombrePrograma: string;
  programa: JerarquiaDTO;
  ambiente: JerarquiaDTO;

  documentoAprobacion: string;
  esVisualizacion: boolean;
}
