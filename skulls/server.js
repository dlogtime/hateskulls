const express = require('express');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

const { OpenRouterProvider } = require('./ai/OpenRouterProvider');

const app = express();
const PORT = process.env.PORT || 3000;

// UUID namespace for frontend admin APIs to avoid backend collision
const FRONTEND_API_NAMESPACE = process.env.FRONTEND_API_NAMESPACE || 'f47b3c8e-1a2d-4e5f-9c8b-3d7e2f1a5c9e';

// Middleware
app.use(express.json());
app.use(express.static(path.join(__dirname)));

// Initialise AI provider
const aiProvider = new OpenRouterProvider();

// AI API endpoints - using UUID namespace to avoid collision with backend APIs
app.post(`/api/${FRONTEND_API_NAMESPACE}/generate-ui`, async (req, res) => {
  try {
    const { hateoasResponse } = req.body;
    const result = await aiProvider.generateUI(hateoasResponse);
    res.json(result);
  } catch (error) {
    console.error(`${aiProvider.getName()} generation error:`, error);
    res.status(500).json({ error: 'Failed to generate UI' });
  }
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Serve index.html for any non-API routes (SPA fallback)
app.get('/{*any}', (req, res) => {
  if (!req.path.startsWith('/api/')) {
    res.sendFile(path.join(__dirname, 'index.html'));
  } else {
    res.status(404).json({ error: 'API endpoint not found' });
  }
});

app.listen(PORT, () => {
  console.log(`Skulls HATEOAS frontend running on http://localhost:${PORT}`);
  console.log(`AI Provider: ${aiProvider.getName()}`);
});
