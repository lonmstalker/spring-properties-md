---
name: spring-properties-md-annotations
description: "Reference and usage patterns for spring-properties-md annotations on @ConfigurationProperties: PropertyGroupDoc, PropertyDoc and Requirement, PropertyExample and PropertyExamples, PropertyDeprecation, PropertyCategory, PropertySince, PropertySee and PropertySeeReferences, PropertyCustomMetadata and PropertyCustomMetadataEntries. Triggers: PropertyDoc, PropertyGroupDoc, PropertyExample, PropertyDeprecation, PropertyCategory, PropertySince, PropertySee, PropertyCustomMetadata."
---

# spring-properties-md annotations (reference)

## Effective targets (current processor behavior)

- TYPE (class): `@PropertyGroupDoc`, `@PropertyCategory`
- Property metadata: `@PropertyDoc`, `@PropertyExample`, `@PropertyDeprecation`, `@PropertyCategory`, `@PropertySince`, `@PropertySee`, `@PropertyCustomMetadata` on fields, record components, JavaBean getters, and constructor parameters where the annotation target allows it.

Note: when multiple elements describe the same logical property, field/record metadata has priority over getter metadata, which has priority over constructor parameter metadata.

## Annotations

### `@PropertyGroupDoc` (TYPE)
Use on a `@ConfigurationProperties` class to document the group:
- `displayName`: заголовок группы (если пусто, используется имя класса)
- `description`: описание группы
- `category`: категория группы (может быть переопределена через `@PropertyCategory` на TYPE)
- `order`: сортировка групп (меньше = раньше)

### `@PropertyDoc` (FIELD, METHOD, PARAMETER, RECORD_COMPONENT)
Use on a property field to describe it:
- `description`: человекочитаемое описание
- `required`: `Requirement.AUTO | Requirement.REQUIRED | Requirement.OPTIONAL`
  - важно: сейчас “required=true” выставляется только при `Requirement.REQUIRED`
- `profiles`: список профилей (строки)
- `sensitive`: пометка “sensitive”
- `typeDisplay`: override для отображаемого типа (строка)

### `Requirement` (enum)
- `AUTO`
- `REQUIRED`
- `OPTIONAL`

### `@PropertyExample` (FIELD, METHOD, PARAMETER, RECORD_COMPONENT, repeatable)
Use to provide example values:
- `value`: пример значения (обязателен)
- `description`: подпись к примеру (опционально)

Container (служебное): `@PropertyExamples` (его обычно не пишут руками).

### `@PropertyDeprecation` (FIELD, METHOD, RECORD_COMPONENT)
Use to mark a property as deprecated:
- `reason`
- `replacedBy`
- `since`
- `removalVersion`

### `@PropertyCategory` (TYPE, FIELD, METHOD, RECORD_COMPONENT)
Use to categorize:
- `value`: category
- `subcategory`: subcategory (в основном полезно для properties)

Правила:
- для группы: TYPE-level `@PropertyCategory` имеет приоритет над `@PropertyGroupDoc.category`
- для property: FIELD-level `@PropertyCategory` задает `category/subcategory` у property

### `@PropertySince` (FIELD, METHOD, RECORD_COMPONENT)
Use to indicate since-version for a property:
- `value`: версия (строка)

### `@PropertySee` (FIELD, METHOD, RECORD_COMPONENT, repeatable)
Use for “see also” references:
- `value`: строка-ссылка (например ключ/URL/имя свойства)

Container (служебное): `@PropertySeeReferences`.

### `@PropertyCustomMetadata` (FIELD, METHOD, RECORD_COMPONENT, repeatable)
Use for arbitrary key/value metadata:
- `key`
- `value`

Container (служебное): `@PropertyCustomMetadataEntries`.

Note: custom metadata is stored as a map; duplicate keys overwrite previous values.

## Naming and filtering rules

- prefix берется из `@ConfigurationProperties(prefix = "...")` (или `value`)
- имя поля конвертируется в kebab-case: `maxRetries` -> `max-retries`
- поля, начинающиеся с `$`, игнорируются processor’ом

## Default value note

Сейчас `defaultValue` извлекается только из compile-time constants (`field.getConstantValue()`).
Для обычных полей (например `private int port = 8080;`) значение по умолчанию может не попасть в output.
Если это важно для docs, используй `@PropertyExample` или явно укажи в `@PropertyDoc.description`.

## Example

```java
import io.github.springpropertiesmd.api.annotation.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.server")
@PropertyGroupDoc(
        displayName = "Server Configuration",
        description = "HTTP server settings",
        category = "Server",
        order = 1
)
public class ServerProperties {

    @PropertyDoc(description = "Server port number", required = Requirement.REQUIRED)
    @PropertyExample(value = "8080", description = "default")
    @PropertyExample(value = "443", description = "HTTPS")
    @PropertySince("1.0")
    private int port;

    @PropertyDoc(description = "Server bind address", required = Requirement.OPTIONAL)
    @PropertyCategory(value = "Server", subcategory = "Networking")
    @PropertyCustomMetadata(key = "env", value = "APP_SERVER_HOST")
    private String host;
}
```
