# Automação Avançada - Avaliação 2
# Reconciliação de Dados e Escalonamento em Tempo Real

### Autor

Marcos Carvalho Ferreira

Matrícula: 202010203

Email: marcos.ferreira8@estudante.ufla.br

## Introdução
Este projeto visa abordar desafios relacionados à logística e mobilidade, focando em otimizar custos e prazos no deslocamento de veículos. Utilizando dados da Revista Quatro Rodas sobre a relação entre velocidade e consumo, a proposta é implementar soluções de Reconciliação de Dados (RD) para determinar a melhor velocidade em diferentes pontos de passagem de veículos, considerando influências como trânsito e condições da via.

Tomando como base a aplicação da AV1 que consistia em simular um ambiente de mobilidade urbana com várias entidades interagindo entre si: 
![image](https://github.com/MarquinhoCF/Avaliacao_2-Automacao_Avancada/blob/main/assets/Ilustracao_AV1.png)

## Preparação do Ambiente
1. **Requisitos:**
    - [SUMO](https://www.eclipse.org/sumo/)
    - [Java](https://www.java.com/)
    - [VSCode](https://code.visualstudio.com/)
    - [Maven](https://maven.apache.org/)

2. **Configurações:**
    - Todas as classes devem ser Threads.
    - Utilização da notação JSON para estruturar as mensagens trocadas.
    - Inclusão de pelo menos 3 testes unitários JUnit na aplicação.

## Desenvolvimento (Parte I - Reconciliação de Dados)
1. **Seleção de Veículo e Rota:**
    - Escolher ao menos 1 veículo e 1 rota para a solução.
    - Possibilidade de repetir o processo para múltiplos veículos e rotas.

2. **Implementação:**
    - Dividir a trajetória em pontos de passagem.
    - Realizar a simulação da rota pelo menos 100 vezes, coletando dados.
    - Apresentar graficamente a distribuição dos dados.
    - Calcular média, desvio padrão, polarização (bias), precisão e incerteza.

3. **Geração de Relatórios em Excel:**
    - Registrar dados consolidados para tempos de chegada e velocidades recomendadas.

## Desenvolvimento (Parte II - Escalonamento em Tempo Real)
1. **Definição de Tarefas:**
    - Selecionar pelo menos 10 tarefas (T1, T2, ..., TN) com regras de prioridade e dependência.
    - Descrever interferências entre tarefas quando aplicável.

2. **Grafo de Atividades:**
    - Propor e apresentar um grafo de atividades.

3. **Tabela de Tarefas:**
    - Preencher a tabela com informações de cada tarefa (Ji, Ci, Pi, Di).

4. **Escalonamento e Verificação:**
    - Implementar e testar o sistema de escalonamento.
    - Avaliar se as tarefas permanecem escalonáveis.

5. **Variação do Número de CPUs:**
    - Testar com todos os processadores e depois com um, dois e três.
    - Utilizar o Monitor de Recursos do Windows para verificar o uso das CPUs.

6. **Resultados e Comentários:**
    - Comentar sobre os resultados obtidos em diferentes configurações de processadores.

### Instalação de Ferramentas Necessárias

Antes de começar, certifique-se de ter instalado as seguintes ferramentas:

1. **SUMO:** Você precisa instalar o SUMO, uma ferramenta de simulação de tráfego. Baixe e instale a versão mais recente do SUMO no [site oficial](https://sumo.dlr.de/docs/index.html).

2. **Maven:** O Maven é uma ferramenta de automação de compilação e gerenciamento de dependências para Java. Você pode baixar e instalar o Maven do [site oficial](https://maven.apache.org/).

3. **Visual Studio Code (VSCode):** Um ambiente de desenvolvimento de código aberto. Você pode baixar e instalar o VSCode do [site oficial](https://code.visualstudio.com/download).

### Configurando as Dependências do Projeto

Após instalar o SUMO, o Maven e o VSCode, você precisará configurar as dependências do projeto. Siga estas etapas para fazer isso:

1. **Abra um Terminal:** Abra um terminal no seu sistema operacional.

2. **Instale as Dependências com o Maven:** No terminal, navegue até o diretório do seu projeto e execute os seguintes comandos Maven para instalar as dependências listadas no arquivo `Pom.xml`. Por exemplo:

   ```bash
   mvn install:install-file -Dfile="SEUCAMINHO\vscode-workspace\sim\lib\libsumo-1.18.0.jar" -DgroupId="libsumo-1.18.0" -DartifactId="libsumo-1.18.0" -Dversion="libsumo-1.18.0" -Dpackaging="jar" -DgeneratePom=true
   ```

   Certifique-se de substituir `"SEUCAMINHO"` pelo caminho real no qual você baixou o projeto.

3. **Realize as Alterações Necessárias no Código Fonte:** No diretório do seu projeto, navegue até `vscode-workspace\sim\src\main\java\sim\traci4j` e faça as alterações necessárias no código fonte para se conectar corretamente ao SUMO. Certifique-se de seguir as orientações fornecidas na documentação, especialmente para a classe `Vehicle`.

   Importante: Ao atuar em um veículo, tenha cuidado, pois é possível alterar as rotas dos veículos. Se uma rota reprogramada for inviável, o veículo será "teletransportado" para fora da simulação antes de atingir a rota final.

   Tenha em mente que ao alterar a velocidade de um veículo, o SUMO assume que você está controlando o veículo e manterá a velocidade até que você comande outra alteração ou devolva o controle ao SUMO (definindo a velocidade como -1). Isso pode potencialmente causar acidentes, e o SUMO está preparado para lidar com situações como acidentes e congestionamentos que podem afetar as vias e criar atrasos no tráfego, assim como em situações reais.

## Conclusão
Resumo das principais conclusões, desafios enfrentados e possíveis melhorias para futuras implementações.
