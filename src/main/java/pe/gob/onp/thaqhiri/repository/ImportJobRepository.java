package pe.gob.onp.thaqhiri.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.gob.onp.thaqhiri.entity.ImportJob;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
}