import { PaginacionRequest } from "../PaginacionRequest.model";

export class PertenenciaDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorPertenencia: string;
    declare textoBuscar: string;
}