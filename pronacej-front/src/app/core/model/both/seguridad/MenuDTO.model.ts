export class MenuDTO {
    declare id: string;
    declare realizaAuditoria: boolean;
    declare title: string;
    declare subtitle: string;
    declare nemonico: string;
    declare type: string;
    declare mostrarEnFront: boolean;
    declare icon: string;
    declare link: string;
    declare esPadre: boolean;
    declare tokenIdentificadorPadre: string;
    declare tokenIdentificador: string;
    declare children: MenuDTO[];
    declare mostrarAccionesPermisos?: boolean;
}