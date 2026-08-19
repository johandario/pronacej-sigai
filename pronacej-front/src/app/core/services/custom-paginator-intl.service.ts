import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';

@Injectable(
    {
        providedIn: "root"
    }
)

export class CustomPaginatorIntl extends MatPaginatorIntl {
  itemsPerPageLabel = 'Elementos por página'; // Personaliza el texto
  nextPageLabel = 'Siguiente página'; // Personaliza el texto
  previousPageLabel = 'Página anterior'; // Personaliza el texto
  firstPageLabel = 'Primera página'; // Personaliza el texto
  lastPageLabel = 'Última página'; // Personaliza el texto

  // Personaliza el texto de la paginación
  getRangeLabel = (page: number, pageSize: number, length: number) => {
    if (length === 0 || pageSize === 0) {
      return `0 de ${length}`;
    }
    length = Math.max(length, 0);
    const startIndex = page * pageSize + 1;
    // Si el índice de inicio está fuera de los límites
    const endIndex = Math.min(startIndex + pageSize - 1, length);
    return `${startIndex} - ${endIndex} de ${length}`;
  };
}
