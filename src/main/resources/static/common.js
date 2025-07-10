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
    return `https://${backendHost}`;
}
initializeBackendHost();