package com.example.green.domain.enums;

public enum AchievementCode {
    FIRST_ACTION(
            "Первый шаг",
            "Совершено первое экологическое действие"
    ),

    FIRST_SHARED_TRIP(
            "Первая совместная поездка",
            "Завершена первая совместная поездка"
    ),

    CARPOOL_REGULAR(
            "Постоянный попутчик",
            "Завершено 10 совместных поездок"
    ),

    FIRST_WASTE_DEPOSIT(
            "Начало переработки",
            "Совершена первая сдача отходов"
    ),

    RECYCLING_REGULAR(
            "Ответственный переработчик",
            "Совершено 10 сдач отходов"
    ),

    ECOCOINS_100(
            "Первые 100 EcoCoins",
            "Накоплено не менее 100 EcoCoins"
    ),

    ESG_70(
            "ESG-лидер",
            "Достигнут ESG-рейтинг 70"
    ),

    CO2_10_KG(
            "Защитник климата",
            "Предотвращено не менее 10 кг CO₂"
    );

    private final String title;
    private final String description;

    AchievementCode(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }


}
