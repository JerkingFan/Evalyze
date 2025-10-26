package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.ProfileSnapshot;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SupabaseProfileSnapshotRepository {

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Получить все снимки профилей
     */
    public Mono<List<ProfileSnapshot>> findAll() {
        return supabaseService.select("profile_snapshots", ProfileSnapshot.class);
    }

    /**
     * Найти снимок по ID
     */
    public Mono<Optional<ProfileSnapshot>> findById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("profile_snapshots", ProfileSnapshot.class, filters)
                .map(snapshots -> snapshots.isEmpty() ? Optional.empty() : Optional.of(snapshots.get(0)));
    }

    /**
     * Найти последние снимки по ID пользователя
     */
    public Mono<List<ProfileSnapshot>> findLatestSnapshotsByUserId(UUID userId) {
        Map<String, String> filters = Map.of("user_id", "eq." + userId.toString());
        return supabaseService.select("profile_snapshots", ProfileSnapshot.class, filters)
                .map(snapshots -> snapshots.stream()
                        .sorted((s1, s2) -> s2.getSnapshotDate().compareTo(s1.getSnapshotDate()))
                        .collect(Collectors.toList()));
    }

    /**
     * Найти снимки между датами
     */
    public Mono<List<ProfileSnapshot>> findSnapshotsBetweenDates(OffsetDateTime startDate, OffsetDateTime endDate) {
        return supabaseService.select("profile_snapshots", ProfileSnapshot.class)
                .map(snapshots -> snapshots.stream()
                        .filter(s -> s.getSnapshotDate().isAfter(startDate) && s.getSnapshotDate().isBefore(endDate))
                        .collect(Collectors.toList()));
    }

    /**
     * Сохранить снимок профиля
     */
    public Mono<ProfileSnapshot> save(ProfileSnapshot snapshot) {
        return supabaseService.insert("profile_snapshots", snapshot, ProfileSnapshot.class)
                .cast(ProfileSnapshot.class);
    }

    /**
     * Обновить снимок
     */
    public Mono<ProfileSnapshot> update(ProfileSnapshot snapshot) {
        Map<String, String> filters = Map.of("id", "eq." + snapshot.getId().toString());
        return supabaseService.update("profile_snapshots", snapshot, filters)
                .cast(ProfileSnapshot.class);
    }

    /**
     * Удалить снимок
     */
    public Mono<Void> deleteById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.delete("profile_snapshots", filters);
    }

    /**
     * Найти снимки по ID пользователя с сортировкой по дате
     */
    public Mono<List<ProfileSnapshot>> findByUserIdOrderBySnapshotDateDesc(UUID userId) {
        return findLatestSnapshotsByUserId(userId);
    }

    /**
     * Подсчитать все снимки
     */
    public Mono<Long> count() {
        return supabaseService.select("profile_snapshots", ProfileSnapshot.class)
                .map(List::size)
                .cast(Long.class);
    }
}

