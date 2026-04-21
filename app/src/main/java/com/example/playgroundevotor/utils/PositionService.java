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
        return positions(null);
    }

    public static List<PositionAdd> positions(String scenarioLabel) {
        List<PositionAdd> changes = new ArrayList<>();
        for (Position position : receiptPositions(scenarioLabel)) {
            changes.add(new PositionAdd(position));
        }
        return changes;
    }

    public static List<Position> receiptPositions() {
        return receiptPositions(null);
    }

    public static List<Position> receiptPositions(String scenarioLabel) {
        List<Position> positions = new ArrayList<>();
        String normalizedLabel = scenarioLabel == null ? "internet receipt happy" : scenarioLabel.trim();

        positions.add(
                Position.Builder.newInstance(
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                normalizedLabel,
                                new Measure("шт", 0, 0),
                                new BigDecimal("100"),
                                BigDecimal.ONE
                        )
                        .setTaxNumber(TaxNumber.NO_VAT)
                        .build()
        );

        return positions;
    }
}
