package com.tmv.core.controller.position;

import com.tmv.core.model.position.Position;
import com.tmv.core.persistence.position.PositionRepository;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor
@RestController
class PositionController {

    private PositionRepository repository;

    PositionController(PositionRepository repository) {
        this.repository = repository;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/positions")
    Iterable<Position> all() {
        return repository.findAll();
    }
    // end::get-aggregate-root[]

    @PostMapping("/positions")
    Position newEmployee(@RequestBody Position newPosition) {
        return repository.save(newPosition);
    }

    // Single item

    @GetMapping("/position/{id}")
    Position one(@PathVariable Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
    }

    @PutMapping("/position/{id}")
    Position replacePosition(@RequestBody Position newPosition, @PathVariable Long id) {

        return repository.findById(id)
                .map(position -> {
                    position.setDateTime(newPosition.getDateTime());
                    return repository.save(position);
                })
                .orElseGet(() -> {
                    return repository.save(newPosition);
                });
    }

    @DeleteMapping("/position/{id}")
    void deletePosition(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
