# Constância — Guia de estudo para a defesa

## 1. A arquitetura em uma frase

> **A tela só desenha estado e avisa eventos; o ViewModel guarda o estado e decide; o Repository busca os dados; o DAO (Room) e o Retrofit falam com o banco e a internet.**

```
 Tela (@Composable) ──evento──▶ ViewModel ──▶ Repository ──┬──▶ DAO (Room) ──▶ SQLite
        ▲                           │                       └──▶ Retrofit ──▶ BrasilAPI
        └──── estado (StateFlow) ◀──┘◀───────── Flow ◀──────────────┘
```

Isso é **MVVM**. Em .NET seria: componente Blazor → serviço/ViewModel → repositório → DbContext/HttpClient.

---

## 2. Onde está cada requisito (abra estes arquivos se o professor pedir)

| Requisito | Pontos | Onde mostrar |
|---|---|---|
| Kotlin + Compose | — | qualquer `*Screen.kt`; `MainActivity.kt` só chama `ConstanciaApp()` |
| CRUD em 2 tabelas | 1,5 | **Habito**: `HabitoDao.kt` + telas Hábitos/Formulário/Detalhes. **RegistroDiario**: `RegistroDiarioDao.kt` + telas Hoje/Histórico |
| Room | 1,0 | `Habito.kt`, `RegistroDiario.kt` (FK + CASCADE + índice único), `Converters.kt`, `ConstanciaDatabase.kt` |
| API | 1,0 | `BrasilApiService.kt`, `FeriadoRepository.kt` (erros), `CalculadoraSequencia.kt` (uso dos dados) |
| Compartilhar | 0,5 | `DetalhesScreen.kt` → botão "Compartilhar progresso" (`Intent.ACTION_SEND`) |
| Interface | 1,0 | estados carregando/vazio/erro em todas as telas; estado no ViewModel (rotação) |

### CRUD — onde cada operação acontece

| | Habito | RegistroDiario |
|---|---|---|
| **Create** | botão + → formulário → Salvar | marcar na tela Hoje · "Registrar dia" no Histórico |
| **Read** | lista de Hábitos · Detalhes | tela Hoje (registros do dia) · Histórico (registros do hábito) |
| **Update** | lápis → formulário preenchido | botões +/− na tela Hoje · lápis no Histórico (valor e data) |
| **Delete** | lixeira (lista ou Detalhes) com confirmação | desmarcar na tela Hoje · lixeira no Histórico |

---

## 3. Roteiro de demonstração (≈ 3 minutos)

1. **Hábitos → +**: tente salvar vazio (mostra a validação). Crie "Beber água" com meta 8 e "Ler" só de segunda a sexta.
2. **Gire a tela** com o formulário preenchido → nada se perde.
3. **Hoje**: toque + três vezes em Beber água (3 de 8), marque Ler.
4. Toque no hábito → **Detalhes**: sequência, recorde e próximo feriado (vindo da API).
5. **Compartilhar progresso** → janela do Android com o texto.
6. **Ver histórico** → "Registrar dia" em 1º/11 e 3/11 → **Finados (2/11) aparece destacado**; edite um valor; exclua um registro.
7. **Modo avião** → fechar e abrir o app → Detalhes mostra o erro amigável → tirar modo avião → **Tentar novamente** → o recorde muda porque o feriado passa a contar como descanso.
8. Excluir o hábito → o histórico some junto (CASCADE).

> Dica: comece a demonstração com o app limpo (Configurações do emulador → Apps → Constância → Armazenamento → Limpar dados).

---

## 4. Resumo de cada arquivo

### `data/local` — banco (Room)
- **`Habito.kt`**: `@Entity` da tabela `habitos`. `meta: Int?` é nulo para hábitos "feito/não feito". `alvoDiario` = `meta ?: 1`. `programadoPara(data)` diz se o hábito vale naquele dia da semana.
- **`RegistroDiario.kt`**: tabela `registros_diarios`. `@ForeignKey(onDelete = CASCADE)` para `habitos.id` e `@Index(habitoId, data, unique = true)` (no máximo um registro por dia).
- **`Converters.kt`**: `LocalDate` ↔ `Long`, `LocalTime` ↔ `String`, `Set<DayOfWeek>` ↔ `"1,3,5"`. O SQLite só entende tipos simples.
- **`HabitoDao.kt` / `RegistroDiarioDao.kt`**: interfaces com `@Insert`, `@Query`, `@Update`, `@Delete`. O KSP gera a implementação na compilação. Leituras retornam `Flow` (atualizam sozinhas); escritas são `suspend`.
- **`ConstanciaDatabase.kt`**: `@Database` com as duas entidades, versão 1, singleton (`companion object` + `synchronized`).

