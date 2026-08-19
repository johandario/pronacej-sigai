package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_segui_carpeta")
@EqualsAndHashCode(of = {"idPlanTratamientoIndSeguiCarpeta"}, callSuper = true)
@Comment("Tabla de ficha de seguimiento de pti que se relaciona con carpetas")
public class PlanTratamientoIndSeguiCarpeta extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idPlanTratamientoIndSeguiCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_plan_tratamiento_ind_segui", referencedColumnName = "idPlanTratamientoIndSegui")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de pertenencias asociado")
    private PlanTratamientoIndSegui planTratamientoIndSegui;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
