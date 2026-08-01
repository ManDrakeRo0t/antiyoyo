# План: документация игрового модуля `game`

## Цель

Создать набор Markdown-документов, который:
- Фиксирует правила игры из `src/main/resources/static/rules.html` в структурированном виде.
- Описывает, как эти правила реализованы в коде модуля `ru.bogatov.antiyoyo.game`.
- Даёт быструю шпаргалку по проекту, чтобы при следующем рефакторинге можно было быстро восстановить контекст.

## Границы

- В scope — только пакет `ru.bogatov.antiyoyo.game` и файл `rules.html`.
- В out of scope — серверный код (`server.*`), фронтенд (`static/*.html`, `*.css`, `*.js`), инфраструктура (`pom.xml`, Docker).
- Исходный `rules.html` не редактируется. Документация создаётся рядом с кодом, а не заменяет существующую страницу правил.

## Решения

1. **Язык**: русский (как в `rules.html` и в запросе).
2. **Расположение**: `docs/game/` в корне репозитория.
3. **Файлы**:
   - `docs/game/game-rules.md` — правила игры.
   - `docs/game/game-mechanics-and-architecture.md` — техническое описание механик и архитектуры со ссылками на Java-файлы.
   - `docs/game/quick-reference.md` — краткая шпаргалка.

## Контекст, полученный из кода

### Структура модуля `game`

```
ru.bogatov.antiyoyo.game
├── Main.java                         // standalone-утилита для печати пустой гексагональной карты
├── engine
│   ├── GameEngine.java               // фасад: makeMove, endMove, undoMove, handleBeforeMoveClick
│   └── util
│       ├── EntityUtils.java          // фабрика сущностей по EntityType
│       ├── HexCalculator.java        // расстояния, соседи, доступные клетки, правила перемещения/атаки
│       ├── HexMapGenerator.java      // генерация пустой карты
│       ├── MapUtils.java             // регионы, экономика, защита, огонь, дроны, могилы
│       ├── MoveValidator.java        // проверка очерёдности, from/to
│       ├── Pair.java                 // вспомогательная пара
│       ├── PowerCalculator.java      // расчёт мощности и порогов для дронов
│       └── SnapshotUtils.java        // JSON-снапшоты для undo
├── model
│   ├── GameSession.java              // полное состояние сессии
│   ├── GameSetting.java              // флаги: undoMove, grave, demolition, cut, farmsDensity, secondsToMove
│   ├── Move.java                     // действие игрока
│   ├── Player.java                   // userId, цвет, selectedTownHall, isIlluminated
│   └── common
│       ├── Currency.java             // золото/дерево/камень
│       ├── Hex.java                  // клетка: цвет, сущность, защита, доступность, glue
│       ├── HexColor.java             // цвета игроков + EMPTY
│       └── Vector3.java              // кубические координаты гекса
└── model/entity                      // иерархия сущностей (см. ниже)
```

### Иерархия сущностей

- База: `Entity` (`movedOnThisTurn`, `getType()`, `getStorageChanges()`).
- Маркеры/интерфейсы:
  - `Interactable` — `level`, `attackRadius`, `moveRadius`.
  - `Sellable` — `getPrice(unitsCount)`.
  - `Farmable` — `getFarm(neighbors)`, `farmableType()`.
  - `Mineable` — `getReward()`.
- Сущности:
  - Государство: `TownHall`.
  - Войска: `UnitStageOne` (ур.1), `UnitStageTwo` (ур.2), `UnitStageThree` (ур.3), `Tank` (ур.4).
  - Башни: `Tower` (ур.2), `BigTower` (ур.3).
  - Здания: `Factory` (ферма/завод золота), `ForestFarm`, `MineFarm`.
  - Дрон: `Drone`.
  - Ресурсы/препятствия: `Field`, `Tree`, `Stone`, `Forest`, `Mine`, `Grave`, `Fire`.

### Ключевые механики (кратко)

