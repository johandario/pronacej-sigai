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

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Entity
@Data
@Table(name = "ia_datos_hijo_ingresado")
@EqualsAndHashCode(of = {"idDatosHijoIngresado"}, callSuper = true)
public class DatosHijoIngresado extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idDatosHijoIngresado;

    @JoinColumn(name = "id_ficha_ingreso", referencedColumnName = "idFichaIngreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de ingreso")
    private FichaIngreso fichaIngreso;
    
    @JoinColumn(name = "id_personas_relacionadas", referencedColumnName = "idPersonasRelacionadas")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la persona relacionada")
    private PersonaRelacionada personaRelacionada;

    @Comment("apellido paterno del hijo")
    private String hijoApellidoPaterno;

    @Comment("apellido materno del hijo")
    private String hijoApellidoMaterno;

    @Comment("nombres completos del hijo")
    private String hijoNombresCompletos;

    @Comment("edad del hijo")
    @Column(columnDefinition = "int")
    private Integer hijoEdad;

    @Comment("DNI del hijo")
    private String hijoDNI;

    @Comment("indica si el hijo ha sido victima de agresion")
    private Boolean hijoVictimaAgresion;

    @Comment("especificacion de la agresion sufrida")
    private String hijoEspecificarAgresion;

    @Comment("indica si el hijo tiene moretones")
    private Boolean hijoMoretones;

    @Comment("especificacion de la zona de moretones")
    private String hijoEspecificarZonaMoretones;

    @Comment("indica si el hijo tiene cicatrices")
    private Boolean hijoCicatrices;

    @Comment("especificacion de la zona de cicatrices")
    private String hijoEspecificarZonaCicatrices;

    @Comment("indica si el hijo tiene tatuajes")
    private Boolean hijoTatuajes;

    @Comment("especificacion de la zona de tatuajes")
    private String hijoEspecificarZonaTatuajes;

    @Comment("especificacion de otras caracteristicas")
    private String hijoOtroEspecificar;

    @Comment("observaciones generales sobre el hijo")
    @Column(columnDefinition = "TEXT")
    private String hijoObservaciones;
    
    @Comment("id empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}