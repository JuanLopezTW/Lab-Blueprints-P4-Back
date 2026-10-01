package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entity.PointEmbeddable;
import edu.eci.arsw.blueprints.persistence.mapper.BlueprintMapper;
import edu.eci.arsw.blueprints.persistence.repository.BlueprintJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintJpaRepository repository;
    private final BlueprintMapper mapper;

    public PostgresBlueprintPersistence(BlueprintJpaRepository repository, BlueprintMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repository.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: %s:%s".formatted(bp.getAuthor(), bp.getName()));
        }
        repository.save(mapper.toEntity(bp));
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        return mapper.toDomain(entity);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> entities = repository.findByAuthor(author);
        if (entities.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return entities.stream().map(mapper::toDomain).collect(Collectors.toSet());
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toSet()));
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        entity.addPoint(new PointEmbeddable(x, y));
        repository.save(entity);
    }

    @Override
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        repository.delete(entity);
    }
}
