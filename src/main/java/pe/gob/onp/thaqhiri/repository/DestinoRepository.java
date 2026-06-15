package pe.gob.onp.thaqhiri.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.entity.Destino;

public interface DestinoRepository extends JpaRepository<Destino, Long> {

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 10:12 UTC-5 (Lima)][desc: Lista destinos activos para plantilla/import de plan de visitas][obj: DestinoRepository]
    List<Destino> findByEstadoRegistroOrderByNombreAsc(String estado);
    
    List<Destino> findByEstadoRegistro(String estado);

    Optional<Destino> findFirstByNombreIgnoreCase(String nombre);

    @Query("""
            SELECT d
            FROM Destino d
            WHERE d.estadoRegistro = '1'
              AND ( :destino IS NULL OR UPPER(d.nombre) LIKE CONCAT('%', UPPER(:destino), '%') )
              AND ( :direccion IS NULL OR UPPER(d.direccion) LIKE CONCAT('%', UPPER(:direccion), '%') )
        """)
    Page<Destino> buscarPaginado(
            @Param("destino") String destino,
            @Param("direccion") String direccion,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE Destino d
               SET d.estadoRegistro = '0',
                   d.usuarioModificacion = :usuario,
                   d.terminalModificacion = :terminal
             WHERE d.id = :id
           """)
    int desactivar(
            @Param("id") Long id,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );
    
    
    /**
     * Busca un destino por nombre y direccion coincidencia exacta sin tomar en cuenta mayusculas y minusculas 
     */
    Optional<Destino> findFirstByNombreIgnoreCaseAndDireccionIgnoreCaseAndEstadoRegistro(
            String nombre,
            String direccion,
            String estado
    );
    
}
