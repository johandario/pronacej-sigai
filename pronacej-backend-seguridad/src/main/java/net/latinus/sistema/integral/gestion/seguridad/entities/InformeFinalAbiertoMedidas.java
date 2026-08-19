package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_informe_final_abierto_medidas")
@Comment("Medidas accesorias de informe final para régimen abierto")
@EqualsAndHashCode(of = {"idInformeFinalAbiertoMedidas"}, callSuper = true)
public class InformeFinalAbiertoMedidas extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idInformeFinalAbiertoMedidas;

    @Comment("Medida accesoria")
    @Column(columnDefinition = "TEXT")
    private String medidaAccesoria;

    @Comment("Acción")
    @Column(columnDefinition = "TEXT")
    private String accion;

    @Comment("Objetivo")
    @Column(columnDefinition = "TEXT")
    private String objetivo;

    @Comment("Análisis cualitativo")
    @Column(columnDefinition = "TEXT")
    private String analisisCualitativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_informe_final_abierto")
    @Comment("Encabezado al que pertence el registro")
    private InformeFinalAbierto informeFinalAbierto;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
