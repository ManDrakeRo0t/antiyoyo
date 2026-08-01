# План рефакторинга игрового движка (`game.engine.v2`)

## Цель

Создать новую версию игрового движка в отдельном пакете, которая:
- сохраняет текущую игровую логику без изменений;
- заменяет монолитные `GameEngine` / `MapUtils` / `HexCalculator` на pipeline из независимых стадий;
- собирает общий контекст хода, чтобы избежать повторных пересчётов;
- даёт сущностям точки расширения через behavior-интерфейсы;
- оставляет старый код нетронутым и работоспособным.

Альянсы и туман войны отложены (out of scope). Существующие фича-флаги (`undoMove`, `grave`, `demolition`, `cut`, `farmsDensity`) сохраняются и используются в новом движке.

---

## Границы и ограничения

- **Только пакет `game`** — сервер (`server.*`) не меняется.
- **Старый код не редактируется**: классы `ru.bogatov.antiyoyo.game.engine.*`, `game.model.*`, `game.model.common.*`, `game.model.entity.*` остаются как есть.
- **Публичный API нового движка** (`GameEngineV2`) повторяет API текущего [`GameEngine`](../../src/main/java/ru/bogatov/antiyoyo/game/engine/GameEngine.java), чтобы сервер мог переключиться одной заменой импорта позже.
- Новый движок работает с **существующими** `GameSession`, `Hex`, `Entity` и т.д. — не создаёт параллельную модель состояния.

---

## Принятые решения

| Вопрос | Решение |
|--------|---------|
| Публичный API | Зеркало `GameEngine` — класс `GameEngineV2` с теми же методами. |
| Расположение | Новый пакет `ru.bogatov.antiyoyo.game.engine.v2` и подпакеты. |
| События | Иммутабельные data-классы (`MoveEvent` и наследники). |
| Pipeline | Фиксированные стадии с общим `MoveContext`. |
| Сущности | Старые entity-классы не трогаются. Поведение описывается в v2-интерфейсах `EntityBehavior` и регистрируется в `BehaviorResolver`. |
| Применение событий | `EventApplier` на каждый тип события. |
| Пересчёты | `MoveContext` кеширует регионы, соседей, текущий цвет, выбранный `TownHall` и т.д. |
| Фича-флаги | Используется существующий `GameSetting` (можно обернуть в `FeatureFlags` в v2). |
| Альянсы / туман | Out of scope. |

---

## Целевая структура пакетов

```
ru.bogatov.antiyoyo.game.engine.v2
├── GameEngineV2.java                      // фасад, зеркало старого API
├── pipeline
│   ├── MovePipeline.java                  // оркестратор стадий
│   ├── MoveContext.java                   // общий контекст хода
│   ├── MoveType.java                      // BUY, MOVE, DEMOLISH, CLICK, FINISH_TURN
│   ├── MoveResult.java                    // результат: применённые события, ошибка и т.д.
│   ├── stage
│   │   ├── ValidationStage.java
│   │   ├── SnapshotStage.java
│   │   ├── EventGenerationStage.java
│   │   ├── EventApplicationStage.java
│   │   └── PostProcessStage.java
│   ├── event
│   │   ├── MoveEvent.java                 // sealed / abstract base
│   │   ├── EntityPurchasedEvent.java
│   │   ├── EntityMovedEvent.java
│   │   ├── EntityMergedEvent.java
│   │   ├── EntityDestroyedEvent.java
│   │   ├── StorageChangedEvent.java
│   │   ├── RegionSplitEvent.java
│   │   ├── TownHallCreatedEvent.java
│   │   ├── FireIgnitedEvent.java
│   │   └── ... (дополняется по мере необходимости)
│   ├── applier
│   │   ├── EventApplier.java              // interface
│   │   ├── EntityPurchasedApplier.java
│   │   ├── EntityMovedApplier.java
│   │   └── ...
│   └── behavior
│       ├── EntityBehavior.java            // marker
│       ├── Movable.java
│       ├── Purchasable.java
│       ├── Harvestable.java
│       ├── Defensive.java
│       ├── Upgradable.java
│       ├── DroneBehavior.java
│       └── BehaviorResolver.java          // entityType / class -> behavior
├── service
│   ├── RegionService.java                 // регионы, flood-fill, разрезы
│   ├── EconomyService.java                // доход, цены, банкротство
│   ├── DefenseService.java                // защита клеток
│   ├── PowerService.java                  // мощность и дроны
│   └── FarmService.java                   // разброс ресурсов
├── rule
│   ├── MovementRules.java                 // чистые правила перемещения
│   └── PurchaseRules.java                 // правила покупки/размещения
└── util
    └── HexGeometry.java                   // расстояния, соседи (без игровой логики)
```

