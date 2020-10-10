# Guião de Demonstração

## 1. Preparação do Sistema

### 1.1. Compilar o Projeto

Em primeiro lugar, é necessário instalar as dependências necessárias para o *silo* e os clientes (*eye* e *spotter*), bem como compilar estes componentes.
Para isso, basta executar o seguinte comandona diretoria *root* do projeto:

```
$ mvn clean install -DskipTests
```

### 1.2. *Silo*

Para proceder aos testes, é preciso o servidor *silo* estar a correr. 
Para isso basta ir à diretoria *silo-server* e executar:

```
$ mvn exec:java
```

Este comando vai colocar o *silo* no endereço *localhost* e na porta *8080*.

### 1.3. *Eye*

Vamos registar 1 câmara e as respetivas observações. 
A câmara vai ter o seu ficheiro de entrada próprio com observações já definidas.
Para isso basta ir à diretoria *eye* e correr o seguinte comando:

```
$ mvn exec:java -Dname=RNL -Dlat=38.737857 -Dlon=-9.137855 < ../demo/cam_input.txt
```

A seguir a executar o comando acima, é necessário esperar 10 segundos para todos os dados serem enviados. 

## 2. Teste das Operações

Nesta secção vamos correr os comandos necessários para testar todas as operações. 
Cada subsecção é respetiva a cada operação presente no *silo*.

### 2.1. *cam_join*

Esta operação já foi testada na preparação do ambiente, no entanto ainda é necessário testar algumas restrições.

#### 2.1.1. Teste das câmaras com nome duplicado e coordenadas diferentes  
O servidor deve rejeitar esta operação. 
Para isso basta executar um *eye* com o seguinte comando:

```
$ mvn exec:java -Dname=RNL -Dlat=1.0 -Dlon=1.0
```

#### 2.1.2. Teste do tamanho do nome 
O servidor deve rejeitar estas operações. 
Para isso basta executar um *eye* com o seguinte comando:

```
$ mvn exec:java -Dname=ab -Dlat=1.0 -Dlon=1.0
$ mvn exec:java -Dname=abcdefghijklmnop -Dlat=1.0 -Dlon=1.0
```

### 2.2. *cam_info*

Esta operação não tem nenhum comando específico associado e para isso é necessário ver qual o nome do comando associado a esta operação. 
Para isso precisamos instanciar um cliente *spotter*, presente na diretoria com o mesmo nome:

```
$ mvn exec:java
```

#### 2.2.1. Teste para uma câmara existente  
O servidor deve responder com as coordenadas de localização da câmara *RNL*:

```
> info RNL
RNL: 38.737857, -9.137855
```

#### 2.2.2. Teste para câmara inexistente 
O servidor deve rejeitar esta operação:

```
> info Inexistente
Caught exception: Camera not found: 'Inexistente'
```

### 2.3. *report*

Esta operação já foi testada acima na preparação do ambiente. Inclusive, foi testado o sucesso do comando *zzz*, com uma espera de 10 segundos.
Os resultados desta operação serão verificados pelos comandos seguintes.

### 2.4. *track*

Esta operação vai ser testada utilizando o comando *spot* com um identificador.

#### 2.4.1. Teste com pessoa já observada

```
> spot person 112233
person,112233,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.4.2. Teste com uma pessoa não observada

```
> spot person 14388236
Caught exception: Person not found: '14388236'
```

#### 2.4.3. Teste com uma pessoa com id inválido

```
> spot person 0
Caught exception: Invalid person id: '0'
```

#### 2.4.3. Teste com um carro já observado

```
> spot car AA00BB
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.4.4. Teste com um carro não observado

```
> spot car FF11FF
Caught exception: Car not found: 'FF11FF'
```

#### 2.4.5. Teste com um carro com matrícula inválida

```
> spot car AA
Caught exception: Invalid car plate: 'AA'
```

### 2.5. *trackMatch*

Esta operação vai ser testada utilizando o comando *spot* com um fragmento de identificador.

#### 2.5.1. Testes com uma pessoa já observada

```
> spot person 1122*
person,112233,<timestamp>,RNL,38.737857,-9.137855

> spot person 112*33
person,112233,<timestamp>,RNL,38.737857,-9.137855

> spot person *2233
person,112233,<timestamp>,RNL,38.737857,-9.137855

> spot person *1223*
person,112233,<timestamp>,RNL,38.737857,-9.137855

> spot person *1*2*3*
person,112233,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.5.2. Testes com múltiplas pessoas observadas

```
> spot person 11*
person,112233,<timestamp>,RNL,38.737857,-9.137855
person,114455,<timestamp>,RNL,38.737857,-9.137855
person,114655,<timestamp>,RNL,38.737857,-9.137855

> spot person 11*55
person,114455,<timestamp>,RNL,38.737857,-9.137855
person,114655,<timestamp>,RNL,38.737857,-9.137855

