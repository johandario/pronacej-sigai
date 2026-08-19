package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "ia_ficha_identificacion_documento")
@EqualsAndHashCode(of = {"idFichaIdentificacionDocumento"}, callSuper = true)
@Comment("Tabla de ficha de identificacion relaciona con los documentos")
public class FichaDeIdentificacionDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idFichaIdentificacionDocumento;

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

    @JoinColumn(name = "id_tipo_de_documento_ficha", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del tipo de documento de la ficha")
    private Catalogo tipoDeDocumentoFicha;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}