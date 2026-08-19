import { PaginacionRequest } from "../PaginacionRequest.model";

export class FichaPrincipalDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorFichaIdentificacion: string;
    declare textoBuscar: string;
}