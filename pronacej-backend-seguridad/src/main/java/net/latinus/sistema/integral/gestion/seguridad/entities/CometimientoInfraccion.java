package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.text.SimpleDateFormat;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Entity
@Data
@Table(name = "ia_cometimiento_infraccion")
@EqualsAndHashCode(of = {"idCometimientoInfraccion"}, callSuper = true)
public class CometimientoInfraccion extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idCometimientoInfraccion;
    
    @Comment("suspension de visitas asociada")
    @JoinColumn(name = "id_suspension_visitas", referencedColumnName = "idSuspensionVisitas")
    @ManyToOne(fetch = FetchType.LAZY)
    private SuspensionVisitas suspensionVisitas;
    
    @Comment("causal de suspensión seleccionada")
    @JoinColumn(name = "id_causal_suspension", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo causalSuspension;
    
    @Comment("estado de selección")
    private Boolean seleccionado;
    
    @Comment("id empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}