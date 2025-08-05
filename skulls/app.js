// Skulls - AI-Generated HATEOAS Frontend
class SkullsApp {
    constructor() {
        this.backendUrl = 'http://localhost:8080';
        this.aiServiceUrl = 'http://localhost:3001'; // Placeholder for AI service
        
        this.contentElement = document.getElementById('ai-generated-content');
        this.loadingElement = document.getElementById('loading');
        this.errorElement = document.getElementById('error');
        
        if (!this.contentElement || !this.loadingElement || !this.errorElement) {
            throw new Error('Required DOM elements not found');
        }
        
        this.init();
    }
    
    async init() {
        console.log('Skulls app starting...');
        try {
            await this.discoverAPI();
        } catch (error) {
            this.showError(`Failed to initialize: ${error.message}`);
        }
    }
    
    async discoverAPI() {
        this.showLoading();
        
        try {
            const response = await fetch(`${this.backendUrl}/`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('API Root discovered:', data);
            
            await this.generateUI(data);
            
        } catch (error) {
            throw new Error(`API discovery failed: ${error.message}`);
        }
    }
    
    async generateUI(data) {
        this.showLoading();
        
        try {
            // For now, just display the data dynamically
            // Later this will call an AI service to generate HTML
            const generatedHTML = this.mockAIGeneration(data);
            
            this.contentElement.innerHTML = generatedHTML;
            this.hideLoading();
            
        } catch (error) {
            this.showError(`UI generation failed: ${error.message}`);
        }
    }
    
    // Mock AI generation - will be replaced with actual AI service
    mockAIGeneration(data) {
        let html = '<div class="api-explorer">';
        html += '<h2>HATEOAS API Explorer</h2>';
        html += '<p>Dynamically discovered API structure:</p>';
        
        // Show raw data for debugging
        html += '<details><summary>Raw Response Data</summary>';
        html += `<pre>${JSON.stringify(data, null, 2)}</pre>`;
        html += '</details>';
        
        // Look for links in various possible locations
        const links = this.extractLinks(data);
        
        if (Object.keys(links).length > 0) {
            html += '<h3>Available Actions:</h3>';
            html += '<div class="actions">';
            
            for (const [rel, link] of Object.entries(links)) {
                const href = typeof link === 'string' ? link : link.href;
                
                // Try to get method from link.method, or infer from relationship type
                let method = 'GET';  // default
                if (typeof link === 'object' && link.method) {
                    method = link.method;
                } else {
                    // Infer HTTP method from relationship type
                    if (rel === 'create') method = 'POST';
                    else if (rel === 'update' || rel === 'edit') method = 'PUT';
                    else if (rel === 'delete') method = 'DELETE';
                    // GET is default for search, self, etc.
                }
                
                html += `<button onclick="window.app.followLink('${href}')" class="action-button">
                    ${rel.replace(/[-_]/g, ' ').toUpperCase()} (${method})
                </button>`;
            }
            
            html += '</div>';
        } else {
            html += '<p>No hypermedia links found in this response.</p>';
        }
        
        // Show any embedded data
        if (data._embedded) {
            html += '<h3>Data:</h3>';
            html += this.renderEmbeddedData(data._embedded);
        } else if (data.id || data.title || data.name) {
            // Single resource
            html += '<h3>Resource:</h3>';
            html += this.renderResource(data);
        }
        
        html += '</div>';
        
        // Add inline styles
        html += `<style>
            .api-explorer { padding: 1rem; }
            .actions { margin: 1rem 0; }
            .action-button { 
                margin: 0.5rem; 
                padding: 0.5rem 1rem; 
                background: #3498db; 
                color: white; 
                border: none; 
                border-radius: 4px; 
                cursor: pointer; 
            }
            .action-button:hover { background: #2980b9; }
            details { margin: 1rem 0; }
            pre { background: #f8f8f8; padding: 1rem; border-radius: 4px; overflow-x: auto; }
            .resource { margin: 0.5rem 0; padding: 1rem; border: 1px solid #ddd; border-radius: 4px; }
            .resource-list { display: grid; gap: 1rem; }
        </style>`;
        
        return html;
    }
    
    // Extract links from response, handling different hypermedia formats
    extractLinks(data) {
        const links = {};
        
        // HAL format (_links)
        if (data._links) {
            Object.assign(links, data._links);
        }
        
        // Direct links property
        if (data.links) {
            Object.assign(links, data.links);
        }
        
        // Look for href properties at root level
        if (data.href) {
            links.self = data.href;
        }
        
        return links;
    }
    
    renderEmbeddedData(embedded) {
        let html = '<div class="resource-list">';
        
        for (const [key, items] of Object.entries(embedded)) {
            if (Array.isArray(items)) {
                html += `<h4>${key.replace(/[-_]/g, ' ').toUpperCase()}:</h4>`;
                items.forEach(item => {
                    html += this.renderResource(item);
                });
            }
        }
        
        html += '</div>';
        return html;
    }
    
    renderResource(resource) {
        let html = '<div class="resource">';
        
        // Show key properties
        const importantProps = ['id', 'title', 'name', 'description', 'status', 'type'];
        for (const prop of importantProps) {
            if (resource[prop] !== undefined) {
                html += `<strong>${prop}:</strong> ${resource[prop]}<br>`;
            }
        }
        
        // Show any other properties (except _links)
        for (const [key, value] of Object.entries(resource)) {
            if (!importantProps.includes(key) && key !== '_links' && typeof value !== 'object') {
                html += `<strong>${key}:</strong> ${value}<br>`;
            }
        }
        
        html += '</div>';
        return html;
    }
    
    async followLink(href) {
        console.log('Following link:', href);
        
        try {
            const response = await fetch(href);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('Link response:', data);
            
            await this.generateUI(data);
            
        } catch (error) {
            this.showError(`Failed to follow link: ${error.message}`);
        }
    }
    
    showLoading() {
        this.loadingElement.classList.remove('hidden');
        this.errorElement.classList.add('hidden');
    }
    
    hideLoading() {
        this.loadingElement.classList.add('hidden');
    }
    
    showError(message) {
        this.hideLoading();
        this.errorElement.textContent = message;
        this.errorElement.classList.remove('hidden');
    }
}

// Initialize the app
document.addEventListener('DOMContentLoaded', () => {
    window.app = new SkullsApp();
});
