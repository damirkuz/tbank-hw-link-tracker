package backend.academy.linktracker.scrapper.service;

// Используйте REST API репозиториев, чтобы проверять изменения — основные варианты:
//
// Получить метаданные репозитория и поле обновления
// Вызов: GET /repos/{owner}/{repo}
// Посмотрите поле updated_at в ответе — оно показывает время последнего обновления репозитория.
// Документация: REST API — Repositories
// Пример curl (по образцу REST-примеров):
//
// curl -H "Accept: application/vnd.github+json" \
// -H "Authorization: Bearer <YOUR-TOKEN>" \
// https://api.github.com/repos/OWNER/REPO

public class UpdateChecker {}
