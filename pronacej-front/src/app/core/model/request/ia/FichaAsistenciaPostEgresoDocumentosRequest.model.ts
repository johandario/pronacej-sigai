import { PaginacionRequest } from "../PaginacionRequest.model";

export class FichaAsistenciaPostEgresoDocumentosRequest extends PaginacionRequest {
    declare tokenIdentificadorFichaAsistenciaPostEgreso: string;
    declare textoBuscar: string;
}