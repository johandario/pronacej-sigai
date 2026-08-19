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
@Table(name = "ia_datos_familiares_documento")
@EqualsAndHashCode(of = {"idDatosFamiliaresDocumento"}, callSuper = true)
@Comment("Tabla de datos familiares que se relaciona con documentos")
public class DatosFamiliaresDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idDatosFamiliaresDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

    @JoinColumn(name = "id_datos_familiares", referencedColumnName = "idDatosFamiliares")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de datos familiares asociado")
    private DatosFamiliares datosFamiliares;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
