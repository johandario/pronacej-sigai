import { PaginacionRequest } from "../PaginacionRequest.model";

export class EvaluacionDocumentoRequest extends PaginacionRequest {
    declare tokenEvaluacion?: string;
    declare nemonicoCarpeta: string;
}