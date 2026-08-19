export class Paginacion {
    pageIndex: number;
    pageSizeOptions: number[];
    pageSize: number;
    totalItems: number;

    constructor() {
        this.pageIndex = 0;
        this.pageSizeOptions = [5, 10, 15, 20];
        this.pageSize = this.pageSizeOptions[0];
        this.totalItems = 0;
    }
}