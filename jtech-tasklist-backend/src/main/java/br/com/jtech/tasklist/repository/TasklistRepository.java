package br.com.jtech.tasklist.repository;

import br.com.jtech.tasklist.domain.TasklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TasklistRepository extends JpaRepository<TasklistEntity, UUID> {
    List<TasklistEntity> findAllByOwnerIdOrderByCreatedAtAsc(UUID ownerId);

    Optional<TasklistEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(UUID ownerId, String name);

    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(UUID ownerId, String name, UUID id);
}