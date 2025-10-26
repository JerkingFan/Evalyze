package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.JobRole;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobRoleRepository {

    @Autowired
    private SupabaseService supabaseService;

    public Mono<List<JobRole>> findAll() {
        return supabaseService.select("job_roles", JobRole.class);
    }

    public Mono<Optional<JobRole>> findById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("job_roles", JobRole.class, filters)
                .map(roles -> roles.isEmpty() ? Optional.empty() : Optional.of(roles.get(0)));
    }

    public Mono<List<JobRole>> findByCompanyId(UUID companyId) {
        Map<String, String> filters = Map.of("company_id", "eq." + companyId.toString());
        return supabaseService.select("job_roles", JobRole.class, filters);
    }

    public Mono<List<JobRole>> findByRoleType(String roleType) {
        Map<String, String> filters = Map.of("role_type", "eq." + roleType);
        return supabaseService.select("job_roles", JobRole.class, filters);
    }

    public Mono<List<JobRole>> findByCompanyIdAndRoleType(UUID companyId, String roleType) {
        Map<String, String> filters = Map.of(
            "company_id", "eq." + companyId.toString(),
            "role_type", "eq." + roleType
        );
        return supabaseService.select("job_roles", JobRole.class, filters);
    }

    public Mono<JobRole> save(JobRole jobRole) {
        return supabaseService.insert("job_roles", jobRole, JobRole.class)
                .cast(JobRole.class);
    }

    public Mono<JobRole> update(JobRole jobRole) {
        Map<String, String> filters = Map.of("id", "eq." + jobRole.getId().toString());
        return supabaseService.update("job_roles", jobRole, filters)
                .cast(JobRole.class);
    }

    public Mono<Void> deleteById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.delete("job_roles", filters);
    }

    public Mono<Long> count() {
        return supabaseService.select("job_roles", JobRole.class)
                .map(List::size)
                .cast(Long.class);
    }
}
