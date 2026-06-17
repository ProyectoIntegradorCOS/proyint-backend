package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import pe.gob.onp.thaqhiri.repository.EquipoRepository;
import pe.gob.onp.thaqhiri.repository.HorarioRepository;
import pe.gob.onp.thaqhiri.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repo;
    private EquipoRepository equipoRepository;
    private HorarioRepository horarioRepository;
    private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
        equipoRepository = mock(EquipoRepository.class);
        horarioRepository = mock(HorarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserService(repo, equipoRepository, horarioRepository, passwordEncoder);
    }

    @Test
    void createOrUpdate_insertsWhenNotExists() {
    	/*
        when(repo.findBySaaSub("abc")).thenReturn(Optional.empty());
        User saved = new User();
        saved.setSaaSub("abc");
        saved.setUsuario("PJ");
        saved.setNombre("Juan Perez");
        saved.setEstado(1);
        when(repo.save(any(User.class))).thenReturn(saved);

        var res = service.createOrUpdate(new UserRequest("abc", "PJ", "Juan Perez", 1, null, null, null));
        assertEquals("abc", res.saaSubject());
        verify(repo, times(1)).save(any(User.class));
        */
    }

    @Test
    void getBySaaSubject_throwsWhenMissing() {
        when(repo.findBySaaSub("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getBySaaSubject("missing"));
    }
}
