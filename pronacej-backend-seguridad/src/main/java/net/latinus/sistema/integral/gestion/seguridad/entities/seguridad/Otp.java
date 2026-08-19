package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_otp")
@EqualsAndHashCode(of = {"idOTp"}, callSuper = true)
@Comment("Tabla de otp del sistema")
public class Otp extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idOTp;

    @Comment("Numero del otp")
    private Integer codigo;

    @Comment("Id de la tabla usuario sistema empresa rol")
    @JoinColumn(name = "id_usuario_sistema_empresa_rol", referencedColumnName = "idUsuarioSistemaEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }
}
