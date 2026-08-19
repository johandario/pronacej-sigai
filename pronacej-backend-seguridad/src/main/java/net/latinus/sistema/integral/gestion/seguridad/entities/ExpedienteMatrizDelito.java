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
@Table(name = "ia_expediente_matriz_delito")
@Comment("Lista de delitos vinculados a detalle de expediente")
@EqualsAndHashCode(of = {"idExpedienteDelito"}, callSuper = true)
public class ExpedienteMatrizDelito extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idExpedienteDelito;

    @JoinColumn(name = "id_catalogo_delito_generico", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de delito genérico al que pertenece")
    private Catalogo delitoGenerico;

    @JoinColumn(name = "id_catalogo_delito_especifico", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de delito específico de acuerdo a delito genérico")
    private Catalogo delitoEspecifico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_expediente_detalle")
    @Comment("Encabezado de detalle")
    private ExpedienteMatrizDetalle expedienteMatrizDetalle;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Empresa a la que pertenence el expediente")
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
