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
@Table(name = "ia_seguimiento_educativo_laboral_otros_documento")
@EqualsAndHashCode(of = {"idSeguimientoEducativoLaboralOtrosDocumento"}, callSuper = true)
@Comment("Tabla de seguimiento educativo laboral otros que relaciona con los documentos")
public class SeguimientoEducativoLaboralOtrosDocumento extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idSeguimientoEducativoLaboralOtrosDocumento;
    
    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;
    
    @JoinColumn(name = "id_seguimiento_educativo_laboral_otros", referencedColumnName = "idSeguimientoEducativoLaboral")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del seguimiento al que esta asociado")
    private SeguimientoEducativoLaboralOtros seguimientoEducativoLaboralOtros;
    
    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
    
    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
