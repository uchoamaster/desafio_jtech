package br.com.jtech.tasklist.adapters.output.repositories;

import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskItemEntity, UUID> {
    List<TaskItemEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<TaskItemEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByTasklistIdAndTitleIgnoreCase(UUID tasklistId, String title);

    boolean existsByTasklistIdAndTitleIgnoreCaseAndIdNot(UUID tasklistId, String title, UUID id);
}