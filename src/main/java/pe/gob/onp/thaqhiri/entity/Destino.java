package pe.gob.onp.thaqhiri.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DESTINO")
public class Destino {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "destino_seq")
    @SequenceGenerator(name = "destino_seq", sequenceName = "seq_destino", allocationSize = 1)
    @Column(name = "ID_DEST")
    private Long id;

    @Column(name = "NO_DEST", nullable = false, length = 200)
    private String nombre;

    @Column(name = "TI_CATE", nullable = true, length = 50)
    private String categoria;

    @Column(name = "DE_DIRE", length = 400)
    private String direccion;

    @Column(name = "NU_LATI")
    private Double latitud;

    @Column(name = "NU_LONG")
    private Double longitud;

    @Column(name = "DE_REFE", length = 400)
    private String referencia;

    @Column(name = "CO_ZONA", length = 20)
    private String zona;

    @Column(name = "DE_HORA", length = 400)
    private String horarios;

    @Column(name = "DE_CONT", length = 400)
    private String contacto;

    @Column(name = "TI_PREC", nullable = false, length = 20)
    private String precision;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Destino.estado]
    @Column(name = "ST_REGI", nullable = false)
    private String estadoRegistro;
    
    @Column(name = "IN_UBIC", length = 50)
    private String ubicabilidadOnp;
    
    @Column(name = "IN_ESTA_ONP", length = 50)
    private String estadoOnp;
    
    @Column(name = "FE_ACTU_ONP", length = 50)
    private LocalDate fechaActualizacionOnp;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCreacion;

    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCreacion;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String usuarioModificacion;

    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime fechaModificacion;

    @Column(name = "DE_TERM_MODI", length = 30)
    private String terminalModificacion;
}
