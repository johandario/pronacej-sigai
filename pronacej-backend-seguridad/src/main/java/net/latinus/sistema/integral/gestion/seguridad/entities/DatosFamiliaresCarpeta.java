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
@Table(name = "ia_datos_familiares_carpeta")
@EqualsAndHashCode(of = {"idDatosFamiliaresCarpeta"}, callSuper = true)
@Comment("Tabla de datos familiares que se relaciona con carpetas")
public class DatosFamiliaresCarpeta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idDatosFamiliaresCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_datos_familiares", referencedColumnName = "idDatosFamiliares")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de datos familiares asociado")
    private DatosFamiliares datosFamiliares;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}