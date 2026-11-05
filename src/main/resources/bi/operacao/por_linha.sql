SELECT linha,
       count(*) AS cargas,
       sum(entregas) AS entregas,
       COALESCE(sum(km), 0) AS km,
       sum(litros) AS litros,
       round(sum(km) / NULLIF(sum(litros), 0), 2) AS km_por_litro,
       COALESCE(sum(peso_t), 0) AS toneladas
FROM cg
GROUP BY linha
ORDER BY count(*) DESC
