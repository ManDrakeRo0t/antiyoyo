// --- Глобальные переменные для drag/click ---
let mouseDown = false;
let mouseMoved = false;
// Backend host management
const colorMap = {
        'EMPTY': '#4bd187',  // серый
        'RED': '#ff0000',    // красный
        'BLUE': '#0000ff',   // синий
        'GREEN': '#00ff00',  // зеленый
        'YELLOW': '#ffff00', // желтый
        'PURPLE': '#800080', // фиолетовый
        'ORANGE': '#ffa500', // оранжевый
        'PINK': '#FEC0D5',  // черный
        'CIAN': '#23deff'   // белый
    };
// Get WebSocket URL
function getWebSocketUrl() {
    if (window.location.protocol === 'https:') {
        return `wss://${backendHost}/ws`;
    }
    return `ws://${backendHost}:8080/ws`;
}

let colorForHex = null

const selectableUnits = new Set();
            selectableUnits.add("UNIT_1");
            selectableUnits.add("UNIT_2");
            selectableUnits.add("UNIT_3");
            selectableUnits.add("TANK");
            selectableUnits.add("DRONE");

// WebSocket and STOMP client variables
let stompClient = null;
let sessionId = null;
let hexData = {};
let sendPath = null;
let currentUser = null;
let selectedColor = null;
let isGameReady = false;

// Canvas setup
const canvas = document.getElementById('hexCanvas');
const ctx = canvas.getContext('2d');
ctx.globalCompositeOperation = "lighter";
ctx.shadowOffsetX = 25;
ctx.shadowOffsetY = 25;
// Configuration
const config = {
    hexSize: 30,
    minHexSize: 10,
    maxHexSize: 60,
    showCoordinates: false,
    backgroundColor: '#f0cca5fa',
    existingHexColor: '#cccccc',
    missingHexColor: '#4b8bde',
    gridColor: '#000000',
    selectedHexColor: '#ffff00',  // Color for selected hex highlight
    selectedHexBorderWidth: 3,     // Border width for selected hex
    // Grass pattern colors - different shades of green
    grassColors: [
        '#3CB371',
        '#4bd187',
        '#3bbf76'
    ]
};

// Map state
const state = {
    currentColor: null,
    spectatorMode: false,
    gameStarted: false,
    currentPlayer: 0,
    selectedUnit: null,
    editMode: false,
    settings: null,
    offsetX: 0,
    offsetY: 0,
    isDragging: false,
    lastMouseX: 0,
    lastMouseY: 0,
    scale: 1,
    minScale: 0.5,
    maxScale: 3,
    selectedHex: null,
    selectedTownHall: null,
    hasBouncingEntities: false
};

// Color mapping function
function getColorFromName(colorName) {
    return colorMap[colorName] || config.existingHexColor;
}


// Function to get deterministic grass color based on hex coordinates
function getGrassColorForHex(hex) {
    const { x, y, z } = hex.vector;

    // Улучшенный хеш: умножение на большие простые числа + XOR
    let hash = (x * 0x8da6b343) ^ (y * 0xd8163841) ^ (z * 0xcb1ab31f);
    hash = Math.abs(hash);

    const index = hash % config.grassColors.length;
    return config.grassColors[index];
}

// Function to draw territory borders only on specific sides
function drawTerritoryBorders(x, y, size, hex, hexData) {
    if (!hex.color || hex.color === 'EMPTY') return;
     borderWidth = 3;
     if (hex.glue) {
            ctx.shadowBlur = 15;
            ctx.shadowColor = "white";
             borderWidth = 5;
      }
    // Порядок соседей для вашей системы!
    const neighbors = [
        { x: hex.vector.x,     y: hex.vector.y + 1, z: hex.vector.z - 1 }, // 0: top
        { x: hex.vector.x + 1, y: hex.vector.y,     z: hex.vector.z - 1 }, // 1: top-right
        { x: hex.vector.x + 1, y: hex.vector.y - 1, z: hex.vector.z },     // 2: bottom-right
        { x: hex.vector.x,     y: hex.vector.y - 1, z: hex.vector.z + 1 }, // 3: bottom
        { x: hex.vector.x - 1, y: hex.vector.y,     z: hex.vector.z + 1 }, // 4: bottom-left
        { x: hex.vector.x - 1, y: hex.vector.y + 1, z: hex.vector.z }      // 5: top-left
    ];

    const borderColor = getColorFromName(hex.color);

    ctx.lineWidth = borderWidth;
    ctx.strokeStyle = borderColor;
    ctx.lineCap = 'round';
    //console.log("Клетка : " + hex.vector.x + " " + hex.vector.y + " " + hex.vector.z)
    //console.log(hex)

    for (let i = 0; i < 6; i++) {
        const neighbor = neighbors[i];
        const neighborKey = `Vector3(x=${neighbor.x}, y=${neighbor.y}, z=${neighbor.z})`;
        const neighborHex = hexData.map[neighborKey];
        //console.log("Сосед : " + i)
        //console.log(neighbor)

        if (!neighborHex || neighborHex.color !== hex.color) {
            // ВАЖНО: используем pointy-topped углы!
            
            const angle1 = (2 * Math.PI / 6 * i) - (2 * Math.PI / 3);
            const angle2 = (2 * Math.PI / 6 * ((i + 1) % 6) ) - (2 * Math.PI / 3);
            //console.log("Рисую границу : " + i + " Углы " + angle1 + " , " + angle2)
            const x1 = x + (size * 0.95) * Math.cos(angle1);
            const y1 = y + (size * 0.95) * Math.sin(angle1);
            const x2 = x + (size * 0.95) * Math.cos(angle2);
            const y2 = y + (size * 0.95) * Math.sin(angle2);

            ctx.beginPath();

            // if (i == 0) {
            //     ctx.strokeStyle = "#ffffff";
            // } else if (i == 1) {
            //     ctx.strokeStyle = "#000000";
            // } else {
            //     ctx.strokeStyle = borderColor;
            // }
            ctx.moveTo(x1, y1);
            ctx.lineTo(x2, y2);
            ctx.stroke();

        }
    }

    ctx.shadowBlur = 0;
}

// Add image preloading
const entityImages = {
    'BIG_TOWER': 'images/big_tower.png',
    'TOWER': 'images/tower.png',
    'FACTORY': 'images/factory.png',
    'UNIT_1': 'images/unit1.png',
    'UNIT_2': 'images/unit2.png',
    'UNIT_3': 'images/unit3.png',
    'TANK': 'images/tank.png',
    'TOWN_HALL': 'images/town-hall.png',
    'TREE': 'images/tree.png',
    'GRAVE': 'images/grave.png',
    'SHIELD': 'images/shield.png',
    'STONE': 'images/stone.png',
    'MINE': 'images/mine.png',
    'MINE_FARM' : 'images/mine-farm.png',
    'FOREST' : 'images/forest.png',
    'FOREST_FARM' : 'images/forest-farm.png',
    'DRONE': 'images/drone.png',
    'FIRE': 'images/fire.png',
    'WARNING': 'ui-elements/warningmove.png',
    'BACKGROUND': 'ui-elements/water.jpg'
};

