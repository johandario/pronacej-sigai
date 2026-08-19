package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "seg_historico_ficha_identificacion")
@Comment("Tabla que registra el historico de la ficha de identificación")
@EqualsAndHashCode(of = {"idHistoricoFichaIdentificacion"},callSuper = true)
public class HistoricoFichaIdentificacion extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idHistoricoFichaIdentificacion;

    @Comment("Estado activo del registro")
    private Boolean activo;

    @Comment("Fecha de inicio de registro")
    private Date fechaInicio;

    @Comment("Fecha de finalización")
    private Date fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo_tipo_ingreso")
    @Comment("Ficha de identificación asociada a este registro")
    private Catalogo tipoIngreso;

    @Comment("Observación de ingreso")
    @Column(columnDefinition = "TEXT")
    private String observacionIngreso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación asociada a este registro")
    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_centro")
    @Comment("Centro correspondiente al registro")
    private Jerarquia centro;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_registro_salida" , nullable = true)
    @Comment("Registro de salida")
    RegistroSalida registroSalida;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
