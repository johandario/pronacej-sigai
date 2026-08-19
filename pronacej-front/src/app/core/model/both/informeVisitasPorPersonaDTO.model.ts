import { CampoDTO } from "./campoDTO.model";
import { InformeVisitasDTO } from "./informeVisitasDTO.model";

export class InformeVisitasPorPersonaDTO extends CampoDTO {  
    declare tokenIdentificadorFichaPrincipal: string;
    declare listaInformeVisitas: InformeVisitasDTO[];
}