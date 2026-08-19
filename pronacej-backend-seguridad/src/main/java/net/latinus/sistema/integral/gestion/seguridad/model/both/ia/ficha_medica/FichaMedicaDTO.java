package net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.FichaMedicaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadDTO;

import java.util.ArrayList;


@Data
public class FichaMedicaDTO {
    private String tokenIdentificador;

    private String tokenIdFichaIdentificacion;

    private String estadoSalud;

    private String lesiones;

    private String enfermedades;

    private String medicamentos;

    private String seguroMedico;

    private String institucionAcude;

    private String internadoHospital;

    private CatalogoDTO tipoSangre;

    private ArrayList<PersonaRelacionadaEnfermedadDTO> enfermedadesPersonasRelacionada;

    private ArrayList<String> tokensEnfermedadEliminar;

    private ArrayList<FichaMedicaEnfermedadDTO> enfermedadesRelacionadas;

    private ArrayList<String> tokensEnfermedadesFichaEliminar;

    private Boolean alergiaAlimentos;
    private String detalleAlergiasAlimentos;
    private Boolean alergiaMedicamentos;
    private String medicamentosAlergicos;
    private Boolean cirugiaQuirurgica;
    private String detalleCirugias;
    private Boolean fracturas;
    private String detalleFracturas;
    private String irs;
    private Boolean usoDePreservativo;

    private String relacionGenero;

    private String peso; // Detalla peso en kilogramos
    private String talla; // Detalla altura en metros
    private String aspectoGeneralFisico; // Detalla un aspecto general físico del adolescente
    private String inspeccion; // Detalla una inspección física del adolescente
    private String pielFaneras;
    private String icd;

    private String presion;
    private String saturacionOxigeno;
    private String indiceMasaCorporal;

    private String edadTabaco;
    private String edadAlcohol;
    private Boolean tomaAlcohol;
    private Boolean tabaco;
    private Boolean habitosNocivos;

    private String drogaInicio;

    private String cabezaDetalle;
    private String ojosDetalle;
    private String oidoDetalle;
    private String narizDetalle;
    private String bocaDetalle;
    private String orofaringeDetalle;
    private String corazonDetalle;
    private String pulmonesDetalle;
    private String abdomenDetalle;
    private String urinarioDetalle;
    private String pplDetalle;
    private String pruDetalle;
    private String impresionDiagnostico;
}
