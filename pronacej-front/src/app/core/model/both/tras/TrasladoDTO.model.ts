import { CampoDTO } from "../campoDTO.model";
import { CatalogoDTO } from "../catalogoDTO.model";
import { FichaIdentificacionDTO } from "../fichaIdentificacionDTO.model";
import { InstanciaProcesoDTO } from "../flujo/InstanciaProcesoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class TrasladoDTO extends CampoDTO {
  idTraslado: number;
  numTraslado: number;
  centroOrigen: JerarquiaDTO;
  centroDestino: JerarquiaDTO;
  motivoTraslado: CatalogoDTO;
  antecedentes: string;
  analisis: string;
  conclusiones: string;
  recomendaciones: string;
  descripcionSolicitud: string;
  comentarioRechazo: string;
  instanciaProceso: InstanciaProcesoDTO;
  tokenProceso?: string;
  trasladoAdolescentes: TrasladoAdolescenteDTO[];
  html?: string;
  instanciaProcesoDTO: InstanciaProcesoDTO;
  completado?:boolean;
  usuarioCreaTraslado?: string;
}

export class TrasladoAdolescenteDTO {
  fichaIdentificacion: FichaIdentificacionDTO;
  isComplete?:boolean;
  estadoEvento?: CatalogoDTO;
  idTrasladoAdolescente: number;
  completado?:boolean;
}