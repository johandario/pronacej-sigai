package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
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
@Table(name = "ia_instrumento_evaluacion")
@EqualsAndHashCode(of = {"idInstrumentoEvaluacion"}, callSuper = true)
public class InstrumentoEvaluacion extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idInstrumentoEvaluacion;
    
    @JoinColumn(name = "id_informe_seguimiento", referencedColumnName = "idInformeSeguimientoPII")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id del informe de seguimiento al que pertenece")
    private InformeSeguimientoPII informeSeguimientoPII;
    
    @JoinColumn(name = "id_tipo_instrumento", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tipo de instrumento")
    private Catalogo tipoInstrumento;
    
    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}