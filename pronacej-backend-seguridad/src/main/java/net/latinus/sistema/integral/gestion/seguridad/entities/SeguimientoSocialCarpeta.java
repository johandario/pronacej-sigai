package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_seguimiento_social_carpeta")
@EqualsAndHashCode(of = {"idSeguimientoSocialCarpeta"}, callSuper = true)
@Comment("Tabla de seguimiento social que se relaciona con carpetas")
public class SeguimientoSocialCarpeta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idSeguimientoSocialCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_seguimiento_social", referencedColumnName = "idSeguimientoSocial")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del seguimiento asociado")
    private SeguimientoSocial seguimientoSocial;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