### `data/remote` e `data/repository`
- **`BrasilApiService.kt`**: `FeriadoDto` (formato do JSON) + interface Retrofit `@GET("api/feriados/v1/{ano}")`.
- **`FeriadoRepository.kt`**: chama a API, guarda cache em memória por ano, trata `IOException` (sem internet), `HttpException` (erro do servidor) e qualquer outra `Exception`. Devolve `ResultadoFeriados.Sucesso` ou `.Erro` (`sealed interface`).
- **`HabitoRepository.kt` / `RegistroRepository.kt`**: repassam para os DAOs. O de registros tem `ajustarValor()` e `alternarFeito()`, que decidem entre criar, atualizar ou apagar o registro do dia.
- **`AppContainer.kt`**: cria banco, Retrofit e repositórios uma vez só (injeção de dependência manual, `by lazy`).
- **`ConstanciaApplication.kt`**: cria o `AppContainer` quando o app abre (registrada no `AndroidManifest.xml`).

### `domain`
- **`CalculadoraSequencia.kt`**: regra de negócio pura. Cada dia é **concluído** (+1), **neutro** (não programado, feriado ou hoje ainda não feito) ou **falha** (quebra). Sequência atual = de hoje para trás até a primeira falha; recorde = maior sequência do início até hoje. Testada em `CalculadoraSequenciaTest.kt` (6 testes).

### `ui`
- **`AppViewModelProvider.kt`**: fábrica que cria cada ViewModel com seus repositórios (e o `SavedStateHandle` com o id da rota).
- **`navigation/ConstanciaApp.kt`**: `NavHost` com as rotas `hoje`, `habitos`, `habito_form?id={id}`, `detalhes/{habitoId}`, `historico/{habitoId}` e a barra inferior.
- **`habitos/`**: lista (`HabitosViewModel` com `StateFlow`) e formulário (`HabitoFormViewModel` com `mutableStateOf`, validação, mesmo formulário para criar e editar).
- **`hoje/`**: `combine` de hábitos + registros do dia + feriado de hoje.
- **`detalhes/`**: estatísticas + próximo feriado + compartilhar + excluir.
- **`historico/`**: lista misturando registros e feriados (`sealed interface ItemHistorico`), calendário (`DatePickerDialog`), diálogos de editar/excluir, Snackbar.
- **`util/`**: emojis/cores, formatação de datas em pt-BR, componentes de "carregando/erro de feriados".

---

## 5. Kotlin ↔ C# (cola rápida)

| Kotlin | C# |
|---|---|
| `val` / `var` | `readonly` / variável normal |
| `data class` + `copy()` | `record` + `with { }` |
| `Int?`, `?.`, `?:`, `!!` | `int?`, `?.`, `??`, "garanto que não é null" (lança exceção) |
| `suspend fun` | `async Task` |
| `viewModelScope.launch { }` | disparar uma `Task` presa ao ciclo de vida |
| `Flow<T>` / `StateFlow<T>` | `IAsyncEnumerable` / `IObservable` com valor atual |
| `companion object` | membros `static` |
| `object` | classe `static` / singleton |
| `sealed interface` + `when` | união discriminada + `switch` exaustivo |
| `fun Tipo.metodo()` (extensão) | extension method |
| `filter` / `map` / `associateBy` | `Where` / `Select` / `ToDictionary` |
| `by lazy { }` | `Lazy<T>` |
| Room | Entity Framework (+ SQL escrito à mão, validado na compilação) |
| Retrofit + Gson | Refit / `HttpClient` + `System.Text.Json` |
| KSP | Source Generators |
| `@Composable` | componente `.razor` |
| Navigation Compose | `@page` + `NavigationManager` |

---

## 6. Perguntas prováveis — com respostas curtas

### Room / banco
1. **O que é uma Entity?** Uma classe que vira tabela. Cada propriedade vira coluna; `@PrimaryKey(autoGenerate = true)` é o `IDENTITY`.
2. **O que é um DAO e quem implementa?** A interface com as operações do banco. Eu só declaro; o Room gera a implementação na compilação com o KSP (`HabitoDao_Impl`).
3. **Como as tabelas se relacionam?** `RegistroDiario.habitoId` é chave estrangeira para `Habito.id` (1 hábito → N registros).
4. **O que acontece com os registros ao excluir um hábito?** São apagados pelo banco por causa do `onDelete = ForeignKey.CASCADE`.
5. **Como você impede dois registros no mesmo dia?** Índice único em `(habitoId, data)`. A regra fica garantida no próprio banco.
6. **Para que servem os TypeConverters?** O SQLite só guarda tipos simples; os converters transformam `LocalDate`, `LocalTime` e `Set<DayOfWeek>` em `Long`/`String` e de volta.
7. **Por que o banco é singleton?** Abrir o banco é caro e várias instâncias podem conflitar. `@Volatile` + `synchronized` garantem uma só instância mesmo com várias threads.
8. **E se você mudar uma entidade?** Tenho que subir `version`. Configurei `fallbackToDestructiveMigration` (recria o banco); num app real eu escreveria uma `Migration`.
9. **Por que leituras retornam `Flow` e escritas são `suspend`?** O `Flow` emite de novo sempre que a tabela muda (tela atualiza sozinha). Escrita é uma operação única que precisa sair da thread principal, então é `suspend`.

