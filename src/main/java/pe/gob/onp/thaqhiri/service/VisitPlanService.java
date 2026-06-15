package pe.gob.onp.thaqhiri.service;

import lombok.extern.slf4j.Slf4j;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.dto.VisitItemCompleteRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemCreateRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemPendingReprogramResponse;
import pe.gob.onp.thaqhiri.dto.VisitItemReassignRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemResponse;
import pe.gob.onp.thaqhiri.dto.VisitItemStateChangeRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemsReorderRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanResponse;
import pe.gob.onp.thaqhiri.entity.Destino;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.entity.VisitItem;
import pe.gob.onp.thaqhiri.entity.VisitItemHistory;
import pe.gob.onp.thaqhiri.entity.VisitItemReassignHistory;
import pe.gob.onp.thaqhiri.entity.VisitPlan;
import pe.gob.onp.thaqhiri.exception.BusinessException;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import pe.gob.onp.thaqhiri.model.VisitItemEventType;
import pe.gob.onp.thaqhiri.model.VisitItemPriority;
import pe.gob.onp.thaqhiri.model.VisitItemState;
import pe.gob.onp.thaqhiri.model.VisitPlanStatus;
import pe.gob.onp.thaqhiri.repository.VisitItemHistoryRepository;
import pe.gob.onp.thaqhiri.repository.VisitItemRepository;
import pe.gob.onp.thaqhiri.repository.VisitItemReassignHistoryRepository;
import pe.gob.onp.thaqhiri.repository.VisitPlanRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import pe.gob.onp.thaqhiri.dto.VisitaPlanMasivoRequest;
import pe.gob.onp.thaqhiri.model.ResultadoValidacion;
import pe.gob.onp.thaqhiri.model.TipoResultadoValidacion;
import pe.gob.onp.thaqhiri.model.VisitItemMasivo;
import pe.gob.onp.thaqhiri.repository.DestinoRepository;
import pe.gob.onp.thaqhiri.util.UFecha;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Service
@Transactional
@Slf4j
public class VisitPlanService {

    private final VisitPlanRepository planRepository;
    private final VisitItemRepository itemRepository;
    private final VisitItemHistoryRepository historyRepository;
    private final VisitItemReassignHistoryRepository reassignHistoryRepository;
    private final UserService userService;
    private final DestinoService destinoService;
    private final DestinoRepository destinoRepository;
    
    /* Para identificar a un plan en la carga masiva */
    public record PlanKey(Long verifierId, LocalDate plannedFor) {}
    

    public VisitPlanService(VisitPlanRepository planRepository,
                            VisitItemRepository itemRepository,
                            VisitItemHistoryRepository historyRepository,
                            VisitItemReassignHistoryRepository reassignHistoryRepository,
                            UserService userService,
                            DestinoService destinoService,
                            DestinoRepository destinoRepository) {
        this.planRepository = planRepository;
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
        this.reassignHistoryRepository = reassignHistoryRepository;
        this.userService = userService;
        this.destinoService = destinoService;
        this.destinoRepository = destinoRepository;
    }

    public VisitPlanResponse createPlan(VisitPlanRequest request, String usuario, String terminal) {
        User verifier = userService.getEntity(request.verifierId());

        VisitPlan plan = new VisitPlan();
        plan.setVerifier(verifier);
        plan.setStatus(VisitPlanStatus.PLANNED);
        plan.setTitle(request.title());
        plan.setPlannedFor(request.plannedFor());
        plan.setCreatedBy(usuario);
        plan.setCreatedFrom(terminal);
        plan.setStRegi(UConstante.ACTIVO_REGI);

        VisitPlan savedPlan = planRepository.save(plan);

        if (request.items() != null && !request.items().isEmpty()) {
            int order = 1;
            
	            for (VisitItemCreateRequest itemRequest : request.items()) {
	            	VisitItemPriority prioridadItem = parsePriority(itemRequest.prioridad());
	            	
		                VisitItem item = new VisitItem();
		                item.setPlan(savedPlan);
		                item.setCompanyName(itemRequest.companyName());
		                item.setDireccion(itemRequest.direccion());
		                item.setTargetTime(itemRequest.targetTime());                
		                item.setPriority(prioridadItem);
		                item.setPvTemplate(normalizeNullable(itemRequest.plantillaPv()));
		                item.setOrderIndex(order++);
		                item.setState(VisitItemState.PENDING);
		                item.setCreatedBy(usuario);
		                item.setCreatedFrom(terminal);
		                
		                Destino destino = new Destino();
		                destino.setId(itemRequest.destinoId());		                
		                
		                item.setDestino(destino);
		                
		                itemRepository.save(item);
	            }
        }

        return toPlanResponse(savedPlan);
    }
    
    
    public VisitPlanResponse updatePlan(VisitPlanRequest request, String usuario, String terminal) {
    	Long planId = request.id();
    	
        User verifier = userService.getEntity(request.verifierId());

        VisitPlan plan = planRepository
                .findById(planId)
                .orElseThrow();
        
        plan.setVerifier(verifier);
        plan.setTitle(request.title());
        plan.setPlannedFor(request.plannedFor());
        plan.setUpdatedBy(usuario);
        plan.setUpdatedFrom(terminal);
        plan.setStRegi(UConstante.ACTIVO_REGI);

        VisitPlan updatedPlan = planRepository.save(plan);

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:21 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
        if (request.items() == null) {
            return toPlanResponse(updatedPlan);
        }
        
        // Sincronizar items
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 09:58 UTC-5 (Lima)][desc: Cambia borrado físico por borrado lógico (ST_ITEM=DELETED) y registra historial de cambios][obj: VisitPlanService.updatePlan]
        List<VisitItem> existentes = itemRepository.findByPlanIdAndStateNot(plan.getId(), VisitItemState.DELETED);
        List<Long> incomingIds = request.items().stream()
                .map(VisitItemCreateRequest::id)
                .filter(Objects::nonNull)
                .toList();
        
        // Marcar como eliminados (lógico) los items que ya no están en la solicitud
        existentes.stream()
                .filter(i -> !incomingIds.contains(i.getId()))
                .forEach(i -> {
                    VisitItemState prev = i.getState();
                    i.setState(VisitItemState.DELETED);
                    i.setUpdatedBy(usuario);
                    i.setUpdatedFrom(terminal);
                    itemRepository.save(i);
                    recordHistory(
                            i,
                            VisitItemEventType.STATE_CHANGE,
                            String.valueOf(prev),
                            String.valueOf(VisitItemState.DELETED),
                            null,
                            null,
                            usuario,
                            terminal,
                            OffsetDateTime.now()
                    );
                });

        
        int order = 1;
                