---

## Основные компоненты

### 1. `GameEngineV2`

```java
public class GameEngineV2 {
    public void makeMove(GameSession session, Move move) { ... }
    public void endMove(GameSession session) { ... }
    public void undoMove(GameSession session) { ... }
    public void handleBeforeMoveClick(GameSession session, GameEvent event) { ... }
    public Pair<Integer, Set<HexColor>> validateSessionAndGetPlayersCount(GameSession session) { ... }
}
```

- Принимает серверный `GameEvent` только в `handleBeforeMoveClick` — это единственная зависимость `game` → `server` в новом движке.
- Внутри каждого метода создаёт `MoveContext` и запускает `MovePipeline`.

### 2. `MoveContext`

Неизменяемый (или почти неизменяемый) объект, который собирается один раз и передаётся по стадиям:

| Поле | Назначение |
|------|------------|
| `session` | `GameSession`. |
| `move` | `Move` (может быть `null` для `endMove` / `handleBeforeMoveClick`). |
| `moveType` | `MoveType`. |
| `selfColor` | Текущий цвет игрока. |
| `selfPlayer` | Текущий `Player`. |
| `selectedTownHall` | Выбранный город. |
| `fromHex` / `toHex` | Клетки `from` и `to`. |
| `selfRegions` | Кеш регионов текущего цвета. |
| `allRegions` | Кеш всех регионов (для экономики/победы). |
| `neighborsCache` | Кеш соседей по клеткам. |
| `events` | Список событий, накапливаемый в `EventGenerationStage`. |

### 3. Pipeline-стадии

#### 3.1 `ValidationStage`

- Проверка очерёдности (`move.player == session.currentPlayerMove`).
- Проверка `from` (принадлежит игроку; исключение — дрон).
- Проверка `to` (клетка доступна согласно `MovementRules` / `PurchaseRules`).
- Проверка флагов (`undoMove`, `demolition`, `cut`).

#### 3.2 `SnapshotStage`

- Сохраняет JSON-снапшот карты в `session.history` (если `from == null` или перемещение — не для `endMove`).
- Для `endMove` очищает `history`.

#### 3.3 `EventGenerationStage`

- Определяет `MoveType`.
- Для каждого участника (`from` entity, `to` entity) запрашивает `EntityBehavior` у `BehaviorResolver`.
- Behavior-интерфейсы возвращают список `MoveEvent` в зависимости от ситуации.

Примеры behavior-интерфейсов:

```java
public interface Movable {
    Set<Hex> availableDestinations(MoveContext ctx, Hex source);
    List<MoveEvent> onMove(MoveContext ctx, Hex from, Hex to);
}

public interface Purchasable {
    Currency price(MoveContext ctx);
    Set<Hex> availablePlacement(MoveContext ctx);
    List<MoveEvent> onPurchase(MoveContext ctx, Hex target);
}

public interface Upgradable {
    boolean canUpgrade(Entity existing, Entity placed);
    Entity merge(Entity existing, Entity placed);
}

public interface DroneBehavior {
    Set<Hex> availableDestinations(MoveContext ctx, Hex source);
    List<MoveEvent> onStrike(MoveContext ctx, Hex target);
}
```

#### 3.4 `EventApplicationStage`

- Перебирает события по порядку.
- Для каждого события находит `EventApplier` и применяет к `GameSession`.
- Важно: события не должны пересчитывать защиту/регионы — только менять состояние клеток и ресурсов.

#### 3.5 `PostProcessStage`

Пересчёт после применения событий (строгий порядок):

