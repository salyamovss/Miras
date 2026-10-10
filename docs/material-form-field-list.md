# Material form: field inventory

**Status:** working draft based on the Admin form and Super Admin publication
queue screenshots shared in chat, plus the project discussion. Steps 2 and 4 of
the Admin form and the Super Admin material review screen were not shown, so
their exact fields still need confirmation.

## Admin form

The form has four steps: **1. Основное**, **2. Метаданные**, **3. Файлы и
доступ**, and **4. Проверка**. The shared form screenshot exposes fields from
steps 1 and 3.

### Step 1 — Основное

| Field | Required | Notes |
| --- | --- | --- |
| Название материала | Yes | Entered in the material's primary language. Metadata must ultimately be available in ky / ru / en; the translation interface is expected in step 2 or Super Admin review. |
| Краткое описание | Yes | Same translation behavior as the title; no automatic translation is assumed. |
| Тип материала | Yes | Selected from a fixed list. Exact option values still need confirmation before defining the enum. UI labels are localized to the admin's selected platform language. |
| Основной язык | Yes | The screenshot defaults to Кыргызский. This is the content/file language, not the site visitor's UI language. |
| Страна | Yes | Screenshot shows Кыргызстан. Confirm if this is a fixed country list and whether it means country of origin. |
| Год или период | Yes | Free-form text; examples shown: `1968` or `XIX век`. This should not be stored as an integer year only. |
| Автор, носитель знания или организация | No marker shown | One combined input is visible. The project requirement allows multiple authors; the form's support for adding multiple contributors should be confirmed. The submitter is separate. |
| Категории и ключевые слова | Yes | One combined field is visible, with examples such as `Эпос, устная традиция, Манас, сказительство`. Confirm whether this is free text, multiple tags, or structured categories plus keywords. |
| Место создания или записи | No marker shown | Optional in the screenshot; example: `Нарынская область, Кыргызстан`. |

### Step 3 — Файлы и доступ

| Field | Required | Notes |
| --- | --- | --- |
| Основной файл | Yes in practice | Upload accepts PDF, MP3, MP4, JPG, TIFF, up to 2 GB. The screen shows one primary-file drop zone; multiple files/editions per material are not confirmed. |
| Основной язык | Yes | The actual uploaded file language is tracked independently of metadata/UI language. A Kyrgyz-only book remains a Kyrgyz-only file. |
| Источник и правообладатель | Yes | Text field; placeholder mentions a foundation, archive, or original rights holder. |
| Условия публичного доступа | Yes | Current backend options: Открытый доступ and Только чтение. По запросу исследователя is omitted because the public site has no account or request workflow. The precise difference between open and read-only still needs definition. |

### Step 4 — Проверка and submission

The screenshot footer says required fields are marked with `*` and autosave is
enabled. Actions shown: **Сохранить черновик** and **Отправить на проверку**.

## Super Admin publication queue

The shared queue screenshot shows:

- search by title, author, or request number;
- status and material-type filters, plus sort order (oldest first shown);
- table columns: title, type, country, status, action;
- statuses including `На согласовании` and `Одобрено`;
- an **Открыть** action for each request.

The review detail screen was not included. Per the user's description, Super
Admin sees the material metadata and chooses a language at the top; the page
shows or edits the title and description in that language. Super Admin then
approves or rejects the submission. This is a workflow requirement, not a
visible control in the provided queue screenshot.

## Cross-cutting fields and rules

| Field / concept | Expected data | Notes |
| --- | --- | --- |
| Created/submitted by | Admin account reference | Derived from the logged-in admin; distinct from author(s), knowledge bearer(s), or organization. |
| Authors/contributors | One or more ordered entries | The UI currently shows one combined field, while the agreed domain rule allows multiple authors. |
| Localized metadata | One title and short description per `ky`, `ru`, `en` | Public site returns the locale selected by the visitor. Admin UI locale is separate. No whole-book translation is implied. |
| Review decision | Approve/reject, reviewer, time, optional note | Whether a rejection reason is required is not visible. |
| File storage reference | Storage key and file metadata | File bytes will later live in S3. S3 is not configured in this stage. |

## Workflow represented by the screenshots and discussion

1. Admin enters the material's primary-language metadata and provenance fields.
2. Admin attaches the actual file and enters its language and source/rights
   information, then selects a public access condition.
3. Admin can save a draft or send it for review.
4. Super Admin opens the request, selects a metadata language, reviews or
   completes the title/description in that language, then approves or rejects.
5. Approved material becomes available in the public catalog.

## Still needs confirmation

- Exact material type options and any fields conditional on type.
- All fields in Admin steps 2 (Метаданные) and 4 (Проверка).
- Whether the combined author/contributor input supports multiple authors,
  knowledge bearers, and organizations.
- Whether categories and keywords are free-form, multiple tags, or distinct
  structured values.
- Whether a cover image is separate from the primary file and whether multiple
  files/editions can be attached to one material.
- Whether **Открытый доступ** versus **Только чтение** controls downloading,
  online viewing, or both.
- Whether rejection requires a reason and whether Admin can edit/resubmit.
- Whether approval immediately publishes or a separate publish action/date is
  needed.

Do not add fields for the unresolved items to the database until confirmed.
