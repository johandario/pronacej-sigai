package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.EqualsAndHashCode;

import java.text.SimpleDateFormat;
import java.util.List;

@Entity
@Data
@Table(name = "flu_paso")
@Comment("Tabla de gestión de pasos de un proceso")
@EqualsAndHashCode(of = {"idPaso"}, callSuper = true)
public class Paso extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPaso;

    @Comment("Nombre del paso")
    private String nombre;

    @Comment("Ruta del componente del paso")
    private String url;

    @Comment("Define el total de avance (0,20,40,60,100)")
    private Integer porcentajeAvance;

    @Comment("El número secuencial del orden de la serie de pasos")
    private Integer orden;

    @Comment("Si el paso tiene un condicional, se almacena en formato JSON")
    private String jsonCondicional;

    @Comment("Rol o Usuario que tiene acceso a dicha tarea")
    private String rolUsuario;

    @Comment("Si paso requiere notificación de correo")
    private Boolean requiereNotificacionCorreo = false;

    @Comment("Si debe omitirse el paso en la creación del flujo")
    private Boolean omitePaso = false;

    @Comment("Rol o Usuario que requiere notificación correo")
    private String rolUsuarioNotificacion;

    @Comment("Ícono asociado en caso de que se requiera en el cliente")
    private String icono;

    @OneToMany(mappedBy = "paso", fetch = FetchType.LAZY)
    @Comment("Lista de usuarios con acceso al paso")
    private List<PasoUsuario> pasoUsuarioList;

    @OneToMany(mappedBy = "paso", fetch = FetchType.LAZY)
    @Comment("Lista de roles con acceso al paso")
    private List<PasoRol> pasoRolList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paso_anterior")
    @JsonIgnore
    @Comment("Paso que antecede al actual")
    private Paso pasoAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paso_siguiente")
    @JsonIgnore
    @Comment("Paso siguiente al actual")
    private Paso pasoSiguiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paso_subsanacion")
    @JsonIgnore
    @Comment("En caso de subsanación, el paso al que debe redirigirse, si está vacío no tiene")
    private Paso pasoSubsanacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso", nullable = false)
    @Comment("Proceso al que pertenece")
    private Proceso proceso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
