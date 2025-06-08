package ru.bogatov.antiyoyo.game;

import ru.bogatov.antiyoyo.game.engine.util.HexMapGenerator;
import ru.bogatov.antiyoyo.game.model.Hex;

import java.util.Arrays;
import java.util.Collection;

public class Main {

    public static void main(String[] args) {

        var map = HexMapGenerator.generateEmptyMap(3);

        System.out.println(map.size());

        printHexGrid(map.values());

    }


    public static void printHexGrid(Collection<Hex> hexes) {
        // Шаблон для отрисовки одного шестиугольника
        String[] hexTemplate = {
                "/-\\",
                "\\_/"
        };

        // Определяем границы сетки
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Hex hex : hexes) {
            minX = Math.min(minX, hex.getVector().getX());
            maxX = Math.max(maxX, hex.getVector().getX());
            minY = Math.min(minY, hex.getVector().getY());
            maxY = Math.max(maxY, hex.getVector().getY());
        }

        // Размеры шаблона
        int hexHeight = hexTemplate.length;
        int hexWidth = hexTemplate[0].length();

        // Создаем область для вывода
        int gridWidth = (maxX - minX + 2) * hexWidth + hexWidth/2;
        int gridHeight = (maxY - minY + 2) * hexHeight;
        char[][] grid = new char[gridHeight][gridWidth];

        // Заполняем пробелами
        for (char[] row : grid) {
            Arrays.fill(row, ' ');
        }

        // Рисуем все шестиугольники
        for (Hex hex : hexes) {
            // Позиция в выходной сетке
            int x = (hex.getVector().getX() - minX) * hexWidth + (hex.getVector().getY() - minY) * (hexWidth / 2);
            int y = (hex.getVector().getY() - minY) * hexHeight;

            // Копируем шаблон
            for (int i = 0; i < hexHeight; i++) {
                for (int j = 0; j < hexWidth; j++) {
                    char c = hexTemplate[i].charAt(j);
                    if (c != ' ') {
                        grid[y + i][x + j] = c;
                    }
                }
            }
        }

        // Выводим результат
        for (char[] row : grid) {
            System.out.println(new String(row));
        }
    }
}