	        // Insertar o actualizar items	        
	        for (VisitItemCreateRequest itemReq : request.items()) {
	        	
	        	VisitItemPriority prioridadItem = parsePriority(itemReq.prioridad());	
	        	
		            if (itemReq.id() != null) {
                        // Actualizar existente
                        VisitItem item = itemRepository.findById(itemReq.id())
                                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
                        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:21 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
                        if (item.getPlan() == null || !Objects.equals(item.getPlan().getId(), planId)) {
                            throw new BusinessException("El item no pertenece al plan");
                        }
		
		                String prevCompanyName = item.getCompanyName();
		                String prevDireccion = item.getDireccion();                
		                var prevPriority = item.getPriority();
		                String prevTemplate = item.getPvTemplate();
		                var prevTargetTime = item.getTargetTime();
		
		                item.setCompanyName(itemReq.companyName());
		                item.setDireccion(itemReq.direccion());
		                item.setTargetTime(itemReq.targetTime());                
		                item.setPriority(prioridadItem);
		                item.setPvTemplate(normalizeNullable(itemReq.plantillaPv()));                
		                item.setOrderIndex(order++);
		                item.setUpdatedBy(usuario);
		                item.setUpdatedFrom(terminal);
		                
		                Destino destino = new Destino();
		                destino.setId(itemReq.destinoId());
		                
		                item.setDestino(destino);
		                
		                itemRepository.save(item);
		
		                if (!Objects.equals(prevCompanyName, item.getCompanyName())) {
		                    recordHistory(
                                    item,
                                    VisitItemEventType.COMPANY_NAME_CHANGE,
                                    prevCompanyName,
                                    item.getCompanyName(),
                                    null,
                                    null,
                                    usuario,
                                    terminal,
                                    OffsetDateTime.now()
                            );
		                }

		                if (!Objects.equals(prevDireccion, item.getDireccion())) {
		                    recordHistory(
                                    item,
                                    VisitItemEventType.ADDRESS_CHANGE,
                                    prevDireccion,
                                    item.getDireccion(),
                                    null,
                                    null,
                                    usuario,
                                    terminal,
                                    OffsetDateTime.now()
                            );
		                }

		                if (!Objects.equals(prevPriority, item.getPriority())) {
		                    recordHistory(
                                    item,
                                    VisitItemEventType.PRIORITY_CHANGE,
                                    prevPriority != null ? prevPriority.name() : null,
                                    item.getPriority() != null ? item.getPriority().name() : null,
                                    null,
                                    null,
                                    usuario,
                                    terminal,
                                    OffsetDateTime.now()
                            );
		                }

		                if (!Objects.equals(prevTemplate, item.getPvTemplate())) {
		                    recordHistory(
                                    item,
                                    VisitItemEventType.PV_TEMPLATE_CHANGE,
                                    prevTemplate,
                                    item.getPvTemplate(),
                                    null,
                                    null,
                                    usuario,
                                    terminal,
                                    OffsetDateTime.now()
                            );
		                }

		                if (!Objects.equals(prevTargetTime, item.getTargetTime())) {
		                    recordHistory(
                                    item,
                                    VisitItemEventType.TARGET_TIME_CHANGE,
                                    prevTargetTime != null ? prevTargetTime.toString() : null,
                                    item.getTargetTime() != null ? item.getTargetTime().toString() : null,
                                    null,
                                    null,
                                    usuario,
                                    terminal,
                                    OffsetDateTime.now()
                            );
		                }
		            } 
		            else {
		                // Nuevo item
		                VisitItem newItem = new VisitItem();
		                newItem.setPlan(plan);
		                newItem.setCompanyName(itemReq.companyName());
		                newItem.setTargetTime(itemReq.targetTime());
		                newItem.setDireccion(itemReq.direccion());
		                newItem.setPriority(prioridadItem);
		                newItem.setPvTemplate(normalizeNullable(itemReq.plantillaPv()));
		                newItem.setOrderIndex(order++);
		                newItem.setState(VisitItemState.PENDING);
		                newItem.setCreatedBy(usuario);
		                newItem.setCreatedFrom(terminal);
		                
		                Destino destino = new Destino();
		                destino.setId(itemReq.destinoId());
		                
		                newItem.setDestino(destino);		                
		                
		                itemRepository.save(newItem);
		            }
	        	//}
	        }   
        //}

