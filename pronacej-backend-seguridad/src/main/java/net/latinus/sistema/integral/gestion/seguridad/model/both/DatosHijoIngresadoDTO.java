package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatosHijoIngresadoDTO extends CamposDTO implements Serializable {

    private String tokenIdentificadorFichaIngreso;
    private String tokenIdentificadorPersonaRelacionada;
    private String hijoApellidoPaterno;
    private String hijoApellidoMaterno;
    private String hijoPrimerNombre;
    private String hijoSegundoNombre;
    private Date hijoFechaNacimiento;
    private String hijoDNI;
    private String hijoTipoSexo;
    private String hijoOcupacion;
    private String hijoParentesco;
    private String hijoTelefono;
    private String hijoEstadoCivil;
    private String hijoInstruccion;
    private String hijoRoles;
    private Boolean hijoVictimaAgresion;
    private String hijoEspecificarAgresion;
    private Boolean hijoMoretones;
    private String hijoEspecificarZonaMoretones;
    private Boolean hijoCicatrices;
    private String hijoEspecificarZonaCicatrices;
    private Boolean hijoTatuajes;
    private String hijoEspecificarZonaTatuajes;
    private String hijoOtroEspecificar;
    private String hijoObservaciones;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}