// Preload all images
const preloadedImages = {};
function preloadImages() {
    Object.entries(entityImages).forEach(([type, path]) => {
        const img = new Image();
        img.src = path;
        preloadedImages[type] = img;
    });
}
preloadImages();
// Entity image mapping function
function getEntityImage(entityType) {
    return preloadedImages[entityType];
}

// Function to go back to main menu
function goBack() {
    window.location.href = 'index.html';
}

// WebSocket connection functions
function connectToSession() {
    const sessionInput = document.getElementById('sessionId');
    sessionId = sessionInput.value.trim();
    
    let sendTopic = "/topic/sessions.X.event.send"
    let destTopic = "/topic/sessions.X.event.fetch"
    let destPath = destTopic.replace("X", sessionId)
    sendPath = sendTopic.replace("X", sessionId)

    if (!sessionId) {
        alert('Please enter a session UUID');
        return;
    }

    if (stompClient && stompClient.connected) {
        sendGetSession()
    } else {
        const stompConfig = {
            connectHeaders: {
                    Authorization: getUserId(),
            },
            brokerURL: getWebSocketUrl(),
            reconnectDelay: 200,
            onConnect: function (frame) {
                // Hide session input immediately when connected
                const sessionInputDiv = document.getElementById('sessionInput');
                sessionInputDiv.classList.add('hidden');

                const subscription = stompClient.subscribe(destPath, function (message) {
                    const payload = JSON.parse(message.body);
                    updateHexData(payload);
                    renderGrid();
                }, {Authorization : getUserId()});

                sendGetSession();
                
                setTimeout(() => {sendGetSession()}, 1000);


                state.selectedUnit = null;
                updateUnitButtons();

            },
            onDisconnect: function() {
                const sessionInputDiv = document.getElementById('sessionInput');
                sessionInputDiv.classList.remove('hidden');
            }
        };
        
        stompClient = new StompJs.Client(stompConfig);
        stompClient.activate();
    }

}

function onError(error) {
    console.error('WebSocket Error:', error);
}

function sendMessage(message) {
    stompClient.publish({
            destination: sendPath,
            body: JSON.stringify(message),
            headers: { Authorization: getUserId() },
        });
}

function sendGetSession() {
    const message = {
        type: "GET_SESSION"
    };
    sendMessage(message);


}

// Update control panel visibility based on selected town hall
function updateControlPanelVisibility() {
    const controlPanel = document.getElementById('controlPanel');
    const sessionInput = document.getElementById('sessionInput');
    const actionButtonsPanel = document.getElementById('actionButtonsPanel');
    const droneButtonContainer = document.querySelector('.drone-button-container');
    if (
        hexData.players && 
        hexData.players[state.currentPlayer] && 
        hexData.players[state.currentPlayer].selectedTownHall) {
        state.selectedTownHall = hexData.players[state.currentPlayer].selectedTownHall;
        controlPanel.style.display = 'flex';
        sessionInput.classList.add('hidden');
        updateUnitPrices();
        updateUnitButtons();
        // Показываем кнопку DRONE только если isDronesAvailable
        if (state.selectedTownHall.dronesAvailable) {
            droneButtonContainer.style.display = 'flex';
        } else {
            droneButtonContainer.style.display = 'none';
        }
    } else {
        state.selectedTownHall = null;
        controlPanel.style.display = 'none';
        if (!stompClient || !stompClient.connected) {
            sessionInput.classList.remove('hidden');
        }
        droneButtonContainer.style.display = 'none';
    }
    if (isPlayerTurn() && state.gameStarted) {
        actionButtonsPanel.style.display = 'flex';
    } else {
        actionButtonsPanel.style.display = 'none';
    }
    updateBalanceDisplay();
}

// Функция для обновления цен юнитов и состояния кнопок
function updateUnitPrices() {
    if (!state.selectedTownHall || !state.selectedTownHall.prices) return;
    const storage = state.selectedTownHall.storage || { gold: 0, tree: 0, stone: 0 };
    const prices = state.selectedTownHall.prices;
    Object.entries(prices).forEach(([unitType, price]) => {
        const priceElement = document.querySelector(`.unit-price[data-unit="${unitType}"]`);
        const button = document.getElementById(`${unitType}_Btn`);
        if (priceElement && button) {
            // Форматируем цену в столбик
            priceElement.innerHTML = `
                <div style='display: flex; flex-direction: column; align-items: center; gap: 2px;'>
                    <span style='color:#d5a815; font-family: rusty_typewriter;'>${price.gold||0}</span>
                    <span style='color:#888; font-family: rusty_typewriter;'>${price.stone||0}</span>
                    <span style='color:#228B22; font-family: rusty_typewriter;'>${price.tree||0}</span> 
                </div>`; //🌲
            // Проверяем хватает ли всех ресурсов
            const canAfford = (storage.gold || 0) >= (price.gold || 0) && (storage.tree || 0) >= (price.tree || 0) && (storage.stone || 0) >= (price.stone || 0);
            button.disabled = !canAfford;
            priceElement.classList.remove('affordable', 'unaffordable');
            priceElement.classList.add(canAfford ? 'affordable' : 'unaffordable');
        }
    });
}

// Обновляем отображение баланса
function updateBalanceDisplay() {
    const balanceDisplay = document.getElementById('balanceDisplay');
    const balanceAmountSpan = balanceDisplay.querySelector('.balance-amount');
    const treeAmountSpan = balanceDisplay.querySelector('.tree-amount');
    const stoneAmountSpan = balanceDisplay.querySelector('.stone-amount');
    if (!state.selectedTownHall) {
        balanceDisplay.style.display = 'none';
        return;
    }
    const goldAmount = state.selectedTownHall.storage.gold || 0;
    const goldChange = state.selectedTownHall.storageUpdate.gold || 0;
    const treeAmount = state.selectedTownHall.storage.tree || 0;
    const stoneAmount = state.selectedTownHall.storage.stone || 0;
    const treeChange = state.selectedTownHall.storageUpdate.tree || 0;
    const stoneChange = state.selectedTownHall.storageUpdate.stone || 0;
    let changeGoldText = '';
    if (goldChange !== 0) {
        const changeClass = goldChange > 0 ? 'positive' : 'negative';
        const changeSign = goldChange > 0 ? '+' : '';
        changeGoldText = `<span class="balance-change ${changeClass}">(${changeSign}${goldChange})</span>`;
    }
    let changeTreeText = '';
    if (treeChange !== 0) {
        const changeClass = treeChange > 0 ? 'positive' : 'negative';
        const changeSign = treeChange > 0 ? '+' : '';
        changeTreeText = `<span class="balance-change ${changeClass}">(${changeSign}${treeChange})</span>`;
    }
    let changeStoneText = '';
    if (stoneChange !== 0) {
        const changeClass = stoneChange > 0 ? 'positive' : 'negative';
        const changeSign = stoneChange > 0 ? '+' : '';
        changeStoneText = `<span class="balance-change ${changeClass}">(${changeSign}${stoneChange})</span>`;
    }
    // Порядок: золото, камень, дерево
    balanceAmountSpan.innerHTML = `${goldAmount}${changeGoldText}`;
    stoneAmountSpan.innerHTML = `${stoneAmount}${changeStoneText}`;
    treeAmountSpan.innerHTML = `${treeAmount}${changeTreeText}`;
    balanceDisplay.style.display = 'flex';
    updateUnitPrices();
}

