# GenBang

## Структура
Сделать такую структуру требует сам clojure. Я пытался сделать кастом, но оно отказывается работать. В наименованиях директорий надо использовать "_" тк clojure преобразует их в "-" в namespace.
В итоге:

GenBang/ <br>
├── src/ <br>
│&emsp;&emsp;└── genotype_mutator/ <br>
│&emsp;&emsp;&emsp;&emsp;&emsp;└── mutoslav.clj <br>
│&emsp;&emsp;└── genotype_generator/ <br>
│&emsp;&emsp;&emsp;&emsp;&emsp;└── gennadiy.clj <br>
└── deps.edn <br>
└── limits.edn <br>

### gennadiy.clj
Генератор изначального генотипа

### mutoslav.clj
Генератор мутаций

### limits.edn
Файл с ограничениями для каждого гена

### deps.edn
Конфиг файл

## Запуск
Производится из корневой директории проекта

### mutoslav.clj
clojure -M -m genotype-mutator.mutoslav \"[-3 -1 -7 0 -5 0 0 0 -9 0 3 7 1 -3 0 10]\" <br>
(вектор как пример) (вектор должен быть единственным аргументом) (обязательны двойные кавычки)

### gennadiy.clj
clojure -M -m genotype-generator.gennadiy

## Вывод

mutoslav.clj - генотип с произведённой мутацией в рамках лимитов (EDN-вектор)

gennadiy.clj - первичный генотип (EDN-вектор)
