# HateSkulls - AI-generated Frontends

Just exploring the idea that if webservers follow standardised patterns -- e.g. HATEOAS + HAL-FORMS in this case -- then AI can generate functional UIs without custom frontend development.

`hate/`: Spring Boot HATEOAS implementation. Simple CRUD example of a Change Request ticketing system.

`skulls/`: Node.js server that calls AI to generate HTML. Should work with any HATEOAS compliant backend in theory.

## Etymology

HATEOAS -> HATE -> HateSkulls

It's the title of a track from the game Sayonara Wild Hearts.
Not much more to it than this word association. Great game btw.

## How to run

### Prerequisites

- Java 21+ (for Spring Boot)
- Node.js 22+
- OpenRouter API key (didn't bother implementing other providers yet)

### Configuration

Copy `skulls/.env.example` to `skulls/.env` and update with your OpenRouter API key:
```
OPENROUTER_API_KEY=your-key-here
```
You can also specify the model, but anything cheaper than `claude-sonnet-4` produced buggy output during testing.

### HATE

```bash
cd hate
./mvnw spring-boot:run
```

### SKULLS

```bash
cd skulls
node server.js
```

## Notes

Yes I know the generation is slow.