// Update hex data with message filtering
function updateHexData(data) {
    if (!data) return;


    if (data.map) {
        if (colorForHex == null) {
            colorForHex = generateBiomeColorMap(Object.values(data.map || {}), data.id)
        }
        hexData = data;
        state.gameStarted = data.started
        state.settings = data.setting;
        powerByColor = data.powerByColor || null;
        // Запускаем обновление таймера при каждом новом событии
        if (data.endMoveTime) {
            lastEndMoveTime = data.endMoveTime;
            startTimerUpdater();
        }
        updateControlPanelVisibility();
        checkPlayerStatus();
        checkGameStatus(); // Check for winner or destroyed player
        updateCurrentTurnIndicator(); // Update current turn display
        updateUnitButtons();
        renderGrid(); // Trigger render to start animation if needed
    } else if (data.message) {
        showNotification(data.message);
    } else {
        console.warn('Received payload does not contain map data:', data);
    }
}

// Load current user from localStorage
function loadCurrentUser() {
    const userData = localStorage.getItem('userData');
    if (userData) {
        try {
            currentUser = JSON.parse(userData);
        } catch (e) {
            console.error('Error parsing user data:', e);
            localStorage.removeItem('userData');
            window.location.href = 'index.html';
        }
    } else {
        window.location.href = 'index.html';
    }
}

// Check player status and show appropriate UI
function checkPlayerStatus() {
    // Если данных о игроках нет, показываем панель ожидания
    if (!hexData.players || Object.keys(hexData.players).length === 0) {
        const colorWaitingPanel = document.getElementById('colorWaitingPanel');
        colorWaitingPanel.style.display = 'flex';
        // Можно добавить кастомное сообщение, если нужно
        return;
    }
    updatePlayerCounter();
    const players = hexData.players;
    const totalPlayers = Object.keys(players).length;
    const connectedPlayers = Object.values(players).filter(player => player.userId !== null).length;
    const hasUnconnectedPlayers = Object.values(players).some(player => player.userId === null);
    // Find the player number for the current user
    let currentPlayerNumber = null;
    let currentColor = null;
    Object.entries(players).forEach(([playerNumber, player]) => {
        if (player.userId === currentUser.id) {
            currentPlayerNumber = parseInt(playerNumber);
            currentColor = player.color
        }
    });
    // Update currentPlayer if found
    if (currentPlayerNumber !== null) {
        state.currentPlayer = currentPlayerNumber;
        state.currentColor = currentColor;
    }
    // Check if current user is already in the game
    const currentUserInGame = Object.values(players).some(player => player.userId === currentUser.id);
    // Показываем новую панель всегда, если не все игроки подключены или пользователь не в игре
    if (hasUnconnectedPlayers || !currentUserInGame) {
        showWaitingUI(currentUserInGame, hasUnconnectedPlayers);
        isGameReady = false;
    } else {
        hideWaitingUI();
        isGameReady = true;
    }
}

// Check game status (winner, destroyed player)
function checkGameStatus() {
    if (!hexData.players) return;

    const players = hexData.players;
    
    // Check if current player is destroyed
    if (state.currentPlayer !== null && players[state.currentPlayer]) {
        const currentPlayer = players[state.currentPlayer];
        if (currentPlayer.illuminated === true) {
            showDestroyedMessage();
            return;
        }
    }
    
    // Check if game has a winner
    if (hexData.winnerId !== null && hexData.winnerId !== undefined) {
        const winnerPlayer = Object.values(players).find(player => player.userId === hexData.winnerId);
        if (winnerPlayer && getUserId() === winnerPlayer.userId) {
            showWinnerMessage(winnerPlayer.color);
            return;
        }
    }
    
    // If no special status, hide game status overlay
    hideGameStatusOverlay();

    
}

// Show destroyed player message
function showDestroyedMessage() {
    const watchModal = document.getElementById('watchModal');
    if (state.spectatorMode) {
        watchModal.style.display = 'none';
    } else {
        watchModal.style.display = 'block';
    }
   
}

// Show winner message
function showWinnerMessage(winnerColor) {
    const victoryModal = document.getElementById('victoryModal');
    victoryModal.style.display = 'block';
}

// Hide game status overlay
function hideGameStatusOverlay() {
    const gameStatusOverlay = document.getElementById('gameStatusOverlay');
    if (!gameStatusOverlay) return;
    gameStatusOverlay.style.display = 'none';
}

// Update current turn indicator
function updateCurrentTurnIndicator() {
    const currentTurnIndicator = document.getElementById('currentTurnIndicator');
    const currentTurnIconDiv = currentTurnIndicator.querySelector('.current-turn-icon'); /* Get the icon div */
    
    if (!hexData.players || hexData.currentPlayerMove === null || hexData.currentPlayerMove === undefined) {
        currentTurnIndicator.style.display = 'none';
        return;
    }
    
    const currentPlayer = hexData.players[hexData.currentPlayerMove];
    if (!currentPlayer) {
        currentTurnIndicator.style.display = 'none';
        return;
    }
    
    if (!state.gameStarted) {
            currentTurnIndicator.style.display = 'none';
            return;
    }
    const colorHex = colorMap[currentPlayer.color] || '#333'; /* Default to dark grey if not found */

    currentTurnIconDiv.style.backgroundColor = colorHex; /* Set background color for the flag icon via mask */
    currentTurnIndicator.style.display = 'flex'; /* Changed to flex */
}

// Show appropriate waiting UI
function showWaitingUI(currentUserInGame, hasUnconnectedPlayers) {
    const colorWaitingPanel = document.getElementById('colorWaitingPanel');
    const colorSelectModal = document.getElementById('colorSelectModal');
    colorWaitingPanel.style.display = 'flex';
    if (currentUserInGame) {
        colorSelectModal.style.display = 'none';
    } else {
        colorSelectModal.style.display = 'flex';
        updateAvailableColors();
    }
}

// Hide waiting UI and show game controls
function hideWaitingUI() {
    const colorWaitingPanel = document.getElementById('colorWaitingPanel');
    colorWaitingPanel.style.display = 'none';
    document.getElementById('colorSelectModal').style.display = 'none';
}

