function initializeBackendHost() {
    const storedHost = localStorage.getItem('backendHost');
    if (!storedHost) {
        currentHost = window.location.hostname;
        localStorage.setItem('backendHost', currentHost);
        backendHost = currentHost;
    } else {
        backendHost = storedHost;
    }
}
function getBackendUrl() {
    if (window.location.protocol === 'https:') {
        return `https://${backendHost}`;
    }
    return `http://${backendHost}:8080`;
}
initializeBackendHost();