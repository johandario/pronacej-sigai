package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_segui_documento")
@EqualsAndHashCode(of = {"idPlanTratamientoIndSeguiDocumento"}, callSuper = true)
@Comment("Tabla de ficha de seguimiento de pti que se relaciona con documentos")
public class PlanTratamientoIndSeguiDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idPlanTratamientoIndSeguiDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

    @JoinColumn(name = "id_plan_tratamiento_ind_segui", referencedColumnName = "idPlanTratamientoIndSegui")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de pertenencias asociado")
    private PlanTratamientoIndSegui planTratamientoIndSegui;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