- **Ход**: `GameEngine.makeMove` → валидация (`MoveValidator`) → сохранение снапшота (`SnapshotUtils`) → применение (`applyMove`) → восстановление доступности (`MapUtils.restoreAvailability`) → обновление "клея" (`updateGlue`).
- **Конец хода**: `GameEngine.endMove` → очистка истории → экономика по регионам (`MapUtils.updateRegionAfterMove`) → проверка победителя (`checkPlayersCount`) → восстановление карты → фермы (`processFarms`) → восстановление дронов (`restoreDrones`) → огонь (`processFire`) → мощность/дроны (`updatePowerAndDronesAvailability`) → смена игрока.
- **Экономика**: каждая клетка даёт +1 золота, кроме `Tree`. `Farmable` добавляет бонусы. `TownHall.storageUpdate` накапливает изменения; если золото < 0 — все `dieableUnits` погибают (`Grave` или `Field` в зависимости от настройки `grave`).
- **Защита**: `MapUtils.calculateDefenseLevel` берёт максимальный `level` среди соседей в радиусе 1 включая центр. `Tower/BigTower/TownHall/Unit` распространяют защиту. Юнит может атаковать вражескую клетку, если `defenseLevel < unit.level`, кроме `Tank` (ур.4), который игнорирует защиту.
- **Дроны**: движение радиусом 4 по любым свободным клеткам; уничтожает юнитов 1–2 уровня и здания, оставляя `Fire`. Доступны только отстающим игрокам по `PowerCalculator`.
- **Огонь**: `Fire.stage` 3/2/1, каждый ход уменьшается, при 0 становится `Field`. Дает отрицательный `storageChanges`.
- **Разрезы**: при захвате клетки внутри территории `findTownHallWithRegion` разбивает регион; лишние `TownHall` сливаются в один (богатейший), остальные превращаются в `Field`. Если регион остался без места для `TownHall` — он уничтожается (`killInRegion`).

## План работ

### 1. Создать директорию для документации

```bash
mkdir -p docs/game
```

### 2. Написать `docs/game/game-rules.md`

Структура (на основе `rules.html`):

1. **Суть игры** — цель, победа.
2. **Цвет и игроки** — выбор цвета, государство.
3. **Ход** — текущий игрок, иконки `flag.svg`/`clock.png`, отмена хода, досрочное завершение хода.
4. **Государство** — `TownHall`, несколько государств у одного цвета, территория и ресурсы у каждого.
5. **Ресурсы** — золото, дерево, камень; способы добычи.
6. **Месторождения** — `Forest`, `Mine`, генерация в радиусе 1.
7. **Здания** — `Factory`, `ForestFarm`, `MineFarm`, правила строительства и бонусы.
8. **Юниты** — 5 типов, характеристики, вложенность (`cut`), стоимость и содержание.
9. **Башни** — `Tower`, `BigTower`, защита, кто может сломать.
10. **Мощность** — формула мощности (золото + 2×дерево + 3×камень в хранилище + сущности), таблица дронов.
11. **Огонь** — стадии, последствия для экономики.
12. **Экономика** — содержание войск, отрицательный баланс, могилы.
13. **Могилы и деревья** — `Grave` → `Tree`, штраф к доходу.
14. **Разрезы** — пример разделения территории.
15. **Разрушение своих зданий** — настройка `demolition`, возврат половины стоимости.
16. **Тактика** — краткие рекомендации.
17. **Настройки игры** — `grave`, `undo`, `cut`, `demolition`, время хода, `farmsDensity`.
18. **Заключение** — страницы лобби/create/play/ratings.

Требования:
- Сохранить все числовые константы и примеры из `rules.html`.
- Использовать термины, близкие к коду (например, "Государство" → `TownHall`, "Ферма" → `Factory`), чтобы документ был мостом между правилами и кодом.
- Отметить известные опечатки и несоответствия как **Примечание**, не исправляя фактический смысл без проверки.

### 3. Написать `docs/game/game-mechanics-and-architecture.md`

Разделы:

1. **Архитектура модуля `game`**
   - Назначение каждого пакета: `model`, `model.common`, `model.entity`, `engine`, `engine.util`.
   - Граница с сервером: `GameEngine` вызывается из `server.service.GameService`; в `game.engine.GameEngine` есть зависимость `server.domain.GameEvent` (отметить как точку для будущего рефакторинга).

2. **Модель состояния**
   - `GameSession` — корневой агрегат с картой, игроками, историей undo, настройками.
   - `Hex` + `Vector3` + `HexColor` + `Currency`.
   - `Player`, `Move`, `GameSetting`.

3. **Жизненный цикл хода**
   - Последовательность `makeMove`, `applyMove`, `endMove`, `undoMove`.
   - Ссылки на методы `GameEngine` и `MapUtils`.
   - Описание `SnapshotUtils` и механики undo.

