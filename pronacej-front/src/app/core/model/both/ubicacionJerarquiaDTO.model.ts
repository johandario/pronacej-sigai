import { CatalogoDTO } from './catalogoDTO.model';
import { CampoDTO } from './campoDTO.model';
import { JerarquiaDTO } from './jerarquiaDTO.model';

export class UbicacionJerarquiaDTO extends CampoDTO {
    declare ubicacionJerarquiaPadre?: UbicacionJerarquiaDTO;
    declare jerarquiaTipo?: JerarquiaDTO;
    declare jerarquiaCentro?: JerarquiaDTO;
    declare nombre: string;
    declare nombreCorto?: string;
    declare descripcion?: string;
    declare tipoSexo?: CatalogoDTO;
    declare atencionPrioritaria?: CatalogoDTO;
    declare tipoUbicacion?: CatalogoDTO;
    declare rangoInicio?: number;
    declare rangoFin?: number;
}
