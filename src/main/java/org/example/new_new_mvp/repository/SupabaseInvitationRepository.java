package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Invitation;
import org.example.new_new_mvp.model.InvitationStatus;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupabaseInvitationRepository {

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Получить все приглашения
     */
    public Mono<List<Invitation>> findAll() {
        return supabaseService.select("invitations", Invitation.class);
    }

    /**
     * Найти приглашение по ID
     */
    public Mono<Optional<Invitation>> findById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(invitations -> invitations.isEmpty() ? Optional.empty() : Optional.of(invitations.get(0)));
    }

    /**
     * Найти приглашение по email
     */
    public Mono<Optional<Invitation>> findByEmail(String email) {
        Map<String, String> filters = Map.of("email", "eq." + email);
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(invitations -> invitations.isEmpty() ? Optional.empty() : Optional.of(invitations.get(0)));
    }

    /**
     * Найти приглашение по коду
     */
    public Mono<Optional<Invitation>> findByInvitationCode(String invitationCode) {
        Map<String, String> filters = Map.of("invitation_code", "eq." + invitationCode);
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(invitations -> invitations.isEmpty() ? Optional.empty() : Optional.of(invitations.get(0)));
    }

    /**
     * Найти приглашения по статусу
     */
    public Mono<List<Invitation>> findByStatus(InvitationStatus status) {
        Map<String, String> filters = Map.of("status", "eq." + status.name());
        return supabaseService.select("invitations", Invitation.class, filters);
    }

    /**
     * Найти приглашения по ID компании
     */
    public Mono<List<Invitation>> findByCompanyId(UUID companyId) {
        Map<String, String> filters = Map.of("company_id", "eq." + companyId.toString());
        return supabaseService.select("invitations", Invitation.class, filters);
    }

    /**
     * Найти приглашения по ID компании и статусу
     */
    public Mono<List<Invitation>> findByCompanyIdAndStatus(UUID companyId, InvitationStatus status) {
        Map<String, String> filters = Map.of(
            "company_id", "eq." + companyId.toString(),
            "status", "eq." + status.name()
        );
        return supabaseService.select("invitations", Invitation.class, filters);
    }

    /**
     * Подсчитать количество приглашений в компании
     */
    public Mono<Long> countByCompanyId(UUID companyId) {
        Map<String, String> filters = Map.of("company_id", "eq." + companyId.toString());
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(List::size)
                .cast(Long.class);
    }

    /**
     * Подсчитать количество приглашений по статусу
     */
    public Mono<Long> countByStatus(InvitationStatus status) {
        Map<String, String> filters = Map.of("status", "eq." + status.name());
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(List::size)
                .cast(Long.class);
    }

    /**
     * Сохранить приглашение
     */
    public Mono<Invitation> save(Invitation invitation) {
        return supabaseService.insert("invitations", invitation, Invitation.class)
                .cast(Invitation.class);
    }

    /**
     * Обновить приглашение
     */
    public Mono<Invitation> update(Invitation invitation) {
        Map<String, String> filters = Map.of("id", "eq." + invitation.getId().toString());
        return supabaseService.update("invitations", invitation, filters)
                .cast(Invitation.class);
    }

    /**
     * Удалить приглашение
     */
    public Mono<Void> deleteById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.delete("invitations", filters);
    }

    /**
     * Проверить существование приглашения
     */
    public Mono<Boolean> existsById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(invitations -> !invitations.isEmpty());
    }

    /**
     * Проверить существование приглашения по email и ID компании
     */
    public Mono<Boolean> existsByEmailAndCompanyId(String email, UUID companyId) {
        Map<String, String> filters = Map.of(
            "email", "eq." + email,
            "company_id", "eq." + companyId.toString()
        );
        return supabaseService.select("invitations", Invitation.class, filters)
                .map(invitations -> !invitations.isEmpty());
    }

    /**
     * Подсчитать все приглашения
     */
    public Mono<Long> count() {
        return supabaseService.select("invitations", Invitation.class)
                .map(List::size)
                .map(Long::valueOf);
    }
}
