package pe.gob.onp.thaqhiri.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "equipo_trabajo")
public class Equipo {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equipo_seq")
    @SequenceGenerator(name = "equipo_seq", sequenceName = "seq_equipo_trabajo", allocationSize = 1)
    @Column(name = "ID_EQUI")
    private Integer id;

    @Column(name = "NO_EQUI", length = 60, nullable = false)
    private String nombre;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SUPE", referencedColumnName = "ID_PERS")
    private User supervisor;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Equipo.estado]
    @Column(name = "ST_REGI", nullable = false)
    private Integer estado;
    
    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCreacion;

    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCreacion;

    @Column(name = "ID_USUA_MODI", length = 30, insertable = false)
    private String usuarioModificacion;

    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime fechaModificacion;

    @Column(name = "DE_TERM_MODI", length = 30, insertable = false)
    private String terminalModificacion;
    
    @Column(name = "IN_VISI", nullable = true)
    private Integer realizaVisitas;
    
    @Column(name = "ID_CUES", nullable = true)
    private Integer idCuestionario;
    

	public Equipo() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}
