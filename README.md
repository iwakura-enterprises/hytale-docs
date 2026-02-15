# Docs - Ingame documentation

## MVP Goals
- Markdown support, incl. tip/hint/warning boxes
- Loading from files
- UI similar to docs.slimeblock.net / hytalemodding.dev

## 1.0.0 Goals
- Viewer, který bude schopný zparsovat Markdown (zapomocí https://github.com/commonmark/commonmark-java)
- Editor, který bude schopný vytvářet a upravovat existující dokumentace (uložené na serveru)
- API, pomocí kterého mody budou moct přidat vlastní dokumentaci

### Little Things
- Kategorie nalevo budou mít možný obrázek
- Topics pod kategoriemi budou mít možný obrázek taktéž (ale menší)
- Napravo bude chapter tree, který možná bude klikatelný
  - Ještě nevím jak, ale očividně je možné scrollovat z kodu. (viz. https://hytalemodding.dev/en/docs/official-documentation/custom-ui/type-documentation/elements/group, Event Callbacks, Scrolled)

## Viewer 1.0.0
Než začnu dělat editor, tak bude viewer.

- Simple struktura, jeden element pod druhým, kvůli tomu, že jsem nebyl schopný rozchodit vnotřené listy
- Jeden .ui soubor, ve kterém budou nadefinované veškeré elementy pomocí @templates (viz. https://hytalemodding.dev/en/docs/official-documentation/custom-ui/markup#templates)

```ui
// Obsahuje styly, atd
@DocsParagraph {
  Label { Text: @Content }
}

Group {

  // Bude jen zapisovat věci do @variablů atd.
  @DocsParagraph {
    @Content = "abcd";
  }
}
```

- Markdown support bude umět správně parsovat paragraphy, atd. do @DocsParagraph atd.
- Načítání z konfigurace pluginu