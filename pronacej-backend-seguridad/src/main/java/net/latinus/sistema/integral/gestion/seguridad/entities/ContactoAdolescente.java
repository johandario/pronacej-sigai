package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import java.text.SimpleDateFormat;
import java.util.Date;


@Entity
@Data
@Table(name = "inf_contacto_adolescente")
@Comment("Tabla que registra todos los contactos que ha tenido el adolescente")
@EqualsAndHashCode(of = {"idContactoAdolescente"},callSuper = true)
public class ContactoAdolescente extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del registro de contacto adolescente")
    private Long idContactoAdolescente;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Comment("Ficha identificacion del adolescente que se esta fugando")
    private FichaIdentificacion tokenFichaIdentificacion;


    @Comment("Usuario que realiza el contacto")
    private String usuarioResponsable;

    @Comment("Fecha y Hora de salida")
    private Date fechaRegistro;

    @Comment("Modalidad de entrevista con el adolescente")
    private String modalidadEntrevista;

    @Column(columnDefinition = "TEXT")
    @Comment("Observaciones")
    private String observaciones;

    @Column(columnDefinition = "TEXT")
    @Comment("Actividades que se realizan durante el contacto con el adolescente")
    private String actividades;




    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
