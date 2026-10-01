package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dto.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Set;

@Tag(name = "Blueprints", description = "Operaciones sobre planos (blueprints)")
@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @Operation(summary = "Obtener todos los blueprints")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "execute ok", services.getAllBlueprints()));
    }

    // GET /blueprints/{author}
    @Operation(summary = "Obtener blueprints por autor")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Autor no encontrado")
    })
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<?>> byAuthor(@Parameter(description = "Nombre del autor") @PathVariable String author) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(200, "execute ok", services.getBlueprintsByAuthor(author)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(summary = "Obtener un blueprint específico de un autor")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(@Parameter(description = "Nombre del autor") @PathVariable String author,
                                                          @Parameter(description = "Nombre del blueprint") @PathVariable String bpname) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(200, "execute ok", services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // POST /blueprints
    @Operation(summary = "Crear un nuevo blueprint")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "execute ok", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un blueprint existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<?>> addPoint(@Parameter(description = "Nombre del autor") @PathVariable String author,
                                                   @Parameter(description = "Nombre del blueprint") @PathVariable String bpname,
                                                   @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(202, "execute ok", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // DELETE /blueprints/{author}/{bpname}
    @Operation(summary = "Eliminar un blueprint")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @DeleteMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<?>> delete(@Parameter(description = "Nombre del autor") @PathVariable String author,
                                                 @Parameter(description = "Nombre del blueprint") @PathVariable String bpname) {
        try {
            services.deleteBlueprint(author, bpname);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(200, "execute ok", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}