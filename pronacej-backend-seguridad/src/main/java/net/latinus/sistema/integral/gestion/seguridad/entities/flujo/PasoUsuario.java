package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "flu_paso_usuario")
@Comment("Tabla de relación entre un paso y un usuario")
@EqualsAndHashCode(of = {"idPasoUsuario"}, callSuper = true)
public class PasoUsuario extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPasoUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paso", nullable = false)
    @Comment("Paso del proceso respectivo")
    private Paso paso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_sistema", nullable = false)
    @Comment("Usuario con permiso a ese paso")
    private UsuarioSistema usuarioSistema;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