// Update available colors based on players structure
function updateAvailableColors() {
    if (!hexData.players) return;
    const colorCirclesContainer = document.querySelector('#colorSelectModal .cw-color-circles');
    colorCirclesContainer.innerHTML = '';
    // Get colors only from players where userId is null (available slots)
    const availableColors = Object.values(hexData.players)
        .filter(player => player.userId === null)
        .map(player => player.color);
    // Color mapping for display
    
    if (availableColors.length === 0) {
        const msg = document.createElement('div');
        msg.textContent = 'Нет доступных цветов';
        msg.style.fontFamily = 'Castlefire, sans-serif';
        msg.style.fontSize = '1.1rem';
        msg.style.color = '#222';
        colorCirclesContainer.appendChild(msg);
    } else {
        availableColors.forEach(color => {
            if (colorMap[color]) {
                const circle = document.createElement('div');
                circle.className = 'cw-color-circle';
                circle.style.backgroundColor = colorMap[color];
                circle.setAttribute('data-color', color);
                circle.onclick = function() {
                    document.querySelectorAll('.cw-color-circle').forEach(btn => btn.classList.remove('selected'));
                    circle.classList.add('selected');
                    selectedColor = color;
                    document.getElementById('cwSelectBtn').disabled = false;
                };
                colorCirclesContainer.appendChild(circle);
            }
        });
    }
    // Reset selection
    selectedColor = null;
    document.getElementById('cwSelectBtn').disabled = true;
}

// Кнопка SELECT
document.addEventListener('DOMContentLoaded', function() {
    const selectBtn = document.getElementById('cwSelectBtn');
    if (selectBtn) {
        selectBtn.onclick = async function() {
            if (!selectedColor || !currentUser || !sessionId) {
                alert('Пожалуйста, выберите цвет');
                return;
            }
            try {
                selectBtn.disabled = true;
                selectBtn.querySelector('.button-text').textContent = '...';
                const response = await fetch(`${getBackendUrl()}/api/sessions/join`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ sessionId: sessionId, userId: currentUser.id, color: selectedColor })
                });
                if (response.ok) {
                    // Скрыть поп-ап выбора цвета, оставить только счетчик
                    document.getElementById('colorSelectModal').style.display = 'none';
                } else {
                    const errorData = await response.json();
                    alert(errorData.message || 'Ошибка присоединения к игре');
                    selectBtn.disabled = false;
                    selectBtn.querySelector('.button-text').textContent = 'SELECT';
                }
            } catch (error) {
                alert('Ошибка соединения с сервером');
                selectBtn.disabled = false;
                selectBtn.querySelector('.button-text').textContent = 'SELECT';
            }
        };
    }
});

// Обновление счетчика игроков
function updatePlayerCounter() {
    if (!hexData.players) return;
    const totalPlayers = Object.keys(hexData.players).length;
    const connectedPlayers = Object.values(hexData.players).filter(player => player.userId !== null).length;
    const cwConnected = document.getElementById('cwConnectedPlayers');
    const cwTotal = document.getElementById('cwTotalPlayers');
    if (cwConnected) cwConnected.textContent = connectedPlayers;
    if (cwTotal) cwTotal.textContent = totalPlayers;
}

// Canvas functions
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    renderGrid();
}

function getQueryParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

function init() {
    loadCurrentUser();
    // --- Проверка query параметра session ---
    const sessionParam = getQueryParam('session');
    if (sessionParam && sessionParam.trim() !== '') {
        const sessionInput = document.getElementById('sessionId');
        if (sessionInput) {
            sessionInput.value = sessionParam.trim();
        }
        // sessionId будет установлен в connectToSession
        connectToSession();
    }
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);
    updateControlPanelVisibility();

    // Mouse event handlers
    canvas.addEventListener('mousedown', handleMouseDown);
    canvas.addEventListener('mousemove', handleMouseMove);
    canvas.addEventListener('mouseup', handleMouseUp);
    canvas.addEventListener('mouseleave', handleMouseUp);
    canvas.addEventListener('wheel', handleWheel, { passive: false });
    // canvas.addEventListener('click', handleCanvasClick); // УДАЛЕНО, чтобы не было двойного вызова

    // Keyboard event handlers
    document.addEventListener('keydown', handleKeyDown);
}

function cubeToPixel(cube) {
    const size = config.hexSize * state.scale;
    const x = size * (3/2) * cube.x;
    const y = size * (Math.sqrt(3)/2 * cube.x + Math.sqrt(3) * cube.z);
    return {
        x: (canvas.width / 2) + x + state.offsetX,
        y: (canvas.height / 2) + y + state.offsetY
    };
}

function renderGrid() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = config.backgroundColor;
    //ctx.drawImage(getEntityImage('BACKGROUND'),0, 0, canvas.width, canvas.height);
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    if (!hexData || typeof hexData.map !== 'object' || !hexData.map) return;

    const size = config.hexSize * state.scale;
    ctx.imageSmoothingEnabled = true; // Всегда включаем сглаживание для иконок

    const hexList = Object.values(hexData.map || {});
    if (!hexList.length) return;

    hexList.forEach(hex => {
        if (!hex || !hex.vector) return;
        const pixelPos = cubeToPixel(hex.vector);
        drawHexagonWithoutBorders(pixelPos.x, pixelPos.y, size, hex, hexData);
    });

    hexList.forEach(hex => {
        if (!hex || !hex.vector) return;
        const pixelPos = cubeToPixel(hex.vector);
        drawHexagonBorders(pixelPos.x, pixelPos.y, size, hex, hexData);
    });

    // Рисуем круговую диаграмму powerByColor
    if (powerByColor) {
        drawPowerPieChart(powerByColor);
    }
}

// Function to check if it's the current player's turn
function isPlayerTurn() {
    return hexData && hexData.currentPlayerMove === state.currentPlayer;
}

// Function to check if interaction is allowed (game ready and player's turn)
function isInteractionAllowed() {
    return isGameReady && isPlayerTurn();
}

// Mouse event handlers
function handleMouseDown(e) {
    
    
    state.isDragging = true;
    state.lastMouseX = e.clientX;
    state.lastMouseY = e.clientY;
    canvas.style.cursor = 'grabbing';
}

function handleMouseMove(e) {
    if (state.isDragging) {
        const dx = e.clientX - state.lastMouseX;
        const dy = e.clientY - state.lastMouseY;
        state.offsetX += dx;
        state.offsetY += dy;
        state.lastMouseX = e.clientX;
        state.lastMouseY = e.clientY;
        renderGrid();
    }
}

function handleMouseUp() {
    state.isDragging = false;
    canvas.style.cursor = 'grab';
}

