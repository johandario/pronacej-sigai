package net.latinus.sistema.integral.gestion.seguridad.entities.informe;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "inf_valor")
@Comment("Tabla para almacenar valores de informes")
@EqualsAndHashCode(of = {"idValor"}, callSuper = true)
public class ValorInforme extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valor")
    @Comment("Identificador único del valor")
    private Long idValor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_informe", nullable = false)
    @Comment("Informe al que pertenece el valor")
    private Informe informe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campo", nullable = false)
    @Comment("Campo al que pertenece el valor")
    private CampoInforme campoInforme;

    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    @Comment("Valor del campo")
    private String valor;
}
