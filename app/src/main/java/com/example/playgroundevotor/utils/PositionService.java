package com.example.playgroundevotor.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd;
import ru.evotor.framework.receipt.Measure;
import ru.evotor.framework.receipt.Position;
import ru.evotor.framework.receipt.TaxNumber;

public class PositionService {

    public static List<PositionAdd> positions() {
        List<PositionAdd> positions = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            List<Position> subPositions = List.of(
                    Position.Builder.newInstance(
                            UUID.randomUUID().toString(),
                            null,
                            "Субпозиция",
                            new Measure("дроб", 3, 255),
                            BigDecimal.ONE,
                            BigDecimal.TEN
                    ).build()
            );

            Position positionToBeAdded = Position.Builder.newInstance(
                            UUID.randomUUID().toString(),
                            UUID.randomUUID().toString(),
                            "Зажигалка" + i,
                            new Measure("Шт", 0, 11),
                            new BigDecimal(30),
                            new BigDecimal(1)
                    ).setTaxNumber(TaxNumber.VAT_7)
                    .setSubPositions(subPositions)
                    .build();

            positions.add(new PositionAdd(positionToBeAdded));
        }

        Position positionToBeAdded2 = Position.Builder.newInstance(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Спички",
                new Measure("кг", 0, 41),
                new BigDecimal(30),
                new BigDecimal(3)
        ).setTaxNumber(TaxNumber.VAT_5).build();

        positions.add(new PositionAdd(positionToBeAdded2));

        return positions;
    }
}
