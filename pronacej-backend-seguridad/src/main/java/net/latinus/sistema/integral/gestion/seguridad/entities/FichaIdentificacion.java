package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;

@Entity
@Data
@Table(name = "ia_ficha_identificacion")
@EqualsAndHashCode(of = {"idFichaIdentificacion"}, callSuper = true)
public class FichaIdentificacion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idFichaIdentificacion;

    @Column(columnDefinition = "varchar(128)")
    @Comment("apellido paterno")
    private String apellidoPaterno;

    @Comment("apellido materno")
    @Column(columnDefinition = "varchar(128)")
    private String apellidoMaterno;

    @Comment("nombres")
    @Column(columnDefinition = "varchar(128)")
    private String nombres;

    @Comment("fecha de nacimiento")
    @Column(columnDefinition = "timestamp")
    private Date fechaNacimiento;

    @Comment("edad")
    @Column(columnDefinition = "int")
    private Integer edad;

    @Comment("sexo")
    @Column(columnDefinition = "char(1)")
    private Character sexo;

    @Comment("alias")
    @Column(columnDefinition = "varchar(32)")
    private String alias;

    @Comment("nacionalidad")
    @Column(columnDefinition = "varchar(32)")
    private String nacionalidad;

    @Comment("sinDni")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean sinDni;

    @Comment("dni")
    @Column(columnDefinition = "varchar(16)")
    private String dni;

    @Comment("estado civil")
    @JoinColumn(name = "estado_civil", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estadoCivil;

    @Comment("numero de hijos")
    @Column(columnDefinition = "int")
    private Integer numeroHijos;

    @Comment("origden etnico")
    @JoinColumn(name = "origen_etnico", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo origenEtnico;

    @Comment("impedimento discapacidad")
    private Boolean impedimentoDiscapacidad;

    @Comment("nombre del padre")
    @Column(columnDefinition = "varchar(128)")
    private String nombrePadre;

    @Comment("nombre de la madre")
    @Column(columnDefinition = "varchar(128)")
    private String nombreMadre;

    @Comment("domicilio actual")
    @Column(columnDefinition = "varchar(128)")
    private String domicilioActual;

    @Comment("direccion")
    @Column(columnDefinition = "TEXT")
    private String direccion;
    
    /*
    @JoinColumn(name = "departamento", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo departamento;
    
    @JoinColumn(name = "provincia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo provincia;
    
    @JoinColumn(name = "distrito", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo distrito;
    */

    @Comment("grado instruccion")
    @Column(columnDefinition = "varchar(128)")
    private String gradoInstruccion;

    @Comment("ocupacion")
    @Column(columnDefinition = "varchar(128)")
    private String ocupacion;

    @Comment("vive con")
    @Column(columnDefinition = "varchar(64)")
    private String viveCon;

    @Comment("lugar de nacimiento")
    @Column(columnDefinition = "varchar(56)")
    private String lugarNacimiento;

    /*
    @Column(columnDefinition= "text")
    private String fotoPerfil;
    
    @Column(columnDefinition= "text")
    private String fotoFrente;
     */

    @Comment("oficio internamiento")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean oficioInternamiento;

    @Comment("sentencia resolucion")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean sentenciaResolucion;

    @Comment("dni fisico")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean dniFisico;

    @Comment("ficha reniec")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean fichaRENIEC;

    @Comment("examenes medicos")
    @Column(columnDefinition = "boolean default' false'")
    private Boolean examenesMedicos;

    @Comment("otros especificar")
    @Column(columnDefinition = "varchar(128)")
    private String otrosEspecificar;

    @Comment("grupo vulnerable")
    @JoinColumn(name = "grupo_vulnerable", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo grupoVulnerable;

    @Comment("id del estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Comment("departamento de nacimiento no peru")
    private String departamentoDeNacimientoNoPeru;

    @Comment("provincia de nacimiento no peru")
    private String provinciaDeNacimientoNoPeru;

    @Comment("distrito de nacimiento no peru")
    private String distritoDeNacimientoNoPeru;

    @Comment("pais nacimiento")
    @ManyToOne
    @JoinColumn(name = "id_pais_nacimiento", referencedColumnName = "idLocalidad")
    private Localidad paisNacimiento;

    @Comment("departamento nacimiento")
    @ManyToOne
    @JoinColumn(name = "id_departamento_nacimiento", referencedColumnName = "idLocalidad")
    private Localidad departamentoNacimiento;

    @Comment("provincia nacimiento")
    @ManyToOne
    @JoinColumn(name = "id_provincia_nacimiento", referencedColumnName = "idLocalidad")
    private Localidad provinciaNacimiento;

    @Comment("distrito nacimiento")
    @ManyToOne
    @JoinColumn(name = "id_distrito_nacimiento", referencedColumnName = "idLocalidad")
    private Localidad distritoNacimiento;

    @Comment("codigo ubigeo nacimiento")
    private String codigoUbigeoNacimiento;
    
    @Comment("ubigeo direccion")
    @JoinColumn(name = "ubigeo_direccion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo ubigeoDireccion;

    @Comment("codigo ubigeo direccion")
    private String codigoUbigeoDireccion;

    @Comment("numero identificacion")
    @Column(columnDefinition = "varchar(16)")
    private String numeroIdentificacion;

    @Comment("tipo de identificacion")
    @JoinColumn(name = "tipo_identificacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoIdentificacion;

    @Comment("tipo sexo")
    @JoinColumn(name = "tipo_sexo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoSexo;

    @Comment("vive con parentesco")
    @JoinColumn(name = "vive_con_parentesco", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo viveConParentesco;

    @Comment("id del genero")
    @JoinColumn(name = "tipo_genero", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo genero;

    @Comment("tipo ocupacion")
    @JoinColumn(name = "id_tipo_ocupacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoOcupacion;

    @Column(columnDefinition = "timestamp")
    @Comment("fecha de ingreso al centro")
    private Date fechaIngreso;

    @Comment("hora de ingreso al centro")
    private String horaIngreso;

    @Comment("centro de ingreso")
    @JoinColumn(name = "id_centro", referencedColumnName = "idJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    private Jerarquia centroIngreso;

    @Comment("juez encargado")
    private String juez;

    @Comment("juzgado encargado")
    private String juzgado;

    @Comment("numero de documentos al ingreso")
    @Column(columnDefinition = "int")
    private Long numeroDocumentosIngreso;

    @Comment("numero de documentos fojas al ingreso")
    @Column(columnDefinition = "int")
    private Long numeroFojas;

    @Comment("ingreso con hijos")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean ingresoConHijo;

    @Comment("observaciones en el ingreso")
    @Column(columnDefinition = "TEXT")
    private String observacionIngreso;
    
    @Comment("otro origen etnico")
    private String otroOrigenEtnico;

    @JoinColumn(name = "id_catalogo_corte_justicia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Corte superior de justicia")
    private Catalogo corteJusticia;

    @JoinColumn(name = "id_catalogo_instancia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Corte superior de justicia")
    private Catalogo instancia;

    @JoinColumn(name = "id_catalogo_especialidad", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Corte superior de justicia")
    private Catalogo especialidad;

    @Comment("Juzgado al que pertenece")
    private String organoJurisdiccional;

    @Comment("Secretario judicial especialista legal")
    private String secretario;
    
    @Comment("modalidad estudio")
    @Column(columnDefinition = "varchar(32)")
    private String modalidadEstudio;

    @Comment("nivel EBR")
    @JoinColumn(name = "nivel_ebr", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo nivelEBR;

    @Comment("nivel superior")
    @JoinColumn(name = "nivel_superior", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo nivelSuperior;

    @Comment("nivel EBA")
    @JoinColumn(name = "nivel_eba", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo nivelEBA;

    @Comment("tipo entrada")
    @JoinColumn(name = "tipo_entrada", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoEntrada;

    @Comment("postEgreso")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean postEgreso;

    @Comment("permisoSalida")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean permisoTemporal;

    @Comment("tiene eventos de salida activos")
    @Column(columnDefinition = "boolean default 'false'")
    private Boolean tieneProceso= false;

    @Comment("email del adolescente")
    @Column(columnDefinition = "TEXT")
    private String email;


    public FichaIdentificacionDTO convertirADTO() {
        FichaIdentificacionDTO objetoDTO = new FichaIdentificacionDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        objetoDTO.setAlias(this.alias);
        objetoDTO.setDni(this.dni);
        objetoDTO.setEdad(this.edad);
        objetoDTO.setApellidoMaterno(this.apellidoMaterno);
        objetoDTO.setApellidoPaterno(this.apellidoPaterno);

        objetoDTO.setDireccion(this.direccion);
        objetoDTO.setDniFisico(this.dniFisico);
        objetoDTO.setExamenesMedicos(this.examenesMedicos);
        objetoDTO.setDomicilioActual(this.domicilioActual);
        objetoDTO.setFechaNacimiento(this.fechaNacimiento);
        objetoDTO.setFichaRENIEC(this.fichaRENIEC);
        objetoDTO.setImpedimentoDiscapacidad(this.impedimentoDiscapacidad);
        objetoDTO.setOtroOrigenEtnico(this.otroOrigenEtnico);
        
        objetoDTO.setModalidadEstudio(this.modalidadEstudio);
        objetoDTO.setNumeroIdentificacion(this.numeroIdentificacion);
        objetoDTO.setPermisoTemporal(this.permisoTemporal);
        objetoDTO.setTieneProceso(this.tieneProceso);

    
        if (this.nivelEBR != null) {
            objetoDTO.setNivelEBR(this.nivelEBR.getTokenIdentificador());
        }
        if (this.nivelSuperior != null) {
            objetoDTO.setNivelSuperior(this.nivelSuperior.getTokenIdentificador());
        }
        if (this.nivelEBA != null) {
            objetoDTO.setNivelEBA(this.nivelEBA.getTokenIdentificador());
        }


        Empresa empresa = this.getEmpresa();
        objetoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        objetoDTO.setFechaCreacion(super.getFechaCreacion());

        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