function handleWheel(e) {
  
    
    e.preventDefault();

    const delta = e.deltaY > 0 ? -1 : 1;
    const scaleFactor = 1.1;

    const mouseX = e.clientX - canvas.offsetLeft;
    const mouseY = e.clientY - canvas.offsetTop;
    const worldX = (mouseX - canvas.width/2 - state.offsetX) / state.scale;
    const worldY = (mouseY - canvas.height/2 - state.offsetY) / state.scale;

    const newScale = delta > 0 ?
        Math.min(state.scale * scaleFactor, state.maxScale) :
        Math.max(state.scale / scaleFactor, state.minScale);

    if (newScale !== state.scale) {
        state.offsetX = mouseX - canvas.width/2 - worldX * newScale;
        state.offsetY = mouseY - canvas.height/2 - worldY * newScale;
        state.scale = newScale;
        renderGrid();
    }
}

// Function to convert pixel coordinates to cubic coordinates
function pixelToCube(x, y) {
    const size = config.hexSize * state.scale;
    
    // Adjust coordinates for canvas center and offset
    const adjustedX = x - (canvas.width / 2) - state.offsetX;
    const adjustedY = y - (canvas.height / 2) - state.offsetY;
    
    // Convert to axial coordinates
    const q = (2/3) * adjustedX / size;
    const r = (-1/3) * adjustedX / size + (Math.sqrt(3)/3) * adjustedY / size;
    
    // Convert to cube coordinates
    let rx = Math.round(q);
    let ry = Math.round(-q - r);
    let rz = Math.round(r);
    
    // Round to nearest valid cube coordinates
    const xDiff = Math.abs(rx - q);
    const yDiff = Math.abs(ry - (-q - r));
    const zDiff = Math.abs(rz - r);
    
    if (xDiff > yDiff && xDiff > zDiff) {
        rx = -ry - rz;
    } else if (yDiff > zDiff) {
        ry = -rx - rz;
    } else {
        rz = -rx - ry;
    }
    
    return { x: rx, y: ry, z: rz };
}

// Function to log current state
function logCurrentState() {
    console.log('Current State:', {
        currentPlayer: state.currentPlayer,
        selectedUnit: state.selectedUnit,
        editMode: state.editMode,
        selectedHex: state.selectedHex,
        selectedTownHall: state.selectedTownHall ? {
            type: state.selectedTownHall.type,
            level: state.selectedTownHall.level,
            balance: state.selectedTownHall.balance,
            balanceChanges: state.selectedTownHall.balanceChanges
        } : null,
        hexData: hexData ? {
            currentPlayerMove: hexData.currentPlayerMove,
            players: hexData.players ? Object.keys(hexData.players).length : 0,
            mapSize: hexData.map ? Object.keys(hexData.map).length : 0
        } : null
    });
}

// Click handler for canvas
function handleCanvasClick(e) {
    if (state.isDragging || !isInteractionAllowed()) {
        return;
    }
    
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    const cubicCoords = pixelToCube(x, y);
    const hexKey = `Vector3(x=${cubicCoords.x}, y=${cubicCoords.y}, z=${cubicCoords.z})`;
    const hex = hexData.map[hexKey];
    
    if (hex) {
        const pixelPos = cubeToPixel(hex.vector);
        const size = config.hexSize * state.scale;
        
        const dx = x - pixelPos.x;
        const dy = y - pixelPos.y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        const hexRadius = size;
        
        if (distance <= hexRadius) {
            if (hex.isAvailable !== false) {
                onHexClick(cubicCoords);
            }
        }
    } else {
        sendClickBeforeMove(null, null);
        state.selectedHex = null
        state.selectedUnit = null
    }
}

// Function to handle hex click
function onHexClick(cubicCoords) {
    console.log('onHexClick', cubicCoords);
    let move_sended = false;
    if (state.selectedUnit) {
        // If unit is selected, try to place it
        sendUnitMove(state.selectedUnit, cubicCoords);
        state.selectedUnit = null;
        updateUnitButtons();
    } else {
        const hexKey = `Vector3(x=${cubicCoords.x}, y=${cubicCoords.y}, z=${cubicCoords.z})`;
        const clickedHex = hexData.map[hexKey];

        if (state.selectedHex) {
            // If we already have a selected hex, try to move the unit
            const fromHexKey = `Vector3(x=${state.selectedHex.x}, y=${state.selectedHex.y}, z=${state.selectedHex.z})`;
            const fromHex = hexData.map[fromHexKey];

            if (fromHex && fromHex.entity && fromHex.entity.type) {
                // Send move event if there's a unit in the selected hex
                if (stompClient && stompClient.connected) {
                    const message = {
                        type: "MOVE",
                        move: {
                            redactorMode: state.editMode,
                            player: state.currentPlayer,
                            entityType: fromHex.entity.type,
                            to: {
                                x: cubicCoords.x,
                                y: cubicCoords.y,
                                z: cubicCoords.z
                            },
                            from: {
                                x: state.selectedHex.x,
                                y: state.selectedHex.y,
                                z: state.selectedHex.z
                            }
                        }
                    };
                    console.log('Sending move message:', message);
                    sendMessage(message);
                    move_sended = true;
                }
            }
            // Clear selection after move attempt
            state.selectedHex = null;
        } else if (
            (
                (clickedHex.color == state.currentColor && (!clickedHex.entity.ownerColor || clickedHex.entity.ownerColor == state.currentColor)) || clickedHex.entity.ownerColor == state.currentColor) 
                && state.selectedHex == null
            ) {

            if (selectableUnits.has(clickedHex.entity.type)) {
                 state.selectedHex = cubicCoords;
            }


        }
        
        renderGrid();
        if (!move_sended) {
            sendClickBeforeMove(clickedHex, null);
        }
    }
}

function sendClickBeforeMove(hex, entityType) {
    if (stompClient && stompClient.connected) {
        const message = {
            type: "BEFORE_MOVE",
            hex : hex,
            entityType: entityType,
            move: {
                redactorMode : state.editMode,
                player: state.currentPlayer
            }
        };
        sendMessage(message);
    }
}

// Function to send unit move (now only used for placing new units)
function sendUnitMove(entityType, cubicCoords) {
    if (!isInteractionAllowed()) return;
    
    if (!cubicCoords) {
        // If no coordinates provided, this is unit selection
        state.selectedUnit = entityType;
        sendClickBeforeMove(null, entityType);
        updateUnitButtons();
        logCurrentState();
        return;
    }

    if (stompClient && stompClient.connected) {
        const message = {
            type: "MOVE",
            move: {
                redactorMode: state.editMode,
                player: state.currentPlayer,
                entityType: entityType,
                to: {
                    x: cubicCoords.x,
                    y: cubicCoords.y,
                    z: cubicCoords.z
                }
            }
        };
        console.log('Sending move message:', message);
        sendMessage(message);
        state.selectedHex = null;
        renderGrid();
    }
}