4. **Экономика**
   - `MapUtils.updateTownHallEconomy` — расчёт `storageUpdate`.
   - `MapUtils.updateRegionAfterMove` — применение и проверка банкротства.
   - `MapUtils.updatePricesForTownHall` — цены, масштабирование `Factory`.
   - Содержание юнитов (`getStorageChanges` в `UnitStageOne/Two/Three`, `Tank`, `Tower`, `BigTower`).
   - Майнинг ресурсов (`Mineable.getReward`).

5. **Боевая система и защита**
   - `HexCalculator.getAvailableHexesForExistingEntity` — движение юнитов.
   - `HexCalculator.getAvailableHexesForNewEntity` — создание юнитов/зданий, учёт `cut`.
   - `MapUtils.calculateDefenseLevel` / `updateDefenseLevel`.
   - Правила атаки: `canMoveToEnemyHex`.
   - Башни: `Tower`, `BigTower`.

6. **Регионы и разрезы**
   - `MapUtils.findTownHallWithRegion` — flood-fill и выбор главного `TownHall`.
   - `MapUtils.validateTownHallsAndRegions` — восстановление/уничтожение регионов.
   - `MapUtils.findPlaceForTownHall` — выбор места для нового `TownHall`.

7. **Дроны и мощность**
   - `PowerCalculator.calculatePowerForRegion` / `calculateTotalPower`.
   - Таблица лимитов дронов из `MapUtils.getDronesLimitFromDiff`.
   - `Drone`, его движение и создание `Fire`.

8. **Огонь, могилы, фермы**
   - `Fire` — стадии, `processFire`.
   - `Grave` → `Tree` в `updateRegionAfterMove`.
   - `processFarms` — разброс ресурсов от `Farmable` с плотностью `farmsDensity`.

9. **Генерация и сериализация**
   - `HexMapGenerator.generateEmptyMap`.
   - `EntityUtils.fromType`.
   - `SnapshotUtils.makeSnapshot` / `restoreSnapshot`.

10. **Точки для рефакторинга**
    - Зависимость `game` → `server.domain.GameEvent`.
    - `MapUtils` как God-class (~570 строк), возможность разделения на `RegionService`, `EconomyService`, `CombatService`.
    - `GameEngine.applyMove` и `setEntity` — сложная логика слияния юнитов и замены сущностей.
    - `HexCalculator` смешивает математику карты и игровые правила.

### 4. Написать `docs/game/quick-reference.md`

Структура:

1. **Где что лежит** — таблица: механика → класс/метод.
2. **Константы и пороги** — доходы, цены, радиусы, таблица дронов, очки мощности.
3. **Фазы хода** — 5–7 шагов в одном списке.
4. **Частые сценарии** — "как создать юнита", "как происходит атака", "как работает разрез", "что происходит при банкротстве".
5. **Глоссарий** — термины игры ↔ классы.

### 5. Проверка

- Все внутренние Markdown-ссылки должны указывать на реально существующие Java-файлы (`src/main/java/ru/bogatov/antiyoyo/game/...`).
- Проверить, что `docs/game/` добавлена в проект и файлы рендерятся без ошибок Markdown.
- Прочитать оба документа "свежим взглядом" — убедиться, что между `game-rules.md` и `game-mechanics-and-architecture.md` нет противоречий.

## Риски

1. **Расхождение правил и кода**. `rules.html` написан вольно; в документации нужно отметить расхождения (например, числовые константы в `rules.html` могут не совпадать с кодом). Решение: в `game-mechanics-and-architecture.md` всегда приводить кодовое значение и ссылку на файл.
2. **Устаревание документации**. Решение: не дублировать код, а давать ссылки на файлы и методы; документировать "почему", а не только "что".
3. **Затягивание объёма**. Решение: `game-rules.md` — строго по `rules.html`; `game-mechanics-and-architecture.md` — строго по коду; `quick-reference.md` — только самое нужное.

## Критерии готовности

- [ ] Создана директория `docs/game/`.
- [ ] `docs/game/game-rules.md` содержит структурированные правила игры на русском.
- [ ] `docs/game/game-mechanics-and-architecture.md` содержит описание механик со ссылками на Java-файлы модуля `game`.
- [ ] `docs/game/quick-reference.md` содержит шпаргалку по структуре проекта и механикам.
- [ ] Все ссылки на исходники проверены на существование файлов.
- [ ] Ни один исходный файл модуля `game` не изменён.

## Следующий шаг

Передать план на выполнение агенту, который умеет создавать и редактировать файлы (не планировочному агенту).
