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

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;


@Entity
@Data
@Table(name = "ia_sancion_disciplinaria")
@EqualsAndHashCode(of = {"idSancionDisciplinaria"}, callSuper = true)
public class SancionDisciplinaria extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idSancionDisciplinaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @Comment("ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Comment("fecha de inicio")
    private Date fechaInicio;

    @Comment("fecha de fin")
    private Date fechaFin;

    @Comment("fecha de registro")
    private Date fechaRegistro;

    @Column(columnDefinition = "TEXT")
    private String nroResolucion;

    @JoinColumn(name = "tipificacion_falta", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tipificacion de la sancion")
    private Catalogo tipificacionFalta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_programa", referencedColumnName = "idJerarquia")
    @Comment("programa asociado")
    private Jerarquia programa;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ambiente", referencedColumnName = "idJerarquia")
    @Comment("ambiente asociado")
    private Jerarquia ambiente;

    @Comment("Falta cometida")
    @Column(columnDefinition = "TEXT")
    private String falta;

    @Comment("Sancion impuesta")
    @Column(columnDefinition = "TEXT")
    private String sancion;

    @Comment("observacion")
    @Column(columnDefinition = "TEXT")
    private String observacion;

    @ManyToOne
    @JoinColumn(name = "id_centro")
    @Comment("Centro")
    private Jerarquia centro;


    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
