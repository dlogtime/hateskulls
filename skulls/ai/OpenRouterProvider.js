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
                DO NOT GENERATE PLACEHOLDER TEXT OR ELEMENTS.
                IF AN ELEMENT IS INTERACTABLE IT NEEDS TO DO SOMETHING.
                Forms and other elements may be made to appear dynamically where they make sense (e.g. modals, expanding boxes, other common CSS tricks). Use your creativity!
                BUT ONCLICK EVENTS MUST NOT CALL FUNCTIONS. THEY CAN ONLY CHANGE THE DISPLAY STYLE OF ELEMENTS OR NAVIGATE.

                NAVIGATION: Always use "window.app.followLink(url)" for GET navigation.

                HAL-FORMS HANDLING:
                - Look for "_templates" in the HATEOAS response for form specifications
                - If it doesn't exist, try to find a similar field that makes sense, but if that doesn't exist either, 
                  then show a warning that the backend is not properly implemented and DO NOT generate the form.
                - Generate HTML forms based on the template properties (field names, types, validation)
                - For form submissions, collect form data and use:
                  window.app.followLink(template.target, { 
                    method: template.method, 
                    data: { field1: value1, field2: value2 } 
                  })
                - Form buttons should gather all form field values and submit them
                - Use template validation rules (required, min, max, regex) for client-side validation
                - Make forms beautiful with proper styling, animations, and UX feedback

                EXAMPLE FORM SUBMIT BUTTON:
                onclick="
                  const formData = {
                    title: document.getElementById('title').value,
                    description: document.getElementById('description').value,
                    requestedBy: document.getElementById('requestedBy').value
                  };
                  window.app.followLink('http://localhost:8080/change-requests', {
                    method: 'POST',
                    data: formData
                  });
                "
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
