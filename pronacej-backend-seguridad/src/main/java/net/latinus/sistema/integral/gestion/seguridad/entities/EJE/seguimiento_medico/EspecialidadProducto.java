package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EspecialidadProductoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "par_especialidad_producto")
@Comment("Especialidad y productos de orden médica")
@EqualsAndHashCode(of = {"idEspecialidadProducto"}, callSuper = true)
public class EspecialidadProducto extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la entidad")
    private Long idEspecialidadProducto;

    @Comment("Código del medicamento")
    private String especialidad;

    @Comment("Nombre del medicamento")
    @Column(columnDefinition = "TEXT")
    private String producto;

    @Comment("Presentación del medicamento")
    private String tipoProducto;

    public EspecialidadProductoDTO convertirADTO() {
        EspecialidadProductoDTO dto = new EspecialidadProductoDTO();
        dto.setEspecialidad(this.especialidad);
        dto.setProducto(this.producto);
        dto.setTipoProducto(this.tipoProducto);
        dto.setTokenIdentificador(this.getTokenIdentificador());
        return dto;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}