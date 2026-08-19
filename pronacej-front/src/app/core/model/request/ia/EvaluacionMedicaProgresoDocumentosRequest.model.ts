import { PaginacionRequest } from "../PaginacionRequest.model";

export class EvaluacionMedicaProgresoDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorEvaluacionMedicaProgreso: string;
    declare textoBuscar: string;
}