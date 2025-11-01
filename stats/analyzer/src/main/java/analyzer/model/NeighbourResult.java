package analyzer.model;

// DTO для получения плоского списка "соседей" из нативного запроса
// интерфейс вместо класса для проекции — это мощный механизм Spring Data JPA. Он автоматически создаст прокси-объекты.
public interface NeighbourResult {
    Long getPrimaryId();
    Long getNeighbourId();
    Double getScore();
}