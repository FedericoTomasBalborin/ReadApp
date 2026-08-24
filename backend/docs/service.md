## Service

#### Funciones

#### Validaciones

```
@PutMapping("/update/{idBook}")
fun updateBook(@PathVariable @Positive idBook: Int, @RequestBody @Valid newBookDTO: NewBookDTO)
```

`@PathVariable`
`@RequestBody`

#### Tests