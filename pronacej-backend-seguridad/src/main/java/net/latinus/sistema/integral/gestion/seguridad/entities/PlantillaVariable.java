package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "par_plantilla_variable")
@EqualsAndHashCode(of = {"idPlantillaVariable"}, callSuper = true)
@Comment("Tabla de variables dinamica para formularios")
public class PlantillaVariable extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la variable de la plantilla")
    private Long idPlantillaVariable;
    
    @Comment("Plantilla formulario a la que pertenece la variable")
    @JoinColumn(name = "plantilla_formulario", referencedColumnName = "idPlantillaFormulario")
    @ManyToOne(fetch = FetchType.LAZY)
    private PlantillaFormulario plantillaFormulario;

    @Comment("Nombre de la variable de la plantilla")
    private String nombre;

    @Comment("Clave de la variable de la plantilla")    
    private String clave;

    @Comment("Orden de la variable de la plantilla")
    private Integer orden;
    
    @Comment("Valor de la variable de la plantilla")
    private String valor;

    @Comment("Id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
