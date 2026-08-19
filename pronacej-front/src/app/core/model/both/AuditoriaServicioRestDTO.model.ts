import { CampoDTO } from "./campoDTO.model";

export class AuditoriaServicioRestDTO extends CampoDTO{
    declare accept: string;
    declare acceptLanguage: string;
    declare contentLength: number;
    declare contentType: string;
    declare fechaRequest: Date;
    declare fechaResponse: Date;
    declare headerAuthorization: string;
    declare headersJson: string;
    declare host: string;
    declare jsonRequest: string;
    declare jsonResponse: string;
    declare origin: string;
    declare platform: string;
    declare referer: string;
    declare tipoDeMetodo: string;
    declare url: string;
    declare userAgent: string;
}