import { PaginacionRequest } from "../PaginacionRequest.model";

export class ExpedienteMatrizDetalleDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorExpedienteDetalle: string;
    declare textoBuscar: string;
}