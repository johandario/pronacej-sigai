import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';

export type UbicacionNodeType = 'centro' | 'ubicacion';

export const UBICACION_TIPO_CELDA = 'UBICACION_TIPO_CELDA';

export interface UbicacionNodoItem {
    id: string;
    name: string;
    description?: string;
    tipoNombre?: string;
    nodeType: UbicacionNodeType;
    hasChild: boolean;
    isLeaf: boolean;
    isReadonly: boolean;
    raw: JerarquiaDTO | UbicacionJerarquiaDTO;
}

export interface UbicacionBreadcrumb {
    id: string;
    name: string;
    nodeType: UbicacionNodeType;
}

export interface UbicacionLevelData {
    title: string;
    entries: UbicacionNodoItem[];
    breadcrumbs: UbicacionBreadcrumb[];
    contextParent?: UbicacionNodoItem;
}

export interface UbicacionSavePayload {
    nombre: string;
    nombreCorto?: string;
    descripcion?: string;
    jerarquiaTipo?: JerarquiaDTO;
    tipoSexo?: CatalogoDTO;
    atencionPrioritaria?: CatalogoDTO;
    tipoUbicacion?: CatalogoDTO;
    rangoInicio?: number;
    rangoFin?: number;
}
