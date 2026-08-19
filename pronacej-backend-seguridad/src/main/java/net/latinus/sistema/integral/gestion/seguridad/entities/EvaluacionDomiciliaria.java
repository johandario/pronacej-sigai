package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Entity
@Data
@Table(name = "ia_evaluacion_domiciliaria")
@EqualsAndHashCode(of = {"idEvaluacionDomiciliaria"}, callSuper = true)
public class EvaluacionDomiciliaria extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idEvaluacionDomiciliaria;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @Comment("centro")
    @JoinColumn(name = "id_centro", referencedColumnName = "idJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    private Jerarquia centro;
    
    @Column(columnDefinition= "timestamp")
    @Comment("fecha del registro")
    private Date fechaRegistro;
    
    @Column(columnDefinition= "timestamp")
    @Comment("fecha de la entrevista")
    private Date fechaEntrevista;

    @Comment("id de la persona relacionada")
    @JoinColumn(name = "id_persona_relacionada", referencedColumnName = "idPersonasRelacionadas")
    @ManyToOne(fetch = FetchType.LAZY)
    private PersonaRelacionada personaRelacionada;

    @Column(columnDefinition = "TEXT")
    @Comment("otra persona relacionada (campo libre)")
    private String otraPersonaRelacionada;

    @Comment("duracion de la entrevista")
    private Float duracionVista;

    @Comment("visita realizada")
    private Boolean visitaRealizada;

    @Column(columnDefinition = "TEXT")
    @Comment("motivo no visita")
    private String motivoNoVisita;

    @Column(columnDefinition = "TEXT")
    @Comment("objetivo general")
    private String objetivoGeneral;

    @Column(columnDefinition = "TEXT")
    @Comment("desarrollo viita domiciliaria")
    private String desarrolloVisitaDomiciliaria;

    @Column(columnDefinition = "TEXT")
    @Comment("caracterustucas domicilio visitado")
    private String caracteristicasDomicilioVisitado;

    @Column(columnDefinition = "TEXT")
    @Comment("conclusiones")
    private String conclusiones;

    @Column(columnDefinition = "TEXT")
    @Comment("recomendaciones")
    private String recomendaciones;
    
    @Column(columnDefinition = "TEXT")
    @Comment("dinamica familiar disfuncional")
    private String dinamicaFamiliarDisfuncional;
    
    @Column(columnDefinition = "TEXT")
    @Comment("caracteristicas entorno social mc")
    private String caracteristicasEntornoSocialMC;
    
    @Column(columnDefinition = "TEXT")
    @Comment("factores protectores")
    private String factoresProtectores;
    
    @Column(columnDefinition = "TEXT")
    @Comment("factores riesgo familia")
    private String factoresRiesgoFamilia;
    
    @Column(columnDefinition = "TEXT")
    @Comment("factores riesgo social")
    private String factoresRiesgoSocial;
    
    @Column(columnDefinition = "TEXT")
    @Comment("factores protectores familia")
    private String factoresProtectoresFamilia;
    
    @Column(columnDefinition = "TEXT")
    @Comment("factores protectores social")
    private String factoresProtectoresSocial;
    
    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    
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
