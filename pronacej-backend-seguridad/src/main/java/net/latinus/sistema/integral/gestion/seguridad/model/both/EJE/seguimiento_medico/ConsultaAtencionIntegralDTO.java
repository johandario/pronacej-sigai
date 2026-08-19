package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultaAtencionIntegralDTO extends CamposDTO implements Serializable {

    private String tokenIdFichaMedica;

    private Date fechaInicio;

    private String observaciones;

    private String motivoConsulta;

    private String edad;

    private String tipoEnfermedad;

    private String formaDeInicio;

    private String estadoDeAnimo;

    private Boolean sed;

    private Boolean sueno;

    private Boolean apetito;

    private String orina;

    private String deposiciones;

    private String fiebre15dias;

    private String tos15dias;

    private String secrecionGenitales;

    private String perdidaPeso;

    private String peso;

    private String talla;

    private String presion;

    private String imc;

    private String temperatura;

    private String diagnostico;

    private String tratamiento;

    private String examenesAuxiliares;

    private Date fechaProximaCita;

    private String tiempoEnfermedad;

    private RecetaDTO receta;

    private OrdenMedicaDTO orden;

    private String doctorAtencion;

    private String lugarAtencion;
}