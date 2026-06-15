package pe.gob.onp.thaqhiri.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import pe.gob.onp.thaqhiri.entity.VisitItem;
import pe.gob.onp.thaqhiri.entity.VisitItemHistory;

public interface VisitItemHistoryRepository extends JpaRepository<VisitItemHistory, Long> {
	
	@Modifying
    @Transactional
    @Query("""
        DELETE FROM VisitItemHistory h
        WHERE h.item.id = :itemId
    """)
    int deleteByVisitItemId(@Param("itemId") Long itemId);
	
}
