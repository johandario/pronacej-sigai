package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.InformePermisoSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class FichaIdentificacionDTO extends CamposDTO implements Serializable {

    private Long idFichaIdentificacion;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private Date fechaNacimiento;
    private Integer edad;
    private char sexo;
    private String alias;
    private String nacionalidad;
    private Boolean sinDni;
    private String dni;
    private String tokenIdentificadorEstadoCivil;
    private Integer numeroHijos;
    private String tokenIdentificadorOrigenEtnico;
    private String nombrePadre;
    private String nombreMadre;
    private String domicilioActual;
    private String direccion;
    /*
    private String tokenIdentificadorDepartamento;
    private String tokenIdentificadorProvincia;
    private String tokenIdentificadorDistrito;
    */
    private String ocupacion;
    private String viveCon;
    private String lugarNacimiento;
    private String fotoPerfil;
    private String fotoFrente;
    private Boolean oficioInternamiento;
    private Boolean sentenciaResolucion;
    private Boolean dniFisico;
    private Boolean fichaRENIEC;
    private Boolean examenesMedicos;
    private String otrosEspecificar;
        
    private String tokenIdentificadorGrupoVulnerable;
    private Boolean impedimentoDiscapacidad;

    private String paisNacimiento;
    private String departamentoNacimiento;
    private String provinciaNacimiento;
    private String distritoNacimiento;

    private String ubigeoNacimiento;
    private String ubigeoUbicacion;
    private String tokenIdentificadorUbigeoDireccion;

    private String tipoDocumento;
    private String numeroDocumento;
    private String tipoSexo;
    private String tipoGenero;
    private String tipoViveCon;

    private Integer cantIngresos;
    private Integer cantExpedientes;
    private Integer cantPertenencias;

    private Date fechaIngreso; // Fecha de ingreso
    private String horaIngreso; // Hora de ingreso
    private String juez; // Juez de ingreso
    private String juzgado; // Juzgado de ingreso
    private String centroIngreso; // Centro de ingreso
    private Boolean ingresahijos; // Indica si ingresa con hijos
    private String observacionIngreso;
    private String otroOrigenEtnico;

    private String numeroFojas;
    private ArrayList<String> tokensDocumentosIngreso;
    private JerarquiaDTO centro;

    private Boolean crearFichaIngreso = false;

    private CatalogoDTO corteJusticia;
    private CatalogoDTO instancia;
    private CatalogoDTO especialidad;
    private String organoJurisdiccional;
    private String secretario;
    private String modalidadEstudio;
    private String nivelEBR;
    private String nivelSuperior;
    private String nivelEBA;

    private String tipoEstadoCivil;
    private String nombreTipoDocumento;

    private RegistroSalidaDTO registroSalidaDTO;

    private CatalogoDTO tipoEntrada;

    private EventoFugaDTO fuga;
    private TrasladoDTO traslado;
    private InformePermisoSalidaDTO permisoSalida;
    private TrasladoAdolescenteDTO trasladoAdolescente;
    private InformeFinalAbiertoDTO informeFinalAbierto;
    private ActaExternamientoDTO actaExternamiento;

    private CatalogoDTO estadoAdolescente;
    private String numeroIdentificacion;

    private Boolean permisoTemporal;
    private Boolean tieneProceso;
    private String email;
    private String nombreSexo;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
