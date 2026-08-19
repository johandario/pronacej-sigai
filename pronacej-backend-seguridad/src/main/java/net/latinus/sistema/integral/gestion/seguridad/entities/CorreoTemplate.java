package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "par_correo_template")
@EqualsAndHashCode(of = {"idCorreoTemplate"}, callSuper = true)
@Comment("Tabla de correos template")
public class CorreoTemplate extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de correos template")
    private Long idCorreoTemplate;

    @Comment("Nemonico de correos template")
    private String nemonico;

    @Comment("Cuerpo del correo template")
    @Column(columnDefinition = "TEXT")
    private String correoString;

    @Comment("Razon del correo template")
    private String razon;

    @Comment("Descipcion del correo template")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Comment("Id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
