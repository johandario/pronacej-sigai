import { PaginacionRequest } from "./PaginacionRequest.model";

export class PaginacionRequestFichaIdentificacion extends PaginacionRequest {

    declare tokenCentro?: string;
    declare todosEstados: boolean;
    declare postEgreso: boolean;

}