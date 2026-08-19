package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_sancion_disciplinaria_carpeta")
@EqualsAndHashCode(of = {"idSancionDisciplinariaCarpeta"}, callSuper = true)
@Comment("Tabla de seguimiento social que se relaciona con carpetas")
public class SancionDisciplinariaCarpeta extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idSancionDisciplinariaCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_sancion_disciplinaria", referencedColumnName = "idSancionDisciplinaria")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la sancion")
    private SancionDisciplinaria sancionDisciplinaria;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}