// Update unit buttons state based on selection
function updateUnitButtons() {
    const buttons = document.querySelectorAll('.unit-button');
    buttons.forEach(button => {
        const entityType = button.getAttribute('onclick').match(/'([^']+)'/)[1];
        button.classList.toggle('selected', state.selectedUnit === entityType);
        // Disable unit buttons if it's not the player's turn (or if unaffordable)
        if (!isInteractionAllowed()) {
            button.disabled = true;
        } else {
            // Проверяем хватает ли всех ресурсов
            const priceElement = document.querySelector(`.unit-price[data-unit="${entityType}"]`);
            if (priceElement) {
                const price = state.selectedTownHall && state.selectedTownHall.prices ? state.selectedTownHall.prices[entityType] : null;
                const storage = state.selectedTownHall && state.selectedTownHall.storage ? state.selectedTownHall.storage : { gold: 0, tree: 0, stone: 0 };
                const canAfford = price && (storage.gold || 0) >= (price.gold || 0) && (storage.tree || 0) >= (price.tree || 0) && (storage.stone || 0) >= (price.stone || 0);
                button.disabled = !canAfford;
            }
        }
    });

    if (state.settings.demolition) {
        document.getElementById('DESTROY_Btn').disabled = false
        document.getElementById('DESTROY_Btn').style.display = 'block'
    } else {
        document.getElementById('DESTROY_Btn').disabled = true
        document.getElementById('DESTROY_Btn').style.display = 'none'
    }



}

// Function to send undo move message
function sendUndoMove() {
    if (!isInteractionAllowed()) return;
    
    if (stompClient && stompClient.connected) {
        const message = {
            type: "UNDO_MOVE"
        };
        console.log('Sending undo move message:', message);
        sendMessage(message);
    }
}

// Функция для обработки окончания хода
function endTurn() {
    if (!isInteractionAllowed()) return;
    
    if (stompClient && stompClient.connected) {
        const message = {
            type: "FINISH_TURN",
            move: {
                redactorMode: state.editMode,
                player: state.currentPlayer
            }
        };
        console.log('Отправка события окончания хода:', message);
        sendMessage(message);
    }
}

// Keyboard event handler
function handleKeyDown(e) {
    if (!isInteractionAllowed()) return;
    
    if (e.key === 'Escape') {
        state.selectedHex = null;
        state.selectedUnit = null;
        sendClickBeforeMove(null, null);
    }
}

// Function to draw hexagon without borders
function drawHexagonWithoutBorders(x, y, size, hex, hexData) {
    ctx.beginPath();
    for (let i = 0; i < 6; i++) {
        const angle = 2 * Math.PI / 6 * i;
        const xi = x + size * Math.cos(angle);
        const yi = y + size * Math.sin(angle);
        if (i === 0) {
            ctx.moveTo(xi, yi);
        } else {
            ctx.lineTo(xi, yi);
        }
    }
    ctx.closePath();
    
    // Fill with grass pattern color
    grassColor = null
    if ((state.selectedHex || state.selectedUnit) && isInteractionAllowed()) {
        grassColor = getColorFromName(hex.color)
    } else {
        grassColor = getBiomeColorForHex(hex);
    }

    if (hex.isAvailable === false) {
        ctx.globalAlpha = 0.3;
    }
    ctx.fillStyle = grassColor;
    ctx.fill();
    ctx.globalAlpha = 1.0;


    // Draw entity icon if present
    if (hex.entity && hex.entity.type) {
        const img = getEntityImage(hex.entity.type);
        if (img && img.complete) {
            let iconSize = size * 1;
            let yOffset = 0;
            if (hex.entity.type === 'FIRE') {
                // Меняем размер в зависимости от stage
                if (hex.entity.stage === 2) {
                    iconSize = size * 0.8;
                } else if (hex.entity.stage === 1) {
                    iconSize = size * 0.6;
                } else {
                    iconSize = size * 1;
                }
            }
            // Если DRONE, рисуем кружок ownerColor
            if (hex.entity.type === 'DRONE' && hex.entity.ownerColor) {
                ctx.beginPath();
                ctx.arc(x, y - iconSize * 0.05, iconSize * 0.5, 0, 2 * Math.PI);
                ctx.fillStyle = colorMap[hex.entity.ownerColor] || '#000';
                ctx.globalAlpha = 0.85;
                ctx.fill();
                ctx.globalAlpha = 1.0;
            }
            if (hex.entity.movedOnThisTurn === false) {
                                ctx.drawImage(getEntityImage('WARNING'),
                                        x - iconSize / 2.5,
                                        y - (iconSize / 1.2) + yOffset,
                                        iconSize / 1.3,
                                        iconSize / 1.3
                                    );
                        }
            ctx.drawImage(img, 
                x - iconSize/2, 
                y - iconSize/2 + yOffset, 
                iconSize, 
                iconSize
            );

        }
    }

    // Draw shield icon if displayDefence is true
    if (hex.displayDefence) {
        const shieldImg = getEntityImage('SHIELD');
        if (shieldImg && shieldImg.complete) {
            const shieldSize = size * 1; // Shield is slightly smaller than entity
            ctx.drawImage(shieldImg,
                x - shieldSize/2,
                y - shieldSize/2,
                shieldSize,
                shieldSize
            );
            ctx.fillStyle = '#ffff00';
            ctx.font = `${Math.max(8, 10 * state.scale)}px Arial`;
            ctx.fillText(`${hex.defenseLevel}`,
                x - shieldSize/10,
                y + shieldSize/10);
            ctx.fillStyle = '#00FFFF';
        }
    }

    // Draw coordinates if enabled
    if (config.showCoordinates) {
        ctx.fillStyle = '#00FFFF';
        ctx.font = `${Math.max(8, 10 * state.scale)}px Arial`;
        ctx.fillText(`${hex.vector.x},${hex.vector.y},${hex.vector.z}`,
                    x - 20 * state.scale,
                    y + 5 * state.scale);
        ctx.fillText(`${hex.defenseLevel}`,
                    x - 15 * state.scale,
                    y + 15 * state.scale);
    }
}

// Function to draw hexagon borders only
function drawHexagonBorders(x, y, size, hex, hexData) {
    // Check if this hex is selected
    const isSelected = state.selectedHex && 
        state.selectedHex.x === hex.vector.x && 
        state.selectedHex.y === hex.vector.y && 
        state.selectedHex.z === hex.vector.z;
    
    if (isSelected) {
        // Selected hex border - draw full border
        ctx.beginPath();

        for (let i = 0; i < 6; i++) {
            const angle = 2 * Math.PI / 6 * i;
            const xi = x + size * Math.cos(angle);
            const yi = y + size * Math.sin(angle);
            if (i === 0) {
                ctx.moveTo(xi, yi);
            } else {
                ctx.lineTo(xi, yi);
            }
        }

        ctx.closePath();
        
        ctx.lineWidth = config.selectedHexBorderWidth;
        ctx.strokeStyle = config.selectedHexColor;
        ctx.stroke();
    } else if (hex.color && hex.color !== 'EMPTY') {
        // Territory border - draw only specific sides
        drawTerritoryBorders(x, y, size, hex, hexData);
    } else if (hex.isAvailable === false) {
        // Unavailable hex border
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = 2 * Math.PI / 6 * i;
            const xi = x + size * Math.cos(angle);
            const yi = y + size * Math.sin(angle);
            if (i === 0) {
                ctx.moveTo(xi, yi);
            } else {
                ctx.lineTo(xi, yi);
            }
        }

        ctx.closePath();
        
        ctx.lineWidth = 3; // Thicker border for unavailable areas
        ctx.strokeStyle = '#666666';
        ctx.stroke();
    }
}

