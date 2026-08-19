import { ReseteoDePasswordRequest } from "../request/ReseteoDePasswordRequest.model";

export class ActualizarDatosDeSeguridadDTO extends ReseteoDePasswordRequest {

    declare passwordActual: string;

    declare habilitar2DoFactorDeAutenticacion: boolean;
    declare cambioDeContraseniaCadaNDias: boolean;

    declare diasExpiracionContrasenia: number;
}