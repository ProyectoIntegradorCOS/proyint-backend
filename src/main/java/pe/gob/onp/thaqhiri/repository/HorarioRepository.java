package pe.gob.onp.thaqhiri.repository;

import pe.gob.onp.thaqhiri.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
}