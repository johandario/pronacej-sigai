package net.latinus.sistema.integral.gestion.seguridad.entities.institucion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "adolescente_derivado_inst")
@Comment("Tabla que gestiona los procesos de derivacion de adolescente a instituciones")
@EqualsAndHashCode(of = {"idAdolescenteDerivado"},callSuper = true)
public class AdolescenteDerivadoInst extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del proceso de derivacion")
    private Long idAdolescenteDerivado;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Comment("Ficha identificacion del adolescente que se esta fugando")
    private FichaIdentificacion tokenFichaIdentificacion;

    @Comment("Fecha del registro")
    private Date fechaRegistro;

    @Comment("Fecha de la derivacion")
    private Date fechaDerivacion;

    @ManyToOne
    @JoinColumn(name = "id_institucion")
    @Comment("Institucion a la que se lo deriva")
    private RegistroInstitucion institucion;

    @Comment("Departamento al que se lo deriva")
    private String departamento;

    @Comment("Departamento al que se lo deriva")
    private String tiempoServicio;

    @Comment("Departamento al que se lo deriva")
    private String personaResponsable;

    @Comment("Departamento al que se lo deriva")
    @Column(columnDefinition = "TEXT")
    private String servicio;

    @Comment("Estado de la gestion")
    private String estado;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
}
}
