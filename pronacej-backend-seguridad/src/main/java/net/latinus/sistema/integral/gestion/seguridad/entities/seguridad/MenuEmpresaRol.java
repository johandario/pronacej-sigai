package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

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
@Table(name = "seg_menu_empresa_rol")
@EqualsAndHashCode(of = {"idMenuEmpresaRol"}, callSuper = true)
@Comment("Tabla que relaciona la empresa el menu y el rol")
public class MenuEmpresaRol extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idMenuEmpresaRol;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @JoinColumn(name = "id_rol", referencedColumnName = "idRol")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del rol")
    private Rol rol;

    @JoinColumn(name = "id_menu", referencedColumnName = "idMenu")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del menu")
    private Menu menu;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }
}
