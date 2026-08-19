import { CampoDTO } from "./campoDTO.model";


export class DatosFamiliaresDTO extends CampoDTO{

    declare tipoFamilia: string;
    declare organizacionFamiliar: string;
    declare ejercicioAutoridad: string;
    declare entornoFamiliar: string;

    // Booleanos S/N
    declare relacionIntraFamiliarPadres: string;
    declare relacionIntraFamiliarFilial: string;
    declare relacionIntraFamiliarParentales: string;
    declare relacionIntraFamiliarPareja: string;
    declare partidaNacimiento: string;
    // declare bautismo: string;
    // declare primeraComunion: string;
    // declare confirmacion: string;

    declare observacionesRelacionIntrafamiliar: string;
    declare causaAusenciaPadres: string;
    declare religion: string;

    declare otroSacramento: string;
    declare tokenIdentificadorFicha: string;

    declare tipoSacramento: string;

}