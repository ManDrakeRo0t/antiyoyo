# Шпаргалка по модулю `game`

> Быстрое восстановление контекста перед рефакторингом.  
> Подробнее: [`game-rules.md`](game-rules.md), [`game-mechanics-and-architecture.md`](game-mechanics-and-architecture.md).

---

## 1. Где что искать

| Ответственность | Класс / файл |
|-----------------|--------------|
| Входная точка движка | [`GameEngine`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java) |
| Корневое состояние игры | [`GameSession`](../../src/main/java/ru/bogatov/antiyoyo/game/model/GameSession.java) |
| Игрок | [`Player`](../../src/main/java/ru/bogatov/antiyoyo/game/model/Player.java) |
| Действие | [`Move`](../../src/main/java/ru/bogatov/antiyoyo/game/model/Move.java) |
| Настройки | [`GameSetting`](../../src/main/java/ru/bogatov/antiyoyo/game/model/GameSetting.java) |
| Клетка и координаты | [`Hex`](../../src/main/java/ru/bogatov/antiyoyo/game/model/common/Hex.java), [`Vector3`](../../src/main/java/ru/bogatov/antiyoyo/game/model/common/Vector3.java) |
| Ресурсы | [`Currency`](../../src/main/java/ru/bogatov/antiyoyo/game/model/common/Currency.java) |
| Валидация ходов | [`MoveValidator`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MoveValidator.java) |
| Доступные клетки | [`HexCalculator`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/HexCalculator.java) |
| Регионы, экономика, защита, огонь, дроны | [`MapUtils`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java) |
| Мощность | [`PowerCalculator`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/PowerCalculator.java) |
| Undo / сериализация | [`SnapshotUtils`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/SnapshotUtils.java) |
| Фабрика сущностей | [`EntityUtils`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/EntityUtils.java) |
| Генерация карты | [`HexMapGenerator`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/HexMapGenerator.java) |
| Правила игры (исходник) | [`rules.html`](../../src/main/resources/static/rules.html) |
| Новый движок v2 | [`GameEngineV2`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/v2/GameEngineV2.java) |
| Pipeline v2 | [`MovePipeline`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/v2/pipeline/MovePipeline.java), [`MoveContext`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/v2/pipeline/MoveContext.java) |
| События v2 | [`MoveEvent`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/v2/pipeline/event/MoveEvent.java) + record'ы в `pipeline/event` |
| Behavior v2 | [`BehaviorResolver`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/v2/pipeline/behavior/BehaviorResolver.java) |
| Сервисы v2 | `service/` в `engine/v2` |

---

## 2. Фазы хода

1. **Игрок кликает / делает ход** → сервер вызывает [`GameEngine.makeMove`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java).
2. **Валидация** ([`MoveValidator`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MoveValidator.java)).
3. **Сохранение снапшота** для undo ([`SnapshotUtils`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/SnapshotUtils.java)).
4. **Применение хода** ([`applyMove`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java) → [`setEntity`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java)).
5. **Обновление UI-флагов** ([`restoreAvailability`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java), [`updateGlue`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java)).

При завершении хода ([`GameEngine.endMove`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java)):

1. Очистка `history`.
2. Экономика по регионам ([`updateRegionAfterMove`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java)).
3. Проверка победителя ([`checkPlayersCount`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java)).
4. Сброс UI-состояния.
5. Разброс ресурсов от месторождений ([`processFarms`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java)).
6. Сброс дронов.
7. Угасание огня ([`processFire`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java)).
8. Пересчёт мощности и дронов ([`updatePowerAndDronesAvailability`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java)).
9. Следующий активный игрок.

---

## 3. Константы баланса

### Юниты

| Юнит | Уровень | Цена | Содержание | Радиус | Мощность |
|------|---------|------|------------|--------|----------|
| `UnitStageOne` | 1 | 10 золота | −2 золота/ход | 3 | 10 |
| `UnitStageTwo` | 2 | 20 золота | −6 золота/ход | 3 | 20 |
| `UnitStageThree` | 3 | 30/1/1 | −18 золота/ход | 3 | 30 |
| `Tank` | 4 | 40/2/2 | −36 золота/ход | 3 | 40 |
| `Drone` | 0 | 5 золота | — | 4 | — |

### Здания и башни

| Сущность | Уровень защиты | Цена | Доход/содержание | Мощность |
|----------|----------------|------|------------------|----------|
| `TownHall` | 1 | — | — | — |
| `Factory` | 0 | 0/(4+n/2)/(2+n/2) | +4 золота | 15 |
| `ForestFarm` | 0 | 10 золота | +2 дерева | 24 |
| `MineFarm` | 0 | 10 золота | +2 камня | 36 |
| `Tower` | 2 | 5/5/5 | −2 золота | 15 |
| `BigTower` | 3 | 10/10/10 | −5 золота | 35 |

