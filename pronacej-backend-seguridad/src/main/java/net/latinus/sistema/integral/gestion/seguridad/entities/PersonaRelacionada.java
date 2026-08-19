package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "seg_personas_relacionadas")
@EqualsAndHashCode(of = {"idPersonasRelacionadas"}, callSuper = true)
public class PersonaRelacionada extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de persona relacionada")
    private Long idPersonasRelacionadas;
    
    @JoinColumn(name = "id_evaluacion_social", referencedColumnName = "idEvaluacionSocial")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la evaluacion social")
    private EvaluacionSocial evaluacionSocial;

    @Comment("id tipo de documento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoDocumento", referencedColumnName = "idCatalogo")
    private Catalogo tipoDocumento;

    @Comment("tipo sexo biologico")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSexoBiologico", referencedColumnName = "idCatalogo")
    private Catalogo tipoSexoBiologico;

    @Comment("modalidad de estudio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modalidad_estudio", referencedColumnName = "idCatalogo")
    private Catalogo modalidadEstudio;

    @Comment("nivel EBR") 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel_ebr", referencedColumnName = "idCatalogo")
    private Catalogo nivelEBR;

    @Comment("nivel superior")
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "id_nivel_superior", referencedColumnName = "idCatalogo")
    private Catalogo nivelSuperior;

    @Comment("nivel EBA")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel_eba", referencedColumnName = "idCatalogo") 
    private Catalogo nivelEBA;

    @Comment("id tipo ocupacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_ocupacion", referencedColumnName = "idCatalogo")
    private Catalogo tipoOcupacion;

    @Comment("id estado civil")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_civil", referencedColumnName = "idCatalogo")
    private Catalogo estadoCivil;

    @Comment("id condicion laboral")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_condicion_laboral", referencedColumnName = "idCatalogo")
    private Catalogo condicionLaboral;

    @Comment("id parentesco")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parentesco", referencedColumnName = "idCatalogo")
    private Catalogo parentesco;

    @Column(columnDefinition = "TEXT")
    @Comment("id identificacion")
    private String identificacion;

    @Column(columnDefinition = "TEXT")
    @Comment("primer nombre")
    private String primerNombre;

    @Column(columnDefinition = "TEXT")
    @Comment("segundo nombre")
    private String segundoNombre;

    @Column(columnDefinition = "TEXT")
    @Comment("primer apellido")
    private String primerApellido;

    @Column(columnDefinition = "TEXT")
    @Comment("segundo apellido")
    private String segundoApellido;

    @Column(columnDefinition = "TEXT")
    @Comment("nombres completos")
    private String nombresCompletos;

    @Comment("fecha nacimiento")
    private Date fechaNacimiento;

    @Comment("discapacidad")
    private Boolean discapacidad;

    @Column(columnDefinition = "TEXT")
    @Comment("observaciones")
    private String observaciones;

    @Column(columnDefinition = "TEXT")
    @Comment("nacionalidad")
    private String nacionalidad;

    @Column(columnDefinition = "TEXT")
    @Comment("otros")
    private String otros;

    @Comment("ingresio promedio")
    private BigDecimal ingresoPromedio;

    @Comment("numero hijos")
    private Long numeroHijos;

    @Column(columnDefinition = "TEXT")
    @Comment("ocupacion")
    private String ocupacion;

    @Column(columnDefinition = "TEXT")
    @Comment("telefono")
    private String telefono;

    @Comment("es responsable econom")
    private Boolean esResponsableEconom;

    @Comment("autoriza visita")
    private Boolean autorizaVisita = false;

    @Comment("fallecido")
    private Boolean fallecido = false;

    @Comment("es tutor")
    private Boolean esTutor = false;

    @Comment("enfermo")
    private Boolean enfermo = false;

    @Comment("id estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;

    @Comment("id empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Column(columnDefinition = "TEXT")
    @Comment("reloes influencias")
    private String rolesInfluencias;

    @Comment("relacion afectiva")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_relacion_afectiva", referencedColumnName = "idCatalogo")
    private Catalogo relacionAfectiva;

    @Column(columnDefinition = "TEXT")
    @Comment("nombres")
    private String nombres;

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(
                    EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

}