// Initialize the application
init();

// Initialize unit buttons as disabled
document.addEventListener('DOMContentLoaded', function() {
    updateUnitButtons();
    // Добавим эффекты нажатия для цветовых кружков
    document.querySelector('#colorSelectModal').addEventListener('mousedown', function(e) {
        if (e.target.classList.contains('cw-color-circle')) {
            e.target.classList.add('active');
        }
    });
    document.querySelector('#colorSelectModal').addEventListener('mouseup', function(e) {
        if (e.target.classList.contains('cw-color-circle')) {
            e.target.classList.remove('active');
        }
    });
    document.querySelector('#colorSelectModal').addEventListener('mouseleave', function(e) {
        document.querySelectorAll('.cw-color-circle.active').forEach(el => el.classList.remove('active'));
    });
    document.querySelector('#colorSelectModal').addEventListener('touchstart', function(e) {
        if (e.target.classList.contains('cw-color-circle')) {
            e.target.classList.add('active');
        }
    }, {passive:true});
    document.querySelector('#colorSelectModal').addEventListener('touchend', function(e) {
        if (e.target.classList.contains('cw-color-circle')) {
            e.target.classList.remove('active');
        }
    });
});

// --- Жесты для мобильных устройств ---
let lastTouchDistance = null;
let lastTouchCenter = null;
let isTouchDragging = false;
let touchStartX = 0;
let touchStartY = 0;
let touchMoved = false;
let longTapTimeout = null;
const LONG_TAP_DURATION = 500; // мс
let wasTouch = false; // <--- для блокировки ghost click только после touch

function getTouchDistance(touches) {
    if (touches.length < 2) return 0;
    const dx = touches[0].clientX - touches[1].clientX;
    const dy = touches[0].clientY - touches[1].clientY;
    return Math.sqrt(dx * dx + dy * dy);
}
function getTouchCenter(touches) {
    if (touches.length < 2) return {x: 0, y: 0};
    return {
        x: (touches[0].clientX + touches[1].clientX) / 2,
        y: (touches[0].clientY + touches[1].clientY) / 2
    };
}
canvas.addEventListener('touchstart', function(e) {
    if (e.touches.length === 1) {
        isTouchDragging = false;
        touchMoved = false;
        touchStartX = e.touches[0].clientX;
        touchStartY = e.touches[0].clientY;
        state.lastMouseX = touchStartX;
        state.lastMouseY = touchStartY;
        // long tap detection
        longTapTimeout = setTimeout(() => {
            state.selectedHex = null;
            sendClickBeforeMove(null, null);
        }, LONG_TAP_DURATION);
    } else if (e.touches.length === 2) {
        lastTouchDistance = getTouchDistance(e.touches);
        lastTouchCenter = getTouchCenter(e.touches);
    }
}, { passive: false });
canvas.addEventListener('touchmove', function(e) {
    if (e.touches.length === 1 && !touchMoved) {
        const dx = e.touches[0].clientX - touchStartX;
        const dy = e.touches[0].clientY - touchStartY;
        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
            isTouchDragging = true;
            touchMoved = true;
            if (longTapTimeout) {
                clearTimeout(longTapTimeout);
                longTapTimeout = null;
            }
        }
    }
    if (e.touches.length === 1 && isTouchDragging) {
        const dx = e.touches[0].clientX - state.lastMouseX;
        const dy = e.touches[0].clientY - state.lastMouseY;
        state.offsetX += dx;
        state.offsetY += dy;
        state.lastMouseX = e.touches[0].clientX;
        state.lastMouseY = e.touches[0].clientY;
        renderGrid();
    } else if (e.touches.length === 2) {
        const newDistance = getTouchDistance(e.touches);
        const center = getTouchCenter(e.touches);
        if (lastTouchDistance) {
            const scaleChange = newDistance / lastTouchDistance;
            let newScale = state.scale * scaleChange;
            newScale = Math.max(state.minScale, Math.min(state.maxScale, newScale));
            // Центрируем относительно центра pinch
            const worldX = (center.x - canvas.width/2 - state.offsetX) / state.scale;
            const worldY = (center.y - canvas.height/2 - state.offsetY) / state.scale;
            state.offsetX = center.x - canvas.width/2 - worldX * newScale;
            state.offsetY = center.y - canvas.height/2 - worldY * newScale;
            state.scale = newScale;
            renderGrid();
        }
        lastTouchDistance = newDistance;
        lastTouchCenter = center;
    }
    e.preventDefault();
}, { passive: false });
canvas.addEventListener('touchend', function(e) {
    // Обычный тап (если не было drag)
    if (!isTouchDragging && !touchMoved && e.changedTouches.length === 1) {
        handleCanvasClick({ clientX: e.changedTouches[0].clientX, clientY: e.changedTouches[0].clientY, preventDefault: ()=>{}, stopPropagation: ()=>{} });
        wasTouch = true;
        setTimeout(() => { wasTouch = false; }, 700);
        e.preventDefault(); // предотвращаем ghost click
    }
    if (longTapTimeout) {
        clearTimeout(longTapTimeout);
        longTapTimeout = null;
    }
    isTouchDragging = false;
    touchMoved = false;
    if (e.touches.length < 2) {
        lastTouchDistance = null;
        lastTouchCenter = null;
    }
    if (e.touches.length === 0) {
        isTouchDragging = false;
    }
}, { passive: false });

// --- Таймер ---
let timerInterval = null;
let lastEndMoveTime = null;
let clockImg = new window.Image();
clockImg.src = 'ui-elements/clock.svg';

function getSecondsLeft(endMoveTime) {
    if (!endMoveTime) return null;
    const end = new Date(endMoveTime).getTime();
    const now = Date.now();
    return Math.max(0, Math.round((end - now) / 1000));
}

function startTimerUpdater() {
    if (timerInterval) clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        if (typeof renderGrid === 'function') renderGrid();
    }, 1000);
}

