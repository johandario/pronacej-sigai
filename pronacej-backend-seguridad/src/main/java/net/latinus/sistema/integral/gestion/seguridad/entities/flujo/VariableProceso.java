package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "flu_variable_proceso")
@Comment("Tabla que gestiona las variables condicionales de un proceso vinculado a una instancia")
@EqualsAndHashCode(of = {"idVariableProceso"},callSuper = true)
public class VariableProceso extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idVariableProceso;

    @Comment("Nombre de la variable")
    private String nombre;

    @Comment("Valor de la variable")
    private String valor;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
