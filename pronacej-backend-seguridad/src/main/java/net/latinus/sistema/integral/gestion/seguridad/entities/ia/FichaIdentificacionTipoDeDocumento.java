package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionTipoDeDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_ficha_identicacion_tipo_de_documento")
@EqualsAndHashCode(of = {"idFichaIdentificacionTipoDeDocumento"}, callSuper = true)
@Comment("Tabla de notificaciones de email")
public class FichaIdentificacionTipoDeDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idFichaIdentificacionTipoDeDocumento;

    @JoinColumn(name = "id_seccion_ficha_identificacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del catalogo que representa el tipo")
    private Catalogo seccionFichaDeIdentificacion;

    @JoinColumn(name = "id_tipo_de_archivo_sistema", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del catalogo que representa el tipo")
    private Catalogo tipoArchivoSistema;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    private Boolean requerido = false;

    public FichaIdentificacionTipoDeDocumentoDTO convertirADTO() {
        FichaIdentificacionTipoDeDocumentoDTO objetoDTO = new FichaIdentificacionTipoDeDocumentoDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        Catalogo seccionFichaDeIdentificacion = this.getSeccionFichaDeIdentificacion();
        objetoDTO.setSeccionFichaDeIdentificacionDTO(seccionFichaDeIdentificacion != null ? seccionFichaDeIdentificacion.convertirADTO() : null);

        Catalogo tipoArchivoSistema = this.getTipoArchivoSistema();
        objetoDTO.setTipoArchivoSistemaDTO(tipoArchivoSistema != null ? tipoArchivoSistema.convertirADTO() : null);
        objetoDTO.setFechaCreacion(this.getFechaCreacion());
        objetoDTO.setRequerido(this.getRequerido());
        Empresa empresa = this.getEmpresa();

        objetoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
