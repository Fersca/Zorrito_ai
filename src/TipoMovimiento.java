/**
 * Enum que define los tipos de movimiento disponibles para los personajes.
 * Reemplaza las lambdas por valores constantes más fáciles de debuggear.
 */
public enum TipoMovimiento {
    NULO,       // Sin movimiento (fondo, jaula)
    REBOTE,     // Rebota en los bordes (pájaros)
    ARCO,       // Se mueve en curvas suaves
    ALEATORIO,  // Movimiento aleatorio con cambios de dirección
    CAZAR       // Persigue a otro personaje (águilas)
}
