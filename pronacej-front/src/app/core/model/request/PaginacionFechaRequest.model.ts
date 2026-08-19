import { PaginacionRequest } from "./PaginacionRequest.model";

export class PaginacionFechaRequest extends PaginacionRequest {

    declare fechaInicio: Date;
    declare fechaFin: Date;
}