SELECT round(sum(litros), 0) AS litros,
       sum(valor) AS gasto,
       round(sum(valor) / NULLIF(sum(litros) FILTER (WHERE NOT anomalo), 0), 2) AS preco_medio
FROM ab
