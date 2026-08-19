package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.MedicamentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "par_medicamento")
@Comment("Medicamentos que pueden ser relacionados a la receta médica")
@EqualsAndHashCode(of = {"idMedicamento"}, callSuper = true)
public class Medicamento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la entidad")
    private Long idMedicamento;

    @Comment("Código del medicamento")
    private String codigo;

    @Comment("Nombre del medicamento")
    private String nombre;

    @Comment("Presentación del medicamento")
    @Column(columnDefinition = "TEXT")
    private String presentacion;

    @Comment("Concentración del medicamento")
    private String concentracion;

    public MedicamentoDTO convertirADTO() {
        MedicamentoDTO dto = new MedicamentoDTO();
        dto.setCodigo(this.codigo);
        dto.setNombre(this.nombre);
        dto.setPresentacion(this.presentacion);
        dto.setConcentracion(this.concentracion);
        dto.setTokenIdentificador(this.getTokenIdentificador());
        return dto;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}