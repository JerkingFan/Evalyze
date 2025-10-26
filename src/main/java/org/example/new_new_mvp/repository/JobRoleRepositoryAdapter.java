package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.JobRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobRoleRepositoryAdapter {

    @Autowired
    private JobRoleRepository jobRoleRepository;

    public List<JobRole> findAll() {
        return jobRoleRepository.findAll().block();
    }

    public Optional<JobRole> findById(UUID id) {
        return jobRoleRepository.findById(id).block();
    }

    public List<JobRole> findByCompanyId(UUID companyId) {
        return jobRoleRepository.findByCompanyId(companyId).block();
    }

    public List<JobRole> findByRoleType(String roleType) {
        return jobRoleRepository.findByRoleType(roleType).block();
    }

    public List<JobRole> findByCompanyIdAndRoleType(UUID companyId, String roleType) {
        return jobRoleRepository.findByCompanyIdAndRoleType(companyId, roleType).block();
    }

    public JobRole save(JobRole jobRole) {
        return jobRoleRepository.save(jobRole).block();
    }

    public JobRole update(JobRole jobRole) {
        return jobRoleRepository.update(jobRole).block();
    }

    public void deleteById(UUID id) {
        jobRoleRepository.deleteById(id).block();
    }

    public long count() {
        return jobRoleRepository.count().block();
    }
}
