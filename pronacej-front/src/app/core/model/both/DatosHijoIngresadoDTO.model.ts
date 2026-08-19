import { CampoDTO } from "./campoDTO.model";

export class DatosHijoIngresadoDTO extends CampoDTO {
    // Información personal
    declare hijoApellidoPaterno: string;
    declare hijoApellidoMaterno: string;
    declare hijoPrimerNombre: string;
    declare hijoSegundoNombre: string;
    declare hijoFechaNacimiento: Date;
    declare hijoDNI: string;
    declare hijoTipoSexo: string;

    // Agresión
    declare hijoVictimaAgresion: boolean;
    declare hijoEspecificarAgresion: string;

    // Moretones
    declare hijoMoretones: boolean;
    declare hijoEspecificarZonaMoretones: string;

    // Cicatrices
    declare hijoCicatrices: boolean;
    declare hijoEspecificarZonaCicatrices: string;

    // Tatuajes
    declare hijoTatuajes: boolean;
    declare hijoEspecificarZonaTatuajes: string;

    // Otros detalles
    declare hijoOtroEspecificar: boolean;
    declare hijoObservaciones: string;

    declare tokenIdentificadorPersonaRelacionada: string;

    // Token identificador de la ficha
    declare tokenIdentificadorFichaIdentificacion: string;
}