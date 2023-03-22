# Snake-AI

### 1.4

> On remarque une différence entre les résultats en mode train et en mode test.

> Les résultats sont croissants au fil du temps.

> Mode train a des résultats plus faibles à cause de l'aléatoire qui est plus fort qu'en mode test.

Compute score in test mode
Test - agent 0 - strategy strategy.TabularQLearning_solo@70177ecd average global score : 2.51
Play and collect examples - train mode
Train - agent 0 - strategy strategy.TabularQLearning_solo@70177ecd average global score : 4.22

Compute score in test mode
Test - agent 0 - strategy strategy.TabularQLearning_solo@70177ecd average global score : 6.98
Play and collect examples - train mode
Train - agent 0 - strategy strategy.TabularQLearning_solo@70177ecd average global score : 5.85

### 1.5

> Cet algorithme est plus efficace avec les plateaux plus petits.
> Des états ne sont surement jamais rencontré pendant l'entrainement expliquant ce problème.

### 2.1

> La distance entre la tête du snake et la pomme