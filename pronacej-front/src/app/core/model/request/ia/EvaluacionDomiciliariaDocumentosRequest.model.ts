import { PaginacionRequest } from "../PaginacionRequest.model";

export class EvaluacionDomiciliariaDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorEvaluacionDomiciliaria: string;
    declare textoBuscar: string;
}