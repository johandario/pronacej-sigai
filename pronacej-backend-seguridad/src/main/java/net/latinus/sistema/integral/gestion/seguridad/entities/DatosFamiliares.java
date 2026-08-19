package net.latinus.sistema.integral.gestion.seguridad.entities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;
import java.text.SimpleDateFormat;
@Entity
@Data
@Table(name = "par_datos_familiares")
@EqualsAndHashCode(of = {"idDatosFamiliares"}, callSuper = true)
@Comment("Tabla general del aspecto familiar")
public class DatosFamiliares extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idDatosFamiliares;
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    @JoinColumn(name = "id_tipo_familia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id del tipo de familia")
    private Catalogo tipoFamilia;
    @JoinColumn(name = "id_organizacion_familiar", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id del tipo de la organizacion familiar")
    private Catalogo organizacionFamiliar;
    @Column(columnDefinition = "TEXT")
    @Comment("ejercicio autoridad")
    private String ejercicioAutoridad;
    @Column(columnDefinition = "TEXT")
    @Comment("id del tipo de la organizacion familiar")
    private String entornoFamiliar;
    @Comment("relacion intra familiar padres")
    private Boolean relacionIntraFamiliarPadres;
    @Comment("relacion intra familiar filial")
    private Boolean relacionIntraFamiliarFilial;
    @Comment("relacion intra familiar parentales")
    private Boolean relacionIntraFamiliarParentales;
    @Comment("relacion intrafamiliar pareja")
    private Boolean relacionIntraFamiliarPareja;
    @Column(columnDefinition = "TEXT")
    @Comment("observaciones relacion intrafamiliar")
    private String observacionesRelacionIntrafamiliar;
    @Column(columnDefinition = "TEXT")
    @Comment("causa ausencia padres")
    private String causaAusenciaPadres;
    @Comment("partida de nacimiento")
    private Boolean partidaNacimiento;
    @Column(columnDefinition = "TEXT")
    @Comment("religion")
    private String religion;
    //Religion
    @Comment("bautismo")
    private Boolean bautismo;
    @Comment("primera comunion")
    private Boolean primeraComunion;
    @Comment("confirmacion")
    private Boolean confirmacion;
    @Column(columnDefinition = "TEXT")
    @Comment("otro sacramento")
    private String otroSacramento;
    @Comment("id del estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;
    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    @Comment("id tipo de sacramento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_sacramento", referencedColumnName = "idCatalogo")
    private Catalogo tipoSacramento;
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