> spot person *55
person,114455,<timestamp>,RNL,38.737857,-9.137855
person,114655,<timestamp>,RNL,38.737857,-9.137855
person,662255,<timestamp>,RNL,38.737857,-9.137855

> spot person *22*
person,112233,<timestamp>,RNL,38.737857,-9.137855
person,662255,<timestamp>,RNL,38.737857,-9.137855

> spot person *1*4*5*
person,114455,<timestamp>,RNL,38.737857,-9.137855
person,114655,<timestamp>,RNL,38.737857,-9.137855

> spot person *
person,112233,<timestamp>,RNL,38.737857,-9.137855
person,114455,<timestamp>,RNL,38.737857,-9.137855
person,114655,<timestamp>,RNL,38.737857,-9.137855
person,662255,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.5.3. Teste com uma pessoa não observada

```
> spot person 22*
Person(s) not found
```

#### 2.5.4. Teste com uma pessoa com regex inválido

```
> spot person ABC*
Caught exception: Invalid person id regex: 'ABC*'
```


#### 2.5.5. Testes com um carro já observado

```
> spot car AA00*
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855

> spot car AA*BB
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855

> spot car *00BB
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855

> spot car *A00B*
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855

> spot car *A*0*B*
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.5.6. Testes com múltiplos carros observados

```
> spot car AA*
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
car,AA11CC,<timestamp>,RNL,38.737857,-9.137855
car,AA12CC,<timestamp>,RNL,38.737857,-9.137855

> spot car AA*CC
car,AA11CC,<timestamp>,RNL,38.737857,-9.137855
car,AA12CC,<timestamp>,RNL,38.737857,-9.137855

> spot car *CC
car,AA11CC,<timestamp>,RNL,38.737857,-9.137855
car,AA12CC,<timestamp>,RNL,38.737857,-9.137855
car,DD00CC,<timestamp>,RNL,38.737857,-9.137855

> spot car *00*
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
car,DD00CC,<timestamp>,RNL,38.737857,-9.137855

> spot car *A*1*C*
car,AA11CC,<timestamp>,RNL,38.737857,-9.137855
car,AA12CC,<timestamp>,RNL,38.737857,-9.137855

> spot car *
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
car,AA11CC,<timestamp>,RNL,38.737857,-9.137855
car,AA12CC,<timestamp>,RNL,38.737857,-9.137855
car,DD00CC,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.5.7. Teste com um carro não observado

```
> spot car ZZ*
Car(s) not found
```

#### 2.5.8. Teste com um carro com regex inválido

```
> spot car XXYYWWZZ*
Caught exception: Invalid car plate regex: 'XXYYWWZZ*'
```
### 2.6. *trace*

Esta operação vai ser testada utilizando o comando *trail* com um identificador.

#### 2.6.1. Teste com uma pessoa já observada

```
> trail person 112233
person,112233,<timestamp>,RNL,38.737857,-9.137855
person,112233,<timestamp>,RNL,38.737857,-9.137855
person,112233,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.6.2. Teste com uma pessoa não observada (devolve vazio)

```
> trail person 14388236

```

#### 2.6.3. Teste com uma pessoa com id inválido

```
> trail person 0
Caught exception: Invalid person id: '0'
```

#### 2.6.4. Teste com um carro já observado

```
> trail car AA00BB
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
car,AA00BB,<timestamp>,RNL,38.737857,-9.137855
```

#### 2.6.5. Teste com um carro não observado (devolve vazio)

```
> trail car FF11FF

