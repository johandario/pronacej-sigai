export class PaginacionRequest {
    declare page: number;
    declare size: number;
    declare sort: string;
    declare direction: string;
    declare filter: string;
    declare tokenIdentificador?: string;
}