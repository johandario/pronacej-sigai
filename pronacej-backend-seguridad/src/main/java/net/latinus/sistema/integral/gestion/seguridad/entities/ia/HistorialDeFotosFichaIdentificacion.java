package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.HistorialDeFotosFichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "ia_historial_foto_ficha_identificacion")
@EqualsAndHashCode(of = {"idHistorialFotoFichaIdentificacion"}, callSuper = true)
@Comment("Tabla de ficha de identificacion relaciona con los documentos")
public class HistorialDeFotosFichaIdentificacion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idHistorialFotoFichaIdentificacion;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_ficha_de_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la ficha de identificacion a que esta asociado")
    private FichaIdentificacion fichaIdentificacion;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

    @JoinColumn(name = "id_tipo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del tipo de foto subida")
    private Catalogo tipo;

    public HistorialDeFotosFichaIdentificacionDTO convertirADTO(){
        HistorialDeFotosFichaIdentificacionDTO objetoDTO = new HistorialDeFotosFichaIdentificacionDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        CarpetaDTO carpetaDTO = this.getCarpeta().convertirADTO();

        objetoDTO.setCarpetaDTO(carpetaDTO);
        objetoDTO.setFichaIdentificacionDTO(this.getFichaIdentificacion().convertirADTO());

        objetoDTO.setFechaCreacion(super.getFechaCreacion());
        objetoDTO.setDocumentoDTO(this.getDocumento().convertirADTO());
        objetoDTO.setTipo(this.getTipo().convertirADTO());
        Empresa empresa = this.fichaIdentificacion.getEmpresa();

        objetoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador(): null);
        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
