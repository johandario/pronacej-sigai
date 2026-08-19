package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

@MappedSuperclass
@Data
public class EntidadBase {

    //Datos que todas las tablas deberian de tener
    @Comment("Token identificador del valor guardado")
    private String tokenIdentificador = UUID.randomUUID().toString();

    @Comment("Fecha de creacion")
    private Date fechaCreacion = new Date();

    @Comment("Fecha de edicion")
    private Date fechaEdicion;

    @Comment("Fecha de eliminacion")
    private Date fechaEliminacion;

    @Comment("Ip que crea el objeto")
    private String ipCrea;

    @Comment("Ip que edita el objeto")
    private String ipEdita;

    @Comment("Ip que elimina el objeto")
    private String ipElimina;

    @Comment("Estado del objeto removido o no")
    private Boolean removido = false;

    @Comment("Estado del objeto bloqueado o no")
    private Boolean bloqueado = false;

    @JoinColumn(name = "id_usuario_crea", referencedColumnName = "idUsuarioSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del usuario que creo el objeto")
    private UsuarioSistema usuarioSistemaCrea;

    @JoinColumn(name = "id_usuario_edita", referencedColumnName = "idUsuarioSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del usuario que edito el objeto")
    @JsonBackReference
    private UsuarioSistema usuarioSistemaEdita;

    @JoinColumn(name = "id_usuario_elimina", referencedColumnName = "idUsuarioSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del usuario que elimino el objeto")
    private UsuarioSistema usuarioSistemaElimina;

    @Override
    public String toString() {
        try {
            Gson gson = new GsonBuilder().setDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER).create();
            return gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger logger = LoggerFactory.getLogger(e.getClass());
            logger.error(e.toString());
            return "";
        }
    }
}
