## Controller

#### Funciones

#### Validaciones


Ctrl + Click `import jakarta.validation.constraints` para ver las anotaciones de validaciones disponibles para el DTO.

```
@PutMapping("/update/{idBook}")
fun updateBook(@PathVariable @Positive idBook: Int, @RequestBody @Valid newBookDTO: NewBookDTO)
```

`@PathVariable`
`@RequestBody`

#### Tests