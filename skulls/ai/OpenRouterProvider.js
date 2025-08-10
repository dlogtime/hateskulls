const { AIProvider } = require('./AIProvider');

/**
 * OpenRouter implementation of AIProvider
 */
class OpenRouterProvider extends AIProvider {

  getName() {
    return 'OpenRouter';
  }

  async callProviderAPI(hateoasResponse) {
    try {
      const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${process.env.OPENROUTER_API_KEY}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          model: process.env.OPENROUTER_MODEL || 'anthropic/claude-sonnet-4',
          messages: [
            {
              role: 'system',
              content: `
                You are a UI generator that creates HTML from HATEOAS + HAL-FORMS compliant JSON responses.
                The <html> and <head> and <body> tags are already present, and your output will be inserted into the <body>.
                DO NOT use any external libraries or frameworks.
                DO NOT include any scripts.
                ONLY GENERATE PURE HTML WITH INLINE CSS.
                BE CREATIVE AND HAVE FUN with your CSS styling - use animations, gradients, shadows, interesting layouts!
                ENSURE THE HTML IS BEAUTIFUL AND USABLE;
                DO NOT GENERATE PLACEHOLDER TEXT OR ELEMENTS.
                IF AN ELEMENT IS INTERACTABLE IT NEEDS TO DO SOMETHING.
                ALWAYS use "window.app.followLink(url)" for navigation.
              `
            },
            {
              role: 'user',
              content: `Generate HTML UI for this HATEOAS response: ${JSON.stringify(hateoasResponse)}`
            }
          ]
        })
      });

      if (!response.ok) {
        throw new Error(`OpenRouter API error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      return data.choices[0].message.content;
      
    } catch (error) {
      console.error('OpenRouter generation failed:', error);
      throw new Error(`Failed to generate UI: ${error.message}`);
    }
  }

}

module.exports = { OpenRouterProvider };