```

#### 2.6.6. Teste com um carro com matrícula inválida

```
> trail car AA
Caught exception: Invalid car plate: 'AA'
```

## 3. Testes com Múltiplas Réplicas

### 3.1. Teste com *eye* e *spotter* ligados a réplicas diferentes

#### 3.1.1. *Silo*

Para correr o teste, é necessário ter duas réplicas a correr.
Abrindo em dois terminais diferentes na diretoria do *silo-server*, basta executar:

```
$ mvn exec:java -Dinstance=1 -Dreplicas=2 -Dgossip=10
$ mvn exec:java -Dinstance=2 -Dreplicas=2 -Dgossip=10
```

#### 3.1.2. *Eye*

Num novo terminal, é necessário correr um *eye* que se conecta à réplica 1 executando o seguinte comando na diretoria *eye*:

```
$ mvn exec:java -Dexec.args="localhost 2181 1 2 20 RNL 38.737857 -9.137855"
```

#### 3.1.3. *Spotter*

Num novo terminal, é necessário correr um *spotter* que se conecta à réplica 2 executando o seguinte comando na diretoria *spotter*:

```
$ mvn exec:java -Dexec.args="localhost 2181 2 2 20"
```

#### 3.1.4. Execução

1. No terminal *eye*, escrever o seguinte comando e carregar no botão *Enter* duas vezes para enviar a observação:

```
> person,12345
```

2. Esperar 10 segundos para que as réplicas comuniquem entre si (aparece no terminal da réplica 2 uma mensagem da receção da atualização):

```
INFO: Received 1 updates from replica 1 with timestamp '[2, 0]': [CamReport{name='RNL', info={ts=[2, 0], prev=[1, 0], replica=1}}]
```

3. No terminal *spotter*:

```
> spot person 12345
person,12345,<timestamp>,RNL,38.737857,-9.137855
```

### 3.2. Teste com *eye* e duas réplicas onde uma falha

#### 3.2.1. *Silo* 1

Para iniciar o teste, é necessário uma réplica. Num passo futuro será ligada outra. Num terminal na diretoria do *silo-server*, executa-se:

```
$ mvn exec:java -Dinstance=1 -Dreplicas=2 -Dgossip=10
```

#### 3.2.2. *Eye*

Num novo terminal, é necessário correr um *eye* executando o seguinte comando na diretoria *eye*. O cliente conecta-se com a réplica 1 pois é a única disponível:

```
$ mvn exec:java -Dreplicas=2 -Dname=RNL -Dlat=38.737857 -Dlon=-9.137855
```

#### 3.2.3. *Silo* 2

Num terminal na diretoria do *silo-server*, executa-se:

```
$ mvn exec:java -Dinstance=2 -Dreplicas=2 -Dgossip=10
```

#### 3.2.4. Execução

1. No terminal *eye*, escrever o seguinte comando e carregar no botão *Enter* duas vezes para enviar a observação:

```
> person,12345
```

2. No terminal *Silo* 1, aparece a confirmação do report:

```
INFO: Received report from 'RNL'
```

3. Esperar 10 segundos para que as réplicas comuniquem entre si (aparece no terminal *Silo* 2 uma mensagem informando que a atualização foi recebida):

```
INFO: Received 1 updates from replica 1 with timestamp '[2, 0]': [CamReport{name='RNL', info={ts=[2, 0], prev=[1, 0], replica=1}}]
```

4. No terminal *Silo* 1, terminar o servidor com o seguinte comando (ctrl+C):

```
> ^C
```

5. No terminal *eye*, escrever o seguinte comando e carregar no botão *Enter* duas vezes para enviar a observação:

```
> person,12345
```

6. No terminal *eye*, verificar que este enviou o *report* para o *Silo* 2:

```
INFO: Unable to send to 'localhost:8081'
INFO: Connecting to server '/grpc/sauron/silo/2' at 'localhost:8082'
INFO: Connection successful
Sent observations
```

7. No terminal *Silo* 2, verificar que este recebeu o *report* do *eye*:

```
INFO: Received report from 'RNL'
```

### 3.3. Teste com *spotter* e duas réplicas onde uma falha para verificação da cache

#### 3.3.1. *Silo* 1

Para iniciar o teste, é necessário uma réplica. Num passo futuro será ligada outra. Num terminal na diretoria do *silo-server*, executa-se:

```
$ mvn exec:java -Dinstance=1 -Dreplicas=2 -Dgossip=1000
```

#### 3.3.2. *Eye*

Num novo terminal, é necessário correr um *eye* executando o seguinte comando na diretoria *eye*. O cliente conecta-se com a réplica 1 pois é a única disponível:

```
$ mvn exec:java -Dreplicas=2 -Dname=RNL -Dlat=38.737857 -Dlon=-9.137855
```

#### 3.3.3. *Spotter*

Num novo terminal, é necessário correr um *spotter* executando o seguinte comando na diretoria *spotter*. O cliente conecta-se com a réplica 1 pois é a única disponível:

```
$ mvn exec:java -Dreplicas=2
```

#### 3.3.4. *Silo* 2

Num terminal na diretoria do *silo-server*, executa-se:

```
$ mvn exec:java -Dinstance=2 -Dreplicas=2 -Dgossip=1000
```

#### 3.3.5. Execução

1. No terminal *eye*, escrever o seguinte comando e carregar no botão *Enter* duas vezes para enviar a observação:

```
> person,12345
```

2. No terminal *Silo* 1, aparece a confirmação do report:

```
INFO: Received report from 'RNL'
```

3. No terminal *spotter*:

```
> spot person 12345
person,12345,<timestamp>,RNL,38.737857,-9.137855
```

4. No terminal *Silo* 1, terminar o servidor com o seguinte comando (ctrl+C):

```
> ^C
```

5. No terminal *spotter*, voltar a executar o passo 3. A ligação entre o *spotter* e o *Silo* 1 foi fechada pelo que este conecta-se ao *Silo* 2:

```
INFO: Unable to send to 'localhost:8081'
INFO: Connecting to server '/grpc/sauron/silo/2' at 'localhost:8082'
INFO: Connection successful
person,12345,<timestamp>,RNL,38.737857,-9.137855
```

6. No terminal *spotter*, executar o seguinte comando (devolve vazio). Verifica-se que a réplica 2 não tem a observação da pessoa 12345 e a resposta anterior deve-se à cache:

```
trail person 12345

```
