package model;

/**
 * Action icons printed on the columns and on the upgrades.
 */
enum Icon {
    HORSE,              // 1 movement
    HAND,               // collect 1 tribute
    KHAN,               // move the Khan (mandatory)
    RECOVER,            // take back the activation tokens (white column)
    VIRTUAL_COIN,       // 1 coin usable only this turn
    VIRTUAL_SWORD,
    VIRTUAL_YURT,
    VIRTUAL_TREASURE
}
