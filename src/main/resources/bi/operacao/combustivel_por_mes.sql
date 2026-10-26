SELECT to_char(date_trunc('month', data), 'YYYY-MM') AS mes,
       round(sum(litros), 0) AS litros,
       sum(valor) AS gasto,
       round(sum(valor) / NULLIF(sum(litros) FILTER (WHERE NOT anomalo), 0), 2) AS preco_medio
FROM ab
GROUP BY date_trunc('month', data)
ORDER BY date_trunc('month', data)
