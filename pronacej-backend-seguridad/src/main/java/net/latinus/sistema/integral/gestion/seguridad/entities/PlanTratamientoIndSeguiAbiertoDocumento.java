package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_segui_abierto_documento")
@EqualsAndHashCode(of = {"idPlanTratamientoIndSeguiAbiertoDocumento"}, callSuper = true)
@Comment("Tabla de ficha de seguimiento de pti abierto que se relaciona con documentos")
public class PlanTratamientoIndSeguiAbiertoDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idPlanTratamientoIndSeguiAbiertoDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

    @JoinColumn(name = "id_plan_tratamiento_ind_segui_abierto", referencedColumnName = "idPlanTratamientoIndSeguiAbierto")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de pertenencias asociado")
    private PlanTratamientoIndSeguiAbierto planTratamientoIndSeguiAbierto;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
