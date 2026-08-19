import { CatalogoDTO } from "../catalogoDTO.model"; 
import { CampoDTO } from "../campoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";
import { GestionFugaDTO } from "../GestionFugaDTO.model";
import { TrasladoDTO } from "../tras/TrasladoDTO.model";
import { PermisoSalidaDTO } from "./PermisoSalidaDTO.model";
import { ActaExternamientoDTO } from "../ia/actaExternamientoDTO.model";
import { InformeFinalAbiertoDTO } from "../informeFinalAbiertoDTO.model";

export class RegistroSalidaDTO extends CampoDTO{

   
    idRegistroSalida: number;
    tokenFichaIdentificacion: string;
    fechaHoraSalida: string | Date;
    fechaHoraRegreso?: string | Date;
    tipoSalidaLugar?: string;
    usuarioSalida?: string;
    nroDocumento?: string; 
    observaciones?: string;
    tipoSalida?: CatalogoDTO;
    motivoSalida?: CatalogoDTO;
    centroSalida?: JerarquiaDTO;
    actividades?: ActividadSalidaDTO[] = [];
    eventoFuga?: GestionFugaDTO;
    traslado?: TrasladoDTO;
    permisoSalida?:PermisoSalidaDTO;
    externamiento?: ActaExternamientoDTO;
    informeFinalAbierto?: InformeFinalAbiertoDTO;
    tokenIdentificadorAdolescente?: string;
    nombreMotivoSalida?: string;

  
}

export class ActividadSalidaDTO {
    descripcion: string;
}