### Coroutines / Flow / ViewModel
10. **O que é uma coroutine?** Código assíncrono leve, como `async/await`. Room e Retrofit rodam fora da thread da tela, então a interface não trava.
11. **O que é `viewModelScope`?** Um escopo de coroutines que é cancelado quando o ViewModel é destruído. Evita trabalho "órfão".
12. **Por que o estado fica no ViewModel?** Ao girar a tela a Activity é recriada, mas o ViewModel sobrevive. O que o usuário digitou continua lá. Um `remember` comum se perderia.
13. **Diferença entre `Flow` e `StateFlow`?** `StateFlow` sempre tem um valor atual (a tela tem o que desenhar imediatamente). Convertemos com `stateIn`.
14. **O que faz `combine`?** Junta vários fluxos e recalcula quando qualquer um muda (ex.: hábitos + registros do dia + feriado).
15. **O que é `WhileSubscribed(5_000)`?** Mantém a consulta ativa por 5s depois que a tela some — suficiente para sobreviver a uma rotação sem refazer tudo.

### Compose / interface
16. **O que é recomposição?** Quando um estado muda, o Compose chama de novo só as funções `@Composable` que leem esse estado.
17. **Para que serve o `Scaffold`?** Estrutura padrão Material (barra de topo, FAB, Snackbar) e fornece o `innerPadding` para o conteúdo não ficar embaixo das barras — evita sobreposição.
18. **Por que `LazyColumn` e não `Column`?** `LazyColumn` só desenha os itens visíveis (lista virtualizada), como o `<Virtualize>` do Blazor.
19. **Como você trata estados de carregando, vazio e erro?** Cada `UiState` tem `carregando`; a tela usa `when` para mostrar `CircularProgressIndicator`, `MensagemVazia` ou o cartão de erro com "Tentar novamente".
20. **Como funciona a navegação?** `NavHost` com rotas em texto. Parâmetros como `{habitoId}` chegam ao ViewModel pelo `SavedStateHandle`.

### API
21. **Como você consome a API?** Retrofit: declaro uma interface com `@GET` e `@Path`; o Retrofit gera a implementação. O `GsonConverterFactory` converte JSON em `FeriadoDto`.
22. **O que acontece se a API estiver fora do ar ou sem internet?** O `FeriadoRepository` captura `IOException`/`HttpException`, devolve `ResultadoFeriados.Erro`, e a tela mostra uma mensagem amigável com "Tentar novamente". O app não fecha e a sequência é calculada sem feriados.
23. **Como os dados da API são usados?** Feriados viram dias "neutros" na `CalculadoraSequencia` (não quebram a sequência), aparecem destacados no histórico e como "próximo feriado" nos Detalhes.
24. **Por que a permissão `INTERNET`?** Sem ela o Android bloqueia qualquer acesso à rede do app.
25. **O que é um DTO?** Uma classe só com o formato do JSON. Converto para o que o app usa (`LocalDate` → nome) no repositório.

### Compartilhamento
26. **Como funciona o compartilhar?** `Intent(ACTION_SEND)` com `type = "text/plain"` e `EXTRA_TEXT`; `createChooser` mostra a lista de apps que aceitam texto.
27. **Intent implícito × explícito?** Implícito diz *o que* fazer (enviar texto) e o Android escolhe o app; explícito diz *qual* tela/classe abrir.

### Regra de negócio
28. **Se o usuário ainda não marcou hoje, a sequência zera?** Não: "hoje" é neutro enquanto o dia não acabou.
29. **E num sábado, para um hábito de segunda a sexta?** Neutro: não soma e não quebra.
30. **Como você testou a sequência?** Testes unitários JUnit em `CalculadoraSequenciaTest.kt` (rodam sem emulador): dias seguidos, dia esquecido, feriado, fim de semana e meta não atingida.

---

## 7. Limitações conhecidas (melhor você falar antes que perguntem)

- **Cache de feriados só em memória**: ao reabrir o app sem internet, os feriados não são considerados até a conexão voltar. Melhoria: salvar no Room.
- **"Hoje" é fixado quando a tela abre**: se o app ficar aberto virando a meia-noite, a tela Hoje só atualiza ao ser reaberta.
- **Morte de processo**: rotação está coberta (ViewModel); se o Android matar o app em segundo plano, um formulário em edição se perde. Melhoria: guardar no `SavedStateHandle`.
- **Injeção de dependência manual**: num projeto maior eu usaria Hilt.
- **Lembretes (WorkManager)** não foram feitos — eram extras.
