const BIOME_COLORS = {
  FIELD: generateColorGradient('#4bd187', '#38E084', 10),
  FOREST: generateColorGradient('#1F541F', '#155E15', 10),
  MINE: generateColorGradient('#5a5a5a', '#777777', 10),
  TRANSITION_FOREST: generateColorGradient('#3a7a4a', '#5d9d6d', 6),
  TRANSITION_MINE: generateColorGradient('#7d8b91', '#b0b8bc', 5)
};

// Функция для генерации градиента между двумя цветами
function generateColorGradient(color1, color2, steps) {
  const colors = [];
  const c1 = hexToRgb(color1);
  const c2 = hexToRgb(color2);

  for (let i = 0; i < steps; i++) {
    const ratio = i / (steps - 1);
    const r = Math.round(c1.r + ratio * (c2.r - c1.r));
    const g = Math.round(c1.g + ratio * (c2.g - c1.g));
    const b = Math.round(c1.b + ratio * (c2.b - c1.b));
    colors.push(rgbToHex(r, g, b));
  }

  return colors;
}

// Вспомогательные функции для работы с цветами
function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : { r: 0, g: 0, b: 0 };
}

function rgbToHex(r, g, b) {
  return '#' + [r, g, b].map(x => {
    const hex = x.toString(16);
    return hex.length === 1 ? '0' + hex : hex;
  }).join('');
}

// Все 6 направлений в кубических координатах
const CUBE_DIRECTIONS = [
  { x: 1, y: -1, z: 0 }, { x: 1, y: 0, z: -1 },
  { x: 0, y: 1, z: -1 }, { x: -1, y: 1, z: 0 },
  { x: -1, y: 0, z: 1 }, { x: 0, y: -1, z: 1 }
];

function seededRandom(seed) {
  let x = Math.sin(seed) * 10000;
  return x - Math.floor(x);
}

function getRandomFromArray(arr, seed) {
  const randomIndex = Math.floor(seededRandom(seed) * arr.length);
  return arr[randomIndex];
}

// Функция для получения соседей в радиусе 1
function getNeighbors(vector) {
  const neighbors = [];

  // Добавляем все 6 соседних клеток
  for (const dir of CUBE_DIRECTIONS) {
    neighbors.push({
      x: vector.x + dir.x,
      y: vector.y + dir.y,
      z: vector.z + dir.z
    });
  }

  return neighbors;
}

function hexToBiomeKey(hex) {
    return `${hex.vector.x},${hex.vector.y},${hex.vector.z}`;
}

function getBiomeColorForHex(hex) {
    return colorForHex.get(hexToBiomeKey(hex))
}

function generateBiomeColorMap(hexMap, seed) {
  const colorMap = new Map();
  const hexDict = new Map();
  const numericSeed = Array.from(seed).reduce((acc, char) => acc + char.charCodeAt(0), 0);

  // Сначала создаем словарь для быстрого доступа
  for (const hex of hexMap) {
    const key = hexToBiomeKey(hex);
    hexDict.set(key, hex);
  }

  // Обрабатываем каждый шестиугольник
  for (const hex of hexMap) {
    const { vector, entity } = hex;
    const key = `${vector.x},${vector.y},${vector.z}`;
    const hexSeed = numericSeed + vector.x * 100 + vector.y * 1000 + vector.z * 10000;

    // Определяем основной биом
    let biomeType = entity.type === 'FOREST' ? 'FOREST' :
                   entity.type === 'MINE' ? 'MINE' : 'FIELD';

    // Получаем соседей
    const neighbors = getNeighbors(vector);
    let isBorderCell = false;

    // Проверяем, есть ли соседи другого биома
    for (const neighbor of neighbors) {
      const neighborKey = `${neighbor.x},${neighbor.y},${neighbor.z}`;
      const neighborHex = hexDict.get(neighborKey);

      if (neighborHex) {
        const neighborBiome = neighborHex.entity.type === 'FOREST' ? 'FOREST' :
                            neighborHex.entity.type === 'MINE' ? 'MINE' : 'FIELD';

        if (neighborBiome !== biomeType) {
          isBorderCell = true;
          break;
        }
      }
    }

    let color;
    if (isBorderCell) {
      // Для граничных клеток используем переходные цвета
      if (biomeType === 'FOREST') {
        color = getRandomFromArray(BIOME_COLORS.TRANSITION_FOREST, hexSeed);
      } else if (biomeType === 'MINE') {
        color = getRandomFromArray(BIOME_COLORS.TRANSITION_MINE, hexSeed);
      } else {
        // Для полевых клеток рядом с другими биомами
        const hasForestNeighbor = neighbors.some(n => {
          const nKey = `${n.x},${n.y},${n.z}`;
          return hexDict.get(nKey)?.entity.type === 'FOREST';
        });

        const hasMineNeighbor = neighbors.some(n => {
          const nKey = `${n.x},${n.y},${n.z}`;
          return hexDict.get(nKey)?.entity.type === 'MINE';
        });

        if (hasForestNeighbor) {
          color = getRandomFromArray(BIOME_COLORS.TRANSITION_FOREST, hexSeed);
        } else if (hasMineNeighbor) {
          color = getRandomFromArray(BIOME_COLORS.TRANSITION_MINE, hexSeed);
        } else {
          color = getRandomFromArray(BIOME_COLORS[biomeType], hexSeed);
        }
      }
    } else {
      // Для обычных клеток используем основные цвета биома
      color = getRandomFromArray(BIOME_COLORS[biomeType], hexSeed);
    }

    colorMap.set(key, color);
  }

  return colorMap;
}