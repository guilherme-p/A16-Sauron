package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

/**
 * Interface for observation repositories. Offers basic storage and
 * search operations for Observations
 * @param <I> Id class for the observation
 * @param <T> Observation class
 */
public class ObservationRepository<I extends Comparable<I>, T extends SavedObservation<I>> {

    // Priority Queue so that the first element always has the highest timestamp
    private ConcurrentHashMap<I, PriorityBlockingQueue<T>> repository = new ConcurrentHashMap<>();
    // Lock for methods with multiple operations on the repository
    private final Object lock = new Object();

    public void report(T observation) {
        // Lock doesnt allow report while matching
        synchronized (lock) {
            addObservation(observation);
        }
    }

    public Optional<T> track(I id) {
        return repository.containsKey(id) ? Optional.of(getLastObservation(id)) : Optional.empty();
    }

    public Stream<T> match(String regex) {
        // Lock so that no observations can be inserted while matching
        // Lock to enable gets
        synchronized (lock) {
            return repository.keySet().stream()
                    .filter(id -> id.toString().matches(regex))
                    .map(this::getLastObservation);
        }
    }

    public Stream<T> trace(I id) {
        if (!repository.containsKey(id)) {
            return Stream.empty();
        }

        PriorityBlockingQueue<T> observationList = repository.get(id);
        // Lock observation list so that no one can insert while copying
        synchronized (observationList) {
            return new ArrayList<>(observationList).stream();
        }
    }

    public void clear() {
        repository.clear();
    }

    private void addObservation(T observation) {
        I observationId = observation.getId();

        if (repository.containsKey(observationId)) {
            repository.get(observationId).add(observation);
        }
        else {
            PriorityBlockingQueue<T> list = new PriorityBlockingQueue<>();
            list.add(observation);
            if (repository.putIfAbsent(observationId, list) != null) {
                repository.get(observationId).add(observation);
            }
        }
    }

    private T getLastObservation(I id) {
        PriorityBlockingQueue<T> observationList = repository.get(id);
        return observationList.peek();
    }
}
