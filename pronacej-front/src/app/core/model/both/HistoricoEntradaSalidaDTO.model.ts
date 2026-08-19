import { CampoDTO } from './campoDTO.model';
import { CatalogoDTO } from './catalogoDTO.model';
import { GestionFugaDTO } from './GestionFugaDTO.model';
import { ActaExternamientoDTO } from './ia/actaExternamientoDTO.model';
import { InformeFinalAbiertoDTO } from './informeFinalAbiertoDTO.model';
import { JerarquiaDTO } from './jerarquiaDTO.model';
import { PermisoSalidaDTO } from './salida/PermisoSalidaDTO.model';
import { RegistroSalidaDTO } from './salida/RegistroSalidaDTO.model';
import { TrasladoAdolescenteDTO, TrasladoDTO } from './tras/TrasladoDTO.model';

export class HistoricoEntradaSalidaDTO extends CampoDTO {

    declare fechaSalida: Date;
    declare centroSalida: JerarquiaDTO;
    declare centroIngreso: JerarquiaDTO;
    declare fuga: GestionFugaDTO;
    declare traslado: TrasladoDTO;
    declare trasladoAdolescente: TrasladoAdolescenteDTO
    declare permisoSalida: PermisoSalidaDTO;
    declare motivoSalida: CatalogoDTO;
    declare registroSalida: RegistroSalidaDTO
    declare actaExternamiento: ActaExternamientoDTO;
    declare informeFinalAbierto: InformeFinalAbiertoDTO
}