package main.service.request.model;

public enum RequestStatus {
    /**
     * Запрос ожидает подтверждения
     */
    PENDING,

    /**
     * Запрос подтвержден
     */
    CONFIRMED,

    /**
     * Запрос отклонен
     */
    REJECTED,

    /**
     * Запрос отменен
     */
    CANCELED
}
