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
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
@Entity
@Data
@Table(name = "ia_seguimiento_social")
@EqualsAndHashCode(of = {"idSeguimientoSocial"}, callSuper = true)
public class SeguimientoSocial extends EntidadBase {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Comment("id de la tabla")
   private Long idSeguimientoSocial;
   
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
   @Comment("ficha de identificacion")
   private FichaIdentificacion fichaIdentificacion;
   @Column(columnDefinition = "timestamp")
   @Comment("fecha del seguimiento")
   private Date fecha;
   
   @JoinColumn(name = "tipo_actividad_social", referencedColumnName = "idCatalogo")
   @ManyToOne(fetch = FetchType.LAZY)
   @Comment("tipo de actividad social")
   private Catalogo tipoActividadSocial;
   
   @Comment("descripcion social")
   @Column(columnDefinition = "TEXT")
   private String descripcionSocial;
   
   @Comment("acciones adoptadas")
   @Column(columnDefinition = "TEXT")
   private String accionesAdoptadas;
   
   @Comment("comentarios adicionales")
   @Column(columnDefinition = "TEXT")
   private String comentarios;
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_programa", referencedColumnName = "idJerarquia")
   @Comment("programa asociado")
   private Jerarquia programa;
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_ambiente", referencedColumnName = "idJerarquia")
   @Comment("ambiente asociado")
   private Jerarquia ambiente;
   @Comment("empresa")
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
           LogService logService = new LogService(ex.getClass());
           logService.error("Ha ocurrido un error: {}", ex.getMessage(), ex);
           return "";
       }
   }
}