        return toPlanResponse(updatedPlan);
    }

    @Transactional(readOnly = true)
    public VisitPlanResponse getPlan(Long planId) {
        VisitPlan plan = planRepository.findByIdAndStRegi(planId, UConstante.ACTIVO_REGI)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));
        return toPlanResponse(plan);
    }


    @Transactional(readOnly = true)
    public VisitPlanResponse getPlanForVerifier(SaaPrincipal principal) {
        User verifier = userService.getEntityBySaaSubject(principal.getName());
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-08 13:56 UTC-5 (Lima)][desc: Devuelve plan del día del colaborador incluso si está completado][obj: VisitPlanService.getPlanForVerifier]
        VisitPlan plan = planRepository
                .findFirstByVerifierIdAndPlannedForAndStRegiOrderByCreatedAtDesc(
                        verifier.getId(),
                        hoy,
                        UConstante.ACTIVO_REGI)
                .orElseThrow(() -> new ResourceNotFoundException("No hay plan asignado al colaborador para hoy"));
        return toPlanResponse(plan);
    }

    public VisitItemResponse addItem(Long planId, VisitItemCreateRequest request, String usuario, String terminal) {
        VisitPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 10:03 UTC-5 (Lima)][desc: Calcula el siguiente orden excluyendo items eliminados lógicamente][obj: VisitPlanService.addItem]
        Integer nextOrder = itemRepository.findByPlanIdAndStateNotOrderByOrderIndexAsc(planId, VisitItemState.DELETED)
                .stream()
                .map(VisitItem::getOrderIndex)
                .max(Integer::compareTo)
                .map(i -> i + 1)
                .orElse(1);

        VisitItem item = new VisitItem();
        item.setPlan(plan);
        item.setCompanyName(request.companyName());
        item.setTargetTime(request.targetTime());
        item.setDireccion(request.direccion());
        item.setPriority(parsePriority(request.prioridad()));
        item.setPvTemplate(normalizeNullable(request.plantillaPv()));
        item.setOrderIndex(nextOrder);
        item.setState(VisitItemState.PENDING);
        item.setCreatedBy(usuario);
        item.setCreatedFrom(terminal);

        VisitItem saved = itemRepository.save(item);
        return toItemResponse(saved);
    }

    public VisitPlanResponse reorderItems(Long planId, VisitItemsReorderRequest request, String usuario, String terminal) {
        VisitPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 10:03 UTC-5 (Lima)][desc: Reordena solo items activos (excluye eliminados lógicamente)][obj: VisitPlanService.reorderItems]
        List<VisitItem> items = itemRepository.findByPlanIdAndStateNotOrderByOrderIndexAsc(planId, VisitItemState.DELETED);
        if (items.size() != request.itemIds().size()) {
            throw new BusinessException("La lista de items no coincide con el plan");
        }

        Map<Long, VisitItem> itemMap = items.stream().collect(Collectors.toMap(VisitItem::getId, i -> i));
        int order = 1;
        for (Long id : request.itemIds()) {
            VisitItem item = itemMap.get(id);
            if (item == null) {
                throw new BusinessException("El item " + id + " no pertenece al plan");
            }
            Integer previousOrder = item.getOrderIndex();
            item.setOrderIndex(order++);
            item.setUpdatedBy(usuario);
            item.setUpdatedFrom(terminal);
            
            if (!Objects.equals(previousOrder, item.getOrderIndex())) {
                recordHistory(
                        item,
                        VisitItemEventType.ORDER_CHANGE,
                        String.valueOf(previousOrder),
                        String.valueOf(item.getOrderIndex()),
                        null,
                        null,
                        usuario,
                        terminal,
                        OffsetDateTime.now()
                );
            }
        }
        itemRepository.saveAll(items);
        return toPlanResponse(plan);
    }

    public VisitItemResponse updateState(Long itemId, VisitItemStateChangeRequest request, SaaPrincipal principal, String usuario, String terminal) {
        VisitItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 10:05 UTC-5 (Lima)][desc: Evita operar sobre visitas eliminadas lógicamente][obj: VisitPlanService.updateState]
        if (item.getState() == VisitItemState.DELETED) {
            throw new ResourceNotFoundException("Visita no encontrada");
        }
        VisitPlan plan = item.getPlan();
        User actor = userService.getEntityBySaaSubject(principal.getName());
        validateVerifier(plan, actor);

        VisitItemState target = request.newState();
        validateTransition(item, target, plan.getVerifier().getId());

        VisitItemState previous = item.getState();
        item.setState(target);

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Usa la fecha/hora real del dispositivo; si no viene (clientes legacy) usa now() como fallback][obj: VisitPlanService.updateState occurredAt]
        OffsetDateTime occurredAt = request.occurredAt() != null ? request.occurredAt() : OffsetDateTime.now();

        if ((target == VisitItemState.EN_ROUTE || target == VisitItemState.IN_VISIT)
                && item.getStartTime() == null) {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:10 UTC-5 (Lima)][desc: Registra inicio al pasar a EN_ROUTE (si aún no tiene startTime)][obj: VisitPlanService.updateState startTime]
            item.setStartTime(occurredAt);
        }

        if (target == VisitItemState.EN_ROUTE
                && plan.getStartAt() == null
                && request.startLatitude() != null
                && request.startLongitude() != null) {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Registra coordenadas del inicio del plan en el primer EN_ROUTE][obj: VisitPlanService.updateState plan start coords]
            plan.setStartLatitude(request.startLatitude());
            plan.setStartLongitude(request.startLongitude());
            plan.setStartAt(occurredAt);
            plan.setUpdatedBy(usuario);
            plan.setUpdatedFrom(terminal);
        }

        if (target == VisitItemState.DONE && item.getEndTime() == null) {
            item.setEndTime(occurredAt);
        }

        item.setUpdatedBy(usuario);
        item.setUpdatedFrom(terminal);

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Propaga coordenadas del evento para cierre del plan cuando corresponde][obj: VisitPlanService.updateState plan end coords]
        updatePlanStatus(
                plan,
                target,
                request.eventLatitude(),
                request.eventLongitude(),
                occurredAt
        );

        recordHistory(
                item,
                VisitItemEventType.STATE_CHANGE,
                previous.name(),
                target.name(),
                request.eventLatitude(),
                request.eventLongitude(),
                usuario,
                terminal,
                occurredAt
        );
        
        return toItemResponse(item);
    }

    public VisitItemResponse completeVisit(Long itemId, VisitItemCompleteRequest request, SaaPrincipal principal, String usuario, String terminal) {
        VisitItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 10:05 UTC-5 (Lima)][desc: Evita operar sobre visitas eliminadas lógicamente][obj: VisitPlanService.completeVisit]
        if (item.getState() == VisitItemState.DELETED) {
            throw new ResourceNotFoundException("Visita no encontrada");
        }
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-18 09:00 UTC-5 (Lima)][desc: Idempotencia: si ya está DONE, retorna sin error (evita bloqueo de cola offline en reintento)][obj: VisitPlanService.completeVisit]
        if (item.getState() == VisitItemState.DONE) {
            return toItemResponse(item);
        }

        VisitPlan plan = item.getPlan();
        User actor = userService.getEntityBySaaSubject(principal.getName());
        validateVerifier(plan, actor);

        if (item.getState() != VisitItemState.IN_VISIT) {
            throw new BusinessException("Solo puedes completar visitas en curso");
        }

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Usa la fecha/hora real del dispositivo; si no viene (clientes legacy) usa now() como fallback][obj: VisitPlanService.completeVisit occurredAt]
        OffsetDateTime occurredAt = request.occurredAt() != null ? request.occurredAt() : OffsetDateTime.now();

        item.setOtherInfo(request.otherInfo());
        item.setEndTime(occurredAt);

        VisitItemState previous = item.getState();
        item.setState(VisitItemState.DONE);
        item.setUpdatedBy(usuario);
        item.setUpdatedFrom(terminal);

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Propaga coordenadas del evento al cerrar visita via /complete][obj: VisitPlanService.completeVisit plan end coords]
        updatePlanStatus(
                plan,
                VisitItemState.DONE,
                request.eventLatitude(),
                request.eventLongitude(),
                occurredAt
        );

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Registra coordenadas del cierre en histórico de visita][obj: VisitPlanService.completeVisit history coords]
        recordHistory(
                item,
                VisitItemEventType.STATE_CHANGE,
                previous.name(),
                VisitItemState.DONE.name(),
                request.eventLatitude(),
                request.eventLongitude(),
                usuario,
                terminal,
                occurredAt
        );
        
        return toItemResponse(item);
    }

    private void validateTransition(VisitItem item, VisitItemState target, Long verifierId) {
        VisitItemState current = item.getState();
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-18 09:00 UTC-5 (Lima)][desc: Idempotencia ampliada: si el estado actual ya superó el objetivo (replay offline), ignorar silenciosamente; excluye DELETED para preservar su error][obj: VisitPlanService.validateTransition]
        if (current != VisitItemState.DELETED && current.ordinal() >= target.ordinal()) return;
        if (current == VisitItemState.DELETED) {
            throw new BusinessException("La visita fue eliminada");
        }
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Bloquea cambios de estado en visitas pendientes de reprogramacion][obj: VisitPlanService.validateTransition]
        if (current == VisitItemState.PENDING_REPROGRAMAR) {
            throw new BusinessException("La visita esta pendiente de reprogramacion");
        }
        if (current == VisitItemState.DONE || current == VisitItemState.CANCELLED) {
            throw new BusinessException("La visita ya fue cerrada");
        }
        if (target == VisitItemState.IN_VISIT) {
            long active = itemRepository.countByPlanVerifierIdAndState(verifierId, VisitItemState.IN_VISIT);
            boolean anotherActive = active > 0 && current != VisitItemState.IN_VISIT;
            if (anotherActive) {
                throw new BusinessException("Ya existe una visita en curso, termina antes de iniciar otra");
            }
        }
        if (target == VisitItemState.DONE && current != VisitItemState.IN_VISIT) {
            throw new BusinessException("Debes iniciar la visita antes de marcarla como completada");
        }
        if (!isAllowedTransition(current, target)) {
            throw new BusinessException("Transición inválida de " + current + " a " + target);
        }
    }

    private boolean isAllowedTransition(VisitItemState current, VisitItemState target) {
        return switch (current) {
            case PENDING -> target == VisitItemState.EN_ROUTE
                    || target == VisitItemState.ON_SITE
                    || target == VisitItemState.IN_VISIT
                    || target == VisitItemState.DONE;
            case PENDING_REPROGRAMAR -> false;
            case EN_ROUTE -> target == VisitItemState.ON_SITE
                    || target == VisitItemState.IN_VISIT
                    || target == VisitItemState.DONE;
            case ON_SITE -> target == VisitItemState.IN_VISIT || target == VisitItemState.DONE;
            case IN_VISIT -> target == VisitItemState.DONE;
            default -> false;
        };
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Acepta occurredAt para grabar la fecha/hora real del dispositivo en el fin del plan][obj: VisitPlanService.updatePlanStatus]
    private void updatePlanStatus(
            VisitPlan plan,
            VisitItemState state,
            Double eventLatitude,
            Double eventLongitude,
            OffsetDateTime occurredAt
    ) {
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 14:57 UTC-5 (Lima)][desc: Marca plan IN_PROGRESS al iniciar recorrido (EN_ROUTE/ON_SITE/IN_VISIT)][obj: VisitPlanService.updatePlanStatus]
        if ((state == VisitItemState.EN_ROUTE
                || state == VisitItemState.ON_SITE
                || state == VisitItemState.IN_VISIT)
                && plan.getStatus() == VisitPlanStatus.PLANNED) {
            plan.setStatus(VisitPlanStatus.IN_PROGRESS);
        }
        if (state == VisitItemState.DONE) {
            boolean allDone = itemRepository.findByPlanIdAndStateNotOrderByOrderIndexAsc(plan.getId(), VisitItemState.DELETED)
                    .stream()
                    .allMatch(i -> i.getState() == VisitItemState.DONE);

            if (allDone) {
                plan.setStatus(VisitPlanStatus.COMPLETED);
                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Guarda coordenadas y fecha del fin del plan][obj: VisitPlanService.updatePlanStatus finish coords]
                if (plan.getEndAt() == null) {
                    plan.setEndAt(occurredAt);
                }
                if (eventLatitude != null && plan.getEndLatitude() == null) {
                    plan.setEndLatitude(eventLatitude);
                }
                if (eventLongitude != null && plan.getEndLongitude() == null) {
                    plan.setEndLongitude(eventLongitude);
                }
            }
        }
    }


    private void validateVerifier(VisitPlan plan, User actor) {
        if (!Objects.equals(plan.getVerifier().getId(), actor.getId())) {
            throw new BusinessException("Solo el colaborador asignado puede modificar la visita");
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Acepta occurredAt para grabar la fecha/hora real del evento; las operaciones administrativas pasan OffsetDateTime.now()][obj: VisitPlanService.recordHistory]
    private void recordHistory(VisitItem item,
                               VisitItemEventType eventType,
                               String previous,
                               String current,
                               Double latitude,
                               Double longitude,
                               String usuario,
                               String terminal,
                               OffsetDateTime occurredAt) {

        VisitItemHistory history = new VisitItemHistory();
        history.setItem(item);
        history.setEventType(eventType);
        history.setPreviousValue(previous);
        history.setNewValue(current);
        history.setLatitude(latitude);
        history.setLongitude(longitude);
        history.setCreatedBy(usuario);
        history.setCreatedFrom(terminal);
        history.setEventAt(occurredAt);

        historyRepository.save(history);
    }

    private VisitPlanResponse toPlanResponse(VisitPlan plan) {
    	
        List<VisitItemResponse> items = itemRepository
                .findByPlanIdAndStateNotOrderByOrderIndexAsc(plan.getId(), VisitItemState.DELETED)
                .stream()
                .sorted(Comparator
                        .comparingInt((VisitItem i) -> priorityRank(i.getPriority()))
                        .thenComparing(
                                VisitItem::getOrderIndex,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                .map(this::toItemResponse)
                .toList();
        
        Long idEquipo = null;
        String nombreEquipo = null;
        
        if(plan.getVerifier().getEquipo() != null) {
        	idEquipo = plan.getVerifier().getEquipo().getId().longValue();
        	nombreEquipo = plan.getVerifier().getEquipo().getNombre();
        }
        
        VisitPlanResponse respuesta = new VisitPlanResponse(
                plan.getId(),
                plan.getTitle(),
                plan.getPlannedFor(),
                plan.getStatus(),
                plan.getVerifier().getId(),
                plan.getVerifier().getNombre(),
                nombreEquipo,
                idEquipo,
                plan.getVerifier().getId(),
                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Incluye coordenadas y fecha de inicio del plan][obj: VisitPlanService.toPlanResponse start coords]
                plan.getStartLatitude(),
                plan.getStartLongitude(),
                plan.getStartAt(),
                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Incluye coordenadas y fecha de fin del plan][obj: VisitPlanService.toPlanResponse finish coords]
                plan.getEndLatitude(),
                plan.getEndLongitude(),
                plan.getEndAt(),
                items);
                
        return respuesta;
    }

    private int priorityRank(VisitItemPriority priority) {
        if (priority == null) return 2;
        return switch (priority) {
            case MUY_ALTA -> 0;
            case ALTA -> 1;
            case NORMAL -> 2;
        };
    }

	    private VisitItemResponse toItemResponse(VisitItem item) {
	        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-06 00:00 UTC-5 (Lima)][desc: En plan de visitas, expone lat/lng del destino y evita NPE si ID_DEST es null][obj: VisitPlanService.toItemResponse destino coords]
	        final Destino destino = item.getDestino();
	        final Long destinoId = destino != null ? destino.getId() : null;
	        final String destinoNombre = destino != null ? destino.getNombre() : null;
	        final Double lat = destino != null ? destino.getLatitud() : null;
	        final Double lng = destino != null ? destino.getLongitud() : null;

	        return new VisitItemResponse(
	                item.getId(),
	                item.getCompanyName(),
	                item.getTargetTime(),
	                item.getOrderIndex(),
	                item.getPriority() != null ? item.getPriority().name() : VisitItemPriority.NORMAL.name(),
	                item.getPvTemplate(),
	                item.getState(),
	                item.getStartTime(),
	                item.getEndTime(),
	                item.getOtherInfo(),
	                item.getDireccion(),
	                destinoId,
	                destinoNombre,
	                lat,
	                lng
	        );
	    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:20 UTC-5 (Lima)][desc: Expone items activos para export Excel de planes][obj: VisitPlanService.getItemsForExport]
    @Transactional(readOnly = true)
    public List<VisitItemResponse> getItemsForExport(Long planId) {
        return itemRepository.findByPlanIdAndStateNotOrderByOrderIndexAsc(planId, VisitItemState.DELETED)
                .stream()
                .map(this::toItemResponse)
                .toList();
    }

    private static VisitItemPriority parsePriority(String raw) {
        if (raw == null || raw.isBlank()) return VisitItemPriority.NORMAL;
        try {
            return VisitItemPriority.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            return VisitItemPriority.NORMAL;
        }
    }

    private static String normalizeNullable(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public RespuestaBusquedaDTO<VisitPlanResponse> buscarPlanes(String idPersona, LocalDate fechaPlan,
	    	   int pagina, 
	           int tamanioPagina,
	           String orden, 
	           String columnaOrden) 
	{
    	RespuestaBusquedaDTO<VisitPlanResponse> respuesta = new RespuestaBusquedaDTO<>();
    	Page<VisitPlan> pageResult = null;
    	
		//Determinar el ordenamiento
		Sort sort = UConstante.ORDEN_DESCENDENTE.equalsIgnoreCase(orden)
		? Sort.by(columnaOrden).descending()
		: Sort.by(columnaOrden).ascending();
		
		//La paginación en JPA es 0-indexada, por ello se resta 1 al numero de pagina recibido.
		Pageable pageable = PageRequest.of(pagina - 1, tamanioPagina, sort);		
		        
		//Pasar la lista de ids de personas a una coleccion
		Collection<Long> ids = null;

	    if (idPersona != null && !idPersona.isBlank()) {
	        ids = Arrays.stream(idPersona.split(","))
	                .map(String::trim)
	                .map(Long::valueOf)
	                .toList();
	    }
	    
	    //Ejecutar la bsuqueda
		pageResult = planRepository.buscarPlanes(ids, fechaPlan, pageable);
		long totalRegistros = pageResult.getTotalElements();
		
		Page<VisitPlanResponse> resultado = pageResult.map(this::toPlanResponse);
		
		if (resultado.getContent().isEmpty()) {
    		respuesta.setCodigoResultado(UConstante.RESULTADO_NO_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("No se encontraron planes con los criterios especificados.");
    		respuesta.setResultados(List.of());
    		respuesta.setTotalPaginas(0);
    		respuesta.setTotalRegistros(0);
    	} else {
    		respuesta.setCodigoResultado(UConstante.RESULTADO_SI_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("Búsqueda exitosa.");
    		respuesta.setResultados(resultado.getContent()); // Contenido de la página actual
    		respuesta.setTotalPaginas(resultado.getTotalPages()); // Total de páginas
    		respuesta.setPaginaActual(pagina);
    		respuesta.setTamanioPagina(tamanioPagina);
    		respuesta.setTotalRegistros(totalRegistros);
    	}
		
		return respuesta;
	}
    
    
    /**
     * Determina si ya existe un plan de visita para una persona en una fecha.
     *
     * @param idPersona  id del colaborador
     * @param fechaPlan  fecha planificada
     * @return true si existe un plan válido para esa persona en la fecha indicada
     */
    @Transactional(readOnly = true)
    public boolean existePlanParaPersonaEnFecha(Long idPersona, LocalDate fechaPlan) {    	
    	
        return planRepository.existsByVerifierIdAndPlannedForAndStRegi(
                idPersona,
                fechaPlan,
                UConstante.ACTIVO_REGI
        );
    }
    
    
    /**
     * Determina si ya existe un plan de visita para una persona en una fecha y que el id del plan sea distinto al indicado.
     *
     * @param idPersona  id del colaborador
     * @param fechaPlan  fecha planificada
     * @return true si existe un plan válido para esa persona en la fecha indicada
     */
    @Transactional(readOnly = true)
    public boolean existePlanParaPersonaEnFechaOtroId(Long idPersona, LocalDate fechaPlan, Long idPlanExcluido) {    	
    	
        return planRepository.existsByVerifierIdAndPlannedForAndIdNotAndStRegi(
                idPersona,
                fechaPlan,
                idPlanExcluido,
                UConstante.ACTIVO_REGI
        );
    }

    

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Reasigna visitas pendientes con historial y nuevo plan][obj: VisitPlanService.reprogramarItem]
    public VisitItemPendingReprogramResponse reprogramarItem(
            Long itemId,
            VisitItemReassignRequest request,
            SaaPrincipal principal,
            String usuario, 
            String terminal
    ) {
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:44 UTC-5 (Lima)][desc: Permite reprogramar con perfil admin sin usuario PERSONAL][obj: VisitPlanService.reprogramarItem admin bypass]
        boolean esAdminSistema = isAdminSistema(principal);
        User supervisor = esAdminSistema ? null : resolveSupervisor(principal);
        
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:59 UTC-5 (Lima)][desc: Log de ingreso a reprogramacion][obj: VisitPlanService.reprogramarItem]
        log.info("Reprogramacion solicitud itemId={} supervisor={} nuevoVerifierId={} nuevaFecha={}",
                itemId, supervisor != null ? supervisor.getId() : null, request.newVerifierId(), request.newPlannedFor());
        
        if (!esAdminSistema) {
            Set<Long> supervisadosIds = resolveSupervisados(supervisor);
            if (!supervisadosIds.contains(request.newVerifierId())) {
                throw new BusinessException("El colaborador no pertenece a tu supervision.");
            }
        }

        VisitItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));

        if (item.getState() != VisitItemState.PENDING_REPROGRAMAR) {
            throw new BusinessException("La visita no esta pendiente de reprogramacion.");
        }

        VisitPlan oldPlan = item.getPlan();
        Long oldPlanId = oldPlan.getId();
        Long oldVerifierId = oldPlan.getVerifier().getId();
        LocalDate oldPlannedFor = oldPlan.getPlannedFor();

        VisitPlan newPlan = planRepository
                .findFirstByVerifierIdAndPlannedForAndStRegiOrderByCreatedAtDesc(
                        request.newVerifierId(),
                        request.newPlannedFor(),
                        UConstante.ACTIVO_REGI
                )
                .orElseGet(() -> {
                    VisitPlan plan = new VisitPlan();
                    User verifier = userService.getEntity(request.newVerifierId());
                    plan.setVerifier(verifier);
                    plan.setStatus(VisitPlanStatus.PLANNED);
                    plan.setTitle(null);
                    plan.setPlannedFor(request.newPlannedFor());
                    plan.setCreatedBy(usuario);
                    plan.setCreatedFrom(terminal);
                    plan.setStRegi(UConstante.ACTIVO_REGI);
                    return planRepository.save(plan);
                });

        int nextOrder = itemRepository.findByPlanIdAndStateNotOrderByOrderIndexAsc(
                        newPlan.getId(),
                        VisitItemState.DELETED
                )
                .stream()
                .map(VisitItem::getOrderIndex)
                .max(Integer::compareTo)
                .map(i -> i + 1)
                .orElse(1);

        item.setPlan(newPlan);
        item.setState(VisitItemState.PENDING);
        item.setOrderIndex(nextOrder);
        item.setUpdatedBy(usuario);
        item.setUpdatedFrom(terminal);
        itemRepository.save(item);

        recordHistory(
                item,
                VisitItemEventType.REPROGRAM_ASSIGN,
                String.valueOf(oldPlanId),
                String.valueOf(newPlan.getId()),
                null,
                null,
                usuario,
                terminal,
                OffsetDateTime.now()
        );

        VisitItemReassignHistory history = new VisitItemReassignHistory();
        history.setItem(item);
        history.setPreviousPlanId(oldPlanId);
        history.setNewPlanId(newPlan.getId());
        history.setPreviousVerifierId(oldVerifierId);
        history.setNewVerifierId(newPlan.getVerifier().getId());
        history.setPreviousPlannedFor(oldPlannedFor);
        history.setNewPlannedFor(newPlan.getPlannedFor());
        history.setReason(normalizeNullable(request.reason()));
        history.setCreatedBy(usuario);
        history.setCreatedFrom(terminal);
        history.setStRegi(UConstante.ACTIVO_REGI);
        
        reassignHistoryRepository.save(history);

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:59 UTC-5 (Lima)][desc: Log de reprogramacion completada][obj: VisitPlanService.reprogramarItem]
        log.info("Reprogramacion completada itemId={} planAnterior={} planNuevo={} verifierAnterior={} verifierNuevo={}",
                item.getId(), oldPlanId, newPlan.getId(), oldVerifierId, newPlan.getVerifier().getId());
        return toPendingReprogramResponse(item);
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:13 UTC-5 (Lima)][desc: Lista colaboradores con plan en fecha para reprogramacion][obj: VisitPlanService.listarColaboradoresConPlan]
    @Transactional(readOnly = true)
    public List<UserResponse> listarColaboradoresConPlanNoCompletado(
            LocalDate fechaPlan,
            String idPersona
    ) {
        log.info("listarColaboradoresConPlanNoCompletado - fechaPlan: "+fechaPlan);
        log.info("listarColaboradoresConPlanNoCompletado - idPersona: "+idPersona);
        
        if (fechaPlan == null) {
            throw new BusinessException("La fecha de plan es obligatoria.");
        }
        
        //Pasar la lista de ids de personas a una coleccion
  		Collection<Long> ids = null;

  	    if (idPersona != null && !idPersona.isBlank()) {
  	        ids = Arrays.stream(idPersona.split(","))
  	                .map(String::trim)
  	                .map(Long::valueOf)
  	                .toList();
  	    }
        
        
        List<User> usuarios = planRepository.findVerifiersWithPlanNotCompleted(fechaPlan, ids, VisitPlanStatus.COMPLETED);
        
        return usuarios.stream()
                .map(this::toUserResponseSimple)
                .toList();
    }
    
    
    /******************************************************************************/
    /************************* Metodos para la carga masiva ***********************/
    /**
     * @throws Exception ****************************************************************************/
    @Transactional
    public ResultadoValidacion registrarVisitasMasivas(VisitaPlanMasivoRequest request, String usuario, String terminal) {

    	//Validacion inicial de los datos
    	ResultadoValidacion resultado = validarVisitas(request.getVisitas()); 
    	if(resultado.getTipoResultado() == TipoResultadoValidacion.CON_ERRORES) {
    		return resultado;
    	}
    	
    	//Leer los datos con estructura
    	Map<PlanKey, List<VisitItemMasivo>> agrupado = agrupar(request.getVisitas());
    	
    	//Validar que los planes no existan
    	resultado = validarPlanExiste(agrupado);    	
    	if(resultado.getTipoResultado() == TipoResultadoValidacion.CON_ERRORES) {
    		return resultado;
    	}
    	
    	//Pasó la validacion, proceder a grabar.
        for (Map.Entry<PlanKey, List<VisitItemMasivo>> entry : agrupado.entrySet()) {

            PlanKey key = entry.getKey();
            List<VisitItemMasivo> items = entry.getValue();

            // 1️⃣ Crear plan
            VisitPlan plan = new VisitPlan();
            
            User colaborador = userService.getEntity(key.verifierId());
            plan.setVerifier(colaborador);
            
            plan.setPlannedFor(key.plannedFor());
            plan.setStatus(VisitPlanStatus.PLANNED);
            plan.setCreatedBy(usuario);            
            plan.setCreatedFrom(terminal);
            plan.setStRegi(UConstante.ACTIVO_REGI);            

            planRepository.save(plan);

            // Crear items
            guardarItems(plan, items, usuario, terminal);
        }
        
        //Si llega a este punto, todo bien.
        resultado.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);
        resultado.setMensaje("Proceso terminado exitósamente.");
        
        return resultado;
    }

    // ===============================
    // Agrupador
    // ===============================
    private Map<PlanKey, List<VisitItemMasivo>> agrupar(
            List<VisitItemMasivo> visitas) {

        Map<PlanKey, List<VisitItemMasivo>> resultado = new HashMap<>();

        for (VisitItemMasivo item : visitas) {

            Long verifierId = item.getColaboradorId();
            LocalDate fecha = LocalDate.parse(item.getFecha());

            PlanKey key = new PlanKey(verifierId, fecha);

            resultado
                .computeIfAbsent(key, k -> new ArrayList<>())
                .add(item);
        }

        return resultado;
    }

    // ===============================
    // Guardar items
    // ===============================
    private void guardarItems(
            VisitPlan plan,
            List<VisitItemMasivo> items,
            String usuario,
            String terminal) {

        int orden = 1;

        for (VisitItemMasivo src : items) {

            VisitItem item = new VisitItem();
            item.setPlan(plan);
            item.setCompanyName(src.getDestinoNombre());
            item.setDireccion(src.getDireccion());
            item.setOrderIndex(orden++);
            item.setCreatedBy(usuario);
            item.setCreatedFrom(terminal);
            item.setPvTemplate(src.getPlantillaPv());
            item.setState(VisitItemState.PENDING);

            if (src.getPrioridad() != null) {
                item.setPriority(
                    VisitItemPriority.valueOf(src.getPrioridad())
                );
            }

            if (src.getDestinoId() != null) {
            	
            	Destino destino = destinoRepository.getReferenceById(src.getDestinoId()); 
                item.setDestino(destino);
            }

            if (src.getHoraCita() != null && !src.getHoraCita().isBlank()) {
                item.setTargetTime(
                    OffsetDateTime.of(
                        plan.getPlannedFor(),
                        LocalTime.parse(src.getHoraCita()),
                        ZoneOffset.ofHours(-5)
                    )
                );
            }

            itemRepository.save(item);
        }
    }    
    
    
    // =======================================
    // Validaciones
    // =======================================    
    private ResultadoValidacion validarPlanExiste(Map<PlanKey, List<VisitItemMasivo>> agrupado) {
    	
    	ResultadoValidacion resultado = new ResultadoValidacion();
    	resultado.setTipoResultado(TipoResultadoValidacion.CON_ERRORES); //Valor predeterminado
    	
    	for (Map.Entry<PlanKey, List<VisitItemMasivo>> entry : agrupado.entrySet()) {
    		
    		PlanKey key = entry.getKey();            
            boolean existe = planRepository.existsByVerifierIdAndPlannedForAndStRegi(key.verifierId(), key.plannedFor(), UConstante.ACTIVO_REGI);

    	    if (existe) {    	  
    	    	User colaborador = userService.getEntity(key.verifierId());    	    	
    	    	String fecha = UFecha.localDateToString(key.plannedFor());
    	    	
    	        resultado.setMensaje("Ya existe un plan registrado para el colaborador '" + colaborador.getNombre() + "' en la fecha '" + fecha + "'.");
    	        return resultado;
    	    }
    	}
    	
    	//Si llega a este punto ningun plan existe.
    	resultado.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);
    	
    	return resultado;
    }   
    
    
    private ResultadoValidacion validarVisitas(List<VisitItemMasivo> visitas) {

    	ResultadoValidacion resultado = new ResultadoValidacion();
    	resultado.setTipoResultado(TipoResultadoValidacion.CON_ERRORES); //Valor predeterminado
    	
        if (visitas == null || visitas.isEmpty()) {
        	resultado.setMensaje("Debe agregar al menos una fila de visita.");
        	return resultado;
        }

        Set<String> clavesUnicas = new HashSet<>();
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < visitas.size(); i++) {

            VisitItemMasivo v = visitas.get(i);
            int fila = i + 1;

            // 👤 Colaborador
            if (v.getColaboradorId() == null || v.getColaboradorId() == 0) {
                resultado.setMensaje("Seleccione un colaborador en la fila " + fila + ".");
            	return resultado;
            }

            // 📅 Fecha
            if (v.getFecha() == null || v.getFecha().isBlank()) {
                resultado.setMensaje("Seleccione la fecha en la fila " + fila + ".");
            	return resultado;
            }

            LocalDate fechaVisita;
            try {
                fechaVisita = LocalDate.parse(v.getFecha()); // yyyy-MM-dd
            } catch (Exception e) {
                resultado.setMensaje("La fecha tiene un formato inválido en la fila " + fila + ".");
            	return resultado;
            }

            if (fechaVisita.isBefore(hoy)) {
                resultado.setMensaje("La fecha no puede ser menor a la fecha actual en la fila " + fila + ".");
            	return resultado;
            }

            // 📍 Destino
            if (v.getDestinoId() == null) {
                resultado.setMensaje("Seleccione un destino en la fila " + fila + ".");
            	return resultado;
            }

            if (v.getDestinoNombre() == null || v.getDestinoNombre().isBlank()) {
                resultado.setMensaje("El destino es obligatorio en la fila " + fila + ".");
            	return resultado;
            }

            if (v.getDireccion() == null || v.getDireccion().isBlank()) {
                resultado.setMensaje("Ingrese la dirección en la fila " + fila + ".");
            	return resultado;
            }

            // ⏰ horaCita es opcional → NO se valida

            // 🔺 Prioridad
            if (v.getPrioridad() == null || v.getPrioridad().isBlank()) {
                resultado.setMensaje("Seleccione la prioridad en la fila " + fila + ".");
            	return resultado;
            }

            List<String> prioridadesValidas = List.of("MUY_ALTA", "ALTA", "NORMAL");
            if (!prioridadesValidas.contains(v.getPrioridad())) {
                resultado.setMensaje("La prioridad seleccionada no es válida en la fila " + fila + ".");
            	return resultado;
            }

            // 🧾 Plantilla PV
            if (v.getPlantillaPv() == null || v.getPlantillaPv().isBlank()) {
                resultado.setMensaje("Ingrese el código de plantilla en la fila " + fila + ".");
            	return resultado;
            }

            // ✅ Destino validado
            if (!v.isValidado()) {
                resultado.setMensaje("Debe validar el destino (coordenadas de la dirección) en la fila " + fila + ".");
            	return resultado;
            }

            // 🔁 Duplicados: colaborador + fecha + destino
            String clave = v.getColaboradorId()
                    + "|" + fechaVisita
                    + "|" + v.getDestinoId();

            if (!clavesUnicas.add(clave)) {
                resultado.setMensaje("Existen datos duplicados (colaborador, fecha, destino y dirección) en la fila " + fila + ".");
            	return resultado;
            }
        }
        
        //Si llega a este punto, todo Ok.
    	resultado.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);
    	
    	return resultado;
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Mapea visita pendiente de reprogramacion a DTO][obj: VisitPlanService.toPendingReprogramResponse]
    private VisitItemPendingReprogramResponse toPendingReprogramResponse(VisitItem item) {
        VisitPlan plan = item.getPlan();
        User verifier = plan.getVerifier();
        Long equipoId = null;
        String equipoNombre = null;
        if (verifier.getEquipo() != null) {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Ajusta casteo de id de equipo a Long][obj: VisitPlanService.toPendingReprogramResponse equipoId]
            equipoId = verifier.getEquipo().getId().longValue();
            equipoNombre = verifier.getEquipo().getNombre();
        }
        return new VisitItemPendingReprogramResponse(
                item.getId(),
                item.getCompanyName(),
                item.getDireccion(),
                item.getPriority() != null ? item.getPriority().name() : VisitItemPriority.NORMAL.name(),
                item.getPvTemplate(),
                item.getState(),
                item.getTargetTime(),
                plan.getId(),
                plan.getPlannedFor(),
                verifier.getId(),
                verifier.getNombre(),
                equipoId,
                equipoNombre
        );
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:13 UTC-5 (Lima)][desc: Mapea usuario a respuesta simple para reprogramacion][obj: VisitPlanService.toUserResponseSimple]
    private UserResponse toUserResponseSimple(User user) {
        Integer equipoId = null;
        String equipoNombre = null;
        if (user.getEquipo() != null) {
            equipoId = user.getEquipo().getId();
            equipoNombre = user.getEquipo().getNombre();
        }
        Integer estadoNumerico = user.getEstado() != null ? Integer.valueOf(user.getEstado()) : null;
        String estadoDescripcion = UConstante.ACTIVO_REGI.equals(user.getEstado())
                ? UConstante.ACTIVO_DESCRIPCION
                : UConstante.INACTIVO_DESCRIPCION;
        Long horarioId = null;
        String horarioNombre = null;
        if (user.getHorario() != null) {
            horarioId = user.getHorario().getId();
            horarioNombre = user.getHorario().getNombre();
        }
        return new UserResponse(
                user.getId(),
                user.getSaaSub(),
                user.getUsuario(),
                user.getNombre(),
                estadoNumerico,
                estadoDescripcion,
                equipoId,
                equipoNombre,
                horarioId,
                horarioNombre
        );
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Valida y obtiene IDs de supervisados para reprogramacion][obj: VisitPlanService.resolveSupervisados]
    private Set<Long> resolveSupervisados(User supervisor) {
        Set<Long> supervisadosIds = userService.getSupervisadosIds(supervisor.getId());
        if (supervisadosIds.isEmpty()) {
            throw new BusinessException("No tienes permisos para reprogramar visitas.");
        }
        return supervisadosIds;
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Permite resolver supervisor por usuario, saaSub o id][obj: VisitPlanService.resolveSupervisor]
    private User resolveSupervisor(SaaPrincipal principal) {
        return resolveSupervisor(principal, null);
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 16:44 UTC-5 (Lima)][desc: Resuelve supervisor con override idSupervisor][obj: VisitPlanService.resolveSupervisor]
    private User resolveSupervisor(SaaPrincipal principal, Long idSupervisor) {
        if (idSupervisor != null) {
            return userService.getEntity(idSupervisor);
        }
        ResourceNotFoundException lastError = null;

        if (principal != null) {
            String usuario = principal.getUsuario();
            if (usuario != null && !usuario.isBlank()) {
                try {
                    return userService.getEntityByUsuario(usuario);
                } catch (ResourceNotFoundException ex) {
                    lastError = ex;
                    // fallback
                }
            }
            String subject = principal.getName();
            if (subject != null && !subject.isBlank()) {
                try {
                    return userService.getEntityBySaaSubject(subject);
                } catch (ResourceNotFoundException ex) {
                    lastError = ex;
                }
                try {
                    return userService.getEntity(Long.parseLong(subject));
                } catch (Exception ex) {
                    if (ex instanceof ResourceNotFoundException rnfe) {
                        lastError = rnfe;
                    }
                }
            }
            Long claimId = extractClaimId(principal);
            if (claimId != null) {
                try {
                    return userService.getEntity(claimId);
                } catch (ResourceNotFoundException ex) {
                    lastError = ex;
                }
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new ResourceNotFoundException("Usuario no encontrado para el principal.");
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 16:16 UTC-5 (Lima)][desc: Extrae id desde claims del token cuando existe][obj: VisitPlanService.extractClaimId]
    private Long extractClaimId(SaaPrincipal principal) {
        if (principal == null || principal.getDetails() == null) {
            return null;
        }
        Map<String, Object> claims = principal.getDetails().claims();
        if (claims == null || claims.isEmpty()) {
            return null;
        }
        return readNumericClaim(claims, "idUsuaSist", "idUsuario", "IdUsuario", "id_pers", "idPers");
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 16:16 UTC-5 (Lima)][desc: Lee claims numericos del token][obj: VisitPlanService.readNumericClaim]
    private Long readNumericClaim(Map<String, Object> claims, String... keys) {
        for (String key : keys) {
            Object value = claims.get(key);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String str) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }



    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:44 UTC-5 (Lima)][desc: Determina si token tiene perfil admin del sistema][obj: VisitPlanService.isAdminSistema]
    private boolean isAdminSistema(SaaPrincipal principal) {
        if (principal == null || principal.getDetails() == null) {
            return false;
        }
        Map<String, Object> claims = principal.getDetails().claims();
        if (claims == null || claims.isEmpty()) {
            return false;
        }
        Long sistema = readNumericClaim(claims, "sistema", "idSistema", "IdSistema");
        log.info("1449 - sistema: "+sistema);
        Object perfiles = claims.get("PerfilPermiso");
        if (perfiles instanceof List<?> lista) {
            for (Object perfil : lista) {
                if (!(perfil instanceof Map<?, ?> perfilMap)) {
                    continue;
                }
                Long idPerfil = readNumericClaimFromMap(perfilMap, "idPerfil", "IdPerfil");
                Long idSistema = readNumericClaimFromMap(perfilMap, "idSistema", "IdSistema", "sistema");
                log.info("1458 - idPerfil: "+idPerfil);
                log.info("1459 - idSistema: "+idSistema);
                boolean sistemaOk = (idSistema != null && idSistema.equals(641L)) || (sistema != null && sistema.equals(641L));
                log.info("1461 - sistemaOK: "+sistemaOk);
                
                //FALTA: Cambiar para que valide contra el nombre del perfil.
                //4014L es el codigo del sistema Thaqhiri en el SAA, este dato puede cambiar en otro SAA
                if (idPerfil != null && idPerfil.equals(4014L) && sistemaOk) {
                    return true;
                }
            }
        }
        return false;
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:44 UTC-5 (Lima)][desc: Lee claims numericos desde mapas genericos][obj: VisitPlanService.readNumericClaimFromMap]
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:13 UTC-5 (Lima)][desc: Lee claims numericos desde mapas genericos][obj: VisitPlanService.readNumericClaimFromMap]
    private Long readNumericClaimFromMap(Map<?, ?> claims, String... keys) {
        for (String key : keys) {
            Object value = claims.get(key);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String str) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
    
    
    @Transactional
    public String eliminar(Long id, String usuarioSesion, String terminalSesion) {

    	//Validar que el plan exista y esté activo
    	Optional<VisitPlan> oVisitPlan = planRepository.findById(id);
    	
        if (!oVisitPlan.isPresent()) {
            return "El plan no existe.";
        }
        
        VisitPlan entidad = oVisitPlan.get();
        
        //Validar que el equipo no tenga personal asignado
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Ajusta comparacion de ST_REGI a string][obj: VisitPlanService.eliminar]
        if (!"1".equals(entidad.getStRegi())) {
        	return "El plan debe estar activo.";
        }
        
        if(entidad.getStatus() != VisitPlanStatus.PLANNED) {
        	return "El plan debe estar en estado PLANNED.";
        }

        //Eliminar
        int filas = planRepository.eliminarPlan(id, usuarioSesion, terminalSesion);
        
        return filas > 0 ? null : "No se pudo eliminar el plan indicado.";
    }
    
    

    public void marcarPendientesReprogramar(String usuario, String terminal) {
    	
        List<VisitItem> pendientes = itemRepository.findPendientesParaReprogramar(
                LocalDate.now(),
                List.of(
                        VisitItemState.PENDING,
                        VisitItemState.EN_ROUTE,
                        VisitItemState.ON_SITE,
                        VisitItemState.IN_VISIT
                )
        );

        if (pendientes.isEmpty()) {
            return;
        }

        for (VisitItem item : pendientes) {
            VisitItemState prevState = item.getState();
            item.setState(VisitItemState.PENDING_REPROGRAMAR);
            item.setUpdatedBy(usuario);
            item.setUpdatedFrom(terminal);
            
            recordHistory(
                    item,
                    VisitItemEventType.REPROGRAM_MARK,
                    prevState != null ? prevState.name() : null,
                    VisitItemState.PENDING_REPROGRAMAR.name(),
                    null,
                    null,
                    usuario,
                    terminal,
                    OffsetDateTime.now()
            );
        }

        itemRepository.saveAll(pendientes);
    }    
    
    
    public RespuestaBusquedaDTO<VisitItemPendingReprogramResponse> buscarPendientesReprogramar(
            String idPersona,
            LocalDate fechaPlan,
            int pagina,
            int tamanioPagina,
            String usuario,
            String terminal
    ) {

        Sort sort = Sort.by(
                Sort.Order.desc("plan.plannedFor"),
                Sort.Order.asc("orderIndex"),
                Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(pagina - 1, tamanioPagina, sort);

        //Pasar la lista de ids de personas a una coleccion
  		Collection<Long> ids = null;

  	    if (idPersona != null && !idPersona.isBlank()) {
  	        ids = Arrays.stream(idPersona.split(","))
  	                .map(String::trim)
  	                .map(Long::valueOf)
  	                .toList();
  	    }
        
        Page<VisitItem> pageResult = itemRepository.findReprogramables(
        		                                        ids,
									                    VisitItemState.PENDING_REPROGRAMAR,
									                    fechaPlan,
									                    pageable
									        			);        

        RespuestaBusquedaDTO<VisitItemPendingReprogramResponse> respuesta = new RespuestaBusquedaDTO<>();
        
        if (pageResult.getContent().isEmpty()) {
            respuesta.setCodigoResultado(UConstante.RESULTADO_NO_ENCONTRO_FILAS);
            respuesta.setMensajeResultado("No se encontraron visitas pendientes de reprogramacion.");
            respuesta.setResultados(List.of());
            respuesta.setTotalPaginas(0);
            respuesta.setTotalRegistros(0);
            respuesta.setPaginaActual(pagina);
            respuesta.setTamanioPagina(tamanioPagina);
        }
        else {
	        Page<VisitItemPendingReprogramResponse> resultado = pageResult.map(this::toPendingReprogramResponse);
	        
	        respuesta.setCodigoResultado(UConstante.RESULTADO_SI_ENCONTRO_FILAS);
	        respuesta.setMensajeResultado("Busqueda exitosa.");
	        respuesta.setResultados(resultado.getContent());
	        respuesta.setTotalPaginas(resultado.getTotalPages());
	        respuesta.setPaginaActual(pagina);
	        respuesta.setTamanioPagina(tamanioPagina);
	        respuesta.setTotalRegistros(pageResult.getTotalElements());
        }
        
        return respuesta;
    }
    
}
