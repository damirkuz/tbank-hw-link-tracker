# LinkTracker

LinkTracker – Telegram-бот, который отслеживает изменения на веб-страницах и оперативно информирует пользователя о них.

## Запуск проекта

1. положить в переменные среды Idea:
   - для Bot: APP_TELEGRAM_TOKEN=токен
   - для Scrapper:
     - GITHUB_TOKEN=токен
     - STACKOVERFLOW_KEY=токен
     - STACKOVERFLOW_ACCESS_KEY=тут можно оставить пустым
2. Запустить класс bot.src.main.java.backend.academy.linktracker.bot.BotApplication
3. Запустить класс scrapper.src.main.java.backend.academy.linktracker.scrapper.ScrapperApplication

