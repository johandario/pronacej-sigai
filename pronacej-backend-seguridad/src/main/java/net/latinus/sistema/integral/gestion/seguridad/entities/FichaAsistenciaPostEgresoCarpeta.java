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

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_ficha_asistencia_post_egreso_carpeta")
@EqualsAndHashCode(of = {"idFichaAsistenciaPostEgresoCarpeta"}, callSuper = true)
@Comment("Tabla de fichas de asistencia post egreso que se relacionan con carpetas")
public class FichaAsistenciaPostEgresoCarpeta extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idFichaAsistenciaPostEgresoCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_ficha_asistencia_post_egreso", referencedColumnName = "idFichaAsistenciaPostEgreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de la ficha asociada")
    private FichaAsistenciaPostEgreso fichaAsistenciaPostEgreso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
