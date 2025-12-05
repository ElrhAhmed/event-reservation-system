package ma.projet.events.service;

import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // 1. Ajouter un nouvel événement ou modifier un événement existant
    public Event saveEvent(Event event) {
        // Si c'est une création (ID est null), on vérifie que la date est future
        if (event.getId() == null) {
            if (event.getDateDebut().isBefore(java.time.LocalDateTime.now())) {
                throw new RuntimeException("La date de début doit être dans le futur !");
            }
        }

        // Si c'est une modification (l'ID existe déjà)
        if (event.getId() != null) {
            Event eventExistant = eventRepository.findById(event.getId()).orElse(null);

            // Règle : Un événement TERMINE ne peut plus être modifié
            if (eventExistant != null && eventExistant.getStatut() == EventStatus.TERMINE) {
                throw new RuntimeException("Impossible de modifier un événement terminé !");
            }
        }
        return eventRepository.save(event);
    }

    // 2. Voir tout le catalogue
    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    // 3. Chercher un événement précis (par son ID)
    public Event findEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    // 4. Moteur de recherche (pour la barre de recherche du site)
    public List<Event> findEventsByFilter(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            // Si l'utilisateur n'a rien tapé, on affiche tout
            return eventRepository.findAll();
        } else {
            // Sinon, on utilise notre méthode "Magique" personnalisée du Repository
            return eventRepository.search(filterText);
        }
    }

    // 5. Supprimer un événement (Action Admin)
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}