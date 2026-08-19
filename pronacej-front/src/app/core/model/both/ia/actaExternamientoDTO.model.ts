import { CampoDTO } from "../campoDTO.model";
import { ActaExternamientoDocumentoDTO } from "./actaExternamientoDocumentoDTO.model";

export class ActaExternamientoDTO extends CampoDTO {
    idActaExternamiento?: number;
    fechaRegistro: Date;
    ingreso: string;
    institucion: string;
    autorizacion: string;
    tipoDocumento: string;
    nemonicoTipoDocumento: string;
    numeroDocumento: string;
    resolucion: string;
    domicilio: string;
    mandatoDetencion: Boolean;
    retiroSolo: Boolean;
    familiares: string;
    parentescos: string;
    identificaciones: string;
    direcciones: string;
    telefonos: string;
    observaciones: string;
    impreso: Boolean;
    firmado: Boolean;
    tokenFichaIdentificacion: string;
    tokenExpedienteMatriz: string;
    numeroExpedienteMatriz: string;
    actaExternamientoDocumentoDTO: ActaExternamientoDocumentoDTO;
    isComplete: Boolean;
}
