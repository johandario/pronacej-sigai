import { CampoDTO } from './campoDTO.model';
import { JerarquiaDTO } from './jerarquiaDTO.model';
import { UbicacionJerarquiaDTO } from "./ubicacionJerarquiaDTO.model";

export class FichaUbicacionDTO extends CampoDTO {
    declare tokenIdentificadorFichaIdentificacion: string;
    declare fechaIngreso: Date;
    declare ubicacionActual: boolean;
    declare ubicacionJerarquia: UbicacionJerarquiaDTO;
    declare centro: JerarquiaDTO;
    declare numeroCama: number;
    declare atencionPrioritaria: boolean;
    declare ingresoExpediente: boolean;
    declare observaciones: string;

    private celdaActualTexto?: string;
    private centroActualTexto?: string;
}