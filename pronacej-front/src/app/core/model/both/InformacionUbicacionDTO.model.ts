import { CampoDTO } from "./campoDTO.model";

export class InformacionUbicacionDTO extends CampoDTO{

    declare idInformacionUbicacion: number;
    declare tipoInformacionUbicacion: string;
    declare idPersonaRelacionada: number;
    declare valor: string; 
    declare nombreTipoInformacion: string;

    declare id_temporal: number;

}