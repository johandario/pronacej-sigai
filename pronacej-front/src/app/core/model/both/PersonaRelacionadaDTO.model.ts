import { CampoDTO } from "./campoDTO.model";
import { InformacionUbicacionDTO } from "./InformacionUbicacionDTO.model";


export class PersonaRelacionadaDTO extends CampoDTO {
    declare apellidoPaterno: string;
    declare apellidoMaterno: string;
    declare nombres: string;
    declare primerNombre: string;
    declare segundoNombre: string;
    declare primerApellido: string;
    declare segundoApellido: string;
    declare tipoIdentificacion: string;
    declare numeroDocumento: string;
    declare modalidadEstudio: string;
    declare nivelEBR: string;
    declare nivelSuperior: string;
    declare nivelEBA: string;
    declare tipoOcupacion: string;
    declare estadoCivil: string;
    declare observaciones: string;
    declare parentesco: string;
    declare tipoParentesco: string;
    declare fechaNacimiento: Date;
    declare tipoSexo: string;
    declare telefono: string;

    declare tokenIdentificadorEvaluacionSocial: string;
    declare tokenIdentificadorCondicionLaboral: string;
    declare otros: string;
    declare ingresoPromedio: number;
    declare numeroHijos: number;
    declare esResponsableEconom: boolean;

    declare ocupacion: string;
    declare idPersonaRelacionada: number;

    declare esTutor: string;
    declare visitaAutorizada: string;
    declare fallecido: string;

    declare informacionUbicaciones: InformacionUbicacionDTO[];
    declare informacionUbicacionesEliminar: string[];

    declare tokenIdentificadorFicha: string;

    declare rolesInfluencias: string;
    declare relacionAfectiva: string;

    declare enfermo: Boolean;
}

