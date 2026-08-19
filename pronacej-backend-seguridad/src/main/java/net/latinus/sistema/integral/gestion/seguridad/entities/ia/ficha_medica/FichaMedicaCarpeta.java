package net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_ficha_medica_carpeta")
@EqualsAndHashCode(of = {"idFichaMedicaCarpeta"}, callSuper = true)
@Comment("Fichas médicas que se relacionan con carpetas")
public class FichaMedicaCarpeta extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idFichaMedicaCarpeta;

    @JoinColumn(name = "id_carpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_ficha_medica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro asociado")
    private FichaMedica fichaMedica;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
