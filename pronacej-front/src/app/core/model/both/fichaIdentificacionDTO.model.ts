import { CampoDTO } from './campoDTO.model';
import { CatalogoDTO } from './catalogoDTO.model';
import { GestionFugaDTO } from './GestionFugaDTO.model';
import { ActaExternamientoDTO } from './ia/actaExternamientoDTO.model';
import { InformeFinalAbiertoDTO } from './informeFinalAbiertoDTO.model';
import { JerarquiaDTO } from './jerarquiaDTO.model';
import { PermisoSalidaDTO } from './salida/PermisoSalidaDTO.model';
import { TrasladoDTO, TrasladoAdolescenteDTO } from './tras/TrasladoDTO.model';

export class FichaIdentificacionDTO extends CampoDTO {
    declare idFichaIdentificacion: number;
    declare apellidoPaterno: string;
    declare apellidoMaterno: string;
    declare nombres: string;
    declare fechaNacimiento: string;
    declare edad: number;
    declare sexo: string;
    declare nombreSexo: string;
    declare alias: string;
    declare nacionalidad: string;
    declare sinDni: boolean;
    declare dni: string;
    declare tokenIdentificadorEstadoCivil: string;
    declare numeroHijos: number;
    declare tokenIdentificadorOrigenEtnico: string;
    declare impedimentoDiscapacidad: boolean;
    declare nombrePadre: string;
    declare nombreMadre: string;
    declare domicilioActual: string;
    declare direccion: string;

    // probablemente estos campos sean sustituidos por direccion mas adelante
    // declare tokenIdentificadorDepartamento: string;
    // declare tokenIdentificadorProvincia: string;
    // declare tokenIdentificadorDistrito: string;

    declare gradoInstruccion: string;
    declare ocupacion: string;
    declare viveCon: string;
    declare lugarNacimiento: string;
    declare fotoPerfil: string;
    declare fotoFrente: string;
    declare oficioInternamiento: boolean;
    declare sentenciaResolucion: boolean;
    declare dnifisico: boolean;
    declare fichaRENIEC: boolean;
    declare examenesMedicos: boolean;
    declare otrosEspecificar: string;
    declare tokenIdentificadorGrupoVulnerable: string;

    declare esEdicion: boolean;

    declare paisNacimiento: string;
    declare departamentoNacimiento: string;
    declare provinciaNacimiento: string;
    declare distritoNacimiento: string;

    declare ubigeoNacimiento: string;
    declare ubigeoUbicacion: string;
    declare tokenIdentificadorUbigeoDireccion: string;

    declare tipoDocumento: string;
    declare numeroDocumento: string;
    declare tipoSexo: string;
    declare tipoGenero: string;
    declare tipoViveCon: string;
    declare tipoGradoInstruccion: string;

    declare cantExpedientes: number;
    declare cantIngresos: number;
    declare cantPertenencias: number;
    declare fechaIngreso: Date;
    declare horaIngreso: string;
    declare juez: string;
    declare juzgado: string;
    declare centroIngreso: string;
    declare ingresahijos: boolean;
    declare observacionIngreso: string;
    declare otroOrigenEtnico: string;

    declare centro: JerarquiaDTO;
    declare tokensDocumentosIngreso: string[];

    declare crearFichaIngreso: Boolean;
    declare numeroFojas: string;

    declare corteJusticia: CatalogoDTO;
    declare instancia: CatalogoDTO;
    declare especialidad: CatalogoDTO;
    declare organoJurisdiccional: string;
    declare secretario: string;

    declare modalidadEstudio: string;
    declare nivelEBR: string;
    declare nivelSuperior: string;
    declare nivelEBA: string;

    declare esVisualizacion: boolean;

    declare tipoEstadoCivil: string;
    declare nombreTipoDocumento: string;

    declare tipoEntrada: CatalogoDTO;

    declare fuga: GestionFugaDTO;
    declare traslado: TrasladoDTO;
    declare trasladoAdolescente: TrasladoAdolescenteDTO
    declare permisoSalida: PermisoSalidaDTO;

    declare estadoAdolescente: CatalogoDTO;

    declare actaExternamiento: ActaExternamientoDTO;
    declare informeFinalAbierto: InformeFinalAbiertoDTO;
    declare numeroIdentificacion: string;
    declare permisoTemporal: boolean;
    declare tieneProceso: boolean;

    declare email: string;
}

export class FichaIdentificacionResumenDTO {
    declare nombreCompleto: string;
    declare numeroIdentificacion: string;
    declare centro: string;
    declare estado: string;
    declare tokenIdentificador: string;
}
