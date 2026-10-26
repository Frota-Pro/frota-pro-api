SELECT to_char(date_trunc('month', data), 'YYYY-MM') AS mes,
       count(*) AS cargas,
       sum(entregas) AS entregas,
       sum(notas) AS notas
FROM cg
GROUP BY date_trunc('month', data)
ORDER BY date_trunc('month', data)