1. `DefenseService.recalculateDefense(context)` — пересчёт `defenseLevel`.
2. `RegionService.validateRegions(context)` — разрезы, создание/удаление `TownHall`, деление ресурсов.
3. `EconomyService.applyEndOfTurnEconomy(context)` — применение `storageUpdate`, банкротство (только для `endMove`).
4. `EconomyService.updatePrices(context)` — актуализация цен в `TownHall`.
5. `PowerService.updatePowerAndDrones(context)` — мощность и доступность дронов.
6. `RegionService.checkWinCondition(context)` — проверка победителя.
7. `MapUIService.resetUiFlags(context)` — сброс `isAvailable`, `glue`, `displayDefence` (для `makeMove` / `endMove`).

### 4. Сервисы

| Сервис | Ответственность |
|--------|-----------------|
| `RegionService` | `findTownHallWithRegion`, `findRegions`, `validateRegions`, `killInRegion`, `findPlaceForTownHall`, `checkWinCondition`. |
| `EconomyService` | `updateTownHallEconomy`, `updatePrices`, `applyEndOfTurnEconomy`, `calculateIncome`. |
| `DefenseService` | `calculateDefenseLevel`, `updateDefenseLevel`, `showDefenceForColor`. |
| `PowerService` | `calculatePowerForRegion`, `calculateTotalPower`, `updateDronesAvailability`. |
| `FarmService` | `processFarms` (разброс ресурсов от `Forest`/`Mine`). |

### 5. `HexGeometry`

- `distance(Hex, Hex)`
- `neighbors(Map<Vector3, Hex>, Hex, int radius, boolean includeCenter)`
- Всё без игровых правил.

### 6. Feature flags

- Использовать существующий [`GameSetting`](../../src/main/java/ru/bogatov/antiyoyo/game/model/GameSetting.java).
- В v2 можно обернуть в `FeatureFlags` для удобного чтения в стадиях/сервисах.
- Все механики проверяют флаги явно (например, `if (!featureFlags.isUndoEnabled()) return;`).

---

## Примеры потока данных

### Покупка юнита

1. `GameEngineV2.makeMove(session, move)` → `MoveType.BUY`.
2. `ValidationStage` проверяет доступность целевой клетки через `Purchasable.availablePlacement`.
3. `SnapshotStage` сохраняет снапшот.
4. `EventGenerationStage`:
   - `Purchasable.onPurchase` → `EntityPurchasedEvent` (списание цены, размещение сущности).
   - Если целевая клетка `Mineable`, дополнительно `ResourceHarvestedEvent`.
5. `EventApplicationStage` применяет события.
6. `PostProcessStage`: защита, регионы, экономика, цены, мощность, UI-флаги.

### Перемещение юнита

1. `MoveType.MOVE`.
2. `ValidationStage` проверяет `Movable.availableDestinations`.
3. `SnapshotStage`.
4. `EventGenerationStage`:
   - `EntityMovedEvent` (освобождение `from`, размещение на `to`).
   - Если вражеская клетка — `EntityDestroyedEvent`.
   - Если своя клетка и upgrade — `EntityMergedEvent`.
   - Если дрон — `FireIgnitedEvent`.
5. `EventApplicationStage`.
6. `PostProcessStage`.

### Завершение хода

1. `MoveType.FINISH_TURN`.
2. `SnapshotStage` очищает `history`.
3. `EventGenerationStage` пустой (или порождает события от `Farmable`, если нужно).
4. `EventApplicationStage` пропускается.
5. `PostProcessStage`:
   - экономика по всем регионам;
   - банкротство;
   - `FarmService.processFarms`;
   - сброс дронов;
   - угасание огня;
   - мощность/дроны;
   - проверка победы;
   - переход хода.

---

## Миграция и внедрение

1. Старый `GameEngine` остаётся рабочим. Сервер продолжает использовать его.
2. `GameEngineV2` создаётся в `game.engine.v2` и не пересекается со старым кодом.
3. После завершения и проверки `GameEngineV2` переключение сервера сводится к замене `new GameEngine()` на `new GameEngineV2()` в [`GameService`](../../src/main/java/ru/bogatov/antiyoyo/server/service/GameService.java) — **это действие вне scope текущего плана**.

---

## Валидация

