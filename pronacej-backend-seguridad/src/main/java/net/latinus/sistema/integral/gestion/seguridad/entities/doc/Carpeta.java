package net.latinus.sistema.integral.gestion.seguridad.entities.doc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "doc_carpeta")
@EqualsAndHashCode(of = {"idCarpeta"}, callSuper = true)
@Comment("Tabla carpeta")
public class Carpeta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idCarpeta;

    @Comment("Nombre de la carpeta para mostrar al cliente")
    private String nombreCliente;

    @Comment("Nombre de la carpeta en alfresco")
    private String nombreAlfresco;

    private String descripcion;

    @Column(columnDefinition = "TEXT")
    @Comment("Id de alfresco")
    private String identificadorAlfresco;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @JoinColumn(name = "id_carpeta_padre", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta padre")
    private Carpeta carpetaPadre;

    public CarpetaDTO convertirADTO() {
        CarpetaDTO objetoDTO = new CarpetaDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        objetoDTO.setNombreCliente(this.getNombreCliente());
        objetoDTO.setNombreAlfresco(this.getNombreAlfresco());

        Empresa empresa = this.getEmpresa();
        objetoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        objetoDTO.setDescripcion(this.getDescripcion());
        objetoDTO.setFechaCreacion(super.getFechaCreacion());

        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
