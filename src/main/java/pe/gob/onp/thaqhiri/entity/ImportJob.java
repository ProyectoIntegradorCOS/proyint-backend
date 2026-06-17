package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "import_job")
public class ImportJob {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_job_seq")
    @SequenceGenerator(name = "import_job_seq", sequenceName = "seq_impo_job", allocationSize = 1)
    @Column(name = "id_impo")
	private Long id;

    @Column(name = "nu_tota_fila", nullable = false)
    private int totalFilas;

    @Column(name = "nu_fila_proc", nullable = false)
    private int filasProcesadas;

    @Column(name = "nu_porc", nullable = false)
    private int porcentaje;

    @Column(name = "de_esta", nullable = false, length = 20)
    private String estado; // PROCESANDO, COMPLETADO, ERROR

    @Column(name = "de_mens")
    private String mensaje;

    @Column(name = "fe_inic", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fe_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "nu_hora_resta")
    private long nuHorasRestantes;
    
    @Column(name = "nu_minu_resta")
    private long nuMinutosRestantes;
    
    @Column(name = "nu_segu_resta")
    private long nuSegundosRestantes;
    
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
    

	public ImportJob() {
		super();
		// TODO Auto-generated constructor stub
	}
	    
}