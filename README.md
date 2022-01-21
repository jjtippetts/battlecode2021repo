# Battlecode 2021 Influencer Player

## Strategy

## Overview:
1. Send scouts
2. Attack Enemy EC -> Win by destroying enemy robots
3. Slanderer Wall -> Win by generating a lot of influence to win more votes

### Enlightenment Center:
* Send scouts in every direction
    * Report back to EC if found enemy EC or edge
    * If found enemy EC, send politicians to attack
    * If found edge, notify slanderers the edge

### Politicians (built with lower ec):
* Defend slanderer edge from enemy muckrakers

### Slandereres (built with higher ec):
* Start by wandering randomly to find edge
* Hide at edge/wall
* Attack enemy HC after turning into Politicians

### Muckrakers:
* Scout
* Kill slanderers


