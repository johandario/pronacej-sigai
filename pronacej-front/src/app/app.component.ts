import { Component, LOCALE_ID, OnInit } from '@angular/core';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: true,
    imports: [RouterOutlet],
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
        { provide: LOCALE_ID, useValue: 'es' },
        { provide: MatPaginatorIntl, useValue: getEspPaginatorIntl() },
    ],
})
export class AppComponent implements OnInit {

    /**
     * Constructor
     */
    constructor() {
        //console.log(environment.URL_SERVICIOS);
    }

    ngOnInit(): void {
    }

}

export function getEspPaginatorIntl() {
    const paginatorIntl = new MatPaginatorIntl();

    paginatorIntl.itemsPerPageLabel = 'Elementos por página:';
    paginatorIntl.firstPageLabel = 'Ir al inicio';
    paginatorIntl.nextPageLabel = 'Siguiente';
    paginatorIntl.previousPageLabel = 'Anterior';
    paginatorIntl.lastPageLabel = 'Ir al final';
    // paginatorIntl.getRangeLabel = EspRangeLabel;

    paginatorIntl.getRangeLabel = (
        page: number,
        pageSize: number,
        length: number
    ) => {
        if (length === 0 || pageSize === 0) {
            return `0 / ${length}`;
        }
        length = Math.max(length, 0);
        const startIndex = page * pageSize;
        const endIndex =
            startIndex < length
                ? Math.min(startIndex + pageSize, length)
                : startIndex + pageSize;
        return `${startIndex + 1} - ${endIndex} de ${length}`;
    };

    return paginatorIntl;
}
