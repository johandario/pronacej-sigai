import { CatalogoDTO } from "../../catalogoDTO.model";

export class DiagnosticoDTO{
    declare tokenIdentificador? : string;
    declare tokenIdEvaluacionMedica: string;

    declare codDiagnostico: string;
    declare diagnostico: string;
    declare tratamiento: string;
    declare indicaciones: string;
    declare examenes: string;
    declare medicamentos: string;

    declare tipoDiagnostico: CatalogoDTO;
}