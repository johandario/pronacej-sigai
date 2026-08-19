package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.InformePermisoSalidaAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import jakarta.persistence.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "historico_entrada_salida")
@Comment("Tabla que registra el historico de entrada y salida de adolescentes infractores")
@EqualsAndHashCode(of = {"idHistoricoEntradaSalida"},callSuper = true)
public class HistoricoEntradaSalida extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idHistoricoEntradaSalida;

    @Comment("Número de identificación de la persona")
    private String numeroIdentificacion;

    @ManyToOne
    @JoinColumn(name = "id_tipo_documento_identificacion", referencedColumnName = "idCatalogo")
    @Comment("Tipo de documento de identificación")
    private Catalogo tipoDocumentoIdentificacion;

    @Comment("Fecha de entrada")
    private Date fechaEntrada;

    @Comment("Fecha de salida (nullable)")
    private Date fechaSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro_salida")
    @Comment("Registro de salida asociado si es un egreso")
    private RegistroSalida registroSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación asociada a este registro")
    private FichaIdentificacion fichaIdentificacion;

    @Comment("Indica si el registro sigue activo (true: sigue dentro, false: ya salió)")
    private Boolean registroActivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_registro")
    @Comment("Tipo de registro (entrada o salida)")
    private Catalogo tipoRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_centro_ingreso")
    @Comment("Centro donde ocurrió la entrada")
    private Jerarquia centroIngreso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_centro_salida")
    @Comment("Centro donde ocurrió la salida")
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

    @ManyToOne
    @JoinColumn(name = "id_catalogo_motivo_salida")
    @Comment("Motivo de salida (traslado, externamiento, salida temporal)")
    private Catalogo motivoSalida;

    @ManyToOne
    @JoinColumn(name = "id_traslado_adolescente")
    @Comment("Relacion la el tipo de salida")
    private TrasladoAdolescente trasladoAdolescente;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_informe_final" , nullable = true)
    @Comment("Registro de informe final")
    private InformeFinalAbierto informeFinalAbierto;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
