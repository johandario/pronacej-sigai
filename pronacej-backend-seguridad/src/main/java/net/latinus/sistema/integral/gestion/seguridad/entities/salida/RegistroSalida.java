package net.latinus.sistema.integral.gestion.seguridad.entities.salida;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "salida_registro")
@Comment("Tabla que registra las salidas de los adolescentes infractores")
@EqualsAndHashCode(of = {"idRegistroSalida"},callSuper = true)
public class RegistroSalida extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del registro de salida")
    private Long idRegistroSalida;

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

    @Column(columnDefinition = "TEXT")
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

    @ManyToOne
    @JoinColumn(name = "id_centro_salida")
    @Comment("Centro origen de salida")
    private Jerarquia centroSalida;

    @ManyToOne
    @JoinColumn(name = "id_fuga")
    @Comment("Relacion la el tipo de salida")
    private EventoFuga eventoFuga;

    @ManyToOne
    @JoinColumn(name = "id_traslado")
    @Comment("Relacion la el tipo de salida")
    private Traslado traslado;

    @ManyToOne
    @JoinColumn(name = "id_permiso_salida")
    @Comment("Relacion la el tipo de salida")
    private InformePermisoSalidaAdolescente permisoSalida;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_externamiento" , nullable = true)
    @Comment("Registro de externamiento")
    private ActaExternamiento externamiento;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_informe_final" , nullable = true)
    @Comment("Registro de informe final")
    private InformeFinalAbierto informeFinalAbierto;





    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

//    public void setActividades(List<ActividadSalida> actividades) {
//        this.actividades = actividades;
//        for (ActividadSalida actividad : actividades) {
//            actividad.setRegistroSalida(this); // Asociar actividades al registro
//        }
//    }




}
