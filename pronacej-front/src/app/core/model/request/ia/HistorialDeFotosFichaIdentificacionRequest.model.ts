import { PaginacionRequest } from "../PaginacionRequest.model";

export class HistorialDeFotosFichaIdentificacionRequest extends PaginacionRequest {
    declare filtroBusqueda: string;
    declare tokenIdentificadorFichaDeIdentificacion: string;
}