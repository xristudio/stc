package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class InscripcionController {

    private static final String DATA_FILE = "./data/pilotos.json";
    private final ObjectMapper mapper;

    public InscripcionController() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private List<Map<String, Object>> readPilotos() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new ArrayList<>();
        return mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
    }

    private void savePilotos(List<Map<String, Object>> pilotos) throws IOException {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        mapper.writeValue(file, pilotos);
    }

    @GetMapping("/pilotos")
    public synchronized List<Map<String, Object>> getPilotos() throws IOException {
        return readPilotos();
    }

    @PostMapping("/inscripcion")
    public synchronized ResponseEntity<?> inscribir(@RequestBody Map<String, Object> body) throws IOException {
        String nombrePiloto  = (String) body.get("nombrePiloto");
        String steamId       = (String) body.get("steamId");
        Object autoNumeroObj = body.get("autoNumero");

        if (nombrePiloto == null || nombrePiloto.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre del piloto es requerido"));
        if (steamId == null || steamId.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "El Steam ID es requerido"));
        if (autoNumeroObj == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Falta el número de auto"));

        int autoNumero = ((Number) autoNumeroObj).intValue();
        List<Map<String, Object>> pilotos = readPilotos();

        boolean autoTomado = pilotos.stream()
                .anyMatch(p -> ((Number) p.get("autoNumero")).intValue() == autoNumero);
        if (autoTomado)
            return ResponseEntity.badRequest().body(Map.of("error", "Este auto ya está inscripto por otro piloto"));

        boolean pilotoRegistrado = pilotos.stream()
                .anyMatch(p -> steamId.trim().equalsIgnoreCase((String) p.get("steamId")));
        if (pilotoRegistrado)
            return ResponseEntity.badRequest().body(Map.of("error", "Este Steam ID ya tiene un auto inscripto"));

        Map<String, Object> inscripcion = new LinkedHashMap<>();
        inscripcion.put("autoNumero",   autoNumero);
        inscripcion.put("archivo",      body.getOrDefault("archivo", ""));
        inscripcion.put("nombre",       body.getOrDefault("nombre",  ""));
        inscripcion.put("marca",        body.getOrDefault("marca",   ""));
        inscripcion.put("nombrePiloto", nombrePiloto.trim());
        inscripcion.put("steamId",      steamId.trim());

        pilotos.add(inscripcion);
        pilotos.sort(Comparator.comparingInt(p -> ((Number) p.get("autoNumero")).intValue()));
        savePilotos(pilotos);
        return ResponseEntity.ok(inscripcion);
    }

    @DeleteMapping("/inscripcion/{autoNumero}")
    public synchronized ResponseEntity<?> desinscribir(@PathVariable int autoNumero) throws IOException {
        List<Map<String, Object>> pilotos = readPilotos();
        boolean removed = pilotos.removeIf(p -> ((Number) p.get("autoNumero")).intValue() == autoNumero);
        if (!removed) return ResponseEntity.notFound().build();
        savePilotos(pilotos);
        return ResponseEntity.ok(Map.of("mensaje", "Inscripción eliminada"));
    }
}
