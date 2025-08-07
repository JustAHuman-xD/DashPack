Deadline: 08/18/2025

Stuff I've realized I should PR into PK from working on this project:
- Non-combo ability whose cooldown is displayed in the same way a combo is
- Make `CoreAbility#getAbility(Class)` use type variable
- When registering plugin abilities (`CoreAbility#registerPluginAbilities`), if its an addon ability, load it like one