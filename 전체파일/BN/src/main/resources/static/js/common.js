function scrollToMainContent() {
    const mainContent = document.getElementById('main-content');

    if (mainContent) {
        mainContent.scrollIntoView({ behavior: 'smooth' });
    } else {
        console.error("Error: Element with ID 'main-content' not found. Ensure the main content section has this ID.");
    }
}