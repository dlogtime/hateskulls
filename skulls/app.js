// Skulls - AI-Generated HATEOAS Frontend
class SkullsApp {
  constructor() {
    this.backendUrl = 'http://localhost:8080';
    // Hardcoded UUID namespace for frontend admin APIs
    this.generateUiEndpoint = '/api/f47b3c8e-1a2d-4e5f-9c8b-3d7e2f1a5c9e/generate-ui';
    
    this.contentElement = document.getElementById('ai-generated-content');
    this.errorElement = document.getElementById('error');
    
    if (!this.contentElement || !this.errorElement) {
      throw new Error('Required DOM elements not found');
    }
    
    // Set up browser history support
    this.setupHistoryNavigation();
    
    this.init();
  }
  
  setupHistoryNavigation() {
    // Handle browser back/forward buttons
    window.addEventListener('popstate', (event) => {
      if (event.state?.href) {
        // Don't update history when responding to history change
        this.followLink(event.state.href, { updateHistory: false });
      } else {
        // Handle initial page load or manual URL entry
        const currentPath = window.location.pathname + window.location.search;
        if (currentPath !== '/') {
          const fullUrl = `${this.backendUrl}${currentPath}`;
          this.followLink(fullUrl, { updateHistory: false });
        }
      }
    });
  }
  
  async init() {
    console.log('Skulls app starting...');
    try {
      const path = window.location.pathname || '/';
      await this.followLink(`${this.backendUrl}${path}`);
    } catch (error) {
      this.showError(`Failed to initialise: ${error.message}`);
    }
  }
  
  async generateUI(data) {
    try {
      // Call the real AI service to generate HTML
      const response = await fetch(this.generateUiEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          hateoasResponse: data
        })
      });
      
      if (!response.ok) {
        throw new Error(`AI service returned ${response.status}: ${response.statusText}`);
      }
      
      const result = await response.json();
      
      // Display the AI-generated HTML
      this.contentElement.innerHTML = result.html;
      this.errorElement.classList.add('hidden');
      
    } catch (error) {
      console.error('AI generation error:', error);
      this.showError(`AI generation failed: ${error.message}`);
    }
  }
  
  async followLink(href, options = {}) {
    console.log('Following link:', href, options);
    
    try {
      // Default to GET request
      const fetchOptions = {
        method: options.method || 'GET',
        headers: {
          'Accept': 'application/prs.hal-forms+json,application/hal+json,application/json'
        }
      };
      
      // Add body for POST/PUT requests
      if (options.data && (options.method === 'POST' || options.method === 'PUT')) {
        fetchOptions.headers['Content-Type'] = 'application/json';
        fetchOptions.body = JSON.stringify(options.data);
      }
      
      const response = await fetch(href, fetchOptions);
      
      // Handle different response types
      if (options.method === 'DELETE') {
        if (response.status === 204) {
          // Successful deletion - refresh the current view
          await this.init();
          return;
        }
      }
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Link response:', data);
      
      await this.generateUI(data);
      
      // Update browser history for GET requests or successful operations
      if (options.updateHistory !== false && (options.method === 'GET' || !options.method)) {
        const url = new URL(href);
        const displayPath = url.pathname + url.search;
        window.history.pushState({ href }, '', displayPath);
      }
      
    } catch (error) {
      this.showError(`Failed to follow link: ${error.message}`);
    }
  }
  
  showError(message) {
    this.contentElement.classList.add('hidden');
    this.errorElement.textContent = message;
    this.errorElement.classList.remove('hidden');
  }
}

// Initialise the app
document.addEventListener('DOMContentLoaded', () => {
  window.app = new SkullsApp();
});
