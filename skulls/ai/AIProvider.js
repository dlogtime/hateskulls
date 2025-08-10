/**
 * Abstract base class for AI providers
 * 
 * PUBLIC API:
 * generateUI(hateoasResponse): Promise<string>
 *   - Main entry point - call this to generate UI from HATEOAS data
 *   - Returns clean, validated HTML ready for insertion
 *   - Handles all processing, validation, and error fallbacks
 * 
 * IMPLEMENTATION FLOW:
 * 1. Client calls generateUI(hateoasResponse)
 * 2. Calls your callProviderAPI(hateoasResponse) 
 * 3. Processes raw response (strips markdown, validates)
 * 4. Returns clean HTML or fallback UI on errors
 * 
 * TO IMPLEMENT A NEW PROVIDER:
 * 
 * getName(): string
 *   - Return a human-readable provider name (e.g., 'OpenRouter', 'Claude')
 * 
 * callProviderAPI(hateoasResponse: Object): Promise<string>
 *   - Take HATEOAS response object containing _links, _embedded, etc.
 *   - Return raw HTML content from AI (may include markdown code fences)
 *   - Throw descriptive error if API call fails
 *   - Processing/validation is handled automatically
 */
class AIProvider {
  /**
   * Generate UI suggestions based on HATEOAS response
   * @param {Object} hateoasResponse - The HATEOAS API response
   * @returns {Promise<Object>} UI generation result
   */

  /**
   * Get provider name for identification
   * @returns {string} Human-readable provider name (e.g., 'OpenRouter', 'Claude')
   */
  getName() {
    throw new Error('getName method must be implemented by subclass');
  }

  async generateUI(hateoasResponse) {
    const rawContent = await this.callProviderAPI(hateoasResponse);
    return this.processAndValidateContent(rawContent);
  }

  /**
   * Call the AI provider's API to generate HTML content
   * @param {Object} hateoasResponse - The HATEOAS API response object containing _links, _embedded, etc.
   * @returns {Promise<string>} Raw HTML content from AI (may include markdown code fences)
   * @throws {Error} Should throw descriptive error if API call fails
   */
  async callProviderAPI(hateoasResponse) {
    throw new Error('callProviderAPI method must be implemented by subclass');
  }

  processAndValidateContent(rawContent) {
    const processedContent = this.stripCodeFences(rawContent);
    // TODO validation
    return { html: processedContent };
  }

  /**
   * Strip markdown code fences from AI response content
   * @param {string} content - Raw content with potential ```language markers
   * @returns {string} Clean content without code fences
   */
  stripCodeFences(content) {
    return content
      .replace(/^```\w*\n?/, '')  // Remove opening ```language
      .replace(/\n?```$/, '')     // Remove closing ```
      .trim();
  }

}

module.exports = { AIProvider };