### Ресурсы на карте

| Сущность | Доход | Мощность | Награда при сборе |
|----------|-------|----------|-------------------|
| `Forest` | +1 дерево, +5 если рядом `ForestFarm` | 6 | — |
| `Mine` | +1 камень, +5 если рядом `MineFarm` | 9 | — |
| `Tree` | — (не даёт +1 золота) | 6 | +3 дерева |
| `Stone` | — | 9 | +3 камня |

### Дроны по мощности

| % от максимальной мощности | Лимит дронов |
|----------------------------|--------------|
| 82–100 % | 0 |
| 65–81 % | 1 |
| 56–64 % | 2 |
| 47–55 % | 3 |
| 32–46 % | 4 |
| 1–31 % | 5 |

### Огонь

| Уничтоженная сущность | Стадия огня | Штраф золота/ход |
|-----------------------|-------------|------------------|
| Здания (`Factory`, `ForestFarm`, `MineFarm`) | 3 | −3 |
| `UnitStageTwo` | 2 | −2 |
| `UnitStageOne` | 1 | −1 |

---

## 4. Типичные сценарии

### Создание юнита

1. Игрок кликает свою клетку → [`handleBeforeMoveClick`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java) находит `TownHall` региона.
2. Игрок выбирает тип юнита → [`HexCalculator.getAvailableHexesForNewEntity`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/HexCalculator.java).
3. Игрок кликает целевую клетку → [`MoveValidator.checkToHex`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MoveValidator.java).
4. Стоимость списывается с `TownHall.storage` → [`applyMove`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java).

### Атака вражеской клетки

1. [`HexCalculator.getAvailableHexesForExistingEntity`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/HexCalculator.java) строит BFS по своим клеткам.
2. [`canMoveToEnemyHex`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/HexCalculator.java) проверяет: `defenseLevel < unit.level` (или `level == 4`).
3. [`setEntity`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java) заменяет сущность и цвет, обновляет защиту.
4. [`validateTownHallsAndRegions`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java) проверяет/восстанавливает регионы.

### Разрез территории

1. Вражеский юнит захватывает клетку внутри связной области.
2. [`validateTownHallsAndRegions`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java) перебирает области.
3. Для области без `TownHall` вызывается [`findPlaceForTownHall`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java).
4. Ресурсы старого `TownHall` делятся между новыми.
5. Если места нет — [`killInRegion`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java).

### Банкротство

1. В конце хода [`updateRegionAfterMove`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/util/MapUtils.java) применяет `storageUpdate`.
2. Если `storage.gold < 0`, все `dieableUnits` в регионе становятся `Grave`/`Field`.
3. `storage.gold` обнуляется.
4. На следующем ходу `Grave` → `Tree`.

---

## 5. Глоссарий

| Термин игры | Термин кода | Класс |
|-------------|-------------|-------|
| Государство | `TownHall` | [`TownHall`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/TownHall.java) |
| Ферма | `Factory` | [`Factory`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Factory.java) |
| Лесопилка | `ForestFarm` | [`ForestFarm`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/ForestFarm.java) |
| Завод | `MineFarm` | [`MineFarm`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/MineFarm.java) |
| Рядовый | `UnitStageOne` | [`UnitStageOne`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/UnitStageOne.java) |
| Солдат | `UnitStageTwo` | [`UnitStageTwo`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/UnitStageTwo.java) |
| Танк | `UnitStageThree` | [`UnitStageThree`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/UnitStageThree.java) |
| Истребитель | `Tank` | [`Tank`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Tank.java) |
| Башня 1 | `Tower` | [`Tower`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Tower.java) |
| Башня 2 | `BigTower` | [`BigTower`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/BigTower.java) |
| Месторождение дерева | `Forest` | [`Forest`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Forest.java) |
| Месторождение камня | `Mine` | [`Mine`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Mine.java) |
| Дерево | `Tree` | [`Tree`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Tree.java) |
| Камень | `Stone` | [`Stone`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Stone.java) |
| Могила | `Grave` | [`Grave`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Grave.java) |
| Огонь | `Fire` | [`Fire`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Fire.java) |
| Пустая клетка | `Field` | [`Field`](../../src/main/java/ru/bogatov/antiyoyo/game/model/entity/Field.java) |

---

## 6. Что помнить при рефакторинге

- `game` должен **не знать** о `server`. Убрать зависимость от [`GameEvent`](../../src/main/java/ru/bogatov/antiyoyo/server/domain/GameEvent.java) в [`GameEngine`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java).
- `MapUtils` слишком большой — разбить на доменные сервисы.
- Все числа баланса лучше вынести в `GameSetting`/конфиг, а не держать в `getStorageChanges()`/`getPrice()`.
- `Currency` удобен, но легко перепутать доход и цену — добавить именованные типы или документацию.
