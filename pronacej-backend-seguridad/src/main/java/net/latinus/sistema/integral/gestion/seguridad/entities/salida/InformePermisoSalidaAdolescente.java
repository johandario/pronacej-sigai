package net.latinus.sistema.integral.gestion.seguridad.entities.salida;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "inf_permiso_salida")
@Comment("Tabla que registra las los permisos salidas de los adolescentes infractores")
@EqualsAndHashCode(of = {"idPermisoSalida"},callSuper = true)
public class InformePermisoSalidaAdolescente extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del permiso de salida")
    private Long idPermisoSalida;

    @Comment("Fecha y Hora de salida")
    @Column(name = "fecha_hora_salida", nullable = false)
    private Date fechaHoraSalida;

    @Comment("Usuario que realiza la salida")
    private String usuarioSalida;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Comment("Ficha identificacion del adolescente que se esta fugando")
    private FichaIdentificacion tokenFichaIdentificacion;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_motivo_salida")
    @Comment("Motivo de salida (traslado, externamiento, salida temporal)")
    private Catalogo motivoSalida;

    @Column(name = "nro_documento", length = 50)
    @Comment("Número de documento con el que sale")
    private String nroDocumento;


    @Comment("Fecha y hora de regreso si es salida temporal")
    private Date fechaHoraRegreso;

    @Comment("Observaciones")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("lugar de salida segun el tipo de salida")
    private String tipoSalidaLugar;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_tipo_salida")
    @Comment("Tipo de salida (Laboral, medico, educativo audiencia)")
    private Catalogo tipoSalida;


    @OneToMany(mappedBy = "informePermisoSalidaAdolescente", cascade = CascadeType.ALL , orphanRemoval = true)
    @Comment("Lista de actividades")
    private List<ActividadSalida> actividades;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_frencuencia_salida")
    @Comment("Frecuencia de salida (Diario, Una vez)")
    private Catalogo frecuenciaSalida;

    @Comment("Campo que indica si el proceso finalizo con registro de salida")
    private Boolean isComplete ;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado_evento")
    @Comment("Estado que tiene el evento")
    private Catalogo estadoEvento;

    @ManyToOne
    @JoinColumn(name = "id_centro")
    @Comment("Centro de salida temporal")
    private Jerarquia centro;

    @Comment("Justificacion de otros en tipo de frecuencia de salida")
    @Column(columnDefinition = "TEXT")
    private String otrosSalida;



    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

    public void setActividades(List<ActividadSalida> actividades) {
        this.actividades = actividades;
        for (ActividadSalida actividad : actividades) {
            actividad.setInformePermisoSalidaAdolescente(this); // Asociar actividades al registro
        }
    }



}