// Модифицируем drawPowerPieChart
function drawPowerPieChart(powerObj) {
    const colors = Object.keys(powerObj);
    const values = Object.values(powerObj);
    const total = values.reduce((a, b) => a + b, 0);
    if (total === 0) return;
    const centerY = 40; // отступ сверху
    const radius = 40;
    // --- Центрирование двух элементов ---
    const gap = 20; // px между диаграммой и часами
    const totalWidth = radius * 2 + gap + radius * 2;
    const centerX = canvas.width / 2 - totalWidth / 2 + radius;
    const clockX = centerX + radius + gap + radius; // центр clock.svg

    // Размеры для таймера
    const clockRadius = radius * 0.7; // Уменьшаем размер часов
    const timerFontSize = 24; // Размер шрифта для секунд
    const clockY = centerY - timerFontSize/3; // Сдвигаем часы чуть выше

    let startAngle = -Math.PI / 2; // сверху
    // Цвета для секторов
    colors.forEach((color, i) => {
        const value = powerObj[color];
        if (value <= 0) return;
        const percent = value / total;
        const endAngle = startAngle + percent * 2 * Math.PI;
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.closePath();
        ctx.fillStyle = colorMap[color] || '#ccc';
        ctx.globalAlpha = 0.85;
        ctx.fill();
        ctx.globalAlpha = 1.0;
        // --- подпись процента ---
        const midAngle = (startAngle + endAngle) / 2;
        const labelRadius = radius * 0.65;
        const labelX = centerX + labelRadius * Math.cos(midAngle);
        const labelY = centerY + labelRadius * Math.sin(midAngle) + 4;
        const percentText = Math.round(percent * 100) + '%';
        let textColor = '#fff';
        if (colorMap[color]) {
            const hex = colorMap[color].replace('#','');
            const r = parseInt(hex.substring(0,2),16);
            const g = parseInt(hex.substring(2,4),16);
            const b = parseInt(hex.substring(4,6),16);
            const brightness = (r*299 + g*587 + b*114) / 1000;
            if (brightness > 170) textColor = '#222';
        }
        ctx.font = 'bold 15px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = textColor;
        ctx.fillText(percentText, labelX, labelY);
        startAngle = endAngle;
    });
    // Белая обводка
    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI);
    ctx.lineWidth = 3;
    ctx.strokeStyle = '#fff';
    ctx.stroke();

    // --- CLOCK & TIMER ---
    // endMoveTime должен быть в hexData.endMoveTime
    if (typeof hexData !== 'undefined' && hexData.endMoveTime) {
        // Запускаем обновление таймера
        if (lastEndMoveTime !== hexData.endMoveTime) {
            lastEndMoveTime = hexData.endMoveTime;
            startTimerUpdater();
        }
        const secondsLeft = getSecondsLeft(hexData.endMoveTime);
        // Рисуем иконку часов
        if (clockImg.complete) {
            ctx.save();
            ctx.drawImage(clockImg, 
                clockX - clockRadius, 
                clockY - clockRadius, 
                clockRadius * 2, 
                clockRadius * 2
            );
            ctx.restore();
        } else {
            clockImg.onload = () => renderGrid();
        }
        // Рисуем секунды под иконкой
        ctx.save();
        ctx.font = `bold ${timerFontSize}px "Castlefire", Arial, sans-serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        ctx.fillStyle = '#222';
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 4;
        const timerText = secondsLeft + 's';
        const textY = clockY + clockRadius - timerFontSize/2;
        // Белая обводка для читаемости
        ctx.strokeText(timerText, clockX, textY);
        ctx.fillText(timerText, clockX, textY);
        ctx.restore();
    }
}

// --- ПК: обработка клика только через click ---
canvas.addEventListener('click', function(e) {
    if (wasTouch) return; // предотвращаем ghost click после touch
    handleCanvasClick(e);
});

// --- Удаляем вызовы handleCanvasClick из mouseup и других mouse событий ---
// (оставляем только drag-логику)
//
// Было:
// canvas.addEventListener('mouseup', function(e) {
//     ...
//     handleCanvasClick(e); // <-- УДАЛИТЬ!
// });
//
// Оставляем только drag-логику:
canvas.addEventListener('mousedown', function(e) {
    mouseDown = true;
    mouseMoved = false;
    state.lastMouseX = e.clientX;
    state.lastMouseY = e.clientY;
    canvas.style.cursor = 'grabbing';
});
canvas.addEventListener('mousemove', function(e) {
    if (mouseDown) {
        const dx = e.clientX - state.lastMouseX;
        const dy = e.clientY - state.lastMouseY;
        if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
            state.offsetX += dx;
            state.offsetY += dy;
            state.lastMouseX = e.clientX;
            state.lastMouseY = e.clientY;
            renderGrid();
            mouseMoved = true;
        }
    }
});
canvas.addEventListener('mouseup', function(e) {
    canvas.style.cursor = 'grab';
    mouseDown = false;
    mouseMoved = false;
});
canvas.addEventListener('mouseleave', function(e) {
    mouseDown = false;
    mouseMoved = false;
    canvas.style.cursor = 'grab';
});

// --- Notifications ---
let notifications = [];

function showNotification(message, delayBeforeAppear = 500) {
    const container = document.getElementById('notificationContainer');
    if (!container) return;

    // Создать элемент
    const notif = document.createElement('div');
    notif.className = 'notification-message';
    notif.textContent = message;
    container.appendChild(notif);
    notifications.push(notif);

    // Стили для контейнера (позиция: снизу слева, прозрачный фон)
    container.style.position = 'fixed';
    container.style.left = '0';
    container.style.bottom = '0';
    container.style.right = '';
    container.style.top = '';
    container.style.zIndex = '9999';
    container.style.display = 'flex';
    container.style.flexDirection = 'column';
    container.style.alignItems = 'flex-start';
    container.style.pointerEvents = 'none';
    container.style.background = 'transparent';
    container.style.padding = '0 0 24px 24px'; // отступ от краёв

    // Стили для сообщения (изначально невидимое)
    notif.style.position = 'relative';
    notif.style.margin = '8px 0';
    notif.style.padding = '0';
    notif.style.background = 'transparent';
    notif.style.color = 'rgba(0,0,0,0.7)';
    notif.style.fontSize = '1.1rem';
    notif.style.fontFamily = 'Castlefire, Arial, sans-serif';
    notif.style.borderRadius = '';
    notif.style.boxShadow = '';
    notif.style.textAlign = 'left';
    notif.style.minWidth = '120px';
    notif.style.opacity = '0'; // Начальная прозрачность = 0 (невидимо)
    notif.style.transition = 'opacity 0.5s';
    notif.style.userSelect = 'none';
    notif.style.pointerEvents = 'none';

    // Через delayBeforeAppear мс плавно появится
    setTimeout(() => {
        notif.style.opacity = '1'; // Плавное появление
    }, delayBeforeAppear);

    // Удалить через 6 секунд (сначала плавное исчезновение)
    setTimeout(() => {
        notif.style.opacity = '0';
        setTimeout(() => {
            if (container.contains(notif)) container.removeChild(notif);
            notifications = notifications.filter(n => n !== notif);
        }, 2000); // Время на исчезновение
    }, 6000);
}

