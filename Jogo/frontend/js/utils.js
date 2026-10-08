/** Converte segundos em "MM:SS". */
export function formatarTempo(segundos) {
  const minutos = String(Math.floor(segundos / 60)).padStart(2, "0");
  const resto = String(segundos % 60).padStart(2, "0");
  return minutos + ":" + resto;
}
