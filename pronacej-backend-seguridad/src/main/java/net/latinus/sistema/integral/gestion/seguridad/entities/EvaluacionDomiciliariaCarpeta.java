package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "ia_evaluacion_domiciliaria_carpeta")
@EqualsAndHashCode(of = {"idEvaluacionDomiciliariaCarpeta"}, callSuper = true)
@Comment("Tabla de evaluación domiciliaria que se relaciona con carpetas")
public class EvaluacionDomiciliariaCarpeta extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idEvaluacionDomiciliariaCarpeta;
    
    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;
    
    @JoinColumn(name = "id_evaluacion_domiciliaria", referencedColumnName = "idEvaluacionDomiciliaria")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la evaluación domiciliaria asociada")
    private EvaluacionDomiciliaria evaluacionDomiciliaria;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}