import { CatalogoDTO } from "./catalogoDTO.model"; 
import { CampoDTO } from "./campoDTO.model";
import { InstanciaProcesoDTO } from "./flujo/InstanciaProcesoDTO.model";
import { JerarquiaDTO } from "./jerarquiaDTO.model";

export class GestionFugaDTO extends CampoDTO{

    tokenInstancia?: string;
    idFuga: number;
    tokenFichaIdentificacion: string;
    fechaRegistro: string; 
    fechaFuga: string | Date;
    fechaInformeDirector: string;
    fechaInformeApoderado: string;
    presenciaDe: string; 
    parentesco: CatalogoDTO;
    accionesRealizadas: string;
    descripcionHechos: string; 
    dirigidoA: string; 
    asunto: CatalogoDTO; 
    de: string; 
    apoderado: string; 
    instanciaProceso: InstanciaProcesoDTO;
    tokenProceso?: string;
    dni?: string;
    html?: string;
    isComplete?:boolean;
    estadoEvento?: CatalogoDTO;
    centro?: JerarquiaDTO;
    numFuga?: string;
    numeroIdentificacion?: string;
    nombreAdolescente?: string;
    ultimoPaso?:boolean;
    fechaNacimiento: string | Date;
   
    
   
    
   
   
     
}