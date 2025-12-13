# TravisHeads

Plugin de heads customizáveis para Spigot 1.8.x - 1.21.x com sistema de raridades e trocas.

## 📋 Comandos

| Comando | Permissão | Descrição |
|---------|-----------|-----------|
| `/heads` | `travisheads.use` | Abre o menu principal |
| `/heads reload` | `travisheads.admin` | Recarrega as configurações |

## 🎯 Placeholders

Requer [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

```yaml
| `%travisheads_heads%` | Total de heads do jogador 
| `%travisheads_total%` | Mesma coisa que `heads`
| `%travisheads_comum%` | Quantidade de heads comuns
| `%travisheads_raro%` | Quantidade de heads raras
| `%travisheads_epico%` | Quantidade de heads épicas
| `%travisheads_lendario%` | Quantidade de heads lendárias
| `%travisheads_rarity_<id>%` | Heads de uma raridade específica | `%styleheads_rarity_comum%` |
```

## ⚙️ Configuração Básica

### config.yml
```yaml
settings:
  # Chance de dropar head ao matar jogador (%)
  drop-chance: 50.0
  
  # INVENTORY = adiciona no inventário do killer
  # DROP = dropa no chão
  # BOTH = ambos
  drop-mode: "INVENTORY"

# Aparência da head dropada
head:
  display-name: "&f%player% &8[%rarity_color%%rarity%&8]"
  lore:
    - "&7Raridade: %rarity_color%%rarity%"
    - "&7Obtido em: &f%date%"
```

### rarities.yml
```yaml
rarities:
  comum:
    displayName: "Comum"
    chance: 50.0  # 50% de chance
    color: "&f"
    
  raro:
    displayName: "Raro"
    chance: 30.0  # 30% de chance
    color: "&9"
    
  epico:
    displayName: "Épico"
    chance: 15.0  # 15% de chance
    color: "&5"
    
  lendario:
    displayName: "Lendário"
    chance: 5.0   # 5% de chance
    color: "&6"
```

### trocas.yml
```yaml
exchanges:
  troca_1:
    slot: 10
    
    # Item que aparece no menu
    icon:
      type: "SKULL"
      custom-skull: true
      skull-url: "eyJ0ZXh0dXJlcyI6..."
      display-name: "&aTrocar 10 Comuns"
      lore:
        - "&7Troque 10 heads comuns por:"
        - "&e  • 1000 coins"
        - "&e  • 1 chave misteriosa"
    
    # O que o jogador precisa ter
    requirements:
      rarity: "comum"
      amount: 10
    
    # O que o jogador recebe
    rewards:
      commands:
        - "eco give %player% 1000"
        - "crate give %player% mistica 1"
      items:
        item_1:
          material: "DIAMOND"
          amount: 5
          display-name: "&bDiamante Especial"
          lore:
            - "&7Recompensa da troca!"
```

### gui.yml - Menu Principal
```yaml
menus:
  main:
    title: "&8Menu Principal"
    size: 27
    
    items:
      heads:
        slot: 11
        type: "SKULL"
        skull-owner: "%player%"
        display-name: "&eMinhas Heads"
        lore:
          - "&7Total: &f%total_heads%"
          - ""
          - "&aClique para ver!"
        action: "OPEN_HEADS"
        
      exchanges:
        slot: 15
        type: "ITEM"
        material: "EMERALD"
        display-name: "&aTrocas"
        lore:
          - "&7Troque suas heads por"
          - "&7recompensas incríveis!"
          - ""
          - "&aClique para abrir!"
        action: "OPEN_EXCHANGES"
```

### gui.yml - Menu de Heads
```yaml
  heads:
    title: "&8Suas Heads"
    size: 54
    heads-start-slot: 10
    
    # Configuração por raridade
    comum:
      slot: 10
      type: "SKULL"
      custom-skull: true
      skull-url: "eyJ0ZXh0dXJlcyI6..."
      display-name: "%rarity_color%%rarity_name%"
      lore:
        - "&7Quantidade: &f%count%"
        
    raro:
      slot: 11
      type: "SKULL"
      custom-skull: true
      skull-url: "eyJ0ZXh0dXJlcyI6..."
      display-name: "%rarity_color%%rarity_name%"
      lore:
        - "&7Quantidade: &f%count%"
    
    # Botão de voltar
    back-button:
      slot: 49
      type: "ITEM"
      material: "ARROW"
      display-name: "&cVoltar"
```

## 📦 Dependências

- **PlaceholderAPI** (opcional, para placeholders)

## 🔧 Instalação

1. Baixe o plugin
2. Coloque na pasta `plugins/`
3. Reinicie o servidor
4. Configure em `plugins/TravisHeads/`
5. Use `/heads reload` após editar configs

## 📝 Mensagens

Todas as mensagens são configuráveis em `mensagens/messages.yml`:

```yaml
messages:
  prefix: "&8[&6TravisHeads&8] &7"
  
  only-players: "&cApenas jogadores podem usar este comando!"
  no-permission: "&cVocê não tem permissão para isso!"
  reload-success: "&aPlugin recarregado com sucesso!"
  
  head-obtained: "&aVocê obteve a head de &f%player% &8[%rarity_color%%rarity%&8]&a!"
  
  not-enough-heads: "&cVocê precisa de &f%amount%x %rarity% &cpara fazer esta troca! (Você tem: &f%current%&c)"
  exchange-success: "&aTroca realizada! Você trocou &f%amount%x %rarity%&a!"
  exchange-error: "&cErro ao processar troca!"
  
  gui-error: "&cErro ao abrir menu!"
```

## 🤝 Suporte

Encontrou algum bug? Tem alguma sugestão?
https://discord.gg/hRSavcGPZ2
