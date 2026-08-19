import { CampoDTO } from "./campoDTO.model";

export class ReportesDTO extends CampoDTO{
    declare nemonicoTipoSexo: string;
    declare tokenIdentificadorCentro: string;
    declare nemonicoCentro: string;
}

export class AdolescenteExternadoDTO {
    nombreCompleto: string;
    numeroIdentificacion: string;
    centro: string;
    numeroExpediente: string;
    fechaIngreso: string;
    fechaSalida: string;
    motivoIngreso: string;
    motivoSalida: string;
    observacionIngreso: string;
    observacionSalida: string;
}