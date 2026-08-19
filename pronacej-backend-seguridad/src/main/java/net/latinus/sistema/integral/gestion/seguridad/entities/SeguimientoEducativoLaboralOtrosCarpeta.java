package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_seguimiento_educativo_laboral_otros_carpeta")
@EqualsAndHashCode(of = {"idSeguimientoEducativoLaboralOtrosCarpeta"}, callSuper = true)
@Comment("Tabla de seguimiento educativo laboral otros que se relaciona con carpetas")
public class SeguimientoEducativoLaboralOtrosCarpeta extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idSeguimientoEducativoLaboralOtrosCarpeta;
    
    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;
    
    @JoinColumn(name = "id_seguimiento_educativo_laboral_otros", referencedColumnName = "idSeguimientoEducativoLaboral")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del seguimiento asociado")
    private SeguimientoEducativoLaboralOtros seguimientoEducativoLaboralOtros;
    
    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
