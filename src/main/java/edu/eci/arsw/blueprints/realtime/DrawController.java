package edu.eci.arsw.blueprints.realtime;

import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DrawController {

    private static final Logger log = LoggerFactory.getLogger(DrawController.class);
    private static final int MAX_COORD = 5000;

    private final BlueprintsServices services;
    private final SimpMessagingTemplate template;

    public DrawController(BlueprintsServices services, SimpMessagingTemplate template) {
        this.services = services;
        this.template = template;
    }

    @MessageMapping("/draw")
    public void draw(@Payload @Valid DrawMessage msg) {
        int x = msg.point().x();
        int y = msg.point().y();

        if (x < 0 || y < 0 || x > MAX_COORD || y > MAX_COORD) {
            log.warn("Punto fuera de rango: ({}, {})", x, y);
            return;
        }

        try {
            services.addPoint(msg.author(), msg.name(), x, y);
            String topic = "/topic/blueprints." + msg.author() + "." + msg.name();
            template.convertAndSend(topic, msg);
            log.info("Punto ({}, {}) agregado y enviado a {}", x, y, topic);
        } catch (BlueprintNotFoundException e) {
            log.warn("Plano no encontrado: {}/{}", msg.author(), msg.name());
        }
    }

    @MessageExceptionHandler
    public void handleInvalid(Exception e) {
        log.warn("Mensaje STOMP rechazado: {}", e.getMessage());
    }
}