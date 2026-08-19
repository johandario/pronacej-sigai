import { PaginacionRequest } from "../PaginacionRequest.model";

export class FichaIngresoDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorFichaIngreso: string;
    declare textoBuscar: string;
}