1. **Компиляция**: проект должен собираться (`mvn clean compile`) без изменений в старом коде.
2. **Старые тесты**: [`HexMapGeneratorTest`](../../src/test/java/ru/bogatov/antiyoyo/game/engine/util/HexMapGeneratorTest.java) и `AntiyoyoApplicationTests` должны проходить.
3. **Новые unit-тесты** в `src/test/java/ru/bogatov/antiyoyo/game/engine/v2/`:
   - тесты отдельных сервисов (`RegionService`, `EconomyService`, `DefenseService`, `PowerService`, `FarmService`);
   - тесты pipeline-стадий;
   - тесты behavior для каждого типа сущности.
4. **Интеграционные тесты «старый vs новый»**:
   - набор фикстур с типичными состояниями карты;
   - применяем одну и ту же последовательность ходов к `GameEngine` и `GameEngineV2`;
   - сравниваем итоговое состояние `GameSession` (карта, ресурсы, победитель).
5. **Ручная проверка**: после переключения `GameService` на `GameEngineV2` игра должна вести себя идентично текущей версии.

---

## Риски

| Риск | Митигация |
|------|-----------|
| Логика нового движка расходится со старой | Интеграционные тесты «старый vs новый» на типичных сценариях. |
| Повышенная сложность pipeline | Чёткое разделение стадий; события — data-классы; applier'ы изолированы. |
| Старые entity-классы не имеют behavior-методов | `BehaviorResolver` и внешние behavior-реализации; в плане зафиксировано, что сущности не трогаются. |
| Производительность из-за множества мелких объектов (события) | Кеширование в `MoveContext`; при необходимости можно пулить/переиспользовать события. |
| Путаница с `GameEvent` сервера и `MoveEvent` v2 | Внутри v2 только `MoveEvent`; преобразование `GameEvent` → `MoveContext` происходит в `GameEngineV2.handleBeforeMoveClick`. |

---

## Пошаговый план выполнения

### Этап 1. Инфраструктура v2

1. Создать пакет `ru.bogatov.antiyoyo.game.engine.v2` и подпакеты.
2. Создать пустой `GameEngineV2` с нужными методами (заглушки).
3. Создать `MoveType`, `MoveContext`, `MoveResult`.
4. Создать `MovePipeline` и интерфейс `PipelineStage`.

### Этап 2. Утилиты и сервисы

1. `HexGeometry` — перенести/реализовать геометрию гексов.
2. `RegionService` — перенести логику регионов из `MapUtils`.
3. `DefenseService` — перенести логику защиты.
4. `EconomyService` — перенести экономику, цены, банкротство.
5. `PowerService` — перенести мощность и дронов.
6. `FarmService` — перенести разброс ресурсов.

### Этап 3. События и applier'ы

1. Создать базовый `MoveEvent` и основные наследники.
2. Создать `EventApplier` и реализации для каждого события.
3. Создать `EventApplicationStage`.

### Этап 4. Behavior и генерация событий

1. Создать behavior-интерфейсы (`Movable`, `Purchasable`, `Upgradable`, `DroneBehavior` и т.д.).
2. Создать `BehaviorResolver`, который отдаёт реализацию по `EntityType`/`Class`.
3. Реализовать behavior для всех сущностей.
4. Создать `EventGenerationStage` и `ValidationStage`.

### Этап 5. Post-processing и UI

1. Реализовать `PostProcessStage` с вызовом сервисов в правильном порядке.
2. Реализовать логику `handleBeforeMoveClick` через отдельный selection-pipeline или через `MoveType.CLICK`.
3. Реализовать `SnapshotStage` и `undoMove`.

### Этап 6. Тестирование

1. Написать unit-тесты для сервисов и behavior.
2. Написать интеграционные тесты `GameEngine` vs `GameEngineV2`.
3. Убедиться, что старые тесты проходят.

### Этап 7. Документация

1. Обновить `docs/game/game-mechanics-and-architecture.md` разделом про v2-архитектуру (после реализации).
2. Добавить в `docs/game/quick-reference.md` ссылки на новые классы.

---

## Открытые вопросы

Нет критичных открытых вопросов. Альянсы и туман войны отложены. Серверный переезд на `GameEngineV2` вне scope текущего плана.
