package edu.eci.arsw.blueprints.realtime;

import edu.eci.arsw.blueprints.model.Point;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DrawMessage(
        @NotBlank String author,
        @NotBlank String name,
        @NotNull Point point
) {}