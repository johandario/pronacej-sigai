import { DocumentoDTOTabla } from "app/core/components/documentos/documentos-subidos-tabla/documentoDTOTabla.model";

export class DocumentoDTOTablaFicha extends DocumentoDTOTabla {
    declare documentoDe: string;

    declare tokenFichaPrincipalDocumento:string;
}