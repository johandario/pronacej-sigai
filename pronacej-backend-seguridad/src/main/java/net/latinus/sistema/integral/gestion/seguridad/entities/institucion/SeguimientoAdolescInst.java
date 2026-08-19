package net.latinus.sistema.integral.gestion.seguridad.entities.institucion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "adolescente_seg_inst")
@Comment("Tabla que registra los seguimeintos de los adolescentes en las instituciones")
@EqualsAndHashCode(of = {"idAdolescenteSeguimiento"},callSuper = true)
public class SeguimientoAdolescInst extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del seguimiento del adolescente en la institucion")
    private Long idAdolescenteSeguimiento;

    @Comment("Token Identificador")
    private String tokenIdentificador = UUID.randomUUID().toString();

    @Column(columnDefinition = "TEXT")
    private String medioEntrevista;

    @Column(columnDefinition = "TEXT")
    private String resultadoEntrevista;

    @Column(columnDefinition = "TEXT")
    private String recomendacion;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Comment("Fecha y hora del seguimiento")
    private Date fechaSeguimiento;

    @ManyToOne
    @JoinColumn(name = "id_derivado_inst")
    @Comment("Derivado institucion que pertenece")
    private AdolescenteDerivadoInst adolescenteDerivadoInst;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
