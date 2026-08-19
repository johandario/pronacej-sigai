package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.FuncionarioJerarquiaRol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;


@Entity
@Data
@Table(name = "seg_funcionario")
@EqualsAndHashCode(of = {"idFuncionario"}, callSuper = true)
public class Funcionario extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id del funcionario")
    private Long idFuncionario;

    @Comment("nombres")
    private String nombres;

    @Comment("apeliidos")
    private String apellidos;

    @Comment("email")
    private String email;

    @Comment("tipo de documento")
    @JoinColumn(name = "tipo_documento", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoDeDocumento;

    @Comment("numero de documento")
    private String numeroDeDocumento;

    @Comment("telefono")
    private String telefono;

    @Comment("numero de celular")
    private String numeroDeCelular;

    @Comment("departamento")
    @JoinColumn(name = "departamento", referencedColumnName = "idJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    private Jerarquia departamento;

    @Comment("cargo")
    @JoinColumn(name = "cargo", referencedColumnName = "idCargosJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    private CargosJerarquia cargo;

    @Comment("url logo")
    @Column(columnDefinition = "TEXT")
    private String urlLogo = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQAFM_xyIubtJwKiuFsU3IsBZqxlRbneCKvei3_rifJE098371NG05Ptm0cfoLoAqSrXCg&usqp=CAU";

    @Comment("id del estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;

    @OneToMany(
            mappedBy="funcionario",
            cascade=CascadeType.ALL,
            orphanRemoval=true,
            fetch=FetchType.LAZY
    )

    @ToString.Exclude
    private Set<FuncionarioJerarquiaRol> asignaciones = new HashSet<>();

    public FuncionarioDTO convertirADTO() {
        FuncionarioDTO dto = new  FuncionarioDTO();
        dto.setTokenIdentificador(this.getTokenIdentificador());
        return dto;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
